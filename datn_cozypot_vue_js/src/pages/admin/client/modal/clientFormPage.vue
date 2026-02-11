<template>
  <div class="client-form-page bg-light min-vh-100 p-4">
    <div class="container mb-4">
      <div class="d-flex justify-content-between align-items-center">
        <div class="d-flex align-items-center gap-3">
          <div class="icon-header-box">
             <i class="fas" :class="clientId ? 'fa-user-edit' : 'fa-user-plus'"></i>
          </div>
          <div>
            <h4 class="title-page text-wine fw-bold m-0">
              {{ clientId ? 'Cập nhật thông tin khách hàng' : 'Thêm khách hàng mới' }}
            </h4>
          </div>
        </div>
        <button class="btn btn-outline-secondary rounded-pill px-4" @click="handleBack">
          <i class="fas fa-arrow-left me-2"></i> Quay lại danh sách
        </button>
      </div>
    </div>

    <div class="container">
      <div class="row g-4">
        <div class="col-lg-4">
          <div class="card shadow-sm border-0 rounded-4 sticky-top" style="top: 20px;">
            <div class="card-body p-4">
              <h6 class="form-label-custom mb-4 text-center">Ảnh đại diện</h6>
              
              <div class="upload-container mb-4">
                <div class="upload-zone" @click="triggerFileInput" :class="{'has-image': previewUrl}">
                  <template v-if="previewUrl">
                    <img :src="previewUrl" alt="Avatar" class="img-preview" />
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
                      <span class="text-muted tiny">JPG, PNG, WEBP (Max 2MB)</span>
                    </div>
                  </template>
                </div>
                <input type="file" ref="fileInput" class="d-none" @change="onFileChange" accept="image/*" />
              </div>

              <hr class="dashed">

              <div v-if="clientId" class="client-stats mb-4">
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted small">Mã khách hàng:</span>
                  <span class="fw-bold text-wine">{{ formData.maKhachHang || '---' }}</span>
                </div>
                <div class="d-flex justify-content-between mb-2">
                  <span class="text-muted small">Điểm tích lũy:</span>
                  <span class="badge bg-gold text-dark fw-bold">{{ formData.diemTichLuy || 0 }} điểm</span>
                </div>
                <div class="d-flex justify-content-between">
                  <span class="text-muted small">Ngày tham gia:</span>
                  <span class="small">{{ dayjs(formData.ngayTaoTaiKhoan).format('DD/MM/YYYY') }}</span>
                </div>
              </div>

              <div class="info-alert p-3 rounded-3 bg-light-wine border-start-wine">
                <p class="small text-muted mb-0">
                  <i class="fas fa-info-circle text-wine me-2"></i>
                  Lưu ý: Các trường đánh dấu <span class="star">*</span> bắt buộc phải nhập đầy đủ.
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
                    <h5 class="m-0 fw-bold">Thông tin định danh</h5>
                  </div>
                  
                  <div class="row g-4">
                    <div class="col-md-6">
                      <label class="form-label-custom">Họ và tên <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="far fa-user icon-input"></i>
                        <input type="text" class="form-control" :class="{'is-invalid': errors.tenKhachHang}" 
                               v-model="formData.tenKhachHang" placeholder="Nguyễn Văn A">
                      </div>
                      <div class="error-text">{{ errors.tenKhachHang }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Số điện thoại <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="fas fa-phone-alt icon-input"></i>
                        <input type="text" class="form-control" :class="{'is-invalid': errors.soDienThoai}" 
                               v-model="formData.soDienThoai" placeholder="09xxxxxxxx">
                      </div>
                      <div class="error-text">{{ errors.soDienThoai }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Ngày sinh <span class="star">*</span></label>
                      <input type="date" class="form-control custom-input" :class="{'is-invalid': errors.ngaySinh}" 
                             v-model="formData.ngaySinh">
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

                <div class="form-section mb-5">
                  <div class="d-flex align-items-center mb-4">
                    <span class="badge-number">2</span>
                    <h5 class="m-0 fw-bold">Tài khoản & Địa chỉ</h5>
                  </div>

                  <div class="row g-4">
                    <div class="col-12">
                      <label class="form-label-custom">Địa chỉ Email <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="far fa-envelope icon-input"></i>
                        <input type="email" class="form-control" :class="{'is-invalid': errors.email}" 
                               v-model="formData.email" placeholder="example@gmail.com">
                      </div>
                      <div class="error-text">{{ errors.email }}</div>
                    </div>

                    <div class="col-12">
                      <label class="form-label-custom">Địa chỉ thường trú <span class="star">*</span></label>
                      <textarea class="form-control custom-input" rows="2" :class="{'is-invalid': errors.diaChi}" 
                                v-model="formData.diaChi" placeholder="Số nhà, đường, phường/xã..."></textarea>
                      <div class="error-text">{{ errors.diaChi }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Tên đăng nhập <span class="star">*</span></label>
                      <input type="text" class="form-control custom-input bg-light" v-model="formData.tenDangNhap" 
                             :readonly="!!clientId" placeholder="username">
                      <div class="error-text">{{ errors.tenDangNhap }}</div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Mật khẩu <span v-if="!clientId" class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="fas fa-lock icon-input"></i>
                        <input v-if="!clientId" type="password" class="form-control" :class="{'is-invalid': errors.matKhauDangNhap}"
                               v-model="formData.matKhauDangNhap" placeholder="••••••••">
                        <input v-else type="text" class="form-control bg-light" value="********" readonly>
                      </div>
                      <div class="error-text">{{ errors.matKhauDangNhap }}</div>
                    </div>

                    <div class="col-md-6" v-if="clientId">
                       <label class="form-label-custom">Trạng thái tài khoản</label>
                       <select class="form-select custom-input" v-model="formData.trangThai">
                         <option :value="1">Đang hoạt động</option>
                         <option :value="0">Ngừng hoạt động</option>
                       </select>
                    </div>
                  </div>
                </div>

                <div class="d-flex justify-content-between align-items-center pt-4 border-top">
                  <p class="text-muted small mb-0">* Thông tin này sẽ được bảo mật tuyệt đối</p>
                  <div class="d-flex gap-3">
                    <button type="button" class="btn btn-light-custom px-4" @click="handleBack">Hủy bỏ</button>
                    <button type="button" class="btn btn-main-custom px-5 py-2 shadow-sm" @click="handleSave" :disabled="loading">
                      <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
                      {{ clientId ? 'Cập nhật ngay' : 'Tạo tài khoản khách' }}
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
import { reactive, ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import clientService from '@/services/clientService';
import dayjs from 'dayjs';
import { useToast } from "vue-toastification";
import Swal from 'sweetalert2';

const route = useRoute();
const router = useRouter();
const toast = useToast();

const clientId = ref(route.params.id || null);
const loading = ref(false);
const fileInput = ref(null);
const selectedFile = ref(null);
const previewUrl = ref(null);

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
    tenKhachHang: '', soDienThoai: '', email: '',
    tenDangNhap: '', matKhauDangNhap: '', ngaySinh: '', diaChi: ''
});

// Cấu hình Swal chung giống nhân viên
const swalConfig = {
    confirmButtonColor: '#800000',
    cancelButtonColor: '#6c757d',
};

// Theo dõi thay đổi để xóa báo đỏ
watch(() => ({ ...formData }), (val) => {
    Object.keys(val).forEach(key => {
        if (val[key] && errors[key]) errors[key] = '';
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

const handleBack = () => {
    if (Object.values(formData).some(x => x !== '' && x !== true)) {
        Swal.fire({
            ...swalConfig,
            title: 'Xác nhận thoát?',
            text: "Các thay đổi chưa lưu sẽ bị mất!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Đồng ý',
            cancelButtonText: 'Ở lại'
        }).then((result) => {
            if (result.isConfirmed) router.push('/admin/client');
        });
    } else {
        router.push('/admin/client');
    }
};

const validateForm = async () => {
    let ok = true;
    const today = dayjs();

    // 1. Reset toàn bộ thông báo lỗi cũ
    Object.keys(errors).forEach(k => errors[k] = '');

    // 2. Kiểm tra các trường không được để trống (Required)
    const requiredFields = [
        { key: 'tenKhachHang', msg: 'Vui lòng nhập họ và tên khách hàng' },
        { key: 'soDienThoai', msg: 'Vui lòng nhập số điện thoại liên lạc' },
        { key: 'email', msg: 'Vui lòng nhập địa chỉ email' },
        { key: 'ngaySinh', msg: 'Vui lòng chọn ngày tháng năm sinh' },
        { key: 'diaChi', msg: 'Vui lòng nhập địa chỉ thường trú' },
    ];

    if (!clientId.value) {
        requiredFields.push({ key: 'tenDangNhap', msg: 'Vui lòng thiết lập tên đăng nhập' });
        requiredFields.push({ key: 'matKhauDangNhap', msg: 'Vui lòng tạo mật khẩu cho tài khoản' });
    }

    requiredFields.forEach(field => {
        if (!formData[field.key] || formData[field.key].toString().trim() === '') {
            errors[field.key] = field.msg;
            ok = false;
        }
    });

    // 3. Kiểm tra định dạng (Format) và Logic ngày sinh
    if (formData.soDienThoai && !/^0\d{9}$/.test(formData.soDienThoai)) {
        errors.soDienThoai = 'Số điện thoại không hợp lệ (phải gồm 10 chữ số)';
        ok = false;
    }

    if (formData.email && !/^\S+@\S+\.\S+$/.test(formData.email)) {
        errors.email = 'Địa chỉ email không đúng định dạng';
        ok = false;
    }

    if (formData.ngaySinh && dayjs(formData.ngaySinh).isAfter(today)) {
        errors.ngaySinh = 'Ngày sinh không thể lớn hơn ngày hiện tại';
        ok = false;
    }

    // Nếu các lỗi cơ bản ở trên đã có (ok = false), dừng lại luôn để người dùng sửa
    if (!ok) return false;

    // 4. KIỂM TRA TRÙNG LẶP (Đây là phần bạn cần)
    // Khi nhấn nút, code sẽ chạy xuống đây để gọi API check từng trường
    try {
        const checks = [
            { key: 'soDienThoai', label: 'Số điện thoại' },
            { key: 'email', label: 'Email' }
        ];
        
        if (!clientId.value) {
            checks.push({ key: 'tenDangNhap', label: 'Tên đăng nhập' });
        }

        for (const item of checks) {
            // Gọi lên server để kiểm tra giá trị đã tồn tại chưa
            const res = await clientService.checkDuplicate(item.key, formData[item.key], clientId.value);
            
            if (res.data.exists) {
                // Nếu trùng, gán thông báo vào object errors
                // Thông báo này sẽ hiển thị ngay dưới ô input nhờ vào <div class="error-text">
                errors[item.key] = `${item.label} này đã được sử dụng, vui lòng nhập lại`;
                ok = false;
            }
        }
    } catch (e) {
        console.error("Lỗi khi kiểm tra trùng lặp:", e);
    }

    return ok; 
};

const handleSave = async () => {
    if (!(await validateForm())) {
        toast.warning("Vui lòng hoàn thiện thông tin!");
        return;
    }

    const result = await Swal.fire({
        ...swalConfig,
        title: clientId.value ? 'Cập nhật khách hàng?' : 'Xác nhận thêm mới?',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Xác nhận',
        cancelButtonText: 'Hủy'
    });

    if (!result.isConfirmed) return;

    try {
        loading.value = true;
        const data = new FormData();
        Object.keys(formData).forEach(key => {
            let val = formData[key];
            if (key === 'gioiTinh') val = val ? 1 : 0;
            data.append(key, val);
        });

        if (selectedFile.value) data.append('hinhAnhFile', selectedFile.value);

        if (clientId.value) await clientService.update(clientId.value, data);
        else await clientService.create(data);

        toast.success("Thao tác thành công!");
        router.push('/admin/client');
    } catch (e) {
        toast.error("Đã xảy ra lỗi khi lưu dữ liệu");
    } finally {
        loading.value = false;
    }
};

onMounted(async () => {
    if (clientId.value) {
        try {
            const res = await clientService.getDetail(clientId.value);
            Object.assign(formData, res.data);
            formData.ngaySinh = dayjs(res.data.ngaySinh).format('YYYY-MM-DD');
            if (res.data.anhDaiDien) {
                previewUrl.value = `http://localhost:8080/uploads/customers/${res.data.anhDaiDien}`;
            }
        } catch {
            toast.error("Không tải được thông tin khách hàng");
        }
    }
});
</script>

<style scoped>
/* Màu chủ đạo */
.text-wine { color: #800000; }
.bg-light-wine { background-color: #fff8f8; }
.border-start-wine { border-left: 4px solid #800000; }
.bg-gold { background-color: #ffc107; }

/* Icon Header */
.icon-header-box {
  width: 50px; height: 50px;
  background: #800000; color: white;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px;
}

/* Khung Ảnh Modern */
.upload-zone {
  width: 100%; aspect-ratio: 1/1;
  border: 2px dashed #ddd; border-radius: 20px;
  overflow: hidden; position: relative; cursor: pointer;
  background: #fafafa; display: flex; align-items: center; justify-content: center;
  transition: 0.3s;
}
.upload-zone:hover { border-color: #800000; background: #fff5f5; }
.img-preview { width: 100%; height: 100%; object-fit: cover; }
.upload-overlay {
  position: absolute; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(128, 0, 0, 0.7); color: white;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  opacity: 0; transition: 0.3s;
}
.upload-zone:hover .upload-overlay { opacity: 1; }
.icon-circle {
  width: 60px; height: 60px; background: white; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto; color: #800000; font-size: 24px; box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}

/* Form Input Custom */
.form-label-custom { font-weight: 700; font-size: 13px; color: #444; text-transform: uppercase; margin-bottom: 8px; display: block; }
.input-group-custom { position: relative; }
.icon-input { position: absolute; left: 15px; top: 50%; transform: translateY(-50%); color: #999; z-index: 5; }
.input-group-custom .form-control { padding-left: 45px; height: 48px; border-radius: 10px; border: 1px solid #e0e0e0; }
.custom-input { border-radius: 10px; border: 1px solid #e0e0e0; padding: 10px 15px; }
.form-control:focus { border-color: #800000; box-shadow: 0 0 0 4px rgba(128, 0, 0, 0.1); }

/* Badge Number */
.badge-number {
  width: 32px; height: 32px; background: #800000; color: white;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-weight: bold; margin-right: 15px; flex-shrink: 0;
}

/* Nút giới tính kiểu hiện đại */
.btn-outline-wine { color: #800000; border-color: #800000; border-radius: 10px; padding: 10px; }
.btn-check:checked + .btn-outline-wine { background-color: #800000; color: white; }

/* Buttons */
.btn-main-custom { background: #800000; color: white; border-radius: 10px; font-weight: 600; transition: 0.3s; }
.btn-main-custom:hover:not(:disabled) { background: #600000; transform: translateY(-2px); }
.btn-light-custom { background: #f0f0f0; color: #555; border-radius: 10px; font-weight: 600; }

.error-text { color: #dc3545; font-size: 12px; margin-top: 5px; min-height: 18px; font-weight: 500; }
.star { color: #ff4d4f; }
.dashed { border-top: 1px dashed #ddd; margin: 25px 0; }
.tiny { font-size: 11px; }
</style>