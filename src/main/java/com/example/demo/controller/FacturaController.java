package com.example.demo.controller;

import com.example.demo.dto.FacturaDTO;
import com.example.demo.service.FacturaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.security.JwtUtil;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.model.NombreRol;
import com.example.demo.model.Usuario;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/facturas")
public class FacturaController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<FacturaDTO>> getAll() {
        return ResponseEntity.ok(facturaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(facturaService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FacturaDTO> create(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @Valid @RequestBody FacturaDTO facturaDTO) {
        
        String token = null;
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
        } else if (cookieToken != null && !cookieToken.isEmpty()) {
            token = cookieToken;
        }

        if (token != null) {
            String email = jwtUtil.getEmailFromToken(token);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
            
            if (!"ADMIN".equals(usuario.getRol().getNombreRol().name())) {
                facturaDTO.setIdUsuario(usuario.getId());
            }
        }
        
        FacturaDTO created = facturaService.crearFactura(facturaDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // OBTENER FACTURAS DEL USUARIO LOGUEADO
    @GetMapping("/mis-facturas")
    public ResponseEntity<List<FacturaDTO>> getMisFacturas(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
        
        // 1. Extraemos el string del token
        String token = extraerTokenLimpio(tokenHeader, cookieToken);

        // 2. Sacamos el email del usuario
        String email = jwtUtil.getEmailFromToken(token);

        // 3. Buscamos al usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // 4. Llamamos al servicio para obtener sus facturas
        return ResponseEntity.ok(facturaService.findByUsuarioId(usuario.getId()));
    }

    // --- Método auxiliar privado para el token ---
    private String extraerTokenLimpio(String tokenHeader, String cookieToken) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        } else if (cookieToken != null && !cookieToken.isEmpty()) {
            return cookieToken;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no encontrado");
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<FacturaDTO> actualizarEstado(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @RequestParam String estado) {
    	jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN); 
        FacturaDTO updated = facturaService.actualizarEstado(id, estado);
        return ResponseEntity.ok(updated);
    }
}
