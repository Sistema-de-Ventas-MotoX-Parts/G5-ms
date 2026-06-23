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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Motocicleta no encontrada"));

        // 2. Determinar el cliente (usuario)
        Usuario usuario = null;
        if (requestDTO.getIdUsuario() != null) {
            usuario = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        } else if (motocicleta.getUsuario() != null) {
            usuario = motocicleta.getUsuario();
        } else {
            usuario = usuarioRepository.findByEmail("consumidor@final")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La motocicleta no tiene un usuario asociado y el usuario de fallback 'consumidor@final' no fue encontrado."));
        }

        // 3. Validar método de pago preferido
        MetodoPago metodoPago = null;
        if (requestDTO.getIdMetodoPago() != null) {
            metodoPago = metodoPagoRepository.findById(requestDTO.getIdMetodoPago())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
        } else {
            metodoPago = metodoPagoRepository.findByNombre("EFECTIVO")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se especificó método de pago y el método por defecto 'EFECTIVO' no fue encontrado."));
        }

        // 4. Crear la orden
        Orden orden = new Orden();
        orden.setMotocicleta(motocicleta);
        orden.setFechaIngreso(LocalDateTime.now());
        orden.setTelefonoContacto(requestDTO.getTelefonoContacto());

        if (requestDTO.getEstado() == EstadoOrden.ENTREGADO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede crear una orden directamente en estado ENTREGADO.");
        }

        if (requestDTO.getEstado() == EstadoOrden.SERVICE_TERMINADO) {
            orden.setPin(generarPinAleatorio());
            orden.setFechaExpiracionPin(LocalDateTime.now().plusMinutes(15));
        }

        orden.setEstado(requestDTO.getEstado());
        orden.setNotas(requestDTO.getNotas());
        orden.setCliente(usuario);
        orden.setMetodoPago(metodoPago);

        // Validar y asignar mecánico si se proporciona
        if (requestDTO.getIdMecanico() != null) {
            Usuario mecanico = usuarioRepository.findById(requestDTO.getIdMecanico())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mecánico no encontrado"));
            if (!Boolean.TRUE.equals(mecanico.getActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mecánico asignado no está activo");
            }
            if (mecanico.getRol() == null || mecanico.getRol().getNombreRol() != NombreRol.MECHANIC) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario asignado no tiene el rol de mecánico");
            }
            orden.setMecanico(mecanico);
        }

        // Guardar orden inicial
        Orden ordenGuardada = ordenRepository.save(orden);

        // 5. Procesar servicios
        List<OrdenServicio> ordenServicios = new ArrayList<>();
        if (requestDTO.getServicios() != null) {
            for (OrdenServicioRequestDTO servReq : requestDTO.getServicios()) {
                Servicio servicio = servicioRepository.findById(servReq.getIdServicio())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

                OrdenServicio ordenServicio = new OrdenServicio();
                ordenServicio.setOrden(ordenGuardada);
                ordenServicio.setServicio(servicio);

                double precioAcordado = servReq.getPrecioAcordado() != null ? servReq.getPrecioAcordado() : servicio.getPrecioBase();
                ordenServicio.setPrecioAcordado(precioAcordado);
                ordenServicios.add(ordenServicio);
            }
        }
        ordenGuardada.setServicios(ordenServicios);

        // 6. Procesar productos
        List<OrdenProducto> productosOrden = new ArrayList<>();
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

                // Agregar a productos de la orden
                OrdenProducto ordenProducto = new OrdenProducto();
                ordenProducto.setOrden(ordenGuardada);
                ordenProducto.setProducto(producto);
                ordenProducto.setCantidad(cantidad);
                productosOrden.add(ordenProducto);
            }
        }
        ordenGuardada.setProductos(productosOrden);

        // Guardar la orden actualizada
        ordenGuardada = ordenRepository.save(ordenGuardada);

        // Si se finaliza el servicio de inmediato, generar la factura
        if (ordenGuardada.getEstado() == EstadoOrden.SERVICE_TERMINADO) {
            generarFactura(ordenGuardada);
        }

        // Volver a cargar para traer facturas mapeadas correctamente
        return obtenerPorId(ordenGuardada.getId());
    }

    public List<OrdenResponseDTO> obtenerTodas(Long idMecanico) {
        return ordenRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")).stream()
                .filter(o -> idMecanico == null || (o.getMecanico() != null && o.getMecanico().getId().equals(idMecanico)))
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

        // Si pasa a ENTREGADO, requiere validación del PIN
        if (requestDTO.getEstado() == EstadoOrden.ENTREGADO && orden.getEstado() != EstadoOrden.ENTREGADO) {
            if (orden.getPin() == null || orden.getPin().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se ha generado un PIN de entrega para esta orden.");
            }
            if (orden.getFechaExpiracionPin() == null || LocalDateTime.now().isAfter(orden.getFechaExpiracionPin())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El PIN ha expirado.");
            }
            if (!orden.getPin().equals(requestDTO.getPin())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El PIN ingresado es incorrecto.");
            }
            // Limpiar pin al entregar exitosamente
            orden.setPin(null);
            orden.setFechaExpiracionPin(null);
        }

        // Si pasa a SERVICE_TERMINADO, generamos el PIN si no existe o si ya expiró
        if (requestDTO.getEstado() == EstadoOrden.SERVICE_TERMINADO) {
            if (orden.getPin() == null || orden.getPin().isEmpty() || 
                orden.getFechaExpiracionPin() == null || LocalDateTime.now().isAfter(orden.getFechaExpiracionPin())) {
                orden.setPin(generarPinAleatorio());
                orden.setFechaExpiracionPin(LocalDateTime.now().plusMinutes(15));
            }
        }

        orden.setEstado(requestDTO.getEstado());
        orden.setNotas(requestDTO.getNotas());

        // Validar y asignar mecánico si se proporciona
        if (requestDTO.getIdMecanico() != null) {
            Usuario mecanico = usuarioRepository.findById(requestDTO.getIdMecanico())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mecánico no encontrado"));
            if (!Boolean.TRUE.equals(mecanico.getActivo())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mecánico asignado no está activo");
            }
            if (mecanico.getRol() == null || mecanico.getRol().getNombreRol() != NombreRol.MECHANIC) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario asignado no tiene el rol de mecánico");
            }
            orden.setMecanico(mecanico);
        } else {
            orden.setMecanico(null);
        }

        // Actualizar preferencias de cliente y método de pago
        if (requestDTO.getIdUsuario() != null) {
            Usuario cliente = usuarioRepository.findById(requestDTO.getIdUsuario())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
            orden.setCliente(cliente);
        }
        if (requestDTO.getIdMetodoPago() != null) {
            MetodoPago metodoPago = metodoPagoRepository.findById(requestDTO.getIdMetodoPago())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
            orden.setMetodoPago(metodoPago);
        }

        // Buscar si ya existe una factura en estado PENDIENTE asociada a la orden
        Factura factura = facturaRepository.findAll().stream()
                .filter(f -> f.getOrden() != null && f.getOrden().getId().equals(id) && f.getEstado() == EstadoFactura.PENDIENTE)
                .findFirst()
                .orElse(null);

        if (factura != null) {
            // Caso A: Ya existe una factura. Revertimos y actualizamos tanto la factura como la orden.
            for (DetalleFactura detalle : factura.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.setStock(producto.getStock() + detalle.getCantidad());
                productoRepository.save(producto);
            }

            factura.getDetalles().clear();
            factura.getDetallesServicios().clear();

            double totalFactura = 0.0;

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

            orden.getProductos().clear();
            if (requestDTO.getProductos() != null) {
                for (OrdenProductoRequestDTO prodReq : requestDTO.getProductos()) {
                    Producto producto = productoRepository.findById(prodReq.getIdProducto())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

                    int cantidad = prodReq.getCantidad() != null ? prodReq.getCantidad() : 1;

                    if (producto.getStock() < cantidad) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + producto.getNombre());
                    }

                    producto.setStock(producto.getStock() - cantidad);
                    productoRepository.save(producto);

                    OrdenProducto ordenProducto = new OrdenProducto();
                    ordenProducto.setOrden(orden);
                    ordenProducto.setProducto(producto);
                    ordenProducto.setCantidad(cantidad);
                    orden.getProductos().add(ordenProducto);

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

            if (requestDTO.getIdMetodoPago() != null) {
                MetodoPago metodoPago = metodoPagoRepository.findById(requestDTO.getIdMetodoPago())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Método de pago no encontrado"));
                factura.setMetodoPago(metodoPago);
            }

            facturaRepository.save(factura);
        } else {
            // Caso B: Aún no existe la factura (orden en progreso). Gestionamos el stock directamente sobre la orden.
            if (orden.getProductos() != null) {
                for (OrdenProducto op : orden.getProductos()) {
                    Producto producto = op.getProducto();
                    producto.setStock(producto.getStock() + op.getCantidad());
                    productoRepository.save(producto);
                }
                orden.getProductos().clear();
            }

            if (requestDTO.getProductos() != null) {
                for (OrdenProductoRequestDTO prodReq : requestDTO.getProductos()) {
                    Producto producto = productoRepository.findById(prodReq.getIdProducto())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

                    int cantidad = prodReq.getCantidad() != null ? prodReq.getCantidad() : 1;

                    if (producto.getStock() < cantidad) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock insuficiente para el producto: " + producto.getNombre());
                    }

                    producto.setStock(producto.getStock() - cantidad);
                    productoRepository.save(producto);

                    OrdenProducto ordenProducto = new OrdenProducto();
                    ordenProducto.setOrden(orden);
                    ordenProducto.setProducto(producto);
                    ordenProducto.setCantidad(cantidad);
                    orden.getProductos().add(ordenProducto);
                }
            }

            if (orden.getServicios() != null) {
                orden.getServicios().clear();
            }
            if (requestDTO.getServicios() != null) {
                for (OrdenServicioRequestDTO servReq : requestDTO.getServicios()) {
                    Servicio servicio = servicioRepository.findById(servReq.getIdServicio())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Servicio no encontrado"));

                    OrdenServicio ordenServicio = new OrdenServicio();
                    ordenServicio.setOrden(orden);
                    ordenServicio.setServicio(servicio);

                    double precioAcordado = servReq.getPrecioAcordado() != null ? servReq.getPrecioAcordado() : servicio.getPrecioBase();
                    ordenServicio.setPrecioAcordado(precioAcordado);
                    orden.getServicios().add(ordenServicio);
                }
            }
        }

        Orden saved = ordenRepository.save(orden);

        // Si se cambia a terminado y no se había facturado, se genera la factura
        if (saved.getEstado() == EstadoOrden.SERVICE_TERMINADO) {
            generarFactura(saved);
        }

        return convertToResponseDTO(saved);
    }

    @Transactional
    public OrdenResponseDTO actualizarEstado(Long id, String nuevoEstadoStr, String pin) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        EstadoOrden nuevoEstado;
        try {
            nuevoEstado = EstadoOrden.valueOf(nuevoEstadoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de orden no válido: " + nuevoEstadoStr);
        }

        // Si pasa a ENTREGADO, requiere validación del PIN
        if (nuevoEstado == EstadoOrden.ENTREGADO) {
            if (orden.getPin() == null || orden.getPin().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se ha generado un PIN de entrega para esta orden.");
            }
            if (orden.getFechaExpiracionPin() == null || LocalDateTime.now().isAfter(orden.getFechaExpiracionPin())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El PIN ha expirado.");
            }
            if (!orden.getPin().equals(pin)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El PIN ingresado es incorrecto.");
            }
            // Limpiar pin al entregar exitosamente
            orden.setPin(null);
            orden.setFechaExpiracionPin(null);
        }

        // Si pasa a SERVICE_TERMINADO, generamos el PIN si no existe o si ya expiró
        if (nuevoEstado == EstadoOrden.SERVICE_TERMINADO) {
            if (orden.getPin() == null || orden.getPin().isEmpty() || 
                orden.getFechaExpiracionPin() == null || LocalDateTime.now().isAfter(orden.getFechaExpiracionPin())) {
                orden.setPin(generarPinAleatorio());
                orden.setFechaExpiracionPin(LocalDateTime.now().plusMinutes(15));
            }
        }

        orden.setEstado(nuevoEstado);
        Orden saved = ordenRepository.save(orden);

        if (nuevoEstado == EstadoOrden.SERVICE_TERMINADO) {
            generarFactura(saved);
        }

        return convertToResponseDTO(saved);
    }

    @Transactional
    public void eliminarOrden(Long id) {
        Orden orden = ordenRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orden no encontrada"));

        // Buscar facturas asociadas a la orden
        List<Factura> facturasAsociadas = facturaRepository.findAll().stream()
                .filter(f -> f.getOrden() != null && f.getOrden().getId().equals(id))
                .collect(Collectors.toList());

        if (facturasAsociadas.isEmpty()) {
            // Caso sin factura generada aún (orden en progreso): Devolvemos el stock de los productos directamente de la orden
            if (orden.getProductos() != null) {
                for (OrdenProducto op : orden.getProductos()) {
                    Producto producto = op.getProducto();
                    producto.setStock(producto.getStock() + op.getCantidad());
                    productoRepository.save(producto);
                }
            }
        } else {
            // Caso con facturas asociadas: Devolvemos stock de productos de la factura PENDIENTE si existe
            for (Factura f : facturasAsociadas) {
                if (f.getEstado() == EstadoFactura.PENDIENTE) {
                    for (DetalleFactura detalle : f.getDetalles()) {
                        Producto producto = detalle.getProducto();
                        producto.setStock(producto.getStock() + detalle.getCantidad());
                        productoRepository.save(producto);
                    }
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
        dto.setPin(orden.getPin());
        dto.setFechaExpiracionPin(orden.getFechaExpiracionPin());

        if (orden.getMecanico() != null) {
            dto.setIdMecanico(orden.getMecanico().getId());
            dto.setNombreMecanico(orden.getMecanico().getNombre());
        }

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
            dto.setProductos(orden.getProductos().stream().map(op -> {
                OrdenProductoResponseDTO pDto = new OrdenProductoResponseDTO();
                pDto.setIdProducto(op.getProducto().getId());
                pDto.setNombreProducto(op.getProducto().getNombre());
                pDto.setPrecio(op.getProducto().getPrecio());
                pDto.setCantidad(op.getCantidad());
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

    private void generarFactura(Orden orden) {
        // Verificar si ya existe una factura para esta orden
        boolean facturaExiste = facturaRepository.findAll().stream()
                .anyMatch(f -> f.getOrden() != null && f.getOrden().getId().equals(orden.getId()));
        if (facturaExiste) {
            return;
        }

        Factura factura = new Factura();
        factura.setOrden(orden);
        factura.setFecha(LocalDateTime.now());
        factura.setEstado(EstadoFactura.PENDIENTE);

        // Cliente
        Usuario cliente = orden.getCliente();
        if (cliente == null) {
            if (orden.getMotocicleta().getUsuario() != null) {
                cliente = orden.getMotocicleta().getUsuario();
            } else {
                cliente = usuarioRepository.findByEmail("consumidor@final")
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Usuario Consumidor Final no configurado"));
            }
        }
        factura.setUsuario(cliente);

        // Metodo Pago
        MetodoPago metodoPago = orden.getMetodoPago();
        if (metodoPago == null) {
            metodoPago = metodoPagoRepository.findByNombre("EFECTIVO")
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Método de pago EFECTIVO no configurado"));
        }
        factura.setMetodoPago(metodoPago);

        double total = 0.0;

        // Detalles de productos
        List<DetalleFactura> detalles = new ArrayList<>();
        if (orden.getProductos() != null) {
            for (OrdenProducto op : orden.getProductos()) {
                DetalleFactura df = new DetalleFactura();
                df.setFactura(factura);
                df.setProducto(op.getProducto());
                df.setCantidad(op.getCantidad());
                df.setPrecioUnitario(op.getProducto().getPrecio());
                double subtotal = op.getProducto().getPrecio() * op.getCantidad();
                df.setSubtotal(subtotal);
                detalles.add(df);
                total += subtotal;
            }
        }
        factura.setDetalles(detalles);

        // Detalles de servicios
        List<DetalleFacturaServicio> detallesServicios = new ArrayList<>();
        if (orden.getServicios() != null) {
            for (OrdenServicio os : orden.getServicios()) {
                DetalleFacturaServicio dfs = new DetalleFacturaServicio();
                dfs.setFactura(factura);
                dfs.setServicio(os.getServicio());
                dfs.setPrecioUnitario(os.getPrecioAcordado());
                dfs.setSubtotal(os.getPrecioAcordado());
                detallesServicios.add(dfs);
                total += os.getPrecioAcordado();
            }
        }
        factura.setDetallesServicios(detallesServicios);

        factura.setTotal(total);

        facturaRepository.save(factura);
    }

    private String generarPinAleatorio() {
        java.util.Random random = new java.util.Random();
        int number = 100000 + random.nextInt(900000); // 6-digit PIN
        return String.valueOf(number);
    }
}
