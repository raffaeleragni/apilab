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
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_SCHEDULED;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_QUIT_AFTER_MIGRATION;
import com.github.raffaeleragni.apilab.http2.JettyHttp2Creator;
import com.github.raffaeleragni.apilab.queues.QueueService;
import com.rabbitmq.client.ConnectionFactory;
import io.javalin.Javalin;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Raffaele Ragni
 */
public class Application {
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Application.class);

  @Inject Javalin javalin;
  @Inject Env env;
  @Inject ConnectionFactory rabbitConnectionFactory;
  @Inject Set<Endpoint> endpoints;
  @Inject Set<QueueService> consumers;
  @Inject ApplicationScheduler appClock;
  
  @Inject
  public Application() {
    // Injection constructor
  }

  public void start() throws IOException {

    java.security.Security.setProperty("networkaddress.cache.ttl" , "60");
    
    boolean enabledEndpoints = Optional.ofNullable(env.get(API_ENABLE_ENDPOINTS))
        .map(Boolean::valueOf)
        .orElse(true);
    boolean enableMigrations = Optional.ofNullable(env.get(API_ENABLE_MIGRATION))
        .map(Boolean::valueOf)
        .orElse(false);
    boolean quitAfterMigrations = Optional.ofNullable(env.get(API_QUIT_AFTER_MIGRATION))
      .map(Boolean::valueOf)
      .orElse(false);
    boolean enableConsumers = Optional.ofNullable(env.get(API_ENABLE_CONSUMERS))
      .map(Boolean::valueOf)
      .orElse(false);
    boolean enableClock = Optional.ofNullable(env.get(API_ENABLE_SCHEDULED))
      .map(Boolean::valueOf)
      .orElse(false);

    if (enableMigrations && quitAfterMigrations) {
      return;
    }
    
    if (enabledEndpoints) {
      LOG.info("## ENDPOINTS ENABLED");
      endpoints.stream().forEach(e -> {
        LOG.info("## ENDPOINTS Registering {}", e.getClass().getName());
        e.register(javalin);
      });
    }
    
    if (enableConsumers) {
      LOG.info("## CONSUMERS ENABLED");
      consumers.stream().forEach(l -> {
        LOG.info("## CONSUMERS Registering {}", l.getClass().getName());
        l.registerQueueListener();
      });
    }
    
    if (enableClock) {
      appClock.start();
    }
    
    javalin.start();
    JettyHttp2Creator.startMetrics(env);

    long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
    long currentTime = System.currentTimeMillis();
    LOG.info("Startup IN: {}ms", kv("startuptime", (currentTime - vmStartTime)));
  }

  public void stop() {
    boolean enableConsumers = Optional.ofNullable(env.get(API_ENABLE_CONSUMERS))
      .map(Boolean::valueOf)
      .orElse(false);
    boolean enableClock = Optional.ofNullable(env.get(API_ENABLE_SCHEDULED))
      .map(Boolean::valueOf)
      .orElse(false);
    
    if (enableConsumers) {
      consumers.stream().forEach(QueueService::unregisterQueueListener);
    }
    
    if (enableClock) {
      appClock.stop();
    }
    
    javalin.stop();
    JettyHttp2Creator.stopMetrics();
  }


}
