/*
 * Copyright 2019 Raffaele Ragni.
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

import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Delivery;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 
 * @author Raffaele Ragni
 */
public class QueueListenerTest {
   
  @Test
  public void testQueueing() throws IOException, TimeoutException {
    
    var listenerExceptional = new MyListenerExceptional();
    // This should throw no exception
    listenerExceptional.handle("tag", mock(Delivery.class));
    
    var listener = new MyListener();
    var rabbitFactory = mock(ConnectionFactory.class);
    var rabbitConnection = mock(Connection.class);
    var rabbitChannel = mock(Channel.class);
    
    when(rabbitFactory.newConnection()).thenReturn(rabbitConnection);
    when(rabbitConnection.createChannel()).thenReturn(rabbitChannel);
    
    listener.handle("tag", mock(Delivery.class));
    
    listener.withinChannel(rabbitFactory, ch -> {});
    
    listener.registerQueueListener(rabbitFactory);
    
    listener.unregisterQueueListener(rabbitFactory);
    
    doThrow(IOException.class).when(rabbitFactory).newConnection();
    
    assertThrows(ApplicationException.class, () ->{
      listener.registerQueueListener(rabbitFactory);
    });
    
    assertThrows(ApplicationException.class, () ->{
      listener.withinChannel(rabbitFactory, ch -> {});
    });
    
  }
  
  static class MyListener implements QueueListener {

    Optional<Runnable> callback;
    
    @Override
    public void setDeregisterCallback(Optional<Runnable> callback) {
      this.callback = callback;
    }

    @Override
    public Optional<Runnable> getDeregisterCallback() {
      return callback;
    }

    @Override
    public String getQueueName() {
      return "testqueue";
    }

    @Override
    public void receive(Delivery message) {
    }
  }
  
  static class MyListenerExceptional implements QueueListener {

    Optional<Runnable> callback;
    
    @Override
    public void setDeregisterCallback(Optional<Runnable> callback) {
      this.callback = callback;
    }

    @Override
    public Optional<Runnable> getDeregisterCallback() {
      return callback;
    }

    @Override
    public String getQueueName() {
      return "testqueue";
    }

    @Override
    public void receive(Delivery message) {
      throw new IllegalStateException();
    }
  }
}
