package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.NhanVienRequest;
import com.example.datn_cozypot_spring_boot.dto.NhanVienResponse;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.example.datn_cozypot_spring_boot.repository.VaiTroRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
// Import cho POI (Xử lý Excel)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Import cho Spring Framework (Resource & Response)
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

// Import cho Java IO
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class NhanVienService {

    @Autowired
    private NhanVienRepository repo;

    @Autowired
    private VaiTroRepository vaiTroRepo;

    // Đường dẫn gốc lưu ảnh: Project_Root/uploads/images
    private final Path storageFolder = Paths.get("uploads/images");

    public NhanVienService() {
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ ảnh!", e);
        }
    }

    // --- LOGIC LƯU FILE ẢNH ---
    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            // Tạo tên file duy nhất: UUID + Tên gốc
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path destinationFile = storageFolder.resolve(fileName).normalize().toAbsolutePath();

            // Sao chép file vào thư mục đích
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
        }
    }

    // --- CHECK TRÙNG (Real-time từ Vue) ---
    public boolean checkDuplicate(String type, String value, Integer excludeId) {
        if (value == null || value.isBlank()) return false;
        if (excludeId != null) {
            return switch (type) {
                case "tenDangNhap" -> repo.existsByTenDangNhapAndIdNot(value, excludeId);
                case "email" -> repo.existsByEmailAndIdNot(value, excludeId);
                case "sdtNhanVien" -> repo.existsBySdtNhanVienAndIdNot(value, excludeId);
                case "soCccd" -> repo.existsBySoCccdAndIdNot(value, excludeId);
                default -> false;
            };
        } else {
            return switch (type) {
                case "tenDangNhap" -> repo.existsByTenDangNhap(value);
                case "email" -> repo.existsByEmail(value);
                case "sdtNhanVien" -> repo.existsBySdtNhanVien(value);
                case "soCccd" -> repo.existsBySoCccd(value);
                default -> false;
            };
        }
    }

    public Page<NhanVienResponse> getAll(String keyword, Integer trangThai, LocalDate tuNgay, int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("trangThaiLamViec"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.searchNhanVien(keyword, trangThai, tuNgay, pageable).map(this::convertToResponse);
    }

    public NhanVienResponse getDetail(Integer id) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên id: " + id));
        return convertToResponse(nv);
    }

    // --- THÊM MỚI ---
    @Transactional
    public NhanVienResponse create(NhanVienRequest req, MultipartFile file) {
        if (repo.existsByTenDangNhap(req.getTenDangNhap())) throw new RuntimeException("Tên đăng nhập đã tồn tại");
        if (repo.existsByEmail(req.getEmail())) throw new RuntimeException("Email đã tồn tại");

        NhanVien nv = new NhanVien();
        mapRequestToEntity(req, nv);

        // Lưu ảnh nếu có
        if (file != null && !file.isEmpty()) {
            nv.setAnhDaiDien(saveImage(file));
        }

        return convertToResponse(repo.save(nv));
    }

    // --- CẬP NHẬT ---
    @Transactional
    public NhanVienResponse update(Integer id, NhanVienRequest req, MultipartFile file) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (repo.existsByTenDangNhapAndIdNot(req.getTenDangNhap(), id)) throw new RuntimeException("Tên đăng nhập bị trùng");
        if (repo.existsByEmailAndIdNot(req.getEmail(), id)) throw new RuntimeException("Email đã bị trùng");

        mapRequestToEntity(req, nv);

        // Nếu có file mới thì cập nhật, không thì giữ tên ảnh cũ (đã có trong Entity sau khi map)
        if (file != null && !file.isEmpty()) {
            nv.setAnhDaiDien(saveImage(file));
        }

        return convertToResponse(repo.save(nv));
    }

    @Transactional
    public NhanVienResponse toggleStatus(Integer id) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        nv.setTrangThaiLamViec(nv.getTrangThaiLamViec() == 1 ? 2 : 1);
        return convertToResponse(repo.save(nv));
    }

    private void mapRequestToEntity(NhanVienRequest req, NhanVien nv) {
        nv.setHoTenNhanVien(req.getHoTenNhanVien());
        nv.setSdtNhanVien(req.getSdtNhanVien());
        nv.setDiaChi(req.getDiaChi());
        nv.setEmail(req.getEmail());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgayVaoLam(req.getNgayVaoLam());
        nv.setTrangThaiLamViec(req.getTrangThaiLamViec());
        nv.setTenDangNhap(req.getTenDangNhap());
        nv.setNgaySinh(req.getNgaySinh());
        nv.setSoCccd(req.getSoCccd());
        nv.setNgayCapCccd(req.getNgayCapCccd());
        nv.setNoiCapCccd(req.getNoiCapCccd());

        // Lưu ý: Không map trực tiếp AnhDaiDien từ Request vì Request gửi lên thường là null khi dùng FormData
        if (req.getMatKhauDangNhap() != null && !req.getMatKhauDangNhap().isEmpty()) {
            nv.setMatKhauDangNhap(req.getMatKhauDangNhap());
        }
        if(req.getIdVaiTro() != null) {
            nv.setIdVaiTro(vaiTroRepo.findById(req.getIdVaiTro()).orElse(null));
        }
    }

    private NhanVienResponse convertToResponse(NhanVien nv) {
        NhanVienResponse res = new NhanVienResponse();
        BeanUtils.copyProperties(nv, res);
        if (nv.getIdVaiTro() != null) {
            res.setTenVaiTro(nv.getIdVaiTro().getTenVaiTro());
            res.setIdVaiTro(nv.getIdVaiTro().getId());
        }
        return res;
    }

    public ResponseEntity<Resource> exportExcel(String keyword, Integer trangThai, LocalDate tuNgay) throws IOException {
        java.util.List<NhanVien> list = repo.searchNhanVien(keyword, trangThai, tuNgay, Pageable.unpaged()).getContent();

        // Khai báo định dạng ngày tháng Việt Nam
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String[] columns = {
                "STT", "Mã NV", "Họ Tên", "SĐT", "Email", "Giới Tính", "Ngày Sinh",
                "Số CCCD", "Ngày Cấp", "Nơi Cấp", "Địa Chỉ", "Vai Trò",
                "Ngày Vào Làm", "Tên Đăng Nhập", "Trạng Thái"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Nhân Viên");

            // --- Style cho Header ---
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- Đổ dữ liệu ---
            int rowIdx = 1;
            for (NhanVien nv : list) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(nv.getMaNhanVien() != null ? nv.getMaNhanVien() : "");
                row.createCell(2).setCellValue(nv.getHoTenNhanVien() != null ? nv.getHoTenNhanVien() : "");
                row.createCell(3).setCellValue(nv.getSdtNhanVien() != null ? nv.getSdtNhanVien() : "");
                row.createCell(4).setCellValue(nv.getEmail() != null ? nv.getEmail() : "");
                row.createCell(5).setCellValue(nv.getGioiTinh() != null ? (nv.getGioiTinh() ? "Nam" : "Nữ") : "");

                // Định dạng: Ngày Sinh
                row.createCell(6).setCellValue(nv.getNgaySinh() != null ? nv.getNgaySinh().format(formatter) : "");

                row.createCell(7).setCellValue(nv.getSoCccd() != null ? nv.getSoCccd() : "");

                // Định dạng: Ngày Cấp CCCD
                row.createCell(8).setCellValue(nv.getNgayCapCccd() != null ? nv.getNgayCapCccd().format(formatter) : "");

                row.createCell(9).setCellValue(nv.getNoiCapCccd() != null ? nv.getNoiCapCccd() : "");
                row.createCell(10).setCellValue(nv.getDiaChi() != null ? nv.getDiaChi() : "");
                row.createCell(11).setCellValue(nv.getIdVaiTro() != null ? nv.getIdVaiTro().getTenVaiTro() : "N/A");

                // Định dạng: Ngày Vào Làm
                row.createCell(12).setCellValue(nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(formatter) : "");

                row.createCell(13).setCellValue(nv.getTenDangNhap() != null ? nv.getTenDangNhap() : "");
                row.createCell(14).setCellValue(nv.getTrangThaiLamViec() != null ? (nv.getTrangThaiLamViec() == 1 ? "Đang làm việc" : "Ngừng hoạt động") : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            InputStreamResource resource = new InputStreamResource(in);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Sach_Nhan_Vien.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        }
    }}