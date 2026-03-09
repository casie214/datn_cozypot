package com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository;

import com.example.datn_cozypot_spring_boot.entity.SetLau;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SetLauRepository extends JpaRepository<SetLau, Integer> {
    @Query("SELECT s.maSetLau FROM SetLau s WHERE s.maSetLau LIKE :prefix% ORDER BY s.maSetLau DESC LIMIT 1")
    String findMaxCodeByPrefix(@Param("prefix") String prefix);

    List<SetLau> findAllByTrangThai(Integer trangThai);

    List<SetLau> findByTrangThai(int i);
    @Query(value = "SELECT COUNT(*) FROM set_lau WHERE id_loai_set = :idLoaiSet", nativeQuery = true)
    Integer countByIdLoaiSet(@Param("idLoaiSet") Integer idLoaiSet);

    @Modifying
    @Transactional
    @Query(value = "UPDATE set_lau SET trang_thai = 0 " +
            "WHERE id_loai_set = :idLoaiSet " +
            "AND trang_thai = 1 " + // Chỉ xét các set đang hoạt động
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM chi_tiet_hoa_don cthd " +
            "   JOIN hoa_don_thanh_toan hd ON cthd.id_hoa_don = hd.id_hoa_don " +
            "   WHERE cthd.id_set_lau = set_lau.id_set_lau " +
            "   AND hd.trang_thai_hoa_don IN (3, 4)" + // Loại trừ set lẩu khách đang ăn
            ")", nativeQuery = true)
    int ngungKinhDoanhSetLauTheoLoai(@Param("idLoaiSet") Integer idLoaiSet);
}
