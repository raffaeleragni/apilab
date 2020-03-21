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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Raffaele Ragni
 */
public class EnvTest {
  @Test
  public void test() {
    Env env = new Env();
    
    assertThat("test default value", env.get(Env.Vars.API_JWT_SECRET), is("test"));
        
    System.setProperty(Env.Vars.API_JWT_SECRET.name(), "value");
    
    assertThat("found", env.get(Env.Vars.API_JWT_SECRET), is("value"));
  }
}
