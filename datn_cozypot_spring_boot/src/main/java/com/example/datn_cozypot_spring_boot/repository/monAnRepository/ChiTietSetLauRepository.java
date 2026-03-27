package com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietSetLau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChiTietSetLauRepository extends JpaRepository<ChiTietSetLau, Integer> {
    void deleteAllBySetLauId(Integer id);

    List<ChiTietSetLau> findBySetLau_Id(Integer id);
}
