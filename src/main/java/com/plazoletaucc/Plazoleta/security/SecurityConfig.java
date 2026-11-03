package com.plazoletaucc.Plazoleta.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())
                )
                .authorizeHttpRequests(auth-> auth
                        .requestMatchers(
                                "/api/v1/auth/login",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // HU-08: registro de cliente libre
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/cliente").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro-propietario")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/empleado")
                        .hasRole("PROPIETARIO")
                        .requestMatchers(HttpMethod.POST, "/api/v1/restaurantes")
                        .hasRole("ADMINISTRADOR")
                        // HU-09: el cliente es el unico que lista los restaurantes
                        .requestMatchers(HttpMethod.GET, "/api/v1/restaurantes")
                        .hasRole("CLIENTE")
                        // HU-10: el cliente lista el menu de un restaurante
                        .requestMatchers(HttpMethod.GET, "/api/v1/platos")
                        .hasRole("CLIENTE")
                        .requestMatchers("/api/v1/platos/**")
                        .hasRole("PROPIETARIO")
                        // HU-11: el cliente crea pedidos
                        .requestMatchers(HttpMethod.POST, "/api/v1/pedidos")
                        .hasRole("CLIENTE")
                        // HU-12: el empleado lista los pedidos de su restaurante
                        .requestMatchers(HttpMethod.GET, "/api/v1/pedidos")
                        .hasRole("EMPLEADO")
                        // HU-13: el empleado se asigna a un pedido y lo pasa a EN_PREPARACION
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/asignar")
                        .hasRole("EMPLEADO")
                        // HU-14: el empleado notifica que el pedido esta listo
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/listo")
                        .hasRole("EMPLEADO")
                        // HU-15: el empleado entrega el pedido con el PIN del cliente
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/pedidos/*/entregar")
                        .hasRole("EMPLEADO")
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                            UsernamePasswordAuthenticationFilter.class
                )
        ;
        return http.build();
    }
}
