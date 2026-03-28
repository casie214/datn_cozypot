package com.example.datn_cozypot_spring_boot.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;
    private final String cloudName = "df2jdczrw"; // Nhớ thay cái này

    public CloudinaryService() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", "797653333789121",
                "api_secret", "jjmh4lrr7opjrUqaKWsIXPDalh4"
        ));
    }

    // 1. Hàm tạo URL dựa trên Public ID (Không cần DB)
    public String getUrl(String publicId) {
        return String.format("https://res.cloudinary.com/%s/image/upload/%s.jpg", cloudName, publicId);
    }

    // 2. Hàm kiểm tra và upload
    public void uploadIfNotExist(String base64Data, String publicId) {
        if (base64Data == null || !base64Data.contains(",")) return;

        try {
            // Kiểm tra xem ảnh đã tồn tại trên Cloudinary chưa bằng Resource API
            // (Lưu ý: Cách này tốn 1 lượt request API, nhưng an toàn)
            cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
            System.out.println("  - " + publicId + " đã tồn tại, bỏ qua.");
        } catch (Exception e) {
            // Nếu báo lỗi (thường là 404), nghĩa là chưa có -> Upload ngay
            try {
                System.out.println("  🚀 Đang đẩy ảnh lên Cloudinary: " + publicId);
                String base64Image = base64Data.split(",")[1];
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                cloudinary.uploader().upload(imageBytes, ObjectUtils.asMap(
                        "public_id", publicId,
                        "overwrite", true
                ));
            } catch (Exception ex) {
                System.err.println("  ! Lỗi upload: " + ex.getMessage());
            }
        }
    }
}