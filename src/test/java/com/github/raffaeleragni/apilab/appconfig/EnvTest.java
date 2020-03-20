package com.github.raffaeleragni.apilab.appconfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

public class EnvTest {
  @Test
  public void test() {
    Env env = new Env();
    
    assertThat("test default value", env.get(Env.Vars.API_JWT_SECRET), is("test"));
        
    System.setProperty(Env.Vars.API_JWT_SECRET.name(), "value");
    
    assertThat("found", env.get(Env.Vars.API_JWT_SECRET), is("value"));
  }
}
