<template>
  <div class="pagination-wrapper d-flex justify-content-between align-items-center mt-4 px-2">
    <div class="d-flex align-items-center">
      <span class="me-2 text-muted">Hiển thị</span>
      <select 
        :value="pageSize" 
        @change="updatePageSize($event.target.value)" 
        class="form-select form-select-sm select-per-page"
      >
        <option :value="5">5 dòng</option>
        <option :value="8">8 dòng</option>
        <option :value="10">10 dòng</option>
        <option :value="20">20 dòng</option>
      </select>
    </div>

    <div class="pagination-controls d-flex align-items-center">
      <button class="btn-page" :disabled="currentPage === 1" @click="onGoToPage(1)">
        <i class="fas fa-step-backward"></i>
      </button>
      <button class="btn-page" :disabled="currentPage === 1" @click="onChangePage(-1)">
        <i class="fas fa-chevron-left"></i>
      </button>

      <div class="mx-3 d-flex align-items-center">
        <template v-if="totalPages <= 5">
          <span 
            v-for="p in totalPages" 
            :key="p" 
            class="page-number" 
            :class="{ active: currentPage === p }" 
            @click="onGoToPage(p)"
          >
            {{ p }}
          </span>
        </template>

        <template v-else>
          <div class="input-page-wrapper d-flex align-items-center">
            <input 
              type="number" 
              v-model.number="inputPageLocal" 
              class="input-go-to" 
              @keyup.enter="onJumpToPage"
            >
            <span class="ms-2 text-muted">/ {{ totalPages }}</span>
          </div>
        </template>
      </div>

      <button class="btn-page" :disabled="currentPage === totalPages || totalPages === 0" @click="onChangePage(1)">
        <i class="fas fa-chevron-right"></i>
      </button>
      <button class="btn-page" :disabled="currentPage === totalPages || totalPages === 0" @click="onGoToPage(totalPages)">
        <i class="fas fa-step-forward"></i>
      </button>
    </div>

    <div class="total-info text-muted">
      Hiển thị {{ currentCount }} trong tổng số {{ totalElements }} bản ghi
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';

// 1. Định nghĩa Props (Dữ liệu nhận vào)
const props = defineProps({
  currentPage: { type: Number, required: true, default: 1 },
  pageSize: { type: Number, required: true, default: 8 },
  totalPages: { type: Number, required: true, default: 0 },
  totalElements: { type: Number, required: true, default: 0 },
  currentCount: { type: Number, required: true, default: 0 } // Số lượng item đang hiển thị trên màn hình
});

// 2. Định nghĩa Emits (Sự kiện bắn ra ngoài)
const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change']);

// State nội bộ cho ô input nhập trang
const inputPageLocal = ref(props.currentPage);

// Đồng bộ input khi props currentPage thay đổi từ bên ngoài
watch(() => props.currentPage, (newVal) => {
  inputPageLocal.value = newVal;
});

// --- Các hàm xử lý ---

// Thay đổi số dòng/trang
const updatePageSize = (value) => {
  const size = parseInt(value);
  emit('update:pageSize', size); // Cập nhật v-model pageSize
  // Reset về trang 1 khi đổi size để tránh lỗi trang trắng
  emit('update:currentPage', 1); 
  emit('change'); // Gọi hàm search
};

// Chuyển đến trang cụ thể
const onGoToPage = (page) => {
  if (page !== props.currentPage) {
    emit('update:currentPage', page);
    emit('change');
  }
};

// Nút Prev/Next
const onChangePage = (step) => {
  const newPage = props.currentPage + step;
  if (newPage >= 1 && newPage <= props.totalPages) {
    onGoToPage(newPage);
  }
};

// Nhập số vào ô input và Enter
const onJumpToPage = () => {
  let p = parseInt(inputPageLocal.value);
  if (p >= 1 && p <= props.totalPages) {
    onGoToPage(p);
  } else {
    // Nếu nhập sai, reset lại số cũ
    inputPageLocal.value = props.currentPage;
  }
};
</script>

<style scoped>
/* Khung bao quanh ô nhập số (Màu xám) */
.input-page-wrapper {
  background-color: #f8f9fa;
  padding: 4px 10px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

/* Ô nhập số trang bên trong */
.input-go-to {
  width: 42px;
  border: 1px solid #7B121C; /* Viền đỏ đô */
  border-radius: 4px;
  text-align: center;
  font-weight: bold;
  color: #7B121C;
  outline: none;
}

/* Nút số trang đang được chọn (Màu đỏ đô) */
.page-number {
  margin: 0 3px;
  cursor: pointer;
  padding: 5px;
}

.page-number.active {
  background-color: #7B121C !important;
  color: white !important;
  border-radius: 8px;
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.btn-page {
  background: transparent;
  border: none;
  color: #333;
  padding: 5px 10px;
  cursor: pointer;
}

.btn-page:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.select-per-page {
    width: auto;
    border-color: #dee2e6;
    color: #333;
}
</style>