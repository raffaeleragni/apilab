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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
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

    var mapper = mock(ObjectMapper.class);
    var rabbitFactory = mock(ConnectionFactory.class);
    var rabbitConnection = mock(Connection.class);
    var rabbitChannel = mock(Channel.class);
    var message = mock(Delivery.class);
    when(message.getEnvelope()).thenReturn(mock(Envelope.class));
    
    when(rabbitFactory.newConnection()).thenReturn(rabbitConnection);
    when(rabbitConnection.createChannel()).thenReturn(rabbitChannel);
    
    var listenerExceptional = new MyListenerExceptional(rabbitFactory, mapper);
    
    var listener = new MyListener(rabbitFactory, mapper);
    
    when(rabbitChannel.basicConsume(any(), anyBoolean(), any(DeliverCallback.class), any(CancelCallback.class)))
      .thenAnswer(invok -> {
        invok.getArgument(2, DeliverCallback.class).handle("tag", message);
        return "tag";
      });
    
    listener.send("message");
    listener.receive("message");
    
    listener.registerQueueListener();
    
    listener.unregisterQueueListener();
    
    listenerExceptional.registerQueueListener();
    
    doThrow(IOException.class).when(rabbitChannel)
      .basicAck(anyLong(), anyBoolean());
    listener.registerQueueListener();
    
    doThrow(IOException.class).when(rabbitChannel)
      .queueDeclare(any(), anyBoolean(), anyBoolean(), anyBoolean(), any());
    assertThrows(ApplicationException.class, () ->{
      listener.send("message");
    });
    
    doThrow(IOException.class).when(rabbitConnection).createChannel();
    assertThrows(ApplicationException.class, () ->{
      listener.send("message");
    });
    
    doThrow(IOException.class).when(rabbitFactory).newConnection();
    assertThrows(ApplicationException.class, () ->{
      listener.send("message");
    });
    
    assertThrows(ApplicationException.class, () ->{
      listener.registerQueueListener();
    });
    assertThrows(ApplicationException.class, () ->{
      listener.send("message");
    });
    assertThrows(ApplicationException.class, () ->{
      listenerExceptional.registerQueueListener();
    });
    
    
    
  }
  
  static class MyListener extends QueueService<String> {

    public MyListener(ConnectionFactory rabbitFactory, ObjectMapper mapper) {
      super(rabbitFactory, mapper, "my-queue-example-test", String.class);
    }

    @Override
    public void receive(String message) {
    }
    
  }
  
  static class MyListenerExceptional extends QueueService<String> {

    public MyListenerExceptional(ConnectionFactory rabbitFactory, ObjectMapper mapper) {
      super(rabbitFactory, mapper, "my-exceptional-queue-example-test", String.class);
    }

    @Override
    public void receive(String message) {
      throw new IllegalStateException();
    }
  }
}
