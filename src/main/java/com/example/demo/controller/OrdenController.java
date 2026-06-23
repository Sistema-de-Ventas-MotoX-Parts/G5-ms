package com.example.demo.controller;

import com.example.demo.dto.OrdenRequestDTO;
import com.example.demo.dto.OrdenResponseDTO;
import com.example.demo.service.OrdenService;
import com.example.demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin(origins = "http://localhost:4200")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<OrdenResponseDTO> crearOrden(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @Valid @RequestBody OrdenRequestDTO requestDTO) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        OrdenResponseDTO response = ordenService.crearOrden(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrdenResponseDTO>> obtenerTodas(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(ordenService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponseDTO> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(ordenService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenResponseDTO> actualizarOrden(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @Valid @RequestBody OrdenRequestDTO requestDTO) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(ordenService.actualizarOrden(id, requestDTO));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenResponseDTO> actualizarEstado(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @RequestParam String estado) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        return ResponseEntity.ok(ordenService.actualizarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrden(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        ordenService.eliminarOrden(id);
        return ResponseEntity.noContent().build();
    }
}
