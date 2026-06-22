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
@CrossOrigin(origins = "*")
@RequestMapping("/api/motocicletas")
public class MotocicletaController {

    private final MotocicletaService motocicletaService;

    public MotocicletaController(MotocicletaService motocicletaService) {
        this.motocicletaService = motocicletaService;
    }

    //ALTA
    @PostMapping
    public ResponseEntity<MotocicletaResponseDTO> crearMotocicleta(
            @Valid @RequestBody MotocicletaRequestDTO requestDTO) {
        MotocicletaResponseDTO response = motocicletaService.crearMotocicleta(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //LISTAR
    @GetMapping
    public ResponseEntity<List<MotocicletaResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(motocicletaService.obtenerTodas());
    }

    //OBTENER
    @GetMapping("/{id}")
    public ResponseEntity<MotocicletaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(motocicletaService.obtenerPorId(id));
    }

    // OBTENER POR ID DE USUARIO
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<MotocicletaResponseDTO>> obtenerPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(motocicletaService.obtenerPorUsuario(idUsuario));
    }

    //OBTENER POR PATENTE
    @GetMapping("/patente/{patente}")
    public ResponseEntity<MotocicletaResponseDTO> obtenerPorPatente(@PathVariable String patente) {
        return ResponseEntity.ok(motocicletaService.obtenerPorPatente(patente));
    }

    //EDITAR
    @PutMapping("/{id}")
    public ResponseEntity<MotocicletaResponseDTO> actualizarMotocicleta(
            @PathVariable Long id,
            @Valid @RequestBody MotocicletaRequestDTO requestDTO) {
        return ResponseEntity.ok(motocicletaService.actualizarMotocicleta(id, requestDTO));
    }

    //BAJA
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMotocicleta(@PathVariable Long id) {
        motocicletaService.eliminarMotocicleta(id);
        return ResponseEntity.noContent().build();
    }
}
