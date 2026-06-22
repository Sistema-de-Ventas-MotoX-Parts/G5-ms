package com.example.demo.repository;

import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE usuarios SET id_rol = :rolId WHERE id_rol = 0 OR id_rol IS NULL", nativeQuery = true)
    void fixUsuariosSinRol(@Param("rolId") Long rolId);
}
