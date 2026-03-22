package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietKhuyenMaiSet;
import com.example.datn_cozypot_spring_boot.entity.ChiTietKhuyenMaiSetId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ChiTietKhuyenMaiSetRepository extends JpaRepository<ChiTietKhuyenMaiSet, ChiTietKhuyenMaiSetId> {

    @Query("SELECT ct.dotKhuyenMai.phanTramGiam FROM ChiTietKhuyenMaiSet ct " +
            "WHERE ct.setLau.id = :idSet AND ct.dotKhuyenMai.trangThai = 1 " +
            "AND :now BETWEEN ct.dotKhuyenMai.ngayBatDau AND ct.dotKhuyenMai.ngayKetThuc")
    Integer findActiveDiscount(@Param("idSet") Integer idSet, @Param("now") LocalDate now);

}
