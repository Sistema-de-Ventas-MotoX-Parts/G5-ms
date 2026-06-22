package com.example.demo.service;

import com.example.demo.dto.ServicioRequestDTO;
import com.example.demo.dto.ServicioResponseDTO;
import com.example.demo.model.Servicio;
import com.example.demo.repository.ServicioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public ServicioResponseDTO crearServicio(ServicioRequestDTO requestDTO) {
        Servicio servicio = new Servicio();
        servicio.setNombre(requestDTO.getNombre());
        servicio.setDescripcion(requestDTO.getDescripcion());
        servicio.setPrecioBase(requestDTO.getPrecioBase());
        
        Servicio guardado = servicioRepository.save(servicio);
        return mapToResponseDTO(guardado);
    }

    public List<ServicioResponseDTO> obtenerTodos() {
        return servicioRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public ServicioResponseDTO obtenerPorId(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        return mapToResponseDTO(servicio);
    }

    public ServicioResponseDTO actualizarServicio(Long id, ServicioRequestDTO requestDTO) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        
        servicio.setNombre(requestDTO.getNombre());
        servicio.setDescripcion(requestDTO.getDescripcion());
        servicio.setPrecioBase(requestDTO.getPrecioBase());
        
        Servicio actualizado = servicioRepository.save(servicio);
        return mapToResponseDTO(actualizado);
    }

    public void eliminarServicio(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
        servicioRepository.delete(servicio);
    }

    private ServicioResponseDTO mapToResponseDTO(Servicio servicio) {
        ServicioResponseDTO dto = new ServicioResponseDTO();
        dto.setId(servicio.getId());
        dto.setNombre(servicio.getNombre());
        dto.setDescripcion(servicio.getDescripcion());
        dto.setPrecioBase(servicio.getPrecioBase());
        return dto;
    }
}
