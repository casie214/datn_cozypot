package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.request.EmailDatBanDTO;
import com.example.datn_cozypot_spring_boot.dto.request.EmailHuyDatBanDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Slf4j
@Service
public class EmailDatBanService {

    private final JavaMailSender bookingMailSender;

    private static final String FROM_EMAIL = "5762382@gmail.com";
    private static final String FROM_NAME  = "CozyPot - Đặt bàn";

    public EmailDatBanService(
            @Qualifier("bookingMailSender") JavaMailSender bookingMailSender) {
        this.bookingMailSender = bookingMailSender;
    }

    // ==================== PUBLIC API ====================

    /**
     * SYNC — DatBanService gọi bản này.
     * Throw exception nếu lỗi để caller biết và KHÔNG update trạng thái.
     */
    public void sendXacNhanDatBanSync(EmailDatBanDTO dto) throws MessagingException {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new MessagingException("Email trống");
        }

        MimeMessage message = bookingMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // ✅ Sửa: encode tên tiếng Việt đúng cách
        try {
            helper.setFrom(new InternetAddress(FROM_EMAIL,FROM_NAME, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            helper.setFrom(FROM_EMAIL); // fallback nếu encode lỗi
        }        helper.setTo(dto.getEmail());
        helper.setSubject("✅ Xác nhận đặt bàn thành công - CozyPot");
        helper.setText(buildHtmlContent(dto), true);

        bookingMailSender.send(message); // ← throw nếu lỗi SMTP
        log.info("✅ Đã gửi mail xác nhận đặt bàn tới: {}", dto.getEmail());
    }

    /**
     * ASYNC — fire-and-forget, dùng khi không cần biết kết quả.
     * Gọi lại sendXacNhanDatBanSync để tránh duplicate code.
     */
    @Async
    public void sendXacNhanDatBan(EmailDatBanDTO dto) {
        try {
            sendXacNhanDatBanSync(dto);
        } catch (MessagingException e) {
            log.error("❌ Lỗi gửi mail đặt bàn tới {}: {}", dto.getEmail(), e.getMessage());
        } catch (Exception e) {
            log.error("❌ Lỗi không xác định khi gửi mail: {}", e.getMessage());
        }
    }

    // ==================== PRIVATE HTML BUILDER ====================

    private String buildHtmlContent(EmailDatBanDTO dto) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0; padding:0; background:#f4f4f4;">
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 30px auto;
                            border: 1px solid #e0e0e0; border-radius: 14px; overflow: hidden;
                            box-shadow: 0 4px 16px rgba(0,0,0,0.08);">

                  <!-- ===== HEADER ===== -->
                  <div style="background: #7d161a; padding: 28px 24px; text-align: center;">
                    <h1 style="color: #fff; margin: 0; font-size: 24px; letter-spacing: 1px;">
                      🍲 CozyPot
                    </h1>
                    <p style="color: #f8d7da; margin: 8px 0 0; font-size: 15px;">
                      Xác nhận đặt bàn thành công
                    </p>
                  </div>

                  <!-- ===== BODY ===== -->
                  <div style="padding: 30px 28px; background: #ffffff;">
                    <p style="font-size: 15px; margin: 0 0 8px;">
                      Xin chào <strong>%s</strong>,
                    </p>
                    <p style="color: #555; font-size: 14px; margin: 0 0 20px; line-height: 1.6;">
                      Chúng tôi đã nhận được yêu cầu đặt bàn của bạn và xác nhận thông tin như sau:
                    </p>

                    <!-- Mã phiếu -->
                    <div style="background: #fff8f8; border: 1.5px dashed #7d161a;
                                border-radius: 8px; padding: 12px 16px; margin-bottom: 20px;
                                text-align: center;">
                      <span style="font-size: 13px; color: #999;">Mã phiếu đặt bàn</span><br/>
                      <span style="font-size: 22px; font-weight: bold; color: #7d161a;
                                   letter-spacing: 2px;">%s</span>
                    </div>

                    <!-- Bảng thông tin -->
                    <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                      <tr style="background: #f9f9f9;">
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; width: 38%%; color: #444;">📅 Thời gian</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; color: #444;">🪑 Bàn</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%s</td>
                      </tr>
                      <tr style="background: #f9f9f9;">
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; color: #444;">📍 Khu vực</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; color: #444;">👥 Số khách</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%d người</td>
                      </tr>
                      <tr style="background: #f9f9f9;">
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; color: #444;">📞 Số điện thoại</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%s</td>
                      </tr>
                    </table>

                    <!-- Lưu ý -->
                    <div style="background: #fff8f8; border-left: 4px solid #7d161a;
                                padding: 14px 16px; border-radius: 6px; margin-top: 22px;">
                      <p style="margin: 0 0 6px; color: #7d161a; font-weight: bold; font-size: 14px;">
                        ⚠️ Lưu ý quan trọng
                      </p>
                      <ul style="margin: 0; padding-left: 18px; color: #555;
                                 font-size: 13px; line-height: 1.8;">
                        <li>Vui lòng có mặt trước <strong>15 phút</strong> so với giờ đặt.</li>
                        <li>Bàn sẽ được giữ trong <strong>30 phút</strong> kể từ giờ đặt.</li>
                        <li>Mang theo mã phiếu <strong>%s</strong> khi đến nhà hàng.</li>
                      </ul>
                    </div>
                  </div>

                  <!-- ===== FOOTER ===== -->
                  <div style="background: #f5f5f5; padding: 18px 24px; text-align: center;
                              color: #888; font-size: 13px; line-height: 1.8;">
                    Cảm ơn bạn đã tin tưởng và chọn <strong>CozyPot</strong>! 🙏<br/>
                    Hotline hỗ trợ: <strong style="color: #7d161a;">0123 456 789</strong><br/>
                    <span style="font-size: 12px; color: #bbb;">
                      Email này được gửi tự động, vui lòng không phản hồi.
                    </span>
                  </div>

                </div>
                </body>
                </html>
                """.formatted(
                dto.getTenKhachHang(),
                dto.getMaPhieuDatBan(),
                dto.getThoiGianDat(),
                dto.getTenBan(),
                dto.getKhuVuc(),
                dto.getSoLuongKhach(),
                dto.getSoDienThoai(),
                dto.getMaPhieuDatBan()
        );
    }

    @Async
    public void sendEmailCamOnDatBan(EmailDatBanDTO dto) {
        try {
            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                return;
            }

            MimeMessage message = bookingMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(FROM_EMAIL);
            }

            helper.setTo(dto.getEmail());
            helper.setSubject("🕒 Cảm ơn bạn đã đặt bàn tại CozyPot - Đơn đang chờ xác nhận");
            helper.setText(buildHtmlCamOnContent(dto), true);

            bookingMailSender.send(message);
            log.info("✅ Đã gửi mail cảm ơn đặt bàn tới: {}", dto.getEmail());
        } catch (Exception e) {
            log.error("❌ Lỗi gửi mail cảm ơn tới {}: {}", dto.getEmail(), e.getMessage());
        }
    }

    private String buildHtmlCamOnContent(EmailDatBanDTO dto) {
        String thongBaoTaiKhoan = "";
        if (Boolean.TRUE.equals(dto.getIsTaiKhoanMoi())) {
            thongBaoTaiKhoan = """
                <div style="background: #e8f4fd; border-left: 4px solid #1890ff; padding: 14px 16px; border-radius: 6px; margin-top: 22px;">
                    <p style="margin: 0 0 6px; color: #0050b3; font-weight: bold; font-size: 14px;">
                        🎁 Tặng bạn: Tài khoản Thành viên
                    </p>
                    <p style="margin: 0; color: #434343; font-size: 13px; line-height: 1.6;">
                        Để quản lý đơn dễ dàng hơn, CozyPot đã tạo tự động cho bạn một tài khoản:<br>
                        • Email đăng nhập: <b>%s</b><br>
                        • Mật khẩu: <b>%s</b><br>
                        <i>(Bạn có thể đăng nhập để đổi mật khẩu, hoặc tiếp tục tra cứu nhanh bằng SĐT và Mã phiếu trên website).</i>
                    </p>
                </div>
                """.formatted(dto.getEmail(), dto.getMatKhauMoi());
        }

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0; padding:0; background:#f4f4f4;">
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 30px auto;
                            border: 1px solid #e0e0e0; border-radius: 14px; overflow: hidden;
                            box-shadow: 0 4px 16px rgba(0,0,0,0.08);">

                  <div style="background: #7d161a; padding: 28px 24px; text-align: center;">
                    <h1 style="color: #fff; margin: 0; font-size: 24px; letter-spacing: 1px;">
                      🍲 CozyPot
                    </h1>
                    <p style="color: #f8d7da; margin: 8px 0 0; font-size: 15px;">
                      Hệ thống đã ghi nhận yêu cầu đặt bàn
                    </p>
                  </div>

                  <div style="padding: 30px 28px; background: #ffffff;">
                    <p style="font-size: 15px; margin: 0 0 8px;">
                      Xin chào <strong>%s</strong>,
                    </p>
                    <p style="color: #555; font-size: 14px; margin: 0 0 20px; line-height: 1.6;">
                      Cảm ơn bạn đã lựa chọn CozyPot. Chúng tôi đã nhận được yêu cầu đặt bàn của bạn. Nhân viên của chúng tôi sẽ sớm liên hệ hoặc gửi email xác nhận chính thức cho bạn.
                    </p>

                    <div style="background: #fff8f8; border: 1.5px dashed #7d161a;
                                border-radius: 8px; padding: 12px 16px; margin-bottom: 20px;
                                text-align: center;">
                      <span style="font-size: 13px; color: #999;">Mã tra cứu đặt bàn của bạn</span><br/>
                      <span style="font-size: 22px; font-weight: bold; color: #7d161a;
                                   letter-spacing: 2px;">%s</span>
                    </div>

                    <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                      <tr style="background: #f9f9f9;">
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; width: 38%%; color: #444;">📅 Thời gian đến</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; color: #444;">👥 Số khách</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%d người</td>
                      </tr>
                      <tr style="background: #f9f9f9;">
                        <td style="padding: 11px 14px; border: 1px solid #eee;
                                   font-weight: bold; color: #444;">📞 Số điện thoại</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%s</td>
                      </tr>
                    </table>
                    
                    %s

                    <div style="background: #fff8f8; border-left: 4px solid #7d161a;
                                padding: 14px 16px; border-radius: 6px; margin-top: 22px;">
                      <p style="margin: 0 0 6px; color: #7d161a; font-weight: bold; font-size: 14px;">
                        💡 Mẹo nhỏ
                      </p>
                      <p style="margin: 0; color: #555; font-size: 13px; line-height: 1.6;">
                        Bạn có thể sử dụng <strong>Mã tra cứu</strong> ở trên để kiểm tra tiến trình đơn đặt bàn trực tiếp trên website của chúng tôi.
                      </p>
                    </div>
                  </div>

                  <div style="background: #f5f5f5; padding: 18px 24px; text-align: center;
                              color: #888; font-size: 13px; line-height: 1.8;">
                    Cảm ơn bạn đã tin tưởng và chọn <strong>CozyPot</strong>! 🙏<br/>
                    Hotline hỗ trợ: <strong style="color: #7d161a;">0123 456 789</strong><br/>
                    <span style="font-size: 12px; color: #bbb;">
                      Email này được gửi tự động, vui lòng không phản hồi.
                    </span>
                  </div>

                </div>
                </body>
                </html>
                """.formatted(
                dto.getTenKhachHang(),
                dto.getMaPhieuDatBan(),
                dto.getThoiGianDat(),
                dto.getSoLuongKhach(),
                dto.getSoDienThoai(),
                thongBaoTaiKhoan
        );
    }

    @Async
    public void sendEmailThanhToanThatBai(EmailDatBanDTO dto) {
        try {
            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                return;
            }

            MimeMessage message = bookingMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(FROM_EMAIL);
            }

            helper.setTo(dto.getEmail());
            helper.setSubject("⚠️ Thông báo thanh toán cọc không thành công - CozyPot");
            helper.setText(buildHtmlThanhToanThatBai(dto), true);

            bookingMailSender.send(message);
            log.info("✅ Đã gửi mail báo lỗi thanh toán cọc tới: {}", dto.getEmail());
        } catch (Exception e) {
            log.error("❌ Lỗi gửi mail báo lỗi thanh toán tới {}: {}", dto.getEmail(), e.getMessage());
        }
    }

    private String buildHtmlThanhToanThatBai(EmailDatBanDTO dto) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0; padding:0; background:#f4f4f4;">
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 30px auto;
                            border: 1px solid #e0e0e0; border-radius: 14px; overflow: hidden;
                            box-shadow: 0 4px 16px rgba(0,0,0,0.08);">

                  <div style="background: #7d161a; padding: 28px 24px; text-align: center;">
                    <h1 style="color: #fff; margin: 0; font-size: 24px; letter-spacing: 1px;">
                      🍲 CozyPot
                    </h1>
                    <p style="color: #f8d7da; margin: 8px 0 0; font-size: 15px;">
                      Thanh toán đặt cọc chưa hoàn tất
                    </p>
                  </div>

                  <div style="padding: 30px 28px; background: #ffffff;">
                    <p style="font-size: 15px; margin: 0 0 8px;">
                      Xin chào <strong>%s</strong>,
                    </p>
                    <p style="color: #555; font-size: 14px; margin: 0 0 20px; line-height: 1.6;">
                      Hệ thống ghi nhận giao dịch thanh toán tiền cọc cho đơn đặt bàn của bạn <strong>chưa thành công</strong> (hoặc đã bị hủy bỏ).
                    </p>

                    <div style="background: #fff8f8; border: 1.5px dashed #7d161a;
                                border-radius: 8px; padding: 12px 16px; margin-bottom: 20px;
                                text-align: center;">
                      <span style="font-size: 13px; color: #999;">Mã tra cứu đặt bàn của bạn</span><br/>
                      <span style="font-size: 22px; font-weight: bold; color: #7d161a;
                                   letter-spacing: 2px;">%s</span>
                    </div>

                    <div style="background: #fff8f8; border-left: 4px solid #7d161a;
                                padding: 14px 16px; border-radius: 6px; margin-top: 22px;">
                      <p style="margin: 0 0 6px; color: #7d161a; font-weight: bold; font-size: 14px;">
                        💡 Hướng dẫn tiếp theo
                      </p>
                      <p style="margin: 0 0 8px; color: #555; font-size: 13px; line-height: 1.6;">
                        Để bàn của bạn được giữ lại, vui lòng truy cập website của chúng tôi, sử dụng <strong>Mã tra cứu</strong> ở trên để tìm lại đơn hàng và thực hiện thanh toán cọc lại.
                      </p>
                      <p style="margin: 0; color: #d32f2f; font-size: 13px; font-weight: bold; line-height: 1.6;">
                        ⚠️ Lưu ý: Vui lòng hoàn tất thanh toán cọc trong vòng 15 phút kể từ lúc đặt bàn. Sau thời gian này, hệ thống sẽ tự động hủy đơn đặt bàn của bạn!
                      </p>
                    </div>
                  </div>

                  <div style="background: #f5f5f5; padding: 18px 24px; text-align: center;
                              color: #888; font-size: 13px; line-height: 1.8;">
                    Cảm ơn bạn đã tin tưởng và chọn <strong>CozyPot</strong>! 🙏<br/>
                    Hotline hỗ trợ: <strong style="color: #7d161a;">0123 456 789</strong><br/>
                    <span style="font-size: 12px; color: #bbb;">
                      Email này được gửi tự động, vui lòng không phản hồi.
                    </span>
                  </div>

                </div>
                </body>
                </html>
                """.formatted(
                dto.getTenKhachHang(),
                dto.getMaPhieuDatBan()
        );
    }


    // ==================== EMAIL HỦY ĐẶT BÀN ====================

    @Async
    public void sendEmailHuyDatBan(EmailHuyDatBanDTO dto) {
        try {
            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                return;
            }

            MimeMessage message = bookingMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            try {
                helper.setFrom(new InternetAddress(FROM_EMAIL, FROM_NAME, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                helper.setFrom(FROM_EMAIL);
            }

            helper.setTo(dto.getEmail());
            helper.setSubject("🚫 Thông báo hủy đặt bàn - CozyPot");
            helper.setText(buildHtmlHuyDatBan(dto), true);

            bookingMailSender.send(message);
            log.info("✅ Đã gửi mail thông báo hủy đặt bàn tới: {}", dto.getEmail());
        } catch (Exception e) {
            log.error("❌ Lỗi gửi mail hủy đặt bàn tới {}: {}", dto.getEmail(), e.getMessage());
        }
    }

    private String buildHtmlHuyDatBan(EmailHuyDatBanDTO dto) {
        String hoanCocHtml = "";
        if (dto.getTienHoanTra() != null && !dto.getTienHoanTra().isBlank() && !dto.getTienHoanTra().equals("0") && !dto.getTienHoanTra().contains("0 ₫")) {
            hoanCocHtml = """
                      <tr>
                        <td style="padding: 11px 14px; border: 1px solid #eee; font-weight: bold; color: #444;">💰 Tiền hoàn cọc</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #28a745; font-weight: bold;">%s</td>
                      </tr>
                    """.formatted(dto.getTienHoanTra());
        }

        String nguoiHuy = (dto.getNguoiHuy() != null && !dto.getNguoiHuy().isBlank()) ? dto.getNguoiHuy() : "Hệ thống";
        String lyDo = (dto.getLyDoHuy() != null && !dto.getLyDoHuy().isBlank()) ? dto.getLyDoHuy() : "Không có lý do cụ thể";

        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head><meta charset="UTF-8"/></head>
                <body style="margin:0; padding:0; background:#f4f4f4;">
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 30px auto; border: 1px solid #e0e0e0; border-radius: 14px; overflow: hidden; box-shadow: 0 4px 16px rgba(0,0,0,0.08);">
                  <div style="background: #6c757d; padding: 28px 24px; text-align: center;">
                    <h1 style="color: #fff; margin: 0; font-size: 24px; letter-spacing: 1px;">🍲 CozyPot</h1>
                    <p style="color: #e2e3e5; margin: 8px 0 0; font-size: 15px;">Thông báo hủy đơn đặt bàn</p>
                  </div>
                  <div style="padding: 30px 28px; background: #ffffff;">
                    <p style="font-size: 15px; margin: 0 0 8px;">Xin chào <strong>%s</strong>,</p>
                    <p style="color: #555; font-size: 14px; margin: 0 0 20px; line-height: 1.6;">Rất tiếc phải thông báo rằng đơn đặt bàn của bạn tại CozyPot đã bị hủy. Dưới đây là thông tin chi tiết:</p>
                    <div style="background: #f8f9fa; border: 1.5px dashed #6c757d; border-radius: 8px; padding: 12px 16px; margin-bottom: 20px; text-align: center;">
                      <span style="font-size: 13px; color: #999;">Mã phiếu đặt bàn</span><br/>
                      <span style="font-size: 22px; font-weight: bold; color: #495057; letter-spacing: 2px;">%s</span>
                    </div>
                    <table style="width: 100%%; border-collapse: collapse; font-size: 14px;">
                      <tr style="background: #f9f9f9;">
                        <td style="padding: 11px 14px; border: 1px solid #eee; font-weight: bold; width: 38%%; color: #444;">👤 Người thực hiện</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #222;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding: 11px 14px; border: 1px solid #eee; font-weight: bold; color: #444;">📝 Lý do hủy</td>
                        <td style="padding: 11px 14px; border: 1px solid #eee; color: #d32f2f;">%s</td>
                      </tr>
                      %s
                    </table>
                    <div style="background: #fff8f8; border-left: 4px solid #7d161a; padding: 14px 16px; border-radius: 6px; margin-top: 22px;">
                      <p style="margin: 0; color: #555; font-size: 13px; line-height: 1.6;">Nếu bạn có thắc mắc hoặc cần hỗ trợ về việc hoàn cọc (nếu có), vui lòng liên hệ ngay với hotline của nhà hàng. Chúng tôi rất mong được phục vụ bạn trong những lần tới!</p>
                    </div>
                  </div>
                  <div style="background: #f5f5f5; padding: 18px 24px; text-align: center; color: #888; font-size: 13px; line-height: 1.8;">
                    Cảm ơn bạn đã quan tâm tới <strong>CozyPot</strong>! 🙏<br/>
                    Hotline hỗ trợ: <strong style="color: #7d161a;">0123 456 789</strong><br/>
                    <span style="font-size: 12px; color: #bbb;">Email này được gửi tự động, vui lòng không phản hồi.</span>
                  </div>
                </div>
                </body>
                </html>
                """.formatted(
                dto.getTenKhachHang(),
                dto.getMaPhieuDatBan(),
                nguoiHuy,
                lyDo,
                hoanCocHtml
        );
    }
}