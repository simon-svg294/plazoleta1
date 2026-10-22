package com.plazoletaucc.Plazoleta.repository;

import com.plazoletaucc.Plazoleta.entity.Restaurante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Integer> {
    boolean existsByNit(String nit);
}
