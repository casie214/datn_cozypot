package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.MonAnDiKem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonAnDiKemRepository extends JpaRepository<MonAnDiKem, Integer> {
    List<MonAnDiKem> findAllByTrangThai(Integer trangThai);
}