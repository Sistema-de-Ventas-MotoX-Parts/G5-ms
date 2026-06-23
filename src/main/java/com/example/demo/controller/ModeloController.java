package com.example.demo.controller;

import com.example.demo.dto.ModeloRequestDTO;
import com.example.demo.dto.ModeloResponseDTO;
import com.example.demo.service.ModeloService;
import com.example.demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modelos")
@CrossOrigin(origins = "*")
public class ModeloController {

    private final ModeloService modeloService;

    @Autowired
    private JwtUtil jwtUtil;

    public ModeloController(ModeloService modeloService) {
        this.modeloService = modeloService;
    }

    @PostMapping
    public ResponseEntity<ModeloResponseDTO> crear(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @Valid @RequestBody ModeloRequestDTO requestDTO) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        ModeloResponseDTO response = modeloService.crearModelo(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ModeloResponseDTO>> obtenerTodos(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(modeloService.obtenerTodos());
    }

    @GetMapping("/marca/{idMarca}")
    public ResponseEntity<List<ModeloResponseDTO>> obtenerPorMarca(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long idMarca) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(modeloService.obtenerPorMarca(idMarca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeloResponseDTO> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(modeloService.obtenerPorId(id));
    }
}
