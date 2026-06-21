package com.example.demo.config;

import com.example.demo.model.NombreRol;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        // Seed Roles if they don't exist
        Rol adminRol = seedRol(NombreRol.ADMIN);
        Rol userRol = seedRol(NombreRol.USER);

        // Seed Admin User
        String adminEmail = "admin@admin";
        Optional<Usuario> adminExistente = usuarioRepository.findByEmail(adminEmail);
        if (adminExistente.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(adminEmail);
            // Encrypt password using BCrypt (matching UsuarioService hashpw logic)
            String passwordEncriptado = BCrypt.hashpw("Admin123", BCrypt.gensalt());
            admin.setContrasenia(passwordEncriptado);
            admin.setRol(adminRol);
            usuarioRepository.save(admin);
            System.out.println("Usuario ADMIN creado exitosamente (admin@admin / Admin123).");
        } else {
            System.out.println("Usuario ADMIN ya existe.");
        }
    }

    private Rol seedRol(NombreRol nombreRol) {
        return rolRepository.findByNombreRol(nombreRol)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol(nombreRol);
                    return rolRepository.save(nuevoRol);
                });
    }
}
