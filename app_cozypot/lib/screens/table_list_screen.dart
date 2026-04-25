import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../utils/api_config.dart';
import 'order_food_screen.dart';
import 'login_screen.dart';
import 'dart:async';

class TableListScreen extends StatefulWidget {
  const TableListScreen({super.key});

  @override
  State<TableListScreen> createState() => _TableListScreenState();
}

class _TableListScreenState extends State<TableListScreen> {
  bool isLoading = true;
  List<dynamic> allActiveTables = [];
  List<dynamic> displayTables = [];

  String searchQuery = "";
  String selectedFloor = "Tất cả";
  List<String> floors = ["Tất cả"];

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      fetchActiveTables();
    });
  }

  void startTimer() {
    Timer.periodic(const Duration(seconds: 5), (timer) {
      print("Function called at ${DateTime.now()}");
      fetchActiveTables();
    });
  }

  // 1. LẤY DANH SÁCH BÀN (Lọc trạng thái & Sắp xếp tầng)
  Future<void> fetchActiveTables() async {
    if (!mounted) return;
    setState(() => isLoading = true);
    try {
      final authHeader = await ApiConfig.getAuthHeader();
      final response = await http.get(
        Uri.parse('${ApiConfig.baseUrl}/guest/ban-an/active'),
        headers: authHeader,
      );

      if (response.statusCode == 200) {
        final dynamic decoded = jsonDecode(utf8.decode(response.bodyBytes));
        List<dynamic> data = (decoded is List)
            ? decoded
            : (decoded['data'] ?? []);

        if (mounted) {
          setState(() {
            // LỌC: Chỉ lấy các bàn đang có khách (trangThai == 1)
            allActiveTables = data
                .where((b) => b != null && b['trangThai'] == 1)
                .toList();

            // SẮP XẾP: Theo số tầng (soTang) tăng dần
            allActiveTables.sort((a, b) {
              int aFloor = a['soTang'] ?? 0;
              int bFloor = b['soTang'] ?? 0;
              return aFloor.compareTo(bFloor);
            });

            // TRÍCH XUẤT KHU VỰC: Lấy từ trường tenKhuVuc của bạn
            final Set<String> floorSet = {"Tất cả"};
            for (var ban in allActiveTables) {
              String khuVuc = ban['tenKhuVuc']?.toString() ?? "Khu vực khác";
              floorSet.add(khuVuc);
            }

            floors = floorSet.toList();
            _applyFilter(shouldSetState: false);
            isLoading = false;
          });
        }
      } else if (response.statusCode == 401) {
        _handleLogout();
      }
    } catch (e) {
      debugPrint("Lỗi TableList: $e");
      showError('Lỗi kết nối: $e');
      if (mounted) setState(() => isLoading = false);
    }
  }

  // 2. LOGIC LỌC TÌM KIẾM & KHU VỰC
  void _applyFilter({bool shouldSetState = true}) {
    final filtered = allActiveTables.where((ban) {
      // Tìm theo mã bàn
      final matchesSearch = (ban['maBan'] ?? '')
          .toString()
          .toLowerCase()
          .contains(searchQuery.toLowerCase());

      // Lọc theo tên khu vực
      final khuVucBan = ban['tenKhuVuc']?.toString() ?? "Khu vực khác";
      final matchesFloor =
          selectedFloor == "Tất cả" || khuVucBan == selectedFloor;

      return matchesSearch && matchesFloor;
    }).toList();

    if (shouldSetState) {
      setState(() => displayTables = filtered);
    } else {
      displayTables = filtered;
    }
  }

  void _filterTables() => _applyFilter(shouldSetState: true);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFFFDFCFB), Color(0xFFE2D1C3)],
          ),
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: [
                _buildTopHeader(),
                const SizedBox(height: 12),
                _buildFilterBar(),
                const SizedBox(height: 12),
                Expanded(
                  child: isLoading
                      ? const Center(
                          child: CircularProgressIndicator(
                            color: Color(0xFF7D161A),
                          ),
                        )
                      : _buildTableGrid(),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTopHeader() {
    return _buildGlassContainer(
      child: Container(
        height: 60,
        padding: const EdgeInsets.symmetric(horizontal: 15),
        child: Row(
          children: [
            const Icon(
              Icons.table_restaurant_outlined,
              color: Color(0xFF7D161A),
            ),
            const SizedBox(width: 15),
            const Text(
              "DANH SÁCH BÀN ĐANG PHỤC VỤ",
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: Colors.black87,
              ),
            ),
            const Spacer(),
            IconButton(
              onPressed: fetchActiveTables,
              icon: const Icon(Icons.refresh, color: Colors.blueGrey),
            ),
            IconButton(
              onPressed: _handleLogout,
              icon: const Icon(Icons.logout, color: Colors.redAccent),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFilterBar() {
    return _buildGlassContainer(
      child: Container(
        padding: const EdgeInsets.all(12),
        child: Column(
          children: [
            TextField(
              onChanged: (v) {
                searchQuery = v;
                _filterTables();
              },
              decoration: InputDecoration(
                hintText: "Nhập mã bàn (VD: BA001)...",
                prefixIcon: const Icon(Icons.search, color: Color(0xFF7D161A)),
                filled: true,
                fillColor: Colors.black.withOpacity(0.05),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(10),
                  borderSide: BorderSide.none,
                ),
                contentPadding: EdgeInsets.zero,
              ),
            ),
            const SizedBox(height: 10),
            SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: (floors).map((floor) {
                  bool isSelected = selectedFloor == floor;
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: ChoiceChip(
                      label: Text(floor),
                      selected: isSelected,
                      onSelected: (val) {
                        if (val) {
                          setState(() {
                            selectedFloor = floor;
                          });
                          _filterTables();
                        }
                      },
                      selectedColor: const Color(0xFF7D161A),
                      labelStyle: TextStyle(
                        color: isSelected ? Colors.white : Colors.black87,
                        fontWeight: isSelected
                            ? FontWeight.bold
                            : FontWeight.normal,
                      ),
                      backgroundColor: Colors.white,
                    ),
                  );
                }).toList(),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTableGrid() {
    if (displayTables.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.event_seat_outlined,
              size: 60,
              color: Colors.grey.withOpacity(0.5),
            ),
            const SizedBox(height: 10),
            Text(
              "Không có bàn nào đang hoạt động!",
              style: TextStyle(color: Colors.grey.shade600, fontSize: 16),
            ),
          ],
        ),
      );
    }
    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 5,
        childAspectRatio: 1.1,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
      ),
      itemCount: displayTables.length,
      itemBuilder: (context, index) {
        final ban = displayTables[index];
        return Card(
          elevation: 2,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(15),
          ),
          child: InkWell(
            onTap: () => handleTableClick(ban),
            borderRadius: BorderRadius.circular(15),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(
                  Icons.restaurant_menu,
                  color: Color(0xFF7D161A),
                  size: 30,
                ),
                const SizedBox(height: 8),
                Text(
                  ban['maBan'] ?? 'Bàn',
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 17,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  ban['tenKhuVuc'] ?? 'Sảnh chính',
                  style: const TextStyle(color: Colors.grey, fontSize: 11),
                ),
                const Text(
                  'ĐANG ĂN',
                  style: TextStyle(
                    color: Colors.green,
                    fontSize: 10,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildGlassContainer({required Widget child}) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.75),
        borderRadius: BorderRadius.circular(15),
        border: Border.all(color: Colors.white.withOpacity(0.4)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: child,
    );
  }

  void _handleLogout() => Navigator.pushReplacement(
    context,
    MaterialPageRoute(builder: (context) => const LoginScreen()),
  );

  void showError(String m) {
    if (!mounted) return;
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(SnackBar(content: Text(m), backgroundColor: Colors.red));
  }

  Future<void> handleTableClick(dynamic ban) async {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (ctx) =>
          const Center(child: CircularProgressIndicator(color: Colors.white)),
    );
    try {
      final authHeader = await ApiConfig.getAuthHeader();
      final res = await http.get(
        Uri.parse(
          '${ApiConfig.baseUrl}/hoa-don-thanh-toan/active-by-ban/${ban['id']}',
        ),
        headers: authHeader,
      );
      if (!mounted) return;
      Navigator.pop(context);
      if (res.statusCode == 200 && res.body.isNotEmpty) {
        final data = jsonDecode(utf8.decode(res.bodyBytes));
        final bill = (data is List) ? data[0] : data;
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => OrderFoodScreen(
              idBan: ban['id'],
              maBan: ban['maBan'] ?? 'Bàn',
              idHoaDon: bill['idHoaDon'] ?? bill['id'] ?? 0,
              idKhachHang: bill['idKhachHang'] ?? 2,
            ),
          ),
        );
      } else {
        showError('Bàn chưa được mở hóa đơn!');
      }
    } catch (e) {
      Navigator.pop(context);
      showError('Lỗi: $e');
    }
  }
}
