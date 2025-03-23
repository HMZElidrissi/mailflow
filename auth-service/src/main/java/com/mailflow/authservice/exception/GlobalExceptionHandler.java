package com.mailflow.authservice.exception;

import com.mailflow.authservice.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authorization.AuthorizationDeniedException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(AuthenticationException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public ErrorResponse handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {
    log.error("Authentication error: {}", ex.getMessage());
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.UNAUTHORIZED.value())
        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ErrorResponse handleAuthorizationException(
      AuthorizationDeniedException ex, HttpServletRequest request) {
    log.error("Authorization error: {}", ex.getMessage());
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.FORBIDDEN.value())
        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleResourceNotFoundException(
      ResourceNotFoundException ex, HttpServletRequest request) {
    log.error("Resource not found: {}", ex.getMessage());
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.NOT_FOUND.value())
        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public ErrorResponse handleUserAlreadyExistsException(
      UserAlreadyExistsException ex, HttpServletRequest request) {
    log.error("User already exists: {}", ex.getMessage());
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.CONFLICT.value())
        .error(HttpStatus.CONFLICT.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    log.error("Validation error occurred: {}", ex.getMessage());

    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              validationErrors.put(fieldName, errorMessage);
            });

    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message("Validation failed")
        .path(request.getRequestURI())
        .errors(validationErrors)
        .build();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse handleGenericException(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error occurred", ex);
    return ErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("An unexpected error occurred")
        .path(request.getRequestURI())
        .build();
  }
}
