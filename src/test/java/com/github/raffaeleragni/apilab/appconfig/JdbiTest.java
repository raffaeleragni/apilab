package com.github.raffaeleragni.apilab.appconfig;

import com.github.raffaeleragni.apilab.exceptions.ApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class JdbiTest {

  @Test
  void testJdbi() {
    var config = new ApplicationConfig();
    assertThrows(ApplicationException.class, () -> {
      config.runMigrations(new Env(), "wrong", "url", "and password");
    });
  }

}
