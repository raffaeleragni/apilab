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
import io.javalin.Javalin;
import io.javalin.core.plugin.Plugin;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import static java.util.Collections.emptyList;
import java.util.Locale;
import java.util.Optional;
import static java.util.Optional.ofNullable;
import java.util.function.Function;
import static java.util.stream.Collectors.toSet;

/**
 * A filter that extracts data from JWT and puts it into the request attributes,
 * but with optional secret to also verify JWT token or throw JWTVerificationException.
 *
 * The reason why the secret is optional is because in some situations an API could be behind a
 * gateway that already verifies the token and we don't want to put the secret into the API itself.
 *
 * @author Raffaele Ragni raffaele.ragni@gmail.com
 */
public class JavalinJWTFilter implements Plugin, Handler {

  public static final String REQ_ATTR_SUBJECT = "JWT.SUBJECT";
  public static final String REQ_ATTR_ROLES = "JWT.ROLES";

  private final Optional<Algorithm> secret;
  private final String rolesProperty;
  private final Function<String, Role> toRole;

  /**
   * Creates a new filter with default configuration.
   *
   * @param configuration The configuration for this filter
   */
  public JavalinJWTFilter(Configuration configuration) {
    // Optional.of ensures it will be present and cannot be null
    this.toRole = Optional.of(configuration.roleMapper()).get();
    this.secret = configuration.jwtSecret();
    this.rolesProperty = configuration.jwtRolesProperty().orElse("roles");
  }

  @Override
  public void apply(Javalin app) {
    app.before(this);
  }

  @Override
  public void handle(Context ctx) throws Exception {

    var token = ofNullable(ctx.header("Authorization"))
      .filter(header -> header.trim().toLowerCase(Locale.ROOT).startsWith("bearer"))
      .map(header -> header.substring("bearer".length()).trim())
      .orElse(null);

    if (token == null) {
      // no token to put in context.
      return;
    }

    // Validation via secret only if secret is present
    secret.ifPresent(alg ->
      // Throws JWTVerificationException
      JWT.require(alg).build().verify(token)
    );

    var decoded = JWT.decode(token);

    // Extract the subject.
    // Subject is always treated as a string,
    // and the property is not customizable as it is part of the JWT specification.
    // If null then it's null.
    ctx.attribute(REQ_ATTR_SUBJECT, decoded.getSubject());

    // Extract the roles
    // Non roles or invalid roles means empty list -> means no roles.
    var strRoles = Optional.ofNullable(
        decoded
          .getClaims()
          .get(rolesProperty))
        .map(c -> c.asList(String.class))
        .orElse(emptyList());

    var typedRoles = strRoles.stream()
      .map(String::toUpperCase)
      .map(toRole)
      .collect(toSet());

    ctx.attribute(REQ_ATTR_ROLES, typedRoles);

  }

}
