import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../utils/api_config.dart';

class OrderFoodScreen extends StatefulWidget {
  final int idBan;
  final String maBan;
  final int idHoaDon;
  final String? maHoaDon;
  final int idKhachHang;

  const OrderFoodScreen({
    super.key,
    required this.idBan,
    required this.maBan,
    required this.idHoaDon,
    this.maHoaDon,
    required this.idKhachHang,
  });

  @override
  State<OrderFoodScreen> createState() => _OrderFoodScreenState();
}

class _OrderFoodScreenState extends State<OrderFoodScreen> {
  bool isLoading = true;
  bool isSubmitting = false;

  List<dynamic> allItems = [];
  List<dynamic> orderedItems = [];
  List<dynamic> categories = [];
  List<dynamic> hotpotTypes = [];

  String searchKey = "";
  int? selectedCategoryId;
  int? selectedHotpotTypeId;
  String selectedType = "Tất cả";

  Map<String, int> cart = {};

  // 🚨 THÊM BIẾN LƯU MÃ HÓA ĐƠN LẤY TỪ API
  String? currentMaHoaDon;

  @override
  void initState() {
    super.initState();
    currentMaHoaDon = widget.maHoaDon; // Lấy tạm mã từ widget nếu có
    fetchAllData();
  }

  String _getCartKey(dynamic item) {
    bool isSet = item['tenSetLau'] != null;
    return isSet ? 'set_${item['id']}' : 'food_${item['id']}';
  }

  Future<void> fetchAllData() async {
    try {
      final header = await ApiConfig.getAuthHeader();
      final res = await Future.wait([
        http.get(
          Uri.parse('${ApiConfig.baseUrl}/guest/category-detail/active'),
          headers: header,
        ),
        http.get(
          Uri.parse('${ApiConfig.baseUrl}/guest/hotpot/active'),
          headers: header,
        ),
        http.get(
          Uri.parse(
            '${ApiConfig.baseUrl}/hoa-don-thanh-toan/active-by-ban/${widget.idBan}',
          ),
          headers: header,
        ),
        http.get(
          Uri.parse('${ApiConfig.baseUrl}/guest/category/active'),
          headers: header,
        ),
        http.get(
          Uri.parse('${ApiConfig.baseUrl}/guest/hotpot-type/active'),
          headers: header,
        ),
      ]);

      if (!mounted) return;

      setState(() {
        allItems = [
          ...jsonDecode(utf8.decode(res[0].bodyBytes)),
          ...jsonDecode(utf8.decode(res[1].bodyBytes)),
        ];

        var billData = jsonDecode(utf8.decode(res[2].bodyBytes));

        // 🚀 LẤY MAHOADON TỪ API BACKEND TẠI ĐÂY
        if (billData is List && billData.isNotEmpty) {
          orderedItems = billData[0]['chiTiet'] ?? [];
          currentMaHoaDon = billData[0]['maHoaDon'] ?? currentMaHoaDon;
        } else if (billData is Map) {
          orderedItems = billData['chiTiet'] ?? [];
          currentMaHoaDon = billData['maHoaDon'] ?? currentMaHoaDon;
        }

        cart.clear();
        for (var ordered in orderedItems) {
          if (ordered['trangThaiMon'] == 0) continue;

          int qty = ordered['soLuong'] ?? 0;
          int? productId = ordered['id'];
          String itemType = ordered['type'] ?? '';

          if (productId != null) {
            String key = itemType == 'SET'
                ? 'set_$productId'
                : 'food_$productId';
            cart[key] = (cart[key] ?? 0) + qty;
          }
        }

        categories = jsonDecode(utf8.decode(res[3].bodyBytes));
        hotpotTypes = jsonDecode(utf8.decode(res[4].bodyBytes));
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      _showSnackBar('Lỗi tải dữ liệu: $e', Colors.red);
    }
  }

  Future<void> _confirmOrder() async {
    List<Map<String, dynamic>> payloadItems = [];

    cart.forEach((key, qty) {
      if (qty > 0) {
        bool isSet = key.startsWith('set_');
        int id = int.parse(key.split('_')[1]);

        payloadItems.add({
          "idChiTietMonAn": isSet ? null : id,
          "idSetLau": isSet ? id : null,
          "soLuong": qty,
          "ghiChu": "",
        });
      }
    });

    if (payloadItems.isEmpty) {
      _showSnackBar('Giỏ hàng trống! Hãy chọn ít nhất 1 món.', Colors.orange);
      return;
    }

    setState(() => isSubmitting = true);
    if (Navigator.canPop(context)) Navigator.pop(context);

    try {
      final header = await ApiConfig.getAuthHeader();
      final url = Uri.parse('${ApiConfig.baseUrl}/hoa-don-thanh-toan/tao-don');

      final body = jsonEncode({
        "idHoaDon": widget.idHoaDon > 0 ? widget.idHoaDon : null,
        "idBanAn": widget.idBan,
        "idNhanVien": null,
        "idKhachHang": widget.idKhachHang > 0 ? widget.idKhachHang : null,
        "chiTietHoaDon": payloadItems,
      });

      final response = await http.post(url, headers: header, body: body);

      if (response.statusCode == 200 || response.statusCode == 201) {
        _showSnackBar('Cập nhật món thành công!', Colors.green);
        setState(() => isLoading = true);
        fetchAllData();
      } else {
        _showSnackBar('Lỗi: ${response.statusCode}', Colors.red);
      }
    } catch (e) {
      _showSnackBar('Lỗi kết nối: $e', Colors.red);
    } finally {
      if (mounted) setState(() => isSubmitting = false);
    }
  }

  List<dynamic> get filteredItems {
    return allItems.where((item) {
      bool isSet = item['tenSetLau'] != null;

      String name = (item['tenMon'] ?? item['tenSetLau'] ?? "")
          .toString()
          .toLowerCase();
      bool matchesSearch = name.contains(searchKey.toLowerCase());

      bool matchesType =
          selectedType == "Tất cả" ||
          (selectedType == "Món lẻ" && !isSet) ||
          (selectedType == "Set lẩu" && isSet);

      bool matchesCategory = true;
      if (selectedCategoryId != null) {
        matchesCategory = !isSet && item['idDanhMuc'] == selectedCategoryId;
      }

      bool matchesHotpotType = true;
      if (selectedHotpotTypeId != null) {
        matchesHotpotType = isSet && item['idLoaiSet'] == selectedHotpotTypeId;
      }

      return matchesSearch &&
          matchesType &&
          matchesCategory &&
          matchesHotpotType;
    }).toList();
  }

  String formatPrice(dynamic price) =>
      '${price.toString().replaceAllMapped(RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'), (m) => '${m[1]}.')}đ';

  int get tempTotal {
    int total = 0;
    for (var item in allItems) {
      String key = _getCartKey(item);
      if ((cart[key] ?? 0) > 0) {
        total += (item['giaBan'] as int) * cart[key]!;
      }
    }
    return total;
  }

  Widget _buildImage(String? imageUrl) {
    if (imageUrl == null || imageUrl.isEmpty) {
      return const Center(
        child: Icon(Icons.fastfood, color: Colors.grey, size: 40),
      );
    }

    // 1. Xử lý ảnh dạng chuỗi Base64
    if (imageUrl.startsWith('data:image')) {
      try {
        // Cắt bỏ phần "data:image/jpeg;base64," để lấy chuỗi mã gốc ở phía sau dấu phẩy
        final base64String = imageUrl.split(',').last;

        return Image.memory(
          base64Decode(base64String), // Giải mã chuỗi thành hình ảnh
          fit: BoxFit.cover,
          width: double.infinity,
          height: double.infinity,
          errorBuilder: (context, error, stackTrace) => const Center(
            child: Icon(Icons.broken_image, color: Colors.grey, size: 40),
          ),
        );
      } catch (e) {
        return const Center(
          child: Icon(Icons.broken_image, color: Colors.grey, size: 40),
        );
      }
    }

    // 2. Xử lý ảnh dạng Link HTTP bình thường (Dự phòng nếu sau này BE đổi cách lưu)
    String fullUrl = imageUrl.startsWith('http')
        ? imageUrl
        : '${ApiConfig.baseUrl.replaceAll('/api', '')}/$imageUrl';

    return Image.network(
      fullUrl,
      fit: BoxFit.cover,
      width: double.infinity,
      height: double.infinity,
      errorBuilder: (context, error, stackTrace) => const Center(
        child: Icon(Icons.broken_image, color: Colors.grey, size: 40),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    // 🚀 DÙNG BIẾN currentMaHoaDon THAY VÌ widget.maHoaDon
    String titleText = 'Bàn ${widget.maBan}';
    if (currentMaHoaDon != null && currentMaHoaDon!.isNotEmpty) {
      titleText += ' - HĐ: $currentMaHoaDon';
    }

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: const Color(0xFF7D161A),
        title: Text(
          titleText,
          style: const TextStyle(
            color: Colors.white,
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: Stack(
        children: [
          if (isLoading)
            const Center(child: CircularProgressIndicator())
          else
            Column(
              children: [
                _buildFilterHeader(),
                Expanded(
                  child: GridView.builder(
                    padding: const EdgeInsets.all(10),
                    gridDelegate:
                        const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 2,
                          childAspectRatio: 0.72,
                          crossAxisSpacing: 10,
                          mainAxisSpacing: 10,
                        ),
                    itemCount: filteredItems.length,
                    itemBuilder: (context, index) =>
                        _buildFoodCard(filteredItems[index]),
                  ),
                ),
                if (tempTotal > 0) _buildBottomSummary(),
              ],
            ),
          if (isSubmitting)
            Container(
              color: Colors.black45,
              child: const Center(
                child: CircularProgressIndicator(color: Colors.white),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildFilterHeader() {
    return Container(
      padding: const EdgeInsets.all(10),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 5),
        ],
      ),
      child: Column(
        children: [
          TextField(
            onChanged: (v) => setState(() => searchKey = v),
            decoration: InputDecoration(
              hintText: 'Tìm món...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(8),
              ),
              contentPadding: EdgeInsets.zero,
            ),
          ),
          const SizedBox(height: 8),

          Row(
            children: [
              Expanded(
                child: _buildDropdown(
                  "Loại",
                  ["Tất cả", "Món lẻ", "Set lẩu"],
                  selectedType,
                  (v) {
                    setState(() {
                      selectedType = v!;
                      selectedCategoryId = null;
                      selectedHotpotTypeId = null;
                    });
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),

          Row(
            children: [
              if (selectedType == "Tất cả" || selectedType == "Món lẻ")
                Expanded(
                  child: _buildDropdownID(
                    "Danh mục món lẻ",
                    categories,
                    selectedCategoryId,
                    (v) {
                      setState(() {
                        selectedCategoryId = v;
                        if (v != null) selectedHotpotTypeId = null;
                        if (v != null) selectedType = "Món lẻ";
                      });
                    },
                  ),
                ),
              if (selectedType == "Tất cả") const SizedBox(width: 8),
              if (selectedType == "Tất cả" || selectedType == "Set lẩu")
                Expanded(
                  child: _buildDropdownID(
                    "Loại set lẩu",
                    hotpotTypes,
                    selectedHotpotTypeId,
                    (v) {
                      setState(() {
                        selectedHotpotTypeId = v;
                        if (v != null) selectedCategoryId = null;
                        if (v != null) selectedType = "Set lẩu";
                      });
                    },
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildDropdown(
    String hint,
    List<String> items,
    String current,
    Function(String?) onChanged,
  ) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10),
      decoration: BoxDecoration(
        border: Border.all(color: Colors.grey.shade300),
        borderRadius: BorderRadius.circular(5),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: current,
          isExpanded: true,
          style: const TextStyle(fontSize: 12, color: Colors.black),
          items: items
              .map((e) => DropdownMenuItem(value: e, child: Text(e)))
              .toList(),
          onChanged: onChanged,
        ),
      ),
    );
  }

  Widget _buildDropdownID(
    String hint,
    List<dynamic> items,
    int? current,
    Function(int?) onChanged,
  ) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10),
      decoration: BoxDecoration(
        border: Border.all(color: Colors.grey.shade300),
        borderRadius: BorderRadius.circular(5),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<int>(
          hint: Text(
            hint,
            style: const TextStyle(fontSize: 12, color: Colors.black87),
          ),
          value: current,
          isExpanded: true,
          items: [
            DropdownMenuItem<int>(
              value: null,
              child: Text(
                "➤ $hint",
                style: const TextStyle(color: Colors.grey),
              ),
            ),
            ...items.map(
              (e) => DropdownMenuItem<int>(
                value: e['id'],
                child: Text(e['tenDanhMuc'] ?? e['tenLoaiSet'] ?? ""),
              ),
            ),
          ],
          onChanged: onChanged,
        ),
      ),
    );
  }

  Widget _buildFoodCard(dynamic item) {
    String key = _getCartKey(item);
    int qty = cart[key] ?? 0;

    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade200),
      ),
      child: Column(
        children: [
          Expanded(
            child: Stack(
              children: [
                Container(
                  width: double.infinity,
                  color: Colors.grey[100],
                  child: _buildImage(item['hinhAnh']),
                ),
                if (qty > 0)
                  Container(
                    color: Colors.black45,
                    child: Center(
                      child: CircleAvatar(
                        backgroundColor: Colors.white,
                        child: Text(
                          '$qty',
                          style: const TextStyle(
                            color: Color(0xFF7D161A),
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(8),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  item['tenMon'] ?? item['tenSetLau'] ?? 'Món không tên',
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 13,
                  ),
                ),
                Text(
                  formatPrice(item['giaBan']),
                  style: const TextStyle(
                    color: Colors.red,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 5),
                qty == 0
                    ? SizedBox(
                        width: double.infinity,
                        child: OutlinedButton(
                          onPressed: () => setState(() => cart[key] = 1),
                          child: const Text('+ Chọn'),
                        ),
                      )
                    : Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          IconButton(
                            icon: const Icon(Icons.remove_circle_outline),
                            onPressed: () =>
                                setState(() => cart[key] = qty - 1),
                          ),
                          Text('$qty'),
                          IconButton(
                            icon: const Icon(
                              Icons.add_circle,
                              color: Color(0xFF7D161A),
                            ),
                            onPressed: () =>
                                setState(() => cart[key] = qty + 1),
                          ),
                        ],
                      ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBottomSummary() {
    int totalItems = cart.values.where((v) => v > 0).length;
    return Container(
      padding: const EdgeInsets.all(15),
      color: const Color(0xFF7D161A),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                formatPrice(tempTotal),
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              Text(
                'Tổng cộng: $totalItems loại món',
                style: const TextStyle(color: Colors.white70, fontSize: 11),
              ),
            ],
          ),
          ElevatedButton(
            onPressed: _showOrderPreview,
            style: ElevatedButton.styleFrom(backgroundColor: Colors.white),
            child: const Row(
              children: [
                Text('Xem đơn', style: TextStyle(color: Color(0xFF7D161A))),
                Icon(Icons.keyboard_arrow_up, color: Color(0xFF7D161A)),
              ],
            ),
          ),
        ],
      ),
    );
  }

  void _showOrderPreview() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.7,
        maxChildSize: 0.95,
        builder: (_, controller) => Container(
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
          ),
          child: Column(
            children: [
              const SizedBox(height: 10),
              // 🚀 CẬP NHẬT TITLE BẢNG XÁC NHẬN BẰNG BIẾN MỚI
              Text(
                'Danh sách gửi bếp (HĐ: ${currentMaHoaDon ?? 'Mới'})',
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const Divider(),
              Expanded(
                child: ListView(
                  controller: controller,
                  padding: const EdgeInsets.all(15),
                  children: [
                    ...allItems
                        .where((i) => (cart[_getCartKey(i)] ?? 0) > 0)
                        .map((item) {
                          String key = _getCartKey(item);
                          return ListTile(
                            contentPadding: EdgeInsets.zero,
                            title: Text(item['tenMon'] ?? item['tenSetLau']),
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.remove_circle_outline),
                                  onPressed: () => setState(
                                    () => cart[key] = cart[key]! - 1,
                                  ),
                                ),
                                Text('${cart[key]}'),
                                IconButton(
                                  icon: const Icon(
                                    Icons.add_circle,
                                    color: Color(0xFF7D161A),
                                  ),
                                  onPressed: () => setState(
                                    () => cart[key] = cart[key]! + 1,
                                  ),
                                ),
                              ],
                            ),
                          );
                        }),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(20),
                child: SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: const Color(0xFF7D161A),
                    ),
                    onPressed: _confirmOrder,
                    // 🚀 CẬP NHẬT TRÊN NÚT BẤM BẰNG BIẾN MỚI
                    child: Text(
                      'XÁC NHẬN GỬI BẾP - ${currentMaHoaDon ?? 'Mới'}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showSnackBar(String m, Color c) => ScaffoldMessenger.of(
    context,
  ).showSnackBar(SnackBar(content: Text(m), backgroundColor: c));
}
