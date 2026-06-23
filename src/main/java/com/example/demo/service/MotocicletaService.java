package com.example.demo.service;

import com.example.demo.dto.MotocicletaRequestDTO;
import com.example.demo.dto.MotocicletaResponseDTO;
import com.example.demo.model.Motocicleta;
import com.example.demo.model.Modelo;
import com.example.demo.model.Usuario;
import com.example.demo.repository.MotocicletaRepository;
import com.example.demo.repository.ModeloRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotocicletaService {

    private final MotocicletaRepository motocicletaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModeloRepository modeloRepository;

    public MotocicletaService(MotocicletaRepository motocicletaRepository, 
                              UsuarioRepository usuarioRepository, 
                              ModeloRepository modeloRepository) {
        this.motocicletaRepository = motocicletaRepository;
        this.usuarioRepository = usuarioRepository;
        this.modeloRepository = modeloRepository;
    }

    //ALTA
    public MotocicletaResponseDTO crearMotocicleta(MotocicletaRequestDTO requestDTO) {
        Modelo modelo = modeloRepository.findById(requestDTO.getIdModelo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado con id: " + requestDTO.getIdModelo()));

        // Validar que el modelo pertenezca a la marca especificada
        if (modelo.getMarca() == null || !modelo.getMarca().getId().equals(requestDTO.getIdMarca())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El modelo especificado no pertenece a la marca seleccionada.");
        }

        Motocicleta motocicleta = new Motocicleta();
        motocicleta.setModeloEntity(modelo);
        motocicleta.setPatente(requestDTO.getPatente());
        motocicleta.setDni(requestDTO.getDni());
        motocicleta.setActivo(true);

        Usuario usuario;
        if (requestDTO.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        } else {
            usuario = usuarioRepository.findByEmail("consumidor@final")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario Consumidor Final no configurado"));
        }
        motocicleta.setUsuario(usuario);

        Motocicleta guardada = motocicletaRepository.save(motocicleta);
        return mapToResponseDTO(guardada);
    }

    //LISTAR
    public List<MotocicletaResponseDTO> obtenerTodas() {
        return motocicletaRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // OBTENER POR ID USUARIO
    public List<MotocicletaResponseDTO> obtenerPorUsuario(Long idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return motocicletaRepository.findByUsuarioIdOrderByIdDesc(idUsuario).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //OBTENER
    public MotocicletaResponseDTO obtenerPorId(Long id) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motocicleta no encontrada"));
        return mapToResponseDTO(motocicleta);
    }

    // OBTENER POR PATENTE
    public MotocicletaResponseDTO obtenerPorPatente(String patente) {
        Motocicleta motocicleta = motocicletaRepository.findByPatente(patente)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motocicleta no encontrada"));
        return mapToResponseDTO(motocicleta);
    }

    // EDITAR
    public MotocicletaResponseDTO actualizarMotocicleta(Long id, MotocicletaRequestDTO requestDTO) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motocicleta no encontrada"));

        Modelo modelo = modeloRepository.findById(requestDTO.getIdModelo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado con id: " + requestDTO.getIdModelo()));

        // Validar que el modelo pertenezca a la marca especificada
        if (modelo.getMarca() == null || !modelo.getMarca().getId().equals(requestDTO.getIdMarca())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El modelo especificado no pertenece a la marca seleccionada.");
        }

        motocicleta.setModeloEntity(modelo);
        motocicleta.setPatente(requestDTO.getPatente());
        motocicleta.setDni(requestDTO.getDni());

        Usuario usuario;
        if (requestDTO.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        } else {
            usuario = usuarioRepository.findByEmail("consumidor@final")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario Consumidor Final no configurado"));
        }
        motocicleta.setUsuario(usuario);

        Motocicleta actualizada = motocicletaRepository.save(motocicleta);
        return mapToResponseDTO(actualizada);
    }

    //BAJA
    public void eliminarMotocicleta(Long id) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motocicleta no encontrada"));
        motocicleta.setActivo(false);
        motocicletaRepository.save(motocicleta);
    }

    // DTO DE RESPUESTA
    private MotocicletaResponseDTO mapToResponseDTO(Motocicleta motocicleta) {
        MotocicletaResponseDTO dto = new MotocicletaResponseDTO();
        dto.setId(motocicleta.getId());
        dto.setMarca(motocicleta.getMarca());
        dto.setModelo(motocicleta.getModelo());
        dto.setPatente(motocicleta.getPatente());
        dto.setDni(motocicleta.getDni());
        dto.setDniUsuario(motocicleta.getDni());

        if (motocicleta.getModeloEntity() != null) {
            dto.setIdModelo(motocicleta.getModeloEntity().getId());
            dto.setAnio(motocicleta.getModeloEntity().getAnio());
            if (motocicleta.getModeloEntity().getMarca() != null) {
                dto.setIdMarca(motocicleta.getModeloEntity().getMarca().getId());
            }
        }

        if (motocicleta.getUsuario() != null) {
            dto.setIdUsuario(motocicleta.getUsuario().getId());
            dto.setNombreUsuario(motocicleta.getUsuario().getNombre());
        }

        return dto;
    }
}
