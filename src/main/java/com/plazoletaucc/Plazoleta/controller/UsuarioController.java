package com.plazoletaucc.Plazoleta.controller;

import com.plazoletaucc.Plazoleta.dto.reponse.RegistroResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.ClienteRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.EmpleadoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.RegistroRequestDTO;
import com.plazoletaucc.Plazoleta.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Usuarios", description = "Registro de propietarios, empleados y clientes")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @Operation(summary = "Registrar propietario", description = "HU-01: el administrador registra un propietario.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/registro-propietario")
    public ResponseEntity<RegistroResponseDTO> registrarUsuario(@Valid @RequestBody RegistroRequestDTO registroRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.registrarUsuario(registroRequestDTO));
    }

    @Operation(summary = "Crear empleado", description = "HU-06: el propietario crea un empleado en su restaurante.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/empleado")
    public ResponseEntity<RegistroResponseDTO> crearEmpleado(@Valid @RequestBody EmpleadoRequestDTO empleadoRequestDTO,
                                                             Authentication authentication){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.crearEmpleado(empleadoRequestDTO, authentication.getName()));
    }

    @Operation(summary = "Registrar cliente", description = "HU-08: registro publico de una cuenta de cliente.")
    @PostMapping("/cliente")
    public ResponseEntity<RegistroResponseDTO> registrarCliente(@Valid @RequestBody ClienteRequestDTO clienteRequestDTO){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usuarioService.registrarCliente(clienteRequestDTO));
    }
}
