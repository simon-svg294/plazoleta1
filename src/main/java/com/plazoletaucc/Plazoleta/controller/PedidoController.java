package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoListadoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoListoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.EntregarPedidoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;
import com.plazoletaucc.Plazoleta.entity.EstadoPedido;
import com.plazoletaucc.Plazoleta.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Gestion de pedidos: creacion, listado, asignacion, listo y entrega")
@SecurityRequirement(name = "bearerAuth")
public class PedidoController {

    private final PedidoService pedidoService;

    // HU-11: el cliente crea un pedido con platos de un mismo restaurante.
    @Operation(summary = "Crear pedido", description = "HU-11: el cliente crea un pedido con platos de un mismo restaurante.")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO dto,
                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crearPedido(dto, authentication.getName()));
    }

    // HU-12: el empleado lista los pedidos de su restaurante filtrando por estado.
    @Operation(summary = "Listar pedidos por estado", description = "HU-12: el empleado lista los pedidos de su restaurante filtrando por estado.")
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<PedidoListadoResponseDTO>> listarPedidos(
            @RequestParam EstadoPedido estado,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano,
            Authentication authentication) {
        return ResponseEntity.ok(pedidoService.listarPedidosPorEstado(
                estado, pagina, tamano, authentication.getName()));
    }

    // HU-13: el empleado se asigna a un pedido de su restaurante y lo pasa a EN_PREPARACION.
    @Operation(summary = "Asignar pedido", description = "HU-13: el empleado se asigna a un pedido y lo pasa a EN_PREPARACION.")
    @PatchMapping("/{idPedido}/asignar")
    public ResponseEntity<PedidoResponseDTO> asignarPedido(@PathVariable Integer idPedido,
                                                           Authentication authentication) {
        return ResponseEntity.ok(pedidoService.asignarPedido(idPedido, authentication.getName()));
    }

    // HU-14: el empleado notifica que el pedido esta listo y se envia el PIN por SMS.
    @Operation(summary = "Marcar pedido listo", description = "HU-14: el empleado notifica que el pedido esta listo y se envia el PIN por SMS.")
    @PatchMapping("/{idPedido}/listo")
    public ResponseEntity<PedidoListoResponseDTO> marcarPedidoListo(@PathVariable Integer idPedido,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(pedidoService.marcarPedidoListo(idPedido, authentication.getName()));
    }

    // HU-15: el empleado entrega el pedido digitando el PIN de seguridad del cliente.
    @Operation(summary = "Entregar pedido", description = "HU-15: el empleado entrega el pedido digitando el PIN de seguridad del cliente.")
    @PatchMapping("/{idPedido}/entregar")
    public ResponseEntity<PedidoResponseDTO> entregarPedido(
            @PathVariable Integer idPedido,
            @Valid @RequestBody EntregarPedidoRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(pedidoService.entregarPedido(
                idPedido, dto.getPin(), authentication.getName()));
    }

    // HU-16: el cliente cancela su pedido solo si esta PENDIENTE.
    @Operation(summary = "Cancelar pedido", description = "HU-16: el cliente cancela su pedido solo si esta PENDIENTE.")
    @PatchMapping("/{idPedido}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Integer idPedido,
                                                            Authentication authentication) {
        return ResponseEntity.ok(pedidoService.cancelarPedido(idPedido, authentication.getName()));
    }
}
