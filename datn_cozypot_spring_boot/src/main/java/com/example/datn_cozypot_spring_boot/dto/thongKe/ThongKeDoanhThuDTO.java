package com.example.datn_cozypot_spring_boot.dto.thongKe;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ThongKeDoanhThuDTO {
    // 4 chỉ số tổng quát
    private BigDecimal doanhThuHomNay;
    private BigDecimal doanhThuTuanNay;
    private BigDecimal doanhThuThangNay;
    private BigDecimal doanhThuNamNay;

    private Long soDonHomNay;
    private Long soDonTuanNay;
    private Long soDonThangNay;
    private Long soDonNamNay;

    // Chỉ số chi tiết theo bộ lọc (mặc định tháng hiện tại)
    private Long tongHoaDon;
    private BigDecimal giaTriTrungBinhDon;
    private BigDecimal tongTienCoc;
    private BigDecimal tongGiamGia;
    private Long tongHoaDonHuy;
    private BigDecimal doanhThuThucNhan;

    // Thêm vào class ThongKeDoanhThuDTO
    private List<BigDecimal> doanhThu12Thang;
    private List<Map<String, Object>> topSets; // Danh sách Top Set Lẩu bán chạy
    private Double tocDoTangTruong;           // % tăng trưởng (Ví dụ: 15.5)

    private Long tongKhachHang;
    private Long khachMoi;
    private Long khachQuayLai;

    private BigDecimal giaTriTrungBinhHoaDon;

    private Long soHoaDonChoVaDaCoc;
    private BigDecimal tongTienHoaDonChoVaDaCoc;

    private Long TongBanDaDat;
    private BigDecimal DoanhThuDuKien;
    private List<GioCaoDiemResponse> gioCaoDiem;

    private BigDecimal doanhThuTienMat;
    private BigDecimal doanhThuChuyenKhoan;
}
