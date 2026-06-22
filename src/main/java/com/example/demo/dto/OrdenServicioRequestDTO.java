package com.example.demo.dto;

public class OrdenServicioRequestDTO {
    private Long idServicio;
    private Double precioAcordado;

    public Long getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(Long idServicio) {
        this.idServicio = idServicio;
    }

    public Double getPrecioAcordado() {
        return precioAcordado;
    }

    public void setPrecioAcordado(Double precioAcordado) {
        this.precioAcordado = precioAcordado;
    }
}
