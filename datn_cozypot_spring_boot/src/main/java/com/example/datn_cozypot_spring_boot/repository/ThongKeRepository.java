package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface ThongKeRepository extends JpaRepository<HoaDonThanhToan, Integer> {

    @Query(value = """
        SELECT 
            -- DOANH THU (Lọc theo trạng thái 7: COMPLETED)
         ISNULL(SUM(CASE\s
         WHEN CAST(thoi_gian_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)
         AND trang_thai_hoa_don = 7
         THEN (tong_tien_chua_giam - so_tien_da_giam)
         ELSE 0 END),0) AS doanhThuHomNay,
                ISNULL(SUM(CASE WHEN thoi_gian_thanh_toan >= DATEADD(DAY, -7, GETDATE()) AND trang_thai_hoa_don = 7 THEN (tong_tien_chua_giam - so_tien_da_giam) ELSE 0 END), 0) AS doanhThuTuanNay,
            ISNULL(SUM(CASE WHEN MONTH(thoi_gian_thanh_toan) = MONTH(GETDATE()) AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE()) AND trang_thai_hoa_don = 7 THEN (tong_tien_chua_giam - so_tien_da_giam) ELSE 0 END), 0) AS doanhThuThangNay,
            ISNULL(SUM(CASE WHEN YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE()) AND trang_thai_hoa_don = 7 THEN (tong_tien_chua_giam - so_tien_da_giam) ELSE 0 END), 0) AS doanhThuNamNay,
            
            -- SỐ LƯỢNG HÓA ĐƠN THÀNH CÔNG
            COUNT(CASE WHEN CAST(thoi_gian_thanh_toan AS DATE) = CAST(GETDATE() AS DATE) AND trang_thai_hoa_don = 7 THEN 1 END) AS soDonHomNay,
            COUNT(CASE WHEN thoi_gian_thanh_toan >= DATEADD(DAY, -7, GETDATE()) AND trang_thai_hoa_don = 7 THEN 1 END) AS soDonTuanNay,
            COUNT(CASE WHEN MONTH(thoi_gian_thanh_toan) = MONTH(GETDATE()) AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE()) AND trang_thai_hoa_don = 7 THEN 1 END) AS soDonThangNay,
            COUNT(CASE WHEN YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE()) AND trang_thai_hoa_don = 7 THEN 1 END) AS soDonNamNay,

            -- TỔNG HÓA ĐƠN THEO BỘ LỌC
            COUNT(CASE WHEN trang_thai_hoa_don = 7 AND (
                (:loai = N'Hôm nay' AND CAST(thoi_gian_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)) OR
                (:loai = N'Tuần này' AND thoi_gian_thanh_toan >= DATEADD(DAY, -7, GETDATE())) OR
                (:loai = N'Tháng này' AND MONTH(thoi_gian_thanh_toan) = MONTH(GETDATE()) AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE())) OR
            (:loai = N'Năm nay' AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE()))
                                        OR
                                        (:loai = N'Tùy chỉnh' AND CAST(thoi_gian_thanh_toan AS DATE) BETWEEN :tuNgay AND :denNgay)            ) THEN 1 END) AS tongHoaDon,

            -- GIÁ TRỊ TRUNG BÌNH ĐƠN
            ISNULL(AVG(CASE WHEN trang_thai_hoa_don = 7 AND (
                (:loai = N'Hôm nay' AND CAST(thoi_gian_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)) OR
                (:loai = N'Tuần này' AND thoi_gian_thanh_toan >= DATEADD(DAY, -7, GETDATE())) OR
                (:loai = N'Tháng này' AND MONTH(thoi_gian_thanh_toan) = MONTH(GETDATE()) AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE())) OR
            (:loai = N'Năm nay' AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE()))
                                                                   OR
                                                                   (:loai = N'Tùy chỉnh' AND CAST(thoi_gian_thanh_toan AS DATE) BETWEEN :tuNgay AND :denNgay)            ) THEN (tong_tien_chua_giam - so_tien_da_giam) END), 0) AS giaTriTrungBinhDon,

            -- TỔNG TIỀN CỌC (Lấy từ các đơn đã cọc hoặc đã xong: 2, 3, 4, 5, 6, 7)
            ISNULL(SUM(CASE WHEN trang_thai_hoa_don IN (2,3,4,5,6,7) AND (
                (:loai = N'Hôm nay' AND CAST(thoi_gian_tao AS DATE) = CAST(GETDATE() AS DATE)) OR
                (:loai = N'Tuần này' AND thoi_gian_tao >= DATEADD(DAY, -7, GETDATE())) OR
                (:loai = N'Tháng này' AND MONTH(thoi_gian_tao) = MONTH(GETDATE()) AND YEAR(thoi_gian_tao) = YEAR(GETDATE())) OR
                (:loai = N'Năm nay' AND YEAR(thoi_gian_tao) = YEAR(GETDATE()))
            ) THEN tien_coc ELSE 0 END), 0) AS tongTienCoc,

            -- TỔNG GIẢM GIÁ (Chỉ tính trên đơn thành công 7)
            ISNULL(SUM(CASE WHEN trang_thai_hoa_don = 7 AND (
                (:loai = N'Hôm nay' AND CAST(thoi_gian_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)) OR
                (:loai = N'Tuần này' AND thoi_gian_thanh_toan >= DATEADD(DAY, -7, GETDATE())) OR
                (:loai = N'Tháng này' AND MONTH(thoi_gian_thanh_toan) = MONTH(GETDATE()) AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE())) OR
                (:loai = N'Năm nay' AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE()))
            ) THEN so_tien_da_giam ELSE 0 END), 0) AS tongGiamGia,

            -- TỔNG HÓA ĐƠN HỦY (Theo SQL của bạn là trạng thái 8)
            COUNT(CASE WHEN trang_thai_hoa_don = 8 AND (
                (:loai = N'Hôm nay' AND CAST(thoi_gian_tao AS DATE) = CAST(GETDATE() AS DATE)) OR
                (:loai = N'Tuần này' AND thoi_gian_tao >= DATEADD(DAY, -7, GETDATE())) OR
                (:loai = N'Tháng này' AND MONTH(thoi_gian_tao) = MONTH(GETDATE()) AND YEAR(thoi_gian_tao) = YEAR(GETDATE())) OR
                (:loai = N'Năm nay' AND YEAR(thoi_gian_tao) = YEAR(GETDATE()))
            ) THEN 1 END) AS tongHoaDonHuy,
            
                    -- SỐ BÀN ĐÃ ĐẶT (Trạng thái 1: chờ xác nhận, 2: đã cọc)
                    COUNT(CASE WHEN trang_thai_hoa_don IN (1,2) AND (
                        (:loai = N'Hôm nay' AND CAST(thoi_gian_tao AS DATE) = CAST(GETDATE() AS DATE)) OR
                        (:loai = N'Tuần này' AND thoi_gian_tao >= DATEADD(DAY, -7, GETDATE())) OR
                        (:loai = N'Tháng này' AND MONTH(thoi_gian_tao) = MONTH(GETDATE()) AND YEAR(thoi_gian_tao) = YEAR(GETDATE())) OR
                        (:loai = N'Năm nay' AND YEAR(thoi_gian_tao) = YEAR(GETDATE())) OR
                        (:loai = N'Tùy chỉnh' AND CAST(thoi_gian_tao AS DATE) BETWEEN :tuNgay AND :denNgay)
                    ) THEN 1 END) AS tongBanDaDat,
                    
                    -- DOANH THU DỰ KIẾN (Đơn chưa hoàn thành nhưng có tiền)
-- DOANH THU DỰ KIẾN (1 → 6 và món active 1,2)
           ISNULL(SUM(CASE\s
                                          WHEN h.trang_thai_hoa_don BETWEEN 0 AND 6
                                          AND (
                                              (:loai = N'Hôm nay' AND CAST(h.thoi_gian_tao AS DATE) = CAST(GETDATE() AS DATE)) OR
                                              (:loai = N'Tuần này' AND h.thoi_gian_tao >= DATEADD(DAY, -7, GETDATE())) OR
                                              (:loai = N'Tháng này' AND MONTH(h.thoi_gian_tao) = MONTH(GETDATE()) AND YEAR(h.thoi_gian_tao) = YEAR(GETDATE())) OR
                                              (:loai = N'Năm nay' AND YEAR(h.thoi_gian_tao) = YEAR(GETDATE())) OR
                                              (:loai = N'Tùy chỉnh' AND CAST(h.thoi_gian_tao AS DATE) BETWEEN :tuNgay AND :denNgay)
                                          )
                                          THEN (h.tong_tien_chua_giam - h.so_tien_da_giam)
                                          ELSE 0 END),0) AS doanhThuDuKien,
            ISNULL(SUM(CASE
                                          WHEN h.trang_thai_hoa_don BETWEEN 0 AND 7
                                          AND ls.id_phuong_thuc_thanh_toan = 1
                                          AND (
                                              (:loai = N'Hôm nay' AND CAST(ls.ngay_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)) OR
                                              (:loai = N'Tuần này' AND ls.ngay_thanh_toan >= DATEADD(DAY, -7, GETDATE())) OR
                                              (:loai = N'Tháng này' AND MONTH(ls.ngay_thanh_toan) = MONTH(GETDATE()) AND YEAR(ls.ngay_thanh_toan) = YEAR(GETDATE())) OR
                                              (:loai = N'Năm nay' AND YEAR(ls.ngay_thanh_toan) = YEAR(GETDATE())) OR
                                              (:loai = N'Tùy chỉnh' AND CAST(ls.ngay_thanh_toan AS DATE) BETWEEN :tuNgay AND :denNgay)
                                          )
                                          THEN ls.so_tien_thanh_toan
                                          ELSE 0
                                      END),0) AS doanhThuTienMat,
            ISNULL(SUM(CASE
                                                                                       WHEN h.trang_thai_hoa_don BETWEEN 0 AND 7
                                                                                       AND ls.id_phuong_thuc_thanh_toan = 2
                                                                                       AND (
                                                                                           (:loai = N'Hôm nay' AND CAST(ls.ngay_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)) OR
                                                                                           (:loai = N'Tuần này' AND ls.ngay_thanh_toan >= DATEADD(DAY, -7, GETDATE())) OR
                                                                                           (:loai = N'Tháng này' AND MONTH(ls.ngay_thanh_toan) = MONTH(GETDATE()) AND YEAR(ls.ngay_thanh_toan) = YEAR(GETDATE())) OR
                                                                                           (:loai = N'Năm nay' AND YEAR(ls.ngay_thanh_toan) = YEAR(GETDATE())) OR
                                                                                           (:loai = N'Tùy chỉnh' AND CAST(ls.ngay_thanh_toan AS DATE) BETWEEN :tuNgay AND :denNgay)
                                                                                       )
                                                                                       THEN ls.so_tien_thanh_toan
                                                                                       ELSE 0
                                                                                   END),0) AS doanhThuChuyenKhoan,

            -- DOANH THU THỰC NHẬN
            ISNULL(SUM(CASE WHEN trang_thai_hoa_don = 7 AND (
                                                                              (:loai = N'Hôm nay' AND CAST(thoi_gian_thanh_toan AS DATE) = CAST(GETDATE() AS DATE)) OR
                                                                              (:loai = N'Tuần này' AND thoi_gian_thanh_toan >= DATEADD(DAY, -7, GETDATE())) OR
                                                                              (:loai = N'Tháng này' AND MONTH(thoi_gian_thanh_toan) = MONTH(GETDATE()) AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE())) OR
                                                                              (:loai = N'Năm nay' AND YEAR(thoi_gian_thanh_toan) = YEAR(GETDATE())) OR
                                                                              (:loai = N'Tùy chỉnh' AND CAST(thoi_gian_thanh_toan AS DATE) BETWEEN :tuNgay AND :denNgay)
                                                                          )
                                                                          THEN ls.so_tien_thanh_toan
                                                                          ELSE 0 END), 0) AS doanhThuThucNhan
            FROM hoa_don_thanh_toan h
                                                            LEFT JOIN lich_su_thanh_toan ls
                                                                                                                    ON h.id_hoa_don = ls.id_hoa_don
                                                                                                                    AND ls.trang_thai = 1
                                                                                                                    AND ls.loai_giao_dich IN (1,2)        """, nativeQuery = true)
    Map<String, Object> layDuLieuThongKeChiTiet(
            @Param("loai") String loai,
            @Param("tuNgay") String tuNgay,
            @Param("denNgay") String denNgay
    );
    @Query(value = """
    SELECT 
        m.thang,
        ISNULL(SUM(h.tong_tien_chua_giam), 0) AS doanhThu
    FROM (
        SELECT 1 AS thang UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 
        UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 
        UNION SELECT 9 UNION SELECT 10 UNION SELECT 11 UNION SELECT 12
    ) m
    LEFT JOIN hoa_don_thanh_toan h ON MONTH(h.thoi_gian_thanh_toan) = m.thang 
        AND YEAR(h.thoi_gian_thanh_toan) = YEAR(GETDATE()) 
        AND h.trang_thai_hoa_don = 7
    GROUP BY m.thang
    ORDER BY m.thang
    """, nativeQuery = true)
    List<Map<String, Object>> layDoanhThu12Thang();

    @Query(value = """
    SELECT TOP 5 
        s.hinh_anh AS anh, 
        s.ten_set_lau AS tenSet, 
        s.gia_ban AS gia, 
        SUM(ct.so_luong) AS soLuongBan
    FROM chi_tiet_hoa_don ct
    INNER JOIN set_lau s ON ct.id_set_lau = s.id_set_lau
    INNER JOIN hoa_don_thanh_toan h ON ct.id_hoa_don = h.id_hoa_don
    WHERE h.trang_thai_hoa_don = 7 
      AND ct.id_set_lau IS NOT NULL
    GROUP BY s.hinh_anh, s.ten_set_lau, s.gia_ban
    ORDER BY soLuongBan DESC
""", nativeQuery = true)
    List<Map<String, Object>> layTopSetLauBanChay();

    @Query(value = """
        SELECT ISNULL(SUM(tong_tien_chua_giam), 0) 
        FROM hoa_don_thanh_toan 
        WHERE trang_thai_hoa_don = 7 
          AND MONTH(thoi_gian_thanh_toan) = MONTH(DATEADD(MONTH, -1, GETDATE()))
          AND YEAR(thoi_gian_thanh_toan) = YEAR(DATEADD(MONTH, -1, GETDATE()))
    """, nativeQuery = true)
    BigDecimal layDoanhThuThangTruoc();

    @Query(value = """
    SELECT 
        COUNT(DISTINCT id_khach_hang) AS tongKhachHang,
        COUNT(*) AS khachMoi
    FROM (
        SELECT id_khach_hang
        FROM hoa_don_thanh_toan
        WHERE trang_thai_hoa_don = 7
        GROUP BY id_khach_hang
        HAVING COUNT(id_hoa_don) = 1
    ) x
""", nativeQuery = true)
    Map<String, Object> thongKeKhachHang();

    @Query(value = """
    SELECT 
        COUNT(*) AS khachQuayLai
    FROM (
        SELECT id_khach_hang
        FROM hoa_don_thanh_toan
        WHERE trang_thai_hoa_don = 7
        GROUP BY id_khach_hang
        HAVING COUNT(id_hoa_don) > 1
    ) x
""", nativeQuery = true)
    Long demKhachQuayLai();

    @Query(value = """
    SELECT 
        CASE 
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 10 AND 11 THEN '10-12h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 12 AND 13 THEN '12-14h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 14 AND 15 THEN '14-16h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 16 AND 17 THEN '16-18h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 18 AND 19 THEN '18-20h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 20 AND 21 THEN '20-22h'
        END AS khungGio,
        COUNT(*) AS soLuong
    FROM hoa_don_thanh_toan
    WHERE trang_thai_hoa_don = 7
    GROUP BY 
        CASE 
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 10 AND 11 THEN '10-12h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 12 AND 13 THEN '12-14h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 14 AND 15 THEN '14-16h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 16 AND 17 THEN '16-18h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 18 AND 19 THEN '18-20h'
            WHEN DATEPART(HOUR, thoi_gian_thanh_toan) BETWEEN 20 AND 21 THEN '20-22h'
        END
    ORDER BY khungGio
""", nativeQuery = true)
    List<Map<String, Object>> thongKeTheoKhungGio();

    @Query(value = """
    SELECT 
        trang_thai_hoa_don AS trangThai,
        COUNT(*) AS soLuong
    FROM hoa_don_thanh_toan
    GROUP BY trang_thai_hoa_don
    ORDER BY trang_thai_hoa_don
""", nativeQuery = true)
    List<Map<String, Object>> thongKeTrangThaiDonHang();

    @Query(value = """
    SELECT COUNT(*)
    FROM hoa_don_thanh_toan
    WHERE trang_thai_hoa_don IN (1, 2)
""", nativeQuery = true)
    Long demHoaDonChoVaDaCoc();

    @Query(value = """
    SELECT ISNULL(SUM(tong_tien_chua_giam),0)
    FROM hoa_don_thanh_toan
    WHERE trang_thai_hoa_don IN (1, 2)
""", nativeQuery = true)
    BigDecimal tongTienHoaDonChoVaDaCoc();
}