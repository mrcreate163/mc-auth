package com.socialnetwork.auth.service;

import com.socialnetwork.auth.dto.response.CaptchaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private CaptchaService captchaService;

    @BeforeEach
    void setUp() {
        captchaService = new CaptchaService(redisTemplate);
        ReflectionTestUtils.setField(captchaService, "captchaTtlSeconds", 300L);
    }

    @Test
    void testGenerateCaptcha_shouldReturnValidCaptchaDto() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // When
        CaptchaDto result = captchaService.generateCaptcha();

        // Then
        assertNotNull(result);
        assertNotNull(result.getSecret());
        assertEquals(6, result.getSecret().length());
        assertNotNull(result.getImage());
        assertTrue(result.getImage().startsWith("data:image/png;base64,"));
        
        // Verify Redis was called with correct prefix and TTL
        verify(redisTemplate).opsForValue();
        verify(valueOperations).set(
            "captcha:" + result.getSecret(),
            "1",
            300L,
            TimeUnit.SECONDS
        );
    }

    @Test
    void testValidate_withValidCaptchaCode_shouldReturnTrue() {
        // Given
        String captchaCode = "ABC123";
        when(redisTemplate.delete("captcha:" + captchaCode)).thenReturn(true);

        // When
        boolean result = captchaService.validate(captchaCode);

        // Then
        assertTrue(result);
        verify(redisTemplate).delete("captcha:" + captchaCode);
    }

    @Test
    void testValidate_withInvalidCaptchaCode_shouldReturnFalse() {
        // Given
        String captchaCode = "INVALID";
        when(redisTemplate.delete("captcha:" + captchaCode)).thenReturn(false);

        // When
        boolean result = captchaService.validate(captchaCode);

        // Then
        assertFalse(result);
        verify(redisTemplate).delete("captcha:" + captchaCode);
    }

    @Test
    void testValidate_withNullCode_shouldReturnFalse() {
        // When
        boolean result = captchaService.validate(null);

        // Then
        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testValidate_withEmptyCode_shouldReturnFalse() {
        // When
        boolean result = captchaService.validate("");

        // Then
        assertFalse(result);
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void testValidate_whenRedisReturnsNull_shouldReturnFalse() {
        // Given
        String captchaCode = "CODE123";
        when(redisTemplate.delete("captcha:" + captchaCode)).thenReturn(null);

        // When
        boolean result = captchaService.validate(captchaCode);

        // Then
        assertFalse(result);
    }

    @Test
    void testGenerateCaptcha_secretContainsOnlyAllowedCharacters() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // When
        CaptchaDto result = captchaService.generateCaptcha();

        // Then
        String secret = result.getSecret();
        assertTrue(secret.matches("[A-Z0-9]+"), "Secret should contain only uppercase letters and digits");
    }
}
