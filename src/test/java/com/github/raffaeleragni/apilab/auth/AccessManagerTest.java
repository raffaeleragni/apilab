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

import static com.github.raffaeleragni.apilab.auth.JavalinJWTFilter.REQ_ATTR_ROLES;
import static com.github.raffaeleragni.apilab.auth.Roles.ADMIN;
import static com.github.raffaeleragni.apilab.auth.Roles.USER;
import io.javalin.http.Handler;
import io.javalin.http.util.ContextUtil;
import static java.util.Collections.emptySet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 
 * @author Raffaele Ragni
 */
public class AccessManagerTest {

  @Test 
  public void testNoRoleRequired() throws Exception {
    var manager = new JavalinJWTAccessManager();
    var handler = mock(Handler.class);
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var ctx = ContextUtil.init(request, response);

    manager.manage(handler, ctx, emptySet());

    verify(handler).handle(ctx);
  }

  @Test 
  public void testAccessManager() throws Exception {
    var manager = new JavalinJWTAccessManager();
    var handler = mock(Handler.class);
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var ctx = ContextUtil.init(request, response);

    when(request.getAttribute(REQ_ATTR_ROLES))
      .thenReturn(Set.of(ADMIN));

    manager.manage(handler, ctx, Set.of(ADMIN));

    verify(handler).handle(ctx);
  }

  @Test 
  public void testNotAccess() throws Exception {
    var manager = new JavalinJWTAccessManager();
    var handler = mock(Handler.class);
    var request = mock(HttpServletRequest.class);
    var response = mock(HttpServletResponse.class);
    var ctx = ContextUtil.init(request, response);

    when(request.getAttribute(REQ_ATTR_ROLES))
      .thenReturn(Set.of(USER));

    manager.manage(handler, ctx, Set.of(ADMIN));

    verify(handler, times(0)).handle(ctx);
  }
}
