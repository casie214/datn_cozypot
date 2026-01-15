package com.example.datn_cozypot_spring_boot.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data

public class PhieuGiamGiaDTO {
    private Integer id;

    // Dùng để hiển thị – không cần nhập
    private String maPhieuGiamGia;

    @NotNull(message = "Phải chọn đợt khuyến mãi")
    private Integer idDotKhuyenMai;

    @NotBlank(message = "Code giảm giá không được để trống")
    @Size(max = 50, message = "Code giảm giá tối đa 50 ký tự")
    private String codeGiamGia;

    @NotBlank(message = "Tên phiếu không được để trống")
    @Size(min = 3, max = 200, message = "Tên phiếu phải từ 3 đến 200 ký tự")
    private String tenPhieuGiamGia;

    @NotNull(message = "Vui lòng chọn loại giảm giá")
    @Min(value = 1) @Max(value = 2)
    private Integer loaiGiamGia;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTriGiam;

    private BigDecimal giaTriGiamToiDa;

    @NotNull(message = "Đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn hàng tối thiểu phải từ 0 trở lên")
    private BigDecimal donHangToiThieu;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu không được là ngày trong quá khứ")
    private Instant ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private Instant ngayKetThuc;

    @NotNull(message = "Số lượng phát hành không được để trống")
    @Min(value = 1, message = "Số lượng phát hành phải ít nhất là 1")
    private Integer soLuongPhatHanh;

    private Integer soLuongDaDung;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private Integer trangThai;

    // Các trường hệ thống – thường chỉ để trả về
    private Instant ngayTao;
    private Instant ngaySua;

    private String nguoiTao;
    private String nguoiSua;
}
