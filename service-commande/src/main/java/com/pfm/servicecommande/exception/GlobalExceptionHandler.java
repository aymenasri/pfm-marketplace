package com.pfm.servicecommande.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleException ex) {
        return new ResponseEntity<>(error(ex.getMessage()), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DownstreamServiceException.class)
    public ResponseEntity<Map<String, String>> handleDownstream(DownstreamServiceException ex) {
        return new ResponseEntity<>(error(ex.getMessage()), HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return new ResponseEntity<>(error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDuplicate(DataIntegrityViolationException ex) {
        return new ResponseEntity<>(error("Opération déjà traitée avec cette clé"), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    private Map<String, String> error(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("erreur", message);
        return response;
    }

    @ExceptionHandler(ServiceExterneIndisponibleException.class)
    public ResponseEntity<String> serviceExterneIndisponible(ServiceExterneIndisponibleException ex) {
        return ResponseEntity.status(503).body(ex.getMessage());
    }
}