<script setup>
import { useRouter } from 'vue-router';
import { ref, onMounted } from 'vue';
import axiosClient from '@/services/axiosClient';
import CommonNav from '@/components/commonNav.vue';

const router = useRouter();

// Dữ liệu giả lập cho các Set lẩu nổi bật
const featuredSets = ref([]);

const fetchFeaturedSets = async () => {
  try {
    const response = await axiosClient.get('/guest/hotpot/top/3');
    
    featuredSets.value = response.data.map(item => ({
      id: item.id,
      name: item.tenSetLau, 
      desc: item.moTa || 'Thơm ngon trọn vị', 
      price: item.giaBan, 
      image: item.hinhAnh || 'https://via.placeholder.com/300' 
    }));

  } catch (error) {
    console.error("Lỗi tải danh sách Set lẩu nổi bật:", error);
    featuredSets.value = []; 
  }
};

onMounted(() => {
  fetchFeaturedSets();
})



// Điều hướng
const goToMenu = () => {
  // router.push({ name: 'menu' });
  alert("Chuyển đến trang thực đơn...");
};

const goToBooking = () => {
  // router.push({ name: 'booking' });
  alert("Chuyển đến trang đặt bàn...");
};

const goToLogin = () => {
  router.push({ name: 'login' });
};
</script>

<template>
  <div class="guest-home">

    <header id="home" class="hero">
      <div class="hero-overlay"></div>
      <div class="hero-content container">
        <span class="subtitle">Chào mừng đến với CozyPot</span>
        <h1>Thưởng thức Lẩu Ngon<br>Trong Không Gian Ấm Cúng</h1>
        <p>
          Hương vị lẩu trứ danh được chế biến từ nguyên liệu tươi ngon nhất. 
          Đặt bàn ngay hôm nay để nhận ưu đãi đặc biệt!
        </p>
        <div class="hero-btns">
          <button class="btn-lg-primary" @click="goToBooking">
            <i class="fa-solid fa-calendar-check"></i> Đặt bàn ngay
          </button>
          <button class="btn-lg-outline" @click="goToMenu">
            <i class="fa-solid fa-utensils"></i> Xem thực đơn
          </button>
        </div>
      </div>
    </header>

    <section id="menu" class="section-container container">
      <div class="section-header">
        <h2 class="section-title">Set Lẩu Được Yêu Thích</h2>
        <p class="section-desc">Những lựa chọn tuyệt vời nhất dành cho bạn và người thân</p>
      </div>

      <div class="menu-grid">
        <div v-for="set in featuredSets" :key="set.id" class="menu-card">
          <div class="card-image">
            <img :src="set.image" :alt="set.name">
            <div class="badge-price">{{ set.price.toLocaleString() }}đ</div>
          </div>
          <div class="card-details">
            <h3>{{ set.name }}</h3>
            <p>{{ set.desc }}</p>
            <div class="card-actions">
              <button class="btn-sm-outline" @click="goToMenu">Xem chi tiết</button>
              <button class="btn-sm-primary" @click="goToBooking">
                <i class="fa-solid fa-cart-plus"></i> Đặt ngay
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <div class="text-center mt-4">
        <button class="btn-link" @click="goToMenu">Xem toàn bộ thực đơn &rarr;</button>
      </div>
    </section>

    <section id="booking-banner" class="booking-promo">
      <div class="container promo-content">
        <div class="promo-text">
          <h2>Đặt bàn online - Giữ chỗ check-in ngay</h2>
          <p>Không cần chờ đợi. Chọn bàn ưng ý, đặt món trước và chỉ việc đến thưởng thức.</p>
          <div class="features-list">
            <span><i class="fa-solid fa-check"></i> Chọn vị trí bàn</span>
            <span><i class="fa-solid fa-check"></i> Đặt món trước</span>
            <span><i class="fa-solid fa-check"></i> Tích điểm thành viên</span>
          </div>
        </div>
        <div class="promo-form-preview">
          <h3>Đặt bàn ngay</h3>
          <div class="fake-input">
            <i class="fa-solid fa-users"></i> <span>2 người</span>
          </div>
          <div class="fake-input">
            <i class="fa-solid fa-calendar"></i> <span>Hôm nay, 19:00</span>
          </div>
          <button class="btn-full" @click="goToBooking">Tiếp tục</button>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="container footer-content">
        <div class="footer-info">
          <h3>CozyPot</h3>
          <p>AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA.</p>
          <div class="socials">
            <i class="fa-brands fa-facebook"></i>
            <i class="fa-brands fa-instagram"></i>
            <i class="fa-brands fa-tiktok"></i>
          </div>
        </div>
        <div class="footer-contact">
          <h4>Liên hệ</h4>
          <p><i class="fa-solid fa-location-dot"></i> AAAAAAAAAAAAAAAAAAAAAAAAAAAAA</p>
          <p><i class="fa-solid fa-phone"></i> 12345678</p>
        </div>
      </div>
      <div class="copyright">
        © 2024 CozyPot. All rights reserved.
      </div>
    </footer>
  </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

/* --- GLOBAL VARS & RESET --- */
:root {
  --primary: #7d161a;
  --primary-dark: #5c0a16;
  --secondary: #fdfbf7;
  --text-dark: #2c3e50;
  --text-light: #666;
  --white: #ffffff;
  --shadow: 0 10px 30px rgba(0,0,0,0.08);
}

.guest-home {
  font-family: 'Segoe UI', sans-serif;
  color: #333;
  background-color: #fdfbf7; /* Màu nền kem nhẹ */
  width: 100%;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* --- 1. NAVBAR --- */
.navbar {
  background: white;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  position: sticky;
  top: 0;
  z-index: 1000;
  height: 70px;
  display: flex;
  align-items: center;
}

.nav-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 1.5rem;
  font-weight: 800;
  color: #7d161a;
}
.logo img { height: 40px; }

.nav-links {
  display: flex;
  list-style: none;
  gap: 30px;
  margin: 0;
}

.nav-links a {
  text-decoration: none;
  color: #333;
  font-weight: 600;
  transition: color 0.3s;
}
.nav-links a:hover, .nav-links a.active { color: #7d161a; }

.nav-auth { display: flex; gap: 15px; align-items: center; }

/* Buttons */
button { cursor: pointer; border-radius: 30px; font-weight: 600; transition: all 0.3s; }

.btn-primary {
  background: #7d161a;
  color: white;
  border: none;
  padding: 10px 24px;
}
.btn-primary:hover { background: #5c0a16; transform: translateY(-2px); }

.btn-login {
  background: transparent;
  border: none;
  color: #333;
  font-size: 1rem;
}
.btn-login:hover { color: #7d161a; }

/* --- 2. HERO SECTION --- */
.hero {
  position: relative;
  height: 600px;
  /* Bạn thay link ảnh background ở đây */
  background: url('https://grandworld.vinhomes.vn/wp-content/uploads/2023/12/Lau-ba-ly.jpg') center/cover no-repeat;
  display: flex;
  align-items: center;
  text-align: center;
  color: white;
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5); /* Lớp phủ đen mờ */
}

.hero-content {
  position: relative;
  z-index: 2;
  max-width: 800px;
  margin: 0 auto;
}

.hero-content .subtitle {
  font-family: 'Brush Script MT', cursive;
  font-size: 2rem;
  color: #f1c40f;
  display: block;
  margin-bottom: 10px;
}

.hero-content h1 {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: 20px;
}

.hero-content p {
  font-size: 1.2rem;
  margin-bottom: 30px;
  opacity: 0.9;
}

.hero-btns {
  display: flex;
  justify-content: center;
  gap: 20px;
}

.btn-lg-primary {
  background: #7d161a;
  color: white;
  border: none;
  padding: 15px 40px;
  font-size: 1.1rem;
  border-radius: 50px;
}
.btn-lg-primary:hover { background: #a52a2a; }

.btn-lg-outline {
  background: transparent;
  border: 2px solid white;
  color: white;
  padding: 15px 40px;
  font-size: 1.1rem;
  border-radius: 50px;
}
.btn-lg-outline:hover { background: white; color: #7d161a; }

/* --- 3. MENU SECTION --- */
.section-container { padding: 80px 20px; }
.text-center { text-align: center; }
.mt-4 { margin-top: 1.5rem; }

.section-header { text-align: center; margin-bottom: 50px; }
.section-title { font-size: 2.5rem; color: #7d161a; font-weight: 800; margin-bottom: 10px; }
.section-desc { color: #666; font-size: 1.1rem; }

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.menu-card {
  background: white;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.05);
  transition: transform 0.3s;
}
.menu-card:hover { transform: translateY(-10px); }

.card-image { position: relative; height: 200px; overflow: hidden; }
.card-image img { width: 100%; height: 100%; object-fit: cover; }
.badge-price {
  position: absolute;
  bottom: 15px;
  right: 15px;
  background: #7d161a;
  color: white;
  padding: 5px 15px;
  border-radius: 20px;
  font-weight: bold;
  box-shadow: 0 2px 10px rgba(0,0,0,0.2);
}

.card-details { padding: 20px; }
.card-details h3 { font-size: 1.3rem; margin-bottom: 10px; color: #2c3e50; }
.card-details p { color: #666; font-size: 0.95rem; height: 45px; overflow: hidden; margin-bottom: 20px; }

.card-actions { display: flex; gap: 10px; }
.btn-sm-outline { flex: 1; border: 1px solid #ddd; background: white; padding: 8px; border-radius: 8px; }
.btn-sm-primary { flex: 1; background: #7d161a; color: white; border: none; padding: 8px; border-radius: 8px; }
.btn-sm-primary:hover { background: #5c0a16; }

.btn-link { background: none; border: none; color: #7d161a; font-weight: bold; font-size: 1.1rem; text-decoration: underline; }

/* --- 4. BOOKING BANNER --- */
.booking-promo {
  background: linear-gradient(to right, #2c3e50, #4ca1af);
  /* Hoặc màu đỏ đậm */
  background: #7d161a;
  color: white;
  padding: 60px 0;
}

.promo-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 50px;
}

.promo-text h2 { font-size: 2.2rem; margin-bottom: 15px; }
.promo-text p { font-size: 1.1rem; margin-bottom: 25px; opacity: 0.9; }
.features-list { display: flex; gap: 20px; }
.features-list span { display: flex; align-items: center; gap: 8px; font-weight: 600; }
.features-list i { color: #f1c40f; }

.promo-form-preview {
  background: white;
  padding: 30px;
  border-radius: 15px;
  width: 350px;
  color: #333;
}
.promo-form-preview h3 { text-align: center; color: #7d161a; margin-bottom: 20px; }
.fake-input {
  border: 1px solid #ddd;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #666;
  cursor: pointer;
}
.btn-full {
  width: 100%;
  background: #7d161a;
  color: white;
  border: none;
  padding: 12px;
  border-radius: 8px;
  font-size: 1.1rem;
}

/* --- 5. FOOTER --- */
.footer { background: #222; color: white; padding: 50px 0 20px; }
.footer-content { display: flex; justify-content: space-between; margin-bottom: 30px; }
.footer-info h3 { color: #f1c40f; margin-bottom: 15px; }
.socials { display: flex; gap: 15px; margin-top: 15px; font-size: 1.2rem; }
.footer-contact h4 { margin-bottom: 15px; color: #ddd; }
.footer-contact p { margin-bottom: 10px; color: #aaa; }
.copyright { text-align: center; border-top: 1px solid #333; padding-top: 20px; color: #666; font-size: 0.9rem; }

/* RESPONSIVE */
@media (max-width: 768px) {
  .nav-links, .nav-auth { display: none; } /* Ẩn menu trên mobile demo */
  .hero-content h1 { font-size: 2.5rem; }
  .promo-content { flex-direction: column; text-align: center; }
  .features-list { flex-direction: column; align-items: center; }
  .footer-content { flex-direction: column; gap: 30px; }
}

.navbar {
  /* ... các thuộc tính cũ ... */
  height: 70px;
  
  /* 👇 THÊM DÒNG NÀY */
  overflow: visible !important; 
}
</style>