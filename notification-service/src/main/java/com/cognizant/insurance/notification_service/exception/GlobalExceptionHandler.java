package com.cognizant.insurance.notification_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

// This service had no handler at all, so a validation failure or a bad path
// variable came back as a raw container error page.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(ForbiddenException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("fields", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler({ MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class })
    public ResponseEntity<Map<String, Object>> handleUnreadable(Exception ex) {
        log.debug("Rejected malformed request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request");
    }

    // Last resort. Log the detail for us; return something generic rather than
    // echoing an internal exception message back over the wire.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleEverythingElse(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(baseBody(status, message));
    }

    private Map<String, Object> baseBody(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
