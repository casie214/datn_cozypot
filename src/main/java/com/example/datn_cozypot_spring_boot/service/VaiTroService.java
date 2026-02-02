package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.VaiTroRequest;
import com.example.datn_cozypot_spring_boot.dto.VaiTroResponse;
import com.example.datn_cozypot_spring_boot.entity.VaiTro;
import com.example.datn_cozypot_spring_boot.repository.VaiTroRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VaiTroService {

    @Autowired
    private VaiTroRepository repo;

    // 1. Lấy danh sách phân trang và tìm kiếm
    public Page<VaiTroResponse> getAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return repo.searchVaiTro(keyword, pageable).map(this::convertToResponse);
    }

    // 2. Lấy danh sách vai trò đang hoạt động để đổ vào Dropdown/Select
    public List<VaiTro> getActiveRoles() {
        return repo.findByTrangThai(1); // 1: Hoạt động, 0: Ngừng hoạt động
    }

    // 3. Thêm mới vai trò
    @Transactional
    public VaiTro create(VaiTroRequest req) {
        // Kiểm tra mã vai trò đã tồn tại chưa nếu cần
        VaiTro vt = new VaiTro();
        vt.setMaVaiTro(req.getMaVaiTro());
        vt.setTenVaiTro(req.getTenVaiTro());
        vt.setMoTaVaiTro(req.getMoTaVaiTro());
        vt.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1);
        return repo.save(vt);
    }

    // 4. Cập nhật vai trò
    @Transactional
    public VaiTro update(Integer id, VaiTroRequest req) {
        VaiTro vt = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò có ID: " + id));

        vt.setTenVaiTro(req.getTenVaiTro());
        vt.setMoTaVaiTro(req.getMoTaVaiTro());
        vt.setTrangThai(req.getTrangThai());

        return repo.save(vt);
    }

    // 5. Xóa vai trò (Có kiểm tra ràng buộc)
    @Transactional
    public void delete(Integer id) {
        VaiTro vt = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò để xóa"));

        // Kiểm tra xem danh sách nhân viên có rỗng không
        if (vt.getNhanViens() != null && !vt.getNhanViens().isEmpty()) {
            throw new RuntimeException("Lỗi: Không thể xóa vai trò vì đang có "
                    + vt.getNhanViens().size() + " nhân viên đảm nhận vai trò này!");
        }

        repo.deleteById(id);
    }

    // Chuyển đổi từ Entity sang DTO Response
    private VaiTroResponse convertToResponse(VaiTro vt) {
        VaiTroResponse res = new VaiTroResponse();
        BeanUtils.copyProperties(vt, res);
        // Nếu NhanViens là danh sách Lazy, BeanUtils sẽ không copy sang trừ khi bạn xử lý thủ công
        return res;
    }
}