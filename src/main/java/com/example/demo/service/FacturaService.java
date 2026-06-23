package com.example.demo.service;

import com.example.demo.dto.DetalleFacturaDTO;
import com.example.demo.dto.DetalleFacturaServicioDTO;
import com.example.demo.dto.FacturaDTO;
import com.example.demo.model.DetalleFactura;
import com.example.demo.model.DetalleFacturaServicio;
import com.example.demo.model.EstadoFactura;
import com.example.demo.model.Factura;
import com.example.demo.model.MetodoPago;
import com.example.demo.model.Orden;
import com.example.demo.model.Producto;
import com.example.demo.model.Servicio;
import com.example.demo.model.Usuario;
import com.example.demo.repository.FacturaRepository;
import com.example.demo.repository.MetodoPagoRepository;
import com.example.demo.repository.OrdenRepository;
import com.example.demo.repository.ProductoRepository;
import com.example.demo.repository.ServicioRepository;
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

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Transactional
    public FacturaDTO crearFactura(FacturaDTO facturaDTO) {
        Factura factura = new Factura();
        
        Usuario usuario;
        if (facturaDTO.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(facturaDTO.getIdUsuario())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        } else {
            usuario = usuarioRepository.findByEmail("consumidor@final")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario Consumidor Final no configurado"));
        }
        factura.setUsuario(usuario);
        
        MetodoPago metodoPago = metodoPagoRepository.findById(facturaDTO.getIdMetodoPago())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
        factura.setMetodoPago(metodoPago);

        if (facturaDTO.getIdOrden() != null) {
            Orden orden = ordenRepository.findById(facturaDTO.getIdOrden())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));
            factura.setOrden(orden);
        }
        
        factura.setFecha(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE);
        
        double total = 0.0;
        
        List<DetalleFactura> detalles = new ArrayList<>();
        if (facturaDTO.getDetalles() != null && !facturaDTO.getDetalles().isEmpty()) {
            for (DetalleFacturaDTO detDTO : facturaDTO.getDetalles()) {
                Producto producto = productoRepository.findById(detDTO.getIdProducto())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
                
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

        List<DetalleFacturaServicio> detallesServicios = new ArrayList<>();
        if (facturaDTO.getDetallesServicios() != null && !facturaDTO.getDetallesServicios().isEmpty()) {
            for (DetalleFacturaServicioDTO detServDTO : facturaDTO.getDetallesServicios()) {
                Servicio servicio = servicioRepository.findById(detServDTO.getIdServicio())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));
                
                DetalleFacturaServicio detalle = new DetalleFacturaServicio();
                detalle.setFactura(factura);
                detalle.setServicio(servicio);
                
                double precio = servicio.getPrecioBase();
                detalle.setPrecioUnitario(precio);
                detalle.setSubtotal(precio);
                
                total += precio;
                detallesServicios.add(detalle);
            }
        }
        factura.setDetallesServicios(detallesServicios);
        
        if (detalles.isEmpty() && detallesServicios.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La factura debe tener al menos un producto o un servicio");
        }

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

    public List<FacturaDTO> findByUsuarioId(Long usuarioId) {
        return facturaRepository.findByUsuarioId(usuarioId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<com.example.demo.dto.ProductoCompradoDTO> obtenerProductosCompradosPorUsuario(Long usuarioId) {
        List<Factura> facturas = facturaRepository.findByUsuarioId(usuarioId);
        List<com.example.demo.dto.ProductoCompradoDTO> productosComprados = new ArrayList<>();

        for (Factura factura : facturas) {
            if (factura.getEstado() != EstadoFactura.CANCELADA) {
                if (factura.getDetalles() != null) {
                    for (DetalleFactura detalle : factura.getDetalles()) {
                        com.example.demo.dto.ProductoCompradoDTO dto = new com.example.demo.dto.ProductoCompradoDTO();
                        dto.setIdFactura(factura.getId());
                        dto.setIdProducto(detalle.getProducto().getId());
                        dto.setNombreProducto(detalle.getProducto().getNombre());
                        dto.setFechaCompra(factura.getFecha());
                        dto.setCantidad(detalle.getCantidad());
                        dto.setPrecioUnitario(detalle.getPrecioUnitario());
                        dto.setSubtotal(detalle.getSubtotal());
                        productosComprados.add(dto);
                    }
                }
            }
        }
        
        productosComprados.sort((p1, p2) -> p2.getFechaCompra().compareTo(p1.getFechaCompra()));
        return productosComprados;
    }

    @Transactional
    public FacturaDTO actualizarEstado(Long id, String nuevoEstadoStr) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada"));

        EstadoFactura nuevoEstado;
        try {
            nuevoEstado = EstadoFactura.valueOf(nuevoEstadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de factura no válido: " + nuevoEstadoStr);
        }

        // Si cambia a CANCELADA y el estado anterior no era CANCELADA, devolvemos el stock
        if (nuevoEstado == EstadoFactura.CANCELADA && factura.getEstado() != EstadoFactura.CANCELADA) {
            if (factura.getDetalles() != null) {
                for (DetalleFactura detalle : factura.getDetalles()) {
                    Producto producto = detalle.getProducto();
                    producto.setStock(producto.getStock() + detalle.getCantidad());
                    productoRepository.save(producto);
                }
            }
        }

        // Por consistencia, prohibimos reactivar una factura cancelada
        if (factura.getEstado() == EstadoFactura.CANCELADA && nuevoEstado != EstadoFactura.CANCELADA) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede reactivar una factura que ya ha sido cancelada");
        }

        factura.setEstado(nuevoEstado);
        Factura saved = facturaRepository.save(factura);
        return convertToDTO(saved);
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
        
        if (factura.getOrden() != null) {
            dto.setIdOrden(factura.getOrden().getId());
        }

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

        if (factura.getDetallesServicios() != null) {
            List<DetalleFacturaServicioDTO> detallesServDTO = factura.getDetallesServicios().stream().map(det -> {
                DetalleFacturaServicioDTO dDto = new DetalleFacturaServicioDTO();
                dDto.setId(det.getId());
                dDto.setIdServicio(det.getServicio().getId());
                dDto.setNombreServicio(det.getServicio().getNombre());
                dDto.setPrecioUnitario(det.getPrecioUnitario());
                dDto.setSubtotal(det.getSubtotal());
                return dDto;
            }).collect(Collectors.toList());
            dto.setDetallesServicios(detallesServDTO);
        }
        
        return dto;
    }
}
