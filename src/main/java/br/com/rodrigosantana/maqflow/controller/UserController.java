package br.com.rodrigosantana.maqflow.controller;

import br.com.rodrigosantana.maqflow.dto.user.*;
import br.com.rodrigosantana.maqflow.service.UserService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService service;

    public UserController(UserService s) {
        service = s;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@RequestBody UserRequestDTO d) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(d));
    }

    @GetMapping("/{id}")
    public UserResponseDTO get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<UserResponseDTO> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public UserResponseDTO update(@PathVariable UUID id, @RequestBody UserRequestDTO d) {
        return service.update(id, d);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
