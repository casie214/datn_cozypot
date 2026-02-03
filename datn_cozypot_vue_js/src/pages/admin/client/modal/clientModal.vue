<template>
    

  <div class="modal-overlay" @click.self="handleCancel">
    <div class="modal-box shadow-lg">
      <div class="modal-header-custom">
        <h5 class="m-0 title-modal">
          <i class="fas" :class="clientId ? 'fa-user-edit' : 'fa-user-plus'"></i>
          {{ clientId ? 'Cập nhật thông tin khách hàng' : 'Thêm khách hàng mới' }}
        </h5>
        <span class="close-x" @click="handleCancel">&times;</span>
      </div>

      <GlobalDialogue 
        :show="dialogVisible"  
        :type="dialogConfig?.type" 
        :variant="dialogConfig?.variant"
        :title="dialogConfig?.title" 
        :message="dialogConfig?.message" 
        @close="handleDialogClose"
        @confirm="handleDialogConfirm" 
    />

      <div class="modal-body-custom">
        <form class="container-fluid" @submit.prevent>
          <div class="row">
            <div class="col-md-4 text-center border-end section-left">
              <label class="form-label-custom d-block mb-3">Ảnh khách hàng</label>
              <div class="avatar-wrapper mb-3" @click="triggerFileInput">
                <img :src="previewUrl || 'https://via.placeholder.com/150'" 
                     @error="(e) => e.target.src = 'https://via.placeholder.com/150'"
                     alt="Avatar" class="avatar-img" />
                <div class="avatar-hint">Thay đổi ảnh</div>
              </div>
              <input type="file" ref="fileInput" class="d-none" @change="onFileChange" accept="image/*" />
              <small class="text-muted">Nhấn vào ảnh để tải lên</small>
            </div>

            <div class="col-md-8">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label-custom">Họ và tên <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.tenKhachHang}" 
                         v-model="formData.tenKhachHang" placeholder="Mời nhập đầy đủ họ tên">
                  <div class="error-text">{{ errors.tenKhachHang }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Số điện thoại <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.soDienThoai}" 
                         v-model="formData.soDienThoai" placeholder="Mời nhập số điện thoại">
                  <div class="error-text">{{ errors.soDienThoai }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Email <span class="star">*</span></label>
                  <input type="email" class="form-control custom-input" :class="{'is-invalid': errors.email}" 
                         v-model="formData.email" placeholder="Mời nhập email">
                  <div class="error-text">{{ errors.email }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Ngày sinh <span class="star">*</span></label>
                  <input type="date" class="form-control custom-input" :class="{'is-invalid': errors.ngaySinh}" 
                         v-model="formData.ngaySinh">
                  <div class="error-text">{{ errors.ngaySinh }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Tên đăng nhập <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.tenDangNhap}" 
                         v-model="formData.tenDangNhap" :readonly="!!clientId" placeholder="Mời nhập tên đăng nhập">
                  <div class="error-text">{{ errors.tenDangNhap }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Mật khẩu <span v-if="!clientId" class="star">*</span></label>
                  <input v-if="!clientId" type="password" class="form-control custom-input" :class="{'is-invalid': errors.matKhauDangNhap}"
                         v-model="formData.matKhauDangNhap" placeholder="Mời nhập mật khẩu">
                  <input v-else type="text" class="form-control custom-input bg-light" value="********" readonly disabled>
                  <div class="error-text">{{ errors.matKhauDangNhap }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Giới tính <span class="star">*</span></label>
                  <div class="d-flex gap-4 mt-2">
                    <div class="form-check">
                      <input class="form-check-input radio-red" type="radio" name="gioiTinh" 
                            id="gtNam" :value="true" v-model="formData.gioiTinh">
                      <label class="form-check-label cursor-pointer" for="gtNam">Nam</label>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input radio-red" type="radio" name="gioiTinh" 
                            id="gtNu" :value="false" v-model="formData.gioiTinh">
                      <label class="form-check-label cursor-pointer" for="gtNu">Nữ</label>
                    </div>
                  </div>
                </div>
                <div class="col-md-6">
                  <label class="form-label-custom">Địa chỉ thường trú <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.diaChi}" 
                         v-model="formData.diaChi" placeholder="Mời nhập địa chỉ thường trú">
                  <div class="error-text">{{ errors.diaChi }}</div>
                </div>
                </div>
            </div>
          </div>
        </form>
      </div>

      <div class="modal-footer-custom">
        <button class="btn btn-light-custom" @click="handleCancel">Hủy bỏ</button>
        <button class="btn btn-main-custom" @click="handleSave" :disabled="loading">
          {{ clientId ? 'Cập nhật ngay' : 'Thêm mới' }}
        </button>
      </div>
    </div>
  </div>

  
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue';
import clientService from '@/services/clientService';
import dayjs from 'dayjs';
import { useToast } from "vue-toastification";
import { useDialog } from '@/services/foodFunction';
import GlobalDialogue from '@/components/globalDialogue.vue';
import Swal from 'sweetalert2';

const swalConfig = {
  confirmButtonColor: '#800000',
  cancelButtonColor: '#6c757d',
  didOpen: () => {
    const container = document.querySelector('.swal2-container');
    if (container) container.style.zIndex = '100000';
  }
};
const { 
    showConfirm, 
    isVisible: dialogVisible,      
    dialogConfig, 
    handleConfirm: handleDialogConfirm, 
    handleClose: handleDialogClose
} = useDialog();

const props = defineProps(['clientId']);
const emit = defineEmits(['close', 'refresh']);
const toast = useToast();

const loading = ref(false);
const fileInput = ref(null);
const selectedFile = ref(null);
const previewUrl = ref(null);
const phoneRegex = /^(0)[1-9]{1}[0-9]{8}$/;

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

const errors = reactive({
  tenKhachHang: '', 
  soDienThoai: '', 
  email: '', 
  tenDangNhap: '', 
  matKhauDangNhap: '',
  ngaySinh: '',
  diaChi: ''
});

// --- LOGIC VALIDATE TẠI CHỖ (Gõ đến đâu check đến đấy) ---
const validateField = async (field, value) => {
  const val = value ? String(value).trim() : '';
  
  switch (field) {
    case 'tenKhachHang':
      errors.tenKhachHang = val ? '' : 'Họ tên không được để trống';
      break;
    case 'diaChi':
      errors.diaChi = val ? '' : 'Địa chỉ không được để trống';
      break;
    case 'ngaySinh':
      if (!val) {
        errors.ngaySinh = 'Vui lòng chọn ngày sinh';
      } else if (dayjs(val).isAfter(dayjs())) {
        errors.ngaySinh = 'Ngày sinh không được ở tương lai';
      } else {
        errors.ngaySinh = '';
      }
      break;
    case 'tenDangNhap':
      if (!props.clientId) {
        if (!val) errors.tenDangNhap = 'Tên đăng nhập không được để trống';
        else {
          const res = await clientService.checkDuplicate('tenDangNhap', val, props.clientId);
          errors.tenDangNhap = res.data.exists ? 'Tên đăng nhập đã tồn tại' : '';
        }
      }
      break;
    case 'email':
      if (!val) errors.email = 'Email không được để trống';
      else if (!/^\S+@\S+\.\S+$/.test(val)) errors.email = 'Email không đúng định dạng';
      else {
        const res = await clientService.checkDuplicate('email', val, props.clientId);
        errors.email = res.data.exists ? 'Email này đã được sử dụng' : '';
      }
      break;
    case 'soDienThoai':
      if (!val) errors.soDienThoai = 'Số điện thoại không được để trống';
      else if (!phoneRegex.test(val)) errors.soDienThoai = 'SĐT phải có 10 số (bắt đầu bằng 0)';
      else {
        const res = await clientService.checkDuplicate('soDienThoai', val, props.clientId);
        errors.soDienThoai = res.data.exists ? 'Số điện thoại này đã tồn tại' : '';
      }
      break;
    case 'matKhauDangNhap':
      if (!props.clientId) {
        if (!val) errors.matKhauDangNhap = 'Mật khẩu không được để trống';
        else if (val.length < 6) errors.matKhauDangNhap = 'Mật khẩu tối thiểu 6 ký tự';
        else errors.matKhauDangNhap = '';
      }
      break;
  }
};

// Theo dõi thay đổi để báo đỏ/xóa đỏ ngay lập tức
watch(() => ({ ...formData }), (newVal) => {
  Object.keys(newVal).forEach(key => {
    if (errors[key] !== undefined) validateField(key, newVal[key]);
  });
}, { deep: true });

const triggerFileInput = () => fileInput.value.click();

const onFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    selectedFile.value = file;
    previewUrl.value = URL.createObjectURL(file);
  }
};

const handleSave = async () => {
  // 1. Validate toàn bộ lần cuối
  for (const key of Object.keys(errors)) {
    await validateField(key, formData[key]);
  }

  if (Object.values(errors).some(msg => msg !== '')) {
    Swal.fire({
      ...swalConfig,
      icon: 'warning',
      title: 'Thông báo',
      text: 'Vui lòng hoàn thiện các trường đang báo đỏ!',
    });
    return;
  }

  // 2. Hỏi xác nhận
  const confirmResult = await Swal.fire({
    ...swalConfig,
    title: props.clientId ? 'Cập nhật khách hàng?' : 'Thêm khách hàng mới?',
    text: "Bạn có chắc chắn muốn lưu thông tin này không?",
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Đồng ý lưu',
    cancelButtonText: 'Kiểm tra lại'
  });

  if (!confirmResult.isConfirmed) return;

  // 3. Thực thi lưu
  try {
    loading.value = true;
    
    // Hiển thị loading
    Swal.fire({
      ...swalConfig,
      title: 'Đang xử lý dữ liệu...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
        if (document.querySelector('.swal2-container')) {
          document.querySelector('.swal2-container').style.zIndex = '100000';
        }
      }
    });

    const data = new FormData();
    Object.keys(formData).forEach(key => {
      // Xử lý giá trị gioiTinh sang 1/0 nếu backend yêu cầu
      let value = formData[key];
      if (key === 'gioiTinh') value = (value === true || value === 'true') ? 1 : 0;
      data.append(key, value !== null && value !== undefined ? value : '');
    });

    if (selectedFile.value) {
      data.append('hinhAnhFile', selectedFile.value);
    }

    if (props.clientId) {
      await clientService.update(props.clientId, data);
    } else {
      await clientService.create(data);
    }

    // 4. Thông báo thành công
    await Swal.fire({
      ...swalConfig,
      icon: 'success',
      title: 'Thành công!',
      text: props.clientId ? 'Thông tin khách hàng đã được cập nhật.' : 'Đã thêm khách hàng mới thành công.',
      timer: 2000,
      showConfirmButton: false
    });

    emit('refresh');
    emit('close');
  } catch (error) {
    console.error("Lỗi:", error.response?.data);
    
    // Nếu có lỗi validate từ Backend
    if (error.response?.data?.errors) {
      const backendErrors = error.response.data.errors;
      Object.keys(backendErrors).forEach(key => {
        if (errors[key] !== undefined) errors[key] = backendErrors[key];
      });
    }

    Swal.fire({
      ...swalConfig,
      icon: 'error',
      title: 'Lỗi lưu dữ liệu',
      text: error.response?.data?.message || "Dữ liệu không hợp lệ, vui lòng kiểm tra lại!",
    });
  } finally {
    loading.value = false;
  }
};

const handleCancel = () => {
    showConfirm(
        "Dữ liệu chưa lưu sẽ bị mất. Bạn muốn thoát?",
        () => {
            emit('close'); 
        },
        "Xác nhận thoát",
        "warning" 
    );
};

onMounted(async () => {
  if (props.clientId) {
    try {
      const res = await clientService.getDetail(props.clientId);
      const data = res.data;
      Object.assign(formData, data);

      if (data.ngaySinh) {
        formData.ngaySinh = dayjs(data.ngaySinh).format('YYYY-MM-DD');
      }

      // ✅ CHỈ DÙNG ĐÚNG FIELD BACKEND
      if (data.anhDaiDien) {
        previewUrl.value =
          `http://localhost:8080/uploads/customers/${data.anhDaiDien}?t=${Date.now()}`;
      } else {
        previewUrl.value = null;
      }
    } catch (e) {
      toast.error("Không thể tải dữ liệu khách hàng");
    }
  }
});

</script>

<style scoped>
.is-invalid { border-color: #ff4d4f !important; background-color: #fff2f0 !important; }
.error-text { color: #ff4d4f; font-size: 11px; margin-top: 4px; font-weight: 600; min-height: 15px; text-align: left; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; justify-content: center; align-items: center; z-index: 9999; backdrop-filter: blur(4px); }
.modal-box { background: #fff; width: 1000px; border-radius: 20px; overflow: hidden; animation: modalIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
@keyframes modalIn { from { transform: translateY(-30px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
.modal-header-custom { padding: 20px 30px; border-bottom: 1px solid #f0f0f0; display: flex; justify-content: space-between; align-items: center; }
.title-modal { color: #800000; font-weight: 800; font-size: 1.3rem; }
.close-x { font-size: 32px; cursor: pointer; color: #bbb; transition: 0.2s; }
.close-x:hover { color: #800000; }
.modal-body-custom { padding: 30px; max-height: 80vh; overflow-y: auto; }
.avatar-wrapper { width: 180px; height: 180px; border-radius: 50%; border: 4px solid #f8f9fa; margin: auto; overflow: hidden; position: relative; cursor: pointer; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
.avatar-img { width: 100%; height: 100%; object-fit: cover; }
.avatar-hint { position: absolute; bottom: 0; width: 100%; background: rgba(0,0,0,0.5); color: #fff; font-size: 12px; padding: 5px 0; opacity: 0; transition: 0.3s; }
.avatar-wrapper:hover .avatar-hint { opacity: 1; }
.form-label-custom { font-weight: 700; font-size: 12px; color: #555; text-transform: uppercase; margin-bottom: 5px; }
.star { color: #ff4d4f; }
.custom-input { border-radius: 8px; padding: 10px; font-size: 14px; border: 1px solid #ddd; width: 100%; transition: 0.3s; }
.custom-input:focus { border-color: #800000; outline: none; box-shadow: 0 0 0 3px rgba(128,0,0,0.1); }
.error-text { color: #ff4d4f; font-size: 11px; margin-top: 4px; font-weight: 600; min-height: 15px; }
.is-invalid { border-color: #ff4d4f !important; background-color: #fff2f0 !important; }
.modal-footer-custom { padding: 20px 30px; border-top: 1px solid #f0f0f0; display: flex; justify-content: flex-end; gap: 10px; }
.btn-main-custom { background: #800000; color: #fff; border: none; padding: 10px 30px; border-radius: 8px; font-weight: 700; transition: 0.3s; }
.btn-main-custom:hover:not(:disabled) { background: #a00000; transform: translateY(-2px); }
.btn-light-custom { background: #eee; color: #333; border: none; padding: 10px 25px; border-radius: 8px; font-weight: 600; }
.radio-red:checked { background-color: #800000; border-color: #800000; }
</style>