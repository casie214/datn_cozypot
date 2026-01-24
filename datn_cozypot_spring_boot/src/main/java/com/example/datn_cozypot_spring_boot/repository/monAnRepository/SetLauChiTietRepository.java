package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietSetLau;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetLauChiTietRepository extends JpaRepository<ChiTietSetLau, Integer> {
    List<ChiTietSetLau> findBySetLau_Id(Integer idSetLau);

    void deleteBySetLau_Id(Integer idSetLau);
}
