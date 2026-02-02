package com.example.datn_cozypot_spring_boot.exceptionHandler;

public class ApiException extends RuntimeException {
    private final String code;
    public ApiException(String message, String code) {
        super(message);
        this.code = code;
    }
}
