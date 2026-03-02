package com.example.datn_cozypot_spring_boot.service.implementation;

import com.example.datn_cozypot_spring_boot.dto.thongKe.ThongKeDoanhThuDTO;
import com.example.datn_cozypot_spring_boot.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ScheduledTasks {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ThongKeService thongKeService;

    // Chạy vào lúc 23:55 mỗi ngày
    @Scheduled(cron = "0 55 23 * * *") // 23:55 mỗi ngày
    public void autoReportToAdmin() {
        try {
            // 1. Lấy DTO thống kê cho ngày hôm nay
            // Giả sử tham số "Ngày" sẽ lấy dữ liệu trong ngày hiện tại
            ThongKeDoanhThuDTO thongKeHomNay =
                    thongKeService.layThongKeTheoLoai("Hôm nay", null, null);
            // 2. Tạo file Excel từ DTO vừa lấy được
            byte[] excelFile = thongKeService.generateExcelData(thongKeHomNay);

            // 3. Gửi cho Admin (Thay email thật của bạn vào đây)
            reportService.sendDailyExcelReport("linhnnth03605@fpt.edu.vn", excelFile);

            System.out.println("===> [CozyPot] Báo cáo doanh thu ngày " + LocalDate.now() + " đã gửi thành công!");
        } catch (Exception e) {
            System.err.println("===> [CozyPot] Lỗi gửi báo cáo tự động: " + e.getMessage());
            e.printStackTrace();
        }
    }
}