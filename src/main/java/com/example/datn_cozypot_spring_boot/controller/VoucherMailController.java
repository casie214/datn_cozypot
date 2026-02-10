package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.service.VoucherMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/voucher-mail")
public class VoucherMailController {

    @Autowired
    private VoucherMailService voucherMailService;

    /**
     * Gửi mail cho các voucher đã hết hạn (chưa gửi lần nào)
     * Dùng cho admin / test
     */
    @PostMapping("/send-expired")
    public ResponseEntity<?> sendExpiredVoucherMail() {
        voucherMailService.guiMailVoucherHetHan();
        return ResponseEntity.ok("Đã gửi mail voucher hết hạn (nếu có)");
    }
}
