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

import com.auth0.jwt.algorithms.Algorithm;
import io.javalin.core.security.Role;
import java.util.Optional;
import java.util.function.Function;
import org.immutables.value.Value;

/**
 * Configuration for the filter.
 * 
 * - roleMapper: required, the function that maps your 'Roles' enum to the Javalin Role interface.
 *               Most of the times a Roles::valueOf is enough.
 * - jwtSecret: it's optional if you have secret validation in this satellite API.
 * - jwtRolesProperty: optionally specify a different property for the roles array inside the JWT.
 *
 * @author Raffaele Ragni raffaele.ragni@gmail.com
 */
@Value.Immutable
public interface Configuration {
  Function<String, Role> roleMapper();
  Optional<Algorithm> jwtSecret();
  Optional<String> jwtRolesProperty();
}
