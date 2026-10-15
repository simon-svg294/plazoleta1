package com.plazoletaucc.Plazoleta.service.impl;

import com.plazoletaucc.Plazoleta.dto.reponse.LoginResponseDTO;
import com.plazoletaucc.Plazoleta.dto.request.LoginRequestDTO;
import com.plazoletaucc.Plazoleta.entity.Usuario;
import com.plazoletaucc.Plazoleta.exception.CredencialesInvalidasException;
import com.plazoletaucc.Plazoleta.repository.UsuarioRepository;
import com.plazoletaucc.Plazoleta.security.JwtService;
import com.plazoletaucc.Plazoleta.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        // HU-05: intentos ilimitados, sin bloqueo ni contador
        Usuario usuario = usuarioRepository.findByCorreo(loginRequestDTO.getCorreo())
                .orElseThrow(() -> new CredencialesInvalidasException("Usuario no encontrado"));

        boolean claveCorrecta = passwordEncoder.matches(loginRequestDTO.getClave(), usuario.getClave());
        if (!claveCorrecta) {
            throw new CredencialesInvalidasException("Clave incorrecta");
        }

        //Inicia sesión
        //Llamar al método para generar el token
        String token = jwtService.generateToken(usuario.getCorreo(), usuario.getRol());

        return LoginResponseDTO.builder().token(token).build();
    }
}
