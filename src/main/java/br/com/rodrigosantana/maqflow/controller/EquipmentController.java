package br.com.rodrigosantana.maqflow.controller;

import br.com.rodrigosantana.maqflow.dto.equipment.*;
import br.com.rodrigosantana.maqflow.service.EquipmentService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {
    private final EquipmentService service;

    public EquipmentController(EquipmentService s) {
        service = s;
    }

    @PostMapping
    public ResponseEntity<EquipmentResponseDTO> create(@RequestBody EquipmentRequestDTO d) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(d));
    }

    @GetMapping("/{id}")
    public EquipmentResponseDTO get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    public List<EquipmentResponseDTO> list() {
        return service.list();
    }

    @PutMapping("/{id}")
    public EquipmentResponseDTO update(@PathVariable UUID id, @RequestBody EquipmentRequestDTO d) {
        return service.update(id, d);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
