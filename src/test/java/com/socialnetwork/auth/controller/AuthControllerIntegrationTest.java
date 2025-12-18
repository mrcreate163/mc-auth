package com.socialnetwork.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.socialnetwork.auth.config.TestRedisConfig;
import com.socialnetwork.auth.dto.request.AuthenticateRq;
import com.socialnetwork.auth.dto.request.RefreshTokenRequest;
import com.socialnetwork.auth.dto.request.RegistrationDto;
import com.socialnetwork.auth.entity.RefreshToken;
import com.socialnetwork.auth.entity.User;
import com.socialnetwork.auth.repository.RefreshTokenRepository;
import com.socialnetwork.auth.repository.UserRepository;
import com.socialnetwork.auth.service.CaptchaService;
import com.socialnetwork.auth.service.JwtService;
import com.socialnetwork.auth.service.KafkaProducerService;
import com.socialnetwork.auth.service.TokenBlacklistService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfig.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.bootstrap-servers=localhost:9093",
        "spring.kafka.producer.bootstrap-servers=localhost:9093",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration",
        "spring.main.allow-bean-definition-overriding=true"
})
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private CaptchaService captchaService;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Mock captcha validation to always pass
        when(captchaService.validate(anyString())).thenReturn(true);
        
        // Mock Kafka producer
        doNothing().when(kafkaProducerService).sendUserRegisteredEvent(any());
        
        // Mock TokenBlacklistService
        when(tokenBlacklistService.isTokenBlacklisted(anyString())).thenReturn(false);

        // Create test user
        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .password(passwordEncoder.encode("password123"))
                .isDeleted(false)
                .build();
    }

    @AfterEach
    void tearDown() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testRegister_withValidData_shouldReturn200() throws Exception {
        // Given
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword1("password123");
        registrationDto.setPassword2("password123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
        registrationDto.setCaptchaCode("ABC123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Registration successful"));
    }

    @Test
    void testRegister_withExistingEmail_shouldReturn409() throws Exception {
        // Given
        userRepository.save(testUser);

        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword1("password123");
        registrationDto.setPassword2("password123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
        registrationDto.setCaptchaCode("ABC123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void testRegister_withMismatchedPasswords_shouldReturn401() throws Exception {
        // Given
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail("newuser@example.com");
        registrationDto.setPassword1("password123");
        registrationDto.setPassword2("differentPassword");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");
        registrationDto.setCaptchaCode("ABC123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_withValidCredentials_shouldReturnTokens() throws Exception {
        // Given
        userRepository.save(testUser);

        AuthenticateRq authenticateRq = new AuthenticateRq();
        authenticateRq.setEmail("test@example.com");
        authenticateRq.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticateRq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void testLogin_withInvalidEmail_shouldReturn401() throws Exception {
        // Given
        AuthenticateRq authenticateRq = new AuthenticateRq();
        authenticateRq.setEmail("nonexistent@example.com");
        authenticateRq.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticateRq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_withInvalidPassword_shouldReturn401() throws Exception {
        // Given
        userRepository.save(testUser);

        AuthenticateRq authenticateRq = new AuthenticateRq();
        authenticateRq.setEmail("test@example.com");
        authenticateRq.setPassword("wrongPassword");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticateRq)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testValidateToken_withValidToken_shouldReturnTrue() throws Exception {
        // Given
        testUser = userRepository.save(testUser);
        String validToken = jwtService.generateAccessToken(testUser);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate")
                        .param("token", validToken))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testValidateToken_withInvalidToken_shouldReturnFalse() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/auth/validate")
                        .param("token", "invalid.jwt.token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void testRefreshToken_withValidRefreshToken_shouldReturnNewAccessToken() throws Exception {
        // Given
        testUser = userRepository.save(testUser);
        String refreshTokenValue = jwtService.generateRefreshToken(testUser);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(testUser)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .isRevoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshTokenValue);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.refreshToken", notNullValue()));
    }

    @Test
    void testRefreshToken_withInvalidToken_shouldReturn401() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid.refresh.token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogout_shouldReturn200() throws Exception {
        // Given
        testUser = userRepository.save(testUser);
        String userId = testUser.getId().toString();

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("X-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout successful"));
    }

    @Test
    void testGenerateCaptcha_shouldReturnCaptchaDto() throws Exception {
        // Given
        com.socialnetwork.auth.dto.response.CaptchaDto mockCaptcha = 
            new com.socialnetwork.auth.dto.response.CaptchaDto();
        mockCaptcha.setSecret("testSecret");
        mockCaptcha.setImage("data:image/png;base64,test");
        
        when(captchaService.generateCaptcha()).thenReturn(mockCaptcha);

        // When & Then
        mockMvc.perform(get("/api/v1/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secret", notNullValue()))
                .andExpect(jsonPath("$.image", notNullValue()));
    }

    @Test
    void testEndToEndFlow_registerLoginRefreshLogout() throws Exception {
        // 1. Register
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setEmail("endtoend@example.com");
        registrationDto.setPassword1("password123");
        registrationDto.setPassword2("password123");
        registrationDto.setFirstName("End");
        registrationDto.setLastName("ToEnd");
        registrationDto.setCaptchaCode("ABC123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk());

        // 2. Login
        AuthenticateRq authenticateRq = new AuthenticateRq();
        authenticateRq.setEmail("endtoend@example.com");
        authenticateRq.setPassword("password123");

        String loginResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticateRq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var tokenResponse = objectMapper.readValue(loginResponse, 
                com.socialnetwork.auth.dto.response.TokenResponse.class);

        // 3. Validate token
        mockMvc.perform(get("/api/v1/auth/validate")
                        .param("token", tokenResponse.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 4. Refresh token
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(tokenResponse.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));

        // 5. Logout
        User user = userRepository.findActiveUserByEmail("endtoend@example.com").get();
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("X-User-Id", user.getId().toString()))
                .andExpect(status().isOk());
    }
}
