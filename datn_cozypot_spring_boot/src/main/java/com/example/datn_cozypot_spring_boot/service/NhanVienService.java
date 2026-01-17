package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.NhanVienRequest;
import com.example.datn_cozypot_spring_boot.dto.NhanVienResponse;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import com.example.datn_cozypot_spring_boot.repository.VaiTroRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class NhanVienService {
    @Autowired private NhanVienRepository repo;
    @Autowired private VaiTroRepository vaiTroRepo;

    public Page<NhanVienResponse> getAll(String keyword, Integer trangThai, LocalDate tuNgay, int page, int size) {
        // Sắp xếp trạng thái Tăng dần (ASC): 1 (Hoạt động) hiện trước, 2 (Ngừng hoạt động) hiện sau
        // Sau đó sắp xếp ID Giảm dần (DESC): Ai mới vào làm thì hiện lên trên trong cùng nhóm trạng thái
        Sort sort = Sort.by(Sort.Order.asc("trangThaiLamViec"), Sort.Order.desc("id"));

        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.searchNhanVien(keyword, trangThai, tuNgay, pageable).map(this::convertToResponse);
    }

    // Xem chi tiết
    public NhanVienResponse getDetail(Integer id) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        return convertToResponse(nv);
    }

    @Transactional
    public NhanVienResponse create(NhanVienRequest req) {
        NhanVien nv = new NhanVien();
        mapRequestToEntity(req, nv);
        return convertToResponse(repo.save(nv));
    }

    @Transactional
    public NhanVienResponse update(Integer id, NhanVienRequest req) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        mapRequestToEntity(req, nv);
        return convertToResponse(repo.save(nv));
    }

    // Helper Mapping
    private void mapRequestToEntity(NhanVienRequest req, NhanVien nv) {
        nv.setHoTenNhanVien(req.getHoTenNhanVien());
        nv.setSdtNhanVien(req.getSdtNhanVien());
        nv.setDiaChi(req.getDiaChi());
        nv.setEmail(req.getEmail());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgayVaoLam(req.getNgayVaoLam());
        nv.setTrangThaiLamViec(req.getTrangThaiLamViec()); // 0: Nghỉ việc, 1: Đang làm, 2: Nghỉ phép
        nv.setTenDangNhap(req.getTenDangNhap());
        nv.setNgaySinh(req.getNgaySinh());
        if (req.getMatKhauDangNhap() != null && !req.getMatKhauDangNhap().isEmpty()) {
            nv.setMatKhauDangNhap(req.getMatKhauDangNhap());
        }
        if(req.getIdVaiTro() != null) {
            nv.setIdVaiTro(vaiTroRepo.findById(req.getIdVaiTro()).orElse(null));
        }
    }

    private NhanVienResponse convertToResponse(NhanVien nv) {
        NhanVienResponse res = new NhanVienResponse();
        BeanUtils.copyProperties(nv, res);
        res.setNgaySinh(nv.getNgaySinh());
        res.setNgayVaoLam(nv.getNgayVaoLam());
        if (nv.getIdVaiTro() != null) {
            res.setTenVaiTro(nv.getIdVaiTro().getTenVaiTro());
            res.setIdVaiTro(nv.getIdVaiTro().getId());
        }
        return res;
    }

    @Transactional
    public NhanVienResponse toggleStatus(Integer id) {
        // 1. Tìm nhân viên theo ID
        NhanVien nv = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // 2. Đảo trạng thái giữa 1 (Hoạt động) và 2 (Ngừng hoạt động)
        // Nếu hiện tại là 1 thì chuyển thành 2, ngược lại thì chuyển về 1
        if (nv.getTrangThaiLamViec() != null && nv.getTrangThaiLamViec() == 1) {
            nv.setTrangThaiLamViec(2);
        } else {
            nv.setTrangThaiLamViec(1);
        }

        // 3. Lưu và trả về response
        return convertToResponse(repo.save(nv));
    }
}