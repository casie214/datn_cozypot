package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.ChiTietHoaDon;
import com.example.datn_cozypot_spring_boot.repository.ChiTietHoaDonRepository;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.SetLauChiTietRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChiTietHoaDonService {
    @Autowired
    ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    SetLauChiTietRepository setLauChiTietRepository;

    public List<ChiTietHoaDonResponse> getAllChiTietHoaDon(Integer idHoaDon) {
        List<ChiTietHoaDon> list = chiTietHoaDonRepository.findByIdHoaDon(idHoaDon);

        return list.stream().map(item -> {
            ChiTietHoaDonResponse dto = new ChiTietHoaDonResponse();
            dto.setId(item.getId());
            dto.setSoLuong(item.getSoLuong());
            dto.setDonGia(item.getDonGiaTaiThoiDiemBan());
            dto.setThanhTien(item.getThanhTien());
            dto.setGhiChu(item.getGhiChuMon());

            if (item.getIdChiTietMonAn() != null) {
                dto.setTenMon(item.getIdChiTietMonAn().getTenChiTietMonAn());
                dto.setIdSetLau(null);
            }
            else if (item.getIdSetLau() != null) {
                dto.setTenMon(item.getIdSetLau().getTenSetLau());
                dto.setIdSetLau(item.getIdSetLau().getId());
            }
            else {
                dto.setTenMon("Món không xác định");
            }

            dto.setTrangThaiCode(item.getTrangThaiMon());

            String textHienThi = "";
            if (item.getTrangThaiMon() != null) {
                switch (item.getTrangThaiMon()) {
                    case 0:
                        textHienThi = "Đã hủy";
                        break;
                    case 1:
                        textHienThi = "Chưa lên"; // Hoặc "Đang chế biến"
                        break;
                    case 2:
                        textHienThi = "Đã lên";   // Hoặc "Hoàn thành"
                        break;
                    default:
                        textHienThi = "Khác";
                }
            }
            dto.setTrangThaiText(textHienThi);

            return dto;
        }).collect(Collectors.toList());
    }

    public List<ChiTietSetLauResponse> getChiTietSetLau(Integer idSetLau){
        return setLauChiTietRepository.findChiTietBySetId(idSetLau);
    }

    @Transactional
    public void updateToServed(Integer idChiTietHD) {
        ChiTietHoaDon ct = chiTietHoaDonRepository.findById(idChiTietHD)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn!"));
        if (ct.getTrangThaiMon() == 1) {
            ct.setTrangThaiMon(2);
            chiTietHoaDonRepository.save(ct);
        }
    }

    // Cập nhật TẤT CẢ món của 1 hóa đơn sang "Đã lên"
    @Transactional
    public void updateAllToServed(Integer idHoaDon) {
        List<ChiTietHoaDon> dsMon = chiTietHoaDonRepository.findByIdHoaDon(idHoaDon);
        for (ChiTietHoaDon ct : dsMon) {
            if (ct.getTrangThaiMon() == 1) {
                ct.setTrangThaiMon(2);
            }
        }
        chiTietHoaDonRepository.saveAll(dsMon);
    }

    // Kiểm tra xem hóa đơn đã có món nào lên bàn (Trạng thái 2) chưa
    public boolean hasAnyDishServed(Integer idHoaDon) {
        return chiTietHoaDonRepository.existsByIdHoaDonAndTrangThaiMon(idHoaDon, 2);
    }
}