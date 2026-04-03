package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietKhuyenMaiMon;
import com.example.datn_cozypot_spring_boot.entity.ChiTietKhuyenMaiMonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ChiTietKhuyenMaiMonRepository extends JpaRepository<ChiTietKhuyenMaiMon, ChiTietKhuyenMaiMonId> {

    @Query("SELECT ct.dotKhuyenMai.phanTramGiam FROM ChiTietKhuyenMaiMon ct " +
            "WHERE ct.danhMucChiTiet.id = :idMon AND ct.dotKhuyenMai.trangThai = 1 " +
            "AND :now BETWEEN ct.dotKhuyenMai.ngayBatDau AND ct.dotKhuyenMai.ngayKetThuc")
    List<Integer> findActiveDiscount(@Param("idMon") Integer idMon, @Param("now") LocalDate now);
}