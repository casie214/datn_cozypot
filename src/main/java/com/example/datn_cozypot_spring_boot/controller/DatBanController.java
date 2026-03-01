package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.request.*;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepository;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepository;
import com.example.datn_cozypot_spring_boot.repository.KhuVucRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuDatBanRepository;
import com.example.datn_cozypot_spring_boot.service.DatBanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/dat-ban")
public class DatBanController {
    @Autowired
    DatBanService datBanService;

    @Autowired
    PhieuDatBanRepository phieuDatBanRepository;

    @Autowired
    BanAnRepository banAnRepository;

    @Autowired
    KhuVucRepository khuVucRepository;
    @Autowired
    private HoaDonThanhToanRepository hoaDonThanhToanRepository;

    @GetMapping("/danh-sach")
    public List<DatBanListResponse> danhSach(){
        return datBanService.getAll();
    }

    @GetMapping("/danh-sach-check-in")
    public List<DatBanListResponse> danhSachCheckIn(){
        return datBanService.getAllByTrangThai();
    }

    @GetMapping("/danh-sach-ban-an")
    public List<BanAnResponse> danhSachBanAn(){
        return datBanService.getAllBanAn();
    }

    @GetMapping("/ban-an-detail/{id}")
    public BanAnResponse chiTietBanAn(@PathVariable("id") Integer id){
        return datBanService.getBanAnById(id);
    }

    @GetMapping("/danh-sach-khu-vuc")
    public List<KhuVucResponse> danhSachKhuVuc(){
        return datBanService.getAllKhuVuc();
    }


    @PostMapping("/search")
public Page<DatBanListResponse> searchDatBan(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size,
        @RequestBody DatBanSearchRequest req
) {
    int p = page != null ? page : 0;
    int s = size != null ? size : 5;

    Pageable pageable = PageRequest.of(
            p,
            s,
            Sort.by(Sort.Direction.ASC, "thoiGianDat")
    );

    return datBanService.searchDatBan(req, pageable);
}


    @PostMapping("/add-ban-an")
    public void addBanAn(@RequestBody @Valid AddBanAnRequest addBanAnRequest){
        datBanService.addBanAn(addBanAnRequest);
    }

    @PutMapping("/update-ban-an")
    public void updateBanAn(@RequestBody @Valid UpdateBanAnRequest updateBanAnRequest){
        datBanService.updateBanAn(updateBanAnRequest);
    }


    // Endpoint cập nhật bàn cho phiếu đặt
    @PutMapping("/update")
    public void updatePhieuDatBan(@RequestBody @Valid DatBanUpdateRequest datBanUpdateRequest){
        datBanService.updateBanChoPhieu(datBanUpdateRequest);
    }

    @PutMapping("/update-trang-thai-ban")
    public void updateTrangThaiBan(@RequestBody @Valid DatBanUpdateRequest datBanUpdateRequest){
        datBanService.updateCheckIn(datBanUpdateRequest);
    }

    @PutMapping("/update-trang-thai-phieu")
    public ResponseEntity<?> updateTrangThai(
            @RequestBody @Valid UpdateTrangThaiPhieuRequest request
    ) {
        datBanService.updateTrangThai(
                request.getId(),
                request.getTrangThai()
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ban-an/trang-thai-theo-ngay/{date}")
    public List<BanTrangThaiResponse> getTrangThaiBanTheoNgay(
            @PathVariable("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        System.out.println("========================================");
        System.out.println("ENDPOINT HIT! Date received: " + date);
        System.out.println("========================================");

        return datBanService.getTrangThaiBanTheoNgay(date);
    }


    @PostMapping("/create-full")
    public ResponseEntity<?> createFull(@RequestBody CreateBanFullRequest request) {
        datBanService.createFull(request);
        return ResponseEntity.ok("Tạo thành công");
    }

    @PostMapping("/khu-vuc")
    public ResponseEntity<?> create(@RequestBody KhuVucRequest request) {
        return ResponseEntity.ok(datBanService.create(request));
    }


    @GetMapping("/khu-vuc/tang/{tang}")
    public ResponseEntity<?> getByTang(@PathVariable Integer tang) {
        return ResponseEntity.ok(datBanService.getByTang(tang));
    }

    @PostMapping("/add-phieu-dat-ban")
    public ResponseEntity<CreatePhieuDatBanFullResponse> createFull(
            @RequestBody CreatePhieuDatBanFullRequest req) {

        CreatePhieuDatBanFullResponse response = datBanService.createFull(req);
        return ResponseEntity.ok(response);
    }

    // GET /api/khach-hang/all
    // Lấy toàn bộ danh sách → dùng cho multiselect khi mở modal
    @GetMapping("/all-khach-hang")
    public ResponseEntity<List<KhachHangSelectDTO>> getAll() {
        return ResponseEntity.ok(datBanService.getAllForSelect());
    }

    // GET /api/khach-hang/search?keyword=0912...
    // Tìm theo tên hoặc SĐT → dùng cho filter realtime trong multiselect
    @GetMapping("/search-khach")
    public ResponseEntity<List<KhachHangSelectDTO>> search(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        return ResponseEntity.ok(datBanService.searchByKeyword(keyword));
    }

    // GET /api/khach-hang/sdt/0912345678
    // Tìm đúng 1 khách theo SĐT
    @GetMapping("/sdt/{soDienThoai}")
    public ResponseEntity<?> findBySdt(@PathVariable String soDienThoai) {
        KhachHangSelectDTO result = datBanService.findBySoDienThoai(soDienThoai);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

}
