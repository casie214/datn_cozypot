<template>
  <div class="modal-overlay" @click.self="handleCancel">
    <div class="modal-box shadow">
      <div class="modal-header-custom">
        <h5 class="m-0 title-modal">
          {{ staffId ? 'Cập nhật nhân viên' : 'Thêm nhân viên mới' }}
        </h5>
        <span class="close-x" @click="handleCancel">X</span>
      </div>

      <div class="modal-body-custom">
        <form class="container-fluid" @submit.prevent>
          <div class="row mb-3">
            <div class="col-md-12">
              <label class="form-label-custom">Vai trò <span class="text-danger">*</span></label>
              <select class="form-select custom-select" v-model="formData.idVaiTro">
                <option value="" disabled>-- Chọn vai trò --</option>
                <option v-for="role in listRoles" :key="role.id" :value="role.id">{{ role.tenVaiTro }}</option>
              </select>
              <small class="text-danger" v-if="errors.idVaiTro">{{ errors.idVaiTro }}</small>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-12">
               <label class="form-label-custom">Họ và tên <span class="text-danger">*</span></label>
               <input type="text" class="form-control" v-model="formData.hoTenNhanVien" placeholder="Nhập họ và tên...">
               <small class="text-danger" v-if="errors.hoTenNhanVien">{{ errors.hoTenNhanVien }}</small>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Ngày sinh <span class="text-danger">*</span></label>
              <input type="date" class="form-control" v-model="formData.ngaySinh">
              <small class="text-danger" v-if="errors.ngaySinh">{{ errors.ngaySinh }}</small>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Ngày vào làm <span class="text-danger">*</span></label>
              <input type="date" class="form-control" v-model="formData.ngayVaoLam">
              <small class="text-danger" v-if="errors.ngayVaoLam">{{ errors.ngayVaoLam }}</small>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Tên đăng nhập <span class="text-danger">*</span></label>
              <input type="text" class="form-control" :class="{'input-readonly': staffId}" 
                     v-model="formData.tenDangNhap" :readonly="!!staffId" placeholder="Nhập tên đăng nhập...">
              <small class="text-danger" v-if="errors.tenDangNhap">{{ errors.tenDangNhap }}</small>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Mật khẩu <span class="text-danger">*</span></label>
              <input v-if="!staffId" type="password" class="form-control" v-model="formData.matKhauDangNhap" placeholder="Nhập mật khẩu...">
              <input v-else type="text" class="form-control input-readonly" value="********" readonly disabled>
              <small class="text-danger" v-if="errors.matKhauDangNhap">{{ errors.matKhauDangNhap }}</small>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Số điện thoại <span class="text-danger">*</span></label>
              <input type="text" class="form-control" v-model="formData.sdtNhanVien" placeholder="09xxxxxxxx">
              <small class="text-danger" v-if="errors.sdtNhanVien">{{ errors.sdtNhanVien }}</small>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Email <span class="text-danger">*</span></label>
              <input type="email" class="form-control" v-model="formData.email" placeholder="email@example.com">
              <small class="text-danger" v-if="errors.email">{{ errors.email }}</small>
            </div>
          </div>

          <div class="row mb-3 align-items-center">
            <div class="col-md-12">
              <label class="form-label-custom">Giới tính <span class="text-danger">*</span></label>
              <div class="d-flex gap-4 mt-1">
                <div class="form-check d-flex align-items-center gap-2">
                  <input class="form-check-input radio-red" type="radio" v-model="formData.gioiTinh" :value="true" id="male">
                  <label class="form-check-label mb-0" for="male">Nam</label>
                </div>
                <div class="form-check d-flex align-items-center gap-2">
                  <input class="form-check-input radio-red" type="radio" v-model="formData.gioiTinh" :value="false" id="female">
                  <label class="form-check-label mb-0" for="female">Nữ</label>
                </div>
              </div>
              <small class="text-danger" v-if="errors.gioiTinh">{{ errors.gioiTinh }}</small>
            </div>
          </div>

          <div class="row">
            <div class="col-12">
              <label class="form-label-custom">Địa chỉ <span class="text-danger">*</span></label>
              <input type="text" class="form-control" v-model="formData.diaChi" placeholder="Nhập địa chỉ cụ thể...">
              <small class="text-danger" v-if="errors.diaChi">{{ errors.diaChi }}</small>
            </div>
          </div>
        </form>
      </div>

      <div class="modal-footer-custom">
        <button class="btn-cancel" @click="handleCancel" :disabled="loading">Hủy</button>
        <button class="btn-submit" @click="handleConfirmSave" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
          {{ staffId ? 'Cập nhật' : 'Thêm mới' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue';
import staffService from '@/services/staffService';
import dayjs from 'dayjs';

const props = defineProps({
  staffId: { type: [Number, String], default: null }
});

const emit = defineEmits(['close', 'refresh']);
const loading = ref(false);
const listRoles = ref([]);

const formData = reactive({
  idVaiTro: '', 
  hoTenNhanVien: '',
  tenDangNhap: '', 
  matKhauDangNhap: '', 
  sdtNhanVien: '',
  email: '',
  ngaySinh: '',
  ngayVaoLam: '',
  gioiTinh: true,
  diaChi: '',
  trangThaiLamViec: 1
});

const errors = reactive({});

const validateAll = () => {
  let isValid = true;
  Object.keys(formData).forEach(key => errors[key] = '');

  if (!formData.idVaiTro) { errors.idVaiTro = 'Vui lòng chọn vai trò'; isValid = false; }
  if (!formData.hoTenNhanVien?.trim()) { errors.hoTenNhanVien = 'Họ tên không được để trống'; isValid = false; }
  if (!formData.tenDangNhap?.trim()) { errors.tenDangNhap = 'Tên đăng nhập không được để trống'; isValid = false; }
  
  if (!props.staffId) {
    if (!formData.matKhauDangNhap) { errors.matKhauDangNhap = 'Mật khẩu không được để trống'; isValid = false; }
    else if (formData.matKhauDangNhap.length < 6) { errors.matKhauDangNhap = 'Mật khẩu tối thiểu 6 ký tự'; isValid = false; }
  }

  // LOGIC KIỂM TRA ĐỦ 18 TUỔI
  if (!formData.ngaySinh) { 
    errors.ngaySinh = 'Vui lòng chọn ngày sinh'; 
    isValid = false; 
  } else {
    const age = dayjs().diff(dayjs(formData.ngaySinh), 'year');
    if (age < 18) {
      errors.ngaySinh = 'Nhân viên phải từ đủ 18 tuổi trở lên';
      isValid = false;
    }
  }

  if (!formData.ngayVaoLam) { errors.ngayVaoLam = 'Vui lòng chọn ngày vào làm'; isValid = false; }
  
  const phoneRegex = /^(0[3|5|7|8|9])([0-9]{8})$/;
  if (!formData.sdtNhanVien) { errors.sdtNhanVien = 'SĐT không được để trống'; isValid = false; }
  else if (!phoneRegex.test(formData.sdtNhanVien)) { errors.sdtNhanVien = 'SĐT không đúng định dạng'; isValid = false; }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!formData.email) { errors.email = 'Email không được để trống'; isValid = false; }
  else if (!emailRegex.test(formData.email)) { errors.email = 'Email không hợp lệ'; isValid = false; }

  if (formData.gioiTinh === null) { errors.gioiTinh = 'Vui lòng chọn giới tính'; isValid = false; }
  if (!formData.diaChi?.trim()) { errors.diaChi = 'Địa chỉ không được để trống'; isValid = false; }

  return isValid;
};

const handleConfirmSave = () => {
  if (validateAll()) {
    if (confirm("Bạn có chắc chắn muốn thực hiện thao tác này?")) {
      saveData();
    }
  }
};

const saveData = async () => {
  try {
    loading.value = true;
    const payload = { ...formData };
    if (props.staffId) {
      delete payload.matKhauDangNhap;
      await staffService.update(props.staffId, payload);
      alert("Cập nhật thành công!");
    } else {
      await staffService.create(payload);
      alert("Thêm mới thành công!");
    }
    emit('refresh');
    emit('close');
  } catch (error) {
    alert("Có lỗi xảy ra: " + (error.response?.data?.message || "Lỗi kết nối"));
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  if (confirm("Dữ liệu sẽ không được lưu. Bạn có muốn thoát?")) {
    emit('close');
  }
};

onMounted(async () => {
  const roleRes = await staffService.getActiveRoles();
  listRoles.value = roleRes.data;
  if (props.staffId) {
    const res = await staffService.getDetail(props.staffId);
    Object.assign(formData, res.data);
    formData.ngaySinh = res.data.ngaySinh ? dayjs(res.data.ngaySinh).format('YYYY-MM-DD') : '';
    formData.ngayVaoLam = res.data.ngayVaoLam ? dayjs(res.data.ngayVaoLam).format('YYYY-MM-DD') : '';
  }
});
</script>

<style scoped>
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 999; }
.modal-box { background: #fff; width: 700px; border-radius: 8px; overflow: hidden; }
.modal-header-custom { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.title-modal { color: #7B121C; font-weight: bold; }
.close-x { cursor: pointer; font-weight: bold; color: #aaa; }
.modal-body-custom { padding: 20px; max-height: 80vh; overflow-y: auto; }
.form-label-custom {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 5px;
  
  /* Các dòng quan trọng để không xuống hàng */
  display: flex !important;      /* Sử dụng flex để các thành phần con nằm ngang */
  align-items: center;           /* Căn giữa theo chiều dọc */
  white-space: nowrap;           /* Ngăn văn bản tự động xuống dòng */
  gap: 4px;                      /* Tạo khoảng cách nhỏ giữa chữ và dấu * */
}.form-control, .form-select { border-radius: 5px; font-size: 14px; }
.radio-red:checked { background-color: #7B121C; border-color: #7B121C; }
.modal-footer-custom { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #e9ecef; border: none; padding: 8px 25px; border-radius: 5px; }
.btn-submit { background: #7B121C; color: #fff; border: none; padding: 8px 25px; border-radius: 5px; }
.text-danger { font-size: 12px; margin-top: 4px; display: block; font-style: italic; }
.input-readonly { background: #f8f9fa !important; }
</style>