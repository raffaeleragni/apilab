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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_JWT_SECRET;
import com.github.raffaeleragni.apilab.auth.Roles;
import com.github.raffaeleragni.apilab.exceptions.NotFoundException;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import static java.util.Optional.ofNullable;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 * 
 * @author Raffaele Ragni
 */
public class EndpointsIT {

  static OkHttpClient client;
  static Application app;

  @BeforeAll
  public static void prepare() throws Exception {
    client = new OkHttpClient();
    System.setProperty(API_JWT_SECRET.name(), "test");
    app = DaggerApplicationComponent.builder()
      .build()
      .application();
    app.endpoints = Set.of(new Endpoint() {
        @Override
        public void register(Javalin javalin) {
          javalin.get("/notfound", this);
        }

        @Override
        public void handle(Context ctx) throws Exception {
          throw new NotFoundException("not found");
        }
      }, new Endpoint() {
        @Override
        public void register(Javalin javalin) {
          javalin.get("/secured", this, Set.of((Roles.ADMIN)));
        }

        @Override
        public void handle(Context ctx) throws Exception {
          ctx.result("secured");
        }
      });
    app.start();
  }

  @AfterAll
  public static void shutdown() {
    app.stop();
  }

  @Test
  public void testAll() throws IOException, JSONException {

    var result = get("/status/version", false);
    assertThat("Returned expected result", result, is("{\"version\":\"unknown\"}"));

    result = get("/status/health", false);
    JSONAssert.assertEquals("Returned expected result",
        "{\"redis\":true,"
        + "\"database\":true,"
        + "\"rabbitmq\":true}", 
        result,
        true);
    
    result = get("/notfound", false);
    assertThat("404 not found", result, is ("\"not found\""));
    
    result = get("/secured", true);
    assertThat("Token auth works", result, is ("secured"));
  }

  private String get(String url, boolean authenticated) throws IOException {
    var host = "localhost";
    var port = ofNullable(System.getenv("JAVALIN_HTTP2_PORT"))
      .map(Integer::valueOf)
      .orElse(8080);
    var rootURL = "http://"+host+":"+port;
    var alg = Algorithm.HMAC256("test");
    var token = JWT.create()
      .withSubject("admin")
      .withIssuer("own")
      .withExpiresAt(Date.from(Instant.now().plusSeconds(TimeUnit.DAYS.toDays(5))))
      .withArrayClaim("roles", new String[]{"admin"})
      .sign(alg);
    var requestBuilder = new Request.Builder()
      .url(rootURL+url);
    if (authenticated) {
      requestBuilder = requestBuilder.header("Authorization", "Bearer "+token);
    }
    var body = client.newCall(requestBuilder.get().build()).execute().body();
    if (body != null) {
      return body.string();
    } else {
      return "";
    }
  }

}
