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
	
	
	public Producto guardarProducto(Producto producto) {
		return productoRepository.save(producto);
	}

	public List<Producto> obtenerTodos(){
		return productoRepository.findAll();
	}
	

	public void eliminarProducto(Long id) {
		productoRepository.deleteById(id);
		System.out.println("Se elimino el producto");
	}
}
