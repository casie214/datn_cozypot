package com.example.datn_cozypot_spring_boot.dto.LichSuHoaDonDTO;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Builder
public class LichSuHoaDonResponse {
    private Integer idLog;
    private String hanhDong;
    private String thoiGian;
    private String nguoiThucHien;
    private String lyDo;
    private Integer trangThaiMoi;
    private String loaiHanhDong;
}
