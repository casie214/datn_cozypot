package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.dto.monAn.MonAnResponse;
import com.example.datn_cozypot_spring_boot.entity.MonAnDiKem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonAnRepository extends JpaRepository<MonAnDiKem, Integer> {

    @Query(value = """
    SELECT TOP 1 ma_mon_an\s
            FROM mon_an_di_kem\s
            WHERE ma_mon_an LIKE CONCAT(:prefix, '%')\s
            ORDER BY ma_mon_an DESC
""", nativeQuery = true)
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<MonAnDiKem> findAllByTrangThai(Integer trangThai);


    List<MonAnDiKem> findByTrangThaiKinhDoanh(int i);
}
