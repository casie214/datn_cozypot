<template>
  <div class="client-detail-page bg-light min-vh-100 p-4">

    <div class="container-fluid mb-4">
      <div class="d-flex justify-content-between align-items-center">
        <div>
          <h4 class="title-page text-wine fw-bold m-0">
            <i class="fas fa-address-card me-2"></i>
            Chi tiết khách hàng
          </h4>
          <p class="text-muted small mb-0">
            Thông tin chỉ để xem
          </p>
        </div>

        <button class="btn btn-outline-secondary rounded-3 px-4" @click="$router.push('/admin/client')">
          <i class="fas fa-arrow-left me-2"></i> Quay lại danh sách
        </button>
      </div>
    </div>

    <div class="container-fluid">
      <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
        <div class="card-body p-0">
          <div class="row g-0">

            <div class="col-lg-4 bg-white border-end p-5 section-left text-center">
              <div class="avatar-wrapper mb-4">
                <img :src="getAvatarUrl(formData.anhDaiDien) || 'https://via.placeholder.com/150'" class="avatar-img shadow" />
                <div class="rank-badge" v-if="formData.diemTichLuy > 0">
                    <i class="fas fa-crown"></i>
                </div>
              </div>
              <h5 class="fw-bold mb-1 text-dark">{{ formData.tenKhachHang }}</h5>
              <div class="mb-3">
                  <span class="badge bg-gold-soft text-dark rounded-pill px-3">
                    <i class="fas fa-star me-1 text-warning"></i> {{ formData.diemTichLuy || 0 }} điểm
                  </span>
              </div>
              <p class="text-muted small">Mã khách hàng: <span class="fw-bold text-wine">{{ formData.maKhachHang }}</span></p>
              
              <div class="status-box p-3 rounded-3 mt-4" :class="formData.trangThai === 1 ? 'bg-success-light' : 'bg-danger-light'">
                  <small class="fw-bold" :class="formData.trangThai === 1 ? 'text-success' : 'text-danger'">
                    <i class="fas fa-circle me-1 small"></i>
                    {{ formData.trangThai === 1 ? 'ĐANG HOẠT ĐỘNG' : 'NGỪNG HOẠT ĐỘNG' }}
                  </small>
              </div>
            </div>

            <div class="col-lg-8 bg-white p-5">

              <div class="form-section mb-4">
                <h5 class="section-subtitle mb-3">
                  <span class="badge-number">1</span>
                  Tài khoản hệ thống
                </h5>
                <div class="row g-4">
                  <div class="col-md-6">
                    <label class="form-label-custom">Tên đăng nhập</label>
                    <input class="form-control custom-input fw-bold text-wine" :value="formData.tenDangNhap" readonly>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Mật khẩu</label>
                    <input class="form-control custom-input" value="********" readonly>
                  </div>
                </div>
              </div>

              <div class="form-section mb-4 pt-4 border-top">
                <h5 class="section-subtitle mb-3">
                  <span class="badge-number">2</span>
                  Thông tin cá nhân
                </h5>
                <div class="row g-4">
                  <div class="col-md-6">
                    <label class="form-label-custom">Họ và tên</label>
                    <input class="form-control custom-input" :value="formData.tenKhachHang" readonly>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Số điện thoại</label>
                    <input class="form-control custom-input" :value="formData.soDienThoai" readonly>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Ngày sinh</label>
                    <input class="form-control custom-input" :value="formatDate(formData.ngaySinh)" readonly>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Giới tính</label>
                    <input class="form-control custom-input" :value="formData.gioiTinh ? 'Nam' : 'Nữ'" readonly>
                  </div>
                  <div class="col-12">
                    <label class="form-label-custom">Địa chỉ email</label>
                    <input class="form-control custom-input" :value="formData.email" readonly>
                  </div>
                </div>
              </div>

              <div class="form-section pt-4 border-top">
                <h5 class="section-subtitle mb-3">
                  <span class="badge-number">3</span>
                  Thông tin bổ sung
                </h5>
                <div class="row g-4">
                  <div class="col-12">
                    <label class="form-label-custom">Địa chỉ thường trú</label>
                    <textarea class="form-control custom-input" rows="2" readonly>{{ formData.diaChi || 'Chưa cập nhật' }}</textarea>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Ngày tạo tài khoản</label>
                    <input class="form-control custom-input" :value="formatDate(formData.ngayTaoTaiKhoan)" readonly>
                  </div>
                  <div class="col-md-6">
                    <label class="form-label-custom">Cập nhật lần cuối</label>
                    <input class="form-control custom-input" :value="formatDate(formData.ngayCapNhat)" readonly>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import dayjs from 'dayjs';
import clientService from '@/services/clientService';

const route = useRoute();
const id = route.params.id;

const formData = reactive({});

const formatDate = (d) => (d ? dayjs(d).format('DD/MM/YYYY') : '---');

const getAvatarUrl = (fileName) => {
  if (!fileName) return null;
  // Lưu ý: Thay đổi folder upload tương ứng với Backend của khách hàng
  return `http://localhost:8080/uploads/customers/${fileName}`;
};

onMounted(async () => {
  try {
    const res = await clientService.getDetail(id);
    Object.assign(formData, res.data);
  } catch (error) {
    console.error("Lỗi khi tải chi tiết khách hàng:", error);
  }
});


</script>

<style scoped>
/* ===============================
    PREMIUM ADMIN THEME (COPIED FROM STAFF)
=================================*/
:root {
  --primary-red: #7B121C;
  --dark-red: #600000;
  --bg-white: #ffffff;
  --text-main: #2c3e50;
}

.text-wine { color: #800000; }
.bg-gold-soft { background: #fff8e1; border: 1px solid #ffe082; }

/* CARD & SECTION */
.card { background: #fff; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.04); }
.section-left { background: #fafafa; }

/* AVATAR: Kế thừa từ bên nhân viên */
.avatar-wrapper { position: relative; display: inline-block; }
.avatar-img {
  width: 180px; height: 180px;
  border-radius: 50%; object-fit: cover;
  border: 5px solid white;
  transition: 0.3s;
}
.rank-badge {
  position: absolute; top: 10px; right: 10px;
  background: #ffc107; color: white;
  width: 35px; height: 35px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  border: 3px solid white; box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

/* INPUT READONLY: Giống hệt trang nhân viên */
.custom-input {
  background: #f9fafc !important;
  border-radius: 10px;
  padding: 12px;
  font-size: 14px;
  border: 1px solid rgba(0, 0, 0, 0.05);
  color: #2c3e50;
  font-weight: 500;
  cursor: default;
}

/* SECTION SUBTITLE & BADGE NUMBER */
.section-subtitle { font-weight: 800; color: #2c3e50; display: flex; align-items: center; gap: 10px; }
.badge-number {
  background: linear-gradient(135deg, #7B121C, #600000);
  color: white; font-weight: 800;
  width: 28px; height: 28px; border-radius: 50%;
  display: inline-flex; align-items: center; justify-content: center;
  font-size: 13px; box-shadow: 0 4px 10px rgba(123, 18, 28, 0.3);
}

/* LABEL */
.form-label-custom {
  font-weight: 700; font-size: 11px;
  text-transform: uppercase; color: #666;
  margin-bottom: 5px; letter-spacing: 0.5px;
}

/* STATUS */
.bg-success-light { background: #e8f5e9; border: 1px solid #c8e6c9; }
.bg-danger-light { background: #ffebee; border: 1px solid #ffcdd2; }

.btn-outline-secondary { border-radius: 10px; font-weight: 600; }
</style>