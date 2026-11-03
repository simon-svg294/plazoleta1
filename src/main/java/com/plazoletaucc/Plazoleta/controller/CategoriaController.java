package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.CategoriaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.CategoriaRequestDTO;
import com.plazoletaucc.Plazoleta.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Gestion de categorias de platos")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @Operation(summary = "Registrar categoria", description = "Crea una nueva categoria de platos.")
    @PostMapping("/registro")
    public ResponseEntity<CategoriaResponseDTO> registrarCategoria(@RequestBody CategoriaRequestDTO categoriaRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoriaService.registrarCategoria(categoriaRequestDTO));
    }

    @Operation(summary = "Listar categorias", description = "Lista todas las categorias de platos disponibles.")
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listarCategorias() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(categoriaService.listarCategorias());
    }
}
