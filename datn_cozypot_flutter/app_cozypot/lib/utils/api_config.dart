import 'package:shared_preferences/shared_preferences.dart';

class ApiConfig {
  static String baseUrl = 'http://localhost:8080/api';

  static Future<void> loadSavedIP() async {
    final prefs = await SharedPreferences.getInstance();
    String? savedIP = prefs.getString('server_ip');
    if (savedIP != null && savedIP.isNotEmpty) {
      baseUrl = 'http://localhost:8080/api';
    }
  }

  static Future<void> saveIP(String ip) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('server_ip', ip);
    baseUrl = 'http://$ip:8080/api';
  }

  // --- THÊM PHẦN QUẢN LÝ JWT VÀO ĐÂY ---

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
      'Content-Type': 'application/json',
      'Authorization': 'Bearer $token', // Thẻ bài thông hành đây!
    };
  }

  static Future<void> logout() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('jwt_token');
  }
}
