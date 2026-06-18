package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Producto;
import com.example.demo.repository.ProductoRepository;
import java.util.List;


@Service
public class ProductoService {
	
	@Autowired
	private ProductoRepository productoRepository;
	
	@Autowired
	private com.example.demo.repository.CategoriaRepository categoriaRepository;
	
	private void validarCategoria(Producto producto) {
		if (producto.getCategoria() != null && producto.getCategoria().getId() != null) {
			if (!categoriaRepository.existsById(producto.getCategoria().getId())) {
				throw new IllegalArgumentException("La categoría proporcionada no existe en la base de datos.");
			}
		} else {
			throw new IllegalArgumentException("El ID de la categoría es obligatorio.");
		}
	}
	
	// DAR DE ALTA
	public Producto guardarProducto(Producto producto) {
		validarCategoria(producto);
		return productoRepository.save(producto);
	}

	//LISTAR
	public List<Producto> obtenerTodos(){
		return productoRepository.findAll();
	}
	
	//OBTENER POR ID
	public Producto obtenerPorId(Long id) {
		return productoRepository.findById(id).orElse(null);
	}
	
    //EDITAR
	public Producto editarProducto(Long id, Producto productoActualizado) {
		validarCategoria(productoActualizado);
		return productoRepository.findById(id).map(producto -> {
			producto.setCodigoSku(productoActualizado.getCodigoSku());
			producto.setNombre(productoActualizado.getNombre());
			producto.setDescripcion(productoActualizado.getDescripcion());
			producto.setPrecio(productoActualizado.getPrecio());
			producto.setStock(productoActualizado.getStock());
			producto.setImagenUrl(productoActualizado.getImagenUrl());
			producto.setCategoria(productoActualizado.getCategoria());
			producto.setActivo(productoActualizado.getActivo());
			return productoRepository.save(producto);
		}).orElse(null);
	}


	//DAR DE BAJA
	public void eliminarProducto(Long id) {
		productoRepository.deleteById(id);
	}
}
