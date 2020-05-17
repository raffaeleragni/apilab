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
package com.github.raffaeleragni.apilab.core;

import static com.github.raffaeleragni.apilab.core.Env.Vars.API_JWT_SECRET;
import static com.github.raffaeleragni.apilab.core.Env.Vars.JAVALIN_HTTP2_PORT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Raffaele Ragni
 */
public class EnvTest {
  @Test
  public void testEnv() {
    System.setProperty(JAVALIN_HTTP2_PORT.name(), "9090");
    var env = new Env();
    assertThat("Env read from properties", env.get(JAVALIN_HTTP2_PORT), is("9090"));
    assertThat("Env read from properties", env.get(API_JWT_SECRET), is(nullValue()));
  }
}
