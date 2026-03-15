<script setup>
import "@vuepic/vue-datepicker/dist/main.css";
import { onMounted, ref, watch, computed } from "vue";
import dayjs from "dayjs";
import {
  createPhieuDatBanFullService,
  fetchAllBanAn,
  fetchAllCustomers,
  fetchTableStatusByDate,
  searchDatBanService,
  checkBanTrongService,
} from "@/services/tableManageService";
import Multiselect from "@vueform/multiselect";
import "@vueform/multiselect/themes/default.css";
import Swal from "sweetalert2";

//---- --- Khai báo State ---
const listPhieuDatBan = ref([]);
const totalPages = ref(0);
const currentPage = ref(1);
const pageSize = ref(6);
const refreshKey = ref(0);
const isSubmitting = ref(false);

const searchForm = ref({
  soDienThoai: "",
  trangThai: "",
  ngayDat: "",
});

// 🚨 ĐÃ SỬA: Thêm danhSachBanChon vào form để lưu mảng bàn
const createForm = ref({
  danhSachBanChon: [], // Chứa mảng object các bàn được chọn
  idKhachHang: "",
  tenKhachHang: "",
  soDienThoai: "",
  email: "",
  ngaySinh: "",
  gioiTinh: true,
  thoiGianDat: "",
  soLuongKhach: 1,
  hinhThucDat: 2,
});

const formErrors = ref({
  tenKhachHang: "",
  soDienThoai: "",
  email: "",
  thoiGianDat: "",
  soLuongKhach: "",
  idBanAn: "", // Dùng chung key lỗi cho Bàn
});

const showCreateModal = ref(false);
const listBanAn = ref([]);
const customerOptions = ref([]);
const selectedCustomer = ref(null);
const isOldCustomer = ref(false);

const allTables = ref([]);
const tableStatusMap = ref({});

const normalizeBookingDateTime = (value) => {
  if (!value) return "";
  const parsed = dayjs(value);
  return parsed.isValid() ? parsed.format("YYYY-MM-DDTHH:mm:ss") : "";
};

const checkTablesByDate = async (thoiGian) => {
  if (!thoiGian) {
    tableStatusMap.value = {};
    return;
  }
  try {
    const data = await fetchTableStatusByDate(thoiGian);
    tableStatusMap.value = Object.fromEntries(data.map((b) => [b.banId, b]));
  } catch (err) {
    console.error("Lỗi check bàn theo ngày:", err);
  }
};

// ===== LOAD DATA =====
const loadTables = async () => {
  const data = await fetchAllBanAn();
  allTables.value = data.map((b) => ({
    value: b.id,
    label: `${b.maBan} - ${b.tenKhuVuc} (Tầng ${b.soTang})`,
    raw: b,
  }));
};

loadTables();

// ================= SEARCH =================
const buildSearchPayload = () => {
  const payload = {
    soDienThoai: searchForm.value.soDienThoai?.trim() || "",
    trangThai:
      searchForm.value.trangThai === "" ? null : searchForm.value.trangThai,
    ngayDat: null,
  };

  if (searchForm.value.ngayDat) {
    const d = new Date(searchForm.value.ngayDat);
    const offset = d.getTimezoneOffset();
    const localDate = new Date(d.getTime() - offset * 60 * 1000);
    payload.ngayDat = localDate.toISOString().split("T")[0];
  }
  return payload;
};

const searchDatBan = async () => {
  try {
    const data = await searchDatBanService({
      payload: buildSearchPayload(),
      page: currentPage.value - 1,
      size: pageSize.value,
    });
    listPhieuDatBan.value = data.content;
    totalPages.value = data.totalPages;
  } catch (error) {
    console.error("Lỗi searchDatBan:", error);
  }
};

const currentStep = ref(1);

// ================= OPEN MODAL =================
// ================= OPEN MODAL =================
const openCreateModal = async () => {
  selectedCustomer.value = null;
  customerOptions.value = [];
  isOldCustomer.value = false;
  currentStep.value = 1; // 🚨 Reset về bước 1

  createForm.value = {
    danhSachBanChon: [],
    idKhachHang: "",
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    ngaySinh: "",
    gioiTinh: true,
    thoiGianDat: "",
    soLuongKhach: 1,
    hinhThucDat: 2,
  };
  resetCreateFormErrors();
  showCreateModal.value = true;

  const [banRaw, _] = await Promise.all([fetchAllBanAn(), loadAllCustomers()]);
  listBanAn.value = banRaw.map((ban, index) => {
    const defaultCol = (index % 3) * 4 + 1;
    const defaultRow = Math.floor(index / 3) * 2 + 1;
    return {
      ...ban,
      column: ban.column || defaultCol,
      row: ban.row || defaultRow,
    };
  });
};

watch(() => createForm.value.thoiGianDat, (newVal) => {
  if (newVal) {
    // So sánh thời gian vừa chọn với thời gian hiện tại (tính tới phút)
    if (dayjs(newVal).isBefore(dayjs(), 'minute')) {
      formErrors.value.thoiGianDat = "Thời gian đặt không được ở trong quá khứ!";
    } else {
      formErrors.value.thoiGianDat = ""; // Xóa lỗi nếu chọn đúng
    }
  }
});


// Thêm hàm load all
const allCustomers = ref([]);
const loadAllCustomers = async () => {
  try {
    const data = await fetchAllCustomers(); 
    allCustomers.value = data.map((c) => ({
      value: c.idKhachHang,
      label: `${c.tenKhachHang} - ${c.soDienThoai}`,
      raw: c,
    }));
    customerOptions.value = allCustomers.value;
  } catch (err) {
    console.error("Load customers error:", err);
  }
};

// ================= SEARCH CUSTOMER =================
const searchCustomer = async (query) => {
  if (!query || query.trim() === "") {
    customerOptions.value = allCustomers.value;
    return;
  }

  const q = query.toLowerCase();
  const filtered = allCustomers.value.filter(
    (c) =>
      c.raw.tenKhachHang?.toLowerCase().includes(q) ||
      c.raw.soDienThoai?.includes(q),
  );

  if (filtered.length === 0) {
    customerOptions.value = [
      {
        value: "create-new",
        label: `➕ Thêm khách mới: ${query}`,
        isNew: true,
        phone: query,
      },
    ];
  } else {
    customerOptions.value = filtered;
  }
};

const handleSelectCustomer = (value) => {
  const option = customerOptions.value.find((o) => o.value === value);
  if (!option) return;

  if (option.isNew) {
    isOldCustomer.value = false;
    createForm.value.idKhachHang = null;
    createForm.value.soDienThoai = option.phone;
    createForm.value.tenKhachHang = "";
    createForm.value.email = "";
    createForm.value.ngaySinh = "";
    createForm.value.gioiTinh = true;
    formErrors.value.tenKhachHang = "";
    formErrors.value.soDienThoai = "";
    formErrors.value.email = "";
    return;
  }

  const c = option.raw;
  isOldCustomer.value = true;
  createForm.value.idKhachHang = c.idKhachHang;
  createForm.value.tenKhachHang = c.tenKhachHang;
  createForm.value.soDienThoai = c.soDienThoai;
  createForm.value.email = c.email;
  createForm.value.ngaySinh = c.ngaySinh;
  createForm.value.gioiTinh = c.gioiTinh;
  formErrors.value.tenKhachHang = "";
  formErrors.value.soDienThoai = "";
  formErrors.value.email = "";
};

const clearCustomer = () => {
  isOldCustomer.value = false;
  selectedCustomer.value = null;
  createForm.value.idKhachHang = null;
};

const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
const isValidVietnamPhone = (phone) => /^(0|\+84)(3|5|7|8|9)\d{8}$/.test(phone);

const resetCreateFormErrors = () => {
  formErrors.value = {
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    thoiGianDat: "",
    soLuongKhach: "",
    idBanAn: "",
  };
};

const validateCreateForm = () => {
  const errors = {
    tenKhachHang: "",
    soDienThoai: "",
    email: "",
    thoiGianDat: "",
    soLuongKhach: "",
    idBanAn: "", 
  };

  const tenKhachHang = (createForm.value.tenKhachHang || "").trim();
  const soDienThoai = (createForm.value.soDienThoai || "").trim();
  const email = (createForm.value.email || "").trim();
  const thoiGianDat = normalizeBookingDateTime(createForm.value.thoiGianDat);
  const soLuongKhach = Number(createForm.value.soLuongKhach);

  if (!tenKhachHang) {
    errors.tenKhachHang = "Vui lòng nhập tên khách hàng";
  } else if (tenKhachHang.length < 2) {
    errors.tenKhachHang = "Tên khách hàng phải có ít nhất 2 ký tự";
  }

  if (!soDienThoai) {
    errors.soDienThoai = "Vui lòng nhập số điện thoại";
  } else if (!isValidVietnamPhone(soDienThoai)) {
    errors.soDienThoai = "Số điện thoại không đúng định dạng";
  }

  if (email && !isValidEmail(email)) {
    errors.email = "Email không đúng định dạng";
  }

  if (!thoiGianDat) {
    errors.thoiGianDat = "Vui lòng chọn thời gian đặt";
  } else if (dayjs(thoiGianDat).isSame(dayjs()) || dayjs(thoiGianDat).isBefore(dayjs())) {
    errors.thoiGianDat = "Thời gian đặt phải lớn hơn ngày giờ hiện tại";
  }

  if (!Number.isInteger(soLuongKhach) || soLuongKhach < 1) {
    errors.soLuongKhach = "Số lượng khách phải lớn hơn 0";
  }
  
  if (!createForm.value.danhSachBanChon || createForm.value.danhSachBanChon.length === 0) {
    errors.idBanAn = "Vui lòng chọn ít nhất 1 bàn!";
  }

  formErrors.value = errors;
  return !Object.values(errors).some(Boolean);
};

// ========================================================
// 🚨 LOGIC CHỌN BÀN TRÊN SƠ ĐỒ KHI TẠO PHIẾU MỚI
// ========================================================
const showSelectTableModal = ref(false);
const tempSelectedTables = ref([]); 
const modalActiveFloor = ref(1);

const danhSachTangModal = computed(() => {
  if (!listBanAn.value || listBanAn.value.length === 0) return [];
  const floors = listBanAn.value.map(ban => Number(ban.soTang || ban.tang || 1));
  return [...new Set(floors)].sort((a, b) => a - b);
});

const banTheoTangModal = computed(() => {
  return listBanAn.value.filter(ban => Number(ban.soTang || ban.tang || 1) === modalActiveFloor.value);
});

const totalTempCapacity = computed(() => {
  return tempSelectedTables.value.reduce((sum, ban) => sum + (Number(ban.soCho || ban.soNguoiToiDa) || 0), 0);
});

const openTableSelectionModal = async () => {
  if (!createForm.value.thoiGianDat) {
    return Swal.fire('Lưu ý', 'Vui lòng chọn <b>Thời gian đặt</b> trước!', 'warning');
  }
  if (!createForm.value.soLuongKhach || createForm.value.soLuongKhach < 1) {
    return Swal.fire('Lưu ý', 'Vui lòng nhập <b>Số lượng khách</b> hợp lệ trước!', 'warning');
  }

  if (dayjs(createForm.value.thoiGianDat).isBefore(dayjs(), 'minute')) {
    formErrors.value.thoiGianDat = "Thời gian đặt không được ở trong quá khứ!";
    return Swal.fire('Lưu ý', 'Thời gian đặt không được ở trong quá khứ!', 'error');
  }

  Swal.fire({ title: 'Đang kiểm tra sức chứa...', didOpen: () => Swal.showLoading() });

  try {
    // 1. Định dạng lại dữ liệu khớp 100% với class DatBanRequest trong Java
    const dateObj = dayjs(createForm.value.thoiGianDat);
    const payload = {
        ngayDat: dateObj.format('YYYY-MM-DD'), // Trả ra dạng: 2026-03-15
        gioDat: dateObj.format('HH:mm:ss'),    // Trả ra dạng: 18:30:00
        soNguoi: createForm.value.soLuongKhach // Chú ý: Backend đang gọi request.getSoNguoi() nên key phải là soNguoi
    };

    // 2. Gọi API kèm payload
    const availableTables = await checkBanTrongService(payload);

    // 3. Xử lý logic chặn nếu danh sách bàn trả về rỗng
    if (!availableTables || availableTables.length === 0) {
        Swal.close();
        createForm.value.danhSachBanChon = []; 
        return Swal.fire({
            icon: 'error',
            title: 'Từ chối đặt bàn!',
            text: 'Nhà hàng đã hết bàn hoặc vượt quá 90% sức chứa tại thời điểm này. Vui lòng chọn giờ khác!'
        });
    }

    // 4. Nếu BE trả về bàn trống -> Cập nhật trạng thái lưới và mở Sơ đồ
    const tempMap = {};
    // Khóa (Màu xám/Trạng thái 2) tất cả các bàn trước
    listBanAn.value.forEach(ban => { tempMap[ban.id] = { trangThai: 2 }; });
    // Bàn nào rỗng thì mở sáng lên (Trạng thái 0)
    availableTables.forEach(ban => { tempMap[ban.id] = { trangThai: 0 }; });

    tableStatusMap.value = tempMap;

    if (danhSachTangModal.value.length > 0) {
        modalActiveFloor.value = danhSachTangModal.value[0];
    }
    
    tempSelectedTables.value = [...(createForm.value.danhSachBanChon || [])];
    
    Swal.close();
    showSelectTableModal.value = true;

  } catch (error) {
    Swal.close();
    console.error("Lỗi khi gọi API checkBanTrong:", error);
    Swal.fire('Lỗi', 'Hệ thống gián đoạn hoặc sai định dạng dữ liệu gửi lên', 'error');
  }
};

const nextStep = () => {
    let isValid = true;
    resetCreateFormErrors();

    if (!createForm.value.thoiGianDat) {
        formErrors.value.thoiGianDat = "Vui lòng chọn thời gian đặt";
        isValid = false;
    } 
    else if (dayjs(createForm.value.thoiGianDat).isBefore(dayjs(), 'minute')) {
        formErrors.value.thoiGianDat = "Thời gian đặt không được ở trong quá khứ!";
        isValid = false;
    }

    if (!createForm.value.soLuongKhach || createForm.value.soLuongKhach < 1) {
        formErrors.value.soLuongKhach = "Số lượng khách phải lớn hơn 0";
        isValid = false;
    }
    
    if (createForm.value.danhSachBanChon.length === 0) {
        formErrors.value.idBanAn = "Vui lòng chọn ít nhất 1 bàn phục vụ";
        isValid = false;
    }

    if (isValid) {
        currentStep.value = 2; // Chuyển sang bước nhập thông tin khách
    }
};

const toggleTableForCreate = (ban) => {
  const status = tableStatusMap.value[String(ban.id)]?.trangThai || 0;
  if (status !== 0) {
    return Swal.fire({toast: true, position: 'top-end', icon: 'error', title: 'Bàn đã có lịch lúc này!', showConfirmButton: false, timer: 1500});
  }

  const idx = tempSelectedTables.value.findIndex(b => b.id === ban.id);
  if (idx !== -1) {
    tempSelectedTables.value.splice(idx, 1); 
  } else {
    // 🚨 ĐÃ SỬA: Chặn nếu đã chọn đủ số người
    if (totalTempCapacity.value >= createForm.value.soLuongKhach) {
      return Swal.fire({
        toast: true, 
        position: 'top-end', 
        icon: 'warning', 
        title: 'Đã đủ chỗ, không cần chọn thêm bàn!', 
        showConfirmButton: false, 
        timer: 2000
      });
    }
    tempSelectedTables.value.push(ban); 
  }
};

const confirmTableSelection = () => {
  if (totalTempCapacity.value < createForm.value.soLuongKhach) {
    Swal.fire({
      title: 'Chưa đủ chỗ!',
      text: `Khách đi ${createForm.value.soLuongKhach} người nhưng các bàn chọn chỉ chứa được ${totalTempCapacity.value} người. Kê thêm ghế?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#7d161a',
      confirmButtonText: 'Vẫn tiếp tục',
      cancelButtonText: 'Chọn thêm bàn'
    }).then((result) => {
      if (result.isConfirmed) {
        createForm.value.danhSachBanChon = [...tempSelectedTables.value];
        showSelectTableModal.value = false;
        formErrors.value.idBanAn = "";
      }
    });
  } else {
    createForm.value.danhSachBanChon = [...tempSelectedTables.value];
    showSelectTableModal.value = false;
    formErrors.value.idBanAn = "";
  }
};

const removeSelectedTable = (index) => {
  createForm.value.danhSachBanChon.splice(index, 1);
};


// ================= SUBMIT =================
const submitCreate = async () => {
  if (!validateCreateForm()) return;

  isSubmitting.value = true; 

  try {
    const thoiGianDat = normalizeBookingDateTime(createForm.value.thoiGianDat);

    const payload = { 
      ...createForm.value, 
      thoiGianDat: thoiGianDat,
      danhSachIdBanAn: createForm.value.danhSachBanChon.map(b => b.id)
    };

    await createPhieuDatBanFullService(payload);

    showCreateModal.value = false;
    refreshKey.value += 1;

    // 🚨 ĐÃ SỬA: Chỉ hiện thông báo tạo thành công, không nhắc đến Email nữa
    showToast("success", "✅ Thành công!", "Đã tạo phiếu đặt bàn mới.");

  } catch (error) {
    showToast("error", "❌ Tạo phiếu thất bại", error.response?.data?.message || "Có lỗi xảy ra, vui lòng thử lại.");
  } finally {
    isSubmitting.value = false; 
  }
};

const closeCreateModal = () => {
  showCreateModal.value = false;
  selectedCustomer.value = null;
  customerOptions.value = [];
  resetCreateFormErrors();
};

// ===== TOAST =====
const toast = ref({
  show: false,
  type: "success",
  title: "",
  message: "",
});

const showToast = (type, title, message, duration = 4000) => {
  toast.value = { show: true, type, title, message };
  setTimeout(() => {
    toast.value.show = false;
  }, duration);
};

onMounted(() => {
  searchDatBan();
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
      <div class="header-manager">
        <div>
          <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem">
            Quản lý phiếu đặt bàn
          </h3>
        </div>
        <div>
          <button class="btn btn-add" @click="openCreateModal">
            + Thêm phiếu đặt bàn
          </button>
        </div>
      </div>
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link class="nav-link" exact-active-class="active" to="/admin/tables">
            <i class="fa-solid fa-list"></i> Danh sách phiếu
          </router-link>
        </li>
        <li class="nav-item">
          <router-link class="nav-link" active-class="active" to="/admin/tables/calendar">
            <i class="fa-regular fa-calendar"></i> Lịch đặt bàn
          </router-link>
        </li>
      </ul>
      <hr />
      <div class="contain-frame mt-3">
        <router-view :key="refreshKey" />
      </div>
    </div>
  </div>

  <div v-if="showCreateModal" class="modal-overlay">
    <div class="modal-box">
      
      <div class="modal-header border-bottom pb-3 mb-3">
        <h4 class="m-0 text-danger fw-bold">
          <i class="fa-solid fa-clipboard-user me-2"></i>Thêm phiếu đặt bàn
          <span class="fs-6 text-muted fw-normal ms-2">
            {{ currentStep === 1 ? '(1/2: Chọn bàn)' : '(2/2: Khách hàng)' }}
          </span>
        </h4>
        <button class="close-btn" @click="closeCreateModal">✕</button>
      </div>

      <div v-if="currentStep === 1" class="step-1-animation">
        <div class="info-grid">
            <div>
              <label>Thời gian đặt <span class="required-star">*</span></label>
              <VueDatePicker v-model="createForm.thoiGianDat" :enable-time-picker="true" :is-24="true"
                :min-date="new Date()" :hide-input-icon="true" :clearable="false" auto-apply format="dd/MM/yyyy HH:mm" placeholder="Chọn ngày giờ"
                class="custom-datetime-picker" :class="{ 'input-error': formErrors.thoiGianDat }" />
              <small v-if="formErrors.thoiGianDat" class="error-text">{{ formErrors.thoiGianDat }}</small>
            </div>

            <div>
              <label>Số lượng khách <span class="required-star">*</span></label>
              <input type="number" min="1" step="1" v-model.number="createForm.soLuongKhach"
                :class="{ 'input-error': formErrors.soLuongKhach }" />
              <small v-if="formErrors.soLuongKhach" class="error-text">{{ formErrors.soLuongKhach }}</small>
            </div>

            <div style="grid-column: span 2;">
              <label>Bàn phục vụ <span class="required-star">*</span></label>
              <div class="d-flex align-items-center gap-2 mb-2">
                <button class="btn btn-sm btn-custom-outline px-3 py-2 fw-bold" @click="openTableSelectionModal"
                  style="border-radius: 6px;">
                  <i class="fa-solid fa-map-location-dot me-1"></i> Chọn bàn trên sơ đồ
                </button>
                <span v-if="createForm.danhSachBanChon?.length > 0" class="small fw-bold text-success">
                  Đã chọn {{ createForm.danhSachBanChon.length }} bàn
                </span>
              </div>
              <div class="d-flex flex-wrap gap-2 p-2 border rounded bg-light" style="min-height: 50px;">
                <div v-for="(ban, index) in createForm.danhSachBanChon" :key="ban.id"
                  class="badge bg-danger d-flex align-items-center py-2 px-3" style="font-size: 13px;">
                  <span>Bàn {{ ban.maBan }} ({{ ban.soCho || ban.soNguoiToiDa }} chỗ)</span>
                  <i class="fa-solid fa-xmark ms-2" style="cursor: pointer" @click="removeSelectedTable(index)" title="Xóa bàn này"></i>
                </div>
                <div v-if="!createForm.danhSachBanChon?.length" class="text-muted small fst-italic w-100 text-center mt-1">
                  Vui lòng chọn thời gian, số lượng và bấm chọn bàn trên sơ đồ.
                </div>
              </div>
              <small v-if="formErrors.idBanAn" class="error-text">{{ formErrors.idBanAn }}</small>
            </div>
        </div>

        <div class="d-flex justify-content-end mt-4 pt-3 border-top">
          <button class="btn btn-custom-outline me-2" @click="closeCreateModal">Hủy</button>
          <button class="btn px-4" @click="nextStep">Tiếp tục <i class="fa-solid fa-arrow-right ms-2"></i></button>
        </div>
      </div>

      <div v-if="currentStep === 2" class="step-2-animation">
        
        <label>Tìm khách hàng cũ (Theo SĐT)</label>
        <Multiselect v-model="selectedCustomer" :options="customerOptions" :searchable="true" :filter-results="false"
          value-prop="value" label="label" track-by="value" placeholder="Tìm theo tên hoặc SĐT..."
          no-options-text="Không có khách hàng" no-results-text="Không tìm thấy" class="custom-filter-multiselect mb-3"
          @open="() => { customerOptions = allCustomers; }" @search-change="searchCustomer" @select="handleSelectCustomer"
          @clear="clearCustomer">
          <template #option="{ option }">
            <div v-if="option.isNew" style="color: #7d161a; font-weight: bold">
              {{ option.label }}
            </div>
            <div v-else>
              <span style="font-weight: 600">{{ option.raw.tenKhachHang }}</span>
              <span style="color: #888; margin-left: 8px">{{ option.raw.soDienThoai }}</span>
            </div>
          </template>
        </Multiselect>

        <div class="info-grid">
          <div>
            <label>Tên khách <span class="required-star">*</span></label>
            <input v-model="createForm.tenKhachHang" :disabled="isOldCustomer" placeholder="Tên khách"
              :class="{ 'input-error': formErrors.tenKhachHang }" />
            <small v-if="formErrors.tenKhachHang" class="error-text">{{ formErrors.tenKhachHang }}</small>
          </div>

          <div>
            <label>Số điện thoại <span class="required-star">*</span></label>
            <input v-model="createForm.soDienThoai" :disabled="isOldCustomer" placeholder="SĐT"
              :class="{ 'input-error': formErrors.soDienThoai }" />
            <small v-if="formErrors.soDienThoai" class="error-text">{{ formErrors.soDienThoai }}</small>
          </div>

          <div>
            <label>Email</label>
            <input v-model="createForm.email" :disabled="isOldCustomer" placeholder="Email"
              :class="{ 'input-error': formErrors.email }" />
            <small v-if="formErrors.email" class="error-text">{{ formErrors.email }}</small>
          </div>

          <div>
            <label>Giới tính</label>
            <select v-model="createForm.gioiTinh" :disabled="isOldCustomer">
              <option :value="true">Nam</option>
              <option :value="false">Nữ</option>
            </select>
          </div>

          <div>
            <label>Ngày sinh</label>
            <input type="date" v-model="createForm.ngaySinh" :disabled="isOldCustomer" />
          </div>
        </div>

        <div class="d-flex justify-content-between mt-4 pt-3 border-top">
          <button class="btn btn-custom-outline" @click="currentStep = 1"><i class="fa-solid fa-arrow-left me-2"></i> Quay lại</button>
          <button class="btn px-4" @click="submitCreate" :disabled="isSubmitting">
            <i v-if="isSubmitting" class="fa-solid fa-spinner fa-spin me-1"></i>
            {{ isSubmitting ? 'Đang lưu...' : 'Hoàn tất tạo phiếu' }}
          </button>
        </div>
      </div>

    </div>
  </div>

  <div v-if="showSelectTableModal" class="modal-overlay" style="z-index: 10005;">
    <div class="modal-box-2 modal-fullscreen d-flex flex-column bg-light">
      <div class="modal-header-custom bg-white shadow-sm z-1">
        <h4 class="modal-title-custom text-danger m-0">
          <i class="fa-solid fa-chair me-2"></i> Chọn bàn cho khách ({{ createForm.soLuongKhach }} người)
        </h4>
        <button class="close-btn" @click="showSelectTableModal = false">✕</button>
      </div>

      <div class="modal-body-custom p-3 d-flex flex-column h-100 overflow-hidden">
        
        <div class="d-flex justify-content-between align-items-center mb-3 flex-shrink-0">
            <div class="alert alert-info mb-0 border-0 shadow-sm flex-grow-1 me-4 d-flex align-items-center justify-content-between">
                <div>
                  <i class="fa-solid fa-clock me-2 text-primary fs-5"></i>
                  <span>Kiểm tra bàn trống lúc: <b>{{ dayjs(createForm.thoiGianDat).format('HH:mm - DD/MM/YYYY') }}</b></span>
                </div>
                <div class="fw-bold fs-5" :class="totalTempCapacity >= createForm.soLuongKhach ? 'text-success' : 'text-danger'">
                  Đã đáp ứng: {{ totalTempCapacity }} / {{ createForm.soLuongKhach }} chỗ
                </div>
            </div>
            
            <div class="d-flex bg-white p-1 rounded shadow-sm border">
                <button v-for="tang in danhSachTangModal" :key="'select-tang-' + tang" 
                        class="btn px-4 py-2 fw-bold" 
                        :class="modalActiveFloor === tang ? 'btn-active' : 'text-muted'" 
                        @click="modalActiveFloor = tang" 
                        style="border-radius: 6px; border: none;">
                    Tầng {{ tang }}
                </button>
            </div>
        </div>

        <div class="floor-plan-layout flex-grow-1 overflow-hidden d-flex gap-3">
            <div class="floor-plan-section flex-grow-1 bg-white shadow-sm border rounded">
              <div class="grid-container h-100 overflow-auto">
                  <div class="grid-canvas">
                    <div v-for="ban in banTheoTangModal" :key="'grid-'+ban.id" class="table-card" 
                        :class="{
                          'is-dimmed': (tableStatusMap[String(ban.id)]?.trangThai !== 0 && tableStatusMap[String(ban.id)]?.trangThai !== undefined) && !tempSelectedTables.some(b => b.id === ban.id), 
                          'is-selected-merge': tempSelectedTables.some(b => b.id === ban.id) 
                        }" 
                        :style="{gridColumnStart: ban.column, gridRowStart: ban.row, gridColumnEnd: 'span 3', gridRowEnd: 'span 2', cursor: (tableStatusMap[String(ban.id)]?.trangThai === 0 || tableStatusMap[String(ban.id)]?.trangThai === undefined) ? 'pointer' : 'not-allowed'}" 
                        @click="toggleTableForCreate(ban)">
                        
                        <div v-if="tempSelectedTables.some(b => b.id === ban.id)" class="checked-icon-overlay">
                            <i class="fa-solid fa-check"></i>
                        </div>

                        <div class="table-content text-center pt-2">
                            <strong class="fs-5">{{ ban.maBan }}</strong>
                            <div class="text-muted small mb-2"><i class="fa-solid fa-users text-secondary"></i> {{ ban.soCho || ban.soNguoiToiDa }} chỗ</div>
                            
                            <div v-if="tempSelectedTables.some(b => b.id === ban.id)" class="status-tag bg-success text-white w-100 rounded-pill"><i class="fa-solid fa-check-double"></i> Đã chọn</div>
                            <div v-else-if="tableStatusMap[String(ban.id)]?.trangThai !== 0 && tableStatusMap[String(ban.id)]?.trangThai !== undefined" class="status-tag bg-secondary text-white w-100 rounded-pill"><i class="fa-solid fa-ban"></i> Đã có lịch</div>
                            <div v-else class="status-tag status-empty w-100 rounded-pill"><i class="fa-solid fa-check"></i> Còn trống</div>
                        </div>
                    </div>
                  </div>
              </div>
            </div>

            <div class="bg-white shadow-sm border rounded p-3 d-flex flex-column" style="width: 350px;">
                <h6 class="fw-bold mb-3 pb-2 border-bottom text-success flex-shrink-0">
                  <i class="fa-solid fa-list-check me-2"></i> Bàn trống có thể chọn
                </h6>
                <div class="overflow-auto flex-grow-1 pe-2" style="max-height: calc(100vh - 250px);">
                   <div v-if="banTheoTangModal.filter(b => tableStatusMap[String(b.id)]?.trangThai === 0 || tableStatusMap[String(b.id)]?.trangThai === undefined).length === 0" class="text-center text-muted mt-5">
                       <i class="fa-solid fa-box-open fa-3x mb-3 text-light"></i>
                       <p>Không có bàn nào trống tại tầng này!</p>
                   </div>
                   
                   <div v-else class="row g-2">
                      <div v-for="ban in banTheoTangModal.filter(b => tableStatusMap[String(b.id)]?.trangThai === 0 || tableStatusMap[String(b.id)]?.trangThai === undefined)" :key="'list-'+ban.id" class="col-6">
                         
                         <div class="mini-table-card border rounded p-3 text-center transition-all" 
                              :class="{ 'is-selected-list': tempSelectedTables.some(b => b.id === ban.id) }" 
                              @click="toggleTableForCreate(ban)">
                              
                            <div v-if="tempSelectedTables.some(b => b.id === ban.id)" class="checked-icon-overlay-mini">
                                <i class="fa-solid fa-check"></i>
                            </div>

                            <div class="fw-bold fs-5" :class="tempSelectedTables.some(b => b.id === ban.id) ? 'text-white' : 'text-success'">{{ ban.maBan }}</div>
                            <div class="small mb-1" :class="tempSelectedTables.some(b => b.id === ban.id) ? 'text-white' : 'text-muted'">
                                <i class="fa-solid fa-users"></i> {{ ban.soCho || ban.soNguoiToiDa }} chỗ
                            </div>
                            <div class="badge border" :class="tempSelectedTables.some(b => b.id === ban.id) ? 'bg-white text-success border-white' : 'bg-light text-dark'">
                                Tầng {{ ban.soTang || ban.tang }}
                            </div>
                         </div>

                      </div>
                   </div>
                </div>
            </div>
        </div>

        <div class="mt-3 pt-3 border-top bg-white px-3 py-2 rounded shadow-sm d-flex justify-content-between flex-shrink-0">
            <button class="btn btn-outline-secondary px-4 fw-bold" @click="showSelectTableModal = false"><i class="fa-solid fa-arrow-left me-1"></i> Quay lại</button>
            <button class="btn btn-update-status px-5 fw-bold" style="width: auto; background-color: #28a745;" 
                    :disabled="tempSelectedTables.length === 0" 
                    @click="confirmTableSelection">
                Xác nhận chọn {{ tempSelectedTables.length }} bàn <i class="fa-solid fa-check-double ms-2"></i>
            </button>
        </div>
      </div>
    </div>
  </div>

  <Transition name="toast">
    <div v-if="toast.show" :class="['toast-noti', `toast-${toast.type}`]">
      <div class="toast-icon">
        <span v-if="toast.type === 'success'">✅</span>
        <span v-else-if="toast.type === 'warning'">⚠️</span>
        <span v-else>❌</span>
      </div>
      <div class="toast-content">
        <div class="toast-title">{{ toast.title }}</div>
        <div class="toast-msg">{{ toast.message }}</div>
      </div>
      <button class="toast-close" @click="toast.show = false">×</button>
    </div>
  </Transition>
</template>

<style scoped>
/* ================= LAYOUT ================= */
.layout-table {
  display: flex;
  background: #fff;
}

.navbar-search {
  width: 100%;
}

.grid-canvas {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  
  /* 🚨 ĐÃ FIX: Chuyển 1fr thành 100px. Bắt buộc fix cứng chiều cao mỗi hàng 
     thì Grid mới biết đường trải rộng các bàn ra thành 2D */
  grid-template-rows: repeat(15, 100px); 
  
  gap: 15px;
  padding: 20px;
  width: 100%;
  min-width: 800px;
  box-sizing: border-box;
  background-image:
    linear-gradient(to right, #eee 1px, transparent 1px),
    linear-gradient(to bottom, #eee 1px, transparent 1px);
  background-size: calc(100% / 12) 100px;
}

.header-manager {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

hr {
  border: 0;
  border-top: 2px solid #7d161a;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-box {
  width: 700px;
  background: #fff;
  border-radius: 15px;
  padding: 25px;
  animation: fadeIn 0.2s ease-in-out;
}

.modal-box-2 {
  width: 70%;
  background: #fff;
  border-radius: 15px;
  padding: 25px;
  animation: fadeIn 0.2s ease-in-out;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.close-btn {
  border: none;
  background: none;
  font-size: 22px;
  cursor: pointer;
  color: #888;
}

.close-btn:hover {
  color: #333;
}

.custom-filter-multiselect {
  --ms-border-color: #ddd;
  --ms-radius: 8px;
  --ms-ring-color: rgba(125, 22, 26, 0.15);
  --ms-border-color-active: #7d161a;
  --ms-option-bg-pointed: #fcf4f4;
  --ms-option-color-pointed: #7d161a;
  --ms-option-bg-selected: #7d161a;
  --ms-option-color-selected: #ffffff;
  --ms-option-bg-selected-pointed: #5f0f12;
}

.custom-filter-multiselect :deep(.multiselect-option.is-selected) {
  background-color: #7d161a !important;
  color: white !important;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }

  to {
    opacity: 1;
    transform: scale(1);
  }
}

/* ================= NAV TABS ================= */
.nav-tabs .nav-link {
  color: #7d161a !important;
  font-weight: 600;
  border-radius: 10px 10px 0 0;
  transition: 0.2s;
}

.nav-tabs .nav-link:hover {
  background: #fcf4f4;
  color: #5a0f12 !important;
}

.nav-tabs .nav-link.active {
  font-weight: bold;
  background: #fff;
  border-color: #dee2e6 #dee2e6 #fff;
}

.contain-frame {
  margin-top: 15px;
}

/* ================= BUTTON ================= */
.btn {
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%);
  color: white !important;
  transition: 0.2s;
  border: none;
}

.btn:hover {
  transform: scale(1.04);
}

.btn-outline {
  background: white !important;
  color: #333 !important;
  border: 1px solid #ccc !important;
}

.btn-outline:hover {
  border-color: #7d161a !important;
  color: #7d161a !important;
  background-color: #fff5f5 !important;
}

.btn-active {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%) !important;
  color: white !important;
  border: 1px solid #7d161a !important;
  cursor: default;
}

.btn-custom-outline {
  background: transparent !important;
  color: #7d161a !important;
  border: 1px solid #7d161a !important;
  transition: 0.2s;
}

.btn-custom-outline:hover {
  background: #7d161a !important;
  color: #fff !important;
}

/* ================= GRID FORM ================= */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 18px 0;
}

.info-grid label {
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.required-star {
  color: #dc3545;
  margin-left: 2px;
  font-weight: 700;
}

.info-grid input,
.info-grid select {
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #ddd;
  transition: 0.2s;
}

.info-grid input:focus,
.info-grid select:focus {
  border-color: #7d161a;
  box-shadow: 0 0 0 2px rgba(125, 22, 26, 0.15);
  outline: none;
}

.custom-datetime-picker :deep(.dp__input) {
  width: 100%;
  padding: 8px 10px 8px 35px !important; /* 🚨 ĐÃ FIX: Thụt lề trái 35px để chừa chỗ cho Icon */
  border-radius: 8px;
  border: 1px solid #ddd;
}

.custom-datetime-picker.input-error :deep(.dp__input) {
  border-color: #dc3545 !important;
}

/* Xóa luôn class ẩn icon nếu có để icon hiện đẹp tự nhiên */
.custom-datetime-picker :deep(.dp__input_icon) {
  /* display: none !important; Xóa dòng này đi */
  color: #7d161a; /* Đổi màu icon cho đẹp */
}

.input-error {
  border-color: #dc3545 !important;
}

.error-text {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #dc3545;
}

/* ================= BÀN CARD ================= */
.ban-grid {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.ban-card {
  width: 130px;
  height: 100px;
  border: 2px solid #ccc;
  border-radius: 10px;
  text-align: center;
  padding-top: 15px;
  cursor: pointer;
  transition: 0.2s;
  background: white;
}

.ban-card:hover:not(.disabled) {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
  border-color: #7d161a;
}

.ban-card.selected {
  border-color: #28a745;
  background: #f0fdf4;
}

.ban-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: #f8f9fa;
}

.ban-name {
  font-weight: bold;
  font-size: 18px;
  color: #333;
}

/* ================= TOAST ================= */
.toast-noti {
  position: fixed;
  bottom: 28px;
  right: 28px;
  z-index: 99999;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-width: 320px;
  max-width: 420px;
  padding: 16px 18px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  background: #fff;
  border-left: 5px solid #ccc;
}

.toast-success {
  border-left-color: #27ae60;
}

.toast-warning {
  border-left-color: #f39c12;
}

.toast-error {
  border-left-color: #e74c3c;
}

.toast-icon {
  font-size: 22px;
  line-height: 1;
  margin-top: 2px;
}

.toast-content {
  flex: 1;
}

.toast-title {
  font-weight: 700;
  font-size: 14px;
  color: #222;
  margin-bottom: 4px;
}

.toast-msg {
  font-size: 13px;
  color: #555;
  line-height: 1.5;
}

.toast-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #aaa;
  cursor: pointer;
  padding: 0;
  margin-top: -2px;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(40px);
}

.is-dimmed {
  opacity: 0.4 !important;
  filter: grayscale(100%) !important;
}

.is-selected-merge {
  border: 3px solid #28a745 !important;
  background-color: #f0fdf4 !important;
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(40, 167, 69, 0.4) !important;
  opacity: 1 !important;
  filter: grayscale(0%) !important;
  z-index: 10;
  position: relative;
}

.checked-icon-overlay {
  position: absolute;
  top: -10px;
  right: -10px;
  background: #28a745;
  color: white;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 16px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.3);
  z-index: 20;
}

.checked-icon-overlay-mini {
  position: absolute;
  top: -8px;
  right: -8px;
  background: white;
  color: #28a745;
  border-radius: 50%;
  width: 22px;
  height: 22px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
  z-index: 20;
}

/* Mini Table Card cho phần Danh sách (List) bên phải */
.mini-table-card {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 15px 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
  position: relative;
}

.mini-table-card:hover {
  border-color: #28a745;
  background-color: #f8fff9;
  transform: translateY(-3px);
  box-shadow: 0 4px 10px rgba(40, 167, 69, 0.15);
}

.mini-table-card.is-selected-list {
  background-color: #28a745 !important;
  border-color: #28a745 !important;
  box-shadow: 0 4px 10px rgba(40, 167, 69, 0.3) !important;
  transform: scale(1.05);
  z-index: 5;
}

.floor-plan-layout {
  display: flex;
  gap: 15px;
  height: 100%;
  width: 100%;
}

.floor-plan-section {
  flex: 2;
  display: flex;
  flex-direction: column;
}

.grid-container {
  flex-grow: 1;
  position: relative;
}

/* Kế thừa giao diện button Active của hệ thống */
.btn-active {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%) !important;
  color: white !important;
  border: 1px solid #7d161a !important;
  cursor: default;
}
</style>