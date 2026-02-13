<template>
<div class="staff-form-page bg-light min-vh-100 p-4">

    <!-- HEADER -->
    <div class="container-fluid mb-4">
        <div class="d-flex justify-content-between align-items-center">
            <div>
                <h4 class="title-page text-wine fw-bold m-0">
                    <i class="fas fa-user"></i>
                    Chi tiết nhân viên
                </h4>
                <p class="text-muted small mb-0">
                    Thông tin chỉ để xem
                </p>
            </div>

            <button class="btn btn-outline-secondary rounded-3"
                @click="$router.push('/admin/staff')">
                <i class="fas fa-arrow-left me-2"></i> Quay lại danh sách 
            </button>
        </div>
    </div>

    <!-- BODY -->
    <div class="container-fluid">
        <div class="card shadow-sm border-0 rounded-4 overflow-hidden">
            <div class="card-body p-0">

                <div class="row g-0">

                    <!-- LEFT -->
                    <div class="col-lg-4 bg-white border-end p-5 section-left text-center">
                        <img
                            :src="imagePreview || 'https://via.placeholder.com/150'"
                            class="avatar-img mb-3"
                        />
                        <h6 class="fw-bold">{{ formData.hoTenNhanVien }}</h6>
                        <small class="text-muted">
                            {{ formData.tenVaiTro }}
                        </small>
                    </div>

                    <!-- RIGHT -->
                    <div class="col-lg-8 bg-white p-5">

                        <!-- ID + MÃ -->
                        <div class="form-section mb-4">
                            <h5 class="section-subtitle mb-3">
                                <span class="badge-number">1</span>
                                Thông tin hệ thống
                            </h5>

                            <div class="row g-4">
                                <div class="col-md-6">
                                    <label class="form-label-custom">ID nhân viên</label>
                                    <input class="form-control custom-input"
                                        :value="formData.id" readonly>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label-custom">Mã nhân viên</label>
                                    <input class="form-control custom-input"
                                        :value="formData.maNhanVien" readonly>
                                </div>
                            </div>
                        </div>

                        <!-- THÔNG TIN CÁ NHÂN -->
                        <div class="form-section mb-4 pt-4 border-top">
                            <h5 class="section-subtitle mb-3">
                                <span class="badge-number">2</span>
                                Thông tin cá nhân
                            </h5>

                            <div class="row g-4">

                                <div class="col-md-6">
                                    <label class="form-label-custom">Họ tên</label>
                                    <input class="form-control custom-input"
                                        :value="formData.hoTenNhanVien" readonly>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label-custom">CCCD</label>
                                    <input class="form-control custom-input"
                                        :value="formData.soCccd" readonly>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label-custom">SĐT</label>
                                    <input class="form-control custom-input"
                                        :value="formData.sdtNhanVien" readonly>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label-custom">Email</label>
                                    <input class="form-control custom-input"
                                        :value="formData.email" readonly>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label-custom">Ngày sinh</label>
                                    <input class="form-control custom-input"
                                        :value="formatDate(formData.ngaySinh)" readonly>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label-custom">Giới tính</label>
                                    <input class="form-control custom-input"
                                        :value="formData.gioiTinh ? 'Nam' : 'Nữ'" readonly>
                                </div>

                                <div class="col-12">
                                    <label class="form-label-custom">Địa chỉ</label>
                                    <input class="form-control custom-input"
                                        :value="formData.diaChi" readonly>
                                </div>
                            </div>
                        </div>

                        <!-- CCCD -->
                        <div class="form-section pt-4 border-top">
                            <h5 class="section-subtitle mb-3">
                                <span class="badge-number">3</span>
                                Thông tin CCCD
                            </h5>

                            <div class="row g-4">
                                <div class="col-md-6">
                                    <label class="form-label-custom">Ngày cấp</label>
                                    <input class="form-control custom-input"
                                        :value="formatDate(formData.ngayCapCccd)" readonly>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label-custom">Nơi cấp</label>
                                    <input class="form-control custom-input"
                                        :value="formData.noiCapCccd" readonly>
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
import { reactive, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import dayjs from 'dayjs'
import staffService from '@/services/staffService'

const route = useRoute()
const id = route.params.id

const formData = reactive({})
const imagePreview = ref(null)

const formatDate = d =>
    d ? dayjs(d).format('DD/MM/YYYY') : ''

onMounted(async () => {
    const res = await staffService.getDetail(id)
    Object.assign(formData, res.data)

    if (res.data.anhDaiDien) {
        imagePreview.value =
            `http://localhost:8080/uploads/images/${res.data.anhDaiDien}`
    }
})
</script>

<style scoped>
/* ===============================
   PREMIUM ADMIN THEME
=================================*/

:root {
  --primary-red: #7B121C;
  --dark-red: #600000;
  --bg-white: #ffffff;
  --text-main: #2c3e50;
  --text-muted: #666;
  --border-light: rgba(0, 0, 0, 0.05);
  --shadow-premium: 0 10px 30px rgba(0, 0, 0, 0.04),
                    0 2px 8px rgba(0, 0, 0, 0.02);
  --shadow-hover: 0 15px 45px rgba(0, 0, 0, 0.08);
}

/* PAGE */
.staff-form-page {
  background: #f6f7fb;
}

/* CARD */
.card {
  background: var(--bg-white);
  box-shadow: var(--shadow-premium);
  transition: 0.3s;
}

.card:hover {
  box-shadow: var(--shadow-hover);
}

/* TITLE */
.title-page {
  color: var(--primary-red);
  letter-spacing: 0.5px;
}

/* SECTION TITLE */
.section-subtitle {
  font-weight: 800;
  color: var(--text-main);
  display: flex;
  align-items: center;
  gap: 10px;
}

/* NUMBER BADGE */
.badge-number {
  background: linear-gradient(135deg, var(--primary-red), var(--dark-red));
  color: white;
  font-weight: 800;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  box-shadow: 0 4px 10px rgba(123, 18, 28, 0.3);
}

/* LEFT PANEL */
.section-left {
  background: #fafafa;
}

/* AVATAR */
.avatar-img {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  object-fit: cover;
  border: 5px solid white;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  transition: 0.3s;
}

.avatar-img:hover {
  transform: scale(1.03);
}

/* LABEL */
.form-label-custom {
  font-weight: 700;
  font-size: 11px;
  text-transform: uppercase;
  color: var(--text-muted);
  margin-bottom: 5px;
  letter-spacing: 0.5px;
}

/* INPUT READONLY */
.custom-input {
  background: #f9fafc;
  border-radius: 10px;
  padding: 12px;
  font-size: 14px;
  border: 1px solid var(--border-light);
  color: var(--text-main);
  font-weight: 500;
  transition: 0.2s;
}

.custom-input:focus {
  outline: none;
  border-color: var(--primary-red);
  box-shadow: 0 0 0 3px rgba(123, 18, 28, 0.08);
}

/* BORDER SECTION */
.form-section {
  position: relative;
}

/* BUTTON BACK */
.btn-outline-secondary {
  border-radius: 10px;
  font-weight: 600;
  transition: 0.2s;
}

.btn-outline-secondary:hover {
  background: var(--primary-red);
  border-color: var(--primary-red);
  color: white;
}

/* TEXT */
.text-muted {
  color: var(--text-muted) !important;
}

</style>