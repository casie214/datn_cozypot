package com.example.datn_cozypot_spring_boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailReservedConfig {
    @Bean("bookingMailSender")
    public JavaMailSender bookingMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();

        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername("5762382@gmail.com");
        sender.setPassword("jbxj lgwi nlxw mnnm");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.debug", "true"); // Bật true để xem log chi tiết nếu vẫn lỗi

        // --- DÒNG QUAN TRỌNG ĐỂ FIX LỖI PKIX ---
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        // ---------------------------------------

        return sender;
    }
}