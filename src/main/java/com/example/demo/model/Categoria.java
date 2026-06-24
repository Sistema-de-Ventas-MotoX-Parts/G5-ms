package com.example.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "categorias")
public class Categoria {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
	
	// Mínimo 2 y máximo 50 caracteres. Solo letras (con tildes), números, espacios, guiones y barras.
	@NotBlank(message = "El nombre de la categoría es obligatorio")
	@Size(min = 2, max = 50,
		  message = "El nombre debe tener entre 2 y 50 caracteres")
	@Pattern(regexp = "^[\\p{L}0-9][\\p{L}0-9 \\-/]*$",
			 message = "El nombre solo puede contener letras, números, espacios, guiones (-) y barras (/)")
	@Column(length = 50, nullable = false)
	private String nombre;
	
	//Campo para desactivar categoria (lo asigna el service, no se valida en el request)
	@Column(nullable = false)
	private Boolean activo;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}



}
