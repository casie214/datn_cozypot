package com.example.datn_cozypot_spring_boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Lệnh này sẽ tự động khởi tạo SimpMessagingTemplate cho bạn
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt broker để gửi tin nhắn từ Server xuống Client
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đây là "cửa ngõ" kết nối mà SockJS ở Frontend gọi tới
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép gọi từ port 5173 của Vue
                .withSockJS();
    }
}