package com.example.datn_cozypot_spring_boot.service.implementation;

import com.example.datn_cozypot_spring_boot.service.VoucherMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class VoucherMailScheduler {

    @Autowired
    private VoucherMailService voucherMailService;

    @Scheduled(cron = "0 0 0 * * ?") // 0h mỗi ngày
    public void run() {
        voucherMailService.guiMailVoucherHetHan();
    }
}

