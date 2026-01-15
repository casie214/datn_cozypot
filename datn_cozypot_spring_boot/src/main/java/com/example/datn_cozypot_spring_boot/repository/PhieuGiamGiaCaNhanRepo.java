package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGiaCaNhan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PhieuGiamGiaCaNhanRepo extends JpaRepository<PhieuGiamGiaCaNhan, Integer> {
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE " +
            "(:tenKH IS NULL OR p.idKhachHang.tenKhachHang LIKE %:tenKH%) AND " +
            "(:status IS NULL OR p.trangThaiSuDung = :status)")
    List<PhieuGiamGiaCaNhan> search(String tenKH, Integer status);
}
