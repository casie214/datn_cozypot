<script setup>
import {
  fetchAllBanAn,
  fetchAllCheckIn,
  updateTrangThaiBan,
  createOrder,
  fetchActiveBillByBan,
} from "../../../../services/tableManageService";
import { computed, onMounted, ref, onUnmounted, watch } from "vue";
import dayjs from "dayjs";
import router from "@/App";
import { useRoute, useRouter } from "vue-router";
import FoodList from "../modal/innerComponents/foodList.vue";
import Swal from "sweetalert2";
import { BeGetChiTietHoaDon } from "../../order/screens/orderService";

const activeFloor = ref(1);
const danhSachBan = ref([]);
const selectedItems = ref({});


const fetchAllBan = async () => {
  try {
    const data = await fetchAllBanAn();

    danhSachBan.value = data.map((ban, index) => {
      const defaultCol = (index % 3) * 4 + 1;
      const defaultRow = Math.floor(index / 3) * 2 + 1;

      return {
        ...ban,
        column: ban.column || defaultCol,
        row: ban.row || defaultRow,
      };
    });
  } catch (e) {
    console.error("Lỗi load danh sách bàn", e);
  }
};

const banTheoTang = computed(() => {
  return danhSachBan.value.filter(
    (ban) => Number(ban.soTang) === activeFloor.value,
  );
});

const thongKeTang = computed(() => {
  const total = banTheoTang.value.length;
  const free = banTheoTang.value.filter((ban) =>
    ["0", "trống", "trong"].includes(
      ban.trangThai?.toString().toLowerCase().trim(),
    ),
  ).length;
  return { total, free };
});

const searchQuery = ref("");
const filterDate = ref(null);
const danhSachCho = ref([]);

const handleFetchAllCheckIn = async () => {
  try {
    danhSachCho.value = await fetchAllCheckIn();
  } catch (error) {
    console.error("Lỗi fetch khách chờ:", error);
  }
};

const danhSachLoc = computed(() => {
  return danhSachCho.value.filter((khach) => {
    if (!khach.maBan && !khach.idBanAn) return false;

    const thoiGianDat = dayjs(khach.thoiGianDat);
    const hienTai = dayjs(currentTime.value);

    const matchSearch =
      khach.soDienThoai?.includes(searchQuery.value) ||
      khach.tenKhachHang
        ?.toLowerCase()
        .includes(searchQuery.value.toLowerCase());

    //  Cho phép check-in tới +10 phút
    //  Hiển thị nếu: Chưa tới giờ HOẶC quá giờ dưới 10 phút
    const phutChenh = hienTai.diff(thoiGianDat, "minute");
    const trongThoiGianCheckIn = phutChenh <= 10;

    //  Lọc theo ngày
    let matchDate = true;
    if (filterDate.value) {
      matchDate = thoiGianDat.format("YYYY-MM-DD") === filterDate.value;
    }

    return matchSearch && trongThoiGianCheckIn && matchDate;
  });
});

// Cập nhật currentTime mỗi giây để trigger toàn bộ hàm getCountdown trên giao diện
const currentTime = ref(new Date());
let timer;

const currentStaffName = ref("");

onMounted(() => {
  fetchAllBan();
  handleFetchAllCheckIn();
  // Cứ mỗi 1 giây, giá trị currentTime thay đổi -> Vue tự động vẽ lại những chỗ dùng getCountdown
  timer = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);

  const user = JSON.parse(localStorage.getItem("user"));
    if (user) currentStaffName.value = user.hoTen || user.username;
});

onUnmounted(() => clearInterval(timer));

// Hàm này sẽ được gọi trực tiếp từ Template
const getCountdown = (dbTime) => {
  if (!dbTime) return "00:00:00";

  const now = dayjs(currentTime.value);
  const target = dayjs(dbTime);
  const diff = target.diff(now);

  if (diff <= 0) return "00:00:00";

  const h = Math.floor(diff / 3600000)
    .toString()
    .padStart(2, "0");
  const m = Math.floor((diff % 3600000) / 60000)
    .toString()
    .padStart(2, "0");
  const s = Math.floor((diff % 60000) / 1000)
    .toString()
    .padStart(2, "0");

  return `${h}:${m}:${s}`;
};

// --- HELPER UI ---
const getStatusClass = (s) => {
  s = Number(s);

  if (s === 1) return "status-occupied-light"; // Có khách
  if (s === 2) return "status-booked"; // Đã đặt
  return "status-empty"; // Trống (0 hoặc null)
};

const getStatusText = (s) => {
  s = Number(s);
  if (s === 1) return "Có khách";
  if (s === 2) return "Đã đặt";
  return "Trống";
};

// --- QUẢN LÝ MODAL & CẬP NHẬT ---
const isShowModal = ref(false);
const selectedBan = ref(null);
const selectedPhieu = ref(null);
const draftOrders = ref({});
const activeHoaDonId = ref(null);

const openManageModal = async (ban) => {
  selectedBan.value = { ...ban };
  modalView.value = 'info';
  activeHoaDonId.value = null;
  listMonDaChon.value = []; 
  selectedPhieu.value = null;

   
    try {
      const resHd = await fetchActiveBillByBan(ban.id);
      
      if (resHd) {
        activeHoaDonId.value = resHd.id;
        console.log("🔥 Đã tìm thấy hóa đơn ID:", resHd.id);

        selectedPhieu.value = {
          maDatBan: resHd.maHoaDon || 'N/A',
          tenKhachHang: resHd.tenKhachHang || 'Khách lẻ',
          idKhachHang: resHd.idKhachHang, 
          thoiGianDat: resHd.thoiGianTao
        };

        const items = await BeGetChiTietHoaDon(resHd.id);
        console.log("🍱 Danh sách chi tiết món từ DB:", items);

        if (items && items.length > 0) {
          listMonDaChon.value = items.map(dbItem => {
            // LƯU Ý QUAN TRỌNG: Kiểm tra tên trường từ log Console để sửa dbItem.xxxx cho đúng
            const isFood = dbItem.idChiTietMonAn !== null;
            return {
              uniqueId: isFood ? `food_${dbItem.idChiTietMonAn}` : `set_${dbItem.idSetLau}`,
              originalId: isFood ? dbItem.idChiTietMonAn : dbItem.idSetLau,
              dbDetailId: dbItem.id, // ID này dùng để Backend biết dòng nào cần hủy/sửa
              type: isFood ? 'FOOD' : 'SET',
              name: dbItem.tenMonAn || dbItem.tenSetLau || dbItem.tenMon || "Không tên",
              price: dbItem.donGia,
              quantity: dbItem.soLuong,
              note: dbItem.ghiChu || ''
            };
          });
          console.log("✅ Đã map thành công vào listMonDaChon:", listMonDaChon.value);
        }
      }
    } catch (e) {
      console.error("❌ Lỗi khi lấy thông tin món ăn:", e);
    }
  
    // Bàn trống thì lấy từ nháp
    listMonDaChon.value = draftOrders.value[ban.id] || [];
  

  isShowModal.value = true;
};

const closeModal = () => {
  isShowModal.value = false;
  selectedBan.value = null;
  selectedPhieu.value = null;
};

const updateStatus = async () => {
  if (!selectedBan.value) return;

  const payload = {
    id: selectedPhieu.value?.id || null,
    idBanAn: selectedBan.value.id, // ID Bàn
    trangThai: Number(selectedBan.value.trangThai),
  };
  console.log(selectedBan.value);

  console.log("Dữ liệu gửi đi:", payload);
  try {
    await updateTrangThaiBan(payload);

    Swal.fire({ icon: 'success', title: 'Thành công!', text: `Cập nhật bàn ${selectedBan.value.maBan} thành công!`, timer: 1500, showConfirmButton: false });
    closeModal();
    await fetchAllBan();
    await handleFetchAllCheckIn();
  } catch (err) {
    console.error(err);
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Cập nhật thất bại!' + err.message });
  }
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

const modalView = ref('info');

const switchToAddFood = () => {
  modalView.value = 'addFood';
};

// Phần xử lí thêm món ăn
// Cái này là array các món/set đã chọn nhé, dùng thì lấy từ đây ra



const listMonDaChon = ref([]);

// Tính tổng giá của mấy món đã thêm
const totalTempPrice = computed(() => {
  return listMonDaChon.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
});

const handleSaveFood = async (itemsArray) => {
  listMonDaChon.value = itemsArray;
  
  if (selectedBan.value) {
    draftOrders.value[selectedBan.value.id] = itemsArray;
  }

  if (itemsArray.length === 0) {
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Vui lòng chọn ít nhất 1 món' });
    return;
  }

  try {
    const userStorage = localStorage.getItem("user");
    let currentStaffId = 1;
    if (userStorage) {
      const user = JSON.parse(userStorage);
      if (user.id) currentStaffId = user.id;
    }

    let customerId = null;
    let phieuDatId = null;
    if (selectedPhieu.value) {
      const sp = selectedPhieu.value;
      customerId = sp.idKhachHang?.id || sp.idKhachHang || (sp.khachHang ? sp.khachHang.id : null);
      phieuDatId = sp.idDatBan || sp.id;
    }

    const payload = {
      idBanAn: selectedBan.value.id,
      idNhanVien: currentStaffId,
      idKhachHang: customerId,
      idPhieuDatBan: phieuDatId,

      chiTietHoaDon: itemsArray.map(item => ({
        // 👈 Gửi ID cũ lên. Nếu ID này có mà SL thay đổi, Backend sẽ hủy dòng cũ và tạo dòng mới
        idChiTietHoaDonCu: item.dbDetailId || null, 
        idChiTietMonAn: item.type === 'FOOD' ? item.originalId : null,
        idSetLau: item.type === 'SET' ? item.originalId : null,
        soLuong: item.quantity,
        donGia: item.price,
        thanhTien: item.price * item.quantity,
        ghiChu: item.note || ''
      })),

      tongTien: totalTempPrice.value
    };

    // Gọi API lưu
    await createOrder(payload);

    // Xóa nháp sau khi lưu thành công
    delete draftOrders.value[selectedBan.value.id];

    await Swal.fire({
      icon: 'success',
      title: 'Thành công!',
      text: "Đã cập nhật thực đơn thành công!",
      timer: 1500,
      showConfirmButton: false
    });

    await fetchAllBan();
    modalView.value = 'info';

  } catch (e) {
    console.error(e);
    Swal.fire({
      icon: 'error',
      title: 'Lỗi',
      text: "Lỗi khi lưu hóa đơn: " + (e.response?.data?.message || e.message)
    });
  }
};

const props = defineProps({
    initialItems: {
        type: Array,
        default: () => []
    }
});

const initSelectedItems = () => {
    selectedItems.value = {};
    if (props.initialItems && props.initialItems.length > 0) {
        props.initialItems.forEach(item => {
            const key = item.uniqueId;
            if (key) {
                selectedItems.value[key] = { ...item };
            }
        });
    }
};


watch(() => props.initialItems, (newItems) => {
    console.log("🔄 FoodList nhận dữ liệu mới từ cha:", newItems);
    initSelectedItems();
}, { deep: true, immediate: true });
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
      <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem">
        Check-in bàn
      </h3>

      <div class=" search-form">
        <h5 style="font-size: 1rem; font-weight: bold">Khu vực</h5>

        <!-- Button chọn tầng -->
        <div class="mb-3">
          <div class="d-inline-block">
            <div class="d-inline-block me-2">
              <button class="btn" :class="activeFloor === 1 ? 'btn-active' : 'btn-outline'" @click="activeFloor = 1">
                Tầng 1
              </button>
            </div>
            <div class="d-inline-block">
              <button class="btn" :class="activeFloor === 2 ? 'btn-active' : 'btn-outline'" @click="activeFloor = 2">
                Tầng 2
              </button>
            </div>
          </div>

          <div class="floor-info mt-2">
            Tầng {{ activeFloor }} - Trống {{ thongKeTang.free }}/{{
              thongKeTang.total
            }}
            bàn
          </div>
        </div>
      </div>

      <div class="contain-frame mt-3">
        <!-- FRAME CHUNG -->
        <div class="floor-frame">
          <!-- Sơ đồ tầng 1 -->
          <div v-show="activeFloor === 1" class="floor-map">
            <div class="floor-plan-layout">
              <div class="floor-plan-section">
                <div class="floor-header"></div>
                <div class="grid-container">
                  <div class="grid-canvas" @dragover.prevent @drop="onDrop" :class="{ 'editing-mode': isEditing }">
                    <div v-for="ban in banTheoTang" :key="ban.idBanAn" class="table-card"
                      :class="{ 'highlight-red': Number(ban.trangThai) === 0 }" @click="openManageModal(ban)" :style="{
                        gridColumnStart: ban.column,
                        gridRowStart: ban.row,
                        gridColumnEnd: 'span 4',
                        gridRowEnd: 'span 2',
                        cursor: 'pointer',
                      }">
                      <div class="table-content">
                        <div class="table-id">
                          <strong>{{ ban.maBan }}</strong>
                        </div>
                        <div class="table-id">({{ ban.soCho }} chỗ)</div>
                        <div :class="['status-tag', getStatusClass(ban.trangThai)]">
                          {{ getStatusText(ban.trangThai) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="list-section">
                <div class="list-card">
                  <div class="checkin-container">
                    <h5 style="font-weight: bold; font-size: 16px">
                      Khách chờ check-in
                    </h5>

                    <div class="filter-section">
                      <div class="filter-group">
                        <label class="filter-label">
                          <i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm
                        </label>
                        <div class="filter-input-wrapper">
                          <input type="text" v-model="searchQuery" placeholder="SĐT khách hàng" />
                        </div>
                      </div>

                      <div class="filter-group">
                        <label class="filter-label">
                          <i class="fa-solid fa-calendar-days"></i> Lọc theo
                          ngày đến
                        </label>
                        <div class="filter-input-wrapper">
                          <input type="date" v-model="filterDate" class="date-input" />
                        </div>
                      </div>
                    </div>

                    <div class="list-waiting">
                      <p v-if="danhSachLoc.length === 0" class="text-center text-muted mt-3">
                        Không có khách nào thỏa mãn tìm kiếm
                      </p>

                      <div v-for="khach in danhSachLoc" :key="khach.id" class="customer-card">
                        <div class="card-header">
                          <span class="customer-name">{{
                            khach.tenKhachHang
                          }}</span>
                          <span class="table-badge">{{ khach.maBan }}</span>
                        </div>

                        <div class="card-body">
                          <p class="time-info">
                            {{ formatDate(khach.thoiGianDat) }}
                          </p>
                          <div class="card-footer">
                            <button class="btn btn-checkable">
                              Có thể check-in
                            </button>
                          </div>
                          <div class="countdown-layout">
                            <div>
                              <p class="arrival-note">Khách hàng sẽ tới sau</p>
                            </div>
                            <div class="countdown-timer">
                              {{ getCountdown(khach.thoiGianDat) }}
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

          <!-- Sơ đồ tầng 2 -->
          <div v-show="activeFloor === 2" class="floor-map">
            <div class="floor-plan-layout">
              <div class="floor-plan-section">
                <div class="floor-header"></div>

                <div class="grid-container">
                  <div class="grid-canvas" @dragover.prevent @drop="onDrop" :class="{ 'editing-mode': isEditing }">
                    <div v-for="ban in banTheoTang" :key="ban.idBanAn" class="table-card"
                      :class="{ 'highlight-red': Number(ban.trangThai) === 0 }" @click="openManageModal(ban)" :style="{
                        gridColumnStart: ban.column,
                        gridRowStart: ban.row,
                        gridColumnEnd: 'span 4',
                        gridRowEnd: 'span 2',
                        cursor: 'pointer',
                      }">
                      <div class="table-content">
                        <div class="table-id">
                          <strong>{{ ban.maBan }}</strong>
                        </div>
                        <div class="table-id">({{ ban.soCho }} chỗ)</div>
                        <div :class="['status-tag', getStatusClass(ban.trangThai)]">
                          {{ getStatusText(ban.trangThai) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="list-section">
                <div class="list-card">
                  <div class="checkin-container">
                    <h5 style="font-weight: bold; font-size: 16px">
                      Khách chờ check-in
                    </h5>
                    <div class="filter-section">
                      <div class="filter-group">
                        <label class="filter-label">
                          <i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm
                        </label>
                        <div class="filter-input-wrapper">
                          <input type="text" v-model="searchQuery" placeholder="SĐT khách hàng" />
                        </div>
                      </div>

                      <div class="filter-group">
                        <label class="filter-label">
                          <i class="fa-solid fa-calendar-days"></i> Lọc theo
                          ngày đến
                        </label>
                        <div class="filter-input-wrapper">
                          <input type="date" v-model="filterDate" class="date-input" />
                        </div>
                      </div>
                    </div>

                    <div class="list-waiting">
                      <p v-if="danhSachLoc.length === 0" class="text-center text-muted mt-3">
                        Không có khách nào thỏa mãn tìm kiếm
                      </p>

                      <div v-for="khach in danhSachLoc" :key="khach.id" class="customer-card">
                        <div class="card-header">
                          <span class="customer-name">{{
                            khach.tenKhachHang
                          }}</span>
                          <span class="table-badge">{{ khach.maBan }}</span>
                        </div>

                        <div class="card-body">
                          <p class="time-info">
                            {{ formatDate(khach.thoiGianDat) }}
                          </p>
                          <div class="card-footer">
                            <button class="btn btn-checkable">
                              Có thể check-in
                            </button>
                          </div>
                          <div class="countdown-layout">
                            <div>
                              <p class="arrival-note">Khách hàng sẽ tới sau</p>
                            </div>
                            <div class="countdown-timer">
                              {{ getCountdown(khach.thoiGianDat) }}
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
        </div>
      </div>
    </div>
  </div>

  <div v-if="isShowModal" class="modal-overlay">
    <div class="modal-box" :class="{ 'modal-fullscreen': modalView === 'addFood' }">
      <div class="modal-header-custom">
        <h4 class="modal-title-custom">
          {{ modalView === 'info' ? `Check-in bàn ${selectedBan?.maBan}` : 'Thêm món ăn' }}
        </h4>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>

      <div class="modal-body-custom">

        <div v-if="modalView === 'info'">
          <div v-if="selectedPhieu || Number(selectedBan?.trangThai) === 1"
            class="alert alert-danger p-3 mb-3 border-0 shadow-sm"
            style="background-color: #fff5f5; border-left: 5px solid #7d161a !important;">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <div class="mb-1"><i class="fa-solid fa-ticket me-2"></i><strong>Mã phiếu:</strong> {{
                  selectedPhieu?.maDatBan || 'N/A' }}</div>
                <div class="mb-1"><i class="fa-solid fa-user me-2"></i><strong>Khách:</strong> {{
                  selectedPhieu?.tenKhachHang || 'Khách lẻ' }}</div>
                <div><i class="fa-solid fa-clock me-2"></i><strong>Giờ vào:</strong> {{
                  formatDate(selectedPhieu?.thoiGianDat) || '---' }}</div>
              </div>
              <span class="badge bg-danger p-2">ĐANG PHỤC VỤ</span>
            </div>
          </div>

          <h6 class="section-title">Thông tin bàn</h6>
          <div class="info-row">
            <span>Mã bàn:</span>
            <strong>{{ selectedBan?.maBan }}</strong>
          </div>
          <div class="info-row">
            <span>Sức chứa:</span>
            <strong>{{ selectedBan?.soCho }} người</strong>
          </div>
          <div class="info-row align-items-center">
            <span>Trạng thái:</span>
            <span class="badge-status">{{ getStatusText(selectedBan?.trangThai) }}</span>
          </div>
          <div class="info-row">
            <span>Vị trí:</span>
            <strong>Tầng {{ selectedBan?.soTang }}</strong>
          </div>
          <div class="info-row">
            <span>Nhân viên:</span>
            <strong>{{ currentStaffName || 'Chưa xác định' }}</strong>
          </div>

          <div v-if="listMonDaChon.length > 0" class="selected-summary mt-3">
            <div class="d-flex justify-content-between">
              <span class="text-success fw-bold">Món vừa thêm:</span>
              <span class="text-danger fw-bold">{{ totalTempPrice.toLocaleString() }}đ</span>
            </div>
            <ul class="summary-list">
              <li v-for="item in listMonDaChon" :key="item.id">
                {{ item.name }} <span class="text-muted">x{{ item.quantity }}<span class="text-muted">: <strong>{{ item.price }} VNĐ</strong></span></span>
              </li>
            </ul>
          </div>

          <hr class="my-3" />

          <div class="action-buttons">
            <button class="btn-action" :class="{ 'has-items': listMonDaChon.length > 0 }" @click="switchToAddFood">
              <i class="fa-solid" :class="listMonDaChon.length > 0 ? 'fa-pen-to-square' : 'fa-plus'"></i>
              <span v-if="listMonDaChon.length === 0">Thêm món</span>
              <span v-else>
                Đã chọn {{ listMonDaChon.length }} món
              </span>
            </button>
            <button class="btn-action">QR đặt món</button>
            <button class="btn-action">Xem đơn hàng</button>
            <button class="btn-action">Đổi bàn</button>
          </div>

          <hr class="my-3" />

          <h6 class="section-title">Tùy chỉnh trạng thái bàn</h6>
          <div class="status-options">
            <div class="status-item" :class="{ 'active-border': selectedBan?.trangThai === 0 }"
              @click="() => (selectedBan.trangThai = 0)">
              <label>
                <i :class="selectedBan?.trangThai === 0 ? 'fa-solid fa-circle-dot' : 'fa-regular fa-circle'"></i>
                Trống
              </label>
            </div>

            <div class="status-item" :class="{ 'active-border': selectedBan?.trangThai === 1 }"
              @click="() => (selectedBan.trangThai = 1)">
              <label>
                <i :class="selectedBan?.trangThai === 1 ? 'fa-solid fa-circle-dot' : 'fa-regular fa-circle'"></i>
                Checked-in
              </label>
            </div>
          </div>

          <button class="btn btn-update-status mt-4" @click="updateStatus">
            Cập nhật trạng thái bàn
          </button>
        </div>

        <div v-else class="h-100 full-modal-content">
          <FoodList :initial-items="listMonDaChon" @close="modalView = 'info'" @save="handleSaveFood" />
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.layout-table {
  display: flex;
  background-color: white;
}

.search-form {
  border: solid 1px #cacaca;
  border-radius: 15px;
  padding-top: 1%;
  padding-left: 2%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.btn-outline {
  border: 1px solid #ccc !important;
  background-color: white;
  color: #333;
}

/* Tùy chỉnh thêm hiệu ứng khi hover vào nút chưa chọn */
.btn-outline:hover {
  border-color: #7d161a !important;
  color: #7d161a !important;
  background-color: #fff5f5 !important;
}

.navbar-search {
  width: 100%;
}

hr {
  border: 0;
  border-top: 2px solid #7d161a;
  /* Chỉnh độ dày ở đây */
}

.btn-checkable {
  background-color: #7d161a !important;
  /* Đỏ đậm hơn */
  color: white !important;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.table-container {
  border: 1px solid #dee2e6;
  /* Viền bao ngoài */
  border-radius: 15px;
  /* Độ bo góc bạn muốn */
  overflow: hidden;
  /* Quan trọng: Cắt các góc nhọn của header/footer bên trong */
}

.table thead tr th {
  background-color: #e9e9e9 !important;
  color: #767676;
  font-weight: 600;
}

/* Button tầng */
/* Trạng thái cho nút ĐANG được chọn (Active) */
.btn {
  background-color: white;
  color: #333;
}

.btn:hover {
  background-color: #5c0a16 !important;
  /* Đỏ đậm hơn một chút khi di chuột */
  color: white !important;
}

.btn-active {
  background-color: #7d161a !important;
  color: white !important;
  border: 1px solid #7d161a;
  cursor: default;
  /* Đã chọn rồi thì không hiện con trỏ tay */
}

.btn-active:hover {
  background-color: #5c0a16 !important;
  /* Đỏ đậm hơn một chút khi di chuột */
  color: white !important;
}

/* Frame chung */
.floor-frame {
  width: 100%;
  height: calc(100vh - 250px);
  /* Tự động tính toán: Toàn màn hình trừ đi phần header/search ở trên */
  min-height: 450px;
  /* Đảm bảo không quá nhỏ trên màn hình thấp */
  border: 1px solid #dee2e6;
  border-radius: 12px;
  background: #fff;
  padding: 16px;
  display: flex;
  overflow: hidden;
  /* Không cho phép cả khung lớn bị cuộn */
}

/* Nội dung sơ đồ */
.floor-map {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floor-title {
  font-size: 1rem;
  font-weight: 600;
  color: #7d161a;
}

.floor-info {
  margin-top: 6px;
  font-size: 0.875rem;
  /* 14px */
  color: #555;
  font-weight: 500;
}

.floor-plan-layout {
  display: flex;
  gap: 15px;
  height: 100%;
  width: 100%;
}

/* --- PHẦN SƠ ĐỒ BÀN --- */
.floor-plan-section {
  flex: 2;
  background: white;
  border-radius: 8px;
  border: 1px solid #ddd;
  display: flex;
  flex-direction: column;
}

.floor-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.tab-btn.active {
  background: #5c0a16;
  color: white;
}

.edit-pos-btn {
  background: #f5f5f5;
  border: 1px solid #ccc;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* Vùng chứa lưới có chức năng scroll */
.grid-container {
  flex-grow: 1;
  overflow: auto;
  /* Hiện thanh cuộn khi grid bên trong lớn hơn */
  position: relative;
  background-color: #f8f9fa;
}

/* Lưới bàn - Thiết lập kích thước cố định để buộc phải scroll */
.grid-canvas {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-rows: repeat(15,
      1fr);
  /* Tăng số hàng và cố định chiều cao mỗi hàng (ví dụ 100px) */
  gap: 15px;
  padding: 20px;
  width: 100%;
  min-width: 800px;
  /* Đảm bảo không bị quá hẹp trên màn hình nhỏ */
  box-sizing: border-box;
  background-image:
    linear-gradient(to right, #eee 1px, transparent 1px),
    linear-gradient(to bottom, #eee 1px, transparent 1px);
  background-size: calc(100% / 12) 100px;
}

.grid-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.grid-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.grid-container::-webkit-scrollbar-thumb {
  background: #7d161a;
  border-radius: 10px;
}

.grid-container::-webkit-scrollbar-thumb:hover {
  background: #5c0a16;
}

/* Thẻ bàn */
.table-card {
  background: white;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  font-size: 0.8rem;
}

.table-card.highlight-red {
  background: #f9ebeb;
}

.table-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.table-id strong {
  font-size: 14px;
  display: block;
}

.table-id span {
  font-size: 13px;
  color: #555;
}

/* Badge trạng thái */
.status-tag {
  text-align: center;
  padding: 6px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  color: white;
  margin-top: 10px;
}

.status-occupied-light {
  background-color: #e67e22;
}

.status-empty {
  background-color: #5c0a16;
}

.status-booked {
  background-color: #f1c40f;
  color: #333;
}

/* --- PHẦN DANH SÁCH --- */
/* Container chứa bộ lọc */
.filter-section {
  display: flex;
  flex-direction: column;
  /* Ép các nhóm lọc xếp chồng lên nhau */
  gap: 10px;
  margin-bottom: 15px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 10px;
}

/* Từng nhóm nhãn + ô nhập */
.filter-group {
  display: flex;
  flex-direction: column;
  /* QUAN TRỌNG: Nhãn ở trên, Input ở dưới */
  width: 100%;
}

/* Nhãn tiêu đề nhỏ gọn */
.filter-label {
  font-size: 11px !important;
  font-weight: 700;
  color: #656565;
  margin-bottom: 4px;
  /* Tạo khoảng cách với ô nhập */
  display: flex;
  align-items: center;
  gap: 5px;
  text-transform: uppercase;
}

/* Ô nhập liệu */
.filter-input-wrapper input {
  width: 100%;
  height: 34px;
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
}

.filter-input-wrapper input:focus {
  border-color: #7d161a;
  box-shadow: 0 0 0 2px rgba(125, 22, 26, 0.1);
}

.list-section {
  flex: 1.2;
}

.checkin-container {
  max-width: 450px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  font-family: Arial, sans-serif;
}

.title {
  font-weight: bold;
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
}

.search-input {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input input,
.date-input {
  width: 100%;
  padding: 6px 10px 6px 10px !important;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 13px;
}

/* Style cho từng Card khách hàng */
.customer-card {
  border: 1px solid #eee;
  border-radius: 15px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.customer-name {
  font-weight: bold;
  font-size: 16px;
}

.table-badge {
  background: #f0f0f0;
  color: #888;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 14px;
}

.time-info {
  font-size: 15px;
  margin-bottom: 15px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-checkin {
  background: #68051b;
  /* Màu đỏ sẫm như hình */
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
}

.countdown-timer {
  border: 1px solid #68051b;
  color: #68051b;
  padding: 5px 15px;
  border-radius: 8px;
  font-weight: bold;
  background: #fff5f5;
}

.arrival-note {
  font-size: 13px;
  color: #888;
  margin-top: 10px;
  margin-bottom: 0;
}

/* Tìm và thay thế class .list-waiting cũ bằng cái này */
.list-waiting {
  max-height: 15rem;
  /* Giới hạn chiều cao cố định (khoảng 3-4 card) */
  overflow-y: auto;
  /* Hiện thanh cuộn khi danh sách dài vượt quá max-height */
  padding-right: 8px;
  /* Khoảng cách để không đè lên card khi hiện thanh cuộn */

  /* Tùy chỉnh thanh cuộn (Scrollbar) cho Chrome/Edge/Safari */
}

.list-waiting::-webkit-scrollbar {
  width: 6px;
}

.list-waiting::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.list-waiting::-webkit-scrollbar-thumb {
  background: #7d161a;
  /* Màu đỏ sẫm đồng bộ với theme của bạn */
  border-radius: 10px;
}

.list-waiting::-webkit-scrollbar-thumb:hover {
  background: #5c0a16;
}

.countdown-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* CSS cho Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  width: 100%;
  max-width: 620px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  transition: all 0.1s ease-in-out;
}

.modal-box.modal-fullscreen {
  width: 80vw !important;
  height: 90vh !important;
  max-width: none !important;
  max-height: none !important;

  border-radius: 0 !important;
  margin: 0 !important;
}

.modal-header-custom {
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.modal-title-custom {
  color: #7d161a;
  font-weight: bold;
  margin: 0;
}

.modal-body-custom {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.h-100 {
  height: 100%;
}

.full-modal-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* Prevent double scrollbars */
}

.selected-summary {
  background: #f9f9f9;
  padding: 10px;
  border-radius: 6px;
  border: 1px dashed #ddd;
  font-size: 0.9rem;
}

.summary-list {
  margin: 5px 0 0 0;
  padding-left: 20px;
  color: #555;
  max-height: 180px;
  overflow-y: auto;
}

.btn-action.has-items {
  background: #5c0a16;
  color: white;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 1.2rem;
  cursor: pointer;
}

.modal-title-custom {
  margin: 0;
  font-weight: bold;
  color: #7d161a;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.badge-status {
  background: #7d161a;
  color: white;
  padding: 2px 12px;
  border-radius: 15px;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-action {
  width: 100%;
  padding: 10px;
  border-radius: 6px;
  border: none;
  font-weight: bold;
  transition:
    background 0.3s ease,
    color 0.3s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.btn-action:hover {
  background: linear-gradient(to right, #7d161a, #c0392b);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(125, 22, 26, 0.35);
}

.status-options {
  display: flex;

  flex-direction: row;
  gap: 10px;
  margin-top: 10px;
}

.status-item {
  padding: 3%;
  width: 100%;
  border: 1px solid #ddd;
  /* Viền mặc định xám */
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
  /* Màu chữ mặc định */
}

/* Khi được chọn: Viền đỏ, Chữ đỏ, Nền hồng nhạt */
.status-item.active-border {
  border: 2px solid #7d161a !important;
  /* Màu đỏ viền */
  color: #7d161a;
  background-color: #fff5f5;
  font-weight: bold;
}

.status-item i {
  flex: 1;
  margin-right: 10px;
  font-size: 1.1rem;
}

.status-item:hover:not(.active-border) {
  background-color: #f9f9f9;
}

.btn-update-status {
  width: 100%;
  background: #7d161a;
  color: white;
  border-radius: 8px;
  padding: 12px;
  border: none;
  font-weight: bold;
}

.table-card {
  cursor: pointer;
  /* Thêm con trỏ tay để khách biết bàn có thể click */
  transition: transform 0.2s;
}

.table-card:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

.close-btn {
  border: none;
  background-color: white;
}

:global(.swal2-container) {
  z-index: 20000 !important;
  /* Cao hơn 9999 của .modal-overlay */
}
</style>
