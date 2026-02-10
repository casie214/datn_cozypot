package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeNgayDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeThanhToanDTO;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeTongDTO;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface ThongKeRepository
        extends JpaRepository<HoaDonThanhToan, Long> {

    // ================= TỔNG DASHBOARD =================
    @Query(value = """
    SELECT
        COUNT(*) AS tongDon,
        SUM(hd.tong_tien_thanh_toan) AS tongDoanhThu,
        SUM(hd.so_tien_da_giam) AS tongGiam,
        AVG(hd.tong_tien_thanh_toan) AS doanhThuTrungBinh
    FROM hoa_don_thanh_toan hd
    WHERE hd.trang_thai_hoa_don = 2
    AND CAST(hd.thoi_gian_thanh_toan AS DATE)
        BETWEEN :from AND :to
    """, nativeQuery = true)
    ThongKeTongDTO thongKeTong(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );


    // ================= ONLINE / OFFLINE =================
    @Query(value = """
    SELECT
        CASE
            WHEN pt.ten_phuong_thuc = N'Tiền mặt'
            THEN N'Offline'
            ELSE N'Online'
        END AS hinhThuc,

        SUM(ls.so_tien_thanh_toan) AS tongTien

    FROM lich_su_thanh_toan ls
    JOIN phuong_thuc_thanh_toan pt
        ON ls.id_phuong_thuc_thanh_toan = pt.id_phuong_thuc
    JOIN hoa_don_thanh_toan hd
        ON ls.id_hoa_don = hd.id_hoa_don

    WHERE hd.trang_thai_hoa_don = 2
    AND CAST(ls.ngay_thanh_toan AS DATE)
        BETWEEN :from AND :to

    GROUP BY
        CASE
            WHEN pt.ten_phuong_thuc = N'Tiền mặt'
            THEN N'Offline'
            ELSE N'Online'
        END
    """, nativeQuery = true)
    List<ThongKeThanhToanDTO> thongKeThanhToan(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );


    // ================= THEO NGÀY =================
    @Query(value = """
    SELECT
        CAST(hd.thoi_gian_thanh_toan AS DATE) AS ngay,

        COUNT(*) AS soDon,

        SUM(hd.tong_tien_thanh_toan) AS doanhThu,

        SUM(hd.so_tien_da_giam) AS daGiam

    FROM hoa_don_thanh_toan hd

    WHERE hd.trang_thai_hoa_don = 2
    AND CAST(hd.thoi_gian_thanh_toan AS DATE)
        BETWEEN :from AND :to

    GROUP BY CAST(hd.thoi_gian_thanh_toan AS DATE)

    ORDER BY ngay DESC
    """, nativeQuery = true)
    List<ThongKeNgayDTO> thongKeTheoNgay(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
