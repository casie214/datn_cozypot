package com.example.datn_cozypot_spring_boot.service.AuthenticationService;

import com.example.datn_cozypot_spring_boot.config.AuthProvider;
import com.example.datn_cozypot_spring_boot.config.RegisterRequest;
import com.example.datn_cozypot_spring_boot.entity.DiaChiKhachHang; // Import Entity mới
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.repository.DangKyTamRepository;
import com.example.datn_cozypot_spring_boot.repository.DiaChiKhachHangRepository; // Cần Repository này
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Nên có Transaction

import java.time.Instant;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final NhanVienRepository nhanVienRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final DangKyTamRepository dangKyTamRepository;
    private final JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper; // Jackson dùng để biến Object thành JSON string

    @Transactional
    public void register(RegisterRequest request) {
        if (khachHangRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại!");
        }
        if (khachHangRepository.existsByEmail(request.getEmail()) ||
                nhanVienRepository.existsByTenDangNhap(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại trên hệ thống!");
        }
        if (khachHangRepository.existsBySoDienThoai(request.getSoDienThoai())) {
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        }

        try {
            request.setMatKhauDangNhap(passwordEncoder.encode(request.getMatKhauDangNhap()));
            String otp = String.format("%06d", new java.util.Random().nextInt(1000000));
            String jsonPayload = objectMapper.writeValueAsString(request);
            dangKyTamRepository.deleteByEmail(request.getEmail());
            com.example.datn_cozypot_spring_boot.entity.DangKyTam temp = new com.example.datn_cozypot_spring_boot.entity.DangKyTam();
            temp.setEmail(request.getEmail());
            temp.setOtp(otp);
            temp.setDataJson(jsonPayload);
            temp.setExpireAt(java.time.LocalDateTime.now().plusMinutes(15));
            dangKyTamRepository.save(temp);
            sendOtpEmail(request.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi xảy ra khi xử lý đăng ký: " + e.getMessage());
        }
    }

    // Hàm gửi mail bổ trợ
    public void sendOtpEmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("CozyPot <linhnnth03605@fpt.edu.vn>");
            helper.setTo(email);
            helper.setSubject("Mã OTP xác thực đăng ký - CozyPot");

            String content = "<div style=\"background-color: #f4f4f4; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">" +
                    "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                    "        <div style=\"background-color: #800000; padding: 25px; text-align: center; color: #ffffff;\">" +
                    "            <h1 style=\"margin: 0; font-size: 24px; letter-spacing: 2px;\">COZYPOT</h1>" +
                    "            <p style=\"margin: 5px 0 0; opacity: 0.8; font-size: 12px;\">XÁC THỰC TÀI KHOẢN</p>" +
                    "        </div>" +
                    "        <div style=\"padding: 25px;\">" +
                    "            <p>Xin chào,</p>" +
                    "            <p>Chào mừng bạn đến với <b>CozyPot</b>. Để hoàn tất quá trình đăng ký, vui lòng sử dụng mã OTP dưới đây:</p>" +
                    "            <div style=\"border-top: 1px solid #eee; padding-top: 15px; margin-top: 15px;\">" +
                    "                <div style=\"margin-bottom: 10px; background-color: #fff4f4; padding: 15px; border-radius: 4px; border-left: 4px solid #800000; text-align: center;\">" +
                    "                    <span style=\"color: #888; font-size: 13px; display: block; margin-bottom: 10px;\">Mã xác thực của bạn là:</span>" +
                    "                    <span style=\"color: #800000; font-weight: bold; font-size: 32px; letter-spacing: 5px;\">" + otp + "</span>" +
                    "                    <p style=\"margin: 10px 0 0; font-size: 12px; color: #666;\">Mã có hiệu lực trong <b>15 phút</b>.</p>" +
                    "                </div>" +
                    "            </div>" +
                    "            <p style=\"font-size: 13px; color: #cc0000; margin-top: 20px;\"><b>Lưu ý:</b> Vui lòng không cung cấp mã này cho bất kỳ ai để bảo mật tài khoản.</p>" +
                    "            <p style=\"font-size: 13px; color: #666;\">Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>" +
                    "        </div>" +
                    "        <div style=\"background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 11px; color: #999;\">" +
                    "            © 2026 CozyPot Restaurant. Hệ thống gửi tin tự động." +
                    "        </div>" +
                    "    </div>" +
                    "</div>";

            helper.setText(content, true); // Quan trọng: true để hiểu đây là HTML
            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
            // Bạn có thể ném ra RuntimeException nếu cần xử lý ở Controller
        }
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        com.example.datn_cozypot_spring_boot.entity.DangKyTam temp = dangKyTamRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu đăng ký hoặc mã đã hết hạn!"));
        if (temp.getExpireAt().isBefore(java.time.LocalDateTime.now())) {
            dangKyTamRepository.delete(temp);
            throw new RuntimeException("Mã OTP đã hết hạn, vui lòng đăng ký lại!");
        }
        if (!temp.getOtp().equals(otp)) {
            throw new RuntimeException("Mã OTP không chính xác!");
        }
        try {
            RegisterRequest request = objectMapper.readValue(temp.getDataJson(), RegisterRequest.class);
            KhachHang user = new KhachHang();
            user.setTenDangNhap(request.getTenDangNhap());
            user.setEmail(request.getEmail());
            user.setTenKhachHang(request.getTenKhachHang());
            user.setSoDienThoai(request.getSoDienThoai());
            user.setGioiTinh(request.getGioiTinh());
            user.setNgaySinh(request.getNgaySinh());
            user.setMatKhauDangNhap(request.getMatKhauDangNhap());
            user.setTrangThai(1);
            user.setAuthProvider(AuthProvider.LOCAL);
            user.setDiemTichLuy(0);
            user.setNgayTaoTaiKhoan(Instant.now());
            KhachHang savedUser = khachHangRepository.save(user);
            DiaChiKhachHang diaChi = new DiaChiKhachHang();
            diaChi.setKhachHang(savedUser);
            diaChi.setIdTinhThanh(request.getIdTinhThanh());
            diaChi.setIdQuanHuyen(request.getIdQuanHuyen());
            diaChi.setIdPhuongXa(request.getIdPhuongXa());
            diaChi.setTenTinhThanh(request.getTenTinhThanh());
            diaChi.setTenQuanHuyen(request.getTenQuanHuyen());
            diaChi.setTenPhuongXa(request.getTenPhuongXa());
            diaChi.setDiaChiChiTiet(request.getDiaChiChiTiet());
            diaChi.setHoTenNhan(request.getTenKhachHang());
            diaChi.setSoDienThoaiNhan(request.getSoDienThoai());
            diaChi.setLaMacDinh(true);
            diaChiKhachHangRepository.save(diaChi);
            dangKyTamRepository.delete(temp);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi hoàn tất đăng ký: " + e.getMessage());
        }
    }

    @Transactional
    public void resendOtp(String email) {
        com.example.datn_cozypot_spring_boot.entity.DangKyTam temp = dangKyTamRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Yêu cầu không còn hiệu lực, vui lòng đăng ký lại!"));
        String newOtp = String.format("%06d", new java.util.Random().nextInt(1000000));
        temp.setOtp(newOtp);
        temp.setExpireAt(java.time.LocalDateTime.now().plusMinutes(15));
        dangKyTamRepository.save(temp);
        sendOtpEmail(email, newOtp);
    }

}