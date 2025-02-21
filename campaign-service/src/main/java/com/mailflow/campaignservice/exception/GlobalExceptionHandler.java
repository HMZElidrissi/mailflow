package com.mailflow.campaignservice.exception;

import com.mailflow.campaignservice.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.error("Resource not found - Message: {}", ex.getMessage());
    return ErrorResponse.builder()
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .build();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleGlobalException(Exception ex) {
    log.error(
        "Unexpected error occurred in {} - Message: {}",
        ex.getClass().getSimpleName(),
        ex.getMessage());
    return ErrorResponse.builder()
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message(ex.getMessage())
        .build();
  }
}
