package com.example.datn_cozypot_spring_boot.service;
import com.example.datn_cozypot_spring_boot.dto.KhuyenMaiThongKeResponse;
import com.example.datn_cozypot_spring_boot.dto.request.ApDungVoucherRequest;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.*;
import jakarta.mail.internet.MimeMessage;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.example.datn_cozypot_spring_boot.dto.KhachHangResponse;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaDTO;
import com.example.datn_cozypot_spring_boot.dto.PhieuGiamGiaResponseDTO;
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

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    @Autowired
    private HoaDonThanhToanRepository hoaDonThanhToanRepository;
    @Autowired
    private PhieuGiamGiaCaNhanRepository phieuGiamGiaCaNhanRepository;
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    public KhuyenMaiThongKeResponse thongKeKhuyenMai() {
        KhuyenMaiThongKeResponse res = new KhuyenMaiThongKeResponse();

        res.setDangHoatDong(repo.countDangHoatDong());
        res.setHetHan(repo.countHetHan());
        res.setDaTat(repo.countDaTat());
        res.setTongKhuyenMai(repo.countAll());

        return res;
    }


    // ============= TRUY VẤN NÂNG CAO =============
    public Page<PhieuGiamGiaResponseDTO> getAll(String keyword, Integer doiTuong, Integer loaiGiamGia,
                                                Integer trangThai, String ngayBatDau, String ngayKetThuc,
                                                int page, int size) {
        if (keyword != null) {
            keyword = keyword.trim();      // bỏ khoảng trắng
            if (keyword.isEmpty()) {
                keyword = null;            // tránh LIKE %%
            }
        }
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

    public Page<PhieuGiamGiaResponseDTO> getValidPersonalVouchers(String keyword, Integer loaiGiamGia, Integer idKhachHang, int page, int size) {
        // Tiền xử lý keyword tránh lỗi LIKE %%
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

        // Sắp xếp mới nhất đưa lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Gọi query trong Repo
        Page<PhieuGiamGia> list = repo.findValidPersonalVouchers(keyword, loaiGiamGia, idKhachHang, pageable);

        // Map sang DTO trả về (đảm bảo mày đã có sẵn hàm mapToResponseDTO trong Service)
        return list.map(this::mapToResponseDTO);
    }

    public Page<PhieuGiamGiaResponseDTO> getValidPublicVouchers(String keyword, Integer loaiGiamGia, int page, int size) {
        if (keyword != null) {
            keyword = keyword.trim();
            if (keyword.isEmpty()) {
                keyword = null;
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Gọi thẳng query chỉ lấy phiếu còn hạn
        Page<PhieuGiamGia> list = repo.findValidPublicVouchers(keyword, loaiGiamGia, pageable);

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

        // ✅ CHECK TRÙNG CODE TRƯỚC
        if (repo.existsByCodeGiamGia(dto.getCodeGiamGia())) {
            throw new RuntimeException("Mã code đã tồn tại");
        }

        PhieuGiamGia entity = new PhieuGiamGia();
        mapToEntity(dto, entity);

        PhieuGiamGia savedPhieu = repo.save(entity);

        // Lưu bảng cá nhân
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

        // Gửi mail
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

    @Transactional
    public PhieuGiamGia update(Integer id, PhieuGiamGiaDTO dto) {
        PhieuGiamGia entity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ID: " + id));
        Integer oldTrangThai = entity.getTrangThai();

        if (repo.existsByCodeGiamGiaAndIdNot(dto.getCodeGiamGia(), id)) {
            throw new RuntimeException("Mã code đã tồn tại");
        }

        // --- LOGIC XỬ LÝ THAY ĐỔI KHÁCH HÀNG VÀ GỬI MAIL ---
        if (dto.getDoiTuong() == 1 && dto.getListIdKhachHang() != null) {
            List<Integer> newIds = dto.getListIdKhachHang() != null ? dto.getListIdKhachHang() : new ArrayList<>();

            // 1. Lấy danh sách khách hàng hiện tại từ DB
            List<PhieuGiamGiaCaNhan> currentList = phieuGiamGiaCaNhanRepo.findByPhieuGiamGiaId(id);
            List<Integer> oldIds = currentList.stream()
                    .map(cn -> cn.getKhachHang().getId())
                    .toList();

            // 2. Tìm khách hàng bị xóa (Có trong cũ, không có trong mới) -> Gửi mail HỦY
            for (PhieuGiamGiaCaNhan cn : currentList) {
                if (!newIds.contains(cn.getKhachHang().getId())) {
                    sendCancelEmail(cn.getKhachHang().getEmail(), entity);
                    phieuGiamGiaCaNhanRepo.delete(cn); // Xóa khỏi DB
                }
            }

            // 3. Tìm khách hàng mới thêm (Có trong mới, không có trong cũ) -> Gửi mail TẶNG
            for (Integer newId : newIds) {
                if (!oldIds.contains(newId)) {
                    KhachHang kh = khachHangRepo.findById(newId).orElse(null);
                    if (kh != null) {
                        // Lưu vào DB
                        PhieuGiamGiaCaNhan moi = new PhieuGiamGiaCaNhan();
                        moi.setPhieuGiamGia(entity);
                        moi.setKhachHang(kh);
                        phieuGiamGiaCaNhanRepo.save(moi);
                        // Gửi mail thông báo
                        sendNotificationEmails(List.of(kh.getEmail()), entity);
                    }
                }
            }
        }

        mapToEntity(dto, entity);

        PhieuGiamGia saved = repo.save(entity);

        // ✅ Nếu chuyển từ HOẠT ĐỘNG → NGỪNG thì gửi mail HỦY
        if (oldTrangThai != null
                && oldTrangThai == 1
                && saved.getTrangThai() == 0
                && saved.getDoiTuong() == 1) {

            sendMailWhenDeactivate(saved);
        }


// ✅ Nếu chuyển từ NGỪNG → HOẠT ĐỘNG thì gửi mail
        if (oldTrangThai != null
                && oldTrangThai == 0
                && saved.getTrangThai() == 1
                && saved.getDoiTuong() == 1) {

            sendMailWhenActive(saved);
        }

        return saved;

    }
    @Async
    protected void sendMailWhenDeactivate(PhieuGiamGia p) {

        List<PhieuGiamGiaCaNhan> list =
                phieuGiamGiaCaNhanRepo.findByPhieuGiamGiaId(p.getId());

        if (list == null || list.isEmpty()) return;

        for (PhieuGiamGiaCaNhan cn : list) {

            if (cn.getKhachHang() != null
                    && cn.getKhachHang().getEmail() != null) {

                sendCancelEmail(
                        cn.getKhachHang().getEmail(),
                        p
                );
            }
        }
    }

    @Async
    protected void sendMailWhenActive(PhieuGiamGia p) {

        // Lấy danh sách khách đã gán voucher
        List<PhieuGiamGiaCaNhan> list =
                phieuGiamGiaCaNhanRepo.findByPhieuGiamGiaId(p.getId());

        if (list == null || list.isEmpty()) return;

        List<String> emails = new ArrayList<>();

        for (PhieuGiamGiaCaNhan cn : list) {
            if (cn.getKhachHang() != null
                    && cn.getKhachHang().getEmail() != null) {

                emails.add(cn.getKhachHang().getEmail());
            }
        }

        if (!emails.isEmpty()) {
            sendNotificationEmails(emails, p);
        }
    }


    @Async
    protected void sendCancelEmail(String email, PhieuGiamGia p) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("CozyPot <email_cua_ban@gmail.com>");
            helper.setTo(email);
            helper.setSubject("[CozyPot] Thông báo thay đổi chương trình ưu đãi");

            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; padding: 20px;'>" +
                            "<h2 style='color: #7B1F1F;'>Thông báo thu hồi mã giảm giá</h2>" +
                            "<p>Xin chào quý khách,</p>" +
                            "<p>Chúng tôi rất tiếc phải thông báo rằng mã giảm giá <strong>" + p.getCodeGiamGia() + "</strong> " +
                            "trước đó đã được thay đổi hoặc không còn áp dụng cho tài khoản của bạn trong đợt cập nhật này.</p>" +
                            "<p>Đừng lo lắng, hãy theo dõi các chương trình khuyến mãi tiếp theo của <strong>CozyPot</strong> để nhận được nhiều ưu đãi hấp dẫn khác.</p>" +
                            "<p>Trân trọng,<br>Đội ngũ CozyPot</p>" +
                            "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            System.out.println("Đã gửi mail HỦY thành công cho: " + email);
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail hủy đến " + email + ": " + e.getMessage());
        }
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

        // 🔥 FIX QUAN TRỌNG
        if (p.getDotKhuyenMai() != null) {
            dto.setIdDotKhuyenMai(p.getDotKhuyenMai().getId());
            dto.setTenDotKhuyenMai(p.getDotKhuyenMai().getTenDotKhuyenMai());
        }


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

    @Transactional
    public void exportExcel(
            String keyword,
            Integer trangThai,
            Integer doiTuong,
            Integer loaiGiamGia,
            LocalDateTime ngayBatDau,
            LocalDateTime ngayKetThuc,
            OutputStream outputStream
    ) throws IOException {

        List<PhieuGiamGia> list = repo.findForExport(
                keyword,
                trangThai,
                doiTuong,
                loaiGiamGia,
                ngayBatDau,
                ngayKetThuc
        );

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Phiếu giảm giá");

        // ===== HEADER =====
        Row header = sheet.createRow(0);
        String[] columns = {
                "STT",
                "Mã phiếu",
                "Code",
                "Tên phiếu",
                "Loại giảm",
                "Giá trị giảm",
                "Đối tượng",
                "Số lượng",
                "Ngày bắt đầu",
                "Ngày kết thúc",
                "Trạng thái"
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }

        int rowIndex = 1;
        int stt = 1;
        long now = System.currentTimeMillis();

        // ===== DATA =====
        for (PhieuGiamGia pg : list) {
            Row row = sheet.createRow(rowIndex++);

            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(pg.getMaPhieuGiamGia());
            row.createCell(2).setCellValue(pg.getCodeGiamGia());
            row.createCell(3).setCellValue(pg.getTenPhieuGiamGia());
            row.createCell(4).setCellValue(pg.getLoaiGiamGia() == 1 ? "%" : "Tiền");
            row.createCell(5).setCellValue(
                    pg.getGiaTriGiam() != null ? pg.getGiaTriGiam().doubleValue() : 0
            );
            row.createCell(6).setCellValue(pg.getDoiTuong() == 0 ? "Công khai" : "Cá nhân");
            row.createCell(7).setCellValue(pg.getSoLuong());

            // NULL SAFE DATE
            row.createCell(8).setCellValue(
                    pg.getNgayBatDau() != null ? pg.getNgayBatDau().toString() : ""
            );
            row.createCell(9).setCellValue(
                    pg.getNgayKetThuc() != null ? pg.getNgayKetThuc().toString() : ""
            );

            String trangThaiText = "Không xác định";
            if (pg.getTrangThai() != null) {
                if (pg.getTrangThai() == 0) {
                    trangThaiText = "Ngừng hoạt động";
                } else if (pg.getNgayBatDau() != null && pg.getNgayKetThuc() != null) {
                    long start = pg.getNgayBatDau()
                            .atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli();
                    long end = pg.getNgayKetThuc()
                            .atZone(ZoneId.systemDefault())
                            .toInstant().toEpochMilli();

                    if (now < start) trangThaiText = "Sắp diễn ra";
                    else if (now > end) trangThaiText = "Hết hạn";
                    else trangThaiText = "Hoạt động";
                }
            }

            row.createCell(10).setCellValue(trangThaiText);
        }

        // ===== AUTO SIZE =====
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(outputStream);
        workbook.close();
    }


    @Async
    public void sendVoucherExpiredMail(PhieuGiamGia p) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("CozyPot <email_cua_ban@gmail.com>");
            helper.setTo("linhnnth03605@fpt.edu.vn"); // hoặc email KH
            helper.setSubject("[CozyPot] Voucher đã hết hạn");

            String html = """
            <h3>Voucher đã hết hạn</h3>
            <p>Mã: <b>%s</b></p>
            <p>Tên: %s</p>
            <p>Hạn: %s</p>
        """.formatted(
                    p.getCodeGiamGia(),
                    p.getTenPhieuGiamGia(),
                    p.getNgayKetThuc()
            );

            helper.setText(html, true);
            mailSender.send(mimeMessage);

        } catch (Exception e) {
            System.err.println("Lỗi gửi mail voucher hết hạn: " + e.getMessage());
        }
    }

    private void tinhLaiTongTienThanhToan(HoaDonThanhToan hoaDon) {
        BigDecimal tongTienChuaGiam = hoaDon.getTongTienChuaGiam() != null ? hoaDon.getTongTienChuaGiam() : BigDecimal.ZERO;
        BigDecimal giamGia = hoaDon.getSoTienDaGiam() != null ? hoaDon.getSoTienDaGiam() : BigDecimal.ZERO;
        BigDecimal tienCoc = hoaDon.getTienCoc() != null ? hoaDon.getTienCoc() : BigDecimal.ZERO;
        BigDecimal vatPhanTram = hoaDon.getVatApDung() != null ? hoaDon.getVatApDung() : BigDecimal.ZERO;

        // 1. Tiền sau giảm = Tổng tiền - Giảm giá
        BigDecimal tienSauGiam = tongTienChuaGiam.subtract(giamGia);
        if (tienSauGiam.compareTo(BigDecimal.ZERO) < 0) tienSauGiam = BigDecimal.ZERO;

        // 2. Tiền VAT = Tiền sau giảm * (VAT / 100)
        BigDecimal tienVat = tienSauGiam.multiply(vatPhanTram).divide(BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);

        // 3. Cần thanh toán = Tiền sau giảm + VAT - Tiền cọc
        BigDecimal tongTienThanhToan = tienSauGiam.add(tienVat).subtract(tienCoc);

        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongTienThanhToan = BigDecimal.ZERO;
        }

        // Cập nhật lại giá trị vào thực thể
        hoaDon.setTongTienThanhToan(tongTienThanhToan);
    }

    @Transactional
    public void apDungVoucher(ApDungVoucherRequest request) {
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn"));

        // 🚨 BƯỚC 0: Nếu hóa đơn đã có voucher cũ, hoàn lại số lượng cho mã cũ trước
        hoanLaiSoLuongVoucher(hoaDon);

        PhieuGiamGia phieu = null;
        // 1. Tìm phiếu giảm giá
        if (request.getIdPhieuGiamGia() != null) {
            phieu = phieuGiamGiaRepository.findById(request.getIdPhieuGiamGia())
                    .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));
        } else if (request.getCodeGiamGia() != null && !request.getCodeGiamGia().isEmpty()) {
            phieu = phieuGiamGiaRepository.findByCodeGiamGiaAndTrangThai(request.getCodeGiamGia(), 1)
                    .orElseThrow(() -> new RuntimeException("Mã Code không hợp lệ hoặc đã hết hạn"));
        }

        if (phieu == null) throw new RuntimeException("Vui lòng chọn hoặc nhập mã giảm giá");

        // 2. Validate điều kiện tối thiểu & thời gian
        if (request.getTongTienHienTai().compareTo(phieu.getDonHangToiThieu()) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu: " + phieu.getDonHangToiThieu());
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(phieu.getNgayBatDau())) throw new RuntimeException("Chương trình chưa bắt đầu!");
        if (now.isAfter(phieu.getNgayKetThuc())) throw new RuntimeException("Chương trình đã kết thúc!");

        // 3. Phân loại đối tượng và Trừ số lượng
        if (phieu.getDoiTuong() != null && phieu.getDoiTuong() == 1) { // Mã Cá nhân
            if (request.getIdKhachHang() == null) throw new RuntimeException("Vui lòng đăng nhập để dùng mã này");

            PhieuGiamGiaCaNhan phieuCaNhan = phieuGiamGiaCaNhanRepository
                    .findByKhachAndPhieu(request.getIdKhachHang(), phieu.getId())
                    .orElseThrow(() -> new RuntimeException("Bạn không sở hữu mã này"));

            if (phieuCaNhan.getTrangThaiSuDung() != 0) throw new RuntimeException("Mã này đã được sử dụng");

            phieuCaNhan.setTrangThaiSuDung(1); // Đang áp dụng
            phieuCaNhan.setIdHoaDonThanhToan(hoaDon);
            phieuGiamGiaCaNhanRepository.save(phieuCaNhan);
        } else { // Mã Public
            if (phieu.getSoLuong() <= 0) throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");

            // 🚨 GIẢM số lượng trong kho
            phieu.setSoLuong(phieu.getSoLuong() - 1);
            phieuGiamGiaRepository.save(phieu);
        }

        // 4. Tính toán số tiền được giảm
        BigDecimal soTienGiam = BigDecimal.ZERO;
        if (phieu.getLoaiGiamGia() == 0) {
            soTienGiam = phieu.getGiaTriGiam();
        } else if (phieu.getLoaiGiamGia() == 1) {
            soTienGiam = request.getTongTienHienTai().multiply(phieu.getGiaTriGiam()).divide(BigDecimal.valueOf(100));
            if (soTienGiam.compareTo(phieu.getGiaTriGiamToiDa()) > 0) soTienGiam = phieu.getGiaTriGiamToiDa();
        }

        // 5. Cập nhật Hóa đơn
        hoaDon.setSoTienDaGiam(soTienGiam);

        // 🔥 THÊM MỚI: Cập nhật trực tiếp Khóa Ngoại thay vì ghi chú
        hoaDon.setIdPhieuGiamGia(phieu);

        tinhLaiTongTienThanhToan(hoaDon);
        hoaDonThanhToanRepository.save(hoaDon);
    }

    @Transactional
    public void huyVoucher(Integer idHoaDon) {
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Hóa đơn"));

        // 🚨 BƯỚC 1: Hoàn lại số lượng cho kho
        hoanLaiSoLuongVoucher(hoaDon);

        // 2. Trả lại các trường tài chính
        hoaDon.setSoTienDaGiam(BigDecimal.ZERO);

        // 🔥 XÓA MỚI: Xóa khóa ngoại voucher ra khỏi hóa đơn
        hoaDon.setIdPhieuGiamGia(null);

        tinhLaiTongTienThanhToan(hoaDon);
        hoaDonThanhToanRepository.save(hoaDon);

        // 4. Giải phóng Phiếu cá nhân (nếu có)
        List<PhieuGiamGiaCaNhan> phieuDangGiu = phieuGiamGiaCaNhanRepository.findAllByHoaDon(idHoaDon);
        for (PhieuGiamGiaCaNhan p : phieuDangGiu) {
            p.setTrangThaiSuDung(0); // Trở lại trạng thái có thể sử dụng
            p.setIdHoaDonThanhToan(null);
            phieuGiamGiaCaNhanRepository.save(p);
        }
    }

    private void hoanLaiSoLuongVoucher(HoaDonThanhToan hoaDon) {
        // 🔥 SỬA LẠI: Trích xuất trực tiếp từ khóa ngoại, không cần Regex phức tạp
        PhieuGiamGia p = hoaDon.getIdPhieuGiamGia();

        if (p != null) {
            // Chỉ hoàn số lượng nếu là mã PUBLIC (0)
            if (p.getDoiTuong() != null && p.getDoiTuong() == 0) {
                p.setSoLuong(p.getSoLuong() + 1);
                phieuGiamGiaRepository.save(p);
            }
        }
    }

    @Transactional
    public void kiemTraLaiDieuKienVoucher(Integer idHoaDon) {
        HoaDonThanhToan hoaDon = hoaDonThanhToanRepository.findById(idHoaDon).orElse(null);

        // Bỏ qua nếu hóa đơn không tồn tại hoặc chưa áp dụng voucher nào
        if (hoaDon == null || hoaDon.getIdPhieuGiamGia() == null) {
            return;
        }

        // 🔥 SỬA LẠI: Lấy phiếu trực tiếp từ Entity
        PhieuGiamGia phieu = hoaDon.getIdPhieuGiamGia();

        // Tính lại tổng tiền món hiện tại của hóa đơn
        BigDecimal tongTienMoi = hoaDon.getTongTienChuaGiam();

        // Nếu tổng tiền rớt xuống thấp hơn yêu cầu của phiếu -> HỦY PHIẾU TỰ ĐỘNG
        if (tongTienMoi.compareTo(phieu.getDonHangToiThieu()) < 0) {
            huyVoucher(idHoaDon);
        } else {
            // CẬP NHẬT LẠI SỐ TIỀN GIẢM
            if (phieu.getLoaiGiamGia() == 1) { // 1 = Theo %
                BigDecimal phanTram = phieu.getGiaTriGiam().divide(BigDecimal.valueOf(100));
                BigDecimal soTienGiamMoi = tongTienMoi.multiply(phanTram);

                if (soTienGiamMoi.compareTo(phieu.getGiaTriGiamToiDa()) > 0) {
                    soTienGiamMoi = phieu.getGiaTriGiamToiDa();
                }

                hoaDon.setSoTienDaGiam(soTienGiamMoi);
                tinhLaiTongTienThanhToan(hoaDon); // Cập nhật cả tổng thanh toán
                hoaDonThanhToanRepository.save(hoaDon);
            }
        }
    }
}