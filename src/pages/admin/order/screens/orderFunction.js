import { ref, onMounted, watch, computed } from "vue";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import Swal from "sweetalert2";
import html2pdf from "html2pdf.js";
dayjs.locale("vi");

import {
  BeGetAllHoaDon,
  BeGetChiTietHoaDon,
  BeGetThamSoHeThong,
  BeSearchHoaDon,
  BeGetLichSuHoaDon,
  //BeUpdateMonDaLen,
  //BeUpdateTatCaDaLen,
  BeHuyHoaDon,
  BeGetHoaDonById,
  BeGetLichSuThanhToan,
  BeHoanTatHoaDon,
} from "./orderService";

const today = dayjs().format("YYYY-MM-DD");
const currentPage = ref(0);
const pageSize = ref(5);
const filters = ref({
  search: "",
  status: "Tất cả",
  fromDate: today,
  toDate: "",
});

export function useOrderManager() {
  const totalPages = ref(0);
  const totalElements = ref(0);

  const selectedOrder = ref(null);
  const orderList = ref([]);
  const orderDetails = ref([]);
  const currentVAT = ref(10);
  const configHoldTime = ref(15);
  const configCancelLimit = ref(2);

  const isHistoryModalOpen = ref(false);
  const selectedHistoryOrder = ref(null);
  const historyEvents = ref([]);
  const paymentHistory = ref([]);

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

  const invoiceDate = computed(() => {
    if (
      selectedOrder.value?.trangThai === "Hoàn thành" &&
      paymentHistory.value &&
      paymentHistory.value.length > 0
    ) {
      const lastPayment = paymentHistory.value[paymentHistory.value.length - 1];
      return dayjs(lastPayment.ngayThanhToan).format("HH:mm - DD/MM/YYYY");
    }
    return dayjs().format("HH:mm - DD/MM/YYYY");
  });

  const cancelModalState = ref({
    isOpen: false,
    orderData: null,
    isDeposit: false,
    isWarning: false,
    warningMessage: "",
    reason: "",
    isSafe: false,
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
      0: "Vừa tạo",
      1: "Chờ cọc",
      2: "Đã cọc",
      3: "Đã xác nhận",
      4: "Khách đã đến",
      5: "Chờ thanh toán",
      6: "Đã thanh toán",
      7: "Hoàn thành",
      8: "Đã hủy",
      9: "Đã hoàn tiền",
    };
    return statuses[statusInt] || "Không xác định";
  };

  const handlePrintOrder = async (orderId) => {
    // 1. Lấy phần tử DOM chứa mẫu hóa đơn
    const element = document.getElementById("invoice-template");

    if (!element) {
      Swal.fire("Lỗi", "Không tìm thấy mẫu hóa đơn để in!", "error");
      return;
    }

    // 2. Cấu hình cho file PDF
    const opt = {
      margin: 5, 
      filename: `HoaDon_${orderId}_${dayjs().format("DDMMYYYY")}.pdf`,
      image: { type: "jpeg", quality: 0.98 },
      html2canvas: { scale: 2, useCORS: true },
      jsPDF: { unit: "mm", format: "a4", orientation: "portrait" },
    };

    // 3. Thực hiện xuất PDF
    // Hiển thị loading nhẹ hoặc thông báo
    const Toast = Swal.mixin({
      toast: true,
      position: "top-end",
      showConfirmButton: false,
      timer: 2000,
    });
    Toast.fire({ icon: "info", title: "Đang tạo file PDF..." });

    try {
      await html2pdf().set(opt).from(element).save();
    } catch (err) {
      console.error("Lỗi xuất PDF:", err);
      Swal.fire("Lỗi", "Không thể xuất file PDF", "error");
    }
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
        tongTien: formatCurrency(item.tongTienChuaGiam),
        tongTienRaw: item.tongTienThanhToan,
        tongTienHangRaw: item.tongTienChuaGiam || 0,
        soTienDaGiam: item.soTienDaGiam || 0,
        tienCoc: formatCurrency(item.tienCoc),
        tienCocRaw: item.tienCoc || 0,
        tienHoanTra: formatCurrency(item.tienHoanTra),
        trangThai: mapStatus(item.trangThaiHoaDon),
        trangThaiCode: item.trangThaiHoaDon,
        ngayTao: formatDate(item.thoiGianTao),
        vatApDung: item.vatApDung,
        thoiGianDat: item.thoiGianDat || item.thoiGianTao,
      };
    });
  };

  const fetchConfig = async () => {
    try {
      const config = await BeGetThamSoHeThong();
      if (config) {
        currentVAT.value = Number(config.VAT || 10);
        configHoldTime.value = Number(config.MAX_HOLD_TIME || 15);
        configCancelLimit.value = Number(config.CANCEL_LIMIT_HOURS || 2);
      }
    } catch (error) {
      console.error("Lỗi lấy tham số hệ thống, dùng mặc định", error);
      currentVAT.value = 10;
      configHoldTime.value = 15;
      configCancelLimit.value = 2;
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
        "Vừa tạo": 0,
        "Chờ cọc": 1,
        "Đã cọc": 2,
        "Đã xác nhận": 3,
        "Khách đã đến": 4,
        "Chờ thanh toán": 5,
        "Đã thanh toán": 6,
        "Hoàn thành": 7,
        "Đã hủy": 8,
        "Đã hoàn tiền": 9,
      };

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
        pageSize.value,
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
        else if (act.includes("thanh toán") || act.includes("hoàn tiền"))
          type = "payment";

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
          trangThaiMoi: item.trangThaiMoi,
          trangThaiTruocDo: item.trangThaiTruocDo,
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
          trangThai: mapStatus(invoiceInfo.trangThaiHoaDon),
          trangThaiCode: invoiceInfo.trangThaiHoaDon,
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
  // const handleUpdateMonDaLen = async (idChiTietHD) => {
  //   try {
  //     await BeUpdateMonDaLen(idChiTietHD);
  //     if (selectedOrder.value) {
  //       orderDetails.value = await BeGetChiTietHoaDon(selectedOrder.value.dbId);
  //     }
  //   } catch (error) {
  //     alert("Lỗi cập nhật món!");
  //   }
  // };

  // const handleUpdateTatCaDaLen = async () => {
  //   if (!selectedOrder.value) return;
  //   try {
  //     await BeUpdateTatCaDaLen(selectedOrder.value.dbId);
  //     orderDetails.value = await BeGetChiTietHoaDon(selectedOrder.value.dbId);
  //     alert("Đã xác nhận tất cả món!");
  //   } catch (error) {
  //     alert("Cập nhật thất bại!");
  //   }
  // };

  //Hủy hóa đơn
  const openCancelModal = async (order) => {
    if (!order) return;

    if (!order.tienCocRaw || order.tienCocRaw === 0) {
      // Hiện Popup hỏi xác nhận
      Swal.fire({
        title: "Xác nhận hủy đơn?",
        text: `Bạn có chắc chắn muốn hủy hóa đơn ${order.id}?`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#8b0000",
        cancelButtonColor: "#6c757d",
        confirmButtonText: "Đồng ý hủy",
        cancelButtonText: "Thoát",
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
              icon: "success",
              title: "Thành công",
              text: "Hủy hóa đơn thành công!",
            });

            if (filters.value.search || filters.value.status !== "Tất cả") {
              performSearch(false);
            } else {
              fetchOrders();
            }
            if (
              selectedOrder.value &&
              selectedOrder.value.dbId === order.dbId
            ) {
              await handleViewDetail(order.dbId);
            }
          } catch (error) {
            console.error(error);
            // Hiện thông báo lỗi (Góc phải)
            Toast.fire({
              icon: "error",
              title: "Lỗi",
              text: error.response?.data?.message || error.message,
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

    const holdTimeHours = configHoldTime.value / 60;
    const cancelLimitHours = configCancelLimit.value;

    // LOGIC CẢNH BÁO
    if (diffHours < 0) {
      if (Math.abs(diffHours) <= holdTimeHours) {
        isWarning = true;
        const minutesLate = Math.round(Math.abs(diffMinutes));
        message = `Đã quá giờ đặt ${minutesLate} phút. (Vẫn trong ${configHoldTime.value}p giữ bàn). Khách hủy => MẤT CỌC.`;
      } else {
        isWarning = true;
        message = `Đã quá thời gian giữ bàn (${configHoldTime.value}p). Đơn lẽ ra đã bị hủy tự động.`;
      }
    } else if (diffHours < cancelLimitHours) {
      isWarning = true;
      let timeDisplay = "";
      if (diffHours < 1) {
        const minutesLeft = Math.round(diffMinutes);
        timeDisplay = minutesLeft <= 1 ? "vài giây" : `${minutesLeft} phút`;
      } else {
        timeDisplay = `${diffHours.toFixed(1)} giờ`;
      }
      message = `Chỉ còn ${timeDisplay} nữa là đến giờ đặt (Quy định: Hủy trước ${configCancelLimit.value}h). Khách hủy lúc này sẽ MẤT CỌC.`;
    }

    if (diffHours > 24) isWarning = false;

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
        icon: "warning",
        title: "Thiếu thông tin",
        text: "Vui lòng nhập lý do hủy đơn!",
      });
      return;
    }

    const isLoiDoQuan = loiDoAi === "quan";
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
        icon: "success",
        title: "Thành công",
        text: isLoiDoQuan
          ? "Đã hủy & Hoàn tiền cọc!"
          : "Đã hủy & Giữ tiền cọc!",
      });

      if (filters.value.search || filters.value.status !== "Tất cả") {
        performSearch(false);
      } else {
        fetchOrders();
      }
      if (
        selectedOrder.value &&
        selectedOrder.value.dbId === payload.idHoaDon
      ) {
        await handleViewDetail(payload.idHoaDon);
      }
    } catch (error) {
      console.error(error);
      Toast.fire({
        icon: "error",
        title: "Lỗi",
        text: error.response?.data?.message || "Không thể hủy hóa đơn",
      });
    }
  };

  // Hoàn tất hóa đơn (Chốt sổ & Giải phóng bàn)
  const handleHoanTatHoaDon = async (orderId) => {
    const idToComplete = orderId || selectedOrder.value?.dbId;
    if (!idToComplete) return;

    Swal.fire({
      title: "Khách đã ra về?",
      text: "Hóa đơn sẽ được chuyển sang Hoàn thành và bàn sẽ được dọn trống.",
      icon: "question",
      showCancelButton: true,
      confirmButtonColor: "#8b0000",
      cancelButtonColor: "#6c757d",
      confirmButtonText: "Đồng ý chốt",
      cancelButtonText: "Hủy",
    }).then(async (result) => {
      if (result.isConfirmed) {
        try {
          const payload = {
            idHoaDon: idToComplete,
            idNhanVien: 1, // Sau này thay bằng ID user đang đăng nhập nhé
            hanhDong: "Khách ra về - Hoàn tất hóa đơn",
            thoiGianThucHien: new Date().toISOString(),
          };

          await BeHoanTatHoaDon(payload);

          // Bắn thông báo góc phải
          Toast.fire({
            icon: "success",
            title: "Thành công",
            text: "Hóa đơn đã hoàn tất, bàn đã trống!",
          });

          // Tải lại danh sách bên ngoài
          if (filters.value.search || filters.value.status !== "Tất cả") {
            performSearch(false);
          } else {
            fetchOrders();
          }

          // Cập nhật lại màn hình chi tiết nếu đang mở
          if (
            selectedOrder.value &&
            selectedOrder.value.dbId === idToComplete
          ) {
            await handleViewDetail(idToComplete);
          }
        } catch (error) {
          console.error(error);
          Toast.fire({
            icon: "error",
            title: "Lỗi",
            text: error.response?.data?.message || "Không thể hoàn tất hóa đơn",
          });
        }
      }
    });
  };

  const closeCancelModal = () => {
    cancelModalState.value.isOpen = false;
  };

  onMounted(async () => {
    await fetchConfig();
    await performSearch(false);
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
    configHoldTime,
    configCancelLimit,
    isHistoryModalOpen,
    selectedHistoryOrder,
    historyEvents,
    invoiceDate,
    formatDate,
    handleViewDetail,
    handleViewHistory,
    closeHistoryModal: () => {
      isHistoryModalOpen.value = false;
    },
    handleSearch,
    handleReset,
    handlePageChange,
    handlePrintOrder,
    // handleUpdateMonDaLen,
    // handleUpdateTatCaDaLen,
    cancelModalState,
    openCancelModal,
    confirmCancelOrder,
    closeCancelModal,
    handleHoanTatHoaDon,
  };
}
