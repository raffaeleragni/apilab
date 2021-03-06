/*
 * Copyright 2020 Raffaele Ragni.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.raffaeleragni.apilab.queues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles asynchronous processing using APMQ queues.
 * Implement the receive() method, then use the given send() to push the asynchronous process.
 * @author Raffaele Ragni
 */
public abstract class QueueService<T> {

  private static final Logger LOG = LoggerFactory.getLogger(QueueService.class);

  ConnectionFactory rabbitFactory;
  Optional<Runnable> deregisterCallback;
  ObjectMapper mapper;
  String queueName;
  Class<T> clazz;
  QueueServiceOptions options;

  public QueueService(
    ConnectionFactory rabbitFactory,
    ObjectMapper mapper,
    String queueName,
    Class<T> clazz) {

    this(
      rabbitFactory,
      mapper,
      queueName,
      clazz,
      ImmutableQueueServiceOptions.builder().build());
  }

  public QueueService(
    ConnectionFactory rabbitFactory,
    ObjectMapper mapper,
    String queueName,
    Class<T> clazz,
    QueueServiceOptions options) {

    this.rabbitFactory = Objects.requireNonNull(rabbitFactory);
    this.mapper = Objects.requireNonNull(mapper);
    this.queueName = Objects.requireNonNull(queueName);
    this.clazz = Objects.requireNonNull(clazz);
    this.options = options;
  }

  public void send(T message) {
    withinChannel(rabbitFactory, ch -> {
      try {
        queueDeclare(ch);
        ch.basicPublish("", queueName, null, mapper.writeValueAsBytes(message));
        LOG.debug("Sent messsage: {}", message);
      } catch (IOException ex) {
        throw new ApplicationException(ex.getMessage(), ex);
      }
    });
  }

  public abstract void receive(T message);

  /**
   * Registers the queue listeners, and puts this class on listen.
   * The callback will be forwarded so this one needs to be called just ONCE.
   */
  public void registerQueueListener() {
    try {
      var connection = rabbitFactory.newConnection();
      var channel = connection.createChannel();
      queueDeclare(channel);
      var tag = channel.basicConsume(queueName, false, (t, d) -> {
        try {
          receive(mapper.readValue(d.getBody(), clazz));
          channel.basicAck(d.getEnvelope().getDeliveryTag(), false);
        } catch (IOException | RuntimeException ex) {
          // Must swallow all exceptions or the queue consumer will die otherwise.
          // Give an explicit NACK so the message ends up in the dead letter queue
          // and it is not requeued. Retry behavior will kick in in the DLQ later.
          try {
            channel.basicNack(d.getEnvelope().getDeliveryTag(), false, false);
          } catch (RuntimeException ex2) {
            LoggerFactory.getLogger(this.getClass()).error(ex2.getMessage(), ex2);
          }
          LoggerFactory.getLogger(this.getClass()).error(ex.getMessage(), ex);
        }
      }, t -> {});
      deregisterCallback= Optional.of(() -> {
        try { channel.basicCancel(tag); } catch (IOException ex) { LoggerFactory.getLogger(QueueService.class).warn(ex.getMessage(), ex); }
        try { channel.close(); } catch (IOException | TimeoutException ex) { LoggerFactory.getLogger(QueueService.class).warn(ex.getMessage(), ex); }
        try { connection.close(); } catch (IOException ex) { LoggerFactory.getLogger(QueueService.class).warn(ex.getMessage(), ex); }
      });
    } catch (IOException | TimeoutException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }

  /**
   * Unregister the consumer(s).
   */
  public void unregisterQueueListener() {
    deregisterCallback.ifPresent(callback -> {
      callback.run();
      deregisterCallback = empty();
    });
  }

  private void queueDeclare(Channel ch) throws IOException {
    // the dead letter queue is declared first
    ch.queueDeclare(queueName+"_dlq", options.durable(), options.exclusive(), options.autoDelete(), null);
    // the actual queue is declared with routing erorr message to the dead letter queue
    ch.queueDeclare(queueName, options.durable(), options.exclusive(), options.autoDelete(), Map.of(
      "x-dead-letter-exchange", "",
      "x-dead-letter-routing-key", queueName+"_dlq"
    ));
  }

  private static void withinChannel(ConnectionFactory rabbitFactory, Consumer<Channel> fn) {
    try (var connection = rabbitFactory.newConnection()){
      try (var channel = connection.createChannel()) {
        fn.accept(channel);
      }
    } catch (IOException | TimeoutException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }

}
