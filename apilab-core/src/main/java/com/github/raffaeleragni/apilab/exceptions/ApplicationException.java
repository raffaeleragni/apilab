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

/**
 * 
 * @author Raffaele Ragni
 */
public class ApplicationException extends RuntimeException {

  private final int httpCode;

  public ApplicationException(int httpCode, String message) {
    super(message);
    this.httpCode = httpCode;
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
    this.httpCode = 500;
  }

  public int getHttpCode() {
    return httpCode;
  }


}
