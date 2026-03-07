<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import Swal from 'sweetalert2';
import axiosClient from '@/services/axiosClient';
// IMPORT THƯ VIỆN MULTISELECT VÀ CSS CỦA NÓ
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.css';

import { 
  getAllFoodGeneralActive, 
  getAllHotpotGeneralActive, 
  getAllCategoryGeneralActive,
  getAllLoaiSetLauActive, 
  createOrder 
} from '@/services/tableManageService'; 

const route = useRoute();

const idBanAn = ref(route.query.ban);
const idHoaDon = ref(route.query.hoadon);

// State quản lý UI
const isLoading = ref(true);
const isCartOpen = ref(false);

// STATE CHO BỘ LỌC MULTISELECT
const searchQuery = ref(''); 

// Dữ liệu mảng cho Multiselect
const typeOptions = ref([
  { id: 'ALL', name: 'Tất cả loại' },
  { id: 'SET', name: 'Set lẩu' },
  { id: 'FOOD', name: 'Món lẻ' }
]);

// Model v-model cho Multiselect (Phải là Object)
const filterType = ref(typeOptions.value[0]); 
const filterFoodCategory = ref({ id: 'ALL', tenDanhMuc: 'Danh mục món lẻ' });
const filterSetCategory = ref({ id: 'ALL', tenLoaiSet: 'Loại Set lẩu' });

// Dữ liệu
const menuItems = ref([]);
const foodCategories = ref([]); 
const setCategories = ref([]);  
const cart = ref([]);

// 🚨 1. GỌI API VÀ BẢO VỆ DỮ LIỆU CHỐNG CRASH
const fetchMenu = async () => {
  try {
    isLoading.value = true;
    
    const [foodRes, hotpotRes, catFoodRes, catSetRes] = await Promise.all([
      getAllFoodGeneralActive(),
      getAllHotpotGeneralActive(),
      getAllCategoryGeneralActive(),
      getAllLoaiSetLauActive()
    ]);

    // Ép kiểu mảng an toàn chống lỗi "not iterable"
    if (catFoodRes && Array.isArray(catFoodRes.data)) {
      foodCategories.value = [{ id: 'ALL', tenDanhMuc: 'Danh mục món lẻ' }, ...catFoodRes.data];
    } else {
      foodCategories.value = [{ id: 'ALL', tenDanhMuc: 'Danh mục món lẻ' }];
    }

    if (catSetRes && Array.isArray(catSetRes.data)) {
      setCategories.value = [{ id: 'ALL', tenLoaiSet: 'Loại Set lẩu' }, ...catSetRes.data.map(s => ({
        ...s,
        tenLoaiSet: s.tenLoaiSet || s.tenLoai || s.tenDanhMuc || 'Chưa rõ'
      }))];
    } else {
      setCategories.value = [{ id: 'ALL', tenLoaiSet: 'Loại Set lẩu' }];
    }

    let allItems = [];

    if (foodRes && Array.isArray(foodRes.data)) {
      const foodData = foodRes.data.map(mon => ({
        originalId: mon.id,
        type: 'FOOD',
        name: mon.tenMon || 'Món ăn',
        price: mon.donGia || mon.giaBan || 0, 
        image: mon.hinhAnh || 'https://via.placeholder.com/150', 
        categoryId: mon.idDanhMuc || 'ALL', 
        description: mon.moTa || ''
      }));
      allItems = [...allItems, ...foodData];
    }

    if (hotpotRes && Array.isArray(hotpotRes.data)) {
      const hotpotData = hotpotRes.data.map(set => ({
        originalId: set.id,
        type: 'SET',
        name: set.tenSetLau || set.tenSet || set.tenMon || set.ten || 'Set Lẩu', 
        price: set.donGia || set.giaBan || 0,
        image: set.hinhAnh || 'https://via.placeholder.com/150',
        categoryId: set.idLoaiSetLau || set.idLoaiSet || set.idDanhMuc || 'ALL', 
        description: set.moTa || ''
      }));
      allItems = [...allItems, ...hotpotData];
    }

    menuItems.value = allItems;

  } catch (error) {
    console.error("Lỗi lấy thực đơn", error);
  } finally {
    isLoading.value = false;
  }
};

const fetchBillDetails = async () => {
  try {
    const res = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${idBanAn.value}`);
    if (res.data && Array.isArray(res.data.chiTiet)) {
      const listFromApi = res.data.chiTiet.filter(m => m.trangThaiMon !== 0);

      cart.value = listFromApi.map(mon => {
        const isSet = mon.type === 'SET' || mon.idSetLau != null;
        // Bảo vệ id không bao giờ bị null gây sập Vue v-for
        const originalId = isSet ? (mon.idSetLau || mon.id) : (mon.idChiTietMonAn || mon.idMonAn || mon.id);
        
        return {
          dbDetailId: mon.idChiTietHd || mon.id || null,       
          originalId: originalId,
          type: isSet ? 'SET' : 'FOOD',
          name: mon.tenMon || mon.tenSetLau || 'Món trong hóa đơn',
          price: mon.donGia || mon.giaBan || 0,
          quantity: mon.soLuong || 1,
          baseQuantity: mon.soLuong || 1,         
          note: mon.ghiChu || '',
          served: mon.trangThaiMon === 2     
        };
      });
    }
  } catch (e) {
    console.log("Bàn chưa có món hoặc lỗi tải hóa đơn cũ");
  }
};

// 🚨 HÀM LỌC MÓN THEO MULTI-SELECT 
const filteredMenu = computed(() => {
  let result = menuItems.value;

  if (searchQuery.value.trim() !== '') {
    const keyword = searchQuery.value.toLowerCase().trim();
    result = result.filter(item => (item.name || '').toLowerCase().includes(keyword));
  } 

  const typeId = filterType.value?.id || 'ALL';
  const foodCatId = filterFoodCategory.value?.id || 'ALL';
  const setCatId = filterSetCategory.value?.id || 'ALL';

  if (typeId !== 'ALL') {
    result = result.filter(item => item.type === typeId);
  }

  if ((typeId === 'ALL' || typeId === 'FOOD') && foodCatId !== 'ALL') {
    result = result.filter(item => item.type !== 'FOOD' || item.categoryId === foodCatId);
  }

  if ((typeId === 'ALL' || typeId === 'SET') && setCatId !== 'ALL') {
    result = result.filter(item => item.type !== 'SET' || item.categoryId === setCatId);
  }

  return result.filter(item => {
      if (typeId === 'FOOD' && foodCatId !== 'ALL' && item.categoryId !== foodCatId) return false;
      if (typeId === 'SET' && setCatId !== 'ALL' && item.categoryId !== setCatId) return false;
      return true;
  });
});

const onTypeChange = () => {
  if (filterType.value?.id === 'FOOD') filterSetCategory.value = setCategories.value[0] || { id: 'ALL', tenLoaiSet: 'Loại Set lẩu' };
  if (filterType.value?.id === 'SET') filterFoodCategory.value = foodCategories.value[0] || { id: 'ALL', tenDanhMuc: 'Danh mục món lẻ' };
};

const getCartQuantity = (item) => {
  const found = cart.value.find(c => c.originalId === item.originalId && c.type === item.type);
  return found ? found.quantity : 0;
};

const addToCart = (item) => {
  const existing = cart.value.find(c => c.originalId === item.originalId && c.type === item.type);
  if (existing) {
    existing.quantity++;
  } else {
    cart.value.push({ 
      ...item, quantity: 1, baseQuantity: 0, note: '', dbDetailId: null, served: false 
    });
  }
};

const removeFromCart = (item) => {
  const index = cart.value.findIndex(c => c.originalId === item.originalId && c.type === item.type);
  if (index !== -1) {
    const existing = cart.value[index];

    if (existing.served && existing.quantity <= existing.baseQuantity) {
      Swal.fire({
        icon: 'warning',
        iconColor: '#7D161A',
        title: 'Lưu ý',
        text: 'Món này bếp đã làm, không thể hủy. Vui lòng gọi nhân viên!',
        confirmButtonColor: '#7d161a'
      });
      return;
    }

    existing.quantity--;
    if (existing.quantity === 0) cart.value.splice(index, 1);
  }
  if (cartTotalCount.value === 0) isCartOpen.value = false;
};

const cartTotalCount = computed(() => cart.value.reduce((sum, item) => sum + (item.quantity || 0), 0));
const cartTotalPrice = computed(() => cart.value.reduce((sum, item) => sum + ((item.price || 0) * (item.quantity || 0)), 0));

const submitOrder = async () => {
  if (cart.value.length === 0) return;

  try {
    Swal.fire({ title: 'Đang gửi bếp...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    const payload = {
      idHoaDon: Number(idHoaDon.value),
      idBanAn: Number(idBanAn.value),
      idNhanVien: null, idKhachHang: null, 
      chiTietHoaDon: cart.value.map(item => ({
        id: item.dbDetailId || null, 
        idChiTietMonAn: item.type === 'FOOD' ? item.originalId : null,
        idSetLau: item.type === 'SET' ? item.originalId : null,
        soLuong: item.quantity, ghiChu: item.note || '' 
      }))
    };

    await createOrder(payload); 
    
    // Đóng ngăn kéo ngay lập tức cho mượt
    isCartOpen.value = false;

    Swal.fire({ 
      icon: 'success', title: 'Thành công!', iconColor: '#7D161A', 
      text: 'Đơn hàng đã gửi tới bếp.',
      confirmButtonColor: '#7d161a'
    });

    // Timeout giúp data kịp vào DB rồi mới fetch lại
    setTimeout(() => { fetchBillDetails(); }, 600);

  } catch (error) {
    console.error(error);
    Swal.fire('Lỗi', 'Không thể gửi đơn lúc này. Vui lòng gọi nhân viên!', 'error');
  }
};

onMounted(() => {
  if (!idBanAn.value || !idHoaDon.value) {
    Swal.fire('Lỗi liên kết', 'Mã QR không hợp lệ hoặc đã hết hạn!', 'error');
    return;
  }
  fetchMenu();
  fetchBillDetails(); 
});
</script>

<template>
  <div class="customer-menu-app">
    <header class="app-header shadow-sm">
      <div class="d-flex justify-content-between align-items-center px-3 py-2 bg-main-color">
        <div class="brand d-flex align-items-center">
          <div style="margin-left: 20px;">
            <h5 class="mb-0 fw-bold text-white">Gọi món</h5>
            <small class="text-white-50">Tự phục vụ tại bàn</small>
          </div>
        </div>
        <div class="table-badge bg-white text-main-color fw-bold px-3 py-1 rounded-pill shadow-sm">
          Bàn {{ idBanAn || '...' }}
        </div>
      </div>
    </header>

    <div class="app-container-2">
      
      <div class="filter-section bg-white border-bottom p-3">
        
        <div class="input-group mb-3 shadow-sm">
          <span class="input-group-text bg-white border-end-0 text-muted"><i class="fa-solid fa-magnifying-glass"></i></span>
          <input type="text" class="form-control border-start-0 ps-0" placeholder="Tìm tên món/set lẩu..." v-model="searchQuery">
          <button v-if="searchQuery" class="btn border border-start-0 bg-white" @click="searchQuery = ''">
            <i class="fa-solid fa-xmark text-muted"></i>
          </button>
        </div>

        <div class="row g-2">
          <div class="col-12 col-md-4">
            <Multiselect 
              v-model="filterType" 
              :options="typeOptions" 
              track-by="id" 
              label="name" 
              :searchable="false" 
              :allow-empty="false" 
              select-label="Nhấn để chọn"
              selected-label="Đã chọn"
              deselect-label="Bỏ chọn"
              placeholder="Chọn loại"
              @update:modelValue="onTypeChange"
            />
          </div>
          
          <div class="col-6 col-md-4">
            <Multiselect 
              v-model="filterFoodCategory" 
              :options="foodCategories" 
              track-by="id" 
              label="tenDanhMuc" 
              :searchable="true" 
              :allow-empty="false" 
              select-label="Nhấn để chọn"
              selected-label="Đã chọn"
              deselect-label="Bỏ chọn"
              placeholder="Danh mục món lẻ"
              :disabled="filterType?.id === 'SET'"
            />
          </div>

          <div class="col-6 col-md-4">
            <Multiselect 
              v-model="filterSetCategory" 
              :options="setCategories" 
              track-by="id" 
              label="tenLoaiSet" 
              :searchable="true" 
              :allow-empty="false" 
              select-label="Nhấn để chọn"
              selected-label="Đã chọn"
              deselect-label="Bỏ chọn"
              placeholder="Loại Set lẩu"
              :disabled="filterType?.id === 'FOOD'"
            />
          </div>
        </div>
      </div>

      <main class="app-body custom-scrollbar px-2 pt-3 pb-5 mb-5">
        <div v-if="isLoading" class="text-center py-5">
          <div class="spinner-border text-danger" role="status"></div>
          <p class="mt-2 text-muted">Đang tải thực đơn...</p>
        </div>

        <div v-else-if="filteredMenu.length === 0" class="text-center py-5">
          <i class="fa-solid fa-utensils fa-3x text-muted mb-3"></i>
          <p class="text-muted">Không tìm thấy món ăn phù hợp.</p>
        </div>

        <div v-else class="row g-2">
          <div v-for="(item, index) in filteredMenu" :key="'menu_' + (item.originalId || index) + '_' + item.type" class="col-6 col-sm-4 col-md-3 col-lg-2-4">
            
            <div class="food-card bg-white rounded-3 shadow-sm overflow-hidden h-100 d-flex flex-column"
                 :class="{ 'selected-card': getCartQuantity(item) > 0 }">
              
              <div class="food-img-wrapper position-relative">
                <img :src="item.image" :alt="item.name" class="food-img w-100 h-100 object-fit-cover">

                <span class="position-absolute top-0 start-0 m-2 badge"
                      :class="item.type === 'SET' ? 'bg-warning text-dark' : 'bg-success'">
                  {{ item.type === 'SET' ? 'Set' : 'Món' }}
                </span>

                <div v-if="getCartQuantity(item) > 0"
                     class="overlay-qty position-absolute top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center">
                  <div class="qty-circle shadow">
                    {{ getCartQuantity(item) }}
                  </div>
                </div>
              </div>

              <div class="p-2 d-flex flex-column flex-grow-1">
                <h6 class="food-name fw-bold mb-1 text-dark" style="font-size: 13px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; line-height: 1.4;">
                  {{ item.name }}
                </h6>

                <div class="d-flex justify-content-between align-items-center mb-2 mt-1">
                  <small class="text-muted" style="font-size: 11px;">{{ item.type === 'SET' ? 'Set' : 'Phần' }}</small>
                  <span class="text-danger fw-bold" style="font-size: 14px;">{{ (item.price || 0).toLocaleString() }}đ</span>
                </div>
                
                <div class="mt-auto">
                  <div v-if="getCartQuantity(item) > 0" class="d-flex gap-2">
                    <button class="btn btn-light border w-50 fw-bold text-dark d-flex justify-content-center align-items-center" 
                            style="height: 34px;" @click="removeFromCart(item)">
                      <i class="fa-solid fa-minus" style="font-size: 12px;"></i>
                    </button>
                    <button class="btn btn-light border w-50 fw-bold text-dark d-flex justify-content-center align-items-center" 
                            style="height: 34px;" @click="addToCart(item)">
                      <i class="fa-solid fa-plus" style="font-size: 12px;"></i>
                    </button>
                  </div>
                  <button v-else class="btn btn-outline-danger w-100 fw-bold" 
                          style="height: 34px; font-size: 13px;" @click="addToCart(item)">
                    + Chọn
                  </button>
                </div>

              </div>
            </div>
            
          </div>
        </div>
      </main>

      <div class="bottom-cart-bar shadow-lg transition-transform" 
           :class="{ 'show': cartTotalCount > 0 }" 
           @click="isCartOpen = true">
        <div class="px-3 h-100 d-flex justify-content-between align-items-center">
          <div class="d-flex align-items-center text-white">
            
            <div class="position-relative me-3 d-flex align-items-center justify-content-center" style="width: 45px; height: 45px; min-width: 45px; background: rgba(255,255,255,0.15); border-radius: 50%; flex-shrink: 0;">
              <i class="fa-solid fa-cart-shopping fs-5"></i>
              <span class="position-absolute bg-warning text-dark d-flex align-items-center justify-content-center" 
                    style="top: -2px; right: -5px; width: 24px; height: 24px; min-width: 24px; min-height: 24px; border-radius: 50%; font-size: 12px; font-weight: bold; padding: 0; border: 2px solid #7d161a; flex-shrink: 0; line-height: 1;">
                {{ cartTotalCount }}
              </span>
            </div>

            <div>
              <div class="fw-bold fs-5" style="line-height: 1.2;">{{ cartTotalPrice.toLocaleString() }} đ</div>
              <small class="text-white-50" style="font-size: 11px;">Chưa bao gồm VAT/Khuyến mãi</small>
            </div>
          </div>
          <div class="btn bg-white text-danger fw-bold rounded-pill px-4" @click.stop="isCartOpen = true">
            Xem đơn <i class="fa-solid fa-chevron-up ms-1"></i>
          </div>
        </div>
      </div>

      <div class="cart-drawer-overlay" :class="{'open': isCartOpen}" @click="isCartOpen = false"></div>
      <div class="cart-drawer bg-white rounded-top-4" :class="{'open': isCartOpen}">
        <div class="drawer-header p-3 border-bottom d-flex justify-content-between align-items-center bg-light rounded-top-4">
          <h5 class="fw-bold mb-0 text-danger"><i class="fa-solid fa-clipboard-list me-2"></i>Món đã chọn</h5>
          <button class="btn-close" @click="isCartOpen = false"></button>
        </div>
        
        <div class="drawer-body p-3 overflow-auto custom-scrollbar" style="max-height: 50vh;">
          <div v-for="(item, index) in cart" :key="'cart_' + (item.originalId || index) + '_' + item.type" class="cart-item d-flex align-items-center mb-3 pb-3 border-bottom">
            <div class="flex-grow-1 pe-2">
              <div class="fw-bold" style="font-size: 14px;">{{ item.name }}</div>
              <div class="text-danger fw-bold my-1" style="font-size: 13px;">{{ (item.price || 0).toLocaleString() }} đ</div>
            </div>
            <div class="d-flex flex-column align-items-end">
              <div class="d-flex align-items-center border rounded-pill p-1 shadow-sm">
                <button class="btn btn-sm text-muted px-2 py-0 border-0" @click="removeFromCart(item)"><i class="fa-solid fa-minus"></i></button>
                <span class="fw-bold mx-2" style="font-size: 14px; min-width: 15px; text-align: center;">{{ item.quantity }}</span>
                <button class="btn btn-sm text-danger px-2 py-0 border-0" @click="addToCart(item)"><i class="fa-solid fa-plus"></i></button>
              </div>
              <strong class="mt-2 text-dark" style="font-size: 14px;">{{ ((item.price || 0) * item.quantity).toLocaleString() }} đ</strong>
            </div>
          </div>
        </div>

        <div class="drawer-footer p-3 bg-white border-top shadow-lg">
          <div class="d-flex justify-content-between mb-3">
            <span class="text-muted fw-bold">Tổng tạm tính:</span>
            <span class="text-danger fw-bold fs-4">{{ cartTotalPrice.toLocaleString() }} đ</span>
          </div>
          <div class="btn w-100 fw-bold fs-5 text-white shadow-sm d-flex justify-content-center align-items-center" 
               style="background-color: #7d161a; border-radius: 12px; padding: 12px; cursor: pointer;" 
               @click="submitOrder">
            <i class="fa-solid fa-bell-concierge me-2"></i> XÁC NHẬN ĐẶT MÓN
          </div>
        </div>
      </div>
    </div> 
  </div>
</template>

<style scoped>
/* CUSTOM VUE-MULTISELECT CSS (CHUYỂN MÀU ĐỎ QUÁN) */
:deep(.multiselect__option--highlight) {
  background: #7d161a !important;
  color: white !important;
}
:deep(.multiselect__option--highlight::after) {
  background: #7d161a !important;
  color: white !important;
}
:deep(.multiselect__option--selected.multiselect__option--highlight) {
  background: #610e12 !important; 
}
:deep(.multiselect__tags) {
  border-radius: 6px;
  border: 1px solid #dee2e6;
  min-height: 38px;
  padding-top: 6px;
}
:deep(.multiselect--disabled .multiselect__tags) {
  background: #f8f9fa;
  opacity: 0.6;
}

/* Base UI */
.bg-main-color {
  background-color: #7d161a;
}
.text-main-color {
  color: #7d161a;
}
.customer-menu-app {
  background-color: #e9ecef; 
  min-height: calc(100vh - 60px); 
  width: 100%;
  font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  display: flex;
  flex-direction: column; 
  align-items: center; 
}
.app-header {
  position: sticky;
  top: 0;
  z-index: 1000;
  width: 100%; 
  flex-shrink: 0;
}
.app-container-2 {
  width: 100%;
  max-width: 75%; 
  background-color: #f4f6f8; 
  height: calc(100vh - 60px); 
  position: relative; 
  display: flex !important;
  flex-direction: column !important; 
  overflow: hidden; 
}

@media (max-width: 768px) {
  .app-container-2 {
    max-width: 100%;
  }
}

.input-group-text, .form-control {
  box-shadow: none !important;
}

.app-body {
  flex-grow: 1; 
  overflow-y: auto; 
}
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}

@media (min-width: 992px) {
  .col-lg-2-4 {
    flex: 0 0 auto;
    width: 20%; 
  }
}

/* Thẻ món ăn */
.food-img-wrapper {
  height: 150px; 
  overflow: hidden;
  background-color: #f8f9fa;
  border-bottom: 1px solid #f1f1f1;
}
.food-card {
  border: 2px solid transparent; 
  transition: all 0.2s ease-in-out;
}
.food-card:active {
  transform: scale(0.97); 
}
.food-card.selected-card {
  border-color: #7d161a !important;
  box-shadow: 0 4px 15px rgba(125, 22, 26, 0.15) !important;
}
.overlay-qty {
  background-color: rgba(125, 22, 26, 0.65); 
  backdrop-filter: blur(1px); 
  z-index: 5;
}
.qty-circle {
  width: 45px;
  height: 45px;
  border-radius: 50%;
  border: 2px solid white;
  color: white;
  font-size: 22px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: transparent;
}

/* Drawer / Footer */
.bottom-cart-bar {
  position: fixed; 
  bottom: 0;
  left: 50%; 
  transform: translateX(-50%) translateY(100%); 
  width: 100%;
  max-width: 75%; 
  height: 65px;
  background-color: #7d161a;
  z-index: 1040;
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  cursor: pointer;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
.bottom-cart-bar.show {
  transform: translateX(-50%) translateY(0); 
}
@media (max-width: 768px) {
  .bottom-cart-bar {
    max-width: 100%; 
  }
}
.cart-drawer-overlay {
  position: fixed; 
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.5);
  z-index: 1045;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
}
.cart-drawer-overlay.open {
  opacity: 1;
  visibility: visible;
}
.cart-drawer {
  position: absolute; 
  bottom: -100%;
  left: 0;
  width: 100%;
  z-index: 1050;
  transition: bottom 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  box-shadow: 0 -5px 20px rgba(0,0,0,0.15);
}
.cart-drawer.open {
  bottom: 0;
}
</style>