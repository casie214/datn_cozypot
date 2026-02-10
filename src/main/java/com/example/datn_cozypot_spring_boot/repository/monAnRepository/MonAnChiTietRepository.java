package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.ChiTietMonAn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

<<<<<<< HEAD
=======
import java.util.Arrays;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
import java.util.List;

public interface MonAnChiTietRepository extends JpaRepository<ChiTietMonAn, Integer> {
    List<ChiTietMonAn> findByIdMonAnDiKem_Id(int id);

    @Query("SELECT c.maChiTietMonAn FROM ChiTietMonAn c WHERE c.maChiTietMonAn LIKE :prefix% ORDER BY c.maChiTietMonAn DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);
<<<<<<< HEAD
=======

    List<ChiTietMonAn> findByTrangThai(int i);
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
}
