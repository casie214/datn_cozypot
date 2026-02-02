<script setup>
import { fetchAllBanAn } from "@/services/tableManageService";
import { computed, onMounted, onUnmounted, ref } from "vue";

/* 1. KHỞI TẠO TRẠNG THÁI */
const activeFloor = ref(1);
const danhSachBan = ref([]);
const isEditing = ref(false);
const draggingTable = ref(null);

/* 2. CÁC HÀM TIỆN ÍCH (Sửa lỗi "is not a function") */
const getStatusText = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "Trống";
  if (status === "1") return "Có khách";
  return "Đã đặt";
};

const getStatusClass = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "status-empty";
  if (status === "1") return "status-occupied-light";
  return "status-booked";
};


/* 3. FETCH DỮ LIỆU TỪ BACKEND */
const fetchAllBan = async () => {
  try {
    const data = await fetchAllBanAn();

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

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);
});

onUnmounted(() => {
  clearInterval(timer);
});

onMounted(() => {
  fetchAllBan();
});
</script>
<template>
  <div class="container search-form">
    <h5 style="font-size: 1rem; font-weight: bold">Khu vực</h5>
    <div class="mb-3">
      <button
        class="btn me-2"
        :class="activeFloor === 1 ? 'btn-active' : 'btn-outline'"
        @click="activeFloor = 1"
      >
        Tầng 1
      </button>
      <button
        class="btn"
        :class="activeFloor === 2 ? 'btn-active' : 'btn-outline'"
        @click="activeFloor = 2"
      >
        Tầng 2
      </button>

      <div class="floor-info mt-2">
        Tầng {{ activeFloor }} - Trống {{ thongKeTang.free }}/{{
          thongKeTang.total
        }}
        bàn
      </div>
    </div>
  </div>

  <div class="notice-contain">
    <div class="container notice mt-3">
      <h5 style="font-size: 1rem; font-weight: bold">
        <i
          class="fa-solid fa-wifi"
          style="
            border-color: darkgrey;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
          "
        ></i>
        Có thể đặt online
      </h5>
    </div>
    <div
      class="container notice mt-3"
      style="border: #7d161a 1px solid; background-color: #efe3e4"
    >
      <h5 style="font-size: 1rem; font-weight: bold">
        <i
          class="fa-solid fa-check"
          style="
            background-color: #7d161a;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
            border-color: #7d161a;
            color: white;
          "
        ></i>
        Trống
      </h5>
    </div>
    <div class="container notice mt-3">
      <h5 style="font-size: 1rem; font-weight: bold">
        <i
          class="fa-solid fa-users"
          style="
            border-color: #e2583b;
            background-color: #e2583b;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
            color: white;
          "
        ></i>
        Có khách
      </h5>
    </div>
    <div class="container notice mt-3">
      <h5 style="font-size: 1rem; font-weight: bold">
        <i
          class="fa-regular fa-clock"
          style="
            border-color: #fdd165;
            background-color: #fdd165;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
          "
        ></i>
        Đã đặt
      </h5>
    </div>
    <div class="container notice mt-3">
      <h5 style="font-size: 1rem; font-weight: bold">
        <i
          class="fa-solid fa-circle-exclamation"
          style="
            border-color: darkgray;
            box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
            background-color: #e9e9e9;
          "
        ></i>
        Đang dọn
      </h5>
    </div>
  </div>
  <div class="contain-frame mt-3">
    <div class="floor-frame">
      <div class="floor-plan-layout">
        <div class="floor-plan-section">
          <div class="floor-header">
            <div class="current-time">
              Thời gian hiện tại: {{ currentTime }}
            </div>
            <button
              class="edit-pos-btn"
              @click="isEditing = !isEditing"
              :class="{ 'btn-save': isEditing }"
            >
              <i
                :class="
                  isEditing
                    ? 'fa-solid fa-floppy-disk'
                    : 'fa-solid fa-pen-to-square'
                "
              ></i>
              {{ isEditing ? " Lưu vị trí" : " Chỉnh sửa vị trí bàn" }}
            </button>
          </div>
          <div class="grid-container">
            <div
              class="grid-canvas"
              @dragover.prevent
              @drop="onDrop"
              :class="{ 'editing-mode': isEditing }"
            >
              <div
                v-for="ban in banTheoTang"
                :key="ban.idBanAn"
                class="table-card"
                :class="{
                  'highlight-red': String(ban.trangThai) === '0',
                  'draggable-item': isEditing,
                }"
                :draggable="isEditing"
                @dragstart="onDragStart(ban)"
                :style="{
                  gridColumnStart: ban.column,
                  gridRowStart: ban.row,
                  gridColumnEnd: 'span 3',
                  gridRowEnd: 'span 2',
                }"
              >
               <!-- ICON WIFI -->
  <i
    v-if="ban.loaiDatBan"
    class="fa-solid fa-wifi wifi-icon"
    title="Có thể đặt online"
  ></i>
                <div class="table-content">
                  <strong>{{ ban.maBan }}</strong>
                  <div class="small">({{ ban.soCho }} chỗ)</div>
                  <div class="small">Khu vực: {{ ban.tenKhuVuc }}</div>
                  <div :class="['status-tag', getStatusClass(ban.trangThai)]">
                    {{ getStatusText(ban.trangThai) }}
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
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("styles/tableManagementStyle.css");
.wifi-icon {
  position: absolute;
  top: 8px;
  right: 8px;

  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;

  font-size: 12px;
  color: black;

  background-color: #ffffff;
  border-radius: 30%;
  border: 1px solid #e0e0e0;

  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.15);
  z-index: 3;
}

.table-card {
  position: relative;
}

</style>
