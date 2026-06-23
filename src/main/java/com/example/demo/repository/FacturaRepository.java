package com.example.demo.repository;

import com.example.demo.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    List<Factura> findByUsuarioIdOrderByIdDesc(Long usuarioId);
}
