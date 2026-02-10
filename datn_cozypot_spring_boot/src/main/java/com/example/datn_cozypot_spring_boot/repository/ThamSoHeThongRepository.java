package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.ThamSoHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThamSoHeThongRepository extends JpaRepository<ThamSoHeThong, Integer> {
    Optional<ThamSoHeThong> findByMaThamSo(String maThamSo);
}
