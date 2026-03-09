import 'package:flutter/material.dart';
import 'package:webview_flutter/webview_flutter.dart';

class OrderWebView extends StatefulWidget {
  final String url;

  // Nhận URL từ màn hình Scanner truyền sang
  const OrderWebView({super.key, required this.url});

  @override
  State<OrderWebView> createState() => _OrderWebViewState();
}

class _OrderWebViewState extends State<OrderWebView> {
  late final WebViewController controller;
  bool isLoading = true;

  @override
  void initState() {
    super.initState();

    // Khởi tạo Controller để điều khiển WebView
    controller = WebViewController()
      ..setJavaScriptMode(
        JavaScriptMode.unrestricted,
      ) // Cho phép chạy Vue.js/React
      ..setBackgroundColor(const Color(0x00000000))
      ..setNavigationDelegate(
        NavigationDelegate(
          onPageStarted: (String url) {
            setState(() {
              isLoading = true;
            });
          },
          onPageFinished: (String url) {
            setState(() {
              isLoading = false;
            });
          },
          onWebResourceError: (WebResourceError error) {
            debugPrint('Lỗi trình duyệt: ${error.description}');
          },
        ),
      )
      ..loadRequest(Uri.parse(widget.url)); // Tải URL từ QR
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Thực đơn CozyPot'),
        backgroundColor: const Color(0xFF7D161A), // Màu đỏ thương hiệu của bạn
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      body: Stack(
        children: [
          WebViewWidget(controller: controller),
          // Hiển thị vòng xoay chờ khi trang đang load
          if (isLoading)
            const Center(
              child: CircularProgressIndicator(color: Color(0xFF7D161A)),
            ),
        ],
      ),
    );
  }
}
