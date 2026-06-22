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

import com.example.demo.model.Producto;
import com.example.demo.service.ProductoService;
import com.example.demo.security.JwtUtil;
import java.util.List;

@RestController
@RequestMapping("api/productos")
@CrossOrigin(origins = "*")
public class ProductoController {

	@Autowired
	private ProductoService productoService;

	@Autowired
	private JwtUtil jwtUtil;

	// LISTAR
	@GetMapping
	public List<Producto> listar() {
		return productoService.obtenerTodos();
	}

	// DAR DE ALTA
	@PostMapping
	public Producto crear(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@Valid @RequestBody Producto producto) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		return productoService.guardarProducto(producto);
	}

	// OBTENER POR ID
	@GetMapping("/{id}")
	public Producto obtenerPorId(@PathVariable Long id) {
		return productoService.obtenerPorId(id);
	}

	// EDITAR
	@PutMapping("/{id}")
	public Producto editar(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@PathVariable Long id,
			@Valid @RequestBody Producto producto) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		return productoService.editarProducto(id, producto);
	}

	// DAR DE BAJA
	@DeleteMapping("/{id}")
	public void eliminar(
			@RequestHeader(value = "Authorization", required = false) String tokenHeader,
			@CookieValue(value = "token_jwt", required = false) String cookieToken,
			@PathVariable Long id) {
		jwtUtil.validarAdmin(tokenHeader, cookieToken);
		productoService.eliminarProducto(id);
	}
}
