package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.PedidoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.PedidoRequestDTO;
import com.plazoletaucc.Plazoleta.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    // HU-11: el cliente crea un pedido con platos de un mismo restaurante.
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@Valid @RequestBody PedidoRequestDTO dto,
                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.crearPedido(dto, authentication.getName()));
    }
}
