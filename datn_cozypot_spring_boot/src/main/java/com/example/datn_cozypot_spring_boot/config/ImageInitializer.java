package com.example.datn_cozypot_spring_boot.config;

import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.DanhMucChiTietRepository;
import com.example.datn_cozypot_spring_boot.repository.DanhMucChiTietRepository.SetLauRepository;
import com.example.datn_cozypot_spring_boot.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ImageInitializer implements CommandLineRunner {

    private final SetLauRepository setLauRepository;
    private final DanhMucChiTietRepository danhMucChiTietRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 [START-UP] Đang kiểm tra và khởi tạo toàn bộ ảnh từ Database...");

        // 1. Quét và tạo ảnh cho Set Lẩu
        setLauRepository.findAll().forEach(s ->
                cloudinaryService.uploadIfNotExist(s.getHinhAnh(), "setlau_" + s.getId()));

        danhMucChiTietRepository.findAll().forEach(m ->
                cloudinaryService.uploadIfNotExist(m.getHinhAnh(), "monan_" + m.getId()));

        System.out.println("✅ [START-UP] Hoàn tất khởi tạo folder uploads!");
    }

    private void generateFile(String base64Data, String fileName) {
        if (base64Data == null || !base64Data.contains(",")) return;

        try {
            File uploadDir = new File("uploads");
            if (!uploadDir.exists()) uploadDir.mkdirs();

            File file = new File(uploadDir, fileName + ".jpg");
            if (!file.exists()) {
                String base64Image = base64Data.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Files.write(file.toPath(), imageBytes);
                System.out.println("  + Đã tạo ảnh: " + fileName + ".jpg");
            }
        } catch (Exception e) {
            System.err.println("  - Lỗi tạo ảnh " + fileName + ": " + e.getMessage());
        }
    }
}