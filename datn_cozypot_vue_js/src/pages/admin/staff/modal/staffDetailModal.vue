<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-box shadow-lg">
      
      <div class="detail-header">
        <h5 class="m-0 title-modal text-uppercase">Chi tiết nhân viên</h5>
        <span class="close-x" @click="$emit('close')">X</span>
      </div>

      <div class="modal-body-custom">
        <form class="container-fluid">
          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">ID nhân viên</label>
              <input type="text" class="form-control view-only" :value="formData.id" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Vai trò</label>
              <input type="text" class="form-control view-only" 
                     :value="formData.tenVaiTro || (formData.idVaiTro === 1 ? 'Quản lý' : 'Nhân viên')" readonly>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Mã nhân viên</label>
              <input type="text" class="form-control view-only" :value="formData.maNhanVien" readonly>
            </div>
            <div class="col-md-6">
               <label class="form-label-custom">Họ và tên</label>
               <input type="text" class="form-control view-only" :value="formData.hoTenNhanVien" readonly>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Tên đăng nhập</label>
              <input type="text" class="form-control view-only" :value="formData.tenDangNhap" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Mật khẩu</label>
              <input type="text" class="form-control view-only" value="********" readonly>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Số điện thoại</label>
              <input type="text" class="form-control view-only" :value="formData.sdtNhanVien" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Email</label>
              <input type="text" class="form-control view-only" :value="formData.email" readonly>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Ngày sinh</label>
              <input type="text" class="form-control view-only" :value="formData.ngaySinh" readonly>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Ngày vào làm</label>
              <input type="text" class="form-control view-only" :value="formData.ngayVaoLam" readonly>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Giới tính</label>
              <div class="d-flex gap-4 mt-1">
                <div class="form-check">
                  <input class="form-check-input" type="radio" :checked="formData.gioiTinh === true" disabled>
                  <label class="form-check-label">Nam</label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" :checked="formData.gioiTinh === false" disabled>
                  <label class="form-check-label">Nữ</label>
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Trạng thái</label>
              <select class="form-select view-only" v-model="formData.trangThaiLamViec" disabled>
                <option :value="1">Đang làm việc</option>
                <option :value="2">Nghỉ phép</option>
                <option :value="3">Nghỉ việc</option>
              </select>
            </div>
          </div>

          <div class="row">
            <div class="col-12">
              <label class="form-label-custom">Địa chỉ</label>
              <input type="text" class="form-control view-only" :value="formData.diaChi" readonly>
            </div>
          </div>
        </form>
      </div>

      <div class="detail-footer">
        <button class="btn-secondary-custom me-2" @click="$emit('close')">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue';
import staffService from '@/services/staffService'; // Đường dẫn import @ từ src
import dayjs from 'dayjs';

const props = defineProps({
  staffId: { type: [Number, String], required: true }
});

const emit = defineEmits(['close', 'edit']);
const formData = reactive({});

const fetchStaffDetail = async () => {
  try {
    const response = await staffService.getDetail(props.staffId);
    const data = response.data;
    Object.assign(formData, data);
    
    // Định dạng lại ngày để hiển thị
    formData.ngaySinh = data.ngaySinh ? dayjs(data.ngaySinh).format('DD/MM/YYYY') : '';
    formData.ngayVaoLam = data.ngayVaoLam ? dayjs(data.ngayVaoLam).format('DD/MM/YYYY') : '';
  } catch (error) {
    console.error("Lỗi tải dữ liệu:", error);
    alert("Không thể tải thông tin chi tiết!");
  }
};

onMounted(fetchStaffDetail);
</script>

<style scoped>
/* CSS căn giữa Modal tuyệt đối */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5); /* Nền mờ */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.modal-box {
  background: white;
  width: 800px;
  max-width: 95%;
  max-height: 90vh;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.detail-header {
  padding: 15px 25px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fcfcfc;
}

.title-modal {
  color: #7B121C;
  font-weight: bold;
  font-size: 1.1rem;
}

.close-x {
  cursor: pointer;
  font-size: 1.2rem;
  color: #999;
  font-weight: bold;
}

.modal-body-custom {
  padding: 25px;
  overflow-y: auto;
}

.form-label-custom {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 5px;
  color: #666;
}

.view-only {
  background-color: #f8f9fa !important;
  border: 1px solid #e9ecef !important;
  color: #333 !important;
  cursor: default;
  font-size: 14px;
}

.detail-footer {
  padding: 15px 25px;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: flex-end;
  background: #fcfcfc;
}

.btn-secondary-custom {
  background: #6c757d;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
}

.btn-edit-link {
  background: #7B121C;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
  font-weight: 600;
}

/* Ẩn thanh cuộn nhưng vẫn cuộn được */
.modal-body-custom::-webkit-scrollbar { display: none; }
.modal-body-custom { -ms-overflow-style: none; scrollbar-width: none; }
</style>