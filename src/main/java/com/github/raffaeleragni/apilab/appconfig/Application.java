package com.github.raffaeleragni.apilab.appconfig;

import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_CONSUMERS;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_MIGRATION;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_QUIT_AFTER_MIGRATION;
import com.github.raffaeleragni.apilab.http2.JettyHttp2Creator;
import com.github.raffaeleragni.apilab.queues.QueueListener;
import com.rabbitmq.client.ConnectionFactory;
import io.javalin.Javalin;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import static net.logstash.logback.argument.StructuredArguments.kv;
import org.slf4j.LoggerFactory;

public class Application {
  private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(Application.class);

  @Inject Javalin javalin;
  @Inject Env env;
  @Inject ConnectionFactory rabbitConnectionFactory;
  @Inject Set<QueueListener> listeners;
  
  @Inject
  public Application() {
    // Injection constructor
  }

  public void start() throws IOException {

    boolean enableMigrations = Optional.ofNullable(env.get(API_ENABLE_MIGRATION))
        .map(Boolean::valueOf)
        .orElse(false);
    boolean quitAfterMigrations = Optional.ofNullable(env.get(API_QUIT_AFTER_MIGRATION))
      .map(Boolean::valueOf)
      .orElse(false);
    boolean enableConsumers = Optional.ofNullable(env.get(API_ENABLE_CONSUMERS))
      .map(Boolean::valueOf)
      .orElse(false);

    if (enableMigrations && quitAfterMigrations) {
      return;
    }
    
    if (enableConsumers) {
      listeners.stream().forEach(l -> l.registerQueueListener(rabbitConnectionFactory));
    }
    
    javalin.start();
    JettyHttp2Creator.startMetrics();

    long vmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
    long currentTime = System.currentTimeMillis();
    LOG.info("Startup IN: {}ms", kv("startuptime", (currentTime - vmStartTime)));
  }

  public void stop() {
    boolean enableConsumers = Optional.ofNullable(env.get(API_ENABLE_CONSUMERS))
      .map(Boolean::valueOf)
      .orElse(false);
    if (enableConsumers) {
      listeners.stream().forEach(l -> l.unregisterQueueListener(rabbitConnectionFactory));
    }
    javalin.stop();
    JettyHttp2Creator.stopMetrics();
  }


}
