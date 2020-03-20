package com.github.raffaeleragni.apilab.appconfig;

import io.javalin.Javalin;
import io.javalin.http.Handler;

public interface Endpoint extends Handler {
  void register(Javalin javalin);
}
