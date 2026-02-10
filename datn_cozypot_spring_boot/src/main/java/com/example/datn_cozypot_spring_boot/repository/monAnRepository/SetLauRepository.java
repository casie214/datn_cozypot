package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.SetLau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetLauRepository extends JpaRepository<SetLau, Integer> {
    @Query("SELECT s.maSetLau FROM SetLau s WHERE s.maSetLau LIKE :prefix% ORDER BY s.maSetLau DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<SetLau> findAllByTrangThai(Integer trangThai);
}
