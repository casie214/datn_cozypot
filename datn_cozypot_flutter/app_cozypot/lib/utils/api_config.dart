import 'package:shared_preferences/shared_preferences.dart';

class ApiConfig {
  // Gắn cứng domain ngrok (nhớ dùng https và thêm /api ở đuôi)
  static String baseUrl =
      'https://unrheumatic-gametically-yajaira.ngrok-free.dev/api';

  // --- PHẦN QUẢN LÝ JWT ---

  // Lưu Token vào máy
  static Future<void> saveToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('jwt_token', token);
  }

  // Lấy Token ra để dùng
  static Future<String?> getToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString('jwt_token');
  }

  // Hàm tạo Header có chứa Token (Dùng cái này cho mọi request sau này)
  static Future<Map<String, String>> getAuthHeader() async {
    String? token = await getToken();
    return {
      'Content-Type': 'application/json; charset=UTF-8',
      'Authorization': 'Bearer $token',
      // DÒNG QUAN TRỌNG: Ép Ngrok bỏ qua trang HTML cảnh báo
      'ngrok-skip-browser-warning': 'true',
      'Accept': 'application/json',
    };
  }

  // Đăng xuất (xóa token)
  static Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('jwt_token');
  }
}
