package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    // KhachHangRepository.java
    @Query("SELECT kh FROM KhachHang kh WHERE " +
            "(:keyword IS NULL OR kh.maKhachHang LIKE %:keyword% " +
            "OR kh.tenKhachHang LIKE %:keyword% " +
            "OR kh.soDienThoai LIKE %:keyword% " +
            "OR kh.email LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR kh.trangThai = :trangThai) AND " +
            "(CAST(:tuNgay AS date) IS NULL OR kh.ngayTaoTaiKhoan >= :tuNgay)")
    Page<KhachHang> searchKhachHang( // Tên hàm phải là searchKhachHang
                                     @Param("keyword") String keyword,
                                     @Param("trangThai") Integer trangThai,
                                     @Param("tuNgay") java.time.LocalDateTime tuNgay, // Dùng LocalDateTime để khớp với DB
                                     Pageable pageable);

    // Kiểm tra trùng lặp dữ liệu
    boolean existsBySoDienThoai(String soDienThoai);
    boolean existsByTenDangNhap(String tenDangNhap);

    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = 1")
    List<KhachHang> findAllByTrangThaiActive();

}