package com.plazoletaucc.Plazoleta.repository;

import com.plazoletaucc.Plazoleta.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    boolean existsByCorreo(String correo);
    boolean existsByDocumentoDeIdentidad(String documentoDeIdentidad);
    Optional<Usuario> findByCorreo(String correo);
}
