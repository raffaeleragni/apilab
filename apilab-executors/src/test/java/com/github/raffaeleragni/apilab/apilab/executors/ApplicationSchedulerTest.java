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
package com.github.raffaeleragni.apilab.apilab.executors;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Raffaele Ragni
 */
public class ApplicationSchedulerTest {
  @Test
  public void testScheduler() throws InterruptedException {
    var scheduler = new Executor();

    scheduler.scheduled = Set.of(new MyScheduled(), new MyScheduledExceptional());

    scheduler.stop();

    scheduler.start();
    scheduler.stop();

    scheduler.stop();

    scheduler.start();
    scheduler.start();

    TimeUnit.MILLISECONDS.sleep(100L);

    scheduler.stop();

  }
}

class MyScheduled implements Scheduled {

  @Override
  public void run() {
    System.out.println("test");
  }

  @Override
  public long period() {
    return 10;
  }

}

class MyScheduledExceptional implements Scheduled {

  @Override
  public void run() {
    throw new IllegalStateException();
  }

  @Override
  public long period() {
    return 10;
  }

}