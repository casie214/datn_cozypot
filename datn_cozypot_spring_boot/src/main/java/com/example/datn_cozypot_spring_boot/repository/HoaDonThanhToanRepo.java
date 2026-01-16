package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HoaDonThanhToanRepo extends JpaRepository<HoaDonThanhToan, Integer> {
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse(" +
            "hd.id, " +
            "hd.maHoaDon, " +
            "kh.tenKhachHang, " +
            "kh.soDienThoai, " +
            "b.tenBan, " +
            "hd.tongTienThanhToan, " +
            "hd.soTienDaGiam, " +
            "hd.trangThaiHoaDon, " +
            "hd.thoiGianTao) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " + // Join qua field idKhachHang trong Entity
            "LEFT JOIN hd.idBanAn b " +      // Join qua field idBanAn trong Entity
            "ORDER BY " +
            "CASE " +
            "  WHEN hd.trangThaiHoaDon = 1 THEN 1 " + // Đã xác nhận lên đầu
            "  WHEN hd.trangThaiHoaDon = 2 THEN 2 " + // Hoàn thành thứ 2
            "  ELSE 3 " +                             // Hủy xuống cuối
            "END ASC, " +
            "hd.thoiGianTao DESC")
    List<HoaDonThanhToanResponse> getAllHoaDon();
}