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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;
import dagger.Component;
import io.javalin.Javalin;
import io.lettuce.core.RedisClient;
import javax.inject.Singleton;
import org.jdbi.v3.core.Jdbi;

/**
 * 
 * @author Raffaele Ragni
 */
@Component(modules = {ApplicationConfig.class})
@Singleton
public interface ApplicationComponent {
  Application application();
  
  Javalin javalin();
  ObjectMapper objectMapper();
  Jdbi jdbi();
  ConnectionFactory rabbit();
  RedisClient redis();
}
