package com.mailflow.templateservice.exception;

public class TemplateAlreadyExistsException extends RuntimeException {
    public TemplateAlreadyExistsException(String message) {
        super(message);
    }
}