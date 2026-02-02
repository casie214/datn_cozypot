package com.example.datn_cozypot_spring_boot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PhieuDatBanScheduler {

    private final DatBanService service;

    @Scheduled(cron = "0 */5 * * * ?")
    public void autoUpdatePhieuDatBan() {
        service.autoUpdateTrangThaiPhieu();
    }
}
