import 'package:flutter/material.dart';
import 'utils/api_config.dart';
import 'screens/login_screen.dart';
import 'screens/main_navigation_screen.dart';

void main() async {
  // Bắt buộc khởi tạo cho các plugin hệ thống
  WidgetsFlutterBinding.ensureInitialized();

  // Lấy token trực tiếp từ bộ nhớ
  String? token = await ApiConfig.getToken();

  // Chốt chặn: Ép kiểu về bool tuyệt đối (không bao giờ null)
  bool isLoggedIn = (token != null && token.isNotEmpty);

  runApp(MyApp(hasToken: isLoggedIn));
}

class MyApp extends StatelessWidget {
  final bool hasToken;
  const MyApp({super.key, required this.hasToken});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'CozyPot POS',
      theme: ThemeData(
        primaryColor: const Color(0xFF7D161A),
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF7D161A)),
        useMaterial3: true,
      ),
      // Nếu đã có token thì vào thẳng MainNavigation, ngược lại vào Login
      home: hasToken ? const MainNavigationScreen() : const LoginScreen(),
    );
  }
}
