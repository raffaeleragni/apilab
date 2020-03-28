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
import java.util.Optional;
import static java.util.Optional.empty;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Raffaele Ragni
 */
public class ApplicationScheduler {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationScheduler.class);
  
  Optional<ScheduledExecutorService> scheduler = empty();
  @Inject Set<Scheduled> scheduled;

  @Inject
  public ApplicationScheduler() {
    // Constructor injector
  }
  
  public void start() {
    // start is also a restart, so stop before in any case.
    stop();
    scheduler = Optional.of(Executors.newScheduledThreadPool(1));
    scheduler.ifPresent(s -> {
      scheduled.stream().forEach(task -> {
        s.scheduleAtFixedRate(() -> {
          try {
          task.run();
          } catch (RuntimeException ex) {
            LOG.warn(ex.getMessage(), ex);
          }
        }, 0, task.period(), MILLISECONDS);
      });
    });
  }
  
  public void stop() {
    scheduler.ifPresent(ScheduledExecutorService::shutdownNow);
    scheduler = empty();
  }
  
}
