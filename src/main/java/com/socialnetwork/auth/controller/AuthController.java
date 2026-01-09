package com.socialnetwork.auth.controller;

import com.socialnetwork.auth.dto.request.*;
import com.socialnetwork.auth.dto.response.CaptchaDto;
import com.socialnetwork.auth.dto.response.ErrorResponse;
import com.socialnetwork.auth.dto.response.TokenResponse;
import com.socialnetwork.auth.dto.response.ValidationResponse;
import com.socialnetwork.auth.service.AuthService;
import com.socialnetwork.auth.service.CaptchaService;
import com.socialnetwork.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Аутентификация", description = "API для управления аутентификацией и авторизацией пользователей")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final CaptchaService captchaService;

    /**
     * POST /api/v1/auth/register - Регистрация пользователя
     */
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрирует нового пользователя в системе. Требуется валидный email, пароль (минимум 6 символов), " +
                    "имя, фамилия и правильный код капчи. Пароли должны совпадать."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно зарегистрирован",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Пользователь успешно зарегистрирован"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных или неверная капча",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Пользователь с таким email уже существует",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Parameter(description = "Данные для регистрации пользователя", required = true)
            @Valid @RequestBody RegistrationDto dto) {
        log.info("Register endpoint called for email: {}", dto.getEmail());
        String result = authService.register(dto);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/auth/login - Аутентификация пользователя
     */
    @Operation(
            summary = "Вход в систему",
            description = "Аутентифицирует пользователя по email и паролю. В случае успеха возвращает JWT access и refresh токены."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная аутентификация, токены возвращены",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Неверный email или пароль",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "Учетные данные пользователя", required = true)
            @Valid @RequestBody AuthenticateRq dto) {
        log.info("Login endpoint called for email: {}", dto.getEmail());
        TokenResponse tokens = authService.login(dto);
        return ResponseEntity.ok(tokens);
    }

    /**
     * GET /api/v1/auth/validate - Проверка валидности токена
     */
    @Operation(
            summary = "Проверка валидности JWT токена",
            description = "Проверяет, является ли предоставленный JWT токен валидным и не истекшим"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Результат проверки токена",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Токен невалиден или истёк",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(
            @Parameter(description = "JWT токен для проверки", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestParam("token") String token) {
        log.info("Validate token endpoint called");
        ValidateTokenRequest dto = ValidateTokenRequest.builder()
                .token(token)
                .build();
        ValidationResponse response = authService.validateToken(dto);
        return ResponseEntity.ok(response.isValid());
    }

    /**
     * POST /api/v1/auth/refresh - Обновление токена
     */
    @Operation(
            summary = "Обновление access токена",
            description = "Обновляет истёкший access токен с помощью валидного refresh токена. Возвращает новую пару токенов."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Токены успешно обновлены",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh токен невалиден или истёк",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "Refresh токен для обновления", required = true)
            @Valid @RequestBody RefreshTokenRequest dto) {
        log.info("Refresh token endpoint called");
        TokenResponse tokens = authService.refreshAccessToken(dto);
        return ResponseEntity.ok(tokens);
    }

    /**
     * POST /api/v1/auth/logout - Выход из системы
     */
    @Operation(
            summary = "Выход из системы",
            description = "Завершает сеанс пользователя, инвалидирует все refresh токены. " +
                    "Опционально принимает access токен для добавления в черный список (blacklist) до истечения срока его действия."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный выход из системы",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Вы успешно вышли из системы"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверный формат ID пользователя",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
//            @Parameter(description = "ID пользователя", required = false, example = "550e8400-e29b-41d4-a716-446655440000")
//            @RequestHeader("X-User-Id") String userIdStr,
            @Parameter(description = "JWT токен в формате Bearer (опционально). Если указан, будет добавлен в blacklist",
                    example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        UUID userId = claimsUserId(authHeader);
        String accessToken = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        String result = authService.logout(userId, accessToken);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/auth/captcha - Генерация капчи
     */
    @Operation(
            summary = "Генерация капчи",
            description = "Генерирует новую капчу для защиты от ботов. Возвращает секретный код и изображение в формате Base64."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Капча успешно сгенерирована",
                    content = @Content(schema = @Schema(implementation = CaptchaDto.class))
            )
    })
    @GetMapping("/captcha")
    public ResponseEntity<CaptchaDto> generateCaptcha() {
        log.debug("Captcha generation endpoint called");
        CaptchaDto captcha = captchaService.generateCaptcha();
        return ResponseEntity.ok(captcha);
    }

    /**
     * POST /api/v1/auth/password/recovery/ - Запрос на восстановление пароля
     */
    @Operation(
            summary = "Запрос на восстановление пароля",
            description = "Отправляет письмо с ссылкой для восстановления пароля на указанный email"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Письмо для восстановления пароля отправлено",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Ссылка для восстановления пароля отправлена на email"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации email",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/password/recovery/")
    public ResponseEntity<String> recoverPassword(
            @Parameter(description = "Email для восстановления пароля", required = true)
            @Valid @RequestBody RecoveryPasswordLinkRq request) {
        log.info("Password recovery endpoint called for email: {}", request.getEmail());
        String result = authService.sendPasswordRecoveryLink(request);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/auth/change-password-link - Смена пароля по токену
     */
    @Operation(
            summary = "Смена пароля по токену",
            description = "Изменяет пароль пользователя используя токен из письма восстановления. Новый пароль должен быть минимум 6 символов."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Пароль успешно изменен",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Пароль успешно изменен"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Токен невалиден или истёк",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/change-password-link")
    public ResponseEntity<String> changePassword(
            @Parameter(description = "Данные для смены пароля", required = true)
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password endpoint called");
        String result = authService.changePassword(request);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/auth/change-email-link - Запрос на изменение email
     */
    @Operation(
            summary = "Запрос на изменение email",
            description = "Отправляет письмо с ссылкой для подтверждения изменения email на новый адрес"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Письмо для подтверждения отправлено на новый email",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Ссылка для подтверждения отправлена на новый email"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибка валидации данных или неверный формат ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Новый email уже используется другим пользователем",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/change-email-link")
    public ResponseEntity<String> changeEmail(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "Новый email адрес", required = true)
            @Valid @RequestBody ChangeEmailRequest request) {
        UUID userId = claimsUserId(authHeader);

        log.info("Change email endpoint called to: {} for user: {}", request.getNewEmail(), userId);
        String result = authService.sendChangeEmailLink(userId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/auth/confirm-email-change - Подтверждение изменения email
     */
    @Operation(
            summary = "Подтверждение изменения email",
            description = "Подтверждает изменение email пользователя по токену из письма"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email успешно изменен",
                    content = @Content(schema = @Schema(implementation = String.class, example = "Email успешно изменен"))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Токен невалиден или истёк",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/confirm-email-change")
    public ResponseEntity<String> confirmEmailChange(
            @Parameter(description = "Токен подтверждения из письма", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestParam("token") String token) {
        log.info("Confirm email change endpoint called");
        ConfirmEmailChangeRequest request = ConfirmEmailChangeRequest.builder()
                .token(token)
                .build();
        String result = authService.confirmEmailChange(request);
        return ResponseEntity.ok(result);
    }

    private UUID claimsUserId(String authHeader) {
        UUID userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);

            try {
                Claims claims = jwtService.validateAndExtractClaims(accessToken);
                userId = UUID.fromString(claims.get("userId", String.class));
                log.info("userId exctracted from token: {}", userId);
            } catch (Exception e) {
                log.error("Failed to validate user id from token: {}", accessToken);
            }
        }
        return userId;
    }
}
