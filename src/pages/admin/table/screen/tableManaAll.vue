<script setup>
import {
  addBanAn,
  createBanFull,
  createKhuVuc,
  fetchAllBanAn,
  fetchAllKhuVuc,
  fetchTableStatusByDate,
} from "@/services/tableManageService";
import { computed, onMounted, onUnmounted, ref, watch } from "vue";
import { provide } from "vue";
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
import { usePermission } from "@/components/permissionHelper";
import Swal from "sweetalert2";

const { handleActionWithAuth } = usePermission();
/* 1. KHỞI TẠO TRẠNG THÁI */
const danhSachBan = ref([]);
const listKhuVuc = ref([]);

const refreshKey = ref(0);

const listLoaiDatBanOptions = [
  { label: "Cho phép đặt online", value: 1 },
  { label: "Không cho phép", value: 0 },
];

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

const handleFetchAllKhuVuc = async () => {
  try {
    listKhuVuc.value = await fetchAllKhuVuc();
  } catch (error) {
    console.log(error);
  }
};

const currentTime = ref("");

let timer = null;

const updateTime = () => {
  const now = new Date();
  currentTime.value = now.toLocaleTimeString("vi-VN");
};

// Pop up
const listTang = computed(() => {
  return [...new Set(listKhuVuc.value.map((kv) => kv.tang))];
});

const isTangMoi = computed(() => {
  if (!selectedTang.value) return false;
  return !listTang.value.includes(Number(selectedTang.value));
});
const selectedTang = ref(null);
const newKhuVucName = ref("");
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
  tang: "",
});

// watch(
//   () => form.value.idKhuVuc,
//   (id) => {
//     console.log('ID chọn:', id, typeof id);
//     console.log('Danh sách KV:', listKhuVuc.value);

//     const khuVuc = listKhuVuc.value.find(kv => kv.id === Number(id));
//     console.log('KV tìm được:', khuVuc);

//     form.value.tang = khuVuc ? khuVuc.tang : 0;
//   }
// );

const khuVucTheoTang = computed(() => {
  if (!form.value.tang) return [];
  return listKhuVuc.value.filter((kv) => kv.tang === Number(form.value.tang));
});

//Thêm tầng
// Popup thêm tầng riêng
const showAddTangModal = ref(false);

const openAddTangModal = () => {
  showAddTangModal.value = true;
};

const closeAddTangModal = () => {
  showAddTangModal.value = false;
};

const tangForm = ref({
  tang: "",
  tenKhuVuc: "",
  moTa: "",
});

const submitAddTang = async () => {
  try {
    // Chuyển giá trị nhập vào thành Number để kiểm tra chắc chắn
    const parsedTang = Number(tangForm.value.tang);

    if (!tangForm.value.tang || isNaN(parsedTang) || parsedTang <= 0) {
      Swal.fire("Cảnh báo", "Tầng phải là một số hợp lệ và lớn hơn 0", "warning");
      return;
    }

    if (!tangForm.value.tenKhuVuc) {
      Swal.fire("Cảnh báo", "Vui lòng nhập tên khu vực", "warning");
      return;
    }

    await createKhuVuc({
      tang: parsedTang, // Dùng số đã parse
      tenKhuVuc: tangForm.value.tenKhuVuc.trim(),
      moTa: tangForm.value.moTa,
    });

    Swal.fire("Thành công", "Thêm tầng và khu vực thành công!", "success");

    await handleFetchAllKhuVuc();
    refreshKey.value += 1;

    tangForm.value = {
      tang: "",
      tenKhuVuc: "",
      moTa: "",
    };

    closeAddTangModal();
  } catch (error) {
    console.log("FULL ERROR:", error);
    Swal.fire("Lỗi", error.response?.data || error.message || "Có lỗi xảy ra", "error");
  }
};

watch(selectedTang, (val) => {
  form.value.tang = Number(val);

  if (isTangMoi.value) {
    form.value.idKhuVuc = "";
  }
});

watch(
  () => form.value.tang,
  () => {
    form.value.idKhuVuc = "";
  },
);

// const submitAddBan = async () => {
//   try {
//     await addBanAn(form.value);
//     closeAddModal();

//     await refreshData();
//     // await fetchAllBan();
//     alert("Thêm thành công!");
//   } catch (e) {
//     console.error("Lỗi thêm bàn", e);
//   }
// };

const submitAddBan = async () => {
  try {
    // 1️⃣ Validate cơ bản
    if (!form.value.soNguoiToiDa) {
      Swal.fire("Cảnh báo", "Vui lòng nhập số người tối đa", "warning");
      return;
    }

    if (!form.value.tang) {
      Swal.fire("Cảnh báo", "Vui lòng chọn hoặc nhập tầng", "warning");
      return;
    }

    // 2️⃣ Xử lý tên khu vực
    let tenKhuVucFinal = null;

    // Nếu đã chọn khu vực cũ
    if (form.value.idKhuVuc) {
      const kv = listKhuVuc.value.find((k) => k.id === form.value.idKhuVuc);
      tenKhuVucFinal = kv?.tenKhuVuc;
    }

    // Nếu không chọn khu vực → phải nhập tên mới
    if (!tenKhuVucFinal) {
      if (!newKhuVucName.value) {
        Swal.fire("Cảnh báo", "Vui lòng nhập tên khu vực mới", "warning");
        return;
      }
      tenKhuVucFinal = newKhuVucName.value.trim();
    }

    // 3️⃣ Gọi API create-full
    await createBanFull({
      soNguoiToiDa: Number(form.value.soNguoiToiDa),
      loaiDatBan: form.value.loaiDatBan,
      tang: Number(form.value.tang),
      tenKhuVuc: tenKhuVucFinal,
    });

    // 4️⃣ Reset form
    form.value = {
      soNguoiToiDa: null,
      idKhuVuc: "",
      loaiDatBan: 0,
      tang: "",
    };

    newKhuVucName.value = "";

    closeAddModal();
    await refreshData();
    refreshKey.value += 1;

    Swal.fire("Thành công", "Thêm bàn mới thành công!", "success");
  } catch (error) {
    console.error("Lỗi thêm bàn:", error);
    Swal.fire("Lỗi", "Có lỗi xảy ra khi thêm bàn!", "error");
  }
};

const refreshData = async () => {
  await Promise.all([
    fetchAllBan(),
    // fetchTableStatus()
  ]);
};

// ✅ Provide hàm refresh và data cho component con
provide("refreshTableData", refreshData);
provide("danhSachBan", danhSachBan);
// provide('tableStatusMap', tableStatusMap);
// provide('selectedDate', selectedDate);

onMounted(() => {
  updateTime();
  timer = setInterval(updateTime, 1000);

  fetchAllBan();
  handleFetchAllKhuVuc();
});

provide("listKhuVuc", listKhuVuc);

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
          <button class="btn" @click="handleActionWithAuth(() => openAddModal(), 'ADMIN')">Thêm bàn</button>
          <button class="btn ms-2" @click="handleActionWithAuth(() => openAddTangModal(), 'ADMIN')">Thêm tầng</button>
        </div>
      </div>

      <!-- TAB -->
      <ul class="nav nav-tabs">
        <li class="nav-item">
          <router-link
            class="nav-link"
            exact-active-class="active"
            to="/manage/all"
          >
            <i class="fa-solid fa-list"></i>
            Lịch đặt bàn
          </router-link>
        </li>

        <li class="nav-item">
          <router-link
            class="nav-link"
            active-class="active"
            to="/manage/all/danh-sach"
          >
            <i class="fa-regular fa-calendar"></i>
            Danh sách bàn
          </router-link>
        </li>
      </ul>

      <hr />

      <div class="contain-frame mt-3">
        <router-view
          :key="refreshKey"
          :selectedDate="selectedDate"
          :tableStatusMap="tableStatusMap"
        />
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

        <div class="form-group">
          <label>Tầng</label>
          <div class="multiselect-wrapper-sm">
            <Multiselect 
              v-model="selectedTang" 
              :options="listTang" 
              :searchable="true" 
              :create-option="true" 
              :canClear="true" 
              placeholder="Chọn hoặc nhập tầng mới"
              no-results-text="Không tìm thấy tầng này" 
              class="custom-filter-multiselect" 
            /> 
          </div>
        </div>
        
        <div v-if="isTangMoi" class="form-group">
          <label>Tên khu vực mới</label>
          <input
            type="text"
            v-model="newKhuVucName"
            placeholder="Nhập tên khu vực mới"
          />
        </div>

        <div class="form-group" v-if="!isTangMoi">
          <label>Khu vực</label>
          <Multiselect 
            v-model="form.idKhuVuc" 
            :options="khuVucTheoTang.map(kv => ({ value: kv.id, label: kv.tenKhuVuc }))" 
            :searchable="true" 
            :canClear="true" 
            :disabled="!form.tang"
            placeholder="-- Chọn khu vực --"
            no-results-text="Không tìm thấy khu vực nào" 
            class="custom-filter-multiselect" 
          />
        </div>

        <!-- <div class="form-group">
          <label>Tầng</label>
          <input type="number" v-model="form.tang" readonly/>
        </div> -->

        
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="closeAddModal">Hủy</button>
        <button class="btn btn-active" @click="submitAddBan">Lưu</button>
      </div>
    </div>
  </div>
  <!-- POPUP THÊM TẦNG & KHU VỰC -->
<div v-if="showAddTangModal" class="modal-overlay">
  <div class="modal-box">
    <div class="modal-header">
      <h4>Thêm tầng & khu vực</h4>
      <button class="close-btn" @click="closeAddTangModal">
        &times;
      </button>
    </div>

    <div class="modal-body">

      
      <div class="form-group">
        <label>Tầng</label>
        <Multiselect
          v-model="tangForm.tang"
          :options="listTang"
          :searchable="true"
          :create-option="true"
          placeholder="Chọn hoặc nhập tầng mới"
          class="custom-filter-multiselect"
        />
      </div>
      

      <div class="form-group">
        <label>Tên khu vực</label>
        <input type="text" v-model="tangForm.tenKhuVuc" />
      </div>

      <!-- ✅ Ô MÔ TẢ NẰM Ở ĐÂY -->
      <div class="form-group">
        <label>Mô tả</label>
        <textarea
          v-model="tangForm.moTa"
          rows="3"
          placeholder="Nhập mô tả khu vực (không bắt buộc)"
        ></textarea>
      </div>

    </div>

    <div class="modal-footer">
      <button class="btn btn-outline" @click="closeAddTangModal">
        Hủy
      </button>
      <button class="btn btn-active" @click="submitAddTang">
        Lưu
      </button>
    </div>
  </div>
</div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:global(.swal2-container) {
  z-index: 99999 !important;
}

.layout-table {
  display: flex;
  background-color: white;
  height: 100%;
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
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white;
  transition: 0.2s;
}

.btn:hover {
  transform: scale(1.04);
  background-color: white;
  color: white;
  border: 1px solid #7d161a;
}

.btn-outline {
  background-color: white;
  color: white;
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
textarea {
  padding: 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
  resize: none;
}

.filter-item {
    display: flex;
    flex-direction: column;
}


/* Định dạng tổng thể */
.custom-filter-multiselect {
    --ms-border-color: #ddd;
    --ms-radius: 8px; 
    --ms-py: 6px; 
    
    /* Màu khi focus */
    --ms-ring-color: rgba(125, 22, 26, 0.15);
    --ms-border-color-active: #7d161a;
    
    /* Hover vào Option */
    --ms-option-bg-pointed: #fcf4f4;
    --ms-option-color-pointed: #7d161a;
    
    /* Option đã chọn (Màu đỏ giống ảnh 5) */
    --ms-option-bg-selected: #7d161a;
    --ms-option-color-selected: #ffffff;
    --ms-option-bg-selected-pointed: #5f0f12;
    
    /* TAG ĐÃ CHỌN (MÀU XANH LÁ GIỐNG ẢNH 6) */
    --ms-tag-bg: #7d161a; /* Đổi từ #38c172 thành #7d161a */
    --ms-tag-color: #ffffff;
    --ms-tag-radius: 4px;
    --ms-tag-font-weight: 500;
    
    /* Nút Xóa (Clear all) */
    --ms-clear-color: #999;
    --ms-clear-color-hover: #dc3545;
}

/* Sửa text bị lệch */
:deep(.multiselect-single-labelText),
:deep(.multiselect-placeholder) {
    font-size: 14px;
    color: #495057;
}

/* Icon mũi tên thả xuống */
:deep(.multiselect-caret) {
    background-color: #666;
}
:deep(.multiselect.is-active .multiselect-caret) {
    background-color: #7d161a;
}

.btn {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white;
  transition: 0.2s;
  border: none; /* Đảm bảo không có viền đen khi focus */
}

/* Ngăn Bootstrap đổi màu khi click */
.btn:focus,
.btn:active,
.btn:focus-visible {
  color: white !important;
  box-shadow: none !important;
  outline: none !important;
}

.btn:hover {
  transform: scale(1.04);
  color: white;
}

/* Tách riêng btn-outline để không bị đè màu trắng */
.btn-outline {
  background: white !important;
  color: #333 !important;
  border: 1px solid #ccc !important;
}

.btn-outline:hover {
  background: #f1f1f1 !important;
  color: #000 !important;
  transform: scale(1.04);
}
</style>
