package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.RegistroResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.EmpleadoRequestDTO;
import com.plazoletaucc.Plazoleta.dto.request.RegistroRequestDTO;
import com.plazoletaucc.Plazoleta.entity.Rol;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.CorreoDuplicadoException;
import com.plazoletaucc.Plazoleta.exception.DocumentoDuplicadoException;
import com.plazoletaucc.Plazoleta.exception.MenorDeEdadException;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegistroResponseDTO registrarUsuario(RegistroRequestDTO registroRequestDTO) {
        if(usuarioRepository.existsByCorreo(registroRequestDTO.getCorreo())){
            throw new CorreoDuplicadoException("El correo ya existe");
        }

        int edad = Period.between(registroRequestDTO.getFechaDeNacimiento(), LocalDate.now()).getYears();
        if (edad < 18) {
            throw new MenorDeEdadException("El usuario debe ser mayor de edad (18 anios o mas)");
        }

        Usuario usuario = Usuario.builder()
                .nombre(registroRequestDTO.getNombre())
                .apellido(registroRequestDTO.getApellido())
                .documentoDeIdentidad(registroRequestDTO.getDocumentoDeIdentidad())
                .celular(registroRequestDTO.getCelular())
                .fechaDeNacimiento(registroRequestDTO.getFechaDeNacimiento())
                .correo(registroRequestDTO.getCorreo())
                .clave(passwordEncoder.encode(registroRequestDTO.getClave()))
                .rol(Rol.PROPIETARIO)
                .build();

        Usuario usuarioGuardado =  usuarioRepository.save(usuario);

        return RegistroResponseDTO.builder()
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .documentoDeIdentidad(usuario.getDocumentoDeIdentidad())
                .celular(usuario.getCelular())
                .build();
    }

    @Override
    public RegistroResponseDTO crearEmpleado(EmpleadoRequestDTO empleadoRequestDTO, String correoPropietario) {
        if(usuarioRepository.existsByCorreo(empleadoRequestDTO.getCorreo())){
            throw new CorreoDuplicadoException("El correo ya existe");
        }
        if(usuarioRepository.existsByDocumentoDeIdentidad(empleadoRequestDTO.getDocumentoDeIdentidad())){
            throw new DocumentoDuplicadoException("El documento de identidad ya existe");
        }

        Usuario empleado = Usuario.builder()
                .nombre(empleadoRequestDTO.getNombre())
                .apellido(empleadoRequestDTO.getApellido())
                .documentoDeIdentidad(empleadoRequestDTO.getDocumentoDeIdentidad())
                .celular(empleadoRequestDTO.getCelular())
                .correo(empleadoRequestDTO.getCorreo())
                .clave(passwordEncoder.encode(empleadoRequestDTO.getClave()))
                .rol(Rol.EMPLEADO)
                .build();

        Usuario empleadoGuardado = usuarioRepository.save(empleado);

        return RegistroResponseDTO.builder()
                .nombre(empleadoGuardado.getNombre())
                .apellido(empleadoGuardado.getApellido())
                .correo(empleadoGuardado.getCorreo())
                .documentoDeIdentidad(empleadoGuardado.getDocumentoDeIdentidad())
                .celular(empleadoGuardado.getCelular())
                .build();
    }


}
