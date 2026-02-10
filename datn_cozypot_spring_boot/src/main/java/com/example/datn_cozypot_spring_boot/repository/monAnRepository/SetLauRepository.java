package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.SetLau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

<<<<<<< HEAD
=======
import java.util.Collection;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
import java.util.List;

public interface SetLauRepository extends JpaRepository<SetLau, Integer> {
    @Query("SELECT s.maSetLau FROM SetLau s WHERE s.maSetLau LIKE :prefix% ORDER BY s.maSetLau DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<SetLau> findAllByTrangThai(Integer trangThai);
<<<<<<< HEAD
=======

    List<SetLau> findByTrangThai(int i);
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
}
