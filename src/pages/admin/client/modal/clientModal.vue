<template>
  <div class="modal-overlay" @click.self="handleCancel">
    <div class="modal-box shadow">
      <div class="modal-header-custom">
        <h5 class="m-0 title-modal">
          {{ clientId ? 'Cập nhật thông tin khách hàng' : 'Thêm khách hàng mới' }}
        </h5>
        <span class="close-x" @click="handleCancel">X</span>
      </div>

      <div class="modal-body-custom">
        <form class="container-fluid" @submit.prevent>
          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Họ và tên <span class="text-danger">*</span></label>
              <input type="text" class="form-control" v-model="formData.tenKhachHang" placeholder="Nhập họ và tên...">
              <small class="text-danger" v-if="errors.tenKhachHang">{{ errors.tenKhachHang }}</small>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Số điện thoại <span class="text-danger">*</span></label>
              <input type="text" class="form-control" v-model="formData.soDienThoai" placeholder="09xxxxxxxx">
              <small class="text-danger" v-if="errors.soDienThoai">{{ errors.soDienThoai }}</small>
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Email <span class="text-danger">*</span></label>
              <input type="email" class="form-control" v-model="formData.email" placeholder="email@example.com">
              <small class="text-danger" v-if="errors.email">{{ errors.email }}</small>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Ngày sinh</label>
              <input type="date" class="form-control" v-model="formData.ngaySinh">
            </div>
          </div>

          <div class="row mb-3">
            <div class="col-md-6">
              <label class="form-label-custom">Tên đăng nhập <span class="text-danger">*</span></label>
              <input type="text" class="form-control" :class="{'input-readonly': clientId}" 
                     v-model="formData.tenDangNhap" :readonly="!!clientId" placeholder="Nhập tên đăng nhập...">
              <small class="text-danger" v-if="errors.tenDangNhap">{{ errors.tenDangNhap }}</small>
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Mật khẩu <span v-if="!clientId" class="text-danger">*</span></label>
              <input v-if="!clientId" type="password" class="form-control" v-model="formData.matKhauDangNhap" placeholder="Nhập mật khẩu...">
              <input v-else type="text" class="form-control input-readonly" value="********" readonly disabled>
              <small class="text-danger" v-if="errors.matKhauDangNhap">{{ errors.matKhauDangNhap }}</small>
            </div>
          </div>

          <div class="row mb-3 align-items-center">
            <div class="col-md-6">
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
            </div>
            <div class="col-md-6">
              <label class="form-label-custom">Trạng thái <span class="text-danger">*</span></label>
              <select class="form-select" v-model="formData.trangThai" style="pointer-events: none; background-color: #f2f2f2; color: #999; border-color: #ddd;">
                <option :value="1">Đang hoạt động</option>
                <option :value="0">Ngừng hoạt động</option>
              </select>
            </div>
          </div>

          <div class="row">
            <div class="col-12">
              <label class="form-label-custom">Địa chỉ</label>
              <input type="text" class="form-control" v-model="formData.diaChi" placeholder="Số nhà, tên đường, phường/xã...">
            </div>
          </div>
        </form>
      </div>

      <div class="modal-footer-custom">
        <button class="btn-cancel" @click="handleCancel" :disabled="loading">Hủy</button>
        <button class="btn-submit" @click="handleConfirmSave" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-1"></span>
          {{ clientId ? 'Cập nhật' : 'Thêm mới' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue';
import clientService from '@/services/clientService';
import dayjs from 'dayjs';

const props = defineProps({
  clientId: { type: [Number, String], default: null }
});

const emit = defineEmits(['close', 'refresh']);
const loading = ref(false);

const formData = reactive({
  tenKhachHang: '',
  soDienThoai: '',
  email: '',
  tenDangNhap: '',
  matKhauDangNhap: '',
  ngaySinh: '',
  gioiTinh: true,
  diaChi: '',
  trangThai: 1
});

const errors = reactive({});

const validateAll = () => {
  let isValid = true;
  Object.keys(errors).forEach(key => errors[key] = '');

  if (!formData.tenKhachHang?.trim()) { errors.tenKhachHang = 'Họ tên không được để trống'; isValid = false; }
  if (!formData.tenDangNhap?.trim()) { errors.tenDangNhap = 'Tên đăng nhập không được để trống'; isValid = false; }
  
  if (!props.clientId) {
    if (!formData.matKhauDangNhap) { errors.matKhauDangNhap = 'Mật khẩu không được để trống'; isValid = false; }
    else if (formData.matKhauDangNhap.length < 6) { errors.matKhauDangNhap = 'Mật khẩu tối thiểu 6 ký tự'; isValid = false; }
  }

  const phoneRegex = /^(0[3|5|7|8|9])([0-9]{8})$/;
  if (!formData.soDienThoai) { errors.soDienThoai = 'SĐT không được để trống'; isValid = false; }
  else if (!phoneRegex.test(formData.soDienThoai)) { errors.soDienThoai = 'SĐT không đúng định dạng'; isValid = false; }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!formData.email) { errors.email = 'Email không được để trống'; isValid = false; }
  else if (!emailRegex.test(formData.email)) { errors.email = 'Email không hợp lệ'; isValid = false; }

  return isValid;
};

const handleConfirmSave = () => {
  if (validateAll()) {
    if (confirm("Xác nhận lưu thông tin khách hàng này?")) {
      saveData();
    }
  }
};

const saveData = async () => {
  try {
    loading.value = true;
    const payload = { ...formData };
    if (props.clientId) {
      delete payload.matKhauDangNhap; // Không gửi mật khẩu khi cập nhật nếu backend không yêu cầu
      await clientService.update(props.clientId, payload);
    } else {
      await clientService.create(payload);
    }
    emit('refresh');
    emit('close');
  } catch (error) {
    alert("Lỗi: " + (error.response?.data?.message || "Không thể kết nối đến máy chủ"));
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
  // Vì là popup nhập liệu, nên hỏi trước khi đóng để tránh mất dữ liệu đang gõ
  if (confirm("Dữ liệu đang nhập sẽ không được lưu. Bạn có chắc muốn thoát?")) {
    emit('close');
  }
};

onMounted(async () => {
  if (props.clientId) {
    const res = await clientService.getDetail(props.clientId);
    Object.assign(formData, res.data);
    if (res.data.ngaySinh) {
      formData.ngaySinh = dayjs(res.data.ngaySinh).format('YYYY-MM-DD');
    }
  }
});
</script>

<style scoped>
/* Giữ nguyên CSS đẹp của bạn */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100vw; height: 100vh; background: rgba(0,0,0,0.4); display: flex; justify-content: center; align-items: center; z-index: 999; cursor: pointer; }
.modal-box { background: #fff; width: 700px; border-radius: 8px; overflow: hidden; cursor: default; }
.modal-header-custom { padding: 15px 20px; border-bottom: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
.title-modal { color: #7B121C; font-weight: bold; }
.close-x { cursor: pointer; font-weight: bold; color: #aaa; }
.modal-body-custom { padding: 20px; max-height: 80vh; overflow-y: auto; }
.form-label-custom { font-weight: 600; font-size: 14px; margin-bottom: 5px; display: flex; align-items: center; white-space: nowrap; gap: 4px; }
.form-control, .form-select { border-radius: 5px; font-size: 14px; }
.radio-red:checked { background-color: #7B121C; border-color: #7B121C; }
.modal-footer-custom { padding: 15px 20px; border-top: 1px solid #eee; display: flex; justify-content: flex-end; gap: 10px; }
.btn-cancel { background: #e9ecef; border: none; padding: 8px 25px; border-radius: 5px; }
.btn-submit { background: #7B121C; color: #fff; border: none; padding: 8px 25px; border-radius: 5px; }
.text-danger { font-size: 12px; margin-top: 4px; display: block; font-style: italic; }
.input-readonly { background: #f8f9fa !important; }
</style>