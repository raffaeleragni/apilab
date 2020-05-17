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
package com.github.raffaeleragni.apilab.rest;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import javax.inject.Inject;

/**
 *
 * @author Raffaele Ragni
 */
public class GetEndpointSample implements Endpoint {

  @Inject
  public GetEndpointSample() {
  }

  @Override
  public void register(Javalin javalin) {
    javalin.get("/api/test", this);
  }

  @OpenApi(responses = {
    @OpenApiResponse(status = "200")
  })
  @Override
  public void handle(Context ctx) throws Exception {
    ctx.result("Hello universe.");
  }

}