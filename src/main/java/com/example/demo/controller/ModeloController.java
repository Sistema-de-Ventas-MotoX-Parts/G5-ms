package com.example.demo.controller;

import com.example.demo.dto.ModeloRequestDTO;
import com.example.demo.dto.ModeloResponseDTO;
import com.example.demo.model.NombreRol;
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
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        ModeloResponseDTO response = modeloService.crearModelo(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ModeloResponseDTO>> obtenerTodos(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        return ResponseEntity.ok(modeloService.obtenerTodos());
    }

    @GetMapping("/marca/{idMarca}")
    public ResponseEntity<List<ModeloResponseDTO>> obtenerPorMarca(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long idMarca) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        return ResponseEntity.ok(modeloService.obtenerPorMarca(idMarca));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModeloResponseDTO> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        return ResponseEntity.ok(modeloService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModeloResponseDTO> actualizar(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @Valid @RequestBody ModeloRequestDTO requestDTO) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        ModeloResponseDTO response = modeloService.actualizarModelo(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        modeloService.eliminarModelo(id);
        return ResponseEntity.noContent().build();
    }
}
