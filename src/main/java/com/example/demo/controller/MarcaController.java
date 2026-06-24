package com.example.demo.controller;

import com.example.demo.dto.MarcaRequestDTO;
import com.example.demo.dto.MarcaResponseDTO;
import com.example.demo.model.NombreRol;
import com.example.demo.service.MarcaService;
import com.example.demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marcas")
@CrossOrigin(origins = "*")
public class MarcaController {

    private final MarcaService marcaService;

    @Autowired
    private JwtUtil jwtUtil;

    public MarcaController(MarcaService marcaService) {
        this.marcaService = marcaService;
    }

    @PostMapping
    public ResponseEntity<MarcaResponseDTO> crear(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @Valid @RequestBody MarcaRequestDTO requestDTO) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
    	MarcaResponseDTO response = marcaService.crearMarca(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MarcaResponseDTO>> obtenerTodas(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        return ResponseEntity.ok(marcaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        return ResponseEntity.ok(marcaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MarcaResponseDTO> actualizar(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @Valid @RequestBody MarcaRequestDTO requestDTO) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        MarcaResponseDTO response = marcaService.actualizarMarca(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        marcaService.eliminarMarca(id);
        return ResponseEntity.noContent().build();
    }
}
