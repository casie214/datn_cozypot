package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.KhuyenMaiThongKeResponse;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaDTO;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaResponseDTO;
import com.example.datn_cozypot_spring_boot.dto.request.ApDungVoucherRequest;
import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import com.example.datn_cozypot_spring_boot.service.PhieuGiamGiaService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@CrossOrigin(origins = "http://localhost:5173")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService service;

    // ============= GET ALL (PHÂN TRANG + TÌM KIẾM) =============

    @GetMapping("/search")
    public ResponseEntity<?> searchVouchers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer doiTuong,
            @RequestParam(required = false) Integer loaiGiamGia,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String ngayBatDau,
            @RequestParam(required = false) String ngayKetThuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(service.getAll(
                keyword, doiTuong, loaiGiamGia, trangThai, ngayBatDau, ngayKetThuc, page, size
        ));
    }

    @GetMapping("/public")
    public ResponseEntity<?> getPublicVouchers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer loaiGiamGia,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        // Cố định doiTuong = 0 (Công khai) và trangThai = 1 (Đang hoạt động)
        Integer doiTuongCongKhai = 0;
        Integer trangThaiHoatDong = 1;

        // Tận dụng hàm getAll có sẵn nhưng truyền cứng tham số
        return ResponseEntity.ok(service.getAll(
                keyword,
                doiTuongCongKhai,
                loaiGiamGia,
                trangThaiHoatDong,
                null, null, // Không lọc ngày bắt đầu/kết thúc ở đây vì Service thường đã check HSD
                page,
                size
        ));
    }

    // ============= GET BY ID =============

    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(
                service.getById(id)
        );
    }

    // ============= CREATE =============

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PhieuGiamGiaDTO dto) {
        service.create(dto);
        return ResponseEntity.ok("Thêm phiếu giảm giá thành công");
    }

    @GetMapping("/thong-ke")
    public KhuyenMaiThongKeResponse thongKe() {
        return service.thongKeKhuyenMai();
    }   // ============= UPDATE =============

    @GetMapping("/export-excel")
    public void exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) Integer doiTuong,
            @RequestParam(required = false) Integer loaiGiamGia,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime ngayBatDau,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime ngayKetThuc,
            HttpServletResponse response
    ) {
        try {
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=danh_sach_phieu_giam_gia.xlsx"
            );

            service.exportExcel(
                    keyword, trangThai, doiTuong, loaiGiamGia,
                    ngayBatDau, ngayKetThuc,
                    response.getOutputStream()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Export Excel failed", e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestBody PhieuGiamGiaDTO dto
    ) {
        try {
            PhieuGiamGia updated = service.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi cập nhật: " + e.getMessage());
        }
    }

    // ============= DELETE =============

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.ok("Xóa thành công phiếu giảm giá ID: " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Lỗi khi xóa: " + e.getMessage());
        }
    }

    @PostMapping("/ap-dung")
    public ResponseEntity<?> apDungVoucher(@RequestBody ApDungVoucherRequest request) {
        try {
            // Gọi hàm logic đã viết ở Service
            service.apDungVoucher(request);

            // Trả về thành công
            Map<String, String> response = new HashMap<>();
            response.put("message", "Áp dụng mã giảm giá thành công");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Các lỗi nghiệp vụ (Vd: Chưa đủ tiền, mã hết hạn...)
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error); // Trả về HTTP 400

        } catch (Exception e) {
            // Lỗi code/server
            Map<String, String> error = new HashMap<>();
            error.put("message", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error); // Trả về HTTP 500
        }
    }

    @PostMapping("/huy-ap-dung/{idHoaDon}")
    public ResponseEntity<?> huyVoucher(@PathVariable Integer idHoaDon) {
        try {
            service.huyVoucher(idHoaDon);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Hủy mã giảm giá thành công");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Đã xảy ra lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}



