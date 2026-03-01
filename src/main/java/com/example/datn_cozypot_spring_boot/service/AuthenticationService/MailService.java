package com.example.datn_cozypot_spring_boot.service.AuthenticationService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendForgotPasswordEmail(String to, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("CozyPot <linhnnth03605@fpt.edu.vn>");
            helper.setTo(to);
            helper.setSubject("Khôi phục mật khẩu - Hệ thống CozyPot");
            String content = "<div style=\"background-color: #f4f4f4; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">" +
                    "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                    "        <div style=\"background-color: #800000; padding: 25px; text-align: center; color: #ffffff;\">" +
                    "            <h1 style=\"margin: 0; font-size: 24px; letter-spacing: 2px;\">COZYPOT</h1>" +
                    "            <p style=\"margin: 5px 0 0; opacity: 0.8; font-size: 12px;\">KHÔI PHỤC MẬT KHẨU</p>" +
                    "        </div>" +
                    "        <div style=\"padding: 25px;\">" +
                    "            <p>Xin chào,</p>" +
                    "            <p>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản <b>CozyPot</b> của bạn. Dưới đây là thông tin mật khẩu mới:</p>" +
                    "            <div style=\"border-top: 1px solid #eee; padding-top: 15px; margin-top: 15px;\">" +
                    "                <div style=\"margin-bottom: 10px; background-color: #fff4f4; padding: 15px; border-radius: 4px; border-left: 4px solid #800000;\">" +
                    "                    <span style=\"color: #888; font-size: 13px;\">Mật khẩu mới:</span>" +
                    "                    <span style=\"color: #800000; font-weight: bold; margin-left: 10px; font-size: 18px; letter-spacing: 1px;\">" + newPassword + "</span>" +
                    "                    <p style=\"margin: 5px 0 0; font-size: 11px; color: #cc0000;\">* Vì lý do bảo mật, vui lòng đổi mật khẩu ngay sau khi đăng nhập thành công.</p>" +
                    "                </div>" +
                    "            </div>" +
                    "            <p style=\"font-size: 13px; color: #666; margin-top: 20px;\">Nếu bạn không yêu cầu thay đổi này, bạn có thể an tâm bỏ qua email này.</p>" +
                    "            <div style=\"margin-top: 30px; text-align: center;\">" +
                    "                <a href=\"http://localhost:5173/login\" style=\"display: inline-block; background-color: #800000; color: #ffffff; padding: 12px 25px; border-radius: 5px; text-decoration: none; font-weight: bold;\">ĐĂNG NHẬP LẠI</a>" +
                    "            </div>" +
                    "        </div>" +
                    "        <div style=\"background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 11px; color: #999;\">" +
                    "            © 2026 CozyPot Restaurant. Hệ thống gửi tin tự động." +
                    "        </div>" +
                    "    </div>" +
                    "</div>";
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
           // throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }
    public void sendChangePasswordEmail(String to, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("CozyPot <linhnnth03605@fpt.edu.vn>");
            helper.setTo(to);
            helper.setSubject("Thông báo đổi mật khẩu thành công - CozyPot");

            String content = "<div style=\"background-color: #f4f4f4; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">" +
                    "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                    "        <div style=\"background-color: #800000; padding: 25px; text-align: center; color: #ffffff;\">" +
                    "            <h1 style=\"margin: 0; font-size: 24px; letter-spacing: 2px;\">COZYPOT</h1>" +
                    "            <p style=\"margin: 5px 0 0; opacity: 0.8; font-size: 12px;\">THÔNG BÁO THAY ĐỔI MẬT KHẨU</p>" +
                    "        </div>" +
                    "        <div style=\"padding: 25px;\">" +
                    "            <p>Xin chào,</p>" +
                    "            <p>Bạn vừa thực hiện thay đổi mật khẩu cho tài khoản <b>CozyPot</b> của mình. Dưới đây là thông tin mật khẩu mới của bạn:</p>" +
                    "            <div style=\"border-top: 1px solid #eee; padding-top: 15px; margin-top: 15px;\">" +
                    "                <div style=\"margin-bottom: 10px; background-color: #fffbf4; padding: 15px; border-radius: 4px; border-left: 4px solid #f39c12;\">" +
                    "                    <span style=\"color: #888; font-size: 13px;\">Mật khẩu mới:</span>" +
                    "                    <span style=\"color: #800000; font-weight: bold; margin-left: 10px; font-size: 18px; letter-spacing: 1px;\">" + newPassword + "</span>" +
                    "                </div>" +
                    "            </div>" +
                    "            <p style=\"font-size: 13px; color: #666; margin-top: 20px;\">Nếu bạn <b>không</b> thực hiện việc đổi mật khẩu này, hãy liên hệ ngay với quản trị viên để bảo vệ tài khoản.</p>" +
                    "            <div style=\"margin-top: 30px; text-align: center;\">" +
                    "                <a href=\"http://localhost:5173/login\" style=\"display: inline-block; background-color: #800000; color: #ffffff; padding: 12px 25px; border-radius: 5px; text-decoration: none; font-weight: bold;\">ĐĂNG NHẬP NGAY</a>" +
                    "            </div>" +
                    "        </div>" +
                    "        <div style=\"background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 11px; color: #999;\">" +
                    "            © 2026 CozyPot Restaurant. Tin nhắn này được gửi tự động từ hệ thống." +
                    "        </div>" +
                    "    </div>" +
                    "</div>";

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendNotifyChangePasswordEmail(String to, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("CozyPot <linhnnth03605@fpt.edu.vn>");
            helper.setTo(to);
            helper.setSubject("Cảnh báo bảo mật: Mật khẩu đã được thay đổi - CozyPot");

            String content = "<div style=\"background-color: #f4f4f4; padding: 20px; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\">" +
                    "    <div style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1);\">" +
                    "        <div style=\"background-color: #2c3e50; padding: 25px; text-align: center; color: #ffffff;\">" + // Đổi màu xám xanh cho trang nhã
                    "            <h1 style=\"margin: 0; font-size: 24px; letter-spacing: 2px;\">COZYPOT</h1>" +
                    "            <p style=\"margin: 5px 0 0; opacity: 0.8; font-size: 12px;\">THÔNG BÁO BẢO MẬT</p>" +
                    "        </div>" +
                    "        <div style=\"padding: 25px;\">" +
                    "            <p>Xin chào <b>" + fullName + "</b>,</p>" +
                    "            <p>Chúng tôi gửi email này để xác nhận rằng mật khẩu tài khoản CozyPot của bạn <b>vừa được thay đổi thành công</b>.</p>" +
                    "            <div style=\"margin-top: 20px; padding: 15px; background-color: #fff9db; border-left: 4px solid #f1c40f; color: #856404;\">" +
                    "                Nếu bạn <b>không thực hiện</b> thay đổi này, hãy nhấn vào nút bên dưới để liên hệ với chúng tôi và khóa tài khoản ngay lập tức nhằm đảm bảo an toàn." +
                    "            </div>" +
                    "            <div style=\"margin-top: 30px; text-align: center;\">" +
                    "                <a href=\"http://localhost:5173/login\" style=\"display: inline-block; background-color: #800000; color: #ffffff; padding: 12px 25px; border-radius: 5px; text-decoration: none; font-weight: bold;\">ĐẾN TRANG QUẢN LÝ</a>" +
                    "            </div>" +
                    "        </div>" +
                    "        <div style=\"background-color: #f9f9f9; padding: 15px; text-align: center; font-size: 11px; color: #999;\">" +
                    "            © 2026 CozyPot Restaurant. Tin nhắn này được gửi tự động vì lý do bảo mật." +
                    "        </div>" +
                    "    </div>" +
                    "</div>";

            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
