package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse;
import com.example.datn_cozypot_spring_boot.entity.ChiTietSetLau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SetLauChiTietRepository extends JpaRepository<ChiTietSetLau, Integer> {
    @Query("SELECT new com.example.datn_cozypot_spring_boot.dto.ChiTietHoaDonDTO.ChiTietSetLauResponse(" +
            " ct.chiTietMonAn.tenChiTietMonAn, " +
            " ct.chiTietMonAn.donVi, " +
            " ct.soLuong) " +
            "FROM ChiTietSetLau ct " +
            "WHERE ct.setLau.id = :idSetLau")
    List<ChiTietSetLauResponse> findChiTietBySetId(@Param("idSetLau") Integer idSetLau);
}
