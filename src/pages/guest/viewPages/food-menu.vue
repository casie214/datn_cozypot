<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import axiosClient from '@/services/axiosClient';

// --- 1. STATE QUẢN LÝ DỮ LIỆU ---
const categories = ref([]);
const menuData = ref([]);
const isLoading = ref(false);
const searchQuery = ref('');
const activeCategory = ref('');

// --- 2. STATE GIỎ HÀNG ---
const cart = ref([]);
const isCartOpen = ref(false);   // Trạng thái Modal giỏ hàng
const isCartHover = ref(false);  // Trạng thái Hover xem trước

// --- 3. STATE MODAL CHI TIẾT MÓN (BIẾN THỂ) ---
const variantsMap = ref({});      // Map lưu: { foodId: [detail1, detail2...] }
const isDetailModalOpen = ref(false);
const selectedProduct = ref(null);    // Món đang click chọn
const currentVariants = ref([]);      // Danh sách biến thể của món đang chọn
const selectedVariantId = ref(null);  // ID biến thể đang chọn
const modalQuantity = ref(1);         // Số lượng trong modal

// ==========================================
// A. PHẦN XỬ LÝ DỮ LIỆU (FETCH & MAP)
// ==========================================
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

        // 1. Map Danh mục con -> Danh mục cha
        const subToRootMap = {};
        const subNameMap = {};
        const rootToSubsList = {};

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

        // 2. Gom nhóm Food Detail (Biến thể) theo Food ID
        const vMap = {};
        listFoodDetail.forEach(detail => {
            const foodId = detail.idMonAnDiKem;
            if (!vMap[foodId]) vMap[foodId] = [];

            vMap[foodId].push({
                id: detail.id,
                name: detail.tenChiTietMonAn || 'Tiêu chuẩn',
                price: detail.giaBan,
                unit: detail.donVi,
                image: detail.hinhAnh
            });
        });
        // Sắp xếp biến thể theo giá tăng dần
        for (const key in vMap) {
            vMap[key].sort((a, b) => a.price - b.price);
        }
        variantsMap.value = vMap;

        // 3. Xử lý Menu hiển thị
        const processedMenu = [];

        // --- Nhóm Set Lẩu ---
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
                    image: item.hinhAnh || 'https://placehold.co/400x300?text=Hotpot',
                    desc: item.moTa || 'Set lẩu đầy đặn.',
                    type: 'SET',
                    groupId: item.idLoaiSet,
                    isRange: false,
                    hasVariants: false
                }))
            });
        }

        // --- Nhóm Món Ăn ---
        listDanhMuc.forEach(dm => {
            const monsInCat = listMonAn.filter(m => {
                const rootId = subToRootMap[m.idDanhMucChiTiet];
                return String(rootId) === String(dm.id);
            });

            if (monsInCat.length > 0) {
                const subFilters = rootToSubsList[dm.id]
                    ? [{ id: 'all', name: 'Tất cả' }, ...rootToSubsList[dm.id]]
                    : [];

                processedMenu.push({
                    categoryId: `cat-${dm.id}`,
                    categoryName: dm.tenDanhMuc.toUpperCase(),
                    activeFilter: 'all',
                    filters: subFilters,
                    items: monsInCat.map(item => {
                        const variants = vMap[item.id] || [];
                        let displayPrice = item.giaBan;
                        let isRange = false;

                        // Tính khoảng giá nếu có biến thể
                        if (variants.length > 0) {
                            const minP = variants[0].price;
                            const maxP = variants[variants.length - 1].price;
                            if (minP !== maxP) {
                                isRange = true;
                                displayPrice = { min: minP, max: maxP };
                            } else {
                                displayPrice = minP;
                            }
                        }

                        return {
                            id: item.id,
                            name: item.tenMonAn,
                            price: displayPrice,
                            image: item.hinhAnh || 'https://placehold.co/300x200?text=Food',
                            desc: item.moTa,
                            type: 'MON',
                            isRange: isRange,
                            subCategoryName: subNameMap[item.idDanhMucChiTiet],
                            groupId: item.idDanhMucChiTiet
                        };
                    })
                });
            }
        });

        menuData.value = processedMenu;

        // Tạo Nav Categories
        categories.value = processedMenu.map(s => ({ id: s.categoryId, name: s.categoryName }));
        if (categories.value.length) activeCategory.value = categories.value[0].id;

    } catch (error) {
        console.error("Lỗi tải dữ liệu:", error);
    } finally {
        isLoading.value = false;
    }
};

// ==========================================
// B. LOGIC CHỌN MÓN & MODAL SẢN PHẨM
// ==========================================
const handleItemClick = (item) => {
    // 1. Set Lẩu -> Thêm ngay
    if (item.type === 'SET') {
        pushToCart({
            id: item.id,
            detailId: null,
            name: item.name,
            price: item.price,
            image: item.image,
            type: 'SET',
            quantity: 1
        });
        return;
    }

    // 2. Món ăn -> Kiểm tra biến thể
    const variants = variantsMap.value[item.id] || [];

    if (variants.length <= 1) {
        // Nếu chỉ có 1 biến thể (hoặc ko có - fallback) -> Thêm ngay
        const variant = variants.length === 1 ? variants[0] : { id: null, price: item.price };
        pushToCart({
            id: item.id,
            detailId: variant.id,
            name: item.name + (variants.length === 1 && variant.name !== 'Tiêu chuẩn' ? ` (${variant.name})` : ''),
            price: variant.price,
            image: item.image,
            type: 'MON',
            quantity: 1
        });
    } else {
        // Có nhiều biến thể -> Mở Modal
        openProductModal(item, variants);
    }
};

const openProductModal = (item, variants) => {
    selectedProduct.value = item;
    currentVariants.value = variants;
    selectedVariantId.value = variants[0].id; // Chọn cái đầu tiên (rẻ nhất)
    modalQuantity.value = 1;
    isDetailModalOpen.value = true;
};

const closeDetailModal = () => {
    isDetailModalOpen.value = false;
    selectedProduct.value = null;
    currentVariants.value = [];
};

const confirmAddToCart = () => {
    if (!selectedProduct.value || !selectedVariantId.value) return;
    const variant = currentVariants.value.find(v => v.id === selectedVariantId.value);

    pushToCart({
        id: selectedProduct.value.id,
        detailId: variant.id,
        name: selectedProduct.value.name + ` (${variant.name})`,
        price: variant.price,
        image: selectedProduct.value.image,
        type: selectedProduct.value.type,
        quantity: modalQuantity.value
    });
    closeDetailModal();
};

// ==========================================
// C. LOGIC GIỎ HÀNG
// ==========================================
const pushToCart = (newItem) => {
    // Tìm món trùng (so sánh ID và DetailID)
    const existingIndex = cart.value.findIndex(i => {
        if (i.type === 'SET') return i.id === newItem.id; // Set lẩu so theo ID chính
        return i.detailId === newItem.detailId;           // Món ăn so theo ID biến thể
    });

    if (existingIndex !== -1) {
        cart.value[existingIndex].quantity += newItem.quantity;
    } else {
        cart.value.push(newItem);
    }
};

const openCartModal = () => { if (cart.value.length > 0) isCartOpen.value = true; };
const closeCartModal = () => isCartOpen.value = false;

const increaseQty = (index) => cart.value[index].quantity++;
const decreaseQty = (index) => {
    if (cart.value[index].quantity > 1) cart.value[index].quantity--;
    else removeItem(index);
};
const removeItem = (index) => {
    cart.value.splice(index, 1);
    if (cart.value.length === 0) closeCartModal();
};

// Computed Cart
const totalCount = computed(() => cart.value.reduce((sum, item) => sum + item.quantity, 0));
const totalPrice = computed(() => cart.value.reduce((sum, item) => sum + (item.price * item.quantity), 0));

// ==========================================
// D. UTILS & SCROLL SPY
// ==========================================
const formatPrice = (value) => {
    if (typeof value === 'object' && value !== null && value.min !== undefined) {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value.min);
    }
    if (typeof value !== 'number' || isNaN(value)) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

const getFilteredItems = (section) => {
    if (section.activeFilter === 'all') return section.items;
    return section.items.filter(item => item.groupId === section.activeFilter);
};
const selectFilter = (section, filterId) => { section.activeFilter = filterId; };

// Search
const allSearchItem = computed(() => menuData.value ? menuData.value.flatMap(s => s.items) : []);
const searchResult = computed(() => {
    if (!searchQuery.value.trim()) return [];
    const q = searchQuery.value.trim().toLowerCase();
    return allSearchItem.value.filter(i => i.name.toLowerCase().includes(q));
});
const clearSearch = () => searchQuery.value = '';

// Scroll Spy
const pageContainer = ref(null);
const scrollToCategory = (id) => {
    activeCategory.value = id;
    const el = document.getElementById(id);
    if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
};
const onScroll = () => {
    const offset = 150;
    for (const cat of categories.value) {
        const el = document.getElementById(cat.id);
        if (el) {
            const rect = el.getBoundingClientRect();
            if (rect.top <= offset && rect.bottom > offset) {
                if (activeCategory.value !== cat.id) {
                    activeCategory.value = cat.id;
                    const nav = document.getElementById(`nav-item-${cat.id}`);
                    if (nav) nav.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'center' });
                }
                break;
            }
        }
    }
};

onMounted(() => {
    fetchData();
    window.addEventListener('scroll', onScroll);
});
onUnmounted(() => {
    window.removeEventListener('scroll', onScroll);
});
</script>

<template>
    <div class="customer-menu-page" ref="pageContainer">
        <div v-if="isLoading" class="text-center py-5 mt-5">
            <div class="spinner-border text-primary-red" role="status"></div>
            <p class="mt-2 text-muted">Đang tải thực đơn...</p>
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

                    <div class="search-wrapper ms-3">
                        <div class="search-input-group d-flex align-items-center bg-white border rounded-pill px-3 py-1"
                            style="width: 250px;">
                            <input type="text" class="form-control border-0 shadow-none p-0" placeholder="Tìm món..."
                                v-model="searchQuery">
                            <i v-if="searchQuery" class="fas fa-times text-muted cursor-pointer ms-2"
                                @click="clearSearch"></i>
                            <i v-else class="fas fa-search text-muted ms-2"></i>
                        </div>
                        <div v-if="searchQuery" class="search-dropdown shadow-lg">
                            <div v-if="searchResult.length > 0" class="search-results-list custom-scrollbar">
                                <div v-for="item in searchResult" :key="item.id + item.type" class="search-item"
                                    @click="handleItemClick(item)">
                                    <div class="search-img-box">
                                        <img :src="item.image" :alt="item.name">
                                        <span v-if="item.type === 'SET'" class="badge-mini-set">SET</span>
                                    </div>
                                    <div class="search-item-info">
                                        <h6 class="search-item-name">{{ item.name }}</h6>
                                        <p class="search-item-desc">{{ item.desc }}</p>
                                        <div class="d-flex justify-content-between align-items-center mt-1">
                                            <span class="search-item-price">
                                                {{ item.isRange ? `${formatPrice(item.price.min)} -
                                                ${formatPrice(item.price.max)}` : formatPrice(item.price) }}
                                            </span>
                                            <button class="btn-search-add"><i class="fas fa-plus"></i></button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div v-else class="text-center py-4 text-muted">
                                <p class="mb-0 small">Không tìm thấy món nào.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </nav>

            <div class="main-content container py-4">
                <div v-for="section in menuData" :key="section.categoryId" :id="section.categoryId"
                    class="menu-section mb-5">
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
                        <div v-for="item in getFilteredItems(section)" :key="item.id"
                            :class="section.categoryId === 'combo-set' ? 'col-md-4 col-lg-3 col-6' : 'col-md-3 col-lg-2 col-6'">
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
                                        <span class="food-price"
                                            :class="section.categoryId === 'combo-set' ? 'food-price-lg' : ''">
                                            {{ item.isRange ? `${formatPrice(item.price.min)} +` :
                                            formatPrice(item.price) }}
                                        </span>
                                        <button class="btn-add" @click.stop="handleItemClick(item)">
                                            <i class="fas fa-plus"></i>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </TransitionGroup>
                </div>
            </div>
        </div>

        <div class="cart-wrapper-fixed" v-if="cart.length > 0" @mouseenter="isCartHover = true"
            @mouseleave="isCartHover = false">
            <Transition name="fade-up">
                <div v-if="isCartHover" class="cart-preview-popup">
                    <div class="preview-header">Đang chọn {{ totalCount }}món</div>
                    <div class="preview-list">
                        <div v-for="(item, index) in cart.slice().reverse().slice(0, 3)" :key="index"
                            class="preview-item">
                            <span class="p-name">{{ item.quantity }}x {{ item.name }}</span>
                            <span class="p-price">{{ formatPrice(item.price * item.quantity) }}</span>
                        </div>
                        <div v-if="cart.length > 3" class="preview-more text-center text-muted small">...và {{
                            cart.length - 3 }} món khác</div>
                    </div>
                    <div class="preview-arrow"></div>
                </div>
            </Transition>

            <div class="floating-cart" @click="openCartModal">
                <div class="cart-info" style="display: flex; align-items: center; gap: 0.5em;">
                    <span class="cart-count"><i class="fas fa-shopping-cart me-1"></i> {{ totalCount }} món</span>
                    <span class="cart-total">{{ formatPrice(totalPrice) }}</span>
                </div>
                <button class="btn-view-cart">Chi tiết <i class="fas fa-chevron-up ms-1"></i></button>
            </div>
        </div>

        <Transition name="modal-fade">
            <div v-if="isCartOpen" class="cart-modal-overlay" @click.self="closeCartModal">
                <div class="cart-modal-content">
                    <div class="modal-header">
                        <h5>Giỏ hàng <span class="badge bg-danger rounded-pill">{{ totalCount }}</span></h5>
                        <button class="btn-close-modal" @click="closeCartModal"><i class="fas fa-times"></i></button>
                    </div>
                    <div class="modal-body custom-scrollbar">
                        <div v-for="(item, index) in cart" :key="index" class="cart-item-row">
                            <div class="item-img"><img :src="item.image" :alt="item.name"></div>
                            <div class="item-details">
                                <h6 class="item-name">{{ item.name }}</h6>
                                <div class="item-price-unit">{{ formatPrice(item.price) }}</div>
                            </div>
                            <div class="item-actions">
                                <div class="qty-control">
                                    <button @click="decreaseQty(index)">-</button>
                                    <span>{{ item.quantity }}</span>
                                    <button @click="increaseQty(index)">+</button>
                                </div>
                                <button class="btn-remove" @click="removeItem(index)"><i
                                        class="fas fa-trash-alt"></i></button>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <div class="d-flex justify-content-between align-items-center w-100">
                            <span class="fw-bold text-dark fs-5">Tổng: <span class="text-danger">{{
                                    formatPrice(totalPrice) }}</span></span>
                            <button class="btn-checkout">XÁC NHẬN</button>
                        </div>
                    </div>
                </div>
            </div>
        </Transition>

        <Transition name="modal-fade">
            <div v-if="isDetailModalOpen" class="product-modal-overlay" @click.self="closeDetailModal">
                <div class="product-modal-content">
                    <button class="btn-close-product" @click="closeDetailModal"><i class="fas fa-times"></i></button>
                    <div class="product-header">
                        <div class="product-img"><img :src="selectedProduct?.image" :alt="selectedProduct?.name"></div>
                        <div class="product-info-basic">
                            <h4>{{ selectedProduct?.name }}</h4>
                            <p class="text-muted small mb-0">{{ selectedProduct?.desc || 'Món ngon mỗi ngày' }}</p>
                        </div>
                    </div>
                    <div class="product-body custom-scrollbar">
                        <div class="option-group">
                            <label class="option-title">Chọn phân loại:</label>
                            <div class="option-list">
                                <label v-for="variant in currentVariants" :key="variant.id" class="option-item"
                                    :class="{ active: selectedVariantId === variant.id }">
                                    <input type="radio" :value="variant.id" v-model="selectedVariantId" hidden>
                                    <span class="opt-name">{{ variant.name }}</span>
                                    <span class="opt-price">{{ formatPrice(variant.price) }}</span>
                                </label>
                            </div>
                        </div>
                    </div>
                    <div class="product-footer">
                        <div class="qty-selector">
                            <button @click="modalQuantity > 1 ? modalQuantity-- : null"
                                :disabled="modalQuantity <= 1">-</button>
                            <span>{{ modalQuantity }}</span>
                            <button @click="modalQuantity++">+</button>
                        </div>
                        <button class="btn-confirm-add" @click="confirmAddToCart">
                            Thêm - {{formatPrice(currentVariants.find(v => v.id === selectedVariantId)?.price *
                            modalQuantity) }}
                        </button>
                    </div>
                </div>
            </div>
        </Transition>
    </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
    --primary-red: #7d161a;
    --text-dark: #333;
}

.text-primary-red {
    color: #7d161a !important;
}

.customer-menu-page {
    background-color: #f9f9f9;
    min-height: 100vh;
    padding-bottom: 90px;
}

.menu-section {
    scroll-margin-top: 140px;
}

/* NAV & SEARCH */
.category-nav {
    background: #fff;
    position: sticky;
    top: 0;
    z-index: 1000;
    box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
    border-bottom: 1px solid #eee;
    height: 60px;
}

.nav-scroll-container {
    flex: 1;
    overflow-x: auto;
    white-space: nowrap;
    height: 100%;
    display: flex;
    align-items: center;
    scrollbar-width: none;
}
.nav-list {
    display: flex !important;       
    flex-direction: row !important;  
    flex-wrap: nowrap !important;    
    list-style: none;
    padding: 0;
    margin: 0;
    height: 100%;
    align-items: center;
}

.nav-scroll-container::-webkit-scrollbar {
    display: none;
}

.nav-item {
    padding: 15px 20px;
    font-weight: 600;
    color: #666;
    cursor: pointer;
    text-transform: uppercase;
    border-bottom: 3px solid transparent;
    transition: 0.3s;
}

.nav-item:hover {
    color: #7d161a;
    background: #fdf2f2;
}

.nav-item.active {
    color: #7d161a;
    border-bottom: 3px solid #7d161a;
    font-weight: 800;
    background: #fff5f5;
}

.search-wrapper {
    position: relative;
    z-index: 2000;
}

.search-dropdown {
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%;
    background: white;
    border-radius: 12px;
    margin-top: 8px;
    z-index: 1100;
    border: 1px solid #eee;
    overflow: hidden;
}

.search-results-list {
    max-height: 350px;
    overflow-y: auto;
}

.search-item {
    display: flex;
    padding: 10px;
    border-bottom: 1px solid #f5f5f5;
    cursor: pointer;
    transition: background 0.2s;
}

.search-item:hover {
    background: #f9f9f9;
}

.search-img-box {
    width: 50px;
    height: 50px;
    border-radius: 8px;
    overflow: hidden;
    margin-right: 12px;
    position: relative;
    flex-shrink: 0;
}

.search-img-box img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.badge-mini-set {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 100%;
    background: rgba(125, 22, 26, 0.9);
    color: white;
    font-size: 9px;
    font-weight: bold;
    text-align: center;
}

.search-item-info {
    flex: 1;
    overflow: hidden;
}

.search-item-name {
    font-size: 13px;
    font-weight: 700;
    color: #333;
    margin: 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.search-item-desc {
    font-size: 11px;
    color: #888;
    margin: 2px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.search-item-price {
    font-size: 12px;
    color: #7d161a;
    font-weight: 800;
}

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
}

.search-item:hover .btn-search-add {
    background: #7d161a;
    color: white;
}

/* FOOD CARDS */
.section-title {
    color: #7d161a;
    font-weight: 800;
    font-size: 1.4rem;
    margin-bottom: 20px;
    border-left: 5px solid #7d161a;
    padding-left: 10px;
    text-transform: uppercase;
}

.filter-scroll-wrapper {
    display: flex;
    overflow-x: auto;
    gap: 8px;
    padding: 5px 0;
    scrollbar-width: none;
}

.filter-scroll-wrapper::-webkit-scrollbar {
    display: none;
}

.btn-filter {
    background: #fff;
    border: 1px solid #e0e0e0;
    color: #666;
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
    white-space: nowrap;
    transition: 0.2s;
}

.btn-filter:hover {
    background: #f5f5f5;
}

.btn-filter.active {
    background: #7d161a;
    color: white;
    border-color: #7d161a;
    box-shadow: 0 2px 6px rgba(125, 22, 26, 0.3);
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
    transition: 0.2s;
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

.card-img-wrapper.square-ratio {
    padding-top: 100%;
}

.card-img-wrapper img {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: 0.5s;
}

.food-card:hover img {
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
}

.card-body {
    padding: 12px;
    display: flex;
    flex-direction: column;
    flex-grow: 1;
}

.sub-cat-name {
    font-size: 11px;
    color: #999;
    text-transform: uppercase;
    font-weight: 600;
    margin-bottom: 2px;
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

.food-name.text-small {
    font-size: 14px;
    min-height: 36px;
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
    transition: 0.2s;
}

.btn-add:hover {
    background: #7d161a;
    color: white;
    transform: rotate(90deg);
}

/* FLOATING CART & PREVIEW */
.cart-wrapper-fixed {
    position: fixed;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    width: 90%;
    max-width: 500px;
    z-index: 1100;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.floating-cart {
    position: relative;
    width: 100%;
    background: #7d161a;
    color: white;
    border-radius: 50px;
    padding: 10px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    box-shadow: 0 8px 25px rgba(125, 22, 26, 0.4);
    cursor: pointer;
    border: 2px solid rgba(255, 255, 255, 0.2);
    transition: 0.2s;
}

.floating-cart:active {
    transform: scale(0.98);
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

.cart-preview-popup {
    position: absolute;
    bottom: 100%;
    margin-bottom: 15px;
    width: 100%;
    background: white;
    border-radius: 12px;
    box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
    padding: 12px;
    border: 1px solid #eee;
    z-index: 1090;
}

.preview-header {
    font-size: 12px;
    font-weight: 700;
    color: #999;
    text-transform: uppercase;
    margin-bottom: 8px;
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 4px;
}

.preview-item {
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    margin-bottom: 6px;
    color: #333;
}

.preview-item .p-name {
    font-weight: 500;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 70%;
}

.preview-item .p-price {
    font-weight: 700;
    color: #7d161a;
}

.preview-arrow {
    position: absolute;
    bottom: -6px;
    left: 50%;
    transform: translateX(-50%);
    width: 12px;
    height: 12px;
    background: white;
    border-bottom: 1px solid #eee;
    border-right: 1px solid #eee;
    transform: translateX(-50%) rotate(45deg);
}

/* CART MODAL (LARGE) */
.cart-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.7);
    z-index: 2000;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(5px);
}

.cart-modal-content {
    background: white;
    width: 95%;
    max-width: 900px;
    height: 90vh;
    border-radius: 16px;
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.3);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    animation: zoomIn 0.3s;
}

@keyframes zoomIn {
    from {
        transform: scale(0.95);
        opacity: 0;
    }

    to {
        transform: scale(1);
        opacity: 1;
    }
}

.modal-header {
    padding: 20px 25px;
    background: #f8f9fa;
    border-bottom: 1px solid #e0e0e0;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.modal-header h5 {
    margin: 0;
    font-weight: 700;
    color: #333;
    font-size: 1.25rem;
}

.btn-close-modal {
    background: #f1f1f1;
    border: none;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: 0.2s;
}

.btn-close-modal:hover {
    background: #e0e0e0;
}

.modal-body {
    flex: 1;
    overflow-y: auto;
    padding: 25px;
    background: #fff;
}

.cart-item-row {
    display: flex;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px;
    border: 1px solid #eee;
    border-radius: 12px;
    transition: background 0.2s;
}

.cart-item-row:hover {
    background: #fcfcfc;
    border-color: #ddd;
}

.item-img {
    width: 80px;
    height: 80px;
    border-radius: 8px;
    overflow: hidden;
    margin-right: 20px;
    flex-shrink: 0;
}

.item-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.item-details {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    margin-right: 10px;
}

.item-name {
    font-size: 16px;
    font-weight: 600;
    margin-bottom: 8px;
    color: #333;
    line-height: 1.3;
}

.item-price-unit {
    font-size: 15px;
    color: #7d161a;
    font-weight: 700;
}

.item-actions {
    display: flex;
    align-items: center;
    gap: 20px;
}

@media (min-width: 768px) {
    .cart-item-row {
        justify-content: space-between;
    }

    .item-details {
        margin-right: 40px;
    }

    .item-actions {
        flex-direction: row;
    }
}

.qty-control {
    display: flex;
    align-items: center;
    background: #f5f5f5;
    border-radius: 6px;
    padding: 4px;
    border: 1px solid #ddd;
}

.qty-control button {
    width: 32px;
    height: 32px;
    border: none;
    background: white;
    border-radius: 4px;
    font-weight: bold;
    color: #333;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
    cursor: pointer;
    font-size: 16px;
}

.qty-control span {
    width: 40px;
    text-align: center;
    font-size: 15px;
    font-weight: 600;
}

.btn-remove {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #fff0f0;
    color: #dc3545;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    cursor: pointer;
    transition: 0.2s;
}

.btn-remove:hover {
    background: #dc3545;
    color: white;
}

.modal-footer {
    padding: 25px;
    background: #f8f9fa;
    border-top: 1px solid #e0e0e0;
    display: flex;
    flex-direction: column;
    gap: 15px;
}

@media (min-width: 768px) {
    .modal-footer {
        flex-direction: row;
        align-items: center;
        justify-content: space-between;
    }

    .btn-checkout {
        width: 300px;
    }
}

.btn-checkout {
    background: #7d161a;
    color: white;
    border: none;
    padding: 16px;
    border-radius: 12px;
    font-weight: 800;
    font-size: 18px;
    letter-spacing: 1px;
    cursor: pointer;
    transition: 0.3s;
    box-shadow: 0 4px 15px rgba(125, 22, 26, 0.3);
    text-transform: uppercase;
}

.btn-checkout:hover {
    background: #5e1013;
    transform: translateY(-2px);
}

/* PRODUCT DETAIL MODAL */
.product-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    background: rgba(0, 0, 0, 0.6);
    z-index: 2100;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(3px);
}

.product-modal-content {
    background: white;
    width: 90%;
    max-width: 500px;
    border-radius: 20px;
    display: flex;
    flex-direction: column;
    max-height: 85vh;
    position: relative;
    overflow: hidden;
    animation: slideUp 0.3s ease-out;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

@keyframes slideUp {
    from {
        transform: translateY(50px);
        opacity: 0;
    }

    to {
        transform: translateY(0);
        opacity: 1;
    }
}

.btn-close-product {
    position: absolute;
    top: 15px;
    right: 15px;
    width: 32px;
    height: 32px;
    background: rgba(0, 0, 0, 0.5);
    color: white;
    border: none;
    border-radius: 50%;
    z-index: 10;
    cursor: pointer;
    backdrop-filter: blur(4px);
}

.product-img {
    width: 100%;
    height: 200px;
    overflow: hidden;
}

.product-img img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.product-info-basic {
    padding: 15px 20px;
    border-bottom: 1px solid #f0f0f0;
}

.product-info-basic h4 {
    margin: 0;
    font-weight: 800;
    color: #333;
    font-size: 1.2rem;
}

.product-body {
    padding: 20px;
    overflow-y: auto;
    flex: 1;
}

.option-title {
    font-size: 14px;
    font-weight: 700;
    color: #555;
    margin-bottom: 10px;
    display: block;
}

.option-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.option-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 15px;
    border: 1px solid #e0e0e0;
    border-radius: 8px;
    cursor: pointer;
    transition: 0.2s;
    user-select: none;
}

.option-item:hover {
    background: #fafafa;
}

.option-item.active {
    border-color: #7d161a;
    background: #fdf2f2;
    color: #7d161a;
}

.opt-name {
    font-weight: 600;
    font-size: 14px;
}

.opt-price {
    font-weight: 700;
    font-size: 14px;
}

.product-footer {
    padding: 15px 20px;
    border-top: 1px solid #eee;
    background: white;
    display: flex;
    gap: 15px;
    align-items: center;
}

.qty-selector {
    display: flex;
    align-items: center;
    border: 1px solid #ddd;
    border-radius: 8px;
    height: 44px;
}

.qty-selector button {
    width: 36px;
    height: 100%;
    background: none;
    border: none;
    font-weight: bold;
    font-size: 18px;
    color: #333;
    cursor: pointer;
}

.qty-selector button:disabled {
    color: #ccc;
    cursor: not-allowed;
}

.qty-selector span {
    width: 30px;
    text-align: center;
    font-weight: 600;
}

.btn-confirm-add {
    flex: 1;
    background: #7d161a;
    color: white;
    border: none;
    height: 44px;
    border-radius: 8px;
    font-weight: 700;
    font-size: 15px;
    cursor: pointer;
    transition: 0.2s;
    box-shadow: 0 4px 10px rgba(125, 22, 26, 0.2);
}

.btn-confirm-add:hover {
    background: #5e1013;
    transform: translateY(-2px);
}

/* TRANSITIONS */
.fade-up-enter-active,
.fade-up-leave-active {
    transition: all 0.3s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
    opacity: 0;
    transform: translateY(10px);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
    transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
    opacity: 0;
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

.filter-list-leave-active {
    position: absolute;
    z-index: -1;
}

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