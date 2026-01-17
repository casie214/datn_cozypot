package com.example.datn_cozypot_spring_boot.dto.danhMuc;

import com.example.datn_cozypot_spring_boot.entity.DanhMucChiTiet;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DanhMucResponse {
    private Integer id;
    private String maDanhMuc;
    private String tenDanhMuc;
    private String moTa;

    // Audit fields
    private Instant ngayTao;
    private Instant ngaySua;
    private String nguoiTao;
    private String nguoiSua;

    private Integer trangThai;
}

