<script setup>
import Sidebar from '../../../components/sidebar.vue';
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

//  Hàm thêm món vào danh sách
const addToOrder = (food) => {
    // Kiểm tra món đã có trong list chưa
    const existingItem = selectedItems.value.find(item => item.id === food.id);
    
    if (existingItem) {
        // Nếu có rồi -> Tăng số lượng
        existingItem.quantity++;
    } else {
        // Nếu chưa -> Thêm mới vào mảng
        selectedItems.value.push({
            ...food,
            quantity: 1,
            note: ''
        });
    }
};

//  Các hàm tăng giảm số lượng / Xóa
const increaseQty = (index) => {
    selectedItems.value[index].quantity++;
};

const decreaseQty = (index) => {
    if (selectedItems.value[index].quantity > 1) {
        selectedItems.value[index].quantity--;
    } else {
        // Nếu giảm về 0 thì hỏi xóa
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

// Tính toán tiền tự động (Computed)
const subTotal = computed(() => {
    return selectedItems.value.reduce((total, item) => total + (item.rawPrice * item.quantity), 0);
});

const tax = computed(() => subTotal.value * 0.1); // 10% thuế
const total = computed(() => subTotal.value + tax.value);

// Hàm format tiền tệ (VND)
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
    // Logic lưu vào DB hoặc API ở đây
    alert("Đã thêm món thành công!");
    router.back();
};
</script>

<template>
  <div class="app-layout">
    <Sidebar />

    <main class="main-content">
      <div class="header-section">
          <div>
            <h1 class="page-title">Thêm món</h1>
            <p class="sub-title">Thêm món vào đơn hàng</p>
          </div>
          <button class="btn-back" @click="handleBack">⬅ Quay lại</button>
      </div>

      <div class="add-food-container">
        
        <div class="left-panel">
            <div class="panel-header">
                <h3>🍴 Chọn món ăn</h3>
            </div>
            <div class="search-box">
                <input type="text" placeholder="Tìm kiếm món ăn..." class="form-control" />
            </div>
            
            <div class="food-grid">
                <div v-for="food in foods" :key="food.id" class="food-card" @click="addToOrder(food)">
                    <div class="food-icon">{{ food.icon }}</div>
                    <div class="food-details">
                        <b>{{ food.name }}</b>
                        <span class="price">{{ food.priceStr }}</span>
                        <span class="time">{{ food.time }}</span>
                    </div>
                </div>
            </div>
        </div>

        <div class="right-panel">
            <h3>Tóm tắt</h3>
            
            <div class="selected-list">
                
                <div v-if="selectedItems.length === 0" class="empty-state">
                    Chưa có món nào được chọn
                </div>

                <div v-for="(item, index) in selectedItems" :key="item.id" class="summary-card">
                    <div class="summary-item">
                        <div class="item-header">
                            <b>{{ item.name }}</b>
                            <div class="qty-control">
                                <button @click="decreaseQty(index)">-</button> 
                                <span>{{ item.quantity }}</span> 
                                <button class="btn-plus" @click="increaseQty(index)">+</button> 
                                <button class="btn-x" @click="removeItem(index)">✖</button>
                            </div>
                        </div>
                        <div class="item-price-row">
                             {{ formatMoney(item.rawPrice * item.quantity) }}
                        </div>
                        <input type="text" v-model="item.note" placeholder="Ghi chú cho món này..." class="note-input" />
                    </div>
                </div>

            </div>

            <div class="footer-summary">
                <div class="summary-totals">
                    <div class="row"><span>Tạm tính:</span> <b>{{ formatMoney(subTotal) }}</b></div>
                    <div class="row"><span>Thuế(10%):</span> <b>{{ formatMoney(tax) }}</b></div>
                    <div class="row total"><span>Tổng cộng:</span> <b class="red">{{ formatMoney(total) }}</b></div>
                </div>

                <div class="form-group">
                    <label>Ghi chú chung</label>
                    <textarea class="form-control" placeholder="Ghi chú đơn hàng..." rows="2"></textarea>
                </div>

                <div class="action-buttons">
                    <button class="btn-confirm-add" @click="handleConfirmAdd">Thêm món</button>
                    <button class="btn-clear-all" @click="clearAll">Xóa tất cả</button>
                </div>
            </div>
        </div>

      </div>
    </main>
  </div>
</template>

<style scoped>
.app-layout { min-height: 100vh; background-color: #fff; }
.main-content { margin-left: 250px; width: calc(100% - 250px); padding: 30px 40px; }

.header-section { display: flex; justify-content: space-between; margin-bottom: 20px; }
.page-title { color: #8B0000; margin: 0; font-size: 24px; }
.sub-title { color: #8B0000; font-size: 14px; margin-top: 5px; }
.btn-back { background: #e0e0e0; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; font-weight: 600; height: fit-content; }

.add-food-container { display: flex; gap: 20px; height: calc(100vh - 150px);  }


.left-panel { flex: 2; border: 1px solid #ddd; border-radius: 8px; padding: 20px; background: #fff; display: flex; flex-direction: column; height: 100%; }
.panel-header { background: #8B0000; color: white; padding: 10px; text-align: center; border-radius: 4px; margin-bottom: 15px; }
.panel-header h3 { margin: 0; font-size: 16px; }
.search-box { margin-bottom: 20px; }
.form-control { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; outline: none; }

.food-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 15px; overflow-y: auto; padding-right: 5px; }
.food-card { border: 1px solid #eee; border-radius: 8px; padding: 10px; display: flex; gap: 10px; cursor: pointer; transition: 0.2s; box-shadow: 0 2px 5px rgba(0,0,0,0.05); height: fit-content; }
.food-card:hover { border-color: #8B0000; background: #fff5f5; transform: translateY(-2px); }
.food-card:active { transform: scale(0.98); }
.food-icon { width: 50px; height: 50px; background: #ddd; display: flex; align-items: center; justify-content: center; font-size: 24px; border-radius: 4px; }
.food-details { display: flex; flex-direction: column; }
.food-details b { font-size: 13px; margin-bottom: 2px; }
.food-details .price { color: #d32f2f; font-weight: bold; font-size: 13px; }
.food-details .time { font-size: 11px; color: #888; }


.right-panel { flex: 1; border: 1px solid #ddd; border-radius: 8px; padding: 20px; background: #f9f9f9; display: flex; flex-direction: column; height: 100%; }
.right-panel h3 { margin-top: 0; font-size: 16px; margin-bottom: 15px; }


.selected-list { flex: 1; overflow-y: auto; margin-bottom: 15px; padding-right: 5px; }
.empty-state { text-align: center; color: #999; font-style: italic; margin-top: 50px; }

.summary-card { background: white; border: 1px solid #ddd; border-radius: 4px; padding: 10px; margin-bottom: 10px; }
.item-header { display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 5px; align-items: center; }
.qty-control { display: flex; gap: 5px; align-items: center; }
.qty-control button { width: 24px; height: 24px; display: flex; align-items: center; justify-content: center; border: 1px solid #ccc; background: #fff; cursor: pointer; border-radius: 4px; font-weight: bold; }
.qty-control button:hover { background: #eee; }
.btn-plus { background: #8B0000 !important; color: white; border-color: #8B0000 !important; }
.btn-x { background: #fff !important; color: #d32f2f !important; border-color: #ddd !important; font-size: 12px; margin-left: 5px; }

.item-price-row { font-size: 13px; font-weight: bold; margin-bottom: 5px; color: #333; }
.note-input { width: 100%; padding: 6px; border: 1px solid #eee; font-size: 12px; outline: none; border-radius: 4px; background: #fcfcfc; }


.footer-summary { border-top: 2px solid #ddd; padding-top: 15px; }

.summary-totals .row { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; }
.summary-totals .total { font-size: 18px; margin-top: 10px; border-top: 1px solid #ddd; padding-top: 10px; }
.red { color: #d32f2f; }

.form-group { margin: 15px 0; }
.form-group label { font-size: 13px; font-weight: bold; display: block; margin-bottom: 5px; }

.action-buttons { margin-top: auto; display: flex; flex-direction: column; gap: 10px; }
.btn-confirm-add { background: #b71c1c; color: white; border: none; padding: 12px; border-radius: 4px; font-weight: bold; cursor: pointer; transition: 0.2s; }
.btn-confirm-add:hover { background: #8B0000; }
.btn-clear-all { background: #e0e0e0; color: #333; border: none; padding: 12px; border-radius: 4px; font-weight: bold; cursor: pointer; }
.btn-clear-all:hover { background: #d0d0d0; }
</style>