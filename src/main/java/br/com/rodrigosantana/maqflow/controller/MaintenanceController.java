package br.com.rodrigosantana.maqflow.controller;

import br.com.rodrigosantana.maqflow.dto.maintenance.*;
import br.com.rodrigosantana.maqflow.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/maintenances")
public class MaintenanceController {
    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService s) {
        service = s;
    }

    @PostMapping
    public ResponseEntity<MaintenanceResponseDTO> create(@Valid @RequestBody MaintenanceRequestDTO d) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(d));
    }

    @GetMapping("/{id}")
    public MaintenanceResponseDTO get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<MaintenanceResponseDTO> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public MaintenanceResponseDTO update(@PathVariable UUID id, @Valid @RequestBody MaintenanceUpdateRequestDTO d) {
        return service.update(id, d);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
