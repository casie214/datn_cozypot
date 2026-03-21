package com.example.datn_cozypot_spring_boot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ThongBaoService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotify(String title, String content) {
        Map<String, String> data = new HashMap<>();
        data.put("id", String.valueOf(System.currentTimeMillis())); // Tạo ID tạm
        data.put("title", title);
        data.put("content", content);
        data.put("createdAt", LocalDateTime.now().toString());

        // Bắn thẳng qua WebSocket
        messagingTemplate.convertAndSend("/topic/notifications", data);
    }
}