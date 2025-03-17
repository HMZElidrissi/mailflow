package com.mailflow.templateservice.exception;

public class InvalidTemplateException extends RuntimeException {
  public InvalidTemplateException(String message) {
    super(message);
  }
}