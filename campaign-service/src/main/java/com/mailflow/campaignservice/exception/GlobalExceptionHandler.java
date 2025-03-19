package com.mailflow.campaignservice.exception;

import com.mailflow.campaignservice.dto.response.ErrorResponse;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex,
      HttpServletRequest request) {
    log.error("Resource not found - Message: {}", ex.getMessage());
    return ErrorResponse.builder()
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(FeignException.class)
  @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
  public ErrorResponse handleFeignException(FeignException ex, HttpServletRequest request) {
    log.error("Service communication error: {}", ex.getMessage());
    return ErrorResponse.builder()
            .status(HttpStatus.SERVICE_UNAVAILABLE.value())
            .error("Service Communication Error")
            .message("Failed to communicate with downstream service")
            .path(request.getRequestURI())
            .build();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidationExceptions(
          MethodArgumentNotValidException ex, HttpServletRequest request) {

    log.error("Validation error occurred", ex);

    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult()
            .getAllErrors()
            .forEach(
                    (error) -> {
                      String fieldName = ((FieldError) error).getField();
                      String errorMessage = error.getDefaultMessage();
                      validationErrors.put(fieldName, errorMessage);
                    });

    return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("Input validation failed")
            .path(request.getRequestURI())
            .errors(validationErrors)
            .build();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception occurred", ex);
    return ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
            .message("An unexpected error occurred")
            .path(request.getRequestURI())
            .build();
  }
}
