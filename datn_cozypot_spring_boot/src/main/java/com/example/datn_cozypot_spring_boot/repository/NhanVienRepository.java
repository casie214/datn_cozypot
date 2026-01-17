package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    @Query("SELECT nv FROM NhanVien nv WHERE " +
            "(:keyword IS NULL OR nv.hoTenNhanVien LIKE %:keyword% OR nv.maNhanVien LIKE %:keyword% " +
            "OR nv.sdtNhanVien LIKE %:keyword% OR nv.email LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR nv.trangThaiLamViec = :trangThai) AND " +
            "(:tuNgay IS NULL OR nv.ngayVaoLam >= :tuNgay)")
    Page<NhanVien> searchNhanVien(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") LocalDate tuNgay,
            Pageable pageable);
}