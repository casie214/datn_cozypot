package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.DiaChiKhachHang;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Integer> {
    // Hàm này để xóa địa chỉ cũ khi cập nhật
    void deleteByKhachHang(KhachHang khachHang);
}