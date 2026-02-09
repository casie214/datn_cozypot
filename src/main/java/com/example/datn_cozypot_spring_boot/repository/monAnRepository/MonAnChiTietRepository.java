package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietMonAn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;

public interface MonAnChiTietRepository extends JpaRepository<ChiTietMonAn, Integer> {
    List<ChiTietMonAn> findByIdMonAnDiKem_Id(int id);

    @Query("SELECT c.maChiTietMonAn FROM ChiTietMonAn c WHERE c.maChiTietMonAn LIKE :prefix% ORDER BY c.maChiTietMonAn DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<ChiTietMonAn> findByTrangThai(int i);
}
