package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.config.VNPayConfig;
import com.example.datn_cozypot_spring_boot.dto.request.EmailDatBanDTO;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.service.EmailDatBanService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vnpay")
public class VNPayController {

    private static final Logger log = LoggerFactory.getLogger(VNPayController.class);

    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;
    private final PhieuDatBanRepository phieuDatBanRepository;
    private final BanAnRepository banAnRepository;
    private final com.example.datn_cozypot_spring_boot.repository.thanhToanRepository.PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final NhanVienRepository nhanVienRepository;
    private final EmailDatBanService emailDatBanService;
    private final phieuDatBanBanAnRepository phieuDatBanBanAnRepository;

    // 🚨 KEY CHUẨN CỦA VNPAY
    private final String vnp_TmnCode = "UKPOJ88W";
    private final String vnp_HashSecret = "PN902AZ1XJ8BMLR5BPV1P8585EQ31RIU";
    private final String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private final String vnp_ReturnUrl = "http://localhost:8080/api/vnpay/vnpay-return";
    private final String vnp_ReturnUrl_Deposit = "http://localhost:8080/api/vnpay/vnpay-return-deposit";

    @GetMapping("/create-payment/{idHoaDon}")
    public ResponseEntity<?> createPayment(
            @PathVariable String idHoaDon,
            @RequestParam(required = false) Long amount,
            HttpServletRequest request) throws UnsupportedEncodingException {

        log.info("\n========== [1] BẮT ĐẦU TẠO GIAO DỊCH VNPAY (THANH TOÁN) ==========");

        long finalAmount;
        if (amount != null && amount > 0) {
            finalAmount = amount * 100;
        } else {
            Integer idGoc = Integer.parseInt(idHoaDon.split("_")[0]);
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idGoc)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
            finalAmount = hoaDon.getTongTienThanhToan().longValue() * 100;
        }

        if (finalAmount < 1000000) finalAmount = 1000000;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(finalAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "");
        vnp_Params.put("vnp_TxnRef", idHoaDon + "_" + System.currentTimeMillis());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan hoa don " + idHoaDon);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // 🚨 CHUẨN VNPAY SDK JAVA CHÍNH HÃNG 🚨
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
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
        String vnp_SecureHash = this.hmacSHA512Native(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;

        log.info("=> Chuỗi HashData ĐÚNG CHUẨN: " + hashData.toString());
        log.info("=> Chuỗi Query ĐÚNG CHUẨN: " + query.toString());
        log.info("=> SecureHash (Lowercase): " + vnp_SecureHash);
        log.info("========== HOÀN TẤT ==========\n");

        Map<String, String> result = new HashMap<>();
        result.put("url", paymentUrl);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vnpay-return")
    @Transactional
    public void vnpayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("\n========== [2] NHẬN KẾT QUẢ TỪ VNPAY (THANH TOÁN) ==========");
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

        String signValue = this.hmacSHA512Native(vnp_HashSecret, hashData.toString());
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String vnp_TxnRef = fields.get("vnp_TxnRef");

        log.info("=> Mã Hash VNPay gửi về: " + vnp_SecureHash);
        log.info("=> Mã Hash Tự tính lại: " + signValue);

        if (signValue.equals(vnp_SecureHash)) {
            log.info("=> CHỮ KÝ HỢP LỆ! Xử lý Database...");
            if ("00".equals(vnp_ResponseCode)) {
                PhuongThucThanhToan ptVnPay = phuongThucThanhToanRepository.findByMaPhuongThuc("PT01");
                String amountStr = fields.get("vnp_Amount");
                BigDecimal tongTienVnPayTraVe = new BigDecimal(amountStr).divide(new BigDecimal(100));

                String[] idArray = vnp_TxnRef.split("_");
                try {
                    Integer idHoaDon = Integer.parseInt(idArray[0]);
                    HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon).orElse(null);

                    if (hoaDon != null && hoaDon.getTrangThaiHoaDon() != 6) {
                        Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();
                        hoaDon.setTrangThaiHoaDon(6);
                        hoaDon.setThoiGianThanhToan(Instant.now().plus(7, ChronoUnit.HOURS));
                        hoaDon.setTongTienThanhToan(tongTienVnPayTraVe);
                        BigDecimal tienDuaTrk = hoaDon.getTienKhachDua() != null ? hoaDon.getTienKhachDua() : BigDecimal.ZERO;
                        hoaDon.setTienKhachDua(tienDuaTrk.add(tongTienVnPayTraVe));

                        if (hoaDon.getIdPhieuDatBan() != null) {
                            PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
                            phieu.setTrangThai(4);
                            for (PhieuDatBanBanAn link : phieu.getDsBanAn()) {
                                BanAn ban = link.getBanAn();
                                List<PhieuDatBanBanAn> cacPhieuCungBan = phieuDatBanBanAnRepository.findActiveLinksByBanId(ban.getId());
                                long countOtherActive = cacPhieuCungBan.stream()
                                        .filter(l -> !l.getPhieuDatBan().getId().equals(phieu.getId())).count();
                                if (countOtherActive == 0) {
                                    ban.setTrangThai(0);
                                    banAnRepository.save(ban);
                                }
                            }
                            phieuDatBanRepository.save(phieu);
                        }

                        ghiLichSu(hoaDon, null, "Đã thanh toán VNPay: " + tongTienVnPayTraVe.setScale(0) + "đ", "Mã GD: " + vnp_TxnRef, trangThaiCu, 6);

                        LichSuThanhToan lichSu = new LichSuThanhToan();
                        lichSu.setPhuongThucThanhToan(ptVnPay);
                        lichSu.setHoaDon(hoaDon);
                        lichSu.setTenPhuongThuc(ptVnPay != null ? ptVnPay.getTenPhuongThuc() : "VNPay");
                        lichSu.setMaGiaoDich(vnp_TxnRef);
                        lichSu.setSoTienThanhToan(tongTienVnPayTraVe);
                        lichSu.setLoaiGiaoDich(1);
                        lichSu.setNgayThanhToan(Instant.now());
                        lichSu.setTrangThai(1);
                        lichSu.setGhiChu("Thanh toán điện tử qua VNPay");
                        lichSuThanhToanRepository.save(lichSu);

                        hoaDonThanhToanRepository.save(hoaDon);
                    }
                } catch (Exception e) { log.error("Lỗi cập nhật: " + e.getMessage()); }
                response.sendRedirect("http://localhost:5173/payment-success");
            } else {
                log.info("=> Giao dịch thất bại tại VNPay (ResponseCode != 00)");
                response.sendRedirect("http://localhost:5173/payment-failed");
            }
        } else {
            log.error("=> SAI CHỮ KÝ! VNPay trả về: " + vnp_SecureHash + " | Tự tính: " + signValue);
            response.sendRedirect("http://localhost:5173/payment-failed?error=checksum");
        }
    }

    @GetMapping("/create-payment-deposit/{idHoaDon}")
    public ResponseEntity<?> createPaymentDeposit(@PathVariable Integer idHoaDon, HttpServletRequest request) throws UnsupportedEncodingException {

        log.info("\n========== [1] BẮT ĐẦU TẠO GIAO DỊCH VNPAY (ĐẶT CỌC) ==========");

        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        long finalAmount = hoaDon.getTienCoc().longValue() * 100;
        if (finalAmount < 1000000) finalAmount = 1000000;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(finalAmount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "");
        vnp_Params.put("vnp_TxnRef", idHoaDon + "_" + System.currentTimeMillis());
        vnp_Params.put("vnp_OrderInfo", "Thanh toan tien coc cho don " + idHoaDon);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl_Deposit);
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
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
        String vnp_SecureHash = this.hmacSHA512Native(vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnp_PayUrl + "?" + queryUrl;

        log.info("=> Chuỗi HashData ĐÚNG CHUẨN: " + hashData.toString());
        log.info("=> Chuỗi Query ĐÚNG CHUẨN: " + query.toString());
        log.info("=> SecureHash (Lowercase): " + vnp_SecureHash);
        log.info("========== HOÀN TẤT ==========\n");

        Map<String, String> result = new HashMap<>();
        result.put("url", paymentUrl);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/vnpay-return-deposit")
    @Transactional
    public void vnpayReturnDeposit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("\n========== [2] NHẬN KẾT QUẢ TỪ VNPAY (ĐẶT CỌC) ==========");
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) fields.put(fieldName, fieldValue);
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
            if (fieldValue != null && fieldValue.length() > 0) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) hashData.append('&');
            }
        }

        String signValue = this.hmacSHA512Native(vnp_HashSecret, hashData.toString());
        String vnp_ResponseCode = fields.get("vnp_ResponseCode");
        String vnp_TxnRef = fields.get("vnp_TxnRef");
        Integer idHoaDon = Integer.parseInt(vnp_TxnRef.split("_")[0]);

        log.info("=> Mã Hash VNPay gửi về: " + vnp_SecureHash);
        log.info("=> Mã Hash Tự tính lại: " + signValue);

        if (signValue.equals(vnp_SecureHash)) {
            log.info("=> CHỮ KÝ HỢP LỆ! Xử lý Database...");
            if ("00".equals(vnp_ResponseCode)) {
                HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon).orElse(null);
                if (hoaDon != null) {
                    Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();
                    hoaDon.setTrangThaiHoaDon(2);
                    hoaDonThanhToanRepository.save(hoaDon);

                    if (hoaDon.getIdPhieuDatBan() != null) {
                        PhieuDatBan phieu = hoaDon.getIdPhieuDatBan();
                        phieu.setTrangThai(0);
                        phieuDatBanRepository.save(phieu);
                    }

                    LichSuThanhToan ls = new LichSuThanhToan();
                    ls.setHoaDon(hoaDon);
                    ls.setMaGiaoDich(vnp_TxnRef);
                    ls.setSoTienThanhToan(new BigDecimal(fields.get("vnp_Amount")).divide(new BigDecimal(100)));
                    ls.setLoaiGiaoDich(2);
                    ls.setNgayThanhToan(Instant.now());
                    ls.setTrangThai(1);
                    ls.setTenPhuongThuc("VNPay");
                    lichSuThanhToanRepository.save(ls);

                    try {
                        KhachHang kh = hoaDon.getIdKhachHang();
                        PhieuDatBan p = hoaDon.getIdPhieuDatBan();
                        if (kh != null && kh.getEmail() != null && p != null) {
                            EmailDatBanDTO dto = EmailDatBanDTO.builder()
                                    .tenKhachHang(kh.getTenKhachHang()).soDienThoai(kh.getSoDienThoai())
                                    .email(kh.getEmail()).soLuongKhach(p.getSoLuongKhach())
                                    .thoiGianDat(p.getThoiGianDat().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                                    .maPhieuDatBan("PDB" + String.format("%04d", p.getId())).build();
                            emailDatBanService.sendEmailCamOnDatBan(dto);
                        }
                    } catch (Exception e) { log.error("Mail error: " + e.getMessage()); }
                }
                response.sendRedirect("http://localhost:5173/payment-success?type=deposit");
            } else {
                log.info("=> Giao dịch thất bại tại VNPay (ResponseCode != 00)");
                response.sendRedirect("http://localhost:5173/payment-failed?type=deposit");
            }
        } else {
            log.error("=> SAI CHỮ KÝ! VNPay trả về: " + vnp_SecureHash + " | Tự tính: " + signValue);
            response.sendRedirect("http://localhost:5173/payment-failed?type=deposit&error=checksum");
        }
    }

    // 🚨 HÀM HMAC-SHA512 CHUẨN VNPAY SDK
    private String hmacSHA512Native(String key, String data) {
        try {
            if (key == null || data == null) return "";
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            // Standard VNPay Java SDK code doesn't specify charset, meaning it uses default.
            // But we will force US_ASCII to match the URLEncoder
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.US_ASCII);
            SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.US_ASCII));
            StringBuilder sb = new StringBuilder(128);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff)); // Lowercase hex
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Lỗi mã hóa: " + ex.getMessage());
            return "";
        }
    }

    private void ghiLichSu(HoaDonThanhToan hd, Integer idNV, String hanhDong, String lyDo, Integer cu, Integer moi) {
        LichSuHoaDon logItem = new LichSuHoaDon();
        logItem.setIdHoaDon(hd);
        if (idNV != null) nhanVienRepository.findById(idNV).ifPresent(logItem::setIdNhanVien);
        logItem.setHanhDong(hanhDong); logItem.setLyDoThucHien(lyDo);
        logItem.setThoiGianThucHien(Instant.now()); logItem.setTrangThaiTruocDo(cu); logItem.setTrangThaiMoi(moi);
        lichSuHoaDonRepository.save(logItem);
    }
}