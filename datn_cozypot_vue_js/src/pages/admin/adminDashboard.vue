<script setup>
import { useRouter } from 'vue-router';
// Import logo của bạn (có thể đổi đường dẫn nếu cần)
import logo from "@/assets/images/dashboardimage.png";

const router = useRouter();

// Danh sách các component trong Sidebar để làm thẻ truy cập nhanh
const quickLinks = [
  { name: 'Thống kê', icon: 'fa-solid fa-chart-area', path: '/admin/statistics', desc: 'Xem doanh thu & báo cáo' },
  { name: 'Lịch đặt bàn', icon: 'fa-solid fa-calendar-days', path: '/admin/tables', desc: 'Quản lý lịch hẹn khách' },
  { name: 'Check-in bàn', icon: 'fa-solid fa-circle-check', path: '/admin/checkin', desc: 'Nhận bàn cho khách' },
  { name: 'Hóa đơn', icon: 'fa-solid fa-cart-shopping', path: '/admin/orders', desc: 'Thanh toán & tra cứu' },
  { name: 'Thực đơn', icon: 'fa-solid fa-bell-concierge', path: '/manage/all/danh-sach', desc: 'Quản lý món ăn & set lẩu' },
  { name: 'Khách hàng', icon: 'fa-solid fa-users', path: '/admin/client', desc: 'Chăm sóc khách hàng' }
];

const navigateTo = (path) => {
  router.push(path);
};
</script>

<template>
  <div class="home-container">
    <div class="intro-section">
      <div class="intro-text">
        <h1>Chào mừng đến với <span class="highlight">CozyPot</span></h1>
        <p>
          Nền tảng quản lý nhà hàng thông minh, giúp tối ưu vận hành và nâng cao
          trải nghiệm khách hàng. Bắt đầu công việc ngay hôm nay!
        </p>
      </div>

      <div class="quick-links-grid">
        <div 
          v-for="(link, index) in quickLinks" 
          :key="index" 
          class="quick-card"
          @click="navigateTo(link.path)"
        >
          <div class="card-icon">
            <i :class="link.icon"></i>
          </div>
          <div class="card-info">
            <h3>{{ link.name }}</h3>
            <p>{{ link.desc }}</p>
          </div>
          <i class="fa-solid fa-arrow-right card-arrow"></i>
        </div>
      </div>
    </div>

    <div class="logo-wrapper">
      <span class="smoke smoke-1"></span>
      <span class="smoke smoke-2"></span>
      <span class="smoke smoke-3"></span>
      <img class="logo-trans rounded-circle" :src="logo" alt="Logo CozyPot" />
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.home-container {
  min-height: calc(100vh - 60px); /* Trừ đi header nếu có */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40px 60px;
  background: transparent;
  gap: 40px;
}

/* ===== LEFT SECTION ===== */
.intro-section {
  flex: 1;
  max-width: 650px;
  animation: fadeIn 0.8s ease-out;
}

.intro-text h1 {
  font-size: 42px;
  font-weight: 800;
  color: #333;
  margin-bottom: 15px;
}

.intro-text .highlight {
  color: #7d161a;
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.intro-text p {
  font-size: 16px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 40px;
}

/* Grid truy cập nhanh */
.quick-links-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.quick-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
  border: 1px solid #f0f0f0;
  box-shadow: 0 4px 15px rgba(0,0,0,0.02);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.quick-card:hover {
  transform: translateY(-5px);
  border-color: #7d161a;
  box-shadow: 0 10px 25px rgba(125, 22, 26, 0.1);
}

.quick-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: #7d161a;
  transform: scaleY(0);
  transition: transform 0.3s ease;
  transform-origin: bottom;
}

.quick-card:hover::before {
  transform: scaleY(1);
}

.card-icon {
  width: 45px;
  height: 45px;
  background: #fff5f5;
  color: #7d161a;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  transition: 0.3s;
}

.quick-card:hover .card-icon {
  background: #7d161a;
  color: #fff;
}

.card-info {
  flex: 1;
}

.card-info h3 {
  font-size: 16px;
  font-weight: 700;
  color: #333;
  margin: 0 0 4px 0;
}

.card-info p {
  font-size: 12px;
  color: #888;
  margin: 0;
  line-height: 1.3;
}

.card-arrow {
  color: #ccc;
  font-size: 14px;
  transition: 0.3s;
}

.quick-card:hover .card-arrow {
  color: #7d161a;
  transform: translateX(5px);
}

/* ===== RIGHT SECTION (LOGO & ANIMATION) ===== */
.logo-wrapper {
  position: relative;
  flex: 1;
  max-width: 400px;
  height: 450px;
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeInRight 1s ease-out;
}

.logo-trans {
  width: 320px;
  height: 320px;
  object-fit: cover;
  animation: floatLogo 4s ease-in-out infinite;
  transition: transform 0.4s ease, filter 0.4s ease;
  cursor: pointer;
  filter: drop-shadow(0 15px 35px rgba(80, 80, 80, 0.25));
  z-index: 2;
}

.logo-trans:hover {
  transform: scale(1.05) rotate(2deg);
  filter: drop-shadow(0 25px 50px rgba(125, 22, 26, 0.4));
}

@keyframes floatLogo {
  0% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
  100% { transform: translateY(0px); }
}

/* KHÓI */
.smoke {
  position: absolute;
  display: block;
  bottom: 250px;
  width: 120px;
  height: 120px;
  background: radial-gradient(
    circle,
    rgba(255, 140, 0, 0.85) 0%,
    rgba(255, 140, 0, 0.65) 40%,
    rgba(255, 140, 0, 0.35) 65%,
    transparent 70%
  );
  border-radius: 50%;
  filter: blur(25px);
  animation: smokeUp 8s infinite ease-in-out;
  opacity: 0.7;
  z-index: 1;
}

.smoke-1 { left: 20%; animation-delay: 0s; }
.smoke-2 { left: 45%; animation-delay: 2.5s; }
.smoke-3 { left: 30%; animation-delay: 5s; }

@keyframes smokeUp {
  0% { transform: translateY(0) scale(0.8); opacity: 0; }
  15% { opacity: 0.7; }
  60% { transform: translateY(-120px) scale(1.2); opacity: 0.4; }
  100% { transform: translateY(-220px) scale(1.4); opacity: 0; }
}

/* ===== ANIMATIONS ===== */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInRight {
  from { opacity: 0; transform: translateX(30px); }
  to { opacity: 1; transform: translateX(0); }
}

/* RESPONSIVE */
@media (max-width: 1024px) {
  .quick-links-grid { grid-template-columns: 1fr; }
  .logo-trans { width: 250px; height: 250px; }
}

@media (max-width: 768px) {
  .home-container {
    flex-direction: column-reverse;
    padding: 20px;
    gap: 20px;
  }
  .intro-section { max-width: 100%; text-align: center; }
  .quick-links-grid { text-align: left; }
  .logo-wrapper { height: 300px; }
}
</style>