package com.socialnetwork.auth.service;

import com.socialnetwork.auth.entity.User;
import com.socialnetwork.auth.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", "bXlTZWNyZXRLZXlGb3JKV1RUb2tlbkdlbmVyYXRpb25NaW5pbXVtMjU2Qml0c0xvbmdCYXNlNjRFbmNvZGVkPT0=");
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 2592000000L);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("hashedPassword")
                .isDeleted(false)
                .build();
    }

    @Test
    void testGenerateAccessToken_shouldReturnValidToken() {
        // When
        String token = jwtService.generateAccessToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts
    }

    @Test
    void testGenerateRefreshToken_shouldReturnValidToken() {
        // When
        String token = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void testValidateAndExtractClaims_withValidToken_shouldReturnClaims() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Claims claims = jwtService.validateAndExtractClaims(token);

        // Then
        assertNotNull(claims);
        assertEquals(testUser.getEmail(), claims.getSubject());
        assertEquals(testUser.getId().toString(), claims.get("userId", String.class));
        assertEquals(testUser.getEmail(), claims.get("email", String.class));
    }

    @Test
    void testValidateAndExtractClaims_withInvalidToken_shouldThrowException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThrows(InvalidTokenException.class, () -> jwtService.validateAndExtractClaims(invalidToken));
    }

    @Test
    void testExtractUserId_shouldReturnCorrectUserId() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        UUID extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(testUser.getId(), extractedUserId);
    }

    @Test
    void testExtractEmail_shouldReturnCorrectEmail() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        String extractedEmail = jwtService.extractEmail(token);

        // Then
        assertEquals(testUser.getEmail(), extractedEmail);
    }

    @Test
    void testIsTokenExpired_withValidToken_shouldReturnFalse() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        boolean isExpired = jwtService.isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testAccessTokenContainsRequiredClaims() {
        // Given
        String token = jwtService.generateAccessToken(testUser);

        // When
        Claims claims = jwtService.validateAndExtractClaims(token);

        // Then
        assertTrue(claims.containsKey("userId"));
        assertTrue(claims.containsKey("email"));
        assertNotNull(claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testRefreshTokenContainsRequiredClaims() {
        // Given
        String token = jwtService.generateRefreshToken(testUser);

        // When
        Claims claims = jwtService.validateAndExtractClaims(token);

        // Then
        assertTrue(claims.containsKey("userId"));
        assertNotNull(claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void testGeneratedTokensAreDifferent() throws InterruptedException {
        // When
        String token1 = jwtService.generateAccessToken(testUser);
        Thread.sleep(1000); // Wait 1 second to ensure different timestamp
        String token2 = jwtService.generateAccessToken(testUser);

        // Then
        assertNotEquals(token1, token2); // Different because of timestamp
    }
}
