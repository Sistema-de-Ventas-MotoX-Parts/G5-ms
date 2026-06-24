package com.example.demo.service;

import com.example.demo.model.Categoria;
import com.example.demo.repository.CategoriaRepository;
import com.example.demo.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

	@Autowired
	private CategoriaRepository categoriaRepository;

	@Autowired
	private ProductoRepository productoRepository;

	//DAR DE ALTA
	public Categoria guardarCategoria(Categoria categoria) {
		// Validar nombre duplicado (insensible a mayúsculas)
		String nombreTrim = categoria.getNombre().trim();
		Optional<Categoria> existente = categoriaRepository.findByNombreIgnoreCase(nombreTrim);
		if (existente.isPresent() && Boolean.TRUE.equals(existente.get().getActivo())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Ya existe una categoría activa con el nombre \"" + nombreTrim + "\"");
		}
		categoria.setNombre(nombreTrim);
		categoria.setActivo(true);
		return categoriaRepository.save(categoria);
	}

	//LISTAR
	public List<Categoria> obtenerTodos(){
		return categoriaRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")).stream()
				.filter(c -> Boolean.TRUE.equals(c.getActivo()))
				.collect(java.util.stream.Collectors.toList());
	}

	//OBTENER POR ID
	public Categoria obtenerPorId(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Categoría no encontrada con id: " + id));
	}

	//EDITAR
	public Categoria editarCategoria(Long id, Categoria categoriaActualizada) {
		Categoria categoria = categoriaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Categoría no encontrada con id: " + id));

		String nombreTrim = categoriaActualizada.getNombre().trim();

		// Validar que el nuevo nombre no pertenezca a otra categoría activa
		Optional<Categoria> existente = categoriaRepository.findByNombreIgnoreCase(nombreTrim);
		if (existente.isPresent()
				&& Boolean.TRUE.equals(existente.get().getActivo())
				&& !existente.get().getId().equals(id)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Ya existe otra categoría activa con el nombre \"" + nombreTrim + "\"");
		}

		categoria.setNombre(nombreTrim);
		return categoriaRepository.save(categoria);
	}

	//DAR DE BAJA
	public void eliminarCategoria(Long id) {
		Categoria categoria = categoriaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Categoría no encontrada con id: " + id));

		// Validar que no tenga productos activos asociados
		if (productoRepository.existsByCategoriaIdAndActivoTrue(id)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"No se puede eliminar la categoría porque tiene productos activos asociados");
		}

		categoria.setActivo(false);
		categoriaRepository.save(categoria);
	}
}

