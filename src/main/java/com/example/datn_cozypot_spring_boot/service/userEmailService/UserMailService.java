package com.example.datn_cozypot_spring_boot.service.userEmailService;

import com.example.datn_cozypot_spring_boot.dto.DiaChiRequest;
import com.example.datn_cozypot_spring_boot.dto.NhanVienRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import com.example.datn_cozypot_spring_boot.dto.KhachHangRequest;
@Service
public class UserMailService {

    @Autowired
    private JavaMailSender mailSender;

    private final String COMPANY_EMAIL = "datlichlaucozypot@gmail.com";

    public void sendStaffNotificationMail(NhanVienRequest request, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = type.equals("CREATE") ? "[COZYPOT] Thông báo tài khoản nhân viên mới" : "[COZYPOT] Thông báo cập nhật hồ sơ nhân sự";
            String htmlContent = buildHtmlTemplate(request, type);

            helper.setFrom(COMPANY_EMAIL, "COZYPOT SYSTEM");
            helper.setTo(request.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            // Bạn có thể quăng lỗi hoặc log tùy ý
        }
    }

    private String buildHtmlTemplate(NhanVienRequest req, String type) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String ngayBaoCao = java.time.LocalDate.now().format(dtf);
        String title = type.equals("CREATE") ? "THÔNG BÁO TÀI KHOẢN" : "CẬP NHẬT HỒ SƠ";
        String gender = req.getGioiTinh() ? "Nam" : "Nữ";

        return "<div style=\"background-color: #f4f4f4; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                "        " +
                "        <div style=\"background-color: #800000; padding: 25px; text-align: center; color: #ffffff;\">" +
                "            <h1 style=\"margin: 0; font-size: 24px; letter-spacing: 2px;\">COZYPOT</h1>" +
                "            <p style=\"margin: 5px 0 0; opacity: 0.8; font-size: 12px;\">INTERNAL STAFF REPORT</p>" +
                "        </div>" +
                "        " +
                "        <div style=\"padding: 25px;\">" +
                "            <div style=\"text-align: center; margin-bottom: 25px;\">" +
                "                <h2 style=\"color: #333; margin: 0; text-transform: uppercase; font-size: 18px;\">" + title + "</h2>" +
                "                <p style=\"color: #888; font-size: 13px;\">Ngày báo cáo: " + ngayBaoCao + "</p>" +
                "            </div>" +
                "            " +
                "            " +
                "            <div style=\"border-top: 1px solid #eee; padding-top: 15px;\">" +
                "                <div style=\"margin-bottom: 15px;\">" +
                "                    <label style=\"color: #888; font-size: 12px; display: block; text-transform: uppercase;\">Họ và tên</label>" +
                "                    <span style=\"color: #800000; font-weight: bold; font-size: 16px;\">" + req.getHoTenNhanVien().toUpperCase() + "</span>" +
                "                </div>" +
                "                " +
                "                <div style=\"margin-bottom: 15px;\">" +
                "                    <label style=\"color: #888; font-size: 12px; display: block; text-transform: uppercase;\">Tên đăng nhập</label>" +
                "                    <span style=\"color: #333; font-weight: 500;\">" + req.getTenDangNhap() + "</span>" +
                "                </div>" +
                "                " +
                (type.equals("CREATE") ?
                        "                <div style=\"margin-bottom: 15px; background-color: #fff4f4; padding: 10px; border-radius: 4px; border-left: 4px solid #800000;\">" +
                                "                    <label style=\"color: #888; font-size: 12px; display: block; text-transform: uppercase;\">Mật khẩu đăng nhập lần đầu</label>" +
                                "                    <span style=\"color: #800000; font-weight: bold; font-size: 18px;\">" + req.getMatKhauDangNhap() + "</span>" +
                                "                    <p style=\"margin: 5px 0 0; font-size: 11px; color: #666;\">* Vui lòng đổi mật khẩu sau khi đăng nhập để bảo mật.</p>" +
                                "                </div>" : "") +
                "                <div style=\"margin-bottom: 15px;\">" +
                "                    <label style=\"color: #888; font-size: 12px; display: block; text-transform: uppercase;\">Email liên hệ</label>" +
                "                    <span style=\"color: #007bff;\">" + req.getEmail() + "</span>" +
                "                </div>" +
                "                " +
                "                <div style=\"margin-bottom: 15px;\">" +
                "                    <label style=\"color: #888; font-size: 12px; display: block; text-transform: uppercase;\">Số điện thoại</label>" +
                "                    <span style=\"color: #333;\">" + req.getSdtNhanVien() + "</span>" +
                "                </div>" +
                "                " +
                "                <div style=\"margin-bottom: 15px;\">" +
                "                    <label style=\"color: #888; font-size: 12px; display: block; text-transform: uppercase;\">Ngày sinh | Giới tính</label>" +
                "                    <span style=\"color: #333;\">" + req.getNgaySinh().format(dtf) + " | " + gender + "</span>" +
                "                </div>" +
                "                " +
                "                <div style=\"margin-bottom: 15px;\">" +
                "                    <label style=\"color: #888; font-size: 12px; display: block; text-transform: uppercase;\">Địa chỉ thường trú</label>" +
                "                    <span style=\"color: #333; line-height: 1.5;\">" + req.getDiaChi() + "</span>" +
                "                </div>" +
                "            </div>" +
                "            " +
                "            " +
                "            <div style=\"margin-top: 40px; border-top: 2px solid #800000; padding-top: 20px;\">" +
                "                <table width=\"100%\" style=\"text-align: center; font-size: 12px;\">" +
                "                    <tr>" +
                "                        <td>" +
                "                            <b>NGƯỜI LẬP BIỂU</b><br>" +
                "                            <span style=\"color: #800000;\">Hệ thống CozyPot</span>" +
                "                        </td>" +
                "                        <td>" +
                "                            <b>XÁC NHẬN</b><br>" +
                "                            <span style=\"color: #800000;\">Phòng Nhân Sự</span>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </div>" +
                "        </div>" +
                "        " +
                "        <div style=\"background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 11px; color: #999;\">" +
                "            Thông báo tự động từ hệ thống quản trị CozyPot. Vui lòng không phản hồi email này." +
                "        </div>" +
                "    </div>" +
                "</div>";
    }

    // --- BỔ SUNG HÀM DÀNH CHO KHÁCH HÀNG ---
    public void sendClientNotificationMail(KhachHangRequest request, String type) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = type.equals("CREATE") ? "[COZYPOT] Chào mừng khách hàng mới!" : "[COZYPOT] Thông báo cập nhật thông tin tài khoản";
            String htmlContent = buildClientHtmlTemplate(request, type);

            helper.setFrom(COMPANY_EMAIL, "COZYPOT RESTAURANT");
            helper.setTo(request.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String buildClientHtmlTemplate(KhachHangRequest req, String type) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String title = type.equals("CREATE") ? "CHÀO MỪNG THÀNH VIÊN MỚI" : "CẬP NHẬT THÔNG TIN";

        // An toàn hơn: Tránh NullPointerException hoặc hiển thị chữ "null"
        String userName = (req.getTenDangNhap() != null) ? req.getTenDangNhap() : "Liên hệ Admin";
        String password = (req.getMatKhauDangNhap() != null) ? req.getMatKhauDangNhap() : "Đã bảo mật";

        // --- LOGIC LẤY ĐỊA CHỈ ĐÃ CHỌN ---
        String diaChiChon = "Chưa cập nhật";
        if (req.getDanhSachDiaChi() != null && !req.getDanhSachDiaChi().isEmpty()) {
            diaChiChon = req.getDanhSachDiaChi().stream()
                    .filter(dc -> Boolean.TRUE.equals(dc.getLaMacDinh()))
                    .map(DiaChiRequest::getThongTinDiaChi)
                    .findFirst()
                    .orElse(req.getDanhSachDiaChi().get(0).getThongTinDiaChi());
        }

        return "<div style=\"background-color: #f4f4f4; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">" +
                "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                "        <div style=\"background-color: #800000; padding: 25px; text-align: center; color: #ffffff;\">" +
                "            <h1 style=\"margin: 0; font-size: 24px; letter-spacing: 2px;\">COZYPOT</h1>" +
                "            <p style=\"margin: 5px 0 0; opacity: 0.8; font-size: 12px;\">" + title + "</p>" +
                "        </div>" +
                "        <div style=\"padding: 25px;\">" +
                "            <p>Xin chào <b>" + req.getTenKhachHang() + "</b>,</p>" +
                "            <p>Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ tại <b>CozyPot</b>. Dưới đây là thông tin tài khoản của bạn:</p>" +
                "            <div style=\"border-top: 1px solid #eee; padding-top: 15px; margin-top: 15px;\">" +
                "                <div style=\"margin-bottom: 10px;\">" +
                "                    <span style=\"color: #888; font-size: 13px;\">Tên đăng nhập:</span>" +
                "                    <span style=\"color: #333; font-weight: bold; margin-left: 10px;\">" + userName + "</span>" +
                "                </div>" +
                (type.equals("CREATE") ?
                        "                <div style=\"margin-bottom: 10px; background-color: #fff4f4; padding: 15px; border-radius: 4px; border-left: 4px solid #800000;\">" +
                                "                    <span style=\"color: #888; font-size: 13px;\">Mật khẩu tạm thời:</span>" +
                                "                    <span style=\"color: #800000; font-weight: bold; margin-left: 10px; font-size: 16px;\">" + password + "</span>" +
                                "                    <p style=\"margin: 5px 0 0; font-size: 11px; color: #cc0000;\">* Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu.</p>" +
                                "                </div>" : "") +
                "                <div style=\"margin-bottom: 10px;\">" +
                "                    <span style=\"color: #888; font-size: 13px;\">Số điện thoại:</span>" +
                "                    <span style=\"color: #333; margin-left: 10px;\">" + req.getSoDienThoai() + "</span>" +
                "                </div>" +
                "                <div style=\"margin-bottom: 10px;\">" +
                "                    <span style=\"color: #888; font-size: 13px;\">Địa chỉ mặc định:</span>" +
                "                    <span style=\"color: #333; margin-left: 10px;\">" + diaChiChon + "</span>" +
                "                </div>" +
                "            </div>" +
                "            <div style=\"margin-top: 30px; text-align: center;\">" +
                "                <a href=\"http://your-website.com/login\" style=\"display: inline-block; background-color: #800000; color: #ffffff; padding: 12px 25px; border-radius: 5px; text-decoration: none; font-weight: bold; margin-top: 10px;\">ĐĂNG NHẬP NGAY</a>" +
                "            </div>" +
                "        </div>" +
                "        <div style=\"background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 11px; color: #999;\">" +
                "            © 2026 CozyPot Restaurant. Hệ thống gửi tin tự động." +
                "        </div>" +
                "    </div>" +
                "</div>";
    }
}