<script setup>
import { onMounted, computed, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useOrderManager } from "./orderFunction";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import relativeTime from "dayjs/plugin/relativeTime";
import logoUrl from "@/assets/images/logo_upscaled.jpg";
import Swal from "sweetalert2";
import axiosClient from "@/services/axiosClient";

dayjs.locale("vi");
dayjs.extend(relativeTime);
const route = useRoute();
const router = useRouter();

const {
  selectedOrder,
  orderDetails,
  // currentVAT,
  configHoldTime,
  configCancelLimit,
  handleViewDetail,
  openCancelModal,
  confirmCancelOrder,
  closeCancelModal,
  cancelModalState,
  handlePrintOrder,
  historyEvents,
  paymentHistory,
  invoiceDate,
  formatDate,
  handleHoanTatHoaDon,
  currentStaffId,
} = useOrderManager();

const formatMoney = (value) => {
  if (value === undefined || value === null) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const formatDateTime = (dateString) => {
  if (!dateString) return "---";
  return dayjs(dateString).format("HH:mm - DD/MM/YYYY");
};

const getEventColor = (type) => {
  if (type === "create") return "bg-warning";
  if (type === "delete") return "bg-danger";
  if (type === "payment") return "bg-warning";
  return "bg-secondary";
};

// Tính tiền
const subTotal = computed(() => selectedOrder.value?.tongTienHangRaw || 0);
const discount = computed(() => selectedOrder.value?.soTienDaGiam || 0);
const rawDeposit = computed(() => selectedOrder.value?.tienCocRaw || 0);
const deposit = computed(() => {
  const code = selectedOrder.value?.trangThaiCode;

  // 1. Nếu đang ở trạng thái Vừa tạo (0) hoặc Chờ cọc (1) -> Chắc chắn chưa có cọc
  if (code === 0 || code === 1) {
    return 0;
  }

  // 2. Nếu đã Hủy (8) hoặc Hoàn tiền (9), kiểm tra xem trước đó khách đã đóng cọc chưa
  if (code === 8 || code === 9) {
    const historyCodes =
      historyEvents.value
        ?.map((e) => e.trangThaiMoi)
        .filter((c) => c !== null && c !== undefined && c < 8) || [];

    const maxCodeBeforeCancel =
      historyCodes.length > 0 ? Math.max(...historyCodes) : 0;

    // Nếu trước khi hủy mà trạng thái đi được xa nhất chỉ là 0 hoặc 1 -> Khách chưa đóng cọc
    if (maxCodeBeforeCancel < 2) {
      return 0;
    }
  }

  return rawDeposit.value;
});
const finalTotal = computed(() => {
  const baseTotal = selectedOrder.value?.tongTienRaw || 0;
  const code = selectedOrder.value?.trangThaiCode;

  // THÊM MỚI: Nếu trạng thái là Đã hủy (8) hoặc Hoàn tiền (9) -> Thành tiền = 0
  if (code === 8 || code === 9) {
    return 0;
  }

  if (code === 0 || code === 1) {
    return baseTotal + rawDeposit.value;
  }
  return baseTotal;
});

// const taxAmount = computed(() => {
//   if (
//     selectedOrder.value?.vatApDung !== undefined &&
//     selectedOrder.value?.vatApDung !== null
//   ) {
//     // Ép kiểu về Number để đảm bảo an toàn tính toán
//     return Number(selectedOrder.value.vatApDung);
//   }
//   return 0; // Mặc định là 0 nếu không có dữ liệu
// });

const isReadOnly = computed(() => {
  const code = selectedOrder.value?.trangThaiCode;
  return code === 7 || code === 8 || code === 9 || code === 10;
});

const hasServedDish = computed(() =>
  orderDetails.value?.some((item) => item.trangThaiCode === 2),
);

const getPaymentTypeInfo = (type) => {
  switch (type) {
    case 1:
      return { label: "Thanh toán", color: "success", icon: "fa-check" };
    case 2:
      return { label: "Đặt cọc", color: "warning", icon: "fa-coins" };
    case 3:
      return {
        label: "Hoàn tiền",
        color: "danger",
        icon: "fa-arrow-rotate-left",
      };
    default:
      return { label: "Giao dịch", color: "secondary", icon: "fa-money-bill" };
  }
};

// --- LOGIC STEPPER MỚI (XỬ LÝ BỎ QUA CỌC & LẤY LỊCH SỬ HỦY) ---
const currentStatusCode = computed(
  () => selectedOrder.value?.trangThaiCode ?? -1,
);
const isCancelledOrRefunded = computed(
  () =>
    currentStatusCode.value === 8 ||
    currentStatusCode.value === 9 ||
    currentStatusCode.value === 10,
);

const visibleSteps = computed(() => {
  if (!selectedOrder.value || currentStatusCode.value === -1) return [];

  // 1. Định nghĩa gốc (happy path từ 0 đến 7)
  let baseSteps = [
    { code: 0, label: "Vừa tạo", icon: "fa-file-pen" },
    { code: 1, label: "Chờ cọc", icon: "fa-clock" },
    { code: 2, label: "Đã cọc", icon: "fa-money-bill-transfer" },
    { code: 3, label: "Xác nhận", icon: "fa-circle-check" },
    { code: 4, label: "Khách đến", icon: "fa-utensils" },
    { code: 5, label: "Chờ TT", icon: "fa-file-invoice-dollar" },
    { code: 6, label: "Đã TT", icon: "fa-hand-holding-dollar" },
    { code: 7, label: "Hoàn thành", icon: "fa-flag-checkered" },
  ];

  // 2. Lọc bỏ cọc nếu không có tiền cọc
  if (!selectedOrder.value.tienCocRaw || selectedOrder.value.tienCocRaw === 0) {
    baseSteps = baseSteps.filter((step) => step.code !== 1 && step.code !== 2);
  }

  // 3. Tìm trạng thái bình thường cuối cùng trước khi bị Hủy/Hoàn tiền
  let targetCode = currentStatusCode.value;
  if (isCancelledOrRefunded.value) {
    const historyCodes = historyEvents.value
      .map((e) => e.trangThaiMoi)
      .filter((code) => code !== null && code !== undefined && code < 8);

    targetCode = historyCodes.length > 0 ? Math.max(...historyCodes) : 0;
  }

  // 4. Đưa các bước bình thường vào danh sách hiển thị
  const stepsToRender = baseSteps
    .filter((step) => step.code <= targetCode)
    .map((step) => ({ ...step, isErrorStep: false }));

  // 5. XỬ LÝ RIÊNG CHO HỦY / HOÀN TIỀN (8, 9, 10)
  if (isCancelledOrRefunded.value) {
    // TRƯỜNG HỢP ĐẶC BIỆT: Nếu là trạng thái 10 (Đã hoàn), phải hiện thêm bước 9 (Chờ hoàn) trước nó
    if (currentStatusCode.value === 10) {
      stepsToRender.push({
        code: 9,
        label: "Chờ hoàn tiền",
        icon: "fa-clock-rotate-left",
        isErrorStep: false, 
      });
    }

    // Xác định thông tin cho bước hiện tại (8, 9 hoặc 10)
    let stepLabel = "Đã hủy";
    let stepIcon = "fa-ban";

    if (currentStatusCode.value === 9) {
      stepLabel = "Chờ hoàn tiền";
      stepIcon = "fa-clock-rotate-left";
    } else if (currentStatusCode.value === 10) {
      stepLabel = "Đã hoàn tiền";
      stepIcon = "fa-arrow-rotate-left";
    }

    // Đẩy trạng thái cuối cùng vào (đang active, hiện màu đỏ đậm)
    stepsToRender.push({
      code: currentStatusCode.value,
      label: stepLabel,
      icon: stepIcon,
      isErrorStep: true,
    });
  }

  return stepsToRender;
});
// --------------------------------------------------------------

const onBack = () => router.push({ name: "orderManager" });

onMounted(async () => {
  const orderDbId = route.params.id;

  if (orderDbId && orderDbId !== "undefined") {
    console.log("Đang lấy chi tiết cho ID:", orderDbId);
    await handleViewDetail(orderDbId);
  } else {
    console.error("Không tìm thấy ID hóa đơn trên URL!");
    router.push({ name: "orderManager" });
  }
});

const handleConfirmOrder = async (idHoaDon) => {
  console.log("👉 BƯỚC 1: Đã bấm nút! ID Hóa đơn nhận được là:", idHoaDon);

  if (!idHoaDon) {
    Swal.fire("Lỗi", "Không tìm thấy ID hóa đơn để xác nhận!", "error");
    return;
  }

  const danhSachBan = selectedOrder.value?.danhSachTenBan || [];

  if (danhSachBan.length === 0) {
    Swal.fire({
      icon: "warning",
      title: "Chưa xếp bàn!",
      text: "Vui lòng xếp bàn cho khách trước khi Xác nhận và Gửi mail.",
      confirmButtonColor: "#8b0000",
      confirmButtonText: "Đồng ý",
    });
    return;
  }

  try {
    console.log("👉 BƯỚC 2: Chuẩn bị bật Swal xác nhận");
    const confirm = await Swal.fire({
      title: "Xác nhận đơn hàng?",
      html: "Trạng thái hóa đơn sẽ chuyển sang <b>Xác nhận (3)</b>, phiếu đặt bàn sang <b>(1)</b> và hệ thống sẽ <b>gửi Email</b> thông báo cho khách hàng.",
      icon: "question",
      showCancelButton: true,
      confirmButtonColor: "#8b0000",
      cancelButtonColor: "#6c757d",
      confirmButtonText:
        '<i class="fa-solid fa-paper-plane me-1"></i> Đồng ý gửi',
      cancelButtonText: "Hủy bỏ",
    });

    console.log("👉 BƯỚC 3: Người dùng đã chọn:", confirm.isConfirmed);
    if (!confirm.isConfirmed) return;

    Swal.fire({
      title: "Đang xử lý & Gửi email...",
      text: "Vui lòng không đóng trình duyệt lúc này.",
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading(),
    });

    console.log("👉 BƯỚC 4: Chuẩn bị gọi API bằng axiosClient...");

    // Kiểm tra xem axiosClient có bị undefined không
    if (!axiosClient) {
      console.error("🚨 LỖI: axiosClient chưa được import hoặc bị undefined!");
    }

    const response = await axiosClient.put(
      `/hoa-don-thanh-toan/xac-nhan-va-gui-mail/${idHoaDon}`,
    );
    console.log("👉 BƯỚC 5: API chạy thành công! Kết quả:", response);

    Swal.fire({
      icon: "success",
      title: "Hoàn tất!",
      text: "Đã xác nhận đơn và gửi email thành công.",
      timer: 2000,
      showConfirmButton: false,
    });

    await handleViewDetail(idHoaDon);
  } catch (error) {
    console.error("🚨 BƯỚC LỖI: Cú pháp hoặc API bị lỗi!", error);
    Swal.fire({
      icon: "error",
      title: "Lỗi xử lý",
      text:
        error.response?.data?.message ||
        error.message ||
        "Không thể xác nhận đơn lúc này.",
    });
  }
};

const handleXacNhanHoanTien = async () => {
  const dbId = selectedOrder.value?.dbId; // Ưu tiên hiển thị tiền hoàn trả, nếu không có thì hiện tiền cọc
  const staffId = currentStaffId.value;
  const soTien =
    selectedOrder.value?.tienHoanTra || selectedOrder.value?.tienCoc;

  const confirm = await Swal.fire({
    title: "Xác nhận đã hoàn tiền?",
    html: `Xác nhận đã chuyển khoản hoàn số tiền <b class="text-danger fs-5">${soTien}</b> cho khách hàng?`,
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: "#8b0000",
    cancelButtonColor: "#6c757d",
    confirmButtonText: '<i class="fa-solid fa-check me-1"></i> Đã hoàn tiền',
    cancelButtonText: "Hủy",
  });

  if (!confirm.isConfirmed) return;

  Swal.fire({
    title: "Đang cập nhật...",
    allowOutsideClick: false,
    didOpen: () => Swal.showLoading(),
  });

  try {
    // LƯU Ý: Chỉnh sửa lại chuỗi URL API này cho khớp với cấu hình Backend của team bạn
    await axiosClient.put(
      `/hoa-don-thanh-toan/xac-nhan-hoan-tien/${dbId}?idNhanVien=${staffId}`,
    );

    Swal.fire({
      icon: "success",
      title: "Thành công!",
      text: "Đã lưu lịch sử hoàn tiền thành công.",
      timer: 2000,
      showConfirmButton: false,
    }); // Reload lại chi tiết hóa đơn

    await handleViewDetail(dbId);
  } catch (error) {
    console.error(error);
    Swal.fire({
      icon: "error",
      title: "Lỗi",
      text: error.response?.data?.message || "Không thể xác nhận lúc này.",
    });
  }
};

const cannotCancel = computed(() => {
  if (isReadOnly.value) return true;

  const code = selectedOrder.value?.trangThaiCode;

  // Khách đã check-in -> không cho hủy
  if (code >= 4) return true;

  // Nếu đã lên món -> không cho hủy
  if (hasServedDish.value) return true;

  return false;
});

const cannotPrint = computed(() => {
  // Hóa đơn Đã hủy hoặc Hoàn tiền -> Không cho in
  const code = selectedOrder.value?.trangThaiCode;
  if (code === 8 || code === 9) return true;

  // Danh sách món rỗng hoặc tất cả các món đều bị hủy (Code 0) -> Không cho in
  if (!orderDetails.value || orderDetails.value.length === 0) return true;

  // Kiểm tra xem có ít nhất 1 món có trạng thái khác 0 không
  const hasValidItems = orderDetails.value.some(
    (item) => item.trangThaiCode !== 0,
  );
  if (!hasValidItems) return true;

  return false;
});

// --- LOGIC GỘP MÓN DÀNH RIÊNG CHO IN HÓA ĐƠN ---
const printOrderItems = computed(() => {
  if (!orderDetails.value || orderDetails.value.length === 0) return [];

  // 1. Lọc bỏ món đã hủy (trạng thái 0) để không in ra bill
  const validItems = orderDetails.value.filter(item => item.trangThaiCode !== 0);

  const groups = validItems.reduce((acc, item) => {
    // Gộp theo Mã món 
    const key = `${item.maMon}-${item.donGia}`;

    if (!acc[key]) {
      acc[key] = { ...item };
    } else {
      acc[key].soLuong += item.soLuong;
      // Ép kiểu Number để tính toán chính xác
      acc[key].thanhTien = Number(acc[key].thanhTien) + Number(item.thanhTien);
      
      // Gộp ghi chú (tránh trùng lặp)
      if (item.ghiChu && item.ghiChu.trim() !== "") {
        if (!acc[key].ghiChu) {
          acc[key].ghiChu = item.ghiChu;
        } else if (!acc[key].ghiChu.includes(item.ghiChu)) {
          acc[key].ghiChu += `, ${item.ghiChu}`;
        }
      }
    }
    return acc;
  }, {});

  return Object.values(groups);
});
</script>

<template>
  <div class="d-flex bg-light min-vh-100">
    <main class="flex-grow-1 p-4 main-offset">
      <h1 class="page-title mb-4">Quản lý hóa đơn chi tiết</h1>

      <!-- Thong tin chung -->

      <div
        class="card border-0 shadow-sm mb-4"
        style="border-radius: 15px; overflow: hidden"
      >
        <div class="card border-0 mb-0">
          <div class="card-header bg-white border-bottom py-3">
            <span class="fw-bold"
              ><i class="fa-solid fa-clock-rotate-left me-2"></i>Trạng thái hóa
              đơn:
            </span>
            <span class="text-custom-red fw-bold ms-2">{{
              selectedOrder?.id
            }}</span>
          </div>
          <div class="card-body py-5">
            <div class="stepper-scroll-container">
              <div class="stepper-wrapper d-flex position-relative">
                <div
                  class="stepper-line-background"
                  :style="{ width: `${(visibleSteps.length - 1) * 130}px` }"
                ></div>
                <div
                  class="stepper-line-progress"
                  :style="{
                    width: `${(visibleSteps.length - 1) * 130}px`,
                    backgroundColor: isCancelledOrRefunded
                      ? '#dc3545'
                      : '#8b0000',
                  }"
                ></div>

                <div
                  v-for="(step, index) in visibleSteps"
                  :key="index"
                  class="step-item d-flex flex-column align-items-center position-relative"
                >
                  <div
                    class="step-circle d-flex align-items-center justify-content-center rounded-circle mb-2 shadow-sm"
                    :class="{
                      error: step.isErrorStep,
                      active:
                        index === visibleSteps.length - 1 && !step.isErrorStep,
                      completed:
                        index < visibleSteps.length - 1 && !step.isErrorStep,
                    }"
                  >
                    <i
                      v-if="
                        index < visibleSteps.length - 1 && !step.isErrorStep
                      "
                      class="fa-solid fa-check fw-bold"
                    ></i>
                    <i v-else class="fa-solid" :class="step.icon"></i>
                  </div>

                  <div
                    class="step-label text-uppercase fw-bold"
                    :class="{
                      'text-danger': step.isErrorStep,
                      'text-warning': !step.isErrorStep,
                    }"
                  >
                    {{ step.label }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="card-body">
          <div class="row g-4">
            <div class="col-md">
              <label class="d-block text-muted small mb-1"
                >Trạng thái hóa đơn</label
              >
              <p class="mb-0 fw-bold">
                {{ selectedOrder?.trangThai }}
              </p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Khách hàng</label>
              <p class="mb-0 fw-medium">
                {{ selectedOrder?.khachHang || "Khách vãng lai" }}
              </p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Số điện thoại</label>
              <p class="mb-0 fw-medium">{{ selectedOrder?.sdt || "---" }}</p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Danh sách bàn</label>
              <div
                class="d-flex flex-wrap gap-1 align-items-center"
                style="max-height: 80px; overflow-y: auto; padding-right: 5px"
              >
                <template v-if="selectedOrder?.danhSachTenBan?.length > 0">
                  <span
                    v-for="(tenBan, bIdx) in selectedOrder.danhSachTenBan"
                    :key="bIdx"
                    class="badge table-badge-item"
                  >
                    {{ tenBan }}
                  </span>
                </template>

                <span
                  v-else
                  class="text-danger fw-bold"
                  style="font-size: 0.9rem"
                >
                  <i class="fas fa-exclamation-circle me-1"></i> Chưa xếp bàn
                </span>
              </div>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Tiền cọc</label>
              <p class="mb-0 fw-bold">{{ selectedOrder?.tienCoc }}</p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Thời gian tạo</label>
              <p class="mb-0 fw-medium">{{ selectedOrder?.ngayTao }}</p>
            </div>
            <div class="col-md">
              <label class="d-block text-muted small mb-1">Thời gian đến</label>
              <p class="mb-0 fw-bold">
                {{ formatDateTime(selectedOrder?.thoiGianDat) }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Bảng món -->

      <div
        class="card border-0 shadow-sm mb-4"
        style="border-radius: 15px; overflow: hidden"
      >
        <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
          <i class="fas fa-utensils" style="color: silver"></i> Thông tin món đã
          đặt
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead class="bg-custom-red text-white">
                <tr>
                  <th class="text-center py-3" style="width: 5%">STT</th>
                  <th class="py-3" style="width: 10%">MÃ MÓN</th>
                  <th class="py-3" style="width: 30%">TÊN MÓN ĂN</th>
                  <th class="text-center py-3" style="width: 10%">SỐ LƯỢNG</th>
                  <th class="text-end py-3" style="width: 15%">ĐƠN GIÁ</th>
                  <th class="text-end py-3" style="width: 15%">THÀNH TIỀN</th>
                  <th class="text-center py-3" style="width: 15%">
                    TRẠNG THÁI
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(item, index) in orderDetails" :key="index">
                  <td class="fw-bold text-center">{{ index + 1 }}</td>
                  <td class="fw-medium">{{ item.maMon }}</td>
                  <td>
                    <div class="d-flex flex-column">
                      <span class="fw-medium">{{ item.tenMon }}</span>
                      <small
                        v-if="item.ghiChu"
                        class="text-custom-red"
                        style="font-size: 0.8rem"
                        >Ghi chú: {{ item.ghiChu }}</small
                      >
                    </div>
                  </td>
                  <td class="text-center">{{ item.soLuong }}</td>
                  <td class="text-end">{{ formatMoney(item.donGia) }}</td>
                  <td class="text-end fw-bold">
                    {{ formatMoney(item.thanhTien) }}
                  </td>
                  <td class="text-center">
                    <span
                      class="badge px-2 py-1"
                      :class="
                        item.trangThaiCode === 2
                          ? 'bg-success-subtle text-success'
                          : item.trangThaiCode === 1
                            ? 'bg-warning-subtle text-warning'
                            : 'bg-custom-red-subtle text-custom-red'
                      "
                    >
                      {{ item.trangThaiText }}
                    </span>
                  </td>
                </tr>

                <tr v-if="!orderDetails || orderDetails.length === 0">
                  <td colspan="6" class="text-center py-5 text-muted">
                    <div class="d-flex flex-column align-items-center">
                      <i class="fa-solid fa-utensils fs-3 mb-2 opacity-25"></i>
                      <span>Chưa có món ăn nào được gọi</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <div class="row g-4 mb-4 align-items-stretch">
        <div class="col-md-6 d-flex flex-column gap-4">
          <div
            class="card border-0 shadow-sm flex-grow-1"
            style="border-radius: 15px; overflow: hidden"
          >
            <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
              <i class="far fa-clock"></i> Lịch sử hóa đơn
            </div>
            <div
              class="card-body p-3 h-100"
              style="max-height: 310px; overflow-y: auto"
            >
              <div
                v-if="!historyEvents || historyEvents.length === 0"
                class="text-center text-muted py-4"
              >
                <small>Chưa có lịch sử ghi nhận</small>
              </div>
              <div v-else>
                <div
                  v-for="(event, index) in historyEvents"
                  :key="index"
                  class="d-flex mb-3 position-relative"
                >
                  <div class="flex-shrink-0 mt-1">
                    <div
                      class="rounded-circle"
                      :class="getEventColor(event.type)"
                      style="width: 12px; height: 12px"
                    ></div>
                  </div>
                  <div class="ms-3">
                    <div class="fw-bold small">{{ event.title }}</div>
                    <div class="text-muted" style="font-size: 0.8rem">
                      {{ event.time }} -
                      <span class="fw-medium">{{ event.user }}</span>
                    </div>
                    <div
                      v-if="event.detail"
                      class="text-secondary fst-italic mt-1"
                      style="font-size: 0.85rem"
                    >
                      "{{ event.detail }}"
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div
            v-if="paymentHistory && paymentHistory.length > 0"
            class="card border-0 shadow-sm mt-4"
            style="border-radius: 15px; overflow: hidden"
          >
            <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
              <i class="fas fa-credit-card" style="color: cornflowerblue"></i>
              Lịch sử thanh toán
            </div>
            <div class="card-body p-3">
              <div
                v-for="(pay, index) in paymentHistory"
                :key="index"
                class="d-flex align-items-start mb-3 pb-3 border-bottom-dashed last-no-border"
              >
                <div class="me-3">
                  <div
                    class="rounded-circle d-flex align-items-center justify-content-center"
                    :class="[
                      `bg-${getPaymentTypeInfo(pay.loaiGiaoDich).color}-subtle`,
                      `text-${getPaymentTypeInfo(pay.loaiGiaoDich).color}`,
                    ]"
                    style="width: 32px; height: 32px"
                  >
                    <i
                      class="fa-solid small"
                      :class="getPaymentTypeInfo(pay.loaiGiaoDich).icon"
                    ></i>
                  </div>
                </div>

                <div class="flex-grow-1">
                  <div class="d-flex justify-content-between">
                    <span
                      class="fw-bold"
                      :class="`text-${getPaymentTypeInfo(pay.loaiGiaoDich).color}`"
                    >
                      <template v-if="pay.loaiGiaoDich === 3">- </template>
                      {{ formatMoney(pay.soTienThanhToan) }}
                    </span>
                    <span class="text-muted small">{{
                      formatDateTime(pay.ngayThanhToan)
                    }}</span>
                  </div>
                  <div
                    class="d-flex justify-content-between align-items-center mt-1"
                  >
                    <span class="small text-dark">
                      {{ pay.tenPhuongThuc }}
                      <span
                        class="badge ms-1"
                        :class="`bg-${getPaymentTypeInfo(pay.loaiGiaoDich).color}`"
                      >
                        {{ getPaymentTypeInfo(pay.loaiGiaoDich).label }}
                      </span>
                    </span>
                  </div>
                  <div class="small text-muted fst-italic mt-1">
                    Mã GD: {{ pay.maGiaoDich || "---" }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-md-6">
          <div
            class="card border-0 shadow-sm h-100"
            style="border-radius: 15px; overflow: hidden"
          >
            <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
              <i class="fas fa-sack-dollar" style="color: orange"></i> Tổng kết
              đơn hàng
            </div>
            <div
              class="card-body p-4 d-flex flex-column"
              :style="{ opacity: isCancelledOrRefunded ? 0.5 : 1 }"
            >
              <div class="d-flex justify-content-between mb-1">
                <span class="text-muted fw-medium">Tổng tiền hàng:</span>
                <span class="fw-bold">{{ formatMoney(subTotal) }}</span>
              </div>
              <div class="text-end text-muted small mb-3 fst-italic">
                Giá trị sản phẩm
              </div>

              <hr
                class="border-secondary border-opacity-25 border-dashed my-3"
              />

              <!-- <div class="d-flex justify-content-between mb-3">
                <span class="text-muted fw-medium">Thuế VAT:</span>
                <span class="fw-bold">{{ formatMoney(taxAmount) }}</span>
              </div> -->

              <div v-if="discount > 0">
                <div class="d-flex justify-content-between mb-1">
                  <span class="text-muted fw-medium">Giảm giá:</span>
                  <span class="fw-bold text-custom-red"
                    >({{ formatMoney(discount) }})</span
                  >
                </div>

                <div class="d-flex justify-content-end mb-3">
                  <div
                    v-if="selectedOrder?.maPhieuGiamGia"
                    class="badge rounded-pill bg-danger-subtle text-custom-red border border-danger border-opacity-25 px-3 py-1 d-inline-flex align-items-center"
                  >
                    <i class="fa-solid fa-ticket-simple me-2"></i>
                    <span>{{ selectedOrder.maPhieuGiamGia }}</span>
                  </div>
                  <div v-else class="text-muted small fst-italic">
                    Khuyến mãi áp dụng
                  </div>
                </div>
              </div>

              <hr class="border-secondary border-opacity-25 my-3" />

              <div
                v-if="deposit > 0 && subTotal > 0"
                class="d-flex justify-content-between mb-3"
              >
                <span class="text-muted fw-medium">Đã đặt cọc:</span>
                <span class="fw-bold text-custom-red"
                  >- {{ formatMoney(deposit) }}</span
                >
              </div>

              <div
                class="d-flex justify-content-between align-items-center mb-1"
              >
                <span class="fs-5 fw-bold text-dark">Thành tiền:</span>
                <span class="fs-4 fw-bold text-custom-red">{{
                  formatMoney(finalTotal)
                }}</span>
              </div>
              <div class="text-end text-muted small fst-italic">
                Số tiền phải thanh toán
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="d-flex justify-content-between mt-4 pb-5">
        <div class="d-flex gap-2">
          <button
            v-if="!cannotCancel"
            class="btn btn-outline-custom px-4 py-2 fw-medium"
            @click="openCancelModal(selectedOrder)"
          >
            Hủy hóa đơn
          </button>
          <button
            v-if="selectedOrder?.trangThaiCode === 9"
            class="btn btn-warning px-4 py-2 fw-medium text-dark shadow-sm border-warning"
            @click="handleXacNhanHoanTien"
          >
            <i class="fa-solid fa-money-bill-transfer me-2"></i> Xác nhận đã
            hoàn tiền
          </button>
        </div>

        <div class="d-flex gap-2">
          <button
            class="btn btn-white border px-4 py-2 fw-medium"
            @click="onBack"
          >
            Quay lại
          </button>

          <button
            v-if="selectedOrder?.trangThaiCode === 6"
            class="btn btn-custom px-4 py-2 fw-medium text-white shadow-sm"
            @click="handleHoanTatHoaDon(selectedOrder?.dbId)"
          >
            <i class="fa-solid fa-check-double me-2"></i>Hoàn thành
          </button>

          <button
            class="btn btn-print px-4 py-2 fw-medium text-white"
            @click="handlePrintOrder(selectedOrder?.id)"
            :disabled="cannotPrint"
          >
            In hóa đơn
          </button>
        </div>
      </div>

      <!-- Thông báo hủy hóa đơn -->

      <div
        v-if="cancelModalState.isOpen"
        class="modal-backdrop fade show"
        style="z-index: 1050"
      ></div>
      <div
        v-if="cancelModalState.isOpen"
        class="modal fade show d-block"
        tabindex="-1"
        style="z-index: 1055"
      >
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <!-- Không cọc -->

            <div class="modal-header bg-custom-red text-white">
              <h5 class="modal-title">
                Xác nhận hủy hóa đơn: {{ cancelModalState.orderData?.id }}
              </h5>
              <button
                type="button"
                class="btn-close btn-close-white"
                @click="closeCancelModal"
              ></button>
            </div>

            <!-- Có cọc -->

            <div class="modal-body">
              <div
                v-if="cancelModalState.isDeposit"
                class="alert alert-brand d-flex align-items-center mb-3 py-2"
              >
                <i class="fas fa-info-circle me-2 fs-5"></i>
                <div class="small">
                  <strong>Quy định hiện tại:</strong><br />
                  - Thời gian giữ bàn: <b>{{ configHoldTime }} phút</b>.<br />
                  - Hủy trước <b>{{ configCancelLimit }} giờ</b>: Được hoàn 100%
                  cọc.
                </div>
              </div>
              <div
                v-if="cancelModalState.isWarning"
                class="alert alert-warning d-flex align-items-start border-warning mb-3"
              >
                <i
                  class="fas fa-exclamation-triangle mt-1 me-2 text-warning"
                ></i>
                <div>
                  <strong>Cảnh báo hủy đơn:</strong><br />
                  {{ cancelModalState.warningMessage }}
                </div>
              </div>

              <div
                v-if="cancelModalState.isDeposit"
                class="mb-3 p-2 bg-light rounded border"
              >
                <span
                  >Khách đã cọc:
                  <strong class="text-custom-red">{{
                    cancelModalState.orderData?.tienCoc
                  }}</strong></span
                >
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold"
                  >Lý do hủy đơn <span class="text-custom-red">*</span></label
                >
                <textarea
                  v-model="cancelModalState.reason"
                  class="form-control"
                  rows="3"
                  placeholder="Nhập chi tiết lý do (VD: Khách đổi ý, Hết bàn, ...)"
                ></textarea>
              </div>
            </div>

            <div class="modal-footer d-flex justify-content-between">
              <button
                type="button"
                class="btn btn-secondary"
                @click="closeCancelModal"
              >
                Đóng
              </button>

              <div class="d-flex gap-2">
                <template v-if="!cancelModalState.isDeposit">
                  <button
                    type="button"
                    class="btn btn-custom px-4"
                    @click="confirmCancelOrder('khach')"
                  >
                    Xác nhận Hủy đơn
                  </button>
                </template>

                <template v-else>
                  <template v-if="!cancelModalState.isSafe">
                    <button
                      type="button"
                      class="btn btn-outline-custom"
                      @click="confirmCancelOrder('khach')"
                      title="Khách vi phạm -> Mất cọc"
                    >
                      Lỗi do Khách <br />
                      <small style="font-size: 0.7rem">(Không hoàn cọc)</small>
                    </button>

                    <button
                      type="button"
                      class="btn btn-custom"
                      @click="confirmCancelOrder('quan')"
                      title="Lỗi quán -> Hoàn cọc"
                    >
                      Lỗi do Quán <br />
                      <small style="font-size: 0.7rem">(Hoàn cọc 100%)</small>
                    </button>
                  </template>

                  <template v-else>
                    <div
                      class="d-flex align-items-center me-2 text-warning small fw-bold"
                    >
                      <i class="fa-solid fa-check-circle me-1"></i>
                      {{
                        cancelModalState.orderData?.trangThaiCode === 2
                          ? "Đơn chưa xác nhận"
                          : "Hủy đúng hạn"
                      }}
                    </div>
                    <button
                      type="button"
                      class="btn btn-custom px-4"
                      @click="confirmCancelOrder('quan')"
                      :title="
                        cancelModalState.orderData?.trangThaiCode === 2
                          ? 'Chưa xác nhận -> Hoàn tiền'
                          : 'Hủy đúng quy định -> Hoàn tiền'
                      "
                    >
                      Xác nhận Hủy đơn <br />
                      <small style="font-size: 0.7rem">(Hoàn cọc 100%)</small>
                    </button>
                  </template>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>

  <!-- Hóa đơn in -->
  <div style="position: absolute; left: -9999px; top: -9999px">
    <div id="invoice-template" class="invoice-wrapper">
      <div class="main-frame">
        <div class="d-flex justify-content-between align-items-start mb-4">
          <div
            class="d-flex flex-column align-items-center"
            style="width: 150px"
          >
            <img :src="logoUrl" alt="Logo" class="invoice-logo mb-2" />
            <span class="fw-bold text-custom-red text-uppercase small"
              >CozyPot</span
            >
          </div>

          <div class="text-center flex-grow-1 pt-2">
            <h1
              class="fw-bold text-uppercase mb-1"
              style="font-size: 26px; letter-spacing: 2px"
            >
              HÓA ĐƠN
            </h1>
            <p class="mb-0 small fst-italic">Mã HĐ: #{{ selectedOrder?.id }}</p>
          </div>

          <div class="text-end" style="width: 150px">
            <img
              src="https://api.qrserver.com/v1/create-qr-code/?size=100x100&data=CozyPot-Invoice"
              alt="QR"
              style="
                width: 80px;
                height: 80px;
                border: 1px solid #000;
                padding: 2px;
              "
            />
          </div>
        </div>

        <div class="info-section mb-4">
          <div class="row g-0">
            <div class="col-6 pe-3">
              <div class="border-bottom border-dark fw-bold mb-2 pb-1">
                Hóa đơn cho:
              </div>
              <p class="mb-1 fw-bold">
                {{ selectedOrder?.khachHang || "Khách lẻ" }}
              </p>
              <p class="mb-0">SĐT: {{ selectedOrder?.sdt || "---" }}</p>
            </div>
            <div class="col-6 ps-3">
              <div class="border-bottom border-dark fw-bold mb-2 pb-1 text-end">
                Chi tiết:
              </div>

              <p class="mb-1 text-end">Ngày in: {{ invoiceDate }}</p>

              <p class="mb-1 text-end" v-if="selectedOrder?.thoiGianNhanBan">
                Giờ đến: {{ formatDateTime(selectedOrder?.thoiGianNhanBan) }}
              </p>

              <p
                v-if="selectedOrder?.tienCocRaw > 0"
                class="mb-1 text-end text-muted fst-italic small"
              >
                (Ngày đặt: {{ formatDate(selectedOrder?.thoiGianDat) }})
              </p>

              <p class="mb-0 text-end">
                Bàn:
                <span class="fw-bold">
                  {{
                    selectedOrder?.danhSachTenBan?.length > 0
                      ? selectedOrder.danhSachTenBan.join(", ")
                      : "Chưa xếp bàn"
                  }}
                </span>
              </p>
            </div>
          </div>
        </div>

        <h6 class="fw-bold mb-2 text-uppercase">Thông tin chi tiết</h6>

        <table class="cozypot-print-table mb-4">
          <thead>
            <tr>
              <th class="text-center" style="width: 50px">STT</th>
              <th class="text-start">Tên dịch vụ / Món ăn</th>
              <th class="text-end" style="width: 120px">Đơn giá</th>
              <th class="text-center" style="width: 70px">SL</th>
              <th class="text-end" style="width: 130px">Tổng tiền</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in printOrderItems" :key="index">
              <td class="text-center fw-bold">{{ index + 1 }}</td>
              <td>
                <span class="fw-medium">{{ item.tenMon }}</span>
                <div v-if="item.idSetLau" class="small text-muted fst-italic">
                  (Set lẩu)
                </div>
              </td>
              <td class="text-end">
                {{ formatMoney(item.donGia).replace(" ₫", "") }}
              </td>
              <td class="text-center fw-bold">{{ item.soLuong }}</td>
              <td class="text-end fw-bold">
                {{ formatMoney(item.thanhTien).replace(" ₫", "") }}
              </td>
            </tr>
            <tr v-if="orderDetails.length < 5">
              <td colspan="5" style="height: 30px"></td>
            </tr>
          </tbody>
        </table>

        <div class="row">
          <div class="col-12">
            <h6 class="fw-bold mb-2 text-uppercase">Chi phí khác & Tổng kết</h6>
            <table class="cozypot-print-table">
              <tbody>
                <tr>
                  <td class="fw-medium" style="width: 70%">Tổng tiền hàng:</td>
                  <td class="text-end fw-bold">{{ formatMoney(subTotal) }}</td>
                </tr>
                <!-- <tr>
                  <td class="fw-medium" style="width: 70%">Thuế VAT:</td>
                  <td class="text-end fw-bold">{{ formatMoney(taxAmount) }}</td>
                </tr> -->
                <tr v-if="discount > 0">
                  <td class="fw-medium">
                    Giảm giá / Voucher:
                    <div v-if="selectedOrder?.maPhieuGiamGia" class="mt-1">
                      <span
                        style="
                          font-size: 12px;
                          background-color: #f8d7da;
                          border: 1px solid #f5c6cb;
                          color: #721c24;
                          padding: 2px 8px;
                          border-radius: 4px;
                          display: inline-block;
                        "
                      >
                        Mã HĐ: {{ selectedOrder.maPhieuGiamGia }}
                      </span>
                    </div>
                  </td>
                  <td class="text-end fw-bold text-danger align-middle">
                    - {{ formatMoney(discount) }}
                  </td>
                </tr>
                <tr v-if="deposit > 0">
                  <td class="fw-medium">Đã đặt cọc:</td>
                  <td class="text-end fw-bold text-success">
                    - {{ formatMoney(deposit) }}
                  </td>
                </tr>

                <tr style="background-color: #f8f9fa">
                  <td class="align-middle">
                    <span class="fw-bold fs-6 text-uppercase"
                      >Tổng thanh toán:</span
                    >
                    <div class="small fst-italic text-muted mt-1">
                      (Đã bao tổng tiền và các khoản giảm trừ)
                    </div>
                  </td>
                  <td class="text-end align-middle">
                    <span class="fw-bold fs-4 text-custom-red">{{
                      formatMoney(finalTotal)
                    }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <div class="text-center mt-5 pt-4">
          <p class="mb-1 fw-bold fst-italic">
            Cảm ơn quý khách và hẹn gặp lại!
          </p>
          <div class="small text-muted">
            <p class="mb-0">COZYPOT RESTAURANT | 123 Đường FPT, Hà Nội</p>
            <p class="mb-0">
              Hotline: 1900 888 888 | Email: contact@cozypot.vn
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.text-custom-red {
  color: #8b0000 !important;
}
.bg-custom-red {
  background-color: #8b0000 !important;
}
.border-bottom-red {
  border-bottom: 2px solid #8b0000 !important;
}
.page-title {
  color: #8b0000;
  font-size: 24px;
  font-weight: bold;
}
.btn-custom,
.btn-print {
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%) !important;
  color: white !important;
  border: none !important;
  transition: 0.2s;
}

.btn-custom:hover,
.btn-print:hover {
  opacity: 0.9;
  color: white !important;
}
.btn-outline-custom {
  color: #8b0000 !important;
  border: 1px solid #8b0000 !important;
  background-color: transparent !important;
  transition: 0.2s;
}

.btn-outline-custom:hover {
  background-color: #8b0000 !important;
  color: white !important;
}
.btn-close-white {
  filter: invert(1) grayscale(100%) brightness(200%);
}

.bg-success-subtle {
  background-color: #d4edda !important;
}
.bg-warning-subtle {
  background-color: #fff3cd !important;
}
.bg-danger-subtle {
  background-color: #f8d7da !important;
}
.text-warning {
  color: #856404 !important;
}
.alert-brand {
  background-color: rgba(139, 0, 0, 0.1);
  border: 1px solid rgba(139, 0, 0, 0.3);
  color: #8b0000;
}
.alert-brand i {
  color: #8b0000 !important;
}

.border-dashed {
  border-style: dashed !important;
}
.last-no-border:last-child {
  border-bottom: none !important;
  padding-bottom: 0 !important;
  margin-bottom: 0 !important;
}
.border-bottom-dashed {
  border-bottom: 1px dashed #eee;
}
.modal {
  background-color: rgba(0, 0, 0, 0.5);
}

/* Style bảng chính  */
.table-responsive {
  overflow-x: auto;
}
.table thead th {
  vertical-align: middle;
}

/* Thanh trang thái */
.stepper-wrapper {
  margin-top: 10px;
  margin-bottom: 10px;
}
.step-item {
  min-width: 100px;
}
.step-circle {
  background-color: #fff;
  transition: all 0.4s ease-in-out;
}
.position-absolute {
  transition: all 0.4s ease-in-out;
}

/* Hóa đơn in */
.invoice-wrapper {
  width: 760px;
  background: white;
  padding: 20px;
  box-sizing: border-box;
}
.main-frame {
  border: 2px solid #000;
  border-radius: 15px;
  padding: 30px;
  min-height: 900px;
  position: relative;
  font-family: "Times New Roman", serif;
  color: #000;
}
.invoice-logo {
  width: 80px;
  height: 80px;
  object-fit: contain;
}
/* --- BẢNG IN HÓA ĐƠN ĐỘC LẬP (ÉP BUỘC OVERRIDE TOÀN BỘ) --- */
.cozypot-print-table {
  width: 100% !important;
  border-collapse: collapse !important;
  margin-bottom: 20px !important;
  background-color: transparent !important;
}

.cozypot-print-table thead,
.cozypot-print-table thead tr,
.cozypot-print-table tbody,
.cozypot-print-table tbody tr {
  background: transparent !important;
  background-color: transparent !important;
  border-radius: 0 !important;
  box-shadow: none !important;
}

.cozypot-print-table th,
.cozypot-print-table td {
  border: 1px solid #000 !important;
  padding: 8px 10px !important;
  vertical-align: middle !important;
  font-size: 14px !important;
  color: #000 !important;
  border-radius: 0 !important;
}

.cozypot-print-table thead th {
  background-color: #f0f0f0 !important;
  border-top: 2px solid #000 !important;
  border-bottom: 2px solid #000 !important;
  text-transform: uppercase !important;
  font-size: 13px !important;
  font-weight: bold !important;
  -webkit-print-color-adjust: exact !important;
  print-color-adjust: exact !important;
}
.table-bordered > :not(caption) > * {
  border-width: 1px;
}

/* --- CẬP NHẬT CSS CHO STEPPER --- */
.stepper-scroll-container {
  overflow-x: auto;
  padding: 10px 20px;
}
.stepper-wrapper {
  position: relative;
  display: flex;
}
.step-item {
  width: 130px; /* Cố định khoảng cách giữa các khối */
  z-index: 3;
}
.stepper-line-background,
.stepper-line-progress {
  position: absolute;
  top: 23px;
  left: 65px; /* Xuất phát từ giữa hình tròn đầu tiên (130 / 2) */
  height: 4px;
}
.stepper-line-background {
  background-color: #e9ecef;
  z-index: 1;
}
.stepper-line-progress {
  z-index: 2;
  transition: width 0.5s ease-in-out;
}

/* Style cho các vòng tròn */
.step-circle {
  width: 50px;
  height: 50px;
  font-size: 1.3rem;
  background-color: #fff;
  border: 3px solid #e9ecef;
  color: #adb5bd;
  transition: all 0.4s;
}
.step-circle.completed {
  background-color: #8b0000;
  border-color: #8b0000;
  color: white;
}
.step-circle.active {
  border-color: #8b0000;
  color: #8b0000;
  transform: scale(1.2);
  box-shadow: 0 0 10px rgba(139, 0, 0, 0.3);
}
.step-circle.error {
  background-color: #dc3545 !important;
  border-color: #dc3545 !important;
  color: white !important;
  transform: scale(1.3);
  box-shadow: 0 0 12px rgba(220, 53, 69, 0.5);
}
.step-label {
  font-size: 13px;
  text-align: center;
  letter-spacing: 0.5px;
  margin-top: 6px;
}
.table-badge-item {
  background-color: #fff5f5;
  color: #8b0000;
  border: 1px solid #ffcccc;
  font-weight: 600;
  font-size: 0.85rem;
  padding: 4px 10px;
  border-radius: 6px;
}
</style>
