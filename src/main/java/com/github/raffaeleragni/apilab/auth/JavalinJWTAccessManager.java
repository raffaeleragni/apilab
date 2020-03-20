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
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import static java.lang.String.format;
import java.util.Collections;
import static java.util.Optional.ofNullable;
import java.util.Set;
import static java.util.stream.Collectors.joining;

/**
 * Works along the JavalinJWTRolesFilter to check roles.
 * @author Raffaele Ragni raffaele.ragni@gmail.com
 */
public class JavalinJWTAccessManager implements AccessManager {

  @Override
  public void manage(Handler handler, Context ctx, Set<Role> permittedRoles) throws Exception {

    if (ofNullable(permittedRoles).map(Set::isEmpty).orElse(true)) {
      // No roles required for this path, no need to check.
      handler.handle(ctx);
      return;
    }

    // Roles are required at this point, even if it's an empty list
    // it means the JWT token was there.
    Set<Role> roles = ctx.attribute(REQ_ATTR_ROLES);
    // No roles at all, no auth.
    // No roles are in common, means no auth
    if (ofNullable(roles).map(r -> Collections.disjoint(r, permittedRoles)).orElse(true)) {
      ctx.status(403)
        .result(format(
          "Not allowed: missing roles. Required roles for this path: %s",
          permittedRoles.stream()
            .map(Object::toString)
            .collect(joining("] [", "[", "]")
          )));
      return;
    }

    handler.handle(ctx);

  }

}
