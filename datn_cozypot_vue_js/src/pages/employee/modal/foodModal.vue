<script setup>
import { defineProps, defineEmits, ref } from 'vue';
import { useFoodModal } from '../screens/foodFunction';
import FoodDetailAddModal from './FoodAddModals/FoodDetailAddModal.vue';

const props = defineProps({
  isOpen: Boolean,
  foodItem: Object
});

const emit = defineEmits(['close']);

const {
  currentView,
  selectedVariant,
  variants,
  openEditMode,
  backToList,
  fetchVariants
} = useFoodModal(props.foodItem);

const isAddModalOpen = ref(false);
const isSelfVisible = ref(true);

const openAddModal = () => {
  isSelfVisible.value = false;
  isAddModalOpen.value = true;
};

const handleCloseAddModal = () => {
  isAddModalOpen.value = false;
  isSelfVisible.value = true;
};

const handleRefresh = () => {
  fetchVariants();
  handleCloseAddModal();
};

const closeModal = () => {
  backToList();
  emit('close');
};
</script>

<template>
  <div v-if="isOpen && isSelfVisible" class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">

      <div class="modal-header">
        <div class="header-left">
          <button v-if="currentView === 'update'" @click="backToList" class="btn-back">←</button>
          <h2>{{ currentView === 'list' ? 'Chi tiết' : 'Cập nhật loại chi tiết' }}</h2>
        </div>
        <button class="btn-close" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">

        <div v-if="currentView === 'list'">
          <div class="info-grid">
            <div class="info-item"><span>Món ăn</span> <b>{{ foodItem?.tenMonAn || 'N/A' }}</b></div>
            <div class="info-item"><span>Mã món ăn</span> <b>{{ foodItem?.maMonAn || 'N/A' }}</b></div>
            <div class="info-item"><span>Mô tả</span> <span class="text-gray">{{ foodItem?.moTa }}</span></div>
            <div class="info-item"><span>Danh mục</span> <b>{{ foodItem?.tenDanhMuc || 'N/A' }}</b></div>
            <div class="info-item"><span>Chi tiết danh mục</span> <b>{{ foodItem?.chitiet || 'N/A' }}</b></div>
          </div>

          <hr class="divider">

          <div class="variants-grid">
            <div v-for="v in variants" :key="v.id" class="variant-card" @click="openEditMode(v)">
              <div class="v-header">
                <b>{{ v.tenChiTietMonAn }}</b>
                <span class="icon-edit">🏷️</span>
              </div>
              <div class="v-price">{{ v.giaBan }} VNĐ</div>
            </div>

            <div class="variant-card add-card" @click="openAddModal">
              <div class="icon-plus">+</div>
              <div>Thêm loại</div>
            </div>
          </div>
        </div>

        <div v-else class="update-view">
          <div class="info-grid">
            <div class="info-item"><span>Món ăn</span> <b>{{ foodItem?.ten }}</b></div>
            <div class="info-item"><span>Mã món ăn</span> <b>{{ foodItem?.ma }}</b></div>
            <div class="info-item"><span>Chi tiết món</span> <b>{{ selectedVariant?.name }}</b></div>
            <div class="info-item"><span>Danh mục</span> <b>{{ foodItem?.danhmuc }}</b></div>
            <div class="info-item"><span>Chi tiết danh mục</span> <b>{{ foodItem?.chitiet }}</b></div>
            <div class="info-item"><span>Mã chi tiết</span> <b>{{ selectedVariant?.code }}</b></div>
          </div>

          <hr class="divider">
        </div>

        <div class="form-container">
          <div class="form-row">
            <div class="form-group">
              <label>Tên chi tiết</label>
              <input type="text" :placeholder="currentView === 'update' ? selectedVariant.name : 'Placeholder'">
            </div>
            <div class="form-group">
              <label>Kích cỡ</label>
              <input type="text" placeholder="Placeholder">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Mã chi tiết</label>
              <input type="text" :placeholder="currentView === 'update' ? selectedVariant.code : 'Placeholder'">
            </div>
            <div class="form-group">
              <label>Đơn vị</label>
              <input type="text" placeholder="Placeholder">
            </div>
          </div>
          <div class="form-group full-width">
            <label>Giá bán</label>
            <input type="number" :placeholder="currentView === 'update' ? selectedVariant.price : 'Placeholder'">
          </div>
        </div>

      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button class="btn-confirm">Xác nhận thay đổi</button>
      </div>
    </div>
  </div>
  <FoodDetailAddModal v-if="isAddModalOpen" :isOpen="isAddModalOpen" :foodId="foodItem?.id" @close="handleCloseAddModal"
    @refresh="handleRefresh" />
</template>

<style scoped></style>