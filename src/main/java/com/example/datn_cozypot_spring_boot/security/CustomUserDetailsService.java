package com.example.datn_cozypot_spring_boot.security;

import com.example.datn_cozypot_spring_boot.entity.KhachHang;
import com.example.datn_cozypot_spring_boot.entity.NhanVien;
import com.example.datn_cozypot_spring_boot.repository.KhachHangRepository;
import com.example.datn_cozypot_spring_boot.repository.NhanVienRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // tìm trong bảng nhân viên trước
        Optional<NhanVien> nhanVien = nhanVienRepository.findNhanVienByTenDangNhap(identifier);
        // thấy thì trả về ADMIN/EMPLOYEE
        if (nhanVien.isPresent()) {
            NhanVien nv = nhanVien.get();
            String roleName = nv.getIdVaiTro().getTenVaiTro();
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_" + roleName)
            );

            return new User(
                    nv.getTenDangNhap(),
                    nv.getMatKhauDangNhap(),
                    authorities
            );
        }

        // hoặc tìm trong bảng khách hàng
        Optional<KhachHang> khachHang = khachHangRepository.findKhachHangByEmail(identifier);
        // thấy thì trả UserDetails quyền USER
        if (khachHang.isPresent()) {
            KhachHang kh = khachHang.get();
            List<GrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            String password = (kh.getMatKhauDangNhap() == null) ? "" : kh.getMatKhauDangNhap();

            return new User(
                    (kh.getEmail() != null) ? kh.getEmail() : kh.getSoDienThoai(),
                    password,
                    authorities
            );
        }
        throw new UsernameNotFoundException("Không tìm thấy người dùng với thông tin: " + identifier);
    }
}
