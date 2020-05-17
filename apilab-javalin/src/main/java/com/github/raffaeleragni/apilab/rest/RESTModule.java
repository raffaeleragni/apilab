/*
 * Copyright 2020 r.ragni.
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
package com.github.raffaeleragni.apilab.rest;

import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raffaeleragni.apilab.core.ApplicationService;
import com.github.raffaeleragni.apilab.core.Env;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_ENABLE_ENDPOINTS;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_JWT_SECRET;
import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import com.github.raffaeleragni.apilab.rest.auth.ImmutableConfiguration;
import com.github.raffaeleragni.apilab.rest.auth.JavalinJWTAccessManager;
import com.github.raffaeleragni.apilab.rest.auth.JavalinJWTFilter;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.javalin.Javalin;
import io.javalin.plugin.json.JavalinJackson;
import io.javalin.plugin.openapi.InitialConfigurationCreator;
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 *
 * @author Raffaele Ragni
 */
@dagger.Module
public class RESTModule {

  private static final String HEADER_REQUEST_UUID = "X-APP-Request-UUID";
  // This property name will appear as is in log key/value
  private static final String MDC_REQUEST_UUID = "request_uuid";

  private static final Logger LOG = LoggerFactory.getLogger(RESTModule.class);

  @Provides @IntoSet ApplicationService service(RESTService service) {
    return service;
  }

  @Provides @Singleton
  public Javalin javalin(
      Env env,
      RESTInitializer initializer,
      ObjectMapper objectMapper,
      Set<Endpoint> endpoints,
      @Named("healthChecks") Map<String, Supplier<Boolean>> healthChecks) {

    JavalinJackson.configure(objectMapper);

    var javalin = Javalin.create(config -> {
      config.showJavalinBanner = false;
      config.server(() -> JettyHttp2Creator.createHttp2Server(env));
      config.accessManager(new JavalinJWTAccessManager());
      config.registerPlugin(new OpenApiPlugin(getOpenApiOptions(objectMapper)));
      config.registerPlugin(new JavalinJWTFilter(ImmutableConfiguration.builder()
        .roleMapper(initializer.roleMapper())
        .jwtSecret(Algorithm.HMAC256(
          Optional.ofNullable(
            env.get(API_JWT_SECRET)).orElse("")
        ))
        .build()));
      config.registerPlugin(new HealthCheckPlugin(healthChecks));
    });

    javalin.exception(ApplicationException.class, (ex, ctx) -> {
      ctx.status(ex.getHttpCode());
      ctx.json(ex.getMessage());
    });

    javalin.get("/", c -> c.redirect("/swagger"));

    boolean enabledEndpoints = Optional.ofNullable(env.get(API_ENABLE_ENDPOINTS))
        .map(Boolean::valueOf)
        .orElse(true);

    if (enabledEndpoints) {
      LOG.info("## ENDPOINTS ENABLED");
      endpoints.stream().forEach(e -> {
        LOG.info("## ENDPOINTS Registering {}", e.getClass().getName());
        e.register(javalin);
      });
    }

    // Add to the logging context the key/value of request uuid
    // And also respond in the response header with the request uuid so the users
    // can send them for throubleshooting and finding the requests logs via uuid.
    javalin.before(ctx -> MDC.put(MDC_REQUEST_UUID, UUID.randomUUID().toString()));
    javalin.after(ctx -> {
      String uuid = MDC.get(MDC_REQUEST_UUID);
      ctx.header(HEADER_REQUEST_UUID, uuid);
      MDC.remove(MDC_REQUEST_UUID);
    });
    return javalin;
  }

  // API Docs

  public OpenApiOptions getOpenApiOptions(ObjectMapper mapper) {

    SecurityScheme securityScheme = new SecurityScheme();
    securityScheme.setType(SecurityScheme.Type.HTTP);
    securityScheme.setScheme("bearer");
    securityScheme.setBearerFormat("JWT");

    InitialConfigurationCreator initialConfigurationCreator = () -> new OpenAPI().info(new Info().version("1.0").description("API").title("API")).schemaRequirement("BearerAuth", securityScheme);

    return new OpenApiOptions(initialConfigurationCreator)
      .jacksonMapper(mapper)
      .path("/swagger-docs")
      .ignorePath("/")
      .ignorePath("/status/*")
      .swagger(new SwaggerOptions("/swagger").title("API"));
  }
}
