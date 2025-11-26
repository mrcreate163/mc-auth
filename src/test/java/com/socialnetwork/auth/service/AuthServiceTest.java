package com.socialnetwork.auth.service;

import com.socialnetwork.auth.dto.request.AuthenticateRq;
import com.socialnetwork.auth.dto.request.RefreshTokenRequest;
import com.socialnetwork.auth.dto.request.RegistrationDto;
import com.socialnetwork.auth.dto.request.ValidateTokenRequest;
import com.socialnetwork.auth.dto.response.TokenResponse;
import com.socialnetwork.auth.dto.response.ValidationResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailChangeTokenRepository emailChangeTokenRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegistrationDto registrationDto;
    private AuthenticateRq authenticateRq;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encodedPassword")
                .isDeleted(false)
                .build();

        registrationDto = new RegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword1("password123");
        registrationDto.setPassword2("password123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
        registrationDto.setCaptchaCode("ABC123");

        authenticateRq = new AuthenticateRq();
        authenticateRq.setEmail("test@example.com");
        authenticateRq.setPassword("password123");
    }

    @Test
    void testRegister_withValidData_shouldSucceed() {
        // Given
        when(captchaService.validate(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        String result = authService.register(registrationDto);

        // Then
        assertEquals("Registration successful", result);
        verify(userRepository).save(any(User.class));
        verify(kafkaProducerService).sendUserRegisteredEvent(any());
    }

    @Test
    void testRegister_withInvalidCaptcha_shouldThrowException() {
        // Given
        when(captchaService.validate(anyString())).thenReturn(false);

        // When & Then
        assertThrows(CaptchaValidationException.class, () -> {
            authService.register(registrationDto);
        });
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_withMismatchedPasswords_shouldThrowException() {
        // Given
        registrationDto.setPassword2("differentPassword");
        when(captchaService.validate(anyString())).thenReturn(true);

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.register(registrationDto);
        });
    }

    @Test
    void testRegister_withExistingEmail_shouldThrowException() {
        // Given
        when(captchaService.validate(anyString())).thenReturn(true);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        assertThrows(UserAlreadyExistsExcpetion.class, () -> {
            authService.register(registrationDto);
        });
    }

    @Test
    void testLogin_withValidCredentials_shouldReturnTokens() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refreshToken");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(new RefreshToken());

        // When
        TokenResponse response = authService.login(authenticateRq);

        // Then
        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertEquals("refreshToken", response.getRefreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void testLogin_withInvalidEmail_shouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(authenticateRq);
        });
    }

    @Test
    void testLogin_withInvalidPassword_shouldThrowException() {
        // Given
        when(userRepository.findActiveUserByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(authenticateRq);
        });
    }

    @Test
    void testValidateToken_withValidToken_shouldReturnValidResponse() {
        // Given
        ValidateTokenRequest request = ValidateTokenRequest.builder()
                .token("validToken")
                .build();
        
        when(tokenBlacklistService.isTokenBlacklisted(anyString())).thenReturn(false);
        
        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
        when(claims.get("userId", String.class)).thenReturn(testUser.getId().toString());
        when(claims.get("email", String.class)).thenReturn(testUser.getEmail());
        when(jwtService.validateAndExtractClaims(anyString())).thenReturn(claims);

        // When
        ValidationResponse response = authService.validateToken(request);

        // Then
        assertTrue(response.isValid());
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getEmail(), response.getEmail());
    }

    @Test
    void testValidateToken_withInvalidToken_shouldReturnInvalidResponse() {
        // Given
        ValidateTokenRequest request = ValidateTokenRequest.builder()
                .token("invalidToken")
                .build();
        
        when(tokenBlacklistService.isTokenBlacklisted(anyString())).thenReturn(false);
        when(jwtService.validateAndExtractClaims(anyString())).thenThrow(new InvalidTokenException("Invalid token"));

        // When
        ValidationResponse response = authService.validateToken(request);

        // Then
        assertFalse(response.isValid());
    }

    @Test
    void testRefreshAccessToken_withValidRefreshToken_shouldReturnNewAccessToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("validRefreshToken");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("validRefreshToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .isRevoked(false)
                .build();

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));
        when(jwtService.validateAndExtractClaims(anyString())).thenReturn(mock(io.jsonwebtoken.Claims.class));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("newAccessToken");

        // When
        TokenResponse response = authService.refreshAccessToken(request);

        // Then
        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertEquals("validRefreshToken", response.getRefreshToken());
    }

    @Test
    void testRefreshAccessToken_withRevokedToken_shouldThrowException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("revokedToken");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("revokedToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .isRevoked(true)
                .build();

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> {
            authService.refreshAccessToken(request);
        });
    }

    @Test
    void testRefreshAccessToken_withExpiredToken_shouldThrowException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expiredToken");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("expiredToken")
                .user(testUser)
                .expiresAt(LocalDateTime.now().minusDays(1))
                .isRevoked(false)
                .build();

        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(refreshToken));

        // When & Then
        assertThrows(InvalidTokenException.class, () -> {
            authService.refreshAccessToken(request);
        });
    }

    @Test
    void testLogout_shouldRevokeAllUserTokens() {
        // Given
        UUID userId = testUser.getId();
        String accessToken = "sampleAccessToken";

        // When
        String result = authService.logout(userId, accessToken);

        // Then
        assertEquals("Logout successful", result);
        verify(refreshTokenRepository).revokeAllUserTokens(userId);
    }
}
