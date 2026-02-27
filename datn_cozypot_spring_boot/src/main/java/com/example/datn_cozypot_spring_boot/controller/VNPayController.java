package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.config.VNPayConfig;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vnpay")
public class VNPayController {

    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;
    private final PhieuDatBanRepository phieuDatBanRepository;
    private final BanAnRepository banAnRepository;

    // THÊM 2 REPOSITORY DÀNH CHO LỊCH SỬ THANH TOÁN
    private final com.example.datn_cozypot_spring_boot.repository.thanhToanRepository.PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final NhanVienRepository nhanVienRepository;

    // THÔNG TIN CẤU HÌNH VNPAY
    private String vnp_TmnCode = "UKPOJ88W";
    private String vnp_HashSecret = "PN902AZ1XJ8BMLR5BPV1P8585EQ31RIU";
    private String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private String vnp_ReturnUrl = "http://localhost:8080/api/vnpay/vnpay-return";

    @GetMapping("/create-payment/{idHoaDon}")
    public ResponseEntity<?> createPayment(
            @PathVariable Integer idHoaDon,
            @RequestParam(required = false) Long amount, // Dùng để nhận số tiền từ thanh toán hỗn hợp
            HttpServletRequest request) throws UnsupportedEncodingException {

        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // XỬ LÝ SỐ TIỀN THANH TOÁN
        long finalAmount;
        if (amount != null && amount > 0) {
            finalAmount = amount * 100; // Thanh toán hỗn hợp (phần còn lại)
        } else {
            finalAmount = hoaDon.getTongTienThanhToan().longValue() * 100; // Thanh toán Full
        }

        // VNPay yêu cầu số tiền tối thiểu là 10.000 VNĐ
        if (finalAmount < 1000000) {
            finalAmount = 1000000;
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(finalAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", ""); // Để trống để khách tự chọn phương thức thẻ/QR

        String vnp_TxnRef = idHoaDon + "_" + System.currentTimeMillis();
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan hoa don " + idHoaDon);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        // FIX LỖI THỜI GIAN VNPAY
        java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(zoneId);
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String createDate = now.format(formatter);
        vnp_Params.put("vnp_CreateDate", createDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;

        Map<String, String> result = new HashMap<>();
        result.put("url", paymentUrl);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vnpay-return")
    @Transactional // Rất quan trọng để đảm bảo tất cả cập nhật DB và ghi Log đều thành công cùng lúc
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // Sắp xếp dữ liệu để kiểm tra chữ ký
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String signValue = VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String vnp_TxnRef = fields.get("vnp_TxnRef");
        Integer idHoaDon = Integer.parseInt(vnp_TxnRef.split("_")[0]);

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(vnp_ResponseCode)) {
                HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon).orElse(null);

                if (hoaDon != null) {
                    Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();

                    // 1. CẬP NHẬT TRẠNG THÁI HÓA ĐƠN -> Đã thanh toán (6)
                    hoaDon.setTrangThaiHoaDon(6);
                    hoaDon.setThoiGianThanhToan(Instant.now());

                    // 2. CẬP NHẬT PHIẾU ĐẶT BÀN -> Hoàn tất (4)
                    if (hoaDon.getIdPhieuDatBan() != null) {
                        PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
                        phieu.setTrangThai(4);
                        phieuDatBanRepository.save(phieu);
                    }

                    // 3. GIẢI PHÓNG BÀN ĂN -> Trống (0)
                    if (hoaDon.getIdBanAn() != null) {
                        BanAn banAn = hoaDon.getIdBanAn();
                        banAn.setTrangThai(0);
                        banAnRepository.save(banAn);
                    }

                    // 4. TRÍCH XUẤT SỐ TIỀN THANH TOÁN THÀNH CÔNG
                    String amountStr = fields.get("vnp_Amount");
                    BigDecimal amountGiaoDich = new BigDecimal(amountStr).divide(new BigDecimal(100));

                    // 📝 5. GHI LOG VÀO TIMELINE LỊCH SỬ HÓA ĐƠN
                    // ID nhân viên để null vì đây là hệ thống tự xác nhận từ VNPay
                    ghiLichSu(
                            hoaDon,
                            null,
                            "Đã thanh toán VNPay thành công: " + amountGiaoDich.setScale(0) + "đ",
                            "Mã giao dịch VNPay: " + vnp_TxnRef,
                            trangThaiCu,
                            6
                    );

                    // 💰 6. LƯU VÀO BẢNG LỊCH SỬ THANH TOÁN (ĐỐI SOÁT)
                    try {
                        PhuongThucThanhToan ptVnPay = phuongThucThanhToanRepository.findByMaPhuongThuc("PT01");
                        LichSuThanhToan lichSu = new LichSuThanhToan();
                        lichSu.setPhuongThucThanhToan(ptVnPay);
                        lichSu.setHoaDon(hoaDon);
                        lichSu.setTenPhuongThuc(ptVnPay != null ? ptVnPay.getTenPhuongThuc() : "VNPay");
                        lichSu.setMaGiaoDich(vnp_TxnRef);
                        lichSu.setSoTienThanhToan(amountGiaoDich);
                        lichSu.setLoaiGiaoDich(1); // Giao dịch thu tiền
                        lichSu.setNgayThanhToan(Instant.now());
                        lichSu.setTrangThai(1); // Thành công
                        lichSu.setGhiChu("Thanh toán điện tử qua VNPay");
                        lichSuThanhToanRepository.save(lichSu);
                    } catch (Exception e) {
                        System.out.println("❌ Lỗi lưu đối soát VNPay: " + e.getMessage());
                    }

                    hoaDonThanhToanRepository.save(hoaDon);
                }
                // Chuyển hướng về trang thành công của Vue
                response.sendRedirect("http://localhost:5173/payment-success");
            } else {
                response.sendRedirect("http://localhost:5173/payment-failed");
            }
        } else {
            response.sendRedirect("http://localhost:5173/payment-failed?error=checksum");
        }
    }

    // Hàm bổ trợ ghi Log Timeline (Bạn có thể để hàm này ở Service để dùng chung)
    private void ghiLichSu(HoaDonThanhToan hd, Integer idNV, String hanhDong, String lyDo, Integer cu, Integer moi) {
        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        if (idNV != null) {
            // Nếu có nhân viên thực hiện (như thu tiền mặt) thì lấy thông tin
            // Còn VNPay hệ thống tự làm nên idNV truyền vào là null
            nhanVienRepository.findById(idNV).ifPresent(log::setIdNhanVien);
        }
        log.setHanhDong(hanhDong);
        log.setLyDoThucHien(lyDo);
        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(cu);
        log.setTrangThaiMoi(moi);
        lichSuHoaDonRepository.save(log);
    }
}
