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
package com.github.raffaeleragni.apilab.rabbitmq;

import static com.github.raffaeleragni.apilab.core.Env.Vars.API_ENABLE_CONSUMERS;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_QUIT_AFTER_MIGRATION;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Raffaele Ragni
 */
public class ApplicationTest {
  @Test
  public void testComponent() {
    System.setProperty(API_ENABLE_CONSUMERS.name(), "false");
    var instance = DaggerApplicationComponent.create().instance();
    instance.start();
    instance.stop();

    System.setProperty(API_ENABLE_CONSUMERS.name(), "true");
    instance = DaggerApplicationComponent.create().instance();
    instance.start();
    instance.stop();

    System.setProperty(API_QUIT_AFTER_MIGRATION.name(), "true");
    instance.start();
    instance.stop();
  }
}
