package com.example.datn_cozypot_spring_boot.repository;


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

    @Query("SELECT d FROM DotKhuyenMai d WHERE " +
            "(:ten IS NULL OR d.tenDotKhuyenMai LIKE %:ten% OR d.maDotKhuyenMai LIKE %:ten%) AND " +
            "(:trangThai IS NULL OR d.trangThai = :trangThai) AND " +
            "(:ngayBD IS NULL OR d.ngayBatDau >= :ngayBD) AND " +
            "(:ngayKT IS NULL OR d.ngayKetThuc <= :ngayKT)")
    Page<DotKhuyenMai> searchFilter(@Param("ten") String ten,
                                    @Param("trangThai") Integer trangThai,
                                    @Param("ngayBD") LocalDate ngayBD,
                                    @Param("ngayKT") LocalDate ngayKT,
                                    Pageable pageable);

    @Query("""
    select d from DotKhuyenMai d
    where d.trangThai = 1
    and d.ngayBatDau <= CURRENT_DATE
    and d.ngayKetThuc >= CURRENT_DATE
""")
    List<DotKhuyenMai> findDotDangHoatDong();


}
