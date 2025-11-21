package com.socialnetwork.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    /**
     * Добавить токен в черный список
     * @param token токен для блокировки
     * @param expirationInMillis время жизни токена в миллисекундах
     */
    public void blacklistToken(String token, long expirationInMillis) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expirationInMillis, TimeUnit.MILLISECONDS);
        log.info("Token added to blacklist");
    }

    /**
     * Проверить, находится ли токен в черном списке
     * @param token токен для проверки
     * @return true если токен в черном списке
     */
    public boolean isTokenBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
