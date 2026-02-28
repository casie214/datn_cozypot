package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.config.VNPayConfig;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    private final com.example.datn_cozypot_spring_boot.repository.thanhToanRepository.PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;

    private String vnp_TmnCode = "UKPOJ88W";
    private String vnp_HashSecret = "PN902AZ1XJ8BMLR5BPV1P8585EQ31RIU";
    private String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // URL cũ của bạn bạn
    private String vnp_ReturnUrl = "http://localhost:8080/api/vnpay/vnpay-return";

    // URL MỚI TẠO CHO LUỒNG ĐẶT CỌC
    private String vnp_ReturnUrl_Deposit = "http://localhost:8080/api/vnpay/vnpay-return-deposit";

    @GetMapping("/create-payment/{idHoaDon}")
    public ResponseEntity<?> createPayment(
            @PathVariable Integer idHoaDon,
            @RequestParam(required = false) Long amount, // Dùng để nhận số tiền từ thanh toán hỗn hợp
            HttpServletRequest request) throws UnsupportedEncodingException {

        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        long finalAmount;
        if (amount != null && amount > 0) {
            finalAmount = amount * 100; // Thanh toán hỗn hợp (phần còn lại)
        } else {
            finalAmount = hoaDon.getTongTienThanhToan().longValue() * 100; // Thanh toán Full
        }

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
                    hoaDon.setTrangThaiHoaDon(6);
                    hoaDon.setThoiGianThanhToan(Instant.now());
                    hoaDonThanhToanRepository.save(hoaDon);

                    if (hoaDon.getIdPhieuDatBan() != null) {
                        PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
                        phieu.setTrangThai(4);
                        phieuDatBanRepository.save(phieu);
                    }

                    if (hoaDon.getIdBanAn() != null) {
                        BanAn banAn = hoaDon.getIdBanAn();
                        banAn.setTrangThai(0);
                        banAnRepository.save(banAn);
                    }

                    try {
                        PhuongThucThanhToan ptVnPay = phuongThucThanhToanRepository.findByMaPhuongThuc("PT01");
                        LichSuThanhToan lichSu = new LichSuThanhToan();
                        lichSu.setPhuongThucThanhToan(ptVnPay);
                        lichSu.setHoaDon(hoaDon);
                        lichSu.setTenPhuongThuc(ptVnPay != null ? ptVnPay.getTenPhuongThuc() : "VNPay");
                        lichSu.setMaGiaoDich(vnp_TxnRef); // Mã giao dịch trả về từ VNPay

                        // Lấy số tiền thực tế khách đã quẹt (VNPay trả về chuỗi nhân 100, nên phải chia 100)
                        String amountStr = fields.get("vnp_Amount");
                        BigDecimal amountGiaoDich = new BigDecimal(amountStr).divide(new BigDecimal(100));
                        lichSu.setSoTienThanhToan(amountGiaoDich);

                        lichSu.setLoaiGiaoDich(1); // 1 = Giao dịch Thu Tiền
                        lichSu.setNgayThanhToan(Instant.now());
                        lichSu.setTrangThai(1); // 1 = Thành công
                        lichSu.setGhiChu("Thanh toán thành công qua cổng VNPay");
                        lichSuThanhToanRepository.save(lichSu);
                    } catch (Exception e) {
                        System.out.println("Lỗi lưu lịch sử thanh toán VNPay: " + e.getMessage());
                    }
                }
                response.sendRedirect("http://localhost:5173/payment-success");
            } else {
                response.sendRedirect("http://localhost:5173/payment-failed");
            }
        } else {
            response.sendRedirect("http://localhost:5173/payment-failed?error=checksum");
        }
    }


    // =========================================================================
    // DÀNH RIÊNG CHO KHÁCH HÀNG ĐẶT CỌC
    // =========================================================================

    @GetMapping("/create-payment-deposit/{idHoaDon}")
    public ResponseEntity<?> createPaymentDeposit(@PathVariable Integer idHoaDon, HttpServletRequest request) throws UnsupportedEncodingException {
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        // Chỉ lấy tiền cọc để thanh toán
        long finalAmount = hoaDon.getTienCoc().longValue() * 100;
        if (finalAmount < 1000000) {
            finalAmount = 1000000;
        }

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(finalAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "");

        String vnp_TxnRef = idHoaDon + "_" + System.currentTimeMillis();
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan tien coc cho don " + idHoaDon);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");

        // TRỎ VỀ ĐƯỜNG DẪN RETURN MỚI CỦA BẠN
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl_Deposit);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");

        java.time.ZoneId zoneId = java.time.ZoneId.of("Asia/Ho_Chi_Minh");
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", java.time.ZonedDateTime.now(zoneId).format(formatter));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString())).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        Map<String, String> result = new HashMap<>();
        result.put("url", vnp_PayUrl + "?" + queryUrl);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vnpay-return-deposit")
    public void vnpayReturnDeposit(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) hashData.append('&');
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

                    // 1. Cập nhật hóa đơn thành Đã cọc (2)
                    hoaDon.setTrangThaiHoaDon(2);
                    hoaDonThanhToanRepository.save(hoaDon);

                    // 2. Chuyển phiếu đặt bàn thành Đã xác nhận (2)
                    if (hoaDon.getIdPhieuDatBan() != null) {
                        PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
                        phieu.setTrangThai(2);
                        phieuDatBanRepository.save(phieu);
                    }

                    // (Lưu ý: KHÔNG giải phóng bàn ăn ở đây vì khách chưa tới)

                    // 3. Ghi log Lịch sử thanh toán VNPay
                    try {
                        PhuongThucThanhToan ptVnPay = phuongThucThanhToanRepository.findByMaPhuongThuc("PT01");
                        LichSuThanhToan lichSu = new LichSuThanhToan();
                        lichSu.setPhuongThucThanhToan(ptVnPay);
                        lichSu.setHoaDon(hoaDon);
                        lichSu.setTenPhuongThuc(ptVnPay != null ? ptVnPay.getTenPhuongThuc() : "VNPay");
                        lichSu.setMaGiaoDich(vnp_TxnRef);
                        String amountStr = fields.get("vnp_Amount");
                        BigDecimal amountGiaoDich = new BigDecimal(amountStr).divide(new BigDecimal(100));
                        lichSu.setSoTienThanhToan(amountGiaoDich);
                        lichSu.setLoaiGiaoDich(2); // 2 = Giao dịch đặt cọc
                        lichSu.setNgayThanhToan(Instant.now());
                        lichSu.setTrangThai(1);
                        lichSu.setGhiChu("Thanh toán tiền cọc thành công qua cổng VNPay");
                        lichSuThanhToanRepository.save(lichSu);
                    } catch (Exception e) {
                        System.out.println("Lỗi lưu lịch sử TT VNPay: " + e.getMessage());
                    }

                    // 4. Ghi log Lịch sử Hóa đơn
                    try {
                        LichSuHoaDon lsHd = new LichSuHoaDon();
                        lsHd.setIdHoaDon(hoaDon);
                        lsHd.setHanhDong("Thanh toán cọc thành công");
                        lsHd.setTrangThaiTruocDo(trangThaiCu);
                        lsHd.setTrangThaiMoi(2);
                        lsHd.setThoiGianThucHien(Instant.now());
                        lichSuHoaDonRepository.save(lsHd);
                    } catch (Exception e) {
                        System.out.println("Lỗi lưu Log Hóa đơn: " + e.getMessage());
                    }
                }
                // Trả về trang báo thành công kèm type=deposit
                response.sendRedirect("http://localhost:5173/payment-success?type=deposit");
            } else {
                response.sendRedirect("http://localhost:5173/payment-failed");
            }
        } else {
            response.sendRedirect("http://localhost:5173/payment-failed?error=checksum");
        }
    }
}