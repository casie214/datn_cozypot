package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.DanhMuc;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manage/food")
@CrossOrigin(origins = "http://localhost:5173")
public class MonAnController {
    private final MonAnService monAnService;

    @GetMapping
    public ResponseEntity<List<MonAnResponse>> findAllMonAn() {
        List<MonAnResponse> monAnResponses = monAnService.findAllMonAn();
        System.out.println(monAnResponses);

        return ResponseEntity.ok(monAnResponses);
    }

    @GetMapping("/hotpotGeneral")
    public ResponseEntity<List<SetLauResponse>> findAllSetLau(){
        List<SetLauResponse> setLauResponses = monAnService.findAllSetLau();
        return ResponseEntity.ok(setLauResponses);
    }

    @GetMapping("/foodDetail")
    public ResponseEntity<List<MonAnChiTietResponse>> findMonAnFoodDetail(){
        List<MonAnChiTietResponse> monAnChiTietResponses = monAnService.findAllChiTietMonAn();
        return ResponseEntity.ok(monAnChiTietResponses);
    }

    @GetMapping("/modal/{id}")
    public ResponseEntity<List<MonAnChiTietResponse>> findMonAnModal(@PathVariable int id){
        MonAnResponse monAnResponse = monAnService.findMonAnById(id);
        List<MonAnChiTietResponse> monAnChiTietResponses = monAnService.findChiTietMonAnByMonAnId(id);
        return ResponseEntity.ok(monAnChiTietResponses);
    }

    @GetMapping("/category")
    public ResponseEntity<List<DanhMucResponse>> findMonAnCategory(){
        return ResponseEntity.ok(monAnService.findAllDanhMuc());
    }

    @GetMapping("/category/detail")
    public ResponseEntity<List<DanhMucChiTietResponse>> findMonAnCategoryDetail(){
        return ResponseEntity.ok(monAnService.findAllDanhMucChiTiet());
    }

    @GetMapping("/category/hotpotType")
    public ResponseEntity<List<LoaiLauResponse>> findLoaiLauCategory(){
        return ResponseEntity.ok(monAnService.findAllLoaiLau());
    }
}
