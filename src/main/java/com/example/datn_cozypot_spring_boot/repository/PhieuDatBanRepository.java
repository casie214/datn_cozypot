package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuDatBanRepository extends JpaRepository<PhieuDatBan, Integer> {

    @Query("""
        SELECT p FROM PhieuDatBan p
        LEFT JOIN p.idKhachHang kh
        WHERE (:soDienThoai IS NULL OR (kh IS NOT NULL AND kh.soDienThoai LIKE %:soDienThoai%))
        AND (:trangThai IS NULL OR p.trangThai = :trangThai)
        AND (:start IS NULL OR (p.thoiGianDat >= :start AND p.thoiGianDat < :end))
        ORDER BY CASE WHEN p.trangThai IN (2,5) THEN 1 ELSE 0 END, p.thoiGianDat DESC
    """)
    Page<PhieuDatBan> search(
            @Param("soDienThoai") String soDienThoai,
            @Param("trangThai") Integer trangThai,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );

    // Chức năng: Tìm phiếu chờ trong tương lai
    @Query("SELECT p FROM PhieuDatBan p WHERE p.trangThai = 1 AND p.thoiGianDat >= :thoiGianTraCuu ORDER BY p.thoiGianDat ASC")
    List<PhieuDatBan> findWaitingListFuture(@Param("thoiGianTraCuu") LocalDateTime thoiGianTraCuu);

    // Chức năng: Tìm phiếu chờ trong ngày hôm nay
    @Query("SELECT p FROM PhieuDatBan p WHERE p.trangThai = 1 AND p.thoiGianDat >= :thoiGianBatDau AND p.thoiGianDat <= :thoiGianKetThuc ORDER BY p.thoiGianDat ASC")
    List<PhieuDatBan> findWaitingListToday(@Param("thoiGianBatDau") LocalDateTime thoiGianBatDau, @Param("thoiGianKetThuc") LocalDateTime thoiGianKetThuc);

    @Modifying
    @Transactional
    @Query("UPDATE PhieuDatBan p SET p.trangThai = 5 WHERE p.trangThai = 0 AND p.thoiGianDat < CURRENT_DATE")
    int updateChoXacNhanQuaHan();

    @Modifying
    @Transactional
    @Query(value = "UPDATE phieu_dat_ban SET trang_thai = 2 WHERE trang_thai = 1 AND DATEADD(MINUTE, 10, thoi_gian_dat) < GETDATE()", nativeQuery = true)
    int updateDaXacNhanQuaGio();

    @Modifying
    @Transactional
    @Query("UPDATE PhieuDatBan p SET p.trangThai = :trangThai WHERE p.id = :id")
    void updateTrangThai(@Param("id") Integer id, @Param("trangThai") Integer trangThai);

    // 🚨 ĐÃ CẬP NHẬT: Đi xuyên qua dsBanAn để lấy bàn
    @Query("""
        SELECT DISTINCT p
        FROM PhieuDatBan p
        JOIN p.dsBanAn link
        JOIN link.banAn b
        WHERE p.thoiGianDat >= :start
          AND p.thoiGianDat < :end
    """)
    List<PhieuDatBan> findPhieuTrongNgay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 🚨 ĐÃ CẬP NHẬT: Lấy phiếu đang hoạt động tại một bàn cụ thể (Sử dụng cho App/POS)
    @Query("""
        SELECT DISTINCT p 
        FROM PhieuDatBan p 
        JOIN p.dsBanAn link 
        WHERE link.banAn.id = :idBanAn 
          AND p.trangThai IN (2, 3) 
        ORDER BY p.id DESC
    """)
    List<PhieuDatBan> findActivePhieuByBanAn(@Param("idBanAn") Integer idBanAn);

    @Query("SELECT p FROM PhieuDatBan p WHERE p.thoiGianDat >= :startOfDay AND p.thoiGianDat <= :endOfDay AND p.trangThai IN (1, 2, 3)")
    List<PhieuDatBan> findPhieuDatBanTrongNgay(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    // 🚨 ĐÃ CẬP NHẬT: Kiểm tra trùng lịch đặt bàn (Sử dụng EXISTS qua dsBanAn)
    @Query("""
        SELECT COUNT(p) > 0
        FROM PhieuDatBan p
        WHERE p.trangThai != 3
          AND p.thoiGianDat BETWEEN :start AND :end
          AND EXISTS (
              SELECT 1 FROM PhieuDatBanBanAn pb
              WHERE pb.phieuDatBan = p
                AND pb.banAn = :ban
          )
    """)
    boolean existsByTimeRange(
            @Param("ban") BanAn ban,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<PhieuDatBan> findAllByTrangThaiAndThoiGianDatBefore(int i, LocalDateTime limitTime);

    // 🚨 ĐÃ CẬP NHẬT: Query Method theo chuẩn đặt tên mới của dsBanAn -> banAn -> id
    List<PhieuDatBan> findByDsBanAn_BanAn_IdAndTrangThaiInOrderByThoiGianDatAsc(Integer idBanAn, List<Integer> listTrangThai);

}