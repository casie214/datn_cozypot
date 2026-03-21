<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axiosClient from "@/services/axiosClient";
import Swal from "sweetalert2";

// --- 1. STATE QUẢN LÝ DỮ LIỆU ---
const vouchers = ref([]);
const isLoading = ref(false);
const activeTab = ref("public");
const filterType = ref("all");
const sortOption = ref("default");
const searchQuery = ref("");

// --- 2. FETCH DỮ LIỆU TỪ API ---
const fetchData = async () => {
    isLoading.value = true;
    vouchers.value = []; // Xóa list cũ để tránh bị nháy hình

    try {
        // Tự động đổi Endpoint tùy theo tab
        const endpoint = activeTab.value === "public"
            ? "/phieu-giam-gia/public"
            : "/phieu-giam-gia/ca-nhan"; // Giả định endpoint cá nhân của bạn

        const response = await axiosClient.get(endpoint, {
            params: {
                page: 0,
                size: 50,
                // Nếu là tab cá nhân, backend thường sẽ tự lấy userId từ Token
            },
        });

        const responseData = response.data || response;
        vouchers.value = responseData.content || [];
    } catch (error) {
        console.error("Lỗi tải voucher:", error);
        // Nếu lỗi 401 (chưa đăng nhập) khi vào tab cá nhân
        if (error.response?.status === 401 && activeTab.value === "personal") {
            Swal.fire({
                icon: "warning",
                title: "Yêu cầu đăng nhập",
                text: "Vui lòng đăng nhập để xem các ưu đãi dành riêng cho bạn!",
                confirmButtonColor: "#7d161a",
            });
            activeTab.value = "public";
            fetchData();
        }
    } finally {
        isLoading.value = false;
    }
};

watch(activeTab, () => {
    searchQuery.value = "";
    fetchData();
});

// --- 3. LOGIC LỌC & SẮP XẾP ---
const displayedVouchers = computed(() => {
    let result = [...vouchers.value];

    // A. Lọc theo Tìm kiếm (Tên hoặc Mã)
    const q = searchQuery.value.trim().toLowerCase();
    if (q) {
        result = result.filter((v) =>
            (v.tenPhieuGiamGia && v.tenPhieuGiamGia.toLowerCase().includes(q)) ||
            (v.codeGiamGia && v.codeGiamGia.toLowerCase().includes(q))
        );
    }

    // B. Lọc theo Loại giảm giá (0: Tiền mặt, 1: Phần trăm)
    if (filterType.value !== "all") {
        result = result.filter((v) => String(v.loaiGiamGia) === filterType.value);
    }

    // C. Sắp xếp
    if (sortOption.value !== "default") {
        result.sort((a, b) => {
            if (sortOption.value === "expiring_soon") {
                return new Date(a.ngayKetThuc) - new Date(b.ngayKetThuc);
            } else if (sortOption.value === "discount_desc") {
                return b.giaTriGiam - a.giaTriGiam;
            }
            return 0;
        });
    }

    return result;
});

const clearSearch = () => {
    searchQuery.value = "";
};


// --- 4. UTILS & ACTIONS ---
const formatPrice = (value) => {
    if (!value) return "0 ₫";
    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
    }).format(value);
};

const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
};

const copyCode = (code) => {
    navigator.clipboard.writeText(code);
    Swal.fire({
        icon: "success",
        title: "Đã sao chép mã!",
        text: `Mã ${code} đã được lưu vào khay nhớ tạm.`,
        toast: true,
        position: "top-end",
        showConfirmButton: false,
        timer: 2000,
        timerProgressBar: true,
    });
};

const applyVoucher = (voucher) => {
    // Logic chuyển sang trang đặt bàn hoặc giỏ hàng
    Swal.fire({
        icon: "info",
        title: "Sử dụng ưu đãi",
        text: `Hãy nhập mã ${voucher.codeGiamGia} ở bước thanh toán nhé!`,
        confirmButtonText: "Đến thực đơn",
        confirmButtonColor: "#7d161a",
    });
};

onMounted(() => {
    fetchData();
});
</script>

<template>
    <div class="customer-promo-page">
        <div class="promo-banner">
            <div class="banner-content text-center">
                <h1 class="banner-title">Ưu Đãi Độc Quyền</h1>
                <p class="banner-subtitle">Sưu tầm ngay mã giảm giá để thưởng thức Lẩu ngon với giá siêu hời!</p>
            </div>
        </div>

        <div v-if="isLoading" class="text-center py-5 mt-5">
            <div class="spinner-border text-primary-red" role="status"></div>
            <p class="mt-2 text-muted">Đang tải danh sách ưu đãi...</p>
        </div>

        <div v-else class="container promo-container">
            <div class="tab-navigation mb-4">
                <div class="tab-pill">
                    <button :class="{ active: activeTab === 'public' }" @click="activeTab = 'public'">
                        <i class="fas fa-globe-asia me-2"></i>Ưu đãi chung
                    </button>
                    <button :class="{ active: activeTab === 'personal' }" @click="activeTab = 'personal'">
                        <i class="fas fa-user-tag me-2"></i>Ưu đãi của tôi
                    </button>
                    <div class="tab-slider" :class="activeTab"></div>
                </div>
            </div>

            <div class="search-filter-bar bg-white p-3 mb-4 rounded-4 border shadow-sm sticky-top"
                style="top: 20px; z-index: 999">
                <div class="row g-3 align-items-center">

                    <div class="col-lg-5 col-md-6">
                        <div class="input-group">
                            <span class="input-group-text bg-white border-end-0">
                                <i class="fas fa-search text-muted"></i>
                            </span>
                            <input type="text" class="form-control border-start-0 ps-0 shadow-none"
                                placeholder="Tìm theo tên hoặc mã ưu đãi..." v-model="searchQuery" />
                            <button v-if="searchQuery"
                                class="btn btn-outline-secondary border-start-0 border-end-0 bg-white"
                                @click="clearSearch">
                                <i class="fas fa-times text-muted"></i>
                            </button>
                        </div>
                    </div>

                    <div class="col-lg-4 col-md-6">
                        <div class="filter-scroll-wrapper">
                            <button class="btn-filter" :class="{ active: filterType === 'all' }"
                                @click="filterType = 'all'">Tất cả</button>
                            <button class="btn-filter" :class="{ active: filterType === '0' }"
                                @click="filterType = '0'">Tiền mặt</button>
                            <button class="btn-filter" :class="{ active: filterType === '1' }"
                                @click="filterType = '1'">Phần trăm</button>
                        </div>
                    </div>

                    <div class="col-lg-3 col-md-12 text-lg-end">
                        <select class="form-select shadow-none fw-semibold sort-select w-100" v-model="sortOption">
                            <option value="default">Mới nhất</option>
                            <option value="expiring_soon">Sắp hết hạn</option>
                            <option value="discount_desc">Mức giảm cao nhất</option>
                        </select>
                    </div>

                </div>
            </div>

            <div v-if="displayedVouchers.length === 0" class="text-center py-5 mt-4 text-muted">
                <i class="fas fa-ticket-alt fa-4x mb-3" style="color: #ddd"></i>
                <h5>Hiện tại chưa có chương trình ưu đãi nào phù hợp.</h5>
            </div>

            <TransitionGroup name="filter-list" tag="div" class="row g-4">
                <div v-for="voucher in displayedVouchers" :key="voucher.id" class="col-lg-4 col-md-6">
                    <div class="voucher-card">

                        <div class="voucher-header" :class="voucher.loaiGiamGia === 1 ? 'bg-percent' : 'bg-cash'">
                            <div class="badge-type">
                                {{ voucher.loaiGiamGia === 1 ? 'GIẢM %' : 'GIẢM TIỀN' }}
                            </div>
                            <h3 class="discount-value">
                                {{ voucher.loaiGiamGia === 1 ? voucher.giaTriGiam + '%' :
                                formatPrice(voucher.giaTriGiam) }}
                            </h3>
                            <p class="min-order" v-if="voucher.donHangToiThieu > 0">
                                Đơn tối thiểu {{ formatPrice(voucher.donHangToiThieu) }}
                            </p>
                        </div>

                        <div class="voucher-divider"></div>

                        <div class="voucher-body">
                            <h5 class="voucher-name">{{ voucher.tenPhieuGiamGia || 'Mã ưu đãi CozyPot' }}</h5>

                            <div class="voucher-code-box" @click="copyCode(voucher.codeGiamGia)">
                                <span class="code-text">{{ voucher.codeGiamGia }}</span>
                                <i class="far fa-copy ms-2 text-muted"></i>
                            </div>

                            <div class="voucher-time">
                                <i class="far fa-clock me-1"></i> HSD: {{ formatDate(voucher.ngayKetThuc) }}
                            </div>

                            <button class="btn-use-voucher w-100 mt-3" @click="applyVoucher(voucher)">
                                DÙNG NGAY
                            </button>
                        </div>

                    </div>
                </div>
            </TransitionGroup>

        </div>
    </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
    --primary-red: #7d161a;
}

.text-primary-red {
    color: #7d161a !important;
}

.customer-promo-page {
    background-color: #f9f9f9;
    min-height: 100vh;
    padding-bottom: 90px;
}

/* --- BANNER --- */
.promo-banner {
    background: linear-gradient(135deg, #7d161a 0%, #ba2c26 100%);
    padding: 60px 20px 80px 20px;
    position: relative;
    overflow: hidden;
}

.promo-banner::before {
    content: "";
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyMCIgaGVpZ2h0PSIyMCI+PGNpcmNsZSBjeD0iMSIgY3k9IjEiIHI9IjEiIGZpbGw9InJnYmEoMjU1LDI1NSwyNTUsMC4xKSIvPjwvc3ZnPg==');
    opacity: 0.5;
}

.banner-content {
    position: relative;
    z-index: 2;
}

.banner-title {
    color: white;
    font-weight: 800;
    font-size: 2.5rem;
    text-transform: uppercase;
    margin-bottom: 10px;
    text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.banner-subtitle {
    color: #fdf2f2;
    font-size: 1.1rem;
    font-weight: 500;
    margin: 0;
}

.promo-container {
    margin-top: -40px;
    /* Kéo khung filter đè lên banner */
    position: relative;
    z-index: 10;
}

/* --- FILTER BAR --- */
.search-filter-bar {
    background: white;
    padding: 15px;
    border-radius: 16px;
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.08);
    border: 1px solid #eee;
}

.filter-scroll-wrapper {
    display: flex;
    overflow-x: auto;
    gap: 10px;
    scrollbar-width: none;
}

.filter-scroll-wrapper::-webkit-scrollbar {
    display: none;
}

.btn-filter {
    background: #f5f5f5;
    border: 1px solid #eaeaea;
    color: #555;
    padding: 10px 20px;
    border-radius: 30px;
    font-size: 14px;
    font-weight: 600;
    white-space: nowrap;
    transition: all 0.3s ease;
}

.btn-filter:hover {
    background: #fdf2f2;
    color: #7d161a;
    border-color: #fbdada;
}

.btn-filter.active {
    background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%);
    color: white;
    border-color: transparent;
    box-shadow: 0 4px 12px rgba(125, 22, 26, 0.25);
}

.sort-select {
    max-width: 250px;
    color: #495057;
    border-radius: 8px;
    border: 1px solid #ddd;
}

/* --- VOUCHER CARD (TICKET STYLE) --- */
.voucher-card {
    background: #fff;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    overflow: hidden;
    border: 1px solid rgba(0, 0, 0, 0.03);
    height: 100%;
}

.voucher-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 25px rgba(125, 22, 26, 0.15);
}

.voucher-header {
    padding: 20px;
    text-align: center;
    position: relative;
    color: white;
}

.bg-cash {
    background: linear-gradient(135deg, #d32f2f 0%, #ba2c26 100%);
}

.bg-percent {
    background: linear-gradient(135deg, #f57c00 0%, #e65100 100%);
}

.badge-type {
    position: absolute;
    top: 10px;
    left: 10px;
    background: rgba(255, 255, 255, 0.2);
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 11px;
    font-weight: 700;
    backdrop-filter: blur(4px);
}

.discount-value {
    font-size: 2rem;
    font-weight: 800;
    margin: 10px 0 5px 0;
}

.min-order {
    font-size: 13px;
    opacity: 0.9;
    margin: 0;
}

/* Tạo đường răng cưa cắt ngang vé */
.voucher-divider {
    height: 20px;
    background-color: transparent;
    position: relative;
    margin: -10px 0;
    z-index: 1;
}

.voucher-divider::before,
.voucher-divider::after {
    content: "";
    position: absolute;
    width: 20px;
    height: 20px;
    background-color: #f9f9f9;
    /* Khớp với màu nền trang */
    border-radius: 50%;
    top: 0;
}

.voucher-divider::before {
    left: -10px;
    box-shadow: inset -1px 0 0 rgba(0, 0, 0, 0.03);
}

.voucher-divider::after {
    right: -10px;
    box-shadow: inset 1px 0 0 rgba(0, 0, 0, 0.03);
}

.voucher-divider {
    border-top: 2px dashed #e0e0e0;
    top: 10px;
}

.voucher-body {
    padding: 25px 20px 20px;
    display: flex;
    flex-direction: column;
    flex-grow: 1;
}

.voucher-name {
    font-size: 16px;
    font-weight: 700;
    color: #333;
    margin-bottom: 15px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.4;
}

.voucher-code-box {
    background: #fdf2f2;
    border: 1px dashed #7d161a;
    padding: 8px 15px;
    border-radius: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    cursor: pointer;
    margin-bottom: 15px;
    transition: 0.2s;
}

.voucher-code-box:hover {
    background: #fbdada;
}

.code-text {
    font-weight: 800;
    color: #7d161a;
    font-family: monospace;
    font-size: 15px;
    letter-spacing: 1px;
}

.voucher-time {
    font-size: 12px;
    color: #888;
    margin-bottom: auto;
    /* Đẩy button xuống đáy */
}

.btn-use-voucher {
    background: white;
    border: 2px solid #7d161a;
    color: #7d161a;
    font-weight: 700;
    padding: 10px;
    border-radius: 10px;
    transition: 0.3s;
}

.btn-use-voucher:hover {
    background: #7d161a;
    color: white;
}

/* ANIMATION */
.filter-list-enter-active,
.filter-list-leave-active {
    transition: all 0.5s cubic-bezier(0.55, 0, 0.1, 1);
}

.filter-list-enter-from,
.filter-list-leave-to {
    opacity: 0;
    transform: scale(0.9) translateY(30px);
}

.filter-list-leave-active {
    position: absolute;
    z-index: -1;
}

.input-group-text {
    border-radius: 10px 0 0 10px;
}

.form-control {
    border-radius: 0 10px 10px 0;
    font-size: 14px;
}

.form-control:focus {
    border-color: #dee2e6;
}

/* Đảm bảo thanh Filter không bị nhảy dòng trên mobile */
@media (max-width: 991px) {
    .sort-select {
        max-width: 100%;
    }
}

.tab-navigation {
  display: flex;
  justify-content: center;
}

.tab-pill {
  display: flex;
  background: #eee;
  padding: 5px;
  border-radius: 50px;
  position: relative;
  width: 100%;
  max-width: 400px;
}

.tab-pill button {
  flex: 1;
  border: none;
  background: none;
  padding: 10px 20px;
  font-weight: 700;
  font-size: 14px;
  color: #666;
  z-index: 2;
  transition: 0.3s;
  position: relative;
}

.tab-pill button.active {
  color: white;
}

/* Thanh slider màu đỏ chạy bên dưới */
.tab-slider {
  position: absolute;
  top: 5px;
  left: 5px;
  width: calc(50% - 5px);
  height: calc(100% - 100%); /* Sửa thành 100% - 10px */
  height: 38px; /* Khớp với nút */
  background: #7d161a;
  border-radius: 50px;
  transition: transform 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  z-index: 1;
}

.tab-slider.personal {
  transform: translateX(100%);
}

/* CSS cho list voucher trống trong tab cá nhân */
.personal-empty {
    padding: 40px;
    background: white;
    border-radius: 20px;
    border: 2px dashed #eee;
}
</style>