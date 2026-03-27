package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.ChatMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ChatMemoryService {

    // Lưu trữ tin nhắn: Key là sessionId, Value là danh sách tin nhắn
    private final Map<String, List<ChatMessage>> chatSessions = new ConcurrentHashMap<>();

    // Lưu trữ trạng thái xem session nào đã chuyển sang nhân viên
    private final Map<String, Boolean> humanModeMap = new ConcurrentHashMap<>();

    // 1. Lưu tin nhắn mới
    public void addMessage(String sessionId, String sender, String content) {
        chatSessions.putIfAbsent(sessionId, new CopyOnWriteArrayList<>());
        chatSessions.get(sessionId).add(new ChatMessage(sender, content, LocalDateTime.now()));
    }

    // 2. Lấy lịch sử chat
    public List<ChatMessage> getHistory(String sessionId) {
        return chatSessions.getOrDefault(sessionId, new ArrayList<>());
    }

    // 3. Lấy danh sách các session đang active (Cho Admin xem danh sách chờ)
    public List<String> getActiveSessions() {
        return new ArrayList<>(chatSessions.keySet());
    }

    // 4. Đánh dấu session cần nhân viên hỗ trợ
    public void setHumanMode(String sessionId, boolean isHuman) {
        humanModeMap.put(sessionId, isHuman);
    }

    // 5. Kiểm tra xem session có đang ở chế độ nhân viên không
    public boolean isHumanMode(String sessionId) {
        return humanModeMap.getOrDefault(sessionId, false);
    }

    // 6. Xóa session khi Admin bấm "Hoàn tất"
    public void resolveSession(String sessionId) {
        chatSessions.remove(sessionId);
        humanModeMap.remove(sessionId);
    }
}