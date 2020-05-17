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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 *
 * @author Raffaele Ragni
 */
public class JacksonRedisCodecTest {

  @Test
  public void testKey() {
    var mapper = new ObjectMapper();
    var codec = new JacksonRedisCodec<String>(String.class, mapper);

    var result = codec.decodeKey(codec.encodeKey("asd"));

    assertThat("round trip encoding", result, is("asd"));
  }

  @Test
  public void testValue() {
    var mapper = new ObjectMapper();
    var codec = new JacksonRedisCodec<Map>(Map.class, mapper);

    var map = Map.of("k1", "v1", "k2", "v21");

    var result = codec.decodeValue(codec.encodeValue(map));

    assertThat("round trip encoding", result, is(map));
  }

  @Test
  public void testValueErrors() throws IOException {
    var mapper = mock(ObjectMapper.class);
    var codec = new JacksonRedisCodec<Map>(Map.class, mapper);

    var map = Map.of("k1", "v1", "k2", "v21");

    doThrow(IOException.class).when(mapper).readValue(any(byte[].class), any(Class.class));
    assertThrows(ApplicationException.class, () -> {
      codec.decodeValue(ByteBuffer.wrap(new byte[]{}));
    });

    doThrow(JsonProcessingException.class).when(mapper).writeValueAsBytes(any(Map.class));
    assertThrows(ApplicationException.class, () -> {
      codec.encodeValue(map);
    });

  }

}