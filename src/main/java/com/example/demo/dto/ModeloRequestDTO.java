package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModeloRequestDTO {
    @NotBlank(message = "El nombre del modelo es obligatorio")
    private String nombre;

    @NotNull(message = "El año es obligatorio")
    private Integer anio;

    @NotNull(message = "El id de la marca es obligatorio")
    private Long idMarca;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Long getIdMarca() {
        return idMarca;
    }

    public void setIdMarca(Long idMarca) {
        this.idMarca = idMarca;
    }
}
