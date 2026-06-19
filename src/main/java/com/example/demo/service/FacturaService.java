package com.example.demo.service;

import com.example.demo.dto.DetalleFacturaDTO;
import com.example.demo.dto.FacturaDTO;
import com.example.demo.model.DetalleFactura;
import com.example.demo.model.EstadoFactura;
import com.example.demo.model.Factura;
import com.example.demo.model.MetodoPago;
import com.example.demo.model.Producto;
import com.example.demo.model.Usuario;
import com.example.demo.repository.FacturaRepository;
import com.example.demo.repository.MetodoPagoRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private MetodoPagoRepository metodoPagoRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    @Transactional
    public FacturaDTO crearFactura(FacturaDTO facturaDTO) {
        Factura factura = new Factura();
        
        Usuario usuario = usuarioRepository.findById(facturaDTO.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con id: " + facturaDTO.getIdUsuario()));
        factura.setUsuario(usuario);
        
        MetodoPago metodoPago = metodoPagoRepository.findById(facturaDTO.getIdMetodoPago())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado con id: " + facturaDTO.getIdMetodoPago()));
        factura.setMetodoPago(metodoPago);
        
        factura.setFecha(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE);
        
        double total = 0.0;
        List<DetalleFactura> detalles = new ArrayList<>();
        
        if (facturaDTO.getDetalles() != null) {
            for (DetalleFacturaDTO detDTO : facturaDTO.getDetalles()) {
                Producto producto = productoRepository.findById(detDTO.getIdProducto())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + detDTO.getIdProducto()));
                
                if (producto.getStock() < detDTO.getCantidad()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + producto.getNombre());
                }
                
                producto.setStock(producto.getStock() - detDTO.getCantidad());
                productoRepository.save(producto);
                
                DetalleFactura detalle = new DetalleFactura();
                detalle.setFactura(factura);
                detalle.setProducto(producto);
                detalle.setCantidad(detDTO.getCantidad());
                detalle.setPrecioUnitario(producto.getPrecio());
                
                double subtotal = producto.getPrecio() * detDTO.getCantidad();
                detalle.setSubtotal(subtotal);
                
                total += subtotal;
                detalles.add(detalle);
            }
        }
        
        factura.setDetalles(detalles);
        factura.setTotal(total);
        
        Factura saved = facturaRepository.save(factura);
        return convertToDTO(saved);
    }
    
    public List<FacturaDTO> findAll() {
        return facturaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public FacturaDTO findById(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));
        return convertToDTO(factura);
    }

    private FacturaDTO convertToDTO(Factura factura) {
        FacturaDTO dto = new FacturaDTO();
        dto.setId(factura.getId());
        dto.setIdUsuario(factura.getUsuario().getId());
        dto.setNombreUsuario(factura.getUsuario().getNombre());
        dto.setFecha(factura.getFecha());
        dto.setTotal(factura.getTotal());
        dto.setEstado(factura.getEstado().name());
        dto.setIdMetodoPago(factura.getMetodoPago().getId());
        dto.setNombreMetodoPago(factura.getMetodoPago().getNombre());
        
        if (factura.getDetalles() != null) {
            List<DetalleFacturaDTO> detallesDTO = factura.getDetalles().stream().map(det -> {
                DetalleFacturaDTO dDto = new DetalleFacturaDTO();
                dDto.setId(det.getId());
                dDto.setIdProducto(det.getProducto().getId());
                dDto.setNombreProducto(det.getProducto().getNombre());
                dDto.setCantidad(det.getCantidad());
                dDto.setPrecioUnitario(det.getPrecioUnitario());
                dDto.setSubtotal(det.getSubtotal());
                return dDto;
            }).collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }
        
        return dto;
    }
}
