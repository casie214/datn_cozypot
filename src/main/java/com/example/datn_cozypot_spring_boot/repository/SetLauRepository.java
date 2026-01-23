package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.SetLau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetLauRepository extends JpaRepository<SetLau, Integer> {
    // Lấy các set lẩu đang hoạt động (trangThai = 1)
    List<SetLau> findAllByTrangThai(Integer trangThai);
}
