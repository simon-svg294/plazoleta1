package com.plazoletaucc.Plazoleta.repository;

import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import com.plazoletaucc.Plazoleta.entity.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    // HU-11: un cliente solo puede tener un pedido en proceso a la vez.
    boolean existsByCliente_IdAndEstadoIn(Integer idCliente, Collection<EstadoPedido> estados);

    // HU-12: pedidos de un restaurante filtrados por estado, paginados.
    Page<Pedido> findByRestaurante_IdAndEstado(Integer idRestaurante, EstadoPedido estado, Pageable pageable);
}
