package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToan.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.entity.BanAn;
import com.example.datn_cozypot_spring_boot.entity.HoaDonThanhToan;
import com.example.datn_cozypot_spring_boot.entity.LichSuHoaDon;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepo;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepo;
import com.example.datn_cozypot_spring_boot.repository.LichSuHoaDonRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class HoaDonThanhToanService {
    @Autowired
    HoaDonThanhToanRepo hoaDonThanhToanRepo;

    @Autowired
    ChiTietHoaDonService chiTietHoaDonService;

    @Autowired
    LichSuHoaDonRepo lichSuHoaDonRepo;

    @Autowired
    BanAnRepo banAnRepo;

    public List<HoaDonThanhToanResponse> getAllHoaDon(){
        return hoaDonThanhToanRepo.getAllHoaDon();
    }

    public List<HoaDonThanhToanResponse> searchHoaDon(Integer trangThai, Instant tuNgay, Instant denNgay){
        return hoaDonThanhToanRepo.searchHoaDon(trangThai, tuNgay, denNgay);
    }

    public HoaDonThanhToanResponse getHoaDonById(Integer id) {
        return hoaDonThanhToanRepo.getHoaDonById(id);
    }

    @Transactional
    public void huyHoaDon(LichSuHoaDonRequest request) {
        if (chiTietHoaDonService.hasAnyDishServed(request.getIdHoaDon())) {
            throw new RuntimeException("Không thể hủy hóa đơn vì đã có món ăn được phục vụ!");
        }

        HoaDonThanhToan hd = hoaDonThanhToanRepo.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiHDCu = hd.getTrangThaiHoaDon();

        hd.setTrangThaiHoaDon(0);
        hoaDonThanhToanRepo.save(hd);

        if (hd.getIdBanAn()!= null){
            hd.getIdBanAn().setTrangThai(0);
            banAnRepo.save(hd.getIdBanAn());
        }

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong(request.getHanhDong()); // "Hủy hóa đơn"
        log.setLyDoThucHien(request.getLyDoThucHien());
        log.setThoiGianThucHien(request.getThoiGianThucHien() != null ?
                request.getThoiGianThucHien() : Instant.now());

        log.setTrangThaiTruocDo(request.getTrangThaiLichSuTruocDo());
        log.setTrangThaiMoi(0);

        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepo.save(log);
    }

    @Transactional
    public void thanhToanHoaDon(LichSuHoaDonRequest request) {
        HoaDonThanhToan hd = hoaDonThanhToanRepo.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Instant now = Instant.now();
        hd.setTrangThaiHoaDon(2);
        hd.setThoiGianThanhToan(now);
        hoaDonThanhToanRepo.save(hd);

        if (hd.getIdBanAn()!= null){
            hd.getIdBanAn().setTrangThai(3);
            banAnRepo.save(hd.getIdBanAn());
        }

        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Thanh toán");
        log.setTrangThaiTruocDo(1);
        log.setTrangThaiMoi(4);
        log.setThoiGianThucHien(now);
        System.out.println(now);
        if (request.getIdNhanVien() != null) {
            NhanVien nv = new NhanVien();
            nv.setId(request.getIdNhanVien());
            log.setIdNhanVien(nv);
        }

        lichSuHoaDonRepo.save(log);
    }


}
