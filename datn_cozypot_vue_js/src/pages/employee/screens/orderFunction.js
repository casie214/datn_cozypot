import { ref, onMounted } from "vue";
import {
  BeGetAllHoaDon,
  BeGetChiTietHoaDon,
  BeGetThamSoHeThong,
  BeSearchHoaDon,
  BeGetLichSuHoaDon,
  BeUpdateMonDaLen,
  BeUpdateTatCaDaLen,
  BeHuyHoaDon,
} from "./orderService";

export function useOrderManager() {
  const isDetailModalOpen = ref(false);
  const selectedOrder = ref(null);
  const isHistoryModalOpen = ref(false);
  const selectedHistoryOrder = ref(null);
  const historyEvents = ref([]);

  const orderList = ref([]);
  const orderDetails = ref([]);
  const currentVAT = ref(0);

  const filters = ref({
    status: "Tất cả",
    fromDate: "",
    toDate: "",
  });

  const formatDate = (dateString) => {
    if (!dateString) return "---";
    const date = new Date(dateString);

    if (isNaN(date.getTime())) return "Ngày lỗi";

    const day = String(date.getUTCDate()).padStart(2, "0");
    const month = String(date.getUTCMonth() + 1).padStart(2, "0");
    const year = date.getUTCFullYear();
    const hours = String(date.getUTCHours()).padStart(2, "0");
    const minutes = String(date.getUTCMinutes()).padStart(2, "0");

    return `${hours}:${minutes} ${day}/${month}/${year}`;
  };

  const formatCurrency = (value) => {
    if (value === null || value === undefined) return "0 ₫";
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value);
  };

  const mapStatus = (statusInt) => {
    switch (statusInt) {
      case 0:
        return "Đã hủy";
      case 1:
        return "Đã xác nhận";
      case 2:
        return "Hoàn thành";
      default:
        return "Không xác định";
    }
  };

  const processOrderData = (data) => {
    if (!data) return [];
    return data.map((item) => {
      // Ép tiền về 0 nếu trạng thái là "Đã hủy" (0)
      const finalPrice =
        item.trangThaiHoaDon === 0 ? 0 : item.tongTienThanhToan || 0;
      return {
        id: item.maHoaDon,
        dbId: item.id,
        khachHang: item.tenKhachHang,
        sdt: item.sdtKhachHang,
        ban: item.tenBan,
        tongTien: formatCurrency(finalPrice),
        tongTienRaw: finalPrice,
        soTienDaGiam: item.soTienDaGiam || 0,
        trangThai: mapStatus(item.trangThaiHoaDon),
        ngayTao: formatDate(item.thoiGianTao),
      };
    });
  };

  const fetchConfig = async () => {
    try {
      const config = await BeGetThamSoHeThong();
      currentVAT.value = Number(config.VAT || 10);
      console.log("VAT hệ thống:", currentVAT.value);
    } catch (error) {
      console.error("Lỗi lấy config:", error);
      currentVAT.value = 10;
    }
  };

  const fetchOrders = async () => {
    try {
      const data = await BeGetAllHoaDon();
      orderList.value = processOrderData(data);
    } catch (error) {
      console.error("Lỗi khi tải danh sách hóa đơn:", error);
    }
  };

  const handleSearch = async () => {
    try {
      const statusMap = { "Đã hủy": 0, "Đã xác nhận": 1, "Hoàn thành": 2 };
      const trangThaiInt =
        filters.value.status !== "Tất cả"
          ? statusMap[filters.value.status]
          : null;

      let tuNgayISO = null;
      let denNgayISO = null;

      if (filters.value.fromDate) {
        tuNgayISO = `${filters.value.fromDate}T00:00:00Z`;
      }

      if (filters.value.toDate) {
        denNgayISO = `${filters.value.toDate}T23:59:59Z`;
      }

      const data = await BeSearchHoaDon(trangThaiInt, tuNgayISO, denNgayISO);
      orderList.value = processOrderData(data);
    } catch (error) {
      console.error("Lỗi tìm kiếm:", error);
      alert("Tìm kiếm thất bại! Kiểm tra lại kết nối Backend.");
    }
  };

  const handleReset = () => {
    filters.value = { status: "Tất cả", fromDate: "", toDate: "" };
    fetchOrders(); // Load lại toàn bộ data ban đầu
  };

  const handleViewDetail = async (maHoaDon) => {
    const order = orderList.value.find((item) => item.id === maHoaDon);
    if (order) {
      selectedOrder.value = order;
      try {
        orderDetails.value = [];
        const data = await BeGetChiTietHoaDon(order.dbId);
        orderDetails.value = data;
        isDetailModalOpen.value = true;
      } catch (error) {
        console.error("Lỗi lấy chi tiết:", error);
        alert("Không thể tải chi tiết món ăn!");
      }
    }
  };

  const closeDetailModal = () => {
    isDetailModalOpen.value = false;
    selectedOrder.value = null;
  };

  const handleViewHistory = async (maHoaDon) => {
    const order = orderList.value.find((item) => item.id === maHoaDon);
    if (order) {
      selectedHistoryOrder.value = order;
      try {
        historyEvents.value = [];

        // 1. Gọi API lấy dữ liệu từ bảng lich_su_hoa_don (Service mới)
        const data = await BeGetLichSuHoaDon(order.dbId);

        // 2. Map dữ liệu từ Backend
        const events = data.map((item) => ({
          id: item.idLog || item.idChiTietHD, // Khớp với DTO LichSuHoaDonResponse
          action: item.hanhDong,
          title: item.tieuDe || item.hanhDong,
          time: formatDate(item.thoiGian),
          user: item.nguoiThucHien,
          detail: item.lyDo || item.chiTietMon, // Hiển thị lý do thực hiện từ bảng Log
          orderCode: item.maHoaDon,
          type: item.loaiHanhDong,
        }));

        // 3. KIỂM TRA TRƯỚC KHI THÊM THỦ CÔNG:
        // Nếu trong DB chưa có bản ghi "Tạo đơn" thì mới push thủ công
        const hasCreateEvent = events.some((e) => e.type === "create");
        if (!hasCreateEvent) {
          events.push({
            id: "create-order-" + order.dbId,
            action: "Tạo đơn",
            title: "Tạo đơn hàng mới",
            time: order.ngayTao,
            user: "Hệ thống",
            detail: "Khởi tạo đơn hàng trên hệ thống",
            orderCode: order.id,
            type: "create",
          });
        }

        // Cập nhật lại danh sách hiển thị
        historyEvents.value = events;
        isHistoryModalOpen.value = true;
      } catch (error) {
        console.error("Lỗi lấy lịch sử:", error);
        alert("Không thể tải lịch sử đơn hàng!");
      }
    }
  };

  const closeHistoryModal = () => {
    isHistoryModalOpen.value = false;
    selectedHistoryOrder.value = null;
  };

  const handlePrintOrder = (maHoaDon) => {
    console.log("In hóa đơn:", maHoaDon);
  };

  const handleUpdateMonDaLen = async (idChiTietHD) => {
    try {
      await BeUpdateMonDaLen(idChiTietHD);
      // Tải lại chi tiết để cập nhật giao diện modal
      if (selectedOrder.value) {
        const data = await BeGetChiTietHoaDon(selectedOrder.value.dbId);
        orderDetails.value = data;
      }
    } catch (error) {
      console.error("Lỗi cập nhật món:", error);
      alert("Không thể cập nhật trạng thái món!");
    }
  };

  const handleUpdateTatCaDaLen = async () => {
    if (!selectedOrder.value) return;
    try {
      await BeUpdateTatCaDaLen(selectedOrder.value.dbId);
      const data = await BeGetChiTietHoaDon(selectedOrder.value.dbId);
      orderDetails.value = data;
      alert("Đã xác nhận tất cả món đã lên bàn!");
    } catch (error) {
      console.error("Lỗi cập nhật tất cả món:", error);
      alert("Cập nhật thất bại!");
    }
  };

  const handleHuyDon = async (order) => {
    if (!confirm(`Bạn có chắc chắn muốn hủy hóa đơn ${order.id}?`)) return;

    const statusMap = { "Đã hủy": 0, "Đã xác nhận": 1, "Hoàn thành": 2 };

    const payload = {
      idHoaDon: order.dbId,
      idNhanVien: 1, // ID tạm thời, sau này lấy từ thông tin đăng nhập
      hanhDong: "Hủy hóa đơn",
      lyDoThucHien: "Nhân viên hủy đơn/Khách đổi ý",
      trangThaiLichSuTruocDo: statusMap[order.trangThai],
      thoiGianThucHien: new Date().toISOString(),
    };

    try {
      await BeHuyHoaDon(payload);
      alert("Hủy hóa đơn thành công!");
      await fetchOrders(); // Tải lại danh sách hóa đơn ở màn hình chính
      if (isDetailModalOpen.value) closeDetailModal(); // Đóng modal nếu đang mở
    } catch (error) {
      // Hiển thị thông báo: "Không thể hủy hóa đơn vì đã có món ăn được phục vụ!"
      alert("Thất bại: " + error.message);
    }
  };

  onMounted(async () => {
    await fetchConfig();
    await fetchOrders();
  });

  return {
    filters,
    orderList,
    isDetailModalOpen,
    selectedOrder,
    handleViewDetail,
    orderDetails,
    closeDetailModal,
    isHistoryModalOpen,
    selectedHistoryOrder,
    historyEvents,
    handleViewHistory,
    closeHistoryModal,
    handleSearch,
    handleReset,
    handlePrintOrder,
    handleUpdateMonDaLen,
    handleUpdateTatCaDaLen, 
    handleHuyDon,
    currentVAT,
  };
}
