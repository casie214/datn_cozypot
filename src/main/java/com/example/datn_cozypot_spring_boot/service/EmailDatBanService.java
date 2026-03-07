package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.request.EmailDatBanDTO;
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
}