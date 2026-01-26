package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
            "(:keyword IS NULL OR p.codeGiamGia LIKE %:keyword% OR p.tenPhieuGiamGia LIKE %:keyword%) " +
            "AND (:doiTuong IS NULL OR p.doiTuong = :doiTuong) " +
            "AND (:loaiGiamGia IS NULL OR p.loaiGiamGia = :loaiGiamGia) " +
            "AND (:trangThai IS NULL OR p.trangThai = :trangThai) " +
            "AND (:ngayBatDau IS NULL OR p.ngayBatDau >= :ngayBatDau) " +
            "AND (:ngayKetThuc IS NULL OR p.ngayKetThuc <= :ngayKetThuc)")
    Page<PhieuGiamGia> searchVouchers(
            @Param("keyword") String keyword,
            @Param("doiTuong") Integer doiTuong,
            @Param("loaiGiamGia") Integer loaiGiamGia,
            @Param("trangThai") Integer trangThai,
            @Param("ngayBatDau") LocalDateTime ngayBatDau,
            @Param("ngayKetThuc") LocalDateTime ngayKetThuc,
            Pageable pageable
    );

    @Query("SELECT p FROM PhieuGiamGia p LEFT JOIN FETCH p.danhSachCaNhan WHERE p.id = :id")
    Optional<PhieuGiamGia> findDetailById(@Param("id") Integer id);
    boolean existsByCodeGiamGia(String codeGiamGia);
    boolean existsByTenPhieuGiamGia(String tenPhieuGiamGia);

    boolean existsByCodeGiamGiaAndIdNot(String codeGiamGia, Integer id);
    boolean existsByTenPhieuGiamGiaAndIdNot(String tenPhieuGiamGia, Integer id);


    // Tổng số
    @Query("SELECT COUNT(p) FROM PhieuGiamGia p")
    long countAll();

    // Đang hoạt động (chưa hết hạn + trạng thái = 1)
    @Query("""
        SELECT COUNT(p) 
        FROM PhieuGiamGia p 
        WHERE p.trangThai = 1 
        AND p.ngayKetThuc >= CURRENT_TIMESTAMP
    """)
    long countDangHoatDong();

    // Hết hạn
    @Query("""
        SELECT COUNT(p) 
        FROM PhieuGiamGia p 
        WHERE p.ngayKetThuc < CURRENT_TIMESTAMP
    """)
    long countHetHan();

    // Tắt
    @Query("""
        SELECT COUNT(p) 
        FROM PhieuGiamGia p 
        WHERE p.trangThai = 0
    """)
    long countDaTat();


}