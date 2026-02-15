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
                <div class="upload-zone" @click="triggerFileInput" :class="{ 'has-image': previewUrl }">
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
                        <input type="text" class="form-control" :class="{ 'is-invalid': errors.tenKhachHang }"
                          v-model="formData.tenKhachHang" placeholder="Nguyễn Văn A">
                      </div>

                      <div class="error-text">{{ errors.tenKhachHang }}</div>

                    </div>



                    <div class="col-md-6">

                      <label class="form-label-custom">Số điện thoại <span class="star">*</span></label>

                      <div class="input-group-custom">

                        <i class="fas fa-phone-alt icon-input"></i>

                        <input type="text" class="form-control" :class="{ 'is-invalid': errors.soDienThoai }"
                          v-model="formData.soDienThoai" placeholder="09xxxxxxxx">

                      </div>

                      <div class="error-text">{{ errors.soDienThoai }}</div>

                    </div>



                    <div class="col-md-6">

                      <label class="form-label-custom">Email <span class="star">*</span></label>

                      <div class="input-group-custom">

                        <i class="far fa-envelope icon-input"></i>

                        <input type="email" class="form-control" :class="{ 'is-invalid': errors.email }"
                          v-model="formData.email" placeholder="example@gmail.com">

                      </div>

                      <div class="error-text">{{ errors.email }}</div>

                    </div>



                    <div class="col-md-6">

                      <label class="form-label-custom">Ngày sinh <span class="star">*</span></label>

                      <div class="input-group-custom">

                        <i class="far fa-calendar-alt icon-input"></i> <input type="date" class="form-control"
                          :class="{ 'is-invalid': errors.ngaySinh }" v-model="formData.ngaySinh">

                      </div>

                      <div class="error-text">{{ errors.ngaySinh }}</div>

                    </div>



                    <div class="col-md-12"> <label class="form-label-custom">Giới tính <span
                          class="star">*</span></label>

                      <div class="gender-selector d-flex gap-3">

                        <input type="radio" class="btn-check" name="gender" id="male" :value="true"
                          v-model="formData.gioiTinh">

                        <label class="btn btn-outline-wine w-100" for="male"><i class="fas fa-mars me-2"></i>Nam</label>



                        <input type="radio" class="btn-check" name="gender" id="female" :value="false"
                          v-model="formData.gioiTinh">

                        <label class="btn btn-outline-wine w-100" for="female"><i
                            class="fas fa-venus me-2"></i>Nữ</label>

                      </div>

                    </div>

                  </div>

                </div>



                <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4">

                  <div
                    class="card-header bg-white border-0 pt-4 px-4 d-flex justify-content-between align-items-center">

                    <h6 class="fw-bold m-0 text-wine">

                      <i class="fas fa-map-marker-alt me-2"></i>Danh sách địa chỉ nhận hàng

                    </h6>



                    <span v-if="editingIndex !== null" class="badge rounded-pill px-3 py-2"
                      style="background-color: #fff3cd; color: #856404; border: 1px solid #ffeeba;">

                      <i class="fas fa-edit me-1"></i>

                      Đang sửa địa chỉ #{{ editingIndex + 1 }}

                    </span>

                  </div>



                  <div class="card-body p-4">

                    <div class="input-group-custom mb-2">

                      <input v-model="tempAddressText" type="text"
                        class="form-control form-control-lg bg-light border-0 ps-4 rounded-start-pill"
                        :placeholder="editingIndex !== null ? 'Cập nhật địa chỉ...' : 'Nhập địa chỉ mới tại đây...'"
                        @keyup.enter="handleAddressAction">



                      <button @click="handleAddressAction" class="btn rounded-end-pill px-4 text-white"
                        :class="editingIndex !== null ? '' : 'btn-wine'"
                        :style="editingIndex !== null ? 'background-color: #d97706; border-color: #d97706;' : ''"
                        type="button">



                        <i class="fas" :class="editingIndex !== null ? 'fa-check' : 'fa-plus'"></i>



                      </button>

                    </div>



                    <div v-if="editingIndex !== null" class="ms-3 mb-4 d-flex align-items-center">

                      <button @click="cancelEdit" type="button"
                        class="btn btn-sm btn-outline-secondary border-0 d-flex align-items-center gap-1 px-2 py-1 rounded-pill shadow-none"
                        style="font-size: 0.75rem; background-color: #f8f9fa;">

                        <i class="fas fa-undo-alt"></i>

                        <span class="text-decoration-underline">Hủy bỏ cập nhật</span>

                      </button>



                      <small class="text-muted ms-2" style="font-size: 0.7rem; font-style: italic;">

                        (Hệ thống sẽ không lưu thay đổi nếu bạn hủy)

                      </small>

                    </div>



                    <div class="address-list mt-3">

                      <div>

                        <div v-for="(item, index) in formData.danhSachDiaChi" :key="item.id ?? index"
                          class="address-item d-flex align-items-center justify-content-between p-3 mb-2 rounded-3"
                          :class="{ 'border-wine-light bg-wine-subtle': index === defaultIndex }">



                          <div class="d-flex align-items-center overflow-hidden flex-grow-1">

                            <div class="icon-star me-3 cursor-pointer" @click="setDefault(index)"
                              title="Đặt làm mặc định">

                              <i class="fas fa-star"
                                :class="index === defaultIndex ? 'text-warning' : 'text-light-gray'"></i>

                            </div>



                            <div class="d-flex flex-column overflow-hidden">

                              <span class="text-truncate"
                                :class="index === defaultIndex ? 'fw-bold text-wine' : 'text-secondary'">

                                {{ item.thong_tin_dia_chi || 'Địa chỉ không có nội dung' }}

                              </span>



                              <div class="d-flex align-items-center gap-2">

                                <small v-if="index === defaultIndex" class="text-wine tiny fw-bold">Mặc định</small>

                                <small class="text-muted tiny">

                                  - {{ formData.tenKhachHang }} | {{ formData.soDienThoai }}

                                </small>

                              </div>

                            </div>

                          </div>



                          <div class="d-flex gap-2">

                            <button @click="startEdit(index)" type="button"
                              class="btn btn-sm btn-light border rounded-circle shadow-sm">
                              <i class="fas fa-pen text-primary fa-xs"></i>
                            </button>
                            <button @click="removeAddress(index)" type="button"
                              class="btn btn-sm btn-light border rounded-circle shadow-sm">
                              <i class="fas fa-times text-danger fa-xs"></i>
                            </button>
                          </div>
                        </div>
                      </div>
                      <div v-if="!formData.danhSachDiaChi || formData.danhSachDiaChi.length === 0"
                        class="text-center py-4 text-muted opacity-50">
                        <i class="fas fa-map-marked-alt fa-2x mb-2"></i>
                        <p class="small">Chưa có địa chỉ nào được thêm</p>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="d-flex justify-content-between align-items-center pt-4 border-top">
                  <p class="text-muted small mb-0">* Thông tin này sẽ được bảo mật tuyệt đối</p>
                  <div class="d-flex gap-3">
                    <button type="button" class="btn btn-light-custom px-4" @click="handleBack">Hủy bỏ</button>
                    <button type="button" class="btn btn-main-custom px-5 py-2 shadow-sm" @click="handleSave"
                      :disabled="loading">
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
const currentAddress = ref('');
const selectedIndex = ref(0); // Mặc định chọn ô đầu tiên

// Biến kiểm soát trạng thái sửa (null là thêm mới, số là index đang sửa)
const editingIndex = ref(null);
// 1. Dữ liệu chính để gửi lên Backend
const formData = reactive({
  tenKhachHang: '',
  soDienThoai: '',
  email: '',
  tenDangNhap: '',
  matKhauDangNhap: '',
  ngaySinh: '',
  gioiTinh: true,
  trangThai: 1,
  danhSachDiaChi: [] // Nên bắt đầu bằng mảng rỗng để dễ quản lý
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
const tempAddressText = ref('');
const defaultIndex = ref(0);   // Lưu vị trí mặc định (mặc định là cái đầu tiên)
const preparePayload = () => {
  const data = new FormData();
  Object.keys(formData).forEach(key => {
    // CHỈ bỏ qua địa chỉ, KHÔNG bỏ qua anhDaiDien ở đây nữa
    if (key === 'danhSachDiaChi' || key === 'diaChi') return;
    let val = formData[key];
    if (key === 'gioiTinh') val = val ? 1 : 0;
    // Gửi tất cả các trường text lên (bao gồm cả anhDaiDien - tên file cũ)
    if (val !== null && val !== undefined && val !== '') {
      data.append(key, val);
    }
  });

  // Nếu có chọn FILE MỚI, nó sẽ ghi đè/bổ sung vào tham số hinhAnhFile
  if (selectedFile.value) {
    data.append('hinhAnhFile', selectedFile.value);
  }

  // 2. XỬ LÝ ẢNH (Phần quan trọng nhất)
  if (selectedFile.value) {
    // Trường hợp 1: Người dùng chọn FILE MỚI (Gửi binary file)
    data.append('hinhAnhFile', selectedFile.value);
  } else if (clientId.value && formData.anhDaiDien) {
    // Trường hợp 2: Cập nhật và KHÔNG chọn file mới (Gửi lại TÊN FILE CŨ)
    // Để Backend biết vẫn dùng ảnh cũ, không được xóa
    data.append('anhDaiDien', formData.anhDaiDien);
  }

  // 3. Xử lý danh sách địa chỉ
  if (formData.danhSachDiaChi && formData.danhSachDiaChi.length > 0) {
    formData.danhSachDiaChi.forEach((addr, index) => {
      if (addr.id) {
        data.append(`danhSachDiaChi[${index}].id`, addr.id);
      }

      const addressText = addr.thong_tin_dia_chi || addr.thongTinDiaChi;
      data.append(`danhSachDiaChi[${index}].thongTinDiaChi`, addressText || '');

      const isDefault = addr.la_mac_dinh === true || addr.laMacDinh === true;
      data.append(`danhSachDiaChi[${index}].laMacDinh`, isDefault);

      data.append(`danhSachDiaChi[${index}].hoTenNhan`, formData.tenKhachHang || '');
      data.append(`danhSachDiaChi[${index}].soDienThoaiNhan`, formData.soDienThoai || '');
    });
  }

  return data;
};

const submitClient = async (payload) => {

  if (clientId.value) {

    await clientService.update(clientId.value, payload);

  } else {

    await clientService.create(payload);

  }

};

// Hàm xử lý chung cho cả Thêm và Cập nhật

const handleAddressAction = () => {

  const text = tempAddressText.value.trim();

  if (!text) {

    toast.warning("Vui lòng nhập địa chỉ!");

    return;

  }



  if (editingIndex.value !== null) {

    // Cập nhật trường text trong Object hiện tại

    formData.danhSachDiaChi[editingIndex.value].thong_tin_dia_chi = text;

    toast.info("Đã cập nhật địa chỉ");

    editingIndex.value = null;

  } else {

    // Thêm mới một Object chuẩn cấu trúc Backend mong đợi

    formData.danhSachDiaChi.push({

      id: null, // id null để Backend biết đây là thêm mới

      thong_tin_dia_chi: text,

      la_mac_dinh: formData.danhSachDiaChi.length === 0 // Tự động mặc định nếu là cái đầu

    });

    if (formData.danhSachDiaChi.length === 1) defaultIndex.value = 0;

  }

  tempAddressText.value = '';



  console.log("Danh sách địa chỉ:", JSON.stringify(formData.danhSachDiaChi, null, 2));



};



// Bắt đầu sửa

const startEdit = (index) => {

  editingIndex.value = index;

  tempAddressText.value =

    formData.danhSachDiaChi[index].thong_tin_dia_chi;

};





// Hủy sửa

const cancelEdit = () => {

  editingIndex.value = null;

  tempAddressText.value = '';

};



// Chọn địa chỉ mặc định

const setDefault = (index) => {

  // 1. Cập nhật vị trí index mặc định để đổi màu ngôi sao trên giao diện

  defaultIndex.value = index;



  // 2. Quan trọng: Cập nhật biến la_mac_dinh trong mảng để preparePayload gửi đi đúng

  formData.danhSachDiaChi.forEach((addr, i) => {

    addr.la_mac_dinh = (i === index);

  });



  // (Tùy chọn) Hiện thông báo nhỏ cho người dùng biết

  // toast.info("Đã đổi địa chỉ mặc định");

};

// Xóa địa chỉ

const removeAddress = (index) => {

  formData.danhSachDiaChi.splice(index, 1);

  // Nếu xóa địa chỉ mặc định, reset về cái đầu tiên

  if (index === defaultIndex.value) {

    defaultIndex.value = 0;

  } else if (index < defaultIndex.value) {

    // Nếu xóa phần tử nằm trước phần tử mặc định, cần giảm index mặc định xuống

    defaultIndex.value--;

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
  ];

  requiredFields.forEach(field => {
    if (!formData[field.key] || formData[field.key].toString().trim() === '') {
      errors[field.key] = field.msg;
      ok = false;
    }
  });

  // Kiểm tra danh sách địa chỉ (Báo lỗi vào errors.diaChi)
  if (!formData.danhSachDiaChi || formData.danhSachDiaChi.length === 0) {
    errors.diaChi = 'Vui lòng thêm ít nhất một địa chỉ nhận hàng';
    ok = false;
  }

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

  // Nếu đã có lỗi từ các bước kiểm tra cơ bản, dừng lại để hiện thông báo đỏ ngay
  if (!ok) return false;

  // 4. KIỂM TRA TRÙNG LẶP QUA API (Backend)
  try {
    const checks = [
      { key: 'soDienThoai', label: 'Số điện thoại' },
      { key: 'email', label: 'Email' }
    ];

    // Chỉ kiểm tra tên đăng nhập khi thêm mới
    if (!clientId.value) {
      checks.push({ key: 'tenDangNhap', label: 'Tên đăng nhập' });
    }

    // Chạy vòng lặp kiểm tra trùng lặp
    for (const item of checks) {
      const val = formData[item.key];
      if (val) {
        const res = await clientService.checkDuplicate(item.key, val, clientId.value);
        if (res.data.exists) {
          errors[item.key] = `${item.label} này đã được sử dụng trên hệ thống`;
          ok = false;
        }
      }
    }
  } catch (e) {
    console.error("Lỗi khi kiểm tra trùng lặp:", e);
    // Không chặn việc lưu nếu API check trùng gặp lỗi hệ thống (tùy bạn quyết định)
  }

  return ok;
};

const generateRandomPassword = (length = 8) => {
  const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let retVal = "";
  for (let i = 0, n = charset.length; i < length; ++i) {
    retVal += charset.charAt(Math.floor(Math.random() * n));
  }
  return retVal;
};


const handleSave = async () => {
  // 1. Kiểm tra tính hợp lệ (Validate FE)
  const isValid = await validateForm();
  if (!isValid) {
    // Hiện thông báo cảnh báo nếu có lỗi đỏ
    Swal.fire({
      ...swalConfig,
      title: 'Thông tin chưa hợp lệ',
      text: 'Vui lòng kiểm tra lại các trường bị báo đỏ!',
      icon: 'warning',
      confirmButtonText: 'Đã hiểu'
    });
    return;
  }

  // 2. Gán giá trị ngầm (Chỉ cho thêm mới)
  if (!clientId.value) {
    formData.tenDangNhap = formData.soDienThoai;
    if (!formData.matKhauDangNhap) {
      formData.matKhauDangNhap = generateRandomPassword(10);
    }
  }

  // 3. Hộp thoại xác nhận hành động
  const result = await Swal.fire({
    ...swalConfig,
    title: clientId.value ? 'Cập nhật khách hàng?' : 'Xác nhận thêm mới?',
    text: clientId.value
      ? 'Bạn có chắc chắn muốn lưu các thay đổi này?'
      : 'Hệ thống sẽ tạo tài khoản và gửi thông tin qua Email khách hàng.',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy bỏ'
  });

  if (!result.isConfirmed) return;

  try {
    loading.value = true;
    const payload = preparePayload();

    await submitClient(payload);

    // 4. Thông báo thành công (Hiện giữa màn hình và tự đóng sau 2s)
    await Swal.fire({
      ...swalConfig,
      title: 'Thành công!',
      text: clientId.value ? 'Thông tin khách hàng đã được cập nhật.' : 'Đã thêm khách hàng mới thành công.',
      icon: 'success',
      timer: 2000,
      showConfirmButton: false
    });

    router.push('/admin/client');

  } catch (e) {
    console.error("Chi tiết lỗi:", e.response?.data);
    
    // 5. Thông báo lỗi hệ thống (Nếu trùng lặp hoặc lỗi server)
    const errorMsg = e.response?.data?.message || "Đã có lỗi xảy ra trong quá trình xử lý dữ liệu.";
    Swal.fire({
      ...swalConfig,
      title: 'Thao tác thất bại',
      text: errorMsg,
      icon: 'error',
      confirmButtonText: 'Quay lại'
    });
  } finally {
    loading.value = false;
  }
};


onMounted(async () => {
  if (clientId.value) {
    try {
      loading.value = true;
      const res = await clientService.getDetail(clientId.value);
      const data = res.data;

      // Log để debug: Kiểm tra xem data trả về có trường anhDaiDien không
      console.log("Dữ liệu khách hàng nhận về:", data);

      // 1. Tách địa chỉ và các thông tin khác
      const { danhSachDiaChi, ...rest } = data;

      // 2. Gán các thông tin cơ bản vào formData
      Object.assign(formData, rest);

      // 3. Xử lý hiển thị ảnh đại diện
      // Thử lấy cả 2 trường hợp camelCase và snake_case cho chắc chắn
      const fileName = data.anhDaiDien || data.anh_dai_dien;

      if (fileName) {
        // Gán vào previewUrl để hiển thị trên giao diện
        previewUrl.value = `http://localhost:8080/uploads/customers/${fileName}`;

        // Gán lại vào formData để nếu không thay đổi ảnh thì Backend vẫn giữ ảnh cũ
        formData.anhDaiDien = fileName;

        console.log("Đường dẫn ảnh preview:", previewUrl.value);
      } else {
        previewUrl.value = null; // Hoặc một ảnh mặc định nếu muốn
      }

      // 4. Xử lý danh sách địa chỉ
      if (danhSachDiaChi && danhSachDiaChi.length > 0) {
        formData.danhSachDiaChi = danhSachDiaChi.map(d => ({
          id: d.id,
          // Map từ CamelCase của Java sang snake_case của giao diện Vue
          thong_tin_dia_chi: d.thongTinDiaChi,
          la_mac_dinh: d.laMacDinh === true
        }));

        // Tìm vị trí địa chỉ mặc định để hiển thị ngôi sao vàng
        const idx = formData.danhSachDiaChi.findIndex(d => d.la_mac_dinh === true);
        defaultIndex.value = idx !== -1 ? idx : 0;
      }

      // 5. Định dạng ngày sinh để hiển thị lên thẻ <input type="date">
      if (data.ngaySinh) {
        formData.ngaySinh = dayjs(data.ngaySinh).format('YYYY-MM-DD');
      }

    } catch (e) {
      console.error("Lỗi khi tải chi tiết khách hàng:", e);
      toast.error("Không tải được thông tin khách hàng");
    } finally {
      loading.value = false;
    }
  }
});

</script>

<style scoped>
/* Màu chủ đạo */
.text-wine {
  color: #800000;
}

.bg-light-wine {
  background-color: #fff8f8;
}
.border-start-wine {
  border-left: 4px solid #800000;
}
.bg-gold {
  background-color: #ffc107;
}

/* Icon Header */

.icon-header-box {
  width: 50px;
  height: 50px;
  background: #800000;
  color: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}
.upload-zone {
  width: 100%;
  aspect-ratio: 1/1;
  border: 2px dashed #ddd;
  border-radius: 20px;
  overflow: hidden;
  position: relative;
  cursor: pointer;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.3s;
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

  top: 0;

  left: 0;

  width: 100%;

  height: 100%;

  background: rgba(128, 0, 0, 0.7);

  color: white;

  display: flex;

  flex-direction: column;

  align-items: center;

  justify-content: center;

  opacity: 0;

  transition: 0.3s;

}



.upload-zone:hover .upload-overlay {

  opacity: 1;

}



.icon-circle {

  width: 60px;

  height: 60px;

  background: white;

  border-radius: 50%;

  display: flex;

  align-items: center;

  justify-content: center;

  margin: 0 auto;

  color: #800000;

  font-size: 24px;

  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);

}



/* Form Input Custom */

.form-label-custom {

  font-weight: 700;

  font-size: 13px;

  color: #444;

  text-transform: uppercase;

  margin-bottom: 8px;

  display: block;

}



.input-group-custom {

  position: relative;

}



.icon-input {

  position: absolute;

  left: 15px;

  top: 50%;

  transform: translateY(-50%);

  color: #999;

  z-index: 5;

}



.input-group-custom .form-control {

  padding-left: 45px;

  height: 48px;

  border-radius: 10px;

  border: 1px solid #e0e0e0;

}



.custom-input {

  border-radius: 10px;

  border: 1px solid #e0e0e0;

  padding: 10px 15px;

}



.form-control:focus {

  border-color: #800000;

  box-shadow: 0 0 0 4px rgba(128, 0, 0, 0.1);

}



/* Badge Number */

.badge-number {

  width: 32px;

  height: 32px;

  background: #800000;

  color: white;

  border-radius: 50%;

  display: flex;

  align-items: center;

  justify-content: center;

  font-weight: bold;

  margin-right: 15px;

  flex-shrink: 0;

}



/* Nút giới tính kiểu hiện đại */

.btn-outline-wine {

  color: #800000;

  border-color: #800000;

  border-radius: 10px;

  padding: 10px;

}



.btn-check:checked+.btn-outline-wine {

  background-color: #800000;

  color: white;

}



/* Buttons */

.btn-main-custom {

  background: #800000;

  color: white;

  border-radius: 10px;

  font-weight: 600;

  transition: 0.3s;

}



.btn-main-custom:hover:not(:disabled) {

  background: #600000;

  transform: translateY(-2px);

}



.text-wine {

  color: #800000;

}



.btn-wine {

  background: #800000;

  color: white;

  border: none;

}



.btn-wine:hover {

  background: #600000;

}



.input-group-custom {

  display: flex;

  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);

  border-radius: 50px;

}



.address-item {

  background: #fdfdfd;

  border: 1px solid #f0f0f0;

  transition: all 0.3s ease;

}



.address-item:hover {

  background: #fff;

  border-color: #800000;

  transform: translateX(5px);

  box-shadow: 0 4px 12px rgba(128, 0, 0, 0.08);

}



.icon-map-pin {

  width: 32px;

  height: 32px;

  background: #fff0f0;

  display: flex;

  align-items: center;

  justify-content: center;

  border-radius: 50%;

  flex-shrink: 0;

}



/* Hiệu ứng mượt mà khi thêm/xóa */

.list-enter-active,

.list-leave-active {

  transition: all 0.4s ease;

}



.list-enter-from,

.list-leave-to {

  opacity: 0;

  transform: translateY(20px);

}



.btn-light-custom {

  background: #f0f0f0;

  color: #555;

  border-radius: 10px;

  font-weight: 600;

}



.error-text {

  color: #dc3545;

  font-size: 12px;

  margin-top: 5px;

  min-height: 18px;

  font-weight: 500;

}



.star {

  color: #ff4d4f;

}



.dashed {

  border-top: 1px dashed #ddd;

  margin: 25px 0;

}



.tiny {

  font-size: 11px;

}



.border-wine {

  border: 2px solid #800000 !important;

}



.text-wine {

  color: #800000 !important;

}



.cursor-pointer {

  cursor: pointer;

}



.transition-all {

  transition: all 0.2s ease-in-out;

}



.hover-scale:hover {

  transform: scale(1.2);

}



.border-dashed {

  border: 2px dashed #dee2e6 !important;

}



/* Hiệu ứng làm mờ các ô không được chọn để nổi bật ô được chọn */

.opacity-75:hover {

  opacity: 1;

  border-color: #dee2e6 !important;

}



/* Hiệu ứng cho danh sách địa chỉ */

.text-wine {

  color: #800000;

}



.btn-wine {

  background: #800000;

  color: white;

}



.bg-wine-subtle {

  background-color: #fff5f5 !important;

}



.border-wine-light {

  border: 1px solid #ffcccc !important;

}



.text-light-gray {

  color: #dee2e6;

}



.cursor-pointer {

  cursor: pointer;

}



.tiny {

  font-size: 0.7rem;

}



.address-item {

  transition: all 0.3s ease;

  border: 1px solid #f0f0f0;

}



.address-item:hover {

  transform: translateX(8px);

  border-color: #800000;

  background-color: white;

  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);

}



.icon-star i {

  transition: transform 0.2s;

}



.icon-star:hover i {

  transform: scale(1.3);

}
</style>