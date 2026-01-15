package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGiamGiaRepo extends JpaRepository<PhieuGiamGia,Integer> {
    @Query("SELECT p FROM PhieuGiamGia p WHERE " +
            "(:ten IS NULL OR p.tenPhieuGiamGia LIKE %:ten%) AND " +
            "(:trangThai IS NULL OR p.trangThai = :trangThai)")
    List<PhieuGiamGia> searchFilter(@Param("ten") String ten,
                                    @Param("trangThai") Integer trangThai);
}
