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
package com.github.raffaeleragni.apilab.rest;

import com.github.raffaeleragni.apilab.core.ApplicationService;
import com.github.raffaeleragni.apilab.core.Env;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_QUIT_AFTER_MIGRATION;
import io.javalin.Javalin;
import java.util.Optional;
import javax.inject.Inject;

/**
 *
 * @author Raffaele Ragni
 */
public class RESTService implements ApplicationService {

  @Inject
  public RESTService() {
    ////
  }

  @Inject Javalin javalin;
  @Inject Env env;

  @Override
  public void start() {
    if (ignore()) {
      return;
    }
    javalin.start();
    JettyHttp2Creator.startMetrics(env);
  }

  @Override
  public void stop() {
    if (ignore()) {
      return;
    }
    javalin.stop();
    JettyHttp2Creator.stopMetrics();
  }

  private boolean ignore() {
    return Optional.ofNullable(env.get(API_QUIT_AFTER_MIGRATION))
       .map(Boolean::valueOf)
       .orElse(false);
  }

}
