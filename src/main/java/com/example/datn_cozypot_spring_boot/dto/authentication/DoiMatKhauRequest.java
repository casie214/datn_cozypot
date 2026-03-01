package com.example.datn_cozypot_spring_boot.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DoiMatKhauRequest {
    private String matKhauCu;
    private String matKhauMoi;
}
