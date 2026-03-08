package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.*;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepository;
import com.example.datn_cozypot_spring_boot.service.DatBanService;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/guest")
@RequiredArgsConstructor
@CrossOrigin("*")
@Tag(name = "Guest API - Menu & Món ăn", description = "API lấy dữ liệu hiển thị cho khách hàng (Chỉ lấy trạng thái Active)")
public class GuestController {

    private final MonAnService monAnService;
    private final BanAnRepository banAnRepository;
    private final ModelMapper modelMapper;
    private final DatBanService datBanService;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucRepository danhMucRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.LoaiLauRepository loaiLauRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository setLauRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository danhMucChiTietRepository;

    // 1. Lấy danh sách Danh Mục (Category) đang hoạt động
    @GetMapping("/category/active")
    @Operation(summary = "Lấy danh mục Active")
    public ResponseEntity<List<DanhMucResponse>> getDanhMucActive() {
        return ResponseEntity.ok(monAnService.findDanhMucActive());
    }

    @GetMapping("/ban-an/active")
    public List<BanAnResponse> danhSachBanAn(){
        return datBanService.getAllBanAn();
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

    @GetMapping("/table/active")
    public ResponseEntity<List<BanAnResponse>> getBanAnActive(){
        List<BanAnResponse> response = banAnRepository.findByTrangThai(0).stream()
                .map(banAn -> {
                    BanAnResponse banAnResponse = modelMapper.map(banAn, BanAnResponse.class);
                    banAnResponse.setSoCho(banAn.getSoNguoiToiDa());
                    banAnResponse.setSoTang(banAn.getIdKhuVuc().getTang());
                    return banAnResponse;
                }).toList();

        return ResponseEntity.ok(response);
    }

    private String saveBase64ToFile(String base64Data, String fileName) {
        if (base64Data == null || !base64Data.contains(",")) return null;
        try {
            String base64Image = base64Data.split(",")[1];
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);

            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) uploadDir.mkdir();

            Path path = Paths.get("uploads/" + fileName + ".jpg");
            Files.write(path, imageBytes);
            return fileName + ".jpg";
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<MenuSummaryDTO> getMenuSummary() {
        // 1. Lấy Danh mục
        List<CategoryDTOCB> categories = danhMucRepository.findAll().stream()
                .filter(dm -> dm.getTrangThai() == 1)
                .map(dm -> new CategoryDTOCB(dm.getTenDanhMuc()))
                .toList();

        // 2. Lấy Loại Set Lẩu
        List<LoaiSetLauDTOCB> setTypes = loaiLauRepository.findAll().stream()
                .filter(ls -> ls.getTrangThai() == 1)
                .map(ls -> new LoaiSetLauDTOCB(ls.getTenLoaiSet()))
                .toList();

        // 3. Lấy Set Lẩu cụ thể
        List<SetLauDTOCB> sets = setLauRepository.findAll().stream()
                .filter(s -> s.getTrangThai() == 1)
                .map(s -> {
                    // Lấy danh sách tên món từ bảng chi tiết
                    List<String> monThanhPhan = s.getListChiTietSetLau().stream()
                            .map(ct -> ct.getMonAn().getTenMon())
                            .toList();

                    return new SetLauDTOCB(
                            s.getTenSetLau(),
                            s.getIdLoaiSet().getTenLoaiSet(),
                            s.getGiaBan(),
                            s.getMoTa(),
                            monThanhPhan // Đưa danh sách món vào đây
                    );
                })
                .toList();

        String baseUrl = "https://unrheumatic-gametically-yajaira.ngrok-free.dev/uploads/";
        String placeholderUrl = baseUrl + "placeholder.jpg";
        List<MonAnDTOCB> items = danhMucChiTietRepository.findAll().stream()
                .filter(m -> m.getTrangThai() == 1)
                .map(m -> {
                    String finalImageUrl;

                    // Kiểm tra dữ liệu trong DB
                    if (m.getHinhAnh() != null && !m.getHinhAnh().isEmpty()) {
                        String fileName = "monan_" + m.getId();
                        saveBase64ToFile(m.getHinhAnh(), fileName);
                        finalImageUrl = baseUrl + fileName + ".jpg";
                    } else {
                        // Nếu null hoặc rỗng, dùng ảnh placeholder
                        finalImageUrl = placeholderUrl;
                    }

                    return new MonAnDTOCB(
                            m.getTenMon(),
                            m.getGiaBan(),
                            m.getDanhMuc().getTenDanhMuc(),
                            finalImageUrl
                    );
                }).toList();

        MenuSummaryDTO summary = MenuSummaryDTO.builder()
                .danhMuc(categories)
                .loaiSetLau(setTypes)
                .setLau(sets)
                .monAnLe(items) // Đưa vào DTO tổng
                .build();

        return ResponseEntity.ok(summary);
    }
}