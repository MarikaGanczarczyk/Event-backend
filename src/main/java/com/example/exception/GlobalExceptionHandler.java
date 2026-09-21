package com.example.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEventNotFound(EventNotFoundException ex) {
        log.debug("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    // The only place unexpected failures are logged (once, with stack trace); the client gets no internals.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        log.debug("Validation failed on {}: {}", request.getDescription(false), errors);
        Map<String, Object> body = new LinkedHashMap<>(errorBody(status, "Validation failed"));
        body.put("errors", errors);
        return ResponseEntity.status(status).headers(headers).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.debug("Unreadable request body on {}: {}", request.getDescription(false), ex.getClass().getSimpleName());
        return ResponseEntity.status(status).headers(headers).body(errorBody(status, "Malformed request body"));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("Server error on {}", request.getDescription(false), ex);
        } else {
            log.debug("Client error {} on {}: {}", status.value(), request.getDescription(false),
                    ex.getClass().getSimpleName());
        }
        String message = body instanceof ProblemDetail pd && pd.getDetail() != null
                ? pd.getDetail()
                : HttpStatus.valueOf(status.value()).getReasonPhrase();
        return new ResponseEntity<>(errorBody(status, message), headers, status);
    }

    private static Map<String, Object> errorBody(HttpStatusCode status, String message) {
        return Map.of(
                "status", status.value(),
                "error", HttpStatus.valueOf(status.value()).getReasonPhrase(),
                "message", message,
                "timestamp", Instant.now().toString());
    }
}
