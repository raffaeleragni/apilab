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
package com.github.raffaeleragni.apilab.rest;

import com.github.raffaeleragni.apilab.core.ApplicationModule;
import com.github.raffaeleragni.apilab.core.Env;
import com.github.raffaeleragni.apilab.exceptions.UnprocessableEntityException;
import io.javalin.Javalin;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Raffaele Ragni
 */
public class RESTModuleTest {

  @Test
  public void testJavalin() throws IOException {
    var config = new RESTModule();
    var env = mock(Env.class);
    when(env.get(Env.Vars.API_ENABLE_ENDPOINTS)).thenReturn("false");

    config.javalin(env,
      ImmutableRESTInitializer.builder().build(),
      new ApplicationModule().objectMapper(),
      Set.of(new GetEndpointSample()),
      Map.of("test", (Supplier<Boolean>) () -> true));

    when(env.get(Env.Vars.API_ENABLE_ENDPOINTS)).thenReturn("true");

    Javalin javalin = config.javalin(env,
      ImmutableRESTInitializer.builder().build(),
      new ApplicationModule().objectMapper(),
      Set.of(new GetEndpointSample()),
      Map.of("test", (Supplier<Boolean>) () -> true));

    javalin.get("/testbad", c -> {throw new UnprocessableEntityException("");});

    javalin.start();

    OkHttpClient client = new OkHttpClient();


    Request request = new Request.Builder()
      .url("http://localhost:8080/swagger-doc")
      .build();
    client.newCall(request).execute();


    request = new Request.Builder()
      .url("http://localhost:8080/testbad")
      .build();
    Response response = client.newCall(request).execute();

    javalin.stop();

    assertThat("Response code is 422", response.code(), is(422));
  }

  @Test
  public void testInitializerDefaultValue() {
    var initializer = ImmutableRESTInitializer.builder().build();
    var fn = initializer.roleMapper();
    assertThat("Mapping string straight away", fn.apply("test").toString(), is("test"));
  }

}
