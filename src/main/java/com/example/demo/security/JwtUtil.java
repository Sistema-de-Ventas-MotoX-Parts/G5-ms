package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.model.NombreRol;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:secretkey123456}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 24 horas en milisegundos (1 día)
    private long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            keyBytes = digest.digest(keyBytes);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo inicializar SHA-256 para la firma JWT", e);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, NombreRol rol) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol.name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public String getRolFromToken(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        final Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.before(new Date());
    }

    public Boolean validateToken(String token, String email) {
        final String tokenEmail = getEmailFromToken(token);
        return (tokenEmail.equals(email) && !isTokenExpired(token));
    }

    public void validarAdmin(String tokenHeader) {
    	validarRolRequerido(tokenHeader, null, NombreRol.ADMIN);
    }

 // Reemplaza validarAdmin por un método genérico:
    public void validarRolRequerido(String tokenHeader, String cookieToken, NombreRol rolEsperado) {
        String token = null;
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
        } else if (cookieToken != null && !cookieToken.isEmpty()) {
            token = cookieToken;
        }

        if (token == null) {
            throw new SecurityException("Acceso denegado. Token de autorización no proporcionado.");
        }
        
        String email = getEmailFromToken(token);
        if (!validateToken(token, email)) {
            throw new SecurityException("Acceso denegado. Token inválido o expirado.");
        }
        
        String rolEnToken = getRolFromToken(token);
        
        // Comparamos dinámicamente contra el Enum que pasaste por parámetro
        if (!rolEsperado.name().equals(rolEnToken)) {
            throw new SecurityException("Acceso denegado. Se requiere el rol: " + rolEsperado.name());
        }
    }
    
    public void validarRolesRequeridos(String tokenHeader, String cookieToken, NombreRol... rolesEsperados) {
        String token = null;
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
        } else if (cookieToken != null && !cookieToken.isEmpty()) {
            token = cookieToken;
        }

        if (token == null) {
            throw new SecurityException("Acceso denegado. Token de autorización no proporcionado.");
        }
        
        String email = getEmailFromToken(token);
        if (!validateToken(token, email)) {
            throw new SecurityException("Acceso denegado. Token inválido o expirado.");
        }
        
        String rolEnToken = getRolFromToken(token);
        
        boolean rolValido = false;
        for (NombreRol rolEsperado : rolesEsperados) {
            if (rolEsperado.name().equals(rolEnToken)) {
                rolValido = true;
                break;
            }
        }
        
        if (!rolValido) {
            java.util.List<String> nombresRoles = new java.util.ArrayList<>();
            for(NombreRol r : rolesEsperados) nombresRoles.add(r.name());
            throw new SecurityException("Acceso denegado. Se requiere uno de los siguientes roles: " + String.join(", ", nombresRoles));
        }
    }
}
