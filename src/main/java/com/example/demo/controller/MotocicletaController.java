package com.example.demo.controller;

import com.example.demo.dto.MotocicletaRequestDTO;
import com.example.demo.dto.MotocicletaResponseDTO;
import com.example.demo.service.MotocicletaService;
import com.example.demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/motocicletas")
public class MotocicletaController {

    private final MotocicletaService motocicletaService;

    @Autowired
    private JwtUtil jwtUtil;

    public MotocicletaController(MotocicletaService motocicletaService) {
        this.motocicletaService = motocicletaService;
    }

    @PostMapping
    public ResponseEntity<MotocicletaResponseDTO> crearMotocicleta(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @Valid @RequestBody MotocicletaRequestDTO requestDTO) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        MotocicletaResponseDTO response = motocicletaService.crearMotocicleta(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MotocicletaResponseDTO>> obtenerTodas(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(motocicletaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MotocicletaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(motocicletaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MotocicletaResponseDTO> actualizarMotocicleta(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id, 
            @Valid @RequestBody MotocicletaRequestDTO requestDTO) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(motocicletaService.actualizarMotocicleta(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarMotocicleta(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        motocicletaService.eliminarMotocicleta(id);
        return ResponseEntity.noContent().build();
    }
}
