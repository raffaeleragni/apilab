package com.github.raffaeleragni.addons;

import io.javalin.core.security.Role;

public enum Roles implements Role {
  NONE, USER, ADMIN
}
