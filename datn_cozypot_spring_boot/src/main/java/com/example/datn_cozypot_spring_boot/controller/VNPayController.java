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
import java.time.temporal.ChronoUnit;
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
    private final NhanVienRepository nhanVienRepository;

    private String vnp_TmnCode = "UKPOJ88W";
    private String vnp_HashSecret = "PN902AZ1XJ8BMLR5BPV1P8585EQ31RIU";
    private String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    // URL cũ của bạn bạn
    private String vnp_ReturnUrl = "http://localhost:8080/api/vnpay/vnpay-return";

    // URL MỚI TẠO CHO LUỒNG ĐẶT CỌC
    private String vnp_ReturnUrl_Deposit = "http://localhost:8080/api/vnpay/vnpay-return-deposit";

    @GetMapping("/create-payment/{idHoaDon}")
    public ResponseEntity<?> createPayment(
            @PathVariable String idHoaDon, // 🚨 ĐỔI TỪ Integer SANG String
            @RequestParam(required = false) Long amount, // Dùng để nhận số tiền từ gộp bàn hoặc hỗn hợp
            HttpServletRequest request) throws UnsupportedEncodingException {

        // XỬ LÝ SỐ TIỀN THANH TOÁN
        long finalAmount;
        if (amount != null && amount > 0) {
            // 1. Nếu Frontend truyền tổng tiền xuống (Thanh toán Gộp hoặc Hỗn hợp)
            finalAmount = amount * 100;
        } else {
            // 2. Nếu thanh toán 1 bàn bình thường (Không có amount)
            // Lấy ID đầu tiên trong chuỗi (vd: chuỗi "3" -> lấy 3. Chuỗi "3_1" -> lấy 3)
            Integer idGoc = Integer.parseInt(idHoaDon.split("_")[0]);
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idGoc)
                    .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));
            finalAmount = hoaDon.getTongTienThanhToan().longValue() * 100;
        }

        // VNPay yêu cầu số tiền tối thiểu là 10.000 VNĐ (Nhân 100 lên là 1,000,000)
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

        // 🚨 TẠO MÃ GIAO DỊCH: idHoaDon lúc này có thể là "3" hoặc "3_1"
        // Khi kết hợp, vnp_TxnRef sẽ thành "3_1_1709999999999" (rất chuẩn để hàm Return xử lý)
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

        // Chuỗi chứa các ID hóa đơn ghép lại (VD: "15_16_1699999999")
        String vnp_TxnRef = fields.get("vnp_TxnRef");

        if (signValue.equals(vnp_SecureHash)) {
            if ("00".equals(vnp_ResponseCode)) {

                // Lấy trước phương thức thanh toán VNPay để dùng chung trong vòng lặp (Tối ưu hiệu năng)
                PhuongThucThanhToan ptVnPay = phuongThucThanhToanRepository.findByMaPhuongThuc("PT01");

                // 🚨 TÁCH CHUỖI ID HÓA ĐƠN VÀ CHẠY VÒNG LẶP
                String[] idArray = vnp_TxnRef.split("_");

                for (String idStr : idArray) {
                    try {
                        // Ép kiểu ID. Nếu phần tử cuối là Timestamp (không phải số), nó sẽ nhảy vào catch và bỏ qua.
                        Integer idHoaDon = Integer.parseInt(idStr);

                        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon).orElse(null);

                        if (hoaDon != null && hoaDon.getTrangThaiHoaDon() != 6) { // Chỉ xử lý nếu chưa thanh toán (Tránh VNPay gọi callback 2 lần)
                            Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();

                            // 1. CẬP NHẬT TRẠNG THÁI HÓA ĐƠN -> Đã thanh toán (6)
                            hoaDon.setTrangThaiHoaDon(6);
                            hoaDon.setThoiGianThanhToan(Instant.now().plus(7, ChronoUnit.HOURS));

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

                            // 4. TÍNH TOÁN SỐ TIỀN THỰC TẾ CỦA RIÊNG HÓA ĐƠN NÀY (ĐỂ GHI VÀO ĐỐI SOÁT)
                            // Bằng Tổng tiền cần thanh toán - Số tiền khách đã trả trước đó (như Cọc, Tiền mặt hỗn hợp)
                            BigDecimal tongThanhToan = hoaDon.getTongTienThanhToan() != null ? hoaDon.getTongTienThanhToan() : BigDecimal.ZERO;
                            BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;
                            BigDecimal tienKhachDuaTrk = hoaDon.getTienKhachDua() != null ? hoaDon.getTienKhachDua() : BigDecimal.ZERO;

                            BigDecimal tienThucThuLanNay = tongThanhToan.subtract(tienCoc).subtract(tienKhachDuaTrk);
                            if (tienThucThuLanNay.compareTo(BigDecimal.ZERO) < 0) {
                                tienThucThuLanNay = BigDecimal.ZERO; // Đảm bảo không bị âm
                            }

                            // Cộng dồn tiền vào cột tien_khach_dua (Để biết hóa đơn này đã nhận đủ tiền)
                            hoaDon.setTienKhachDua(tienKhachDuaTrk.add(tienThucThuLanNay));

                            // 📝 5. GHI LOG VÀO TIMELINE LỊCH SỬ HÓA ĐƠN
                            ghiLichSu(
                                    hoaDon,
                                    null, // ID nhân viên để null vì là hệ thống tự xác nhận
                                    "Đã thanh toán VNPay: " + tienThucThuLanNay.setScale(0) + "đ",
                                    "Mã giao dịch VNPay gốc: " + vnp_TxnRef,
                                    trangThaiCu,
                                    6
                            );

                            // 💰 6. LƯU VÀO BẢNG LỊCH SỬ THANH TOÁN (ĐỐI SOÁT THEO TỪNG BÀN)
                            try {
                                LichSuThanhToan lichSu = new LichSuThanhToan();
                                lichSu.setPhuongThucThanhToan(ptVnPay);
                                lichSu.setHoaDon(hoaDon);
                                lichSu.setTenPhuongThuc(ptVnPay != null ? ptVnPay.getTenPhuongThuc() : "VNPay");
                                lichSu.setMaGiaoDich(vnp_TxnRef);
                                lichSu.setSoTienThanhToan(tienThucThuLanNay); // Lưu ĐÚNG số tiền nợ của bàn này, không phải tổng cục VNPay
                                lichSu.setLoaiGiaoDich(1); // Giao dịch thu tiền
                                lichSu.setNgayThanhToan(Instant.now());
                                lichSu.setTrangThai(1); // Thành công
                                lichSu.setGhiChu("Thanh toán điện tử qua VNPay (Gộp bàn)");
                                lichSuThanhToanRepository.save(lichSu);
                            } catch (Exception e) {
                                System.out.println("❌ Lỗi lưu đối soát VNPay cho Hóa đơn " + idHoaDon + ": " + e.getMessage());
                            }

                            hoaDonThanhToanRepository.save(hoaDon);
                        }
                    } catch (NumberFormatException e) {
                        // Nếu phần tử split ra không phải là ID hóa đơn (Ví dụ đoạn timestamp đuôi), thì bỏ qua
                        System.out.println("Bỏ qua thành phần không phải ID trong TxnRef: " + idStr);
                    }
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
                response.sendRedirect("http://localhost:5173/payment-failed?type=deposit");
            }
        } else {
            response.sendRedirect("http://localhost:5173/payment-failed?type=deposit&error=checksum");
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
