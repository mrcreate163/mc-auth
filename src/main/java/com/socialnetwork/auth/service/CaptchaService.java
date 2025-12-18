package com.socialnetwork.auth.service;

import com.socialnetwork.auth.dto.response.CaptchaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptchaService {

    private static final String CAPTCHA_PREFIX = "captcha:";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${captcha.ttl-seconds:300}")
    private long captchaTtlSeconds;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Генерация новой капчи
     */
    public CaptchaDto generateCaptcha() {
        String code = generateRandomCode(6);           // именно код, который введёт пользователь
        String imageBase64 = generateCaptchaImage(code);

        // Сохраняем капчу в Redis с TTL
        String key = CAPTCHA_PREFIX + code;
        redisTemplate.opsForValue().set(key, "1", captchaTtlSeconds, TimeUnit.SECONDS);
        log.debug("Captcha code saved to Redis with TTL {} seconds", captchaTtlSeconds);

        return CaptchaDto.builder()
                .secret(code)      // контракт не меняем: «secret» = сам код
                .image(imageBase64)
                .build();
    }

    /**
        * Валидация капчи
     */
    public boolean validate(String captchaCode) {
        if (captchaCode == null || captchaCode.isEmpty()) {
            return false;
        }

        // Если такой код был и ещё не использован — удаляем и считаем капчу валидной (одноразовая)
        String key = CAPTCHA_PREFIX + captchaCode;
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * Генерация случайного кода
     */
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * Генерация изображения капчи и конвертация в Base64
     */
    private String generateCaptchaImage(String text) {
        int width = 200;
        int height = 60;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Фон
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Рисуем текст
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(Color.BLACK);
        g2d.drawString(text, 30, 40);

        //Добавляем шум
        g2d.setColor(Color.GRAY);
        for (int i = 0; i < 5; i++) {
            int x1 = SECURE_RANDOM.nextInt(width);
            int y1 = SECURE_RANDOM.nextInt(height);
            int x2 = SECURE_RANDOM.nextInt(width);
            int y2 = SECURE_RANDOM.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        g2d.dispose();

        // Конвертация изображения в Base64
        return convertImageToBase64(image);
    }

    private String convertImageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Error converting image to Base64", e);
            return "";
        }
    }


}
