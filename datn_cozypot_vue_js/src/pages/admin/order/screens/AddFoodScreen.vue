<script setup>
import { useRouter } from 'vue-router';
import { ref, computed } from 'vue';

const router = useRouter();

const foods = ref([
  { id: 1, name: 'Lẩu tôm chua cay', priceStr: '320.000 đ', rawPrice: 320000, time: 'Dự kiến 30p', icon: '🍲' },
  { id: 2, name: 'Coca Cola', priceStr: '15.000 đ', rawPrice: 15000, time: 'Dự kiến 1p', icon: '🥤' },
  { id: 3, name: 'Canh chua cá lóc', priceStr: '250.000 đ', rawPrice: 250000, time: 'Dự kiến 30p', icon: '🐟' },
  { id: 4, name: 'Combo Lẩu Hải Sản', priceStr: '550.000 đ', rawPrice: 550000, time: 'Dự kiến 30p', icon: '🐙' },
  { id: 5, name: 'Nước suối', priceStr: '10.000 đ', rawPrice: 10000, time: 'Dự kiến 1p', icon: '💧' },
  { id: 6, name: 'Cơm chiên dương châu', priceStr: '120.000 đ', rawPrice: 120000, time: 'Dự kiến 15p', icon: '🍚' },
]);

const selectedItems = ref([]);

// --- Logic giữ nguyên ---
const addToOrder = (food) => {
    const existingItem = selectedItems.value.find(item => item.id === food.id);
    if (existingItem) {
        existingItem.quantity++;
    } else {
        selectedItems.value.push({ ...food, quantity: 1, note: '' });
    }
};

const increaseQty = (index) => {
    selectedItems.value[index].quantity++;
};

const decreaseQty = (index) => {
    if (selectedItems.value[index].quantity > 1) {
        selectedItems.value[index].quantity--;
    } else {
        removeItem(index);
    }
};

const removeItem = (index) => {
    selectedItems.value.splice(index, 1);
};

const clearAll = () => {
    if(confirm("Bạn có chắc muốn xóa tất cả món đã chọn?")) {
        selectedItems.value = [];
    }
};

const subTotal = computed(() => {
    return selectedItems.value.reduce((total, item) => total + (item.rawPrice * item.quantity), 0);
});

const tax = computed(() => subTotal.value * 0.1);
const total = computed(() => subTotal.value + tax.value);

const formatMoney = (amount) => {
    return amount.toLocaleString('vi-VN') + ' đ';
};

const handleBack = () => {
    router.back();
};

const handleConfirmAdd = () => {
    if (selectedItems.value.length === 0) {
        alert("Vui lòng chọn ít nhất 1 món!");
        return;
    }
    alert("Đã thêm món thành công!");
    router.back();
};
</script>

<template>
  <div class="d-flex" style="background-color: #f8f9fa; min-height: 100vh;">
    <main class="flex-grow-1 p-4" style="height: 100vh; overflow: hidden; display: flex; flex-direction: column;">
      
      <div class="d-flex justify-content-between align-items-center mb-3">
          <div>
            <h2 class="fw-bold mb-0 text-custom-red">Thêm món</h2>
            <small class="text-muted">Chọn món ăn để thêm vào đơn hàng hiện tại</small>
          </div>
          <button class="btn btn-secondary fw-bold shadow-sm" @click="handleBack">
            <i class="fa-solid fa-arrow-left me-2"></i>Quay lại
          </button>
      </div>

      <div class="row g-3 flex-grow-1" style="min-height: 0;"> 
        
        <div class="col-lg-8 d-flex flex-column h-100">
            <div class="card border-0 shadow-sm h-100">
                <div class="card-header bg-white border-bottom-0 p-3 pb-0">
                     <div class="d-flex justify-content-between align-items-center mb-3">
                        <h5 class="mb-0 fw-bold text-dark"><i class="fa-solid fa-utensils me-2 text-custom-red"></i>Menu Món Ăn</h5>
                     </div>
                     <div class="input-group mb-2">
                        <span class="input-group-text bg-light border-end-0"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
                        <input type="text" class="form-control border-start-0 bg-light" placeholder="Tìm kiếm tên món ăn..." />
                     </div>
                </div>

                <div class="card-body overflow-auto custom-scrollbar p-3">
                    <div class="row g-3">
                        <div v-for="food in foods" :key="food.id" class="col-md-6 col-xl-4">
                            <div class="card h-100 food-card border cursor-pointer position-relative" @click="addToOrder(food)">
                                <div class="card-body d-flex align-items-center gap-3 p-3">
                                    <div class="food-icon rounded-3 bg-light d-flex align-items-center justify-content-center flex-shrink-0">
                                        {{ food.icon }}
                                    </div>
                                    <div class="flex-grow-1">
                                        <h6 class="fw-bold mb-1 text-dark line-clamp-2">{{ food.name }}</h6>
                                        <div class="d-flex justify-content-between align-items-center">
                                            <span class="text-custom-red fw-bold">{{ food.priceStr }}</span>
                                        </div>
                                        <small class="text-muted" style="font-size: 0.75rem;">
                                            <i class="fa-regular fa-clock me-1"></i>{{ food.time }}
                                        </small>
                                    </div>
                                    <div class="add-icon-overlay">
                                        <i class="fa-solid fa-plus text-white"></i>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-lg-4 d-flex flex-column h-100">
            <div class="card border-0 shadow-sm h-100">
                <div class="card-header bg-custom-red text-white py-3">
                    <h5 class="mb-0 fw-bold"><i class="fa-solid fa-receipt me-2"></i>Món đã chọn</h5>
                </div>

                <div class="card-body overflow-auto custom-scrollbar p-0 flex-grow-1 bg-light">
                    <div v-if="selectedItems.length === 0" class="d-flex flex-column align-items-center justify-content-center h-100 text-muted opacity-50">
                        <i class="fa-solid fa-basket-shopping display-4 mb-3"></i>
                        <p>Chưa có món nào được chọn</p>
                    </div>

                    <ul v-else class="list-group list-group-flush">
                        <li v-for="(item, index) in selectedItems" :key="item.id" class="list-group-item p-3 border-bottom">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <span class="fw-bold text-dark">{{ item.name }}</span>
                                <span class="fw-bold text-dark">{{ formatMoney(item.rawPrice * item.quantity) }}</span>
                            </div>
                            
                            <div class="d-flex justify-content-between align-items-center gap-2">
                                <input 
                                    type="text" 
                                    v-model="item.note" 
                                    class="form-control form-control-sm bg-light border-0" 
                                    placeholder="Ghi chú..." 
                                    style="font-size: 0.85rem;"
                                />

                                <div class="btn-group btn-group-sm shadow-sm" role="group">
                                    <button type="button" class="btn btn-white border" @click="decreaseQty(index)">-</button>
                                    <button type="button" class="btn btn-white border px-3 fw-bold disabled text-dark" style="opacity: 1; background: #fff;">{{ item.quantity }}</button>
                                    <button type="button" class="btn btn-custom-red text-white" @click="increaseQty(index)">+</button>
                                </div>
                                
                                <button class="btn btn-sm btn-outline-danger border-0" @click="removeItem(index)">
                                    <i class="fa-solid fa-xmark"></i>
                                </button>
                            </div>
                        </li>
                    </ul>
                </div>

                <div class="card-footer bg-white border-top p-3 shadow-top">
                    <div class="d-flex justify-content-between mb-1 small text-muted">
                        <span>Tạm tính:</span>
                        <span>{{ formatMoney(subTotal) }}</span>
                    </div>
                    <div class="d-flex justify-content-between mb-2 small text-muted">
                        <span>Thuế (10%):</span>
                        <span>{{ formatMoney(tax) }}</span>
                    </div>
                    <div class="d-flex justify-content-between align-items-center mb-3 pt-2 border-top">
                        <span class="fw-bold fs-5 text-dark">Tổng cộng:</span>
                        <span class="fw-bold fs-4 text-custom-red">{{ formatMoney(total) }}</span>
                    </div>

                    <div class="mb-3">
                        <textarea class="form-control form-control-sm bg-light" rows="2" placeholder="Ghi chú chung cho đơn hàng..."></textarea>
                    </div>

                    <div class="d-flex gap-2">
                        <button class="btn btn-light text-muted fw-bold flex-grow-1" @click="clearAll">
                            Hủy bỏ
                        </button>
                        <button class="btn btn-custom-red text-white fw-bold flex-grow-1 py-2" @click="handleConfirmAdd">
                            <i class="fa-solid fa-check me-2"></i>Xác nhận thêm
                        </button>
                    </div>
                </div>
            </div>
        </div>

      </div>
    </main>
  </div>
</template>

<style scoped>
.text-custom-red { color: #8B0000 !important; }
.bg-custom-red { background-color: #8B0000 !important; }
.btn-custom-red {
    background-color: #8B0000;
    border-color: #8B0000;
}
.btn-custom-red:hover {
    background-color: #6d0000;
    border-color: #6d0000;
}

.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: #f1f1f1; }
.custom-scrollbar::-webkit-scrollbar-thumb { background: #ccc; border-radius: 3px; }
.custom-scrollbar::-webkit-scrollbar-thumb:hover { background: #8B0000; }

.food-icon {
    width: 48px;
    height: 48px;
    font-size: 1.5rem;
}
.line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}
.food-card { transition: all 0.2s; border: 1px solid #eee; }
.food-card:hover {
    border-color: #8B0000 !important;
    background-color: #fff5f5;
    transform: translateY(-2px);
    box-shadow: 0 .5rem 1rem rgba(0,0,0,.15)!important;
}

.add-icon-overlay {
    position: absolute;
    top: -10px; right: -10px;
    width: 30px; height: 30px;
    background-color: #8B0000;
    border-radius: 50%;
    display: flex; align-items: center; justify-content: center;
    opacity: 0;
    transition: 0.2s;
    box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}
.food-card:hover .add-icon-overlay {
    opacity: 1;
    top: -8px; right: -8px;
}

.shadow-top {
    box-shadow: 0 -4px 10px rgba(0,0,0,0.05);
}
</style>