package com.example.demo.repository;

import com.example.demo.model.Motocicleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import java.util.List;

@Repository
public interface MotocicletaRepository extends JpaRepository<Motocicleta, Long> {
    List<Motocicleta> findByUsuarioId(Long usuarioId);
    Optional<Motocicleta> findByPatente(String patente);
}
