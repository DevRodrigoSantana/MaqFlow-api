package br.com.rodrigosantana.maqflow.exception;

import java.util.Map;

public class ResourceNotFoundException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public ResourceNotFoundException(String message) {
        this(message, Map.of());
    }

    public ResourceNotFoundException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = Map.copyOf(fieldErrors);
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
