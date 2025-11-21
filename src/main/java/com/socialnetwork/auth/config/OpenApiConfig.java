package com.socialnetwork.auth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) документации
 */
@Configuration
public class OpenApiConfig {

    /**
     * Настройка OpenAPI документации
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MC-Auth Microservice API")
                        .version("1.0.0")
                        .description("""
                                API микросервиса аутентификации и авторизации для социальной сети.
                                
                                Основные возможности:
                                - Регистрация новых пользователей с проверкой капчи
                                - Аутентификация (вход) и получение JWT токенов
                                - Обновление access token через refresh token
                                - Валидация JWT токенов
                                - Выход из системы (logout)
                                - Восстановление пароля через email
                                - Смена пароля по токену
                                - Изменение email с подтверждением
                                - Генерация капчи для защиты от ботов
                                """)
                        .contact(new Contact()
                                .name("Development Team")
                                .email("support@socialnetwork.com")
                                .url("https://github.com/mrcreate163/mc-auth"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введите JWT токен в формате: Bearer {token}")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }
}
