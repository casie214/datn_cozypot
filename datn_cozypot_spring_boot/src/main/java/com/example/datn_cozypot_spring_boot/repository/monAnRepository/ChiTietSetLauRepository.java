package com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietSetLau;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChiTietSetLauRepository extends JpaRepository<ChiTietSetLau, Integer> {
    void deleteAllBySetLauId(Integer id);
}
