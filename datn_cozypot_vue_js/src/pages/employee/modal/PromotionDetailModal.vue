<template>
  <Transition name="fade">
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
      <Transition name="zoom">
        <div v-if="isOpen" class="modal-container">
          <div class="modal-header">
            <h2>Thông tin đợt khuyến mãi</h2>
            <button class="close-btn" @click="$emit('close')">&times;</button>
          </div>

          <div class="modal-body" v-if="data">
            <div class="form-group">
              <label>Mã đợt khuyến mãi</label>
              <input type="text" :value="data.maDotKhuyenMai" readonly>
            </div>

            <div class="form-group">
              <label>Tên đợt khuyến mãi</label>
              <input type="text" :value="data.tenDotKhuyenMai" readonly>
            </div>

            <div class="form-group">
              <label>Mô tả</label>
              <textarea readonly rows="2">{{ data.moTa }}</textarea>
            </div>

            <div class="form-group">
              <label>Loại khuyến mãi</label>
              <div class="status-group">
                <span class="status-item">
                  <i class="fa-solid fa-circle" :class="data.loaiKhuyenMai === 1 ? 'dot-active' : 'dot-gray'"></i> Phần trăm
                </span>
                <span class="status-item">
                  <i class="fa-solid fa-circle" :class="data.loaiKhuyenMai === 2 ? 'dot-active' : 'dot-gray'"></i> Tiền mặt
                </span>
              </div>
            </div>

            <div class="form-group">
              <label>Đối tượng áp dụng</label>
              <div class="input-fake">{{ data.doiTuongApDung || 'Tất cả khách hàng' }}</div>
            </div>

            <div class="form-group">
              <label>Khung giờ áp dụng</label>
              <input type="text" :value="data.khungGioApDung || 'Cả ngày (24/24)'" readonly>
            </div>

            <div class="form-group">
              <label>Danh sách món ăn</label>
              <input type="text" :value="data.danhSachMonApDung || 'Tất cả'" readonly>
            </div>

            <div class="form-group">
              <label>Trạng thái</label>
              <div class="status-group">
                <span class="status-item">
                  <i class="fa-solid fa-circle" :class="data.trangThai === 1 ? 'dot-active' : 'dot-gray'"></i> Hoạt động
                </span>
                <span class="status-item">
                  <i class="fa-solid fa-circle" :class="data.trangThai === 0 ? 'dot-active' : 'dot-gray'"></i> Ngưng hoạt động
                </span>
              </div>
            </div>

            <div class="info-grid">
              <div class="form-group">
                <label>Ngày tạo</label>
                <input type="text" :value="data.ngayTao || '---'" readonly>
              </div>
              <div class="form-group">
                <label>Ngày sửa</label>
                <input type="text" :value="data.ngaySua || '---'" readonly>
              </div>
              <div class="form-group">
                <label>Người tạo</label>
                <input type="text" value="Admin" readonly>
              </div>
              <div class="form-group">
                <label>Người sửa</label>
                <input type="text" value="Admin" readonly>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  isOpen: Boolean,
  data: Object
});
defineEmits(['close']);
</script>

<style scoped>
/* GIỮ NGUYÊN CSS CỦA BẠN */
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}
.modal-container {
  background: white; width: 550px; max-height: 95vh;
  border-radius: 8px; overflow-y: auto;
  box-shadow: 0 5px 25px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 15px 20px; border-bottom: 1px solid #eee;
}
.modal-header h2 { font-size: 18px; margin: 0; color: #8B0000; font-weight: bold; }
.close-btn { background: none; border: none; font-size: 24px; cursor: pointer; color: #999; }
.modal-body { padding: 20px; }
.form-group { margin-bottom: 15px; }
.form-group label { display: block; margin-bottom: 5px; font-size: 13px; color: #666; font-weight: bold; }
.form-group input, .input-fake, .form-group textarea {
  width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px;
  background-color: #fcfcfc; color: #333; font-size: 14px;
}
.status-group { display: flex; gap: 25px; margin-top: 5px; }
.status-item { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.dot-active { color: #8B0000; font-size: 14px; }
.dot-gray { color: #e0e0e0; font-size: 14px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 10px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.zoom-enter-active, .zoom-leave-active { transition: transform 0.3s ease; }
.zoom-enter-from, .zoom-leave-to { transform: scale(0.95); }
</style>