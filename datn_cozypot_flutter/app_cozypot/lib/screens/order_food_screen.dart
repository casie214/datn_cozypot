import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../utils/api_config.dart';

class OrderFoodScreen extends StatefulWidget {
  final int idBan;
  final String maBan;
  final int idHoaDon;
  final int idKhachHang;

  const OrderFoodScreen({
    super.key,
    required this.idBan,
    required this.maBan,
    required this.idHoaDon,
    required this.idKhachHang,
  });

  @override
  State<OrderFoodScreen> createState() => _OrderFoodScreenState();
}

class _OrderFoodScreenState extends State<OrderFoodScreen> {
  bool isLoading = true;
  bool isSubmitting = false;

  List<dynamic> allItems = [];
  List<dynamic> orderedItems = []; // Danh sách món đã có trong phiếu
  List<dynamic> categories = [];
  List<dynamic> hotpotTypes = [];

  String searchKey = "";
  int? selectedCategoryId;
  int? selectedHotpotTypeId;
  String selectedType = "Tất cả";

  Map<int, int> cart = {};

  @override
  void initState() {
    super.initState();
    fetchAllData();
  }

  // 1. Tải toàn bộ dữ liệu (Khớp chính xác với JSON bạn gửi)
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

        // 🚨 SỬA TẠI ĐÂY: Khớp với JSON "chiTiet"
        var billData = jsonDecode(utf8.decode(res[2].bodyBytes));
        if (billData is List && billData.isNotEmpty) {
          orderedItems = billData[0]['chiTiet'] ?? [];
        } else {
          orderedItems = billData['chiTiet'] ?? [];
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

  // Logic lọc dữ liệu
  List<dynamic> get filteredItems {
    return allItems.where((item) {
      bool isSet = item['tenSetLau'] != null;
      bool matchesSearch = (item['tenMon'] ?? item['tenSetLau'])
          .toString()
          .toLowerCase()
          .contains(searchKey.toLowerCase());
      bool matchesType =
          selectedType == "Tất cả" ||
          (selectedType == "Món lẻ" && !isSet) ||
          (selectedType == "Set lẩu" && isSet);
      bool matchesCategory =
          selectedCategoryId == null || item['idDanhMuc'] == selectedCategoryId;
      bool matchesHotpotType =
          selectedHotpotTypeId == null ||
          item['idLoaiSet'] == selectedHotpotTypeId;
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
      if ((cart[item['id']] ?? 0) > 0)
        total += (item['giaBan'] as int) * cart[item['id']]!;
    }
    return total;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: const Color(0xFF7D161A),
        title: Text(
          'Gọi món - ${widget.maBan}',
          style: const TextStyle(color: Colors.white, fontSize: 18),
        ),
        iconTheme: const IconThemeData(color: Colors.white),
      ),
      body: isLoading
          ? const Center(child: CircularProgressIndicator())
          : Column(
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
                if (tempTotal > 0 || orderedItems.isNotEmpty)
                  _buildBottomSummary(),
              ],
            ),
    );
  }

  // --- UI COMPONENTS ---
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
                  (v) => setState(() {
                    selectedType = v!;
                    selectedCategoryId = null;
                    selectedHotpotTypeId = null;
                  }),
                ),
              ),
              const SizedBox(width: 8),
              if (selectedType != "Set lẩu")
                Expanded(
                  child: _buildDropdownID(
                    "Danh mục",
                    categories,
                    selectedCategoryId,
                    (v) => setState(() => selectedCategoryId = v),
                  ),
                ),
              if (selectedType == "Set lẩu")
                Expanded(
                  child: _buildDropdownID(
                    "Loại Set",
                    hotpotTypes,
                    selectedHotpotTypeId,
                    (v) => setState(() => selectedHotpotTypeId = v),
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
          hint: Text(hint, style: const TextStyle(fontSize: 12)),
          value: current,
          isExpanded: true,
          items: [
            const DropdownMenuItem<int>(value: null, child: Text("Tất cả")),
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
    int qty = cart[item['id']] ?? 0;
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
                  child: const Icon(Icons.fastfood, color: Colors.grey),
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
                  item['tenMon'] ?? item['tenSetLau'],
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
                          onPressed: () => setState(() => cart[item['id']] = 1),
                          child: const Text('+ Chọn'),
                        ),
                      )
                    : Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          IconButton(
                            icon: const Icon(Icons.remove_circle_outline),
                            onPressed: () =>
                                setState(() => cart[item['id']] = qty - 1),
                          ),
                          Text('$qty'),
                          IconButton(
                            icon: const Icon(
                              Icons.add_circle,
                              color: Color(0xFF7D161A),
                            ),
                            onPressed: () =>
                                setState(() => cart[item['id']] = qty + 1),
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
                'Đã gọi: ${orderedItems.length} món',
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

  // --- BOTTOM SHEET XEM ĐƠN ---
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
              const Text(
                'Chi tiết đơn hàng',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const Divider(),
              Expanded(
                child: ListView(
                  controller: controller,
                  padding: const EdgeInsets.all(15),
                  children: [
                    if (orderedItems.isNotEmpty) ...[
                      const Row(
                        children: [
                          Icon(Icons.history, color: Colors.grey, size: 16),
                          SizedBox(width: 5),
                          Text(
                            'MÓN ĐÃ ĐẶT',
                            style: TextStyle(
                              color: Colors.grey,
                              fontWeight: FontWeight.bold,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 10),
                      // 🚨 SỬA TẠI ĐÂY: Hiển thị đúng key tenMon và soLuong
                      ...orderedItems.map(
                        (item) => ListTile(
                          contentPadding: EdgeInsets.zero,
                          title: Text(item['tenMon'] ?? 'Món không tên'),
                          trailing: Text(
                            'x${item['soLuong']}',
                            style: const TextStyle(fontWeight: FontWeight.bold),
                          ),
                        ),
                      ),
                      const Divider(height: 30),
                    ],
                    const Row(
                      children: [
                        Icon(
                          Icons.add_shopping_cart,
                          color: Color(0xFF7D161A),
                          size: 16,
                        ),
                        SizedBox(width: 5),
                        Text(
                          'MÓN ĐANG CHỌN',
                          style: TextStyle(
                            color: Color(0xFF7D161A),
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    ...allItems
                        .where((i) => (cart[i['id']] ?? 0) > 0)
                        .map(
                          (item) => ListTile(
                            contentPadding: EdgeInsets.zero,
                            title: Text(item['tenMon'] ?? item['tenSetLau']),
                            trailing: Row(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                IconButton(
                                  icon: const Icon(Icons.remove_circle_outline),
                                  onPressed: () => setState(
                                    () => cart[item['id']] =
                                        cart[item['id']]! - 1,
                                  ),
                                ),
                                Text('${cart[item['id']]}'),
                                IconButton(
                                  icon: const Icon(
                                    Icons.add_circle,
                                    color: Color(0xFF7D161A),
                                  ),
                                  onPressed: () => setState(
                                    () => cart[item['id']] =
                                        cart[item['id']]! + 1,
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
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
                    onPressed: () {
                      /* Gọi hàm confirmOrder của bạn tại đây */
                    },
                    child: const Text(
                      'XÁC NHẬN GỬI MÓN',
                      style: TextStyle(
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
