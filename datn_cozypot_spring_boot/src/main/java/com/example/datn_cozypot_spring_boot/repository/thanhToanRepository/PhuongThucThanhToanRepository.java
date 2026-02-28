package com.example.datn_cozypot_spring_boot.repository.thanhToanRepository;

import com.example.datn_cozypot_spring_boot.entity.PhuongThucThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhuongThucThanhToanRepository extends JpaRepository<PhuongThucThanhToan, Integer> {
    PhuongThucThanhToan findByMaPhuongThuc(String maPhuongThuc);
}
