package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.DonVi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonViRepository extends JpaRepository<DonVi, Integer> {
    boolean existsByTenDonVi(String tenDonVi);
    List<DonVi> findByListDanhMuc_Id(Integer danhMucId);
}
