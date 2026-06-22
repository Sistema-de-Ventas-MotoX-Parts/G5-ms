package com.example.demo.controller;

import com.example.demo.dto.MetodoPagoDTO;
import com.example.demo.service.MetodoPagoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoService metodoPagoService;

    @GetMapping
    public ResponseEntity<List<MetodoPagoDTO>> getAll() {
        return ResponseEntity.ok(metodoPagoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(metodoPagoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MetodoPagoDTO> create(@Valid @RequestBody MetodoPagoDTO metodoPagoDTO) {
        MetodoPagoDTO created = metodoPagoService.save(metodoPagoDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}
