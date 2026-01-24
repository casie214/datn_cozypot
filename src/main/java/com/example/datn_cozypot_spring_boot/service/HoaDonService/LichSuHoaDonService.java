package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO.LichSuHoaDonResponse;
import com.example.datn_cozypot_spring_boot.entity.LichSuHoaDon;
import com.example.datn_cozypot_spring_boot.repository.LichSuHoaDonRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LichSuHoaDonService {
    @Autowired
    LichSuHoaDonRepo lichSuHoaDonRepo;

    public List<LichSuHoaDonResponse> layLichSuDonHang(Integer idHoaDon) {
        List<LichSuHoaDon> logs = lichSuHoaDonRepo.findByHoaDonId(idHoaDon);
        return logs.stream().map(log -> {
            String type = "add";

            // Map trạng thái sang loại hành động cho Vue Timeline
            switch (log.getTrangThaiMoi()) {
                case 1: type = "create"; break;
                case 3:
                case 0: type = "delete"; break;
                case 2: type = "add"; break;
                case 4: type = "create"; break;
            }

            return LichSuHoaDonResponse.builder()
                    .idLog(log.getId())
                    .hanhDong(log.getHanhDong())
                    .thoiGian(log.getThoiGianThucHien().toString())
                    .nguoiThucHien(log.getIdNhanVien() != null ? log.getIdNhanVien().getHoTenNhanVien() : "Hệ thống")
                    .lyDo(log.getLyDoThucHien())
                    .trangThaiMoi(log.getTrangThaiMoi())
                    .loaiHanhDong(type)
                    .build();
        }).collect(Collectors.toList());
    }
}
