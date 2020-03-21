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

import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_DATABASE_MAXPOOLSZE;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_DATABASE_PASSWORD;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_DATABASE_URL;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_DATABASE_USERNAME;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_ENDPOINTS;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_MIGRATION;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_JWT_SECRET;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_RABBITMQ_HOST;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_RABBITMQ_PASSWORD;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_RABBITMQ_PORT;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_RABBITMQ_USERNAME;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_REDIS_URL;
import com.github.raffaeleragni.apilab.auth.JavalinJWTAccessManager;
import com.github.raffaeleragni.apilab.auth.JavalinJWTFilter;
import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import com.github.raffaeleragni.apilab.http2.JettyHttp2Creator;
import com.github.raffaeleragni.apilab.metric.HealthCheckPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Provides;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.lettuce.core.RedisClient;
import io.swagger.v3.oas.models.info.Info;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.raffaeleragni.apilab.auth.ImmutableConfiguration;
import com.github.raffaeleragni.apilab.queues.QueueListener;
import dagger.multibindings.ElementsIntoSet;
import java.util.Set;

/**
 * 
 * @author Raffaele Ragni
 */
@dagger.Module
public class ApplicationConfig {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);
  
  private final ApplicationInitializer initializer;

  public ApplicationConfig() {
    this.initializer = ImmutableApplicationInitializer.builder().build();
  }
  
  public ApplicationConfig(ApplicationInitializer initializer) {
    this.initializer = initializer;
  }

  // Main web server

  @Provides @Singleton
  public Javalin javalin(
      Env env,
      ObjectMapper objectMapper,
      @Named("healthcheck") Supplier<Map<String, Boolean>> healthcheck) {

    JavalinJackson.configure(objectMapper);
    
    var javalin = Javalin.create(config -> {
      config.showJavalinBanner = false;
      config.server(() -> JettyHttp2Creator.createHttp2Server(env));
      config.accessManager(new JavalinJWTAccessManager());
      config.registerPlugin(new OpenApiPlugin(getOpenApiOptions()));
      config.registerPlugin(new JavalinJWTFilter(ImmutableConfiguration.builder()
        .roleMapper(initializer.roleMapper())
        .jwtSecret(Algorithm.HMAC256(
          Optional.ofNullable(
            env.get(API_JWT_SECRET)).orElse("")
        ))
        .build()));
      config.registerPlugin(new HealthCheckPlugin(healthcheck));
    });

    javalin.exception(ApplicationException.class, (ex, ctx) -> {
      ctx.status(ex.getHttpCode());
      ctx.json(ex.getMessage());
    });
    
    boolean enabledEndpoints = Optional.ofNullable(env.get(API_ENABLE_ENDPOINTS))
        .map(Boolean::valueOf)
        .orElse(true);
    if (enabledEndpoints) {
      LOG.info("## ENDPOINTS ENABLED");
      initializer.endpoints().stream().forEach(e -> {
        LOG.info("## ENDPOINTS Registering {}", e.getClass().getName());
        e.register(javalin);
      });
    }

    javalin.get("/", c -> c.redirect("/swagger"));

    return javalin;
  }
  
  public @Provides @ElementsIntoSet Set<QueueListener> consumers() {
    return initializer.consumers();
  }
  
  // Persistence
  
  @Provides @Singleton @Named("healthcheck")
  public Supplier<Map<String, Boolean>> healthcheck(
      Jdbi jdbi,
      RedisClient redis,
      com.rabbitmq.client.ConnectionFactory rabbitFactory) {
    // A 'connection' of lettuce is one instance of a client, and
    // it can auto reconnect when it goes down, making effectively
    // the object a client handle, even if the method is called 'connect'
    // This avoids creating resources every time ending up in memory leaks otherwise.
    var redisHandle = redis.connect();
    return () -> Map.of(
      "database", jdbi.withHandle(h -> h.select("select 1").mapTo(String.class).first().equals("1")),
      "redis", redisHandle.sync().ping().equalsIgnoreCase("pong"),
      "rabbitmq", checkRabbit(rabbitFactory)
    );
  }

  public static boolean checkRabbit(com.rabbitmq.client.ConnectionFactory rabbitFactory) {
    try (var connection = rabbitFactory.newConnection()){
      return connection.isOpen();
    } catch (IOException | TimeoutException | RuntimeException ex) {
      LOG.error(ex.getMessage(), ex);
      return false;
    }
  }

  @Provides @Singleton
  public com.rabbitmq.client.ConnectionFactory rabbitMQConnectionFactory(Env env) {

    var factory = new com.rabbitmq.client.ConnectionFactory();
    // "guest"/"guest" by default, limited to localhost connections
    factory.setUsername(ofNullable(env.get(API_RABBITMQ_USERNAME)).orElse("guest"));
    factory.setPassword(ofNullable(env.get(API_RABBITMQ_PASSWORD)).orElse("guest"));
    factory.setHost(ofNullable(env.get(API_RABBITMQ_HOST)).orElse("localhost"));
    factory.setPort(ofNullable(env.get(API_RABBITMQ_PORT)).map(Integer::parseInt).orElse(5672));

    return factory;
  }
  
  public @Provides @Singleton RedisClient redis(Env env) {
    var url = ofNullable(env.get(API_REDIS_URL)).orElse("redis://localhost");
    return RedisClient.create(url);
  }

  public @Provides @Singleton Jdbi jdbi(Env env) {
    var url = ofNullable(env.get(API_DATABASE_URL)).orElse("jdbc:postgresql://[::1]/postgres");
    var username = ofNullable(env.get(API_DATABASE_USERNAME)).orElse("postgres");
    var password = ofNullable(env.get(API_DATABASE_PASSWORD)).orElse("postgres");
    var maxConnections = ofNullable(env.get(API_DATABASE_MAXPOOLSZE)).map(Integer::valueOf).orElse(100);

    runMigrations(env, url, username, password);

    var hikConf = new HikariConfig();
    hikConf.setJdbcUrl(url);
    hikConf.setUsername(username);
    hikConf.setPassword(password);
    hikConf.setMaximumPoolSize(maxConnections);
    var hikDS = new HikariDataSource(hikConf);

    return Jdbi.create(hikDS);
  }
  
  public @Provides @Singleton ObjectMapper objectMapper() {
    // Providing a custom object mapper so it can handle special serialization needs
    ObjectMapper objectMapper = new ObjectMapper();

    JavaTimeModule module = new JavaTimeModule();
    objectMapper.registerModule(module);
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    return objectMapper;
  }

  public void runMigrations(Env env, String url, String username, String password) {
    
    boolean enableMigrations = Optional.ofNullable(env.get(API_ENABLE_MIGRATION))
       .map(Boolean::valueOf)
       .orElse(false);
    if (!enableMigrations) {
      return;
    }
    
    try {
      LOG.info("## MIGRATION ENABLED");
      new Liquibase("db/changelog.xml", new ClassLoaderResourceAccessor(),
        DatabaseFactory.getInstance().openDatabase(
          url, username, password, null, new FileSystemResourceAccessor()))
        .update(new Contexts(), new LabelExpression());
    } catch (LiquibaseException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }

  // API Docs

  public OpenApiOptions getOpenApiOptions() {
    Info applicationInfo = new Info()
        .version("1.0")
        .description("API");
    return new OpenApiOptions(applicationInfo).path("/swagger-docs")
      .ignorePath("/")
      .ignorePath("/status/*")
      .swagger(new SwaggerOptions("/swagger").title("API"));
  }

}
