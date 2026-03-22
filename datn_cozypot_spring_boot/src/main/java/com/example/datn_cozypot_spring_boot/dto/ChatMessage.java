package com.example.datn_cozypot_spring_boot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String sender; // 'user', 'bot', hoặc 'admin'
    private String content;
    private LocalDateTime createdAt;
}
