package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietHoaDonRepo extends JpaRepository<ChiTietHoaDon, Integer> {

    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse(" +
            "cthd.id, " +
            "CASE WHEN cthd.idSetLau IS NOT NULL THEN s.tenSetLau " +
            "     ELSE CONCAT(m.tenMonAn, ' - ', ctma.tenChiTietMonAn) END, " +
            "cthd.soLuong, " +
            "cthd.donGiaTaiThoiDiemBan, " +
            "cthd.thanhTien, " +
            "cthd.ghiChuMon, " +
            "CASE WHEN cthd.trangThaiMon = 1 THEN 'Chưa lên' " +
            "     WHEN cthd.trangThaiMon = 2 THEN 'Đã lên' " +
            "     WHEN cthd.trangThaiMon = 0 THEN 'Đã huy' " +
            "     ELSE 'Khác' END, " +
            "cthd.trangThaiMon) " +
            "FROM ChiTietHoaDon cthd " +
            "LEFT JOIN cthd.idSetLau s " +
            "LEFT JOIN cthd.idChiTietMonAn ctma " +
            "LEFT JOIN ctma.idMonAnDiKem m " +
            "WHERE cthd.idHoaDon.id = :idHoaDon")
    List<ChiTietHoaDonResponse> findChiTietByHoaDonId(@Param("idHoaDon") Integer idHoaDon);

    @Query("SELECT c FROM ChiTietHoaDon c WHERE c.idHoaDon.id = :idHoaDon")
    List<ChiTietHoaDon> findByIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

    @Query("SELECT COUNT(c) > 0 FROM ChiTietHoaDon c WHERE c.idHoaDon.id = :idHoaDon AND c.trangThaiMon = :trangThai")
    boolean existsByIdHoaDonAndTrangThaiMon(@Param("idHoaDon") Integer idHoaDon, @Param("trangThai") Integer trangThai);

}