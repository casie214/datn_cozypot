package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotKhuyenMaiRepo extends JpaRepository<DotKhuyenMai, Integer> {
    @Query("SELECT d FROM DotKhuyenMai d WHERE " +
            "(:ten IS NULL OR d.tenDotKhuyenMai LIKE %:ten%) AND " +
            "(:trangThai IS NULL OR d.trangThai = :trangThai) AND " +
            "(:loaiKM IS NULL OR d.loaiKhuyenMai = :loaiKM)")
    List<DotKhuyenMai> searchFilter(@Param("ten") String ten,
                                    @Param("trangThai") Integer trangThai,
                                    @Param("loaiKM") Integer loaiKM);
}
