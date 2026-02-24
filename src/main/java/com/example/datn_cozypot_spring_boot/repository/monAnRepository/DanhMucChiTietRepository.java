package com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository;

import com.example.datn_cozypot_spring_boot.entity.DanhMucChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DanhMucChiTietRepository extends JpaRepository<DanhMucChiTiet, Integer> {
    @Query("SELECT d.maMon FROM DanhMucChiTiet d WHERE d.maMon LIKE :prefix% ORDER BY d.maMon DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<DanhMucChiTiet> findByTrangThai(int i);

    List<DanhMucChiTiet> findAllByTrangThai(int i);

    boolean existsByMaMon(String finalCode);

    boolean existsByTenMonAndDanhMucIdAndDinhLuongId(String tenMonChuan, Integer idDanhMuc, Integer idDinhLuong);
}
