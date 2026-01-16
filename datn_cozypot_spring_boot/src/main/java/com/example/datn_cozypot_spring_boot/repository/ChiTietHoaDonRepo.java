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
            // --- XỬ LÝ TÊN MÓN ---
            "CASE WHEN cthd.idSetLau IS NOT NULL THEN s.tenSetLau " +
            "     ELSE CONCAT(m.tenMonAn, ' - ', ctma.tenChiTietMonAn) END, " +
            "cthd.soLuong, " +
            "cthd.donGiaTaiThoiDiemBan, " +
            "cthd.thanhTien, " +
            "cthd.ghiChuMon, " +
            // --- XỬ LÝ TRẠNG THÁI TEXT ---
            "CASE WHEN cthd.trangThaiMon = 1 THEN 'Chưa lên' " +
            "     WHEN cthd.trangThaiMon = 2 THEN 'Đã lên' " +
            "     ELSE 'Khác' END, " +
            "cthd.trangThaiMon) " +
            "FROM ChiTietHoaDon cthd " +
            "LEFT JOIN cthd.idSetLau s " +
            "LEFT JOIN cthd.idChiTietMonAn ctma " +
            "LEFT JOIN ctma.idMonAnDiKem m " +
            "WHERE cthd.idHoaDon.id = :idHoaDon") // Sửa: So khớp ID số của hóa đơn
    List<ChiTietHoaDonResponse> findChiTietByHoaDonId(@Param("idHoaDon") Integer idHoaDon);
}