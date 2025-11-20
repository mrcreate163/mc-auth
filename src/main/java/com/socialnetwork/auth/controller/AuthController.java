package com.socialnetwork.auth.controller;

import com.socialnetwork.auth.dto.request.*;
import com.socialnetwork.auth.dto.request.ConfirmEmailChangeRequest;
import com.socialnetwork.auth.dto.response.CaptchaDto;
import com.socialnetwork.auth.dto.response.TokenResponse;
import com.socialnetwork.auth.dto.response.ValidationResponse;
import com.socialnetwork.auth.service.AuthService;
import com.socialnetwork.auth.service.CaptchaService;
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
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationDto dto) {
        log.info("Register endpoint called for email: {}", dto.getEmail());
        String result = authService.register(dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody AuthenticateRq  dto) {
        log.info("Login endpoint called for email: {}", dto.getEmail());
        TokenResponse tokens = authService.login(dto);
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam("token") String token) {
        log.info("Validate token endpoint called");
        ValidateTokenRequest dto = ValidateTokenRequest.builder()
                .token(token)
                .build();
        ValidationResponse response = authService.validateToken(dto);
        return ResponseEntity.ok(response.isValid());
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest dto) {
        log.info("Refresh token endpoint called");
        TokenResponse tokens = authService.refreshAccessToken(dto);
        return ResponseEntity.ok(tokens);
    }

    /**
     * POST /api/v1/auth/logout - Выход из системы
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("X-User-Id") String userIdStr,
                                         @RequestHeader(value = "Authorization", required = false) String authHeader) {
        log.info("Logout endpoint called for user: {}", userIdStr);
        UUID userId = UUID.fromString(userIdStr);
        
        // Извлечь access token из заголовка Authorization
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
    @GetMapping("/captcha")
    public ResponseEntity<CaptchaDto> generateCaptcha() {
        log.debug("Captcha generation endpoint called");
        CaptchaDto captcha = captchaService.generateCaptcha();
        return ResponseEntity.ok(captcha);
    }

    /**
     * POST /api/v1/auth/password/recovery/ - Запрос на восстановление пароля
     */
    @PostMapping("/password/recovery/")
    public ResponseEntity<String> recoverPassword(@Valid @RequestBody RecoveryPasswordLinkRq request) {
        log.info("Password recovery endpoint called for email: {}", request.getEmail());
        String result = authService.sendPasswordRecoveryLink(request);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/auth/change-password-link - Смена пароля по токену
     */
    @PostMapping("/change-password-link")
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        log.info("Change password endpoint called");
        String result = authService.changePassword(request);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/auth/change-email-link - Запрос на изменение email
     */
    @PostMapping("/change-email-link")
    public ResponseEntity<String> changeEmail(@RequestHeader("X-User-Id") String userIdStr,
                                               @Valid @RequestBody ChangeEmailRequest request) {
        log.info("Change email endpoint called to: {} for user: {}", request.getNewEmail(), userIdStr);
        UUID userId = UUID.fromString(userIdStr);
        String result = authService.sendChangeEmailLink(userId, request);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/auth/confirm-email-change - Подтверждение изменения email
     */
    @GetMapping("/confirm-email-change")
    public ResponseEntity<String> confirmEmailChange(@RequestParam("token") String token) {
        log.info("Confirm email change endpoint called");
        ConfirmEmailChangeRequest request = ConfirmEmailChangeRequest.builder()
                .token(token)
                .build();
        String result = authService.confirmEmailChange(request);
        return ResponseEntity.ok(result);
    }
}
