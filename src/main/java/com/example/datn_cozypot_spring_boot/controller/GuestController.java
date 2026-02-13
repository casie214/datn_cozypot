package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
@Tag(name = "Guest API - Menu & Món ăn", description = "API lấy dữ liệu hiển thị cho khách hàng (Chỉ lấy trạng thái Active)")
public class GuestController {

    private final MonAnService monAnService;

    // 1. Lấy danh sách Danh Mục (Category) đang hoạt động
    @GetMapping("/category/active")
    @Operation(summary = "Lấy danh mục Active")
    public ResponseEntity<List<DanhMucResponse>> getDanhMucActive() {
        return ResponseEntity.ok(monAnService.findDanhMucActive());
    }

    // 2. Lấy danh sách Loại Set Lẩu đang hoạt động
    @GetMapping("/hotpot-type/active")
    @Operation(summary = "Lấy loại set lẩu Active")
    public ResponseEntity<List<LoaiLauResponse>> getLoaiSetLauActive() {
        return ResponseEntity.ok(monAnService.findLoaiSetLauActive());
    }

    // 3. Lấy danh sách Set Lẩu đang hoạt động
    @GetMapping("/hotpot/active")
    @Operation(summary = "Lấy danh sách Set lẩu Active")
    public ResponseEntity<List<SetLauResponse>> getSetLauActive() {
        return ResponseEntity.ok(monAnService.findSetLauActive());
    }

    // 4. Lấy danh sách Món Ăn (DanhMucChiTiet) đang hoạt động
    // Endpoint này thay thế cho cả /food/active và /food-detail/active cũ
    @GetMapping("/food/active")
    @Operation(summary = "Lấy danh sách món ăn Active (Flat Data)")
    public ResponseEntity<List<MonAnResponse>> getMonAnActive() {
        return ResponseEntity.ok(monAnService.findMonAnActive());
    }

    // 5. Lấy Top Set Lẩu bán chạy
    @GetMapping("/hotpot/top/{metric}")
    @Operation(summary = "Lấy Top Set lẩu bán chạy")
    public ResponseEntity<List<SetLauResponse>> getSetLauTheoTop(@PathVariable int metric) {
        return ResponseEntity.ok(monAnService.findSetLauTop(metric));
    }

    // (Optional) Nếu bạn vẫn muốn endpoint cũ để tránh lỗi Frontend, có thể để nó gọi chung hàm
    @GetMapping("/category-detail/active")
    public ResponseEntity<List<MonAnResponse>> getDanhMucChiTietActive() {
        return getMonAnActive(); // Tái sử dụng logic
    }
}