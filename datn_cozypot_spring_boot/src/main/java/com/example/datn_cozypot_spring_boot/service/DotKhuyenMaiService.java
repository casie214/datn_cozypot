package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.DotKhuyenMaiDTO;
import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
<<<<<<< HEAD
import com.example.datn_cozypot_spring_boot.entity.MonAnDiKem;
import com.example.datn_cozypot_spring_boot.entity.SetLau;
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepository;
import com.example.datn_cozypot_spring_boot.repository.MonAnDiKemRepository;
import com.example.datn_cozypot_spring_boot.repository.SetLauRepository;
import jakarta.transaction.Transactional;
=======
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepo;
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
<<<<<<< HEAD
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
=======
import java.util.HashMap;
import java.util.List;
import java.util.Map;
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
import java.util.stream.Collectors;

@Service
public class DotKhuyenMaiService {
<<<<<<< HEAD
    @Autowired
    private DotKhuyenMaiRepository dotKhuyenMaiRepo;

    @Autowired
    private SetLauRepository setLauRepo; // Inject thêm repository này

    @Autowired
    private MonAnDiKemRepository monAnRepo;

    // Bổ sung phương thức này nếu chưa có hoặc đang sai tên
=======

    @Autowired
    private DotKhuyenMaiRepo dotKhuyenMaiRepo;

>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
    public List<DotKhuyenMaiDTO> getAll() {
        return dotKhuyenMaiRepo.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

<<<<<<< HEAD
    // Bổ sung thêm getById để phục vụ API lấy chi tiết
    public DotKhuyenMaiDTO getById(Integer id) {
        DotKhuyenMai entity = dotKhuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt khuyến mãi id: " + id));
        return convertToDto(entity);
    }

    @Transactional // Quan trọng: Đảm bảo lưu cả 2 bảng hoặc không lưu gì nếu lỗi
    public DotKhuyenMaiDTO create(DotKhuyenMaiDTO dto) {
        DotKhuyenMai entity = new DotKhuyenMai();
        mapDtoToEntity(dto, entity);

        // Xử lý gán Set Lẩu vào Đợt Khuyến Mãi
        if (dto.getIdSetLauChiTiet() != null && !dto.getIdSetLauChiTiet().isEmpty()) {
            List<SetLau> selectedSets = setLauRepo.findAllById(dto.getIdSetLauChiTiet());
            entity.setSetLaus(new HashSet<>(selectedSets));
        }

        // 2. Xử lý gán Món Ăn Đi Kèm (MỚI)
        if (dto.getIdMonAnChiTiet() != null) {
            List<MonAnDiKem> selectedMons = monAnRepo.findAllById(dto.getIdMonAnChiTiet());
            entity.setMonAnDiKems(new HashSet<>(selectedMons)); // Giả định Entity DotKhuyenMai đã có field monAnDiKems
        }
=======
    public DotKhuyenMaiDTO create(DotKhuyenMaiDTO dto) {
        DotKhuyenMai entity = new DotKhuyenMai();

        entity.setTenDotKhuyenMai(dto.getTenDotKhuyenMai());
        entity.setMoTa(dto.getMoTa());
        entity.setLoaiKhuyenMai(dto.getLoaiKhuyenMai());
        entity.setDoiTuongApDung(dto.getDoiTuongApDung());
        entity.setKhungGioApDung(dto.getKhungGioApDung());
        entity.setDanhSachMonApDung(dto.getDanhSachMonApDung());
        entity.setTrangThai(dto.getTrangThai());
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8

        entity.setNgayTao(Instant.now());
        entity.setNguoiTao("Admin");

        DotKhuyenMai saved = dotKhuyenMaiRepo.save(entity);
<<<<<<< HEAD
        return convertToDto(saved);
    }

    @Transactional
    public DotKhuyenMaiDTO update(Integer id, DotKhuyenMaiDTO dto) {
        DotKhuyenMai entity = dotKhuyenMaiRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt khuyến mãi"));

        mapDtoToEntity(dto, entity);

        // Cập nhật lại danh sách Set Lẩu (Xóa cũ, thêm mới)
        if (dto.getIdSetLauChiTiet() != null) {
            List<SetLau> selectedSets = setLauRepo.findAllById(dto.getIdSetLauChiTiet());
            entity.getSetLaus().clear();
            entity.getSetLaus().addAll(selectedSets);
        }

        // 2. Xử lý gán Món Ăn Đi Kèm (MỚI)
        if (dto.getIdMonAnChiTiet() != null) {
            List<MonAnDiKem> selectedMons = monAnRepo.findAllById(dto.getIdMonAnChiTiet());
            entity.setMonAnDiKems(new HashSet<>(selectedMons)); // Giả định Entity DotKhuyenMai đã có field monAnDiKems
        }
=======

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
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8

        entity.setNgaySua(Instant.now());
        entity.setNguoiSua("Admin");

        return convertToDto(dotKhuyenMaiRepo.save(entity));
    }

<<<<<<< HEAD
    // Trong DotKhuyenMaiService.java
    public DotKhuyenMaiDTO convertToDto(DotKhuyenMai entity) {
        if (entity == null) return null;

        DotKhuyenMaiDTO dto = new DotKhuyenMaiDTO();

        // GÁN DỮ LIỆU CƠ BẢN (Đây là phần bạn đang thiếu)
=======
    private DotKhuyenMaiDTO convertToDto(DotKhuyenMai entity) {
        DotKhuyenMaiDTO dto = new DotKhuyenMaiDTO();

>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
        dto.setId(entity.getId());
        dto.setMaDotKhuyenMai(entity.getMaDotKhuyenMai());
        dto.setTenDotKhuyenMai(entity.getTenDotKhuyenMai());
        dto.setMoTa(entity.getMoTa());
<<<<<<< HEAD
        dto.setPhanTramGiam(entity.getPhanTramGiam());
        dto.setNgayBatDau(entity.getNgayBatDau());
        dto.setNgayKetThuc(entity.getNgayKetThuc());
        dto.setTrangThai(entity.getTrangThai());
=======
        dto.setLoaiKhuyenMai(entity.getLoaiKhuyenMai());
        dto.setDoiTuongApDung(entity.getDoiTuongApDung());
        dto.setKhungGioApDung(entity.getKhungGioApDung());
        dto.setDanhSachMonApDung(entity.getDanhSachMonApDung());
        dto.setTrangThai(entity.getTrangThai());

>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
        dto.setNgayTao(entity.getNgayTao());
        dto.setNgaySua(entity.getNgaySua());
        dto.setNguoiTao(entity.getNguoiTao());
        dto.setNguoiSua(entity.getNguoiSua());

<<<<<<< HEAD
        // Đây là cách làm đúng để tránh vòng lặp:
        if (entity.getSetLaus() != null) {
            dto.setIdSetLauChiTiet(entity.getSetLaus().stream()
                    .map(SetLau::getId)
                    .collect(Collectors.toList()));
        }

        // Xử lý danh sách ID Món Ăn Đi Kèm
        if (entity.getMonAnDiKems() != null) {
            dto.setIdMonAnChiTiet(entity.getMonAnDiKems().stream()
                    .map(MonAnDiKem::getId)
                    .collect(Collectors.toList()));
        } else {
            dto.setIdMonAnChiTiet(new ArrayList<>()); // Trả về mảng rỗng [] thay vì null
        }

        return dto;
    }

    private void mapDtoToEntity(DotKhuyenMaiDTO dto, DotKhuyenMai entity) {
        // 1. Thông tin cơ bản
        entity.setTenDotKhuyenMai(dto.getTenDotKhuyenMai());
        entity.setMoTa(dto.getMoTa());

        // 2. Giá trị giảm giá (Check null để tránh NullPointerException)
        entity.setPhanTramGiam(dto.getPhanTramGiam() != null ? dto.getPhanTramGiam() : 0);

        // 3. Thời gian áp dụng (Khớp LocalDate giữa DTO và Entity)
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());

        // 4. Trạng thái
        // Nếu status từ giao diện gửi về null, mặc định để là 1 (Đang hoạt động)
        entity.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : 1);
    }

    // Trong DotKhuyenMaiService.java

    public Page<DotKhuyenMaiDTO> search(String keyword, Integer status, LocalDate ngayBD, LocalDate ngayKT, Pageable pageable) {
        String searchKey = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        // Gọi Repository với các tham số ngày tháng
        Page<DotKhuyenMai> pageEntity = dotKhuyenMaiRepo.searchFilter(searchKey, status, ngayBD, ngayKT, pageable);
=======
        return dto;
    }

    public Page<DotKhuyenMaiDTO> search(String keyword, Integer status, Integer type, Pageable pageable) {
        // Làm sạch keyword: nếu rỗng hoặc null thì gán null hoàn toàn
        String searchKey = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        // Gọi repository
        Page<DotKhuyenMai> pageEntity = dotKhuyenMaiRepo.searchFilter(searchKey, status, type, pageable);
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8

        return pageEntity.map(this::convertToDto);
    }

<<<<<<< HEAD
    public List<DotKhuyenMai> getDotDangHoatDong() {
        return dotKhuyenMaiRepo.findDotDangHoatDong();
    }

}
=======
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
>>>>>>> 1b190272e5d68a107cc64dc772e59a052d39ace8
