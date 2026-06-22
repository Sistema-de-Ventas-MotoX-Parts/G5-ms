package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.demo.model.Categoria;
import com.example.demo.service.CategoriaService;
import com.example.demo.security.JwtUtil;
import java.util.List;

@RestController
@RequestMapping("api/categorias")
@CrossOrigin(origins = "*")
public class CategoriaController {

	@Autowired
	private CategoriaService categoriaService;

	@Autowired
	private JwtUtil jwtUtil;

	//LISTAR
	@GetMapping
	public List<Categoria> listar() {
		return categoriaService.obtenerTodos();
	}

	//OBTENER POR ID
	@GetMapping("/{id}")
	public Categoria obtenerPorId(@PathVariable Long id) {
		return categoriaService.obtenerPorId(id);
	}

	//ALTA
	@PostMapping
	public Categoria crear(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@RequestBody Categoria categoria) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		return categoriaService.guardarCategoria(categoria);
	}

	//EDITAR
	@PutMapping("/{id}")
	public Categoria editar(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@PathVariable Long id,
			@Valid @RequestBody Categoria categoria) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		return categoriaService.editarCategoria(id, categoria);
	}

	//BAJA
	@DeleteMapping("/{id}")
	public void eliminar(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@PathVariable Long id) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		categoriaService.eliminarCategoria(id);
	}
}
