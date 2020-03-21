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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import io.lettuce.core.codec.RedisCodec;
import java.io.IOException;
import java.nio.ByteBuffer;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 
 * @author Raffaele Ragni
 */
public class JacksonRedisCodec<T> implements RedisCodec<String, T> {

  private final ObjectMapper mapper;
  private final Class<T> clazz;
  
  public JacksonRedisCodec(Class<T> clazz, ObjectMapper mapper) {
    this.mapper = mapper;
    this.clazz = clazz;
  }
  
  @Override
  public String decodeKey(ByteBuffer bytes) {
    return UTF_8.decode(bytes).toString();
  }

  @Override
  public ByteBuffer encodeKey(String key) {
    return UTF_8.encode(key);
  }
  
  @Override
  public T decodeValue(ByteBuffer bytes) {
    try {
      byte[] arr = new byte[bytes.remaining()];
      bytes.get(arr);
      return mapper.readValue(arr, clazz);
    } catch (IOException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }

  @Override
  public ByteBuffer encodeValue(T value) {
    try {
      return ByteBuffer.wrap(mapper.writeValueAsBytes(value));
    } catch (JsonProcessingException ex) {
      throw new ApplicationException(ex.getMessage(), ex);
    }
  }
  
}
