package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.RegistroResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.EmpleadoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.RegistroRequestDTO;
import com.plazoletaucc.Plazoleta.service.UsuarioService;
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
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PostMapping("/registro-propietario")
    public ResponseEntity<RegistroResponseDTO> registrarUsuario(@Valid @RequestBody RegistroRequestDTO registroRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.registrarUsuario(registroRequestDTO));
    }

    @PostMapping("/empleado")
    public ResponseEntity<RegistroResponseDTO> crearEmpleado(@Valid @RequestBody EmpleadoRequestDTO empleadoRequestDTO,
                                                             Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.crearEmpleado(empleadoRequestDTO, authentication.getName()));
    }
}
