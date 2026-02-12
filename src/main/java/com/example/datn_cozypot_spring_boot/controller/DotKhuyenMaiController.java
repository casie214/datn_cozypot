package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.DotKhuyenMaiDTO;
import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import com.example.datn_cozypot_spring_boot.service.DotKhuyenMaiService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/dot-khuyen-mai")
public class DotKhuyenMaiController {
    @Autowired
    private DotKhuyenMaiService dotKhuyenMaiService;

    @GetMapping("/active")
    public ResponseEntity<List<DotKhuyenMaiDTO>> getActiveCampaigns() {
        List<DotKhuyenMai> entities = dotKhuyenMaiService.getDotDangHoatDong();

        // Convert toàn bộ list sang DTO trước khi trả về
        List<DotKhuyenMaiDTO> dtos = entities.stream()
                .map(dotKhuyenMaiService::convertToDto) // Sử dụng hàm convert của bạn
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/get-all")
    public ResponseEntity<List<DotKhuyenMaiDTO>> getAll() {
        return ResponseEntity.ok(dotKhuyenMaiService.getAll());
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@Valid @RequestBody DotKhuyenMaiDTO dto) {
        try {
            return ResponseEntity.ok(dotKhuyenMaiService.create(dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi thêm mới: " + e.getMessage());
        }
    }

    // Thêm vào trong DotKhuyenMaiController.java
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Integer id) {
        try {
            // Giả sử service của bạn đã có hàm getById hoặc findById
            DotKhuyenMaiDTO dto = dotKhuyenMaiService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Không tìm thấy đợt khuyến mãi id: " + id);
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id") Integer id,
            @Valid @RequestBody DotKhuyenMaiDTO dto
    ) {
        try {
            DotKhuyenMaiDTO result = dotKhuyenMaiService.update(id, dto);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    @GetMapping("/export-excel")
    public void exportExcel(HttpServletResponse response) {
        try {
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=dot_khuyen_mai.xlsx"
            );

            dotKhuyenMaiService.exportExcel(response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace(); // QUAN TRỌNG để thấy lỗi thật trong console
            throw new RuntimeException("Export Excel failed", e);
        }
    }



    @GetMapping("/search")
    public Page<DotKhuyenMaiDTO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate ngayBatDau,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate ngayKetThuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return dotKhuyenMaiService.search(
                keyword,
                trangThai,
                ngayBatDau,
                ngayKetThuc,
                pageable
        );
    }
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        dotKhuyenMaiService.toggleStatus(id);
        return ResponseEntity.ok("OK");
    }

}
