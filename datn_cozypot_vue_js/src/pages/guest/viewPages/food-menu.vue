<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import axiosClient from '@/services/axiosClient';

// --- STATE ---
const categories = ref([]);
const menuData = ref([]);
const isLoading = ref(false);
const cart = ref([]);
const activeCategory = ref('');
const searchQuery = ref('')

const fetchData = async () => {
    isLoading.value = true;
    try {
        const [resDanhMuc, resMonAn, resSetLau, resChiTiet, resLoaiSet, resFoodDetail] = await Promise.all([
            axiosClient.get('/guest/category/active'),
            axiosClient.get('/guest/food/active'),
            axiosClient.get('/guest/hotpot/active'),
            axiosClient.get('/guest/category-detail/active'),
            axiosClient.get('/guest/hotpot-type/active'),
            axiosClient.get('/guest/food-detail/active')
        ]);

        const listDanhMuc = resDanhMuc.data || [];
        const listMonAn = resMonAn.data || [];
        const listSetLau = resSetLau.data || [];
        const listChiTiet = resChiTiet.data || [];
        const listLoaiSet = resLoaiSet.data || [];
        const listFoodDetail = resFoodDetail.data || [];


        const subToRootMap = {};
        const subNameMap = {};
        const rootToSubsList = {};

        const priceMap = {};

        listChiTiet.forEach(sub => {
            subToRootMap[sub.id] = sub.idDanhMuc;
            subNameMap[sub.id] = sub.tenDanhMucChiTiet;

            if (!rootToSubsList[sub.idDanhMuc]) {
                rootToSubsList[sub.idDanhMuc] = [];
            }
            rootToSubsList[sub.idDanhMuc].push({
                id: sub.id,
                name: sub.tenDanhMucChiTiet
            });
        });

        listFoodDetail.forEach(detail => {
            const foodId = detail.idMonAnDiKem;
            const price = detail.giaBan;

            if (!priceMap[foodId]) {
                priceMap[foodId] = { min: price, max: price, hasDetail: true };
            } else {
                priceMap[foodId].min = Math.min(priceMap[foodId].min, price);
                priceMap[foodId].max = Math.max(priceMap[foodId].max, price);
            }
        });

        const processedMenu = [];

        if (listSetLau.length > 0) {
            const hotpotFilters = [
                { id: 'all', name: 'Tất cả' },
                ...listLoaiSet.map(ls => ({ id: ls.id, name: ls.tenLoaiSet }))
            ];

            processedMenu.push({
                categoryId: 'combo-set',
                categoryName: 'SET LẨU',
                activeFilter: 'all',
                filters: hotpotFilters,
                items: listSetLau.map(item => ({
                    id: item.id,
                    name: item.tenSetLau,
                    price: item.giaBan,
                    image: item.hinhAnh || 'https://placehold.co/400x300?text=Combo',
                    desc: item.moTa || 'Set lẩu đầy đặn, phù hợp cho nhóm.',
                    type: 'SET',
                    groupId: item.idLoaiSet,
                    isRange: false
                }))
            });
        }

        // --- B. XỬ LÝ MÓN ĂN (FOOD) ---
        listDanhMuc.forEach(dm => {
            // Lọc các món thuộc danh mục cha này
            const monsInCat = listMonAn.filter(m => {
                const rootIdOfFood = subToRootMap[m.idDanhMucChiTiet];
                return String(rootIdOfFood) === String(dm.id);
            });

            if (monsInCat.length > 0) {
                // Lấy danh sách bộ lọc con tương ứng với danh mục cha này
                const subFilters = rootToSubsList[dm.id]
                    ? [{ id: 'all', name: 'Tất cả' }, ...rootToSubsList[dm.id]]
                    : [];

                processedMenu.push({
                    categoryId: `cat-${dm.id}`,
                    categoryName: dm.tenDanhMuc.toUpperCase(),
                    activeFilter: 'all',
                    filters: subFilters,
                    items: monsInCat.map(item => {

                        const detailPrice = priceMap[item.id];
                        let finalPrice = item.giaBan; // Mặc định lấy giá gốc
                        let isRange = false;

                        if (detailPrice) {
                            if (detailPrice.min !== detailPrice.max) {
                                isRange = true; // Đánh dấu là khoảng giá
                                finalPrice = { min: detailPrice.min, max: detailPrice.max };
                            } else {
                                finalPrice = detailPrice.min; // Nếu min == max thì hiện 1 giá
                            }
                        }
                        return {

                            id: item.id,
                            name: item.tenMonAn,
                            price: finalPrice,
                            image: item.hinhAnh || 'https://placehold.co/300x200?text=Mon+An',
                            desc: item.moTa || '',
                            type: 'MON',
                            isRange: isRange,
                            subCategoryName: subNameMap[item.idDanhMucChiTiet] || '',
                            groupId: item.idDanhMucChiTiet 
                        };
                    })
                });
            }
        });

        menuData.value = processedMenu;

        // Tạo Nav Categories
        categories.value = processedMenu.map(section => ({
            id: section.categoryId,
            name: section.categoryName
        }));

        if (categories.value.length > 0) activeCategory.value = categories.value[0].id;

    } catch (error) {
        console.error("Lỗi tải thực đơn:", error);
    } finally {
        isLoading.value = false;
    }
};

// --- HÀM LỌC HIỂN THỊ ---
// Hàm này được gọi trong v-for để trả về danh sách món đã lọc
const getFilteredItems = (section) => {
    if (section.activeFilter === 'all') {
        return section.items;
    }
    // Lọc theo groupId (đã map ở trên: là idLoaiSet hoặc idDanhMucChiTiet)
    return section.items.filter(item => item.groupId === section.activeFilter);
};

// Hàm xử lý khi bấm nút Toggle
const selectFilter = (section, filterId) => {
    section.activeFilter = filterId;
};

// ... (Các hàm cart, formatPrice, scrollSpy GIỮ NGUYÊN như cũ) ...
const addToCart = (item) => {
    const existing = cart.value.find(i => i.id === item.id && i.type === item.type);
    if (existing) existing.quantity++;
    else cart.value.push({ ...item, quantity: 1 });
};
const totalCount = computed(() => cart.value.reduce((sum, item) => sum + item.quantity, 0));
const totalPrice = computed(() => cart.value.reduce((sum, item) => sum + (item.price * item.quantity), 0));
const formatPrice = (value) => {
    if (typeof value !== 'number' || isNaN(value)) {
        if (typeof value === 'object' && value.min) return formatPrice(value.min);
        return '0 ₫';
    }
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};
const pageContainer = ref(null);
const scrollToCategory = (id) => {
    activeCategory.value = id;
    const element = document.getElementById(id);
    if (element) element.scrollIntoView({ behavior: 'smooth', block: 'start' });
};

const allSearchItem = computed(() => {
    if (!menuData.value) {
        return [];
    }
    return menuData.value.flatMap(section => section.items);
})

const searchResult = computed(() => {
    if (!searchQuery.value || searchQuery.value.trim() === '') {
        return [];
    }
    const query = searchQuery.value.trim().toLowerCase();
    return allSearchItem.value.filter(item => {
        const matchName = item.name.toLowerCase().includes(query);
        return matchName;
    })
})

const clearSearch = () => {
    searchQuery.value = '';
}

const onScroll = () => {
    const offset = 150;
    for (const cat of categories.value) {
        const element = document.getElementById(cat.id);
        if (element) {
            const rect = element.getBoundingClientRect();
            if (rect.top <= offset && rect.bottom > offset) {
                if (activeCategory.value !== cat.id) {
                    activeCategory.value = cat.id;
                    const navItem = document.getElementById(`nav-item-${cat.id}`);
                    if (navItem) navItem.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
                }
                break;
            }
        }
    }
};
onMounted(() => {
    fetchData();
    if (pageContainer.value) pageContainer.value.addEventListener('scroll', onScroll);
});
onUnmounted(() => {
    if (pageContainer.value) pageContainer.value.removeEventListener('scroll', onScroll);
});
</script>

<template>
    <div class="customer-menu-page">
        <div v-if="isLoading" class="text-center py-5 mt-5">
            <div class="spinner-border text-primary-red" role="status"></div>
            <p class="mt-2 text-muted">Đang tải thực đơn</p>
        </div>

        <div v-else>
            <nav class="category-nav">
                <div class="container d-flex align-items-center justify-content-between h-100">

                    <div class="nav-scroll-container">
                        <ul class="nav-list">
                            <li v-for="cat in categories" :key="cat.id" :id="`nav-item-${cat.id}`" class="nav-item"
                                :class="{ active: activeCategory === cat.id }" @click="scrollToCategory(cat.id)">
                                {{ cat.name }}
                            </li>
                        </ul>
                    </div>

                    <div class="search-wrapper ms-3" style="position: relative; z-index: 1050;">

                        <div class="search-input-group d-flex align-items-center bg-white border rounded-pill px-3 py-1"
                            style="width: 250px;">
                            <input type="text" class="form-control border-0 shadow-none p-0" placeholder="Tìm món..."
                                v-model="searchQuery">
                            <i v-if="searchQuery" class="fas fa-times text-muted cursor-pointer ms-2"
                                @click="clearSearch"></i>
                            <i v-else class="fas fa-search text-muted ms-2"></i>
                        </div>

                        <div v-if="searchQuery" class="search-dropdown shadow-lg animate__animated animate__fadeIn">
                            <div v-if="searchResult.length > 0" class="search-results-list custom-scrollbar">
                                <div v-for="item in searchResult" :key="item.id + item.type" class="search-item"
                                    @click="addToCart(item)">

                                    <div class="search-img-box">
                                        <img :src="item.image" :alt="item.name">
                                        <span v-if="item.type === 'SET'" class="badge-mini-set">SET</span>
                                    </div>

                                    <div class="search-item-info">
                                        <div class="d-flex align-items-center justify-content-between">
                                            <h6 class="search-item-name">{{ item.name }}</h6>
                                        </div>
                                        <p class="search-item-desc text-muted mb-1"
                                            style="font-size: 11px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                            {{ item.desc }}</p>
                                        <div class="d-flex justify-content-between align-items-center mt-1">
                                            <span class="search-item-price">
                                                <template v-if="item.isRange">
                                                    {{ formatPrice(item.price.min) }} - {{ formatPrice(item.price.max)
                                                    }}
                                                </template>

                                                <template v-else>
                                                    {{ formatPrice(item.price) }}
                                                </template>
                                            </span>
                                            <button class="btn-search-add"><i class="fas fa-plus"></i></button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-else class="text-center py-4 text-muted">
                                <i class="fas fa-search mb-2" style="font-size: 20px; opacity: 0.5;"></i>
                                <p class="mb-0 small">Không tìm thấy món nào.</p>
                            </div>
                        </div>
                    </div>

                </div>
            </nav>



            <div class="main-content container py-4">
                <div v-for="section in menuData" :key="section.categoryId" :id="section.categoryId"
                    class="menu-section mb-5 animate__animated animate__fadeIn">

                    <h3 class="section-title">{{ section.categoryName }}</h3>

                    <div class="filter-bar mb-3" v-if="section.filters && section.filters.length > 1">
                        <div class="filter-scroll-wrapper">
                            <button v-for="filter in section.filters" :key="filter.id" class="btn-filter"
                                :class="{ active: section.activeFilter === filter.id }"
                                @click="selectFilter(section, filter.id)">
                                {{ filter.name }}
                            </button>
                        </div>
                    </div>
                    <TransitionGroup name="filter-list" tag="div" class="row g-3">

                        <div v-for="item in getFilteredItems(section)" :key="item.id" :class="[
                            'col-6',
                            section.categoryId === 'combo-set' ? 'col-md-4 col-lg-3' : 'col-md-3 col-lg-2'
                        ]">
                            <div class="food-card">
                                <div class="card-img-wrapper"
                                    :class="{ 'square-ratio': section.categoryId !== 'combo-set' }">
                                    <img :src="item.image" :alt="item.name" loading="lazy" />
                                    <div v-if="section.categoryId === 'combo-set'" class="combo-badge">HOT</div>
                                </div>
                                <div class="card-body">
                                    <div v-if="item.subCategoryName" class="sub-cat-name">{{ item.subCategoryName }}
                                    </div>
                                    <h5 class="food-name" :class="{ 'text-small': section.categoryId !== 'combo-set' }">
                                        {{ item.name }}</h5>
                                    <p v-if="section.categoryId === 'combo-set'" class="food-desc">{{ item.desc }}</p>
                                    <div class="card-footer-action">
                                        <span class="food-price">
                                            <span class="food-price" :class="section.categoryId === 'combo-set' ? 'food-price-lg' : ''">
                                                <template v-if="item.isRange">
                                                    {{ formatPrice(item.price.min) }} - {{ formatPrice(item.price.max)
                                                    }}
                                                </template>
                                                <template v-else>
                                                    {{ formatPrice(item.price) }}
                                                </template>
                                            </span>

                                        </span>
                                        <button class="btn-add" @click="addToCart(item)"><i
                                                class="fas fa-plus"></i></button>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </TransitionGroup>
                    <div v-if="getFilteredItems(section).length === 0" class="text-center py-4 text-muted w-100">
                        Không tìm thấy món nào.
                    </div>
                </div>
            </div>
        </div>

        <div v-if="cart.length > 0" class="floating-cart animate__animated animate__fadeInUp">
            <div class="cart-info">
                <span class="cart-count"><i class="fas fa-shopping-cart me-1"></i> {{ totalCount }} món</span>
                <span class="cart-total">{{ formatPrice(totalPrice) }}</span>
            </div>
            <button class="btn-view-cart">Xác nhận <i class="fas fa-arrow-right ms-1"></i></button>
        </div>
    </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.menu-section {
    /* 👇 Dòng này cực quan trọng: 
       Khoảng cách = Header (60px) + Nav (60px) + Padding dư ra (20px) = 140px */
    scroll-margin-top: 140px;

    /* Giữ nguyên các thuộc tính cũ */
    margin-bottom: 3rem;
}

:root {
    --primary-red: #7d161a;
    --light-red: #f8ecec;
    --text-white: #ffffff;
    --text-dark: #333;
    --bg-gray: #f4f4f4;
}

.text-primary-red {
    color: #7d161a !important;
}

.customer-menu-page {
    background-color: #f9f9f9;
    min-height: 100vh;
    padding-bottom: 90px;
}

.main-header {
    background-color: #7d161a;
    height: 60px;
    position: sticky;
    top: 0;
    z-index: 1001;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.category-nav {
    background-color: #ffffff;
    position: sticky;
    top: 0px;
    z-index: 1000;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    white-space: nowrap;
    border-bottom: 1px solid #eee;
}

.nav-scroll-container {
    flex: 1;
    /* Chiếm hết chỗ trống bên trái */
    overflow-x: auto;
    /* Cuộn ngang */
    white-space: nowrap;
    height: 100%;
    display: flex;
    align-items: center;
    /* Ẩn thanh cuộn */
    scrollbar-width: none;
}

.nav-scroll-container::-webkit-scrollbar {
    display: none;
}

.search-wrapper {
    /* Đảm bảo search luôn nổi lên trên */
    position: relative;
    z-index: 2000;
    /* Cao hơn nội dung bên dưới */
}

.category-nav::-webkit-scrollbar {
    display: none;
}

.nav-list {
    display: flex;
    list-style: none;
    padding: 0;
    margin: 0;
}

.nav-item {
    padding: 15px 20px;
    font-weight: 600;
    font-size: 14px;
    color: #666;
    cursor: pointer;
    text-transform: uppercase;
    transition: all 0.3s;
    border-bottom: 3px solid transparent;
    user-select: none;
}

.nav-item:hover {
    color: #7d161a;
    background-color: #fdf2f2;
}

.nav-item.active {
    color: #7d161a;
    background-color: #fff5f5;
    border-bottom: 3px solid #7d161a;
    font-weight: 800;
}

.section-title {
    color: #7d161a;
    font-weight: 800;
    font-size: 1.4rem;
    margin-bottom: 20px;
    padding-bottom: 5px;
    text-transform: uppercase;
    border-left: 5px solid #7d161a;
    padding-left: 10px;
}

.food-card {
    background: white;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    height: 100%;
    display: flex;
    flex-direction: column;
    border: 1px solid #f0f0f0;
    transition: transform 0.2s, box-shadow 0.2s;
}

.food-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(125, 22, 26, 0.1);
    border-color: #eecaca;
}

.card-img-wrapper {
    position: relative;
    padding-top: 66%;
    overflow: hidden;
}

.card-img-wrapper img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s;
}

.food-card:hover .card-img-wrapper img {
    transform: scale(1.05);
}

.combo-badge {
    position: absolute;
    top: 10px;
    left: 10px;
    background: #d32f2f;
    color: white;
    font-size: 11px;
    font-weight: 800;
    padding: 4px 10px;
    border-radius: 20px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.card-body {
    padding: 12px;
    display: flex;
    flex-direction: column;
    flex-grow: 1;
}

.food-name {
    font-size: 15px;
    font-weight: 700;
    color: #333;
    margin-bottom: 4px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    min-height: 40px;
}

.food-desc {
    font-size: 12px;
    color: #888;
    margin-bottom: 10px;
    flex-grow: 1;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.4;
}

.card-footer-action {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: auto;
}

.food-price {
    font-weight: 800;
    color: #7d161a;
    font-size: 13px;
}

.food-price-lg {
    font-weight: 800;
    color: #7d161a;
    font-size: 16px !important;
}

.btn-add {
    background: #fff;
    border: 1px solid #7d161a;
    border-radius: 50%;
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #7d161a;
    cursor: pointer;
    transition: all 0.2s;
}

.btn-add:hover {
    background: #7d161a;
    color: white;
    transform: rotate(90deg);
}

.btn-add:active {
    transform: scale(0.9);
}

.floating-cart {
    position: fixed;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    width: 90%;
    max-width: 500px;
    background-color: #7d161a;
    color: white;
    border-radius: 50px;
    padding: 10px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 8px 25px rgba(125, 22, 26, 0.4);
    z-index: 1100;
    cursor: pointer;
    border: 2px solid rgba(255, 255, 255, 0.2);
}

.cart-info {
    display: flex;
    flex-direction: column;
}

.cart-count {
    font-size: 12px;
    opacity: 0.9;
    font-weight: 500;
}

.cart-total {
    font-weight: 800;
    font-size: 16px;
    color: #fff;
}

.btn-view-cart {
    background: white;
    border: none;
    color: #7d161a;
    font-weight: bold;
    font-size: 14px;
    padding: 8px 16px;
    border-radius: 20px;
}

@media (max-width: 768px) {
    .category-nav {
        top: 60px;
    }

    .main-header {
        height: 60px;
    }

    .food-card {
        border-radius: 12px;
    }

    .nav-item {
        padding: 12px 15px;
        font-size: 13px;
    }
}

.card-img-wrapper.square-ratio {
    padding-top: 100%;
}

.food-name.text-small {
    font-size: 14px;
    min-height: 36px;
}


.col-lg-2 .card-body {
    padding: 8px;
}

.col-lg-2 .btn-add {
    width: 28px;
    height: 28px;
    font-size: 12px;
}

.sub-cat-name {
    font-size: 11px;
    color: #999;
    text-transform: uppercase;
    font-weight: 600;
    margin-bottom: 2px;
    letter-spacing: 0.5px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.food-name {
    margin-top: 0;
}

.filter-bar {
    position: relative;
    margin-left: -10px;
    margin-right: -10px;
}

.filter-scroll-wrapper {
    display: flex;
    overflow-x: auto;
    gap: 8px;
    padding: 5px 10px;
    scrollbar-width: none;
}

.filter-scroll-wrapper::-webkit-scrollbar {
    display: none;
}

.btn-filter {
    background-color: #fff;
    border: 1px solid #e0e0e0;
    color: #666;
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
    white-space: nowrap;
    cursor: pointer;
    transition: all 0.2s;
    outline: none;
}

.btn-filter:hover {
    background-color: #f5f5f5;
    border-color: #ccc;
}


.btn-filter.active {
    background-color: #7d161a;
    color: white;
    border-color: #7d161a;
    box-shadow: 0 2px 6px rgba(125, 22, 26, 0.3);
}

.filter-list-enter-active,
.filter-list-leave-active {
    transition: all 0.5s cubic-bezier(0.55, 0, 0.1, 1);
}

.filter-list-enter-from,
.filter-list-leave-to {
    opacity: 0;
    transform: scale(0.9) translateY(30px);
}

.filter-list-move {
    transition: transform 0.5s cubic-bezier(0.55, 0, 0.1, 1);
}

.filter-list-leave-active {
    position: absolute;
    z-index: -1;
}

.search-img-box {
    position: relative;
    width: 50px;
    height: 50px;
    margin-right: 10px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
    /* Không bị co lại */
}

.search-img-box img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

/* Badge SET màu đỏ nhỏ xíu đè lên góc ảnh */
.badge-mini-set {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    background-color: rgba(125, 22, 26, 0.9);
    /* Màu đỏ chủ đạo */
    color: white;
    font-size: 9px;
    font-weight: bold;
    text-align: center;
    padding: 2px 0;
    line-height: 1;
}

/* Chỉnh lại tên món để không bị dính */
.search-item-name {
    font-size: 13px;
    margin-bottom: 0;
    color: #333;
    font-weight: 700;
    line-height: 1.2;
}

.search-dropdown {
    position: absolute;
    top: 100%;
    /* Nằm ngay dưới ô input */
    left: 0;
    width: 100%;
    background: white;
    border-radius: 12px;
    margin-top: 8px;
    z-index: 1100;
    border: 1px solid #eee;
    overflow: hidden;
    /* Bo góc */
}

.search-results-list {
    max-height: 350px;
    /* Chiều cao tối đa */
    overflow-y: auto;
    /* Cho phép cuộn dọc */
}

/* Từng dòng kết quả */
.search-item {
    display: flex;
    padding: 10px;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    transition: background 0.2s;
}

.search-item:hover {
    background-color: #f9f9f9;
}

.search-item:last-child {
    border-bottom: none;
}

.search-item-info {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    overflow: hidden;
    /* Tránh vỡ layout nếu text dài */
}

/* Ảnh thumbnail */
.search-img-box {
    position: relative;
    width: 50px;
    height: 50px;
    margin-right: 12px;
    border-radius: 8px;
    overflow: hidden;
    flex-shrink: 0;
}

.search-img-box img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

/* Badge SET */
.badge-mini-set {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    background-color: rgba(125, 22, 26, 0.9);
    color: white;
    font-size: 9px;
    font-weight: bold;
    text-align: center;
    padding: 1px 0;
}

/* Text styles */
.search-item-name {
    font-size: 13px;
    margin-bottom: 2px;
    color: #333;
    font-weight: 700;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.search-item-desc {
    font-size: 11px;
    color: #888;
    margin-bottom: 2px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.search-item-price {
    font-size: 12px;
    color: #7d161a;
    font-weight: 800;
}

/* Nút cộng nhỏ */
.btn-search-add {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    border: 1px solid #7d161a;
    background: white;
    color: #7d161a;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    transition: all 0.2s;
}

.search-item:hover .btn-search-add {
    background: #7d161a;
    color: white;
}

/* Thanh cuộn đẹp */
.custom-scrollbar::-webkit-scrollbar {
    width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: #f1f1f1;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #ccc;
    border-radius: 4px;
}
</style>