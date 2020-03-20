package com.github.raffaeleragni.apilab.auth;

import io.javalin.core.security.Role;

public enum Roles implements Role {
  NONE, USER, ADMIN
}
