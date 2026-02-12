package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.LoaiSetLau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface LoaiLauRepository extends JpaRepository<LoaiSetLau, Integer> {
    @Query("SELECT l.maLoaiSet FROM LoaiSetLau l WHERE l.maLoaiSet LIKE :prefix% ORDER BY l.maLoaiSet DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<LoaiSetLau> findByTrangThai(int i);
}
