package com.example.demo.repository;

import com.example.demo.model.Orden;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Long> {
	
    List<Orden> findByMecanicoId(Long mecanicoId);

    
    List<Orden> findByMecanicoEmail(String email);
}
