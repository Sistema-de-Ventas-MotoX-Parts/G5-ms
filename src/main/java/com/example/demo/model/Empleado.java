package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "empleados")
public class Empleado {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_usuario")
    @JsonIgnore
    private Usuario usuario;

    private Double sueldo;

    @Column(name = "dias_trabajo")
    private String diasTrabajo;

    @Column(name = "horario_trabajo")
    private String horarioTrabajo;

    @Column(name = "dias_libres")
    private String diasLibres;

    public Empleado() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Double getSueldo() {
        return sueldo;
    }

    public void setSueldo(Double sueldo) {
        this.sueldo = sueldo;
    }

    public String getDiasTrabajo() {
        return diasTrabajo;
    }

    public void setDiasTrabajo(String diasTrabajo) {
        this.diasTrabajo = diasTrabajo;
    }

    public String getHorarioTrabajo() {
        return horarioTrabajo;
    }

    public void setHorarioTrabajo(String horarioTrabajo) {
        this.horarioTrabajo = horarioTrabajo;
    }

    public String getDiasLibres() {
        return diasLibres;
    }

    public void setDiasLibres(String diasLibres) {
        this.diasLibres = diasLibres;
    }
}
