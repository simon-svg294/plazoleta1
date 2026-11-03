package com.plazoletaucc.Plazoleta.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Plazoleta API",
                version = "1.0.0",
                description = "Documentacion de la API de la plazoleta de comida. " +
                        "Incluye autenticacion JWT, gestion de usuarios, restaurantes, platos y pedidos."
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtenido en POST /api/v1/auth/login. " +
                "Usar el boton Authorize con el valor del token."
)
public class OpenApiConfig {
}
