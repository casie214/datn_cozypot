package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface HoaDonThanhToanRepository extends JpaRepository<HoaDonThanhToan, Integer> {

    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, " +
            "hd.maHoaDon, " +
            "kh.tenKhachHang, " +
            "kh.soDienThoai, " +
            "b.tenBan, " +
            "hd.tongTienChuaGiam, " +
            "hd.soTienDaGiam, " +
            "hd.tongTienThanhToan, " +
            "hd.tienCoc, " +
            "hd.tienHoanTra, " +
            "hd.trangThaiHoanTien, " +
            "hd.trangThaiHoaDon, " +
            "hd.thoiGianTao, " +
            "pdb.hinhThucDat,pdb.thoiGianDat , pdb.soLuongKhach, hd.vatApDung) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idBanAn b " +
            "LEFT JOIN hd.idPhieuDatBan pdb")
    Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable);

    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, " +
            "hd.maHoaDon, " +
            "kh.tenKhachHang, " +
            "kh.soDienThoai, " +
            "b.tenBan, " +
            "hd.tongTienChuaGiam, " +
            "hd.soTienDaGiam, " +
            "hd.tongTienThanhToan, " +
            "hd.tienCoc, " +
            "hd.tienHoanTra, " +
            "hd.trangThaiHoanTien, " +
            "hd.trangThaiHoaDon, " +
            "hd.thoiGianTao, " +
            "pdb.hinhThucDat,pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idBanAn b " +
            "LEFT JOIN hd.idPhieuDatBan pdb " +
            "WHERE (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai) " +
            "AND (:trangThaiHoanTien IS NULL OR hd.trangThaiHoanTien = :trangThaiHoanTien) " +
            "AND (CAST(:tuNgay AS timestamp) IS NULL OR hd.thoiGianTao >= :tuNgay) " +
            "AND (CAST(:denNgay AS timestamp) IS NULL OR hd.thoiGianTao <= :denNgay) " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "     OR LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(kh.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(kh.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY hd.thoiGianTao ASC")
    Page<HoaDonThanhToanResponse> searchHoaDon(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            @Param("trangThaiHoanTien") Integer trangThaiHoanTien,
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            Pageable pageable
    );

    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, b.tenBan, " +
            "hd.tongTienChuaGiam, hd.soTienDaGiam, hd.tongTienThanhToan, " +
            "hd.tienCoc, hd.tienHoanTra, hd.trangThaiHoanTien, hd.trangThaiHoaDon, " +
            "hd.thoiGianTao, pdb.hinhThucDat, pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idBanAn b " +
            "LEFT JOIN hd.idPhieuDatBan pdb " +
            "WHERE hd.id = :id")
    HoaDonThanhToanResponse getHoaDonById(@Param("id") Integer id);
}