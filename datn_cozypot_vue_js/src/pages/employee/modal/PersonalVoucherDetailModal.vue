<template>
  <Transition name="fade">
    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
      <Transition name="zoom">
        <div v-if="isOpen" class="modal-container">
          <div class="modal-header">
            <h2>Chi tiết phiếu: {{ data?.maGiamGiaCaNhan }}</h2>
            <button class="close-btn" @click="$emit('close')">&times;</button>
          </div>

          <div class="modal-body" v-if="data">
            <div class="info-grid">
              <div class="form-group">
                <label>Khách hàng nhận</label>
                <div class="input-fake">{{ data.tenKhachHang }}</div>
              </div>
              <div class="form-group">
                <label>Mã phiếu cá nhân</label>
                <input type="text" :value="data.maGiamGiaCaNhan" readonly>
              </div>
            </div>

            <div class="form-group">
              <label>Tên phiếu giảm giá gốc (Áp dụng)</label>
              <input type="text" :value="data.tenPhieuGiamGia" readonly style="font-weight: bold; color: #8B0000;">
            </div>

            <div class="info-grid">
              <div class="form-group">
                <label>Trạng thái sử dụng</label>
                <div class="status-group">
                  <span class="status-item">
                    <i class="fa-solid fa-circle" :class="data.trangThaiSuDung === 0 ? 'dot-active' : 'dot-gray'"></i> 
                    Chưa dùng
                  </span>
                  <span class="status-item">
                    <i class="fa-solid fa-circle" :class="data.trangThaiSuDung === 1 ? 'dot-active' : 'dot-gray'"></i> 
                    Đã dùng
                  </span>
                </div>
              </div>
              <div class="form-group">
                <label>Mã hóa đơn thanh toán</label>
                <input type="text" :value="data.idHoaDonThanhToan ? 'HD-' + data.idHoaDonThanhToan : 'Chưa có'" readonly>
              </div>
            </div>

            <div class="info-grid">
              <div class="form-group">
                <label>Ngày nhận phiếu</label>
                <input type="text" :value="formatDate(data.ngayNhan)" readonly>
              </div>
              <div class="form-group">
                <label>Ngày sử dụng</label>
                <input type="text" :value="formatDate(data.ngaySuDung)" readonly>
              </div>
            </div>

            <div class="form-group">
              <label>Ngày hết hạn</label>
              <input type="text" :value="formatDate(data.ngayHetHan)" readonly class="text-danger">
            </div>

            <div class="form-group">
              <label>Nguồn gốc / Lý do tặng</label>
              <input type="text" :value="data.nguonGoc || 'Nội bộ tặng'" readonly>
            </div>

            <div class="form-group">
              <label>Ghi chú</label>
              <textarea readonly rows="2">{{ data.ghiChu || 'Không có ghi chú' }}</textarea>
            </div>

            <div class="modal-footer-actions" style="margin-top: 20px;">
              <button type="button" class="btn-huy" @click="$emit('close')" style="width: 100%;">Đóng</button>
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
  data: Object // Nhận object PhieuGiamGiaCaNhanDTO
});

defineEmits(['close']);

const formatDate = (dateString) => {
  if (!dateString) return '---';
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
};
</script>

<style scoped>
@import "./personalVoucherModal.css";

/* Bổ sung tùy chỉnh riêng cho Detail */
.text-danger {
  color: #dc3545 !important;
  font-weight: bold;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.input-fake {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background-color: #fcfcfc;
  color: #333;
  font-size: 14px;
}

/* Đồng bộ dot active theo màu đỏ đô của bạn */
.dot-active { color: #8B0000; }
.dot-gray { color: #e0e0e0; }

textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background-color: #fcfcfc;
  resize: none;
}
</style>