<template>
  <div class="staff-form-page bg-light min-vh-100 p-4">
    <div class="container mb-4">
      <div class="d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-3">
          <div class="icon-header-box">
            <i class="fas" :class="staffId ? 'fa-user-edit' : 'fa-user-plus'"></i>
          </div>
          <div>
            <h4 class="title-page text-wine fw-bold m-0">
              {{ staffId ? 'Cập nhật thông tin nhân viên' : 'Thêm nhân viên mới' }}
            </h4>
            <p class="text-muted small mb-0">Quản lý hồ sơ nhân sự hệ thống</p>
          </div>
        </div>
        <button class="btn btn-outline-secondary rounded-pill px-4" @click="$router.push('/admin/staff')">
          <i class="fas fa-arrow-left me-2"></i> Quay lại danh sách
        </button>
      </div>
    </div>

    <div class="container">
      <div class="row g-4">
        <div class="col-lg-4">
          <div class="card shadow-sm border-0 rounded-4 sticky-top" style="top: 20px;">
            <div class="card-body p-4">
              <h6 class="form-label-custom mb-4 text-center">Ảnh nhân viên</h6>
              
              <div class="upload-container mb-4">
                <div class="upload-zone" @click="triggerFileInput" :class="{'has-image': imagePreview}">
                  <template v-if="imagePreview">
                    <img :src="imagePreview" alt="Avatar" class="img-preview" />
                    <div class="upload-overlay">
                      <i class="fas fa-camera mb-2"></i>
                      <span>Thay đổi ảnh</span>
                    </div>
                  </template>
                  <template v-else>
                    <div class="upload-placeholder">
                      <div class="icon-circle">
                        <i class="fas fa-cloud-upload-alt"></i>
                      </div>
                      <p class="mt-3 mb-1 fw-bold">Tải ảnh lên</p>
                      <span class="text-muted tiny">JPG, PNG (Max 2MB)</span>
                    </div>
                  </template>
                </div>
                <input type="file" ref="fileInput" class="d-none" @change="onFileChange" accept="image/*" />
              </div>

              <hr class="dashed">

              <div class="scanner-box p-3 rounded-4 bg-light-wine border-wine-dashed text-center">
                <h6 class="fw-bold text-wine mb-3 small"><i class="fas fa-qrcode me-2"></i>NHẬP LIỆU NHANH CCCD</h6>
                <button type="button" class="btn btn-wine-sm w-100 mb-2 shadow-sm" @click="toggleScanner">
                  <i class="fas" :class="isScanning ? 'fa-times-circle' : 'fa-camera-retro'"></i>
                  {{ isScanning ? ' Ngừng quét' : ' Bật Camera quét mã' }}
                </button>
                <div v-show="isScanning" id="reader" class="mt-2 border rounded-3 overflow-hidden shadow-sm"></div>
                <p v-if="isScanning" class="tiny text-muted mt-2">Đưa mã QR trên thẻ CCCD vào khung hình</p>
              </div>

              <div class="info-alert mt-4 p-3 rounded-3 bg-light border-start-wine">
                <p class="small text-muted mb-0">
                  <i class="fas fa-info-circle text-wine me-2"></i>
                  Lưu ý: Thông tin CCCD sẽ được tự động điền khi quét thành công.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-8">
          <div class="card shadow-sm border-0 rounded-4 overflow-hidden mb-4">
            <div class="card-body p-4 p-md-5">
              <form @submit.prevent>
                <div class="form-section mb-5">
                  <div class="d-flex align-items-center mb-4">
                    <span class="badge-number">1</span>
                    <h5 class="m-0 fw-bold">Thông tin cá nhân</h5>
                  </div>
                  
                  <div class="row g-4">
                    <div class="col-md-6">
                      <label class="form-label-custom">Họ và tên <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="far fa-user icon-input"></i>
                        <input type="text" class="form-control" :class="{'is-invalid': errors.hoTenNhanVien}" 
                               v-model="formData.hoTenNhanVien" placeholder="Nguyễn Văn A">
                      </div>
                      <div class="error-text">{{ errors.hoTenNhanVien }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Vai trò <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="fas fa-user-tag icon-input"></i>
                        <select class="form-select border-0 bg-transparent" v-model="formData.idVaiTro" :class="{'is-invalid': errors.idVaiTro}">
                          <option value="" disabled>-- Chọn vai trò --</option>
                          <option v-for="role in listRoles" :key="role.id" :value="role.id">{{ role.tenVaiTro }}</option>
                        </select>
                      </div>
                      <div class="error-text">{{ errors.idVaiTro }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Số điện thoại <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="fas fa-phone-alt icon-input"></i>
                        <input type="text" class="form-control" v-model="formData.sdtNhanVien" :class="{'is-invalid': errors.sdtNhanVien}" placeholder="09xxxxxxxx">
                      </div>
                      <div class="error-text">{{ errors.sdtNhanVien }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Email liên hệ <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="far fa-envelope icon-input"></i>
                        <input type="email" class="form-control" v-model="formData.email" :class="{'is-invalid': errors.email}" placeholder="email@congty.com">
                      </div>
                      <div class="error-text">{{ errors.email }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Ngày sinh <span class="star">*</span></label>
                      <input type="date" class="form-control custom-input" v-model="formData.ngaySinh" :class="{'is-invalid': errors.ngaySinh}">
                      <div class="error-text">{{ errors.ngaySinh }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Giới tính <span class="star">*</span></label>
                      <div class="gender-selector d-flex gap-3">
                        <input type="radio" class="btn-check" name="gender" id="male" :value="true" v-model="formData.gioiTinh">
                        <label class="btn btn-outline-wine w-100" for="male"><i class="fas fa-mars me-2"></i>Nam</label>

                        <input type="radio" class="btn-check" name="gender" id="female" :value="false" v-model="formData.gioiTinh">
                        <label class="btn btn-outline-wine w-100" for="female"><i class="fas fa-venus me-2"></i>Nữ</label>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="form-section mb-5 pt-4 border-top">
                  <div class="d-flex align-items-center mb-4">
                    <span class="badge-number">2</span>
                    <h5 class="m-0 fw-bold">Thông tin CCCD & Tài khoản</h5>
                  </div>

                  <div class="row g-4">
                    <div class="col-md-12">
                      <label class="form-label-custom">Số Căn cước công dân <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="far fa-id-card icon-input"></i>
                        <input type="text" class="form-control" v-model="formData.soCccd" :class="{'is-invalid': errors.soCccd}" placeholder="12 chữ số">
                      </div>
                      <div class="error-text">{{ errors.soCccd }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Ngày cấp <span class="star">*</span></label>
                      <input type="date" class="form-control custom-input" v-model="formData.ngayCapCccd" :class="{'is-invalid': errors.ngayCapCccd}">
                      <div class="error-text">{{ errors.ngayCapCccd }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Nơi cấp <span class="star">*</span></label>
                      <input type="text" class="form-control custom-input" v-model="formData.noiCapCccd" :class="{'is-invalid': errors.noiCapCccd}" placeholder="Cục CSQLHC...">
                      <div class="error-text">{{ errors.noiCapCccd }}</div>
                    </div>

                    <div class="col-12">
                      <label class="form-label-custom">Địa chỉ thường trú <span class="star">*</span></label>
                      <textarea class="form-control custom-input" rows="2" v-model="formData.diaChi" :class="{'is-invalid': errors.diaChi}" placeholder="Địa chỉ ghi trên CCCD"></textarea>
                      <div class="error-text">{{ errors.diaChi }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Tên đăng nhập <span class="star">*</span></label>
                      <input type="text" class="form-control custom-input bg-light" v-model="formData.tenDangNhap" :readonly="!!staffId" placeholder="username">
                      <div class="error-text">{{ errors.tenDangNhap }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Mật khẩu <span v-if="!staffId" class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="fas fa-lock icon-input"></i>
                        <input v-if="!staffId" type="password" class="form-control" v-model="formData.matKhauDangNhap" :class="{'is-invalid': errors.matKhauDangNhap}" placeholder="••••••••">
                        <input v-else type="text" class="form-control bg-light" value="********" readonly>
                      </div>
                      <div class="error-text">{{ errors.matKhauDangNhap }}</div>
                    </div>
                  </div>
                </div>

                <div class="d-flex justify-content-between align-items-center pt-4 border-top">
                  <p class="text-muted small mb-0">* Bảo mật thông tin nhân sự nội bộ</p>
                  <div class="d-flex gap-3">
                    <button type="button" class="btn btn-light-custom px-4" @click="$router.push('/admin/staff')">Hủy bỏ</button>
                    <button type="button" class="btn btn-main-custom px-5 py-2 shadow-sm" @click="handleSave" :disabled="loading">
                      <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                      {{ staffId ? 'Cập nhật ngay' : 'Thêm nhân viên' }}
                    </button>
                  </div>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { reactive, nextTick, ref, onMounted, watch, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import staffService from '@/services/staffService';
import dayjs from 'dayjs';
import Swal from 'sweetalert2';
import { useToast } from "vue-toastification";
import { Html5QrcodeScanner } from "html5-qrcode";

const route = useRoute();
const router = useRouter();
const toast = useToast();

// ID từ URL
const staffId = ref(route.params.id || null);

//////////////////////////////////////////////////////
// SCANNER
//////////////////////////////////////////////////////

const isScanning = ref(false);
let html5QrcodeScanner = null;
let scanned = false; // chống quét lặp

const toggleScanner = () => {
    isScanning.value = !isScanning.value;

    if (isScanning.value) {
        nextTick(() => {
            if (html5QrcodeScanner) html5QrcodeScanner.clear();

            html5QrcodeScanner = new Html5QrcodeScanner("reader", {
                fps: 10,
                qrbox: { width: 250, height: 250 }
            });

            html5QrcodeScanner.render(onScanSuccess, () => { });
        });
    } else stopScanner();
};

const stopScanner = () => {
    scanned = false;
    if (html5QrcodeScanner) {
        html5QrcodeScanner.clear().catch(() => { });
        html5QrcodeScanner = null;
    }
    isScanning.value = false;
};

const onScanSuccess = (decodedText) => {
    if (scanned) return;
    scanned = true;

    if (!decodedText.includes('|')) {
        toast.error("QR không phải CCCD hợp lệ");
        return;
    }

    const parts = decodedText.split('|');

    formData.soCccd = parts[0] || '';
    formData.hoTenNhanVien = parts[2] || '';

    if (parts[3]?.length === 8) {
        const d = parts[3];
        formData.ngaySinh = `${d.slice(4)}-${d.slice(2, 4)}-${d.slice(0, 2)}`;
    }

    formData.gioiTinh = parts[4]?.toLowerCase().includes("nam");
    formData.diaChi = parts[5] || '';
    formData.noiCapCccd = "Cục CSQLHC về TTXH";

    if (parts[6]?.length === 8) {
        const d = parts[6];
        formData.ngayCapCccd = `${d.slice(4)}-${d.slice(2, 4)}-${d.slice(0, 2)}`;
    }

    toast.success("Đã lấy thông tin CCCD!");
    checkDuplicate('soCccd');
    stopScanner();
};

onUnmounted(stopScanner);

//////////////////////////////////////////////////////
// DATA
//////////////////////////////////////////////////////

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

//////////////////////////////////////////////////////
// CLEAR ERROR WHEN TYPING
//////////////////////////////////////////////////////

watch(() => ({ ...formData }), (val) => {
    Object.keys(val).forEach(key => {
        if (val[key] && errors[key]) errors[key] = '';
    });
}, { deep: true });

//////////////////////////////////////////////////////
// FILE UPLOAD
//////////////////////////////////////////////////////

const triggerFileInput = () => fileInput.value.click();

const onFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
        selectedFile.value = file;
        imagePreview.value = URL.createObjectURL(file);
    }
};

//////////////////////////////////////////////////////
// DUPLICATE CHECK
//////////////////////////////////////////////////////

const checkDuplicate = async (type) => {
    if (!formData[type]) return;

    try {
        const res = await staffService.checkDuplicate(
            type,
            formData[type],
            staffId.value
        );

        if (res.data.exists) {
            errors[type] = "Thông tin đã tồn tại";
        }
    } catch (e) {
        console.error("Duplicate error:", e);
    }
};



//////////////////////////////////////////////////////
// VALIDATE
//////////////////////////////////////////////////////

const validateForm = async () => {
    let ok = true;
    const today = dayjs();

    // 1. Reset toàn bộ lỗi trước khi kiểm tra
    Object.keys(errors).forEach(k => errors[k] = '');

    // 2. Kiểm tra các trường bắt buộc (Required) với lời nhắc thân thiện
    const requiredFields = [
        { key: 'hoTenNhanVien', msg: 'Vui lòng nhập họ và tên nhân viên' },
        { key: 'idVaiTro', msg: 'Vui lòng chọn vai trò cho nhân viên' },
        { key: 'soCccd', msg: 'Vui lòng nhập số CCCD' },
        { key: 'ngayCapCccd', msg: 'Vui lòng chọn ngày cấp CCCD' },
        { key: 'noiCapCccd', msg: 'Vui lòng nhập nơi cấp CCCD' },
        { key: 'sdtNhanVien', msg: 'Vui lòng nhập số điện thoại' },
        { key: 'email', msg: 'Vui lòng nhập địa chỉ email' },
        { key: 'ngaySinh', msg: 'Vui lòng chọn ngày tháng năm sinh' },
        { key: 'tenDangNhap', msg: 'Vui lòng nhập tên đăng nhập hệ thống' },
        { key: 'diaChi', msg: 'Vui lòng nhập địa chỉ thường trú' }
    ];

    requiredFields.forEach(field => {
        if (!formData[field.key] || formData[field.key].toString().trim() === '') {
            errors[field.key] = field.msg;
            ok = false;
        }
    });

    // 3. Kiểm tra định dạng chuyên sâu (nếu đã nhập dữ liệu)
    
    // Mật khẩu (Chỉ kiểm tra khi thêm mới nhân viên)
    if (!staffId.value) {
        if (!formData.matKhauDangNhap) {
            errors.matKhauDangNhap = 'Vui lòng thiết lập mật khẩu đăng nhập';
            ok = false;
        } else if (formData.matKhauDangNhap.length < 6) {
            errors.matKhauDangNhap = 'Mật khẩu cần tối thiểu 6 ký tự để bảo mật';
            ok = false;
        }
    }

    // Định dạng CCCD (12 chữ số)
    if (formData.soCccd && !/^\d{12}$/.test(formData.soCccd)) {
        errors.soCccd = 'Số CCCD không hợp lệ (phải bao gồm đúng 12 chữ số)';
        ok = false;
    }

    // Định dạng Số điện thoại (Việt Nam)
    if (formData.sdtNhanVien && !/^(0|84)(3|5|7|8|9)\d{8}$/.test(formData.sdtNhanVien)) {
        errors.sdtNhanVien = 'Số điện thoại không đúng định dạng';
        ok = false;
    }

    // Định dạng Email
    if (formData.email && !/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(formData.email)) {
        errors.email = 'Địa chỉ email không đúng định dạng (ví dụ: abc@gmail.com)';
        ok = false;
    }

    // 4. Kiểm tra logic ngày tháng
    if (formData.ngaySinh) {
        const birth = dayjs(formData.ngaySinh);
        if (birth.isAfter(today)) {
            errors.ngaySinh = 'Ngày sinh không thể lớn hơn ngày hiện tại';
            ok = false;
        } else if (today.diff(birth, 'year') < 18) {
            errors.ngaySinh = 'Nhân viên phải từ đủ 18 tuổi trở lên';
            ok = false;
        }
    }

    if (formData.ngayCapCccd && dayjs(formData.ngayCapCccd).isAfter(today)) {
        errors.ngayCapCccd = 'Ngày cấp CCCD không hợp lệ';
        ok = false;
    }

    // 5. Nếu các bước trên đã sai thì dừng lại luôn, chưa cần check DB để tiết kiệm tài nguyên
    if (!ok) return false;

    // 6. 🔥 Kiểm tra trùng lặp dữ liệu trong Cơ sở dữ liệu
    try {
        const duplicateChecks = [
            { key: 'tenDangNhap', label: 'Tên đăng nhập' },
            { key: 'email', label: 'Email' },
            { key: 'sdtNhanVien', label: 'Số điện thoại' },
            { key: 'soCccd', label: 'Số CCCD' }
        ];

        for (const item of duplicateChecks) {
            const res = await staffService.checkDuplicate(
                item.key,
                formData[item.key],
                staffId.value
            );

            if (res.data.exists) {
                errors[item.key] = `${item.label} này đã được sử dụng bởi một nhân viên khác`;
                ok = false;
            }
        }
    } catch (e) {
        console.error("Lỗi kiểm tra trùng lặp:", e);
        toast.error("Hệ thống tạm thời không thể kiểm tra dữ liệu trùng. Vui lòng thử lại sau.");
        return false;
    }

    return ok;
};




//////////////////////////////////////////////////////
// SAVE
//////////////////////////////////////////////////////

const handleSave = async () => {
    if (!(await validateForm())) {
        toast.warning("Vui lòng kiểm tra form");
        return;
    }


    try {
        loading.value = true;

        const data = new FormData();

        Object.keys(formData).forEach(key => {
            let value = formData[key];

            if (key === 'gioiTinh')
                value = value ? true : false;

            if (value !== null && value !== undefined && value !== '') {
                data.append(key, value);
            }
        });

        // 🔥 ép kiểu số cho idVaiTro
        data.set('idVaiTro', Number(formData.idVaiTro));

        if (selectedFile.value)
            data.append('hinhAnhFile', selectedFile.value);

        if (staffId.value)
            await staffService.update(staffId.value, data);
        else
            await staffService.create(data);

        toast.success("Lưu thành công!");
        router.push('/admin/staff');

    } catch (e) {
        console.error(e);
        toast.error("Lỗi lưu dữ liệu");
    } finally {
        loading.value = false;
    }
};


//////////////////////////////////////////////////////
// LOAD DATA
//////////////////////////////////////////////////////

onMounted(async () => {
    try {
        const roleRes = await staffService.getActiveRoles();
        listRoles.value = roleRes.data;

        if (staffId.value) {
            const res = await staffService.getDetail(staffId.value);
            Object.assign(formData, res.data);

            formData.idVaiTro = res.data.idVaiTro;
            formData.ngayCapCccd = dayjs(res.data.ngayCapCccd).format('YYYY-MM-DD');
            formData.ngaySinh = dayjs(res.data.ngaySinh).format('YYYY-MM-DD');

            if (res.data.anhDaiDien)
                imagePreview.value = `http://localhost:8080/uploads/images/${res.data.anhDaiDien}`;
        }
    } catch {
        toast.error("Không tải được dữ liệu");
    }
});
</script>


<style scoped>
/* Tổng thể trang */
.staff-form-page {
  font-family: 'Inter', sans-serif;
  color: #333;
}

.text-wine { color: #800000 !important; }

/* Header Icon */
.icon-header-box {
  width: 48px;
  height: 48px;
  background: #800000;
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  box-shadow: 0 4px 12px rgba(128, 0, 0, 0.15);
}

/* Upload Zone (Vùng ảnh) */
.upload-container {
  display: flex;
  justify-content: center;
}

.upload-zone {
  width: 160px;
  height: 160px;
  border: 2px dashed #ced4da;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  background: #fff;
}

.upload-zone:hover {
  border-color: #800000;
  background: #fff5f5;
}

.img-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0;
  transition: 0.3s;
}

.upload-zone:hover .upload-overlay {
  opacity: 1;
}

/* Input Group theo phong cách Client */
.input-group-custom {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid #dee2e6;
  border-radius: 10px;
  transition: all 0.2s;
  background: #fff;
  overflow: hidden;
}

.input-group-custom:focus-within {
  border-color: #800000;
  box-shadow: 0 0 0 3px rgba(128, 0, 0, 0.1);
}

.icon-input {
  width: 45px;
  text-align: center;
  color: #6c757d;
  font-size: 14px;
}

.input-group-custom .form-control, 
.input-group-custom .form-select {
  border: none !important;
  padding: 10px 12px 10px 0;
  font-size: 14px;
  box-shadow: none !important;
  background: transparent !important;
}

/* Custom Input thông thường (Date, Textarea) */
.custom-input {
  border-radius: 10px;
  padding: 10px 12px;
  border: 1px solid #dee2e6;
  font-size: 14px;
}

.custom-input:focus {
  border-color: #800000;
  box-shadow: 0 0 0 3px rgba(128, 0, 0, 0.1);
}

/* Gender Selector (Nút chọn giới tính) */
.gender-selector .btn-outline-wine {
  border: 1px solid #800000;
  color: #800000;
  border-radius: 10px;
  font-size: 14px;
  padding: 8px;
  transition: 0.2s;
}

.gender-selector .btn-check:checked + .btn-outline-wine {
  background-color: #800000;
  border-color: #800000;
  color: #fff;
}

/* Scanner Box */
.scanner-box {
  background-color: #fff5f5;
  border: 1px dashed #800000;
}

.btn-wine-sm {
  background: #800000;
  color: white;
  border: none;
  font-size: 13px;
  font-weight: 600;
  padding: 10px;
  border-radius: 10px;
}

/* Số thứ tự Section */
.badge-number {
  width: 26px;
  height: 26px;
  background: #800000;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  margin-right: 10px;
}

/* Nút bấm chính */
.btn-main-custom {
  background: #800000;
  color: white;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.3s;
}

.btn-main-custom:hover:not(:disabled) {
  background: #600000;
  transform: translateY(-1px);
}

.btn-light-custom {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 10px;
  font-weight: 500;
  color: #666;
}

/* Lỗi validation */
.error-text {
  color: #dc3545;
  font-size: 11px;
  margin-top: 4px;
  font-weight: 500;
  min-height: 16px;
}

.form-label-custom {
  font-size: 13px;
  font-weight: 600;
  color: #495057;
  margin-bottom: 6px;
  display: block;
}

.star { color: #dc3545; }
.tiny { font-size: 11px; }

/* Đường kẻ */
hr.dashed {
  border: none;
  border-top: 1px dashed #dee2e6;
  margin: 25px 0;
}

.border-start-wine {
  border-left: 4px solid #800000 !important;
}

/* QR Scanner Container */
#reader {
  border-radius: 10px;
  overflow: hidden;
}
</style>