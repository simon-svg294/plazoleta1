package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.PlatoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.ModificarPlatoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PlatoRequestDTO;
import com.plazoletaucc.Plazoleta.service.PlatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platos")
@RequiredArgsConstructor
public class PlatoController {
    private final PlatoService platoService;

    @PostMapping("/registro")
    public ResponseEntity<PlatoResponseDTO> registrarPlato(@Valid @RequestBody PlatoRequestDTO platoRequestDTO,
                                                           Authentication authentication) {
        // authentication.getName() es el correo del token (lo llena JwtAuthenticationFilter)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(platoService.registrarPlato(platoRequestDTO, authentication.getName()));
    }

    // HU-04: modificar precio y descripcion. PATCH = cambio parcial.
    @PatchMapping("/{idPlato}")
    public ResponseEntity<PlatoResponseDTO> modificarPlato(@PathVariable Integer idPlato,
                                                           @Valid @RequestBody ModificarPlatoRequestDTO dto,
                                                           Authentication authentication) {
        return ResponseEntity.ok(platoService.modificarPlato(idPlato, dto, authentication.getName()));
    }
}
