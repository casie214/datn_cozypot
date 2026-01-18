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
  const selectedOrder = ref(null);
  const orderList = ref([]);
  const orderDetails = ref([]);
  const currentVAT = ref(0);
  
  const isHistoryModalOpen = ref(false);
  const selectedHistoryOrder = ref(null);
  const historyEvents = ref([]);

  const filters = ref({
    search: "",
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

  const mapOrderType = (typeInt) => {
    const type = Number(typeInt); 
    if (type === 1) return "Online";
    if (type === 2) return "Tại quầy";
    return "Khác";
  };

  const mapStatus = (statusInt) => {
    const statuses = { 0: "Đã hủy", 1: "Đã xác nhận", 2: "Hoàn thành" };
    return statuses[statusInt] || "Không xác định";
  };

  const processOrderData = (data) => {
    if (!data) return [];
    return data.map((item) => {
      // const finalPrice = item.trangThaiHoaDon === 0 ? 0 : item.tongTienThanhToan || 0;
      return {
        id: item.maHoaDon,
        dbId: item.id,
        khachHang: item.tenKhachHang,
        sdt: item.sdtKhachHang,
        ban: item.tenBan,
        loai: mapOrderType(item.hinhThucDat),
        tongTien: formatCurrency(item.tongTienThanhToan),
        tongTienRaw: item.tongTienThanhToan,
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
    } catch (error) {
      currentVAT.value = 10;
    }
  };

  const fetchOrders = async () => {
    try {
      const data = await BeGetAllHoaDon();
      orderList.value = processOrderData(data);
      // console.log(data);
    } catch (error) {
      console.error("Lỗi khi tải danh sách hóa đơn:", error);
    }
  };

  const handleSearch = async () => {
    try {
      const statusMap = { "Đã hủy": 0, "Đã xác nhận": 1, "Hoàn thành": 2 };
      const trangThaiInt = filters.value.status !== "Tất cả" ? statusMap[filters.value.status] : null;

      const tuNgayISO = filters.value.fromDate ? `${filters.value.fromDate}T00:00:00Z` : null;
      const denNgayISO = filters.value.toDate ? `${filters.value.toDate}T23:59:59Z` : null;

      const data = await BeSearchHoaDon(filters.value.search, trangThaiInt, tuNgayISO, denNgayISO);
      orderList.value = processOrderData(data);
    } catch (error) {
      alert("Tìm kiếm thất bại!");
    }
  };

  const handleReset = () => {
    filters.value = { search: "", status: "Tất cả", fromDate: "", toDate: "" };
    fetchOrders();
  };
  const handleViewDetail = async (dbId) => {
    try {
      const data = await BeGetChiTietHoaDon(dbId);
      orderDetails.value = data;

      if (orderList.value.length === 0) {
        await fetchOrders();
      }
      
      selectedOrder.value = orderList.value.find((item) => item.dbId === Number(dbId));
    } catch (error) {
      console.error("Lỗi lấy chi tiết:", error);
    }
  };

  const handleViewHistory = async (maHoaDon) => {
    const order = orderList.value.find((item) => item.id === maHoaDon);
    if (!order) return;
    
    selectedHistoryOrder.value = order;
    try {
      const data = await BeGetLichSuHoaDon(order.dbId);
      const events = data.map((item) => ({
        id: item.idLog || item.idChiTietHD,
        action: item.hanhDong,
        title: item.tieuDe || item.hanhDong,
        time: formatDate(item.thoiGian),
        user: item.nguoiThucHien,
        detail: item.lyDo || item.chiTietMon,
        orderCode: item.maHoaDon,
        type: item.loaiHanhDong,
      }));

      if (!events.some((e) => e.type === "create")) {
        events.push({
          id: "create-" + order.dbId,
          action: "Tạo đơn",
          title: "Tạo đơn hàng mới",
          time: order.ngayTao,
          user: "Hệ thống",
          detail: "Khởi tạo đơn hàng trên hệ thống",
          type: "create",
        });
      }

      historyEvents.value = events;
      isHistoryModalOpen.value = true;
    } catch (error) {
      alert("Không thể tải lịch sử!");
    }
  };

  const handleUpdateMonDaLen = async (idChiTietHD) => {
    try {
      await BeUpdateMonDaLen(idChiTietHD);
      if (selectedOrder.value) {
        orderDetails.value = await BeGetChiTietHoaDon(selectedOrder.value.dbId);
      }
    } catch (error) {
      alert("Lỗi cập nhật món!");
    }
  };

  const handleUpdateTatCaDaLen = async () => {
    if (!selectedOrder.value) return;
    try {
      await BeUpdateTatCaDaLen(selectedOrder.value.dbId);
      orderDetails.value = await BeGetChiTietHoaDon(selectedOrder.value.dbId);
      alert("Đã xác nhận tất cả món!");
    } catch (error) {
      alert("Cập nhật thất bại!");
    }
  };

  const handleHuyDon = async (order) => {
    if (!order || !confirm(`Bạn có chắc chắn muốn hủy hóa đơn ${order.id}?`)) return;

    const statusMap = { "Đã hủy": 0, "Đã xác nhận": 1, "Hoàn thành": 2 };
    const payload = {
      idHoaDon: order.dbId,
      idNhanVien: 1,
      hanhDong: "Hủy hóa đơn",
      lyDoThucHien: "Nhân viên hủy đơn/Khách đổi ý",
      trangThaiLichSuTruocDo: statusMap[order.trangThai],
      thoiGianThucHien: new Date().toISOString(),
    };

    try {
      await BeHuyHoaDon(payload);
      alert("Hủy hóa đơn thành công!");
      await fetchOrders();
      await handleViewDetail(order.dbId);
    } catch (error) {
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
    selectedOrder,
    orderDetails,
    currentVAT,
    isHistoryModalOpen,
    selectedHistoryOrder,
    historyEvents,
    handleViewDetail,
    handleViewHistory,
    closeHistoryModal: () => { isHistoryModalOpen.value = false; },
    handleSearch,
    handleReset,
    handlePrintOrder: (id) => console.log("In:", id),
    handleUpdateMonDaLen,
    handleUpdateTatCaDaLen,
    handleHuyDon,
  };
}