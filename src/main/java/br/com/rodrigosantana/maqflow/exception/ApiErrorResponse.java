package br.com.rodrigosantana.maqflow.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ApiErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String path,
        Map<String, String> fieldErrors,
        String code) {
}
