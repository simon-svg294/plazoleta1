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
                                "/api/v1/auth/login"
                        ).permitAll()
                        // HU-08: registro de cliente libre (se implementa en su HU)
                        // .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/cliente").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/registro-propietario")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/v1/usuarios/empleado")
                        .hasRole("PROPIETARIO")
                        .requestMatchers("/api/v1/restaurantes/**")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/v1/platos/**")
                        .hasRole("PROPIETARIO")
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
