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
package com.github.raffaeleragni.apilab.appconfig;

import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_CONSUMERS;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_ENDPOINTS;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_MIGRATION;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_QUIT_AFTER_MIGRATION;
import com.github.raffaeleragni.apilab.queues.QueueService;
import com.rabbitmq.client.ConnectionFactory;
import io.javalin.Javalin;
import java.io.IOException;
import static java.util.Collections.emptySet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 
 * @author Raffaele Ragni
 */
public class ApplicationTest {
  
  @Test
  public void testEnableDisable() throws IOException {
    var app = new Application();
    var endpoint = mock(Endpoint.class);
    var consumer = mock(QueueService.class);
    app.javalin = mock(Javalin.class);
    app.env = mock(Env.class);
    app.rabbitConnectionFactory = mock(ConnectionFactory.class);
    app.endpoints = Set.of(endpoint);
    app.consumers = Set.of(consumer);
    
    // Test endpoints
    
    when(app.env.get(API_ENABLE_ENDPOINTS)).thenReturn("false");
    app.start();
    verify(endpoint, times(0)).register(any());
    
    when(app.env.get(API_ENABLE_ENDPOINTS)).thenReturn("true");
    app.start();
    verify(endpoint).register(app.javalin);
    
    // Test consumers
    
    when(app.env.get(API_ENABLE_CONSUMERS)).thenReturn("false");
    app.start();
    app.stop();
    verify(consumer, times(0)).registerQueueListener();
    
    when(app.env.get(API_ENABLE_CONSUMERS)).thenReturn("true");
    app.start();
    app.stop();
    verify(consumer).registerQueueListener();
  }
  
  @Test
  public void testMigrations() throws IOException {
    var app = new Application();
    app.javalin = mock(Javalin.class);
    app.env = mock(Env.class);
    app.endpoints = emptySet();
    app.consumers = emptySet();
    
    // Test the migrations
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("true");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("true");
    app.start();
    
    verify(app.javalin, times(0)).start();
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("false");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("true");
    app.start();
    
    verify(app.javalin, times(1)).start();
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("true");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("false");
    app.start();
    
    verify(app.javalin, times(2)).start();
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("false");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("false");
    app.start();
    
    verify(app.javalin, times(3)).start();
    
  }
  
}
