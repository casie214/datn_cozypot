package com.example.datn_cozypot_spring_boot.service.AuthenticationService;
import com.example.datn_cozypot_spring_boot.config.JwtUtils;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {
    private final KhachHangRepository khachHangRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    public Map<String, Object> loginWithGoogle(String code) {
        RestTemplate restTemplate = new RestTemplate();

        String tokenEndpoint = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String requestBody = "code=" + code +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&redirect_uri=" + redirectUri +
                "&grant_type=authorization_code";

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, request, String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());
        String accessToken = jsonObject.getString("access_token");

        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;
        ResponseEntity<String> userInfoResponse = restTemplate.getForEntity(userInfoEndpoint, String.class);

        JSONObject userInfo = new JSONObject(userInfoResponse.getBody());
        String email = userInfo.getString("email");
        String name = userInfo.optString("name", "Google User");
        String picture = userInfo.optString("picture", "");

        KhachHang khachHang = khachHangRepository.findKhachHangByEmail(email).orElseThrow(() -> new RuntimeException("Tài khoản gmail này chưa tồn tại trong hệ thống CozyPot"));

        String appAccessToken = tokenProvider.generateToken(khachHang.getEmail(), "USER");
        String appRefreshToken = jwtUtils.generateRefreshToken(khachHang.getEmail());
        System.out.println("Đang đăng nhập cho khách hàng: " + khachHang.getTenKhachHang() + " - ID: " + khachHang.getId());
        return Map.of(
                "accessToken", appAccessToken,
                "refreshToken", appRefreshToken,
                "role", "USER",
                "id", khachHang.getId(),
                "email", khachHang.getEmail(),
                "username", khachHang.getTenDangNhap()
        );
    }
}
