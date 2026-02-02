package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGia;
import com.example.datn_cozypot_spring_boot.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherMailService {

    @Autowired
    private PhieuGiamGiaRepository repo;

    @Autowired
    private PhieuGiamGiaService mailService;

    public void guiMailVoucherHetHan() {
        List<PhieuGiamGia> list =
                repo.findByNgayKetThucBeforeAndDaGuiMailHetHanFalse(LocalDateTime.now());

        for (PhieuGiamGia p : list) {
            // 1. gửi mail
            mailService.sendVoucherExpiredMail(p);

            // 2. đánh dấu đã gửi
            p.setDaGuiMailHetHan(true);
        }

        // 3. lưu lại
        repo.saveAll(list);
    }
}
