package com.example.demo.controller;

import com.example.demo.dto.FacturaDTO;
import com.example.demo.service.FacturaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @GetMapping
    public ResponseEntity<List<FacturaDTO>> getAll() {
        return ResponseEntity.ok(facturaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FacturaDTO> create(@Valid @RequestBody FacturaDTO facturaDTO) {
        FacturaDTO created = facturaService.crearFactura(facturaDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
