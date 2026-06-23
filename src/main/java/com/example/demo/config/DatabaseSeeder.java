package com.example.demo.config;

import com.example.demo.model.NombreRol;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.model.MetodoPago;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.MetodoPagoRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        // 1. Asegurar que los registros antiguos tengan activo = true (1) para la Baja Lógica
        try {
            jdbcTemplate.execute("UPDATE usuarios SET activo = 1 WHERE activo = 0 OR activo IS NULL");
            jdbcTemplate.execute("UPDATE productos SET activo = 1 WHERE activo = 0 OR activo IS NULL");
            jdbcTemplate.execute("UPDATE categorias SET activo = 1 WHERE activo = 0 OR activo IS NULL");
            jdbcTemplate.execute("UPDATE metodos_pago SET activo = 1 WHERE activo = 0 OR activo IS NULL");
            jdbcTemplate.execute("UPDATE servicios SET activo = 1 WHERE activo = 0 OR activo IS NULL");
            jdbcTemplate.execute("UPDATE motocicletas SET activo = 1 WHERE activo = 0 OR activo IS NULL");
            System.out.println("Baja Lógica: Registros antiguos actualizados a activo = 1.");
        } catch (Exception e) {
            System.err.println("Aviso: No se pudieron actualizar los estados 'activo'. (Quizás falten tablas).");
        }
        
        // 2. Sembrar todos los roles del Enum de forma dinámica
        for (NombreRol nombreRol : NombreRol.values()) {
            seedRol(nombreRol);
        }
        
        // Obtenemos las referencias a los roles que necesitamos para los usuarios por defecto
        Rol adminRol = rolRepository.findByNombreRol(NombreRol.ADMIN).get();
        Rol userRol = rolRepository.findByNombreRol(NombreRol.USER).get();

        // Corregir usuarios existentes sin rol o con id_rol = 0
        usuarioRepository.fixUsuariosSinRol(userRol.getIdRol());

        // Sembrar usuario administrador
        String adminEmail = "admin@admin";
        Optional<Usuario> adminExistente = usuarioRepository.findByEmail(adminEmail);
        if (adminExistente.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(adminEmail);
            // Encriptar contraseña usando BCrypt (coincidiendo con la lógica hashpw de
            // UsuarioService)
            String passwordEncriptado = BCrypt.hashpw("Admin123", BCrypt.gensalt());
            admin.setContrasenia(passwordEncriptado);
            admin.setRol(adminRol);
            admin.setActivo(true);
            admin.setFechaAlta(LocalDateTime.now()); 
            
            usuarioRepository.save(admin);
            System.out.println("Usuario ADMIN creado exitosamente (admin@admin / Admin123).");
        } else {
            System.out.println("Usuario ADMIN ya existe.");
        }

        // Sembrar usuario Consumidor Final
        String consumidorEmail = "consumidor@final";
        Optional<Usuario> consumidorExistente = usuarioRepository.findByEmail(consumidorEmail);
        if (consumidorExistente.isEmpty()) {
            Usuario consumidor = new Usuario();
            consumidor.setNombre("Consumidor Final");
            consumidor.setEmail(consumidorEmail);
            String passwordEncriptado = BCrypt.hashpw("Consumidor123", BCrypt.gensalt());
            consumidor.setContrasenia(passwordEncriptado);
            consumidor.setRol(userRol);
            consumidor.setActivo(true);
            consumidor.setFechaAlta(LocalDateTime.now()); 
            
            usuarioRepository.save(consumidor);
            System.out.println("Usuario Consumidor Final creado exitosamente.");
        } else {
            System.out.println("Usuario Consumidor Final ya existe.");
        }

        // Sembrar métodos de pago
        seedMetodoPago("EFECTIVO");
        seedMetodoPago("TRANSFERENCIA");
    }

    private Rol seedRol(NombreRol nombreRol) {
        return rolRepository.findByNombreRol(nombreRol)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol(nombreRol);
                    Rol guardado = rolRepository.save(nuevoRol);
                    System.out.println("Rol '" + nombreRol.name() + "' inicializado.");
                    return guardado;
                });
    }

    private void seedMetodoPago(String nombre) {
        if (metodoPagoRepository.findByNombre(nombre).isEmpty()) {
            MetodoPago mp = new MetodoPago();
            mp.setNombre(nombre);
            mp.setActivo(true);
            metodoPagoRepository.save(mp);
            System.out.println("Método de pago '" + nombre + "' creado exitosamente.");
        }
    }
}