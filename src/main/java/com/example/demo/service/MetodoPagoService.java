package com.example.demo.service;

import com.example.demo.dto.MetodoPagoDTO;
import com.example.demo.model.MetodoPago;
import com.example.demo.repository.MetodoPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetodoPagoService {

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    public List<MetodoPagoDTO> findAll() {
        return metodoPagoRepository.findAll().stream()
                .filter(m -> Boolean.TRUE.equals(m.getActivo()))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public MetodoPagoDTO findById(Long id) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
        return convertToDTO(metodoPago);
    }

    public MetodoPagoDTO save(MetodoPagoDTO metodoPagoDTO) {
        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setNombre(metodoPagoDTO.getNombre());
        metodoPago.setActivo(true);
        
        MetodoPago saved = metodoPagoRepository.save(metodoPago);
        return convertToDTO(saved);
    }

    public MetodoPagoDTO update(Long id, MetodoPagoDTO metodoPagoDTO) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
        metodoPago.setNombre(metodoPagoDTO.getNombre());
        MetodoPago saved = metodoPagoRepository.save(metodoPago);
        return convertToDTO(saved);
    }

    public void delete(Long id) {
        MetodoPago metodoPago = metodoPagoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
        metodoPago.setActivo(false);
        metodoPagoRepository.save(metodoPago);
    }

    private MetodoPagoDTO convertToDTO(MetodoPago metodoPago) {
        MetodoPagoDTO dto = new MetodoPagoDTO();
        dto.setId(metodoPago.getId());
        dto.setNombre(metodoPago.getNombre());
        return dto;
    }
}
