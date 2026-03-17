package com.example.datn_cozypot_spring_boot.dto.response;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class DatBanRequest {
    @NotNull(message = "Vui lòng nhập số người")
    @Min(value = 1, message = "Số người ít nhất phải là 1")
    private Integer soNguoi;

    @NotNull(message = "Vui lòng chọn ngày đặt")
    @FutureOrPresent(message = "Ngày đặt không được ở trong quá khứ")
    private LocalDate ngayDat;

    @NotNull(message = "Vui lòng chọn giờ đặt")
    private LocalTime gioDat;

    private Integer idPhieu;
}
