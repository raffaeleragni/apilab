package com.github.raffaeleragni.apilab.queues;

import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import java.io.IOException;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.concurrent.TimeoutException;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Raffaele Ragni
 */
public interface QueueListener extends DeliverCallback {
 
  void setDeregisterCallback(Optional<Runnable> callback);
  Optional<Runnable> getDeregisterCallback();
  String getQueueName();
  void receive(Delivery message);
  
  @Override
  default void handle(String consumerTag, Delivery message) throws IOException {
    try {
      receive(message);
    } catch (RuntimeException ex) {
      // Must swallow all exceptions or the queue consumer will die otherwise.
      LoggerFactory.getLogger(this.getClass()).error(ex.getMessage(), ex);
    }
  }
  
  /**
   * Registers the queue listeners, and puts this class on listen.
   * The callback will be forwarded so this one needs to be called just ONCE.
   * @param rabbitFactory the connection factory
   */
  default void registerQueueListener(ConnectionFactory rabbitFactory) {
    try {
      var connection = rabbitFactory.newConnection();
      var channel = connection.createChannel();
      channel.queueDeclare(getQueueName(), true, false, false, null);
      var tag = channel.basicConsume(getQueueName(), true, this, t -> {});
      setDeregisterCallback(Optional.of(() -> {
        try { channel.basicCancel(tag); } catch (IOException ex) { LoggerFactory.getLogger(QueueListener.class).warn(ex.getMessage(), ex); }
        try { channel.close(); } catch (IOException | TimeoutException ex) { LoggerFactory.getLogger(QueueListener.class).warn(ex.getMessage(), ex); }
        try { connection.close(); } catch (IOException ex) { LoggerFactory.getLogger(QueueListener.class).warn(ex.getMessage(), ex); }
      }));
    } catch (IOException | TimeoutException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }

  /**
   * Unregister the consumer(s).
   * @param rabbitFactory the connection factory
   */
  default void unregisterQueueListener(ConnectionFactory rabbitFactory) {
    getDeregisterCallback().ifPresent(callback -> {
      callback.run();
      setDeregisterCallback(empty());
    });
  }

  default void withinChannel(ConnectionFactory rabbitFactory, ChannelConsumer fn) {
    try (var connection = rabbitFactory.newConnection()){
      try (var channel = connection.createChannel()) {
        fn.channelAccept(channel);
      }
    } catch (IOException | TimeoutException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }
  
}
