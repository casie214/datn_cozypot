package com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository;

import com.example.datn_cozypot_spring_boot.entity.DinhLuongChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DinhLuongChiTietRepository extends JpaRepository<DinhLuongChiTiet, Long> {
    List<DinhLuongChiTiet> findByDanhMucId(Integer idDanhMuc);
}
