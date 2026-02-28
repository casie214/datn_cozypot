<script setup>
import { reactive, ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import axiosClient from "@/services/axiosClient";
import Swal from "sweetalert2";

const router = useRouter();

// --- 1. STATE QUẢN LÝ MÀN HÌNH ---
const step = ref(1);
const isLoading = ref(false);
const isSubmitting = ref(false);

// --- 2. DỮ LIỆU ĐẶT BÀN ---
const bookingData = reactive({
  people: null,
  date: "",
  time: "",
});

const customerInfo = reactive({
  fullName: "",
  phone: "",
  email: "",
  note: "",
  idKhachHang: null,
});

const cart = ref([]);

// --- 3. LIFECYCLE: PHỤC HỒI DỮ LIỆU & TỰ ĐỘNG ĐIỀN THÔNG TIN ---
onMounted(() => {
  // 3.1. LUÔN LẤY THÔNG TIN USER ĐÃ ĐĂNG NHẬP TRƯỚC (QUÉT MỌI KEY CÓ THỂ CÓ)
  const userStr = localStorage.getItem("user");
  if (userStr) {
    try {
      const userObj = JSON.parse(userStr);
      customerInfo.idKhachHang = userObj.id || null;
      customerInfo.fullName =
        userObj.tenKhachHang || userObj.hoTen || userObj.fullName || "";
      customerInfo.phone =
        userObj.soDienThoai ||
        userObj.sdt ||
        userObj.dienThoai ||
        userObj.phone ||
        "";
      customerInfo.email = userObj.email || userObj.username || "";
    } catch (e) {
      console.error("Lỗi parse thông tin user:", e);
    }
  }

  // 3.2. KHÔI PHỤC DỮ LIỆU TỪ GIỎ HÀNG (Nếu từ trang thực đơn quay lại)
  const savedForm = sessionStorage.getItem("pendingBooking");
  if (savedForm) {
    const parsed = JSON.parse(savedForm);
    Object.assign(bookingData, parsed.bookingData);

    // Chỉ ghi đè thông tin khách nếu trong nháp có (tránh làm mất thông tin user đã auto-fill)
    if (parsed.customerInfo?.fullName)
      customerInfo.fullName = parsed.customerInfo.fullName;
    if (parsed.customerInfo?.phone)
      customerInfo.phone = parsed.customerInfo.phone;
    if (parsed.customerInfo?.email)
      customerInfo.email = parsed.customerInfo.email;
    if (parsed.customerInfo?.note) customerInfo.note = parsed.customerInfo.note;

    step.value = 2; // Nhảy thẳng vào màn 2
  }

  // 3.3. Load giỏ hàng từ localStorage
  const savedCart = localStorage.getItem("cart");
  if (savedCart) {
    cart.value = JSON.parse(savedCart);
  }
});

// --- HELPER: TÍNH TOÁN TIỀN & FORMAT ---
const formatPrice = (val) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
    val || 0,
  );

const formattedDate = computed(() => {
  if (!bookingData.date) return "";
  const parts = bookingData.date.split("-");
  return `${parts[2]}/${parts[1]}/${parts[0]}`;
});

const subTotal = computed(() =>
  cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0),
);
const taxAmount = computed(() => subTotal.value * 0.1);
const totalAmount = computed(() => subTotal.value + taxAmount.value);
const depositAmount = computed(() => totalAmount.value * 0.4);

// --- 4. LOGIC BƯỚC 1: KIỂM TRA BÀN ---
const checkAvailability = async () => {
  if (!bookingData.people || !bookingData.date || !bookingData.time) {
    return Swal.fire(
      "Thiếu thông tin",
      "Vui lòng điền đủ số người, ngày và giờ!",
      "warning",
    );
  }

  isLoading.value = true;
  try {
    const payload = {
      soNguoi: bookingData.people,
      ngayDat: bookingData.date,
      gioDat: bookingData.time + ":00",
    };

    const response = await axiosClient.post(
      "/dat-ban/check-ban-trong",
      payload,
    );

    if (response.data === true) {
      step.value = 2;
    } else {
      Swal.fire({
        icon: "error",
        title: "Hết bàn rồi!",
        text: "Rất tiếc, nhà hàng đã hết bàn trống vào khung giờ này. Bạn vui lòng chọn giờ khác nhé!",
        confirmButtonColor: "#7d161a",
      });
    }
  } catch (error) {
    Swal.fire("Lỗi", "Không thể kết nối đến máy chủ.", "error");
  } finally {
    isLoading.value = false;
  }
};

// --- 5. LOGIC BƯỚC 2: CHUYỂN TRANG THỰC ĐƠN & CHỐT ĐƠN ---
const goBack = () => {
  step.value = 1;
  sessionStorage.removeItem("pendingBooking");
};

const goToMenu = () => {
  sessionStorage.setItem(
    "pendingBooking",
    JSON.stringify({ bookingData, customerInfo }),
  );
  router.push("/menu");
};

const removeCartItem = (index) => {
  cart.value.splice(index, 1);
  localStorage.setItem("cart", JSON.stringify(cart.value));
};

const submitFinalBooking = async () => {
  if (!customerInfo.fullName || !customerInfo.phone) {
    return Swal.fire(
      "Thiếu thông tin",
      "Vui lòng nhập Họ Tên và Số Điện Thoại!",
      "warning",
    );
  }

  isSubmitting.value = true;
  try {
    const payload = {
      idKhachHang: customerInfo.idKhachHang,
      fullName: customerInfo.fullName,
      phone: customerInfo.phone,
      email: customerInfo.email,
      thoiGianDat: `${bookingData.date}T${bookingData.time}:00`,
      soNguoi: bookingData.people,
      ghiChu: customerInfo.note,
      tongTien: totalAmount.value,
      tienCoc: depositAmount.value,

      chiTiet: cart.value.map((item) => ({
        idChiTietMonAn: item.type === "MON" ? item.id : null,
        idSetLau: item.type === "SET" ? item.id : null,
        soLuong: item.quantity,
        donGia: item.price,
      })),
    };

    // 1. GỌI API ĐẶT BÀN LÊN BACKEND (Sẽ tạo Phiếu, Hóa đơn và trả về JSON)
    const response = await axiosClient.post("/dat-ban/tao-moi", payload);
    const resultData = response.data; // Đây chính là Map<String, Object> BE trả về

    // 2. KIỂM TRA LUỒNG TIỀN CỌC TỪ KẾT QUẢ BACKEND
    if (resultData.trangThaiHoaDon === 1 && resultData.tienCoc > 0) {
      // 2.1 Hiện thông báo xác nhận cọc
      const confirm = await Swal.fire({
        title: "Xác nhận đặt cọc",
        html: `Để giữ bàn và chuẩn bị món ăn, bạn cần thanh toán trước khoản cọc:<br/>
               <b style="color:#7d161a; font-size: 24px;">${formatPrice(resultData.tienCoc)}</b>`,
        icon: "info",
        showCancelButton: true,
        confirmButtonColor: "#7d161a",
        cancelButtonColor: "#6c757d",
        confirmButtonText: "Đồng ý & Thanh toán",
        cancelButtonText: "Hủy đặt bàn",
      });

      if (confirm.isConfirmed) {
        // 2.2 Gọi API tạo link VNPay
        Swal.fire({
          title: "Đang kết nối cổng thanh toán VNPay...",
          allowOutsideClick: false,
          didOpen: () => {
            Swal.showLoading();
          },
        });

        // DÙNG FETCH THUẦN ĐỂ KHÔNG BỊ RỚT SESSION (Nhớ sửa lại URL cổng 8080 cho đúng với BE của bạn)
        try {
          const vnpayRawResponse = await fetch(
            `http://localhost:8080/api/vnpay/create-payment-deposit/${resultData.idHoaDon}`,
            {
              method: "GET",
              headers: { "Content-Type": "application/json" },
            },
          );

          if (vnpayRawResponse.ok) {
            const vnpayData = await vnpayRawResponse.json();
            if (vnpayData && vnpayData.url) {
              // Chuyển hướng trình duyệt sang trang VNPay
              window.location.href = vnpayData.url;
            } else {
              Swal.fire(
                "Lỗi",
                "Không lấy được đường dẫn thanh toán từ Server",
                "error",
              );
            }
          } else {
            Swal.fire("Lỗi", "Máy chủ VNPay không phản hồi", "error");
          }
        } catch (vnpayError) {
          Swal.fire(
            "Lỗi mạng",
            "Không thể kết nối đến máy chủ thanh toán",
            "error",
          );
          console.error(vnpayError);
        }
      } else {
        Swal.fire("Đã hủy", "Giao dịch đặt bàn đã bị hủy bỏ.", "info");
        // (Tùy chọn: Ở đây bạn có thể gọi thêm API xóa cái Hóa đơn & Phiếu vừa tạo nếu khách Hủy)
      }
    } else {
      // 3. LUỒNG KHÔNG CẦN CỌC (Trạng thái = 0)
      await Swal.fire(
        "Thành công",
        "Lịch hẹn của bạn đã được ghi nhận. Nhà hàng sẽ liên hệ xác nhận trong ít phút!",
        "success",
      );

      resetForm(); 
      
      router.push("/");
    }
  } catch (error) {
    Swal.fire(
      "Lỗi",
      error.response?.data?.message ||
        error.response?.data ||
        "Đặt bàn thất bại",
      "error",
    );
  } finally {
    isSubmitting.value = false;
  }
};

const resetForm = () => {
  bookingData.people = null;
  bookingData.date = "";
  bookingData.time = "";

  // Chỉ reset thông tin nhập tay, KHÔNG reset idKhachHang nếu họ đang đăng nhập
  const userStr = localStorage.getItem("user");
  if (!userStr) {
    customerInfo.fullName = "";
    customerInfo.phone = "";
    customerInfo.email = "";
  }
  customerInfo.note = "";

  cart.value = [];
  localStorage.removeItem("cart");
  sessionStorage.removeItem("pendingBooking");
  step.value = 1;
};
</script>

<template>
  <div
    class="booking-page"
    style="
      min-height: 100vh;
      background-color: #fafafa;
      padding-top: 40px;
      padding-bottom: 60px;
    "
  >
    <section class="container form-container">
      <div v-if="step === 1" class="fade-in mx-auto" style="max-width: 750px">
        <div class="d-flex align-items-center mb-2 px-3">
          <div
            class="flex-grow-1 border-top"
            style="
              border-color: #7d161a !important;
              border-width: 1.5px !important;
              opacity: 0.5;
            "
          ></div>
          <h2
            class="px-4 fw-bold mb-0 text-uppercase text-center"
            style="color: #7d161a; font-size: 26px; letter-spacing: 0.5px"
          >
            ĐẶT BÀN ONLINE
          </h2>
          <div
            class="flex-grow-1 border-top"
            style="
              border-color: #7d161a !important;
              border-width: 1.5px !important;
              opacity: 0.5;
            "
          ></div>
        </div>
        <p class="text-center text-muted small mb-4">
          Vui lòng điền thông tin ngày giờ để kiểm tra bàn
        </p>

        <div class="card border-0 shadow-sm rounded-4 p-4 p-md-5 bg-white">
          <div class="custom-alert alert-orange d-flex align-items-center mb-4">
            <i class="fas fa-unlock-alt me-3 fs-5"></i>
            <div>
              <strong class="me-1">Không cần đăng nhập</strong> - Bạn có thể đặt
              bàn ngay mà không cần tài khoản
            </div>
          </div>

          <form @submit.prevent="checkAvailability">
            <div class="row g-4 mb-4">
              <div class="col-md-6">
                <label class="form-label text-muted small fw-bold"
                  >SỐ NGƯỜI ĐI <span class="text-danger">*</span></label
                >
                <input
                  type="number"
                  v-model="bookingData.people"
                  min="1"
                  class="form-control form-control-lg custom-input"
                  placeholder="Nhập số người"
                  required
                />
              </div>
              <div class="col-md-6">
                <label class="form-label text-muted small fw-bold"
                  >NGÀY ĐẾN <span class="text-danger">*</span></label
                >
                <input
                  type="date"
                  v-model="bookingData.date"
                  class="form-control form-control-lg custom-input"
                  required
                />
              </div>
            </div>

            <div class="mb-5">
              <label class="form-label text-muted small fw-bold"
                >GIỜ ĐẾN <span class="text-danger">*</span></label
              >
              <select
                v-model="bookingData.time"
                class="form-select form-select-lg custom-input"
                required
              >
                <option value="" disabled selected>Chọn giờ</option>
                <option value="17:00">17:00</option>
                <option value="18:00">18:00</option>
                <option value="19:00">19:00</option>
                <option value="20:00">20:00</option>
              </select>
            </div>

            <button
              type="submit"
              :disabled="isLoading"
              class="btn btn-custom-red btn-lg w-100 fw-bold d-flex justify-content-center align-items-center gap-2"
            >
              <span
                v-if="isLoading"
                class="spinner-border spinner-border-sm"
              ></span>
              <i v-else class="fas fa-search"></i>
              {{ isLoading ? "Đang kiểm tra..." : "Kiểm tra bàn" }}
            </button>
          </form>
        </div>
      </div>

      <div v-if="step === 2" class="fade-in mx-auto" style="max-width: 1100px">
        <div class="d-flex align-items-center mb-2 px-3">
          <div
            class="flex-grow-1 border-top"
            style="
              border-color: #7d161a !important;
              border-width: 1.5px !important;
              opacity: 0.5;
            "
          ></div>
          <h2
            class="px-4 fw-bold mb-0 text-uppercase"
            style="color: #7d161a; font-size: 26px; letter-spacing: 0.5px"
          >
            ĐẶT BÀN ONLINE
          </h2>
          <div
            class="flex-grow-1 border-top"
            style="
              border-color: #7d161a !important;
              border-width: 1.5px !important;
              opacity: 0.5;
            "
          ></div>
        </div>
        <p class="text-center text-muted small mb-5">
          Vui lòng điền thông tin để hoàn tất đặt bàn
        </p>

        <div class="row g-4">
          <div class="col-lg-6">
            <div
              class="checkout-panel bg-white shadow-sm h-100 d-flex flex-column"
              style="
                border-radius: 12px;
                overflow: hidden;
                border: 1px solid #eee;
              "
            >
              <div
                class="panel-header text-white px-4 py-3"
                style="background-color: #7d161a"
              >
                <h5 class="mb-0 fw-bold text-uppercase fs-6">
                  Thông tin khách hàng
                </h5>
              </div>

              <div class="panel-body p-4 flex-grow-1 d-flex flex-column">
                <div
                  v-if="customerInfo.idKhachHang"
                  class="badge bg-success mb-4 p-2 w-100 text-start"
                >
                  <i class="fas fa-user-check me-1"></i> Đang đặt bàn với tài
                  khoản hệ thống
                </div>

                <div class="row g-4 mb-4">
                  <div class="col-sm-6">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-user text-danger me-2"></i> KHÁCH
                      HÀNG</label
                    >
                    <input
                      type="text"
                      v-model="customerInfo.fullName"
                      class="form-control custom-input border-0 border-bottom bg-transparent px-1 rounded-0 fw-bold text-dark fs-6"
                      placeholder="Nhập họ và tên..."
                      :readonly="!!customerInfo.idKhachHang"
                    />
                  </div>
                  <div class="col-sm-6">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-phone-alt text-danger me-2"></i> SỐ ĐIỆN
                      THOẠI</label
                    >
                    <input
                      type="tel"
                      v-model="customerInfo.phone"
                      class="form-control custom-input border-0 border-bottom bg-transparent px-1 rounded-0 fw-bold text-dark fs-6"
                      placeholder="Nhập SĐT..."
                      :readonly="!!customerInfo.idKhachHang"
                    />
                  </div>
                </div>

                <div class="row g-4 mb-4">
                  <div class="col-12">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-envelope text-danger me-2"></i>
                      EMAIL</label
                    >
                    <input
                      type="email"
                      v-model="customerInfo.email"
                      class="form-control custom-input border-0 border-bottom bg-transparent px-1 rounded-0 fw-bold text-dark fs-6"
                      placeholder="Nhập email của bạn (không bắt buộc)..."
                      :readonly="!!customerInfo.idKhachHang"
                    />
                  </div>
                </div>

                <div class="row g-4 mb-4">
                  <div class="col-sm-6">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-calendar-alt text-danger me-2"></i> NGÀY
                      ĐẾN</label
                    >
                    <div
                      class="fs-6 fw-bold text-dark px-1 mt-2 border-bottom pb-2 border-light"
                    >
                      {{ formattedDate }}
                    </div>
                  </div>
                  <div class="col-sm-6">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-clock text-danger me-2"></i> GIỜ ĐÓN
                      KHÁCH</label
                    >
                    <div
                      class="fs-6 fw-bold text-dark px-1 mt-2 border-bottom pb-2 border-light"
                    >
                      {{ bookingData.time }}
                    </div>
                  </div>
                </div>

                <div class="row g-4 mb-4">
                  <div class="col-12">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-users text-danger me-2"></i> SỐ LƯỢNG
                      NGƯỜI ĐI</label
                    >
                    <div
                      class="fs-6 fw-bold text-dark px-1 mt-2 border-bottom pb-2 border-light"
                    >
                      {{ bookingData.people }} người
                    </div>
                  </div>
                </div>

                <div class="mb-4 flex-grow-1">
                  <label class="form-label text-muted small fw-bold mb-2"
                    ><i class="fas fa-comment-dots text-danger me-2"></i> YÊU
                    CẦU VÀ GHI CHÚ</label
                  >
                  <textarea
                    v-model="customerInfo.note"
                    class="form-control border-0 h-100 min-h-[100px]"
                    style="
                      background-color: #a8a8a8;
                      border-left: 4px solid #7d161a !important;
                      border-radius: 6px;
                      color: #fff;
                    "
                    placeholder=" Nhập ghi chú của bạn..."
                  ></textarea>
                </div>

                <div class="mt-3">
                  <button
                    type="button"
                    @click="goBack"
                    class="btn text-dark fw-bold px-4 py-2"
                    style="background-color: #c4c4c4; border-radius: 8px"
                  >
                    <i class="fas fa-arrow-left me-2"></i> Quay về
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div class="col-lg-6">
            <div
              class="checkout-panel bg-white shadow-sm h-100 d-flex flex-column"
              style="
                border-radius: 12px;
                overflow: hidden;
                border: 1px solid #eee;
              "
            >
              <div
                class="panel-header px-4 py-3 d-flex justify-content-between align-items-center text-white"
                style="background-color: #7d161a"
              >
                <h5 class="mb-0 fw-bold text-uppercase fs-6">Danh sách món</h5>
                <button
                  type="button"
                  @click="goToMenu"
                  class="btn btn-sm text-white border-0 fw-bold px-0"
                  style="background: transparent"
                >
                  Thêm món
                </button>
              </div>

              <div class="panel-body p-4 flex-grow-1 d-flex flex-column">
                <div
                  class="cart-items-wrapper custom-scrollbar mb-4 p-3"
                  style="
                    max-height: 280px;
                    overflow-y: auto;
                    border: 2px solid #0ea5e9;
                    border-radius: 12px;
                  "
                >
                  <div
                    v-if="cart.length === 0"
                    class="text-center py-4 text-muted small"
                  >
                    <p class="mb-2">Chưa có món nào được chọn.</p>
                    <button
                      @click="goToMenu"
                      class="btn btn-sm text-white"
                      style="background-color: #7d161a; border-radius: 6px"
                    >
                      Chọn món ngay
                    </button>
                  </div>

                  <div v-else>
                    <div
                      v-for="(item, index) in cart"
                      :key="index"
                      class="d-flex justify-content-between align-items-center mb-3 pb-3 border-bottom"
                    >
                      <div>
                        <h6 class="mb-1 fw-bold text-dark fs-6">
                          {{ item.name }}
                        </h6>
                        <div class="text-muted small">
                          Số lượng: {{ item.quantity }} ×
                          <span class="text-decoration-underline">{{
                            formatPrice(item.price)
                          }}</span>
                        </div>
                      </div>
                      <div class="text-end">
                        <h6 class="mb-0 fw-bold text-dark">
                          {{ formatPrice(item.price * item.quantity) }}
                        </h6>
                        <button
                          class="btn btn-link text-danger p-0 mt-1 small text-decoration-none"
                          style="font-size: 0.75rem"
                          @click="removeCartItem(index)"
                        >
                          Xóa
                        </button>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="summary-box mt-auto px-2">
                  <div class="d-flex justify-content-between mb-2">
                    <span class="text-dark fw-bold small">Tạm tính:</span>
                    <span class="text-dark fw-bold small">{{
                      formatPrice(subTotal)
                    }}</span>
                  </div>
                  <div class="d-flex justify-content-between mb-3">
                    <span class="text-dark fw-bold small">Thuế (10%):</span>
                    <span class="text-dark fw-bold small">{{
                      formatPrice(taxAmount)
                    }}</span>
                  </div>

                  <hr class="border-secondary opacity-25" />

                  <div class="d-flex justify-content-between mb-2">
                    <span class="text-dark fw-bold small"
                      >Tổng cộng dự tính:</span
                    >
                    <span class="text-dark fw-bold small">{{
                      formatPrice(totalAmount)
                    }}</span>
                  </div>
                  <div class="d-flex justify-content-between mb-3">
                    <span class="text-dark fw-bold small">Tiền cọc (40%):</span>
                    <span class="text-dark fw-bold small">{{
                      formatPrice(depositAmount)
                    }}</span>
                  </div>

                  <hr class="border-secondary opacity-25" />

                  <div
                    class="d-flex justify-content-between align-items-center mb-4 mt-3"
                  >
                    <span class="text-dark fw-bold fs-6">Cần thanh toán:</span>
                    <span class="fw-bold fs-5" style="color: #e53935">{{
                      formatPrice(depositAmount)
                    }}</span>
                  </div>

                  <div class="text-end">
                    <button
                      @click="submitFinalBooking"
                      :disabled="isSubmitting"
                      class="btn text-white px-5 py-2 fw-bold"
                      style="
                        background-color: #7d161a;
                        border-radius: 8px;
                        font-size: 1rem;
                      "
                    >
                      <span
                        v-if="isSubmitting"
                        class="spinner-border spinner-border-sm me-2"
                      ></span>
                      {{ isSubmitting ? "ĐANG XỬ LÝ..." : "ĐẶT BÀN" }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.booking-page {
  font-family: Arial, sans-serif;
}

.custom-input.border-bottom {
  border-bottom: 1px solid #ddd !important;
  transition: all 0.2s ease;
}
.custom-input.border-bottom:focus {
  border-bottom: 2px solid #7d161a !important;
  box-shadow: none;
  outline: none;
}
input[readonly] {
  color: #555 !important;
  cursor: not-allowed;
  opacity: 0.8;
}
textarea::placeholder {
  color: #ddd !important;
}

.btn-custom-red {
  background-color: #7d161a;
  border: none;
  border-radius: 8px;
  padding: 12px;
  color: white;
  transition: all 0.3s ease;
}

.btn-custom-red:hover:not(:disabled) {
  background-color: #5e1013;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(125, 22, 26, 0.2);
}

.custom-alert {
  padding: 14px 20px;
  border-radius: 8px;
  font-size: 0.9rem;
}

.alert-orange {
  background-color: #fff4e6;
  color: #d35400;
  border: 1px solid #ffe8cc;
}

.icon-red {
  color: #7d161a;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 5px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 5px;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 5px;
}

.fade-in {
  animation: fadeIn 0.4s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:global(.swal2-container) {
  z-index: 20000 !important;
}

.title-with-line {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7d161a;
  font-size: 26px;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 0;
  white-space: nowrap;
}

.title-with-line::before,
.title-with-line::after {
  content: "";
  flex: 1;
  border-bottom: 1.5px solid rgba(125, 22, 26, 0.4);
  margin: 0 25px;
}
</style>
