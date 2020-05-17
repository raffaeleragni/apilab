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
package com.github.raffaeleragni.apilab.rabbitmq;

import com.github.raffaeleragni.apilab.core.ApplicationService;
import com.github.raffaeleragni.apilab.core.Env;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_RABBITMQ_HOST;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_RABBITMQ_PASSWORD;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_RABBITMQ_PORT;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_RABBITMQ_USERNAME;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;
import dagger.multibindings.StringKey;
import java.io.IOException;
import static java.util.Optional.ofNullable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Raffaele Ragni
 */
@dagger.Module
public class RabbitMQModule {
  private static final Logger LOG = LoggerFactory.getLogger(RabbitMQModule.class);

  @Provides
  @Singleton
  @IntoSet
  public ApplicationService service(RabbitMQService service) {
    return service;
  }

  @Provides
  @Named("healthChecks")
  @IntoMap
  @StringKey("rabbitmq")
  public Supplier<Boolean> dbHealthCheck(com.rabbitmq.client.ConnectionFactory rabbitFactory) {
    return () -> checkRabbit(rabbitFactory);
  }

  @Provides
  @Singleton
  public com.rabbitmq.client.ConnectionFactory rabbitMQConnectionFactory(Env env) {

    var factory = new com.rabbitmq.client.ConnectionFactory();
    // "guest"/"guest" by default, limited to localhost connections
    factory.setUsername(ofNullable(env.get(API_RABBITMQ_USERNAME)).orElse("guest"));
    factory.setPassword(ofNullable(env.get(API_RABBITMQ_PASSWORD)).orElse("guest"));
    factory.setHost(ofNullable(env.get(API_RABBITMQ_HOST)).orElse("localhost"));
    factory.setPort(ofNullable(env.get(API_RABBITMQ_PORT)).map(Integer::parseInt).orElse(5672));

    return factory;
  }

  public static boolean checkRabbit(com.rabbitmq.client.ConnectionFactory rabbitFactory) {
    try (var connection = rabbitFactory.newConnection()){
      return connection.isOpen();
    } catch (IOException | TimeoutException | RuntimeException ex) {
      LOG.error(ex.getMessage(), ex);
      return false;
    }
  }
}
