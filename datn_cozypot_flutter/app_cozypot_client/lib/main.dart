import 'package:app_cozypot_client/screens/qrScannerScreen.dart';
import 'package:flutter/material.dart';

void main() {
  // Bắt buộc khởi tạo cho các plugin (Camera & WebView)
  WidgetsFlutterBinding.ensureInitialized();

  // Khách hàng không cần đăng nhập, bỏ qua bước lấy Token
  runApp(const CozyPotGuestApp());
}

class CozyPotGuestApp extends StatelessWidget {
  const CozyPotGuestApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'CozyPot - Đặt Món Tại Bàn',
      theme: ThemeData(
        // Giữ màu đỏ thương hiệu CozyPot
        primaryColor: const Color(0xFF7D161A),
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFF7D161A),
          primary: const Color(0xFF7D161A),
        ),
        useMaterial3: true,
        // Cấu hình Appbar chung cho toàn ứng dụng
        appBarTheme: const AppBarTheme(
          backgroundColor: Color(0xFF7D161A),
          foregroundColor: Colors.white,
          centerTitle: true,
        ),
      ),
      // App mở ra là vào thẳng màn hình Quét mã QR
      home: const QRScannerScreen(),
    );
  }
}
