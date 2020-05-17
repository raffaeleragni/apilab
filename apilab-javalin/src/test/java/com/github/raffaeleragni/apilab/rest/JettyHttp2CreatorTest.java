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

import com.github.raffaeleragni.apilab.core.Env;
import java.io.IOException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Raffaele Ragni
 */
public class JettyHttp2CreatorTest {

  @Test
  public void testServer() throws IOException {
    var server = JettyHttp2Creator.createHttp2Server(new Env());
    assertThat("Server built", server, is(not(nullValue())));
  }

  @Test
  public void testMetrics() {

    JettyHttp2Creator.stopMetrics();

    JettyHttp2Creator.startMetrics(new Env());
    JettyHttp2Creator.stopMetrics();

    assertThat("metrics server created", JettyHttp2Creator.metricServer, is(nullValue()));

    JettyHttp2Creator.startMetrics(new Env());
    JettyHttp2Creator.startMetrics(new Env());
    JettyHttp2Creator.stopMetrics();

    assertThat("metrics server created", JettyHttp2Creator.metricServer, is(nullValue()));
  }

}
