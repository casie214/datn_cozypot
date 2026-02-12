package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
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
    private final MonAnService monAnService;

    @GetMapping("/category/active")
    public ResponseEntity<List<DanhMucResponse>> getDanhMucActive(){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findDanhMucActive());

    }

    @GetMapping("/category-detail/active")
    public ResponseEntity<List<DanhMucChiTietResponse>> getDanhMucChiTietActive(){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findDanhMucChiTietActive());

    }

    @GetMapping("/hotpot/active")
    public ResponseEntity<List<SetLauResponse>> getSetLauActive(){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findSetLauActive());

    }

    @GetMapping("/hotpot-type/active")
    public ResponseEntity<List<LoaiLauResponse>> getLoaiSetLauActive(){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findLoaiSetLauActive());

    }

    @GetMapping("/food-detail/active")
    public ResponseEntity<List<MonAnChiTietResponse>> getChiTietMonAnActive(){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findChiTietMonAnActive());

    }

    @GetMapping("/food/active")
    public ResponseEntity<List<MonAnResponse>> getMonAnActive(){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findMonAnActive());

    }

    @GetMapping("/hotpot/top/{metric}")
    public ResponseEntity<List<SetLauResponse>> getSetLauTheoTop(@PathVariable int metric){
        return ResponseEntity.status(HttpStatus.OK).body(monAnService.findSetLauTop(metric));

    }

}
