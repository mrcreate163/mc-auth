package com.socialnetwork.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Настройка безопасности HTTP
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF (не нужен для stateless API)
                .csrf(AbstractHttpConfigurer::disable)

                // Настройка авторизации запросов
                .authorizeHttpRequests(auth -> auth
                        // Все эндпоинты auth публичны
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Для actuator endpoints (если будут)
                        .requestMatchers("/actuator/**").permitAll()

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // Stateless сессии (без сохранения состояния на сервере)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }

    /**
     * Password Encoder для хеширования паролей
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}



