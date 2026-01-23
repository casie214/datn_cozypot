package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.HoaDonThanhToanDTO.HoaDonThanhToanResponse;
import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonRequest;
import com.example.datn_cozypot_spring_boot.entity.*;
import com.example.datn_cozypot_spring_boot.repository.BanAnRepo;
import com.example.datn_cozypot_spring_boot.repository.ChiTietHoaDonRepo;
import com.example.datn_cozypot_spring_boot.repository.HoaDonThanhToanRepo;
import com.example.datn_cozypot_spring_boot.repository.LichSuHoaDonRepo;
import com.example.datn_cozypot_spring_boot.service.HoaDonService.ChiTietHoaDonService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class HoaDonThanhToanService {
    @Autowired
    HoaDonThanhToanRepo hoaDonThanhToanRepo;

    @Autowired
    ChiTietHoaDonService chiTietHoaDonService;

    @Autowired
    ChiTietHoaDonRepo chiTietHoaDonRepo;

    @Autowired
    LichSuHoaDonRepo lichSuHoaDonRepo;

    @Autowired
    BanAnRepo banAnRepo;

    public Page<HoaDonThanhToanResponse> getAllHoaDon(Pageable pageable){
        return hoaDonThanhToanRepo.getAllHoaDon(pageable);
    }

    public Page<HoaDonThanhToanResponse> searchHoaDon(String key, Integer trangThai, Integer trangThaiHoanTien, Instant tuNgay, Instant denNgay, Pageable pageable){
        return hoaDonThanhToanRepo.searchHoaDon(key, trangThai, trangThaiHoanTien, tuNgay, denNgay, pageable);
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

        //  XỬ LÝ LOGIC HOÀN TIỀN CỌC ---
        BigDecimal tienCoc = hd.getTienCoc();
        if (tienCoc != null && tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            if (Boolean.TRUE.equals(request.getIsLoiDoQuan())) {
                // Lỗi do QUÁN: Chờ hoàn tiền (1), Hoàn trả = Tiền cọc
                hd.setTrangThaiHoanTien(1);
                hd.setTienHoanTra(tienCoc);
            } else {
                // Lỗi do KHÁCH (hoặc hủy sát giờ): Không hoàn (3), Hoàn trả = 0
                hd.setTrangThaiHoanTien(3);
                hd.setTienHoanTra(BigDecimal.ZERO);
            }
        } else {
            hd.setTrangThaiHoanTien(0);
            hd.setTienHoanTra(BigDecimal.ZERO);
        }

        List<ChiTietHoaDon> listgetAllMonAn = chiTietHoaDonRepo.findByIdHoaDon(hd.getId());
        for (ChiTietHoaDon chiTietHoaDon : listgetAllMonAn){
            chiTietHoaDon.setTrangThaiMon(0);
        }
        chiTietHoaDonRepo.saveAll(listgetAllMonAn);
        hoaDonThanhToanRepo.save(hd);
        if (hd.getIdBanAn()!= null){
            hd.getIdBanAn().setTrangThai(0);
            banAnRepo.save(hd.getIdBanAn());
        }
        LichSuHoaDon log = new LichSuHoaDon();
        log.setIdHoaDon(hd);
        log.setHanhDong("Hủy hóa đơn");
        String prefixLyDo = "";
        if (tienCoc != null && tienCoc.compareTo(BigDecimal.ZERO) > 0) {
            prefixLyDo = Boolean.TRUE.equals(request.getIsLoiDoQuan()) ? "[Hoàn cọc] " : "[Mất cọc] ";
        }
        log.setLyDoThucHien(prefixLyDo + request.getLyDoThucHien());

        log.setThoiGianThucHien(Instant.now());
        log.setTrangThaiTruocDo(trangThaiHDCu);
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
        log.setTrangThaiMoi(2);
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
