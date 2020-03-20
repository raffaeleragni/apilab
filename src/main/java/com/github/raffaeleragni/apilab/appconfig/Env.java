package com.github.raffaeleragni.apilab.appconfig;

import javax.inject.Inject;

public class Env {
  
  public enum Vars {
    API_ENABLE_ENDPOINTS,
    API_ENABLE_CONSUMERS,
    API_ENABLE_SCHEDULED,
    API_ENABLE_MIGRATION,
    API_QUIT_AFTER_MIGRATION,
    API_JWT_SECRET,
    API_REDIS_URL,
    API_DATABASE_URL,
    API_DATABASE_USERNAME,
    API_DATABASE_PASSWORD,
    API_DATABASE_MAXPOOLSZE,
    API_RABBITMQ_USERNAME,
    API_RABBITMQ_PASSWORD,
    API_RABBITMQ_HOST,
    API_RABBITMQ_PORT,
  }
  
  @Inject
  public Env() {
    // Injector constructor
  }
  
  public String get(Vars var) {
    var value = System.getProperty(var.name());
    if (value != null) {
      return value;
    }

    // Why is this line ignored:
    // Reading environment variables can cause injection in some situations.
    // The environment variable being read here are custom and not the usual ones
    // that are present in a JVM (such as user name for example).
    // Also, the application is meant to be used in a docker environment where a
    // containers is more easily controlled via environment variables.
    // If you plan to use this application outside docker, then it would be best if
    // this line is removed and the getProperty becomes the only way to receive properties
    // using the command line options, or find alternative ways to load variables.
    return System.getenv(var.name());//NOSONAR
  }
  
}
