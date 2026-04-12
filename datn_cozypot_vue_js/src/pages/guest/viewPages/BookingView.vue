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
const availableTables = ref([]);
const hasCheckedTables = ref(false);
const isNameLocked = ref(false);
const isPhoneLocked = ref(false);
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

const errors = reactive({
  people: "",
  date: "",
  time: "",
  fullName: "",
  phone: "",
  email: "",
});

//Gia trị mặc định
const systemParams = reactive({
  phanTramCoc: 40,
});

const fetchSystemParams = async () => {
  try {
    const response = await axiosClient.get("/tham-so-he-thong/all-map");
    const data = response.data;
    if (data) {
      if (data.PHAN_TRAM_COC)
        systemParams.phanTramCoc = parseFloat(data.PHAN_TRAM_COC);
    }
  } catch (error) {
    console.error("Lỗi khi tải tham số hệ thống:", error);
  }
};

const validatePeople = () => {
  const p = bookingData.people;
  if (!p) {
    errors.people = "Vui lòng nhập số người.";
  } else if (p < 1 || p > 20) {
    errors.people = "Số người phải từ 1 đến 20.";
  } else if (!Number.isInteger(Number(p))) {
    errors.people = "Số người phải là số nguyên dương.";
  } else {
    errors.people = "";
  }
};

const validateDate = () => {
  if (!bookingData.date) {
    errors.date = "Vui lòng chọn ngày đến.";
  } else {
    errors.date = "";
  }
};

const validateTime = () => {
  if (!bookingData.time) {
    errors.time = "Vui lòng chọn giờ đến.";
  } else {
    errors.time = "";
  }
};

const validateFullName = () => {
  if (!customerInfo.fullName || customerInfo.fullName.trim() === "") {
    errors.fullName = "Vui lòng nhập họ và tên.";
  } else if (customerInfo.fullName.trim().length < 2) {
    errors.fullName = "Tên quá ngắn, vui lòng nhập đầy đủ.";
  } else if (customerInfo.fullName.trim().length > 50) { // <--- THÊM CHECK MAX LENGTH
    errors.fullName = "Họ và tên không được vượt quá 50 ký tự.";
  } else {
    errors.fullName = "";
  }
};

const validatePhone = () => {
  const phoneRegex = /^(0[3|5|7|8|9])+([0-9]{8})\b$/;
  if (!customerInfo.phone) {
    errors.phone = "Vui lòng nhập số điện thoại.";
  } else if (!phoneRegex.test(customerInfo.phone)) {
    errors.phone = "Số điện thoại không hợp lệ";
  } else {
    errors.phone = "";
  }
};

const validateEmail = () => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!customerInfo.email) {
    errors.email = "Vui lòng nhập email.";
  } else if (!emailRegex.test(customerInfo.email)) {
    errors.email = "Định dạng email không hợp lệ.";
  } else {
    errors.email = "";
  }
};

const cart = ref([]);

// --- 3. LIFECYCLE: PHỤC HỒI DỮ LIỆU & TỰ ĐỘNG ĐIỀN THÔNG TIN ---
onMounted(async () => {
  await fetchSystemParams();
  // 3.1. LUÔN LẤY THÔNG TIN USER ĐÃ ĐĂNG NHẬP TRƯỚC (QUÉT MỌI KEY CÓ THỂ CÓ)
  const userStr = localStorage.getItem("user");
  console.log(userStr);
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
      customerInfo.email = userObj.email || "";
      isNameLocked.value =
        !!customerInfo.fullName && customerInfo.fullName !== "Người dùng";
      isPhoneLocked.value = !!customerInfo.phone;
    } catch (e) {
      console.error("Lỗi parse thông tin user:", e);
    }
  }

  // 3.2. KHÔI PHỤC DỮ LIỆU TỪ GIỎ HÀNG (Nếu từ trang thực đơn quay lại)
  const savedForm = sessionStorage.getItem("pendingBooking");
  if (savedForm) {
    const parsed = JSON.parse(savedForm);
    Object.assign(bookingData, parsed.bookingData);

    // PHỤC HỒI LẠI DANH SÁCH BÀN TỪ NHÁP
    if (parsed.availableTables) {
      availableTables.value = parsed.availableTables;
      hasCheckedTables.value = true;
    }

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

const totalAmount = computed(() => subTotal.value); 
const depositAmount = computed(
  () => totalAmount.value * (systemParams.phanTramCoc / 100),
);

// --- 4. LOGIC BƯỚC 1: KIỂM TRA BÀN ---
const checkAvailability = async () => {
  validatePeople();
  validateDate();
  validateTime();
  if (errors.people || errors.date || errors.time) {
    return;
  }

  const now = new Date();
  const selectedDateTime = new Date(
    `${bookingData.date}T${bookingData.time}:00`,
  );

  if (selectedDateTime <= now) {
    return Swal.fire({
      icon: "warning",
      title: "Thời gian không hợp lệ",
      text: "Vui lòng chọn thời gian đặt bàn ở trong tương lai!",
      confirmButtonColor: "#7d161a",
    });
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

    availableTables.value = response.data;
    hasCheckedTables.value = true;

    // Nếu mảng rỗng -> Báo lỗi hết bàn
    if (availableTables.value.length === 0) {
      Swal.fire({
        icon: "error",
        title: "Không có bàn phù hợp!",
        text: `Hiện nhà hàng không còn bàn trống nào phù hợp cho ${bookingData.people} người vào khung giờ này. Vui lòng thay đổi giờ hoặc số lượng khách!`,
        confirmButtonColor: "#7d161a",
      });
    } else {
      // Nếu có bàn trống -> Nhảy thẳng sang Màn 2
      step.value = 2;
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
    JSON.stringify({
      bookingData,
      customerInfo,
      availableTables: availableTables.value,
    }),
  );
  router.push("/menu");
};

const removeCartItem = (index) => {
  cart.value.splice(index, 1);
  localStorage.setItem("cart", JSON.stringify(cart.value));
};

const submitFinalBooking = async () => {
  validateFullName();
  validatePhone();
  validateEmail();
  if (errors.fullName || errors.phone || errors.email) {
    return;
  }

  // ==========================================================
  // 🚨 THÊM LOGIC CHECK TRÙNG LỊCH 3 TIẾNG CỦA KHÁCH HÀNG
  // ==========================================================
  try {
    Swal.fire({ title: "Đang kiểm tra thông tin...", allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    // Gọi API tìm các phiếu cũ của SĐT này 
    // (Lưu ý: Cần đảm bảo API này cho phép role Guest truy cập)
    const resSearch = await axiosClient.post("/dat-ban/search", {
      soDienThoai: customerInfo.phone,
      trangThai: null
    });

    const historyBookings = resSearch.data?.content || resSearch.data || [];
    const timeToBook = new Date(`${bookingData.date}T${bookingData.time}:00`).getTime();

    const conflictingBooking = historyBookings.find(phieu => {
      // Bỏ qua phiếu Đã hủy (2) hoặc Hoàn thành (4)
      if (String(phieu.trangThai) === "2" || String(phieu.trangThai) === "4") return false;

      // Xử lý parse ngày tháng (hỗ trợ cả dạng mảng [Y,M,D,H,m] hoặc chuỗi ISO)
      let existingTimeObj;
      if (Array.isArray(phieu.thoiGianDat)) {
        const [y, m, d, h, min, s] = phieu.thoiGianDat;
        existingTimeObj = new Date(y, m - 1, d, h || 0, min || 0, s || 0);
      } else {
        existingTimeObj = new Date(phieu.thoiGianDat);
      }

      // Tính khoảng cách giữa 2 lịch (trị tuyệt đối)
      const diffHours = Math.abs(existingTimeObj.getTime() - timeToBook) / (1000 * 60 * 60);
      return diffHours < 3; // Nằm trong khoảng dưới 3 tiếng
    });

    if (conflictingBooking) {
      let existingFormattedTime = "";
      if (Array.isArray(conflictingBooking.thoiGianDat)) {
        const [y, m, d, h, min] = conflictingBooking.thoiGianDat;
        existingFormattedTime = `${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')} ngày ${String(d).padStart(2, '0')}/${String(m).padStart(2, '0')}/${y}`;
      } else {
        const d = new Date(conflictingBooking.thoiGianDat);
        existingFormattedTime = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')} ngày ${String(d.getDate()).padStart(2, '0')}/${String(d.getMonth() + 1).padStart(2, '0')}/${d.getFullYear()}`;
      }

      return Swal.fire({
        icon: 'warning',
        title: 'Trùng lịch đặt bàn!',
        text: `Số điện thoại ${customerInfo.phone} đã có lịch đặt lúc ${existingFormattedTime}. Các lịch phải cách nhau ít nhất 3 tiếng!`,
        confirmButtonColor: '#7d161a',
      });
    }
    
    Swal.close(); // Đóng loading nếu check OK
  } catch (e) {
    console.warn("Không thể check lịch khách hàng từ FE (Có thể do phân quyền API):", e);
    Swal.close(); 
    // Nếu API /search bị chặn 403, vẫn cho đi tiếp để Backend tự block ở API /tao-moi
  }

  let depositHtml = "";
  if (depositAmount.value > 0) {
    depositHtml = `
      <div style="display: flex; justify-content: space-between; padding-top: 12px; border-top: 1px dashed #e0e0e0; margin-top: 12px;">
        <span style="color: #666;"><i class="fas fa-money-bill-wave" style="width: 25px; color: #a0a0a0;"></i> Tiền cọc (${systemParams.phanTramCoc}%):</span>
        <strong style="color: #d32f2f; font-size: 16px;">${formatPrice(depositAmount.value)}</strong>
      </div>
    `;
  }

  // BƯỚC XÁC NHẬN THÔNG TIN
  const isConfirm = await Swal.fire({
    title: '<h3 style="font-weight: bold; color: #333; margin-bottom: 0px;">Xác Nhận Đặt Bàn</h3>',
    html: `
      <p style="color: #888; font-size: 14px; margin-bottom: 20px;">Vui lòng kiểm tra lại thông tin trước khi xác nhận</p>

      <div style="text-align: left; font-size: 14.5px; background: #fff; padding: 15px 20px; border-radius: 12px; border: 1px solid #eaeaea; box-shadow: 0 4px 15px rgba(0,0,0,0.03); margin-bottom: 20px;">
        
        <div style="display: flex; justify-content: space-between; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f5f5f5;">
          <span style="color: #666;"><i class="fas fa-user" style="width: 25px; color: #a0a0a0;"></i> Họ và tên:</span>
          <strong style="color: #333;">${customerInfo.fullName}</strong>
        </div>

        <div style="display: flex; justify-content: space-between; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f5f5f5;">
          <span style="color: #666;"><i class="fas fa-phone-alt" style="width: 25px; color: #a0a0a0;"></i> Số điện thoại:</span>
          <strong style="color: #333;">${customerInfo.phone}</strong>
        </div>

        <div style="display: flex; justify-content: space-between; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f5f5f5;">
          <span style="color: #666;"><i class="fas fa-envelope" style="width: 25px; color: #a0a0a0;"></i> Email:</span>
          <strong style="color: #333;">${customerInfo.email}</strong>
        </div>

        <div style="display: flex; justify-content: space-between; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f5f5f5;">
          <span style="color: #666;"><i class="fas fa-users" style="width: 25px; color: #a0a0a0;"></i> Số người:</span>
          <strong style="color: #333;">${bookingData.people} người</strong>
        </div>

        <div style="display: flex; justify-content: space-between; margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #f5f5f5;">
          <span style="color: #666;"><i class="fas fa-calendar-alt" style="width: 25px; color: #a0a0a0;"></i> Ngày:</span>
          <strong style="color: #333;">${formattedDate.value}</strong>
        </div>

        <div style="display: flex; justify-content: space-between; margin-bottom: 0;">
          <span style="color: #666;"><i class="fas fa-clock" style="width: 25px; color: #a0a0a0;"></i> Giờ:</span>
          <strong style="color: #333;">${bookingData.time}</strong>
        </div>

        ${depositHtml}
      </div>

      <div style="background-color: #fffaf0; color: #b7791f; padding: 12px 15px; border-radius: 8px; font-size: 13px; text-align: left; display: flex; gap: 10px; align-items: flex-start; border: 1px solid #fce8b2;">
        <i class="fas fa-info-circle" style="margin-top: 3px;"></i>
        <span>Sau khi xác nhận, bạn sẽ nhận được thông báo qua email/SMS. Vui lòng kiểm tra thông tin chính xác.</span>
      </div>
    `,
    icon: "question",
    showCancelButton: true,
    confirmButtonText: '<i class="fas fa-check-circle me-1"></i> Xác Nhận Đặt Bàn',
    cancelButtonText: '<i class="fas fa-times me-1"></i> Hủy',
    buttonsStyling: false,
    customClass: {
      popup: "rounded-4",
      confirmButton: "swal-confirm-btn", 
      cancelButton: "swal-cancel-btn",
    },
    didOpen: () => {
      const confirmBtn = Swal.getConfirmButton();
      const cancelBtn = Swal.getCancelButton();

      confirmBtn.style.background =
        "linear-gradient(135deg, #7D161A 0%, #D32F2F 100%)";
      confirmBtn.style.color = "white";
      confirmBtn.style.border = "none";
      confirmBtn.style.padding = "12px 30px";
      confirmBtn.style.fontSize = "1.1rem";
      confirmBtn.style.borderRadius = "10px";
      confirmBtn.style.boxShadow = "0 4px 12px rgba(125, 22, 26, 0.2)";

      cancelBtn.style.backgroundColor = "#f3f4f6";
      cancelBtn.style.color = "#333";
      cancelBtn.style.border = "none";
      cancelBtn.style.padding = "12px 30px";
      cancelBtn.style.fontSize = "1.1rem";
      cancelBtn.style.borderRadius = "10px";
    },
  });

  // Nếu khách bấm "Hủy" hoặc bấm ra ngoài popup -> Dừng hàm lại luôn
  if (!isConfirm.isConfirmed) {
    return;
  }

  // ==========================================================
  // NẾU KHÁCH ĐÃ CHỐT -> BẮT ĐẦU HIỆN LOADING VÀ GỌI API BE
  // ==========================================================
  //isSubmitting.value = true;
  try {
    const payload = {
      idKhachHang: customerInfo.idKhachHang,
      idBanAn: null,
      fullName: customerInfo.fullName,
      phone: customerInfo.phone,
      email: customerInfo.email,
      thoiGianDat: `${bookingData.date}T${bookingData.time}:00`,
      soNguoi: bookingData.people,
      ghiChu: customerInfo.note,
      tongTien: totalAmount.value,
      tienCoc: depositAmount.value,
      chiTiet: cart.value.map((item) => {
        // const vatRate = item.phanTramVat || 0;
        return {
          idChiTietMonAn: item.type === "MON" ? item.id : null,
          idSetLau: item.type === "SET" ? item.id : null,
          soLuong: item.quantity,
          donGia: item.price,
          phanTramVat: 0, 
          tienVat: 0,
        };
      }),
    };

    // 1. GỌI API ĐẶT BÀN LÊN BACKEND
    const response = await axiosClient.post("/dat-ban/tao-moi", payload);
    const resultData = response.data;

    // 2. KIỂM TRA LUỒNG TIỀN CỌC TỪ KẾT QUẢ BACKEND
    if (resultData.trangThaiHoaDon === 1 && resultData.tienCoc > 0) {
      Swal.fire({
        title: "Đang kết nối cổng thanh toán VNPay...",
        allowOutsideClick: false,
        didOpen: () => {
          Swal.showLoading();
        },
      });

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
            resetForm();
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
      // 3. LUỒNG KHÔNG CẦN CỌC (Trạng thái = 0)
      await Swal.fire({
        title: "Thành công",
        text: "Lịch hẹn của bạn đã được ghi nhận. Nhà hàng sẽ liên hệ xác nhận trong ít phút!",
        icon: "success",
        confirmButtonText: "OK",
        buttonsStyling: false,
        customClass: {
          popup: "rounded-4",
          confirmButton: "btn text-white fw-bold px-4 py-2",
        },
        didOpen: () => {
          const confirmBtn = Swal.getConfirmButton();
          confirmBtn.style.background =
            "linear-gradient(135deg, #7D161A 0%, #D32F2F 100%)";
          confirmBtn.style.color = "white";
          confirmBtn.style.border = "none";
          confirmBtn.style.borderRadius = "10px";
          confirmBtn.style.padding = "10px 30px";
          confirmBtn.style.fontSize = "1rem";
        },
      });
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
    //isSubmitting.value = false;
  }
};

const resetForm = () => {
  bookingData.people = null;
  bookingData.date = "";
  bookingData.time = "";

  const userStr = localStorage.getItem("user");
  if (!userStr) {
    customerInfo.fullName = "";
    customerInfo.phone = "";
    customerInfo.email = "";
  }
  customerInfo.note = "";

  errors.people = "";
  errors.date = "";
  errors.time = "";
  errors.fullName = "";
  errors.phone = "";
  errors.email = "";

  cart.value = [];
  localStorage.removeItem("cart");
  sessionStorage.removeItem("pendingBooking");
  step.value = 1;
};

const minDate = computed(() => {
  const today = new Date();
  const yyyy = today.getFullYear();
  const mm = String(today.getMonth() + 1).padStart(2, "0");
  const dd = String(today.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
});
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

          <form @submit.prevent="checkAvailability" novalidate>
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
                <span
                  v-if="errors.people"
                  class="text-danger small mt-1 d-block"
                >
                  <i class="fas fa-exclamation-circle me-1"></i
                  >{{ errors.people }}
                </span>
              </div>
              <div class="col-md-6">
                <label class="form-label text-muted small fw-bold"
                  >NGÀY ĐẾN <span class="text-danger">*</span></label
                >
                <input
                  type="date"
                  v-model="bookingData.date"
                  :min="minDate"
                  class="form-control form-control-lg custom-input"
                  required
                />
                <span v-if="errors.date" class="text-danger small mt-1 d-block">
                  <i class="fas fa-exclamation-circle me-1"></i
                  >{{ errors.date }}
                </span>
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
                <option value="17:30">17:30</option>
                <option value="18:00">18:00</option>
                <option value="18:30">18:30</option>
                <option value="19:00">19:00</option>
                <option value="19:30">19:30</option>
                <option value="20:00">20:00</option>
                <option value="20:30">20:30</option>
                <option value="21:00">21:00</option>
              </select>
              <span v-if="errors.time" class="text-danger small mt-1 d-block">
                <i class="fas fa-exclamation-circle me-1"></i>{{ errors.time }}
              </span>
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
                class="panel-header text-white px-4 d-flex align-items-center"
                style="background-color: #7d161a; height: 60px"
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
                      ><i class="fas fa-user text-danger me-2"></i> KHÁCH HÀNG
                      <span class="text-danger">*</span></label
                    >
                    <input
                      type="text"
                      v-model="customerInfo.fullName"
                      maxlength="50"
                      class="form-control custom-input border-0 border-bottom bg-transparent px-1 rounded-0 fw-bold text-dark fs-6"
                      placeholder="Nhập họ và tên..."
                      :readonly="!!customerInfo.idKhachHang && isNameLocked"
                    />
                    <span
                      v-if="errors.fullName"
                      class="text-danger small mt-1 d-block"
                    >
                      {{ errors.fullName }}
                    </span>
                  </div>
                  <div class="col-sm-6">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-phone-alt text-danger me-2"></i> SỐ ĐIỆN
                      THOẠI <span class="text-danger">*</span></label
                    >
                    <input
                      type="tel"
                      v-model="customerInfo.phone"
                      class="form-control custom-input border-0 border-bottom bg-transparent px-1 rounded-0 fw-bold text-dark fs-6"
                      placeholder="Nhập SĐT..."
                      :readonly="!!customerInfo.idKhachHang && isPhoneLocked"
                    />
                    <span
                      v-if="errors.phone"
                      class="text-danger small mt-1 d-block"
                    >
                      {{ errors.phone }}
                    </span>
                  </div>
                </div>

                <div class="row g-4 mb-4">
                  <div class="col-12">
                    <label class="form-label text-muted small fw-bold mb-1"
                      ><i class="fas fa-envelope text-danger me-2"></i> EMAIL
                      <span class="text-danger">*</span></label
                    >
                    <input
                      type="email"
                      v-model="customerInfo.email"
                      class="form-control custom-input border-0 border-bottom bg-transparent px-1 rounded-0 fw-bold text-dark fs-6"
                      placeholder="Nhập email của bạn ..."
                      :readonly="!!customerInfo.idKhachHang"
                    />
                    <span
                      v-if="errors.email"
                      class="text-danger small mt-1 d-block"
                    >
                      {{ errors.email }}
                    </span>
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
                      background-color: #f8f9fa;
                      border-left: 4px solid #d32f2f !important;
                      border-radius: 6px;
                      color: #333;
                    "
                    placeholder=" Nhập ghi chú của bạn..."
                  ></textarea>
                </div>

                <div class="mt-3">
                  <button
                    type="button"
                    @click="goBack"
                    class="btn text-dark fw-bold px-4 py-2 btn-go-back"
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
                  Thêm món / Sửa món
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
                      class="btn btn-sm btn-custom-red fw-bold px-3 py-2"
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
                          class="btn btn-link text-danger p-0 mt-1 small text-decoration-none btn-delete-item"
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
                  <template v-if="cart.length > 0">
                    <div class="d-flex justify-content-between mb-2">
                      <span class="text-dark fw-bold small">Tạm tính:</span>
                      <span class="text-dark fw-bold small">{{
                        formatPrice(subTotal)
                      }}</span>
                    </div>

                    <!-- <div class="d-flex justify-content-between mb-3">
                      <span class="text-dark fw-bold small"
                        >Tổng thuế VAT:</span
                      >
                      <span class="text-dark fw-bold small">{{
                        formatPrice(taxAmount)
                      }}</span>
                    </div> -->

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
                      <span class="text-dark fw-bold small"
                        >Tiền cọc ({{ systemParams.phanTramCoc }}%):</span
                      >
                      <span class="text-dark fw-bold small">{{
                        formatPrice(depositAmount)
                      }}</span>
                    </div>

                    <hr class="border-secondary opacity-25" />

                    <div
                      class="d-flex justify-content-between align-items-center mb-4 mt-3"
                    >
                      <span class="text-dark fw-bold fs-6"
                        >Cần thanh toán:</span
                      >
                      <span class="fw-bold fs-5" style="color: #e53935">{{
                        formatPrice(depositAmount)
                      }}</span>
                    </div>
                  </template>

                  <div class="text-end">
                    <button
                      @click="submitFinalBooking"
                      :disabled="isSubmitting"
                      class="btn text-white px-5 py-2 fw-bold btn-custom-red"
                      style="font-size: 1rem"
                    >
                      ĐẶT BÀN
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
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%) !important;
  color: white !important;
  border: none;
  border-radius: 8px;
  padding: 12px;
  transition: 0.2s;
}

.btn-custom-red:hover:not(:disabled) {
  opacity: 0.9;
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

.btn-go-back {
  background-color: #c4c4c4;
  border-radius: 8px;
  transition: all 0.2s ease;
  cursor: pointer;
}
.btn-go-back:hover {
  background-color: #a8a8a8;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

/* Hiệu ứng hover cho nút Xóa món */
.btn-delete-item {
  transition: all 0.2s ease;
  cursor: pointer;
}
.btn-delete-item:hover {
  color: #8b0000 !important;
  text-decoration: underline !important;
}

/* === CSS TOÀN CỤC CHO POPUP SWEETALERT === */
/* Do SweetAlert render ngoài #app nên phải dùng :global để CSS ăn được */
:global(.swal-confirm-btn) {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%) !important;
  color: white !important;
  border: none !important;
  padding: 12px 30px !important;
  font-size: 1.1rem !important;
  border-radius: 10px !important;
  box-shadow: 0 4px 12px rgba(125, 22, 26, 0.2) !important;
  transition: all 0.2s ease !important;
  margin: 0 8px !important;
  cursor: pointer !important;
}

:global(.swal-confirm-btn:hover) {
  opacity: 0.85 !important;
  transform: translateY(-2px) !important;
}

:global(.swal-cancel-btn) {
  background-color: #f3f4f6 !important;
  color: #333 !important;
  border: none !important;
  padding: 12px 30px !important;
  font-size: 1.1rem !important;
  border-radius: 10px !important;
  transition: all 0.2s ease !important;
  margin: 0 8px !important;
  cursor: pointer !important;
}

:global(.swal-cancel-btn:hover) {
  background-color: #d1d5db !important; /* Đậm hơn khi hover */
}
</style>
