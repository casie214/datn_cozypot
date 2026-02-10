package com.example.datn_cozypot_spring_boot.repository.monAnRepository;

import com.example.datn_cozypot_spring_boot.entity.DanhMucChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

<<<<<<< HEAD
public interface DanhMucChiTietRepository extends JpaRepository<DanhMucChiTiet, Integer> {
    @Query("SELECT d.maDanhMucChiTiet FROM DanhMucChiTiet d WHERE d.maDanhMucChiTiet LIKE :prefix% ORDER BY d.maDanhMucChiTiet DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);
=======
import java.util.Collection;
import java.util.List;

public interface DanhMucChiTietRepository extends JpaRepository<DanhMucChiTiet, Integer> {
    @Query("SELECT d.maDanhMucChiTiet FROM DanhMucChiTiet d WHERE d.maDanhMucChiTiet LIKE :prefix% ORDER BY d.maDanhMucChiTiet DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<DanhMucChiTiet> findByTrangThai(int i);
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
}
