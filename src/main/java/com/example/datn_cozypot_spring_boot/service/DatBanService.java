package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.request.*;
import com.example.datn_cozypot_spring_boot.dto.response.BanAnResponse;
import com.example.datn_cozypot_spring_boot.dto.response.BanTrangThaiResponse;
import com.example.datn_cozypot_spring_boot.dto.response.DatBanListResponse;
import com.example.datn_cozypot_spring_boot.dto.response.KhuVucResponse;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.repository.thanhToanRepository.PhuongThucThanhToanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DatBanService {
    @Autowired
    private BanAnRepository banAnRepository;

    @Autowired
    private PhieuDatBanRepository phieuDatBanRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private KhuVucRepository khuVucRepository;
    @Autowired
    private HoaDonThanhToanRepository hoaDonThanhToanRepository;
    @Autowired
    private PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    public List<DatBanListResponse> getAll(){
        return phieuDatBanRepository.findAll().stream().map(DatBanListResponse::new).toList();
    }

    public List<DatBanListResponse> getAllByTrangThai(){
        LocalDateTime thoiGianBatDau = LocalDateTime.now().minusHours(3); // Cho phép trễ tối đa 3 tiếng
        LocalDateTime thoiGianKetThuc = LocalDateTime.now().plusDays(2).toLocalDate().atTime(23, 59, 59); // Lấy cho cả ngày mai

        return phieuDatBanRepository.findWaitingListToday(thoiGianBatDau, thoiGianKetThuc)
                .stream()
                .map(DatBanListResponse::new)
                .toList();
    }

    public List<BanAnResponse> getAllBanAn(){
        List<BanAn> allBan = banAnRepository.findAll();

        // Lấy danh sách các phiếu đặt bàn "ảnh hưởng đến hiện tại" (logic chúng ta vừa sửa ở trên)
        List<DatBanListResponse> waitingToday = getAllByTrangThai();

        return allBan.stream().map(ban -> {
            BanAnResponse res = new BanAnResponse(ban);

            // 1. Kiểm tra xem bàn này có nằm trong danh sách "Sắp có khách đến" không
            boolean isReservedToday = waitingToday.stream()
                    .anyMatch(p -> p.getMaBan().equals(ban.getMaBan()));

            // 2. Định nghĩa lại trạng thái hiển thị
            if (ban.getTrangThai() == 1) {

                res.setTrangThai(1);
            } else if (isReservedToday) {

                res.setTrangThai(2);
            } else {

                res.setTrangThai(0);
            }

            return res;
        }).toList();
    }

    public BanAnResponse getBanAnById(Integer id){
        BanAn banAn =  banAnRepository.findById(id).get();
        return new BanAnResponse(banAn);
    }

    public List<KhuVucResponse> getAllKhuVuc(){
        return khuVucRepository.findAll().stream().map(KhuVucResponse::new).toList();
    }

    @Transactional
    public void addBanAn(AddBanAnRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        KhuVuc khuVuc = khuVucRepository.findById(req.getIdKhuVuc())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        BanAn banAn = new BanAn();
        banAn.setIdKhuVuc(khuVuc);          // ⭐ DÒNG QUYẾT ĐỊNH
        banAn.setSoNguoiToiDa(req.getSoNguoiToiDa());
        banAn.setTrangThai(0);
        banAn.setLoaiDatBan(req.getLoaiDatBan());
        banAn.setNguoiTao(username);
        banAnRepository.save(banAn);
    }

    @Transactional
    public void updateBanAn(UpdateBanAnRequest req) {

        BanAn banAn = banAnRepository.findById(req.getId())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại"));

        KhuVuc khuVuc = khuVucRepository.findById(req.getIdKhuVuc())
                .orElseThrow(() -> new RuntimeException("Khu vực không tồn tại"));

        banAn.setMaBan(req.getMaBan());
        banAn.setSoNguoiToiDa(req.getSoNguoiToiDa());
        banAn.setTrangThai(req.getTrangThai());     // ✔ lấy từ FE
        banAn.setLoaiDatBan(req.getLoaiDatBan());
        banAn.setIdKhuVuc(khuVuc);                   // ✔ set quan hệ

        banAnRepository.save(banAn); // ✔ UPDATE
    }




    public Page<DatBanListResponse> searchDatBan(
        DatBanSearchRequest request,
        Pageable pageable
) {
    LocalDateTime start = null;
    LocalDateTime end = null;

    if (request.getThoiGianDat() != null) {
        LocalDate date = request.getThoiGianDat();
        start = date.atStartOfDay();
        end = date.plusDays(1).atStartOfDay();
    }

    Page<PhieuDatBan> page = phieuDatBanRepository.search(
            request.getSoDienThoai(),
            request.getTrangThai(),
            start,
            end,
            pageable
    );

    return page.map(DatBanListResponse::new);
}



    @Transactional
    public void updateBanChoPhieu(DatBanUpdateRequest req) {

        BanAn banAn = banAnRepository.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn"));

        phieuDatBanRepository.updateBanChoPhieu(
                req.getId(),
                banAn
        );
    }

    @Transactional
    public void updateCheckIn(DatBanUpdateRequest request) {
        // =========================================================================
        // 1. XỬ LÝ NGHIỆP VỤ ĐỔI BÀN (NẾU CÓ idBanAnMoi)
        // =========================================================================
        if (request.getId() != null && request.getIdBanAnMoi() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));

            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());

            BanAn banCu = phieu.getIdBanAn();
            BanAn banMoi = banAnRepository.findById(request.getIdBanAnMoi())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn mới"));

            String maBanCu = banCu.getMaBan();
            String maBanMoi = banMoi.getMaBan();

            phieu.setIdBanAn(banMoi);
            if (hoaDon != null) {
                hoaDon.setIdBanAn(banMoi);
            }

            banCu.setTrangThai(0);
            banMoi.setTrangThai(1);

            banAnRepository.save(banCu);
            banAnRepository.save(banMoi);
            phieuDatBanRepository.save(phieu);

            if (hoaDon != null) {
                ghiLichSu(hoaDon, request.getIdNhanVien(),
                        "Đổi bàn: " + maBanCu + " -> " + maBanMoi,
                        "Chuyển toàn bộ dữ liệu từ bàn cũ sang bàn mới",
                        hoaDon.getTrangThaiHoaDon(), hoaDon.getTrangThaiHoaDon());
                hoaDonThanhToanRepository.save(hoaDon);
            }
            return;
        }

        // =========================================================================
        // 2. 🚨 LOGIC TÁCH BÀN / MỞ BÀN PHỤ / KHÁCH VÃNG LAI (KHI id BỊ NULL)
        // =========================================================================
        if (request.getId() == null && request.getTrangThai() != null && request.getTrangThai() == 1) {
            BanAn banPhu = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));

            banPhu.setTrangThai(1);
            banAnRepository.save(banPhu);

            PhieuDatBan phieuMoi = new PhieuDatBan();
            phieuMoi.setIdBanAn(banPhu);
            phieuMoi.setThoiGianDat(java.time.LocalDateTime.now());
            phieuMoi.setSoLuongKhach(banPhu.getSoNguoiToiDa());
            phieuMoi.setTrangThai(3);
            phieuMoi.setHinhThucDat(2);
            phieuMoi.setNguoiTao("Hệ thống");

            // 🚨 TẠO BIẾN KHÁCH HÀNG ĐỂ DÙNG CHUNG CHO CẢ PHIẾU VÀ HÓA ĐƠN
            KhachHang kh = null;
            if (request.getIdKhachHang() != null) {
                kh = khachHangRepository.findById(request.getIdKhachHang()).orElse(null);
                phieuMoi.setIdKhachHang(kh); // Gán cho Phiếu
            }
            phieuMoi = phieuDatBanRepository.save(phieuMoi);

            // C. Tạo Hóa đơn mới cho bàn phụ
            HoaDonThanhToan hoaDonMoi = new HoaDonThanhToan();
            hoaDonMoi.setIdPhieuDatBan(phieuMoi);
            hoaDonMoi.setIdBanAn(banPhu);

            // 🚨 BỔ SUNG DÒNG NÀY: GÁN ID KHÁCH HÀNG CHO HÓA ĐƠN
            if (kh != null) {
                hoaDonMoi.setIdKhachHang(kh);
            }

            hoaDonMoi.setThoiGianTao(Instant.now());
            hoaDonMoi.setTrangThaiHoaDon(4);
            hoaDonMoi.setTongTienChuaGiam(java.math.BigDecimal.ZERO);
            hoaDonMoi.setSoTienDaGiam(java.math.BigDecimal.ZERO);
            hoaDonMoi.setTienCoc(java.math.BigDecimal.ZERO);
            hoaDonMoi.setTongTienThanhToan(java.math.BigDecimal.ZERO);
            hoaDonMoi.setVatApDung(10.0F);



            if (request.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(request.getIdNhanVien()).orElse(null);
                hoaDonMoi.setIdNhanVien(nv);
            }

            hoaDonMoi = hoaDonThanhToanRepository.save(hoaDonMoi);

            ghiLichSu(hoaDonMoi, request.getIdNhanVien(), "Mở bàn phụ", "Tạo hóa đơn cho bàn tách", 4, 4);
            return;
        }

        // =========================================================================
        // 3. CẬP NHẬT TRẠNG THÁI BÀN ĂN (CHO LUỒNG CŨ CÓ ID PHIẾU)
        // =========================================================================
        if (request.getIdBanAn() != null) {
            BanAn banAn = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));
            banAn.setTrangThai(request.getTrangThai());
            banAnRepository.save(banAn);
        }

        // =========================================================================
        // 4. CẬP NHẬT TRẠNG THÁI PHIẾU ĐẶT BÀN
        // =========================================================================
        if (request.getId() != null && request.getTrangThaiPhieu() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));
            phieu.setTrangThai(request.getTrangThaiPhieu());
            phieuDatBanRepository.save(phieu);
        }

        // =========================================================================
        // 5. CẬP NHẬT ĐỒNG BỘ TRẠNG THÁI HÓA ĐƠN & GHI LOG
        // =========================================================================
        if (request.getId() != null) {
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());

            if (hoaDon != null) {
                Integer trangThaiCu = hoaDon.getTrangThaiHoaDon();
                Integer trangThaiMoi = trangThaiCu;
                String hanhDongLog = "";
                String lyDoLog = "Cập nhật hệ thống";

                // Xử lý đổi trạng thái Hóa đơn
                if (request.getTrangThaiHoaDon() != null) {
                    trangThaiMoi = request.getTrangThaiHoaDon();
                    hoaDon.setTrangThaiHoaDon(trangThaiMoi);

                    if (trangThaiMoi == 5) {
                        hanhDongLog = "Yêu cầu tính tiền";
                        lyDoLog = "Khách yêu cầu hóa đơn tạm tính";
                    } else if (trangThaiMoi == 6) {
                        hanhDongLog = "Đã thanh toán";
                        lyDoLog = "Hoàn tất thanh toán tại quầy";
                        hoaDon.setThoiGianThanhToan(Instant.now()); // Sửa lại thành LocalDateTime cho đồng bộ
                    } else if (trangThaiMoi == 8) {
                        hanhDongLog = "Hủy hóa đơn";
                        lyDoLog = "Hủy phiếu đặt bàn / Hủy hóa đơn";
                    }
                } else if (request.getTrangThaiPhieu() != null) {
                    if (request.getTrangThaiPhieu() == 3 && trangThaiCu < 4) {
                        trangThaiMoi = 4; // Trạng thái Phục vụ
                        hoaDon.setTrangThaiHoaDon(trangThaiMoi);
                        hanhDongLog = "Khách nhận bàn";
                        lyDoLog = "Nhân viên xác nhận khách đã vào bàn";
                    }
                }

                // Ghi log lịch sử thay đổi trạng thái
                if (!hanhDongLog.isEmpty()) {
                    ghiLichSu(hoaDon, request.getIdNhanVien(), hanhDongLog, lyDoLog, trangThaiCu, trangThaiMoi);
                }

                // XỬ LÝ TIỀN MẶT (CASH)
                if (request.getTienMat() != null && request.getTienMat().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal tienHienTai = hoaDon.getTienKhachDua() != null ? hoaDon.getTienKhachDua() : BigDecimal.ZERO;
                    hoaDon.setTienKhachDua(tienHienTai.add(request.getTienMat()));

                    ghiLichSu(hoaDon, request.getIdNhanVien(),
                            "Thu tiền mặt: " + request.getTienMat().setScale(0) + "đ",
                            "Thanh toán tại quầy", trangThaiMoi, trangThaiMoi);

                    saveLichSuThanhToan(hoaDon, request.getTienMat(), "PT02", "CASH_");
                }

                hoaDonThanhToanRepository.save(hoaDon);
            }
        }
    }

    private String getTenMonFromEntity(ChiTietHoaDon ct) {
        if (ct.getIdChiTietMonAn() != null) return ct.getIdChiTietMonAn().getTenMon();
        if (ct.getIdSetLau() != null) return ct.getIdSetLau().getTenSetLau();
        return "Món không xác định";
    }

    private void saveLichSuThanhToan(HoaDonThanhToan hd, BigDecimal soTien, String maPT, String prefixMaGD) {
        try {
            PhuongThucThanhToan pt = phuongThucThanhToanRepository.findByMaPhuongThuc(maPT);
            LichSuThanhToan ls = new LichSuThanhToan();
            ls.setPhuongThucThanhToan(pt);
            ls.setHoaDon(hd);
            ls.setTenPhuongThuc(pt != null ? pt.getTenPhuongThuc() : maPT);
            ls.setMaGiaoDich(prefixMaGD.contains("_") ? prefixMaGD + System.currentTimeMillis() : prefixMaGD);
            ls.setSoTienThanhToan(soTien);
            ls.setLoaiGiaoDich(1);
            ls.setNgayThanhToan(Instant.now());
            ls.setTrangThai(1);
            lichSuThanhToanRepository.save(ls);
        } catch (Exception e) {
            System.out.println("Lỗi lưu lịch sử thanh toán: " + e.getMessage());
        }
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

    @Transactional
    public void autoUpdateTrangThaiPhieu() {

        int quaHan = phieuDatBanRepository.updateChoXacNhanQuaHan();
        int daHuy = phieuDatBanRepository.updateDaXacNhanQuaGio();

        System.out.println(
                "Auto update: " + quaHan + " QUÁ HẠN, " + daHuy + " ĐÃ HỦY"
        );
    }

    @Transactional
    public void updateTrangThai(Integer id, Integer trangThai) {
        phieuDatBanRepository.updateTrangThai(id, trangThai);
    }

    public List<BanTrangThaiResponse> getTrangThaiBanTheoNgay(LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<PhieuDatBan> phieuList =
                phieuDatBanRepository.findPhieuTrongNgay(start, end);

        Map<Integer, Integer> banTrangThaiMap = new HashMap<>();

        for (PhieuDatBan phieu : phieuList) {
            Integer banId = phieu.getIdBanAn().getId();
            Integer trangThaiPhieu = phieu.getTrangThai();

            // Nếu đang sử dụng → BÀN CÓ KHÁCH
            if (trangThaiPhieu == 3) {
                banTrangThaiMap.put(banId, 1);
                continue;
            }

            // Nếu chưa có trạng thái và có đặt
            if (!banTrangThaiMap.containsKey(banId)) {
                if (trangThaiPhieu == 0 || trangThaiPhieu == 1) {
                    banTrangThaiMap.put(banId, 2);
                }
            }
        }

        return banTrangThaiMap.entrySet()
                .stream()
                .map(e -> new BanTrangThaiResponse(
                        e.getKey(),
                        e.getValue()
                ))
                .toList();
    }


    @Transactional
    public void createFull(CreateBanFullRequest req) {

        KhuVuc kv = khuVucRepository
                .findByTenKhuVucAndTang(req.getTenKhuVuc(), req.getTang())
                .orElse(null);

        if (kv == null) {
            kv = new KhuVuc();
            kv.setTenKhuVuc(req.getTenKhuVuc());
            kv.setTang(req.getTang());

            // TẠM set để qua validate
            kv.setMaKhuVuc("TEMP");

            kv = khuVucRepository.save(kv);

            // Sau khi có ID mới generate mã chuẩn
            String ma = "KV" + String.format("%03d", kv.getId());
            kv.setMaKhuVuc(ma);

            khuVucRepository.save(kv);
        }

        BanAn ban = new BanAn();
        ban.setIdKhuVuc(kv);
        ban.setSoNguoiToiDa(req.getSoNguoiToiDa());
        ban.setLoaiDatBan(req.getLoaiDatBan());

        banAnRepository.save(ban);
    }

    public KhuVuc create(KhuVucRequest request) {

        if (request.getTang() == null || request.getTang() <= 0) {
            throw new RuntimeException("Tầng không hợp lệ");
        }

        if (khuVucRepository.existsByTangAndTenKhuVucAndTrangThai(
                request.getTang(),
                request.getTenKhuVuc().trim(),
                1)) {
            throw new RuntimeException("Khu vực đã tồn tại");
        }

        KhuVuc kv = new KhuVuc();
        kv.setTang(request.getTang());
        kv.setTenKhuVuc(request.getTenKhuVuc().trim());
        kv.setMoTa(request.getMoTa());
        kv.setTrangThai(1);

        return khuVucRepository.save(kv);
    }

    public List<KhuVuc> getAllActive() {
        return khuVucRepository.findByTrangThai(1);
    }

    public List<KhuVuc> getByTang(Integer tang) {
        return khuVucRepository.findByTangAndTrangThai(tang, 1);
    }

    // Soft delete
    public void delete(Integer id) {
        KhuVuc kv = khuVucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        kv.setTrangThai(0);
        khuVucRepository.save(kv);
    }


}
