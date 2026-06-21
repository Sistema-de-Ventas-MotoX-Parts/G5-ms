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

    public MotocicletaResponseDTO crearMotocicleta(MotocicletaRequestDTO requestDTO) {
        Motocicleta motocicleta = new Motocicleta();
        motocicleta.setMarca(requestDTO.getMarca());
        motocicleta.setModelo(requestDTO.getModelo());
        motocicleta.setPatente(requestDTO.getPatente());

        if (requestDTO.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + requestDTO.getIdUsuario()));
            motocicleta.setUsuario(usuario);
        }

        Motocicleta guardada = motocicletaRepository.save(motocicleta);
        return mapToResponseDTO(guardada);
    }

    public List<MotocicletaResponseDTO> obtenerTodas() {
        return motocicletaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public MotocicletaResponseDTO obtenerPorId(Long id) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motocicleta no encontrada con id: " + id));
        return mapToResponseDTO(motocicleta);
    }

    public MotocicletaResponseDTO actualizarMotocicleta(Long id, MotocicletaRequestDTO requestDTO) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motocicleta no encontrada con id: " + id));

        motocicleta.setMarca(requestDTO.getMarca());
        motocicleta.setModelo(requestDTO.getModelo());
        motocicleta.setPatente(requestDTO.getPatente());

        if (requestDTO.getIdUsuario() != null) {
            Usuario usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + requestDTO.getIdUsuario()));
            motocicleta.setUsuario(usuario);
        } else {
            motocicleta.setUsuario(null);
        }

        Motocicleta actualizada = motocicletaRepository.save(motocicleta);
        return mapToResponseDTO(actualizada);
    }

    public void eliminarMotocicleta(Long id) {
        Motocicleta motocicleta = motocicletaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Motocicleta no encontrada con id: " + id));
        motocicletaRepository.delete(motocicleta);
    }

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
