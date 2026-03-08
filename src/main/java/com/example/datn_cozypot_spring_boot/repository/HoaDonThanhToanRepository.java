package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.thongKe.KenhDatResponse;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonThanhToanRepository extends JpaRepository<HoaDonThanhToan, Integer> {

    // 🚨 ĐÃ SỬA: pdb.banAns -> pdb.dsBanAn link JOIN link.banAn ban
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, " +
            "(SELECT MIN(ban.tenBan) FROM hd.idPhieuDatBan pdb JOIN pdb.dsBanAn link JOIN link.banAn ban), " +
            "hd.tongTienChuaGiam, hd.soTienDaGiam, hd.tongTienThanhToan, hd.tienCoc, hd.tienHoanTra, " +
            "hd.trangThaiHoaDon, hd.thoiGianTao, pdb.hinhThucDat, pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idPhieuDatBan pdb")
    Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable);

    // 🚨 ĐÃ SỬA: pdb.banAns -> pdb.dsBanAn link JOIN link.banAn ban
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, " +
            "(SELECT MIN(ban.tenBan) FROM hd.idPhieuDatBan p2 JOIN p2.dsBanAn link JOIN link.banAn ban), " +
            "hd.tongTienChuaGiam, hd.soTienDaGiam, hd.tongTienThanhToan, hd.tienCoc, hd.tienHoanTra, " +
            "hd.trangThaiHoaDon, hd.thoiGianTao, pdb.hinhThucDat, pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idPhieuDatBan pdb " +
            "WHERE (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai) " +
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
            @Param("tuNgay") Instant tuNgay,
            @Param("denNgay") Instant denNgay,
            Pageable pageable
    );

    // 🚨 ĐÃ SỬA TƯƠNG TỰ
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, " +
            "(SELECT MIN(ban.tenBan) FROM hd.idPhieuDatBan p2 JOIN p2.dsBanAn link JOIN link.banAn ban), " +
            "hd.tongTienChuaGiam, hd.soTienDaGiam, hd.tongTienThanhToan, hd.tienCoc, hd.tienHoanTra, " +
            "hd.trangThaiHoaDon, hd.thoiGianTao, pdb.hinhThucDat, pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idPhieuDatBan pdb " +
            "WHERE hd.id = :id")
    HoaDonThanhToanResponse getHoaDonById(@Param("id") Integer id);

    // 🚨 ĐÃ SỬA: JOIN pdb.dsBanAn link JOIN link.banAn b
    @Query("SELECT DISTINCT h FROM HoaDonThanhToan h JOIN h.idPhieuDatBan pdb JOIN pdb.dsBanAn link JOIN link.banAn b WHERE b.id = :idBanAn AND h.trangThaiHoaDon IN (4)")
    Optional<HoaDonThanhToan> findActiveBillByBanAn(@Param("idBanAn") int idBanAn);

    // 🚨 ĐÃ SỬA TƯƠNG TỰ
    @Query("SELECT DISTINCT h FROM HoaDonThanhToan h JOIN h.idPhieuDatBan pdb JOIN pdb.dsBanAn link JOIN link.banAn b WHERE b.id = :idBanAn AND h.trangThaiHoaDon IN (1, 2) ORDER BY h.id DESC")
    List<HoaDonThanhToan> findActiveBills(@Param("idBanAn") Integer idBanAn);

    List<HoaDonThanhToan> findAllByTrangThaiHoaDonAndThoiGianTaoBefore(Integer trangThai, Instant limitTime);

    @Query("""
    SELECT new com.example.datn_cozypot_spring_boot.dto.thongKe.KenhDatResponse(
        CASE 
            WHEN pdb.hinhThucDat = 1 THEN 'Online'
            WHEN pdb.hinhThucDat = 2 THEN 'Offline'
            ELSE 'Khác'
        END,
        COUNT(h)
    )
    FROM HoaDonThanhToan h
    LEFT JOIN h.idPhieuDatBan pdb
    WHERE pdb.hinhThucDat IS NOT NULL
    GROUP BY pdb.hinhThucDat
""")
    List<KenhDatResponse> thongKeKenhDat();

    List<HoaDonThanhToan> findByThoiGianTaoBetween(LocalDateTime start, LocalDateTime end);

    HoaDonThanhToan findByIdPhieuDatBan_Id(Integer id);

    Optional<HoaDonThanhToan> findByMaHoaDon(String idHoaDon);

    @Query("SELECT h FROM HoaDonThanhToan h JOIN h.idPhieuDatBan p WHERE p.maDatBan = :maPhieu")
    Optional<HoaDonThanhToan> findByMaPhieuDatBan(@Param("maPhieu") String maPhieu);

    List<HoaDonThanhToan> findByIdKhachHang_IdOrderByThoiGianTaoDesc(Integer idKhachHang);
}