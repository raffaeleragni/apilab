package com.github.raffaeleragni.apilab.appconfig;

import com.github.raffaeleragni.apilab.Main;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.lettuce.core.RedisClient;
import java.io.IOException;
import static java.util.Optional.ofNullable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class EndpointsIT {

  static OkHttpClient client;

  @BeforeAll
  public static void prepare() throws Exception {
    client = new OkHttpClient();
    Main.main(new String[]{});
  }

  @AfterAll
  public static void shutdown() {
    Main.stop();
  }

  @Test
  public void testAll() throws IOException, JSONException {

    var result = get("/status/version", true);
    assertThat("Returned expected result", result, is("{\"version\":\"unknown\"}"));

    result = get("/status/health", true);
    JSONAssert.assertEquals("Returned expected result",
        "{\"redis\":true,"
        + "\"database\":true,"
        + "\"rabbitmq\":true}", 
        result,
        true);
    
    result = get("/notfound", true);
    assertThat("404 not found", result, is ("Not found"));
  }

  private String get(String url, boolean authenticated) throws IOException {
    var host = "localhost";
    var port = ofNullable(System.getenv("JAVALIN_HTTP2_PORT"))
      .map(Integer::valueOf)
      .orElse(8080);
    var rootURL = "http://"+host+":"+port;
    var alg = Algorithm.HMAC256("test");
    var token = JWT.create()
      .withArrayClaim("roles", new String[]{"admin"})
      .sign(alg);
    var requestBuilder = new Request.Builder()
      .url(rootURL+url);
    if (authenticated) {
      requestBuilder = requestBuilder.header("Authorization", "Bearer "+token);
    }
    var body = client.newCall(requestBuilder.get().build()).execute().body();
    if (body != null) {
      return body.string();
    } else {
      return "";
    }
  }

}
