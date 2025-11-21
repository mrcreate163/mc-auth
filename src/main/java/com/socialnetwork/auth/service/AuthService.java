package com.socialnetwork.auth.service;

import com.socialnetwork.auth.dto.kafka.AccountChangedEvent;
import com.socialnetwork.auth.dto.kafka.UserRegisteredEvent;
import com.socialnetwork.auth.dto.request.*;
import com.socialnetwork.auth.dto.response.TokenResponse;
import com.socialnetwork.auth.dto.response.ValidationResponse;
import com.socialnetwork.auth.entity.EmailChangeToken;
import com.socialnetwork.auth.entity.PasswordResetToken;
import com.socialnetwork.auth.entity.RefreshToken;
import com.socialnetwork.auth.entity.User;
import com.socialnetwork.auth.exception.CaptchaValidationException;
import com.socialnetwork.auth.exception.InvalidCredentialsException;
import com.socialnetwork.auth.exception.InvalidTokenException;
import com.socialnetwork.auth.exception.UserAlreadyExistsExcpetion;
import com.socialnetwork.auth.repository.EmailChangeTokenRepository;
import com.socialnetwork.auth.repository.PasswordResetTokenRepository;
import com.socialnetwork.auth.repository.RefreshTokenRepository;
import com.socialnetwork.auth.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CaptchaService captchaService;
    private final KafkaProducerService kafkaProducerService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final EmailChangeTokenRepository emailChangeTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;


    /**
     * Регистрация пользователя
     */
    @Transactional
    public String register(RegistrationDto dto) {
        // 1. Проверка капчи
        if (!captchaService.validate(dto.getCaptchaCode())) {
            throw new CaptchaValidationException("Invalid captcha code");
        }

        // 2. Проверка совпадения паролей
        if (!dto.getPassword1().equals(dto.getPassword2())) {
            throw new InvalidCredentialsException("Passwords do not match");
        }

        // 3. Проверка уникальности email
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsExcpetion("Email is already registered");
        }

        // 4. Создание и сохранение пользователя
        User user = User.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword1()))
                .isDeleted(false)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User {} registered successfully", savedUser.getId());

        // 5. Публикация события в Kafka для MC-ACCOUNT
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .firstname(dto.getFirstName())
                .lastname(dto.getLastName())
                .registeredAt(savedUser.getCreatedAt())
                .build();

        kafkaProducerService.sendUserRegisteredEvent(event);

        return "Registration successful";
    }

    /**
     * Аутентификация пользователя и генерация токенов
     */
    @Transactional
    public TokenResponse login(AuthenticateRq dto) {
        log.info("Attempting to authenticate user with email: {}", dto.getEmail());

        // 1. Поиск пользователя по email
        User user = userRepository.findActiveUserByEmail(dto.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        // 2. Проверка пароля
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // 3. Генерация токенов
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. Сохранение refresh токена в БД
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(refreshToken)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isRevoked(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);
        log.info("User {} authenticated successfully", user.getEmail());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Проверка валидности токена (для Gateway и других сервисов)
     */
    public ValidationResponse validateToken(ValidateTokenRequest request) {
        try {
            // Проверить, не находится ли токен в черном списке
            if (tokenBlacklistService.isTokenBlacklisted(request.getToken())) {
                log.warn("Token is blacklisted");
                return ValidationResponse.builder()
                        .valid(false)
                        .build();
            }

            Claims claims = jwtService.validateAndExtractClaims(request.getToken());

            return ValidationResponse.builder()
                    .valid(true)
                    .userId(UUID.fromString(claims.get("userId", String.class)))
                    .email(claims.get("email", String.class))
                    .build();
        } catch (Exception e) {
            log.error("Invalid token provided: {}", request.getToken());
            return ValidationResponse.builder()
                    .valid(false)
                    .build();
        }
    }

    /**
     * Обновление access токена с помощью refresh токена
     */
    @Transactional
    public TokenResponse refreshAccessToken(RefreshTokenRequest request) {

        // 1. Найти refresh токен в БД
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        // 2. Проверить, не отозван ли токен
        if (refreshToken.getIsRevoked()) {
            throw new InvalidCredentialsException("Refresh token is revoked");
        }

        // 3. Проверить не истёк ли токен
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token is expired");
        }

        // 4. Проверить JWT подпись
        jwtService.validateAndExtractClaims(request.getRefreshToken());

        // 4. Генерация нового access токена
        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateAccessToken(user);

        log.info("User {} refreshed successfully", user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .build();
    }

    /**
     * Выход пользователя - отзыв всех refresh токенов и добавление access токена в blacklist
     */
    @Transactional
    public String logout(UUID userId, String accessToken) {
        // Отзыв всех refresh токенов пользователя
        refreshTokenRepository.revokeAllUserTokens(userId);
        
        // Добавить access токен в черный список, если он предоставлен
        if (accessToken != null && !accessToken.isEmpty()) {
            try {
                // Получить время истечения токена из его claims
                Claims claims = jwtService.validateAndExtractClaims(accessToken);
                long expirationTime = claims.getExpiration().getTime();
                long currentTime = System.currentTimeMillis();
                long ttl = expirationTime - currentTime;
                
                // Добавить в blacklist только если токен еще не истек
                if (ttl > 0) {
                    tokenBlacklistService.blacklistToken(accessToken, ttl);
                    log.info("Access token added to blacklist for user: {}", userId);
                }
            } catch (Exception e) {
                log.warn("Failed to blacklist access token during logout: {}", e.getMessage());
                // Не прерываем процесс logout, если не удалось добавить в blacklist
            }
        }
        
        return "Logout successful";
    }

    /**
     * Отправка ссылки для восстановления пароля
     */
    @Transactional
    public String sendPasswordRecoveryLink(RecoveryPasswordLinkRq request) {
        log.info("Password recovery requested for email: {}", request.getEmail());

        User user = userRepository.findActiveUserByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email not found"));

        //Генерация токена для сброса пароля
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Отправка email с ссылкой для сброса пароля
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password recovery link sent to email: {}", request.getEmail());
        return "Password recovery link sent";
    }

    /**
     * Смена пароля по токену восстановления
     */
    @Transactional
    public String changePassword(ChangePasswordRequest request) {
        log.info("Attempting to change password with token: {}", request.getToken());

        //Найти токен восстановления
        PasswordResetToken resetToken =
                passwordResetTokenRepository.findByToken(request.getToken())
                        .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        //Проверки
        if (resetToken.getIsUsed() ) {
            throw new InvalidTokenException("Token is already used");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token is expired");
        }

        //Изменить пароль
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        //Отметить токен как использованный
        resetToken.setIsUsed(true);
        passwordResetTokenRepository.save(resetToken);

        //Отозвать все refresh токены пользователя
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        log.info("Password changed successfully for user: {}", user.getEmail());
        return  "Password changed successfully";
    }

    /**
     * Отправка ссылки для изменения email
     */
    @Transactional
    public String sendChangeEmailLink(UUID userId, ChangeEmailRequest request) {
        log.info("Change email requested to: {} for user: {}", request.getNewEmail(), userId);

        // Найти пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        // Проверить, не занят ли новый email
        if (userRepository.existsByEmail(request.getNewEmail())) {
            throw new UserAlreadyExistsExcpetion("Email is already in use");
        }

        // Генерация токена для подтверждения смены email
        String token = UUID.randomUUID().toString();

        EmailChangeToken changeToken = EmailChangeToken.builder()
                .token(token)
                .user(user)
                .newEmail(request.getNewEmail())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .isUsed(false)
                .build();

        emailChangeTokenRepository.save(changeToken);

        // Отправка email с ссылкой для подтверждения
        emailService.sendEmailChangeConfirmation(request.getNewEmail(), token);

        log.info("Email change confirmation link sent to: {}", request.getNewEmail());
        return "Ссылка для подтверждения нового email отправлена";
    }

    /**
     * Подтверждение изменения email по токену
     */
    @Transactional
    public String confirmEmailChange(ConfirmEmailChangeRequest request) {
        log.info("Attempting to confirm email change with token: {}", request.getToken());

        // Найти токен изменения email
        EmailChangeToken changeToken = emailChangeTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        // Проверки
        if (changeToken.getIsUsed()) {
            throw new InvalidTokenException("Token is already used");
        }
        if (changeToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Token is expired");
        }

        // Проверить, не занят ли новый email (на случай если кто-то успел зарегистрироваться)
        if (userRepository.existsByEmail(changeToken.getNewEmail())) {
            throw new UserAlreadyExistsExcpetion("Email is already in use");
        }

        // Изменить email пользователя
        User user = changeToken.getUser();
        String oldEmail = user.getEmail();
        user.setEmail(changeToken.getNewEmail());
        userRepository.save(user);

        // Отметить токен как использованный
        changeToken.setIsUsed(true);
        emailChangeTokenRepository.save(changeToken);

        // Отозвать все refresh токены пользователя
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        // Отправить событие в Kafka о смене email
        AccountChangedEvent accountChangedEvent = AccountChangedEvent.builder()
                .userId(user.getId())
                .newEmail(user.getEmail())
                .changedAt(LocalDateTime.now())
                .build();
        kafkaProducerService.sendAccountChangedEvent(accountChangedEvent);

        log.info("Email changed successfully from {} to {} for user: {}", oldEmail, user.getEmail(), user.getId());
        return "Email changed successfully";
    }



}
