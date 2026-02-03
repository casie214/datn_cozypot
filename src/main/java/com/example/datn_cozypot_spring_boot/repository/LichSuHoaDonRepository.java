package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {
    @Query("SELECT l FROM LichSuHoaDon l WHERE l.idHoaDon.id = :idHoaDon ORDER BY l.thoiGianThucHien DESC")
    List<LichSuHoaDon> findByHoaDonId(@Param("idHoaDon") Integer idHoaDon);
}
