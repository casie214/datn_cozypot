package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGiamGiaRepo extends JpaRepository<PhieuGiamGia, Integer> {
    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
            "(:keyword IS NULL OR p.tenPhieuGiamGia LIKE %:keyword% " +
            "OR p.maPhieuGiamGia LIKE %:keyword% " +
            "OR p.codeGiamGia LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR p.trangThai = :trangThai)")
    Page<PhieuGiamGia> searchFilter(@Param("keyword") String keyword,
                                    @Param("trangThai") Integer trangThai,
                                    Pageable pageable);

}