package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse;
import com.example.datn_cozypot_spring_boot.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuThanhToanRepo extends JpaRepository<LichSuThanhToan, Integer> {
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse(" +
            "lstt.maGiaoDich, " +
            "lstt.tenPhuongThuc, " +
            "lstt.soTienThanhToan, " +
            "lstt.ngayThanhToan, " +
            "lstt.trangThai) " +
            "FROM LichSuThanhToan lstt " +
            "WHERE lstt.idHoaDon.id = :idHoaDon " +
            "ORDER BY lstt.ngayThanhToan DESC")
    List<LichSuThanhToanResponse> getLichSuByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);
}
