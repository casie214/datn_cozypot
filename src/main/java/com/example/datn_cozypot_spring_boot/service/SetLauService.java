package com.example.datn_cozypot_spring_boot.service;

import com.example.datn_cozypot_spring_boot.dto.SetLauDTO;
import com.example.datn_cozypot_spring_boot.entity.SetLau;
import com.example.datn_cozypot_spring_boot.repository.SetLauRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetLauService {
    @Autowired
    private SetLauRepository setLauRepo;

    public List<SetLauDTO> getActiveSets() {
        return setLauRepo.findAllByTrangThai(1) // Chỉ lấy các set đang kinh doanh
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private SetLauDTO convertToDto(SetLau entity) {
        SetLauDTO dto = new SetLauDTO();
        dto.setId(entity.getId());
        dto.setMaSetLau(entity.getMaSetLau());
        dto.setTenSetLau(entity.getTenSetLau());
        dto.setGiaBan(entity.getGiaBan());
        dto.setHinhAnh(entity.getHinhAnh());
        dto.setTrangThai(entity.getTrangThai());
        return dto;
    }
}