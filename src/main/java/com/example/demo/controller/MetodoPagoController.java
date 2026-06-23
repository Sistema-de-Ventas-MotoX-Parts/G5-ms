package com.example.demo.controller;

import com.example.demo.dto.MetodoPagoDTO;
import com.example.demo.service.MetodoPagoService;
import com.example.demo.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoService metodoPagoService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<MetodoPagoDTO>> getAll() {
        return ResponseEntity.ok(metodoPagoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(metodoPagoService.findById(id));
    }

    @PostMapping
    public ResponseEntity<MetodoPagoDTO> create(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @Valid @RequestBody MetodoPagoDTO metodoPagoDTO) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        MetodoPagoDTO created = metodoPagoService.save(metodoPagoDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> update(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @Valid @RequestBody MetodoPagoDTO metodoPagoDTO) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        MetodoPagoDTO updated = metodoPagoService.update(id, metodoPagoDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        metodoPagoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
