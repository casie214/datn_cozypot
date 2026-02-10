package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.KhachHangRequest;
import com.example.datn_cozypot_spring_boot.dto.KhachHangResponse;
import com.example.datn_cozypot_spring_boot.dto.KhachHangThongKeResponse;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Import cho POI (Xử lý Excel)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Import cho Spring Framework (Resource & Response)

// Import cho Java IO
import java.io.ByteArrayOutputStream;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository repo;

    private final Path root = Paths.get("uploads/customers");


    public KhachHangService() {
        try {
            if (!Files.exists(root)) Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folder for customers");
        }
    }

    // 1. Lấy danh sách + Tìm kiếm + Phân trang
    public Page<KhachHangResponse> getAll(String keyword, Integer trangThai, LocalDate tuNgay, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "trangThai").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        // Truyền thẳng tuNgay vào Repository
        return repo.searchKhachHang(keyword, trangThai, tuNgay, pageable)
                .map(this::convertToResponse);
    }

    // 2. Chi tiết
    public KhachHangResponse getDetail(Integer id) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        return convertToResponse(kh);
    }

    // 3. Kiểm tra trùng lặp (Dùng cho API check-duplicate ở Controller)
    public boolean checkDuplicate(String type, String value, Integer excludeId) {
        return switch (type) {
            case "email" -> excludeId != null
                    ? repo.existsByEmailAndIdNot(value, excludeId)
                    : repo.existsByEmail(value);
            case "soDienThoai" -> excludeId != null
                    ? repo.existsBySoDienThoaiAndIdNot(value, excludeId)
                    : repo.existsBySoDienThoai(value);
            case "tenDangNhap" -> excludeId != null
                    ? repo.existsByTenDangNhapAndIdNot(value, excludeId)
                    : repo.existsByTenDangNhap(value);
            default -> false;
        };
    }

    // 4. Thêm mới
    @Transactional
    public KhachHangResponse create(KhachHangRequest req, MultipartFile file) {
        if (repo.existsBySoDienThoai(req.getSoDienThoai())) throw new RuntimeException("Số điện thoại đã tồn tại!");
        if (repo.existsByEmail(req.getEmail())) throw new RuntimeException("Email đã tồn tại!");

        KhachHang kh = new KhachHang();
        mapRequestToEntity(req, kh);

        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        }

        return convertToResponse(repo.save(kh));
    }

    // 5. Cập nhật
    @Transactional
    public KhachHangResponse update(Integer id, KhachHangRequest req, MultipartFile file) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (repo.existsBySoDienThoaiAndIdNot(req.getSoDienThoai(), id)) throw new RuntimeException("Số điện thoại đã bị sử dụng!");
        if (repo.existsByEmailAndIdNot(req.getEmail(), id)) throw new RuntimeException("Email đã bị sử dụng!");

        mapRequestToEntity(req, kh);

        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        }

        return convertToResponse(repo.save(kh));
    }

    // 6. Đảo trạng thái
    @Transactional
    public KhachHangResponse toggleStatus(Integer id) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        kh.setTrangThai(kh.getTrangThai() != null && kh.getTrangThai() == 1 ? 0 : 1);
        return convertToResponse(repo.save(kh));
    }

    // --- Helper Methods ---
    private String saveFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage());
        }
    }

    private void mapRequestToEntity(KhachHangRequest req, KhachHang kh) {
        kh.setTenKhachHang(req.getTenKhachHang());
        kh.setSoDienThoai(req.getSoDienThoai());
        kh.setEmail(req.getEmail());
        kh.setNgaySinh(req.getNgaySinh());
        kh.setGioiTinh(req.getGioiTinh());
        kh.setDiaChi(req.getDiaChi());
        kh.setTrangThai(req.getTrangThai());
        kh.setTenDangNhap(req.getTenDangNhap());
        if (req.getMatKhauDangNhap() != null && !req.getMatKhauDangNhap().isEmpty()) {
            kh.setMatKhauDangNhap(req.getMatKhauDangNhap());
        }
    }

    private KhachHangResponse convertToResponse(KhachHang kh) {
        KhachHangResponse res = new KhachHangResponse();
        BeanUtils.copyProperties(kh, res);
        return res;
    }

    public byte[] exportExcel(String keyword, Integer trangThai, LocalDate tuNgay) {

        // Lấy toàn bộ khách hàng
        List<KhachHang> list = repo.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Khách hàng");

            // ===== HEADER =====
            Row header = sheet.createRow(0);

            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Mã KH");
            header.createCell(2).setCellValue("Tên khách hàng");
            header.createCell(3).setCellValue("SĐT");
            header.createCell(4).setCellValue("Email");
            header.createCell(5).setCellValue("Ngày sinh");
            header.createCell(6).setCellValue("Điểm tích lũy");
            header.createCell(7).setCellValue("Ngày tạo tài khoản");
            header.createCell(8).setCellValue("Giới tính");
            header.createCell(9).setCellValue("Tên đăng nhập");
            header.createCell(10).setCellValue("Mật khẩu");
            header.createCell(11).setCellValue("Trạng thái");
            header.createCell(12).setCellValue("Địa chỉ");

            // ===== DATA =====
            int rowIndex = 1;

            for (KhachHang kh : list) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(kh.getId());
                row.createCell(1).setCellValue(kh.getMaKhachHang());
                row.createCell(2).setCellValue(kh.getTenKhachHang());
                row.createCell(3).setCellValue(kh.getSoDienThoai());
                row.createCell(4).setCellValue(kh.getEmail());

                row.createCell(5).setCellValue(
                        kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : ""
                );

                row.createCell(6).setCellValue(
                        kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0
                );

                row.createCell(7).setCellValue(
                        kh.getNgayTaoTaiKhoan() != null ? kh.getNgayTaoTaiKhoan().toString() : ""
                );

                row.createCell(8).setCellValue(
                        kh.getGioiTinh() != null && kh.getGioiTinh() ? "Nam" : "Nữ"
                );

                row.createCell(9).setCellValue(kh.getTenDangNhap());
                row.createCell(10).setCellValue(kh.getMatKhauDangNhap());
                row.createCell(11).setCellValue(kh.getTrangThai());
                row.createCell(12).setCellValue(kh.getDiaChi());
            }

            // auto size cho đẹp
            for (int i = 0; i <= 12; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi export Excel: " + e.getMessage());
        }
    }

    public List<KhachHangThongKeResponse> thongKeKhachHang(int thang, int nam) {
        List<Object[]> rows = repo.thongKeKhachHangTheoThang(thang, nam);

        List<KhachHangThongKeResponse> result = new ArrayList<>();

        for (Object[] row : rows) {
            KhachHangThongKeResponse dto = new KhachHangThongKeResponse();

            dto.setId(((Number) row[0]).longValue());
            dto.setMaKhachHang((String) row[1]);
            dto.setTenKhachHang((String) row[2]);
            dto.setSoDienThoai((String) row[3]);
            dto.setEmail((String) row[4]);
            dto.setNgaySinh(row[5] != null ? ((java.sql.Date) row[5]).toLocalDate() : null);

            dto.setSoLanDatTrongThang(
                    row[6] != null ? ((Number) row[6]).intValue() : 0
            );

            dto.setTongChiTieuTrongThang(
                    row[7] != null ? ((Number) row[7]).longValue() : 0L
            );

            dto.setLanDatGanNhat(
                    row[8] != null
                            ? ((java.sql.Timestamp) row[8]).toLocalDateTime()
                            : null
            );

            result.add(dto);
        }

        return result;
    }

}