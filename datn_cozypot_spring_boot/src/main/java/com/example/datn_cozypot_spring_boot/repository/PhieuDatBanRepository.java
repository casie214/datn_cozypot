package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.response.DatBanListResponse;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PhieuDatBanRepository extends JpaRepository<PhieuDatBan,Integer> {
//     @Query("""
//     SELECT pdb
//     FROM PhieuDatBan pdb
//     LEFT JOIN pdb.idKhachHang kh
//     WHERE
//         (:soDienThoai IS NULL
//             OR kh.soDienThoai LIKE CONCAT('%', :soDienThoai, '%'))
//     AND (:trangThai IS NULL
//             OR pdb.trangThai = :trangThai)
//     AND (
//         :ngayDat IS NULL
//         OR pdb.thoiGianDat >= :ngayDat
//     )
// """)
//     Page<PhieuDatBan> search(
//             @Param("soDienThoai") String soDienThoai,
//             @Param("trangThai") Integer trangThai,
//             @Param("ngayDat") LocalDateTime ngayDat,
//             Pageable pageable
//     );

@Query("""
SELECT p FROM PhieuDatBan p
LEFT JOIN p.idKhachHang kh
WHERE (:soDienThoai IS NULL OR (kh IS NOT NULL AND kh.soDienThoai LIKE %:soDienThoai%))
AND (:trangThai IS NULL OR p.trangThai = :trangThai)
AND (
  :start IS NULL OR 
  (p.thoiGianDat >= :start AND p.thoiGianDat < :end)
)
""")
Page<PhieuDatBan> search(
    @Param("soDienThoai") String soDienThoai,
    @Param("trangThai") Integer trangThai,
    @Param("start") LocalDateTime start,
    @Param("end") LocalDateTime end,
    Pageable pageable
);



    @Modifying
    @Transactional
    @Query("""
        UPDATE PhieuDatBan p
        SET p.idBanAn = :banAn
        WHERE p.id = :idPhieu
    """)
    void updateBanChoPhieu(
            @Param("idPhieu") Integer idPhieu,
            @Param("banAn") BanAn banAn
    );


    @Modifying
    @Transactional
    @Query("""
        UPDATE PhieuDatBan p
        SET p.idBanAn = :banAn
        WHERE p.id = :idPhieu
    """)
    void updateCheckIn(
            @Param("idPhieu") Integer idPhieu,
            @Param("banAn") BanAn banAn
    );


    // Chức năng 1: Hiển thị tất cả phiếu trạng thái 1 của hôm nay
//    @Query("""
//        SELECT p FROM PhieuDatBan p
//        WHERE p.trangThai = 1
//        AND p.thoiGianDat >= CURRENT_TIMESTAMP
//        ORDER BY p.thoiGianDat ASC
//    """)
//    List<PhieuDatBan> findWaitingListFuture();

    @Query(value = """
    SELECT * FROM phieu_dat_ban p
    WHERE p.trang_thai = 1
    AND p.thoi_gian_dat >= DATEADD(MINUTE, -10, GETDATE())
    ORDER BY p.thoi_gian_dat ASC
""", nativeQuery = true)
    List<PhieuDatBan> findWaitingListFuture();


    @Modifying
    @Transactional
    @Query("""
        UPDATE PhieuDatBan p
        SET p.trangThai = 5
        WHERE 
            p.trangThai = 0
            AND p.thoiGianDat < CURRENT_DATE
    """)
    int updateChoXacNhanQuaHan();


    /* RULE 2:
       Đã xác nhận (1) + quá giờ 10 phút → Đã hủy (2)
    */
    @Modifying
    @Transactional
    @Query(
            value = """
        UPDATE phieu_dat_ban
        SET trang_thai = 2
        WHERE trang_thai = 1
          AND DATEADD(MINUTE, 10, thoi_gian_dat) < GETDATE()
    """,
            nativeQuery = true
    )
    int updateDaXacNhanQuaGio();


    @Modifying
    @Query("""
    UPDATE PhieuDatBan p
    SET p.trangThai = :trangThai
    WHERE p.id = :id
""")
    void updateTrangThai(
            @Param("id") Integer id,
            @Param("trangThai") Integer trangThai
    );


    @Query("""
    SELECT p
    FROM PhieuDatBan p
    WHERE p.thoiGianDat >= :start
      AND p.thoiGianDat < :end
      AND p.idBanAn IS NOT NULL
""")
    List<PhieuDatBan> findPhieuTrongNgay(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );



}
