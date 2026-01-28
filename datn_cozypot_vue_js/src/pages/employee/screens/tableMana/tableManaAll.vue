<script setup>
import { fetchAllKhuVuc } from "@/services/tableManageService";
import { computed, onMounted, onUnmounted, ref } from "vue";

/* 1. KHỞI TẠO TRẠNG THÁI */
const danhSachBan = ref([]);
const listKhuVuc = ref([]);
const isEditing = ref(false);
const draggingTable = ref(null);

/* 2. CÁC HÀM TIỆN ÍCH (Sửa lỗi "is not a function") */
const getStatusText = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "Trống";
  if (status === "1") return "Có khách";
  return "Đặt trước";
};

const getStatusClass = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "status-empty";
  if (status === "1") return "status-occupied-light";
  return "status-booked";
};

// --- HÀM MỚI BỔ SUNG ĐỂ SỬA LỖI ---
const getBadgeStatusClass = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "badge-empty";
  if (status === "1") return "badge-occupied";
  return "badge-booked";
};

/* 3. FETCH DỮ LIỆU TỪ BACKEND */
const fetchAllBan = async () => {
  try {
    const res = await fetch("http://localhost:8080/dat-ban/danh-sach-ban-an");
    const data = await res.json();

    danhSachBan.value = data.map((ban, index) => {
      // 3 bàn mỗi hàng trên lưới 12 cột nếu chưa có tọa độ
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

const handleFetchAllKhuVuc = async () => {
  try {
    listKhuVuc.value = await fetchAllKhuVuc();
  } catch (error) {
    console.log(error);
  }
};

/* 4. LOGIC TÍNH TOÁN (COMPUTED) */
// Lọc danh sách bàn theo tầng đang chọn
const banTheoTang = computed(() => {
  if (!danhSachBan.value.length) return [];
  return danhSachBan.value.filter(
    (ban) => Number(ban.soTang) === Number(activeFloor.value),
  );
});

// Thống kê số lượng bàn trống của tầng hiện tại
const thongKeTang = computed(() => {
  const total = banTheoTang.value.length;
  const free = banTheoTang.value.filter((ban) => {
    const stt = String(ban.trangThai).toLowerCase().trim();
    return stt === "trống" || stt === "trong" || stt === "0";
  }).length;
  return { total, free };
});

/* 5. LOGIC KÉO THẢ */
const onDragStart = (ban) => {
  if (isEditing.value) draggingTable.value = ban;
};

const onDrop = (event) => {
  if (!isEditing.value || !draggingTable.value) return;
  event.preventDefault();

  const canvas = event.currentTarget;
  const rect = canvas.getBoundingClientRect();

  // Tính toán kích thước ô dựa trên lưới 12 cột
  const cellWidth = canvas.offsetWidth / 12;
  const cellHeight = 100; // Chiều cao hàng cố định cho grid để dễ tính toán

  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;

  let newCol = Math.floor(x / cellWidth) + 1;
  let newRow = Math.floor(y / cellHeight) + 1;

  const table = danhSachBan.value.find(
    (b) => b.idBanAn === draggingTable.value.idBanAn,
  );

  if (table) {
    table.column = Math.min(Math.max(newCol, 1), 9); // Giới hạn cột trong lưới 12
    table.row = Math.max(newRow, 1);
  }
  draggingTable.value = null;
};

const currentTime = ref("");

let timer = null;

const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString("vi-VN");
};

// Pop up

const showAddModal = ref(false);

const openAddModal = () => {
  showAddModal.value = true;
};

const closeAddModal = () => {
  showAddModal.value = false;
};

const form = ref({
  soNguoiToiDa: null,
  idKhuVuc: "",
  loaiDatBan: 0,
});

const submitAddBan = async () => {
  try {
    await fetch("http://localhost:8080/dat-ban/add-ban-an", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(form.value),
    });

    closeAddModal();
    await fetchAllBan();
    alert("Thêm thành công!")
  } catch (e) {
    console.error("Lỗi thêm bàn", e);
  }
};

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);

  fetchAllBan();
  handleFetchAllKhuVuc();
});

onUnmounted(() => {
  clearInterval(timer);
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
      <div class="header-manager">
        <div>
          <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem">
            Quản lý bàn
          </h3>
        </div>
        <div>
          <button class="btn" @click="openAddModal">Thêm bàn</button>
        </div>
      </div>

      <!-- TAB -->
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link
            class="nav-link"
            active-class="active"
            to="/tableManage/trang-thai"
          >
            <i class="fa-solid fa-list"></i>
            Lịch đặt bàn
          </router-link>
        </li>

        <li class="nav-item">
          <router-link
            class="nav-link"
            active-class="active"
            to="/tableManage/danh-sach"
          >
            <i class="fa-regular fa-calendar"></i>
            Danh sách bàn
          </router-link>
        </li>
      </ul>

      <hr />

      <div class="contain-frame mt-3">
        <router-view />
        <!-- 👈 nội dung đổi ở đây -->
      </div>
    </div>
  </div>

  <!-- POPUP THÊM BÀN -->
  <div v-if="showAddModal" class="modal-overlay">
    <div class="modal-box">
      <div class="modal-header">
        <h4>Thêm bàn ăn</h4>
        <button class="close-btn" @click="closeAddModal">&times;</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label>Số người tối đa</label>
          <input type="number" v-model="form.soNguoiToiDa" />
        </div>

        <select class="form-select" v-model="form.idKhuVuc">
          <option value="" disabled>-- Chọn khu vực --</option>
          <option
            v-for="khuVuc in listKhuVuc"
            :key="khuVuc.id"
            :value="khuVuc.id"
          >
            {{ khuVuc.tenKhuVuc }}
          </option>
        </select>

        <div class="form-check">
          <input
            class="form-check-input"
            type="checkbox"
            value=""
            v-model="form.loaiDatBan"
            :true-value="1"
            :false-value="0"
          />
          <label class="form-check-label" for="checkIndeterminate">
            Có thể đặt online
          </label>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="closeAddModal">Hủy</button>
        <button class="btn btn-active" @click="submitAddBan">Lưu</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.layout-table {
  display: flex;
  background-color: white;
}

.header-manager {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-search {
  width: 100%;
}

.search-form {
  border: solid 1px #cacaca;
  border-radius: 15px;
  padding: 15px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.btn {
  background-color: #7d161a;
  color: white;
}

.btn:hover {
  background-color: white;
  color: black;
  border: 1px solid #7d161a;
}

.btn-outline {
  background-color: white;
  color: black;
}

.form-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 12px;
}

.form-group input,
.form-group select {
  padding: 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  background: #fff;
  width: 420px;
  border-radius: 12px;
  animation: fadeIn 0.25s ease;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header,
.modal-footer {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
}

.modal-footer {
  border-top: 1px solid #eee;
  border-bottom: none;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-body {
  padding: 16px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
}
</style>
