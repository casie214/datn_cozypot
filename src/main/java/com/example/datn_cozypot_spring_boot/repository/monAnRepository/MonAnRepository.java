package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.MonAnDiKem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonAnRepository extends JpaRepository<MonAnDiKem, Integer> {
//    @Query("SELECT m.maMonAn FROM MonAnDiKem m WHERE m.maMonAn LIKE :prefix% ORDER BY m.maMonAn DESC LIMIT 1")
//    String findMaxCodeByPrefix(@Param("prefix") String prefix);
    @Query(value = """
    SELECT ma_mon_an 
    FROM mon_an_di_kem 
    WHERE ma_mon_an LIKE CONCAT(:prefix, '%') 
    ORDER BY ma_mon_an DESC 
    LIMIT 1
""", nativeQuery = true)
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<MonAnDiKem> findAllByTrangThai(Integer trangThai);

}
