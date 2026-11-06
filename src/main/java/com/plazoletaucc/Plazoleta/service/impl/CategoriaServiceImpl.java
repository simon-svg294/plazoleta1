package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.CategoriaResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.CategoriaRequestDTO;
import com.plazoletaucc.Plazoleta.entity.Categoria;
import com.plazoletaucc.Plazoleta.repository.CategoriaRepository;
import com.plazoletaucc.Plazoleta.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {
    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponseDTO registrarCategoria(CategoriaRequestDTO categoriaRequestDTO) {
        Categoria categoria = Categoria.builder()
                .nombre(categoriaRequestDTO.getNombre())
                .descripcion(categoriaRequestDTO.getDescripcion())
                .build();
        Categoria categoriaGuardada = categoriaRepository.save(categoria);
        return CategoriaResponseDTO.builder()
                .id(categoriaGuardada.getId())
                .nombre(categoriaGuardada.getNombre())
                .descripcion(categoriaGuardada.getDescripcion())
                .build();
    }

    @Override
    public ArrayList<CategoriaResponseDTO> listarCategorias() {
        return categoriaRepository.findAll()
                .stream()
                .map(categoria -> CategoriaResponseDTO.builder()
                        .id(categoria.getId())
                        .nombre(categoria.getNombre())
                        .descripcion(categoria.getDescripcion())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
