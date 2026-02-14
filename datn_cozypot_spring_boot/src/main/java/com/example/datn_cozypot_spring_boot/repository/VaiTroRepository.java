package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.VaiTro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaiTroRepository extends JpaRepository<VaiTro, Integer> {
    // Tìm kiếm vai trò theo tên hoặc mã
    @Query("SELECT v FROM VaiTro v WHERE " +
            "(:keyword IS NULL OR v.tenVaiTro LIKE %:keyword% OR v.maVaiTro LIKE %:keyword%)")
    Page<VaiTro> searchVaiTro(String keyword, Pageable pageable);

    // Lấy danh sách vai trò đang hoạt động để hiển thị lên ComboBox/Select ở màn hình Nhân Viên
    List<VaiTro> findByTrangThai(Integer trangThai);
    Optional<VaiTro> findByTenVaiTro(String tenVaiTro);
}