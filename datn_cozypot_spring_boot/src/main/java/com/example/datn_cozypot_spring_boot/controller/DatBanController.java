package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.request.*;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepository;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepository;
import com.example.datn_cozypot_spring_boot.repository.KhuVucRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuDatBanRepository;

import com.example.datn_cozypot_spring_boot.dto.response.BanAnResponse;
import com.example.datn_cozypot_spring_boot.dto.response.BanTrangThaiResponse;
import com.example.datn_cozypot_spring_boot.dto.response.DatBanListResponse;
import com.example.datn_cozypot_spring_boot.dto.response.KhuVucResponse;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.service.DatBanService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.HoaDonThanhToanService;
import com.example.datn_cozypot_spring_boot.service.ThongBaoService;
import jakarta.transaction.Transactional;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Autowired
    private ThongBaoService thongBaoService;
    @Autowired
    private HoaDonThanhToanService hoaDonThanhToanService;

    @GetMapping("/danh-sach")
    public List<DatBanListResponse> danhSach(){
        return datBanService.getAll();
    }

    @GetMapping("/danh-sach-check-in")
    public List<DatBanListResponse> danhSachCheckIn(){
        return datBanService.getAllByTrangThai();
    }

    @GetMapping("/danh-sach-dat-truoc")
    public List<DatBanListResponse> danhSachPreCheckedIn(){
        return datBanService.getAllByTrangThaiPreCheckedIn();
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

        // 🚨 SỬA Ở ĐÂY: Xóa bỏ cái Sort.by(...) đi
        // Chỉ cần truyền đúng số trang (p) và kích thước trang (s)
        Pageable pageable = PageRequest.of(p, s);

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


    @PutMapping("/chi-tiet-hoa-don/{id}/trang-thai")
    @Transactional
    public ResponseEntity<?> updateTrangThaiMon(@PathVariable Integer id, @RequestParam Integer trangThai, @RequestParam(required = false) Integer idNhanVien) {
        ChiTietHoaDon chiTiet = chiTietHoaDonRepository.findById(id).orElse(null);
        if (chiTiet == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy chi tiết hóa đơn");
        }

        // 2. Lưu lại thông tin trước khi cập nhật để ghi log
        Integer trangThaiCuCuaMon = chiTiet.getTrangThaiMon();
        HoaDonThanhToan hoaDon = chiTiet.getIdHoaDon();
        Integer trangThaiBillHienTai = hoaDon.getTrangThaiHoaDon();
        String tenMon = getTenMonFromEntity(chiTiet);

        // 3. Cập nhật trạng thái mới
        chiTiet.setTrangThaiMon(trangThai);
        chiTietHoaDonRepository.save(chiTiet);

        // 4. LOGIC GHI LỊCH SỬ HÓA ĐƠN
        String hanhDong = "";
        String lyDo = "Cập nhật trạng thái món ăn";

        if (trangThai == 2) {
            hanhDong = "Lên món: " + tenMon + " x" + chiTiet.getSoLuong();
            lyDo = "Đã phục vụ tại bàn";
        } else if (trangThai == 0) {
            hanhDong = "Hủy món: " + tenMon + " x" + chiTiet.getSoLuong();
            lyDo = "Nhân viên xóa trực tiếp";
        } else if (trangThai == 1 && trangThaiCuCuaMon == 2) {
            hanhDong = "Hoàn tác lên món: " + tenMon;
            lyDo = "Trả lại trạng thái chờ cung ứng";
        }

        if (!hanhDong.isEmpty()) {
            // Gọi hàm ghi log đã viết ở các bước trước
            // Lưu ý: trangThaiTruocDo và trangThaiMoi ở đây là trạng thái của HÓA ĐƠN (vẫn là 4 - Đang phục vụ)
            ghiLichSu(hoaDon, idNhanVien, hanhDong, lyDo, trangThaiBillHienTai, trangThaiBillHienTai);
        }

        return ResponseEntity.ok("Cập nhật thành công");
    }

    private String getTenMonFromEntity(ChiTietHoaDon ct) {
        if (ct.getIdChiTietMonAn() != null) return ct.getIdChiTietMonAn().getTenMon();
        if (ct.getIdSetLau() != null) return ct.getIdSetLau().getTenSetLau();
        return "Món không xác định";
    }

    private void ghiLichSu(HoaDonThanhToan hoaDon, Integer idNhanVien, String hanhDong, String lyDo, Integer trangThaiCu, Integer trangThaiMoi) {
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setIdHoaDon(hoaDon);

        // Tìm nhân viên thực hiện (nếu có idNhanVien truyền lên từ FE)
        if (idNhanVien != null) {
            NhanVien nv = nhanVienRepository.findById(idNhanVien).orElse(null);
            lichSu.setIdNhanVien(nv);
        }

        lichSu.setHanhDong(hanhDong);
        lichSu.setLyDoThucHien(lyDo);
        lichSu.setThoiGianThucHien(Instant.now());
        lichSu.setTrangThaiTruocDo(trangThaiCu);
        lichSu.setTrangThaiMoi(trangThaiMoi);

        lichSuHoaDonRepository.save(lichSu);
    }

    @PutMapping("/update-trang-thai-phieu")
    public ResponseEntity<?> updateTrangThai(
            @RequestBody @Valid UpdateTrangThaiPhieuRequest request
    ) {
        try {
            // 1. Thực hiện cập nhật trạng thái phiếu trong Database
            datBanService.updateTrangThai(
                    request.getId(),
                    request.getTrangThai()
            );

            // =======================================================
            // 🚨 LOGIC MỚI: HỦY PHIẾU -> HỦY HÓA ĐƠN
            // =======================================================
            if (request.getTrangThai() == 2) {
                // 2 = Đã hủy (Phiếu đặt bàn)
                // Gọi hàm từ Service Hóa Đơn để cập nhật trạng thái thành 8 (CANCELLED)
                // Cú pháp cụ thể phụ thuộc vào tên Service của bạn, ví dụ:
                hoaDonThanhToanService.updateTrangThaiHoaDonByIdPhieu(request.getId(), 8);
            }

            // 2. Lấy thông tin phiếu sau khi cập nhật
            PhieuDatBan phieu = datBanService.getPhieuDatBanById(request.getId());

            // 3. Lấy tên trạng thái tương ứng
            String tenTrangThai = getTenTrangThai(request.getTrangThai());

            // 4. Gửi thông báo Real-time
            thongBaoService.sendNotify(
                    "Cập nhật trạng thái phiếu 🔥",
                    "Mã phiếu: " + phieu.getMaDatBan() +
                            "\nTrạng thái mới: " + tenTrangThai +
                            "\nKhách hàng: " + phieu.getIdKhachHang().getTenKhachHang()
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Hàm helper để map trạng thái
    private String getTenTrangThai(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case 0 -> "Chờ xác nhận";
            case 1 -> "Đã xác nhận";
            case 2 -> "Đã hủy";
            case 3 -> "Đang sử dụng (Check-in)";
            case 4 -> "Hoàn thành";
            case 5 -> "Quá hạn / Khách không đến";
            default -> "Không xác định (" + trangThai + ")";
        };
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

    @PostMapping("/check-ban-trong")
    public ResponseEntity<List<BanAnResponse>> checkBanTrong(@RequestBody DatBanRequest request) {
        List<BanAnResponse> danhSachBanTrong = datBanService.checkBanTrong(request);
        return ResponseEntity.ok(danhSachBanTrong);
    }
    @PostMapping("/tao-moi")
    public ResponseEntity<?> taoPhieuDatBanOnline(@RequestBody DatBanOnlineRequest request) {
        try {
            Map<String, Object> responseData = datBanService.taoPhieuDatBanOnline(request);

            thongBaoService.sendNotify(
                    "Có khách đã đặt bàn 🔥",
                    "Số điện thoại khách hàng: " + request.getPhone() + "\nThời gian đặt: " + request.getThoiGianDat()
            );
            System.out.println("Đã lưu thông báo");

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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
