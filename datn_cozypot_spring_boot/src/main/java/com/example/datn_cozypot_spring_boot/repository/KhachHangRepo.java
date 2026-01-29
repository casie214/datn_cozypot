package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepo extends JpaRepository<KhachHang, Integer> {
}
