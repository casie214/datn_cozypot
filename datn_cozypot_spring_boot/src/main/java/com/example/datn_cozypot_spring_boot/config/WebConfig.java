package com.example.datn_cozypot_spring_boot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Lấy đường dẫn thư mục project hiện tại
        String userDir = System.getProperty("user.dir");

        // Đảm bảo đường dẫn có dạng: file:F:/Things/aa/.../datn_cozypot_spring_boot/uploads/
        // Tao dùng "file:" + đường dẫn tuyệt đối để Windows nó không bị nhầm lẫn
        String uploadPath = "file:" + userDir + "/uploads/";

        System.out.println("🔥 [DEBUG] Spring đang tìm ảnh tại: " + uploadPath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}

