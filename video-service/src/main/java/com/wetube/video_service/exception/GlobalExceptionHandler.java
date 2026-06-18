package com.wetube.video_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotReadyException.class)
    public ResponseEntity<String> handleResourceNotReadyException(ResourceNotReadyException e) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(e.getMessage());
    }
}
