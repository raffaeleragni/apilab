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
package com.github.raffaeleragni.apilab.jdbi;

import com.github.raffaeleragni.apilab.core.Env;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_DATABASE_MAXPOOLSZE;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_DATABASE_PASSWORD;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_DATABASE_URL;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_DATABASE_USERNAME;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_ENABLE_MIGRATION;
import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Raffaele Ragni
 */
public class JdbiModuleTest {

  @Test
  public void testModule() {
    var env = mock(Env.class);
    when(env.get(API_DATABASE_URL)).thenReturn("jdbc:h2:mem:test");
    when(env.get(API_DATABASE_USERNAME)).thenReturn("sa");
    when(env.get(API_DATABASE_PASSWORD)).thenReturn("");
    when(env.get(API_DATABASE_MAXPOOLSZE)).thenReturn("1");
    when(env.get(API_ENABLE_MIGRATION)).thenReturn("false");
    new JdbiModule().jdbi(env, emptySet());

    when(env.get(API_ENABLE_MIGRATION)).thenReturn("true");
    var jdbi = new JdbiModule().jdbi(env, emptySet());

    assertThrows(ApplicationException.class, () -> {
      JdbiModule.runMigrations(
        "wrong path does not exist!",
        "jdbc:h2:mem:test2", "sa", "");
    });

    var result = new JdbiModule().dbHealthCheck(jdbi).get();
    assertThat("Healthcheck is ok", result, is(true));
  }

}
