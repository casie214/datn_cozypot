package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.*;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.phieuDatBan.PhieuDatBanResponse;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.ChiTietHoaDon;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBan;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepository;
import com.example.datn_cozypot_spring_boot.repository.ChiTietHoaDonRepository;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuDatBanRepository;
import com.example.datn_cozypot_spring_boot.service.DatBanService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.HoaDonThanhToanService;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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
    private final PhieuDatBanRepository phieuDatBanRepository;
    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final HoaDonThanhToanService hoaDonThanhToanService;

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

    @GetMapping("/active-by-ban/{idBanAn}")
    public ResponseEntity<PhieuDatBanResponse> findActivePhieuByBanAn(
            @PathVariable Integer idBanAn,
            @RequestParam(required = false) Integer idPhieu) {

        // 1. Tìm Bàn ăn mà nhân viên vừa click vào
        BanAn banAn = banAnRepository.findById(idBanAn).orElse(null);
        if (banAn == null) return ResponseEntity.notFound().build();

        // 2. Tìm Phiếu Đặt Bàn chứa bàn này
        List<PhieuDatBan> danhSachPhieu = phieuDatBanRepository.findByDsBanAn_BanAn_IdAndTrangThaiInOrderByThoiGianDatAsc(idBanAn, Arrays.asList(0, 1, 3));

        if (danhSachPhieu == null || danhSachPhieu.isEmpty()) {
            return ResponseEntity.noContent().build(); // Bàn trống
        }

        PhieuDatBan phieu = (idPhieu != null)
                ? danhSachPhieu.stream().filter(p -> p.getId().equals(idPhieu)).findFirst().orElse(danhSachPhieu.get(0))
                : danhSachPhieu.get(0);

        // 3. Map các thông tin cơ bản
        PhieuDatBanResponse res = new PhieuDatBanResponse();
        res.setId(phieu.getId());
        res.setMaPhieu(phieu.getMaDatBan());
        res.setThoiGianDat(phieu.getThoiGianDat());
        res.setSoNguoi(phieu.getSoLuongKhach());
        res.setTrangThai(phieu.getTrangThai());


        if (phieu.getIdKhachHang() != null) {
            res.setIdKhachHang(phieu.getIdKhachHang().getId());
            res.setTenKhachHang(phieu.getIdKhachHang().getTenKhachHang());
            res.setSdtKhachHang(phieu.getIdKhachHang().getSoDienThoai());
        } else {
            res.setTenKhachHang("Khách vãng lai");
        }

        // Set Bàn hiện tại (Bàn được click)
        res.setIdBanAn(banAn.getId());
        res.setMaBan(banAn.getMaBan());
        if (banAn.getIdKhuVuc() != null) {
            res.setTenKhuVuc(banAn.getIdKhuVuc().getTenKhuVuc());
            res.setTang(banAn.getIdKhuVuc().getTang());
        }

        // ===============================================================
        // 🚨 4. FIX LỖI Ở ĐÂY: XỬ LÝ N-N, DANH SÁCH BÀN VÀ BÀN CHÍNH/PHỤ
        // ===============================================================
        Set<BanAn> cacBanTrongPhieu = phieu.getBanAns();

        // Tạo List các bàn để gửi lên VueJS
        List<PhieuDatBanResponse.BanAnInfo> listBanInfo = new ArrayList<>();

        // Quy ước: Bàn đầu tiên được thêm vào Set (hoặc có ID nhỏ nhất) sẽ là Bàn Chính
        BanAn banChinh = null;

        if (cacBanTrongPhieu != null && !cacBanTrongPhieu.isEmpty()) {
            // Tìm Bàn chính (Ví dụ lấy bàn có ID nhỏ nhất để luôn cố định 1 bàn làm gốc)
            banChinh = cacBanTrongPhieu.stream()
                    .min(Comparator.comparing(BanAn::getId))
                    .orElse(null);

            // Map toàn bộ bàn vào listBanInfo
            for (BanAn b : cacBanTrongPhieu) {
                PhieuDatBanResponse.BanAnInfo info = new PhieuDatBanResponse.BanAnInfo();
                info.setId(b.getId());
                info.setMaBan(b.getMaBan());
                if (b.getIdKhuVuc() != null) {
                    info.setTenKhuVuc(b.getIdKhuVuc().getTenKhuVuc());
                    info.setTang(b.getIdKhuVuc().getTang());
                }
                listBanInfo.add(info);
            }
        }

        res.setDanhSachBan(listBanInfo);

        // Xét xem bàn đang click có phải là bàn chính không
        if (banChinh != null) {
            if (!banAn.getId().equals(banChinh.getId())) {
                // Click vào Bàn Phụ
                res.setIsBanPhu(true);
                res.setTenBanChinh(banChinh.getMaBan());
                res.setIdBanChinh(banChinh.getId());
            } else {
                // Click vào Bàn Chính
                res.setIsBanPhu(false);
                res.setTenBanChinh(banChinh.getMaBan());
                res.setIdBanChinh(banChinh.getId());
            }
        }

        // ===============================================================
        // 5. MAP HÓA ĐƠN & MÓN ĂN
        // ===============================================================
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(phieu.getId());

        if (hoaDon != null && hoaDon.getTrangThaiHoaDon() < 6) {
            res.setIdHoaDon(hoaDon.getId());
            res.setMaHoaDon(hoaDon.getMaHoaDon());
            res.setTongTienChuaGiam(hoaDon.getTongTienChuaGiam());
            res.setSoTienDaGiam(hoaDon.getSoTienDaGiam());
            res.setTienCoc(hoaDon.getTienCoc());
            res.setTongTienThanhToan(hoaDon.getTongTienThanhToan());
            res.setVatApDung(hoaDon.getVatApDung() != null ? Double.valueOf(hoaDon.getVatApDung()) : 10.0);

            List<ChiTietHoaDon> chiTietHD = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());
            if (chiTietHD != null) {
                List<PhieuDatBanResponse.ChiTietMonResponse> dsMon = chiTietHD.stream()
                        .filter(m -> m.getTrangThaiMon() != null && m.getTrangThaiMon() != 0)
                        .map(item -> {
                            PhieuDatBanResponse.ChiTietMonResponse dto = new PhieuDatBanResponse.ChiTietMonResponse();
                            dto.setIdChiTietHd(item.getId());
                            dto.setSoLuong(item.getSoLuong());
                            dto.setDonGia(item.getDonGiaTaiThoiDiemBan());
                            dto.setThanhTien(item.getThanhTien());
                            dto.setTrangThaiMon(item.getTrangThaiMon());

                            // Lấy ghi chú chuẩn
                            String ghiChuRaw = item.getGhiChuMon() != null ? item.getGhiChuMon() : "";
                            dto.setGhiChu(ghiChuRaw);

                            if (item.getIdChiTietMonAn() != null) {
                                dto.setTenMon(item.getIdChiTietMonAn().getTenMon());
                                dto.setId(item.getIdChiTietMonAn().getId());
                                dto.setType("FOOD");
                            } else if (item.getIdSetLau() != null) {
                                dto.setTenMon(item.getIdSetLau().getTenSetLau());
                                dto.setId(item.getIdSetLau().getId());
                                dto.setType("SET");
                            }
                            return dto;
                        }).collect(Collectors.toList());
                res.setChiTiet(dsMon);
            } else {
                res.setChiTiet(new ArrayList<>());
            }
        } else {
            res.setTienCoc(hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO);
            res.setChiTiet(new ArrayList<>());
        }

        return ResponseEntity.ok(res);
    }

    @PostMapping("/tao-don")
    public ResponseEntity<?> createOrder(@RequestBody HoaDonThanhToanRequest request) {
        try {
            hoaDonThanhToanService.createOrder(request);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Lên đơn món ăn thành công!");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("status", "error", "message", "Lỗi hệ thống: " + e.getMessage()));
        }
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