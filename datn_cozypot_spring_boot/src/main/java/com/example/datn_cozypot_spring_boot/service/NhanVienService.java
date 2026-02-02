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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class NhanVienService {

    @Autowired
    private NhanVienRepository repo;

    @Autowired
    private VaiTroRepository vaiTroRepo;

    // Đường dẫn gốc lưu ảnh: Project_Root/uploads/images
    private final Path storageFolder = Paths.get("uploads/images");

    public NhanVienService() {
        try {
            Files.createDirectories(storageFolder);
        } catch (IOException e) {
            throw new RuntimeException("Không thể khởi tạo thư mục lưu trữ ảnh!", e);
        }
    }

    // --- LOGIC LƯU FILE ẢNH ---
    private String saveImage(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        try {
            // Tạo tên file duy nhất: UUID + Tên gốc
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path destinationFile = storageFolder.resolve(fileName).normalize().toAbsolutePath();

            // Sao chép file vào thư mục đích
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi lưu file ảnh: " + e.getMessage());
        }
    }

    // --- CHECK TRÙNG (Real-time từ Vue) ---
    public boolean checkDuplicate(String type, String value, Integer excludeId) {
        if (value == null || value.isBlank()) return false;
        if (excludeId != null) {
            return switch (type) {
                case "tenDangNhap" -> repo.existsByTenDangNhapAndIdNot(value, excludeId);
                case "email" -> repo.existsByEmailAndIdNot(value, excludeId);
                case "sdtNhanVien" -> repo.existsBySdtNhanVienAndIdNot(value, excludeId);
                case "soCccd" -> repo.existsBySoCccdAndIdNot(value, excludeId);
                default -> false;
            };
        } else {
            return switch (type) {
                case "tenDangNhap" -> repo.existsByTenDangNhap(value);
                case "email" -> repo.existsByEmail(value);
                case "sdtNhanVien" -> repo.existsBySdtNhanVien(value);
                case "soCccd" -> repo.existsBySoCccd(value);
                default -> false;
            };
        }
    }

    public Page<NhanVienResponse> getAll(String keyword, Integer trangThai, LocalDate tuNgay, int page, int size) {
        Sort sort = Sort.by(Sort.Order.asc("trangThaiLamViec"), Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page, size, sort);
        return repo.searchNhanVien(keyword, trangThai, tuNgay, pageable).map(this::convertToResponse);
    }

    public NhanVienResponse getDetail(Integer id) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên id: " + id));
        return convertToResponse(nv);
    }

    // --- THÊM MỚI ---
    @Transactional
    public NhanVienResponse create(NhanVienRequest req, MultipartFile file) {
        if (repo.existsByTenDangNhap(req.getTenDangNhap())) throw new RuntimeException("Tên đăng nhập đã tồn tại");
        if (repo.existsByEmail(req.getEmail())) throw new RuntimeException("Email đã tồn tại");

        NhanVien nv = new NhanVien();
        mapRequestToEntity(req, nv);

        // Lưu ảnh nếu có
        if (file != null && !file.isEmpty()) {
            nv.setAnhDaiDien(saveImage(file));
        }

        return convertToResponse(repo.save(nv));
    }

    // --- CẬP NHẬT ---
    @Transactional
    public NhanVienResponse update(Integer id, NhanVienRequest req, MultipartFile file) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (repo.existsByTenDangNhapAndIdNot(req.getTenDangNhap(), id)) throw new RuntimeException("Tên đăng nhập bị trùng");
        if (repo.existsByEmailAndIdNot(req.getEmail(), id)) throw new RuntimeException("Email đã bị trùng");

        mapRequestToEntity(req, nv);

        // Nếu có file mới thì cập nhật, không thì giữ tên ảnh cũ (đã có trong Entity sau khi map)
        if (file != null && !file.isEmpty()) {
            nv.setAnhDaiDien(saveImage(file));
        }

        return convertToResponse(repo.save(nv));
    }

    @Transactional
    public NhanVienResponse toggleStatus(Integer id) {
        NhanVien nv = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        nv.setTrangThaiLamViec(nv.getTrangThaiLamViec() == 1 ? 2 : 1);
        return convertToResponse(repo.save(nv));
    }

    private void mapRequestToEntity(NhanVienRequest req, NhanVien nv) {
        nv.setHoTenNhanVien(req.getHoTenNhanVien());
        nv.setSdtNhanVien(req.getSdtNhanVien());
        nv.setDiaChi(req.getDiaChi());
        nv.setEmail(req.getEmail());
        nv.setGioiTinh(req.getGioiTinh());
        nv.setNgayVaoLam(req.getNgayVaoLam());
        nv.setTrangThaiLamViec(req.getTrangThaiLamViec());
        nv.setTenDangNhap(req.getTenDangNhap());
        nv.setNgaySinh(req.getNgaySinh());
        nv.setSoCccd(req.getSoCccd());
        nv.setNgayCapCccd(req.getNgayCapCccd());
        nv.setNoiCapCccd(req.getNoiCapCccd());

        // Lưu ý: Không map trực tiếp AnhDaiDien từ Request vì Request gửi lên thường là null khi dùng FormData
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
        if (nv.getIdVaiTro() != null) {
            res.setTenVaiTro(nv.getIdVaiTro().getTenVaiTro());
            res.setIdVaiTro(nv.getIdVaiTro().getId());
        }
        return res;
    }
}