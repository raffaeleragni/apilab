/*
 * Copyright 2020 Raffaele Ragni.
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

import com.github.raffaeleragni.apilab.appconfig.components.GetEndpointSample;
import com.github.raffaeleragni.apilab.appconfig.components.MyEventServiceSample;
import com.github.raffaeleragni.apilab.appconfig.components.MyScheduledSample;
import com.github.raffaeleragni.apilab.auth.Roles;
import com.github.raffaeleragni.apilab.queues.QueueService;
import com.github.raffaeleragni.apilab.scheduled.Scheduled;
import dagger.Provides;
import dagger.multibindings.IntoSet;

/**
 *
 * @author Raffaele Ragni
 */
@dagger.Module
public class TestComponentProvider {

  @Provides
  public ApplicationInitializer initializer() {
    return ImmutableApplicationInitializer.builder().roleMapper(Roles::valueOf).build();
  }
  
  @Provides @IntoSet
  public Endpoint endpoint(GetEndpointSample o) {
    return o;
  }
  
  @Provides @IntoSet
  public QueueService consumers(MyEventServiceSample o) {
    return o;
  }
  
  @Provides @IntoSet
  public Scheduled scheduled(MyScheduledSample o) {
    return o;
  }
}
