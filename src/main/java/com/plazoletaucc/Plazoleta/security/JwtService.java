package com.plazoletaucc.Plazoleta.security;

import com.plazoletaucc.Plazoleta.entity.Rol;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    //1.Obtener una clave secreta ok
    // 2.Generar el token con el claim ok
    //3. Métodos para extraer: correo, rol ok
    //4. Método para validar el token

    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long JWT_EXPIRATION;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateToken(String correo, Rol rol) {
        return Jwts.builder()
                .subject(correo)
                .claim("rol", rol.name())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + JWT_EXPIRATION)
                )
                .signWith(getSecretKey())
                .compact();
    }

    public String extractCorreo (String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String extractRol (String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token).getPayload().get("rol", String.class);
    }

    public boolean isTokenValido (String token) {
        try{
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
