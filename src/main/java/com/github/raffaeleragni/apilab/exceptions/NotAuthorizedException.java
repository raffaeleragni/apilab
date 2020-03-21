package com.github.raffaeleragni.apilab.exceptions;

public class NotAuthorizedException extends ApplicationException {

  public NotAuthorizedException(String message) {
    super(403, message);
  }
  
}
