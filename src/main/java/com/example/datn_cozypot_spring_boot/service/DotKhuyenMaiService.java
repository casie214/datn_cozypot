package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.DotKhuyenMaiDTO;
import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import com.example.datn_cozypot_spring_boot.entity.MonAnDiKem;
import com.example.datn_cozypot_spring_boot.entity.SetLau;
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepository;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.MonAnRepository;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.SetLauRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.OutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
// Apache POI - Excel
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// Java IO
import java.io.OutputStream;

// Java Time
import java.time.LocalDate;

// Java Util
import java.util.List;

@Service
public class DotKhuyenMaiService {
    @Autowired
    private DotKhuyenMaiRepository dotKhuyenMaiRepo;

    @Autowired
    private SetLauRepository setLauRepo; // Inject thêm repository này

    @Autowired
    private MonAnRepository monAnRepo;

    // Bổ sung phương thức này nếu chưa có hoặc đang sai tên
    public List<DotKhuyenMaiDTO> getAll() {
        return dotKhuyenMaiRepo.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Bổ sung thêm getById để phục vụ API lấy chi tiết
    public DotKhuyenMaiDTO getById(Integer id) {
        DotKhuyenMai entity = dotKhuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt khuyến mãi id: " + id));
        return convertToDto(entity);
    }

    @Transactional
    public DotKhuyenMaiDTO create(DotKhuyenMaiDTO dto) {
        if (dto.getNgayBatDau() == null || dto.getNgayKetThuc() == null) {
            throw new RuntimeException("Phải nhập ngày bắt đầu và kết thúc");
        }

        if (dto.getNgayBatDau().isAfter(dto.getNgayKetThuc())) {
            throw new RuntimeException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        // ===== CHECK TRÙNG TRƯỚC =====
        // CHECK MÓN ĂN
        if (dto.getIdMonAnChiTiet() != null && !dto.getIdMonAnChiTiet().isEmpty()) {

            for (Integer idMon : dto.getIdMonAnChiTiet()) {

                List<DotKhuyenMai> overlaps =
                        dotKhuyenMaiRepo.checkOverlapByMonAn(
                                idMon,
                                dto.getNgayBatDau(),
                                dto.getNgayKetThuc()
                        );

                if (!overlaps.isEmpty()) {
                    throw new RuntimeException(
                            "Món ăn đã có khuyến mãi trong thời gian này!"
                    );
                }
            }
        }

        // CHECK SET LẨU
        if (dto.getIdSetLauChiTiet() != null && !dto.getIdSetLauChiTiet().isEmpty()) {

            for (Integer idSet : dto.getIdSetLauChiTiet()) {

                List<DotKhuyenMai> overlaps =
                        dotKhuyenMaiRepo.checkOverlapBySet(
                                idSet,
                                dto.getNgayBatDau(),
                                dto.getNgayKetThuc()
                        );

                if (!overlaps.isEmpty()) {
                    throw new RuntimeException(
                            "Set lẩu đã có khuyến mãi trong thời gian này!"
                    );
                }
            }
        }



        // ===== TẠO MỚI =====
        DotKhuyenMai entity = new DotKhuyenMai();
        mapDtoToEntity(dto, entity);

        if (dto.getIdSetLauChiTiet() != null && !dto.getIdSetLauChiTiet().isEmpty()) {
            List<SetLau> selectedSets = setLauRepo.findAllById(dto.getIdSetLauChiTiet());
            entity.setSetLaus(new HashSet<>(selectedSets));
        }

        if (dto.getIdMonAnChiTiet() != null) {
            List<MonAnDiKem> selectedMons =
                    monAnRepo.findAllById(dto.getIdMonAnChiTiet());

            entity.setMonAnDiKems(new HashSet<>(selectedMons));
        }

        entity.setNgayTao(Instant.now());
        entity.setNguoiTao("Admin");

        return convertToDto(dotKhuyenMaiRepo.save(entity));
    }


    public void exportExcel(OutputStream outputStream) {
        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Danh sách đợt khuyến mãi");

            // ===== STYLE HEADER =====
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // ===== HEADER =====
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "STT",
                    "Mã KM",
                    "Tên đợt khuyến mãi",
                    "Giảm (%)",
                    "Ngày bắt đầu",
                    "Ngày kết thúc",
                    "Trạng thái"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // ===== DATA =====
            List<DotKhuyenMai> list = dotKhuyenMaiRepo.findAll();

            int rowIndex = 1;
            int stt = 1;
            LocalDate today = LocalDate.now();

            for (DotKhuyenMai km : list) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(km.getMaDotKhuyenMai());
                row.createCell(2).setCellValue(km.getTenDotKhuyenMai());
                row.createCell(3).setCellValue(km.getPhanTramGiam());

                row.createCell(4).setCellValue(
                        km.getNgayBatDau() != null ? km.getNgayBatDau().toString() : ""
                );
                row.createCell(5).setCellValue(
                        km.getNgayKetThuc() != null ? km.getNgayKetThuc().toString() : ""
                );

                String trangThai;
                if (km.getTrangThai() == 0) {
                    trangThai = "Ngừng hoạt động";
                } else if (km.getNgayKetThuc() != null && km.getNgayKetThuc().isBefore(today)) {
                    trangThai = "Đã hết hạn";
                } else {
                    trangThai = "Đang hoạt động";
                }

                row.createCell(6).setCellValue(trangThai);
            }

            // ===== AUTO SIZE =====
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xuất file Excel đợt khuyến mãi", e);
        }
    }


    @Transactional
    public DotKhuyenMaiDTO update(Integer id, DotKhuyenMaiDTO dto) {

        DotKhuyenMai entity = dotKhuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt khuyến mãi"));

        // Nếu FE không gửi ngày → giữ nguyên
        if (dto.getNgayBatDau() == null) {
            dto.setNgayBatDau(entity.getNgayBatDau());
        }

        if (dto.getNgayKetThuc() == null) {
            dto.setNgayKetThuc(entity.getNgayKetThuc());
        }

        if (dto.getNgayBatDau().isAfter(dto.getNgayKetThuc())) {
            throw new RuntimeException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        // CHECK MÓN ĂN
        if (dto.getIdMonAnChiTiet() != null && !dto.getIdMonAnChiTiet().isEmpty()) {

            for (Integer idMon : dto.getIdMonAnChiTiet()) {

                List<DotKhuyenMai> overlaps =
                        dotKhuyenMaiRepo.checkOverlapByMonAn(
                                idMon,
                                dto.getNgayBatDau(),
                                dto.getNgayKetThuc()
                        );
                overlaps.removeIf(km -> km.getId().equals(id));


                if (!overlaps.isEmpty()) {
                    throw new RuntimeException(
                            "Món ăn đã có khuyến mãi trong thời gian này!"
                    );
                }
            }
        }


        // CHECK SET LẨU
        if (dto.getIdSetLauChiTiet() != null && !dto.getIdSetLauChiTiet().isEmpty()) {

            for (Integer idSet : dto.getIdSetLauChiTiet()) {

                List<DotKhuyenMai> overlaps =
                        dotKhuyenMaiRepo.checkOverlapBySet(
                                idSet,
                                dto.getNgayBatDau(),
                                dto.getNgayKetThuc()
                        );

                if (!overlaps.isEmpty()) {
                    throw new RuntimeException(
                            "Set lẩu đã có khuyến mãi trong thời gian này!"
                    );
                }
            }
        }


        // ===== UPDATE DATA =====
        mapDtoToEntity(dto, entity);

        if (dto.getIdSetLauChiTiet() != null) {
            List<SetLau> selectedSets =
                    setLauRepo.findAllById(dto.getIdSetLauChiTiet());

            entity.getSetLaus().clear();
            entity.getSetLaus().addAll(selectedSets);
        }

        if (dto.getIdMonAnChiTiet() != null) {
            List<MonAnDiKem> selectedMons =
                    monAnRepo.findAllById(dto.getIdMonAnChiTiet());

            entity.setMonAnDiKems(new HashSet<>(selectedMons));
        }

        entity.setNgaySua(Instant.now());
        entity.setNguoiSua("Admin");

        return convertToDto(dotKhuyenMaiRepo.save(entity));
    }


    // Trong DotKhuyenMaiService.java
    public DotKhuyenMaiDTO convertToDto(DotKhuyenMai entity) {
        if (entity == null) return null;

        DotKhuyenMaiDTO dto = new DotKhuyenMaiDTO();

        // GÁN DỮ LIỆU CƠ BẢN (Đây là phần bạn đang thiếu)
        dto.setId(entity.getId());
        dto.setMaDotKhuyenMai(entity.getMaDotKhuyenMai());
        dto.setTenDotKhuyenMai(entity.getTenDotKhuyenMai());
        dto.setMoTa(entity.getMoTa());
        dto.setPhanTramGiam(entity.getPhanTramGiam());
        dto.setNgayBatDau(entity.getNgayBatDau());
        dto.setNgayKetThuc(entity.getNgayKetThuc());
        dto.setTrangThai(entity.getTrangThai());
        dto.setNgayTao(entity.getNgayTao());
        dto.setNgaySua(entity.getNgaySua());
        dto.setNguoiTao(entity.getNguoiTao());
        dto.setNguoiSua(entity.getNguoiSua());

        // Đây là cách làm đúng để tránh vòng lặp:
        if (entity.getSetLaus() != null) {
            dto.setIdSetLauChiTiet(entity.getSetLaus().stream()
                    .map(SetLau::getId)
                    .collect(Collectors.toList()));
        }

        // Xử lý danh sách ID Món Ăn Đi Kèm
        if (entity.getMonAnDiKems() != null) {
            dto.setIdMonAnChiTiet(entity.getMonAnDiKems().stream()
                    .map(MonAnDiKem::getId)
                    .collect(Collectors.toList()));
        } else {
            dto.setIdMonAnChiTiet(new ArrayList<>()); // Trả về mảng rỗng [] thay vì null
        }

        return dto;
    }

    private void mapDtoToEntity(DotKhuyenMaiDTO dto, DotKhuyenMai entity) {
        // 1. Thông tin cơ bản
        entity.setTenDotKhuyenMai(dto.getTenDotKhuyenMai());
        entity.setMoTa(dto.getMoTa());

        // 2. Giá trị giảm giá (Check null để tránh NullPointerException)
        entity.setPhanTramGiam(dto.getPhanTramGiam() != null ? dto.getPhanTramGiam() : 0);

        // 3. Thời gian áp dụng (Khớp LocalDate giữa DTO và Entity)
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());

        // 4. Trạng thái
        // Nếu status từ giao diện gửi về null, mặc định để là 1 (Đang hoạt động)
        if (dto.getTrangThai() != null) {
            entity.setTrangThai(dto.getTrangThai());
        }
    }

    // Trong DotKhuyenMaiService.java

    public Page<DotKhuyenMaiDTO> search(String keyword, Integer status, LocalDate ngayBD, LocalDate ngayKT, Pageable pageable) {
        String searchKey = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        // Gọi Repository với các tham số ngày tháng
        Page<DotKhuyenMai> pageEntity = dotKhuyenMaiRepo.searchFilter(searchKey, status, ngayBD, ngayKT, pageable);

        return pageEntity.map(this::convertToDto);
    }

    public List<DotKhuyenMai> getDotDangHoatDong() {
        return dotKhuyenMaiRepo.findDotDangHoatDong();
    }

    @Transactional
    public void toggleStatus(Integer id) {

        DotKhuyenMai km = dotKhuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));

        // Nếu hết hạn → không cho bật
        if (km.getNgayKetThuc().isBefore(LocalDate.now())) {
            throw new RuntimeException("Khuyến mãi đã hết hạn");
        }

        // Đảo trạng thái
        km.setTrangThai(km.getTrangThai() == 1 ? 0 : 1);

        dotKhuyenMaiRepo.save(km);
    }

}