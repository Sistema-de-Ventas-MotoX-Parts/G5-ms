package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.demo.model.Usuario;
import com.example.demo.service.UsuarioService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private com.example.demo.security.JwtUtil jwtUtil;

    private void validarAdmin(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new SecurityException("Acceso denegado. Token de autorización no proporcionado o inválido.");
        }
        String token = tokenHeader.substring(7);
        String email = jwtUtil.getEmailFromToken(token);
        if (!jwtUtil.validateToken(token, email)) {
            throw new SecurityException("Acceso denegado. Token inválido o expirado.");
        }
        String rol = jwtUtil.getRolFromToken(token);
        if (!"ADMIN".equals(rol)) {
            throw new SecurityException("Acceso denegado. Se requiere rol de administrador.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrar(usuario);
            nuevoUsuario.setContrasenia(null);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Usuario usuario = usuarioService.login(loginRequest.getEmail(), loginRequest.getContrasenia());

            String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().getNombreRol().name());

            Map<String, Object> response = new HashMap<>();
            response.put("id", usuario.getId());
            response.put("nombre", usuario.getNombre());
            response.put("email", usuario.getEmail());
            response.put("rol", usuario.getRol().getNombreRol().name());
            response.put("token", token);
            response.put("mensaje", "Inicio de sesión exitoso");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(401).body(error);
        }
    }

    // CRUD para Usuarios (Solo ADMIN)

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @Valid @RequestBody Usuario usuario) {
        validarAdmin(tokenHeader);
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
        nuevoUsuario.setContrasenia(null);
        return ResponseEntity.ok(nuevoUsuario);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @PathVariable Long id,
            @RequestBody Usuario usuario) {
        validarAdmin(tokenHeader);
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        usuarioActualizado.setContrasenia(null);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @PathVariable Long id) {
        validarAdmin(tokenHeader);
        usuarioService.eliminarUsuario(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado exitosamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodos(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader) {
        validarAdmin(tokenHeader);
        java.util.List<Usuario> usuarios = usuarioService.obtenerTodos();
        usuarios.forEach(u -> u.setContrasenia(null));
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @PathVariable Long id) {
        validarAdmin(tokenHeader);
        Usuario usuario = usuarioService.obtenerPorId(id);
        usuario.setContrasenia(null);
        return ResponseEntity.ok(usuario);
    }

    public static class LoginRequest {
        private String email;
        private String contrasenia;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getContrasenia() {
            return contrasenia;
        }

        public void setContrasenia(String contrasenia) {
            this.contrasenia = contrasenia;
        }
    }

}
