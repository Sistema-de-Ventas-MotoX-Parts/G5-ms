package com.example.demo.service;

import com.example.demo.dto.FacturaDTO;
import com.example.demo.dto.DetalleFacturaDTO;
import com.example.demo.dto.DetalleFacturaServicioDTO;
import com.example.demo.dto.OrdenRequestDTO;
import com.example.demo.dto.OrdenResponseDTO;
import com.example.demo.dto.OrdenServicioRequestDTO;
import com.example.demo.dto.OrdenServicioResponseDTO;
import com.example.demo.dto.OrdenProductoRequestDTO;
import com.example.demo.dto.OrdenProductoResponseDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
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
public class OrdenService {

    @Autowired
    private OrdenRepository ordenRepository;

    @Autowired
    private MotocicletaRepository motocicletaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private OrdenServicioRepository ordenServicioRepository;

    @Transactional
    public OrdenResponseDTO crearOrden(OrdenRequestDTO requestDTO) {
        // 1. Validar motocicleta
        Motocicleta motocicleta = motocicletaRepository.findById(requestDTO.getIdMoto())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motocicleta no encontrada con id: " + requestDTO.getIdMoto()));

        // 2. Determinar el cliente (usuario)
        Usuario usuario = null;
        if (requestDTO.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con id: " + requestDTO.getIdUsuario()));
        } else if (motocicleta.getUsuario() != null) {
            usuario = motocicleta.getUsuario();
        } else {
            usuario = usuarioRepository.findByEmail("consumidor@final")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La motocicleta no tiene un usuario asociado y el usuario de fallback 'consumidor@final' no fue encontrado."));
        }

        // 3. Validar método de pago para la factura
        MetodoPago metodoPago = null;
        if (requestDTO.getIdMetodoPago() != null) {
            metodoPago = metodoPagoRepository.findById(requestDTO.getIdMetodoPago())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado con id: " + requestDTO.getIdMetodoPago()));
        } else {
            metodoPago = metodoPagoRepository.findByNombre("EFECTIVO")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se especificó método de pago y el método por defecto 'EFECTIVO' no fue encontrado."));
        }

        // 4. Crear la orden
        Orden orden = new Orden();
        orden.setMotocicleta(motocicleta);
        orden.setFechaIngreso(LocalDateTime.now());
        orden.setTelefonoContacto(requestDTO.getTelefonoContacto());
        orden.setEstado(requestDTO.getEstado());
        orden.setNotas(requestDTO.getNotas());

        // Guardar orden inicial
        Orden ordenGuardada = ordenRepository.save(orden);

        double totalFactura = 0.0;

        // 5. Procesar servicios
        List<OrdenServicio> ordenServicios = new ArrayList<>();
        List<DetalleFacturaServicio> detallesServiciosFactura = new ArrayList<>();
        
        Factura factura = new Factura();
        factura.setUsuario(usuario);
        factura.setMetodoPago(metodoPago);
        factura.setOrden(ordenGuardada);
        factura.setFecha(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE);

        if (requestDTO.getServicios() != null) {
            for (OrdenServicioRequestDTO servReq : requestDTO.getServicios()) {
                Servicio servicio = servicioRepository.findById(servReq.getIdServicio())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado con id: " + servReq.getIdServicio()));

                OrdenServicio ordenServicio = new OrdenServicio();
                ordenServicio.setOrden(ordenGuardada);
                ordenServicio.setServicio(servicio);

                double precioAcordado = servReq.getPrecioAcordado() != null ? servReq.getPrecioAcordado() : servicio.getPrecioBase();
                ordenServicio.setPrecioAcordado(precioAcordado);
                ordenServicios.add(ordenServicio);

                // Crear detalle de factura para el servicio
                DetalleFacturaServicio detalleServicio = new DetalleFacturaServicio();
                detalleServicio.setFactura(factura);
                detalleServicio.setServicio(servicio);
                detalleServicio.setPrecioUnitario(precioAcordado);
                detalleServicio.setSubtotal(precioAcordado);
                detallesServiciosFactura.add(detalleServicio);

                totalFactura += precioAcordado;
            }
        }
        ordenGuardada.setServicios(ordenServicios);

        // 6. Procesar productos
        List<Producto> productosOrden = new ArrayList<>();
        List<DetalleFactura> detallesProductosFactura = new ArrayList<>();

        if (requestDTO.getProductos() != null) {
            for (OrdenProductoRequestDTO prodReq : requestDTO.getProductos()) {
                Producto producto = productoRepository.findById(prodReq.getIdProducto())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + prodReq.getIdProducto()));

                int cantidad = prodReq.getCantidad() != null ? prodReq.getCantidad() : 1;

                if (producto.getStock() < cantidad) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + producto.getNombre());
                }

                // Descontar stock
                producto.setStock(producto.getStock() - cantidad);
                productoRepository.save(producto);

                // Agregar a productos de la orden (como ManyToMany, agregamos el producto)
                productosOrden.add(producto);

                // Crear detalle de factura para el producto
                DetalleFactura detalleFactura = new DetalleFactura();
                detalleFactura.setFactura(factura);
                detalleFactura.setProducto(producto);
                detalleFactura.setCantidad(cantidad);
                detalleFactura.setPrecioUnitario(producto.getPrecio());
                
                double subtotal = producto.getPrecio() * cantidad;
                detalleFactura.setSubtotal(subtotal);
                detallesProductosFactura.add(detalleFactura);

                totalFactura += subtotal;
            }
        }
        ordenGuardada.setProductos(productosOrden);

        // Guardar la orden actualizada
        ordenGuardada = ordenRepository.save(ordenGuardada);

        // 7. Configurar y guardar la factura
        factura.setDetalles(detallesProductosFactura);
        factura.setDetallesServicios(detallesServiciosFactura);
        factura.setTotal(totalFactura);

        facturaRepository.save(factura);

        // Volver a cargar para traer facturas mapeadas correctamente
        return obtenerPorId(ordenGuardada.getId());
    }

    public List<OrdenResponseDTO> obtenerTodas() {
        return ordenRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public OrdenResponseDTO obtenerPorId(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada con id: " + id));
        return convertToResponseDTO(orden);
    }

    @Transactional
    public OrdenResponseDTO actualizarOrden(Long id, OrdenRequestDTO requestDTO) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada con id: " + id));

        // Actualizar motocicleta si cambia
        if (!orden.getMotocicleta().getId().equals(requestDTO.getIdMoto())) {
            Motocicleta motocicleta = motocicletaRepository.findById(requestDTO.getIdMoto())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motocicleta no encontrada con id: " + requestDTO.getIdMoto()));
            orden.setMotocicleta(motocicleta);
        }

        orden.setTelefonoContacto(requestDTO.getTelefonoContacto());
        orden.setEstado(requestDTO.getEstado());
        orden.setNotas(requestDTO.getNotas());

        // Si se actualizan servicios y productos, recalculamos la factura pendiente si existe
        // Para simplificar y mantener la consistencia: si hay una factura PENDIENTE asociada, la actualizamos
        Factura factura = facturaRepository.findAll().stream()
                .filter(f -> f.getOrden() != null && f.getOrden().getId().equals(id) && f.getEstado() == EstadoFactura.PENDIENTE)
                .findFirst()
                .orElse(null);

        if (factura != null) {
            // Devolver stock de productos anteriores
            for (DetalleFactura detalle : factura.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }

            // Limpiar detalles anteriores
            factura.getDetalles().clear();
            factura.getDetallesServicios().clear();

            double totalFactura = 0.0;

            // Procesar nuevos servicios
            // Limpiamos la colección existente y le agregamos los nuevos elementos
            // para evitar reemplazar la referencia de la colección gestionada por Hibernate (orphanRemoval = true)
            orden.getServicios().clear();

            if (requestDTO.getServicios() != null) {
                for (OrdenServicioRequestDTO servReq : requestDTO.getServicios()) {
                    Servicio servicio = servicioRepository.findById(servReq.getIdServicio())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado con id: " + servReq.getIdServicio()));

                    OrdenServicio ordenServicio = new OrdenServicio();
                    ordenServicio.setOrden(orden);
                    ordenServicio.setServicio(servicio);

                    double precioAcordado = servReq.getPrecioAcordado() != null ? servReq.getPrecioAcordado() : servicio.getPrecioBase();
                    ordenServicio.setPrecioAcordado(precioAcordado);
                    orden.getServicios().add(ordenServicio);

                    DetalleFacturaServicio detalleServicio = new DetalleFacturaServicio();
                    detalleServicio.setFactura(factura);
                    detalleServicio.setServicio(servicio);
                    detalleServicio.setPrecioUnitario(precioAcordado);
                    detalleServicio.setSubtotal(precioAcordado);
                    factura.getDetallesServicios().add(detalleServicio);

                    totalFactura += precioAcordado;
                }
            }

            // Procesar nuevos productos
            orden.getProductos().clear();
            if (requestDTO.getProductos() != null) {
                for (OrdenProductoRequestDTO prodReq : requestDTO.getProductos()) {
                    Producto producto = productoRepository.findById(prodReq.getIdProducto())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con id: " + prodReq.getIdProducto()));

                    int cantidad = prodReq.getCantidad() != null ? prodReq.getCantidad() : 1;

                    if (producto.getStock() < cantidad) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + producto.getNombre());
                    }

                    // Descontar stock
                    producto.setStock(producto.getStock() - cantidad);
                    productoRepository.save(producto);

                    orden.getProductos().add(producto);

                    DetalleFactura detalleFactura = new DetalleFactura();
                    detalleFactura.setFactura(factura);
                    detalleFactura.setProducto(producto);
                    detalleFactura.setCantidad(cantidad);
                    detalleFactura.setPrecioUnitario(producto.getPrecio());

                    double subtotal = producto.getPrecio() * cantidad;
                    detalleFactura.setSubtotal(subtotal);
                    factura.getDetalles().add(detalleFactura);

                    totalFactura += subtotal;
                }
            }
            factura.setTotal(totalFactura);

            // Si se envió un nuevo método de pago
            if (requestDTO.getIdMetodoPago() != null) {
                MetodoPago metodoPago = metodoPagoRepository.findById(requestDTO.getIdMetodoPago())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado con id: " + requestDTO.getIdMetodoPago()));
                factura.setMetodoPago(metodoPago);
            }

            facturaRepository.save(factura);
        }

        Orden saved = ordenRepository.save(orden);
        return convertToResponseDTO(saved);
    }

    @Transactional
    public void eliminarOrden(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada con id: " + id));

        // Si se elimina la orden, devolvemos el stock de los productos asociados a la factura PENDIENTE si existe
        List<Factura> facturasAsociadas = facturaRepository.findAll().stream()
                .filter(f -> f.getOrden() != null && f.getOrden().getId().equals(id))
                .collect(Collectors.toList());

        for (Factura f : facturasAsociadas) {
            if (f.getEstado() == EstadoFactura.PENDIENTE) {
                for (DetalleFactura detalle : f.getDetalles()) {
                    Producto producto = detalle.getProducto();
                    producto.setStock(producto.getStock() + detalle.getCantidad());
                    productoRepository.save(producto);
                }
            }
        }

        ordenRepository.delete(orden);
    }

    private OrdenResponseDTO convertToResponseDTO(Orden orden) {
        OrdenResponseDTO dto = new OrdenResponseDTO();
        dto.setId(orden.getId());
        dto.setIdMoto(orden.getMotocicleta().getId());
        dto.setMarcaModeloMoto(orden.getMotocicleta().getMarca() + " " + orden.getMotocicleta().getModelo());
        dto.setPatenteMoto(orden.getMotocicleta().getPatente());
        dto.setFechaIngreso(orden.getFechaIngreso());
        dto.setTelefonoContacto(orden.getTelefonoContacto());
        dto.setEstado(orden.getEstado());
        dto.setNotas(orden.getNotas());

        // Mapear servicios
        if (orden.getServicios() != null) {
            dto.setServicios(orden.getServicios().stream().map(os -> {
                OrdenServicioResponseDTO osDto = new OrdenServicioResponseDTO();
                osDto.setIdServicio(os.getServicio().getId());
                osDto.setNombreServicio(os.getServicio().getNombre());
                osDto.setPrecioAcordado(os.getPrecioAcordado());
                return osDto;
            }).collect(Collectors.toList()));
        }

        // Mapear productos
        if (orden.getProductos() != null) {
            dto.setProductos(orden.getProductos().stream().map(p -> {
                OrdenProductoResponseDTO pDto = new OrdenProductoResponseDTO();
                pDto.setIdProducto(p.getId());
                pDto.setNombreProducto(p.getNombre());
                pDto.setPrecio(p.getPrecio());
                return pDto;
            }).collect(Collectors.toList()));
        }

        // Mapear facturas
        List<Factura> facturas = facturaRepository.findAll().stream()
                .filter(f -> f.getOrden() != null && f.getOrden().getId().equals(orden.getId()))
                .collect(Collectors.toList());

        dto.setFacturas(facturas.stream().map(f -> {
            FacturaDTO fDto = new FacturaDTO();
            fDto.setId(f.getId());
            fDto.setIdUsuario(f.getUsuario().getId());
            fDto.setNombreUsuario(f.getUsuario().getNombre());
            fDto.setFecha(f.getFecha());
            fDto.setTotal(f.getTotal());
            fDto.setEstado(f.getEstado().name());
            fDto.setIdMetodoPago(f.getMetodoPago().getId());
            fDto.setNombreMetodoPago(f.getMetodoPago().getNombre());
            fDto.setIdOrden(orden.getId());

            // Mapear detalles de productos
            if (f.getDetalles() != null) {
                fDto.setDetalles(f.getDetalles().stream().map(det -> {
                    DetalleFacturaDTO detDto = new DetalleFacturaDTO();
                    detDto.setId(det.getId());
                    detDto.setIdProducto(det.getProducto().getId());
                    detDto.setNombreProducto(det.getProducto().getNombre());
                    detDto.setCantidad(det.getCantidad());
                    detDto.setPrecioUnitario(det.getPrecioUnitario());
                    detDto.setSubtotal(det.getSubtotal());
                    return detDto;
                }).collect(Collectors.toList()));
            }

            // Mapear detalles de servicios
            if (f.getDetallesServicios() != null) {
                fDto.setDetallesServicios(f.getDetallesServicios().stream().map(detServ -> {
                    DetalleFacturaServicioDTO detServDto = new DetalleFacturaServicioDTO();
                    detServDto.setId(detServ.getId());
                    detServDto.setIdServicio(detServ.getServicio().getId());
                    detServDto.setNombreServicio(detServ.getServicio().getNombre());
                    detServDto.setPrecioUnitario(detServ.getPrecioUnitario());
                    detServDto.setSubtotal(detServ.getSubtotal());
                    return detServDto;
                }).collect(Collectors.toList()));
            }

            return fDto;
        }).collect(Collectors.toList()));

        return dto;
    }
}
