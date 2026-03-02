package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.config.*;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.example.datn_cozypot_spring_boot.security.CustomUserDetailsService;
import com.example.datn_cozypot_spring_boot.security.JwtTokenProvider;
import com.example.datn_cozypot_spring_boot.service.AuthenticationService.AuthService;
import com.example.datn_cozypot_spring_boot.service.AuthenticationService.GoogleAuthService;
import com.example.datn_cozypot_spring_boot.service.AuthenticationService.MailService;
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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final NhanVienRepository repository;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;
    private final GoogleAuthService googleAuthService;
    private final MailService mailService;

    @PostMapping("/admin/login")
    public ResponseEntity<?> loginAdmin(@RequestBody LoginRequest req) {
        NhanVien nv = nhanVienRepository.findByEmail(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Email nhân viên không tồn tại"));
        if (nv.getTrangThaiLamViec() != 1) {
            return ResponseEntity.status(403).body("Tài khoản nhân viên đã bị ngừng hoạt động.");
        }
        if (!passwordEncoder.matches(req.getPassword(), nv.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Vui lòng kiếm tra lại thông tin đăng nhập của bạn");
        }

        String role = nv.getIdVaiTro().getTenVaiTro();
        String accessToken = tokenProvider.generateToken(nv.getEmail(), role);
        String refreshToken = jwtUtils.generateRefreshToken(nv.getEmail());

        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                refreshToken,
                role,
                nv.getId(),
                nv.getTenDangNhap(),
                nv.getHoTenNhanVien(),
                nv.getEmail(),         // Bổ sung email
                nv.getSdtNhanVien()    // SDT đã có
        ));
    }

    @PostMapping("/client/login")
    public ResponseEntity<?> loginClient(@RequestBody LoginRequest req) {

        KhachHang kh = khachHangRepository
                .findByEmail(req.getUsername())
                .orElseThrow(() -> new RuntimeException("Email khách hàng không tồn tại"));
        if (kh.getTrangThai() != 1) {
            return ResponseEntity.status(403).body("Tài khoản khách hàng đã bị ngừng hoạt động");
        }

        if (!passwordEncoder.matches(req.getPassword(), kh.getMatKhauDangNhap())) {
            return ResponseEntity.status(401).body("Vui lòng kiếm tra lại thông tin đăng nhập của bạn");
        }

//        String identifier = (kh.getTenDangNhap() != null && !kh.getTenDangNhap().isEmpty())
//                ? kh.getTenDangNhap()
//                : kh.getEmail();
//
//        String accessToken = tokenProvider.generateToken(identifier, "USER");
//        String refreshToken = jwtUtils.generateRefreshToken(identifier);

        String identifier = kh.getEmail();

        String accessToken = tokenProvider.generateToken(identifier, "USER");
        String refreshToken = jwtUtils.generateRefreshToken(identifier);

        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                refreshToken,
                "USER",
                kh.getId(),
                kh.getTenDangNhap(),
                kh.getTenKhachHang(),
                kh.getEmail(),         // Bổ sung email
                kh.getSoDienThoai()    // SDT đã có
        ));
    }


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Mã xác thực OTP đã được gửi đến email của bạn. Vui lòng kiểm tra!"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        try {
            authService.verifyOtp(email, otp);
            return ResponseEntity.ok(Collections.singletonMap("message", "Đăng ký thành công! Vui lòng đăng nhập."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            // Bạn cần thêm hàm resendOtp trong AuthService (logic: tạo OTP mới, update bảng tạm, gửi lại mail)
            // Nếu chưa kịp viết resendOtp, bạn có thể tạm gọi lại hàm register nếu bạn đã xử lý MERGE/Delete cũ
            // Ở đây tôi gọi một hàm giả định trong service:
            authService.resendOtp(email);
            return ResponseEntity.ok(Collections.singletonMap("message", "Mã OTP mới đã được gửi!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("message", "Không thể gửi lại mã!"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            if (tokenProvider.validateToken(requestRefreshToken)) {
                String username = tokenProvider.getEmailFromToken(requestRefreshToken);
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
            Map<String, Object> result = googleAuthService.loginWithGoogle(code);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            if (e.getMessage().contains("chưa tồn tại")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi đăng nhập Google: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        try {
            Optional<KhachHang> kh = khachHangRepository.findKhachHangByEmail(email);
            if (kh.isPresent()) {
                String newPassword = generateRandomPassword();
                kh.get().setMatKhauDangNhap(passwordEncoder.encode(newPassword));
                khachHangRepository.save(kh.get());
                mailService.sendForgotPasswordEmail(email, newPassword);
                return ResponseEntity.ok("Mật khẩu mới đã được gửi đến email của bạn.");
            }

            Optional<NhanVien> nv = nhanVienRepository.findNhanVienByTenDangNhap(email);
            if (nv.isPresent()) {
                String newPassword = generateRandomPassword();
                nv.get().setMatKhauDangNhap(passwordEncoder.encode(newPassword));
                nhanVienRepository.save(nv.get());

                mailService.sendForgotPasswordEmail(email, newPassword);
                return ResponseEntity.ok("Mật khẩu mới đã được gửi đến email của bạn.");
            }
            return ResponseEntity.status(404).body("Email không tồn tại trong hệ thống!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    private String generateRandomPassword() {
        return String.valueOf((int) ((Math.random() * 899999) + 100000)); // Tạo số ngẫu nhiên 6 chữ số
    }
}
