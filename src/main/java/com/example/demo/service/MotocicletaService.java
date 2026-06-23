package com.example.demo.service;

import com.example.demo.dto.MotocicletaRequestDTO;
import com.example.demo.dto.MotocicletaResponseDTO;
import com.example.demo.model.Motocicleta;
import com.example.demo.model.Usuario;
import com.example.demo.repository.MotocicletaRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MotocicletaService {

    private final MotocicletaRepository motocicletaRepository;
    private final UsuarioRepository usuarioRepository;

    public MotocicletaService(MotocicletaRepository motocicletaRepository, UsuarioRepository usuarioRepository) {
        this.motocicletaRepository = motocicletaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //ALTA
    public MotocicletaResponseDTO crearMotocicleta(MotocicletaRequestDTO requestDTO) {
        Motocicleta motocicleta = new Motocicleta();
        motocicleta.setMarca(requestDTO.getMarca());
        motocicleta.setModelo(requestDTO.getModelo());
        motocicleta.setPatente(requestDTO.getPatente());
        motocicleta.setActivo(true);

        if (requestDTO.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            motocicleta.setUsuario(usuario);
        } else {
            Usuario consumidor = usuarioRepository.findByEmail("consumidor@final")
                    .orElseThrow(() -> new RuntimeException("Usuario Consumidor Final no configurado"));
            motocicleta.setUsuario(consumidor);
        }

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
            throw new RuntimeException("Usuario no encontrado");
        }
        return motocicletaRepository.findByUsuarioIdOrderByIdDesc(idUsuario).stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    //OBTENER
    public MotocicletaResponseDTO obtenerPorId(Long id) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motocicleta no encontrada"));
        return mapToResponseDTO(motocicleta);
    }

    // OBTENER POR PATENTE
    public MotocicletaResponseDTO obtenerPorPatente(String patente) {
        Motocicleta motocicleta = motocicletaRepository.findByPatente(patente)
                .orElseThrow(() -> new RuntimeException("Motocicleta no encontrada"));
        return mapToResponseDTO(motocicleta);
    }

    // EDITAR
    public MotocicletaResponseDTO actualizarMotocicleta(Long id, MotocicletaRequestDTO requestDTO) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motocicleta no encontrada"));

        motocicleta.setMarca(requestDTO.getMarca());
        motocicleta.setModelo(requestDTO.getModelo());
        motocicleta.setPatente(requestDTO.getPatente());

        if (requestDTO.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            motocicleta.setUsuario(usuario);
        } else {
            Usuario consumidor = usuarioRepository.findByEmail("consumidor@final")
                    .orElseThrow(() -> new RuntimeException("Usuario Consumidor Final no configurado"));
            motocicleta.setUsuario(consumidor);
        }

        Motocicleta actualizada = motocicletaRepository.save(motocicleta);
        return mapToResponseDTO(actualizada);
    }

    //BAJA
    public void eliminarMotocicleta(Long id) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motocicleta no encontrada"));
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

        if (motocicleta.getUsuario() != null) {
            dto.setIdUsuario(motocicleta.getUsuario().getId());
            dto.setNombreUsuario(motocicleta.getUsuario().getNombre());
        }

        return dto;
    }
}
