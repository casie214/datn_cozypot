package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.NhanVienRequest;
import com.example.datn_cozypot_spring_boot.dto.NhanVienResponse;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.entity.VaiTro;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.example.datn_cozypot_spring_boot.repository.VaiTroRepository;
import com.example.datn_cozypot_spring_boot.service.userEmailService.UserMailService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// --- EXCEL IMPORTS ---
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// --- PDF IMPORTS ---
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.stream.Collectors;

@Service
public class NhanVienService {

    @Autowired
    private NhanVienRepository repo;

    @Autowired
    private VaiTroRepository vaiTroRepo;

    @Autowired
    private UserMailService userMailService;

    private final Path storageFolder = Paths.get("uploads/images");

    public NhanVienService() {
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ ảnh!", e);
        }
    }

    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path destinationFile = storageFolder.resolve(fileName).normalize().toAbsolutePath();
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
        }
    }

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

    public Page<NhanVienResponse> getAll(String keyword, Integer trangThai, Boolean gioiTinh, LocalDate tuNgay, int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("trangThaiLamViec"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.searchNhanVien(keyword, trangThai,gioiTinh, tuNgay, pageable).map(this::convertToResponse);
    }

    public NhanVienResponse getDetail(Integer id) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên id: " + id));
        return convertToResponse(nv);
    }

    @Transactional
    public NhanVienResponse create(NhanVienRequest req, MultipartFile file) {
        // 1. Kiểm tra trùng lặp
        if (repo.existsByTenDangNhap(req.getTenDangNhap())) throw new RuntimeException("Tên đăng nhập đã tồn tại");
        if (repo.existsByEmail(req.getEmail())) throw new RuntimeException("Email đã tồn tại");

        // 2. Map dữ liệu và xử lý ảnh
        NhanVien nv = new NhanVien();
        mapRequestToEntity(req, nv);
        if (file != null && !file.isEmpty()) {
            nv.setAnhDaiDien(saveImage(file));
        }

        // 3. Lưu vào Database
        NhanVien savedNv = repo.save(nv);
        NhanVienResponse response = convertToResponse(savedNv);

        // 4. Gửi mail thông báo (Đặt trong try-catch để nếu lỗi mail thì vẫn không mất dữ liệu DB)
        try {
            userMailService.sendStaffNotificationMail(req, "CREATE");
        } catch (Exception e) {
            // Chỉ log lỗi ra console, không chặn luồng trả về của khách hàng
            System.err.println("Lỗi gửi email khi tạo mới: " + e.getMessage());
        }

        return response;
    }

    @Transactional
    public NhanVienResponse update(Integer id, NhanVienRequest req, MultipartFile file) {
        // 1. Kiểm tra tồn tại và trùng lặp
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        if (repo.existsByTenDangNhapAndIdNot(req.getTenDangNhap(), id))
            throw new RuntimeException("Tên đăng nhập bị trùng");
        if (repo.existsByEmailAndIdNot(req.getEmail(), id)) throw new RuntimeException("Email đã bị trùng");

        // 2. Map dữ liệu mới và xử lý ảnh (nếu có ảnh mới)
        mapRequestToEntity(req, nv);
        if (file != null && !file.isEmpty()) {
            nv.setAnhDaiDien(saveImage(file));
        }

        // 3. Cập nhật vào Database
        NhanVien updatedNv = repo.save(nv);
        NhanVienResponse response = convertToResponse(updatedNv);

        // 4. Gửi mail thông báo cập nhật
        try {
            userMailService.sendStaffNotificationMail(req, "UPDATE");
        } catch (Exception e) {
            System.err.println("Lỗi gửi email khi cập nhật: " + e.getMessage());
        }

        return response;
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
        if (req.getMatKhauDangNhap() != null && !req.getMatKhauDangNhap().isEmpty()) {
            nv.setMatKhauDangNhap(req.getMatKhauDangNhap());
        }
        if (req.getIdVaiTro() != null) {
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

    public ResponseEntity<Resource> exportExcel(String keyword, Integer trangThai, Boolean gioiTinh, LocalDate tuNgay, List<Integer> listId) throws IOException {
        List<NhanVien> list;
        if (listId != null && !listId.isEmpty()) {
            list = repo.findAllById(listId);
        } else {
            list = repo.searchNhanVien(keyword, trangThai, gioiTinh, tuNgay, Pageable.unpaged()).getContent();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String[] columns = {"STT", "Mã NV", "Họ Tên", "SĐT", "Email", "Giới Tính", "Ngày Sinh", "Số CCCD", "Ngày Cấp", "Nơi Cấp", "Địa Chỉ", "Vai Trò", "Ngày Vào Làm", "Tên Đăng Nhập", "Trạng Thái"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Nhân Viên");
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (NhanVien nv : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(nv.getMaNhanVien() != null ? nv.getMaNhanVien() : "");
                row.createCell(2).setCellValue(nv.getHoTenNhanVien() != null ? nv.getHoTenNhanVien() : "");
                row.createCell(3).setCellValue(nv.getSdtNhanVien() != null ? nv.getSdtNhanVien() : "");
                row.createCell(4).setCellValue(nv.getEmail() != null ? nv.getEmail() : "");
                row.createCell(5).setCellValue(nv.getGioiTinh() != null ? (nv.getGioiTinh() ? "Nam" : "Nữ") : "");
                row.createCell(6).setCellValue(nv.getNgaySinh() != null ? nv.getNgaySinh().format(formatter) : "");

                String rawCccd = nv.getSoCccd();
                String displayCccd = (rawCccd != null && rawCccd.length() > 6)
                        ? rawCccd.substring(0, 3) + "******" + rawCccd.substring(rawCccd.length() - 3)
                        : (rawCccd != null ? "******" : "");
                row.createCell(7).setCellValue(displayCccd);

                row.createCell(8).setCellValue(nv.getNgayCapCccd() != null ? nv.getNgayCapCccd().format(formatter) : "");
                row.createCell(9).setCellValue(nv.getNoiCapCccd() != null ? nv.getNoiCapCccd() : "");
                row.createCell(10).setCellValue(nv.getDiaChi() != null ? nv.getDiaChi() : "");
                row.createCell(11).setCellValue(nv.getIdVaiTro() != null ? nv.getIdVaiTro().getTenVaiTro() : "N/A");
                row.createCell(12).setCellValue(nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(formatter) : "");
                row.createCell(13).setCellValue(nv.getTenDangNhap() != null ? nv.getTenDangNhap() : "");
                row.createCell(14).setCellValue(nv.getTrangThaiLamViec() != null ? (nv.getTrangThaiLamViec() == 1 ? "Đang làm việc" : "Ngừng hoạt động") : "");
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Danh_Sach_Nhan_Vien.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    // Sửa khai báo hàm: thêm String listId
    public ResponseEntity<Resource> exportTemplate(String listId) throws IOException {

        // 1. Logic lấy dữ liệu: Có chọn thì lọc, không chọn thì lấy hết (hoặc lấy rỗng)
        List<NhanVien> listData;

        if (listId != null && !listId.trim().isEmpty()) {
            // Chuyển chuỗi "1,2,3" từ Frontend thành List<Integer> [1, 2, 3]
            List<Integer> ids = Arrays.stream(listId.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            // Lấy đúng những người đã tích chọn
            listData = repo.findAllById(ids);
        } else {
            // Nếu không truyền listId (bấm tải mẫu trống), bạn có thể lấy tất cả hoặc tạo list rỗng
            listData = repo.findAll();
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String[] columns = {
                "Họ và Tên", "Vai Trò", "SĐT", "Email", "Ngày Sinh",
                "Giới Tính (Nam/Nữ)", "Số CCCD", "Ngày Cấp", "Nơi Cấp",
                "Ngày Vào Làm", "Địa Chỉ Thường Trú"
        };

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Danh Sách Nhân Viên");

            // --- Style cho tiêu đề (Giữ nguyên của bạn) ---
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // --- 2. Đổ dữ liệu từ danh sách ĐÃ LỌC (listData) ---
            int rowIdx = 1;
            for (NhanVien nv : listData) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(nv.getHoTenNhanVien() != null ? nv.getHoTenNhanVien() : "");
                row.createCell(1).setCellValue(nv.getIdVaiTro() != null ? nv.getIdVaiTro().getTenVaiTro() : "");
                row.createCell(2).setCellValue(nv.getSdtNhanVien() != null ? nv.getSdtNhanVien() : "");
                row.createCell(3).setCellValue(nv.getEmail() != null ? nv.getEmail() : "");
                row.createCell(4).setCellValue(nv.getNgaySinh() != null ? nv.getNgaySinh().format(formatter) : "");
                row.createCell(5).setCellValue(nv.getGioiTinh() != null ? (nv.getGioiTinh() ? "Nam" : "Nữ") : "");
                row.createCell(6).setCellValue(nv.getSoCccd() != null ? nv.getSoCccd() : "");
                row.createCell(7).setCellValue(nv.getNgayCapCccd() != null ? nv.getNgayCapCccd().format(formatter) : "");
                row.createCell(8).setCellValue(nv.getNoiCapCccd() != null ? nv.getNoiCapCccd() : "");
                row.createCell(9).setCellValue(nv.getNgayVaoLam() != null ? nv.getNgayVaoLam().format(formatter) : "");
                row.createCell(10).setCellValue(nv.getDiaChi() != null ? nv.getDiaChi() : "");
            }

            for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);

            workbook.write(out);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Mau_Import_Nhan_Vien.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(new ByteArrayInputStream(out.toByteArray())));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void importExcel(MultipartFile file) throws Exception {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        StringBuilder errorMessages = new StringBuilder();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;

            String hoTen = getCellValue(row.getCell(0));
            String sdt = getCellValue(row.getCell(2));
            String email = getCellValue(row.getCell(3));
            String cccd = getCellValue(row.getCell(6));

            // --- KIỂM TRA TRÙNG DỮ LIỆU ---
            if (repo.existsByEmail(email)) {
                errorMessages.append("Dòng ").append(i + 1).append(": Email '").append(email).append("' đã tồn tại.\n");
            }
            if (repo.existsBySdtNhanVien(sdt)) {
                errorMessages.append("Dòng ").append(i + 1).append(": Số điện thoại '").append(sdt).append("' đã tồn tại.\n");
            }
            if (repo.existsBySoCccd(cccd)) {
                errorMessages.append("Dòng ").append(i + 1).append(": Số CCCD '").append(cccd).append("' đã tồn tại.\n");
            }

            // Nếu đã có lỗi ở các dòng trước hoặc dòng này, không cần tạo Object nữa, chỉ quét tiếp để tìm lỗi
            if (errorMessages.length() > 0) continue;

            // --- NẾU KHÔNG CÓ LỖI THÌ TẠO ĐỐI TƯỢNG ---
            NhanVien nv = new NhanVien();
            nv.setHoTenNhanVien(hoTen);

            // Vai trò
            String tenVT = getCellValue(row.getCell(1));
            VaiTro vt = vaiTroRepo.findByTenVaiTro(tenVT);
            nv.setIdVaiTro(vt);

            nv.setSdtNhanVien(sdt);
            nv.setEmail(email);

            String ns = getCellValue(row.getCell(4));
            if(!ns.isEmpty()) nv.setNgaySinh(LocalDate.parse(ns, formatter));

            nv.setGioiTinh("Nam".equalsIgnoreCase(getCellValue(row.getCell(5))));
            nv.setSoCccd(cccd);

            String nc = getCellValue(row.getCell(7));
            if(!nc.isEmpty()) nv.setNgayCapCccd(LocalDate.parse(nc, formatter));

            nv.setNoiCapCccd(getCellValue(row.getCell(8)));

            String nvl = getCellValue(row.getCell(9));
            if(!nvl.isEmpty()) nv.setNgayVaoLam(LocalDate.parse(nvl, formatter));

            nv.setDiaChi(getCellValue(row.getCell(10)));

            // Mặc định
            nv.setTrangThaiLamViec(1);
            nv.setTenDangNhap(email);
            nv.setMatKhauDangNhap("$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqRzVUX5aZ2qz9VvD.S9.f8qSj62"); // 123456

            repo.save(nv);
        }

        workbook.close();

        // Nếu có bất kỳ lỗi nào, ném ra ngoại lệ để Rollback dữ liệu
        if (errorMessages.length() > 0) {
            throw new RuntimeException("Dữ liệu không hợp lệ:\n" + errorMessages.toString());
        }
    }

    // Hàm đọc dữ liệu từ Cell bất kể định dạng String hay Number
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    // Riêng ngày tháng thì vẫn nên format thủ công để đảm bảo đúng dd/MM/yyyy
                    return new java.text.SimpleDateFormat("dd/MM/yyyy").format(cell.getDateCellValue());
                }
                // Các số khác (SĐT, CCCD) dùng formatter này sẽ không bị 1.23E+9
                return formatter.formatCellValue(cell).replaceAll("\\s+", "");
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Nếu ô là công thức, lấy giá trị kết quả sau khi tính toán
                try {
                    return formatter.formatCellValue(cell);
                } catch (Exception e) {
                    return "";
                }
            default:
                return "";
        }
    }
    // Hàm kiểm tra xem dòng đó có thực sự chứa dữ liệu không
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }

    public byte[] generatePdf(List<Integer> listId) throws IOException {
        List<NhanVien> list = repo.findAllById(listId);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // 1. Tùy chỉnh Font (Nếu đã nhúng Arial thì dùng, không thì dùng mặc định Bold)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // 2. Tiêu đề chính (Header)
        document.add(new Paragraph("DANH SÁCH NHÂN VIÊN HỆ THỐNG COZYPOT")
                .setBold()
                .setFontSize(18)
                .setFontColor(ColorConstants.RED) // Tạo điểm nhấn cho thương hiệu
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Ngày xuất báo cáo: " + LocalDate.now().format(formatter))
                .setItalic()
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // 3. Thiết lập bảng chuyên nghiệp
        float[] columnWidths = {30f, 70f, 140f, 90f, 100f, 80f, 85f};
        Table table = new Table(columnWidths);
        table.setWidth(UnitValue.createPercentValue(100));

        // 4. Định dạng Header của bảng (Xanh than, chữ trắng)
        String[] headers = {"STT", "Mã NV", "Họ Và Tên", "Số ĐT", "Vai Trò", "Vào Làm", "Trạng Thái"};
        for (String headerTitle : headers) {
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(headerTitle).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(ColorConstants.DARK_GRAY) // Màu nền tiêu đề
                    .setPadding(5)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // 5. Đổ dữ liệu và định dạng ô (Cell)
        int stt = 1;
        for (NhanVien nv : list) {
            // Màu nền xen kẽ cho dễ nhìn (Zebra Striping)
            com.itextpdf.kernel.colors.Color bgColor = (stt % 2 == 0) ? ColorConstants.LIGHT_GRAY : ColorConstants.WHITE;

            table.addCell(createStyledCell(String.valueOf(stt++), bgColor, TextAlignment.CENTER));
            table.addCell(createStyledCell(nv.getMaNhanVien(), bgColor, TextAlignment.LEFT));
            table.addCell(createStyledCell(nv.getHoTenNhanVien(), bgColor, TextAlignment.LEFT));
            table.addCell(createStyledCell(nv.getSdtNhanVien(), bgColor, TextAlignment.CENTER));

            String vaiTro = (nv.getIdVaiTro() != null) ? nv.getIdVaiTro().getTenVaiTro() : "N/A";
            table.addCell(createStyledCell(vaiTro, bgColor, TextAlignment.LEFT));

            String ngayVao = (nv.getNgayVaoLam() != null) ? nv.getNgayVaoLam().format(formatter) : "";
            table.addCell(createStyledCell(ngayVao, bgColor, TextAlignment.CENTER));

            String tt = (nv.getTrangThaiLamViec() != null && nv.getTrangThaiLamViec() == 1) ? "Đang làm" : "Đã nghỉ";
            table.addCell(createStyledCell(tt, bgColor, TextAlignment.CENTER));
        }

        document.add(table);

        // 6. Chữ ký phía dưới
        document.add(new Paragraph("\n\n"));
        Table footerTable = new Table(2).setWidth(UnitValue.createPercentValue(100));
        footerTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Người lập biểu\n(Ký tên)")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        footerTable.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph("Giám đốc\n(Đóng dấu)")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        document.add(footerTable);

        document.close();
        return out.toByteArray();
    }

    // Hàm phụ để tạo Cell có Style đồng nhất
    private com.itextpdf.layout.element.Cell createStyledCell(String text, com.itextpdf.kernel.colors.Color bgColor, TextAlignment alignment) {
        return new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text != null ? text : "").setFontSize(9))
                .setPadding(5)
                .setBackgroundColor(bgColor)
                .setTextAlignment(alignment);
    }

    public String decodeQRCode(MultipartFile file) {
        try {
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");

            // THÊM DÒNG NÀY: Chỉ định rõ định dạng là QR_CODE để máy đỡ tìm các loại mã vạch khác
            hints.put(DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE));

            Result result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }}