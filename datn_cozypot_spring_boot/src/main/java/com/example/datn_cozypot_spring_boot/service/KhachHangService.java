package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.*;
import com.example.datn_cozypot_spring_boot.dto.KhachHangThongKeResponse;
import com.example.datn_cozypot_spring_boot.entity.DiaChiKhachHang;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.service.userEmailService.UserMailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// Import cho POI (Xử lý Excel)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Import cho Spring Framework (Resource & Response)

// Import cho Java IO
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path root = Paths.get("uploads/customers");
    @Autowired
    private UserMailService userMailService;

    public KhachHangService() {
        try {
            if (!Files.exists(root)) Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folder for customers");
        }
    }

    // 1. Lấy danh sách (Sửa lỗi mapping ở đây)
    public Page<KhachHangResponse> getAll(String keyword, Integer trangThai, LocalDate tuNgay, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "trangThai").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        return repo.searchKhachHang(keyword, trangThai, tuNgay, pageable)
                .map(kh -> this.convertToResponse(kh)); // Dùng Lambda để tránh lỗi Method Reference
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

    @Transactional
    public KhachHangResponse create(KhachHangRequest req, MultipartFile file) {
        if (repo.existsBySoDienThoai(req.getSoDienThoai())) throw new RuntimeException("Số điện thoại đã tồn tại!");

        KhachHang kh = new KhachHang();
        mapRequestToEntity(req, kh);

        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        }
        KhachHang savedKh = repo.save(kh);

        // GỬI MAIL CHÀO MỪNG (Đã sửa request -> req)
        try {
            userMailService.sendClientNotificationMail(req, "CREATE");
        } catch (Exception e) {
            System.err.println("🔥 Lỗi gửi mail khi tạo mới: " + e.getMessage());
        }

        return convertToResponse(savedKh);
    }

    // 5. Cập nhật
    @Transactional
    public KhachHangResponse update(Integer id, KhachHangRequest req, MultipartFile file) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // 1. Map thông tin text (Dùng hàm của bạn - Rất an toàn vì không có field ảnh)
        mapRequestToEntity(req, kh);

        // 2. Xử lý ảnh - Đảm bảo tính duy nhất
        if (file != null && !file.isEmpty()) {
            // CÓ FILE MỚI: Ưu tiên số 1, lưu file và lấy tên mới hoàn toàn
            kh.setAnhDaiDien(saveFile(file));
        } else {
            // KHÔNG CÓ FILE MỚI: Lấy lại tên cũ từ Request
            String oldImage = req.getAnhDaiDien();

            if (oldImage != null && !oldImage.isEmpty()) {
                // Đề phòng trường hợp Frontend gửi chuỗi bị lặp (có dấu phẩy)
                // Ta chỉ lấy phần tử đầu tiên trước dấu phẩy
                if (oldImage.contains(",")) {
                    oldImage = oldImage.split(",")[0];
                }
                kh.setAnhDaiDien(oldImage);
            }
            // Nếu cả 2 đều trống thì giữ nguyên ảnh cũ trong DB (kh.getAnhDaiDien())
        }

        KhachHang savedKh = repo.save(kh);
        return convertToResponse(savedKh);
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
        // 1. Cập nhật thông tin cơ bản khách hàng
        kh.setTenKhachHang(req.getTenKhachHang());
        kh.setSoDienThoai(req.getSoDienThoai());
        kh.setEmail(req.getEmail());
        kh.setNgaySinh(req.getNgaySinh());
        kh.setGioiTinh(req.getGioiTinh());
        kh.setTrangThai(req.getTrangThai());
        kh.setTenDangNhap(req.getTenDangNhap());

        if (req.getMatKhauDangNhap() != null && !req.getMatKhauDangNhap().isEmpty()) {
            kh.setMatKhauDangNhap(req.getMatKhauDangNhap());
        }

        // 2. Đảm bảo danh sách địa chỉ không null
        if (kh.getDanhSachDiaChi() == null) {
            kh.setDanhSachDiaChi(new ArrayList<>());
        }

        // Nếu request không gửi danh sách địa chỉ, ta giữ nguyên dữ liệu cũ hoặc xóa sạch?
        // Thường là giữ nguyên nếu null, xóa sạch nếu gửi list rỗng [].
        if (req.getDanhSachDiaChi() == null) return;

        // Bước A: Lấy danh sách ID để xóa những địa chỉ không còn trong request
        Set<Integer> requestIds = req.getDanhSachDiaChi().stream()
                .map(DiaChiRequest::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        if (!requestIds.isEmpty()) {
            kh.getDanhSachDiaChi().removeIf(entity ->
                    entity.getId() != null && !requestIds.contains(entity.getId())
            );
        }


        // Bước B: Cập nhật hoặc Thêm mới
        for (DiaChiRequest dto : req.getDanhSachDiaChi()) {
            DiaChiKhachHang entity;

            if (dto.getId() != null) {
                // Tìm địa chỉ cũ trong list của khách hàng này
                entity = kh.getDanhSachDiaChi().stream()
                        .filter(dc -> dc.getId().equals(dto.getId()))
                        .findFirst()
                        .orElse(null);

                if (entity == null) continue; // Bỏ qua nếu ID không thuộc về khách hàng này
            } else {
                // Thêm mới hoàn toàn
                entity = new DiaChiKhachHang();
                entity.setKhachHang(kh); // Quan trọng: Gắn cha cho con ngay lập tức
                kh.getDanhSachDiaChi().add(entity);
            }

            // Cập nhật thông tin địa chỉ
            entity.setThongTinDiaChi(dto.getThongTinDiaChi());
            entity.setLaMacDinh(Boolean.TRUE.equals(dto.getLaMacDinh()));

            // SỬA TẠI ĐÂY: Ưu tiên lấy từ DTO, nếu DTO trống mới lấy theo khách hàng
            entity.setHoTenNhan(dto.getHoTenNhan() != null ? dto.getHoTenNhan() : kh.getTenKhachHang());
            entity.setSoDienThoaiNhan(dto.getSoDienThoaiNhan() != null ? dto.getSoDienThoaiNhan() : kh.getSoDienThoai());
        }
    }

    // --- EXPORT EXCEL (HÀM QUAN TRỌNG) ---
    public ResponseEntity<Resource> exportExcel(String keyword, Integer trangThai, LocalDate tuNgay, List<Integer> listId) throws IOException {
        List<KhachHang> list;
        try {
            if (listId != null && !listId.isEmpty()) {
                list = repo.findAllById(listId);
            } else {
                list = repo.searchKhachHang(keyword, trangThai, tuNgay, PageRequest.of(0, 10000)).getContent();
            }
        } catch (Exception e) {
            System.err.println("🔥 Lỗi truy vấn DB khi xuất Excel: " + e.getMessage());
            throw e;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        // Thêm "Địa chỉ" vào mảng cột
        String[] columns = {"STT", "Mã KH", "Tên KH", "SĐT", "Email", "Giới tính", "Ngày sinh", "Điểm", "Ngày tạo", "Trạng thái", "Địa chỉ mặc định"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Khách hàng");

            // Style cho Header (Giữ nguyên logic của bạn)
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (KhachHang kh : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(kh.getMaKhachHang() != null ? kh.getMaKhachHang() : "");
                row.createCell(2).setCellValue(kh.getTenKhachHang() != null ? kh.getTenKhachHang() : "");
                row.createCell(3).setCellValue(kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "");
                row.createCell(4).setCellValue(kh.getEmail() != null ? kh.getEmail() : "");

                String gioiTinh = (kh.getGioiTinh() != null) ? (kh.getGioiTinh() ? "Nam" : "Nữ") : "N/A";
                row.createCell(5).setCellValue(gioiTinh);

                row.createCell(6).setCellValue(kh.getNgaySinh() != null ? kh.getNgaySinh().format(formatter) : "");
                row.createCell(7).setCellValue(kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0);
                row.createCell(8).setCellValue(kh.getNgayTaoTaiKhoan() != null ? kh.getNgayTaoTaiKhoan().toString() : "");

                String tt = (kh.getTrangThai() != null && kh.getTrangThai() == 1) ? "Hoạt động" : "Ngừng hoạt động";
                row.createCell(9).setCellValue(tt);

                // --- MỚI: Logic lấy địa chỉ mặc định ---
                String diaChiMacDinh = "";
                if (kh.getDanhSachDiaChi() != null && !kh.getDanhSachDiaChi().isEmpty()) {
                    diaChiMacDinh = kh.getDanhSachDiaChi().stream()
                            .filter(dc -> dc.getLaMacDinh() != null && dc.getLaMacDinh())
                            .map(dc -> dc.getThongTinDiaChi())
                            .findFirst()
                            .orElse(kh.getDanhSachDiaChi().get(0).getThongTinDiaChi()); // Nếu không có cái nào mặc định, lấy cái đầu tiên
                }
                row.createCell(10).setCellValue(diaChiMacDinh);
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            // ... (phần còn lại giữ nguyên)
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DS_KhachHang.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            System.err.println("🔥 Lỗi tạo file Excel: " + e.getMessage());
            throw e;
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

    private KhachHangResponse convertToResponse(KhachHang kh) {
        if (kh == null) return null;
        KhachHangResponse res = new KhachHangResponse();
        BeanUtils.copyProperties(kh, res);

        // Đảm bảo dòng này tồn tại để chuyển tên file ảnh sang giao diện
        res.setAnhDaiDien(kh.getAnhDaiDien());

        if (kh.getDanhSachDiaChi() != null && !kh.getDanhSachDiaChi().isEmpty()) {
            // 1. Chuyển đổi List Entity sang List DTO (DiaChiResponse)
            List<DiaChiResponse> listDiaChiDto = kh.getDanhSachDiaChi().stream().map(dc -> {
                DiaChiResponse dto = new DiaChiResponse();
                BeanUtils.copyProperties(dc, dto);
                dto.setId(dc.getId()); // Đảm bảo ID được giữ lại để update
                return dto;
            }).collect(Collectors.toList());

            // 2. Gán danh sách này vào Response chính
            res.setDanhSachDiaChi(listDiaChiDto);

            // 3. Logic lấy chuỗi địa chỉ mặc định để hiện ở bảng ngoài (giữ nguyên)
            String diaChiMacDinh = kh.getDanhSachDiaChi().stream()
                    .filter(dc -> Boolean.TRUE.equals(dc.getLaMacDinh()))
                    .map(DiaChiKhachHang::getThongTinDiaChi)
                    .findFirst()
                    .orElse(kh.getDanhSachDiaChi().get(0).getThongTinDiaChi());
            res.setDiaChi(diaChiMacDinh);
        } else {
            res.setDanhSachDiaChi(new ArrayList<>());
        }
        return res;
    }
}