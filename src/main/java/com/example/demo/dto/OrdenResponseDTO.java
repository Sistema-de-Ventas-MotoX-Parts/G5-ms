package com.example.demo.dto;

import com.example.demo.model.EstadoOrden;
import java.time.LocalDateTime;
import java.util.List;

public class OrdenResponseDTO {

    private Long id;
    private Long idMoto;
    private String marcaModeloMoto;
    private String patenteMoto;
    private LocalDateTime fechaIngreso;
    private String telefonoContacto;
    private EstadoOrden estado;
    private String notas;
    private List<OrdenServicioResponseDTO> servicios;
    private List<OrdenProductoResponseDTO> productos;
    private List<FacturaDTO> facturas;
    private Long idMecanico;
    private String nombreMecanico;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdMoto() {
        return idMoto;
    }

    public void setIdMoto(Long idMoto) {
        this.idMoto = idMoto;
    }

    public String getMarcaModeloMoto() {
        return marcaModeloMoto;
    }

    public void setMarcaModeloMoto(String marcaModeloMoto) {
        this.marcaModeloMoto = marcaModeloMoto;
    }

    public String getPatenteMoto() {
        return patenteMoto;
    }

    public void setPatenteMoto(String patenteMoto) {
        this.patenteMoto = patenteMoto;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
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



    public List<OrdenServicioResponseDTO> getServicios() {
        return servicios;
    }

    public void setServicios(List<OrdenServicioResponseDTO> servicios) {
        this.servicios = servicios;
    }

    public List<OrdenProductoResponseDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<OrdenProductoResponseDTO> productos) {
        this.productos = productos;
    }

    public List<FacturaDTO> getFacturas() {
        return facturas;
    }

    public void setFacturas(List<FacturaDTO> facturas) {
        this.facturas = facturas;
    }

    public Long getIdMecanico() {
        return idMecanico;
    }

    public void setIdMecanico(Long idMecanico) {
        this.idMecanico = idMecanico;
    }

    public String getNombreMecanico() {
        return nombreMecanico;
    }

    public void setNombreMecanico(String nombreMecanico) {
        this.nombreMecanico = nombreMecanico;
    }
}
