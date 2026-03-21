<script setup>
import { ref, computed, onMounted } from "vue";
import Swal from "sweetalert2";
import axiosClient from "@/services/axiosClient";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import { useAuthStore } from "@/pages/guest/authentication/authenticationServices/authenticationService";
import { useRouter } from "vue-router";

dayjs.locale("vi");
const authStore = useAuthStore();
const router = useRouter();

// --- STATE QUẢN LÝ ---
const isLoggedIn = computed(
  () => !!authStore.token && authStore.role === "USER",
);
const searchCode = ref("");
const searchSdt = ref("");
const isLoadingSearch = ref(false);
const userHistoryList = ref([]);
const isLoadingHistory = ref(false);
const displayOrderData = ref(null);
const configHoldTime = ref(15);
const configCancelLimit = ref(2);

const fetchConfig = async () => {
  try {
    const response = await axiosClient.get("/tham-so-he-thong/all-map");
    const config = response.data;
    configHoldTime.value = Number(config.THOI_GIAN_GIU_BAN || 15);
    configCancelLimit.value = Number(config.THOI_GIAN_HUY_HOAN_COC || 120) / 60; 
    
  } catch (error) {
    console.error("Lỗi lấy config, dùng mặc định", error);
    configHoldTime.value = 15;
    configCancelLimit.value = 2;
  }
};

// --- FORMAT FORMATTERS ---
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

// --- HELPERS TỪ ADMIN ---
const getEventColor = (type) => {
  if (type === "create") return "bg-warning";
  if (type === "delete") return "bg-danger";
  if (type === "payment") return "bg-success";
  if (type === "add") return "bg-primary";
  return "bg-secondary";
};

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

const getStatusBadge = (code) => {
  switch (code) {
    case 0:
      return { class: "bg-secondary", text: "Vừa tạo" };
    case 1:
      return { class: "bg-warning text-dark", text: "Chờ cọc" };
    case 2:
      return { class: "bg-primary", text: "Đã cọc" };
    case 3:
      return { class: "bg-info text-dark", text: "Đã xác nhận" };
    case 4:
    case 5:
    case 6:
      return { class: "bg-success", text: "Đang phục vụ" };
    case 7:
      return { class: "bg-success", text: "Hoàn thành" };
    case 8:
      return { class: "bg-danger", text: "Đã hủy" };
    case 9:
      return { class: "bg-dark", text: "Đã hoàn tiền" };
    default:
      return { class: "bg-secondary", text: "Không rõ" };
  }
};

// --- LIFECYCLE ---
onMounted(async () => {
  await fetchConfig();
  if (isLoggedIn.value) await fetchUserHistory();
});

// --- API CALLS ---
const fetchUserHistory = async () => {
  isLoadingHistory.value = true;
  try {
    const response = await axiosClient.get(`/lich-su-dat-ban/ca-nhan`);
    userHistoryList.value = response.data;
  } catch (error) {
    console.error(error);
  } finally {
    isLoadingHistory.value = false;
  }
};

const handleSearch = async () => {
  if (!searchCode.value || searchCode.value.trim() === "")
    return Swal.fire("Lỗi", "Vui lòng nhập Mã phiếu đặt bàn!", "warning");

  if (!searchSdt.value || searchSdt.value.trim() === "")
    return Swal.fire("Lỗi", "Vui lòng nhập Số điện thoại đặt bàn!", "warning");

  isLoadingSearch.value = true;
  displayOrderData.value = null;

  try {
    const response = await axiosClient.get(`/lich-su-dat-ban/tra-cuu`, {
      params: { maPhieu: searchCode.value.trim(), sdt: searchSdt.value.trim() },
    });
    if (response.data) displayOrderData.value = response.data;
  } catch (error) {
    Swal.fire(
      "Không tìm thấy",
      error.response?.data?.message || "Kiểm tra lại mã phiếu và số điện thoại",
      "error",
    );
  } finally {
    isLoadingSearch.value = false;
  }
};

const viewHistoryDetail = async (maPhieu) => {
  Swal.fire({
    title: "Đang tải...",
    allowOutsideClick: false,
    didOpen: () => Swal.showLoading(),
  });
  try {
    const response = await axiosClient.get(
      `/lich-su-dat-ban/chi-tiet/${maPhieu}`,
      {
        params: { maPhieu: maPhieu },
      },
    );
    displayOrderData.value = response.data;
    Swal.close();
  } catch (error) {
    Swal.fire("Lỗi", "Không thể tải chi tiết đơn hàng", "error");
  }
};

const closeDetail = () => (displayOrderData.value = null);

// --- HỦY ĐƠN (Tìm đến dòng 142 và thay toàn bộ bằng cục này) ---
const handleCancelOrder = async (idPhieu, tienCoc, currentStatus) => {
  // 1. Nếu chưa cọc (Status 0, 1) -> Hủy nhanh như cũ
  if (!tienCoc || tienCoc === 0 || currentStatus < 2) {
    const { isConfirmed, value: reason } = await Swal.fire({
      title: `<h4 class="fw-bold mb-0">Xác nhận hủy lịch</h4>`,
      html: `
        <div class="text-start mb-3">
          <p class="mb-2">Bạn có chắc chắn muốn hủy lịch đặt bàn này?</p>
          <label class="form-label fw-bold small">Lý do hủy đơn <span class="text-danger">*</span></label>
          <textarea id="swal-cancel-reason" class="form-control" rows="3" placeholder="VD: Bận việc đột xuất..."></textarea>
        </div>
      `,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: "#d33",
      cancelButtonColor: "#6c757d",
      confirmButtonText: "Đồng ý hủy",
      cancelButtonText: "Thoát",
      customClass: { popup: "rounded-4" },
      preConfirm: () => {
        const reasonText = document.getElementById("swal-cancel-reason").value;
        if (!reasonText || reasonText.trim() === "") {
          Swal.showValidationMessage("Vui lòng nhập lý do hủy!");
          return false;
        }
        return reasonText.trim();
      },
    });
    if (isConfirmed) executeCancel(idPhieu, reason);
    return;
  }

  // 2. NẾU CÓ CỌC -> TÍNH TOÁN DỰA TRÊN THAM SỐ ĐỘNG VÀ GIỜ ĐẶT (thoiGianDat)
  // Lưu ý: thoiGianDat lấy từ displayOrderData (cục chi tiết m vừa mở ra xem)
  const bookingTime = dayjs(displayOrderData.value.thoiGianDat);
  const now = dayjs();

  const diffMinutes = bookingTime.diff(now, "minute");
  const diffHours = bookingTime.diff(now, "hour", true);

  const holdTime = configHoldTime.value; // Mặc định 15p
  const cancelLimit = configCancelLimit.value; // Mặc định 2h

  let isWarning = false;
  let isSafe = false;
  let message = "";

  if (diffMinutes <= 0) {
    // Đã đến hoặc quá giờ
    isWarning = true;
    message = `Đã quá giờ nhận bàn (<b>${bookingTime.format("HH:mm")}</b>). Hủy lúc này m sẽ <b>MẤT CỌC</b>.`;
  } else if (diffHours < cancelLimit) {
    // Hủy sát giờ (Dưới 2 tiếng)
    isWarning = true;
    let timeRemaining =
      diffMinutes < 60
        ? `<b>${diffMinutes} phút</b>`
        : `<b>${Math.floor(diffMinutes / 60)} giờ ${diffMinutes % 60} phút</b>`;

    message = `Chỉ còn ${timeRemaining} nữa là đến giờ nhận bàn (<b>${bookingTime.format("HH:mm")}</b>). Quy định hủy trước ${cancelLimit}h, nên bạn sẽ <b>MẤT CỌC</b>.`;
  } else {
    // Hủy an toàn (Trên 2 tiếng)
    isSafe = true;
    message = `Giờ đặt bàn là <b>${bookingTime.format("HH:mm")}</b>. Bạn đang hủy trước hạn nên sẽ được <b>Hoàn 100% cọc</b>.`;
  }

  // 3. HIỂN THỊ POPUP
  const { isConfirmed, value: reason } = await Swal.fire({
    title: `<h4 class="fw-bold mb-0">Xác nhận hủy đơn</h4>`,
    html: `
      <div class="alert alert-brand d-flex align-items-center mb-3 py-2 text-start small" style="background-color: rgba(139, 0, 0, 0.1); border: 1px solid rgba(139, 0, 0, 0.3); color: #8b0000;">
        <i class="fas fa-info-circle me-2 fs-5"></i>
        <div>
          <strong>Quy định hiện tại:</strong><br />
          - Thời gian giữ bàn: <b>${holdTime} phút</b>.<br />
          - Hủy trước <b>${cancelLimit} giờ</b>: Hoàn 100% cọc.
        </div>
      </div>
      
      <div class="alert ${isWarning ? "alert-warning border-warning" : "alert-success border-success"} d-flex align-items-start mb-3 text-start small">
        <i class="fas ${isWarning ? "fa-exclamation-triangle text-warning" : "fa-check-circle text-success"} mt-1 me-2"></i>
        <div><strong>Cảnh báo:</strong><br />${message}</div>
      </div>
      
      <div class="mb-3 text-start p-2 bg-light rounded border small">
        Tiền đã cọc: <strong class="text-custom-red fs-6">${formatMoney(tienCoc)}</strong>
      </div>
      
      <div class="text-start">
        <label class="form-label fw-bold small">Lý do hủy đơn <span class="text-danger">*</span></label>
        <textarea id="swal-cancel-reason" class="form-control" rows="3" placeholder="Vui lòng nhập lý do..."></textarea>
      </div>
    `,
    icon: "warning",
    showCancelButton: true,
    confirmButtonColor: isSafe ? "#198754" : "#d33",
    confirmButtonText: isSafe
      ? "Xác nhận Hủy & Hoàn cọc"
      : "Chấp nhận Mất cọc & Hủy",
    cancelButtonText: "Thoát",
    customClass: { popup: "rounded-4" },
    preConfirm: () => {
      const reasonText = document.getElementById("swal-cancel-reason").value;
      if (!reasonText || reasonText.trim() === "") {
        Swal.showValidationMessage("Vui lòng nhập lý do hủy lịch!");
        return false;
      }
      return reasonText.trim();
    },
  });

  if (isConfirmed) executeCancel(idPhieu, reason);
};

// Hàm phụ để gọi API Hủy (Tránh lặp code)
const executeCancel = async (idPhieu, reason) => {
  Swal.fire({
    title: "Đang xử lý...",
    allowOutsideClick: false,
    didOpen: () => Swal.showLoading(),
  });
  try {
    await axiosClient.put(`/lich-su-dat-ban/huy-don/${idPhieu}`, {
      lyDo: reason,
    });
    Swal.fire(
      "Đã hủy!",
      "Lịch đặt bàn của bạn đã được hủy thành công.",
      "success",
    );
    displayOrderData.value = null;
    if (isLoggedIn.value) await fetchUserHistory();
  } catch (error) {
    Swal.fire(
      "Lỗi",
      error.response?.data?.message || "Lỗi khi hủy đơn",
      "error",
    );
  }
};

// --- STEPPER LOGIC ---
const currentStatusCode = computed(
  () => displayOrderData.value?.trangThaiHoaDon ?? -1,
);
const isCancelledOrRefunded = computed(
  () => currentStatusCode.value === 8 || currentStatusCode.value === 9,
);
const canCancel = computed(
  () => currentStatusCode.value >= 0 && currentStatusCode.value <= 3,
);

const visibleSteps = computed(() => {
  if (!displayOrderData.value) return [];
  let baseSteps = [
    { code: 0, label: "Vừa tạo", icon: "fa-file-pen" },
    { code: 1, label: "Chờ cọc", icon: "fa-clock" },
    { code: 2, label: "Đã cọc", icon: "fa-money-bill-transfer" },
    { code: 3, label: "Xác nhận", icon: "fa-circle-check" },
    { code: 4, label: "Khách đến", icon: "fa-utensils" },
    { code: 7, label: "Hoàn thành", icon: "fa-flag-checkered" },
  ];

  if (!displayOrderData.value.tienCoc || displayOrderData.value.tienCoc === 0) {
    baseSteps = baseSteps.filter((s) => s.code !== 1 && s.code !== 2);
  }

  let targetCode = currentStatusCode.value;
  if (isCancelledOrRefunded.value) {
    const historyCodes = (displayOrderData.value.lichSuHoaDon || [])
      .map((e) => e.trangThaiMoi)
      .filter((code) => code !== null && code !== undefined && code < 8);
    targetCode = historyCodes.length > 0 ? Math.max(...historyCodes) : 0;
  }

  const stepsToRender = baseSteps
    .filter((step) => step.code <= targetCode)
    .map((step) => ({ ...step, isErrorStep: false }));

  if (isCancelledOrRefunded.value) {
    stepsToRender.push({
      code: currentStatusCode.value,
      label: currentStatusCode.value === 9 ? "Đã hoàn tiền" : "Đã hủy",
      icon: currentStatusCode.value === 9 ? "fa-arrow-rotate-left" : "fa-ban",
      isErrorStep: true,
    });
  }
  return stepsToRender;
});

const handlePayDeposit = async (idPhieu) => {
  Swal.fire({
    title: "Đang kết nối cổng thanh toán...",
    allowOutsideClick: false,
    didOpen: () => Swal.showLoading(),
  });

  try {
    const response = await axiosClient.get(
      `/vnpay/create-payment-deposit/${idPhieu}`,
    );

    if (response.data && response.data.url) {
      window.location.href = response.data.url;
    } else {
      Swal.fire("Lỗi", "Không nhận được đường dẫn thanh toán.", "error");
    }
  } catch (error) {
    Swal.fire(
      "Lỗi",
      error.response?.data?.message ||
        "Không thể kết nối đến máy chủ thanh toán.",
      "error",
    );
  }
};

const isOrderDead = computed(() => {
  const status = displayOrderData.value?.trangThaiHoaDon;
  return status === 8 || status === 9;
});

// 1. Tính tổng tiền hàng (Nếu đơn đã hủy thì trả về 0)
const calculatedSubTotal = computed(() => {
  if (!displayOrderData.value || isOrderDead.value) return 0;
  const sum = (displayOrderData.value.chiTiet || []).reduce(
    (acc, item) => acc + item.thanhTien,
    0,
  );
  return sum > 0 ? sum : displayOrderData.value.tongTienChuaGiam || 0;
});

// 2. Tiền thuế (Bây giờ lấy trực tiếp từ trường vatApDung vì nó lưu tổng tiền VAT)
const calculatedTax = computed(() => {
  if (!displayOrderData.value || isOrderDead.value) return 0;
  return displayOrderData.value.vatApDung || 0;
});

// 3. Tiền cọc THỰC TẾ đã thu (Chỉ tính khi trạng thái >= 2 và không phải đơn đã hủy)
const actualDepositPaid = computed(() => {
  if (!displayOrderData.value || isOrderDead.value) return 0;
  const status = displayOrderData.value.trangThaiHoaDon;
  // Trạng thái 0 hoặc 1 coi như chưa thu tiền cọc thực tế
  if (status < 2) return 0;
  return displayOrderData.value.tienCoc || 0;
});

// 4. Thành tiền cuối cùng
const finalBalance = computed(() => {
  if (isOrderDead.value) return 0;
  return (
    calculatedSubTotal.value + calculatedTax.value - actualDepositPaid.value
  );
});
</script>

<template>
  <div class="bg-light min-vh-100 py-5">
    <div class="container">
      <h2
        v-if="!displayOrderData"
        class="text-center fw-bold text-custom-red mb-4 text-uppercase"
      >
        {{ isLoggedIn ? "Lịch sử đặt bàn của bạn" : "Tra cứu lịch đặt bàn" }}
      </h2>

      <template v-if="!isLoggedIn && !displayOrderData">
        <div
          class="card border-0 shadow-sm rounded-4 p-4 mb-5 mx-auto"
          style="max-width: 500px"
        >
          <div class="row g-3">
            <div class="col-md-12 text-center mb-2">
              <i
                class="fa-solid fa-ticket fs-1 text-custom-red opacity-75 mb-3"
              ></i>
              <p class="text-muted small">
                Nhập mã phiếu đặt bàn bạn nhận được trên màn hình hoặc qua Email
                để tra cứu chi tiết.
              </p>
            </div>
            <div class="col-md-12">
              <input
                v-model="searchCode"
                type="text"
                class="form-control form-control-lg text-center fw-bold"
                placeholder="VD: PDB0001"
                @keyup.enter="handleSearch"
              />
            </div>
            <div class="col-md-12">
              <input
                v-model="searchSdt"
                type="text"
                class="form-control form-control-lg text-center fw-bold"
                placeholder="Số điện thoại đặt bàn"
                @keyup.enter="handleSearch"
              />
            </div>
            <div class="col-12 mt-3 text-center">
              <button
                class="btn btn-custom px-5 py-2 fw-bold w-100"
                @click="handleSearch"
                :disabled="isLoadingSearch"
              >
                <i
                  class="fa-solid"
                  :class="
                    isLoadingSearch
                      ? 'fa-spinner fa-spin'
                      : 'fa-magnifying-glass'
                  "
                ></i>
                {{ isLoadingSearch ? "Đang tìm..." : "Tra cứu ngay" }}
              </button>
            </div>
          </div>
        </div>
      </template>

      <template v-if="isLoggedIn && !displayOrderData">
        <div class="card border-0 shadow-sm rounded-4 fade-in">
          <div class="card-body p-4">
            <div v-if="isLoadingHistory" class="text-center py-5 text-muted">
              <i class="fa-solid fa-spinner fa-spin fs-2 mb-3"></i>
              <p>Đang tải dữ liệu...</p>
            </div>
            <div
              v-else-if="userHistoryList.length === 0"
              class="text-center py-5 text-muted"
            >
              <i class="fa-regular fa-calendar-xmark fs-1 mb-3 opacity-50"></i>
              <h5>Bạn chưa có lịch sử đặt bàn nào.</h5>
              <button
                class="btn btn-custom mt-3"
                @click="router.push('/dat-ban')"
              >
                Đặt bàn ngay
              </button>
            </div>
            <div v-else class="table-responsive">
              <table class="table table-hover align-middle mb-0">
                <thead class="bg-custom-red text-white">
                  <tr>
                    <th class="py-3 ps-3">Mã Đơn</th>
                    <th class="py-3">Ngày Đặt</th>
                    <th class="py-3">Bàn/Số lượng</th>
                    <th class="text-end py-3">Tổng Tiền</th>
                    <th class="text-end py-3">Tiền Cọc</th>
                    <th class="text-center py-3">Trạng Thái</th>
                    <th class="text-center py-3 pe-3">Thao Tác</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="don in userHistoryList" :key="don.idPhieu">
                    <td class="fw-bold text-custom-red ps-3">
                      #{{ don.maPhieu }}
                    </td>
                    <td>{{ formatDateTime(don.thoiGianDat) }}</td>
                    <td>
                      <div>{{ don.tenBan || "Chưa xếp" }}</div>
                      <small class="text-muted"
                        ><i class="fa-solid fa-users me-1"></i
                        >{{ don.soNguoi }} người</small
                      >
                    </td>
                    <td class="text-end fw-bold">
                      {{ formatMoney(don.tongTienThanhToan) }}
                    </td>
                    <td class="text-end fw-bold">
                      {{ formatMoney(don.tienCoc) }}
                    </td>
                    <td class="text-center">
                      {{ getStatusBadge(don.trangThaiHoaDon).text }}
                    </td>
                    <td class="text-center pe-3">
                      <button
                        @click="viewHistoryDetail(don.maPhieu)"
                        class="btn btn-sm btn-outline-secondary me-2"
                        title="Xem chi tiết"
                      >
                        <i class="fa-solid fa-eye"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </template>

      <div v-if="displayOrderData" class="fade-in">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <button
            v-if="isLoggedIn || searchCode"
            @click="closeDetail"
            class="btn btn-light border shadow-sm fw-bold"
          >
            <i class="fa-solid fa-arrow-left me-2"></i> Quay lại
          </button>
          <h3
            class="fw-bold text-custom-red mb-0 m-auto text-uppercase"
            style="letter-spacing: 1px"
          >
            Chi Tiết Đơn: #{{ displayOrderData.maPhieu }}
          </h3>
          <div style="width: 100px"></div>
        </div>

        <div class="card border-0 shadow-sm rounded-4 mb-4">
          <div class="card-body py-5">
            <div class="stepper-scroll-container text-center">
              <div
                class="stepper-wrapper d-flex position-relative mx-auto"
                style="width: max-content"
              >
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

        <div class="row g-4">
          <div class="col-lg-8 d-flex flex-column gap-4">
            <div class="card border-0 shadow-sm rounded-4">
              <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
                <i class="fas fa-utensils" style="color: silver"></i> Chi tiết
                món ăn đã đặt
              </div>
              <div class="card-body p-0 table-responsive">
                <table class="table align-middle mb-0">
                  <thead class="bg-custom-red text-white">
                    <tr>
                      <th class="ps-4 py-3">TÊN MÓN</th>
                      <th class="text-center py-3">SỐ LƯỢNG</th>
                      <th class="text-center py-3">ĐƠN GIÁ</th>
                      <th class="text-end py-3 pe-4">THÀNH TIỀN</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="(item, index) in displayOrderData.chiTiet"
                      :key="index"
                    >
                      <td class="ps-4">
                        <span class="fw-medium">{{ item.tenMon }}</span>
                        <div
                          v-if="item.ghiChu"
                          class="small text-custom-red fst-italic mt-1"
                        >
                          Ghi chú: {{ item.ghiChu }}
                        </div>
                      </td>
                      <td class="text-center fw-bold">{{ item.soLuong }}</td>
                      <td class="text-center fw-bold">
                        {{ formatMoney(item.donGia) }}
                      </td>
                      <td class="text-end fw-bold pe-4">
                        {{ formatMoney(item.thanhTien) }}
                      </td>
                    </tr>
                    <tr
                      v-if="
                        !displayOrderData.chiTiet ||
                        displayOrderData.chiTiet.length === 0
                      "
                    >
                      <td colspan="4" class="py-5 bg-light">
                        <div
                          class="d-flex flex-column align-items-center justify-content-center w-100 text-muted"
                        >
                          <i
                            class="fa-solid fa-bell-concierge mb-3"
                            style="font-size: 3rem; color: #dee2e6"
                          ></i>
                          <h6 class="mb-1 text-secondary">
                            Chưa có món ăn nào được đặt trước
                          </h6>
                          <p class="small mb-0">
                            Bạn có thể xem thực đơn và gọi món trực tiếp khi đến
                            nhà hàng nhé!
                          </p>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div class="card border-0 shadow-sm rounded-4">
              <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
                <i class="far fa-clock"></i> Hành trình đơn hàng
              </div>
              <div
                class="card-body p-4"
                style="max-height: 350px; overflow-y: auto"
              >
                <div
                  v-if="
                    !displayOrderData.lichSuHoaDon ||
                    displayOrderData.lichSuHoaDon.length === 0
                  "
                  class="text-center text-muted py-3"
                >
                  <small>Chưa có lịch sử ghi nhận</small>
                </div>
                <div v-else>
                  <div
                    v-for="(event, index) in displayOrderData.lichSuHoaDon"
                    :key="index"
                    class="d-flex mb-3 position-relative"
                  >
                    <div
                      v-if="index !== displayOrderData.lichSuHoaDon.length - 1"
                      class="position-absolute"
                      style="
                        left: 6px;
                        top: 20px;
                        bottom: -20px;
                        width: 2px;
                        background-color: #e9ecef;
                        z-index: 1;
                      "
                    ></div>

                    <div
                      class="flex-shrink-0 mt-1 position-relative"
                      style="z-index: 2"
                    >
                      <div
                        class="rounded-circle"
                        :class="getEventColor(event.loaiHanhDong)"
                        style="
                          width: 14px;
                          height: 14px;
                          outline: 3px solid white;
                          box-shadow: 0 0 0 1px #dee2e6;
                        "
                      ></div>
                    </div>

                    <div class="ms-3 w-100 pb-2">
                      <div class="fw-bold text-dark">{{ event.hanhDong }}</div>
                      <div class="text-muted small mb-1">
                        {{ formatDateTime(event.thoiGian) }} - Người thực hiện:
                        <span class="fw-medium text-dark">{{
                          event.nguoiThucHien
                        }}</span>
                      </div>
                      <div
                        v-if="event.lyDo"
                        class="text-secondary fst-italic bg-light p-2 rounded-2 small mt-1 border"
                      >
                        {{ event.lyDo }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-lg-4 d-flex flex-column gap-4">
            <div class="card border-0 shadow-sm rounded-4">
              <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
                <i class="fas fa-sack-dollar" style="color: orange"></i> Thông
                tin thanh toán
              </div>
              <div class="card-body p-4 d-flex flex-column">
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Tổng tiền hàng:</span>
                  <span class="fw-bold">{{
                    formatMoney(calculatedSubTotal)
                  }}</span>
                </div>

                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted">Tổng thuế VAT:</span>
                  <span class="fw-bold">{{ formatMoney(calculatedTax) }}</span>
                </div>

                <div
                  v-if="actualDepositPaid > 0"
                  class="d-flex justify-content-between mb-3"
                >
                  <span class="text-muted">Đã đặt cọc:</span>
                  <span class="fw-bold text-success"
                    >- {{ formatMoney(actualDepositPaid) }}</span
                  >
                </div>

                <div
                  v-else-if="displayOrderData.trangThaiHoaDon === 1"
                  class="d-flex justify-content-between mb-3"
                >
                  <span class="text-muted small italic">Yêu cầu đặt cọc:</span>
                  <span class="text-dark small fw-bold">{{
                    formatMoney(displayOrderData.tienCoc)
                  }}</span>
                </div>

                <hr
                  class="border-secondary border-opacity-25 mt-0 mb-3 border-dashed"
                />

                <div
                  class="d-flex justify-content-between align-items-center mb-4"
                >
                  <span class="fs-5 fw-bold">Thành tiền:</span>
                  <span class="fs-4 fw-bold text-custom-red">
                    {{ formatMoney(finalBalance) }}
                  </span>
                </div>

                <div class="mt-auto pt-3">
                  <button
                    v-if="displayOrderData.trangThaiHoaDon === 1"
                    @click="handlePayDeposit(displayOrderData.idPhieu)"
                    class="btn btn-success w-100 fw-bold py-2 shadow-sm mb-2"
                  >
                    <i class="fa-solid fa-money-bill-transfer me-2"></i> Thanh
                    toán tiền cọc ngay
                  </button>
                  <button
                    v-if="canCancel && (isLoggedIn || searchSdt)"
                    @click="
                      handleCancelOrder(
                        displayOrderData.idPhieu,
                        displayOrderData.tienCoc,
                        displayOrderData.trangThaiHoaDon,
                      )
                    "
                    class="btn btn-outline-danger w-100 fw-bold py-2 shadow-sm"
                  >
                    <i class="fa-solid fa-ban me-2"></i> Yêu cầu hủy đơn này
                  </button>
                </div>
              </div>
            </div>

            <div
              v-if="
                displayOrderData.lichSuThanhToan &&
                displayOrderData.lichSuThanhToan.length > 0
              "
              class="card border-0 shadow-sm rounded-4"
            >
              <div class="card-header bg-white border-bottom py-3 fw-bold fs-5">
                <i class="fas fa-credit-card" style="color: cornflowerblue"></i>
                Giao dịch thanh toán
              </div>
              <div class="card-body p-4">
                <div
                  v-for="(pay, index) in displayOrderData.lichSuThanhToan"
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
                      style="width: 38px; height: 38px"
                    >
                      <i
                        class="fa-solid"
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
                      <span class="small text-dark fw-medium"
                        >{{ pay.tenPhuongThuc }}
                        <span
                          class="badge ms-1"
                          :class="`bg-${getPaymentTypeInfo(pay.loaiGiaoDich).color}`"
                          >{{
                            getPaymentTypeInfo(pay.loaiGiaoDich).label
                          }}</span
                        >
                      </span>
                    </div>
                    <div
                      v-if="pay.maGiaoDich"
                      class="small text-muted fst-italic mt-1"
                    >
                      Mã GD: {{ pay.maGiaoDich }}
                    </div>
                    <div
                      v-if="pay.ghiChu"
                      class="small text-muted mt-1 bg-light p-2 border rounded mt-2"
                    >
                      {{ pay.ghiChu }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
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
.btn-custom {
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%) !important;
  color: white !important;
  border: none !important;
  transition: 0.2s;
}
.btn-custom:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 10px rgba(139, 0, 0, 0.2);
}
.border-dashed {
  border-style: dashed !important;
}
.border-bottom-dashed {
  border-bottom: 1px dashed #dee2e6;
}
.last-no-border:last-child {
  border-bottom: none !important;
  padding-bottom: 0 !important;
  margin-bottom: 0 !important;
}

/* UTILS CHO MÀU SẮC LỊCH SỬ */
.bg-success-subtle {
  background-color: #d4edda !important;
}
.bg-warning-subtle {
  background-color: #fff3cd !important;
}
.bg-danger-subtle {
  background-color: #f8d7da !important;
}

/* ========================================================
   CSS CHUẨN XÁC TỪ TRANG ADMIN (ĐÃ ĐƯỢC CHỈNH LẠI)
======================================================== */
.stepper-scroll-container {
  overflow-x: auto;
  padding: 10px 20px;
}
.stepper-wrapper {
  position: relative;
  display: flex;
}
.step-item {
  width: 130px;
  z-index: 3;
}
.stepper-line-background,
.stepper-line-progress {
  position: absolute;
  top: 23px;
  left: 65px;
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

/* Các vòng tròn */
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

/* Animation */
.fade-in {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
