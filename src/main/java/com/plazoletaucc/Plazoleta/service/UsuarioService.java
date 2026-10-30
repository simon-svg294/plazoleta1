package com.plazoletaucc.Plazoleta.service;

import com.plazoletaucc.Plazoleta.dto.reponse.RegistroResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.ClienteRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.EmpleadoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.RegistroRequestDTO;

public interface UsuarioService {
    RegistroResponseDTO registrarUsuario(RegistroRequestDTO registroRequestDTO);
    RegistroResponseDTO crearEmpleado(EmpleadoRequestDTO empleadoRequestDTO, String correoPropietario);
    RegistroResponseDTO registrarCliente(ClienteRequestDTO clienteRequestDTO);
}
