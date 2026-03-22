package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.*; // Chỉnh lại theo package Entity của bạn
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OllamaService {

    private final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private final RestTemplate restTemplate = new RestTemplate();

    private final SetLauRepository setLauRepository;
    private final DanhMucChiTietRepository danhMucChiTietRepository;
    // Inject thêm Repo khuyến mãi của bạn
    private final com.example.datn_cozypot_spring_boot.repository.PhieuGiamGiaRepository phieuGiamGiaRepository;

    private final Map<String, Boolean> humanModeMap = new ConcurrentHashMap<>();

    // Đường dẫn ảnh base
    private final String IMAGE_BASE_URL = "https://unrheumatic-gametically-yajaira.ngrok-free.dev/uploads/";

    public String chatWithCozyPot(String sessionId, String userMessage) {
        if (humanModeMap.getOrDefault(sessionId, false)) return null;

        String lowerMsg = userMessage.toLowerCase();
        if (lowerMsg.contains("nhân viên") || lowerMsg.contains("hỗ trợ")) {
            humanModeMap.put(sessionId, true);
            return "Dạ, em đã báo nhân viên trực máy. Chờ em xíu nhé!";
        }

        String menuContext = buildMenuContextFromDB();
        String promoContext = buildPromotionContextFromDB();

        // --- BƯỚC 4: UPDATE PROMPT ĐỂ AI TRẢ VỀ CARD ---
        String prompt =
                "BẠN LÀ MÁY TƯ VẤN NHÀ HÀNG. HÃY TUÂN THỦ CÁC QUY TẮC SAU:\n" +
                        "1. Luôn xưng 'Dạ' và 'Quý khách'.\n" +
                        "2. DỮ LIỆU THỰC ĐƠN:\n" + menuContext + "\n" +
                        "3. QUY TẮC THẺ CARD (QUAN TRỌNG NHẤT):\n" +
                        "   - Khi nhắc đến bất kỳ món nào ở trên, bạn PHẢI viết thêm thẻ này ngay sau tên món: [CARD:Tên món|Giá|Link ảnh]\n" +
                        "   - Tuyệt đối không được tự ý viết link ảnh ra ngoài.\n" +
                        "   - Không được thay đổi cú pháp [CARD:...].\n" +
                        "4. VÍ DỤ:\n" +
                        "   Khách: Có lẩu gì ngon?\n" +
                        "   Bạn: Dạ nhà hàng có Lẩu Thái rất ngon ạ. [CARD:Lẩu Thái|399.000 VNĐ|https://link-anh.jpg]\n\n" +
                        "--- CÂU HỎI CỦA KHÁCH ---\n" +
                        userMessage + "\n\n" +
                        "Câu trả lời của bạn:";

        // Gọi Ollama như cũ...
        // (Giữ nguyên đoạn gọi RestTemplate bên dưới)
        return callOllama(prompt);
    }

    private String buildMenuContextFromDB() {
        StringBuilder sb = new StringBuilder();

        sb.append("DANH SÁCH MÓN ĂN CHÍNH THỨC (CẤM SAI LỆCH):\n");

        setLauRepository.findAll().stream().filter(s -> s.getTrangThai() == 1).forEach(s -> {
            sb.append(String.format("- Món: %s | Giá: %,.0f VNĐ | Ảnh: %ssetlau_%d.jpg\n",
                    s.getTenSetLau(), s.getGiaBan(), IMAGE_BASE_URL, s.getId()));
        });

        danhMucChiTietRepository.findAll().stream().filter(m -> m.getTrangThai() == 1).forEach(m -> {
            sb.append(String.format("- Món: %s | Giá: %,.0f VNĐ | Ảnh: %smonan_%d.jpg\n",
                    m.getTenMon(), m.getGiaBan(), IMAGE_BASE_URL, m.getId()));
        });

        return sb.toString();
    }

    private String buildPromotionContextFromDB() {
        StringBuilder sb = new StringBuilder();
        phieuGiamGiaRepository.findAll().stream()
                .filter(p -> p.getTrangThai() == 1) // Chỉ lấy phiếu đang hoạt động
                .forEach(p -> {
                    sb.append("- Mã: ").append(p.getCodeGiamGia())
                            .append(". Giảm: ").append(p.getLoaiGiamGia() == 1 ? p.getGiaTriGiam() + "%" : p.getGiaTriGiam() + "đ")
                            .append(". Đơn tối thiểu: ").append(p.getDonHangToiThieu()).append("đ\n");
                });
        return sb.toString();
    }

    // Hàm gọi API Ollama tách riêng cho sạch code
    private String callOllama(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", "qwen2.5:1.5b");
        body.put("prompt", prompt);
        body.put("stream", false);
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, new HttpHeaders() {{ setContentType(MediaType.APPLICATION_JSON); }});
            ResponseEntity<Map> response = restTemplate.postForEntity(OLLAMA_API_URL, entity, Map.class);
            return (String) response.getBody().get("response");
        } catch (Exception e) { return "Lỗi kết nối AI."; }
    }
}