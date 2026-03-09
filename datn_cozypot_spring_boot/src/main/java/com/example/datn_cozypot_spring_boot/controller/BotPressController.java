package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.service.BotPressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/botpress")
@RequiredArgsConstructor
@CrossOrigin("*") // Cho phép Vue gọi API
public class BotPressController {

    private final BotPressService botPressService;

    // Lưu tạm danh sách các cuộc hội thoại đang đợi (không cần DB)
    private static final Set<String> waitingList = new LinkedHashSet<>();

    // 1. Webhook nhận tín hiệu từ Botpress (Thẻ Execute Code gọi tới đây)
    @PostMapping("/handoff")
    public void handleHandoff(@RequestBody Map<String, String> payload) {
        String conversationId = payload.get("conversationId");
        if (conversationId != null) {
            waitingList.add(conversationId);
            System.out.println("🚨 Khách hàng mới cần hỗ trợ: " + conversationId);
        }
    }

    // 2. API lấy danh sách các khách đang chờ cho Vue Admin
    @GetMapping("/waiting-list")
    public Set<String> getWaitingList() {
        return waitingList;
    }

    // 3. API lấy lịch sử chat cho Vue Admin
    @GetMapping("/history/{conversationId}")
    public String getHistory(@PathVariable String conversationId) {
        return botPressService.getMessages(conversationId);
    }

    // 4. API gửi tin nhắn trả lời từ Vue Admin
    @PostMapping("/reply")
    public void reply(@RequestBody Map<String, String> payload) {
        String conversationId = payload.get("conversationId");
        String message = payload.get("message");
        botPressService.sendReply(conversationId, message);
    }

    // 5. Kết thúc hỗ trợ (Xóa khỏi danh sách chờ)
    @DeleteMapping("/resolve/{conversationId}")
    public void resolve(@PathVariable String conversationId) {
        waitingList.remove(conversationId);
    }
}