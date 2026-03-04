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
import Multiselect from "vue-multiselect";
import "vue-multiselect/dist/vue-multiselect.css";
import { usePermission } from "@/components/permissionHelper";

const { handleActionWithAuth } = usePermission();
/* 1. KHỞI TẠO TRẠNG THÁI */
const danhSachBan = ref([]);
const listKhuVuc = ref([]);

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
    if (!tangForm.value.tang || tangForm.value.tang <= 0) {
      alert("Tầng phải > 0");
      return;
    }

    if (!tangForm.value.tenKhuVuc) {
      alert("Nhập tên khu vực");
      return;
    }

    await createKhuVuc({
      tang: Number(tangForm.value.tang),
      tenKhuVuc: tangForm.value.tenKhuVuc.trim(),
      moTa: tangForm.value.moTa,
    });

    alert("Thêm thành công");

    await handleFetchAllKhuVuc();

    tangForm.value = {
      tang: "",
      tenKhuVuc: "",
      moTa: "",
    };

    closeAddTangModal();
  } catch (error) {
  console.log("FULL ERROR:", error);
  console.log("RESPONSE:", error.response);
  console.log("DATA:", error.response?.data);
  alert(error.response?.data || error.message || "Có lỗi");
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
      alert("Nhập số người tối đa");
      return;
    }

    if (!form.value.tang) {
      alert("Nhập tầng");
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
        alert("Nhập tên khu vực");
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

    alert("Thêm thành công!");
  } catch (error) {
    console.error("Lỗi thêm bàn:", error);
    alert("Có lỗi xảy ra!");
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
          <Multiselect
            v-model="selectedTang"
            :options="listTang"
            :taggable="true"
            placeholder="Chọn hoặc nhập tầng mới"
            @tag="(newTag) => (selectedTang = Number(newTag))"
          />
        </div>
        <div v-if="isTangMoi" class="form-group">
          <label>Tên khu vực mới</label>
          <input
            type="text"
            v-model="newKhuVucName"
            placeholder="Nhập tên khu vực mới"
          />
        </div>

        <div class="form-group">
          <label>Khu vực</label>
          <select
            class="form-select"
            v-model="form.idKhuVuc"
            :disabled="!form.tang || isTangMoi"
          >
            <option value="" disabled>-- Chọn khu vực --</option>
            <option
              v-for="khuVuc in khuVucTheoTang"
              :key="khuVuc.id"
              :value="khuVuc.id"
            >
              {{ khuVuc.tenKhuVuc }}
            </option>
          </select>
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
  :taggable="true"
  placeholder="Chọn hoặc nhập tầng mới"
  @tag="(newTag) => (tangForm.tang = Number(newTag))"
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
textarea {
  padding: 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
  resize: none;
}
</style>
