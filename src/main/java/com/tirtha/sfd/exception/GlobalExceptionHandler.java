package com.tirtha.sfd.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("errors", fieldErrors);
        body.put("message", "Validation failed");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

   @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
            Map<String, Object> body = new HashMap<>();

            String message = "Database error";

            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("email")) {
                message = "Email already exists";
            }

            body.put("message", message);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }
}
