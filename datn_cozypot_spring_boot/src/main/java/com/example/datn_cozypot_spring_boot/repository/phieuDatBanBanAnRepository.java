package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.PhieuDatBanBanAn;
import com.example.datn_cozypot_spring_boot.entity.PhieuDatBanBanAnId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface phieuDatBanBanAnRepository extends JpaRepository<PhieuDatBanBanAn, PhieuDatBanBanAnId> {
    @Query("SELECT SUM(p.soNguoiNgoi) FROM PhieuDatBanBanAn p WHERE p.banAn.id = :idBanAn AND p.phieuDatBan.trangThai = 3")
    Integer getTongSoNguoiDangNgoiTaiBan(@Param("idBanAn") Integer idBanAn);

    @Query("SELECT p FROM PhieuDatBanBanAn p WHERE p.banAn.id = :idBanAn AND p.phieuDatBan.trangThai = 3")
    List<PhieuDatBanBanAn> findActiveLinksByBanId(@Param("idBanAn") Integer idBanAn);
}
