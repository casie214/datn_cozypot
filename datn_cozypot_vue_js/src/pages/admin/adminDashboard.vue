<script setup>
import logo from "@/assets/images/dashboardimage.png";
</script>
<template>
  <div class="home-container">
    <!-- LEFT: TEXT -->
    <div class="intro">
      <h1>CozyPot</h1>
      <p>
        Nền tảng quản lý nhà hàng thông minh, giúp tối ưu vận hành và nâng cao
        trải nghiệm khách hàng.
      </p>
    </div>

    <!-- RIGHT: LOGO -->
    <div class="logo-wrapper">
      <!-- Khói -->
      <span class="smoke smoke-1"></span>
      <span class="smoke smoke-2"></span>
      <span class="smoke smoke-3"></span>
      <img class="logo-trans" :src="logo" alt="Logo CozyPot" />
    </div>
  </div>
</template>
<style>
.home-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 80px;
  background: white;
}

/* TEXT */
.intro {
  max-width: 500px;
}

.intro h1 {
  font-size: 48px;
  font-weight: 700;
  color: #7d161a;
  margin-bottom: 16px;
}

.intro p {
  font-size: 18px;
  color: #4b5563;
  line-height: 1.6;
}

/* LOGO */
/* .logo img {
  width: 300px;
  height: auto;
} */

/* Logo bên phải */
.logo-wrapper {
  position: relative; /* ⭐ BẮT BUỘC */
  width: 40%;
  height: 400px; /* thêm chiều cao để khói có không gian bay */
  display: flex;
  justify-content: center;
  align-items: center;
}

/* Logo */
.logo-trans {
  width: 320px;
  animation: floatLogo 4s ease-in-out infinite;
  transition:
    transform 0.4s ease,
    filter 0.4s ease;
  cursor: pointer;
  /* Bóng mặc định */
  filter: drop-shadow(0 15px 35px rgba(80, 80, 80, 0.45));
  z-index: 2;
}

/* Hover: bóng mạnh + glow nhẹ */
.logo-trans:hover {
  transform: scale(1.08) rotate(2deg);
  filter: drop-shadow(0 25px 50px rgba(176, 176, 176, 0.6))
    drop-shadow(0 0 30px rgba(249, 118, 118, 0.6));
}

/* Animation di chuyển liên tục */
@keyframes floatLogo {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
  100% {
    transform: translateY(0px);
  }
}

/* ===== KHÓI ===== */
.smoke {
  position: absolute;
  display: block;
  bottom: 300px;
  width: 120px;
  height: 120px;
  background: radial-gradient(
    circle,
    rgba(255, 140, 0, 0.85) 0%,      /*  Tăng opacity */
    rgba(255, 140, 0, 0.65) 40%,     /*  Tăng opacity */
    rgba(255, 140, 0, 0.35) 65%,    /*  Tăng opacity */
    transparent 70%
  );
  border-radius: 50%;
  filter: blur(25px);
  animation: smokeUp 8s infinite ease-in-out;
  opacity: 0.7;  /* ⭐ Thay đổi này - cho opacity ban đầu */
  z-index: 1;

}




/* Khói lệch nhau cho tự nhiên */
.smoke-1 {
  left: 120px;
  animation-delay: 0s;
}
.smoke-2 {
  left: 180px;
  animation-delay: 2.5s;
}
.smoke-3 {
  left: 150px;
  animation-delay: 5s;
}

/* Animation khói bay lên */
@keyframes smokeUp {
  0% {
    transform: translateY(0) scale(0.8);
    opacity: 0;
  }
  15% {
    opacity: 0.7;
  }
  60% {
    transform: translateY(-120px) scale(1.2);
    opacity: 0.4;
  }
  100% {
    transform: translateY(-220px) scale(1.4);
    opacity: 0;
  }
}




/* RESPONSIVE */
@media (max-width: 768px) {
  .home-container {
    flex-direction: column;
    text-align: center;
    padding: 40px 20px;
  }

  .logo {
    margin-top: 30px;
  }
}
</style>
