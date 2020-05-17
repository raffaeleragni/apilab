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
package com.github.raffaeleragni.apilab.rabbitmq;

import com.github.raffaeleragni.apilab.core.Env;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Raffaele Ragni
 */
public class RabbitMQModuleTest {
  @Test
  public void testModule() throws IOException, TimeoutException {
    var env = new Env();
    var module = new RabbitMQModule();

    module.rabbitMQConnectionFactory(env);

    var factory = mock(ConnectionFactory.class);
    var connection = mock(Connection.class);
    when(factory.newConnection()).thenReturn(connection);
    when(connection.isOpen()).thenReturn(true);

    var value =  module.dbHealthCheck(factory).get();
    assertThat("Health check is true", value, is(true));

    when(connection.isOpen()).thenReturn(false);
    value =  module.dbHealthCheck(factory).get();
    assertThat("Health check is false", value, is(false));

    when(factory.newConnection()).thenThrow(new IOException());
    value =  module.dbHealthCheck(factory).get();
    assertThat("Health check is false", value, is(false));
  }
}
