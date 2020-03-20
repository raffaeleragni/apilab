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
package com.github.raffaeleragni.apilab.exceptions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

public class ApplicationExceptionTest {
  
  @Test
  public void testExceptions() {
    var t = new RuntimeException();
    var ex = new ApplicationException("message", t);
    
    assertThat("message taken", ex.getMessage(), is("message"));
    assertThat("cause taken", ex.getCause(), is(t));
    
    ex = new NotFoundException( "not found message");
    
    assertThat("status taken", ex.getHttpCode(), is(404));
    assertThat("message taken", ex.getMessage(), is("not found message"));
  }
  
}
