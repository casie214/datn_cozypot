<script setup>
import { useRouter } from 'vue-router';
import { ref, onMounted, computed } from 'vue';
import axiosClient from '@/services/axiosClient';

// --- QUẢN LÝ USER & CHAT ---
const user = JSON.parse(localStorage.getItem('user') || '{}');
const isChatOpen = ref(false);

const shouldShowChat = computed(() => {
  if (!user || !user.role) return true;
  if (user.role === 'customer' || user.role === 'GUEST') return true;
  return false;
});

const openChat = () => {
  isChatOpen.value = !isChatOpen.value;
};

// --- ROUTER & ĐIỀU HƯỚNG ---
const router = useRouter();
const goToMenu = () => router.push('/menu');
const goToBooking = () => router.push({ name: 'booking' });

// --- DỮ LIỆU API ---
const featuredSets = ref([]);
const fetchFeaturedSets = async () => {
  try {
    const response = await axiosClient.get('/guest/hotpot/top/3');
    featuredSets.value = response.data.map(item => ({
      id: item.id,
      name: item.tenSetLau,
      // Ưu tiên lấy moTa, nếu rỗng thì lấy moTaChiTiet
      desc: item.moTa || item.moTaChiTiet || 'Tuyệt tác hương vị lẩu, sự kết hợp hoàn hảo từ nguyên liệu tươi ngon.',
      
      // 🚨 HỨNG ĐÚNG BIẾN GIÁ TỪ API
      price: item.giaSauGiam || item.giaGoc || 0, 
      originalPrice: item.giaGoc,
      isDiscount: item.isGiamGia,
      discountPercent: item.phanTramGiam,
      
      image: item.hinhAnh || 'https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?q=80&w=1000&auto=format&fit=crop'
    }));
  } catch (error) {
    console.error("Lỗi tải danh sách Set lẩu nổi bật:", error);
  }
};

onMounted(() => {
  fetchFeaturedSets();
  // Chạy animation khi scroll (nếu thích bạn có thể thêm thư viện AOS sau)
});

// --- DỮ LIỆU GIẢ LẬP (MỚI) ---
const features = [
  { icon: 'fa-solid fa-fire-burner', title: 'Nước lẩu tinh túy', desc: 'Ninh hầm hơn 12 tiếng với thảo mộc tự nhiên, mang lại vị ngọt thanh, đậm đà.' },
  { icon: 'fa-solid fa-drumstick-bite', title: 'Thịt bò hảo hạng', desc: '100% bò nhập khẩu tươi mềm, vân mỡ đan xen tan chảy ngay trong miệng.' },
  { icon: 'fa-solid fa-shop', title: 'Không gian ấm cúng', desc: 'Thiết kế mang đậm chất Á Đông, riêng tư nhưng vẫn ấm áp cho những buổi tụ họp.' }
];

const reviews = [
  { name: 'Ngọc Linh', rate: 5, text: 'Nước lẩu Thái ở đây ngon xuất sắc, chua cay vừa vặn. Nhân viên phục vụ rất nhiệt tình!' },
  { name: 'Hoàng Phong', rate: 5, text: 'Thịt bò mềm, đồ nhúng đa dạng. Chắc chắn sẽ quay lại ủng hộ CozyPot nhiều lần nữa.' },
  { name: 'Mai Anh', rate: 5, text: 'Không gian sạch sẽ, ấm cúng. Rất phù hợp để đi ăn cùng gia đình vào dịp cuối tuần.' }
];

const orderHotpotSet = (setName) => {
  router.push({
    path: '/menu', // Đảm bảo đường dẫn này khớp với route trang thực đơn của bạn
    query: { autoAdd: setName }
  });
};
</script>

<template>
  <div class="guest-home">
    
    <header class="hero-section">
      <div class="hero-overlay"></div>
      <div class="container hero-content">
        <div class="hero-text">
          <span class="badge-tag"><i class="fa-solid fa-crown text-warning"></i> Trải nghiệm Lẩu Đỉnh Cao</span>
          <h1>CozyPot<br><span class="text-highlight">Trải nghiệm <br></span> lẩu hải sản cao cấp</h1>
          <p>Khám phá bản giao hưởng của hương vị với những nồi lẩu nghi ngút khói. Nguyên liệu thượng hạng, phục vụ tận tâm, không gian tuyệt vời.</p>
          
          <div class="hero-actions">
            <button class="btn-main shadow-btn" @click="goToBooking">
              <i class="fa-solid fa-calendar-check me-2"></i> Đặt bàn ngay
            </button>
            <button class="btn-outline-white" @click="goToMenu">
              <i class="fa-solid fa-book-open me-2"></i> Xem thực đơn
            </button>
          </div>
        </div>
      </div>
    </header>

    <section class="features-section">
      <div class="container">
        <div class="section-title text-center">
          <span class="sub-heading">Tại sao chọn CozyPot?</span>
          <h2>Chuẩn Vị Trong Từng Gắp Lẩu</h2>
        </div>
        <div class="features-grid">
          <div v-for="(item, index) in features" :key="index" class="feature-card">
            <div class="feature-icon">
              <i :class="item.icon"></i>
            </div>
            <h3>{{ item.title }}</h3>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </div>
    </section>

    <section class="menu-section">
      <div class="container">
        <div class="section-title d-flex justify-content-between align-items-end">
          <div>
            <span class="sub-heading">Thực đơn</span>
            <h2>Set Lẩu Được Yêu Thích Nhất</h2>
          </div>
          <button class="btn-text" @click="goToMenu">Xem tất cả <i class="fa-solid fa-arrow-right"></i></button>
        </div>

        <div class="menu-grid">
          <div v-for="set in featuredSets" :key="set.id" class="food-card">
            <div class="food-img-wrap">
              <img :src="set.image" :alt="set.name" class="food-img">
              <div v-if="set.isDiscount" class="food-discount-badge">
                -{{ set.discountPercent }}%
              </div>
              <div class="food-price">
                <span v-if="set.isDiscount" class="old-price text-muted text-decoration-line-through me-1" style="font-size: 0.8rem; font-weight: normal;">
                  {{ set.originalPrice.toLocaleString() }}₫
                </span>
                {{ set.price.toLocaleString() }}₫
              </div>
            </div>
            <div class="food-info">
              <h3>{{ set.name }}</h3>
              <p>{{ set.desc }}</p>
              <div class="food-actions">
                <button class="btn-outline-red w-100 mb-2" @click="goToMenu">Xem chi tiết</button>
                <button class="btn-main w-100" @click="orderHotpotSet(set.name)">
                  Đặt ngay
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="booking-cta-section">
      <div class="container">
        <div class="cta-wrapper">
          <div class="cta-text">
            <h2>Giữ Chỗ Ngay - Không Lo Chờ Đợi</h2>
            <p>Vào những giờ cao điểm, CozyPot thường xuyên hết bàn. Hãy đặt trước để chúng tôi chuẩn bị không gian và phục vụ bạn chu đáo nhất.</p>
            <ul class="cta-benefits">
              <li><i class="fa-solid fa-check-circle"></i> Chọn vị trí ngồi yêu thích</li>
              <li><i class="fa-solid fa-check-circle"></i> Lên sẵn thực đơn - Đến là dùng ngay</li>
              <li><i class="fa-solid fa-check-circle"></i> Tích lũy điểm thưởng thành viên</li>
            </ul>
            <button class="btn-main btn-large mt-3" @click="goToBooking">
              Tiến hành đặt bàn <i class="fa-solid fa-angle-right ms-2"></i>
            </button>
          </div>
          <div class="cta-image">
            <div class="floating-card">
              <div class="f-header">Bàn cho 4 người</div>
              <div class="f-body">
                <div><i class="fa-regular fa-clock text-danger"></i> 19:00 - Tối nay</div>
                <div><i class="fa-solid fa-fire text-danger"></i> Lẩu Thái Tomyum</div>
              </div>
              <button class="btn-main w-100 mt-2">Xác nhận</button>
            </div>
          </div>
        </div>
      </div>
    </section>


    <section class="gallery-section">
      <div class="gallery-grid">
        <div class="g-item"><img src="https://images.unsplash.com/photo-1552611052-33e04de081de?auto=format&fit=crop&q=80&w=600" alt="Hotpot"></div>
        <div class="g-item"><img src="https://pasgo.vn/Upload/anh-chi-tiet/nha-hang-meat-plus-royal-city-1-normal-1428470245230.webp" alt="Meat"></div>
        <div class="g-item"><img src="https://www.eatingwell.com/thmb/ZsFqmJC3Cwvnb8D4OUuGBqLZRzE=/1500x0/filters:no_upscale():max_bytes(150000):strip_icc()/EWL-Sheet-Pan-Chickpeas-Spring-Veggies-1x1-222_preview_maxWidth_4000_maxHeight_4000_ppi_300_quality_100-8090eacd4bfc4356b45d8b64c0ab901f.jpg" alt="Veggies"></div>
        <div class="g-item"><img src="https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?auto=format&fit=crop&q=80&w=600" alt="Restaurant"></div>
      </div>
    </section>

    <footer class="main-footer">
      <div class="container">
        <div class="footer-grid">
          <div class="footer-col brand-col">
            <h2 class="footer-logo">CozyPot</h2>
            <p>Mang đến trải nghiệm lẩu trứ danh với không gian ấm cúng và sự phục vụ tận tâm nhất. Đến với chúng tôi, bạn không chỉ ăn, mà là thưởng thức.</p>
            <div class="social-links">
              <a href="#"><i class="fa-brands fa-facebook-f"></i></a>
              <a href="#"><i class="fa-brands fa-instagram"></i></a>
              <a href="#"><i class="fa-brands fa-tiktok"></i></a>
            </div>
          </div>
          <div class="footer-col">
            <h3>Liên Kết Nhanh</h3>
            <ul>
              <li><a href="#" @click.prevent="goToMenu">Thực đơn</a></li>
              <li><a href="#" @click.prevent="goToBooking">Đặt bàn</a></li>
              <li><a href="#">Khuyến mãi</a></li>
              <li><a href="#">Về chúng tôi</a></li>
            </ul>
          </div>
          <div class="footer-col">
            <h3>Giờ Mở Cửa</h3>
            <ul class="hours-list">
              <li><span>Thứ 2 - Thứ 6:</span> <span>10:00 - 22:00</span></li>
              <li><span>Thứ 7 - CN:</span> <span>09:00 - 23:00</span></li>
              <li class="text-danger mt-2">Xuyên lễ & Tết</li>
            </ul>
          </div>
          <div class="footer-col">
            <h3>Liên Hệ</h3>
            <ul class="contact-list">
              <li><i class="fa-solid fa-location-dot"></i> Tầng 1, Tòa nhà ABC, Hà Nội</li>
              <li><i class="fa-solid fa-phone"></i> Hotline: 1900 1234</li>
              <li><i class="fa-solid fa-envelope"></i> hotro@cozypot.vn</li>
            </ul>
          </div>
        </div>
        <div class="footer-bottom">
          <p>© 2024 CozyPot Restaurant.</p>
        </div>
      </div>
    </footer>

  </div>
</template>

<style scoped>
/* ==========================================================================
   GLOBAL VARIABLES & RESET
   ========================================================================== */
:root {
  --primary: #8b0000;
  --primary-light: #ba2c26;
  --primary-dark: #5c0000;
  --accent: #f1c40f;
  --bg-color: #fdfbf7;
  --text-main: #2c3e50;
  --text-muted: #666666;
  --border-color: #eeeeee;
  --shadow-sm: 0 4px 12px rgba(0,0,0,0.05);
  --shadow-md: 0 10px 25px rgba(0,0,0,0.08);
  --shadow-hover: 0 15px 35px rgba(139, 0, 0, 0.15);
  --transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

* { box-sizing: border-box; }
.guest-home {
  --primary: #8b0000;
  --primary-light: #ba2c26;
  --primary-dark: #5c0000;
  --accent: #f1c40f;
  --bg-color: #fdfbf7;
  --text-main: #2c3e50;
  --text-muted: #666666;
  --border-color: #eeeeee;
  --shadow-sm: 0 4px 12px rgba(0,0,0,0.05);
  --shadow-md: 0 10px 25px rgba(0,0,0,0.08);
  --shadow-hover: 0 15px 35px rgba(139, 0, 0, 0.15);
  --transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  color: var(--text-main);
  background-color: var(--bg-color);
  line-height: 1.6;
}

/* 2. CHỐT CỨNG MÀU CHO BANNER ĐỂ TRÁNH LỖI TÀNG HÌNH */
.booking-cta-section {
  padding: 100px 0;
  background: linear-gradient(135deg, #8b0000 0%, #5c0000 100%); /* Fix cứng màu đỏ ở đây */
  color: white;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* ==========================================================================
   TYPOGRAPHY & UTILS
   ========================================================================== */
.section-title {
  margin-bottom: 50px;
}
.sub-heading {
  color: var(--primary);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 2px;
  font-size: 0.9rem;
  display: block;
  margin-bottom: 10px;
}
.section-title h2 {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-main);
  margin: 0;
}
.text-highlight { color: var(--primary); }
.text-warning { color: var(--accent); }

/* Buttons */
button { cursor: pointer; font-family: inherit; }
.btn-main {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%);
  color: white;
  border: none;
  padding: 12px 28px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 1rem;
  transition: var(--transition);
}
.btn-main:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(139, 0, 0, 0.3);
  filter: brightness(1.1);
}
.btn-outline-white {
  background: transparent;
  color: white;
  border: 2px solid white;
  padding: 12px 28px;
  border-radius: 8px;
  font-weight: 600;
  transition: var(--transition);
}
.btn-outline-white:hover {
  background: white;
  color: var(--primary);
}
.btn-outline-red {
  background: transparent;
  color: var(--primary);
  border: 1px solid var(--primary);
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  transition: var(--transition);
}
.btn-outline-red:hover {
  background: var(--primary-light);
  color: white;
  border-color: var(--primary-light);
}
.btn-text {
  background: none;
  border: none;
  color: var(--primary);
  font-weight: 700;
  font-size: 1.1rem;
  transition: var(--transition);
}
.btn-text:hover { color: var(--primary-light); transform: translateX(5px); }

/* ==========================================================================
   1. HERO SECTION
   ========================================================================== */
.hero-section {
  position: relative;
  height: 80vh;
  min-height: 600px;
  background: url('https://product.hstatic.net/200000358419/product/hsan__d18c7eeb7a7d49ecafe7fbd487181222_master.png') center/cover no-repeat;
  display: flex;
  align-items: center;
}
.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.3) 100%);
}
.hero-content {
  position: relative;
  z-index: 2;
  color: white;
}
.hero-text {
  max-width: 600px;
}
.badge-tag {
  display: inline-block;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(5px);
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: 600;
  margin-bottom: 20px;
  border: 1px solid rgba(255,255,255,0.2);
}
.hero-text h1 {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 20px;
}
.hero-text p {
  font-size: 1.1rem;
  opacity: 0.9;
  margin-bottom: 40px;
  line-height: 1.6;
}
.hero-actions { display: flex; gap: 15px; }

/* ==========================================================================
   2. FEATURES SECTION
   ========================================================================== */
.features-section { padding: 100px 0; background: white; }
.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 40px;
  margin-top: 50px;
}
.feature-card {
  text-align: center;
  padding: 40px 30px;
  background: var(--bg-color);
  border-radius: 20px;
  transition: var(--transition);
  border: 1px solid var(--border-color);
}
.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: var(--shadow-md);
  border-color: #fbdada;
}
.feature-icon {
  width: 80px;
  height: 80px;
  background: #fdf2f2;
  color: var(--primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  margin: 0 auto 20px;
}
.feature-card h3 { font-size: 1.3rem; margin-bottom: 15px; font-weight: 700; }
.feature-card p { color: var(--text-muted); font-size: 0.95rem; margin: 0; }

/* ==========================================================================
   3. MENU SECTION
   ========================================================================== */
.menu-section { padding: 100px 0; background: var(--bg-color); }
.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 30px;
}
.food-card {
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
  border: 1px solid var(--border-color);
  
  /* THÊM 3 DÒNG NÀY */
  display: flex;
  flex-direction: column;
  height: 100%; 
}
.food-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-hover);
}
.food-img-wrap {
  position: relative;
  height: 240px;
  overflow: hidden;
}
.food-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.food-card:hover .food-img { transform: scale(1.05); }
.food-discount-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  background: #f1c40f; /* Màu vàng nổi bật */
  color: #8b0000;
  font-weight: 900;
  padding: 5px 12px;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
  z-index: 2;
  font-size: 0.9rem;
}

/* Chỉnh lại độ rộng của cục giá để chứa 2 mức giá */
.food-price {
  position: absolute;
  bottom: 15px;
  right: 15px;
  background: white;
  color: var(--primary);
  font-weight: 800;
  padding: 8px 16px;
  border-radius: 30px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
}
.food-info {
  padding: 25px;
  
  /* THÊM 3 DÒNG NÀY */
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}
.food-actions {
  margin-top: auto; /* Lệnh này tự động đẩy khối action xuống dưới cùng */
}
.food-info h3 { 
  font-size: 1.3rem; 
  font-weight: 700; 
  margin-bottom: 10px;
  
  /* Thêm giới hạn 2 dòng */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 3rem; /* Giữ sẵn khoảng trống cho 2 dòng */
}
.food-info p {
  color: var(--text-muted);
  font-size: 0.95rem;
  height: 45px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 25px;
}

/* ==========================================================================
   4. BOOKING CTA SECTION
   ========================================================================== */
.booking-cta-section {
  padding: 100px 0;
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  color: white;
}
.cta-wrapper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 60px;
}
.cta-text { flex: 1; }
.cta-text h2 { font-size: 2.5rem; font-weight: 800; margin-bottom: 20px; }
.cta-text p { font-size: 1.1rem; opacity: 0.9; margin-bottom: 30px; }
.cta-benefits { list-style: none; padding: 0; margin-bottom: 0; }
.cta-benefits li {
  margin-bottom: 15px;
  font-size: 1.1rem;
  display: flex;
  align-items: center;
  gap: 15px;
}
.cta-benefits i { color: var(--accent); font-size: 1.2rem; }

.cta-image {
  flex: 1;
  display: flex;
  justify-content: flex-end;
}
.floating-card {
  background: white;
  color: var(--text-main);
  padding: 30px;
  border-radius: 20px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 50px rgba(0,0,0,0.3);
  transform: rotate(2deg);
}
.floating-card .f-header {
  font-weight: 800;
  font-size: 1.2rem;
  border-bottom: 1px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 15px;
}
.floating-card .f-body div {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 15px;
  font-weight: 600;
}

/* ==========================================================================
   5. REVIEWS SECTION
   ========================================================================== */
.reviews-section { padding: 100px 0; background: white; }
.reviews-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}
.review-card {
  background: var(--bg-color);
  padding: 35px;
  border-radius: 20px;
  border: 1px solid var(--border-color);
  position: relative;
}
.review-card::before {
  content: "\f10d";
  font-family: "Font Awesome 6 Free";
  font-weight: 900;
  position: absolute;
  top: 20px;
  right: 30px;
  font-size: 3rem;
  color: #f1f1f1;
}
.stars { color: var(--accent); margin-bottom: 15px; font-size: 1.1rem; }
.review-text { font-style: italic; color: var(--text-muted); margin-bottom: 25px; line-height: 1.7; }
.reviewer { display: flex; align-items: center; gap: 15px; }
.reviewer .avatar {
  width: 45px;
  height: 45px;
  background: #eee;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
.reviewer .name { font-weight: 700; }

/* ==========================================================================
   6. GALLERY SECTION
   ========================================================================== */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  height: 350px;
}
.g-item { overflow: hidden; }
.g-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}
.g-item:hover img { transform: scale(1.1); }

/* ==========================================================================
   7. FOOTER
   ========================================================================== */
.main-footer {
  background: #111;
  color: white;
  padding: 80px 0 0 0;
}
.footer-grid {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr 1.5fr;
  gap: 50px;
  margin-bottom: 50px;
}
.footer-logo {
  font-size: 2rem;
  font-weight: 800;
  color: white;
  margin-bottom: 20px;
}
.brand-col p { color: #999; margin-bottom: 25px; line-height: 1.8; }
.social-links { display: flex; gap: 15px; }
.social-links a {
  width: 40px;
  height: 40px;
  background: #222;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  text-decoration: none;
  transition: var(--transition);
}
.social-links a:hover { background: var(--primary); }

.footer-col h3 { font-size: 1.2rem; font-weight: 700; margin-bottom: 25px; color: white; }
.footer-col ul { list-style: none; padding: 0; margin: 0; }
.footer-col ul li { margin-bottom: 12px; color: #999; }
.footer-col a { color: #999; text-decoration: none; transition: var(--transition); }
.footer-col a:hover { color: var(--primary-light); padding-left: 5px; }

.hours-list li, .contact-list li { display: flex; justify-content: space-between; }
.contact-list li { justify-content: flex-start; gap: 15px; align-items: flex-start; }
.contact-list i { color: var(--primary-light); margin-top: 5px; }

.footer-bottom {
  border-top: 1px solid #222;
  padding: 25px 0;
  text-align: center;
  color: #666;
  font-size: 0.9rem;
}

/* ==========================================================================
   CHAT BOT 
   ========================================================================== */
.chat-launcher {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 1000;
  width: 65px;
  height: 65px;
  background: linear-gradient(135deg, var(--primary) 0%, #ff4b4b 100%);
  border-radius: 50%;
  color: white;
  font-size: 28px;
  cursor: pointer;
  box-shadow: 0 10px 25px rgba(139,0,0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  transition: var(--transition);
}
.chat-launcher:hover { transform: scale(1.1) translateY(-5px); }
.notification-badge {
  position: absolute;
  top: -2px;
  right: -2px;
  background: var(--accent);
  color: #000;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 900;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
}
.chat-tooltip {
  position: absolute;
  right: 80px;
  background: #333;
  color: white;
  padding: 8px 15px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: var(--transition);
}
.chat-launcher:hover .chat-tooltip { opacity: 1; right: 85px; }

/* ==========================================================================
   RESPONSIVE DESIGN
   ========================================================================== */
@media (max-width: 992px) {
  .hero-text h1 { font-size: 3rem; }
  .cta-wrapper { flex-direction: column; text-align: center; }
  .cta-benefits li { justify-content: center; }
  .footer-grid { grid-template-columns: 1fr 1fr; gap: 40px; }
  .gallery-grid { grid-template-columns: 1fr 1fr; height: auto; }
}

@media (max-width: 768px) {
  .hero-text h1 { font-size: 2.5rem; }
  .hero-actions { flex-direction: column; }
  .footer-grid { grid-template-columns: 1fr; gap: 30px; }
  .gallery-grid { grid-template-columns: 1fr; }
}
</style>