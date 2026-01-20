import { ref, onMounted, watch } from "vue";
import {
  BeGetAllHoaDon,
  BeGetChiTietHoaDon,
  BeGetThamSoHeThong,
  BeSearchHoaDon,
  BeGetLichSuHoaDon,
  BeUpdateMonDaLen,
  BeUpdateTatCaDaLen,
  BeHuyHoaDon,
  BeGetHoaDonById,
  BeGetLichSuThanhToan,
} from "./orderService";

export function useOrderManager() {
  const currentPage = ref(0);
  const totalPages = ref(0);

  const selectedOrder = ref(null);
  const orderList = ref([]);
  const orderDetails = ref([]);
  const currentVAT = ref(0);

  const isHistoryModalOpen = ref(false);
  const selectedHistoryOrder = ref(null);
  const historyEvents = ref([]);
  const paymentHistory = ref([]);

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
    return new Intl.DateTimeFormat("vi-VN", {
      hour: "2-digit",
      minute: "2-digit",
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    }).format(date);
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
    const statuses = { 0: "Đã hủy", 1: "Đang phục vụ", 2: "Hoàn thành" , 3: 'Chờ nhận bàn'};
    return statuses[statusInt] || "Không xác định";
  };

  const processOrderData = (data) => {
    if (!data) return [];
    return data.map((item) => {
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
      const response = await BeGetAllHoaDon(currentPage.value);
      orderList.value = processOrderData(response.content);
      totalPages.value = response.totalPages;
    } catch (error) {
      console.error("Lỗi tải danh sách:", error);
    }
  };

  const performSearch = async (isNewSearch = false) => {
    try {
      if (isNewSearch) {
        currentPage.value = 0; // Reset về trang đầu nếu bấm nút tìm
      }

      const statusMap = { "Đã hủy": 0, "Đang phục vụ": 1, "Hoàn thành": 2 , 'Chờ nhận bàn': 3};
      const trangThaiInt =
        filters.value.status !== "Tất cả"
          ? statusMap[filters.value.status]
          : null;

      const tuNgayISO = filters.value.fromDate
        ? `${filters.value.fromDate}T00:00:00Z`
        : null;
      const denNgayISO = filters.value.toDate
        ? `${filters.value.toDate}T23:59:59Z`
        : null;

      const response = await BeSearchHoaDon(
        filters.value.search,
        trangThaiInt,
        tuNgayISO,
        denNgayISO,
        currentPage.value,
      );

      orderList.value = processOrderData(response.content);
      totalPages.value = response.totalPages;
    } catch (error) {
      alert("Tìm kiếm thất bại!");
    }
  };
  const handleSearch = () => {
    performSearch(true);
  };

  const handlePageChange = async (page) => {
    currentPage.value = page;
    const hasFilters =
      filters.value.search ||
      filters.value.status !== "Tất cả" ||
      filters.value.fromDate ||
      filters.value.toDate;

    if (hasFilters) {
      await performSearch(false);
    } else {
      await fetchOrders();
    }
  };

  const handleReset = () => {
    filters.value = { search: "", status: "Tất cả", fromDate: "", toDate: "" };
    currentPage.value = 0;
    fetchOrders();
  };

  const loadOrderHistory = async (order) => {
    if (!order) return;
    try {
      const data = await BeGetLichSuHoaDon(order.dbId);
      const events = data.map((item) => {
        let type = "bg-gray";
        const act = (item.hanhDong || "").toLowerCase();
        if (act.includes("tạo")) type = "create";
        else if (act.includes("hủy") || act.includes("xóa")) type = "delete";
        else if (act.includes("thanh toán")) type = "payment";

        return {
          id: item.idLog || item.idChiTietHD,
          action: item.hanhDong,
          title: item.tieuDe || item.hanhDong,
          time: formatDate(item.thoiGianThucHien || item.thoiGian),
          user:
            item.nguoiThucHien ||
            (item.idNhanVien ? item.idNhanVien.hoTen : "Hệ thống"),
          detail: item.lyDoThucHien || item.lyDo || item.chiTietMon,
          type: type,
        };
      });
      historyEvents.value = events;
    } catch (error) {
      console.error("Lỗi tải lịch sử:", error);
      historyEvents.value = [];
    }
  };

  const handleViewDetail = async (dbId) => {
    try {
      const details = await BeGetChiTietHoaDon(dbId);
      orderDetails.value = details;
      const invoiceInfo = await BeGetHoaDonById(dbId);

      if (invoiceInfo) {
        selectedOrder.value = {
           id: invoiceInfo.maHoaDon,
           dbId: invoiceInfo.id,
           khachHang: invoiceInfo.tenKhachHang || "Khách vãng lai",
           sdt: invoiceInfo.sdtKhachHang || "---",
           ban: invoiceInfo.tenBan,
           loai: mapOrderType(invoiceInfo.hinhThucDat),
           tongTien: formatCurrency(invoiceInfo.tongTienThanhToan),
           tongTienRaw: invoiceInfo.tongTienThanhToan,
           soTienDaGiam: invoiceInfo.soTienDaGiam || 0,
           trangThai: mapStatus(invoiceInfo.trangThaiHoaDon),
           ngayTao: formatDate(invoiceInfo.thoiGianTao),
        };
        
        await Promise.all([
            loadOrderHistory(selectedOrder.value),
            loadPaymentHistory(dbId) 
        ]);
      }
    } catch (error) {
      console.error("Lỗi lấy chi tiết:", error);
      selectedOrder.value = null;
    }
  };

  const loadPaymentHistory = async (idHoaDon) => {
      try {
          const data = await BeGetLichSuThanhToan(idHoaDon);
          paymentHistory.value = data;
      } catch (error) {
          console.error("Lỗi tải lịch sử thanh toán:", error);
          paymentHistory.value = [];
      }
  }

  const handleViewHistory = async (maHoaDon) => {
    const order = orderList.value.find((item) => item.id === maHoaDon);
    if (!order) return;
    selectedHistoryOrder.value = order;
    await loadOrderHistory(order);
    isHistoryModalOpen.value = true;
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
    if (!order || !confirm(`Bạn có chắc chắn muốn hủy hóa đơn ${order.id}?`))
      return;

    const statusMap = { "Đã hủy": 0, "Đang phục vụ": 1, "Hoàn thành": 2 , 'Chờ nhận bàn': 3};
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
      if (filters.value.search || filters.value.status !== "Tất cả") {
        performSearch(false);
      } else {
        fetchOrders();
      }

      if (selectedOrder.value && selectedOrder.value.dbId === order.dbId) {
        await handleViewDetail(order.dbId);
      }
    } catch (error) {
      alert("Thất bại: " + error.message);
    }
  };

  onMounted(async () => {
    await fetchConfig();
    await fetchOrders();
  });

  return {
    currentPage,
    totalPages,
    filters,
    orderList,
    paymentHistory,
    selectedOrder,
    orderDetails,
    currentVAT,
    isHistoryModalOpen,
    selectedHistoryOrder,
    historyEvents,
    handleViewDetail,
    handleViewHistory,
    closeHistoryModal: () => {
      isHistoryModalOpen.value = false;
    },
    handleSearch,
    handleReset,
    handlePageChange,
    handlePrintOrder: (id) => console.log("In:", id),
    handleUpdateMonDaLen,
    handleUpdateTatCaDaLen,
    handleHuyDon,
  };
}
