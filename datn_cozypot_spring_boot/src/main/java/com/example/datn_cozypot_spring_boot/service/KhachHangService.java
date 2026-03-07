package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.*;
import com.example.datn_cozypot_spring_boot.dto.KhachHangThongKeResponse;
import com.example.datn_cozypot_spring_boot.dto.profile.KhachHangProfileRequest;
import com.example.datn_cozypot_spring_boot.entity.DiaChiKhachHang;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.repository.DiaChiKhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.example.datn_cozypot_spring_boot.service.userEmailService.UserMailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


// Import cho Java IO
import java.io.ByteArrayOutputStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Autowired
    private NhanVienRepository nhanVienRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path root = Paths.get("uploads/images");
    @Autowired
    private UserMailService userMailService;
    @Autowired
    private DiaChiKhachHangRepository diaChiRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public KhachHangService() {
        try {
            if (!Files.exists(root)) Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folder for customers");
        }
    }

    public Page<KhachHangResponse> getAll(String keyword, Integer trangThai, Boolean gioiTinh, LocalDate tuNgay, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "trangThai").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);

        return repo.searchKhachHang(keyword, trangThai, gioiTinh, tuNgay, pageable)
                .map(this::convertToResponse);
    }

    public KhachHangResponse getDetail(Integer id) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        return convertToResponse(kh);
    }

    public boolean checkDuplicate(String type, String value, Integer excludeId) {
        return switch (type) {
            case "email" -> {
                boolean existsInKH = excludeId != null
                        ? repo.existsByEmailAndIdNot(value, excludeId)
                        : repo.existsByEmail(value);
                boolean existsInNV = nhanVienRepo.existsByEmail(value);
                yield existsInKH || existsInNV; // Trả về true nếu trùng ở bất cứ đâu
            }
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
        if (repo.existsBySoDienThoai(req.getSoDienThoai()))
            throw new RuntimeException("Số điện thoại đã tồn tại!");
        if (nhanVienRepo.existsByTenDangNhap(req.getEmail()))
            throw new RuntimeException("Email này đã được sử dụng");
        KhachHang kh = new KhachHang();
        mapRequestToEntity(req, kh);
        kh.setTenDangNhap(req.getEmail());
        String rawPassword = (req.getMatKhauDangNhap() != null && !req.getMatKhauDangNhap().isEmpty())
                ? req.getMatKhauDangNhap() : "123456";
        kh.setMatKhauDangNhap(passwordEncoder.encode(rawPassword));
        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        }
        KhachHang savedKh = repo.save(kh);
        if (req.getDanhSachDiaChi() != null && !req.getDanhSachDiaChi().isEmpty()) {
            saveDanhSachDiaChi(req.getDanhSachDiaChi(), savedKh);
        }
        try {
            req.setTenDangNhap(req.getEmail());
            req.setMatKhauDangNhap(rawPassword);
            //userMailService.sendClientNotificationMail(req, "CREATE");
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail khi tạo mới: " + e.getMessage());
        }
        return convertToResponse(savedKh);
    }

    @Transactional
    public KhachHangResponse update(Integer id, KhachHangRequest req, MultipartFile file) {
        KhachHang kh = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        if (nhanVienRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        if (repo.existsByEmailAndIdNot(req.getEmail(), id)) {
            throw new RuntimeException("Email này đã được sử dụng!");
        }
        if (repo.existsBySoDienThoaiAndIdNot(req.getSoDienThoai(), id)) {
            throw new RuntimeException("Số điện thoại đã tồn tại trên hệ thống!");
        }
        String oldPasswordHash = kh.getMatKhauDangNhap();
        String oldUsername = kh.getTenDangNhap();
        mapRequestToEntity(req, kh);
        kh.setTenDangNhap(oldUsername);
        kh.setMatKhauDangNhap(oldPasswordHash);
        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        } else {
            String oldImage = req.getAnhDaiDien();
            if (oldImage != null && !oldImage.isEmpty()) {
                kh.setAnhDaiDien(oldImage.contains(",") ? oldImage.split(",")[0] : oldImage);
            }
        }

        if (req.getDanhSachDiaChi() != null) {
            diaChiRepo.deleteByKhachHang(kh);
            diaChiRepo.flush();

            if (kh.getDanhSachDiaChi() == null) {
                kh.setDanhSachDiaChi(new ArrayList<>());
            } else {
                kh.getDanhSachDiaChi().clear();
            }

            for (DiaChiRequest dcReq : req.getDanhSachDiaChi()) {
                System.out.println("Tinh: " + dcReq.getTenTinhThanh());
                System.out.println("Huyen: " + dcReq.getTenQuanHuyen());
                System.out.println("Xa: " + dcReq.getTenPhuongXa());
                DiaChiKhachHang dc = new DiaChiKhachHang();
                dc.setIdTinhThanh(dcReq.getIdTinhThanh());
                dc.setIdQuanHuyen(dcReq.getIdQuanHuyen());
                dc.setIdPhuongXa(dcReq.getIdPhuongXa());
                dc.setTenTinhThanh(dcReq.getTenTinhThanh());
                dc.setTenQuanHuyen(dcReq.getTenQuanHuyen());
                dc.setTenPhuongXa(dcReq.getTenPhuongXa());
                dc.setDiaChiChiTiet(dcReq.getDiaChiChiTiet());
                dc.setHoTenNhan(dcReq.getHoTenNhan());
                dc.setSoDienThoaiNhan(dcReq.getSoDienThoaiNhan());
                dc.setLaMacDinh(dcReq.getLaMacDinh());
                dc.setKhachHang(kh);
                kh.getDanhSachDiaChi().add(dc);
            }
        }
        KhachHang savedKh = repo.save(kh);
        try {
            req.setMatKhauDangNhap("******** (Đã bảo mật)");
            //userMailService.sendClientNotificationMail(req, "UPDATE");
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail update: " + e.getMessage());
        }
        return convertToResponse(savedKh);
    }


    @Transactional
    public KhachHangResponse toggleStatus(Integer id) {
        KhachHang kh = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));
        int newStatus = (kh.getTrangThai() != null && kh.getTrangThai() == 1) ? 0 : 1;
        kh.setTrangThai(newStatus);
        KhachHang updatedKh = repo.saveAndFlush(kh);
        return convertToResponse(updatedKh);
    }

    private String saveFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage());
        }
    }
    private void saveDanhSachDiaChi(List<DiaChiRequest> danhSachRequest, KhachHang kh) {
        for (DiaChiRequest dcReq : danhSachRequest) {
            System.out.println("===== DEBUG DIA CHI =====");
            System.out.println("tenTinhThanh = " + dcReq.getTenTinhThanh());
            System.out.println("tenQuanHuyen = " + dcReq.getTenQuanHuyen());
            System.out.println("tenPhuongXa = " + dcReq.getTenPhuongXa());
            DiaChiKhachHang dcEntity = new DiaChiKhachHang();

            // Map dữ liệu từ Request sang Entity
            dcEntity.setIdTinhThanh(dcReq.getIdTinhThanh());
            dcEntity.setIdQuanHuyen(dcReq.getIdQuanHuyen());
            dcEntity.setIdPhuongXa(dcReq.getIdPhuongXa());
            dcEntity.setTenTinhThanh(dcReq.getTenTinhThanh());
            dcEntity.setTenQuanHuyen(dcReq.getTenQuanHuyen());
            dcEntity.setTenPhuongXa(dcReq.getTenPhuongXa());
            dcEntity.setDiaChiChiTiet(dcReq.getDiaChiChiTiet());
            dcEntity.setHoTenNhan(dcReq.getHoTenNhan());
            dcEntity.setSoDienThoaiNhan(dcReq.getSoDienThoaiNhan());
            dcEntity.setLaMacDinh(dcReq.getLaMacDinh());

            // Tạo chuỗi thông tin tổng hợp để hiển thị (tránh bị NULL cột thong_tin_dia_chi)
            String fullText = String.format("%s, %s, %s, %s",
                    dcReq.getDiaChiChiTiet(), dcReq.getIdPhuongXa(),
                    dcReq.getIdQuanHuyen(), dcReq.getIdTinhThanh());
            dcEntity.setDiaChiChiTiet(fullText);

            dcEntity.setKhachHang(kh); // Quan trọng: Liên kết với khách hàng vừa tạo/cập nhật
            diaChiRepo.save(dcEntity);
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
            entity.setDiaChiChiTiet(dto.getDiaChiChiTiet());
            entity.setLaMacDinh(Boolean.TRUE.equals(dto.getLaMacDinh()));

            // SỬA TẠI ĐÂY: Ưu tiên lấy từ DTO, nếu DTO trống mới lấy theo khách hàng
            entity.setHoTenNhan(dto.getHoTenNhan() != null ? dto.getHoTenNhan() : kh.getTenKhachHang());
            entity.setSoDienThoaiNhan(dto.getSoDienThoaiNhan() != null ? dto.getSoDienThoaiNhan() : kh.getSoDienThoai());
        }
    }

    public ResponseEntity<Resource> exportExcel(String keyword, Integer trangThai,Boolean gioiTinh, LocalDate tuNgay, List<Integer> listId) throws IOException {
        List<KhachHang> list;
        try {
            if (listId != null && !listId.isEmpty()) {
                list = repo.findAllById(listId);
            } else {
                list = repo.searchKhachHang(keyword, trangThai, gioiTinh, tuNgay, PageRequest.of(0, 10000)).getContent();
            }
        } catch (Exception e) {
            System.err.println("🔥 Lỗi truy vấn DB khi xuất Excel: " + e.getMessage());
            throw e;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String[] columns = {"STT", "Mã KH", "Tên KH", "SĐT", "Email", "Giới tính", "Ngày sinh", "Điểm", "Ngày tạo", "Trạng thái"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Khách hàng");
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

                String gioiTinhLabel = (kh.getGioiTinh() != null) ? (kh.getGioiTinh() ? "Nam" : "Nữ") : "N/A";
                row.createCell(5).setCellValue(gioiTinhLabel);

                row.createCell(6).setCellValue(kh.getNgaySinh() != null ? kh.getNgaySinh().format(formatter) : "");
                row.createCell(7).setCellValue(kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0);
                row.createCell(8).setCellValue(kh.getNgayTaoTaiKhoan() != null ? kh.getNgayTaoTaiKhoan().toString() : "");

                String tt = (kh.getTrangThai() != null && kh.getTrangThai() == 1) ? "Hoạt động" : "Ngừng hoạt động";
                row.createCell(9).setCellValue(tt);
            }
            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=DS_KhachHang.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            System.err.println("Lỗi tạo file Excel: " + e.getMessage());
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
        res.setAnhDaiDien(kh.getAnhDaiDien());

        if (kh.getDanhSachDiaChi() != null && !kh.getDanhSachDiaChi().isEmpty()) {
            List<DiaChiResponse> listDiaChiDto = kh.getDanhSachDiaChi().stream().map(dc -> {
                DiaChiResponse dto = new DiaChiResponse();
                BeanUtils.copyProperties(dc, dto);
                dto.setId(dc.getId()); // Đảm bảo ID được giữ lại để update
                return dto;
            }).collect(Collectors.toList());
            res.setDanhSachDiaChi(listDiaChiDto);
            DiaChiKhachHang dcMacDinh = kh.getDanhSachDiaChi().stream()
                    .filter(dc -> Boolean.TRUE.equals(dc.getLaMacDinh()))
                    .findFirst()
                    .orElse(kh.getDanhSachDiaChi().get(0));

            String fullAddress = Stream.of(
                            dcMacDinh.getDiaChiChiTiet(),
                            dcMacDinh.getTenPhuongXa(),
                            dcMacDinh.getTenQuanHuyen(),
                            dcMacDinh.getTenTinhThanh()
                    ).filter(Objects::nonNull)
                    .collect(Collectors.joining(", "));

            res.setDiaChi(fullAddress);
        } else {
            res.setDanhSachDiaChi(new ArrayList<>());
        }
        return res;
    }

    @Transactional
    public KhachHangResponse updateMyProfile(String currentEmail, KhachHangProfileRequest req, MultipartFile file) {
        KhachHang kh = repo.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin tài khoản với email: " + currentEmail));
        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(currentEmail)) {
            if (checkDuplicate("email", req.getEmail(), kh.getId())) {
                throw new RuntimeException("Email mới này đã được sử dụng bởi một tài khoản khác!");
            }
            kh.setEmail(req.getEmail());
        }
        if (checkDuplicate("soDienThoai", req.getSoDienThoai(), kh.getId())) {
            throw new RuntimeException("Số điện thoại này đã tồn tại trên hệ thống!");
        }
        if (checkDuplicate("tenDangNhap", req.getTenDangNhap(), kh.getId())) {
            throw new RuntimeException("Tên đăng nhập này đã được sử dụng!");
        }
        kh.setTenKhachHang(req.getTenKhachHang());
        kh.setSoDienThoai(req.getSoDienThoai());
        kh.setNgaySinh(req.getNgaySinh());
        kh.setGioiTinh(req.getGioiTinh());
        kh.setTenDangNhap(req.getTenDangNhap());

        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        }
        if (req.getDanhSachDiaChi() != null) {
            diaChiRepo.deleteByKhachHang(kh);
            diaChiRepo.flush();
            kh.getDanhSachDiaChi().clear();
            for (DiaChiRequest dcReq : req.getDanhSachDiaChi()) {
                DiaChiKhachHang dc = new DiaChiKhachHang();
                dc.setIdTinhThanh(dcReq.getIdTinhThanh());
                dc.setIdQuanHuyen(dcReq.getIdQuanHuyen());
                dc.setIdPhuongXa(dcReq.getIdPhuongXa());
                dc.setTenTinhThanh(dcReq.getTenTinhThanh());
                dc.setTenQuanHuyen(dcReq.getTenQuanHuyen());
                dc.setTenPhuongXa(dcReq.getTenPhuongXa());
                dc.setDiaChiChiTiet(dcReq.getDiaChiChiTiet());
                dc.setHoTenNhan(dcReq.getHoTenNhan());
                dc.setSoDienThoaiNhan(dcReq.getSoDienThoaiNhan());
                dc.setLaMacDinh(dcReq.getLaMacDinh());
                dc.setKhachHang(kh);
                kh.getDanhSachDiaChi().add(dc);
            }
        }
        KhachHang savedKh = repo.save(kh);
        return convertToResponse(savedKh);
    }

    public KhachHangResponse getProfileByEmail(String email) {
        KhachHang kh = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));
        return convertToResponse(kh); // Tận dụng hàm convertToResponse bạn đã viết rất tốt ở trên
    }
}