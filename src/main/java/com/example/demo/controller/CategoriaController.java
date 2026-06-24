package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

import com.example.demo.model.Categoria;
import com.example.demo.service.CategoriaService;
import com.example.demo.security.JwtUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/categorias")
@CrossOrigin(origins = "http://localhost:4200")
public class CategoriaController {

	@Autowired
	private CategoriaService categoriaService;

	@Autowired
	private JwtUtil jwtUtil;

	//LISTAR
	@GetMapping
	public ResponseEntity<List<Categoria>> listar() {
		return ResponseEntity.ok(categoriaService.obtenerTodos());
	}

	//OBTENER POR ID
	@GetMapping("/{id}")
	public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
		try {
			return ResponseEntity.ok(categoriaService.obtenerPorId(id));
		} catch (ResponseStatusException e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getReason());
			return ResponseEntity.status(e.getStatusCode()).body(error);
		}
	}

	//ALTA
	@PostMapping
	public ResponseEntity<?> crear(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@Valid @RequestBody Categoria categoria) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		try {
			Categoria nueva = categoriaService.guardarCategoria(categoria);
			return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
		} catch (ResponseStatusException e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getReason());
			return ResponseEntity.status(e.getStatusCode()).body(error);
		}
	}

	//EDITAR
	@PutMapping("/{id}")
	public ResponseEntity<?> editar(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@PathVariable Long id,
			@Valid @RequestBody Categoria categoria) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		try {
			return ResponseEntity.ok(categoriaService.editarCategoria(id, categoria));
		} catch (ResponseStatusException e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getReason());
			return ResponseEntity.status(e.getStatusCode()).body(error);
		}
	}

	//BAJA
	@DeleteMapping("/{id}")
	public ResponseEntity<?> eliminar(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@PathVariable Long id) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		try {
			categoriaService.eliminarCategoria(id);
			Map<String, String> response = new HashMap<>();
			response.put("mensaje", "Categoría eliminada exitosamente");
			return ResponseEntity.ok(response);
		} catch (ResponseStatusException e) {
			Map<String, String> error = new HashMap<>();
			error.put("error", e.getReason());
			return ResponseEntity.status(e.getStatusCode()).body(error);
		}
	}
}

