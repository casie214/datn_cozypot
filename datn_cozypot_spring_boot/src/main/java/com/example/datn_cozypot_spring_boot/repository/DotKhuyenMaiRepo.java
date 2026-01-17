package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.DotKhuyenMai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DotKhuyenMaiRepo extends JpaRepository<DotKhuyenMai, Integer> {
    @Query("SELECT d FROM DotKhuyenMai d WHERE " +
            "(:ten IS NULL OR d.tenDotKhuyenMai LIKE %:ten% OR d.maDotKhuyenMai LIKE %:ten%) AND " +
            "(:trangThai IS NULL OR d.trangThai = :trangThai) AND " +
            "(:loaiKM IS NULL OR d.loaiKhuyenMai = :loaiKM)")
    Page<DotKhuyenMai> searchFilter(@Param("ten") String ten,
                                    @Param("trangThai") Integer trangThai,
                                    @Param("loaiKM") Integer loaiKM,
                                    Pageable pageable);
    List<DotKhuyenMai> findByTrangThaiOrderByNgayTaoDesc(Integer trangThai);

    // Nếu bạn vẫn muốn giữ tên hàm findAllActive, hãy sửa lại thế này:
    @Query("SELECT d FROM DotKhuyenMai d WHERE d.trangThai = 1 ORDER BY d.ngayTao DESC")
    List<DotKhuyenMai> findAllActive();
}
