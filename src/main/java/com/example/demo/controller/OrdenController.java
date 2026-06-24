package com.example.demo.controller;

import com.example.demo.dto.OrdenRequestDTO;
import com.example.demo.dto.OrdenResponseDTO;
import com.example.demo.model.NombreRol;
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
        jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN);
        OrdenResponseDTO response = ordenService.crearOrden(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrdenResponseDTO>> obtenerTodas(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @RequestParam(required = false) Long idMecanico) {
        jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN);
        return ResponseEntity.ok(ordenService.obtenerTodas(idMecanico));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenResponseDTO> obtenerPorId(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN);
        return ResponseEntity.ok(ordenService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenResponseDTO> actualizarOrden(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @Valid @RequestBody OrdenRequestDTO requestDTO) {
        jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.MECHANIC);
        return ResponseEntity.ok(ordenService.actualizarOrden(id, requestDTO));
    }

    // MODIFICAR EL ESTADO --MECANICO--
    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenResponseDTO> actualizarEstado(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id,
            @RequestParam String estado,
            @RequestParam(required = false) String pin) {
        // 1. Validamos rol: MECÁNICO
        jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.MECHANIC);
        // 2. Modificamos el estado de la orden
        return ResponseEntity.ok(ordenService.actualizarEstado(id, estado, pin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOrden(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken,
            @PathVariable Long id) {
        // 1. Validamos rol: ADMIN
        jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.ADMIN);
        // 2. Eliminamos la orden
        ordenService.eliminarOrden(id);
        return ResponseEntity.noContent().build();
    }

    // OBTENER ORDENES DE MECANICO LOGUEADO
    @GetMapping("/mis-ordenes")
    public ResponseEntity<List<OrdenResponseDTO>> obtenerMisOrdenes(
            @RequestHeader(value = "Authorization", required = false) String tokenHeader,
            @CookieValue(value = "token_jwt", required = false) String cookieToken) {

        // 1. Validamos rol: MECÁNICO
        jwtUtil.validarRolRequerido(tokenHeader, cookieToken, NombreRol.MECHANIC);

        // 2. Extraemos el string del token
        String token = extraerTokenLimpio(tokenHeader, cookieToken);

        // 3. Sacamos el email del mecánico
        String emailMecanico = jwtUtil.getEmailFromToken(token);

        // 4. Llamamos al servicio
        List<OrdenResponseDTO> misOrdenes = ordenService.obtenerOrdenesPorEmailMecanico(emailMecanico);

        return ResponseEntity.ok(misOrdenes);
    }

    // --- Método auxiliar privado para el token ---
    private String extraerTokenLimpio(String tokenHeader, String cookieToken) {
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            return tokenHeader.substring(7);
        } else if (cookieToken != null && !cookieToken.isEmpty()) {
            return cookieToken;
        }
        throw new SecurityException("Token no encontrado");
    }
}
