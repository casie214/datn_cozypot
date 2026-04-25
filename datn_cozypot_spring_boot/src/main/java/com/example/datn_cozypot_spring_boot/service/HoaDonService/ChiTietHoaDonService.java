package com.example.datn_cozypot_spring_boot.service.HoaDonService;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietHoaDonResponse;
import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.ChiTietHoaDon;
import com.example.datn_cozypot_spring_boot.repository.ChiTietHoaDonRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauChiTietRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChiTietHoaDonService {
    @Autowired
    ChiTietHoaDonRepository chiTietHoaDonRepository;

    @Autowired
    SetLauChiTietRepository setLauChiTietRepository;

    public List<ChiTietHoaDonResponse> getAllChiTietHoaDon(Integer idHoaDon) {
        List<ChiTietHoaDon> list = chiTietHoaDonRepository.findByIdHoaDon(idHoaDon);

        // Sử dụng LinkedHashMap để giữ đúng thứ tự món ăn xuất hiện
        Map<String, ChiTietHoaDonResponse> groupedMap = new LinkedHashMap<>();

        for (ChiTietHoaDon item : list) {
            // 1. Map entity sang DTO tạm thời để lấy thông tin
            ChiTietHoaDonResponse currentDto = mapToResponse(item);

            // 2. Tạo một key duy nhất để gộp: kết hợp Mã món + Trạng thái
            // Nếu món giống nhau nhưng trạng thái khác nhau (1 cái 'Chưa lên', 1 cái 'Đã lên') thì KHÔNG gộp.
            String key = currentDto.getMaMon() + "_" + currentDto.getTrangThaiCode() + "_" + currentDto.getDonGia();

            if (groupedMap.containsKey(key)) {
                // Nếu đã tồn tại món này với trạng thái này rồi -> Tiến hành cộng dồn
                ChiTietHoaDonResponse existingDto = groupedMap.get(key);

                existingDto.setSoLuong(existingDto.getSoLuong() + currentDto.getSoLuong());
                existingDto.setThanhTien(existingDto.getThanhTien().add(currentDto.getThanhTien()));

                // Gộp ghi chú nếu có (cách nhau bởi dấu phẩy)
                if (currentDto.getGhiChu() != null && !currentDto.getGhiChu().trim().isEmpty()) {
                    String oldGhiChu = existingDto.getGhiChu();
                    String newGhiChu = (oldGhiChu == null || oldGhiChu.isEmpty())
                            ? currentDto.getGhiChu()
                            : oldGhiChu + ", " + currentDto.getGhiChu();
                    existingDto.setGhiChu(newGhiChu);
                }
            } else {
                // Nếu chưa có thì bỏ vào Map
                groupedMap.put(key, currentDto);
            }
        }

        return new ArrayList<>(groupedMap.values());
    }

    private ChiTietHoaDonResponse mapToResponse(ChiTietHoaDon item) {
        ChiTietHoaDonResponse dto = new ChiTietHoaDonResponse();
        dto.setId(item.getId());
        dto.setSoLuong(item.getSoLuong());
        dto.setDonGia(item.getDonGiaTaiThoiDiemBan());
        dto.setThanhTien(item.getThanhTien());
        dto.setGhiChu(item.getGhiChuMon());
        dto.setTrangThaiCode(item.getTrangThaiMon());

        if (item.getIdChiTietMonAn() != null) {
            dto.setTenMon(item.getIdChiTietMonAn().getTenMon());
            dto.setMaMon(item.getIdChiTietMonAn().getMaMon());
            dto.setIdSetLau(null);
        } else if (item.getIdSetLau() != null) {
            dto.setTenMon(item.getIdSetLau().getTenSetLau());
            dto.setMaMon(item.getIdSetLau().getMaSetLau());
            dto.setIdSetLau(item.getIdSetLau().getId());
        }

        String textHienThi = switch (item.getTrangThaiMon() != null ? item.getTrangThaiMon() : -1) {
            case 0 -> "Đã hủy";
            case 1 -> "Chưa lên";
            case 2 -> "Đã lên";
            default -> "Khác";
        };
        dto.setTrangThaiText(textHienThi);

        return dto;
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