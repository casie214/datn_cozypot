package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.DangKyTam;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DangKyTamRepository extends JpaRepository<DangKyTam, Integer> {
    // Tìm kiếm thông tin tạm bằng email để xác thực OTP sau này
    Optional<DangKyTam> findByEmail(String email);

    // Xóa dữ liệu cũ nếu người dùng đăng ký lại bằng email đó
    void deleteByEmail(String email);
}
