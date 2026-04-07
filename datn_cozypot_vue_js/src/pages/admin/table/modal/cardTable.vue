<script setup>
import {
  fetchAllBanAn,
  fetchTableStatusByDate,
} from "@/services/tableManageService";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { inject } from "vue";
import { usePermission } from "@/components/permissionHelper";

const { handleActionWithAuth } = usePermission();
// const tableStatusMapFromParent = inject('tableStatusMap', ref({}));
// const selectedDateFromParent = inject('selectedDate', ref(''));
const danhSachBanRef = inject("danhSachBan", ref([]));
const listKhuVucRef = inject("listKhuVuc", ref([]));
// ✅ Sử dụng data từ parent thay vì fetch riêng
const danhSachBan = computed(() => {
  // Nếu inject vào là computed (như đã sửa ở cha), ta dùng .value
  return danhSachBanRef.value || []; 
});

// const tableStatusMap = tableStatusMapFromParent;
// const selectedDate = selectedDateFromParent;
/* 1. KHỞI TẠO TRẠNG THÁI */
const activeFloor = ref(1);
// const danhSachBan = ref([]);
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
// const fetchAllBan = async () => {
//   try {
//     const data = await fetchAllBanAn();

//     danhSachBan.value = data.map((ban, index) => {
//       // 3 bàn mỗi hàng trên lưới 12 cột nếu chưa có tọa độ
//       const defaultCol = (index % 3) * 4 + 1;
//       const defaultRow = Math.floor(index / 3) * 2 + 1;

//       return {
//         ...ban,
//         column: ban.column || defaultCol,
//         row: ban.row || defaultRow,
//       };
//     });
//   } catch (e) {
//     console.error("Lỗi load danh sách bàn", e);
//   }
// };

/* 4. LOGIC TÍNH TOÁN (COMPUTED) */
// Lọc danh sách bàn theo tầng đang chọn
const banTheoTang = computed(() => {
  if (!danhSachBan.value.length) return [];

  return danhSachBan.value
    .filter((ban) => Number(ban.soTang) === Number(activeFloor.value))
    .sort((a, b) => {
      const rowA = Number(a.row) || 0;
      const rowB = Number(b.row) || 0;
      const colA = Number(a.column) || 0;
      const colB = Number(b.column) || 0;

      if (rowA !== rowB) return rowA - rowB;
      return colA - colB;
    });
});

// Sửa lại banTheoTungTang - bỏ normalizedRow, dùng row trực tiếp
const banTheoTungTang = computed(() => {
  const result = {};

  listTang.value.forEach((tang) => {
    result[tang] = danhSachBan.value
      .filter((ban) => Number(ban.soTang) === tang)
      .sort((a, b) => {
        const rowDiff = (Number(a.row) || 0) - (Number(b.row) || 0);
        return rowDiff !== 0
          ? rowDiff
          : (Number(a.column) || 0) - (Number(b.column) || 0);
      });
  });

  return result;
});

// const banTheoTang = computed(() => {
//   if (!danhSachBan.value.length) return [];
//   return danhSachBan.value.filter(
//     (ban) => Number(ban.soTang) === Number(activeFloor.value),
//   );
// });

// Thống kê số lượng bàn trống của tầng hiện tại
// const thongKeTang = computed(() => {
//   const total = banTheoTang.value.length;
//   const free = banTheoTang.value.filter((ban) => {
//     const stt = String(ban.trangThai).toLowerCase().trim();
//     return stt === "trống" || stt === "trong" || stt === "0";
//   }).length;
//   return { total, free };
// });
// Thống kê số lượng bàn trống theo ngày đã chọn
const thongKeTang = computed(() => {
  const banCuaTang = banTheoTungTang.value[activeFloor.value] || [];
  const total = banCuaTang.length;
  const free = banCuaTang.filter((ban) => {
    return Number(getTrangThaiTheoNgay(ban.idBanAn)) === 0;
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

  const cellWidth = canvas.offsetWidth / 12;
  const cellHeight = 100; // mỗi grid row = 100px (khớp với gridTemplateRows)

  const x = event.clientX - rect.left;
  const y = event.clientY - rect.top;

  let newCol = Math.floor(x / cellWidth) + 1;
  let newRow = Math.floor(y / cellHeight) + 1;

  newCol = Math.min(Math.max(newCol, 1), 10);
  newRow = Math.max(newRow, 1);

  const oldRow = draggingTable.value.row;
  const oldCol = draggingTable.value.column;

  // Tìm bàn đang ở vị trí mới (cùng tầng)
  const banBiDe = danhSachBan.value.find(
    (ban) =>
      ban.idBanAn !== draggingTable.value.idBanAn &&
      Number(ban.soTang) === Number(draggingTable.value.soTang) &&
      Number(ban.row) === newRow &&
      Number(ban.column) === newCol,
  );

  if (banBiDe) {
    // Swap vị trí: bàn bị đè lấy vị trí cũ của bàn đang kéo
    banBiDe.row = oldRow;
    banBiDe.column = oldCol;
  }

  // Bàn đang kéo di chuyển đến vị trí mới
  draggingTable.value.row = newRow;
  draggingTable.value.column = newCol;

  draggingTable.value = null;
};
const currentTime = ref("");

let timer = null;

const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString("vi-VN");
};

const selectedDate = ref(
  new Date().toISOString().slice(0, 10), // yyyy-MM-dd
);

/**
 * Map trạng thái bàn theo ngày
 * key: idBan
 * value: status
 */
const tableStatusMap = ref({});

// Thêm vào script setup
const getTrangThaiTheoNgay = (banId) => {
  return Number(tableStatusMap.value[banId] ?? 0);
};

const fetchTableStatus = async () => {
  try {
    const data = await fetchTableStatusByDate(selectedDate.value);

    const newMap = {};
    data.forEach((item) => {
      newMap[item.banId] = item.trangThai;
    });

    tableStatusMap.value = newMap;
  } catch (error) {
    tableStatusMap.value = {};
  }
};

// Tự động xếp bàn theo lưới 3 cột nếu chưa có tọa độ hợp lệ
const autoLayoutBan = () => {
  if (!danhSachBan.value.length) return;

  // Group theo tầng
  const banTheoTangMap = {};
  danhSachBan.value.forEach((ban) => {
    const tang = Number(ban.soTang);
    if (!banTheoTangMap[tang]) banTheoTangMap[tang] = [];
    banTheoTangMap[tang].push(ban);
  });

  // Xếp lại tọa độ cho từng tầng độc lập
  Object.values(banTheoTangMap).forEach((dsBan) => {
    dsBan.forEach((ban, index) => {
      ban.column = (index % 3) * 4 + 1; // 3 bàn mỗi hàng, mỗi bàn span 3 + gap 1
      ban.row = Math.floor(index / 3) * 2 + 1; // mỗi hàng cách nhau 2
    });
  });
};

// Gọi khi danhSachBan có data
watch(
  danhSachBan,
  (newVal) => {
    if (newVal.length) {
      autoLayoutBan();
    }
  },
  { immediate: true },
);

const listKhuVuc = computed(() => {
  return listKhuVucRef.value || [];
});

const listTang = computed(() => {
  if (!listKhuVuc.value.length) return [];
  return [...new Set(listKhuVuc.value.map((kv) => Number(kv.tang)))].sort((a, b) => a - b);
});

watch(
  listTang,
  (newList) => {
    if (newList.length && !newList.includes(activeFloor.value)) {
      activeFloor.value = newList[0];
    }
  },
  { immediate: true },
);

watch(
  selectedDate,
  async () => {
    await fetchTableStatus();
  },
  { immediate: true },
);
watch(
  tableStatusMap,
  (val) => {
    console.log("Table status map updated:", val);
  },
  { deep: true },
);

watch(
  danhSachBan,
  (newVal) => {
    if (newVal && newVal.length > 0) {
      autoLayoutBan();
    }
  },
  { immediate: true, deep: true }
);

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);
});

onUnmounted(() => {
  clearInterval(timer);
});


// onMounted(() => {
//     fetchAllBan();
//    fetchTableStatus();
// });
</script>
<template>
  <div class="search-form">
    <div>
      <h5 style="font-size: 1rem; font-weight: bold">Khu vực</h5>
      <div class="mb-3">
        <button
          v-for="tang in listTang"
          :key="tang"
          class="btn me-2"
          :class="activeFloor === tang ? 'btn-active' : 'btn-outline'"
          @click="activeFloor = tang"
        >
          Tầng {{ tang }}
        </button>

        <div class="floor-info mt-2">
          Tầng {{ activeFloor }} - Trống {{ thongKeTang.free }}/{{
            thongKeTang.total
          }}
          bàn
        </div>
      </div>
    </div>
    <div>
      <!-- //Trạng thái bàn theo ngày ở đây -->
      <div class="filter-date px-3">
        <label>📅 Lọc theo ngày</label>
        <input type="date" v-model="selectedDate" class="form-control" />
      </div>
    </div>
  </div>

  <div class="notice-contain gap-2">
    <div
      class="notice-item notice mt-3"
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
    <div class="notice-item notice mt-3">
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
    <div class="notice-item notice mt-3">
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
    <div class="notice-item notice mt-3">
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
              Thời gian hiện tại: <strong>{{ currentTime }}</strong>
            </div>
            <button
  class="edit-pos-btn"
  @click="handleActionWithAuth(() => { isEditing = !isEditing }, 'ADMIN')"
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
              :style="{ gridTemplateRows: `repeat(${maxRow}, 100px)` }"
            >
              <div
                v-for="ban in banTheoTungTang[activeFloor]"
                :key="ban.idBanAn"
                class="table-card"
                :class="{
                  'highlight-red': getTrangThaiTheoNgay(ban.id) === 0, // ✅ Dùng trạng thái theo ngày
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
                  <div
                    :class="[
                      'status-tag',
                      getStatusClass(getTrangThaiTheoNgay(ban.id)),
                    ]"
                  >
                    {{ getStatusText(getTrangThaiTheoNgay(ban.id)) }}
                  </div>
                  <div class="small">
                    ID: {{ ban.id }} | Status:
                    {{ getTrangThaiTheoNgay(ban.id) }}
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

<style src="@/pages/admin/table/tableManagementStyle.css"></style>
<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");
.search-form {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
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

.btn-active {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white !important;
  border: 1px solid #7d161a !important;
}

/* 2. Trạng thái KHÔNG CHỌN (Nền trắng, chữ đỏ, viền đỏ) */
.btn-outline {
  background-color: white !important;
  color: #7d161a !important;
  border: 1px solid #7d161a !important;
}

/* Hiệu ứng khi di chuột vào nút không chọn */
.btn-outline:hover {
  background-color: #fdf2f2 !important; /* Màu hồng nhạt */
}

.table-card {
  position: relative;
}
</style>
