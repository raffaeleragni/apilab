package com.github.raffaeleragni.apilab.appconfig;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.javalin.core.security.Role;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationConfigTest {
  
  @Test
  public void testDisabledMigrations() {
    var config = new ApplicationConfig();
    var env = mock(Env.class);
    when(env.get(Env.Vars.API_ENABLE_MIGRATION)).thenReturn("false");
    
    config.runMigrations(env, "", "", "");
    assertThat("nothing to assert for this case?", config, not(nullValue()));
  }
  
  @Test
  public void testNoJavalinStartup() {
    var config = new ApplicationConfig();
    var env = mock(Env.class);
    var endpoint = mock(Endpoint.class);
    when(env.get(Env.Vars.API_ENABLE_ENDPOINTS)).thenReturn("false");
    
    config.javalin(env,
      s -> new Role(){}, 
      Set.of(endpoint),
      config.objectMapper(),
      () -> Map.of("db", true));
    
    verify(endpoint, times(0)).register(any());
  }
  
  @Test
  public void testRabbitFalse() throws Exception {
    var factory = mock(ConnectionFactory.class);
    var connection = mock(Connection.class);
    when(factory.newConnection()).thenReturn(connection);
    when(connection.isOpen()).thenThrow(RuntimeException.class);
    
    var result = ApplicationConfig.checkRabbit(factory);
    assertThat("failed health check", result, is(false));
  }
  
}
