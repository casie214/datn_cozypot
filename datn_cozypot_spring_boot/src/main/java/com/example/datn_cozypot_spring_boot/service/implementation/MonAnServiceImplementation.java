package com.example.datn_cozypot_spring_boot.service.implementation;

// Các DTO cũ (MonAn, SetLau, DanhMuc...) giữ nguyên
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongItemRequest;
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongResponse;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DinhLuongSimpleResponse;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DonViRequest;
import com.example.datn_cozypot_spring_boot.dto.DonVi.DonViResponse;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnInSetResponse;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;

import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.DonViRepository;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonAnServiceImplementation implements MonAnService {

    // Repositories
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucRepository danhMucRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DinhLuongRepository dinhLuongRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository danhMucChiTietRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.LoaiLauRepository loaiSetLauRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository setLauRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.ChiTietSetLauRepository chiTietSetLauRepository;
    private final DonViRepository donViRepository; // Repository mới cho bảng Cha

    // --- UTILS ---
    private String generatePrefixFromData(String name) {
        if (name == null || name.isEmpty()) return "XX";
        String temp = Normalizer.normalize(name, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String unAccent = pattern.matcher(temp).replaceAll("").replaceAll("đ", "d").replaceAll("Đ", "D");
        String[] words = unAccent.split("\\s+");
        StringBuilder prefix = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) prefix.append(w.charAt(0));
        }
        return prefix.toString().toUpperCase();
    }

    private String generateNextCode(String name, String entityType) {
        String prefix = generatePrefixFromData(name);
        String lastCode = null;

        switch (entityType) {
            case "DANH_MUC":
                lastCode = danhMucRepository.findMaxCodeByPrefix(prefix); break;
            case "MON_AN":
                lastCode = danhMucChiTietRepository.findMaxCodeByPrefix(prefix); break;
            case "SET_LAU":
                lastCode = setLauRepository.findMaxCodeByPrefix(prefix); break;
            case "LOAI_SET":
                lastCode = loaiSetLauRepository.findMaxCodeByPrefix(prefix); break;
            default: return prefix + "01";
        }

        if (lastCode == null) return prefix + "01";
        try {
            int number = Integer.parseInt(lastCode.substring(prefix.length())) + 1;
            return prefix + String.format("%02d", number);
        } catch (Exception e) { return prefix + "01"; }
    }

    // ========================================================================
    // 1. DANH MỤC
    // ========================================================================

    @Override
    public List<DanhMucResponse> getAllDanhMuc() {
        return danhMucRepository.findAll().stream().map(e -> {
            DanhMucResponse res = new DanhMucResponse();
            res.setId(e.getId());
            res.setMaDanhMuc(e.getMaDanhMuc());
            res.setTenDanhMuc(e.getTenDanhMuc());
            res.setSoLuongMon(e.getSoLuongMon() != null ? e.getSoLuongMon() : 0);
            res.setMoTa(e.getMoTa());
            res.setTrangThai(e.getTrangThai());
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public DanhMucResponse createDanhMuc(DanhMucRequest request) {
        DanhMuc d = new DanhMuc();
        d.setTenDanhMuc(request.getTenDanhMuc());
        d.setMoTa(request.getMoTa());
        d.setTrangThai(1);
        d.setMaDanhMuc(generateNextCode(request.getTenDanhMuc(), "DANH_MUC"));
        d.setNgayTao(Instant.now());
        danhMucRepository.save(d);

        DanhMucResponse res = new DanhMucResponse();
        res.setId(d.getId());
        res.setTenDanhMuc(d.getTenDanhMuc());
        res.setMoTa(d.getMoTa());
        return res;
    }

    @Override
    public DanhMucResponse updateDanhMuc(Integer id, DanhMucRequest request) {
        DanhMuc d = danhMucRepository.findById(id).orElseThrow();
        d.setTenDanhMuc(request.getTenDanhMuc());
        d.setTrangThai(request.getTrangThai());
        d.setMoTa(request.getMoTa());
        danhMucRepository.save(d);
        return new DanhMucResponse();
    }

    @Override
    public void deleteDanhMuc(Integer id) {
        DanhMuc d = danhMucRepository.findById(id).orElseThrow();
        d.setTrangThai(0);
        danhMucRepository.save(d);
    }

    @Override
    public List<DanhMucResponse> findDanhMucActive() {
        return danhMucRepository.findByTrangThai(1).stream()
                .map(e -> {
                    DanhMucResponse res = new DanhMucResponse();
                    res.setId(e.getId());
                    res.setMaDanhMuc(e.getMaDanhMuc());
                    res.setTenDanhMuc(e.getTenDanhMuc());
                    res.setMoTa(e.getMoTa());
                    return res;
                }).collect(Collectors.toList());
    }

    // ========================================================================
    // 2. ĐƠN VỊ & ĐỊNH LƯỢNG (LOGIC MỚI: CHA - CON)
    // ========================================================================

    @Override
    public List<DonViResponse> getAllDonVi() {
        List<DonVi> list = donViRepository.findAll();

        return list.stream().map(dv -> {
            DonViResponse res = new DonViResponse();
            res.setId(dv.getId());
            res.setTenDonVi(dv.getTenDonVi());
            res.setMoTa(dv.getMoTa());

            // 1. Map danh sách con (DinhLuong)
            List<DinhLuongSimpleResponse> childs = dv.getListDinhLuong().stream()
                    .map(dl -> new DinhLuongSimpleResponse(dl.getId(), dl.getGiaTri(), dl.getTenHienThi(), dl.getTrangThai()))
                    .collect(Collectors.toList());
            res.setValues(childs);

            // 2. Map danh sách ID Danh Mục (PHẦN THÊM MỚI)
            // Giả sử trong Entity DonVi bạn đặt tên biến quan hệ là listDanhMuc
            if (dv.getListDanhMuc() != null) {
                List<Integer> categoryIds = dv.getListDanhMuc().stream()
                        .map(dm -> dm.getId()) // Lấy ID của từng DanhMuc
                        .collect(Collectors.toList());
                res.setListIdDanhMuc(categoryIds);
            } else {
                res.setListIdDanhMuc(new ArrayList<>()); // Trả về mảng rỗng thay vì null để tránh lỗi FE
            }

            return res;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DonViResponse createDonVi(DonViRequest req) {
        if (donViRepository.existsByTenDonVi(req.getTenDonVi())) {
            throw new RuntimeException("Tên đơn vị đã tồn tại!");
        }

        DonVi donVi = new DonVi();
        donVi.setTenDonVi(req.getTenDonVi());
        donVi.setMoTa(req.getMoTa());

        // 1. XỬ LÝ LIÊN KẾT DANH MỤC (MỚI)
        if (req.getListIdDanhMuc() != null && !req.getListIdDanhMuc().isEmpty()) {
            List<DanhMuc> danhMucs = danhMucRepository.findAllById(req.getListIdDanhMuc());
            donVi.setListDanhMuc(danhMucs);
        }

        // 2. XỬ LÝ GIÁ TRỊ CON (Giữ nguyên logic cũ)
        if (req.getValues() != null) {
            for (DinhLuongItemRequest valDto : req.getValues()) {
                DinhLuong dl = new DinhLuong();
                dl.setGiaTri(valDto.getGiaTri());
                dl.setTenHienThi(valDto.getGiaTri() + " " + req.getTenDonVi());
                dl.setDonVi(donVi);
                donVi.getListDinhLuong().add(dl);
            }
        }

        DonVi saved = donViRepository.save(donVi);
        return mapToDonViResponse(saved);
    }

    @Override
    @Transactional
    public DonViResponse updateDonVi(Integer id, DonViRequest req) {
        DonVi donVi = donViRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị ID: " + id));

        donVi.setTenDonVi(req.getTenDonVi());
        donVi.setMoTa(req.getMoTa());

        // 1. CẬP NHẬT LIÊN KẾT DANH MỤC (MỚI)
        if (req.getListIdDanhMuc() != null) {
            // Thay thế hoàn toàn list cũ bằng list mới gửi lên
            List<DanhMuc> danhMucs = danhMucRepository.findAllById(req.getListIdDanhMuc());
            donVi.setListDanhMuc(danhMucs);
        } else {
            // Nếu gửi null -> Xóa hết liên kết (hoặc giữ nguyên tùy nghiệp vụ, ở đây mình chọn xóa hết)
            donVi.getListDanhMuc().clear();
        }

        // 2. CẬP NHẬT GIÁ TRỊ CON (Logic Merge List đã viết ở câu trước - giữ nguyên)
        updateDinhLuongChilds(donVi, req.getValues()); // (Mình tách ra hàm riêng cho gọn code bên dưới)

        DonVi saved = donViRepository.save(donVi);
        return mapToDonViResponse(saved);
    }

    @Override
    public List<DonViResponse> getDonViByCategoryId(Integer id) {
        // 1. Lấy danh sách đơn vị theo danh mục
        List<DonVi> list = donViRepository.findByListDanhMuc_Id(id);

        // 2. Map sang Response nhưng LỌC BỎ các giá trị con bị tắt (trangThai != 1)
        return list.stream().map(dv -> {
            DonViResponse res = new DonViResponse();
            res.setId(dv.getId());
            res.setTenDonVi(dv.getTenDonVi());
            res.setMoTa(dv.getMoTa());

            // --- LOGIC LỌC: Chỉ lấy DinhLuong có trangThai == 1 ---
            List<DinhLuongSimpleResponse> activeChilds = dv.getListDinhLuong().stream()
                    .filter(dl -> dl.getTrangThai() == 1) // <--- LỌC TẠI ĐÂY
                    .map(dl -> new DinhLuongSimpleResponse(
                            dl.getId(),
                            dl.getGiaTri(),
                            dl.getTenHienThi(),
                            dl.getTrangThai()
                    ))
                    .collect(Collectors.toList());

            res.setValues(activeChilds);

            // Map danh sách ID danh mục (nếu cần)
            if (dv.getListDanhMuc() != null) {
                res.setListIdDanhMuc(dv.getListDanhMuc().stream().map(DanhMuc::getId).collect(Collectors.toList()));
            } else {
                res.setListIdDanhMuc(new ArrayList<>());
            }

            return res;
        }).collect(Collectors.toList());
    }

    public void updateDinhLuongSingle(Integer id, DinhLuongItemRequest req) {
        DinhLuong dl = dinhLuongRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy"));
        dl.setGiaTri(req.getGiaTri());
        // Tự động cập nhật lại tên hiển thị cho đồng bộ
        dl.setTenHienThi(req.getGiaTri() + " " + dl.getDonVi().getTenDonVi());
        dl.setTrangThai(req.getTrangThai());
        dinhLuongRepository.save(dl);
    }

    // Helper: Logic xử lý list con (để hàm updateDonVi đỡ dài)
    private void updateDinhLuongChilds(DonVi donVi, List<DinhLuongItemRequest> incomingValues) {
        if (incomingValues == null) incomingValues = new ArrayList<>();
        List<DinhLuong> currentValues = donVi.getListDinhLuong();

        List<Integer> incomingIds = incomingValues.stream()
                .map(DinhLuongItemRequest::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        currentValues.removeIf(existing -> !incomingIds.contains(existing.getId()));

        for (DinhLuongItemRequest dto : incomingValues) {
            if (dto.getId() == null) {
                DinhLuong newDl = new DinhLuong();
                newDl.setGiaTri(dto.getGiaTri());
                newDl.setTenHienThi(dto.getGiaTri() + " " + donVi.getTenDonVi());
                newDl.setDonVi(donVi);
                currentValues.add(newDl);
            } else {
                currentValues.stream()
                        .filter(dl -> dl.getId().equals(dto.getId()))
                        .findFirst()
                        .ifPresent(existing -> {
                            existing.setGiaTri(dto.getGiaTri());
                            existing.setTenHienThi(dto.getGiaTri() + " " + donVi.getTenDonVi());
                        });
            }
        }
    }

    // Helper map DonVi -> Response
    private DonViResponse mapToDonViResponse(DonVi entity) {
        DonViResponse res = new DonViResponse();
        res.setId(entity.getId());
        res.setTenDonVi(entity.getTenDonVi());
        res.setMoTa(entity.getMoTa());

        if (entity.getListDinhLuong() != null) {
            res.setValues(entity.getListDinhLuong().stream()
                    .map(dl -> new DinhLuongSimpleResponse(dl.getId(), dl.getGiaTri(), dl.getTenHienThi(), dl.getTrangThai()))
                    .collect(Collectors.toList()));
        }

        // --- QUAN TRỌNG: MAP DANH MỤC ---
        if (entity.getListDanhMuc() != null) {
            List<Integer> dmIds = entity.getListDanhMuc().stream()
                    .map(DanhMuc::getId) // Lấy ID từ list danh mục
                    .collect(Collectors.toList());
            res.setListIdDanhMuc(dmIds);
        }

        List<DinhLuongSimpleResponse> childs = entity.getListDinhLuong().stream()
                .map(dl -> new DinhLuongSimpleResponse(dl.getId(), dl.getGiaTri(), dl.getTenHienThi(), dl.getTrangThai()))
                .collect(Collectors.toList());
        res.setValues(childs);

        return res;
    }

    // --- CÁC HÀM GET ĐỊNH LƯỢNG (PHỤC VỤ COMBOBOX MÓN ĂN) ---
    // Cần sửa lại logic này nếu cấu trúc DB thay đổi hoàn toàn.
    // Tạm thời nếu vẫn dùng bảng 'dinh_luong' thì vẫn query được bình thường.
    public List<DinhLuongSimpleResponse> getDinhLuongByDonVi(Integer donViId) {
        DonVi donVi = donViRepository.findById(donViId).orElseThrow();
        return donVi.getListDinhLuong().stream()
                .map(dl -> new DinhLuongSimpleResponse(dl.getId(), dl.getGiaTri(), dl.getTenHienThi(), dl.getTrangThai()))
                .collect(Collectors.toList());
    }

    // ========================================================================
    // 3. MÓN ĂN (DanhMucChiTiet)
    // ========================================================================

    @Override
    public List<MonAnResponse> getAllMonAn() {
        return danhMucChiTietRepository.findAll().stream().map(this::mapToMonAnResponse).collect(Collectors.toList());
    }

    @Override
    public MonAnResponse getMonAnById(Integer id) {
        return mapToMonAnResponse(danhMucChiTietRepository.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public MonAnResponse createMonAn(MonAnRequest request) {
        // 1. Kiểm tra trùng lặp (Tên + Danh mục + Định lượng)
        String tenMonChuan = request.getTenMon().trim();
        if (danhMucChiTietRepository.existsByTenMonAndDanhMucIdAndDinhLuongId(
                tenMonChuan,
                request.getIdDanhMuc(),
                request.getIdDinhLuong()
        )) {
            throw new RuntimeException("Biến thể món ăn này đã tồn tại trong hệ thống!");
        }

        DanhMucChiTiet mon = new DanhMucChiTiet();
        mon.setTenMon(request.getTenMon());
        mon.setGiaBan(request.getGiaBan());
        mon.setGiaVon(request.getGiaVon());
        mon.setTrangThai(1);
        mon.setNgayTao(LocalDateTime.now()); // Set ngày tạo nếu cần

        // 2. Sinh mã tự động (Có check trùng trong DB)
        String baseCode = generateNextCode(request.getTenMon(), "MON_AN");
        String finalCode = baseCode;
        int suffix = 1;
        while (danhMucChiTietRepository.existsByMaMon(finalCode)) {
            finalCode = baseCode + "-" + suffix;
            suffix++;
        }
        mon.setMaMon(finalCode);

        // 3. Tìm Danh mục và Định lượng
        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + request.getIdDanhMuc()));

        DinhLuong dinhLuong = dinhLuongRepository.findById(request.getIdDinhLuong())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy định lượng ID: " + request.getIdDinhLuong()));

        // --- VALIDATION QUAN TRỌNG: Check trạng thái định lượng ---
        if (dinhLuong.getTrangThai() != 1) {
            throw new RuntimeException("Kích cỡ/Định lượng này đang ngừng hoạt động. Vui lòng chọn cái khác!");
        }
        // ----------------------------------------------------------

        mon.setDanhMuc(danhMuc);
        mon.setDinhLuong(dinhLuong);

        // 4. LOGIC MỚI: TỰ ĐỘNG LIÊN KẾT ĐƠN VỊ (CHA) - DANH MỤC (don_vi_danh_muc)
        // Lấy Đơn vị cha của định lượng này (VD: ml)
        DonVi donVi = dinhLuong.getDonVi();

        if (donVi != null) {
            // Kiểm tra xem Đơn vị này (ml) đã được gán cho Danh mục này (Đồ uống) chưa
            boolean alreadyLinked = donVi.getListDanhMuc().stream()
                    .anyMatch(dm -> dm.getId().equals(danhMuc.getId()));

            if (!alreadyLinked) {
                // Nếu chưa có liên kết, ta tự động thêm vào bảng don_vi_danh_muc
                donVi.getListDanhMuc().add(danhMuc);
                donViRepository.save(donVi);
            }
        }

        // 5. Lưu món ăn
        return mapToMonAnResponse(danhMucChiTietRepository.save(mon));
    }

    // ========================================================================
    // UPDATE MÓN ĂN (Full Logic)
    // ========================================================================
    @Override
    @Transactional
    public MonAnResponse updateMonAn(Integer id, MonAnRequest request) {
        // 1. Tìm món ăn hiện tại
        DanhMucChiTiet mon = danhMucChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món ăn ID: " + id));

        // 2. Cập nhật các thông tin cơ bản
        mon.setTenMon(request.getTenMon());
        mon.setGiaBan(request.getGiaBan());
        mon.setGiaVon(request.getGiaVon());
        mon.setTrangThai(request.getTrangThai());
        mon.setMoTa(request.getMoTa());
        mon.setNgaySua(LocalDateTime.now()); // Update ngày sửa

        if (request.getHinhAnh() != null) {
            mon.setHinhAnh(request.getHinhAnh());
        }

        // 3. Cập nhật Danh mục (nếu có thay đổi)
        if (!mon.getDanhMuc().getId().equals(request.getIdDanhMuc())) {
            DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục ID: " + request.getIdDanhMuc()));
            mon.setDanhMuc(danhMuc);
        }

        // 4. Cập nhật Định lượng (nếu có thay đổi)
        if (!mon.getDinhLuong().getId().equals(request.getIdDinhLuong())) {
            DinhLuong dinhLuong = dinhLuongRepository.findById(request.getIdDinhLuong())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy định lượng ID: " + request.getIdDinhLuong()));

            // --- VALIDATION QUAN TRỌNG: Check trạng thái định lượng ---
            if (dinhLuong.getTrangThai() != 1) {
                throw new RuntimeException("Kích cỡ/Định lượng này đang ngừng hoạt động. Vui lòng chọn cái khác!");
            }
            // ----------------------------------------------------------

            mon.setDinhLuong(dinhLuong);

            // 5. LOGIC MỚI: TỰ ĐỘNG CẬP NHẬT LIÊN KẾT ĐƠN VỊ - DANH MỤC
            // Logic: Khi đổi định lượng mới -> Check xem Đơn vị cha của nó đã link với Danh mục hiện tại của món chưa
            DonVi donVi = dinhLuong.getDonVi();
            DanhMuc currentDanhMuc = mon.getDanhMuc(); // Lấy danh mục hiện tại (vừa set ở bước 3 hoặc lấy cũ)

            if (donVi != null && currentDanhMuc != null) {
                boolean alreadyLinked = donVi.getListDanhMuc().stream()
                        .anyMatch(dm -> dm.getId().equals(currentDanhMuc.getId()));

                if (!alreadyLinked) {
                    // Tự động link nếu chưa có
                    donVi.getListDanhMuc().add(currentDanhMuc);
                    donViRepository.save(donVi);
                }
            }
        }

        // 6. Lưu và trả về kết quả
        return mapToMonAnResponse(danhMucChiTietRepository.save(mon));
    }

    @Override
    public void deleteMonAn(Integer id) {
        DanhMucChiTiet mon = danhMucChiTietRepository.findById(id).orElseThrow();
        mon.setTrangThai(0);
        danhMucChiTietRepository.save(mon);
    }

    @Override
    public List<MonAnResponse> findMonAnActive() {
        return danhMucChiTietRepository.findByTrangThai(1).stream()
                .map(this::mapToMonAnResponse)
                .collect(Collectors.toList());
    }

    private MonAnResponse mapToMonAnResponse(DanhMucChiTiet entity) {
        MonAnResponse res = new MonAnResponse();
        res.setId(entity.getId());
        res.setMaMon(entity.getMaMon());
        res.setTrangThai(entity.getTrangThai());
        res.setTenMon(entity.getTenMon());
        res.setGiaBan(entity.getGiaBan());
        res.setHinhAnh(entity.getHinhAnh());
        res.setMoTa(entity.getMoTa());

        if (entity.getDanhMuc() != null) {
            res.setTenDanhMuc(entity.getDanhMuc().getTenDanhMuc());
            res.setIdDanhMuc(entity.getDanhMuc().getId());
        }

        if (entity.getDinhLuong() != null) {
            res.setTenDinhLuong(entity.getDinhLuong().getTenHienThi());
            // Logic cũ: kichCo và dinhLuongText
            // Logic mới: giaTri (trong bảng DinhLuong) và tenDonVi (trong bảng DonVi cha)
            res.setGiaTriDinhLuong(entity.getDinhLuong().getGiaTri());

            // Nếu muốn lấy tên đơn vị cha (ml, L...)
            if (entity.getDinhLuong().getDonVi() != null) {
                res.setKichCo(entity.getDinhLuong().getDonVi().getTenDonVi());
            }
            res.setIdDinhLuong(entity.getDinhLuong().getId());
        }

        return res;
    }

    // ========================================================================
    // 4. SET LẨU
    // ========================================================================

    @Override
    public List<LoaiLauResponse> getAllLoaiSetLau() {
        return loaiSetLauRepository.findAll().stream().map(e -> {
            LoaiLauResponse res = new LoaiLauResponse();
            res.setId(e.getId());
            res.setTenLoaiSet(e.getTenLoaiSet());
            res.setMaLoaiSet(e.getMaLoaiSet());
            res.setTrangThai(e.getTrangThai());
            res.setMoTa(e.getMoTa());
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public LoaiLauResponse createLoaiSetLau(LoaiLauRequest request) {
        LoaiSetLau loai = new LoaiSetLau();
        loai.setTenLoaiSet(request.getTenLoaiSet());
        loai.setTrangThai(1);
        loai.setMaLoaiSet(generateNextCode(request.getTenLoaiSet(), "LOAI_SET"));
        loaiSetLauRepository.save(loai);
        return new LoaiLauResponse();
    }

    @Override
    public LoaiLauResponse updateLoaiSetLau(Integer id, LoaiLauRequest request) {
        LoaiSetLau loai = loaiSetLauRepository.findById(id).orElseThrow();
        loai.setTenLoaiSet(request.getTenLoaiSet());
        loai.setMoTa(request.getMoTa());
        loai.setTrangThai(request.getTrangThai());
        loaiSetLauRepository.save(loai);
        return new LoaiLauResponse();
    }

    @Override
    public List<LoaiLauResponse> findLoaiSetLauActive() {
        return loaiSetLauRepository.findByTrangThai(1).stream()
                .map(e -> {
                    LoaiLauResponse res = new LoaiLauResponse();
                    res.setId(e.getId());
                    res.setTenLoaiSet(e.getTenLoaiSet());
                    return res;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SetLauResponse> getAllSetLau() {
        return setLauRepository.findAll().stream().map(this::mapToSetLauResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SetLauResponse getSetLauById(Integer id) {
        return mapToSetLauResponse(setLauRepository.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public List<SetLauResponse> findSetLauActive() {
        return setLauRepository.findByTrangThai(1).stream()
                .map(this::mapToSetLauResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SetLauResponse createSetLau(SetLauRequest request) {
        SetLau set = new SetLau();
        set.setTenSetLau(request.getTenSetLau());
        set.setGiaBan(request.getGiaBan());
        set.setMoTa(request.getMoTa());
        set.setHinhAnh(request.getHinhAnh());
        set.setTrangThai(1);
        set.setMaSetLau(generateNextCode(request.getTenSetLau(), "SET_LAU"));

        if (request.getIdLoaiSet() != null) {
            set.setIdLoaiSet(loaiSetLauRepository.findById(request.getIdLoaiSet()).orElseThrow());
        }

        SetLau savedSet = setLauRepository.save(set);

        if (request.getChiTietMonAn() != null) {
            List<ChiTietSetLau> details = new ArrayList<>();
            for (SetLauRequest.ChiTietSetItemRequest itemReq : request.getChiTietMonAn()) {
                ChiTietSetLau detail = new ChiTietSetLau();
                detail.setSetLau(savedSet);
                detail.setSoLuong(itemReq.getSoLuong());
                detail.setMonAn(danhMucChiTietRepository.findById(itemReq.getIdMonAn()).orElseThrow());
                details.add(detail);
            }
            chiTietSetLauRepository.saveAll(details);
            savedSet.setListChiTietSetLau(details);
        }
        return mapToSetLauResponse(savedSet);
    }

    @Override
    @Transactional
    public SetLauResponse updateSetLau(Integer id, SetLauRequest request) {
        SetLau set = setLauRepository.findById(id).orElseThrow();
        set.setTenSetLau(request.getTenSetLau());
        set.setGiaBan(request.getGiaBan());
        set.setMoTa(request.getMoTa());
        if(request.getHinhAnh() != null) set.setHinhAnh(request.getHinhAnh());

        if (request.getIdLoaiSet() != null) {
            set.setIdLoaiSet(loaiSetLauRepository.findById(request.getIdLoaiSet()).orElseThrow());
        }

        if (request.getChiTietMonAn() != null) {
            if (set.getListChiTietSetLau() != null) {
                set.getListChiTietSetLau().clear();
            } else {
                set.setListChiTietSetLau(new ArrayList<>());
            }
            for (SetLauRequest.ChiTietSetItemRequest itemReq : request.getChiTietMonAn()) {
                ChiTietSetLau detail = new ChiTietSetLau();
                detail.setSetLau(set);
                detail.setSoLuong(itemReq.getSoLuong());
                detail.setMonAn(danhMucChiTietRepository.findById(itemReq.getIdMonAn()).orElseThrow());
                set.getListChiTietSetLau().add(detail);
            }
        }
        return mapToSetLauResponse(setLauRepository.save(set));
    }

    @Override
    public List<SetLauResponse> findSetLauTop(int metric) {
        int limit = (metric > 0) ? metric : 5;
        return findSetLauActive().stream().limit(limit).collect(Collectors.toList());
    }

    private SetLauResponse mapToSetLauResponse(SetLau entity) {
        SetLauResponse res = new SetLauResponse();
        res.setId(entity.getId());
        res.setMaSetLau(entity.getMaSetLau());
        res.setTenSetLau(entity.getTenSetLau());
        res.setGiaBan(entity.getGiaBan());
        res.setHinhAnh(entity.getHinhAnh());
        res.setMoTa(entity.getMoTa());
        res.setMoTaChiTiet(entity.getMoTaChiTiet());
        res.setTrangThai(entity.getTrangThai());

        if (entity.getIdLoaiSet() != null) {
            res.setTenLoaiSet(entity.getIdLoaiSet().getTenLoaiSet());
            res.setIdLoaiSet(entity.getIdLoaiSet().getId());
        }

        List<MonAnInSetResponse> monAns = new ArrayList<>();
        if (entity.getListChiTietSetLau() != null) {
            for (ChiTietSetLau ct : entity.getListChiTietSetLau()) {
                MonAnInSetResponse item = new MonAnInSetResponse();
                if (ct.getMonAn() != null) {
                    item.setIdMon(ct.getMonAn().getId());
                    item.setTenMon(ct.getMonAn().getTenMon());
                    item.setHinhAnhMon(ct.getMonAn().getHinhAnh());
                    item.setGiaBan(ct.getMonAn().getGiaBan());
                    if (ct.getMonAn().getDinhLuong() != null) {
                        item.setDinhLuong(ct.getMonAn().getDinhLuong().getTenHienThi());
                    }
                }
                item.setSoLuong(ct.getSoLuong());
                monAns.add(item);
            }
        }
        res.setDanhSachMon(monAns);
        return res;
    }
}