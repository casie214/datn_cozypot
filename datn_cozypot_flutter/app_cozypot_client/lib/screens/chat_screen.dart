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
  Timer? _listTimer;
  final TextEditingController _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    fetchWaitingList();
    // 🚀 Quét khách chờ mỗi 5 giây
    _listTimer = Timer.periodic(const Duration(seconds: 5), (_) {
      fetchWaitingList(isSilent: true);
    });
  }

  @override
  void dispose() {
    _listTimer?.cancel(); // Tắt vòng lặp khi chuyển Tab
    _searchController.dispose();
    super.dispose();
  }

  Future<void> fetchWaitingList({bool isSilent = false}) async {
    if (!isSilent && mounted) setState(() => isLoading = true);
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
          // Ô tìm kiếm
          Container(
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
                contentPadding: const EdgeInsets.symmetric(vertical: 0),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(20),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
          ),

          // Danh sách
          Expanded(
            child: isLoading
                ? const Center(
                    child: CircularProgressIndicator(color: Color(0xFF7D161A)),
                  )
                : filteredList.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.mark_chat_read,
                          size: 60,
                          color: Colors.grey.shade300,
                        ),
                        const SizedBox(height: 10),
                        const Text(
                          'Chưa có khách cần hỗ trợ',
                          style: TextStyle(color: Colors.grey, fontSize: 16),
                        ),
                      ],
                    ),
                  )
                : RefreshIndicator(
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

                        return Container(
                          margin: const EdgeInsets.symmetric(horizontal: 10),
                          decoration: BoxDecoration(
                            color: Colors.white,
                            borderRadius: BorderRadius.circular(10),
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withOpacity(0.02),
                                blurRadius: 4,
                                offset: const Offset(0, 2),
                              ),
                            ],
                          ),
                          child: ListTile(
                            leading: Stack(
                              children: [
                                CircleAvatar(
                                  radius: 22,
                                  backgroundColor: const Color(
                                    0xFF7D161A,
                                  ).withOpacity(0.1),
                                  child: const Icon(
                                    Icons.person,
                                    color: Color(0xFF7D161A),
                                    size: 24,
                                  ),
                                ),
                                Positioned(
                                  bottom: 0,
                                  right: 0,
                                  child: Container(
                                    width: 12,
                                    height: 12,
                                    decoration: BoxDecoration(
                                      color: Colors.green,
                                      shape: BoxShape.circle,
                                      border: Border.all(
                                        color: Colors.white,
                                        width: 2,
                                      ),
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            title: Text(
                              'Khách hàng $shortId',
                              style: const TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 15,
                              ),
                            ),
                            subtitle: const Text(
                              'Đang đợi phản hồi...',
                              style: TextStyle(
                                color: Colors.grey,
                                fontSize: 13,
                              ),
                            ),
                            trailing: const Icon(
                              Icons.chevron_right,
                              color: Colors.grey,
                            ),
                            onTap: () async {
                              // Chuyển sang màn hình chat chi tiết
                              await Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => ChatDetailScreen(
                                    conversationId: id,
                                    shortId: shortId,
                                  ),
                                ),
                              );
                              // Khi quay lại thì load lại danh sách
                              fetchWaitingList(isSilent: true);
                            },
                          ),
                        );
                      },
                    ),
                  ),
          ),
        ],
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
  Timer? _msgTimer;
  final TextEditingController _msgController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    fetchMessages();
    // 🚀 Quét tin nhắn mới mỗi 2 giây
    _msgTimer = Timer.periodic(const Duration(seconds: 2), (_) {
      fetchMessages(isSilent: true);
    });
  }

  @override
  void dispose() {
    _msgTimer?.cancel(); // Tắt vòng lặp khi thoát màn hình chat
    _msgController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  // Lấy lịch sử chat
  Future<void> fetchMessages({bool isSilent = false}) async {
    try {
      final header = await ApiConfig.getAuthHeader();
      final url = Uri.parse(
        '${ApiConfig.baseUrl}/botpress/history/${widget.conversationId}',
      );
      final res = await http.get(url, headers: header);

      if (res.statusCode == 200) {
        final data = jsonDecode(utf8.decode(res.bodyBytes));
        if (data['messages'] != null) {
          // Lật ngược list vì Botpress thường trả tin mới nhất lên đầu
          List<dynamic> newMessages = List.from(data['messages'].reversed);

          if (mounted) {
            setState(() {
              if (newMessages.length > messages.length) {
                messages = newMessages;
                _scrollToBottom(); // Có tin mới thì cuộn xuống
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
    }
  }

  // Gửi tin nhắn
  Future<void> sendReply() async {
    String text = _msgController.text.trim();
    if (text.isEmpty || isSending) return;

    setState(() {
      isSending = true;
      _msgController.clear();
      // Thêm tạm vào UI cho mượt
      messages.add({
        "payload": {"text": text},
        "direction": "outgoing",
        "createdAt": DateTime.now().toUtc().toIso8601String(),
      });
    });
    _scrollToBottom();

    try {
      final header = await ApiConfig.getAuthHeader();
      final url = Uri.parse('${ApiConfig.baseUrl}/botpress/reply');
      final body = jsonEncode({
        "conversationId": widget.conversationId,
        "message": text,
      });

      await http.post(url, headers: header, body: body);
      // Gửi xong thì gọi lại API để đồng bộ chắc chắn
      fetchMessages(isSilent: true);
    } catch (e) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Lỗi gửi tin nhắn')));
    } finally {
      if (mounted) setState(() => isSending = false);
    }
  }

  // Kết thúc hỗ trợ
  Future<void> resolveChat() async {
    try {
      final header = await ApiConfig.getAuthHeader();

      // 1. Bắn "tín hiệu ngầm" [RESET_CHAT] xuống Web của khách hàng
      final replyUrl = Uri.parse('${ApiConfig.baseUrl}/botpress/reply');
      await http.post(
        replyUrl,
        headers: header,
        body: jsonEncode({
          "conversationId": widget.conversationId,
          "message":
              "Trợ lý CozyPot đã hoàn tất hỗ trợ. Chúc quý khách dùng bữa ngon miệng! [RESET_CHAT]",
        }),
      );

      // 🚨 QUAN TRỌNG: Bắt buộc đợi 2 giây để Botpress kịp đẩy tin nhắn về Web khách
      await Future.delayed(const Duration(seconds: 2));

      // 2. Gọi API Resolve để đóng phiên bên Backend
      final resolveUrl = Uri.parse(
        '${ApiConfig.baseUrl}/botpress/resolve/${widget.conversationId}',
      );
      final res = await http.delete(resolveUrl, headers: header);

      if (res.statusCode == 200) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text(
              'Đã hoàn tất & Reset khung chat của khách',
              style: TextStyle(color: Colors.white),
            ),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context); // Thoát ra màn hình danh sách chờ
      }
    } catch (e) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('Lỗi khi kết thúc chat')));
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

  String formatTime(String? dateStr) {
    if (dateStr == null) return '';
    try {
      final date = DateTime.parse(dateStr).toLocal();
      return "${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}";
    } catch (e) {
      return '';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF4F6F9),
      appBar: AppBar(
        backgroundColor: const Color(0xFF7D161A),
        iconTheme: const IconThemeData(color: Colors.white),
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'KH-${widget.shortId}',
              style: const TextStyle(
                color: Colors.white,
                fontSize: 16,
                fontWeight: FontWeight.bold,
              ),
            ),
            const Row(
              children: [
                Icon(Icons.circle, color: Colors.greenAccent, size: 10),
                SizedBox(width: 5),
                Text(
                  'Đang trực tuyến',
                  style: TextStyle(color: Colors.white70, fontSize: 12),
                ),
              ],
            ),
          ],
        ),
        actions: [
          TextButton.icon(
            icon: const Icon(Icons.check, color: Colors.white, size: 18),
            label: const Text(
              'Hoàn tất',
              style: TextStyle(color: Colors.white),
            ),
            onPressed: () {
              showDialog(
                context: context,
                builder: (ctx) => AlertDialog(
                  title: const Text('Xác nhận'),
                  content: const Text(
                    'Bạn có chắc chắn muốn kết thúc hỗ trợ khách hàng này?',
                  ),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.pop(ctx),
                      child: const Text(
                        'Hủy',
                        style: TextStyle(color: Colors.grey),
                      ),
                    ),
                    TextButton(
                      onPressed: () {
                        Navigator.pop(ctx);
                        resolveChat();
                      },
                      child: const Text(
                        'Hoàn tất',
                        style: TextStyle(
                          color: Color(0xFF7D161A),
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  ],
                ),
              );
            },
          ),
        ],
      ),
      body: Column(
        children: [
          // Khu vực tin nhắn
          Expanded(
            child: isLoading
                ? const Center(
                    child: CircularProgressIndicator(color: Color(0xFF7D161A)),
                  )
                : ListView.builder(
                    controller: _scrollController,
                    padding: const EdgeInsets.all(15),
                    itemCount: messages.length + 1, // +1 cho dòng "Bắt đầu"
                    itemBuilder: (context, index) {
                      if (index == 0) {
                        return Center(
                          child: Container(
                            margin: const EdgeInsets.only(bottom: 20),
                            padding: const EdgeInsets.symmetric(
                              horizontal: 12,
                              vertical: 4,
                            ),
                            decoration: BoxDecoration(
                              color: Colors.grey.shade300,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: const Text(
                              'Cuộc trò chuyện bắt đầu',
                              style: TextStyle(
                                fontSize: 11,
                                color: Colors.black54,
                              ),
                            ),
                          ),
                        );
                      }

                      final msg = messages[index - 1];
                      bool isAdmin =
                          msg['direction'] ==
                          'outgoing'; // Outgoing là Admin gửi
                      String text = msg['payload'] != null
                          ? (msg['payload']['text'] ?? '')
                          : '';

                      return Align(
                        alignment: isAdmin
                            ? Alignment.centerRight
                            : Alignment.centerLeft,
                        child: Container(
                          margin: const EdgeInsets.only(bottom: 15),
                          constraints: BoxConstraints(
                            maxWidth: MediaQuery.of(context).size.width * 0.75,
                          ),
                          child: Column(
                            crossAxisAlignment: isAdmin
                                ? CrossAxisAlignment.end
                                : CrossAxisAlignment.start,
                            children: [
                              Container(
                                padding: const EdgeInsets.symmetric(
                                  horizontal: 15,
                                  vertical: 12,
                                ),
                                decoration: BoxDecoration(
                                  color: isAdmin
                                      ? const Color(0xFF7D161A)
                                      : Colors.white,
                                  borderRadius: BorderRadius.only(
                                    topLeft: const Radius.circular(15),
                                    topRight: const Radius.circular(15),
                                    bottomLeft: isAdmin
                                        ? const Radius.circular(15)
                                        : const Radius.circular(2),
                                    bottomRight: isAdmin
                                        ? const Radius.circular(2)
                                        : const Radius.circular(15),
                                  ),
                                  boxShadow: [
                                    BoxShadow(
                                      color: Colors.black.withOpacity(0.05),
                                      blurRadius: 3,
                                    ),
                                  ],
                                  border: isAdmin
                                      ? null
                                      : Border.all(color: Colors.grey.shade200),
                                ),
                                child: Text(
                                  text,
                                  style: TextStyle(
                                    color: isAdmin
                                        ? Colors.white
                                        : Colors.black87,
                                    fontSize: 15,
                                  ),
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                formatTime(msg['createdAt']),
                                style: const TextStyle(
                                  fontSize: 10,
                                  color: Colors.grey,
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
          ),

          // Khu vực nhập liệu
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 10),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.grey.shade200,
                  offset: const Offset(0, -2),
                  blurRadius: 5,
                ),
              ],
            ),
            child: SafeArea(
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _msgController,
                      decoration: InputDecoration(
                        hintText: 'Nhập tin nhắn hỗ trợ...',
                        hintStyle: TextStyle(color: Colors.grey.shade400),
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 20,
                          vertical: 10,
                        ),
                        filled: true,
                        fillColor: const Color(0xFFF0F2F5),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(25),
                          borderSide: BorderSide.none,
                        ),
                      ),
                      onSubmitted: (_) => sendReply(),
                    ),
                  ),
                  const SizedBox(width: 10),
                  CircleAvatar(
                    backgroundColor: isSending
                        ? Colors.grey
                        : const Color(0xFF7D161A),
                    radius: 22,
                    child: IconButton(
                      icon: const Icon(
                        Icons.send,
                        color: Colors.white,
                        size: 20,
                      ),
                      onPressed: isSending ? null : sendReply,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
