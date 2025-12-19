package com.socialnetwork.auth.service;

import com.socialnetwork.auth.entity.User;
import com.socialnetwork.auth.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; //15 минут

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 30 дней

    private static final  String USER_ID = "userId";
    private static final  String EMAIL = "email";

    /**
     * Генерация access токена (короткий промежуток, 15 минут)
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = Map.of(
                USER_ID, user.getId().toString(),
                EMAIL, user.getEmail()
        );
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Генерация refresh токена (длинный промежуток, 30 дней)
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = Map.of(
                USER_ID, user.getId().toString()
        );
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Валидация токена и извлечение claims
     */
    public Claims validateAndExtractClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    /**
     * Извлечение userId из токена
     */
    public UUID extractUserId(String token) {
        Claims claims = validateAndExtractClaims(token);
        String userIdStr = claims.get(USER_ID, String.class);
        return UUID.fromString(userIdStr);
    }

    /**
     * Извлечение email из токена
     */
    public String extractEmail(String token) {
        Claims claims = validateAndExtractClaims(token);
        return claims.get(EMAIL, String.class);
    }

    /**
     * Проверка истечения срока действия токена
     */
    public boolean isTokenExpired(String token) {
        Claims claims = validateAndExtractClaims(token);
        Date expiration = claims.getExpiration();
        return expiration.before(new Date());
    }

    /**
     * Получение ключа для подписи токена
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
