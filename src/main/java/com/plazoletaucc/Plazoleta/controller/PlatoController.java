package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.PlatoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.HabilitarPlatoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.ModificarPlatoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.PlatoRequestDTO;
import com.plazoletaucc.Plazoleta.service.PlatoService;
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
@RequestMapping("/api/v1/platos")
@RequiredArgsConstructor
@Tag(name = "Platos", description = "Gestion de platos: listado, creacion, modificacion y estado")
public class PlatoController {
    private final PlatoService platoService;

    // HU-10: el cliente lista el menu de un restaurante, paginado y filtrable por categoria.
    @Operation(summary = "Listar platos", description = "HU-10: el cliente lista el menu de un restaurante, paginado y filtrable por categoria.")
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<PlatoResponseDTO>> listarPlatos(
            @RequestParam Integer idRestaurante,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        return ResponseEntity.ok(platoService.listarPlatos(idRestaurante, idCategoria, pagina, tamano));
    }

    @Operation(summary = "Registrar plato", description = "HU-03: el propietario registra un plato en su restaurante.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/registro")
    public ResponseEntity<PlatoResponseDTO> registrarPlato(@Valid @RequestBody PlatoRequestDTO platoRequestDTO,
                                                           Authentication authentication) {
        // authentication.getName() es el correo del token (lo llena JwtAuthenticationFilter)
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(platoService.registrarPlato(platoRequestDTO, authentication.getName()));
    }

    // HU-04: modificar precio y descripcion. PATCH = cambio parcial.
    @Operation(summary = "Modificar plato", description = "HU-04: el propietario modifica precio y descripcion de un plato.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{idPlato}")
    public ResponseEntity<PlatoResponseDTO> modificarPlato(@PathVariable Integer idPlato,
                                                           @Valid @RequestBody ModificarPlatoRequestDTO dto,
                                                           Authentication authentication) {
        return ResponseEntity.ok(platoService.modificarPlato(idPlato, dto, authentication.getName()));
    }

    // HU-07: habilitar/deshabilitar un plato del restaurante propio.
    @Operation(summary = "Cambiar estado del plato", description = "HU-07: el propietario habilita o deshabilita un plato de su restaurante.")
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{idPlato}/estado")
    public ResponseEntity<PlatoResponseDTO> cambiarEstadoPlato(@PathVariable Integer idPlato,
                                                               @Valid @RequestBody HabilitarPlatoRequestDTO dto,
                                                               Authentication authentication) {
        return ResponseEntity.ok(platoService.cambiarEstadoPlato(idPlato, dto, authentication.getName()));
    }
}
