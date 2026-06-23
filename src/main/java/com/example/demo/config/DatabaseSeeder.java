package com.example.demo.config;

import com.example.demo.model.NombreRol;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.model.MetodoPago;
import com.example.demo.model.Marca;
import com.example.demo.model.Modelo;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.UsuarioRepository;
import com.example.demo.repository.MetodoPagoRepository;
import com.example.demo.repository.MarcaRepository;
import com.example.demo.repository.ModeloRepository;
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

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private ModeloRepository modeloRepository;

    @Override
    public void run(String... args) throws Exception {
        // Sembrar roles si no existen
        Rol adminRol = seedRol(NombreRol.ADMIN);
        Rol userRol = seedRol(NombreRol.USER);

        // Corregir usuarios existentes sin rol o con id_rol = 0
        usuarioRepository.fixUsuariosSinRol(userRol.getIdRol());

        // Sembrar usuario administrador
        String adminEmail = "admin@admin";
        Optional<Usuario> adminExistente = usuarioRepository.findByEmail(adminEmail);
        if (adminExistente.isEmpty()) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(adminEmail);
            // Encriptar contraseña usando BCrypt (coincidiendo con la lógica hashpw de UsuarioService)
            String passwordEncriptado = BCrypt.hashpw("Admin123", BCrypt.gensalt());
            admin.setContrasenia(passwordEncriptado);
            admin.setRol(adminRol);
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
            usuarioRepository.save(consumidor);
            System.out.println("Usuario Consumidor Final creado exitosamente.");
        }

        // Sembrar métodos de pago
        seedMetodoPago("EFECTIVO");
        seedMetodoPago("TRANSFERENCIA");

        // Sembrar marcas y modelos
        seedMarcasYModelos();
    }

    private Rol seedRol(NombreRol nombreRol) {
        return rolRepository.findByNombreRol(nombreRol)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol(nombreRol);
                    return rolRepository.save(nuevoRol);
                });
    }

    private void seedMetodoPago(String nombre) {
        if (metodoPagoRepository.findByNombre(nombre).isEmpty()) {
            MetodoPago mp = new MetodoPago();
            mp.setNombre(nombre);
            metodoPagoRepository.save(mp);
            System.out.println("Método de pago '" + nombre + "' creado exitosamente.");
        }
    }

    private void seedMarcasYModelos() {
        // Honda
        Marca honda = seedMarca("HONDA");
        seedModelo("CB 250 Twister", 2022, honda);
        seedModelo("XR 150L", 2023, honda);
        seedModelo("Tornado XR 250", 2021, honda);

        // Yamaha
        Marca yamaha = seedMarca("YAMAHA");
        seedModelo("FZ 25", 2021, yamaha);
        seedModelo("YBR 125 Z", 2022, yamaha);
        seedModelo("Crypton 110", 2023, yamaha);

        // Kawasaki
        Marca kawasaki = seedMarca("KAWASAKI");
        seedModelo("Ninja 400", 2023, kawasaki);
        seedModelo("Z400", 2022, kawasaki);
    }

    private Marca seedMarca(String nombre) {
        return marcaRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Marca m = new Marca(nombre);
                    return marcaRepository.save(m);
                });
    }

    private void seedModelo(String nombre, Integer anio, Marca marca) {
        boolean existe = modeloRepository.findAll().stream()
                .anyMatch(m -> m.getNombre().equalsIgnoreCase(nombre) && m.getMarca().getId().equals(marca.getId()) && m.getAnio().equals(anio));
        if (!existe) {
            Modelo m = new Modelo();
            m.setNombre(nombre);
            m.setAnio(anio);
            m.setMarca(marca);
            modeloRepository.save(m);
        }
    }
}
