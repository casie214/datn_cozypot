package com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    @Query("SELECT m.maDanhMuc FROM DanhMuc m WHERE m.maDanhMuc LIKE :prefix% ORDER BY m.maDanhMuc DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<DanhMuc> findByTrangThai(int i);
}
