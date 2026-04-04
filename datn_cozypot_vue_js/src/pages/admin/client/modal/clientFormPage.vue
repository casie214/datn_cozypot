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
        <div class="col-lg-12">
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
                      <label class="form-label-custom">Giới tính <span class="star">*</span></label>

                      <div class="d-flex gap-4 align-items-center">

                        <div class="form-check">
                          <input class="form-check-input" type="radio" name="gender" id="male" :value="true"
                            v-model="formData.gioiTinh">
                          <label class="form-check-label" for="male">
                            Nam
                          </label>
                        </div>
                        <div class="form-check">
                          <input class="form-check-input" type="radio" name="gender" id="female" :value="false"
                            v-model="formData.gioiTinh">
                          <label class="form-check-label" for="female">
                            Nữ
                          </label>
                        </div>
                      </div>
                    </div>

                    <div class="col-md-6">
                      <label class="form-label-custom">Ngày sinh <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="far fa-calendar-alt icon-input"></i>
                        <input type="date" class="form-control" :class="{ 'is-invalid': errors.ngaySinh }"
                          v-model="formData.ngaySinh" :max="todayDate" @change="validateNgaySinh" />
                      </div>
                      <div class="error-text">{{ errors.ngaySinh }}</div>
                    </div>

                    <div class="col-md-12">
                      <label class="form-label-custom">Email <span class="star">*</span></label>
                      <div class="input-group-custom">
                        <i class="far fa-envelope icon-input"></i>
                        <input type="email" class="form-control" :class="{ 'is-invalid': errors.email }"
                          v-model="formData.email" placeholder="example@gmail.com">
                      </div>
                      <div class="error-text">{{ errors.email }}</div>
                    </div>
                  </div>
                </div>

                <div class="card border-0 shadow-sm rounded-4 overflow-hidden mb-4" style="background-color: #fdfafb;">
                  <div class="card-header bg-transparent border-0 pt-4 px-4">
                    <h6 class="fw-bold m-0 text-wine">
                      <i class="fas fa-map-marker-alt me-2"></i>Danh sách địa chỉ
                    </h6>
                  </div>

                  <div class="card-body p-4">
                    <div class="address-list">
                      <div v-for="(item, index) in formData.danhSachDiaChi" :key="item.id_dia_chi ?? index"
                        class="address-card border rounded-4 p-4 mb-3 position-relative"
                        :style="index === defaultIndex ? 'background-color: #fdf2f2; border: 1.5px solid #800000 !important;' : 'background-color: #fff; border: 1px solid #eee;'">

                        <span v-if="index === defaultIndex"
                          class="badge position-absolute top-0 end-0 m-3 bg-wine px-3 py-2">
                          <i class="fas fa-check-circle me-1"></i> Mặc định
                        </span>

                        <div class="row g-3">
                          <div class="col-md-4">
                            <label class="small fw-bold text-secondary">Tỉnh/Thành phố *</label>
                            <Multiselect v-model="item.id_tinh_thanh" :options="listTinhThanh"
                              placeholder="Chọn Tỉnh/Thành" :searchable="true" :canClear="false"
                              class="custom-filter-multiselect"
                              :class="{ 'is-invalid-multi': item.errors?.id_tinh_thanh }"
                              @change="onProvinceChange($event, index)" />
                            <div class="error-text mt-1" v-if="item.errors?.id_tinh_thanh">{{ item.errors?.id_tinh_thanh
                            }}</div>
                          </div>

                          <div class="col-md-4">
                            <label class="small fw-bold text-secondary">Quận/Huyện *</label>
                            <Multiselect v-model="item.id_quan_huyen" :options="item.listHuyen"
                              placeholder="Chọn Quận/Huyện" :searchable="true" :canClear="false"
                              :disabled="!item.id_tinh_thanh" class="custom-filter-multiselect"
                              :class="{ 'is-invalid-multi': item.errors?.id_quan_huyen }"
                              @change="onDistrictChange($event, index)" />
                            <div class="error-text mt-1" v-if="item.errors?.id_quan_huyen">{{ item.errors?.id_quan_huyen
                            }}</div>
                          </div>

                          <div class="col-md-4">
                            <label class="small fw-bold text-secondary">Phường/Xã *</label>
                            <Multiselect v-model="item.id_phuong_xa" :options="item.listXa" placeholder="Chọn Phường/Xã"
                              :searchable="true" :canClear="false" :disabled="!item.id_quan_huyen"
                              class="custom-filter-multiselect"
                              :class="{ 'is-invalid-multi': item.errors?.id_phuong_xa }"
                              @change="onWardChange($event, index)" />
                            <div class="error-text mt-1" v-if="item.errors?.id_phuong_xa">{{ item.errors?.id_phuong_xa
                            }}</div>
                          </div>

                          <div class="col-12">
                            <label class="small fw-bold text-secondary">Địa chỉ cụ thể *</label>
                            <textarea v-model="item.dia_chi_chi_tiet" class="form-control shadow-none"
                              :class="{ 'is-invalid': item.errors?.dia_chi_chi_tiet }" rows="2"></textarea>
                            <div class="invalid-feedback">{{ item.errors?.dia_chi_chi_tiet }}</div>
                          </div>
                        </div>


                        <div class="d-flex justify-content-between align-items-center mt-3 pt-3 border-top">
                          <div class="form-check form-switch d-flex align-items-center gap-2">
                            <input class="form-check-input switch-wine shadow-none" type="checkbox"
                              :checked="index === defaultIndex" @change="setDefault(index)">
                            <label class="small text-muted mb-0">Đặt làm địa chỉ chính</label>
                          </div>
                          <button @click="removeAddress(index)"
                            class="btn btn-link text-danger p-0 text-decoration-none small">
                            <i class="fas fa-trash-alt me-1"></i> Xóa địa chỉ
                          </button>
                        </div>
                      </div>

                      <div v-if="!formData.danhSachDiaChi || formData.danhSachDiaChi.length === 0"
                        class="text-center py-5">
                        <i class="fas fa-map-location-dot fa-3x mb-3 text-wine opacity-25"></i>
                        <p class="text-muted">Bạn chưa thêm địa chỉ nào</p>
                      </div>

                      <div class="text-center mt-4">
                        <button @click="addAddress" type="button"
                          class="btn btn-outline-wine rounded-pill px-4 fw-bold">
                          <i class="fas fa-plus-circle me-2"></i>Thêm địa chỉ mới
                        </button>
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
import { reactive, ref, onMounted, watch, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import clientService from '@/services/clientService';
import dayjs from 'dayjs';
import { useToast } from "vue-toastification";
import Swal from 'sweetalert2';
import axios from 'axios';
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';
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
const editingIndex = ref(null);
const todayDate = dayjs().subtract(16, "year").format("YYYY-MM-DD");
const listTinhThanh = ref([]);
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


// Sửa lại hàm addAddress của bạn một chút để kích hoạt Select2 cho card mới
const addAddress = () => {
  formData.danhSachDiaChi.push({
    id_dia_chi: null,
    ho_ten_nhan: formData.tenKhachHang || '',
    so_dien_thoai_nhan: formData.soDienThoai || '',
    id_tinh_thanh: '',
    id_quan_huyen: '',
    id_phuong_xa: '',
    tenTinhThanh: '',
    tenQuanHuyen: '',
    tenPhuongXa: '',
    dia_chi_chi_tiet: '',
    la_mac_dinh: formData.danhSachDiaChi.length === 0,
    listHuyen: [],
    listXa: [],
    errors: {}
  });
};

// Hàm lấy danh sách Tỉnh/Thành
const loadTinhThanh = async () => {
  try {
    const response = await axios.get('https://provinces.open-api.vn/api/p/');
    listTinhThanh.value = response.data.map(item => ({
      value: String(item.code), // Đổi id thành value cho Multiselect
      label: item.name          // Đổi text thành label cho Multiselect
    }));
  } catch (error) {
    console.error("Không thể lấy danh sách tỉnh:", error);
  }
};


const onProvinceChange = async (provinceId, index, isInitial = false) => {
  const item = formData.danhSachDiaChi[index];
  if (!item) return;

  if (!isInitial) {
    item.id_quan_huyen = '';
    item.id_phuong_xa = '';
    item.tenQuanHuyen = '';
    item.tenPhuongXa = '';
    item.listHuyen = [];
    item.listXa = [];

    // Tìm và lưu tên Tỉnh (label)
    if (provinceId) {
      const tinhObj = listTinhThanh.value.find(t => t.value === String(provinceId));
      if (tinhObj) item.tenTinhThanh = tinhObj.label;
    } else {
      item.tenTinhThanh = '';
    }

    if (item.errors) item.errors.id_tinh_thanh = '';
  }

  // Chỉ gọi API nếu có ID tỉnh
  if (provinceId) {
    try {
      const res = await axios.get(`https://provinces.open-api.vn/api/p/${provinceId}?depth=2`);
      item.listHuyen = res.data.districts.map(d => ({
        value: String(d.code),
        label: d.name
      }));
    } catch (e) {
      console.error("Lỗi load huyện:", e);
    }
  }
};

const onDistrictChange = async (districtId, index, isInitial = false) => {
  const item = formData.danhSachDiaChi[index];
  if (!item) return;

  if (!isInitial) {
    item.id_phuong_xa = '';
    item.tenPhuongXa = '';
    item.listXa = [];

    if (districtId) {
      const huyenObj = item.listHuyen.find(h => h.value === String(districtId));
      if (huyenObj) item.tenQuanHuyen = huyenObj.label;
    } else {
      item.tenQuanHuyen = '';
    }

    if (item.errors) item.errors.id_quan_huyen = '';
  }

  if (districtId) {
    try {
      const res = await axios.get(`https://provinces.open-api.vn/api/d/${districtId}?depth=2`);
      item.listXa = res.data.wards.map(w => ({
        value: String(w.code),
        label: w.name
      }));
    } catch (e) {
      console.error("Lỗi load xã:", e);
    }
  }
};

const onWardChange = (wardId, index) => {
  const item = formData.danhSachDiaChi[index];
  if (!item) return;

  if (wardId) {
    const xaObj = item.listXa.find(x => x.value === String(wardId));
    if (xaObj) item.tenPhuongXa = xaObj.label;
  } else {
    item.tenPhuongXa = '';
  }

  if (item.errors) item.errors.id_phuong_xa = '';
};


// Gọi hàm này ngay khi component được khởi tạo

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
watch(formData, (val) => {
  Object.keys(val).forEach(key => {
    if (val[key] && errors[key]) {
      errors[key] = '';
    }
  });
}, { deep: true });

const tempAddressText = ref('');
const defaultIndex = ref(0);   // Lưu vị trí mặc định (mặc định là cái đầu tiên)
const preparePayload = () => {
  const data = new FormData();

  // 1. Đóng gói thông tin khách hàng (Giữ nguyên logic cũ của bạn)
  Object.keys(formData).forEach(key => {
    if (key === 'danhSachDiaChi' || key === 'diaChi') return;
    let val = formData[key];
    if (key === 'gioiTinh') val = val ? 1 : 0;
    if (val !== null && val !== undefined && val !== '') {
      data.append(key, val);
    }
    if (key === 'tenDangNhap' && !val) {
      val = formData.email;
    }
  });
  if (!data.has('tenDangNhap')) {
    data.append('tenDangNhap', formData.email);
  }
  // 2. Xử lý danh sách địa chỉ
  if (formData.danhSachDiaChi && formData.danhSachDiaChi.length > 0) {
    const validAddresses = formData.danhSachDiaChi.filter(addr =>
      addr.id_tinh_thanh && addr.id_tinh_thanh !== ''
    );

    validAddresses.forEach((addr, index) => {
      if (addr.id_dia_chi) {
        data.append(`danhSachDiaChi[${index}].id`, addr.id_dia_chi);
      }
      let cleanAddr = (addr.dia_chi_chi_tiet || '').split(',')[0].trim();
      data.append(`danhSachDiaChi[${index}].diaChiChiTiet`, cleanAddr);

      data.append(`danhSachDiaChi[${index}].idTinhThanh`, addr.id_tinh_thanh);
      data.append(`danhSachDiaChi[${index}].idQuanHuyen`, addr.id_quan_huyen);
      data.append(`danhSachDiaChi[${index}].idPhuongXa`, addr.id_phuong_xa);
      data.append(`danhSachDiaChi[${index}].tenTinhThanh`, addr.tenTinhThanh || '');
      data.append(`danhSachDiaChi[${index}].tenQuanHuyen`, addr.tenQuanHuyen || '');
      data.append(`danhSachDiaChi[${index}].tenPhuongXa`, addr.tenPhuongXa || '');

      // Lấy tên/sdt trực tiếp từ địa chỉ hoặc fallback về thông tin chung
      data.append(`danhSachDiaChi[${index}].hoTenNhan`, addr.ho_ten_nhan || formData.tenKhachHang);
      data.append(`danhSachDiaChi[${index}].soDienThoaiNhan`, addr.so_dien_thoai_nhan || formData.soDienThoai);

      // 🚩 SỬA LỖI INDEX: Dùng trực tiếp giá trị la_mac_dinh trong object
      data.append(`danhSachDiaChi[${index}].laMacDinh`, addr.la_mac_dinh);
    });
  }

  return data;
};

// Chọn địa chỉ mặc định
const setDefault = (index) => {
  defaultIndex.value = index;
  formData.danhSachDiaChi.forEach((addr, i) => {
    addr.la_mac_dinh = (i === index); // Chỉ index được chọn mới là true
  });
};

// Xóa địa chỉ
const removeAddress = (index) => {
  Swal.fire({
    title: 'Bạn chắc chắn chứ?',
    text: "Địa chỉ này sẽ bị xóa khỏi danh sách tạm!",
    icon: 'warning',
    iconColor: '#7D161A',
    showCancelButton: true,
    // Phối màu lại ở đây
    confirmButtonColor: '#eb445a', // Màu đỏ mận (soft red)
    cancelButtonColor: '#6c757d',  // Màu xám thanh lịch (muted gray)
    confirmButtonText: '<i class="fas fa-trash-alt"></i> Đồng ý xóa',
    cancelButtonText: 'Hủy',
    reverseButtons: true, // Đưa nút Hủy sang bên trái, Đồng ý sang bên phải cho thuận tay
    customClass: {
      confirmButton: 'btn btn-danger mx-2', // Bạn có thể dùng class Bootstrap nếu muốn
      cancelButton: 'btn btn-secondary mx-2'
    },
    buttonsStyling: true // Nếu dùng class Bootstrap thì để false, dùng màu manual thì để true
  }).then((result) => {
    if (result.isConfirmed) {
      // Logic xóa (giữ nguyên)
      if (index === defaultIndex.value) {
        defaultIndex.value = 0;
      } else if (index < defaultIndex.value) {
        defaultIndex.value--;
      }

      formData.danhSachDiaChi.splice(index, 1);

      // Thông báo thành công nhỏ gọn (Toast style)
      const Toast = Swal.mixin({
        toast: true,
        position: 'top-end',
        showConfirmButton: false,
        timer: 2000,
        timerProgressBar: true
      });

      Toast.fire({
        icon: 'success', iconColor: '#7D161A',
        title: 'Đã xóa địa chỉ thành công'
      });

      if (formData.danhSachDiaChi.length === 0) {
        defaultIndex.value = -1;
      }
    }
  });
};
const handleBack = () => {
  if (Object.values(formData).some(x => x !== '' && x !== true)) {
    Swal.fire({
      ...swalConfig,
      title: 'Xác nhận thoát?',
      text: "Các thay đổi chưa lưu sẽ bị mất!",
      icon: 'warning',
      iconColor: '#7D161A',
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
const validateNgaySinh = () => {
  const today = dayjs();

  if (!formData.ngaySinh) {
    errors.ngaySinh = "Vui lòng chọn ngày sinh";
    return false;
  }

  const birth = dayjs(formData.ngaySinh);

  // Không được lớn hơn hôm nay
  if (birth.isAfter(today)) {
    errors.ngaySinh = "Ngày sinh không thể lớn hơn ngày hiện tại";
    return false;
  }

  // Phải đủ 16 tuổi
  const age = today.diff(birth, "year");
  if (age < 16) {
    errors.ngaySinh = "Khách hàng phải đủ 16 tuổi!";
    return false;
  }

  errors.ngaySinh = "";
  return true;
};
const validateForm = async () => {
  let ok = true;
  const today = dayjs();

  // 1. Reset toàn bộ thông báo lỗi cũ của thông tin chung
  Object.keys(errors).forEach(k => errors[k] = '');

  // 2. Kiểm tra các trường thông tin khách hàng không được để trống
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

  // --- BẮT ĐẦU VALIDATE CHI TIẾT DANH SÁCH ĐỊA CHỈ ---
  if (!formData.danhSachDiaChi || formData.danhSachDiaChi.length === 0) {
    errors.diaChi = 'Vui lòng thêm ít nhất một địa chỉ';
    ok = false;
  } else {
    // Duyệt qua từng địa chỉ để kiểm tra các ô select và textarea
    formData.danhSachDiaChi.forEach((item, index) => {
      // Đảm bảo item.errors tồn tại để hiển thị báo đỏ trên giao diện
      if (!item.errors) {
        item.errors = { id_tinh_thanh: '', id_quan_huyen: '', id_phuong_xa: '', dia_chi_chi_tiet: '' };
      }

      // Reset lỗi của từng dòng địa chỉ
      item.errors.id_tinh_thanh = '';
      item.errors.id_quan_huyen = '';
      item.errors.id_phuong_xa = '';
      item.errors.dia_chi_chi_tiet = '';

      if (!item.id_tinh_thanh) {
        item.errors.id_tinh_thanh = 'Vui lòng chọn Tỉnh/Thành';
        ok = false;
      }
      if (!item.id_quan_huyen) {
        item.errors.id_quan_huyen = 'Vui lòng chọn Quận/Huyện';
        ok = false;
      }
      if (!item.id_phuong_xa) {
        item.errors.id_phuong_xa = 'Vui lòng chọn Phường/Xã';
        ok = false;
      }
      if (!item.dia_chi_chi_tiet || item.dia_chi_chi_tiet.trim() === '') {
        item.errors.dia_chi_chi_tiet = 'Vui lòng nhập địa chỉ chi tiết';
        ok = false;
      }
    });
  }
  // --- KẾT THÚC VALIDATE DANH SÁCH ĐỊA CHỈ ---

  // 3. Kiểm tra định dạng (Format) và Logic ngày sinh
  if (formData.soDienThoai && !/^0\d{9}$/.test(formData.soDienThoai)) {
    errors.soDienThoai = 'Số điện thoại không hợp lệ (phải gồm 10 chữ số)';
    ok = false;
  }

  if (formData.email && !/^\S+@\S+\.\S+$/.test(formData.email)) {
    errors.email = 'Địa chỉ email không đúng định dạng';
    ok = false;
  }
  if (!validateNgaySinh()) {
    ok = false;
  }

  if (!ok) return false;

  try {
    const checks = [
      { key: 'soDienThoai', label: 'Số điện thoại' },
      { key: 'email', label: 'Email' }
    ];



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

const submitClient = async (payload) => {
  if (clientId.value) {
    // Nếu có ID thì gọi API cập nhật
    return await clientService.update(clientId.value, payload);
  } else {
    // Nếu không có ID thì gọi API thêm mới
    return await clientService.create(payload);
  }
};

const handleSave = async () => {
  const isValid = await validateForm();
  if (!isValid) {
    Swal.fire({
      ...swalConfig,
      title: 'Thông tin chưa hợp lệ',
      text: 'Vui lòng kiểm tra lại các trường bị báo đỏ!',
      icon: 'warning',
      confirmButtonText: 'Đã hiểu'
    });
    return;
  }
if (!formData.tenDangNhap) {
    formData.tenDangNhap = formData.email;
  }
  // Tự động gán tài khoản nếu là thêm mới
  if (!clientId.value) {
    formData.tenDangNhap = formData.soDienThoai;
    if (!formData.matKhauDangNhap) {
      formData.matKhauDangNhap = generateRandomPassword(10);
    }
  }

  const result = await Swal.fire({
    ...swalConfig,
    title: clientId.value ? 'Cập nhật khách hàng?' : 'Xác nhận thêm mới?',
    text: clientId.value
      ? 'Lưu các thay đổi cho khách hàng này?'
      : 'Hệ thống sẽ tạo tài khoản khách hàng mới.',
    icon: 'question',
    showCancelButton: true,
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy bỏ'
  });

  if (!result.isConfirmed) return;

  try {
    loading.value = true;

    // Đảm bảo cập nhật lại lần cuối tên Text của địa chỉ từ ID trước khi đóng gói
    const payload = preparePayload();

    await submitClient(payload);

    await Swal.fire({
      ...swalConfig,
      title: 'Thành công!',
      text: clientId.value ? 'Đã cập nhật thông tin.' : 'Đã thêm khách hàng mới.',
      icon: 'success',
      timer: 1500,
      iconColor: '#7D161A',
      showConfirmButton: false
    });
    router.push('/admin/client');
  } catch (e) {
    console.error("Lỗi Save:", e.response?.data);
    const errorMsg = e.response?.data?.message || "Lỗi hệ thống, vui lòng thử lại.";
    Swal.fire({
      ...swalConfig,
      title: 'Thao tác thất bại',
      text: errorMsg,
      icon: 'error'
    });
  } finally {
    loading.value = false;
  }
};

onMounted(async () => {
  await loadTinhThanh(); // Load danh sách tỉnh đầu tiên

  if (clientId.value) {
    try {
      loading.value = true;
      const res = await clientService.getDetail(clientId.value);
      const data = res.data;

      // 1. Map thông tin cơ bản
      const { danhSachDiaChi, ...rest } = data;
      Object.assign(formData, rest);

      // Fix format ngày sinh nếu cần (YYYY-MM-DD)
      if (formData.ngaySinh) {
        formData.ngaySinh = dayjs(formData.ngaySinh).format("YYYY-MM-DD");
      }

      if (formData.anhDaiDien) {
        previewUrl.value = `http://localhost:8080/uploads/images/${formData.anhDaiDien}`;
      }

      // 2. Map danh sách địa chỉ
      if (danhSachDiaChi && danhSachDiaChi.length > 0) {
        const mappedAddresses = [];

        // Dùng for...of để có thể sử dụng await bên trong
        for (const d of danhSachDiaChi) {
          const addr = {
            id_dia_chi: d.id, // Đảm bảo đúng key id_dia_chi như template
            ho_ten_nhan: d.hoTenNhan || '',
            so_dien_thoai_nhan: d.soDienThoaiNhan || '',

            // Ép kiểu String để Multiselect nhận diện đúng value
            id_tinh_thanh: d.idTinhThanh ? String(d.idTinhThanh) : '',
            id_quan_huyen: d.idQuanHuyen ? String(d.idQuanHuyen) : '',
            id_phuong_xa: d.idPhuongXa ? String(d.idPhuongXa) : '',

            tenTinhThanh: d.tenTinhThanh || '',
            tenQuanHuyen: d.tenQuanHuyen || '',
            tenPhuongXa: d.tenPhuongXa || '',

            dia_chi_chi_tiet: d.diaChiChiTiet || '',
            la_mac_dinh: d.laMacDinh === true,

            listHuyen: [],
            listXa: [],
            errors: {} // Khởi tạo object errors để tránh lỗi undefined khi validate
          };

          // 3. Nạp dữ liệu mồi (options) dựa trên ID có sẵn
          // QUAN TRỌNG: Map đúng value/label cho Multiselect
          if (addr.id_tinh_thanh) {
            try {
              const resH = await axios.get(`https://provinces.open-api.vn/api/p/${addr.id_tinh_thanh}?depth=2`);
              addr.listHuyen = resH.data.districts.map(i => ({ value: String(i.code), label: i.name }));
            } catch (err) { console.error("Lỗi fetch huyện cũ:", err); }
          }

          if (addr.id_quan_huyen) {
            try {
              const resX = await axios.get(`https://provinces.open-api.vn/api/d/${addr.id_quan_huyen}?depth=2`);
              addr.listXa = resX.data.wards.map(i => ({ value: String(i.code), label: i.name }));
            } catch (err) { console.error("Lỗi fetch xã cũ:", err); }
          }

          mappedAddresses.push(addr);
        }

        formData.danhSachDiaChi = mappedAddresses;

        // Cập nhật vị trí mặc định
        const idx = formData.danhSachDiaChi.findIndex(d => d.la_mac_dinh === true);
        defaultIndex.value = idx !== -1 ? idx : 0;

        // Nếu bạn vẫn dùng Select2 (mặc dù Multiselect đã đủ tốt)
        nextTick(() => {
          setTimeout(() => {
            formData.danhSachDiaChi.forEach((_, index) => {
              if (typeof initSelect2ForIndex === 'function') {
                initSelect2ForIndex(index);
              }
            });
          }, 300);
        });
      }
    } catch (e) {
      console.error("Lỗi khi lấy chi tiết khách hàng:", e);
      toast.error("Không thể tải thông tin khách hàng");
    } finally {
      loading.value = false;
    }
  } else {
    formData.danhSachDiaChi = [];
    addAddress();
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
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
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

.btn-check+.btn:hover {
  border: 1px solid black;
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

.text-wine {
  color: #800000 !important;
}

.bg-wine {
  background-color: #800000 !important;
}

.address-card {
  transition: all 0.25s ease;
}

/* Hiệu ứng nẩy nhẹ khi hover vào card địa chỉ */
.address-card:hover {
  box-shadow: 0 10px 20px rgba(128, 0, 0, 0.05);
}

/* Tinh chỉnh Switch màu Wine */
.switch-wine:checked {
  background-color: #800000 !important;
  border-color: #800000 !important;
}

.border-light {
  border-color: #f0f0f0 !important;
}

/* Tùy chỉnh khung Select2 cho giống Bootstrap 5 và màu Wine */
:deep(.select2-container--default .select2-selection--single) {
  border: 1px solid #dee2e6 !important;
  height: 40px !important;
  border-radius: 8px !important;
  display: flex;
  align-items: center;
}

:deep(.select2-container--default .select2-selection--single .select2-selection__arrow) {
  height: 38px !important;
}

:deep(.select2-container--default .select2-selection--single:focus) {
  border-color: #800000 !important;
  box-shadow: 0 0 0 0.25rem rgba(128, 0, 0, 0.1);
}

/* Màu chữ của placeholder */
:deep(.select2-container--default .select2-selection--single .select2-selection__placeholder) {
  color: #6c757d;
  font-size: 0.875rem;
}

.custom-filter-multiselect {
  --ms-border-color: #dee2e6;
  --ms-radius: 8px;

  /* Màu vòng sáng khi focus */
  --ms-ring-color: rgba(128, 0, 0, 0.1);
  --ms-border-color-active: #800000;

  /* Hover vào Option chưa chọn */
  --ms-option-bg-pointed: #fdf2f2;
  --ms-option-color-pointed: #800000;

  /* Màu Option khi đã được chọn */
  --ms-option-bg-selected: #800000;
  --ms-option-color-selected: #ffffff;
  --ms-option-bg-selected-pointed: #600000;
}

/* Đổi màu text hiển thị ra ngoài */
.custom-filter-multiselect :deep(.multiselect-single-label) {
  color: #444;
  font-size: 14px;
}

.custom-filter-multiselect :deep(.multiselect-placeholder) {
  color: #888;
  font-size: 14px;
}

/* Đảm bảo Option đã chọn luôn có chữ màu trắng */
.custom-filter-multiselect :deep(.multiselect-option.is-selected) {
  background-color: #800000 !important;
  color: white !important;
}

/* 🚨 CSS KHI VALIDATE BỊ LỖI (Viền đỏ rực) */
.is-invalid-multi {
  --ms-border-color: #dc3545;
  --ms-ring-color: rgba(220, 53, 69, 0.25);
  box-shadow: 0 0 0 0.25rem rgba(220, 53, 69, 0.25);
}

.error-text {
  font-size: 12px;
  color: #dc3545;
  font-weight: 500;
}

/* Radio khi được chọn */
.form-check-input:checked {
  background-color: #800000;
  border-color: #800000;
}

/* Khi focus */
.form-check-input:focus {
  border-color: #800000;
  box-shadow: 0 0 0 0.2rem rgba(128, 0, 0, 0.25);
}
</style>