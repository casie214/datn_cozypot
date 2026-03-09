import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../utils/api_config.dart';

// =========================================================
// 1. MÀN HÌNH DANH SÁCH KHÁCH HÀNG (Tab Trợ lý)
// =========================================================
class ChatScreen extends StatefulWidget {
  const ChatScreen({super.key});

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  List<String> waitingList = [];
  List<String> filteredList = [];
  bool isLoading = true;
  bool isFetching = false; // 🔒 Khóa chống chồng chéo request
  Timer? _listTimer;
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    fetchWaitingList();
    // 🚀 Quét khách chờ mỗi 5 giây (Đã đồng bộ theo yêu cầu)
    _listTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      fetchWaitingList(isSilent: true);
    });
  }

  @override
  void dispose() {
    _listTimer?.cancel();
    _searchController.dispose();
    super.dispose();
  }

  Future<void> fetchWaitingList({bool isSilent = false}) async {
    // Nếu đang có request chạy thì bỏ qua lần poll này
    if (isFetching) return;

    if (!isSilent && mounted) setState(() => isLoading = true);
    isFetching = true; // Khóa lại

    try {
      final header = await ApiConfig.getAuthHeader();
      final url = Uri.parse('${ApiConfig.baseUrl}/botpress/waiting-list');
      final res = await http.get(url, headers: header);

      if (res.statusCode == 200) {
        final List<dynamic> data = jsonDecode(utf8.decode(res.bodyBytes));
        if (mounted) {
          setState(() {
            waitingList = data.map((e) => e.toString()).toList();
            _filterList(_searchController.text);
            isLoading = false;
          });
        }
      }
    } catch (e) {
      if (mounted) setState(() => isLoading = false);
      print("Lỗi lấy danh sách chat: $e");
    } finally {
      isFetching = false; // Mở khóa cho lần poll tiếp theo
    }
  }

  void _filterList(String query) {
    setState(() {
      filteredList = waitingList
          .where((id) => id.toLowerCase().contains(query.toLowerCase()))
          .toList();
    });
  }

  @override
  Widget build(BuildContext context) {
    // ... UI giữ nguyên như cũ của bạn ...
    return Scaffold(
      backgroundColor: const Color(0xFFF0F2F5),
      appBar: AppBar(
        backgroundColor: const Color(0xFF7D161A),
        title: const Text(
          'Danh sách chờ hỗ trợ',
          style: TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 18,
          ),
        ),
      ),
      body: Column(
        children: [
          _buildSearchBar(),
          Expanded(
            child: isLoading
                ? const Center(
                    child: CircularProgressIndicator(color: Color(0xFF7D161A)),
                  )
                : _buildListView(),
          ),
        ],
      ),
    );
  }

  // --- Tách nhỏ UI cho sạch code ---
  Widget _buildSearchBar() {
    return Container(
      color: Colors.white,
      padding: const EdgeInsets.all(15),
      child: TextField(
        controller: _searchController,
        onChanged: _filterList,
        decoration: InputDecoration(
          hintText: 'Tìm theo mã chat...',
          prefixIcon: const Icon(Icons.search, color: Colors.grey),
          filled: true,
          fillColor: const Color(0xFFF8F9FA),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(20),
            borderSide: BorderSide.none,
          ),
          contentPadding: EdgeInsets.zero,
        ),
      ),
    );
  }

  Widget _buildListView() {
    if (filteredList.isEmpty)
      return const Center(child: Text("Chưa có khách cần hỗ trợ"));
    return RefreshIndicator(
      onRefresh: () => fetchWaitingList(),
      child: ListView.separated(
        padding: const EdgeInsets.symmetric(vertical: 10),
        itemCount: filteredList.length,
        separatorBuilder: (_, __) => const SizedBox(height: 5),
        itemBuilder: (context, index) {
          String id = filteredList[index];
          String shortId = id.length > 8
              ? id.substring(0, 8).toUpperCase()
              : id.toUpperCase();
          return Card(
            margin: const EdgeInsets.symmetric(horizontal: 10),
            child: ListTile(
              leading: const CircleAvatar(
                backgroundColor: Color(0xFF7D161A),
                child: Icon(Icons.person, color: Colors.white),
              ),
              title: Text(
                'Khách hàng $shortId',
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
              subtitle: const Text(
                'Đang đợi phản hồi...',
                style: TextStyle(fontSize: 12),
              ),
              trailing: const Icon(Icons.chevron_right),
              onTap: () async {
                await Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) =>
                        ChatDetailScreen(conversationId: id, shortId: shortId),
                  ),
                );
                fetchWaitingList(isSilent: true);
              },
            ),
          );
        },
      ),
    );
  }
}

// =========================================================
// 2. MÀN HÌNH CHAT CHI TIẾT VỚI KHÁCH
// =========================================================
class ChatDetailScreen extends StatefulWidget {
  final String conversationId;
  final String shortId;
  const ChatDetailScreen({
    super.key,
    required this.conversationId,
    required this.shortId,
  });

  @override
  State<ChatDetailScreen> createState() => _ChatDetailScreenState();
}

class _ChatDetailScreenState extends State<ChatDetailScreen> {
  List<dynamic> messages = [];
  bool isLoading = true;
  bool isSending = false;
  bool isFetching = false; // 🔒 Khóa chống chồng chéo request
  Timer? _msgTimer;
  final TextEditingController _msgController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    fetchMessages();
    // 🚀 SỬA TẠI ĐÂY: Thay đổi từ 2 giây thành 5 giây để giảm tải
    _msgTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      fetchMessages(isSilent: true);
    });
  }

  @override
  void dispose() {
    _msgTimer?.cancel();
    _msgController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  Future<void> fetchMessages({bool isSilent = false}) async {
    if (isFetching) return; // Không lấy tin mới nếu request trước đó chưa xong

    isFetching = true;
    try {
      final header = await ApiConfig.getAuthHeader();
      final url = Uri.parse(
        '${ApiConfig.baseUrl}/botpress/history/${widget.conversationId}',
      );
      final res = await http.get(url, headers: header);

      if (res.statusCode == 200) {
        final data = jsonDecode(utf8.decode(res.bodyBytes));
        if (data['messages'] != null) {
          List<dynamic> newMessages = List.from(data['messages'].reversed);
          if (mounted) {
            setState(() {
              if (newMessages.length > messages.length) {
                messages = newMessages;
                _scrollToBottom();
              } else {
                messages = newMessages;
              }
              isLoading = false;
            });
          }
        }
      }
    } catch (e) {
      if (mounted && !isSilent) setState(() => isLoading = false);
    } finally {
      isFetching = false;
    }
  }

  // ... Các hàm sendReply, resolveChat giữ nguyên ...
  Future<void> sendReply() async {
    String text = _msgController.text.trim();
    if (text.isEmpty || isSending) return;

    setState(() {
      isSending = true;
      _msgController.clear();
      messages.add({
        "payload": {"text": text},
        "direction": "outgoing",
        "createdAt": DateTime.now().toUtc().toIso8601String(),
      });
    });
    _scrollToBottom();

    try {
      final header = await ApiConfig.getAuthHeader();
      await http.post(
        Uri.parse('${ApiConfig.baseUrl}/botpress/reply'),
        headers: header,
        body: jsonEncode({
          "conversationId": widget.conversationId,
          "message": text,
        }),
      );
      fetchMessages(isSilent: true);
    } finally {
      if (mounted) setState(() => isSending = false);
    }
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF4F6F9),
      appBar: AppBar(
        backgroundColor: const Color(0xFF7D161A),
        iconTheme: const IconThemeData(color: Colors.white),
        title: Text(
          'KH-${widget.shortId}',
          style: const TextStyle(color: Colors.white, fontSize: 16),
        ),
        actions: [
          IconButton(
            onPressed: () => _confirmResolve(),
            icon: const Icon(Icons.check_circle_outline, color: Colors.white),
          ),
        ],
      ),
      body: Column(
        children: [
          Expanded(
            child: isLoading
                ? const Center(
                    child: CircularProgressIndicator(color: Color(0xFF7D161A)),
                  )
                : ListView.builder(
                    controller: _scrollController,
                    padding: const EdgeInsets.all(15),
                    itemCount: messages.length,
                    itemBuilder: (context, index) =>
                        _buildMessageItem(messages[index]),
                  ),
          ),
          _buildInputArea(),
        ],
      ),
    );
  }

  Widget _buildMessageItem(dynamic msg) {
    bool isAdmin = msg['direction'] == 'outgoing';
    return Align(
      alignment: isAdmin ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.only(bottom: 10),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isAdmin ? const Color(0xFF7D161A) : Colors.white,
          borderRadius: BorderRadius.circular(15),
        ),
        child: Text(
          msg['payload']['text'] ?? '',
          style: TextStyle(color: isAdmin ? Colors.white : Colors.black87),
        ),
      ),
    );
  }

  Widget _buildInputArea() {
    return Container(
      padding: const EdgeInsets.all(10),
      color: Colors.white,
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _msgController,
              decoration: const InputDecoration(hintText: 'Nhập tin nhắn...'),
            ),
          ),
          IconButton(
            onPressed: sendReply,
            icon: const Icon(Icons.send, color: Color(0xFF7D161A)),
          ),
        ],
      ),
    );
  }

  void _confirmResolve() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Hoàn tất?'),
        content: const Text('Kết thúc hỗ trợ khách hàng này?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx),
            child: const Text('Hủy'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(ctx); /* Gọi hàm resolveChat ở đây */
            },
            child: const Text('Đồng ý'),
          ),
        ],
      ),
    );
  }
}
