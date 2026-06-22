package com.example.demo.controller;

import com.example.demo.dto.PerfilActualizarDTO;
import com.example.demo.model.Usuario;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Extrae el token desde los headers o las cookies
     */
    private String extractToken(String tokenHeader, String cookieToken) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        } else if (cookieToken != null && !cookieToken.isEmpty()) {
            return cookieToken;
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado");
    }

    /**
     * Obtiene el usuario autenticado a partir del token
     */
    private Usuario getAuthenticatedUser(String tokenHeader, String cookieToken) {
        String token = extractToken(tokenHeader, cookieToken);
        String email = jwtUtil.getEmailFromToken(token);
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario autenticado no encontrado"));
    }

    // ==========================================
    // ENDPOINTS DE PERFIL (Para el usuario logueado)
    // ==========================================

    @GetMapping("/perfil")
    public ResponseEntity<?> obtenerMiPerfil(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
        Usuario usuario = getAuthenticatedUser(tokenHeader, cookieToken);
        usuario.setContrasenia(null);
        return ResponseEntity.ok(usuario);
    }

    @PutMapping("/perfil")
    public ResponseEntity<?> actualizarMiPerfil(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @RequestBody PerfilActualizarDTO dto) {
        Usuario usuarioLogueado = getAuthenticatedUser(tokenHeader, cookieToken);
        
        try {
            Usuario usuarioActualizado = usuarioService.actualizarMiPerfil(usuarioLogueado.getId(), dto);
            usuarioActualizado.setContrasenia(null);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/perfil")
    public ResponseEntity<?> eliminarMiPerfil(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
        Usuario usuarioLogueado = getAuthenticatedUser(tokenHeader, cookieToken);
        usuarioService.eliminarUsuario(usuarioLogueado.getId());
        
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Tu cuenta ha sido eliminada exitosamente");
        return ResponseEntity.ok(response);
    }
}
