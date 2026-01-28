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
  <div>
    <div class="list-card">
      <table class="table">
        <thead>
          <tr class="table-dark">
            <th>Bàn</th>
            <th>Số ghế</th>
            <th>Trạng thái</th>
            <th>Đặt online</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="ban in danhSachBan" :key="ban.idBanAn">
            <td>
              <strong>{{ ban.maBan }}</strong>
            </td>
            <td>{{ ban.soCho }} chỗ</td>
            <td>
              <span
                :class="[
                  'badge',
                  String(ban.trangThai) === '0'
                    ? 'badge-empty'
                    : String(ban.trangThai) === '1'
                      ? 'badge-occupied'
                      : 'badge-booked',
                ]"
              >
                {{ getStatusText(ban.trangThai) }}
              </span>
            </td>
            <td>
              {{ ban.loaiDatBan == 0 ? "Không cho phép" : "Cho phép" }}
            </td>
            <td>
              <button
                class="action-list"
                style="border: none; background-color: white"
              >
                <i class="fa-regular fa-pen-to-square action-icon"></i>
              </button>
              <button
                class="action-list"
                style="border: none; background-color: white"
              >
                <i class="fa-solid fa-eye"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
@import url("styles/tableManagementStyle.css");

.table {
  margin-bottom: 0 !important;
}

.table thead tr th {
  background-color: #7d161a;
}
.list-card {
  border-radius: 12px;
  overflow: hidden; /* QUAN TRỌNG */
  border: 1px solid #ddd;
}
/* Body */
.table tbody tr:last-child td {
  border-bottom: none;
}
</style>