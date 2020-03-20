package com.github.raffaeleragni.apilab;

import com.github.raffaeleragni.apilab.appconfig.Application;
import com.github.raffaeleragni.apilab.appconfig.DaggerApplicationComponent;
import java.io.IOException;

public final class Main {

  private Main() {}

  static Application app = DaggerApplicationComponent.create().application();

  public static void main(String[] args) throws IOException {
    java.security.Security.setProperty("networkaddress.cache.ttl" , "60");
    app.start();
  }

  public static void stop() {
    app.stop();
  }

}
