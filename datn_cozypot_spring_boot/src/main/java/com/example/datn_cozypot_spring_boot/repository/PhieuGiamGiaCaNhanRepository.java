package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.PhieuGiamGiaCaNhan;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaCaNhanRepository extends JpaRepository<PhieuGiamGiaCaNhan, Integer> {

    @Modifying
    @Query("DELETE FROM PhieuGiamGiaCaNhan p WHERE p.phieuGiamGia.id = :idPhieu")
    void deleteByPhieuGiamGiaId(Integer id);

    @Query("SELECT pcn FROM PhieuGiamGiaCaNhan pcn " +
            "JOIN pcn.khachHang kh " +
            "JOIN pcn.phieuGiamGia pg " +
            "WHERE (:keyword IS NULL OR " +
            "LOWER(kh.tenKhachHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(pg.codeGiamGia) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<PhieuGiamGiaCaNhan> search(@Param("keyword") String keyword, Pageable pageable);

    List<PhieuGiamGiaCaNhan> findByPhieuGiamGiaId(Integer phieuGiamGiaId);

    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE p.khachHang.id = :idKhach AND p.phieuGiamGia.id = :idPhieu")
    Optional<PhieuGiamGiaCaNhan> findByKhachAndPhieu(Integer idKhach, Integer idPhieu);

    // Lấy ra danh sách các phiếu đang bị khóa bởi 1 hóa đơn
    @Query("SELECT p FROM PhieuGiamGiaCaNhan p WHERE p.idHoaDonThanhToan.id = :idHoaDon")
    List<PhieuGiamGiaCaNhan> findAllByHoaDon(Integer idHoaDon);
}
