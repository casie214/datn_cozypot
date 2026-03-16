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

    // 1. Lấy tất cả hóa đơn
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, " +
            "hd.tongTienChuaGiam, hd.soTienDaGiam, hd.tongTienThanhToan, hd.tienCoc, hd.tienHoanTra, " +
            "hd.trangThaiHoaDon, hd.thoiGianTao, pdb.hinhThucDat, pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung, " +
            "pgg.id, pgg.codeGiamGia, pdb.thoiGianNhanBan) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idPhieuDatBan pdb " +
            "LEFT JOIN hd.idPhieuGiamGia pgg")
    Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable);

    // 2. Tìm kiếm hóa đơn
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, " +
            "hd.tongTienChuaGiam, hd.soTienDaGiam, hd.tongTienThanhToan, hd.tienCoc, hd.tienHoanTra, " +
            "hd.trangThaiHoaDon, hd.thoiGianTao, pdb.hinhThucDat, pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung, " +
            "pgg.id, pgg.codeGiamGia, pdb.thoiGianNhanBan) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idPhieuDatBan pdb " +
            "LEFT JOIN hd.idPhieuGiamGia pgg " +
            "WHERE (:trangThai IS NULL OR hd.trangThaiHoaDon = :trangThai) " +
            "AND (CAST(:tuNgayTao AS timestamp) IS NULL OR hd.thoiGianTao >= :tuNgayTao) " +
            "AND (CAST(:denNgayTao AS timestamp) IS NULL OR hd.thoiGianTao <= :denNgayTao) " +
            "AND (CAST(:tuNgayDat AS timestamp) IS NULL OR pdb.thoiGianDat >= :tuNgayDat) " +
            "AND (CAST(:denNgayDat AS timestamp) IS NULL OR pdb.thoiGianDat <= :denNgayDat) " +
            "AND (:keyword IS NULL OR :keyword = '' " +
            "     OR LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(kh.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(kh.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY hd.thoiGianTao ASC")
    Page<HoaDonThanhToanResponse> searchHoaDon(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            @Param("tuNgayTao") Instant tuNgayTao,
            @Param("denNgayTao") Instant denNgayTao,
            @Param("tuNgayDat") LocalDateTime tuNgayDat,
            @Param("denNgayDat") LocalDateTime denNgayDat,
            Pageable pageable
    );

    // 3. Lấy 1 hóa đơn theo ID
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse(" +
            "hd.id, hd.maHoaDon, kh.tenKhachHang, kh.soDienThoai, " +
            "hd.tongTienChuaGiam, hd.soTienDaGiam, hd.tongTienThanhToan, hd.tienCoc, hd.tienHoanTra, " +
            "hd.trangThaiHoaDon, hd.thoiGianTao, pdb.hinhThucDat, pdb.thoiGianDat, pdb.soLuongKhach, hd.vatApDung, " +
            "pgg.id, pgg.codeGiamGia, pdb.thoiGianNhanBan) " +
            "FROM HoaDonThanhToan hd " +
            "LEFT JOIN hd.idKhachHang kh " +
            "LEFT JOIN hd.idPhieuDatBan pdb " +
            "LEFT JOIN hd.idPhieuGiamGia pgg " +
            "WHERE hd.id = :id")
    HoaDonThanhToanResponse getHoaDonById(@Param("id") Integer id);

    // 4. Tìm mảng tên bàn để bổ sung vào DTO ở Service
    @Query("SELECT b.maBan FROM HoaDonThanhToan h " +
            "JOIN h.idPhieuDatBan p JOIN p.dsBanAn link JOIN link.banAn b " +
            "WHERE h.id = :idHoaDon")
    List<String> findMaBansByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

    // 5. Các hàm lấy thực thể (Entity) để xử lý logic nội bộ
    @Query("SELECT DISTINCT h FROM HoaDonThanhToan h JOIN h.idPhieuDatBan pdb JOIN pdb.dsBanAn link JOIN link.banAn b WHERE b.id = :idBanAn AND h.trangThaiHoaDon IN (4)")
    Optional<HoaDonThanhToan> findActiveBillByBanAn(@Param("idBanAn") int idBanAn);

    @Query("SELECT DISTINCT h FROM HoaDonThanhToan h JOIN h.idPhieuDatBan pdb JOIN pdb.dsBanAn link JOIN link.banAn b WHERE b.id = :idBanAn AND h.trangThaiHoaDon IN (1, 2) ORDER BY h.id DESC")
    List<HoaDonThanhToan> findActiveBills(@Param("idBanAn") Integer idBanAn);

    List<HoaDonThanhToan> findAllByTrangThaiHoaDonAndThoiGianTaoBefore(Integer trangThai, Instant limitTime);

    @Query("""
    SELECT new com.example.datn_cozypot_spring_boot.dto.thongKe.KenhDatResponse(
        CASE WHEN pdb.hinhThucDat = 1 THEN 'Online' WHEN pdb.hinhThucDat = 2 THEN 'Offline' ELSE 'Khác' END,
        COUNT(h)
    ) FROM HoaDonThanhToan h LEFT JOIN h.idPhieuDatBan pdb WHERE pdb.hinhThucDat IS NOT NULL GROUP BY pdb.hinhThucDat
    """)
    List<KenhDatResponse> thongKeKenhDat();

    HoaDonThanhToan findByIdPhieuDatBan_Id(Integer id);

    @Query("SELECT h FROM HoaDonThanhToan h JOIN h.idPhieuDatBan p WHERE p.maDatBan = :maPhieu")
    Optional<HoaDonThanhToan> findByMaPhieuDatBan(@Param("maPhieu") String maPhieu);

    List<HoaDonThanhToan> findByIdKhachHang_IdOrderByThoiGianTaoDesc(Integer idKhachHang);
}