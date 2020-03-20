package com.github.raffaeleragni.apilab.appconfig;

import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_CONSUMERS;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_ENABLE_MIGRATION;
import static com.github.raffaeleragni.apilab.appconfig.Env.Vars.API_QUIT_AFTER_MIGRATION;
import io.javalin.Javalin;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationTest {
  
  @Test
  public void testMigrationQuit() throws IOException {
    var app = new Application();
    app.javalin = mock(Javalin.class);
    app.env = mock(Env.class);
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("true");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("true");
    app.start();
    
    verify(app.javalin, times(0)).start();
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("false");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("true");
    app.start();
    
    verify(app.javalin, times(1)).start();
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("true");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("false");
    app.start();
    
    verify(app.javalin, times(2)).start();
    
    when(app.env.get(API_ENABLE_MIGRATION)).thenReturn("false");
    when(app.env.get(API_QUIT_AFTER_MIGRATION)).thenReturn("false");
    app.start();
    
    verify(app.javalin, times(3)).start();
    
    when(app.env.get(API_ENABLE_CONSUMERS)).thenReturn("false");
    app.stop();
    
    verify(app.javalin).stop();
  }
  
}
