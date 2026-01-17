package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idBanAn b " +
            "ORDER BY " +
            "CASE " +
            "  WHEN hd.trangThaiHoaDon = 1 THEN 1 " + // Đã xác nhận lên đầu
            "  WHEN hd.trangThaiHoaDon = 2 THEN 2 " + // Hoàn thành thứ 2
            "  ELSE 3 " +                             // Hủy xuống cuối
            "END ASC, " +
            "hd.thoiGianTao DESC")
    List<HoaDonThanhToanResponse> getAllHoaDon();

    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, b.tenBan, " +
            "hd.tongTienThanhToan, hd.soTienDaGiam, hd.trangThaiHoaDon, hd.thoiGianTao) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idBanAn b " +
            "WHERE (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai) " +
            "AND (CAST(:tuNgay AS timestamp) IS NULL OR hd.thoiGianTao >= :tuNgay) " +
            "AND (CAST(:denNgay AS timestamp) IS NULL OR hd.thoiGianTao <= :denNgay) " +
            "ORDER BY " +
            "CASE WHEN hd.trangThaiHoaDon = 1 THEN 1 WHEN hd.trangThaiHoaDon = 2 THEN 2 ELSE 3 END ASC, " +
            "hd.thoiGianTao DESC")
    List<HoaDonThanhToanResponse> searchHoaDon(
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay
    );

    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, b.tenBan, " +
            "hd.tongTienThanhToan, hd.soTienDaGiam, hd.trangThaiHoaDon, hd.thoiGianTao) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idBanAn b " +
            "WHERE hd.id = :id")
    HoaDonThanhToanResponse getHoaDonById(@Param("id") Integer id);
}