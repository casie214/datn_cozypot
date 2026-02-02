package com.example.datn_cozypot_spring_boot.controller;

import com.example.datn_cozypot_spring_boot.dto.request.*;
import com.example.datn_cozypot_spring_boot.dto.response.BanAnResponse;
import com.example.datn_cozypot_spring_boot.dto.response.DatBanListResponse;
import com.example.datn_cozypot_spring_boot.dto.response.KhuVucResponse;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepository;
import com.example.datn_cozypot_spring_boot.repository.KhuVucRepository;
import com.example.datn_cozypot_spring_boot.repository.PhieuDatBanRepository;
import com.example.datn_cozypot_spring_boot.service.DatBanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/dat-ban")
public class DatBanController {
    @Autowired
    DatBanService datBanService;

    @Autowired
    PhieuDatBanRepository phieuDatBanRepository;

    @Autowired
    BanAnRepository banAnRepository;

    @Autowired
    KhuVucRepository khuVucRepository;

    @GetMapping("/danh-sach")
    public List<DatBanListResponse> danhSach(){
        return datBanService.getAll();
    }

    @GetMapping("/danh-sach-check-in")
    public List<DatBanListResponse> danhSachCheckIn(){
        return datBanService.getAllByTrangThai();
    }

    @GetMapping("/danh-sach-ban-an")
    public List<BanAnResponse> danhSachBanAn(){
        return datBanService.getAllBanAn();
    }

    @GetMapping("/ban-an-detail/{id}")
    public BanAnResponse chiTietBanAn(@PathVariable("id") Integer id){
        return datBanService.getBanAnById(id);
    }

    @GetMapping("/danh-sach-khu-vuc")
    public List<KhuVucResponse> danhSachKhuVuc(){
        return datBanService.getAllKhuVuc();
    }


    @PostMapping("/search")
    public Page<DatBanListResponse> searchDatBan(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestBody DatBanSearchRequest req

    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "thoiGianDat"));
        return datBanService.searchDatBan(req, pageable);
    }

    @PostMapping("/add-ban-an")
    public void addBanAn(@RequestBody @Valid AddBanAnRequest addBanAnRequest){
        datBanService.addBanAn(addBanAnRequest);
    }

    @PutMapping("/update-ban-an")
    public void updateBanAn(@RequestBody @Valid UpdateBanAnRequest updateBanAnRequest){
        datBanService.updateBanAn(updateBanAnRequest);
    }


    // Endpoint cập nhật bàn cho phiếu đặt
    @PutMapping("/update")
    public void updatePhieuDatBan(@RequestBody @Valid DatBanUpdateRequest datBanUpdateRequest){
        datBanService.updateBanChoPhieu(datBanUpdateRequest);
    }

    @PutMapping("/update-trang-thai-ban")
    public void updateTrangThaiBan(@RequestBody @Valid DatBanUpdateRequest datBanUpdateRequest){
        datBanService.updateCheckIn(datBanUpdateRequest);
    }

    @PutMapping("/update-trang-thai-phieu")
    public ResponseEntity<?> updateTrangThai(
            @RequestBody @Valid UpdateTrangThaiPhieuRequest request
    ) {
        datBanService.updateTrangThai(
                request.getId(),
                request.getTrangThai()
        );
        return ResponseEntity.ok().build();
    }




}
