package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanRequest;
import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository;
import com.example.datn_cozypot_spring_boot.service.PhieuGiamGiaService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HoaDonThanhToanService {
    @Autowired
    HoaDonThanhToanRepository hoaDonThanhToanRepository;

    @Autowired
    ChiTietHoaDonService chiTietHoaDonService;

    @Autowired
    ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    LichSuHoaDonRepository lichSuHoaDonRepository;

    @Autowired
    LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    BanAnRepository banAnRepo;

    @Autowired
    DanhMucChiTietRepository chiTietDanhMucChiTietRepository;
    @Autowired
    private SetLauRepository setLauRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    public Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable){
        return hoaDonThanhToanRepository.getAllHoaDon(pageable);
    }

    public Page<HoaDonThanhToanResponse> searchHoaDon(String key, Integer trangThai, Instant tuNgay, Instant denNgay, Pageable pageable){
        return hoaDonThanhToanRepository.searchHoaDon(key, trangThai, tuNgay, denNgay, pageable);
    }

    public HoaDonThanhToanResponse getHoaDonById(Integer id) {
        return hoaDonThanhToanRepository.getHoaDonById(id);
    }

    @Transactional
    public void huyHoaDon(LichSuHoaDonRequest request) {
        if (chiTietHoaDonService.hasAnyDishServed(request.getIdHoaDon())) {
            throw new RuntimeException("Không thể hủy hóa đơn vì đã có món ăn được phục vụ!");
        }

        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        if (trangThaiHDCu == 7 || trangThaiHDCu == 8 || trangThaiHDCu == 9) {
            throw new RuntimeException("Hóa đơn đã ở trạng thái kết thúc, không thể hủy!");
        }

        BigDecimal tienCoc = hd.getTienCoc() != null ? hd.getTienCoc() : BigDecimal.ZERO;
        int trangThaiMoi;

        if (tienCoc.compareTo(BigDecimal.ZERO) > 0 && Boolean.TRUE.equals(request.getIsLoiDoQuan())) {
            // TRƯỜNG HỢP 1: Có cọc và Lỗi do quán -> Hoàn tiền
            hd.setTienHoanTra(tienCoc);
            trangThaiMoi = 9;
            hd.setTrangThaiHoaDon(trangThaiMoi);

            LichSuThanhToan lsThanhToan = new LichSuThanhToan();
            lsThanhToan.setHoaDon(hd);
            lsThanhToan.setSoTienThanhToan(tienCoc);
            lsThanhToan.setLoaiGiaoDich(3);
            lsThanhToan.setGhiChu("Hoàn trả cọc do hủy hóa đơn: " + request.getLyDoThucHien());
            lsThanhToan.setNgayThanhToan(Instant.now());
            lsThanhToan.setTrangThai(1);
            lsThanhToan.setTenPhuongThuc("Chuyển khoản");

            // TẠO MÃ GIAO DỊCH FAKE GIỐNG THẬT
            // Ví dụ kết quả: REFUND_HD00015_1708945672341
            String maHoaDonStr = hd.getMaHoaDon() != null ? hd.getMaHoaDon() : "HD" + hd.getId();
            String fakeMaGiaoDich = "REFUND_" + maHoaDonStr + "_" + System.currentTimeMillis();
            lsThanhToan.setMaGiaoDich(fakeMaGiaoDich);

            lichSuThanhToanRepository.save(lsThanhToan);

        } else {
            // TRƯỜNG HỢP 2: Không cọc hoặc Lỗi do khách -> Hủy luôn, mất cọc
            hd.setTienHoanTra(BigDecimal.ZERO);
            trangThaiMoi = 8;
            hd.setTrangThaiHoaDon(trangThaiMoi);
        }

        // === CẬP NHẬT CÁC BẢNG LIÊN QUAN ===
        List<ChiTietHoaDon> listgetAllMonAn = chiTietHoaDonRepository.findByIdHoaDon(hd.getId());
        for (ChiTietHoaDon chiTietHoaDon : listgetAllMonAn){
            chiTietHoaDon.setTrangThaiMon(0);
        }
        chiTietHoaDonRepository.saveAll(listgetAllMonAn);

        if (hd.getIdBanAn()!= null){
            hd.getIdBanAn().setTrangThai(0);
            banAnRepo.save(hd.getIdBanAn());
        }

        hoaDonThanhToanRepository.save(hd);

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong(trangThaiMoi == 9 ? "Hủy & Hoàn tiền" : "Hủy hóa đơn");

        String prefixLyDo = "";
        if (tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            prefixLyDo = Boolean.TRUE.equals(request.getIsLoiDoQuan()) ? "[Đã hoàn cọc] " : "[Mất cọc] ";
        }
        log.setLyDoThucHien(prefixLyDo + request.getLyDoThucHien());

        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(trangThaiMoi);

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepository.save(log);
    }

    @Transactional
    public void thanhToanHoaDon(LichSuHoaDonRequest request) {
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        if (trangThaiHDCu == 6 || trangThaiHDCu == 7 || trangThaiHDCu == 8 || trangThaiHDCu == 9) {
            throw new RuntimeException("Hóa đơn đã được thanh toán hoặc đã kết thúc!");
        }

        Instant now = Instant.now();

        hd.setTrangThaiHoaDon(6);
        hd.setThoiGianThanhToan(Instant.now().plus(7, ChronoUnit.HOURS));
        hoaDonThanhToanRepository.save(hd);

        if (hd.getIdBanAn() != null){
            hd.getIdBanAn().setTrangThai(3);
            banAnRepo.save(hd.getIdBanAn());
        }

        LichSuThanhToan lsThanhToan = new LichSuThanhToan();
        lsThanhToan.setHoaDon(hd);
        lsThanhToan.setSoTienThanhToan(hd.getTongTienThanhToan());
        lsThanhToan.setLoaiGiaoDich(1);
        lsThanhToan.setGhiChu("Khách thanh toán tại quầy");
        lsThanhToan.setNgayThanhToan(now);
        lsThanhToan.setTrangThai(1);
        lsThanhToan.setTenPhuongThuc("Tiền mặt");
        lsThanhToan.setMaGiaoDich("CASH_" + hd.getMaHoaDon() + "_" + System.currentTimeMillis());
        lichSuThanhToanRepository.save(lsThanhToan);

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Thanh toán hóa đơn");
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(6);
        log.setThoiGianThucHien(now);

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepository.save(log);
    }

    @Transactional
    public void hoanTatHoaDon(LichSuHoaDonRequest request) {
        HoaDonThanhToan hd = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        if (trangThaiHDCu != 6) {
            throw new RuntimeException("Hóa đơn chưa được thanh toán, không thể hoàn tất!");
        }

        Instant now = Instant.now();

        hd.setTrangThaiHoaDon(7);
        hoaDonThanhToanRepository.save(hd);

        if (hd.getIdBanAn() != null) {
            hd.getIdBanAn().setTrangThai(0);
            banAnRepo.save(hd.getIdBanAn());
        }

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Khách ra về - Hoàn tất hóa đơn");
        log.setTrangThaiTruocDo(trangThaiHDCu);
        log.setTrangThaiMoi(7);
        log.setThoiGianThucHien(now);

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepository.save(log);
    }

    @Transactional
    public void createOrder(HoaDonThanhToanRequest req) {
        // =======================================================
        // 1. TÌM HOẶC TẠO MỚI HÓA ĐƠN
        // =======================================================
        BanAn banAn = banAnRepo.findById(req.getIdBanAn())
                .orElseThrow(() -> new RuntimeException("Bàn ăn không tồn tại"));

        Optional<HoaDonThanhToan> existingBill;
        if (req.getIdHoaDon() != null) {
            existingBill = hoaDonThanhToanRepository.findById(req.getIdHoaDon());
        } else {
            existingBill = hoaDonThanhToanRepository.findActiveBillByBanAn(req.getIdBanAn());
        }

        HoaDonThanhToan hoaDon;

        if (existingBill.isPresent()) {
            hoaDon = existingBill.get();
        } else {
            hoaDon = new HoaDonThanhToan();
            hoaDon.setIdBanAn(banAn);
            hoaDon.setThoiGianTao(Instant.now());
            hoaDon.setTrangThaiHoaDon(4); // 4 = Đang phục vụ
            hoaDon.setTongTienChuaGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(BigDecimal.ZERO);

            if (req.getIdNhanVien() != null) {
                NhanVien nv = nhanVienRepository.findById(req.getIdNhanVien()).orElse(null);
                hoaDon.setIdNhanVien(nv);
            }
            hoaDon = hoaDonThanhToanRepository.save(hoaDon);

            // 📝 LOG: Tạo hóa đơn
            ghiLichSu(hoaDon, req.getIdNhanVien(), "Tạo hóa đơn", "Mở bàn " + banAn.getMaBan(), null, 4);

            banAn.setTrangThai(1); // Bàn chuyển sang Có khách
            banAnRepo.save(banAn);
        }

        Integer trangThaiHienTai = hoaDon.getTrangThaiHoaDon();

        // =======================================================
        // 2. ĐỒNG BỘ CHI TIẾT MÓN ĂN
        // =======================================================
        List<ChiTietHoaDon> existingDetails = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId());
        if (existingDetails == null) existingDetails = new ArrayList<>();

        List<ChiTietHoaDon> itemsToKeep = new ArrayList<>();
        BigDecimal tongTienMoi = BigDecimal.ZERO;

        List<ChiTietHoaDonRequest> requestDetails = req.getChiTietHoaDon();
        if (requestDetails == null) requestDetails = new ArrayList<>();

        for (ChiTietHoaDonRequest itemReq : requestDetails) {
            Optional<ChiTietHoaDon> matchOpt = existingDetails.stream().filter(dbItem -> {
                if (itemReq.getId() != null && itemReq.getId().equals(dbItem.getId())) return true;
                boolean isFoodMatch = itemReq.getIdChiTietMonAn() != null && dbItem.getIdChiTietMonAn() != null
                        && itemReq.getIdChiTietMonAn().equals(dbItem.getIdChiTietMonAn().getId());
                boolean isSetMatch = itemReq.getIdSetLau() != null && dbItem.getIdSetLau() != null
                        && itemReq.getIdSetLau().equals(dbItem.getIdSetLau().getId());
                return isFoodMatch || isSetMatch;
            }).findFirst();

            ChiTietHoaDon chiTiet;
            BigDecimal donGiaGoc = BigDecimal.ZERO;

            if (matchOpt.isPresent()) {
                // MÓN CŨ ĐÃ CÓ TRONG DB
                chiTiet = matchOpt.get();
                donGiaGoc = chiTiet.getDonGiaTaiThoiDiemBan();

                if (chiTiet.getTrangThaiMon() != null && chiTiet.getTrangThaiMon() == 0) {
                    chiTiet.setTrangThaiMon(1);
                    ghiLichSu(hoaDon, req.getIdNhanVien(),
                            "Gọi lại món: " + getTenMonFromEntity(chiTiet) + " x" + itemReq.getSoLuong(),
                            "Khách gọi lại món đã hủy", trangThaiHienTai, trangThaiHienTai);
                }

                chiTiet.setSoLuong(itemReq.getSoLuong());
                chiTiet.setGhiChuMon(itemReq.getGhiChu());
            } else {
                // MÓN MỚI TOANH 100%
                chiTiet = new ChiTietHoaDon();
                chiTiet.setIdHoaDon(hoaDon);

                // 🚨 KIỂM TRA CHẶT CHẼ TRÁNH LỖI NULL
                if (itemReq.getIdChiTietMonAn() != null) {
                    DanhMucChiTiet monAn = chiTietDanhMucChiTietRepository.findById(itemReq.getIdChiTietMonAn())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn ID " + itemReq.getIdChiTietMonAn()));
                    chiTiet.setIdChiTietMonAn(monAn);
                    donGiaGoc = monAn.getGiaBan();
                } else if (itemReq.getIdSetLau() != null) {
                    SetLau setLau = setLauRepository.findById(itemReq.getIdSetLau())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Set lẩu ID " + itemReq.getIdSetLau()));
                    chiTiet.setIdSetLau(setLau);
                    donGiaGoc = setLau.getGiaBan();
                } else {
                    throw new RuntimeException("Dữ liệu không hợp lệ: Phải truyền ID Món hoặc ID Set");
                }

                chiTiet.setDonGiaTaiThoiDiemBan(donGiaGoc);
                chiTiet.setTrangThaiMon(1);
                chiTiet.setNgayGioTao(LocalDateTime.now());
                chiTiet.setSoLuong(itemReq.getSoLuong());
                chiTiet.setGhiChuMon(itemReq.getGhiChu());

                // 📝 LOG: Gọi món mới
                System.out.println("✅ Chuẩn bị thêm món mới: " + getTenMonFromEntity(chiTiet));
                ghiLichSu(hoaDon, req.getIdNhanVien(),
                        "Gọi món: " + getTenMonFromEntity(chiTiet) + " x" + itemReq.getSoLuong(),
                        "Khách gọi món", trangThaiHienTai, trangThaiHienTai);
            }

            BigDecimal thanhTien = donGiaGoc.multiply(BigDecimal.valueOf(itemReq.getSoLuong()));
            chiTiet.setThanhTien(thanhTien);

            // LƯU CHI TIẾT
            chiTiet = chiTietHoaDonRepository.save(chiTiet);
            itemsToKeep.add(chiTiet);
            tongTienMoi = tongTienMoi.add(thanhTien);
        }

        // =======================================================
        // 3. XÓA MỀM CÁC MÓN BỊ LOẠI KHỎI GIỎ HÀNG
        // =======================================================
        List<ChiTietHoaDon> toDelete = existingDetails.stream()
                .filter(oldItem -> oldItem.getTrangThaiMon() != null && oldItem.getTrangThaiMon() != 0)
                .filter(oldItem -> !itemsToKeep.contains(oldItem))
                .collect(Collectors.toList());

        for (ChiTietHoaDon delItem : toDelete) {
            ghiLichSu(hoaDon, req.getIdNhanVien(),
                    "Hủy món: " + getTenMonFromEntity(delItem) + " x" + delItem.getSoLuong(),
                    "Nhân viên xóa trực tiếp", trangThaiHienTai, trangThaiHienTai);

            delItem.setTrangThaiMon(0);
            chiTietHoaDonRepository.save(delItem);
        }

        // =======================================================
        // 4. CHỐT TỔNG TIỀN VÀ LƯU HÓA ĐƠN
        // =======================================================
        // 4.1. Lấy lại danh sách món MỚI NHẤT từ DB (Chỉ lấy món chưa hủy: trangThaiMon != 0)
        List<ChiTietHoaDon> dsMonHienTai = chiTietHoaDonRepository.findByIdHoaDon_Id(hoaDon.getId())
                .stream()
                .filter(item -> item.getTrangThaiMon() != null && item.getTrangThaiMon() != 0)
                .collect(Collectors.toList());

        // 4.2. Tính tổng tiền món ăn (Chưa giảm)
        BigDecimal tongTienChuaGiam = dsMonHienTai.stream()
                .map(ChiTietHoaDon::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 🚨 BƯỚC QUAN TRỌNG 1: Lưu tổng tiền món vào DB trước
        hoaDon.setTongTienChuaGiam(tongTienChuaGiam);
        hoaDon = hoaDonThanhToanRepository.save(hoaDon);

        // 🚨 BƯỚC QUAN TRỌNG 2: Gọi hàm kiểm tra Voucher
        // Lúc này DB đã có tổng tiền mới, hàm kiểm tra sẽ đối chiếu cực kỳ chuẩn xác
        phieuGiamGiaService.kiemTraLaiDieuKienVoucher(hoaDon.getId());

        // 🚨 BƯỚC QUAN TRỌNG 3: Fetch lại hóa đơn từ DB
        // Vì nếu khách bị rớt Voucher, hàm trên đã lén set soTienDaGiam = 0 rồi. Phải lấy lại để tính toán!
        hoaDon = hoaDonThanhToanRepository.findById(hoaDon.getId())
                .orElseThrow(() -> new RuntimeException("Lỗi đồng bộ hóa đơn"));

        // 4.3. Lấy các thông số phụ phí CẬP NHẬT NHẤT
        BigDecimal giamGia = hoaDon.getSoTienDaGiam() != null ? hoaDon.getSoTienDaGiam() : BigDecimal.ZERO;
        BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;

        // Lấy % VAT
        BigDecimal vatPhanTram = hoaDon.getVatApDung() != null ? BigDecimal.valueOf(hoaDon.getVatApDung()) : BigDecimal.TEN;

        // 4.4. Công thức toán học
        // A. Tiền sau giảm = Tổng tiền - Giảm giá
        BigDecimal tienSauGiam = tongTienChuaGiam.subtract(giamGia);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) tienSauGiam = BigDecimal.ZERO;

        // B. Tiền VAT = Tiền sau giảm * (VAT / 100)
        BigDecimal tienVat = tienSauGiam.multiply(vatPhanTram).divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);

        // C. Cần thanh toán = Tiền sau giảm + VAT - Tiền cọc đã đưa
        BigDecimal tongTienThanhToan = tienSauGiam.add(tienVat).subtract(tienCoc);

        // Nếu cọc nhiều hơn tiền ăn -> Không cần thu thêm
        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongTienThanhToan = BigDecimal.ZERO;
        }

        // 4.5. Cập nhật lại thực thể và Lưu
        hoaDon.setTongTienThanhToan(tongTienThanhToan);
        hoaDonThanhToanRepository.save(hoaDon);

        System.out.println("✅ Đã cập nhật lại tài chính cho Hóa đơn ID: " + hoaDon.getId() + " | Giảm giá: " + giamGia + " | Cần trả thêm: " + tongTienThanhToan);
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
}
