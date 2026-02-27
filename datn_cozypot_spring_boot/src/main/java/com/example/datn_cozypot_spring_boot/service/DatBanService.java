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
        LocalDateTime thoiGianBatDau = LocalDateTime.now().minusMinutes(15);
        LocalDateTime thoiGianKetThuc = LocalDateTime.now().toLocalDate().atTime(23, 59, 59);
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
        // 1. XỬ LÝ NGHIỆP VỤ ĐỔI BÀN (NẾU CÓ idBanAnMoi)
        if (request.getId() != null && request.getIdBanAnMoi() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));

            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());

            // Lấy thông tin bàn cũ và bàn mới
            BanAn banCu = phieu.getIdBanAn();
            BanAn banMoi = banAnRepository.findById(request.getIdBanAnMoi())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn mới"));

            // Lưu mã bàn để ghi log
            String maBanCu = banCu.getMaBan();
            String maBanMoi = banMoi.getMaBan();

            // Cập nhật thực thể: Chuyển Phiếu và Hóa đơn sang bàn mới
            phieu.setIdBanAn(banMoi);
            if (hoaDon != null) {
                hoaDon.setIdBanAn(banMoi);
            }

            // Cập nhật trạng thái vật lý của 2 bàn
            banCu.setTrangThai(0); // Bàn cũ về trạng thái Trống
            banMoi.setTrangThai(1); // Bàn mới về trạng thái Có khách

            banAnRepository.save(banCu);
            banAnRepository.save(banMoi);
            phieuDatBanRepository.save(phieu);

            // Ghi Log đổi bàn vào Timeline lịch sử hóa đơn
            if (hoaDon != null) {
                ghiLichSu(hoaDon, request.getIdNhanVien(),
                        "Đổi bàn: " + maBanCu + " -> " + maBanMoi,
                        "Chuyển toàn bộ dữ liệu từ bàn cũ sang bàn mới",
                        hoaDon.getTrangThaiHoaDon(), hoaDon.getTrangThaiHoaDon());
                hoaDonThanhToanRepository.save(hoaDon);
            }
            // Nếu chỉ là tác vụ đổi bàn thì có thể kết thúc sớm tại đây
            return;
        }

        // 2. CẬP NHẬT TRẠNG THÁI BÀN ĂN (CHO CÁC TÁC VỤ KHÁC NHƯ CHECK-IN)
        if (request.getIdBanAn() != null) {
            BanAn banAn = banAnRepository.findById(request.getIdBanAn())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bàn ăn"));
            banAn.setTrangThai(request.getTrangThai());
            banAnRepository.save(banAn);
        }

        // 3. CẬP NHẬT TRẠNG THÁI PHIẾU ĐẶT BÀN
        if (request.getId() != null && request.getTrangThaiPhieu() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));
            phieu.setTrangThai(request.getTrangThaiPhieu());
            phieuDatBanRepository.save(phieu);
        }

        // 4. CẬP NHẬT ĐỒNG BỘ TRẠNG THÁI HÓA ĐƠN & GHI LOG
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
                        hoaDon.setThoiGianThanhToan(java.time.Instant.now());
                    }
                } else if (request.getTrangThaiPhieu() != null) {
                    if (request.getTrangThaiPhieu() == 3) {
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

                // 5. XỬ LÝ TIỀN MẶT (CASH)
                if (request.getTienMat() != null && request.getTienMat().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    BigDecimal tienHienTai = hoaDon.getTienKhachDua() != null ? hoaDon.getTienKhachDua() : BigDecimal.ZERO;
                    hoaDon.setTienKhachDua(tienHienTai.add(request.getTienMat()));

                    // Ghi log thu tiền mặt
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
