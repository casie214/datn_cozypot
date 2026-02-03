<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-box shadow-lg">
      <div class="modal-header-custom">
        <h5 class="m-0 title-modal">
          <i class="fas" :class="staffId ? 'fa-user-edit' : 'fa-user-plus'"></i>
          {{ staffId ? 'Cập nhật nhân viên' : 'Thêm nhân viên mới' }}
        </h5>
        <span class="close-x" @click="$emit('close')">&times;</span>
      </div>

      <div class="modal-body-custom">
        <form class="container-fluid" @submit.prevent>
          <div class="row">
            <div class="col-md-4 text-center border-end section-left">
              <label class="form-label-custom d-block mb-3">Ảnh nhân viên</label>
              <div class="avatar-wrapper mb-2" @click="triggerFileInput">
                <img :src="imagePreview || 'https://via.placeholder.com/150'" alt="Avatar" class="avatar-img" />
                <div class="avatar-hint">Thay đổi ảnh</div>
              </div>
              <input type="file" ref="fileInput" class="d-none" @change="onFileChange" accept="image/*" />
              <small class="text-muted d-block">Nhấn vào ảnh để tải lên</small>
            </div>

            <div class="col-md-8">
              <div class="col-12">
      <div class="scanner-container">
        <button type="button" class="btn btn-scan-qr w-100" @click="toggleScanner">
          <i class="fas" :class="isScanning ? 'fa-times-circle' : 'fa-qrcode'"></i> 
          {{ isScanning ? ' Ngừng quét' : ' Quét mã QR trên CCCD' }}
        </button>
        <div v-show="isScanning" id="reader" class="mt-2 border rounded shadow-sm"></div>
        <div v-if="isScanning" class="scan-hint">Vui lòng đưa mã QR trên CCCD vào khung hình</div>
      </div>
    </div>
              <div class="row g-2"> <div class="col-md-6">
                  <label class="form-label-custom">Họ và tên <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.hoTenNhanVien}" v-model="formData.hoTenNhanVien" placeholder="Nhập họ tên">
                  <div class="error-text">{{ errors.hoTenNhanVien }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Vai trò <span class="star">*</span></label>
                  <select class="form-select custom-input" :class="{'is-invalid': errors.idVaiTro}" v-model="formData.idVaiTro">
                    <option value="" disabled>-- Chọn vai trò --</option>
                    <option v-for="role in listRoles" :key="role.id" :value="role.id">{{ role.tenVaiTro }}</option>
                  </select>
                  <div class="error-text">{{ errors.idVaiTro }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Số CCCD <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.soCccd}" v-model="formData.soCccd" @blur="checkDuplicate('soCccd')" placeholder="12 chữ số">
                  <div class="error-text">{{ errors.soCccd }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Số điện thoại <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.sdtNhanVien}" v-model="formData.sdtNhanVien" @blur="checkDuplicate('sdtNhanVien')" placeholder="Số điện thoại">
                  <div class="error-text">{{ errors.sdtNhanVien }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Ngày cấp CCCD <span class="star">*</span></label>
                  <input type="date" class="form-control custom-input" :class="{'is-invalid': errors.ngayCapCccd}" v-model="formData.ngayCapCccd">
                  <div class="error-text">{{ errors.ngayCapCccd }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Nơi cấp <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.noiCapCccd}" v-model="formData.noiCapCccd" placeholder="Nơi cấp">
                  <div class="error-text">{{ errors.noiCapCccd }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Ngày sinh <span class="star">*</span></label>
                  <input type="date" class="form-control custom-input" :class="{'is-invalid': errors.ngaySinh}" v-model="formData.ngaySinh">
                  <div class="error-text">{{ errors.ngaySinh }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Giới tính <span class="star">*</span></label>
                  <div class="d-flex gap-3 pt-2">
                    <div class="form-check">
                      <input class="form-check-input custom-radio" type="radio" :value="true" v-model="formData.gioiTinh" id="male">
                      <label class="form-check-label" for="male">Nam</label>
                    </div>
                    <div class="form-check">
                      <input class="form-check-input custom-radio" type="radio" :value="false" v-model="formData.gioiTinh" id="female">
                      <label class="form-check-label" for="female">Nữ</label>
                    </div>
                  </div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Email <span class="star">*</span></label>
                  <input type="email" class="form-control custom-input" :class="{'is-invalid': errors.email}" v-model="formData.email" placeholder="Email liên hệ">
                  <div class="error-text">{{ errors.email }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Địa chỉ thường trú <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input" :class="{'is-invalid': errors.diaChi}" v-model="formData.diaChi" placeholder="Số nhà, tên đường...">
                  <div class="error-text">{{ errors.diaChi }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Tên đăng nhập <span class="star">*</span></label>
                  <input type="text" class="form-control custom-input":class="{'is-invalid': errors.tenDangNhap}" v-model="formData.tenDangNhap" :readonly="!!staffId" placeholder="Tên đăng nhập">
                  <div class="error-text">{{ errors.tenDangNhap }}</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label-custom">Mật khẩu <span class="star">*</span></label>
                  <input v-if="!staffId" type="password" class="form-control custom-input" :class="{'is-invalid': errors.matKhauDangNhap}" v-model="formData.matKhauDangNhap" placeholder="Mật khẩu">
                  <input v-else type="text" class="form-control custom-input bg-light" value="********" readonly>
                  <div class="error-text">{{ errors.matKhauDangNhap }}</div>
                </div>
              </div>
            </div>
          </div>
        </form>
      </div>

      <div class="modal-footer-custom">
        <button class="btn btn-light-custom" @click="$emit('close')">Hủy bỏ</button>
        <button class="btn btn-main-custom" @click="handleSave" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          {{ staffId ? 'Cập nhật ngay' : 'Thêm mới' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, watch } from 'vue';
import staffService from '@/services/staffService';
import dayjs from 'dayjs';
import Swal from 'sweetalert2';
import { useToast } from "vue-toastification";
import { Html5QrcodeScanner } from "html5-qrcode";
import { onUnmounted } from 'vue';

// Đảm bảo tắt camera khi thoát modal bất thình lình
onUnmounted(() => {
  stopScanner();
});
const isScanning = ref(false);
let html5QrcodeScanner = null;

// Hàm bật/tắt Scanner
const toggleScanner = () => {
  isScanning.value = !isScanning.value;
  if (isScanning.value) {
    // Đợi DOM render xong id="reader" rồi mới khởi tạo
    setTimeout(() => {
      html5QrcodeScanner = new Html5QrcodeScanner("reader", { 
        fps: 10, 
        qrbox: { width: 250, height: 250 } 
      });
      html5QrcodeScanner.render(onScanSuccess, onScanFailure);
    }, 200);
  } else {
    stopScanner();
  }
};

const stopScanner = () => {
  if (html5QrcodeScanner) {
    html5QrcodeScanner.clear().catch(error => console.error("Lỗi khi đóng scanner:", error));
    html5QrcodeScanner = null;
  }
  isScanning.value = false;
};

// Hàm xử lý khi quét thành công
const onScanSuccess = (decodedText) => {
  const parts = decodedText.split('|');
  
  if (parts.length >= 6) {
    formData.soCccd = parts[0];
    formData.hoTenNhanVien = parts[2];
    
    // Xử lý Ngày sinh
    const rawDate = parts[3];
    if (rawDate && rawDate.length === 8) {
      formData.ngaySinh = `${rawDate.substring(4, 8)}-${rawDate.substring(2, 4)}-${rawDate.substring(0, 2)}`;
    }
    
    formData.gioiTinh = parts[4] === 'Nam';
    formData.diaChi = parts[5];
    
    // 1. Tự động điền Nơi cấp (Vì tất cả CCCD chip đều từ một nơi)
    formData.noiCapCccd = "Cục Cảnh sát quản lý hành chính về trật tự xã hội";

    // 2. Xử lý Ngày cấp (Nếu có ở index 6)
    if (parts[6]) {
        const rawIssueDate = parts[6];
        if (rawIssueDate.length === 8) {
            formData.ngayCapCccd = `${rawIssueDate.substring(4, 8)}-${rawIssueDate.substring(2, 4)}-${rawIssueDate.substring(0, 2)}`;
        }
    }

    toast.success("Đã lấy thông tin thành công!");
    checkDuplicate('soCccd');
    stopScanner(); 
  } else {
    toast.error("Mã QR không đúng định dạng");
  }
};

const onScanFailure = (error) => {
  // Hàm này chạy liên tục khi camera chưa tìm thấy mã QR, để trống để tránh spam log
};


const props = defineProps(['staffId']);
const emit = defineEmits(['close', 'refresh']);
const toast = useToast();

const loading = ref(false);
const listRoles = ref([]);
const imagePreview = ref(null);
const fileInput = ref(null);
const selectedFile = ref(null);

const formData = reactive({
  id: null,
  idVaiTro: '',
  hoTenNhanVien: '',
  soCccd: '',
  ngayCapCccd: '',
  noiCapCccd: '',
  tenDangNhap: '',
  matKhauDangNhap: '',
  sdtNhanVien: '',
  email: '',
  diaChi: '',
  ngaySinh: '',
  ngayVaoLam: dayjs().format('YYYY-MM-DD'),
  gioiTinh: true,
  trangThaiLamViec: 1
});

const errors = reactive({
  idVaiTro: '', hoTenNhanVien: '', soCccd: '', ngayCapCccd: '', 
  noiCapCccd: '', tenDangNhap: '', sdtNhanVien: '', email: '', 
  ngaySinh: '', diaChi: '', matKhauDangNhap: ''
});

// WATCHER: Xóa lỗi đỏ ngay khi người dùng bắt đầu sửa lại dữ liệu
watch(() => ({ ...formData }), (newVal) => {
  Object.keys(newVal).forEach(key => {
    if (newVal[key] && errors[key]) {
      // Riêng các trường đặc thù, chỉ xóa lỗi khi đạt định dạng tối thiểu
      if (key === 'email' && !/^\S+@\S+\.\S+$/.test(newVal[key])) return;
      if (key === 'sdtNhanVien' && !/^\d{10}$/.test(newVal[key])) return;
      if (key === 'soCccd' && !/^\d{12}$/.test(newVal[key])) return;
      
      errors[key] = '';
    }
  });
}, { deep: true });

const triggerFileInput = () => fileInput.value.click();
const onFileChange = (e) => {
  const file = e.target.files[0];
  if (file) {
    selectedFile.value = file;
    imagePreview.value = URL.createObjectURL(file);
  }
};

const checkDuplicate = async (type) => {
  if (!formData[type]) return;
  try {
    const res = await staffService.checkDuplicate({ 
      type, 
      value: formData[type], 
      excludeId: props.staffId 
    });
    if (res.data.exists) {
      errors[type] = `Thông tin này đã tồn tại trên hệ thống`;
    }
  } catch (e) { console.error("Lỗi check trùng"); }
};

const validateForm = () => {
  let isValid = true;
  const today = dayjs();

  // 1. Kiểm tra các trường bắt buộc nhập (Required)
  const requiredFields = {
    hoTenNhanVien: "Vui lòng nhập họ tên",
    idVaiTro: "Vui lòng chọn vai trò",
    soCccd: "Vui lòng nhập số CCCD",
    ngayCapCccd: "Vui lòng chọn ngày cấp",
    noiCapCccd: "Vui lòng nhập nơi cấp",
    sdtNhanVien: "Vui lòng nhập số điện thoại",
    email: "Vui lòng nhập email",
    ngaySinh: "Vui lòng chọn ngày sinh",
    tenDangNhap: "Vui lòng nhập tên đăng nhập",
    diaChi: "Vui lòng nhập địa chỉ"
  };

  Object.keys(requiredFields).forEach(key => {
    if (!formData[key]) {
      errors[key] = requiredFields[key];
      isValid = false;
    }
  });

  // 2. Kiểm tra độ tuổi (Phải đủ 18 tuổi)
  if (formData.ngaySinh) {
    const birthDate = dayjs(formData.ngaySinh);
    const age = today.diff(birthDate, 'year');
    if (birthDate.isAfter(today)) {
      errors.ngaySinh = "Ngày sinh không thể ở tương lai";
      isValid = false;
    } else if (age < 18) {
      errors.ngaySinh = "Nhân viên phải từ 18 tuổi trở lên";
      isValid = false;
    }
  }

  // 3. Kiểm tra ngày cấp CCCD (Không được ở tương lai)
  if (formData.ngayCapCccd) {
    const issueDate = dayjs(formData.ngayCapCccd);
    if (issueDate.isAfter(today)) {
      errors.ngayCapCccd = "Ngày cấp không thể ở tương lai";
      isValid = false;
    }
  }

  // 4. Kiểm tra Mật khẩu (Chỉ bắt buộc khi thêm mới)
  if (!props.staffId && (!formData.matKhauDangNhap || formData.matKhauDangNhap.length < 6)) {
    errors.matKhauDangNhap = "Mật khẩu phải từ 6 ký tự";
    isValid = false;
  }

  // 5. Kiểm tra định dạng Email/SĐT/CCCD
  if (formData.email && !/^\S+@\S+\.\S+$/.test(formData.email)) {
    errors.email = "Email không đúng định dạng";
    isValid = false;
  }
  if (formData.sdtNhanVien && !/^\d{10}$/.test(formData.sdtNhanVien)) {
    errors.sdtNhanVien = "SĐT phải gồm 10 chữ số";
    isValid = false;
  }
  if (formData.soCccd && !/^\d{12}$/.test(formData.soCccd)) {
    errors.soCccd = "CCCD phải gồm 12 chữ số";
    isValid = false;
  }

  return isValid;
};

const handleSave = async () => {
  // Cấu hình Z-index để tái sử dụng
  const swalConfig = {
    confirmButtonColor: '#800000',
    // Ép SweetAlert nổi lên trên mức 9999 của modal-overlay
    didOpen: () => {
      const container = document.querySelector('.swal2-container');
      if (container) container.style.zIndex = '100000';
    }
  };

  // 1. Validate form
  if (!validateForm()) {
    Swal.fire({
      ...swalConfig,
      icon: 'warning',
      title: 'Thông báo',
      text: 'Vui lòng hoàn thiện các trường còn thiếu!',
    });
    return;
  }

  if (Object.values(errors).some(v => v && v.includes("tồn tại"))) {
    Swal.fire({
      ...swalConfig,
      icon: 'info',
      title: 'Chú ý',
      text: 'Vui lòng xử lý các thông tin bị trùng lặp!',
    });
    return;
  }

  // 2. Hỏi xác nhận
  const confirmResult = await Swal.fire({
    ...swalConfig,
    title: props.staffId ? 'Xác nhận cập nhật?' : 'Xác nhận thêm mới?',
    text: "Hành động này sẽ lưu dữ liệu vào hệ thống.",
    icon: 'question',
    showCancelButton: true,
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy'
  });

  if (!confirmResult.isConfirmed) return;

  // 3. Thực thi lưu
  try {
    loading.value = true;
    
    // Loading State
    Swal.fire({
      ...swalConfig,
      title: 'Đang gửi dữ liệu...',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
        // Vẫn phải ép z-index cho loading vì nó render lại container
        if (document.querySelector('.swal2-container')) {
          document.querySelector('.swal2-container').style.zIndex = '100000';
        }
      }
    });

    const data = new FormData();
    Object.keys(formData).forEach(key => {
      let value = formData[key];
      if (key === 'gioiTinh') value = (value === true || value === 'true') ? 1 : 0;
      if (value !== null && value !== undefined && value !== '') {
        data.append(key, value);
      }
    });

    if (selectedFile.value) data.append('hinhAnhFile', selectedFile.value);

    if (props.staffId) {
      await staffService.update(props.staffId, data);
    } else {
      await staffService.create(data);
    }

    // 4. Thành công
    await Swal.fire({
      ...swalConfig,
      icon: 'success',
      title: 'Thành công!',
      text: props.staffId ? 'Thông tin nhân viên đã được cập nhật.' : 'Đã thêm nhân viên mới thành công.',
      timer: 2500,
      timerProgressBar: true,
      showConfirmButton: true
    });
    
    emit('refresh');
    emit('close');
  } catch (error) {
    // 5. Thất bại (Lỗi 400 hoặc lỗi mạng)
    const errorMsg = error.response?.data?.message || "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại!";
    Swal.fire({
      ...swalConfig,
      icon: 'error',
      title: 'Lỗi lưu dữ liệu',
      text: errorMsg,
      confirmButtonColor: '#d33' // Đổi màu nút confirm riêng cho lỗi
    });
  } finally { 
    loading.value = false; 
  }
};

onMounted(async () => {
  try {
    const roleRes = await staffService.getActiveRoles();
    listRoles.value = roleRes.data;
    
    if (props.staffId) {
      const res = await staffService.getDetail(props.staffId);
      Object.assign(formData, res.data);
      formData.gioiTinh = res.data.gioiTinh === true || res.data.gioiTinh === 1;
      // Định dạng ngày để hiển thị lên <input type="date">
      formData.ngayCapCccd = res.data.ngayCapCccd ? dayjs(res.data.ngayCapCccd).format('YYYY-MM-DD') : '';
      formData.ngaySinh = res.data.ngaySinh ? dayjs(res.data.ngaySinh).format('YYYY-MM-DD') : '';
      
      if (res.data.anhDaiDien) {
         imagePreview.value = `http://localhost:8080/uploads/images/${res.data.anhDaiDien}`;
      }
    }
  } catch (e) { 
    toast.error("Không thể tải dữ liệu nhân viên"); 
  }
});
</script>

<style scoped>

/* Giữ nguyên phần Style bạn đã viết rất tốt ở trên */
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
/* Đảm bảo mọi thông báo SweetAlert luôn nằm trên Modal */
.btn-scan-qr {
  border: 2px dashed #800000;
  color: #800000;
  background: #fff;
  font-weight: 700;
  padding: 12px;
  transition: 0.3s;
}

.btn-scan-qr:hover {
  background: #fff5f5;
  border-style: solid;
}

.scan-hint {
  font-size: 11px;
  color: #666;
  text-align: center;
  margin-top: 5px;
  font-style: italic;
}

#reader {
  overflow: hidden;
  background: #000;
}
</style>