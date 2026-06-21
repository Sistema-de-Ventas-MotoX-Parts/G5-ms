package com.example.demo.controller;

import com.example.demo.dto.MotocicletaRequestDTO;
import com.example.demo.dto.MotocicletaResponseDTO;
import com.example.demo.service.MotocicletaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motocicletas")
public class MotocicletaController {

    private final MotocicletaService motocicletaService;

    public MotocicletaController(MotocicletaService motocicletaService) {
        this.motocicletaService = motocicletaService;
    }

    @PostMapping
    public ResponseEntity<MotocicletaResponseDTO> crearMotocicleta(@Valid @RequestBody MotocicletaRequestDTO requestDTO) {
        MotocicletaResponseDTO response = motocicletaService.crearMotocicleta(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MotocicletaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(motocicletaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MotocicletaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(motocicletaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MotocicletaResponseDTO> actualizarMotocicleta(
            @PathVariable Long id, 
            @Valid @RequestBody MotocicletaRequestDTO requestDTO) {
        return ResponseEntity.ok(motocicletaService.actualizarMotocicleta(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMotocicleta(@PathVariable Long id) {
        motocicletaService.eliminarMotocicleta(id);
        return ResponseEntity.noContent().build();
    }
}
