package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.LichSuHoaDon;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {
    @Query("SELECT l FROM LichSuHoaDon l WHERE l.idHoaDon.id = :idHoaDon ORDER BY l.thoiGianThucHien DESC")
    List<LichSuHoaDon> findByHoaDonId(@Param("idHoaDon") Integer idHoaDon);

    List<LichSuHoaDon> findByIdHoaDon_Id(Integer idHoaDon);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "UPDATE lich_su_thanh_toan SET id_hoa_don = :idHoaDonChu WHERE id_hoa_don = :idHoaDonBiNuot", nativeQuery = true)
    void chuyenLichSuThanhToanSangHoaDonMoi(@Param("idHoaDonChu") Integer idHoaDonChu, @Param("idHoaDonBiNuot") Integer idHoaDonBiNuot);

    Optional<LichSuHoaDon> findFirstByIdHoaDon_IdAndTrangThaiMoiInOrderByThoiGianThucHienDesc(Integer idHoaDon, List<Integer> listTrangThai);
}
