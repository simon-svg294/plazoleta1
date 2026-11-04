package com.plazoletaucc.Plazoleta.repository;

import com.plazoletaucc.Plazoleta.entity.PedidoEstadoLog;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoEstadoLogRepository extends JpaRepository<PedidoEstadoLog, Integer> {

    // HU-17: logs de un pedido en orden cronologico. Se trae el empleado para evitar N+1.
    @EntityGraph(attributePaths = "empleado")
    List<PedidoEstadoLog> findByPedido_IdOrderByFechaCambioAscIdAsc(Integer idPedido);
}
