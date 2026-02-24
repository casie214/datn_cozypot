package com.example.datn_cozypot_spring_boot.controller;

// Import DTO Mới
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongItemRequest;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DinhLuongSimpleResponse;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DonViRequest;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DonViResponse;

// Import DTO Cũ (để giữ tương thích nếu cần)
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongResponse;
// Lưu ý: DinhLuongResponse này là cái cũ (phẳng), hoặc bạn tái sử dụng ItemDto

import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.service.MonAnService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manage/food")
public class MonAnController {

    private final MonAnService monAnService;

    // ========================================================================
    // 1. DANH MỤC (CATEGORY) - GIỮ NGUYÊN
    // ========================================================================
    @GetMapping("/category")
    public ResponseEntity<List<DanhMucResponse>> getAllDanhMuc() {
        return ResponseEntity.ok(monAnService.getAllDanhMuc());
    }

    @PostMapping("/category")
    public ResponseEntity<DanhMucResponse> createDanhMuc(@RequestBody @Valid DanhMucRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createDanhMuc(request));
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<DanhMucResponse> updateDanhMuc(@PathVariable Integer id, @RequestBody @Valid DanhMucRequest request) {
        return ResponseEntity.ok(monAnService.updateDanhMuc(id, request));
    }

    @DeleteMapping("/category/{id}")
    public ResponseEntity<Void> deleteDanhMuc(@PathVariable Integer id) {
        monAnService.deleteDanhMuc(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // 2. QUẢN LÝ ĐƠN VỊ & ĐỊNH LƯỢNG (LOGIC MỚI & TƯƠNG THÍCH CŨ)
    // ========================================================================

    // --- A. API MỚI CHO MÀN HÌNH QUẢN LÝ (Master-Detail) ---
    @GetMapping("/unit-types")
    @Operation(summary = "Lấy danh sách đơn vị (Cha-Con)")
    public ResponseEntity<List<DonViResponse>> getAllDonVi() {
        return ResponseEntity.ok(monAnService.getAllDonVi());
    }

    @PostMapping("/unit-types")
    @Operation(summary = "Tạo mới đơn vị tính")
    public ResponseEntity<DonViResponse> createDonVi(@RequestBody DonViRequest request) {
        return ResponseEntity.ok(monAnService.createDonVi(request));
    }

    @PutMapping("/unit-types/{id}")
    @Operation(summary = "Cập nhật đơn vị tính")
    public ResponseEntity<DonViResponse> updateDonVi(@PathVariable Integer id, @RequestBody DonViRequest request) {
        return ResponseEntity.ok(monAnService.updateDonVi(id, request));
    }

    // --- B. API CŨ (GIỮ TƯƠNG THÍCH CHO COMBOBOX) ---
    // Endpoint này vẫn trả về List phẳng để ComboBox "Chọn định lượng" hoạt động
    @GetMapping("/unit")
    public ResponseEntity<List<DinhLuongSimpleResponse>> getAllDinhLuongFlat() {
        // Lấy hết đơn vị (Cha)
        List<DonViResponse> allDonVi = monAnService.getAllDonVi();

        // Làm phẳng (Flatten) thành 1 list duy nhất: [200 ml, 500 ml, 1.5 L...]
        List<DinhLuongSimpleResponse> flatList = new ArrayList<>();
        for (DonViResponse dv : allDonVi) {
            if (dv.getValues() != null) {
                flatList.addAll(dv.getValues());
            }
        }
        return ResponseEntity.ok(flatList);
    }

    @GetMapping("/unit/by-category/{idDanhMuc}")
    public ResponseEntity<List<DinhLuongSimpleResponse>> getDinhLuongByDanhMuc(@PathVariable Integer idDanhMuc) {
        return getAllDinhLuongFlat();
    }

    @GetMapping("/unit-types/by-category/{id}")
    public ResponseEntity<List<DonViResponse>> getDonViByCategoryId(@PathVariable Integer id) {
        return ResponseEntity.ok(monAnService.getDonViByCategoryId(id));
    }

    @PostMapping("/dinh-luong")
    public ResponseEntity<?> createUnitOld(@RequestBody DonViRequest request) {
        // Tái sử dụng hàm createDonVi mới
        return ResponseEntity.ok(monAnService.createDonVi(request));
    }

    // ========================================================================
    // 3. MÓN ĂN (GIỮ NGUYÊN)
    // ========================================================================
    @GetMapping
    public ResponseEntity<List<MonAnResponse>> getAllMonAn() {
        return ResponseEntity.ok(monAnService.getAllMonAn());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonAnResponse> getMonAnById(@PathVariable Integer id) {
        return ResponseEntity.ok(monAnService.getMonAnById(id));
    }

    @PostMapping
    public ResponseEntity<MonAnResponse> createMonAn(@RequestBody @Valid MonAnRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createMonAn(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonAnResponse> updateMonAn(@PathVariable Integer id, @RequestBody @Valid MonAnRequest request) {
        return ResponseEntity.ok(monAnService.updateMonAn(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMonAn(@PathVariable Integer id) {
        monAnService.deleteMonAn(id);
        return ResponseEntity.noContent().build();
    }

    // ========================================================================
    // 4. LOẠI SET LẨU (GIỮ NGUYÊN)
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
    // 5. SET LẨU (GIỮ NGUYÊN)
    // ========================================================================
    @GetMapping("/hotpot")
    public ResponseEntity<List<SetLauResponse>> getAllSetLau() {
        return ResponseEntity.ok(monAnService.getAllSetLau());
    }

    @GetMapping("/hotpot/{id}")
    public ResponseEntity<SetLauResponse> getSetLauById(@PathVariable Integer id) {
        return ResponseEntity.ok(monAnService.getSetLauById(id));
    }

    @PostMapping("/hotpot")
    public ResponseEntity<SetLauResponse> createSetLau(@RequestBody @Valid SetLauRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createSetLau(request));
    }

    @PutMapping("/hotpot/{id}")
    public ResponseEntity<SetLauResponse> updateSetLau(@PathVariable Integer id, @RequestBody @Valid SetLauRequest request) {
        return ResponseEntity.ok(monAnService.updateSetLau(id, request));
    }

    @PutMapping("/quantitative/{id}")
    public ResponseEntity<?> updateDinhLuongSingle(@PathVariable Integer id, @RequestBody DinhLuongItemRequest req) {
        monAnService.updateDinhLuongSingle(id, req);
        return ResponseEntity.ok().build();
    }
}