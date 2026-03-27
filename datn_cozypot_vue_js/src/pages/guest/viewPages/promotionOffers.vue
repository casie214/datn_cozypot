<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axiosClient from "@/services/axiosClient";
import Swal from "sweetalert2";

// --- 1. STATE QUẢN LÝ DỮ LIỆU ---
const vouchers = ref([]);
const isLoading = ref(false);
const activeTab = ref("campaign"); 
const filterType = ref("all");
const sortOption = ref("default");
const searchQuery = ref("");

// 🚨 STATE CHO MODAL CHI TIẾT ĐỢT GIẢM GIÁ
const isModalOpen = ref(false);
const selectedCampaign = ref(null);
const campaignItems = ref({
    sets: [],
    foods: []
});
const isLoadingItems = ref(false);

// --- 2. FETCH DỮ LIỆU TỪ API ---
const fetchData = async () => {
    isLoading.value = true;
    vouchers.value = []; 

    try {
        let endpoint = "";
        if (activeTab.value === "campaign") {
            endpoint = "/dot-khuyen-mai/active/guest"; // Sửa lại URL chuẩn Backend mày đang dùng
        } else if (activeTab.value === "public") {
            endpoint = "/phieu-giam-gia/public";
        } else {
            endpoint = "/phieu-giam-gia/ca-nhan";
        }

        const apiParams = { page: 0, size: 50 };

        if (activeTab.value === "personal") {
            const user = JSON.parse(localStorage.getItem('user') || '{}');
            const idKhach = user.idKhachHang || user.id; 
            
            if (idKhach) {
                apiParams.idKhachHang = idKhach;
            } else {
                throw { response: { status: 401 } }; 
            }
        }

        const response = await axiosClient.get(endpoint, { params: apiParams });
        const responseData = response.data || response;
        const rawContent = responseData.content || responseData || [];

        // 🚨 CHUẨN HÓA DATA CHO ĐỢT GIẢM GIÁ
        vouchers.value = rawContent.map(item => ({
            ...item,
            tenPhieuGiamGia: item.tenPhieuGiamGia || item.tenDotKhuyenMai || 'Chương trình ưu đãi',
            codeGiamGia: item.codeGiamGia || null, 
            
            // 🚨 BÙ LẠI 2 BIẾN MÀ BACKEND THIẾU ĐỂ VUE HIỂN THỊ ĐƯỢC GIAO DIỆN
            loaiGiamGia: item.loaiGiamGia !== undefined ? item.loaiGiamGia : 1, // Đợt KM mặc định là 1 (Giảm %)
            giaTriGiam: item.giaTriGiam !== undefined ? item.giaTriGiam : (item.phanTramGiam || 0)
        }));

    } catch (error) {
        console.error("Lỗi tải dữ liệu khuyến mãi:", error);
        if (error.response?.status === 401 && activeTab.value === "personal") {
            Swal.fire({
                icon: "warning",
                title: "Yêu cầu đăng nhập",
                text: "Vui lòng đăng nhập để xem các ưu đãi dành riêng cho bạn!",
                confirmButtonColor: "#7d161a",
            });
            activeTab.value = "campaign"; 
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

// --- 3. LOGIC XEM CHI TIẾT MÓN TRONG ĐỢT GIẢM GIÁ (MODAL) ---
const openCampaignModal = async (campaign) => {
    selectedCampaign.value = campaign;
    isModalOpen.value = true;
    isLoadingItems.value = true;
    campaignItems.value = { sets: [], foods: [] };

    try {
        // 🚨 TẠO MẢNG REQUEST: Dùng vòng lặp gọi đúng API có sẵn trong MonAnController của mày
        
        // 1. Quét mảng ID Set Lẩu -> Tạo ra một đống API GET /api/manage/food/hotpot/{id}
        const setRequests = (campaign.idSetLauChiTiet || []).map(id => 
            axiosClient.get(`/guest/${id}/chi-tiet`)
        );
        
        // 2. Quét mảng ID Món Lẻ -> Tạo ra một đống API GET /api/manage/food/{id}
        const foodRequests = (campaign.idMonAnChiTiet || []).map(id => 
            axiosClient.get(`/manage/food/${id}`)
        );

        // 🚨 THỰC THI: Bắn toàn bộ các API này lên Backend CÙNG 1 LÚC (Chạy song song)
        const [setResponses, foodResponses] = await Promise.all([
            Promise.all(setRequests),
            Promise.all(foodRequests)
        ]);

        // Lấy ruột data ra và nhét vào giao diện Modal
        campaignItems.value.sets = setResponses.map(res => res.data);
        campaignItems.value.foods = foodResponses.map(res => res.data);

    } catch (error) {
        console.error("Lỗi lấy danh sách món áp dụng:", error);
        Swal.fire({
            icon: "error",
            title: "Lỗi tải dữ liệu",
            text: "Không thể lấy thông tin chi tiết các món ăn, vui lòng thử lại sau.",
            toast: true,
            position: "top-end",
            showConfirmButton: false,
            timer: 3000
        });
    } finally {
        isLoadingItems.value = false;
    }
};

const closeCampaignModal = () => {
    isModalOpen.value = false;
    selectedCampaign.value = null;
};


// --- 4. LOGIC LỌC & SẮP XẾP ---
const displayedVouchers = computed(() => {
    let result = [...vouchers.value];

    const q = searchQuery.value.trim().toLowerCase();
    if (q) {
        result = result.filter((v) =>
            (v.tenPhieuGiamGia && v.tenPhieuGiamGia.toLowerCase().includes(q)) ||
            (v.codeGiamGia && v.codeGiamGia.toLowerCase().includes(q))
        );
    }

    if (filterType.value !== "all") {
        result = result.filter((v) => String(v.loaiGiamGia) === filterType.value);
    }

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


// --- 5. UTILS & ACTIONS ---
const formatPrice = (value) => {
    if (!value) return "0 ₫";
    return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND",
    }).format(value);
};

const calculateDiscountedPrice = (originalPrice) => {
    if (!originalPrice) return 0;
    if (!selectedCampaign.value) return originalPrice;

    let finalPrice = originalPrice;
    if (selectedCampaign.value.loaiGiamGia === 1) { 
        // Giảm theo %
        finalPrice = originalPrice - (originalPrice * selectedCampaign.value.giaTriGiam / 100);
    } else { 
        // Giảm theo tiền mặt
        finalPrice = originalPrice - selectedCampaign.value.giaTriGiam;
    }
    return finalPrice < 0 ? 0 : finalPrice;
};

const isSetDetailModalOpen = ref(false);
const selectedSetDetail = ref(null);

const openSetDetail = (set) => {
    selectedSetDetail.value = set;
    isSetDetailModalOpen.value = true;
};

const closeSetDetailModal = () => {
    isSetDetailModalOpen.value = false;
    selectedSetDetail.value = null;
};

const formatDate = (dateString) => {
    if (!dateString) return "";
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
    });
};

const getImg = (url) => {
  if (!url) return "";
  if (url.startsWith("http") || url.startsWith("data:image")) return url;
  return `http://localhost:8080/uploads/${url}`;
};

const copyCode = (code) => {
    navigator.clipboard.writeText(code);
    Swal.fire({
        icon: "success",
        title: "Đã sao chép mã!",
        toast: true,
        position: "top-end",
        showConfirmButton: false,
        timer: 2000,
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
                <p class="banner-subtitle">Khám phá các chương trình khuyến mãi và tận hưởng mức giá siêu hời!</p>
            </div>
        </div>

        <div v-if="isLoading" class="text-center py-5 mt-5">
            <div class="spinner-border text-primary-red" role="status"></div>
            <p class="mt-2 text-muted">Đang tải danh sách ưu đãi...</p>
        </div>

        <div v-else class="container promo-container">
            <div class="tab-navigation mb-4">
                <div class="tab-pill">
                    <button :class="{ active: activeTab === 'campaign' }" @click="activeTab = 'campaign'">
                        <i class="fas fa-fire me-2"></i>Đang diễn ra
                    </button>
                    <button :class="{ active: activeTab === 'public' }" @click="activeTab = 'public'">
                        <i class="fas fa-globe-asia me-2"></i>Mã dùng chung
                    </button>
                    <button :class="{ active: activeTab === 'personal' }" @click="activeTab = 'personal'">
                        <i class="fas fa-user-tag me-2"></i>Mã của tôi
                    </button>
                    <div class="tab-slider" :class="activeTab"></div>
                </div>
            </div>

            <div class="search-filter-bar bg-white p-3 mb-4 rounded-4 border shadow-sm sticky-top"
                style="top: 20px; z-index: 990">
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
                                {{ voucher.loaiGiamGia === 1 ? voucher.giaTriGiam + '%' : formatPrice(voucher.giaTriGiam) }}
                            </h3>
                            <p class="min-order" v-if="voucher.donHangToiThieu > 0">
                                Đơn tối thiểu {{ formatPrice(voucher.donHangToiThieu) }}
                            </p>
                        </div>

                        <div class="voucher-divider"></div>

                        <div class="voucher-body">
                            <h5 class="voucher-name">{{ voucher.tenPhieuGiamGia }}</h5>

                            <div class="voucher-code-box" v-if="voucher.codeGiamGia" @click="copyCode(voucher.codeGiamGia)">
                                <span class="code-text">{{ voucher.codeGiamGia }}</span>
                                <i class="far fa-copy ms-2 text-muted"></i>
                            </div>
                            <div v-else class="voucher-auto-apply text-success fw-bold mb-3 d-flex align-items-center bg-light p-2 rounded">
                                <i class="fas fa-check-circle me-2"></i> Tự động giảm giá
                            </div>

                            <div class="voucher-time">
                                <i class="far fa-clock me-1"></i> {{ formatDate(voucher.ngayBatDau) }} - {{ formatDate(voucher.ngayKetThuc) }}
                            </div>

                            <button v-if="!voucher.codeGiamGia" class="btn-outline-red w-100 mt-3" @click="openCampaignModal(voucher)">
                                <i class="fas fa-search-plus me-1"></i> Xem danh sách món áp dụng
                            </button>

                        </div>
                    </div>
                </div>
            </TransitionGroup>
        </div>

        <Transition name="modal-fade">
            <div v-if="isModalOpen" class="campaign-modal-overlay" @click.self="closeCampaignModal">
                <div class="campaign-modal-content custom-scrollbar">
                    
                    <div class="c-modal-header text-white" :class="selectedCampaign?.loaiGiamGia === 1 ? 'bg-percent' : 'bg-cash'">
                        <button class="btn-close-modal" @click="closeCampaignModal">
                            <i class="fas fa-times"></i>
                        </button>
                        <h4 class="mb-1 text-uppercase fw-bold">{{ selectedCampaign?.tenPhieuGiamGia }}</h4>
                        <div class="badge bg-white text-dark fw-bold px-3 py-1 mt-2 fs-6">
                            GIẢM {{ selectedCampaign?.loaiGiamGia === 1 ? selectedCampaign?.giaTriGiam + '%' : formatPrice(selectedCampaign?.giaTriGiam) }}
                        </div>
                    </div>

                    <div class="c-modal-body p-4">
                        <p class="text-muted fst-italic mb-4 text-center">
                            {{ selectedCampaign?.moTa || 'Chương trình áp dụng tự động cho các món dưới đây:' }}
                        </p>

                        <div v-if="isLoadingItems" class="text-center py-4">
                            <div class="spinner-border text-primary-red mb-2" role="status"></div>
                            <p class="text-muted">Đang tải danh sách món...</p>
                        </div>

                        <div v-else>
                            <div v-if="campaignItems.sets.length > 0" class="mb-4">
                                <h6 class="fw-bold text-dark border-bottom pb-2 mb-3">
                                    <i class="fas fa-fire text-danger me-2"></i>Các Set Lẩu Áp Dụng
                                </h6>
                                <div class="list-group">
                                    <div v-for="set in campaignItems.sets" :key="set.id" 
                                         class="list-group-item d-flex align-items-center py-3 border-0 bg-light rounded mb-2"
                                         style="cursor: pointer; transition: 0.2s;"
                                         @click="openSetDetail(set)"
                                         onmouseover="this.style.background='#f0f0f0'"
                                         onmouseout="this.style.background='#f8f9fa'">
                                        <img :src="getImg(set.hinhAnh)" class="rounded-circle me-3" style="width: 50px; height: 50px; object-fit: cover;" alt="img" />
                                        <div class="flex-grow-1">
                                            <div class="fw-bold">{{ set.tenSetLau }}</div>
                                            <div class="d-flex align-items-center gap-2 mt-1">
                                                <span class="text-danger fw-bold">{{ formatPrice(calculateDiscountedPrice(set.giaGoc || set.giaBan)) }}</span>
                                                <span class="text-muted small text-decoration-line-through">{{ formatPrice(set.giaGoc || set.giaBan) }}</span>
                                            </div>
                                        </div>
                                        <i class="fas fa-chevron-right text-muted opacity-50"></i>
                                    </div>
                                </div>
                            </div>

                            <div v-if="campaignItems.foods.length > 0">
                                <h6 class="fw-bold text-dark border-bottom pb-2 mb-3">
                                    <i class="fas fa-utensils text-danger me-2"></i>Các Món Gọi Thêm Áp Dụng
                                </h6>
                                <div class="list-group">
                                    <div v-for="food in campaignItems.foods" :key="food.id" class="list-group-item d-flex align-items-center py-3 border-0 bg-light rounded mb-2">
                                        <img :src="getImg(food.hinhAnh)" class="rounded-circle me-3" style="width: 50px; height: 50px; object-fit: cover;" alt="img" />
                                        <div>
                                            <div class="fw-bold">{{ food.tenMon }}</div>
                                            <div class="text-muted small mb-1">{{ food.tenDinhLuong }}</div>
                                            <div class="d-flex align-items-center gap-2">
                                                <span class="text-danger fw-bold">{{ formatPrice(calculateDiscountedPrice(food.giaGoc || food.giaBan || food.donGia)) }}</span>
                                                <span class="text-muted small text-decoration-line-through">{{ formatPrice(food.giaGoc || food.giaBan || food.donGia) }}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div v-if="campaignItems.sets.length === 0 && campaignItems.foods.length === 0" class="text-center py-4">
                                <i class="fas fa-box-open fa-3x text-muted mb-3 opacity-25"></i>
                                <p class="text-muted">Hiện tại đợt giảm giá này chưa áp dụng cho món nào.</p>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </Transition>
        </div>

        <Transition name="modal-fade">
            <div v-if="isSetDetailModalOpen" class="campaign-modal-overlay" style="z-index: 2010;" @click.self="closeSetDetailModal">
                <div class="campaign-modal-content custom-scrollbar" style="max-height: 70vh; width: 85%; max-width: 450px;">
                    
                    <div class="p-3 border-bottom d-flex justify-content-between align-items-center bg-light sticky-top">
                        <h6 class="mb-0 fw-bold text-dark">
                            <i class="fas fa-info-circle text-danger me-2"></i>Chi tiết {{ selectedSetDetail?.tenSetLau }}
                        </h6>
                        <button class="btn btn-sm btn-outline-secondary rounded-circle" @click="closeSetDetailModal" style="width: 30px; height: 30px; padding: 0;">
                            <i class="fas fa-times"></i>
                        </button>
                    </div>

                    <div class="p-3">
                        <p class="text-muted small mb-3 fst-italic">{{ selectedSetDetail?.moTa || 'Set lẩu bao gồm các món:' }}</p>
                        
                        <div class="list-group list-group-flush">
                            <div v-for="(item, index) in selectedSetDetail?.listChiTietSetLau" :key="index" 
                                 class="list-group-item d-flex justify-content-between align-items-center px-0 py-2">
                                <div class="d-flex align-items-center">
                                    <i class="fas fa-check text-success me-2 small"></i>
                                    <div>
                                        <span class="fw-bold fs-6 text-dark">{{ item.danhMucChiTiet?.tenMon || item.tenMon }}</span>
                                        <div class="text-muted small" style="font-size: 12px;">{{ item.danhMucChiTiet?.tenDinhLuong || item.dinhLuong }}</div>
                                    </div>
                                </div>
                                <span class="badge bg-danger rounded-pill px-2 py-1">x{{ item.soLuong }}</span>
                            </div>

                            <div v-if="!selectedSetDetail?.listChiTietSetLau || selectedSetDetail.listChiTietSetLau.length === 0" class="text-center text-muted py-3">
                                Đang cập nhật chi tiết món...
                            </div>
                        </div>
                    </div>

                </div>
            </div>
        </Transition>
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
    margin-top: auto; /* Đẩy thời gian xuống đáy sau khi bỏ nút */
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
  max-width: 500px; /* Tăng chiều rộng lên chút vì có 3 chữ */
}

.tab-pill button {
  flex: 1;
  border: none;
  background: none;
  padding: 10px 10px;
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

/* 🚨 CHIA LẠI SLIDER CHO 3 TABS */
.tab-slider {
  position: absolute;
  top: 5px;
  left: 5px;
  width: calc(33.333% - 3.33px); /* Chia 3 phần và trừ đi padding */
  height: 38px;
  background: #7d161a;
  border-radius: 50px;
  transition: transform 0.3s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  z-index: 1;
}

.tab-slider.campaign {
  transform: translateX(0%);
}

.tab-slider.public {
  transform: translateX(100%);
}

.tab-slider.personal {
  transform: translateX(200%);
}

.personal-empty {
    padding: 40px;
    background: white;
    border-radius: 20px;
    border: 2px dashed #eee;
}

.btn-outline-red {
    background: transparent;
    color: #7d161a;
    border: 1px solid #7d161a;
    padding: 8px;
    border-radius: 8px;
    font-weight: 600;
    transition: 0.3s;
}

.btn-outline-red:hover {
    background: #fdf2f2;
}

/* CSS Modal */
.campaign-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.6);
    z-index: 2000;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(3px);
}

.campaign-modal-content {
    background: white;
    width: 90%;
    max-width: 500px;
    max-height: 85vh;
    border-radius: 20px;
    display: flex;
    flex-direction: column;
    overflow-y: auto;
    position: relative;
    box-shadow: 0 10px 40px rgba(0,0,0,0.3);
}

.c-modal-header {
    padding: 30px 20px;
    text-align: center;
    position: sticky;
    top: 0;
    z-index: 10;
}

.btn-close-modal {
    position: absolute;
    top: 15px;
    right: 15px;
    width: 32px;
    height: 32px;
    background: rgba(255,255,255,0.2);
    color: white;
    border: none;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: 0.2s;
}

.btn-close-modal:hover {
    background: rgba(255,255,255,0.4);
    transform: scale(1.1);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
    transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
    opacity: 0;
}
</style>