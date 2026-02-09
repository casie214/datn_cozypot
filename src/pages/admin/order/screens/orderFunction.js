import { ref, onMounted, watch } from "vue";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import Swal from "sweetalert2";
dayjs.locale("vi");

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
  const pageSize = ref(5); 
  const totalElements = ref(0);

  const selectedOrder = ref(null);
  const orderList = ref([]);
  const orderDetails = ref([]);
  const currentVAT = ref(0);

  const isHistoryModalOpen = ref(false);
  const selectedHistoryOrder = ref(null);
  const historyEvents = ref([]);
  const paymentHistory = ref([]);
  const today = dayjs().format("YYYY-MM-DD");

  const Toast = Swal.mixin({
    toast: true,
    position: "top-end",
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
    didOpen: (toast) => {
      toast.addEventListener("mouseenter", Swal.stopTimer);
      toast.addEventListener("mouseleave", Swal.resumeTimer);
    },
  });

  const cancelModalState = ref({
    isOpen: false,
    orderData: null,
    isDeposit: false,
    isWarning: false,
    warningMessage: "",
    reason: "",
    isSafe: false
  });

  const filters = ref({
    search: "",
    status: "Tất cả",
    refundStatus: "Tất cả",
    fromDate: today,
    toDate: "",
  });

  const formatDateTime = (dateString) => {
    if (!dateString) return "---";
    const date = dayjs(dateString);
    if (!date.isValid()) return "Ngày lỗi";
    return date.format("HH:mm DD/MM/YYYY");
  };

    const formatDate = (dateString) => {
    if (!dateString) return "---";
    const date = dayjs(dateString);
    if (!date.isValid()) return "Ngày lỗi";
    return date.format("DD/MM/YYYY");
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
    const statuses = {
      0: "Đã hủy",
      1: "Đang phục vụ",
      2: "Hoàn thành",
      3: "Chờ nhận bàn",
    };
    return statuses[statusInt] || "Không xác định";
  };

  const mapStatusRefund = (statusInt) => {
    const statuses = {
      0: "Không cần hoàn",
      1: "Chờ hoàn",
      2: "Đã hoàn",
      3: "Không hoàn tiền",
    };
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
        soLuongKhach: item.soLuongKhach,
        tongTien: formatCurrency(item.tongTienThanhToan),
        tongTienRaw: item.tongTienThanhToan,
        tongTienHangRaw: item.tongTienChuaGiam || 0,
        soTienDaGiam: item.soTienDaGiam || 0,
        tienCoc: formatCurrency(item.tienCoc),
        tienCocRaw: item.tienCoc || 0,
        tienHoanTra: formatCurrency(item.tienHoanTra),
        trangThaiHoanTien: mapStatusRefund(item.trangThaiHoanTien),
        trangThai: mapStatus(item.trangThaiHoaDon),
        ngayTao: formatDate(item.thoiGianTao),
        vatApDung: item.vatApDung,
        thoiGianDat: item.thoiGianDat || item.thoiGianTao,
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

  // Lấy tất cả hóa đơn
  const fetchOrders = async () => {
    try {
      const response = await BeGetAllHoaDon(currentPage.value, pageSize.value);
      orderList.value = processOrderData(response.content);
      totalPages.value = response.totalPages;
      totalElements.value = response.totalElements;
    } catch (error) {
      console.error("Lỗi tải danh sách:", error);
    }
  };

  // Tìm kiếm
  const performSearch = async (isNewSearch = false) => {
    try {
      if (isNewSearch) {
        currentPage.value = 0; 
      }

      const statusMap = {
        "Đã hủy": 0,
        "Đang phục vụ": 1,
        "Hoàn thành": 2,
        "Chờ nhận bàn": 3,
      };

      const refundStatusMap = {
        "Không cần hoàn": 0,
        "Chờ hoàn": 1,
        "Đã hoàn": 2,
        "Không hoàn tiền": 3,
      };

      const trangThaiInt =
        filters.value.status !== "Tất cả"
          ? statusMap[filters.value.status]
          : null;

      const trangThaiHoanTienInt =
        filters.value.refundStatus !== "Tất cả"
          ? refundStatusMap[filters.value.refundStatus]
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
        trangThaiHoanTienInt,
        tuNgayISO,
        denNgayISO,
        currentPage.value,
        pageSize.value
      );

      orderList.value = processOrderData(response.content);
      totalPages.value = response.totalPages;
      totalElements.value = response.totalElements;
    } catch (error) {
      console.error(error);
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
      filters.value.refundStatus !== "Tất cả" ||
      filters.value.fromDate ||
      filters.value.toDate;

    if (hasFilters) {
      await performSearch(false);
    } else {
      await fetchOrders();
    }
  };

  const handleReset = () => {
    filters.value = {
      search: "",
      status: "Tất cả",
      refundStatus: "Tất cả",
      fromDate: today,
      toDate: "",
    };
    currentPage.value = 0;
    performSearch(true);
    // fetchOrders();
  };

  //Lịch sử hóa đơn
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
          time: formatDateTime(item.thoiGianThucHien || item.thoiGian),
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

  //Chi tiết hóa đươn
  const handleViewDetail = async (dbId) => {
    try {
      const details = await BeGetChiTietHoaDon(dbId);
      orderDetails.value = details;
      const invoiceInfo = await BeGetHoaDonById(dbId);
      console.log(invoiceInfo);
      if (invoiceInfo) {
        selectedOrder.value = {
          id: invoiceInfo.maHoaDon,
          dbId: invoiceInfo.id,
          khachHang: invoiceInfo.tenKhachHang || "Khách vãng lai",
          sdt: invoiceInfo.sdtKhachHang || "---",
          ban: invoiceInfo.tenBan,
          loai: mapOrderType(invoiceInfo.hinhThucDat),
          soLuongKhach: invoiceInfo.soLuongKhach,
          tongTien: formatCurrency(invoiceInfo.tongTienThanhToan),
          tongTienRaw: invoiceInfo.tongTienThanhToan,
          tongTienHangRaw: invoiceInfo.tongTienChuaGiam || 0,
          soTienDaGiam: invoiceInfo.soTienDaGiam || 0,
          tienCoc: formatCurrency(invoiceInfo.tienCoc),
          tienCocRaw: invoiceInfo.tienCoc || 0,
          tienHoanTra: formatCurrency(invoiceInfo.tienHoanTra),
          trangThaiHoanTien: mapStatusRefund(invoiceInfo.trangThaiHoanTien),
          trangThai: mapStatus(invoiceInfo.trangThaiHoaDon),
          ngayTao: formatDate(invoiceInfo.thoiGianTao),
          vatApDung: invoiceInfo.vatApDung,
          thoiGianDat: invoiceInfo.thoiGianDat || invoiceInfo.thoiGianTao,
        };

        await Promise.all([
          loadOrderHistory(selectedOrder.value),
          loadPaymentHistory(dbId),
        ]);
      }
    } catch (error) {
      console.error("Lỗi lấy chi tiết:", error);
      selectedOrder.value = null;
    }
  };

  //Lịch sử thanh toán
  const loadPaymentHistory = async (idHoaDon) => {
    try {
      const data = await BeGetLichSuThanhToan(idHoaDon);
      paymentHistory.value = data;
    } catch (error) {
      console.error("Lỗi tải lịch sử thanh toán:", error);
      paymentHistory.value = [];
    }
  };

  const handleViewHistory = async (maHoaDon) => {
    const order = orderList.value.find((item) => item.id === maHoaDon);
    if (!order) return;
    selectedHistoryOrder.value = order;
    await loadOrderHistory(order);
    isHistoryModalOpen.value = true;
  };

  //2 cái này để chuyển trạng thái món đã lên(tích vô ô là đã lên)
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

    //Hủy hóa đơn
const openCancelModal = async (order) => {
    if (!order) return;

    if (!order.tienCocRaw || order.tienCocRaw === 0) {
      // Hiện Popup hỏi xác nhận
      Swal.fire({
        title: 'Xác nhận hủy đơn?',
        text: `Bạn có chắc chắn muốn hủy hóa đơn ${order.id}?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#8b0000', 
        cancelButtonColor: '#6c757d',
        confirmButtonText: 'Đồng ý hủy',
        cancelButtonText: 'Thoát'
      }).then(async (result) => {
        if (result.isConfirmed) {
          const payload = {
            idHoaDon: order.dbId,
            idNhanVien: 1, // Lấy ID nhân viên sau
            hanhDong: "Hủy hóa đơn",
            lyDoThucHien: "Hủy hóa đơn thường (Không có cọc)",
            isLoiDoQuan: false,
            trangThaiLichSuTruocDo: 1, 
            thoiGianThucHien: new Date().toISOString(),
          };

          try {
            await BeHuyHoaDon(payload);
            
            // Hiện thông báo thành công (Góc phải)
            Toast.fire({
              icon: 'success',
              title: 'Thành công',
              text: 'Hủy hóa đơn thành công!'
            });

            if (filters.value.search || filters.value.status !== "Tất cả") {
              performSearch(false);
            } else {
              fetchOrders();
            }
            if (selectedOrder.value && selectedOrder.value.dbId === order.dbId) {
              await handleViewDetail(order.dbId);
            }
          } catch (error) {
            console.error(error);
            // Hiện thông báo lỗi (Góc phải)
            Toast.fire({
              icon: 'error',
              title: 'Lỗi',
              text: error.response?.data?.message || error.message
            });
          }
        }
      });
      return; 
    }

    const hasDeposit = true;
    const bookingTime = dayjs(order.thoiGianDat);
    const now = dayjs();

    const diffHours = bookingTime.diff(now, "hour", true);
    const diffMinutes = bookingTime.diff(now, "minute", true);

    let isWarning = false;
    let message = "";

    // LOGIC CẢNH BÁO
    if (diffHours < 0) {
      if (Math.abs(diffHours) <= 0.25) {
        isWarning = true;
        const minutesLate = Math.round(Math.abs(diffMinutes));
        message = `Đã quá giờ đặt ${minutesLate} phút. (Vẫn trong 15p giữ bàn). Khách hủy => MẤT CỌC.`;
      } else {
        isWarning = true;
        message = `Đã quá thời gian giữ bàn (15p). Đơn lẽ ra đã bị hủy tự động.`;
      }
    } else if (diffHours < 2) {
      isWarning = true;
      let timeDisplay = "";
      if (diffHours < 1) {
        const minutesLeft = Math.round(diffMinutes);
        timeDisplay = minutesLeft <= 1 ? "vài giây" : `${minutesLeft} phút`;
      } else {
        timeDisplay = `${diffHours.toFixed(1)} giờ`;
      }
      message = `Chỉ còn ${timeDisplay} nữa là đến giờ đặt (Quy định: Hủy trước 2h). Khách hủy lúc này sẽ MẤT CỌC.`;
    }

    if(diffHours > 24) isWarning = false;

    const isSafe = !isWarning;

    cancelModalState.value = {
      isOpen: true,
      orderData: order,
      isDeposit: hasDeposit,
      isWarning: isWarning,
      warningMessage: message,
      reason: "",
      isSafe: isSafe,
    };
  };

const confirmCancelOrder = async (loiDoAi) => {
    // Validate lý do
    if (!cancelModalState.value.reason.trim()) {
        Toast.fire({
            icon: 'warning',
            title: 'Thiếu thông tin',
            text: 'Vui lòng nhập lý do hủy đơn!'
        });
        return;
    }

    const isLoiDoQuan = loiDoAi === 'quan';
    const payload = {
        idHoaDon: cancelModalState.value.orderData.dbId,
        idNhanVien: 1, 
        hanhDong: "Hủy hóa đơn",
        lyDoThucHien: cancelModalState.value.reason,
        isLoiDoQuan: isLoiDoQuan,
        thoiGianThucHien: new Date().toISOString(),
    };

    try {
        await BeHuyHoaDon(payload);
        
        // Đóng Modal Vue
        closeCancelModal();
        
        // Hiện Toast thông báo thành công
        Toast.fire({
            icon: 'success',
            title: 'Thành công',
            text: isLoiDoQuan ? 'Đã hủy & Hoàn tiền cọc!' : 'Đã hủy & Giữ tiền cọc!'
        });

        if (filters.value.search || filters.value.status !== "Tất cả") {
             performSearch(false);
        } else {
             fetchOrders();
        }
        if (selectedOrder.value && selectedOrder.value.dbId === payload.idHoaDon) {
            await handleViewDetail(payload.idHoaDon);
        }

    } catch (error) {
        console.error(error);
        Toast.fire({
            icon: 'error',
            title: 'Lỗi',
            text: error.response?.data?.message || 'Không thể hủy hóa đơn'
        });
    }
  };

  const closeCancelModal = () => {
    cancelModalState.value.isOpen = false;
  };

  onMounted(async () => {
    await fetchConfig();
    await performSearch(true);
    // await fetchOrders();
    // console.log("Giờ hiện tại:", dayjs().format("HH:mm:ss DD/MM/YYYY"));
  });

  return {
    currentPage,
    totalPages,
    pageSize,       
    totalElements,
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
    cancelModalState,
    openCancelModal,
    confirmCancelOrder,
    closeCancelModal,
  };
}
