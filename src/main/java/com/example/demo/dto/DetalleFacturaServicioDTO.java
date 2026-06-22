package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class DetalleFacturaServicioDTO {

    private Long id;

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long idServicio;

    private String nombreServicio;

    @PositiveOrZero(message = "El precio unitario no puede ser negativo")
    private Double precioUnitario;

    private Double subtotal;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getIdServicio() { return idServicio; }
    public void setIdServicio(Long idServicio) { this.idServicio = idServicio; }

    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
}
