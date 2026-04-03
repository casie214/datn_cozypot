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
import axiosClient from "@/services/axiosClient";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();

const Toast = Swal.mixin({
  toast: true,
  position: 'top-end',
  showConfirmButton: false,
  timer: 3000,
  timerProgressBar: true,
  background: '#ffffff',
  color: '#333',
  didOpen: (toast) => {
    toast.addEventListener('mouseenter', Swal.stopTimer)
    toast.addEventListener('mouseleave', Swal.resumeTimer)
  }
});

//---- --- Khai báo State ---
const listPhieuDatBan = ref([]);
const totalPages = ref(0);
const currentPage = ref(1);
const pageSize = ref(6);
const refreshKey = ref(0);
const isLoadingDetail = ref(false);
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

const checkUrlAndOpenDetail = () => {
    const openId = route.query.openId;
    if (openId) {
        console.log("🚀 Đang tự động mở phiếu ID:", openId);
        
        // Gọi hàm mở detail modal với ID lấy từ URL
        // Chúng ta truyền object có id để hàm openDetailModal của bạn xử lý được
        openDetailModal({ 
            id: Number(openId), 
            idDatBan: Number(openId) 
        });

        // Xóa ID trên URL sau khi đã mở để tránh việc Admin F5 trang nó lại tự mở lại
        router.replace({ query: { ...route.query, openId: undefined } });
    }
};

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

const formatDate = (time) => {
  if (!time) return "";
  // Nếu Backend trả về mảng [2024, 10, 24, 18, 30]
  if (Array.isArray(time)) {
    const [y, m, d, h, min, s] = time;
    return dayjs(new Date(y, m - 1, d, h || 0, min || 0, s || 0)).format("DD/MM/YYYY HH:mm");
  }
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

const getStatusText = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "Chờ xác nhận";
  if (status === "1") return "Đã xác nhận";
  if (status === "2") return "Đã hủy";
  if (status === "3") return "Đang phục vụ";
  if (status === "4") return "Hoàn tất";
  return "Không xác định";
};

const getStatusClass = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "bg-danger";
  if (status === "1") return "bg-danger";
  if (status === "2") return "bg-danger";
  if (status === "3") return "bg-danger";
  if (status === "4") return "bg-danger";
  return "bg-danger";
};

const mapAndGroupItems = (rawList) => {
  const validItems = rawList.filter(m => m.trangThaiMon !== 0);
  const grouped = {};

  validItems.forEach((mon) => {
    const isSet = mon.type === 'SET' || mon.idSetLau != null;
    const originalId = mon.id || mon.idChiTietMonAn || mon.idSetLau;
    const uniqueId = originalId ? (isSet ? `set_${originalId}` : `food_${originalId}`) : `unknown_${mon.tenMon}`;

    if (grouped[uniqueId]) {
      grouped[uniqueId].quantity += (mon.soLuong || 1);
    } else {
      grouped[uniqueId] = {
        id: uniqueId,
        name: mon.tenMon || 'Món không tên',
        quantity: mon.soLuong || 1,
        price: mon.donGia || 0,
        served: mon.trangThaiMon === 2,
      };
    }
  });
  return Object.values(grouped);
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

const handleCancelPhieuFromDetail = async () => {
  const idPhieuCanHuy = phieuDetail.value?.id || phieuDetail.value?.idDatBan || detailHoaDon.value?.idPhieuDatBan;
  
  if (!idPhieuCanHuy) {
    return Swal.fire('Lỗi', 'Không tìm thấy thông tin phiếu để hủy!', 'error');
  }

  const confirm = await Swal.fire({
    title: 'Xác nhận hủy phiếu?',
    html: `Bạn có chắc chắn muốn hủy phiếu <b>${phieuDetail.value?.maDatBan || phieuDetail.value?.maPhieu || 'này'}</b> không?<br><br><i>Hệ thống sẽ tự động hủy Hóa đơn và giải phóng bàn liên quan!</i>`,
    icon: 'warning',
    iconColor: '#7D161A',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#6c757d',
    confirmButtonText: '<i class="fa-solid fa-trash-can me-1"></i> Đồng ý hủy',
    cancelButtonText: 'Quay lại'
  });

  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang hủy phiếu...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    // 🚀 GỌI API MỚI CỦA BÁC
    // Lưu ý: Đổi /dat-ban/ thành prefix đúng của Controller chứa @PutMapping("/update-trang-thai-phieu")
    const payload = {
      id: idPhieuCanHuy,
      trangThai: 2 // 2 = Đã hủy
    };

    await axiosClient.put(`/dat-ban/update-trang-thai-phieu`, payload);

    Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đã hủy phiếu đặt bàn!', timer: 1500, showConfirmButton: false });

    // Đóng Modal và làm mới dữ liệu
    closeDetailModal();
    await searchDatBan(); // Load lại danh sách phiếu bên dưới
    await loadTables();   // Quét lại sơ đồ bàn cho nó trống
    refreshKey.value += 1;

  } catch (error) {
    Swal.fire('Lỗi', error.response?.data?.message || 'Không thể hủy phiếu lúc này', 'error');
  }
};

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

// ========================================================
// 🚨 LOGIC: XEM CHI TIẾT PHIẾU ĐẶT BÀN VÀ XẾP BÀN
// ========================================================
const showDetailModal = ref(false);
const phieuDetail = ref(null);
const detailHoaDon = ref(null);
const detailOrderedItems = ref([]);
const orderHistory = ref([]);
const isAssigningExisting = ref(false);

// 🚨 HÀM MỚI: Tách riêng logic gọi API để tái sử dụng
const reloadDetailData = async (idPhieuCanTim) => {
  isLoadingDetail.value = true;
  try {
    const resBase = await axiosClient.get(`/hoa-don-thanh-toan/active-by-phieu/${idPhieuCanTim}`);

    if (resBase.data && resBase.data.id) {
      const idHoaDon = resBase.data.id;
      const [resChiTiet, resHdFull, resLichSu] = await Promise.all([
        axiosClient.get(`/hoa-don-thanh-toan/chi-tiet-hoa-don/${idHoaDon}`),
        axiosClient.get(`/hoa-don-thanh-toan/get-by-id/${idHoaDon}`),
        axiosClient.get(`/hoa-don-thanh-toan/lich-su/${idHoaDon}`)
      ]);

      detailHoaDon.value = resHdFull.data;
      orderHistory.value = resLichSu.data;
      detailOrderedItems.value = resChiTiet.data.map(mon => ({
        id: mon.id,
        name: mon.tenMon,
        quantity: mon.soLuong,
        price: mon.donGia,
        thanhTien: mon.thanhTien,
        served: mon.trangThaiCode === 2
      }));
    }
  } catch (error) {
    console.error("Phiếu này chưa có hóa đơn:", error.response?.status);
  }
};

const openDetailModal = async (item) => {
  if (!item || (!item.id && !item.idDatBan)) {
    return Swal.fire('Lỗi', 'Không lấy được thông tin phiếu!', 'error');
  }

  phieuDetail.value = item;
  showDetailModal.value = true;
  detailHoaDon.value = null;
  detailOrderedItems.value = [];
  orderHistory.value = [];

  try {
    const idPhieuCanTim = item.id || item.idDatBan;
    console.log("🚀 Đang tìm Hóa Đơn của Phiếu ID:", idPhieuCanTim);

    // BƯỚC 1: Lấy ID Hóa Đơn từ ID Phiếu
    const resBase = await axiosClient.get(`/hoa-don-thanh-toan/active-by-phieu/${idPhieuCanTim}`);

    if (resBase.data && resBase.data.id) {
      const idHoaDon = resBase.data.id;
      console.log("✅ Đã tìm thấy Hóa Đơn ID:", idHoaDon, "-> Đang tải chi tiết...");

      // BƯỚC 2: Gọi song song 3 API bằng idHoaDon
      const [resChiTiet, resHdFull, resLichSu] = await Promise.all([
        axiosClient.get(`/hoa-don-thanh-toan/chi-tiet-hoa-don/${idHoaDon}`),
        axiosClient.get(`/hoa-don-thanh-toan/get-by-id/${idHoaDon}`),
        axiosClient.get(`/hoa-don-thanh-toan/lich-su/${idHoaDon}`)
      ]);

      // Gán dữ liệu Hóa Đơn Full và Lịch sử
      detailHoaDon.value = resHdFull.data;
      orderHistory.value = resLichSu.data;

      // Map lại mảng chi tiết món ăn theo JSON mới
      detailOrderedItems.value = resChiTiet.data.map(mon => ({
        id: mon.id,
        name: mon.tenMon,
        quantity: mon.soLuong,
        price: mon.donGia,
        thanhTien: mon.thanhTien,
        served: mon.trangThaiCode === 2 // 1 là Chưa Lên, 2 là Đã Lên
      }));
    }
  } catch (error) {
    console.error("❌ Lỗi (Phiếu này chưa gọi món hoặc chưa có hóa đơn):", error.response?.status);
  }
};


const closeDetailModal = () => {
  showDetailModal.value = false;
  phieuDetail.value = null;
};

// 2. Kích hoạt chọn bàn cho phiếu ĐÃ TỒN TẠI
const handleAssignTableFromDetail = async () => {
  isAssigningExisting.value = true;
  tempSelectedTables.value = [];

  createForm.value.soLuongKhach = Number(detailHoaDon.value?.soLuongKhach || phieuDetail.value?.soLuongKhach || phieuDetail.value?.soNguoi || 1);

  let dbTime = detailHoaDon.value?.thoiGianDat || phieuDetail.value?.thoiGianDat;
  if (Array.isArray(dbTime)) {
    const [y, m, d, h, min, s] = dbTime;
    createForm.value.thoiGianDat = new Date(y, m - 1, d, h || 0, min || 0, s || 0);
  } else {
    createForm.value.thoiGianDat = dbTime;
  }

  if (!listBanAn.value || listBanAn.value.length === 0) {
    Swal.fire({ title: 'Đang tải sơ đồ...', didOpen: () => Swal.showLoading() });
    try {
      const banRaw = await fetchAllBanAn();
      listBanAn.value = banRaw.map((ban, index) => {
        const defaultCol = (index % 3) * 4 + 1;
        const defaultRow = Math.floor(index / 3) * 2 + 1;
        return { ...ban, column: ban.column || defaultCol, row: ban.row || defaultRow };
      });
      Swal.close();
    } catch (e) {
      return Swal.fire('Lỗi', 'Không thể lấy dữ liệu sơ đồ bàn', 'error');
    }
  }

  openTableSelectionModal();
};

// 3. 🚨 THAY THẾ toàn bộ hàm confirmTableSelection cũ bằng 2 hàm dưới đây:
const confirmTableSelection = async () => {
  if (totalTempCapacity.value < createForm.value.soLuongKhach) {
    // Chỉ hiện thông báo chặn lại, không có nút tiếp tục
    await Swal.fire({
      title: 'Chưa đủ chỗ!',
      text: `Khách đi ${createForm.value.soLuongKhach} người nhưng các bàn chọn chỉ chứa được ${totalTempCapacity.value} người. Vui lòng chọn thêm bàn!`,
      icon: 'warning',
      confirmButtonColor: '#7d161a',
      confirmButtonText: 'Chọn thêm bàn'
    });
    return; // Dừng lại, không gọi hàm lưu
  } else {
    await processSaveTableSelection();
  }
};

const processSaveTableSelection = async () => {
  const idPhieu = phieuDetail.value?.id || phieuDetail.value?.idDatBan;

  if (isAssigningExisting.value && idPhieu) {
    try {
      Swal.fire({ title: 'Đang lưu bàn...', didOpen: () => Swal.showLoading() });

      const payload = {
        idPhieuDatBan: idPhieu,
        danhSachIdBanAn: tempSelectedTables.value.map(b => b.id)
      };

      await axiosClient.put(`/hoa-don-thanh-toan/xep-ban`, payload);

      Swal.fire({ toast: true, position: 'top-end', icon: 'success', title: 'Xếp bàn thành công!', timer: 2000, showConfirmButton: false });

      // 🚨 ĐÃ SỬA: Chỉ tắt Modal sơ đồ bàn, KHÔNG tắt Modal chi tiết
      showSelectTableModal.value = false;
      isAssigningExisting.value = false;

      await searchDatBan();      // Cập nhật lại danh sách phiếu
      await reloadDetailData(idPhieu); // Cập nhật danh sách bàn hiển thị trong Modal chi tiết
      refreshKey.value += 1;     // Re-render component con
      await loadTables();

    } catch (e) {
      Swal.fire('Lỗi', e.response?.data?.message || 'Không thể xếp bàn', 'error');
    }
  }
  else {
    createForm.value.danhSachBanChon = [...tempSelectedTables.value];
    showSelectTableModal.value = false;
    formErrors.value.idBanAn = "";
  }
};

watch(
  () => route.query.openId,
  (newId) => {
    if (newId) {
      checkUrlAndOpenDetail();
    }
  }
);

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
  
  const isValid = !Object.values(errors).some(Boolean);
  // 🚀 BẬT TOAST NẾU LỖI
  if (!isValid) {
    Toast.fire({ icon: 'error', title: 'Dữ liệu không hợp lệ', text: 'Vui lòng kiểm tra lại các trường báo đỏ' });
  }

  return isValid;
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

  // 🚨 ĐÃ SỬA: Chỉ chặn thời gian quá khứ nếu là ĐANG TẠO PHIẾU MỚI (isAssigningExisting = false)
  if (!isAssigningExisting.value && dayjs(createForm.value.thoiGianDat).isBefore(dayjs(), 'minute')) {
    formErrors.value.thoiGianDat = "Thời gian đặt không được ở trong quá khứ!";
    return Swal.fire('Lưu ý', 'Thời gian đặt không được ở trong quá khứ!', 'error');
  }

  Swal.fire({ title: 'Đang kiểm tra sức chứa...', didOpen: () => Swal.showLoading() });

  try {
    const dateObj = dayjs(createForm.value.thoiGianDat);
    const idCuaPhieu = isAssigningExisting.value
      ? (phieuDetail.value?.id || phieuDetail.value?.idDatBan || detailHoaDon.value?.idPhieuDatBan || null)
      : null;
    const payload = {
      ngayDat: dateObj.format('YYYY-MM-DD'),
      gioDat: dateObj.format('HH:mm:ss'),
      soNguoi: createForm.value.soLuongKhach,
      idPhieu: idCuaPhieu
    };

    const availableTables = await checkBanTrongService(payload);

    if (!availableTables || availableTables.length === 0) {
      Swal.close();
      createForm.value.danhSachBanChon = [];
      return Swal.fire({
        icon: 'error',
        title: 'Từ chối!',
        text: 'Nhà hàng đã hết bàn hoặc vượt quá sức chứa tại thời điểm này!'
      });
    }

    const tempMap = {};
    listBanAn.value.forEach(ban => { tempMap[ban.id] = { trangThai: 2 }; });
    availableTables.forEach(ban => { tempMap[ban.id] = { trangThai: 0 }; });

    tableStatusMap.value = tempMap;

    if (danhSachTangModal.value.length > 0) {
      modalActiveFloor.value = danhSachTangModal.value[0];
    }

    // 🚨 ĐÃ SỬA: Nếu xếp bàn cho phiếu cũ thì mảng bàn bắt đầu bằng mảng rỗng
    tempSelectedTables.value = isAssigningExisting.value ? [] : [...(createForm.value.danhSachBanChon || [])];

    Swal.close();
    showSelectTableModal.value = true;

  } catch (error) {
    Swal.close();
    Swal.fire('Lỗi', 'Hệ thống gián đoạn hoặc sai định dạng dữ liệu gửi lên', 'error');
  }
};

const handleConfirmOrder = async (idHoaDon) => {
  console.log("👉 BƯỚC 1: Đã bấm nút! ID Hóa đơn nhận được là:", idHoaDon);

  if (!idHoaDon) {
    Swal.fire("Lỗi", "Không tìm thấy ID hóa đơn để xác nhận!", "error");
    return;
  }

  const danhSachBan = detailHoaDon.value?.danhSachTenBan || phieuDetail.value?.danhSachBan || [];

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
      html: "Trạng thái hóa đơn sẽ chuyển sang <b>Đã xác nhận</b> và hệ thống sẽ <b>gửi Email</b> thông báo cho khách hàng.",
      icon: "question",
      showCancelButton: true,
      confirmButtonColor: "#8b0000", // 🚨 ĐÃ SỬA: Đổi nút Đồng ý thành màu đỏ đậm
      cancelButtonColor: "#6c757d",
      confirmButtonText: '<i class="fa-solid fa-paper-plane me-1"></i> Đồng ý gửi',
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

    if (!axiosClient) {
      console.error("🚨 LỖI: axiosClient chưa được import hoặc bị undefined!");
    }

    const response = await axiosClient.put(
      `/hoa-don-thanh-toan/xac-nhan-va-gui-mail/${idHoaDon}`,
    );
    
    console.log("👉 BƯỚC 5: API chạy thành công! Kết quả:", response);

    Swal.fire({ icon: "success", title: "Hoàn tất!", text: "Đã xác nhận đơn và gửi email.", timer: 1500, showConfirmButton: false });

    // ========================================================
    // 🚨 ĐÃ FIX: ÉP TRẠNG THÁI UI LÊN 1 ĐỂ ẨN NÚT NGAY LẬP TỨC
    // ========================================================
    if (phieuDetail.value) phieuDetail.value.trangThai = 1;
    if (detailHoaDon.value) detailHoaDon.value.trangThaiHoaDon = 1;

    // ✅ REFRESH ĐỒNG BỘ
    const idPhieuCanTim = phieuDetail.value?.id || phieuDetail.value?.idDatBan;
    await reloadDetailData(idPhieuCanTim); // Cập nhật trạng thái ngay trong Modal đang mở
    
    await searchDatBan();  // Cập nhật trạng thái (màu sắc/chữ) ở danh sách bên ngoài
    refreshKey.value += 1;

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
    currentStep.value = 2; 
  } else {
    Toast.fire({ icon: 'error', title: 'Dữ liệu không hợp lệ', text: 'Vui lòng điền đủ thông tin Bước 1' });
  }
};

const toggleTableForCreate = (ban) => {
  const status = tableStatusMap.value[String(ban.id)]?.trangThai || 0;
  if (status !== 0) {
    return Swal.fire({ toast: true, position: 'top-end', icon: 'error', title: 'Bàn đã có lịch lúc này!', showConfirmButton: false, timer: 1500 });
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

const removeSelectedTable = (index) => {
  createForm.value.danhSachBanChon.splice(index, 1);
};


// ================= SUBMIT =================
const submitCreate = async () => {
  if (!validateCreateForm()) return;

  // 🚀 KIỂM TRA TRÙNG LỊCH 3 TIẾNG 🚀
  const phoneToBook = createForm.value.soDienThoai.trim();
  const timeToBook = dayjs(createForm.value.thoiGianDat);

  const conflictingBooking = listPhieuDatBan.value.find(phieu => {
    // Bỏ qua phiếu Đã hủy (2) hoặc Hoàn tất (4)
    if (String(phieu.trangThai) === "2" || String(phieu.trangThai) === "4") return false;

    if (phieu.soDienThoai === phoneToBook) {
      let existingTimeObj;
      if (Array.isArray(phieu.thoiGianDat)) {
        const [y, m, d, h, min, s] = phieu.thoiGianDat;
        existingTimeObj = dayjs(new Date(y, m - 1, d, h || 0, min || 0, s || 0));
      } else {
        existingTimeObj = dayjs(phieu.thoiGianDat);
      }
      
      // Độ chênh lệch thời gian theo giờ
      const diffHours = Math.abs(existingTimeObj.diff(timeToBook, 'hour', true));
      return diffHours < 3; // Nằm trong khoảng dưới 3 tiếng
    }
    return false;
  });

  if (conflictingBooking) {
     let existingFormattedTime = "";
     if (Array.isArray(conflictingBooking.thoiGianDat)) {
       const [y, m, d, h, min, s] = conflictingBooking.thoiGianDat;
       existingFormattedTime = dayjs(new Date(y, m - 1, d, h || 0, min || 0, s || 0)).format("HH:mm DD/MM/YYYY");
     } else {
       existingFormattedTime = dayjs(conflictingBooking.thoiGianDat).format("HH:mm DD/MM/YYYY");
     }

     Toast.fire({
        icon: 'warning',
        title: 'Trùng lịch!',
        text: `Khách đã có lịch lúc ${existingFormattedTime}. Các lịch phải cách nhau ít nhất 3 tiếng!`,
        timer: 6000 // Hiện lâu hơn để khách đọc kịp
     });
     return; // Dừng lại không cho gọi API
  }

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
    await searchDatBan(); // Tải lại danh sách phiếu ở trang 1
    refreshKey.value += 1; // Thông báo cho component con (List/Calendar) load lại
    await loadTables();    // Cập nhật lại trạng thái bàn

    Toast.fire({ icon: 'success', title: 'Thành công!', text: 'Đã tạo phiếu đặt bàn mới.' });

  } catch (error) {
    Toast.fire({ icon: 'error', title: 'Thất bại', text: error.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại.' });
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

onMounted(async () => {
    await searchDatBan(); // Chờ load danh sách xong
    
    // Delay 500ms để đảm bảo mọi thứ đã sẵn sàng rồi mới mở Modal
    setTimeout(() => {
        checkUrlAndOpenDetail();
    }, 500);
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
        <router-view v-slot="{ Component }">
          <component :is="Component" :key="refreshKey" @open-detail="openDetailModal" />
        </router-view>
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
              :min-date="new Date()" :hide-input-icon="true" :clearable="false" auto-apply format="dd/MM/yyyy HH:mm"
              placeholder="Chọn ngày giờ" class="custom-datetime-picker"
              :class="{ 'input-error': formErrors.thoiGianDat }" />
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
              <span v-if="createForm.danhSachBanChon?.length > 0" class="small fw-bold text-danger">
                Đã chọn {{ createForm.danhSachBanChon.length }} bàn
              </span>
            </div>
            <div class="d-flex flex-wrap gap-2 p-2 border rounded bg-light" style="min-height: 50px;">
              <div v-for="(ban, index) in createForm.danhSachBanChon" :key="ban.id"
                class="badge bg-danger d-flex align-items-center py-2 px-3" style="font-size: 13px;">
                <span>Bàn {{ ban.maBan }} ({{ ban.soCho || ban.soNguoiToiDa }} chỗ)</span>
                <i class="fa-solid fa-xmark ms-2" style="cursor: pointer" @click="removeSelectedTable(index)"
                  title="Xóa bàn này"></i>
              </div>
              <div v-if="!createForm.danhSachBanChon?.length"
                class="text-muted small fst-italic w-100 text-center mt-1">
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
          @open="() => { customerOptions = allCustomers; }" @search-change="searchCustomer"
          @select="handleSelectCustomer" @clear="clearCustomer">
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
          <button class="btn btn-custom-outline" @click="currentStep = 1"><i class="fa-solid fa-arrow-left me-2"></i>
            Quay
            lại</button>
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
      
      <div class="modal-header d-flex justify-content-between align-items-center bg-white shadow-sm z-1 p-3">
        <h4 class="modal-title-custom text-danger m-0 fw-bold">
          <i class="fa-solid fa-chair me-2"></i> Chọn bàn cho khách ({{ createForm.soLuongKhach }} người)
        </h4>
        <button class="close-btn m-0 p-0" @click="showSelectTableModal = false">✕</button>
      </div>

      <div class="modal-body-custom p-3 d-flex flex-column h-100 overflow-hidden">

        <div class="d-flex justify-content-between align-items-center mb-3 flex-shrink-0">
          <div
            class="alert alert-info mb-0 border-0 shadow-sm flex-grow-1 me-4 d-flex align-items-center justify-content-between">
            <div>
              <i class="fa-solid fa-clock me-2 text-primary fs-5"></i>
              <span>Kiểm tra bàn trống lúc: <b>{{ dayjs(createForm.thoiGianDat).format('HH:mm - DD/MM/YYYY')
                  }}</b></span>
            </div>
            <div class="fw-bold fs-5"
              :class="totalTempCapacity >= createForm.soLuongKhach ? 'text-danger' : 'text-danger'">
              Đã đáp ứng: {{ totalTempCapacity }} / {{ createForm.soLuongKhach }} chỗ
            </div>
          </div>

          <div class="d-flex bg-white p-1 rounded shadow-sm border">
            <button v-for="tang in danhSachTangModal" :key="'select-tang-' + tang" class="btn px-4 py-2 fw-bold"
              :class="modalActiveFloor === tang ? 'btn-active' : 'text-muted'" @click="modalActiveFloor = tang"
              style="border-radius: 6px; border: none;">
              Tầng {{ tang }}
            </button>
          </div>
        </div>

        <div class="floor-plan-layout flex-grow-1 overflow-hidden d-flex gap-3">
          <div class="floor-plan-section flex-grow-1 bg-white shadow-sm border rounded">
            <div class="grid-container h-100 overflow-auto">
              <div class="grid-canvas">
                <div v-for="ban in banTheoTangModal" :key="'grid-' + ban.id" class="table-card" :class="{
                  'is-dimmed': (tableStatusMap[String(ban.id)]?.trangThai !== 0 && tableStatusMap[String(ban.id)]?.trangThai !== undefined) && !tempSelectedTables.some(b => b.id === ban.id),
                  'is-selected-merge': tempSelectedTables.some(b => b.id === ban.id)
                }"
                  :style="{ gridColumnStart: ban.column, gridRowStart: ban.row, gridColumnEnd: 'span 3', gridRowEnd: 'span 2', cursor: (tableStatusMap[String(ban.id)]?.trangThai === 0 || tableStatusMap[String(ban.id)]?.trangThai === undefined) ? 'pointer' : 'not-allowed' }"
                  @click="toggleTableForCreate(ban)">

                  <div v-if="tempSelectedTables.some(b => b.id === ban.id)" class="checked-icon-overlay">
                    <i class="fa-solid fa-check"></i>
                  </div>

                  <div class="table-content text-center pt-2">
                    <strong class="fs-5">{{ ban.maBan }}</strong>
                    <div class="text-muted small mb-2"><i class="fa-solid fa-users text-secondary"></i> {{ ban.soCho ||
                      ban.soNguoiToiDa }} chỗ</div>

                    <div v-if="tempSelectedTables.some(b => b.id === ban.id)"
                      class="status-tag bg-danger text-white w-100 rounded-pill"><i
                        class="fa-solid fa-check-double"></i>
                      Đã chọn</div>
                    <div
                      v-else-if="tableStatusMap[String(ban.id)]?.trangThai !== 0 && tableStatusMap[String(ban.id)]?.trangThai !== undefined"
                      class="status-tag bg-secondary text-white w-100 rounded-pill"><i class="fa-solid fa-ban"></i> Đã
                      có
                      lịch</div>
                    <div v-else class="status-tag status-empty w-100 rounded-pill"><i class="fa-solid fa-check"></i> Còn
                      trống</div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="bg-white shadow-sm border rounded p-3 d-flex flex-column" style="width: 350px;">
            <h6 class="fw-bold mb-3 pb-2 border-bottom text-danger flex-shrink-0">
              <i class="fa-solid fa-list-check me-2"></i> Bàn trống có thể chọn
            </h6>
            <div class="overflow-auto flex-grow-1 pe-2" style="max-height: calc(100vh - 250px);">
              <div
                v-if="banTheoTangModal.filter(b => tableStatusMap[String(b.id)]?.trangThai === 0 || tableStatusMap[String(b.id)]?.trangThai === undefined).length === 0"
                class="text-center text-muted mt-5">
                <i class="fa-solid fa-box-open fa-3x mb-3 text-light"></i>
                <p>Không có bàn nào trống tại tầng này!</p>
              </div>

              <div v-else class="row g-2">
                <div
                  v-for="ban in banTheoTangModal.filter(b => tableStatusMap[String(b.id)]?.trangThai === 0 || tableStatusMap[String(b.id)]?.trangThai === undefined)"
                  :key="'list-' + ban.id" class="col-6">

                  <div class="mini-table-card border rounded p-3 text-center transition-all"
                    :class="{ 'is-selected-list': tempSelectedTables.some(b => b.id === ban.id) }"
                    @click="toggleTableForCreate(ban)">

                    <div v-if="tempSelectedTables.some(b => b.id === ban.id)" class="checked-icon-overlay-mini">
                      <i class="fa-solid fa-check"></i>
                    </div>

                    <div class="fw-bold fs-5"
                      :class="tempSelectedTables.some(b => b.id === ban.id) ? 'text-white' : 'text-danger'">{{
                      ban.maBan
                      }}</div>
                    <div class="small mb-1"
                      :class="tempSelectedTables.some(b => b.id === ban.id) ? 'text-white' : 'text-muted'">
                      <i class="fa-solid fa-users"></i> {{ ban.soCho || ban.soNguoiToiDa }} chỗ
                    </div>
                    <div class="badge border"
                      :class="tempSelectedTables.some(b => b.id === ban.id) ? 'bg-white text-danger border-white' : 'bg-light text-dark'">
                      Tầng {{ ban.soTang || ban.tang }}
                    </div>
                  </div>

                </div>
              </div>
            </div>
          </div>
        </div>

        <div
          class="mt-3 pt-3 border-top bg-white px-3 py-2 rounded shadow-sm d-flex justify-content-between flex-shrink-0">
          <button class="btn btn-outline-secondary px-4 fw-bold" @click="showSelectTableModal = false"><i
              class="fa-solid fa-arrow-left me-1"></i> Quay lại</button>
          <button class="btn btn-update-status px-5 fw-bold" style="width: auto; background-color: #28a745;"
            :disabled="tempSelectedTables.length === 0" @click="confirmTableSelection">
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

  <div v-if="showDetailModal" class="modal-overlay" style="z-index: 10000;">
    <div class="modal-box" style="width: 800px;">
      <div class="modal-header border-bottom pb-3 mb-3">
        <h4 class="m-0 text-danger fw-bold">
          <i class="fa-solid fa-circle-info me-2"></i>Chi tiết phiếu đặt bàn
        </h4>
        <button class="close-btn" @click="closeDetailModal">✕</button>
      </div>

      <div class="modal-body-custom p-0" style="max-height: 70vh; overflow-y: auto; overflow-x: hidden;">
        <div class="row g-3 mb-4">
          <div class="col-md-6">
            <div class="p-3 border rounded bg-light h-100">
              <h6 class="fw-bold text-secondary mb-3 border-bottom pb-2">Thông tin khách hàng</h6>
              <div class="mb-2"><i class="fa-solid fa-user me-2 text-muted"></i> <strong>{{ detailHoaDon?.tenKhachHang
                ||
                phieuDetail?.tenKhachHang || 'Khách vãng lai' }}</strong></div>
              <div class="mb-2"><i class="fa-solid fa-phone me-2 text-muted"></i> {{ detailHoaDon?.sdtKhachHang ||
                phieuDetail?.soDienThoai || 'N/A' }}</div>
              <div class="mb-2"><i class="fa-solid fa-users me-2 text-muted"></i> {{ detailHoaDon?.soLuongKhach ||
                phieuDetail?.soLuongKhach || phieuDetail?.soNguoi }} người</div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="p-3 border rounded bg-light h-100">
              <h6 class="fw-bold text-secondary mb-3 border-bottom pb-2">Thông tin đặt bàn</h6>
              <div class="mb-2"><i class="fa-solid fa-ticket me-2 text-muted"></i> Hóa đơn: <strong>{{
                detailHoaDon?.maHoaDon || phieuDetail?.maDatBan || 'Chưa tạo' }}</strong></div>
              <div class="mb-2"><i class="fa-solid fa-calendar-days me-2 text-muted"></i> Đặt lúc: <strong>{{
                formatDate(detailHoaDon?.thoiGianDat || phieuDetail?.thoiGianDat) }}</strong></div>
              <div class="mb-2">
                <i class="fa-solid fa-circle-info me-2 text-muted"></i> Trạng thái:
                <span class="badge" :class="getStatusClass(detailHoaDon?.trangThaiHoaDon || phieuDetail?.trangThai)">
                  {{ getStatusText(detailHoaDon?.trangThai || phieuDetail?.trangThai) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="mb-4">
          <h6 class="fw-bold text-danger mb-2"><i class="fa-solid fa-map-location-dot me-2"></i>Bàn phục vụ</h6>

          <div v-if="!detailHoaDon?.danhSachTenBan || detailHoaDon.danhSachTenBan.length === 0"
            class="alert alert-warning d-flex justify-content-between align-items-center mb-0">
            <span><i class="fa-solid fa-triangle-exclamation me-2"></i> Phiếu này chưa được xếp bàn.</span>
            <button class="btn btn-sm btn-danger fw-bold px-3 py-2 rounded" @click="handleAssignTableFromDetail">
              <i class="fa-solid fa-chair me-1"></i> Bấm để chọn bàn
            </button>
          </div>

          <div v-else class="p-3 border rounded bg-light d-flex justify-content-between align-items-center">
            <div class="d-flex gap-2 flex-wrap">
              <div v-for="(tenBan, index) in detailHoaDon.danhSachTenBan" :key="index"
                class="badge bg-danger fs-6 py-2 px-3 shadow-sm">
                <i class="fa-solid fa-chair me-1"></i> Bàn {{ tenBan }}
              </div>
            </div>

            <button v-if="(detailHoaDon?.trangThaiHoaDon === 0 || phieuDetail?.trangThai === 0)"
              class="btn btn-sm btn-outline-danger fw-bold px-3 py-2 rounded text-nowrap ms-3"
              @click="handleAssignTableFromDetail">
              <i class="fa-solid fa-rotate-right me-1"></i> Chọn lại bàn
            </button>
          </div>
        </div>

        <div v-if="detailOrderedItems.length > 0" class="mb-4">
          <h6 class="fw-bold text-danger mb-2"><i class="fa-solid fa-utensils me-2"></i>Thực đơn đã gọi</h6>
          <div class="border rounded p-3 bg-light">
            <ul class="summary-list mb-3" style="max-height: none; padding-left: 0; list-style: none;">
              <li v-for="item in detailOrderedItems" :key="item.id" class="mb-2 border-bottom pb-2">
                <div class="d-flex justify-content-between">
                  <span class="fw-bold">{{ item.name }}</span>
                  <span class="fw-bold text-danger">{{ (item.thanhTien || (item.price * item.quantity)).toLocaleString()
                    }} đ</span>
                </div>
                <div class="text-muted small">
                  Số lượng: x{{ item.quantity }} | Đơn giá: {{ item.price.toLocaleString() }}đ
                  <span v-if="item.served" class="badge bg-danger ms-2" style="font-size: 10px;">Đã lên</span>
                  <span v-else class="badge bg-warning text-dark ms-2" style="font-size: 10px;">Chưa lên</span>
                </div>
              </li>
            </ul>

            <div class="d-flex justify-content-between text-muted small mb-1 pt-2 border-top">
              <span>Tiền món:</span><span>{{ (detailHoaDon?.tongTienChuaGiam || 0).toLocaleString() }} đ</span>
            </div>
            <div v-if="(detailHoaDon?.tienCoc || 0) > 0" class="d-flex justify-content-between text-primary small mb-1">
              <span>Đã cọc:</span><span>- {{ detailHoaDon.tienCoc.toLocaleString() }} đ</span>
            </div>
            <div v-if="(detailHoaDon?.soTienDaGiam || 0) > 0"
              class="d-flex justify-content-between text-danger small mb-1">
              <span>Giảm giá:</span><span>- {{ detailHoaDon.soTienDaGiam.toLocaleString() }} đ</span>
            </div>
            <div class="d-flex justify-content-between align-items-center pt-2 border-top border-danger mt-2">
              <span class="fw-bold text-uppercase" style="font-size: 14px;">Cần thanh toán:</span>
              <span class="fw-bold text-danger fs-5">
                {{ (detailHoaDon?.tongTienThanhToan || 0).toLocaleString() }} đ
              </span>
            </div>
          </div>
        </div>

        <div v-else-if="detailHoaDon" class="alert alert-secondary text-center py-2 mt-3">
          <small><i class="fa-solid fa-box-open me-1"></i> Khách chưa gọi món nào.</small>
        </div>

        <div v-if="orderHistory.length > 0" class="mt-4">
          <h6 class="fw-bold text-danger mb-3"><i class="fa-solid fa-clock-rotate-left me-2"></i>Lịch sử hệ thống</h6>
          <div class="border rounded p-3 bg-light">
            <div v-for="log in orderHistory" :key="log.idLog"
              class="border-start border-2 border-danger ps-3 mb-3 position-relative">
              <div class="position-absolute"
                style="left: -6px; top: 0; width: 10px; height: 10px; border-radius: 50%; background: #7d161a;"></div>
              <div class="d-flex justify-content-between mb-1">
                <strong style="font-size: 14px;">{{ log.hanhDong }}</strong>
                <small class="text-muted">{{ formatDate(log.thoiGian) }}</small>
              </div>
              <div class="small text-secondary mb-1">Bởi: {{ log.nguoiThucHien }}</div>
              <div class="small fst-italic text-muted"><i class="fa-solid fa-angle-right me-1"></i>{{ log.lyDo }}</div>
            </div>
          </div>
        </div>

      </div>

      <div class="d-flex justify-content-between mt-3 pt-3 border-top">
        
        <div>
            <button 
                v-if="phieuDetail?.trangThai === 0 || phieuDetail?.trangThai === 1 || detailHoaDon?.trangThaiHoaDon === 0"
                class="btn btn-outline-danger px-4 py-2 fw-bold" 
                @click="handleCancelPhieuFromDetail"
            >
                <i class="fa-solid fa-ban me-1"></i> Hủy phiếu đặt
            </button>
        </div>

        <div class="d-flex gap-2">
            <button
                v-if="detailHoaDon?.trangThaiHoaDon === 0 || phieuDetail?.trangThai === 0"
                class="btn btn-custom px-4 py-2 fw-medium text-white shadow-sm"
                style="background-color: #28a745; border: none;"
                :disabled="!detailHoaDon?.danhSachTenBan || detailHoaDon.danhSachTenBan.length === 0"
                :title="(!detailHoaDon?.danhSachTenBan || detailHoaDon.danhSachTenBan.length === 0) ? 'Vui lòng xếp bàn trước khi xác nhận' : ''"
                @click="handleConfirmOrder(detailHoaDon?.id || detailHoaDon?.idHoaDon)"
              >
                <i class="fa-solid fa-envelope-circle-check me-2"></i> Xác nhận & Gửi mail
            </button>
            <button class="btn btn-outline-secondary px-4" @click="closeDetailModal">Đóng</button>
        </div>
      </div>
    </div>
  </div>
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
  border-radius: 15px;
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
  padding: 8px 10px 8px 35px !important;
  /* 🚨 ĐÃ FIX: Thụt lề trái 35px để chừa chỗ cho Icon */
  border-radius: 8px;
  border: 1px solid #ddd;
}

.custom-datetime-picker.input-error :deep(.dp__input) {
  border-color: #dc3545 !important;
}

/* Xóa luôn class ẩn icon nếu có để icon hiện đẹp tự nhiên */
.custom-datetime-picker :deep(.dp__input_icon) {
  /* display: none !important; Xóa dòng này đi */
  color: #7d161a;
  /* Đổi màu icon cho đẹp */
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
  border: 3px solid #7D161A !important;
  background-color: #f0fdf4 !important;
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(167, 40, 40, 0.4) !important;
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
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.3);
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
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
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
  border-color: #7D161A;
  background-color: #f8fff9;
  transform: translateY(-3px);
  box-shadow: 0 4px 10px rgba(167, 40, 40, 0.15);
}

.mini-table-card.is-selected-list {
  background-color: #7D161A !important;
  border-color: #7D161A !important;
  box-shadow: 0 4px 10px rgba(167, 40, 40, 0.3) !important;
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

.blink-animation {
  animation: blinker 1.5s linear infinite;
}
</style>