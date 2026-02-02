package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.config.AuthResponse;
import com.example.datn_cozypot_spring_boot.config.LoginRequest;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.example.datn_cozypot_spring_boot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/api/auth")
public class AuthController {
    private final NhanVienRepository repository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    //login cho admin
    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest req) {
        NhanVien nv = nhanVienRepository.findNhanVienByTenDangNhap(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        if (!passwordEncoder.matches(req.getPassword(), nv.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Sai mật khẩu");
        }

        String role = nv.getIdVaiTro().getTenVaiTro();
        String token = tokenProvider.generateToken(nv.getTenDangNhap(), role);

        return ResponseEntity.ok(new AuthResponse(token, role));
    }

    //login cho khách hàng
    @PostMapping("/client/login")
    public ResponseEntity<?> loginClient(@RequestBody LoginRequest req) {
        KhachHang kh = khachHangRepository.findKhachHangByEmail(req.getUsername()) // Giả sử dùng SĐT đăng nhập
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        if (!passwordEncoder.matches(req.getPassword(), kh.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Sai mật khẩu");
        }

        String token = tokenProvider.generateToken(kh.getEmail(), "USER");
        return ResponseEntity.ok(new AuthResponse(token, "USER"));
    }
}
