package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

<<<<<<< HEAD
=======
import com.example.datn_cozypot_spring_boot.dto.danhMuc.DanhMucResponse;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
import com.example.datn_cozypot_spring_boot.entity.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

<<<<<<< HEAD
public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    @Query("SELECT m.maDanhMuc FROM DanhMuc m WHERE m.maDanhMuc LIKE :prefix% ORDER BY m.maDanhMuc DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);
=======
import java.util.List;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    @Query("SELECT m.maDanhMuc FROM DanhMuc m WHERE m.maDanhMuc LIKE :prefix% ORDER BY m.maDanhMuc DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<DanhMuc> findByTrangThai(int i);
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
}
