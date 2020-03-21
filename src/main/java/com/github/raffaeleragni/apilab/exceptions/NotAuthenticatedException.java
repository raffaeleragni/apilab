package com.github.raffaeleragni.apilab.exceptions;

public class NotAuthenticatedException extends ApplicationException {

  public NotAuthenticatedException(String message) {
    super(401, message);
  }
  
}
