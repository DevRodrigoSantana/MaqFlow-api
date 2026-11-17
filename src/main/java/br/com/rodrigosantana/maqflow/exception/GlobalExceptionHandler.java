package br.com.rodrigosantana.maqflow.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> notFound(ResourceNotFoundException e, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", e.getMessage(), request, e.getFieldErrors());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ProblemDetail> duplicateEmail(DuplicateEmailException e, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", e.getMessage(), request,
                Map.of("email", e.getMessage()));
    }

    @ExceptionHandler(DuplicateSerialNumberException.class)
    public ResponseEntity<ProblemDetail> duplicateSerialNumber(DuplicateSerialNumberException e, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "SERIAL_NUMBER_ALREADY_EXISTS", e.getMessage(), request,
                Map.of("serialNumber", e.getMessage()));
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ProblemDetail> invalid(InvalidRequestException e, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage(), request, e.getFieldErrors());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> illegalArgument(IllegalArgumentException e, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", e.getMessage(), request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> unreadableBody(HttpMessageNotReadableException e, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST_BODY", "O corpo JSON está malformado ou contém um valor inválido.",
                request, Map.of("body", "Verifique a sintaxe JSON, os tipos e os valores dos enums."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetail> argumentTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String field = e.getName();
        return problem(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "O parâmetro informado possui formato inválido.",
                request, Map.of(field, "Valor inválido para " + field + "."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> integrity(DataIntegrityViolationException e, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "DATA_INTEGRITY_CONFLICT",
                "A operação viola uma restrição de integridade dos dados.", request, Map.of());
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail,
                                                   HttpServletRequest request, Map<String, String> fieldErrors) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, detail);
        body.setTitle(status.getReasonPhrase());
        body.setType(URI.create("urn:maqflow:problem:" + code.toLowerCase().replace('_', '-')));
        body.setInstance(URI.create(request.getRequestURI()));
        body.setProperty("code", code);
        body.setProperty("fieldErrors", fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
