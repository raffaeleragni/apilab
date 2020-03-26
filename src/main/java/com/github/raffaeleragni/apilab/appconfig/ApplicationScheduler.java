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

import com.github.raffaeleragni.apilab.scheduled.Scheduled;
import it.sauronsoftware.cron4j.Scheduler;
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.Set;
import javax.inject.Inject;

/**
 * @author Raffaele Ragni
 */
public class ApplicationScheduler {

  Optional<Scheduler> scheduler = empty();
  @Inject Set<Scheduled> scheduled;

  @Inject
  public ApplicationScheduler() {
    // Constructor injector
  }
  
  public void start() {
    // start is also a restart, so stop before in any case.
    stop();
    scheduler = Optional.of(new Scheduler());
    scheduler.ifPresent(s -> {
      scheduled.stream().forEach(task -> s.schedule(task.cron(), task));
      s.start();
    });
  }
  
  public void stop() {
    scheduler.ifPresent(Scheduler::stop);
    scheduler = empty();
  }
  
}
