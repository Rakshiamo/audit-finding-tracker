package com.internship.tool.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage() == null ? "An unexpected system error occurred" : ex.getMessage();
        HttpStatus status = resolveStatus(message);
        return buildResponse(status, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied: You do not have the required permissions.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuth(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed: Invalid or expired token.");
    }

    // New: Handles @Valid validation errors from your DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String firstError = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, firstError);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    private HttpStatus resolveStatus(String message) {
        String lower = message.toLowerCase();
        if (lower.contains("finding not found") || lower.contains("not found")) {
            return HttpStatus.NOT_FOUND;
        }
        if (lower.contains("user not found") || lower.contains("invalid credentials") || lower.contains("inactive")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (lower.contains("already exists") || lower.contains("invalid token") || lower.contains("bad request")) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
