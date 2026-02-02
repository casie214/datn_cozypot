package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietMonAn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonAnChiTietRepository extends JpaRepository<ChiTietMonAn, Integer> {
    List<ChiTietMonAn> findByIdMonAnDiKem_Id(int id);
}
