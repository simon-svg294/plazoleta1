package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.PedidoEstadoLogResponseDTO;
import com.plazoletaucc.Plazoleta.service.TrazabilidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Trazabilidad", description = "Consulta del historial de estados de un pedido")
@SecurityRequirement(name = "bearerAuth")
public class TrazabilidadController {

    private final TrazabilidadService trazabilidadService;

    // HU-17: el cliente consulta el historial de estados de sus propios pedidos.
    @Operation(summary = "Consultar trazabilidad",
            description = "HU-17: el cliente consulta el historial de estados de sus propios pedidos.")
    @GetMapping("/{idPedido}/trazabilidad")
    public ResponseEntity<List<PedidoEstadoLogResponseDTO>> consultarTrazabilidad(
            @PathVariable Integer idPedido,
            Authentication authentication) {
        return ResponseEntity.ok(trazabilidadService.consultarTrazabilidad(
                idPedido, authentication.getName()));
    }
}
