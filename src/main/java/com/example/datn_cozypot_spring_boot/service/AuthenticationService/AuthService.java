package com.example.datn_cozypot_spring_boot.service.AuthenticationService;

import com.example.datn_cozypot_spring_boot.config.AuthProvider;
import com.example.datn_cozypot_spring_boot.config.RegisterRequest;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final KhachHangRepository khachHangRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (khachHangRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new RuntimeException("Tên hiển thị đã tồn tại!");
        }
        if (khachHangRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        if(khachHangRepository.existsBySoDienThoai(request.getSoDienThoai())) throw new RuntimeException("Số điện thoại đã tồn tại!");


        KhachHang user = new KhachHang();
        user.setTenDangNhap(request.getTenDangNhap());
        user.setEmail(request.getEmail());
        user.setTenKhachHang(request.getTenKhachHang());
        user.setDiaChi(request.getDiaChi());
        user.setSoDienThoai(request.getSoDienThoai());
        user.setGioiTinh(request.getGioiTinh());
        user.setNgaySinh(request.getNgaySinh());


        String encodedPass = passwordEncoder.encode(request.getMatKhauDangNhap());
        user.setMatKhauDangNhap(encodedPass);

        user.setTrangThai(1);
        user.setAuthProvider(AuthProvider.LOCAL);
        user.setDiemTichLuy(0);
        user.setNgayTaoTaiKhoan(Instant.now());


        khachHangRepository.save(user);
    }
}
