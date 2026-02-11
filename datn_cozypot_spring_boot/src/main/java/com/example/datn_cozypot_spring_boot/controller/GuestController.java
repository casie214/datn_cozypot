package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/guest")
@RestController
public class GuestController {
    private final  MonAnService monAnService;

    @GetMapping("/hotpot/top/{metric}")
    public ResponseEntity<List<SetLauResponse>> getSetLauTheoTop(@PathVariable int metric){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findSetLauTop(metric));
    }
}
