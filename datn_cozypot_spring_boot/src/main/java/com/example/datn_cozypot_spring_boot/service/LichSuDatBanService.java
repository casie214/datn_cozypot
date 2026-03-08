package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.ChiTietHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.LichSuHoaDonService;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.LichSuThanhToanService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LichSuDatBanService {

    private final HoaDonThanhToanRepository hoaDonThanhToanRepository;
    private final PhieuDatBanRepository phieuDatBanRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final BanAnRepository banAnRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final ThamSoHeThongRepository thamSoHeThongRepository;

    private final ChiTietHoaDonService chiTietHoaDonService;
    private final LichSuHoaDonService lichSuHoaDonService;
    private final LichSuThanhToanService lichSuThanhToanService;

    // 1. DÀNH CHO KHÁCH ĐÃ ĐĂNG NHẬP
    public List<Map<String, Object>> layLichSuCaNhan(String emailHoacSdt) {
        KhachHang khachHang = khachHangRepository.findByEmail(emailHoacSdt)
                .orElseGet(() -> khachHangRepository.findBySoDienThoai(emailHoacSdt)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng")));

        List<HoaDonThanhToan> listHoaDon = hoaDonThanhToanRepository.findByIdKhachHang_IdOrderByThoiGianTaoDesc(khachHang.getId());

        return listHoaDon.stream()
                .filter(hd -> hd.getIdPhieuDatBan() != null)
                .map(hd -> {
                    Map<String, Object> map = new HashMap<>();
                    PhieuDatBan phieu = hd.getIdPhieuDatBan();

                    map.put("idPhieu", phieu.getId());
                    map.put("maPhieu", phieu.getMaDatBan());
                    map.put("thoiGianDat", phieu.getThoiGianDat());
                    map.put("soNguoi", phieu.getSoLuongKhach());

                    String tenBan = phieu.getBanAns().stream().findFirst().map(BanAn::getMaBan).orElse("Chưa xếp");
                    map.put("tenBan", tenBan);

                    map.put("tongTienThanhToan", hd.getTongTienThanhToan());
                    map.put("tienCoc", hd.getTienCoc() != null ? hd.getTienCoc() : BigDecimal.ZERO);
                    map.put("trangThaiHoaDon", hd.getTrangThaiHoaDon());
                    return map;
                }).collect(Collectors.toList());
    }

    // 2. DÀNH CHO KHÁCH VÃNG LAI
    public Map<String, Object> traCuuKhachVangLai(String maPhieu) {
        if (maPhieu == null || maPhieu.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập Mã phiếu đặt bàn!");
        }

        HoaDonThanhToan hd = hoaDonThanhToanRepository.findByMaPhieuDatBan(maPhieu.trim())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu cho mã phiếu: " + maPhieu));

        return mapHoaDonChiTietSangFrontend(hd);
    }

    private Map<String, Object> mapHoaDonChiTietSangFrontend(HoaDonThanhToan hd) {
        Map<String, Object> map = new HashMap<>();

        map.put("idPhieu", hd.getIdPhieuDatBan().getId());
        map.put("maPhieu", hd.getIdPhieuDatBan().getMaDatBan());
        map.put("trangThaiHoaDon", hd.getTrangThaiHoaDon());
        map.put("tongTienChuaGiam", hd.getTongTienChuaGiam());
        map.put("tienCoc", hd.getTienCoc() != null ? hd.getTienCoc() : BigDecimal.ZERO);
        map.put("tongTienThanhToan", hd.getTongTienThanhToan());
        map.put("vatApDung", hd.getVatApDung() != null ? hd.getVatApDung() : 10.0);

        if (hd.getTrangThaiHoaDon() == 8 || hd.getTrangThaiHoaDon() == 9) {
            lichSuHoaDonRepository.findFirstByIdHoaDon_IdAndTrangThaiMoiInOrderByThoiGianThucHienDesc(hd.getId(), Arrays.asList(8, 9))
                    .ifPresent(log -> map.put("trangThaiTruocHuy", log.getTrangThaiTruocDo()));
        }

        map.put("chiTiet", chiTietHoaDonService.getAllChiTietHoaDon(hd.getId()));

        map.put("lichSuHoaDon", lichSuHoaDonService.layLichSuDonHang(hd.getId()));

        map.put("lichSuThanhToan", lichSuThanhToanService.getAllLichSuThanhToan(hd.getId()));

        return map;
    }

    // 3. KHÁCH TỰ HỦY BẰNG ID PHIẾU ĐẶT BÀN
    @Transactional
    public void khachTuHuyDon(Integer idPhieu, String lyDoKhachNhap) {
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(idPhieu);
        if (hd == null) throw new RuntimeException("Không tìm thấy thông tin phiếu đặt bàn");

        if (hd.getTrangThaiHoaDon() > 3) {
            throw new RuntimeException("Đơn hàng đang phục vụ hoặc đã hoàn tất, không thể tự hủy!");
        }

        BigDecimal tienCoc = hd.getTienCoc() != null ? hd.getTienCoc() : BigDecimal.ZERO;
        int trangThaiHDCu = hd.getTrangThaiHoaDon();
        int trangThaiMoi = 8;
        String ghiChuHoanTien = "[Khách tự hủy] Hủy đơn hàng";

        if (trangThaiHDCu >= 2 && tienCoc.compareTo(BigDecimal.ZERO) > 0 && hd.getIdPhieuDatBan() != null) {
            LocalDateTime thoiGianDat = hd.getIdPhieuDatBan().getThoiGianDat();

            int phutQuyDinh = thamSoHeThongRepository.findByMaThamSo("THOI_GIAN_HUY_HOAN_COC")
                    .map(ts -> Integer.parseInt(ts.getGiaTri())).orElse(120);

            if (LocalDateTime.now().isBefore(thoiGianDat.minusMinutes(phutQuyDinh))) {
                trangThaiMoi = 9;
                hd.setTienHoanTra(tienCoc);
                ghiChuHoanTien = "[Khách tự hủy] Đúng quy định, hoàn cọc";

                LichSuThanhToan ls = new LichSuThanhToan();
                ls.setHoaDon(hd);
                ls.setSoTienThanhToan(tienCoc);
                ls.setLoaiGiaoDich(3);
                ls.setGhiChu(ghiChuHoanTien);
                ls.setNgayThanhToan(Instant.now());
                ls.setTrangThai(1);
                ls.setTenPhuongThuc("Chuyển khoản");
                String maHoaDonStr = hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "HD" + hd.getId();
                ls.setMaGiaoDich("REFUND_KHACH_" + maHoaDonStr + "_" + System.currentTimeMillis());
                lichSuThanhToanRepository.save(ls);
            }else{
                ghiChuHoanTien = "[Khách tự hủy] Mất cọc do sát giờ";
            }
        }else if (trangThaiHDCu == 1){
            ghiChuHoanTien = "[Khách tự hủy] Hủy đơn khi chưa nộp cọc";
        }

        hd.setTrangThaiHoaDon(trangThaiMoi);
        hoaDonThanhToanRepository.save(hd);

        PhieuDatBan phieu = hd.getIdPhieuDatBan();
        phieu.setTrangThai(2);
        for (BanAn ban : phieu.getBanAns()) {
            ban.setTrangThai(0);
            banAnRepository.save(ban);
        }
        phieuDatBanRepository.save(phieu);

        List<ChiTietHoaDon> listMon = chiTietHoaDonRepository.findByIdHoaDon_Id(hd.getId());
        for (ChiTietHoaDon mon : listMon) mon.setTrangThaiMon(0);
        chiTietHoaDonRepository.saveAll(listMon);

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Khách hàng hủy đơn");
        log.setLyDoThucHien(ghiChuHoanTien + " - Lý do: " + lyDoKhachNhap);
        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(trangThaiMoi);
        lichSuHoaDonRepository.save(log);
    }
}