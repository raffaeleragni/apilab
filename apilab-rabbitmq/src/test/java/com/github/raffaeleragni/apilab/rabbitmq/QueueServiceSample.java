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
package com.github.raffaeleragni.apilab.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raffaeleragni.apilab.queues.QueueService;
import com.rabbitmq.client.ConnectionFactory;
import javax.inject.Inject;

/**
 *
 * @author Raffaele Ragni
 */
public class QueueServiceSample extends QueueService<String> {

  @Inject
  public QueueServiceSample(ConnectionFactory factory, ObjectMapper mapper) {
    super(factory, mapper, "queuename", String.class);
  }

  @Override
  public void receive(String message) {

  }

}
