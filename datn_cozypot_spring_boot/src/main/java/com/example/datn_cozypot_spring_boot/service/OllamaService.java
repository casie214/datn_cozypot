package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.*; // Chỉnh lại theo package Entity của bạn
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository;
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
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

    private final DotKhuyenMaiRepository dotKhuyenMaiRepository;

    private final Map<String, Boolean> humanModeMap = new ConcurrentHashMap<>();

    // Đường dẫn ảnh base
    private final String IMAGE_BASE_URL = "http://localhost:8080/uploads/";
    private final CloudinaryService cloudinaryService;

    public String chatWithCozyPot(String sessionId, String userMessage) {
        if (humanModeMap.getOrDefault(sessionId, false)) return null;

        String lowerMsg = userMessage.toLowerCase();
        if (lowerMsg.contains("nhân viên") || lowerMsg.contains("hỗ trợ")) {
            humanModeMap.put(sessionId, true);
            return "Dạ, em đã báo nhân viên trực máy. Chờ em xíu nhé!";
        }

        // Lấy Context từ DB (Chỗ này đã dùng Cloudinary rồi)
        String menuContext = buildMenuContextFromDB();
        String promoContext = buildPromotionContextFromDB();
        String campaignContext = buildCampaignContextFromDB();

        String fullPromoContext = "--- PHIẾU GIẢM GIÁ CẦN NHẬP MÃ ---\n" + promoContext + "\n" + campaignContext;

        // --- UPDATE PROMPT: THAY LOCALHOST TRONG VÍ DỤ ---
        String prompt =
                "BẠN LÀ TRỢ LÝ ẢO CỦA NHÀ HÀNG COZYPOT. BẠN LÀ MỘT AI (NGƯỜI MÁY), KHÔNG PHẢI LÀ MÓN ĂN.\n" +
                        "TUÂN THỦ NGHIÊM NGẶT CÁC QUY TẮC SAU:\n" +
                        "1. GIAO TIẾP: Luôn xưng 'Dạ' và gọi khách là 'Quý khách'. Thái độ lịch sự, tự nhiên.\n" +
                        "2. DỮ LIỆU THỰC ĐƠN:\n" + menuContext + "\n" +
                        "3. DỮ LIỆU KHUYẾN MÃI HIỆN CÓ:\n" + fullPromoContext + "\n" +
                        "4. QUY TẮC THẺ [CARD] (CẤM LÀM SAI):\n" +
                        "   - KHI GIỚI THIỆU MÓN ĂN, BẮT BUỘC DÙNG ĐÚNG CÚ PHÁP NÀY VÀ ĐẶT CUỐI CÂU: [CARD:Tên món|Giá|Link ảnh]\n" +
                        "   - TUYỆT ĐỐI CẤM dùng định dạng Markdown cho hình ảnh như ![](link) hoặc HTML.\n" +
                        "   - CHỈ SỬ DỤNG LINK ẢNH ĐƯỢC CUNG CẤP TRONG DỮ LIỆU THỰC ĐƠN Ở MỤC 2.\n" + // Thêm luật này cho chắc
                        "5. KỊCH BẢN XỬ LÝ (Hãy học theo cách trả lời này):\n" +
                        "   - Khách: Gửi tôi món thịt bò\n" +
                        "     Bạn: Dạ, nhà hàng có món bò rất ngon cho Quý khách tham khảo ạ: [CARD:Ba chỉ bò Mỹ 100 gram|89,000 VNĐ|https://res.cloudinary.com/your_cloud/image/upload/monan_1.jpg]\n" + // <--- Thay localhost bằng link này
                        "   - Khách: Nhà hàng có ưu đãi gì không?\n" +
                        "     Bạn: Dạ hiện nhà hàng đang có đợt giảm giá 'Hello world' tự động giảm 50% thẳng vào hóa đơn. Ngoài ra còn có mã giảm giá TET2026 giảm 30% nữa ạ!\n\n" +
                        "--- CÂU HỎI CỦA KHÁCH ---\n" +
                        userMessage + "\n\n" +
                        "Câu trả lời của bạn:";

        return callOllama(prompt);
    }

    private String buildCampaignContextFromDB() {
        StringBuilder sb = new StringBuilder();
        sb.append("CÁC ĐỢT GIẢM GIÁ TỰ ĐỘNG ÁP DỤNG (KHÁCH KHÔNG CẦN NHẬP MÃ):\n");

        // DÙNG LocalDate
        java.time.LocalDate today = java.time.LocalDate.now();

        dotKhuyenMaiRepository.findAll().stream()
                .filter(d -> d.getTrangThai() == 1)
                .filter(d -> d.getNgayBatDau() != null && d.getNgayKetThuc() != null &&
                        !d.getNgayBatDau().isAfter(today) && !d.getNgayKetThuc().isBefore(today))
                .forEach(d -> {
                    sb.append("- Chương trình: ").append(d.getTenDotKhuyenMai())
                            .append(". Mức giảm: ").append(d.getPhanTramGiam()).append("%")
                            .append(". (Chương trình tự động áp dụng, không cần nhập mã).\n");
                });
        return sb.toString();
    }

    private String buildMenuContextFromDB() {
        StringBuilder sb = new StringBuilder();
        sb.append("DANH SÁCH MÓN ĂN CHÍNH THỨC:\n");

        setLauRepository.findAll().stream().filter(s -> s.getTrangThai() == 1).forEach(s -> {
            // 🚨 THAY ĐỔI Ở ĐÂY: Lấy link từ CloudinaryService
            String imgLink = cloudinaryService.getUrl("setlau_" + s.getId());
            sb.append(String.format("- Món: %s | Giá: %,.0f VNĐ | Ảnh: %s\n",
                    s.getTenSetLau(), s.getGiaBan(), imgLink));
        });

        danhMucChiTietRepository.findAll().stream().filter(m -> m.getTrangThai() == 1).forEach(m -> {
            // 🚨 THAY ĐỔI Ở ĐÂY: Lấy link từ CloudinaryService
            String imgLink = cloudinaryService.getUrl("monan_" + m.getId());
            sb.append(String.format("- Món: %s | Giá: %,.0f VNĐ | Ảnh: %s\n",
                    m.getTenMon(), m.getGiaBan(), imgLink));
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

            String aiReply = (String) response.getBody().get("response");

            // 🚨 THÊM DÒNG NÀY ĐỂ CHECK LOG:
            System.out.println("========== [LOG OLLAMA] ==========");
            System.out.println("PROMPT GỬI ĐI:\n" + prompt);
            System.out.println("OLLAMA TRẢ VỀ:\n" + aiReply);
            System.out.println("==================================");

            return aiReply;
        } catch (Exception e) {
            return "Lỗi kết nối AI.";
        }
    }
}