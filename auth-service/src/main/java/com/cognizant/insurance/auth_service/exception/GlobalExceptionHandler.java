package com.cognizant.insurance.auth_service.exception;

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

// Without this, an exception thrown in AuthController escaped to the servlet
// container's /error forward, which SecurityConfig then blocked - so a duplicate
// email came back to the client as a bare 401 Unauthorized.
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Turns bean-validation failures into a field -> message map.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("fields", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // Malformed JSON, or a value that cannot be coerced into the target type.
    @ExceptionHandler({ HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class })
    public ResponseEntity<Map<String, Object>> handleUnreadable(Exception ex) {
        log.debug("Rejected malformed request: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed or unreadable request");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Last resort. Log the detail for us, return something generic to the caller
    // rather than echoing an internal exception message back over the wire.
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
