package com.socialnetwork.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@socialnetwork.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    /**
     * Отправка email для восстановления пароля
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetLink = baseUrl + "/api/v1/auth/change-password-link?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Recovery Request");
            message.setText("Hello,\n\n" +
                    "You have requested to reset your password. Please click the link below to reset your password:\n\n" +
                    resetLink + "\n\n" +
                    "This link will expire in 1 hour.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Social Network Team");

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Отправка email для подтверждения нового email
     */
    public void sendEmailChangeConfirmation(String toEmail, String token) {
        try {
            String confirmLink = baseUrl + "/api/v1/auth/confirm-email-change?token=" + token;
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Change Confirmation");
            message.setText("Hello,\n\n" +
                    "You have requested to change your email address. Please click the link below to confirm your new email:\n\n" +
                    confirmLink + "\n\n" +
                    "This link will expire in 1 hour.\n\n" +
                    "If you did not request this, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Social Network Team");

            mailSender.send(message);
            log.info("Email change confirmation sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email change confirmation to: {}", toEmail, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
