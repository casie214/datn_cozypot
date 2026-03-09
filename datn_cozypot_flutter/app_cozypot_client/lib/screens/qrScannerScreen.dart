import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';
import 'dart:convert';
import 'package:http/http.dart' as http;
import '../utils/api_config.dart';
import 'order_food_screen.dart';

class QRScannerScreen extends StatefulWidget {
  const QRScannerScreen({super.key});

  @override
  State<QRScannerScreen> createState() => _QRScannerScreenState();
}

class _QRScannerScreenState extends State<QRScannerScreen> {
  bool isProcessing = false;

  // 🚨 1. KHỞI TẠO CONTROLLER RIÊNG ĐỂ QUẢN LÝ CAMERA
  final MobileScannerController cameraController = MobileScannerController(
    detectionSpeed:
        DetectionSpeed.noDuplicates, // Chống quét trùng lặp liên tục
    facing: CameraFacing.back,
  );

  // 🚨 2. BẮT BUỘC PHẢI TẮT CAMERA KHI RỜI KHỎI MÀN HÌNH NÀY
  @override
  void dispose() {
    cameraController.dispose();
    super.dispose();
  }

  Future<void> _handleQRScan(String code) async {
    if (isProcessing) return;
    setState(() => isProcessing = true);

    try {
      // Tạm dừng camera khi đang xử lý API để tránh quét lại lần 2
      cameraController.stop();

      Uri uri = Uri.parse(code);
      String? idBanStr = uri.queryParameters['ban'];

      if (idBanStr != null) {
        int idBan = int.parse(idBanStr);

        final response = await http.get(
          Uri.parse('${ApiConfig.baseUrl}/guest/active-by-ban/$idBan'),
        );

        if (response.statusCode == 200) {
          final data = jsonDecode(utf8.decode(response.bodyBytes));
          var bill = (data is List && data.isNotEmpty) ? data[0] : data;

          if (mounted) {
            // Chuyển sang màn hình đặt món
            Navigator.pushReplacement(
              context,
              MaterialPageRoute(
                builder: (context) => OrderFoodScreen(
                  idBan: idBan,
                  maBan: bill['maBan'] ?? idBanStr,
                  idHoaDon: bill['id'] ?? 0,
                  maHoaDon: bill['maHoaDon'],
                  idKhachHang: bill['idKhachHang'] ?? 0,
                ),
              ),
            );
          }
        } else {
          _showError("Bàn này chưa có hóa đơn nào đang hoạt động.");
          // Bật lại camera nếu quét lỗi để khách quét lại
          cameraController.start();
        }
      } else {
        _showError("Mã QR không hợp lệ!");
        cameraController.start();
      }
    } catch (e) {
      _showError("Lỗi quét mã: $e");
      cameraController.start();
    } finally {
      if (mounted) setState(() => isProcessing = false);
    }
  }

  void _showError(String msg) {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(msg), backgroundColor: Colors.red));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Quét QR Tại Bàn')),
      body: Stack(
        children: [
          MobileScanner(
            // 🚨 3. GẮN CONTROLLER VÀO SCANNER
            controller: cameraController,
            errorBuilder: (context, error, child) {
              return Center(
                child: Text(
                  'Lỗi Camera: ${error.errorDetails?.message}',
                  style: const TextStyle(color: Colors.red),
                ),
              );
            },
            onDetect: (capture) {
              final barcode = capture.barcodes.first;
              if (barcode.rawValue != null) {
                _handleQRScan(barcode.rawValue!);
              }
            },
          ),
          if (isProcessing)
            const Center(child: CircularProgressIndicator(color: Colors.white)),
        ],
      ),
    );
  }
}
