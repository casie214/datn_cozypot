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
  List<dynamic> categories = [];
  Map<String, int> cart = {};
  String? currentMaHoaDon;

  @override
  void initState() {
    super.initState();
    currentMaHoaDon = widget.maHoaDon;
    fetchAllData();
  }

  // =========================================================
  // LOGIC XỬ LÝ DỮ LIỆU
  // =========================================================

  String _normalizeName(String name) {
    return name.replaceAll(RegExp(r'\s*\(.*?\)'), '').trim();
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
      ]);

      if (!mounted) return;

      setState(() {
        allItems = [
          ...jsonDecode(utf8.decode(res[0].bodyBytes)),
          ...jsonDecode(utf8.decode(res[1].bodyBytes)),
        ];
        _syncCartFromBill(jsonDecode(utf8.decode(res[2].bodyBytes)));
        categories = jsonDecode(utf8.decode(res[3].bodyBytes));
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
      _showSnackBar('Lỗi tải dữ liệu: $e', Colors.red);
    }
  }

  void _syncCartFromBill(dynamic billData) {
    List<dynamic> items = [];
    if (billData is List && billData.isNotEmpty) {
      items = billData[0]['chiTiet'] ?? [];
      currentMaHoaDon = billData[0]['maHoaDon'] ?? currentMaHoaDon;
    } else if (billData is Map) {
      items = billData['chiTiet'] ?? [];
      currentMaHoaDon = billData['maHoaDon'] ?? currentMaHoaDon;
    }
    cart.clear();
    for (var item in items) {
      if (item['trangThaiMon'] == 0) continue;
      String key = item['type'] == 'SET'
          ? 'set_${item['id']}'
          : 'food_${item['id']}';
      cart[key] = (cart[key] ?? 0) + (item['soLuong'] as int);
    }
  }

  List<dynamic> get hotpotItems =>
      allItems.where((i) => i['tenSetLau'] != null).toList();

  Map<int, Map<String, List<dynamic>>> get groupedFoodItems {
    Map<int, Map<String, List<dynamic>>> result = {};
    for (var f in allItems.where((i) => i['tenSetLau'] == null)) {
      int catId = f['idDanhMuc'];
      String norm = _normalizeName(f['tenMon']);
      if (!result.containsKey(catId)) result[catId] = {};
      if (!result[catId]!.containsKey(norm)) result[catId]![norm] = [];
      result[catId]![norm]!.add(f);
    }
    return result;
  }

  int get tempTotal {
    int total = 0;
    for (var item in allItems) {
      String key = _getCartKey(item);
      if ((cart[key] ?? 0) > 0) total += (item['giaBan'] as int) * cart[key]!;
    }
    return total;
  }

  // =========================================================
  // GIAO DIỆN CHÍNH (BUILD)
  // =========================================================

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
          child: isLoading
              ? const Center(
                  child: CircularProgressIndicator(color: Color(0xFF7D161A)),
                )
              : Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: [
                      _buildTopHeader(),
                      const SizedBox(height: 12),
                      Expanded(
                        child: Row(
                          children: [
                            // KHỐI TRÁI: THỰC ĐƠN
                            Expanded(
                              flex: 7,
                              child: _buildGlassContainer(
                                child: Column(
                                  children: [
                                    _buildSearchHeader(), // THANH TÌM KIẾM DÀI - GỢI Ý NHỎ
                                    Expanded(
                                      child: SingleChildScrollView(
                                        padding: const EdgeInsets.all(18),
                                        child: Column(
                                          children: [
                                            _buildSectionTitle(
                                              "SET LẨU CHỌN SẴN",
                                            ),
                                            _buildGrid(
                                              hotpotItems,
                                              isSet: true,
                                            ),
                                            const SizedBox(height: 30),
                                            ...categories.map((cat) {
                                              final items =
                                                  groupedFoodItems[cat['id']] ??
                                                  {};
                                              if (items.isEmpty)
                                                return const SizedBox.shrink();
                                              return Column(
                                                children: [
                                                  _buildSectionTitle(
                                                    cat['tenDanhMuc']
                                                        .toString()
                                                        .toUpperCase(),
                                                  ),
                                                  _buildGroupedGrid(items),
                                                  const SizedBox(height: 20),
                                                ],
                                              );
                                            }).toList(),
                                          ],
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            ),
                            const SizedBox(width: 12),
                            // KHỐI PHẢI: GIỎ HÀNG
                            Expanded(
                              flex: 3,
                              child: _buildGlassContainer(
                                color: const Color(0xEEFFFFFF),
                                child: _buildRightSidebar(),
                              ),
                            ),
                          ],
                        ),
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
        padding: const EdgeInsets.symmetric(horizontal: 10),
        child: Row(
          children: [
            IconButton(
              icon: const Icon(
                Icons.arrow_back_ios_new,
                color: Color(0xFF7D161A),
                size: 22,
              ),
              onPressed: () => Navigator.pop(context),
            ),
            const VerticalDivider(width: 25, indent: 15, endIndent: 15),
            Expanded(
              child: Text(
                "Thực đơn - Bàn ${widget.maBan}",
                style: const TextStyle(
                  fontSize: 19,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
            ),
            if (currentMaHoaDon != null)
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 10,
                  vertical: 5,
                ),
                decoration: BoxDecoration(
                  color: const Color(0xFF7D161A).withOpacity(0.1),
                  borderRadius: BorderRadius.circular(15),
                ),
                child: Text(
                  "HĐ: $currentMaHoaDon",
                  style: const TextStyle(
                    fontSize: 13,
                    color: Color(0xFF7D161A),
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            const SizedBox(width: 10),
            IconButton(
              onPressed: fetchAllData,
              icon: const Icon(Icons.refresh, color: Colors.blueGrey, size: 24),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildGlassContainer({
    required Widget child,
    Color color = const Color(0xCCFFFFFF),
  }) {
    return Container(
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(15),
        border: Border.all(color: Colors.white.withOpacity(0.5)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: ClipRRect(borderRadius: BorderRadius.circular(15), child: child),
    );
  }

  // =========================================================
  // THANH TÌM KIẾM KIỂU GOOGLE (DÀI, GỢI Ý NHỎ CÓ ẢNH)
  // =========================================================

  Widget _buildSearchHeader() {
    return Container(
      width: double.infinity, // ĐỂ THANH SEARCH DÀI HẾT CONTAINER
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        border: Border(bottom: BorderSide(color: Colors.grey.withOpacity(0.1))),
      ),
      child: SearchAnchor(
        // Cấu hình bảng gợi ý nhỏ lại (dropdown)
        viewConstraints: const BoxConstraints(maxHeight: 400, maxWidth: 500),
        builder: (context, controller) => SearchBar(
          controller: controller,
          hintText: "Tìm món ăn hoặc set lẩu...",
          leading: const Icon(Icons.search, color: Color(0xFF7D161A), size: 22),
          onTap: () => controller.openView(),
          onChanged: (_) => controller.openView(),
          elevation: WidgetStateProperty.all(0),
          backgroundColor: WidgetStateProperty.all(
            Colors.black.withOpacity(0.04),
          ),
          // Cho phép thanh SearchBar co giãn theo container cha
          constraints: const BoxConstraints(
            maxHeight: 45,
            minWidth: double.infinity,
          ),
        ),
        suggestionsBuilder: (context, controller) {
          final query = controller.text.toLowerCase();
          final results = allItems.where(
            (i) => (i['tenMon'] ?? i['tenSetLau'])
                .toString()
                .toLowerCase()
                .contains(query),
          );
          Map<String, List<dynamic>> groups = {};
          for (var r in results) {
            String norm = _normalizeName(r['tenMon'] ?? r['tenSetLau']);
            if (!groups.containsKey(norm)) groups[norm] = [];
            groups[norm]!.add(r);
          }
          return groups.keys.map((name) {
            final group = groups[name]!;
            return ListTile(
              leading: Container(
                width: 40,
                height: 40,
                clipBehavior: Clip.antiAlias,
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(5),
                ),
                child: _buildImage(group.first['hinhAnh']),
              ),
              title: Text(
                name,
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                ),
              ),
              subtitle: Text(
                group.length > 1
                    ? "Nhiều kích cỡ"
                    : formatPrice(group.first['giaBan']),
                style: const TextStyle(fontSize: 12),
              ),
              onTap: () {
                controller.closeView(name);
                _handleSelection(name, group);
              },
            );
          }).toList();
        },
      ),
    );
  }

  // =========================================================
  // GRID HIỂN THỊ (2 CỘT)
  // =========================================================

  Widget _buildGrid(List<dynamic> items, {required bool isSet}) {
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        childAspectRatio: 3.0,
        crossAxisSpacing: 15,
        mainAxisSpacing: 15,
      ),
      itemCount: items.length,
      itemBuilder: (context, index) {
        final item = items[index];
        return _buildSleekItemCard(
          name: item['tenSetLau'] ?? item['tenMon'],
          price: item['giaBan'],
          image: item['hinhAnh'],
          isVariant: false,
          variantList: [item],
          onTap: () => isSet
              ? _showSetDetailModal(item)
              : _handleSelection(item['tenMon'], [item]),
        );
      },
    );
  }

  Widget _buildGroupedGrid(Map<String, List<dynamic>> groupedItems) {
    final names = groupedItems.keys.toList();
    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        childAspectRatio: 3.0,
        crossAxisSpacing: 15,
        mainAxisSpacing: 15,
      ),
      itemCount: names.length,
      itemBuilder: (context, index) {
        final name = names[index];
        final group = groupedItems[name]!;
        return _buildSleekItemCard(
          name: name,
          price: group.first['giaBan'],
          image: group.first['hinhAnh'],
          isVariant: group.length > 1,
          variantList: group,
          onTap: () => _handleSelection(name, group),
        );
      },
    );
  }

  Widget _buildSleekItemCard({
    required String name,
    required int price,
    String? image,
    required bool isVariant,
    required VoidCallback onTap,
    required List<dynamic> variantList,
  }) {
    int totalQty = 0;
    for (var v in variantList) {
      totalQty += (cart[_getCartKey(v)] ?? 0);
    }

    return Card(
      elevation: 1,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: totalQty > 0 ? const Color(0xFF7D161A) : Colors.grey.shade100,
          width: totalQty > 0 ? 1.5 : 1,
        ),
      ),
      clipBehavior: Clip.antiAlias,
      child: InkWell(
        onTap: onTap,
        child: Row(
          children: [
            Stack(
              children: [
                Container(
                  width: 100,
                  height: double.infinity,
                  color: Colors.grey[50],
                  child: _buildImage(image),
                ),
                if (totalQty > 0)
                  Positioned.fill(
                    child: Container(
                      color: Colors.black.withOpacity(0.3),
                      child: Center(
                        child: Container(
                          padding: const EdgeInsets.all(8),
                          decoration: const BoxDecoration(
                            color: Colors.white,
                            shape: BoxShape.circle,
                          ),
                          child: Text(
                            '$totalQty',
                            style: const TextStyle(
                              color: Color(0xFF7D161A),
                              fontWeight: FontWeight.bold,
                              fontSize: 18,
                            ),
                          ),
                        ),
                      ),
                    ),
                  ),
              ],
            ),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      name,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 15,
                        color: totalQty > 0
                            ? const Color(0xFF7D161A)
                            : Colors.black87,
                      ),
                    ),
                    const SizedBox(height: 5),
                    isVariant
                        ? const Text(
                            "Nhiều lựa chọn",
                            style: TextStyle(
                              color: Colors.blue,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                            ),
                          )
                        : Text(
                            formatPrice(price),
                            style: const TextStyle(
                              color: Color(0xFF7D161A),
                              fontWeight: FontWeight.bold,
                              fontSize: 15,
                            ),
                          ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  // =========================================================
  // GIỎ HÀNG
  // =========================================================

  Widget _buildRightSidebar() {
    final selected = allItems
        .where((i) => (cart[_getCartKey(i)] ?? 0) > 0)
        .toList();
    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(18),
          width: double.infinity,
          decoration: const BoxDecoration(
            color: Color(0xFF7D161A),
            borderRadius: BorderRadius.vertical(top: Radius.circular(15)),
          ),
          child: const Row(
            children: [
              Icon(Icons.shopping_bag_outlined, color: Colors.white, size: 20),
              SizedBox(width: 8),
              Text(
                "MÓN ĐÃ CHỌN",
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.bold,
                  color: Colors.white,
                ),
              ),
            ],
          ),
        ),
        Expanded(
          child: Container(
            color: const Color(0xFFFFF9F9),
            child: selected.isEmpty
                ? const Center(
                    child: Text(
                      "Giỏ hàng đang trống",
                      style: TextStyle(color: Colors.grey),
                    ),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.all(10),
                    itemCount: selected.length,
                    itemBuilder: (context, index) {
                      final item = selected[index];
                      final key = _getCartKey(item);
                      return Card(
                        elevation: 0.3,
                        margin: const EdgeInsets.only(bottom: 8),
                        child: ListTile(
                          dense: true,
                          title: Text(
                            item['tenMon'] ?? item['tenSetLau'],
                            style: const TextStyle(fontWeight: FontWeight.bold),
                          ),
                          subtitle: Text(
                            formatPrice(item['giaBan'] * cart[key]!),
                            style: const TextStyle(
                              color: Color(0xFF7D161A),
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          trailing: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              IconButton(
                                icon: const Icon(
                                  Icons.remove_circle_outline,
                                  size: 20,
                                ),
                                onPressed: () =>
                                    setState(() => cart[key] = cart[key]! - 1),
                              ),
                              Text(
                                '${cart[key]}',
                                style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15,
                                ),
                              ),
                              IconButton(
                                icon: const Icon(
                                  Icons.add_circle,
                                  color: Color(0xFF7D161A),
                                  size: 20,
                                ),
                                onPressed: () =>
                                    setState(() => cart[key] = cart[key]! + 1),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ),
        _buildSidebarFooter(),
      ],
    );
  }

  Widget _buildSidebarFooter() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: const BoxDecoration(
        color: Colors.white,
        border: Border(top: BorderSide(color: Colors.black12)),
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text("Tạm tính:"),
              Text(
                formatPrice(tempTotal),
                style: const TextStyle(
                  fontSize: 22,
                  fontWeight: FontWeight.bold,
                  color: Color(0xFF7D161A),
                ),
              ),
            ],
          ),
          const SizedBox(height: 15),
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
              onPressed: isSubmitting ? null : _confirmOrder,
              child: isSubmitting
                  ? const CircularProgressIndicator(color: Colors.white)
                  : const Text(
                      "GỬI ĐƠN XUỐNG BẾP",
                      style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
            ),
          ),
        ],
      ),
    );
  }

  // =========================================================
  // CUSTOM MODALS
  // =========================================================

  void _handleSelection(String name, List<dynamic> items) {
    bool isSet = items.first['tenSetLau'] != null;
    if (isSet) {
      _showSetDetailModal(items.first);
    } else if (items.length > 1) {
      _showVariantModal(name, items);
    } else {
      setState(
        () => cart[_getCartKey(items.first)] =
            (cart[_getCartKey(items.first)] ?? 0) + 1,
      );
    }
  }

  void _showVariantModal(String baseName, List<dynamic> variants) {
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(25)),
        child: Container(
          width: 450,
          clipBehavior: Clip.antiAlias,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(25),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              SizedBox(
                height: 200,
                width: double.infinity,
                child: _buildImage(variants.first['hinhAnh']),
              ),
              Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      baseName,
                      style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF7D161A),
                      ),
                    ),
                    const Text(
                      "Chọn phân loại:",
                      style: TextStyle(color: Colors.grey),
                    ),
                    const SizedBox(height: 10),
                    ...variants
                        .map(
                          (v) => ListTile(
                            title: Text(v['tenMon']),
                            subtitle: Text(v['dinhLuong'] ?? ""),
                            trailing: Text(
                              formatPrice(v['giaBan']),
                              style: const TextStyle(
                                color: Colors.red,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            onTap: () {
                              setState(
                                () => cart[_getCartKey(v)] =
                                    (cart[_getCartKey(v)] ?? 0) + 1,
                              );
                              Navigator.pop(context);
                            },
                          ),
                        )
                        .toList(),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _showSetDetailModal(dynamic setLau) {
    List<dynamic> details = setLau['danhSachMon'] ?? [];
    showDialog(
      context: context,
      builder: (context) => Dialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(25)),
        child: Container(
          width: 500,
          clipBehavior: Clip.antiAlias,
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(25),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              SizedBox(
                height: 220,
                width: double.infinity,
                child: _buildImage(setLau['hinhAnh']),
              ),
              Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      setLau['tenSetLau'],
                      style: const TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                        color: Color(0xFF7D161A),
                      ),
                    ),
                    const SizedBox(height: 10),
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: Colors.grey[50],
                        borderRadius: BorderRadius.circular(15),
                      ),
                      child: Column(
                        children: details
                            .map(
                              (d) => Row(
                                children: [
                                  const Icon(
                                    Icons.check_circle,
                                    color: Colors.green,
                                    size: 18,
                                  ),
                                  const SizedBox(width: 8),
                                  Text(d['tenMon'] ?? ""),
                                  const Spacer(),
                                  Text("x${d['soLuong']}"),
                                ],
                              ),
                            )
                            .toList(),
                      ),
                    ),
                    const SizedBox(height: 20),
                    Text(
                      "Giá: ${formatPrice(setLau['giaBan'])}",
                      style: const TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                        color: Colors.red,
                      ),
                    ),
                    const SizedBox(height: 15),
                    SizedBox(
                      width: double.infinity,
                      height: 50,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF7D161A),
                        ),
                        onPressed: () {
                          setState(
                            () => cart[_getCartKey(setLau)] =
                                (cart[_getCartKey(setLau)] ?? 0) + 1,
                          );
                          Navigator.pop(context);
                        },
                        child: const Text(
                          "ĐẶT SET NÀY",
                          style: TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // --- HELPERS ---
  Future<void> _confirmOrder() async {
    List<Map<String, dynamic>> payload = [];
    cart.forEach((key, qty) {
      if (qty > 0) {
        bool isSet = key.startsWith('set_');
        int id = int.parse(key.split('_')[1]);
        payload.add({
          "idChiTietMonAn": isSet ? null : id,
          "idSetLau": isSet ? id : null,
          "soLuong": qty,
          "ghiChu": "",
        });
      }
    });
    if (payload.isEmpty) return;
    setState(() => isSubmitting = true);
    try {
      final header = await ApiConfig.getAuthHeader();
      final res = await http.post(
        Uri.parse('${ApiConfig.baseUrl}/hoa-don-thanh-toan/tao-don'),
        headers: header,
        body: jsonEncode({
          "idHoaDon": widget.idHoaDon > 0 ? widget.idHoaDon : null,
          "idBanAn": widget.idBan,
          "idKhachHang": widget.idKhachHang > 0 ? widget.idKhachHang : null,
          "chiTietHoaDon": payload,
        }),
      );
      if (res.statusCode == 200 || res.statusCode == 201) {
        _showSnackBar('Gửi đơn thành công!', Colors.green);
        fetchAllData();
      }
    } catch (e) {
      _showSnackBar('Lỗi: $e', Colors.red);
    } finally {
      if (mounted) setState(() => isSubmitting = false);
    }
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12, top: 10),
      child: Row(
        children: [
          Container(
            width: 5,
            height: 18,
            decoration: BoxDecoration(
              color: const Color(0xFF7D161A),
              borderRadius: BorderRadius.circular(10),
            ),
          ),
          const SizedBox(width: 10),
          Text(
            title,
            style: const TextStyle(
              fontSize: 17,
              fontWeight: FontWeight.bold,
              color: Colors.black87,
            ),
          ),
        ],
      ),
    );
  }

  String formatPrice(dynamic p) =>
      '${p.toString().replaceAllMapped(RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'), (m) => '${m[1]}.')}đ';

  Widget _buildImage(String? url) {
    if (url == null || url.isEmpty)
      return const Icon(Icons.fastfood, color: Colors.grey, size: 30);
    if (url.startsWith('data:image'))
      return Image.memory(base64Decode(url.split(',').last), fit: BoxFit.cover);
    return Image.network(
      '${ApiConfig.baseUrl.replaceAll('/api', '')}/$url',
      fit: BoxFit.cover,
      errorBuilder: (_, __, ___) => const Icon(Icons.broken_image),
    );
  }

  void _showSnackBar(String m, Color c) =>
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(m),
          backgroundColor: c,
          behavior: SnackBarBehavior.floating,
        ),
      );
}
