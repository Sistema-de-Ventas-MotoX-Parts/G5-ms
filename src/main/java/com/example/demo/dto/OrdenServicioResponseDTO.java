package com.example.demo.dto;

public class OrdenServicioResponseDTO {
    private Long idServicio;
    private String nombreServicio;
    private Double precioAcordado;

    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public Double getPrecioAcordado() {
        return precioAcordado;
    }

    public void setPrecioAcordado(Double precioAcordado) {
        this.precioAcordado = precioAcordado;
    }
}
