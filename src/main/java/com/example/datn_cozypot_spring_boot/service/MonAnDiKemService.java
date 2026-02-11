package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.MonAnDiKemDTO;
import com.example.datn_cozypot_spring_boot.entity.MonAnDiKem;
import com.example.datn_cozypot_spring_boot.repository.monAnRepository.MonAnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MonAnDiKemService {
    @Autowired
    private MonAnRepository monAnRepo;

    public List<MonAnDiKemDTO> getActiveMonAns() {
        return monAnRepo.findAllByTrangThai(1)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private MonAnDiKemDTO convertToDto(MonAnDiKem entity) {
        MonAnDiKemDTO dto = new MonAnDiKemDTO();
        dto.setId(entity.getId());
        dto.setMaMonAn(entity.getMaMonAn());
        dto.setTenMonAn(entity.getTenMonAn());
        dto.setGiaBan(entity.getGiaBan());
        dto.setTrangThai(entity.getTrangThai());
        return dto;
    }
}
