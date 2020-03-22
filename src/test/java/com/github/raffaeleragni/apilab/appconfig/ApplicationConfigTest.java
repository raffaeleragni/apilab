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

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 
 * @author Raffaele Ragni
 */
public class ApplicationConfigTest {
  
  @Test
  public void testDisabledMigrations() {
    var config = new ApplicationConfig();
    var env = mock(Env.class);
    when(env.get(Env.Vars.API_ENABLE_MIGRATION)).thenReturn("false");
    
    config.runMigrations(env, "", "", "");
    assertThat("nothing to assert for this case?", config, not(nullValue()));
  }
  
  @Test
  public void testRabbitFalse() throws Exception {
    var factory = mock(ConnectionFactory.class);
    var connection = mock(Connection.class);
    when(factory.newConnection()).thenReturn(connection);
    when(connection.isOpen()).thenThrow(RuntimeException.class);
    
    var result = ApplicationConfig.checkRabbit(factory);
    assertThat("failed health check", result, is(false));
  }
  
}
