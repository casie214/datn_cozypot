package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse;
import com.example.datn_cozypot_spring_boot.entity.LichSuThanhToan;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.LichSuThanhToanDTO.LichSuThanhToanResponse(" +
            "lstt.maGiaoDich, " +
            "lstt.tenPhuongThuc, " +
            "lstt.soTienThanhToan, " +
            "lstt.ngayThanhToan, " +
            "lstt.trangThai, lstt.loaiGiaoDich, lstt.ghiChu) " +
            "FROM LichSuThanhToan lstt " +
            "WHERE lstt.HoaDon.id = :idHoaDon " +
            "ORDER BY lstt.ngayThanhToan DESC")
    List<LichSuThanhToanResponse> getLichSuByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

    @Modifying
    @Transactional
    @Query(value = "UPDATE lich_su_thanh_toan SET id_hoa_don = :idHoaDonChu WHERE id_hoa_don = :idHoaDonBiNuot", nativeQuery = true)
    void chuyenLichSuThanhToanSangHoaDonMoi(@Param("idHoaDonChu") Integer idHoaDonChu, @Param("idHoaDonBiNuot") Integer idHoaDonBiNuot);
}
