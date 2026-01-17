package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.KhachHangRequest;
import com.example.datn_cozypot_spring_boot.dto.KhachHangResponse;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository repo;

    public Page<KhachHangResponse> getAll(String keyword, Integer trangThai, LocalDate tuNgay, int page, int size) {
        // Sắp xếp:
        // 1. Trạng thái giảm dần (1 -> 0): Những ai trạng thái 1 (Hoạt động) lên đầu.
        // 2. ID giảm dần: Trong cùng một nhóm trạng thái, ai mới hơn (ID lớn hơn) lên trên.
        Sort sort = Sort.by(Sort.Direction.DESC, "trangThai")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime startDateTime = (tuNgay != null) ? tuNgay.atStartOfDay() : null;

        return repo.searchKhachHang(keyword, trangThai, startDateTime, pageable)
                .map(this::convertToResponse);
    }

    public KhachHangResponse getDetail(Integer id) {
        KhachHang kh = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        return convertToResponse(kh);
    }

    @Transactional
    public KhachHangResponse create(KhachHangRequest req) {
        KhachHang kh = new KhachHang();
        mapRequestToEntity(req, kh);
        return convertToResponse(repo.save(kh));
    }

    @Transactional
    public KhachHangResponse update(Integer id, KhachHangRequest req) {
        KhachHang kh = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        mapRequestToEntity(req, kh);
        return convertToResponse(repo.save(kh));
    }
    @Transactional
    public KhachHangResponse toggleStatus(Integer id) {
        // 1. Tìm khách hàng
        KhachHang kh = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // 2. Đảo trạng thái: Nếu 1 (Hoạt động) thì thành 0 (Ngừng hoạt động) và ngược lại
        if (kh.getTrangThai() != null && kh.getTrangThai() == 1) {
            kh.setTrangThai(0);
        } else {
            kh.setTrangThai(1);
        }

        // 3. Lưu và trả về kết quả
        return convertToResponse(repo.save(kh));
    }

    private void mapRequestToEntity(KhachHangRequest req, KhachHang kh) {
        kh.setTenKhachHang(req.getTenKhachHang());
        kh.setSoDienThoai(req.getSoDienThoai());
        kh.setEmail(req.getEmail());
        kh.setNgaySinh(req.getNgaySinh());
        kh.setGioiTinh(req.getGioiTinh());
        kh.setDiaChi(req.getDiaChi());
        kh.setTrangThai(req.getTrangThai());
        kh.setTenDangNhap(req.getTenDangNhap());
        if (req.getMatKhauDangNhap() != null && !req.getMatKhauDangNhap().isEmpty()) {
            kh.setMatKhauDangNhap(req.getMatKhauDangNhap());
        }
    }

    private KhachHangResponse convertToResponse(KhachHang kh) {
        KhachHangResponse res = new KhachHangResponse();
        BeanUtils.copyProperties(kh, res);
        return res;
    }
}