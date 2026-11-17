package br.com.rodrigosantana.maqflow.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ResourceNotFoundException.class) public ResponseEntity<?> notFound(RuntimeException e){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(DuplicateEmailException.class) public ResponseEntity<?> duplicate(RuntimeException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(DuplicateSerialNumberException.class) public ResponseEntity<?> duplicateSerialNumber(DuplicateSerialNumberException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",e.getMessage()));}
 @ExceptionHandler({InvalidRequestException.class, IllegalArgumentException.class}) public ResponseEntity<?> invalid(RuntimeException e){return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));}
 @ExceptionHandler(DataIntegrityViolationException.class) public ResponseEntity<?> integrity(DataIntegrityViolationException e){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error","Operação viola uma integridade referencial ou restrição de dados."));}
}
