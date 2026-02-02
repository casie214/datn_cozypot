package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    @Query("SELECT kh FROM KhachHang kh WHERE " +
            "(:keyword IS NULL OR kh.maKhachHang LIKE %:keyword% " +
            "OR kh.tenKhachHang LIKE %:keyword% " + // Đã khớp với ten_khach_hang
            "OR kh.soDienThoai LIKE %:keyword% " +
            "OR kh.email LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR kh.trangThai = :trangThai) AND " +
            "(CAST(:tuNgay AS date) IS NULL OR kh.ngayTaoTaiKhoan >= :tuNgay)")
    Page<KhachHang> searchKhachHang(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") LocalDateTime tuNgay,
            Pageable pageable);

    // Các hàm check trùng khớp với tên biến Entity
    boolean existsBySoDienThoai(String soDienThoai);
    boolean existsByEmail(String email);
    boolean existsByTenDangNhap(String tenDangNhap);

    // Check trùng khi update (trừ ID hiện tại)
    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByTenDangNhapAndIdNot(String tenDangNhap, Integer id);
}
