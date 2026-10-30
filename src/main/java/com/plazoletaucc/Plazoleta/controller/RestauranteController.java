package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.PaginaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteListadoResponseDTO;
import com.plazoletaucc.Plazoleta.dto.reponse.RestauranteResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.RestauranteRequestDTO;
import com.plazoletaucc.Plazoleta.service.RestauranteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/restaurantes")
@RequiredArgsConstructor
public class RestauranteController {

    private final RestauranteService restauranteService;

    @PostMapping
    public ResponseEntity<RestauranteResponseDTO> crearRestaurante(@Valid @RequestBody RestauranteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(restauranteService.crearRestaurante(dto));
    }

    // HU-09: el cliente lista los restaurantes disponibles (alfabetico y paginado).
    @GetMapping
    public ResponseEntity<PaginaResponseDTO<RestauranteListadoResponseDTO>> listarRestaurantes(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano) {
        return ResponseEntity.ok(restauranteService.listarRestaurantes(pagina, tamano));
    }
}
