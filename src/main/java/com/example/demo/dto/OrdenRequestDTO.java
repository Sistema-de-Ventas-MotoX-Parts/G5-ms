package com.example.demo.dto;

import com.example.demo.model.EstadoOrden;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrdenRequestDTO {

    @NotNull(message = "El ID de la motocicleta es obligatorio")
    private Long idMoto;

    private String telefonoContacto;

    @NotNull(message = "El estado es obligatorio")
    private EstadoOrden estado;

    private String notas;

    private Long idMetodoPago;

    private Long idUsuario; // Opcional, si no se envía se toma el de la motocicleta

    private List<OrdenServicioRequestDTO> servicios;

    private List<OrdenProductoRequestDTO> productos;

    private Long idMecanico;

    public Long getIdMoto() {
        return idMoto;
    }

    public void setIdMoto(Long idMoto) {
        this.idMoto = idMoto;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public void setEstado(EstadoOrden estado) {
        this.estado = estado;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Long getIdMetodoPago() {
        return idMetodoPago;
    }

    public void setIdMetodoPago(Long idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public List<OrdenServicioRequestDTO> getServicios() {
        return servicios;
    }

    public void setServicios(List<OrdenServicioRequestDTO> servicios) {
        this.servicios = servicios;
    }

    public List<OrdenProductoRequestDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<OrdenProductoRequestDTO> productos) {
        this.productos = productos;
    }

    public Long getIdMecanico() {
        return idMecanico;
    }

    public void setIdMecanico(Long idMecanico) {
        this.idMecanico = idMecanico;
    }
}
