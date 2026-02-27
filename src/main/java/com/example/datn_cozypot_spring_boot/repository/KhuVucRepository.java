package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.KhuVuc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhuVucRepository extends JpaRepository<KhuVuc,Integer> {
//    Optional<KhuVuc> findByTangAndTenKhuVuc(Integer tang, String tenKhuVuc);

    Optional<KhuVuc> findByTenKhuVucAndTang(String tenKhuVuc, Integer tang);
    List<KhuVuc> findByTrangThai(Integer trangThai);

    List<KhuVuc> findByTangAndTrangThai(Integer tang, Integer trangThai);

    boolean existsByTangAndTenKhuVucAndTrangThai(
            Integer tang,
            String tenKhuVuc,
            Integer trangThai
    );
}
