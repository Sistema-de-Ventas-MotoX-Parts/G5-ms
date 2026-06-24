package com.example.demo.service;

import com.example.demo.dto.MarcaRequestDTO;
import com.example.demo.dto.MarcaResponseDTO;
import com.example.demo.model.Marca;
import com.example.demo.repository.MarcaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaService(MarcaRepository marcaRepository) {
        this.marcaRepository = marcaRepository;
    }

    public MarcaResponseDTO crearMarca(MarcaRequestDTO requestDTO) {
        if (marcaRepository.findByNombre(requestDTO.getNombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La marca ya existe: " + requestDTO.getNombre());
        }
        Marca marca = new Marca(requestDTO.getNombre());
        Marca guardada = marcaRepository.save(marca);
        return mapToResponseDTO(guardada);
    }

    public List<MarcaResponseDTO> obtenerTodas() {
        return marcaRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public MarcaResponseDTO obtenerPorId(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada con id: " + id));
        return mapToResponseDTO(marca);
    }

    public MarcaResponseDTO actualizarMarca(Long id, MarcaRequestDTO requestDTO) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada con id: " + id));

        marcaRepository.findByNombre(requestDTO.getNombre()).ifPresent(existente -> {
            if (!existente.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La marca con nombre " + requestDTO.getNombre() + " ya existe.");
            }
        });

        marca.setNombre(requestDTO.getNombre());
        Marca actualizada = marcaRepository.save(marca);
        return mapToResponseDTO(actualizada);
    }

    public void eliminarMarca(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada con id: " + id));
        marcaRepository.delete(marca);
    }

    private MarcaResponseDTO mapToResponseDTO(Marca marca) {
        MarcaResponseDTO dto = new MarcaResponseDTO();
        dto.setId(marca.getId());
        dto.setNombre(marca.getNombre());
        return dto;
    }
}
