<script setup>
import { defineProps, defineEmits, ref } from 'vue';
import { useFoodModal } from '../../../../../services/foodFunction';
import FoodDetailAddModal from '../addModal/FoodDetailAddModal.vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  isOpen: Boolean,
  foodItem: Object
});

const emit = defineEmits(['close', 'refresh']);

const {
  currentView,
  selectedVariant,
  variants,
  openEditMode,
  parentFormData,
  filteredSubCategories,
  backToList,
  fetchVariants,
  handleSave,
  listDanhMuc
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

const router = useRouter();

const goToAddScreen = () => router.push({ name: 'addFoodDetail' });

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
            <div class="info-item"><span>Chi tiết danh mục</span> <b>{{ foodItem?.tenDanhMucChiTiet || 'N/A' }}</b>
            </div>
          </div>

          <hr class="divider">

          <div class="variants-grid">
            <div v-for="v in variants" :key="v.id" class="variant-card">
              <div class="v-header">
                <b>{{ v.tenChiTietMonAn }}</b>
                <span class="icon-edit">🏷️</span>
              </div>
              <div class="v-price">{{ v.giaBan }} VNĐ</div>
            </div>

            <div class="variant-card add-card" @click="goToAddScreen">
              <div class="icon-plus">+</div>
              <div>Thêm loại</div>
            </div>
          </div>
        </div>

        <div v-else class="update-view">
          <div v-if="selectedVariant" class="form-container">

            <div class="form-row">
              <div class="form-group">
                <label>Tên chi tiết <span class="required">*</span></label>
                <input v-model="selectedVariant.tenChiTietMonAn" type="text" placeholder="VD: Size L">
              </div>
              <div class="form-group">
                <label>Mã chi tiết</label>
                <input v-model="selectedVariant.maChiTietMonAn" type="text" disabled style="background: #e9ecef">
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Thuộc món ăn</label>
                <input :value="foodItem?.tenMonAn" type="text" disabled style="background: #e9ecef">
              </div>
              <div class="form-group">
                <label>Danh mục</label>
                <input :value="foodItem?.tenDanhMuc" type="text" disabled style="background: #e9ecef">
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Giá bán <span class="required">*</span></label>
                <input v-model.number="selectedVariant.giaBan" type="number">
              </div>
              <div class="form-group">
                <label>Giá vốn</label>
                <input v-model.number="selectedVariant.giaVon" type="number">
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>Kích cỡ</label>
                <input v-model="selectedVariant.kichCo" type="text" placeholder="L, M, S">
              </div>
              <div class="form-group">
                <label>Đơn vị tính</label>
                <input v-model="selectedVariant.donVi" type="text" placeholder="Cái, Đĩa">
              </div>
            </div>

            <div class="form-group full-width">
              <label>Trạng thái kinh doanh</label>
              <div class="toggle-wrapper">
                <span>{{ selectedVariant.trangThai === 1 ? 'Đang hoạt động' : 'Ngưng hoạt động' }}</span>
                <div class="toggle-switch" :class="{ 'on': selectedVariant.trangThai === 1 }"
                  @click="selectedVariant.trangThai = (selectedVariant.trangThai === 1 ? 0 : 1)">
                  <div class="toggle-knob"></div>
                </div>
              </div>
            </div>

          </div>
        </div>

        <div class="form-container">

          <div class="form-group full-width">
            <label>Tên món ăn <span class="required">*</span></label>
            <input type="text" v-model="parentFormData.tenMonAn" placeholder="Nhập tên món ăn">
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>Danh mục gốc</label>
              <select v-model="parentFormData.idDanhMuc" class="form-control">
                <option value="">-- Chọn danh mục --</option>
                <option v-for="dm in listDanhMuc" :key="dm.id" :value="dm.id">
                  {{ dm.tenDanhMuc }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Danh mục chi tiết <span class="required">*</span></label>
              <select v-model="parentFormData.idDanhMucChiTiet" :disabled="!parentFormData.idDanhMuc"
                class="form-control">
                <option value="">-- Chọn chi tiết --</option>
                <option v-for="sub in filteredSubCategories" :key="sub.id" :value="sub.id">
                  {{ sub.tenDanhMucChiTiet }}
                </option>
              </select>
            </div>
          </div>

          <div class="form-group full-width">
            <label>Giá bán <span class="required">*</span></label>
            <input type="number" v-model.number="parentFormData.giaBan" placeholder="0">
          </div>

          <div class="form-group full-width">
            <label>Mô tả</label>
            <textarea v-model="parentFormData.moTa" class="form-control" rows="3"
              placeholder="Mô tả món ăn..."></textarea>
          </div>

          <div class="form-group full-width">
            <label>Trạng thái kinh doanh</label>
            <div class="toggle-wrapper">
              <span :class="{ 'text-active': parentFormData.trangThaiKinhDoanh === 1 }">
                {{ parentFormData.trangThaiKinhDoanh === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}
              </span>

              <div class="toggle-switch" :class="{ 'on': parentFormData.trangThaiKinhDoanh === 1 }"
                @click="parentFormData.trangThaiKinhDoanh = (parentFormData.trangThaiKinhDoanh === 1 ? 0 : 1)">
                <div class="toggle-knob"></div>
              </div>
            </div>
          </div>

        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button class="btn-confirm" @click="handleSave(emit)">Xác nhận thay đổi</button>
      </div>
    </div>
  </div>
  <FoodDetailAddModal v-if="isAddModalOpen" :isOpen="isAddModalOpen" :foodItem="foodItem" @close="handleCloseAddModal"
    @refresh="handleRefresh" />
</template>

<style scoped src="/src/assets/foodModalManager.css"></style>