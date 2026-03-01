package com.example.datn_cozypot_spring_boot.service;
import com.example.datn_cozypot_spring_boot.dto.KhuyenMaiThongKeResponse;
import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import com.example.datn_cozypot_spring_boot.repository.DotKhuyenMaiRepository;
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

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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

        // ✅ CHECK TRÙNG CODE TRƯỚC
        if (repo.existsByCodeGiamGia(dto.getCodeGiamGia())) {
            throw new RuntimeException("Mã giảm giá đã tồn tại");
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
            throw new RuntimeException("Mã giảm giá đã tồn tại");
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


}