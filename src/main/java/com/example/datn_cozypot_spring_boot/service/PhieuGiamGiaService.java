package com.example.datn_cozypot_spring_boot.service;
import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.example.datn_cozypot_spring_boot.dto.KhachHangResponse;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaDTO;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaResponseDTO;
import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGiaCaNhan;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuGiamGiaCaNhanRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuGiamGiaRepository;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async; // Thêm import này

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service

public class PhieuGiamGiaService {
    @Autowired
    private PhieuGiamGiaRepository repo;

    @Autowired
    private PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepo;

    @Autowired
    private KhachHangRepository khachHangRepo;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private DotKhuyenMaiRepository dotKhuyenMaiRepo;



    // ============= TRUY VẤN NÂNG CAO =============
    public Page<PhieuGiamGiaResponseDTO> getAll(String keyword, Integer doiTuong, Integer loaiGiamGia,
                                                Integer trangThai, String ngayBatDau, String ngayKetThuc,
                                                int page, int size) {
        LocalDateTime start = null;
        LocalDateTime end = null;

        // Kiểm tra kỹ chuỗi trước khi parse để tránh lỗi 500
        if (ngayBatDau != null && !ngayBatDau.trim().isEmpty()) {
            start = LocalDate.parse(ngayBatDau).atStartOfDay();
        }
        if (ngayKetThuc != null && !ngayKetThuc.trim().isEmpty()) {
            end = LocalDate.parse(ngayKetThuc).atTime(23, 59, 59);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PhieuGiamGia> list = repo.searchVouchers(
                keyword,
                doiTuong,
                loaiGiamGia,
                trangThai,
                start,
                end,
                pageable
        );

        // 3. Map sang DTO
        return list.map(this::mapToResponseDTO);
    }

    public PhieuGiamGiaResponseDTO getById(Integer id) {
        PhieuGiamGia entity = repo.findDetailById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá với ID: " + id));
        return mapToResponseDTO(entity);
    }

    // ============= THÊM MỚI & GỬI EMAIL =============
    @Transactional
    public PhieuGiamGia create(PhieuGiamGiaDTO dto) {
        PhieuGiamGia entity = new PhieuGiamGia();
        mapToEntity(dto, entity);
        PhieuGiamGia savedPhieu = repo.save(entity);

        // Lưu bảng cá nhân nếu cần
        if (dto.getDoiTuong() == 1 && dto.getListIdKhachHang() != null) {
            for (Integer khId : dto.getListIdKhachHang()) {
                KhachHang kh = khachHangRepo.findById(khId).orElse(null);
                if (kh != null) {
                    PhieuGiamGiaCaNhan caNhan = new PhieuGiamGiaCaNhan();
                    caNhan.setPhieuGiamGia(savedPhieu);
                    caNhan.setKhachHang(kh);
                    phieuGiamGiaCaNhanRepo.save(caNhan);
                }
            }
        }

        // Gửi Email thông báo dựa trên danh sách Vue gửi xuống
        if (dto.getListEmails() != null && !dto.getListEmails().isEmpty()) {
            sendNotificationEmails(dto.getListEmails(), savedPhieu);
        }

        return savedPhieu;
    }

    // ============= GỬI EMAIL (ASYNC) =============
    @Async
    protected void sendNotificationEmails(List<String> emails, PhieuGiamGia p) {
        for (String email : emails) {
            try {
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom("CozyPot <email_cua_ban@gmail.com>");
                helper.setTo(email);
                helper.setSubject("[CozyPot] Ưu Đãi Đặc Biệt: " + p.getTenPhieuGiamGia() + " 🎁");

                String donVi = (p.getLoaiGiamGia() == 1) ? "%" : " VNĐ";

                String htmlContent =
                        // VIỀN ĐỎ BAO QUANH TOÀN BỘ EMAIL
                        "<div style='font-family: \"Segoe UI\", Roboto, sans-serif; max-width: 600px; margin: 20px auto; "
                                + "border: 2px solid #7B1F1F; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1);'>"

                                // HEADER: TIÊU ĐỀ TRẮNG TRÊN NỀN ĐỎ (KHÔNG LOGO)
                                + "<div style='background-color: #7B1F1F; padding: 30px 20px; text-align: center;'>"
                                + "<h1 style='color: #ffffff; margin: 0; font-size: 28px; text-transform: uppercase; letter-spacing: 2px; font-weight: bold;'>"
                                + "ƯU ĐÃI ĐẶC BIỆT"
                                + "</h1>"
                                + "<p style='color: #FFD700; margin: 10px 0 0 0; font-size: 16px;'>Dành riêng cho khách hàng của CozyPot</p>"
                                + "</div>"

                                // CONTENT
                                + "<div style='padding: 35px; background-color: #ffffff; color: #333; line-height: 1.6;'>"
                                + "<p style='font-size: 18px;'>Xin chào quý khách,</p>"
                                + "<p>Chúng tôi xin gửi tặng bạn mã giảm giá ưu đãi để trải nghiệm hương vị lẩu ấm cúng tại <strong>CozyPot</strong>:</p>"

                                // VOUCHER BOX
                                + "<div style='margin: 30px 0; border: 2px dashed #7B1F1F; border-radius: 10px; padding: 25px; text-align: center; background-color: #FFF9F9;'>"
                                + "<p style='margin: 0; color: #666; text-transform: uppercase; font-size: 13px; font-weight: 600;'>Mã quà tặng của bạn</p>"
                                + "<div style='font-size: 36px; font-weight: bold; color: #7B1F1F; margin: 10px 0; letter-spacing: 1px;'>" + p.getCodeGiamGia() + "</div>"
                                + "<div style='font-size: 20px; color: #222; font-weight: bold;'>GIẢM NGAY " + p.getGiaTriGiam() + donVi + "</div>"
                                + "<p style='font-size: 13px; color: #888; margin-top: 15px;'>Hạn sử dụng: <span style='color: #333; font-weight: 600;'>" + p.getNgayKetThuc() + "</span></p>"
                                + "</div>"

                                // NÚT BẤM DÙNG MÃ
                                + "<div style='text-align: center; margin: 30px 0;'>"
                                + "<a href='http://localhost:5173' "
                                + "style='background-color: #7B1F1F; color: #ffffff; padding: 15px 45px; "
                                + "text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px; "
                                + "display: inline-block; transition: background 0.3s;'>"
                                + "ĐẶT BÀN & SỬ DỤNG NGAY"
                                + "</a>"
                                + "</div>"

                                + "<p style='font-size: 13px; color: #777; text-align: center;'>"
                                + "* Áp dụng cho đơn hàng từ " + p.getDonHangToiThieu() + " VNĐ"
                                + "</p>"
                                + "</div>"

                                // FOOTER
                                + "<div style='background-color: #7B1F1F; color: #ffffff; text-align: center; padding: 20px;'>"
                                + "<p style='margin: 0; font-size: 14px; font-weight: bold;'>COZYPOT - LẨU ẤM LÒNG</p>"
                                + "<p style='margin: 5px 0 0 0; font-size: 11px; opacity: 0.8;'>"
                                + "📧 support@cozypot.vn | 📞 0123 456 789<br>© 2026 CozyPot Team"
                                + "</p>"
                                + "</div>"

                                + "</div>";

                helper.setText(htmlContent, true);
                mailSender.send(mimeMessage);
                System.out.println("Đã gửi mail thành công cho: " + email);

            } catch (Exception e) {
                System.err.println("Lỗi gửi mail đến " + email + ": " + e.getMessage());
            }
        }
    }



    // ============= CẬP NHẬT =============
    @Transactional
    public PhieuGiamGia update(Integer id, PhieuGiamGiaDTO dto) {

        PhieuGiamGia entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ID: " + id));

        // ✅ CHECK TRÙNG CODE (TRỪ CHÍNH NÓ)
        if (repo.existsByCodeGiamGiaAndIdNot(dto.getCodeGiamGia(), id)) {
            throw new RuntimeException("Mã giảm giá đã tồn tại");
        }

        mapToEntity(dto, entity);
        return repo.save(entity);
    }


    // ============= XÓA (FIX LỖI CONTROLLER) =============
    @Transactional
    public void delete(Integer id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Không tìm thấy phiếu giảm giá với ID: " + id);
        }
        // Lưu ý: Nếu có bảng cá nhân, cần xóa dữ liệu liên quan trước hoặc cấu hình Cascade
        phieuGiamGiaCaNhanRepo.deleteByPhieuGiamGiaId(id);
        repo.deleteById(id);
    }

    // ============= MAPPING =============
    private void mapToEntity(PhieuGiamGiaDTO dto, PhieuGiamGia entity) {
        entity.setCodeGiamGia(dto.getCodeGiamGia());
        entity.setTenPhieuGiamGia(dto.getTenPhieuGiamGia());
        entity.setLoaiGiamGia(dto.getLoaiGiamGia());
        entity.setGiaTriGiam(dto.getGiaTriGiam());
        entity.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
        entity.setDonHangToiThieu(dto.getDonHangToiThieu());
        entity.setDoiTuong(dto.getDoiTuong());
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());
        entity.setSoLuong(dto.getSoLuong());
        entity.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : 1);

        if (dto.getIdDotKhuyenMai() != null) {
            DotKhuyenMai dot = dotKhuyenMaiRepo.findById(dto.getIdDotKhuyenMai())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt khuyến mãi"));
            entity.setDotKhuyenMai(dot);
        } else {
            entity.setDotKhuyenMai(null);
        }
    }

    private PhieuGiamGiaResponseDTO mapToResponseDTO(PhieuGiamGia p) {
        PhieuGiamGiaResponseDTO dto = new PhieuGiamGiaResponseDTO();
        dto.setId(p.getId());
        dto.setMaPhieuGiamGia(p.getMaPhieuGiamGia());
        dto.setCodeGiamGia(p.getCodeGiamGia());
        dto.setTenPhieuGiamGia(p.getTenPhieuGiamGia());
        dto.setLoaiGiamGia(p.getLoaiGiamGia());
        dto.setGiaTriGiam(p.getGiaTriGiam());
        dto.setGiaTriGiamToiDa(p.getGiaTriGiamToiDa());
        dto.setDonHangToiThieu(p.getDonHangToiThieu());
        dto.setDoiTuong(p.getDoiTuong());
        dto.setNgayBatDau(p.getNgayBatDau());
        dto.setNgayKetThuc(p.getNgayKetThuc());
        dto.setSoLuong(p.getSoLuong());
        dto.setTrangThai(p.getTrangThai());

        // ⭐⭐ PHẦN QUAN TRỌNG NHẤT ⭐⭐
        if (p.getDanhSachCaNhan() != null) {
            List<KhachHangResponse> listKH = new ArrayList<>();

            for (PhieuGiamGiaCaNhan cn : p.getDanhSachCaNhan()) {
                KhachHang kh = cn.getKhachHang();
                if (kh != null) {
                    KhachHangResponse khDto = new KhachHangResponse();
                    khDto.setId(kh.getId());
                    khDto.setTenKhachHang(kh.getTenKhachHang());
                    khDto.setEmail(kh.getEmail());
                    listKH.add(khDto);
                }
            }
            dto.setDanhSachKhachHang(listKH);
        }

        return dto;
    }

}