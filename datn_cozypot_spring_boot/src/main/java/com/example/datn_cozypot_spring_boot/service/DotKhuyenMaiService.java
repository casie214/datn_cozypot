package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.DotKhuyenMaiDTO;
import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DotKhuyenMaiService {

    @Autowired
    private DotKhuyenMaiRepo dotKhuyenMaiRepo;

    public List<DotKhuyenMaiDTO> getAll() {
        return dotKhuyenMaiRepo.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DotKhuyenMaiDTO create(DotKhuyenMaiDTO dto) {
        DotKhuyenMai entity = new DotKhuyenMai();

        entity.setTenDotKhuyenMai(dto.getTenDotKhuyenMai());
        entity.setMoTa(dto.getMoTa());
        entity.setLoaiKhuyenMai(dto.getLoaiKhuyenMai());
        entity.setDoiTuongApDung(dto.getDoiTuongApDung());
        entity.setKhungGioApDung(dto.getKhungGioApDung());
        entity.setDanhSachMonApDung(dto.getDanhSachMonApDung());
        entity.setTrangThai(dto.getTrangThai());

        entity.setNgayTao(Instant.now());
        entity.setNguoiTao("Admin");

        DotKhuyenMai saved = dotKhuyenMaiRepo.save(entity);

        return convertToDto(saved);
    }

    public DotKhuyenMaiDTO update(Integer id, DotKhuyenMaiDTO dto) {

        DotKhuyenMai entity = dotKhuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt khuyến mãi"));

        entity.setTenDotKhuyenMai(dto.getTenDotKhuyenMai());
        entity.setMoTa(dto.getMoTa());
        entity.setLoaiKhuyenMai(dto.getLoaiKhuyenMai());
        entity.setDoiTuongApDung(dto.getDoiTuongApDung());
        entity.setKhungGioApDung(dto.getKhungGioApDung());
        entity.setDanhSachMonApDung(dto.getDanhSachMonApDung());
        entity.setTrangThai(dto.getTrangThai());

        entity.setNgaySua(Instant.now());
        entity.setNguoiSua("Admin");

        return convertToDto(dotKhuyenMaiRepo.save(entity));
    }

    private DotKhuyenMaiDTO convertToDto(DotKhuyenMai entity) {
        DotKhuyenMaiDTO dto = new DotKhuyenMaiDTO();

        dto.setId(entity.getId());
        dto.setMaDotKhuyenMai(entity.getMaDotKhuyenMai());
        dto.setTenDotKhuyenMai(entity.getTenDotKhuyenMai());
        dto.setMoTa(entity.getMoTa());
        dto.setLoaiKhuyenMai(entity.getLoaiKhuyenMai());
        dto.setDoiTuongApDung(entity.getDoiTuongApDung());
        dto.setKhungGioApDung(entity.getKhungGioApDung());
        dto.setDanhSachMonApDung(entity.getDanhSachMonApDung());
        dto.setTrangThai(entity.getTrangThai());

        dto.setNgayTao(entity.getNgayTao());
        dto.setNgaySua(entity.getNgaySua());
        dto.setNguoiTao(entity.getNguoiTao());
        dto.setNguoiSua(entity.getNguoiSua());

        return dto;
    }

    public Page<DotKhuyenMaiDTO> search(String keyword, Integer status, Integer type, Pageable pageable) {
        // Làm sạch keyword: nếu rỗng hoặc null thì gán null hoàn toàn
        String searchKey = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        // Gọi repository
        Page<DotKhuyenMai> pageEntity = dotKhuyenMaiRepo.searchFilter(searchKey, status, type, pageable);

        return pageEntity.map(this::convertToDto);
    }

    public List<DotKhuyenMai> getActivePromotion() {
        // Gọi hàm findAllActive() đã sửa ở trên
        return dotKhuyenMaiRepo.findAllActive();
    }

    public List<Map<String, Object>> getActiveListForCombo() {
        return dotKhuyenMaiRepo.findAllActive().stream().map(dot -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", dot.getId());
            map.put("tenDotKhuyenMai", dot.getTenDotKhuyenMai());
            return map;
        }).collect(Collectors.toList());
    }
}
