package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.dto.response.BanAnResponse;
import com.example.datn_cozypot_spring_boot.entity.BanAn;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BanAnRepository extends JpaRepository<BanAn,Integer> {
    @Modifying
    @Transactional
    @Query("""
        UPDATE BanAn b
        SET b.trangThai = :trangThai
        WHERE b.id = :idBan
    """)
    void updateTrangThaiBan(
            @Param("idBan") Integer idBan,
            @Param("trangThai") Integer trangThai
    );


    List<BanAn> findByTrangThai(int i);

    @Query("SELECT b FROM BanAn b WHERE b.soNguoiToiDa >= :soNguoi")
    List<BanAn> findBanPhuHopChoDatBan(@Param("soNguoi") Integer soNguoi);
}