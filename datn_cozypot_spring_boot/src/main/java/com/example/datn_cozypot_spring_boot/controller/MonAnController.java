package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongRequest;
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongResponse;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;

import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;

import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.DanhMuc;
import com.example.datn_cozypot_spring_boot.entity.DinhLuong;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manage/food")
public class MonAnController {
    private final MonAnService monAnService;

    @GetMapping("/category")
    @Operation(summary = "Lấy danh sách danh mục")
    public ResponseEntity<List<DanhMucResponse>> getAllDanhMuc() {
        return ResponseEntity.ok(monAnService.getAllDanhMuc());
    }

    @PostMapping("/category")
    @Operation(summary = "Tạo danh mục mới")
    public ResponseEntity<DanhMucResponse> createDanhMuc(@RequestBody @Valid DanhMucRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createDanhMuc(request));
    }

    @PutMapping("/category/{id}")
    @Operation(summary = "Cập nhật danh mục")
    public ResponseEntity<DanhMucResponse> updateDanhMuc(@PathVariable Integer id, @RequestBody @Valid DanhMucRequest request) {
        return ResponseEntity.ok(monAnService.updateDanhMuc(id, request));
    }

    @DeleteMapping("/category/{id}")
    @Operation(summary = "Xóa/Ẩn danh mục")
    public ResponseEntity<Void> deleteDanhMuc(@PathVariable Integer id) {
        monAnService.deleteDanhMuc(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // 2. QUẢN LÝ ĐỊNH LƯỢNG (Unit/Size) - MỚI
    // ========================================================================

    @GetMapping("/unit")
    @Operation(summary = "Lấy tất cả định lượng mẫu")
    public ResponseEntity<List<DinhLuongResponse>> getAllDinhLuong() {
        return ResponseEntity.ok(monAnService.getAllDinhLuong());
    }

    @GetMapping("/unit/by-category/{idDanhMuc}")
    @Operation(summary = "Lấy định lượng theo danh mục", description = "Dùng cho ComboBox khi tạo món")
    public ResponseEntity<List<DinhLuongResponse>> getDinhLuongByDanhMuc(@PathVariable Integer idDanhMuc) {
        return ResponseEntity.ok(monAnService.getDinhLuongByDanhMuc(idDanhMuc));
    }

    // ========================================================================
    // 3. QUẢN LÝ MÓN ĂN (Product / DanhMucChiTiet)
    // Thay thế cho cả MonAn và MonAnChiTiet cũ
    // ========================================================================

    @GetMapping
    @Operation(summary = "Lấy danh sách món ăn", description = "Hiển thị tất cả món ăn (kèm thông tin định lượng)")
    public ResponseEntity<List<MonAnResponse>> getAllMonAn() {
        return ResponseEntity.ok(monAnService.getAllMonAn());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết món ăn")
    public ResponseEntity<MonAnResponse> getMonAnById(@PathVariable Integer id) {
        return ResponseEntity.ok(monAnService.getMonAnById(id));
    }

    @PostMapping
    @Operation(summary = "Tạo món ăn mới", description = "Chọn danh mục và định lượng để tạo món")
    public ResponseEntity<MonAnResponse> createMonAn(@RequestBody @Valid MonAnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createMonAn(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật món ăn")
    public ResponseEntity<MonAnResponse> updateMonAn(@PathVariable Integer id, @RequestBody @Valid MonAnRequest request) {
        return ResponseEntity.ok(monAnService.updateMonAn(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonAn(@PathVariable Integer id) {
        monAnService.deleteMonAn(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // 4. QUẢN LÝ LOẠI SET LẨU (Hotpot Type)
    // ========================================================================

    @GetMapping("/category/hotpotType")
    public ResponseEntity<List<LoaiLauResponse>> getAllLoaiSetLau() {
        return ResponseEntity.ok(monAnService.getAllLoaiSetLau());
    }

    @PostMapping("/category/hotpotType")
    public ResponseEntity<LoaiLauResponse> createLoaiSetLau(@RequestBody @Valid LoaiLauRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createLoaiSetLau(request));
    }

    @PutMapping("/category/hotpotType/{id}")
    public ResponseEntity<LoaiLauResponse> updateLoaiSetLau(@PathVariable Integer id, @RequestBody @Valid LoaiLauRequest request) {
        return ResponseEntity.ok(monAnService.updateLoaiSetLau(id, request));
    }

    // ========================================================================
    // 5. QUẢN LÝ SET LẨU (Hotpot Set & Details)
    // ========================================================================

    @GetMapping("/hotpot")
    @Operation(summary = "Lấy danh sách Set lẩu")
    public ResponseEntity<List<SetLauResponse>> getAllSetLau() {
        return ResponseEntity.ok(monAnService.getAllSetLau());
    }

    @GetMapping("/hotpot/{id}")
    @Operation(summary = "Chi tiết Set lẩu", description = "Bao gồm cả danh sách món nhúng đi kèm")
    public ResponseEntity<SetLauResponse> getSetLauById(@PathVariable Integer id) {
        return ResponseEntity.ok(monAnService.getSetLauById(id));
    }

    @PostMapping("/hotpot")
    @Operation(summary = "Tạo Set lẩu mới", description = "Tạo Set và các món chi tiết trong cùng 1 request")
    public ResponseEntity<SetLauResponse> createSetLau(@RequestBody @Valid SetLauRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createSetLau(request));
    }

    @PutMapping("/hotpot/{id}")
    @Operation(summary = "Cập nhật Set lẩu")
    public ResponseEntity<SetLauResponse> updateSetLau(@PathVariable Integer id, @RequestBody @Valid SetLauRequest request) {
        return ResponseEntity.ok(monAnService.updateSetLau(id, request));
    }

    @PostMapping("/dinh-luong")
    public ResponseEntity<?> createUnit(@RequestBody DinhLuongRequest request) {
        try {
            DinhLuong savedUnit = monAnService.createDinhLuong(request);
            return ResponseEntity.ok(savedUnit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi tạo định lượng: " + e.getMessage());
        }
    }
}


