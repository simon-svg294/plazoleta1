package com.plazoletaucc.Plazoleta.repository;

import com.plazoletaucc.Plazoleta.entity.Plato;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatoRespository extends JpaRepository<Plato, Integer> {

    // HU-10: menu de un restaurante, solo platos activos y paginados.
    @EntityGraph(attributePaths = {"categoria", "restaurante"})
    Page<Plato> findByRestaurante_IdAndEstadoTrue(Integer idRestaurante, Pageable pageable);

    // HU-10: mismo menu, pero filtrado por categoria.
    @EntityGraph(attributePaths = {"categoria", "restaurante"})
    Page<Plato> findByRestaurante_IdAndCategoria_IdAndEstadoTrue(Integer idRestaurante,
                                                                 Integer idCategoria,
                                                                 Pageable pageable);
}
