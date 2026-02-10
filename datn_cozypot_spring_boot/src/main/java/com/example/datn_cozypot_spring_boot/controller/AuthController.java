package com.example.datn_cozypot_spring_boot.controller;

<<<<<<< HEAD
import com.example.datn_cozypot_spring_boot.config.AuthResponse;
import com.example.datn_cozypot_spring_boot.config.LoginRequest;
import com.example.datn_cozypot_spring_boot.config.RegisterRequest;
=======
import com.example.datn_cozypot_spring_boot.config.*;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
<<<<<<< HEAD
import com.example.datn_cozypot_spring_boot.security.JwtTokenProvider;
import com.example.datn_cozypot_spring_boot.service.AuthenticationService.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
=======
import com.example.datn_cozypot_spring_boot.security.CustomUserDetailsService;
import com.example.datn_cozypot_spring_boot.security.JwtTokenProvider;
import com.example.datn_cozypot_spring_boot.service.AuthenticationService.AuthService;
import com.example.datn_cozypot_spring_boot.service.AuthenticationService.GoogleAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
<<<<<<< HEAD
=======
import java.util.Map;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77

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
<<<<<<< HEAD

    //login cho admin
=======
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final GoogleAuthService googleAuthService;

>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest req) {
        NhanVien nv = nhanVienRepository.findNhanVienByTenDangNhap(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        if (!passwordEncoder.matches(req.getPassword(), nv.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Sai mật khẩu");
        }

        String role = nv.getIdVaiTro().getTenVaiTro();
<<<<<<< HEAD
        String token = tokenProvider.generateToken(nv.getTenDangNhap(), role);

        return ResponseEntity.ok(new AuthResponse(token, role));
=======
        String accessToken = tokenProvider.generateToken(nv.getTenDangNhap(), role);
        String refreshToken = jwtUtils.generateRefreshToken(nv.getTenDangNhap());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, role));
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
    }

    //login cho khách hàng
    @PostMapping("/client/login")
    public ResponseEntity<?> loginClient(@RequestBody LoginRequest req) {
<<<<<<< HEAD
        KhachHang kh = khachHangRepository.findKhachHangByEmail(req.getUsername()) // Giả sử dùng SĐT đăng nhập
=======
        KhachHang kh = khachHangRepository.findKhachHangByEmail(req.getUsername())
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        if (!passwordEncoder.matches(req.getPassword(), kh.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Sai mật khẩu");
        }

<<<<<<< HEAD
        String token = tokenProvider.generateToken(kh.getEmail(), "USER");
        return ResponseEntity.ok(new AuthResponse(token, "USER"));
=======
        String accessToken = tokenProvider.generateToken(kh.getEmail(), "USER");
        String refreshToken = jwtUtils.generateRefreshToken(kh.getTenDangNhap());

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, "USER"));
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Đăng ký thành công! Vui lòng đăng nhập."));
    }
<<<<<<< HEAD
=======

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            if (tokenProvider.validateToken(requestRefreshToken)) {
                String username = tokenProvider.getUsernameFromToken(requestRefreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                String fullRole = userDetails.getAuthorities().stream()
                        .findFirst()
                        .map(item -> item.getAuthority())
                        .orElse("USER");

                String role = fullRole.replace("ROLE_", "");

                String newAccessToken = tokenProvider.generateToken(username, role);

                return ResponseEntity.ok(new RefreshTokenResponse(newAccessToken, requestRefreshToken, role));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Lỗi refresh token");
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Refresh token không hợp lệ");
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        try {
            Map<String, String> result = googleAuthService.loginWithGoogle(code);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi đăng nhập Google: " + e.getMessage());
        }
    }
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
}
