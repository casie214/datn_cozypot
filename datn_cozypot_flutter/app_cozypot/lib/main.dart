import 'package:flutter/material.dart';
import 'package:flutter/services.dart'; // Thư viện bắt buộc để điều khiển hướng màn hình
import 'package:device_preview/device_preview.dart';
import 'utils/api_config.dart';
import 'screens/login_screen.dart';
import 'screens/main_navigation_screen.dart';

void main() async {
  // 1. Bắt buộc gọi dòng này trước khi sử dụng SystemChrome
  WidgetsFlutterBinding.ensureInitialized();

  // 2. Ép toàn bộ ứng dụng chạy ở chế độ nằm ngang (Landscape)
  // Ngay khi khởi chạy, lệnh này sẽ khóa hướng màn hình
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.landscapeLeft,
    DeviceOrientation.landscapeRight,
  ]);

  // Lấy token để kiểm tra đăng nhập
  String? token = await ApiConfig.getToken();
  bool isLoggedIn = (token != null && token.isNotEmpty);

  // 3. Khởi chạy app với DevicePreview
  runApp(
    DevicePreview(
      enabled: true, // Bật giả lập thiết bị
      defaultDevice:
          Devices.ios.iPad12InchesGen4, // Tùy chọn: Mặc định là iPad Air
      builder: (context) => MyApp(hasToken: isLoggedIn),
    ),
  );
}

class MyApp extends StatelessWidget {
  final bool hasToken;
  const MyApp({super.key, required this.hasToken});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      // Các thuộc tính quan trọng để DevicePreview hoạt động chính xác
      useInheritedMediaQuery: true,
      locale: DevicePreview.locale(context),
      builder: DevicePreview.appBuilder,

      debugShowCheckedModeBanner: false,
      title: 'CozyPot POS',
      theme: ThemeData(
        primaryColor: const Color(0xFF7D161A),
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF7D161A)),
        useMaterial3: true,
      ),
      // Điều hướng dựa trên trạng thái đăng nhập
      home: hasToken ? const MainNavigationScreen() : const LoginScreen(),
    );
  }
}
