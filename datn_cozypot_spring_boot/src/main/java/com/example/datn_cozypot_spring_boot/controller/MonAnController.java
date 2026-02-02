package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.danhMucChiTiet.DanhMucChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauRequest;
import com.example.datn_cozypot_spring_boot.dto.loaiLau.LoaiLauResponse;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnRequest;
import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietRequest;
import com.example.datn_cozypot_spring_boot.dto.monAnChiTiet.MonAnChiTietResponse;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauRequest;
import com.example.datn_cozypot_spring_boot.dto.setLau.SetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.DanhMuc;
import com.example.datn_cozypot_spring_boot.service.MonAnService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/manage/food")
public class MonAnController {
    private final MonAnService monAnService;

    @Operation(summary = "Lấy danh sách món ăn", description = "Trả về danh sách tất cả món ăn trong hệ thống")
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

    @GetMapping("/hotpotGeneral/{id}")
    public ResponseEntity<SetLauResponse> findSetLauById(@PathVariable int id){
        SetLauResponse setLauResponses = monAnService.findSetLauById(id);
        return ResponseEntity.ok(setLauResponses);
    }

    @GetMapping("/foodDetail")
    public ResponseEntity<List<MonAnChiTietResponse>> findMonAnFoodDetail(){
        List<MonAnChiTietResponse> monAnChiTietResponses = monAnService.findAllChiTietMonAn();
        return ResponseEntity.ok(monAnChiTietResponses);
    }

    @GetMapping("/foodDetail/{id}")
    public ResponseEntity<MonAnChiTietResponse> findMonAnFoodDetailById(@PathVariable("id") int id){
        MonAnChiTietResponse monAnChiTietResponses = monAnService.findChiTietMonAnById(id);
        return ResponseEntity.ok(monAnChiTietResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MonAnResponse> findMonAnFoodById(@PathVariable("id") int id){
        MonAnResponse monAnResponses = monAnService.findMonAnById(id);
        return ResponseEntity.ok(monAnResponses);
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

    @PostMapping
    public ResponseEntity<MonAnResponse> addNewMon(@RequestBody @Valid MonAnRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createMonAn(request));
    }

    @PostMapping("/hotpotGeneral")
    public ResponseEntity<SetLauResponse> addNewLau(@RequestBody @Valid SetLauRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createSetLau(request));
    }

    @PostMapping("/foodDetail")
    public ResponseEntity<MonAnChiTietResponse> addNewChiTietMon(@RequestBody @Valid MonAnChiTietRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.createMonAnChiTiet(request));
    }

    @PutMapping("/foodDetail/{id}")
    public ResponseEntity<MonAnChiTietResponse> putChiTietMon(@PathVariable("id") int id, @RequestBody @Valid MonAnChiTietRequest request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(monAnService.putMonAnChiTiet(id, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonAnResponse> putMon(@PathVariable("id") int id, @RequestBody @Valid MonAnRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.putMonAn(id ,request));
    }

    @PutMapping("/hotpotGeneral/{id}")
    public ResponseEntity<SetLauResponse> putLau(@PathVariable("id") int id, @RequestBody @Valid SetLauRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.putLau(id ,request));
    }

    @PutMapping("/category/{id}")
    public ResponseEntity<DanhMucResponse> putDanhMuc(@PathVariable("id") int id, @RequestBody DanhMucRequest request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(monAnService.putDanhMuc(id, request));
    }

    @PostMapping("/category")
    public ResponseEntity<DanhMucResponse> addNewDanhMuc(@RequestBody @Valid DanhMucRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.addNewDanhMuc(request));
    }

    @PutMapping("/category/hotpotType/{id}")
    public ResponseEntity<LoaiLauResponse> putLoaiLau(@PathVariable("id") int id, @RequestBody @Valid LoaiLauRequest request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(monAnService.putLoaiLau(id, request));
    }

    @PostMapping("/category/hotpotType")
    public ResponseEntity<LoaiLauResponse> postNewLoaiLau(@RequestBody @Valid LoaiLauRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(monAnService.addNewLoaiLau(request));
    }

    @PutMapping("/category/detail/{id}")
    public ResponseEntity<DanhMucChiTietResponse> putDanhMucChiTiet(@PathVariable("id") int id, @RequestBody @Valid DanhMucChiTietRequest request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(monAnService.putDanhMucChiTiet(id, request));
    }

    @PostMapping("/category/detail")
    public ResponseEntity<DanhMucChiTietResponse> postNewDanhMucChiTiet(@RequestBody @Valid DanhMucChiTietRequest request){
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(monAnService.addNewDanhMucChiTiet(request));
    }

}
