<template>
  <div class="promotion-statistic-wrapper">
    <div class="header mb-4">
      <h2 class="title-page">
        <i class="fa-solid fa-chart-line me-2"></i>
        Thống kê khuyến mãi
      </h2>
      <p class="sub-title">
        Tổng quan tình trạng khuyến mãi trong hệ thống
      </p>
    </div>

    <!-- Thống kê -->
    <div class="row g-4 mb-5">
      <div class="col-md-3" v-for="item in statistics" :key="item.label">
        <div class="stat-card" :class="item.border">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <p class="stat-label">{{ item.label }}</p>
              <h3 class="stat-value">{{ item.value }}</h3>
            </div>
            <div class="stat-icon-wrapper" :class="item.iconBg">
              <i :class="item.icon"></i>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Điều hướng -->
    <div class="row g-4">
      <div class="col-md-6">
        <div class="action-card">
          <div class="action-icon bg-primary">
            <i class="fa-solid fa-calendar-days"></i>
          </div>
          <h4>Quản lý đợt khuyến mãi</h4>
          <p>Thiết lập và quản lý các đợt khuyến mãi theo thời gian</p>
          <button class="btn btn-primary w-100" @click="goToCampaign">
            Truy cập
          </button>
        </div>
      </div>

      <div class="col-md-6">
        <div class="action-card">
          <div class="action-icon bg-success">
            <i class="fa-solid fa-ticket"></i>
          </div>
          <h4>Quản lý phiếu giảm giá</h4>
          <p>Thêm, sửa và phát hành các phiếu giảm giá cho khách hàng</p>
          <button class="btn btn-outline-primary w-100" @click="goToVoucher">
            Truy cập
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import thongKeKhuyenMaiService from '@/services/thongKeKhuyenMaiService'

const router = useRouter()

const statistics = ref([
  {
    label: 'Đang hoạt động',
    value: 0,
    icon: 'fa-solid fa-circle-check',
    iconBg: 'bg-success',
    border: 'border-success'
  },
  {
    label: 'Hết hạn',
    value: 0,
    icon: 'fa-solid fa-clock',
    iconBg: 'bg-warning text-dark',
    border: 'border-warning'
  },
  {
    label: 'Đã tắt',
    value: 0,
    icon: 'fa-solid fa-circle-minus',
    iconBg: 'bg-danger',
    border: 'border-danger'
  },
  {
    label: 'Tổng khuyến mãi',
    value: 0,
    icon: 'fa-solid fa-gift',
    iconBg: 'bg-info text-dark',
    border: 'border-info'
  }
])

const fetchThongKe = async () => {
  try {
    const data = await thongKeKhuyenMaiService.getThongKePhieuGiamGia()

    statistics.value[0].value = data.dangHoatDong
    statistics.value[1].value = data.hetHan
    statistics.value[2].value = data.daTat
    statistics.value[3].value = data.tongKhuyenMai
  } catch (e) {
    console.error('Lỗi lấy thống kê khuyến mãi', e)
  }
}

onMounted(fetchThongKe)

const goToVoucher = () => {
  router.push('/admin/voucher')
}

const goToCampaign = () => {
  router.push('/admin/promotion')
}
</script>


<style scoped>
.promotion-statistic-wrapper {
  padding: 24px;
  background: #f8f9fa;
  min-height: 100%;
}

/* Header */
.title-page {
  font-weight: 700;
  color: #7b121c;
}

.sub-title {
  color: #6c757d;
  margin-top: 4px;
}

/* Stat card */
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border-left: 6px solid transparent;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.border-success {
  border-left-color: #198754;
}

.border-warning {
  border-left-color: #ffc107;
}

.border-danger {
  border-left-color: #dc3545;
}

.border-info {
  border-left-color: #0dcaf0;
}

.stat-label {
  color: #6c757d;
  font-size: 14px;
}

.stat-value {
  font-size: 30px;
  font-weight: 700;
}

.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

/* Action card */
.action-card {
  background: #fff;
  border-radius: 18px;
  padding: 28px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  transition: all 0.25s ease;
}

.action-card:hover {
  transform: translateY(-6px);
}

.action-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  margin-bottom: 16px;
}

.action-card h4 {
  font-weight: 600;
  margin-bottom: 8px;
}

.btn-primary {
    --bs-btn-color: #fff;
    --bs-btn-bg: #7B121C;
    --bs-btn-border-color: #7B121C;
    --bs-btn-hover-color: #fff;
    --bs-btn-hover-bg: #7B121C;
    --bs-btn-hover-border-color: #7B121C;
    --bs-btn-focus-shadow-rgb: 49, 132, 253;
    --bs-btn-active-color: #fff;
    --bs-btn-active-bg: #7B121C;
    --bs-btn-active-border-color: #7B121C;
    --bs-btn-active-shadow: inset 0 3px 5px rgba(0, 0, 0, 0.125);
    --bs-btn-disabled-color: #fff;
    --bs-btn-disabled-bg: #7B121C;
    --bs-btn-disabled-border-color: #7B121C;
}
.btn-outline-primary {
    --bs-btn-color: #7B121C;
    --bs-btn-border-color: #7B121C;
    --bs-btn-hover-color: #fff;
    --bs-btn-hover-bg: #7B121C;
    --bs-btn-hover-border-color: #7B121C;
    --bs-btn-focus-shadow-rgb: 13, 110, 253;
    --bs-btn-active-color: #fff;
    --bs-btn-active-bg: #7B121C;
    --bs-btn-active-border-color: #7B121C;
    --bs-btn-active-shadow: inset 0 3px 5px rgba(0, 0, 0, 0.125);
    --bs-btn-disabled-color: #7B121C;
    --bs-btn-disabled-bg: transparent;
    --bs-btn-disabled-border-color: #7B121C;
    --bs-gradient: none;
}
.action-card p {
  color: #6c757d;
  font-size: 14px;
  margin-bottom: 20px;
}
</style>
