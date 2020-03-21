package com.github.raffaeleragni.apilab.exceptions;

public class UnprocessableEntityException extends ApplicationException {

  public UnprocessableEntityException(String message) {
    super(422, message);
  }
  
}
