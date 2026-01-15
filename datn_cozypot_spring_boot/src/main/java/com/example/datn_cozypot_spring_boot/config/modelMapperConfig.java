package com.example.datn_cozypot_spring_boot.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class modelMapperConfig {
    @Bean
    ModelMapper modelMapper(){
        return new ModelMapper();
    }
}
