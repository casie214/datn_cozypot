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

    public List<DatBanListResponse> getAll(){
        return phieuDatBanRepository.findAll().stream().map(DatBanListResponse::new).toList();
    }

    public List<DatBanListResponse> getAllByTrangThai(){
        LocalDateTime thoiGianTraCuu = LocalDateTime.now().minusMinutes(60);

        return phieuDatBanRepository.findWaitingListFuture(thoiGianTraCuu)
                .stream()
                .map(DatBanListResponse::new)
                .toList();
    }

    public List<BanAnResponse> getAllBanAn(){
        return banAnRepository.findAll().stream().map(BanAnResponse::new).toList();
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

        // 1. CẬP NHẬT TRẠNG THÁI BÀN ĂN
        if (request.getIdBanAn() != null) {
            BanAn banAn = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));
            banAn.setTrangThai(request.getTrangThai());
            banAnRepository.save(banAn);
        }

        // 2. CẬP NHẬT TRẠNG THÁI PHIẾU ĐẶT BÀN
        if (request.getId() != null && request.getTrangThaiPhieu() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));

            phieu.setTrangThai(request.getTrangThaiPhieu());
            phieuDatBanRepository.save(phieu);
        }

        // 3. CẬP NHẬT ĐỒNG BỘ TRẠNG THÁI HÓA ĐƠN
        if (request.getId() != null) {
            // Tìm hóa đơn dựa theo id của Phiếu Đặt Bàn
            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());

            if (hoaDon != null) {
                // ƯU TIÊN 1: Nếu Frontend truyền rõ trạng thái Hóa Đơn (Ví dụ truyền 5 lúc bấm Thanh toán)
                if (request.getTrangThaiHoaDon() != null) {
                    hoaDon.setTrangThaiHoaDon(request.getTrangThaiHoaDon());

                    // Nếu hoàn tất thanh toán 100% bằng tiền mặt (Trạng thái 6) thì chốt thời gian
                    if (request.getTrangThaiHoaDon() == 6) {
                        hoaDon.setThoiGianThanhToan(java.time.Instant.now());
                    }
                }
                // ƯU TIÊN 2: Dùng logic suy luận mặc định cho các trường hợp check-in thông thường
                else if (request.getTrangThaiPhieu() != null) {
                    if (request.getTrangThaiPhieu() == 3) {
                        hoaDon.setTrangThaiHoaDon(4); // Đang phục vụ
                    } else if (request.getTrangThaiPhieu() == 0) {
                        hoaDon.setTrangThaiHoaDon(0); // Hủy
                    }
                }

                // ==========================================
                // 4. XỬ LÝ LƯU LỊCH SỬ TIỀN MẶT (NẾU CÓ)
                // ==========================================
                if (request.getTienMat() != null && request.getTienMat().compareTo(java.math.BigDecimal.ZERO) > 0) {

                    // 4.1. Cộng dồn số tiền mặt khách đưa vào Hóa Đơn
                    java.math.BigDecimal tienHienTai = hoaDon.getTienKhachDua() != null ? hoaDon.getTienKhachDua() : java.math.BigDecimal.ZERO;
                    hoaDon.setTienKhachDua(tienHienTai.add(request.getTienMat()));

                    // 4.2. Lưu vào bảng LichSuThanhToan
                    try {
                        PhuongThucThanhToan ptCash = phuongThucThanhToanRepository.findByMaPhuongThuc("PT02");

                        LichSuThanhToan lichSuCash = new LichSuThanhToan();
                        lichSuCash.setPhuongThucThanhToan(ptCash);
                        lichSuCash.setHoaDon(hoaDon);
                        lichSuCash.setTenPhuongThuc(ptCash != null ? ptCash.getTenPhuongThuc() : "Tiền mặt");
                        lichSuCash.setMaGiaoDich("CASH_" + System.currentTimeMillis());
                        lichSuCash.setSoTienThanhToan(request.getTienMat());
                        lichSuCash.setLoaiGiaoDich(1); // 1 = Giao dịch Thu Tiền
                        lichSuCash.setNgayThanhToan(java.time.Instant.now());
                        lichSuCash.setTrangThai(1); // 1 = Thành công

                        lichSuThanhToanRepository.save(lichSuCash);
                    } catch (Exception e) {
                        System.out.println("❌ Lỗi lưu lịch sử thanh toán Tiền mặt: " + e.getMessage());
                    }
                }

                // Cuối cùng: Lưu tất cả thay đổi của Hóa Đơn xuống DB
                hoaDonThanhToanRepository.save(hoaDon);
            }
        }
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
