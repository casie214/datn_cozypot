package com.example.datn_cozypot_spring_boot.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtAuthenticationEntryPoint unauthorizedHandler;

    // Giữ lại từ nhánh main để xử lý lỗi 403
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorizedHandler)
                        .accessDeniedHandler(accessDeniedHandler) // Xử lý 403 từ main
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()
                        // Auth & Tài khoản (Từ main)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/tai-khoan/doi-mat-khau").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/tai-khoan/doi-mat-khau").authenticated()
                        .requestMatchers("/api/auth/refresh-token").permitAll()
                        .requestMatchers("/api/phieu-giam-gia/public").permitAll()
                        .requestMatchers("/api/refresh-token").permitAll()

                        .requestMatchers("/api/payment/**").permitAll() // Từ code của bạn
                        .requestMatchers("/api/phieu-giam-gia/export-excel").permitAll()
                        .requestMatchers("/api/dot-khuyen-mai/export-excel").permitAll()
                        .requestMatchers("/api/guest/**").permitAll()
                        .requestMatchers("/api/khach-hang/**").permitAll()
                        .requestMatchers("/api/tham-so-he-thong/**").permitAll()
                        .requestMatchers("/api/thong-ke/**").permitAll()
                        .requestMatchers("/api/mon-an-di-kem/**").permitAll()
                        .requestMatchers("/api/set-lau/**").permitAll()

                        // Đặt bàn (Public) - Từ code của bạn
                        // Lưu ý: Các API cụ thể này phải đặt TRƯỚC rule "/api/dat-ban/**" ở bên dưới
                        .requestMatchers(HttpMethod.POST, "/api/dat-ban/check-ban-trong").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/dat-ban/tao-moi").permitAll()

                        // VNPay (Gộp của bạn và main)
                        .requestMatchers("/api/vnpay/vnpay-return").permitAll()
                        .requestMatchers("/api/vnpay/create-payment/**").permitAll()
                        .requestMatchers("/api/vnpay/vnpay-return-deposit").permitAll()
                        .requestMatchers("/api/vnpay/create-payment-deposit/**").permitAll()
                        .requestMatchers("/api/vnpay/**").permitAll() // Của main bao trọn vnpay

                        // Đặt bàn (Protected)
                        .requestMatchers(HttpMethod.GET, "/api/dat-ban/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/dat-ban/search").hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.GET, "/api/lich-su-dat-ban/tra-cuu").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lich-su-dat-ban/chi-tiet/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/lich-su-dat-ban/huy-don/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lich-su-dat-ban/ca-nhan").authenticated()

                        // Cho ADMIN và EMPLOYEE tự cập nhật profile
                        .requestMatchers(HttpMethod.GET, "/api/nhan-vien/my-profile")
                        .hasAnyRole("ADMIN", "EMPLOYEE")

                        .requestMatchers(HttpMethod.PUT, "/api/nhan-vien/update-my-profile")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/botpress/handoff").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/botpress/waiting-list").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.GET, "/api/botpress/history/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/botpress/reply").hasAnyRole("ADMIN", "EMPLOYEE")
                        // General API Rules
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "EMPLOYEE")
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN")

                        // Static resources & Swagger
                        .requestMatchers("/dat-ban/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedOrigins(List.of("http://localhost:5173",
                "https://unrheumatic-gametically-yajaira.ngrok-free.dev",
                "https://*.ngrok-free.app"));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}