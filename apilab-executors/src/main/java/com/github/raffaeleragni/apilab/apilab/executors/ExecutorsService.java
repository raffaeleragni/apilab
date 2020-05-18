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
package com.github.raffaeleragni.apilab.apilab.executors;

import com.github.raffaeleragni.apilab.core.ApplicationService;
import com.github.raffaeleragni.apilab.core.Env;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_ENABLE_SCHEDULED;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_QUIT_AFTER_MIGRATION;
import java.util.Optional;
import javax.inject.Inject;

/**
 *
 * @author Raffaele Ragni
 */
public class ExecutorsService implements ApplicationService {

  @Inject Env env;
  @Inject Executor applicationScheduler;

  @Inject
  public ExecutorsService() {
    ////
  }

  @Override
  public void start() {
    if (!ignore() && enableExecutors()) {
      applicationScheduler.start();
    }
  }

  @Override
  public void stop() {
    if (!ignore() && enableExecutors()) {
      applicationScheduler.stop();
    }
  }

  public boolean enableExecutors() {
    return Optional.ofNullable(env.get(API_ENABLE_SCHEDULED))
      .map(Boolean::valueOf)
      .orElse(false);
  }

  private boolean ignore() {
    return Optional.ofNullable(env.get(API_QUIT_AFTER_MIGRATION))
       .map(Boolean::valueOf)
       .orElse(false);
  }

}
