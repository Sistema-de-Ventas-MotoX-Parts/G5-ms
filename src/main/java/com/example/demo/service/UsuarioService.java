package com.example.demo.service;

import com.example.demo.model.NombreRol;
import com.example.demo.model.Rol;
import com.example.demo.model.Usuario;
import com.example.demo.repository.RolRepository;
import com.example.demo.repository.UsuarioRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    public Usuario registrar(Usuario usuario) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        String passwordEncriptado = BCrypt.hashpw(usuario.getContrasenia(), BCrypt.gensalt());
        usuario.setContrasenia(passwordEncriptado);

        Rol userRol = rolRepository.findByNombreRol(NombreRol.USER)
                .orElseThrow(() -> new IllegalStateException("El rol USER no está inicializado."));
        usuario.setRol(userRol);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario login(String email, String contrasenia) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Correo electrónico o contraseña incorrectos."));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new IllegalArgumentException("Su cuenta ha sido desactivada. Por favor contacte al soporte.");
        }

        if (!BCrypt.checkpw(contrasenia, usuario.getContrasenia())) {
            throw new IllegalArgumentException("Correo electrónico o contraseña incorrectos.");
        }

        return usuario;
    }

    public Usuario crearUsuario(Usuario usuario) {
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio.");
        }
        if (usuario.getContrasenia() == null || usuario.getContrasenia().length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
        }
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        String passwordEncriptado = BCrypt.hashpw(usuario.getContrasenia(), BCrypt.gensalt());
        usuario.setContrasenia(passwordEncriptado);

        if (usuario.getRol() == null) {
            Rol userRol = rolRepository.findByNombreRol(NombreRol.USER)
                    .orElseThrow(() -> new IllegalStateException("El rol USER no está inicializado."));
            usuario.setRol(userRol);
        } else {
            Rol rolDb = rolRepository.findByNombreRol(usuario.getRol().getNombreRol())
                    .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe."));
            usuario.setRol(rolDb);
        }

        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (usuarioActualizado.getNombre() != null && !usuarioActualizado.getNombre().isBlank()) {
            usuario.setNombre(usuarioActualizado.getNombre());
        }

        if (usuarioActualizado.getEmail() != null && !usuarioActualizado.getEmail().isBlank()) {
            if (!usuarioActualizado.getEmail().equals(usuario.getEmail())) {
                Optional<Usuario> emailExistente = usuarioRepository.findByEmail(usuarioActualizado.getEmail());
                if (emailExistente.isPresent()) {
                    throw new IllegalArgumentException("El correo electrónico ya está en uso por otro usuario.");
                }
                usuario.setEmail(usuarioActualizado.getEmail());
            }
        }

        if (usuarioActualizado.getContrasenia() != null && !usuarioActualizado.getContrasenia().isBlank()) {
            if (usuarioActualizado.getContrasenia().length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres.");
            }
            String passwordEncriptado = BCrypt.hashpw(usuarioActualizado.getContrasenia(), BCrypt.gensalt());
            usuario.setContrasenia(passwordEncriptado);
        }

        if (usuarioActualizado.getRol() != null && usuarioActualizado.getRol().getNombreRol() != null) {
            Rol rolDb = rolRepository.findByNombreRol(usuarioActualizado.getRol().getNombreRol())
                    .orElseThrow(() -> new IllegalArgumentException("El rol especificado no existe."));
            usuario.setRol(rolDb);
        }

        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarMiPerfil(Long id, com.example.demo.dto.PerfilActualizarDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        if (dto.getNombre() != null && !dto.getNombre().isBlank()) {
            usuario.setNombre(dto.getNombre());
        }

        if (dto.getNuevaContrasenia() != null && !dto.getNuevaContrasenia().isBlank()) {
            if (dto.getContraseniaActual() == null || dto.getContraseniaActual().isBlank()) {
                throw new IllegalArgumentException("Debe proporcionar su contraseña actual para cambiarla.");
            }
            if (!BCrypt.checkpw(dto.getContraseniaActual(), usuario.getContrasenia())) {
                throw new IllegalArgumentException("La contraseña actual es incorrecta.");
            }
            if (dto.getNuevaContrasenia().length() < 6) {
                throw new IllegalArgumentException("La nueva contraseña debe tener al menos 6 caracteres.");
            }
            String passwordEncriptado = BCrypt.hashpw(dto.getNuevaContrasenia(), BCrypt.gensalt());
            usuario.setContrasenia(passwordEncriptado);
        }

        return usuarioRepository.save(usuario);
    }

    public void eliminarUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        usuario.setActivo(false);
        usuario.setEmail(usuario.getEmail() + "_eliminado_" + System.currentTimeMillis());
        usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .collect(java.util.stream.Collectors.toList());
    }

    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }
}
