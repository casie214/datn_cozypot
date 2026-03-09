package com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository;

import com.example.datn_cozypot_spring_boot.entity.DanhMucChiTiet;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying
    @Transactional
    @Query(value = "UPDATE danh_muc_chi_tiet SET trang_thai = 0 " +
            "WHERE id_danh_muc = :danhMucId " +
            "AND trang_thai = 1 " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM chi_tiet_set_lau ctsl " +
            "   JOIN set_lau sl ON ctsl.id_set_lau = sl.id_set_lau " +
            "   WHERE ctsl.id_danh_muc_chi_tiet = danh_muc_chi_tiet.id_danh_muc_chi_tiet " +
            "   AND sl.trang_thai = 1" +
            ") " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM chi_tiet_hoa_don cthd " +
            "   JOIN hoa_don_thanh_toan hd ON cthd.id_hoa_don = hd.id_hoa_don " +
            "   WHERE cthd.id_danh_muc_chi_tiet = danh_muc_chi_tiet.id_danh_muc_chi_tiet " +
            "   AND hd.trang_thai_hoa_don IN (3, 4)" +
            ")", nativeQuery = true)
    int ngungKinhDoanhMonTheoDanhMuc(@Param("danhMucId") Integer danhMucId);

    @Query(value = "SELECT COUNT(*) FROM danh_muc_chi_tiet WHERE id_danh_muc = :danhMucId", nativeQuery = true)
    Integer countByDanhMucId(@Param("danhMucId") Integer danhMucId);

    boolean existsByTenMonAndDanhMucIdAndDinhLuongId(String tenMonChuan, Integer idDanhMuc, Integer idDinhLuong);
}
