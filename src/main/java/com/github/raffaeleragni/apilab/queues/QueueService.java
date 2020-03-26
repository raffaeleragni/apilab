package com.github.raffaeleragni.apilab.queues;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Raffaele Ragni
 */
public abstract class QueueService<T> {

  private static final Logger LOG = LoggerFactory.getLogger(QueueService.class);
  
  ConnectionFactory rabbitFactory;
  Optional<Runnable> deregisterCallback;
  ObjectMapper mapper;
  String queueName;
  Class<T> clazz;

  public QueueService(
    ConnectionFactory rabbitFactory,
    ObjectMapper mapper,
    String queueName,
    Class<T> clazz) {
    
    this.rabbitFactory = Objects.requireNonNull(rabbitFactory);
    this.mapper = Objects.requireNonNull(mapper);
    this.queueName = Objects.requireNonNull(queueName);
    this.clazz = Objects.requireNonNull(clazz);
  }
  
  public void send(T message) {
    withinChannel(rabbitFactory, ch -> {
      try {
        ch.queueDeclare(queueName, true, false, false, null);
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
      channel.queueDeclare(queueName, true, false, false, null);
      var tag = channel.basicConsume(queueName, false, (t, d) -> {
        try {
          receive(mapper.readValue(d.getBody(), clazz));
          withinChannel(rabbitFactory, c -> {
            try {
              c.basicAck(d.getEnvelope().getDeliveryTag(), false);
            } catch (IOException ex) {
              LoggerFactory.getLogger(this.getClass()).error(ex.getMessage(), ex);
            }
          });
        } catch (RuntimeException ex) {
          // Must swallow all exceptions or the queue consumer will die otherwise.
          // Still the ack was not done so the message gets retried.
          // TODO implement a max retry?
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