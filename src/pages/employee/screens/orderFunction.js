import { ref, onMounted } from "vue";
import {
  BeGetAllHoaDon,
  BeGetChiTietHoaDon,
  BeGetThamSoHeThong,
} from "./OrderService";

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
    return date.toLocaleString("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  // Hàm format tiền tệ (300000 -> 300.000 ₫)
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

  const fetchConfig = async () => {
    try {
      const config = await BeGetThamSoHeThong();
      currentVAT.value = Number(config.VAT); // Đảm bảo lưu dưới dạng số
      console.log("VAT hệ thống:", currentVAT.value);
    } catch (error) {
      console.error("Lỗi lấy config:", error);
      currentVAT.value = 10; // Fallback mặc định
    }
  };

  const fetchOrders = async () => {
    try {
      const data = await BeGetAllHoaDon();
      console.log("Dữ liệu gốc từ BE:", data);
      orderList.value = data.map((item) => {
        // 1. Lấy giá trị chốt từ Backend
        const finalPrice = item.tongTienThanhToan || 0; 

        return {
          id: item.maHoaDon,       
          dbId: item.id,           
          khachHang: item.tenKhachHang,
          sdt: item.sdtKhachHang,  
          ban: item.tenBan,
          
          tongTien: formatCurrency(finalPrice),
          // Tiền dạng số để truyền vào Modal 
          tongTienRaw: finalPrice,
          
          // Lấy giảm giá để truyền vào Modal
          soTienDaGiam: item.soTienDaGiam || 0, 

          // Khớp với DTO Java (trangThaiHoaDon)
          trangThai: mapStatus(item.trangThaiHoaDon), 
          
          // Khớp với DTO Java (thoiGianTao)
          ngayTao: formatDate(item.thoiGianTao),
        };
      });
    } catch (error) {
      console.error("Lỗi khi tải danh sách hóa đơn:", error);
    }
  };
  // ---------------------

  onMounted(async () => {
    await fetchConfig();
    await fetchOrders();
  });

  const mockHistoryData = [
    {
      id: 1,
      action: "Xóa món",
      title: "Xóa món Lẩu Thái Tomyum x1",
      time: "Vừa xong",
      user: "Administrator",
      detail: "Lẩu Thái Tomyum x1",
      orderCode: "ORD20260101174139",
      type: "delete",
    },
    {
      id: 2,
      action: "Thêm món",
      title: "Thêm món Lẩu nấm x1",
      time: "5 phút trước",
      user: "Administrator",
      detail: "Lẩu nấm x1",
      orderCode: "ORD20260101174139",
      type: "add",
    },
    {
      id: 3,
      action: "Thêm món",
      title: "Thêm món Lẩu hải sản x1",
      time: "7 phút trước",
      user: "Administrator",
      detail: "Lẩu hải sản x1",
      orderCode: "ORD20260101174139",
      type: "add",
    },
    {
      id: 4,
      action: "Tạo đơn",
      title: "Tạo đơn hàng mới",
      time: "10 phút trước",
      user: "Administrator",
      detail: "",
      orderCode: "ORD20260101174139",
      type: "create",
    },
  ];

  const handleSearch = () => {
    console.log("Đang tìm kiếm:", filters.value);
    fetchOrders();
  };

  const handleReset = () => {
    filters.value = { status: "Tất cả", fromDate: "", toDate: "" };
    fetchOrders();
  };

  const handleViewDetail = async (maHoaDon) => {
    // Tìm đơn hàng trong list đã tải
    const order = orderList.value.find((item) => item.id === maHoaDon);

    if (order) {
      selectedOrder.value = order;
      try {
        orderDetails.value = [];
        const data = await BeGetChiTietHoaDon(order.dbId); 
        orderDetails.value = data;
        isDetailModalOpen.value = true;
      } catch (error) {
        console.error("Lỗi lấy chi tiết đơn hàng:", error);
        alert("Không thể tải chi tiết món ăn!");
      }
    }
  };

  const closeDetailModal = () => {
    isDetailModalOpen.value = false;
    selectedOrder.value = null;
  };

  const handleViewHistory = (maHoaDon) => {
    const order = orderList.value.find((item) => item.id === maHoaDon);
    if (order) {
      selectedHistoryOrder.value = order;
      historyEvents.value = mockHistoryData;
      isHistoryModalOpen.value = true;
    }
  };

  const closeHistoryModal = () => {
    isHistoryModalOpen.value = false;
    selectedHistoryOrder.value = null;
  };

  const handlePrintOrder = (maHoaDon) => {
    console.log("In hóa đơn:", maHoaDon);
  };

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
    currentVAT,
  };
}