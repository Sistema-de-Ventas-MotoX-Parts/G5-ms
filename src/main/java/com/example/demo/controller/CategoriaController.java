package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.demo.model.Categoria;
import com.example.demo.service.CategoriaService;
import java.util.List;

@RestController
@RequestMapping("api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

	@Autowired
	private CategoriaService categoriaService;

	//LISTAR
	@GetMapping
	public List<Categoria> listar(){
		return categoriaService.obtenerTodos();
	}

	//OBTENER POR ID
	@GetMapping("/{id}")
	public Categoria obtenerPorId(@PathVariable Long id) {
		return categoriaService.obtenerPorId(id);
	}

	//ALTA
	@PostMapping
	public Categoria crear( @RequestBody Categoria categoria) {
		return categoriaService.guardarCategoria(categoria);
	}

	//EDITAR
	@PutMapping("/{id}")
	public Categoria editar(@PathVariable Long id, @Valid @RequestBody Categoria categoria) {
		return categoriaService.editarCategoria(id, categoria);
	}

	//BAJA
	@DeleteMapping("/{id}")
	public void eliminar(@PathVariable Long id) {
		categoriaService.eliminarCategoria(id);
	}
}
