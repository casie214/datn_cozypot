package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.MonAnDiKemDTO;
import com.example.datn_cozypot_spring_boot.entity.DanhMucChiTiet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonAnDiKemService {
    @Autowired
    private com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository monAnRepo;

    public List<MonAnDiKemDTO> getActiveMonAns() {
        return monAnRepo.findAllByTrangThai(1)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MonAnDiKemDTO convertToDto(DanhMucChiTiet entity) {
        MonAnDiKemDTO dto = new MonAnDiKemDTO();
        dto.setId(entity.getId());
        dto.setMaMonAn(entity.getMaMon());
        dto.setTenMonAn(entity.getTenMon());
        dto.setGiaBan(entity.getGiaBan());
        dto.setTrangThai(entity.getTrangThai());
        dto.setHinhAnh(entity.getHinhAnh());

        return dto;
    }
}
