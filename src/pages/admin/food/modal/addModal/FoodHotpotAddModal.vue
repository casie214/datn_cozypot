<script setup>
import { defineProps, defineEmits } from 'vue';
import { useHotpotAdd } from '../../../../../services/foodFunction'; 

const props = defineProps({
  isOpen: Boolean
});

const emit = defineEmits(['close', 'refresh']);

const {
    formData,
    listLoaiSet,
    listFoodDetails,
    selectedIngredients,
    currentIngredientId,
    totalComponentsPrice,
    addIngredient,
    removeIngredient,
    handleSave,
    goBack
} = useHotpotAdd();

</script>

<template>
  <div class="main-content">
    <div class="page-header flex-row-all-around">
        <h1>Thêm Set Lẩu Mới</h1>
        <button class="btn-back" @click="goBack">← Quay lại</button>
    </div>

    <div class="page-content">
        <div class="section-left">
            <div class="card">
                <h3>Thông tin chung</h3>
                <div class="form-container">
                    <div class="form-group">
                        <label>Tên Set Lẩu <span class="required">*</span></label>
                        <input v-model="formData.tenSetLau" type="text" placeholder="VD: Combo Lẩu Thái 2 Người">
                    </div>

                    <div class="form-group">
                        <label>Loại Set <span class="required">*</span></label>
                        <select v-model="formData.idLoaiSet" class="form-control">
                            <option value="">-- Chọn loại --</option>
                            <option v-for="cat in listLoaiSet" :key="cat.id" :value="cat.id">
                                {{ cat.tenLoaiSet }}
                            </option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label>Giá bán (VNĐ) <span class="required">*</span></label>
                        <input v-model="formData.giaBan" type="number" placeholder="0">
                        <small class="hint">Tổng giá món lẻ: {{ totalComponentsPrice?.toLocaleString() }} đ</small>
                    </div>

                    <div class="form-group">
                        <label>Hình ảnh (URL)</label>
                        <input v-model="formData.hinhAnh" type="text" placeholder="https://...">
                    </div>

                    <div class="form-group">
                        <label>Mô tả ngắn</label>
                        <textarea v-model="formData.moTa" rows="3"></textarea>
                    </div>

                    <div class="form-group">
                        <label>Trạng thái</label>
                        <div class="toggle-wrapper" @click="formData.trangThai = formData.trangThai === 1 ? 0 : 1">
                            <div class="toggle-switch" :class="{ 'on': formData.trangThai === 1 }">
                                <div class="toggle-knob"></div>
                            </div>
                            <span>{{ formData.trangThai === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh' }}</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="section-right">
            <div class="card">
                <h3>Thành phần Set</h3>
                <p class="desc">Chọn các món có trong Set này và nhập số lượng.</p>

                <div class="add-box">
                    <select v-model="currentIngredientId" class="form-control select-search">
                        <option value="">-- Tìm món ăn --</option>
                        <option v-for="food in listFoodDetails" :key="food.id" :value="food.id">
                            {{ food.tenChiTietMonAn }} ({{ food.donVi }}) - {{ food.giaBan?.toLocaleString() }}đ
                        </option>
                    </select>
                    <button class="btn-add" @click="addIngredient" :disabled="!currentIngredientId">+ Thêm</button>
                </div>

                <div class="table-wrap">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Tên món</th>
                                <th>Đơn vị</th>
                                <th width="100px">Số lượng</th>
                                <th width="50px"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-if="selectedIngredients.length === 0">
                                <td colspan="4" class="empty">Chưa có món nào được chọn</td>
                            </tr>
                            <tr v-for="(item, index) in selectedIngredients" :key="item.id">
                                <td>{{ item.ten }}</td>
                                <td>{{ item.donVi }}</td>
                                <td>
                                    <input type="number" v-model="item.soLuong" min="1" class="qty-input">
                                </td>
                                <td>
                                    <button class="btn-del" @click="removeIngredient(index)">🗑️</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="page-footer">
        <button class="btn-large btn-cancel" @click="goBack">Hủy</button>
        <button class="btn-large btn-save" @click="handleSave">Lưu Set Lẩu</button>
    </div>
  </div>
</template>

<style scoped src="/src/assets/foodModalManager.css">
</style>