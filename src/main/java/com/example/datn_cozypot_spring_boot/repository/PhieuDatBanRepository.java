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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PhieuDatBanRepository extends JpaRepository<PhieuDatBan,Integer> {
    @Query("""
    SELECT pdb
    FROM PhieuDatBan pdb
    LEFT JOIN pdb.idKhachHang kh
    LEFT JOIN pdb.idBanAn ba
    LEFT JOIN ba.idKhuVuc kv
    WHERE
        (:soDienThoai IS NULL OR kh.soDienThoai LIKE CONCAT('%', :soDienThoai, '%'))
    AND (:trangThai IS NULL OR pdb.trangThai = :trangThai)
    AND (
        :ngayDat IS NULL
        OR CAST(pdb.thoiGianDat AS date) = CAST(:ngayDat AS date)
    )
""")
    Page<PhieuDatBan> search(
            @Param("soDienThoai") String soDienThoai,
            @Param("trangThai") Integer trangThai,
            @Param("ngayDat") Instant ngayDat,
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
    @Query("""
        SELECT p FROM PhieuDatBan p
        WHERE p.trangThai = 1
        AND p.thoiGianDat >= CURRENT_TIMESTAMP
        ORDER BY p.thoiGianDat ASC
    """)
    List<PhieuDatBan> findWaitingListFuture();
}
//        SELECT new com.example.datn_cozypot_spring_boot.dto.response.DatBanListResponse(p)
//        FROM PhieuDatBan p
//        WHERE p.trangThai = 1
//        AND CAST(p.thoiGianDat AS date) = CAST(CURRENT_DATE AS date)
//        ORDER BY p.thoiGianDat ASC