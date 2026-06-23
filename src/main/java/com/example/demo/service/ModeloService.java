package com.example.demo.service;

import com.example.demo.dto.ModeloRequestDTO;
import com.example.demo.dto.ModeloResponseDTO;
import com.example.demo.model.Marca;
import com.example.demo.model.Modelo;
import com.example.demo.repository.MarcaRepository;
import com.example.demo.repository.ModeloRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModeloService {

    private final ModeloRepository modeloRepository;
    private final MarcaRepository marcaRepository;

    public ModeloService(ModeloRepository modeloRepository, MarcaRepository marcaRepository) {
        this.modeloRepository = modeloRepository;
        this.marcaRepository = marcaRepository;
    }

    public ModeloResponseDTO crearModelo(ModeloRequestDTO requestDTO) {
        Marca marca = marcaRepository.findById(requestDTO.getIdMarca())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada con id: " + requestDTO.getIdMarca()));

        Modelo modelo = new Modelo();
        modelo.setNombre(requestDTO.getNombre());
        modelo.setAnio(requestDTO.getAnio());
        modelo.setMarca(marca);

        Modelo guardado = modeloRepository.save(modelo);
        return mapToResponseDTO(guardado);
    }

    public List<ModeloResponseDTO> obtenerTodos() {
        return modeloRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ModeloResponseDTO> obtenerPorMarca(Long idMarca) {
        if (!marcaRepository.existsById(idMarca)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada con id: " + idMarca);
        }
        return modeloRepository.findByMarcaId(idMarca).stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ModeloResponseDTO obtenerPorId(Long id) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado con id: " + id));
        return mapToResponseDTO(modelo);
    }

    public ModeloResponseDTO actualizarModelo(Long id, ModeloRequestDTO requestDTO) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado con id: " + id));

        Marca marca = marcaRepository.findById(requestDTO.getIdMarca())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada con id: " + requestDTO.getIdMarca()));

        modelo.setNombre(requestDTO.getNombre());
        modelo.setAnio(requestDTO.getAnio());
        modelo.setMarca(marca);

        Modelo actualizado = modeloRepository.save(modelo);
        return mapToResponseDTO(actualizado);
    }

    public void eliminarModelo(Long id) {
        Modelo modelo = modeloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Modelo no encontrado con id: " + id));
        modeloRepository.delete(modelo);
    }

    private ModeloResponseDTO mapToResponseDTO(Modelo modelo) {
        ModeloResponseDTO dto = new ModeloResponseDTO();
        dto.setId(modelo.getId());
        dto.setNombre(modelo.getNombre());
        dto.setAnio(modelo.getAnio());
        dto.setIdMarca(modelo.getMarca().getId());
        dto.setNombreMarca(modelo.getMarca().getNombre());
        return dto;
    }
}
