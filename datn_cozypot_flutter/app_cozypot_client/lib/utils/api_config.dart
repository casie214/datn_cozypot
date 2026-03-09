import 'package:shared_preferences/shared_preferences.dart';

class ApiConfig {
  // Gắn cứng domain ngrok (nhớ dùng https và thêm /api ở đuôi)
  static String baseUrl =
      'https://unrheumatic-gametically-yajaira.ngrok-free.dev/api';

  // --- PHẦN QUẢN LÝ JWT ---

  // Lưu Token vào máy (Giữ lại phòng trường hợp sau này làm tính năng Khách Hàng Thành Viên)
  static Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('jwt_token', token);
  }

  // Lấy Token ra để dùng
  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('jwt_token');
  }

  // 🚨 HÀM TẠO HEADER ĐÃ ĐƯỢC ĐỘ LẠI CHO KHÁCH HÀNG
  static Future<Map<String, String>> getAuthHeader() async {
    String? token = await getToken();

    // Khởi tạo Header mặc định
    Map<String, String> headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
      // 🚀 BẮT BUỘC CÓ: Bypass trang cảnh báo HTML của ngrok free
      'ngrok-skip-browser-warning': 'true',
    };

    // Chỉ đính kèm thẻ bài thông hành nếu khách thực sự có token
    if (token != null && token.isNotEmpty) {
      headers['Authorization'] = 'Bearer $token';
    }

    return headers;
  }

  // Đăng xuất (xóa token)
  static Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('jwt_token');
  }
}
