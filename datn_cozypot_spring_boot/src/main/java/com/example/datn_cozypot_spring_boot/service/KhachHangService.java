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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class KhachHangService {
    @Autowired
    private KhachHangRepository repo;

    private final Path root = Paths.get("uploads/customers");

    public KhachHangService() {
        try {
            if (!Files.exists(root)) Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage folder for customers");
        }
    }

    // 1. Lấy danh sách + Tìm kiếm + Phân trang
    public Page<KhachHangResponse> getAll(String keyword, Integer trangThai, LocalDate tuNgay, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "trangThai").and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime startDateTime = (tuNgay != null) ? tuNgay.atStartOfDay() : null;

        return repo.searchKhachHang(keyword, trangThai, startDateTime, pageable)
                .map(this::convertToResponse);
    }

    // 2. Chi tiết
    public KhachHangResponse getDetail(Integer id) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        return convertToResponse(kh);
    }

    // 3. Kiểm tra trùng lặp (Dùng cho API check-duplicate ở Controller)
    public boolean checkDuplicate(String type, String value, Integer excludeId) {
        return switch (type) {
            case "email" -> excludeId != null
                    ? repo.existsByEmailAndIdNot(value, excludeId)
                    : repo.existsByEmail(value);
            case "soDienThoai" -> excludeId != null
                    ? repo.existsBySoDienThoaiAndIdNot(value, excludeId)
                    : repo.existsBySoDienThoai(value);
            case "tenDangNhap" -> excludeId != null
                    ? repo.existsByTenDangNhapAndIdNot(value, excludeId)
                    : repo.existsByTenDangNhap(value);
            default -> false;
        };
    }

    // 4. Thêm mới
    @Transactional
    public KhachHangResponse create(KhachHangRequest req, MultipartFile file) {
        if (repo.existsBySoDienThoai(req.getSoDienThoai())) throw new RuntimeException("Số điện thoại đã tồn tại!");
        if (repo.existsByEmail(req.getEmail())) throw new RuntimeException("Email đã tồn tại!");

        KhachHang kh = new KhachHang();
        mapRequestToEntity(req, kh);

        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        }

        return convertToResponse(repo.save(kh));
    }

    // 5. Cập nhật
    @Transactional
    public KhachHangResponse update(Integer id, KhachHangRequest req, MultipartFile file) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        if (repo.existsBySoDienThoaiAndIdNot(req.getSoDienThoai(), id)) throw new RuntimeException("Số điện thoại đã bị sử dụng!");
        if (repo.existsByEmailAndIdNot(req.getEmail(), id)) throw new RuntimeException("Email đã bị sử dụng!");

        mapRequestToEntity(req, kh);

        if (file != null && !file.isEmpty()) {
            kh.setAnhDaiDien(saveFile(file));
        }

        return convertToResponse(repo.save(kh));
    }

    // 6. Đảo trạng thái
    @Transactional
    public KhachHangResponse toggleStatus(Integer id) {
        KhachHang kh = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        kh.setTrangThai(kh.getTrangThai() != null && kh.getTrangThai() == 1 ? 0 : 1);
        return convertToResponse(repo.save(kh));
    }

    // --- Helper Methods ---
    private String saveFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi lưu file: " + e.getMessage());
        }
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