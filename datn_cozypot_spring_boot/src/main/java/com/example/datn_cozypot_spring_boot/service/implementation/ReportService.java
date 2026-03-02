package com.example.datn_cozypot_spring_boot.service.implementation;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendDailyExcelReport(String toEmail, byte[] excelContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("BÁO CÁO DOANH THU COZYPOT - " + LocalDate.now());
        helper.setText("Chào Admin, đính kèm là báo cáo doanh thu chi tiết của ngày hôm nay.");

        // Đính kèm file Excel từ mảng byte
        InputStreamSource attachment = new ByteArrayResource(excelContent);
        helper.addAttachment("Bao-cao-CozyPot-" + LocalDate.now() + ".xlsx", attachment);

        mailSender.send(message);
    }
}
