package com.plazoletaucc.Plazoleta.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth-> auth
                        .requestMatchers(
                                "/api/v1/categorias/**",
                                "/api/v1/auth/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/platos/**")
                        .hasRole("PROPIETARIO")
                        .requestMatchers("/api/v1/usuarios/registro-propietario")
                        .hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/v1/restaurantes/**")
                        .hasRole("ADMINISTRADOR")
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
