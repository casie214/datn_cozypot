package com.example.datn_cozypot_spring_boot.dto.Payment;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PaymentRestDTO implements Serializable {

    private String status;
    private String message;
    private String URL;
}
