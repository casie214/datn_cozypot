package com.example.datn_cozypot_spring_boot.service.AuthenticationService;

import com.example.datn_cozypot_spring_boot.dto.authentication.DoiMatKhauRequest;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
@Service
@RequiredArgsConstructor
public class TaiKhoanService {

    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService; // Inject MailService của bạn vào đây

    @Transactional
    public void doiMatKhau(String identifier, DoiMatKhauRequest request) {
        // 1. Tìm trong Nhân viên trước
        Optional<NhanVien> nvOpt = nhanVienRepository.findByTenDangNhap(identifier);
        if (nvOpt.isEmpty()) nvOpt = nhanVienRepository.findByEmail(identifier);

        if (nvOpt.isPresent()) {
            NhanVien nv = nvOpt.get();
            validate(request, nv.getMatKhauDangNhap());
            nv.setMatKhauDangNhap(passwordEncoder.encode(request.getMatKhauMoi()));
            nhanVienRepository.save(nv);
            return;
        }

        // 2. Không thấy nhân viên thì tìm Khách hàng (Dành cho người không có Role)
        Optional<KhachHang> khOpt = khachHangRepository.findByTenDangNhap(identifier);
        if (khOpt.isEmpty()) khOpt = khachHangRepository.findByEmail(identifier);

        if (khOpt.isPresent()) {
            KhachHang kh = khOpt.get();
            validate(request, kh.getMatKhauDangNhap());

            // Nếu dùng login thường thì bỏ qua check Provider hoặc check LOCAL
            if (kh.getAuthProvider() != null && !kh.getAuthProvider().toString().equals("LOCAL")) {
                throw new RuntimeException("Tài khoản mạng xã hội không thể đổi mật khẩu!");
            }

            kh.setMatKhauDangNhap(passwordEncoder.encode(request.getMatKhauMoi()));
            khachHangRepository.save(kh);
            return;
        }

        throw new RuntimeException("Không tìm thấy tài khoản để đổi mật khẩu!");
    }
    private void validate(DoiMatKhauRequest request, String currentEncodedPass) {
        if (!passwordEncoder.matches(request.getMatKhauCu(), currentEncodedPass)) {
            throw new RuntimeException("Mật khẩu hiện tại không chính xác!");
        }
    }
}