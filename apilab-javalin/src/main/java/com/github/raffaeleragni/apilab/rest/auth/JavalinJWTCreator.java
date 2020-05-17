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
package com.github.raffaeleragni.apilab.rest.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.javalin.core.security.Role;
import static java.lang.String.format;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Creates a new JTW token.
 *
 * A secret is required for this class to work, because JTW needs to be signed.
 * Can add extra claims.
 *
 * This is specifically made to work along with the other JWT filters and managers.
 *
 * @author Raffaele Ragni
 */
public class JavalinJWTCreator {

  private final Algorithm secret;
  private final String rolesProperty;

  /**
   * New instance for the maker
   * @param configuration Configuration including the secrets for signing.
   */
  public JavalinJWTCreator(Configuration configuration) {
    this.secret = configuration.jwtSecret().orElseThrow(() -> new IllegalArgumentException(
      format("secret is required for %s", JavalinJWTCreator.class.getName())
    ));
    this.rolesProperty = configuration.jwtRolesProperty().orElse("roles");
  }

  /**
   * Creates a new JTW token.
   * @param roles the roles to give to the token
   * @return the JWT token as string
   */
  public String create(Set<Role> roles) {
    return create(roles, Collections::emptyMap);
  }

  /**
   * Creates a new JTW token.
   * @param roles the roles to give to the token
   * @param extraClaims extra claims that may be added, supplier of map k/v
   * @return the JWT token as string
   */
  public String create(Set<Role> roles, Supplier<Map<String, String>> extraClaims) {
    var jwtBuilder = JWT.create();

    jwtBuilder.withArrayClaim(rolesProperty,
      roles.stream().map(Object::toString).toArray(String[]::new));

    var extra = extraClaims.get();
    for (Map.Entry<String, String> entry: extra.entrySet()) {
      jwtBuilder.withClaim(entry.getKey(), entry.getValue());
    }

    return jwtBuilder.sign(secret);
  }

}
