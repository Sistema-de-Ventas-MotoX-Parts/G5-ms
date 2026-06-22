package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "orden_servicio")
public class OrdenServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @NotNull(message = "La orden es obligatoria")
    @ManyToOne
    @JoinColumn(name = "id_orden", nullable = false)
    private Orden orden;

    @NotNull(message = "El servicio es obligatorio")
    @ManyToOne
    @JoinColumn(name = "id_servicio", nullable = false)
    private Servicio servicio;

    @NotNull(message = "El precio acordado es obligatorio")
    @PositiveOrZero(message = "El precio acordado no puede ser negativo")
    @Column(name = "precio_acordado", nullable = false)
    private Double precioAcordado;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Orden getOrden() {
        return orden;
    }

    public void setOrden(Orden orden) {
        this.orden = orden;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public Double getPrecioAcordado() {
        return precioAcordado;
    }

    public void setPrecioAcordado(Double precioAcordado) {
        this.precioAcordado = precioAcordado;
    }
}
