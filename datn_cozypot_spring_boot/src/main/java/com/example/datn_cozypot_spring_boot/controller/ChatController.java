package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.ChatMessage;
import com.example.datn_cozypot_spring_boot.service.ChatMemoryService;
import com.example.datn_cozypot_spring_boot.service.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private OllamaService ollamaService;

    @Autowired
    private ChatMemoryService chatMemoryService;

    // 1. API cho Khách hàng Chat
    @PostMapping
    public ResponseEntity<?> chatWithBot(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String sessionId = request.get("sessionId");

        if (sessionId == null || sessionId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("reply", "Thiếu Session ID"));
        }

        // Lưu tin nhắn của Khách vào RAM
        chatMemoryService.addMessage(sessionId, "user", userMessage);

        // Bắt từ khóa chuyển nhân viên
        String lowerMsg = userMessage.toLowerCase();
        if (lowerMsg.contains("nhân viên") || lowerMsg.contains("hỗ trợ") || lowerMsg.contains("người thật")) {
            chatMemoryService.setHumanMode(sessionId, true);
            String rep = "Dạ, em đã chuyển yêu cầu của quý khách cho Nhân viên CSKH. Quý khách vui lòng đợi trong giây lát ạ!";
            chatMemoryService.addMessage(sessionId, "bot", rep);
            return ResponseEntity.ok(Map.of("status", "bot_mode", "reply", rep));
        }

        // Kiểm tra xem có đang ở chế độ nhân viên không
        if (chatMemoryService.isHumanMode(sessionId)) {
            // Không gọi Ollama nữa, trả về rỗng để Frontend tự chờ Admin rep
            return ResponseEntity.ok(Map.of("status", "human_mode"));
        }

        // Gọi Ollama nếu vẫn ở chế độ BOT
        String botReply = ollamaService.chatWithCozyPot(sessionId, userMessage);

        // Lưu tin nhắn của Bot vào RAM
        chatMemoryService.addMessage(sessionId, "bot", botReply);

        return ResponseEntity.ok(Map.of("status", "bot_mode", "reply", botReply));
    }

    @PostMapping("/reset")
    public ResponseEntity<?> resetSession(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        if (sessionId != null) {
            chatMemoryService.resolveSession(sessionId); // Xóa dữ liệu trong RAM
        }
        return ResponseEntity.ok(Map.of("message", "Session đã được làm mới"));
    }

    // 2. API lấy lịch sử tin nhắn
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable String sessionId) {
        return ResponseEntity.ok(chatMemoryService.getHistory(sessionId));
    }

    // 3. API cho Admin lấy danh sách khách đang chat
    @GetMapping("/active-sessions")
    public ResponseEntity<List<String>> getActiveSessions() {
        return ResponseEntity.ok(chatMemoryService.getActiveSessions());
    }

    // 4. API cho Admin gửi tin nhắn
    @PostMapping("/admin-reply")
    public ResponseEntity<?> adminReply(@RequestBody Map<String, String> request) {
        String sessionId = request.get("sessionId");
        String message = request.get("message");

        // Lưu tin nhắn của Admin vào RAM
        chatMemoryService.addMessage(sessionId, "admin", message);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // 5. API cho Admin hoàn tất chat
    @DeleteMapping("/resolve/{sessionId}")
    public ResponseEntity<?> resolveChat(@PathVariable String sessionId) {
        chatMemoryService.resolveSession(sessionId);
        return ResponseEntity.ok().build();
    }
}