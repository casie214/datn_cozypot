import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../utils/api_config.dart';
import 'order_food_screen.dart';
import 'login_screen.dart'; // Để quay về nếu token hết hạn

class TableListScreen extends StatefulWidget {
  const TableListScreen({super.key});

  @override
  State<TableListScreen> createState() => _TableListScreenState();
}

class _TableListScreenState extends State<TableListScreen> {
  bool isLoading = true;
  List<dynamic> activeTables = [];

  @override
  void initState() {
    super.initState();
    fetchActiveTables();
  }

  // 1. LẤY DANH SÁCH BÀN (Gắn Token + Fix lỗi Null)
  Future<void> fetchActiveTables() async {
    setState(() => isLoading = true);
    try {
      final authHeader = await ApiConfig.getAuthHeader();
      final response = await http.get(
        Uri.parse('${ApiConfig.baseUrl}/guest/ban-an/active'),
        headers: authHeader, // Thêm token để không bị 401
      );

      if (response.statusCode == 200) {
        List<dynamic> allTables = jsonDecode(utf8.decode(response.bodyBytes));
        setState(() {
          // CHỐT CHẶN LỖI NULL: Kiểm tra ban != null và trangThai != null
          activeTables = allTables.where((ban) {
            if (ban == null) return false;
            var status = ban['trangThai'];
            return status != null && status == 1;
          }).toList();
        });
      } else if (response.statusCode == 401) {
        _handleLogout(); // Token hết hạn, bắt đăng nhập lại
      } else {
        showError('Lỗi Server: ${response.statusCode}');
      }
    } catch (e) {
      showError('Lỗi kết nối: $e');
    } finally {
      setState(() => isLoading = false);
    }
  }

  // 2. XỬ LÝ CLICK BÀN (Gắn Token)
  Future<void> handleTableClick(dynamic ban) async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) =>
          const Center(child: CircularProgressIndicator(color: Colors.white)),
    );

    try {
      final authHeader = await ApiConfig.getAuthHeader();
      final response = await http.get(
        Uri.parse(
          '${ApiConfig.baseUrl}/hoa-don-thanh-toan/active-by-ban/${ban['id']}',
        ),
        headers: authHeader,
      );

      if (!mounted) return;
      Navigator.pop(context);

      if (response.statusCode == 200 && response.body.isNotEmpty) {
        var data = jsonDecode(utf8.decode(response.bodyBytes));

        // Nếu Server trả về mảng, lấy phần tử đầu
        var bill = (data is List) ? data[0] : data;

        int idHoaDon = bill['idHoaDon'] ?? bill['id'] ?? 0;
        int idKhachHang = bill['idKhachHang'] ?? 2;

        if (idHoaDon != 0) {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => OrderFoodScreen(
                idBan: ban['id'],
                maBan: ban['maBan'] ?? 'Bàn ${ban['id']}',
                idHoaDon: idHoaDon,
                idKhachHang: idKhachHang,
              ),
            ),
          );
        } else {
          showError('Dữ liệu hóa đơn không hợp lệ!');
        }
      } else {
        showError('Bàn này chưa được mở hóa đơn (Check-in)!');
      }
    } catch (e) {
      if (mounted) Navigator.pop(context);
      showError('Lỗi: $e');
    }
  }

  void _handleLogout() {
    Navigator.pushReplacement(
      context,
      MaterialPageRoute(builder: (context) => const LoginScreen()),
    );
  }

  void showError(String message) {
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: Colors.red),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text(
          'Chọn bàn thêm món',
          style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
        ),
        backgroundColor: const Color(0xFF7D161A),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: Colors.white),
            onPressed: fetchActiveTables,
          ),
        ],
      ),
      body: isLoading
          ? const Center(
              child: CircularProgressIndicator(color: Color(0xFF7D161A)),
            )
          : activeTables.isEmpty
          ? const Center(child: Text('Không có bàn nào đang phục vụ!'))
          : Padding(
              padding: const EdgeInsets.all(12),
              child: GridView.builder(
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  childAspectRatio: 1.3,
                  crossAxisSpacing: 10,
                  mainAxisSpacing: 10,
                ),
                itemCount: activeTables.length,
                itemBuilder: (context, index) {
                  var ban = activeTables[index];
                  return InkWell(
                    onTap: () => handleTableClick(ban),
                    child: Card(
                      color: Colors.white,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                        side: const BorderSide(color: Color(0xFF7D161A)),
                      ),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(
                            Icons.table_bar,
                            color: Color(0xFF7D161A),
                            size: 30,
                          ),
                          Text(
                            ban['maBan'] ?? 'Bàn',
                            style: const TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                          const Text(
                            'Có khách',
                            style: TextStyle(
                              color: Colors.orange,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
    );
  }
}
