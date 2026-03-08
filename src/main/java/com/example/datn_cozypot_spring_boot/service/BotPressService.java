package com.example.datn_cozypot_spring_boot.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class BotPressService {

    // --- 1. BIẾN DÙNG CHO API LẤY LỊCH SỬ TIN NHẮN ---
    private final String BOTPRESS_TOKEN = "bp_pat_gg64ugKrnTy2CzU7cYifxXSsbpeEXrySaGp0";
    private final String WORKSPACE_ID = "wkspace_01KK5ZZRAQ79RVC5WDA5K3NW13";
    private final String BOT_ID = "2486f9f8-2cbd-4f6e-9c11-933effb886f0"; // Thêm Bot ID để GET ko bị lỗi 400
    private final String API_BASE_URL = "https://api.botpress.cloud/v1";

    // --- 2. BIẾN DÙNG CHO WEBHOOK GỬI TIN NHẮN ---
    private final String WEBHOOK_URL = "https://webhook.botpress.cloud/14c621a8-6379-412e-88cc-12eb48b1f155";

    private final RestTemplate restTemplate = new RestTemplate();

    // Header này giờ chỉ phục vụ riêng cho việc Lấy danh sách tin nhắn
    private HttpHeaders getCloudApiHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + BOTPRESS_TOKEN.trim());
        headers.set("x-workspace-id", WORKSPACE_ID.trim());
        headers.set("x-bot-id", BOT_ID.trim()); // Bắt buộc có cái này để API chấp nhận mã wkspace_
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // 🚀 HÀM MỚI: GỬI TIN NHẮN BẰNG WEBHOOK (KHÔNG LO LỖI 403 NỮA)
    public void sendReply(String conversationId, String text) {
        // Cấu trúc map 1-1 với đoạn code Javascript trong Execute Code của Botpress
        Map<String, Object> body = new HashMap<>();
        body.put("conversationId", conversationId);
        body.put("message", text);

        // Webhook mở cửa tự do, chỉ cần báo định dạng JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(WEBHOOK_URL, entity, String.class);
            System.out.println("✅ Nhân viên đã bắn tin nhắn cho khách qua Webhook thành công!");
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi tin qua Webhook: " + e.getMessage());
        }
    }

    // 🔍 HÀM CŨ: LẤY LỊCH SỬ TIN NHẮN
    public String getMessages(String conversationId) {
        String url = API_BASE_URL + "/chat/messages?conversationId=" + conversationId;
        try {
            HttpEntity<String> entity = new HttpEntity<>(getCloudApiHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}