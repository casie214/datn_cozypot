<template>
  <Transition name="fade">

    <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">

      <Transition name="zoom">

        <div v-if="isOpen" class="modal-container">

          <div class="modal-header">

            <h2>Thông tin chi tiết phiếu giảm giá</h2>

            <button class="close-btn" @click="$emit('close')">&times;</button>

          </div>



          <div class="modal-body" v-if="data">

            <div class="info-grid">

              <div class="form-group">

                <label>Mã phiếu</label>

                <input type="text" :value="data.maPhieuGiamGia" readonly>

              </div>

              <div class="form-group">

                <label>Code giảm giá</label>

                <div class="input-fake voucher-code-text">{{ data.codeGiamGia }}</div>

              </div>

            </div>



            <div class="form-group">

              <label>Tên phiếu giảm giá</label>

              <input type="text" :value="data.tenPhieuGiamGia" readonly>

            </div>



            <div class="form-group">

              <label>Loại giảm giá</label>

              <div class="status-group">

                <span class="status-item">

                  <i class="fa-solid fa-circle" :class="data.loaiGiamGia === 1 ? 'dot-active' : 'dot-gray'"></i> Phần
                  trăm (%)

                </span>

                <span class="status-item">

                  <i class="fa-solid fa-circle" :class="data.loaiGiamGia === 2 ? 'dot-active' : 'dot-gray'"></i> Tiền
                  mặt (VNĐ)

                </span>

              </div>

            </div>



            <div class="info-grid">

              <div class="form-group">

                <label>Giá trị giảm</label>

                <input type="text"
                  :value="data.loaiGiamGia === 1 ? data.giaTriGiam + '%' : formatCurrency(data.giaTriGiam)" readonly>

              </div>

              <div class="form-group">

                <label>Giảm tối đa</label>

                <input type="text" :value="formatCurrency(data.giaTriGiamToiDa)" readonly>

              </div>

            </div>



            <div class="info-grid">

              <div class="form-group">

                <label>Đơn tối thiểu</label>

                <input type="text" :value="formatCurrency(data.donHangToiThieu)" readonly>

              </div>

              <div class="form-group">

                <label>Số lượng (Còn lại / Tổng)</label>

                <input type="text" :value="data.soLuongConLai + ' / ' + data.soLuongPhatHanh" readonly>

              </div>

            </div>



            <div class="form-group">

              <label>Thời gian áp dụng</label>

              <div class="input-fake">

                <i class="fa-regular fa-calendar-days"></i> {{ data.ngayBatDau }} &rarr; {{ data.ngayKetThuc }}

              </div>

            </div>



            <div class="form-group">

              <label>Trạng thái phiếu</label>

              <div class="status-group">

                <span class="status-item">

                  <i class="fa-solid fa-circle" :class="data.trangThai === 1 ? 'dot-active' : 'dot-gray'"></i> Đang hoạt
                  động

                </span>

                <span class="status-item">

                  <i class="fa-solid fa-circle" :class="data.trangThai === 0 ? 'dot-active' : 'dot-gray'"></i> Ngưng
                  hoạt động

                </span>

              </div>

            </div>



            <div class="system-info-box">

              <div class="info-grid">

                <div class="form-group">

                  <label>Ngày tạo</label>

                  <input type="text" :value="data.ngayTao || '---'" readonly class="system-input">

                </div>

                <div class="form-group">

                  <label>Người tạo</label>

                  <input type="text" :value="data.nguoiTao || 'Admin'" readonly class="system-input">

                </div>

                <div class="form-group">

                  <label>Ngày sửa gần nhất</label>

                  <input type="text" :value="data.ngaySua || '---'" readonly class="system-input">

                </div>

                <div class="form-group">

                  <label>Người sửa</label>

                  <input type="text" :value="data.nguoiSua || '---'" readonly class="system-input">

                </div>

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



const formatCurrency = (val) => {

  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val || 0);

};

</script>



<style scoped>
.modal-overlay {

  position: fixed;

  top: 0;
  left: 0;
  width: 100%;
  height: 100%;

  background: rgba(0, 0, 0, 0.4);

  display: flex;
  justify-content: center;
  align-items: center;

  z-index: 9999;

}

.modal-container {

  background: white;
  width: 600px;
  max-height: 90vh;

  border-radius: 8px;
  overflow-y: auto;

  box-shadow: 0 5px 25px rgba(0, 0, 0, 0.2);

}

.modal-header {

  display: flex;
  justify-content: space-between;
  align-items: center;

  padding: 15px 20px;
  border-bottom: 1px solid #eee;

  position: sticky;
  top: 0;
  background: white;
  z-index: 10;

}

.modal-header h2 {
  font-size: 18px;
  margin: 0;
  color: #8B0000;
  font-weight: bold;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
}

.modal-body {
  padding: 20px;
}



.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-size: 12px;
  color: #777;
  font-weight: bold;
  text-transform: uppercase;
}

.form-group input,
.input-fake {

  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 6px;

  background-color: #fcfcfc;
  color: #333;
  font-size: 14px;

}

.input-fake {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 38px;
}



.voucher-code-text {

  font-family: monospace;
  font-weight: bold;
  color: #8B0000;
  letter-spacing: 1px;

}



.status-group {
  display: flex;
  gap: 25px;
  margin-top: 5px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.dot-active {
  color: #8B0000;
  font-size: 12px;
}

.dot-gray {
  color: #e0e0e0;
  font-size: 12px;
}



.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}



/* System Info Section */

.system-info-box {

  margin-top: 20px;

  padding-top: 15px;

  border-top: 2px dashed #f0f0f0;

}

.system-title {

  font-size: 14px;

  color: #8B0000;

  margin-bottom: 15px;

  font-weight: bold;

  display: flex;

  align-items: center;

}

.system-title::before {

  content: "";

  display: inline-block;

  width: 4px;

  height: 15px;

  background: #8B0000;

  margin-right: 8px;

  border-radius: 2px;

}

.system-input {

  background-color: #f9f9f9 !important;

  border-color: #eee !important;

  color: #777 !important;

  font-size: 13px !important;

}



/* Transitions */

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.zoom-enter-active,
.zoom-leave-active {
  transition: transform 0.3s ease;
}

.zoom-enter-from,
.zoom-leave-to {
  transform: scale(0.95);
}
</style>