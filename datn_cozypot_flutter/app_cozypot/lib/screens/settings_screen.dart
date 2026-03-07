import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../utils/api_config.dart';
import 'table_list_screen.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  final TextEditingController _ipController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _loadCurrentIP();
  }

  // Lấy IP đang lưu hiện tại để hiển thị lên ô nhập
  Future<void> _loadCurrentIP() async {
    final prefs = await SharedPreferences.getInstance();
    String? savedIP = prefs.getString('server_ip');
    if (savedIP != null) {
      _ipController.text = savedIP;
    }
  }

  // Lưu IP và chuyển sang màn hình Danh sách bàn
  Future<void> _saveAndContinue() async {
    String ip = _ipController.text.trim();
    if (ip.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Vui lòng nhập IP máy chủ!'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    await ApiConfig.saveIP(ip); // Lưu vào bộ nhớ

    if (!mounted) return;
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (context) => const TableListScreen()),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text(
          'Cài đặt Máy Chủ',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFF7D161A),
      ),
      body: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.wifi, size: 80, color: Color(0xFF7D161A)),
            const SizedBox(height: 20),
            const Text(
              'Kết nối App với Spring Boot',
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 10),
            const Text(
              'Mở CMD trên máy tính chạy Backend, gõ "ipconfig" và nhập địa chỉ IPv4 vào đây:',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.grey),
            ),
            const SizedBox(height: 30),
            TextField(
              controller: _ipController,
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
              decoration: InputDecoration(
                labelText: 'Địa chỉ IPv4 (VD: 192.168.1.15)',
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10),
                ),
                prefixIcon: const Icon(
                  Icons.computer,
                  color: Color(0xFF7D161A),
                ),
              ),
            ),
            const SizedBox(height: 20),
            SizedBox(
              width: double.infinity,
              height: 50,
              child: ElevatedButton(
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF7D161A),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10),
                  ),
                ),
                onPressed: _saveAndContinue,
                child: const Text(
                  'LƯU & KẾT NỐI',
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
