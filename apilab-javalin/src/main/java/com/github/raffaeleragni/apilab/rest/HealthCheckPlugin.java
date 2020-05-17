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

import io.javalin.Javalin;
import io.javalin.core.plugin.Plugin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import static java.util.Optional.ofNullable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Plugin that adds health check from information coming from the configured lambda on constructor.
 * Format is a map, key is the check name, value is the result (true or false).
 * @author Raffaele Ragni
 */
public class HealthCheckPlugin implements Plugin, Handler {

  private final String version;
  private final Map<String, Supplier<Boolean>> checkLambda;

  public HealthCheckPlugin(Map<String, Supplier<Boolean>> checkLambda) {
    this.checkLambda = Objects.requireNonNull(checkLambda);
    this.version = ofNullable(System.getProperty("appVersion"))
      .orElseGet(()
        -> ofNullable(System.getenv("APP_VERSION"))
        .orElse("unknown")
      );
  }

  @Override
  public void apply(Javalin app) {
    app.get("/status/version", this::printVersion);
    app.get("/status/health", this);
  }

  public void printVersion(Context ctx) {
    ctx.json(Map.of("version", version));
  }

  @Override
  public void handle(Context ctx) throws Exception {
    var results = checkLambda.entrySet().stream()
      .collect(Collectors.toMap(
        Entry::getKey,
        e -> e.getValue().get()
      ));
    ctx.json(results);
  }

}
