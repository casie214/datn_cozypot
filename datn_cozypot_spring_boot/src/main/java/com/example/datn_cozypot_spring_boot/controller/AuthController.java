package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.config.*;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.example.datn_cozypot_spring_boot.security.CustomUserDetailsService;
import com.example.datn_cozypot_spring_boot.security.JwtTokenProvider;
import com.example.datn_cozypot_spring_boot.service.AuthenticationService.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
@RequestMapping ("/api/auth")
public class AuthController {
    private final NhanVienRepository repository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;

    //login cho admin
    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest req) {
        NhanVien nv = nhanVienRepository.findNhanVienByTenDangNhap(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        if (!passwordEncoder.matches(req.getPassword(), nv.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Sai mật khẩu");
        }

        String role = nv.getIdVaiTro().getTenVaiTro();
        String accessToken = tokenProvider.generateToken(nv.getTenDangNhap(), role);
        String refreshToken = jwtUtils.generateRefreshToken(nv.getTenDangNhap());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, role));
    }

    //login cho khách hàng
    @PostMapping("/client/login")
    public ResponseEntity<?> loginClient(@RequestBody LoginRequest req) {
        KhachHang kh = khachHangRepository.findKhachHangByEmail(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        if (!passwordEncoder.matches(req.getPassword(), kh.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Sai mật khẩu");
        }

        String accessToken = tokenProvider.generateToken(kh.getEmail(), "USER");
        String refreshToken = jwtUtils.generateRefreshToken(kh.getTenDangNhap());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, "USER"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Đăng ký thành công! Vui lòng đăng nhập."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            if (tokenProvider.validateToken(requestRefreshToken)) {
                // 1. Lấy username từ refresh token
                String username = tokenProvider.getUsernameFromToken(requestRefreshToken);

                // 2. Load lại thông tin user từ DB để lấy Role mới nhất
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 3. Lấy Role ra (Giả sử User chỉ có 1 role, nếu nhiều role phải xử lý list)
                String role = userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(item -> item.getAuthority())
                        .orElse("USER"); // Mặc định nếu không tìm thấy

                // 4. 👇 QUAN TRỌNG: Gọi hàm tạo token có tham số ROLE
                String newAccessToken = tokenProvider.generateToken(username, role);

                // 5. Tạo Refresh token mới (nếu muốn xoay vòng) hoặc trả lại cái cũ
                // Lưu ý: Nếu tạo refresh token mới thì nhớ dùng hàm generateRefreshToken (hạn dài)

                return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, requestRefreshToken, role));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Lỗi refresh token");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh token không hợp lệ");
    }
}
