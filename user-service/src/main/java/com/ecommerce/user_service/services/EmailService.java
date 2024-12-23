package com.ecommerce.user_service.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@ecommerce.com");
            message.setTo(to);
            message.setSubject("Şifre Sıfırlama İsteği");
            message.setText("Şifrenizi sıfırlamak için aşağıdaki linke tıklayın:\n\n" +
                    "http://localhost:3000/reset-password?token=" + token);

            mailSender.send(message);
            log.info("Şifre sıfırlama maili gönderildi: {}", to);
        } catch (Exception e) {
            log.error("Mail gönderimi başarısız: {}", to, e);
            throw new RuntimeException("Mail gönderilemedi");
        }
    }
} 