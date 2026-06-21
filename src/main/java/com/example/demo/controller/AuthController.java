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
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            Usuario usuario = usuarioService.login(loginRequest.getEmail(), loginRequest.getContrasenia());

            String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getRol().getNombreRol().name());

            // Crear la cookie HttpOnly
            org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("token_jwt", token)
                    .httpOnly(true)
                    .secure(false) // cambiar a true en producción si usas HTTPS
                    .path("/")
                    .maxAge(86400) // 1 día de validez
                    .sameSite("Lax")
                    .build();

            response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", usuario.getId());
            responseBody.put("nombre", usuario.getNombre());
            responseBody.put("email", usuario.getEmail());
            responseBody.put("rol", usuario.getRol().getNombreRol().name());
            responseBody.put("token", token); // Se mantiene para compatibilidad con clientes que no usan cookies
            responseBody.put("mensaje", "Inicio de sesión exitoso");

            return ResponseEntity.ok(responseBody);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(401).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Eliminar la cookie seteando maxAge a 0
        org.springframework.http.ResponseCookie cookie = org.springframework.http.ResponseCookie.from("token_jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(org.springframework.http.HttpHeaders.SET_COOKIE, cookie.toString());

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("mensaje", "Sesión cerrada exitosamente");
        return ResponseEntity.ok(responseBody);
    }

    // CRUD para Usuarios (Solo ADMIN)

    @PostMapping("/usuarios")
    public ResponseEntity<?> crearUsuario(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @Valid @RequestBody Usuario usuario) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
        nuevoUsuario.setContrasenia(null);
        return ResponseEntity.ok(nuevoUsuario);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizarUsuario(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @RequestBody Usuario usuario) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        Usuario usuarioActualizado = usuarioService.actualizarUsuario(id, usuario);
        usuarioActualizado.setContrasenia(null);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminarUsuario(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        usuarioService.eliminarUsuario(id);
        Map<String, String> response = new HashMap<>();
        response.put("mensaje", "Usuario eliminado exitosamente");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/usuarios")
    public ResponseEntity<?> obtenerTodos(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
        java.util.List<Usuario> usuarios = usuarioService.obtenerTodos();
        usuarios.forEach(u -> u.setContrasenia(null));
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/usuarios/{id}")
    public ResponseEntity<?> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarAdmin(tokenHeader, cookieToken);
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
