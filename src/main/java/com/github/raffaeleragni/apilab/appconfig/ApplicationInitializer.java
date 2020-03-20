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
package com.github.raffaeleragni.apilab.appconfig;

import com.github.raffaeleragni.apilab.queues.QueueListener;
import io.javalin.core.security.Role;
import java.util.Set;
import java.util.function.Function;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;

@Value.Immutable
public interface ApplicationInitializer {
  @Default default Function<String, Role> roleMapper() { return s -> new Role(){}; }
  Set<Endpoint> endpoints();
  Set<QueueListener> consumers();
}
