package br.com.rodrigosantana.maqflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({AuthenticationFailedException.class, InvalidRefreshTokenException.class, RefreshTokenReuseException.class})
    public ResponseEntity<ApiErrorResponse> authentication(RuntimeException e, HttpServletRequest request) {
        return response(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(ResourceNotFoundException e, HttpServletRequest request) {
        return response(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage(), request, e.getFieldErrors());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiErrorResponse> duplicateEmail(DuplicateEmailException e, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", e.getMessage(), request,
                Map.of("email", e.getMessage()));
    }

    @ExceptionHandler(DuplicateSerialNumberException.class)
    public ResponseEntity<ApiErrorResponse> duplicateSerialNumber(DuplicateSerialNumberException e, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "SERIAL_NUMBER_ALREADY_EXISTS", e.getMessage(), request,
                Map.of("serialNumber", e.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> invalid(InvalidRequestException e, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage(), request, e.getFieldErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> fields = new TreeMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(error.getField(), error.getDefaultMessage() == null ? "Valor inválido." : error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Um ou mais campos são inválidos.", request, fields);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> methodValidation(HandlerMethodValidationException e, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Um ou mais parâmetros são inválidos.", request,
                Map.of("request", "Verifique os parâmetros informados."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "A requisição contém um valor inválido.", request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> unreadableBody(HttpMessageNotReadableException e, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST_BODY",
                "O corpo JSON está malformado ou contém um valor inválido.", request,
                Map.of("body", "Verifique a sintaxe JSON, os tipos e os valores dos enums."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> argumentTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String field = e.getName();
        return response(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "O parâmetro informado possui formato inválido.",
                request, Map.of(field, "Valor inválido para " + field + "."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> integrity(DataIntegrityViolationException e, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "DATA_INTEGRITY_CONFLICT",
                "A operação viola uma restrição de integridade dos dados.", request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String code, String message,
                                                       HttpServletRequest request, Map<String, String> fieldErrors) {
        ApiErrorResponse body = new ApiErrorResponse(OffsetDateTime.now(), status.value(), message,
                request.getRequestURI(), Map.copyOf(fieldErrors), code);
        return ResponseEntity.status(status).body(body);
    }
}
