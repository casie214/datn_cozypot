package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.thongKe.KenhDatResponse;
import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeDoanhThuDTO;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepository;
import com.example.datn_cozypot_spring_boot.repository.ThongKeRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ThongKeService {

    @Autowired
    private ThongKeRepository thongKeRepository;

    @Autowired
    private HoaDonThanhToanRepository hoaDonThanhToanRepository;

    public ThongKeDoanhThuDTO layThongKeTheoLoai(String loai, String tuNgay, String denNgay) {
        // 1. Lấy dữ liệu tổng quan thô từ DB
        Map<String, Object> data = thongKeRepository.layDuLieuThongKeChiTiet(loai, tuNgay, denNgay);
        ThongKeDoanhThuDTO dto = new ThongKeDoanhThuDTO();

        if (data != null) {
            // A. DOANH THU 4 THẺ TỔNG QUAN
            dto.setDoanhThuHomNay(toBigDecimal(data.get("doanhThuHomNay")));
            dto.setDoanhThuTuanNay(toBigDecimal(data.get("doanhThuTuanNay")));
            dto.setDoanhThuThangNay(toBigDecimal(data.get("doanhThuThangNay")));
            dto.setDoanhThuNamNay(toBigDecimal(data.get("doanhThuNamNay")));

            // B. SỐ ĐƠN TƯƠNG ỨNG 4 THẺ
            dto.setSoDonHomNay(toLong(data.get("soDonHomNay")));
            dto.setSoDonTuanNay(toLong(data.get("soDonTuanNay")));
            dto.setSoDonThangNay(toLong(data.get("soDonThangNay")));
            dto.setSoDonNamNay(toLong(data.get("soDonNamNay")));

            // C. CÁC CHỈ SỐ BẢNG CHI TIẾT & DOANH THU LUỒNG
            dto.setTongHoaDon(toLong(data.get("tongHoaDon")));
            dto.setGiaTriTrungBinhDon(toBigDecimal(data.get("giaTriTrungBinhDon")));
            dto.setTongTienCoc(toBigDecimal(data.get("tongTienCoc")));
            dto.setTongGiamGia(toBigDecimal(data.get("tongGiamGia")));
            dto.setTongHoaDonHuy(toLong(data.get("tongHoaDonHuy")));
            dto.setDoanhThuThucNhan(toBigDecimal(data.get("doanhThuThucNhan")));

            dto.setDoanhThuTienMat(toBigDecimal(data.get("doanhThuTienMat")));
            dto.setDoanhThuChuyenKhoan(toBigDecimal(data.get("doanhThuChuyenKhoan")));
            dto.setDoanhThuDuKien(toBigDecimal(data.get("doanhThuDuKien")));

            // D. KHÁCH HÀNG & BÀN (Fix lỗi hiển thị số 0)
            dto.setTongBanDaDat(toLong(data.get("tongBanDaDat")));
            dto.setTongKhachHang(toLong(data.get("tongKhachHang")));
            dto.setKhachMoi(toLong(data.get("khachMoi")));
            dto.setKhachQuayLai(toLong(data.get("khachQuayLai")));
        }

        // --- 2. DỮ LIỆU BIỂU ĐỒ 12 THÁNG ---
        List<Map<String, Object>> listDataChart = thongKeRepository.layDoanhThu12Thang();
        if (listDataChart != null) {
            List<BigDecimal> doanhThu12Thang = listDataChart.stream()
                    .map(m -> toBigDecimal(m.get("doanhThu")))
                    .collect(Collectors.toList());
            dto.setDoanhThu12Thang(doanhThu12Thang);
        }

        // --- 3. TÍNH TỐC ĐỘ TĂNG TRƯỞNG LÝ TƯỞNG ---
        BigDecimal doanhThuThangNay = dto.getDoanhThuThangNay() != null ? dto.getDoanhThuThangNay() : BigDecimal.ZERO;
        BigDecimal doanhThuThangTruoc = thongKeRepository.layDoanhThuThangTruoc();
        if (doanhThuThangTruoc == null) doanhThuThangTruoc = BigDecimal.ZERO;

        double tocDoTangTruongThang = 0;
        if (doanhThuThangTruoc.compareTo(BigDecimal.ZERO) > 0) {
            tocDoTangTruongThang = doanhThuThangNay.subtract(doanhThuThangTruoc)
                    .divide(doanhThuThangTruoc, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100)).doubleValue();
        } else if (doanhThuThangNay.compareTo(BigDecimal.ZERO) > 0) {
            tocDoTangTruongThang = 100.0; // Nếu tháng trước = 0 mà tháng này có tiền thì auto +100%
        }

        double tocDoTangTruong = 0;

        // Điều chỉnh % Tăng trưởng trả về phụ thuộc vào 'loai' Tab đang chọn
        if ("Hôm nay".equals(loai)) {
            BigDecimal doanhThuHomQua = BigDecimal.ZERO;
            try {
                // Nếu BE chưa có hàm layDoanhThuHomQua() thì chỗ này = 0 không sao, nó sẽ chạy an toàn
                // doanhThuHomQua = thongKeRepository.layDoanhThuHomQua();
            } catch (Exception ignored) {}

            BigDecimal doanhThuHomNay = dto.getDoanhThuHomNay() != null ? dto.getDoanhThuHomNay() : BigDecimal.ZERO;
            if (doanhThuHomQua.compareTo(BigDecimal.ZERO) > 0) {
                tocDoTangTruong = doanhThuHomNay.subtract(doanhThuHomQua)
                        .divide(doanhThuHomQua, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue();
            } else if (doanhThuHomNay.compareTo(BigDecimal.ZERO) > 0) {
                tocDoTangTruong = 100.0;
            }
        } else {
            // Mặc định Tuần, Tháng, Năm, Tùy chỉnh thì trả về % của Tháng này
            tocDoTangTruong = tocDoTangTruongThang;
        }

        dto.setTocDoTangTruong(tocDoTangTruong);

        // --- 4. TOP SẢN PHẨM ---
        dto.setTopSets(thongKeRepository.layTopSetLauBanChay());

        // --- 5. HÓA ĐƠN CHỜ & ĐÃ CỌC ---
        Long soHoaDonChoVaDaCoc = thongKeRepository.demHoaDonChoVaDaCoc();
        BigDecimal tongTienChoVaDaCoc = thongKeRepository.tongTienHoaDonChoVaDaCoc();

        dto.setSoHoaDonChoVaDaCoc(soHoaDonChoVaDaCoc != null ? soHoaDonChoVaDaCoc : 0L);
        dto.setTongTienHoaDonChoVaDaCoc(
                tongTienChoVaDaCoc != null ? tongTienChoVaDaCoc : BigDecimal.ZERO
        );

        return dto;
    }

    // Hàm an toàn để chuyển đổi sang BigDecimal
    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(obj.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // Hàm an toàn để chuyển đổi sang Long
    private Long toLong(Object obj) {
        if (obj == null) return 0L;
        try {
            return ((Number) obj).longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    public List<Map<String, Object>> layTrangThaiDonHang() {
        return thongKeRepository.thongKeTrangThaiDonHang();
    }

    public List<KenhDatResponse> thongKeKenhDat() {
        return hoaDonThanhToanRepository.thongKeKenhDat();
    }

    public byte[] generateExcelData(ThongKeDoanhThuDTO data) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Sử dụng ColorMap để tránh lỗi null khi tạo XSSFColor
            DefaultIndexedColorMap colorMap = new DefaultIndexedColorMap();

            Sheet sheet = workbook.createSheet("Báo cáo doanh thu");
            sheet.setColumnWidth(0, 2000);  // STT
            sheet.setColumnWidth(1, 10000); // Hạng mục (rộng hơn tí)
            sheet.setColumnWidth(2, 6000);  // Giá trị
            sheet.setColumnWidth(3, 8000);  // Chú thích

            // --- 1. ĐỊNH NGHĨA MÀU SẮC CHUẨN RGB ---
            XSSFColor cozyRed = new XSSFColor(new byte[]{(byte) 139, (byte) 0, (byte) 0}, colorMap);
            XSSFColor cozyOrange = new XSSFColor(new byte[]{(byte) 244, (byte) 81, (byte) 30}, colorMap);

            // --- 2. TẠO STYLES ---

            // Style Tiêu đề chính (COZYPOT RESTAURANT)
            XSSFCellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 20);
            titleFont.setColor(cozyRed);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // Style Header Bảng
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(cozyOrange);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            this.setFullBorder(headerStyle); // Hàm phụ kẻ viền

            XSSFFont headerFont = workbook.createFont();
            headerFont.setColor(new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 255}, colorMap));
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Style dữ liệu thông thường
            XSSFCellStyle dataStyle = workbook.createCellStyle();
            this.setFullBorder(dataStyle);

            // Style Số tiền (Bold + Format VNĐ)
            XSSFCellStyle moneyStyle = workbook.createCellStyle();
            this.setFullBorder(moneyStyle);
            XSSFDataFormat df = workbook.createDataFormat();
            moneyStyle.setDataFormat(df.getFormat("#,##0\" ₫\""));
            XSSFFont boldFont = workbook.createFont();
            boldFont.setBold(true);
            moneyStyle.setFont(boldFont);

            // --- 3. VẼ NỘI DUNG ---

            // Dòng 1: Tên nhà hàng
            Row row0 = sheet.createRow(0);
            row0.setHeightInPoints(30);
            Cell cell0 = row0.createCell(0);
            cell0.setCellValue("COZYPOT RESTAURANT");
            cell0.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            // Dòng 2: Ngày báo cáo
            Row row1 = sheet.createRow(1);
            Cell cell1 = row1.createCell(0);
            cell1.setCellValue("Báo cáo doanh thu ngày: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            cell1.setCellStyle(dateStyle);

            // --- 4. BẢNG TỔNG QUAN ---
            Row headerRow = sheet.createRow(3);
            headerRow.setHeightInPoints(25);
            String[] headers = {"STT", "Hạng mục thống kê", "Giá trị thực tế", "Ghi chú hệ thống"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dữ liệu lấy từ DTO
            Object[][] summaryData = {
                    {1, "Doanh thu hôm nay", data.getDoanhThuHomNay(), "Ghi nhận tức thời"},
                    {2, "Tổng số hóa đơn", data.getTongHoaDon(), "Đơn đã hoàn tất"},
                    {3, "Tổng tiền cọc đã thu", data.getTongTienCoc(), "Tiền bảo đảm đặt bàn"},
                    {4, "Tổng giảm giá/Voucher", data.getTongGiamGia(), "Chi phí khuyến mãi"},
                    {5, "DOANH THU THỰC NHẬN", data.getDoanhThuThucNhan(), "Sau khi trừ giảm giá"}
            };

            int rowNum = 4;
            for (Object[] ds : summaryData) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(20);

                Cell c0 = row.createCell(0); c0.setCellValue((int) ds[0]); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue((String) ds[1]); c1.setCellStyle(dataStyle);

                Cell valueCell = row.createCell(2);
                valueCell.setCellValue(Double.parseDouble(ds[2].toString()));
                valueCell.setCellStyle(moneyStyle);

                Cell c3 = row.createCell(3); c3.setCellValue((String) ds[3]); c3.setCellStyle(dataStyle);
            }

            // --- 5. XUẤT RA BYTE ARRAY ---
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    // Hàm hỗ trợ kẻ viền nhanh
    private void setFullBorder(XSSFCellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

}