package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "motocicletas")
public class Motocicleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_modelo", nullable = false)
    private Modelo modelo;

    @Column(nullable = false, unique = true)
    private String patente;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = true)
    private Usuario usuario;
    
    @NotNull(message = "El estado de la motocicleta es obligarotio")
	@Column(nullable = false)
	private Boolean activo;

    @Column(nullable = true, unique = true)
    private String dni;

    @Column(name = "fecha_creacion", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCreacion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMarca() {
        return modelo != null && modelo.getMarca() != null ? modelo.getMarca().getNombre() : null;
    }

    public String getModelo() {
        return modelo != null ? modelo.getNombre() : null;
    }

    public Modelo getModeloEntity() {
        return modelo;
    }

    public void setModeloEntity(Modelo modelo) {
        this.modelo = modelo;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
