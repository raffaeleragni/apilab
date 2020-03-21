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
package com.github.raffaeleragni.apilab.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import static com.github.raffaeleragni.apilab.auth.Roles.USER;
import com.github.raffaeleragni.apilab.auth.Configuration;
import com.github.raffaeleragni.apilab.auth.ImmutableConfiguration;
import com.github.raffaeleragni.apilab.auth.JavalinJWTFilter;
import static com.github.raffaeleragni.apilab.auth.JavalinJWTFilter.REQ_ATTR_ROLES;
import static com.github.raffaeleragni.apilab.auth.JavalinJWTFilter.REQ_ATTR_SUBJECT;
import io.javalin.Javalin;
import io.javalin.http.util.ContextUtil;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 
 * @author Raffaele Ragni
 */
public class RolesFilterTest {

  private static final Configuration BASE_CONFIG = ImmutableConfiguration.builder()
      .roleMapper(Roles::valueOf)
      .build();
  private static final Configuration EXTENDED_CONFIG = ImmutableConfiguration.builder()
      .roleMapper(Roles::valueOf)
      .jwtSecret(Algorithm.HMAC256("secret"))
      .jwtRolesProperty("Roles")
      .build();

  @Test 
  public void testRegister() {
    var filter = new JavalinJWTFilter(BASE_CONFIG);
    var javalin = mock(Javalin.class);

    filter.apply(javalin);
    // Pretty important to check this or else authentication will be missed.
    verify(javalin).before(filter);
  }

  @Test
  public void testErrorCases() throws Exception {
    var filter = new JavalinJWTFilter(BASE_CONFIG);

    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    // Cannot mock context because of kotlin shenanigans, so mock req/resp separately and init here
    var ctx = ContextUtil.init(request, response);

    // ----------

    when(request.getHeader("Authorization"))
      .thenReturn(null);

    filter.handle(ctx);

    // Nothing must be put in the req attr
    verify(request, times(0)).setAttribute(any(), any());

    // ----------

    when(request.getHeader("Authorization"))
      .thenReturn("Beaver XXX");

    filter.handle(ctx);

    // Nothing must be put in the req attr
    verify(request, times(0)).setAttribute(any(), any());

    // ----------

    when(request.getHeader("Authorization"))
      .thenReturn("Bearer malformed");
  }

  @Test
  public void testOKWithoutSecretValidation() throws Exception {
    var filter = new JavalinJWTFilter(BASE_CONFIG);
    var alg = Algorithm.HMAC256("secret");
    var token = JWT.create()
      .withSubject("subjectXX")
      .withArrayClaim("roles", new String[]{"user"})
      .sign(alg);

    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var ctx = ContextUtil.init(request, response);

    when(request.getHeader("Authorization"))
      .thenReturn("Bearer "+token);

    filter.handle(ctx);

    verify(request).setAttribute(REQ_ATTR_SUBJECT, "subjectXX");
    verify(request).setAttribute(REQ_ATTR_ROLES, Set.of(USER));
  }

  @Test
  public void testOKWithSecretValidation() throws Exception {
    var alg = Algorithm.HMAC256("secret");
    var filter = new JavalinJWTFilter(EXTENDED_CONFIG);
    var token = JWT.create()
      .withSubject("subjectXX")
      .withArrayClaim("Roles", new String[]{"user"})
      .sign(alg);

    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var ctx = ContextUtil.init(request, response);

    when(request.getHeader("Authorization"))
      .thenReturn("Bearer "+token);

    filter.handle(ctx);

    verify(request).setAttribute(REQ_ATTR_SUBJECT, "subjectXX");
    verify(request).setAttribute(REQ_ATTR_ROLES, Set.of(USER));
  }

}
