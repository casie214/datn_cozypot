package com.example.datn_cozypot_spring_boot.repository;
import org.springframework.data.jpa.repository.EntityGraph;


import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DotKhuyenMaiRepository extends JpaRepository<DotKhuyenMai, Integer> {

    @EntityGraph(attributePaths = {
            "monAnDiKems",
            "setLaus"
    })
    @Query("SELECT d FROM DotKhuyenMai d WHERE " +
            "(:ten IS NULL OR d.tenDotKhuyenMai LIKE %:ten% OR d.maDotKhuyenMai LIKE %:ten%) AND " +
            "(:trangThai IS NULL OR d.trangThai = :trangThai) AND " +
            "(:ngayBD IS NULL OR d.ngayBatDau >= :ngayBD) AND " +
            "(:ngayKT IS NULL OR d.ngayKetThuc <= :ngayKT)")
    Page<DotKhuyenMai> searchFilter(
            @Param("ten") String ten,
            @Param("trangThai") Integer trangThai,
            @Param("ngayBD") LocalDate ngayBD,
            @Param("ngayKT") LocalDate ngayKT,
            Pageable pageable
    );


    @Query("""
    select d from DotKhuyenMai d
    where d.trangThai = 1
    and d.ngayBatDau <= CURRENT_DATE
    and d.ngayKetThuc >= CURRENT_DATE
""")
    List<DotKhuyenMai> findDotDangHoatDong();

    @Query(value = """
SELECT km.*
FROM dot_khuyen_mai km
JOIN chi_tiet_khuyen_mai_mon ctm
    ON km.id_dot_khuyen_mai = ctm.id_dot_khuyen_mai
WHERE ctm.id_mon_an_di_kem = :productId
AND km.trang_thai = 1
AND (
    :startDate <= km.ngay_ket_thuc
    AND :endDate >= km.ngay_bat_dau
)
""", nativeQuery = true)
    List<DotKhuyenMai> checkOverlapByMonAn(
            @Param("productId") Integer productId,
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end
    );
    @Query(value = """
SELECT km.*
FROM dot_khuyen_mai km
JOIN chi_tiet_khuyen_mai_set cts
    ON km.id_dot_khuyen_mai = cts.id_dot_khuyen_mai
WHERE cts.id_set_lau = :setId
AND km.trang_thai = 1
AND (
    :startDate <= km.ngay_ket_thuc
    AND :endDate >= km.ngay_bat_dau
)
""", nativeQuery = true)
    List<DotKhuyenMai> checkOverlapBySet(
            @Param("setId") Integer setId,
            @Param("startDate") LocalDate start,
            @Param("endDate") LocalDate end
    );






}
