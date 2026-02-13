package com.example.datn_cozypot_spring_boot.service.implementation;

import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongRequest;
import com.example.datn_cozypot_spring_boot.dto.DinhLuong.DinhLuongResponse;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnInSetResponse;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.MonAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.request.*;
import com.example.datn_cozypot_spring_boot.dto.response.*;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonAnServiceImplementation implements MonAnService {

    // Repositories (Đã cập nhật theo ERD mới)
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucRepository danhMucRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DinhLuongRepository dinhLuongRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DinhLuongChiTietRepository dinhLuongChiTietRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository danhMucChiTietRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.LoaiLauRepository loaiSetLauRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository setLauRepository;
    private final com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.ChiTietSetLauRepository chiTietSetLauRepository;

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
    // 1. DANH MỤC & ĐỊNH LƯỢNG
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
        return new DanhMucResponse(); // Return proper mapping
    }

    @Override
    public void deleteDanhMuc(Integer id) {
        DanhMuc d = danhMucRepository.findById(id).orElseThrow();
        d.setTrangThai(0);
        danhMucRepository.save(d);
    }

    @Override
    public List<DinhLuongResponse> getAllDinhLuong() {
        return dinhLuongRepository.findAll().stream().map(e -> {
            DinhLuongResponse res = new DinhLuongResponse();
            res.setId(e.getId());
            res.setTenHienThi(e.getTenHienThi());
            res.setKichCo(e.getKichCo());
            res.setDinhLuong(e.getDinhLuong());
            return res;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DinhLuongResponse> getDinhLuongByDanhMuc(Integer idDanhMuc) {
        return dinhLuongChiTietRepository.findByDanhMucId(idDanhMuc).stream().map(link -> {
            DinhLuong e = link.getDinhLuong();
            DinhLuongResponse res = new DinhLuongResponse();
            res.setId(e.getId());
            res.setTenHienThi(e.getTenHienThi());
            res.setDinhLuong(e.getDinhLuong());
            res.setKichCo(e.getKichCo());
            return res;
        }).collect(Collectors.toList());
    }

    // ========================================================================
    // 2. MÓN ĂN (DanhMucChiTiet)
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
    public MonAnResponse createMonAn(MonAnRequest request) {
        DanhMucChiTiet mon = new DanhMucChiTiet();
        mon.setTenMon(request.getTenMon());
        mon.setGiaBan(request.getGiaBan());
        mon.setGiaVon(request.getGiaVon());
        mon.setMoTa(request.getMoTa());
        mon.setHinhAnh(request.getHinhAnh());
        mon.setTrangThai(1);
        mon.setMaMon(generateNextCode(request.getTenMon(), "MON_AN"));

        mon.setDanhMuc(danhMucRepository.findById(request.getIdDanhMuc()).orElseThrow());
        mon.setDinhLuong(dinhLuongRepository.findById(request.getIdDinhLuong()).orElseThrow());

        return mapToMonAnResponse(danhMucChiTietRepository.save(mon));
    }

    @Override
    public MonAnResponse updateMonAn(Integer id, MonAnRequest request) {
        DanhMucChiTiet mon = danhMucChiTietRepository.findById(id).orElseThrow();
        mon.setTenMon(request.getTenMon());
        mon.setGiaBan(request.getGiaBan());
        mon.setGiaVon(request.getGiaVon());
        mon.setTrangThai(request.getTrangThai());
        mon.setMoTa(request.getMoTa());
        if(request.getHinhAnh() != null) mon.setHinhAnh(request.getHinhAnh());

        if (!mon.getDanhMuc().getId().equals(request.getIdDanhMuc())) {
            mon.setDanhMuc(danhMucRepository.findById(request.getIdDanhMuc()).orElseThrow());
        }
        if (!mon.getDinhLuong().getId().equals(request.getIdDinhLuong())) {
            mon.setDinhLuong(dinhLuongRepository.findById(request.getIdDinhLuong()).orElseThrow());
        }
        return mapToMonAnResponse(danhMucChiTietRepository.save(mon));
    }

    @Override
    public void deleteMonAn(Integer id) {
        DanhMucChiTiet mon = danhMucChiTietRepository.findById(id).orElseThrow();
        mon.setTrangThai(0);
        danhMucChiTietRepository.save(mon);
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

        // Đổ dữ liệu từ Danh mục
        if (entity.getDanhMuc() != null) {
            res.setTenDanhMuc(entity.getDanhMuc().getTenDanhMuc());
            res.setIdDanhMuc(entity.getDanhMuc().getId());
        }

        // Đổ dữ liệu từ Định lượng
        if (entity.getDinhLuong() != null) {
            res.setTenDinhLuong(entity.getDinhLuong().getTenHienThi());
            res.setGiaTriDinhLuong(entity.getDinhLuong().getDinhLuong());
            res.setKichCo(entity.getDinhLuong().getKichCo());
            res.setIdDinhLuong(entity.getDinhLuong().getId());
        }

        // Tạo tên hiển thị (nếu DTO có trường này)
        res.setTenDinhLuong(res.getTenMon() + " (" + res.getGiaTriDinhLuong() + ")");

        return res;
    }

    // ========================================================================
    // 3. SET LẨU (SetLau + ChiTietSetLau)
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

        // Lưu chi tiết Set
        if (request.getChiTietMonAn() != null) {
            List<ChiTietSetLau> details = new ArrayList<>();
            // Sử dụng đúng Class con trong Request
            for (SetLauRequest.ChiTietSetItemRequest itemReq : request.getChiTietMonAn()) {
                ChiTietSetLau detail = new ChiTietSetLau();
                detail.setSetLau(savedSet);
                detail.setSoLuong(itemReq.getSoLuong());

                // Tìm món ăn từ bảng DanhMucChiTiet
                DanhMucChiTiet monAn = danhMucChiTietRepository.findById(itemReq.getIdMonAn())
                        .orElseThrow(() -> new RuntimeException("Món ăn ID " + itemReq.getIdMonAn() + " không tồn tại"));
                detail.setMonAn(monAn);

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

        // --- SỬA LẠI LOGIC CẬP NHẬT CHI TIẾT TẠI ĐÂY ---
        if (request.getChiTietMonAn() != null) {

            // 1. Xóa sạch các phần tử trong rổ cũ.
            // Lệnh clear() này sẽ "báo hiệu" cho Hibernate tự động chạy lệnh DELETE dưới DB (nhờ orphanRemoval=true)
            if (set.getListChiTietSetLau() != null) {
                set.getListChiTietSetLau().clear();
            } else {
                set.setListChiTietSetLau(new ArrayList<>());
            }

            // 2. Tạo phần tử mới và nhét vào cái rổ (List) hiện tại
            for (SetLauRequest.ChiTietSetItemRequest itemReq : request.getChiTietMonAn()) {
                ChiTietSetLau detail = new ChiTietSetLau();

                // Map dữ liệu
                detail.setSetLau(set); // Rất quan trọng: Gắn Cha cho Con
                detail.setSoLuong(itemReq.getSoLuong());
                detail.setMonAn(danhMucChiTietRepository.findById(itemReq.getIdMonAn()).orElseThrow());

                // Add thẳng vào List đang được Hibernate theo dõi (Không tạo List mới)
                set.getListChiTietSetLau().add(detail);
            }
        }

        // 3. Bạn không cần gọi chiTietSetLauRepository.saveAll() nữa.
        // Chỉ cần lưu thằng Cha (SetLau), Hibernate sẽ tự động lưu các thằng Con (ChiTietSetLau) nhờ CascadeType.ALL
        return mapToSetLauResponse(setLauRepository.save(set));
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
    public List<SetLauResponse> findSetLauActive() {
        return setLauRepository.findByTrangThai(1).stream()
                .map(this::mapToSetLauResponse) // Tái sử dụng hàm map đã viết ở turn trước
                .collect(Collectors.toList());
    }

    @Override
    public List<MonAnResponse> findMonAnActive() {
        // Lấy tất cả DanhMucChiTiet có trạng thái = 1
        return danhMucChiTietRepository.findByTrangThai(1).stream()
                .map(this::mapToMonAnResponse) // Tái sử dụng hàm map đã viết
                .collect(Collectors.toList());
    }

    @Override
    public List<SetLauResponse> findSetLauTop(int metric) {
        int limit = (metric > 0) ? metric : 5;
        Pageable topPage = PageRequest.of(0, limit);

        // Logic lấy top selling (cần custom query trong repo)
        // Ví dụ: SELECT s FROM SetLau s ORDER BY s.soLuongDaBan DESC
        // Ở đây tạm thời trả về Active sets nếu chưa có thống kê
        return findSetLauActive().stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public DinhLuong createDinhLuong(DinhLuongRequest request) {
        // 1. Tìm Danh mục
        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        // 2. Tạo Entity mới và set dữ liệu
        DinhLuong newDinhLuong = new DinhLuong();
        newDinhLuong.setKichCo(request.getKichCo());
        newDinhLuong.setDinhLuong(request.getDinhLuong());
        newDinhLuong.setTenHienThi(request.getTenHienThi());

        return dinhLuongRepository.save(newDinhLuong);
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