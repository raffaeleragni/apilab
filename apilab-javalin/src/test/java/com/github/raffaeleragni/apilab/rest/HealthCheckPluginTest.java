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
import io.javalin.http.Context;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Raffaele Ragni
 */
public class HealthCheckPluginTest {

  @Test
  public void testHealthCheck() throws Exception {
    var ctx = mock(Context.class);
    var app = mock(Javalin.class);

    var plugin = new HealthCheckPlugin(Map.of("db", (Supplier<Boolean>) () -> true));
    plugin.apply(app);

    verify(app).get(eq("/status/version"), any());
    verify(app).get("/status/health", plugin);

    plugin.printVersion(ctx);
    verify(ctx).json(Map.of("version", "unknown"));

    plugin.handle(ctx);
    verify(ctx).json(Map.of("db", true));

  }

}
