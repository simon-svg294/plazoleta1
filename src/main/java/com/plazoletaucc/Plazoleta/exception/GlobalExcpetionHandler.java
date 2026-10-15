package com.plazoletaucc.Plazoleta.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExcpetionHandler {
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<String> handleCorreoDuplicadoException(CorreoDuplicadoException correoDuplicadoException) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(correoDuplicadoException.getMessage());
    }

    @ExceptionHandler(PropietarioNoValidoException.class)
    public ResponseEntity<String> handlePropietarioNoValido(PropietarioNoValidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(NitDuplicadoException.class)
    public ResponseEntity<String> handleNitDuplicado(NitDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errores.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}
