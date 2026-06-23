package com.example.demo.service;

import com.example.demo.model.Categoria;
import com.example.demo.repository.CategoriaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository categoriaRepository;

	//DAR DE ALTA
	public Categoria guardarCategoria(Categoria categoria) {
		categoria.setActivo(true);
		return categoriaRepository.save(categoria);
	}

	//LISTAR
	public List<Categoria> obtenerTodos(){
		return categoriaRepository.findAll().stream()
				.filter(c -> Boolean.TRUE.equals(c.getActivo()))
				.collect(java.util.stream.Collectors.toList());
	}

	//OBTENER POR ID
	public Categoria obtenerPorId(Long id) {
		return categoriaRepository.findById(id).orElse(null);
	}

	//EDITAR
	public Categoria editarCategoria(Long id, Categoria categoriaActualizada) {
		return categoriaRepository.findById(id).map(categoria -> {
			categoria.setNombre(categoriaActualizada.getNombre());
			return categoriaRepository.save(categoria);
		}).orElse(null);
	}

	//DAR DE BAJA
	public void eliminarCategoria(Long id) {
		Categoria categoria = categoriaRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
		categoria.setActivo(false);
		categoriaRepository.save(categoria);
	}
}
