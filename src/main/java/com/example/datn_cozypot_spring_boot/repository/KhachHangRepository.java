package com.example.datn_cozypot_spring_boot.repository;

import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {

    @Query("SELECT kh FROM KhachHang kh WHERE " +
            "(:keyword IS NULL OR kh.maKhachHang LIKE %:keyword% " +
            "OR kh.tenKhachHang LIKE %:keyword% " +
            "OR kh.soDienThoai LIKE %:keyword% " +
            "OR kh.email LIKE %:keyword%) AND " +
            "(:trangThai IS NULL OR kh.trangThai = :trangThai) AND " +
            "(:tuNgay IS NULL OR CAST(kh.ngayTaoTaiKhoan AS date) >= :tuNgay)") // Sửa chỗ này
    Page<KhachHang> searchKhachHang(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") java.time.LocalDate tuNgay, // Đổi từ LocalDateTime sang LocalDate
            Pageable pageable);

    boolean existsBySoDienThoai(String soDienThoai);
    boolean existsByEmail(String email);
    boolean existsByTenDangNhap(String tenDangNhap);

    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByTenDangNhapAndIdNot(String tenDangNhap, Integer id);
    List<KhachHang> findByTrangThai(Integer trangThai);

    @Query(value = """
    SELECT 
        kh.id_khach_hang,
        kh.ma_khach_hang,
        kh.ten_khach_hang,
        kh.so_dien_thoai,
        kh.email,
        kh.ngay_sinh,

        COUNT(DISTINCT pdb_thang.id_phieu_dat_ban) AS so_lan_dat_trong_thang,

        ISNULL(SUM(
            CASE 
                WHEN hd.trang_thai_hoa_don = 2 
                THEN hd.tong_tien_thanh_toan 
                ELSE 0 
            END
        ), 0) AS tong_chi_tieu_trong_thang,

        MAX(pdb_all.thoi_gian_dat) AS lan_dat_gan_nhat

    FROM khach_hang kh

    LEFT JOIN phieu_dat_ban pdb_thang
        ON kh.id_khach_hang = pdb_thang.id_khach_hang
        AND MONTH(pdb_thang.thoi_gian_dat) = :thang
        AND YEAR(pdb_thang.thoi_gian_dat) = :nam

    LEFT JOIN phieu_dat_ban pdb_all
        ON kh.id_khach_hang = pdb_all.id_khach_hang

    LEFT JOIN hoa_don_thanh_toan hd
        ON kh.id_khach_hang = hd.id_khach_hang
        AND MONTH(hd.thoi_gian_thanh_toan) = :thang
        AND YEAR(hd.thoi_gian_thanh_toan) = :nam
    WHERE kh.trang_thai = 1   -- ⭐ CHỈ KH HOẠT ĐỘNG

    GROUP BY 
        kh.id_khach_hang,
        kh.ma_khach_hang,
        kh.ten_khach_hang,
        kh.so_dien_thoai,
        kh.email,
        kh.ngay_sinh
""", nativeQuery = true)
    List<Object[]> thongKeKhachHangTheoThang(
            @Param("thang") int thang,
            @Param("nam") int nam
    );



    Optional<KhachHang> findKhachHangByEmail(String identifier);
}
