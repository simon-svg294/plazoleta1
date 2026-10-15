package com.plazoletaucc.Plazoleta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExcpetionHandler {
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<String> handleCorreoDuplicadoException(CorreoDuplicadoException correoDuplicadoException) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(correoDuplicadoException.getMessage());
    }
}
