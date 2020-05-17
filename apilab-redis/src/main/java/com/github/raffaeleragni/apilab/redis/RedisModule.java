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
package com.github.raffaeleragni.apilab.redis;

import com.github.raffaeleragni.apilab.core.Env;
import static com.github.raffaeleragni.apilab.core.Env.Vars.API_REDIS_URL;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import io.lettuce.core.RedisClient;
import static java.util.Optional.ofNullable;
import java.util.function.Supplier;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 *
 * @author Raffaele Ragni
 */
@dagger.Module
public class RedisModule {

  @Provides
  @Named("healthChecks")
  @IntoMap
  @StringKey("redis")
  public Supplier<Boolean> dbHealthCheck(RedisClient redis) {
    // A 'connection' of lettuce is one instance of a client, and
    // it can auto reconnect when it goes down, making effectively
    // the object a client handle, even if the method is called 'connect'
    // This avoids creating resources every time ending up in memory leaks otherwise.
    var redisHandle = redis.connect();
    return () -> redisHandle.sync().ping().equalsIgnoreCase("pong");
  }

  @Provides
  @Singleton
  public RedisClient redisClient(Env env) {
    var url = ofNullable(env.get(API_REDIS_URL)).orElse("redis://localhost");
    return RedisClient.create(url);
  }

}
