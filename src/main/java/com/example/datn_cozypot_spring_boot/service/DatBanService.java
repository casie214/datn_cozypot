package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.request.*;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
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
    private EmailDatBanService emailDatBanService;


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


        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.unsorted()   // QUAN TRỌNG
        );

        Page<PhieuDatBan> page = phieuDatBanRepository.search(
                request.getSoDienThoai(),
                request.getTrangThai(),
                start,
                end,
                pageRequest
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
        // Vì trangThaiPhieu giờ là Integer nên set thẳng vào luôn
        if (request.getId() != null && request.getTrangThaiPhieu() != null) {
            PhieuDatBan phieu = phieuDatBanRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu đặt bàn"));

            phieu.setTrangThai(request.getTrangThaiPhieu());
            phieuDatBanRepository.save(phieu);
        }

        // 3. CẬP NHẬT HÓA ĐƠN
        // So sánh bằng == 3 (Giả sử 3 là trạng thái CHECKED_IN của phiếu đặt bàn)
        if (request.getId() != null && request.getTrangThaiPhieu() != null && request.getTrangThaiPhieu() == 3) {

            HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findByIdPhieuDatBan_Id(request.getId());

            if (hoaDon != null) {
                hoaDon.setTrangThaiHoaDon(4); // 4 = IN_PROGRESS (Đang phục vụ)
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

    @Transactional
    public CreatePhieuDatBanFullResponse createFull(CreatePhieuDatBanFullRequest req) {

        KhachHang khachHang;

        // ===============================
        // 1️⃣ Nếu chọn khách cũ
        // ===============================
        if (req.getIdKhachHang() != null) {

            khachHang = khachHangRepository.findById(req.getIdKhachHang())
                    .orElseThrow(() ->
                            new RuntimeException("Không tìm thấy khách hàng"));

        } else {

            // ===============================
            // 2️⃣ Nếu là khách mới
            // ===============================

            if (req.getSoDienThoai() == null || req.getSoDienThoai().isBlank()) {
                throw new RuntimeException("Số điện thoại không được để trống");
            }

            Optional<KhachHang> existing =
                    khachHangRepository.findBySoDienThoai(req.getSoDienThoai());

            if (existing.isPresent()) {
                khachHang = existing.get();
            } else {
                khachHang = new KhachHang();
                khachHang.setTenKhachHang(req.getTenKhachHang());
                khachHang.setSoDienThoai(req.getSoDienThoai());
                khachHang.setEmail(req.getEmail());
                khachHang.setNgaySinh(req.getNgaySinh());
                khachHang.setGioiTinh(req.getGioiTinh());
                khachHang.setTrangThai(1);
                khachHangRepository.save(khachHang);
            }
        }

        // ===============================
        // 3️⃣ Kiểm tra bàn
        // ===============================

        BanAn ban = banAnRepository.findById(req.getIdBanAn())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy bàn"));

        // ===============================
        // 4️⃣ Check trùng giờ
        // ===============================

        LocalDateTime start = req.getThoiGianDat().minusHours(2);
        LocalDateTime end   = req.getThoiGianDat().plusHours(2);

        boolean isTrung = phieuDatBanRepository.existsByTimeRange(ban, start, end);

        if (isTrung) {
            throw new RuntimeException("Bàn đã có lịch trong vòng 2 tiếng");
        }

        // ===============================
        // 5️⃣ Tạo phiếu
        // ===============================

        PhieuDatBan pdb = new PhieuDatBan();
        pdb.setIdBanAn(ban);
        pdb.setIdKhachHang(khachHang);
        pdb.setThoiGianDat(req.getThoiGianDat());
        pdb.setHinhThucDat(req.getHinhThucDat());
        pdb.setSoLuongKhach(req.getSoLuongKhach());
        pdb.setTrangThai(0);

        PhieuDatBan saved = phieuDatBanRepository.save(pdb);

        ban.setTrangThai(1);
        banAnRepository.save(ban);

        // ===============================
        // 6️⃣ Gửi mail + auto confirm
        // ===============================

        boolean daGuiMail = false;

        if (khachHang.getEmail() != null && !khachHang.getEmail().isBlank()) {

            EmailDatBanDTO emailDto = EmailDatBanDTO.builder()
                    .tenKhachHang(khachHang.getTenKhachHang())
                    .soDienThoai(khachHang.getSoDienThoai())
                    .email(khachHang.getEmail())
                    .thoiGianDat(req.getThoiGianDat()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .tenBan(ban.getMaBan())
                    .khuVuc(ban.getIdKhuVuc() != null
                            ? ban.getIdKhuVuc().getTenKhuVuc() + " - Tầng " + ban.getIdKhuVuc().getTang()
                            : "N/A")
                    .soLuongKhach(req.getSoLuongKhach())
                    .maPhieuDatBan("PDB-" + saved.getId())
                    .build();

            try {
                emailDatBanService.sendXacNhanDatBanSync(emailDto); // ← gọi bản sync

                // ✅ Gửi mail OK → tự động xác nhận phiếu
                saved.setTrangThai(1); // 1 = Đã xác nhận
                phieuDatBanRepository.save(saved);

                daGuiMail = true;
                log.info("✅ Tự động xác nhận phiếu PDB-{} sau khi gửi mail", saved.getId());

            } catch (Exception e) {
                // ❌ Gửi mail lỗi → giữ trạng thái 0, không crash API
                log.warn("⚠️ Gửi mail thất bại, phiếu PDB-{} giữ trạng thái chờ xác nhận: {}",
                        saved.getId(), e.getMessage());
            }

        } else {
            // Không có email → vẫn tạo phiếu nhưng trạng thái 0 chờ xác nhận thủ công
            log.info("ℹ️ Khách không có email, phiếu PDB-{} chờ xác nhận thủ công", saved.getId());
        }

        // ===============================
        // 7️⃣ Trả về response cho FE
        // ===============================

        return CreatePhieuDatBanFullResponse.builder()
                .idPhieuDatBan(saved.getId())
                .maPhieuDatBan("PDB-" + saved.getId())
                .daGuiMail(daGuiMail)
                .emailGuiToi(daGuiMail ? khachHang.getEmail() : null)
                .build();
    }

    // ========== Lấy tất cả khách hàng cho multiselect ==========
    public List<KhachHangSelectDTO> getAllForSelect() {
        return khachHangRepository.findAll()
                .stream()
                .map(this::toSelectDTO)
                .collect(Collectors.toList());
    }

    // ========== Tìm kiếm theo keyword (tên hoặc SĐT) ==========
    public List<KhachHangSelectDTO> searchByKeyword(String keyword) {
        return khachHangRepository.searchByKeyword(keyword)
                .stream()
                .map(this::toSelectDTO)
                .collect(Collectors.toList());
    }

    // ========== Tìm theo SĐT chính xác ==========
    public KhachHangSelectDTO findBySoDienThoai(String soDienThoai) {
        return khachHangRepository.findBySoDienThoai(soDienThoai)
                .map(this::toSelectDTO)
                .orElse(null);
    }

    // ========== Mapper Entity -> DTO ==========
    private KhachHangSelectDTO toSelectDTO(KhachHang k) {
        return KhachHangSelectDTO.builder()
                .idKhachHang(k.getId())
                .maKhachHang(k.getMaKhachHang())
                .tenKhachHang(k.getTenKhachHang())
                .soDienThoai(k.getSoDienThoai())
                .email(k.getEmail())
                .ngaySinh(k.getNgaySinh())
                .gioiTinh(k.getGioiTinh())
                .build();
    }




}
