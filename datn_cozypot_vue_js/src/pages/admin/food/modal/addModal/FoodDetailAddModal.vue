<script setup>
import { useFoodDetailAdd } from '../../../../../services/foodFunction'; // Import đúng đường dẫn

const {
    formData,
    listMonAn,
    isParentLocked,
    parentFoodName,
    handleSave,
    goBack,
    handleFileUpload
} = useFoodDetailAdd();
</script>

<template>
  <div class="main-content">
    <div class="page-header">
        <div class="header-title">
            <h1>Thêm Chi Tiết Món Ăn</h1>
            <p class="subtitle">Tạo các biến thể (Size, Topping...) cho món ăn</p>
        </div>
        <button class="btn-back" @click="goBack">
            <i class="fas fa-arrow-left"></i> Quay lại
        </button>
    </div>

    <div class="page-content">
        <div class="section-left">
            <div class="card">
                <h3>Thông tin cơ bản</h3>
                <div class="form-container">
                    
                    <div class="form-group">
                        <label>Thuộc món ăn <span class="required">*</span></label>
                        <select 
                            v-model="formData.idMonAnDiKem" 
                            class="form-control"
                            :disabled="isParentLocked"
                            :class="{ 'locked-field': isParentLocked }"
                        >
                            <option value="">-- Chọn món ăn gốc --</option>
                            <option v-for="item in listMonAn" :key="item.id" :value="item.id">
                                {{ item.tenMonAn }}
                            </option>
                        </select>
                        <small v-if="isParentLocked" class="hint-text">
                            *Đang thêm chi tiết cho món: <b>{{ parentFoodName }}</b>
                        </small>
                    </div>

                    <div class="form-group">
                        <label>Tên chi tiết <span class="required">*</span></label>
                        <input v-model="formData.tenChiTietMonAn" type="text" placeholder="VD: Size L, Thêm trứng...">
                    </div>

                    <div class="form-row-2">
                        <div class="form-group">
                            <label>Giá vốn (VNĐ)</label>
                            <input v-model="formData.giaVon" type="number" placeholder="0">
                        </div>
                        <div class="form-group">
                            <label>Giá bán (VNĐ)</label>
                            <input v-model="formData.giaBan" type="number" placeholder="0">
                        </div>
                    </div>

                     <div class="form-group">
                        <label>Mô tả chi tiết</label>
                        <textarea v-model="formData.moTaChiTiet" rows="3"></textarea>
                    </div>
                </div>
            </div>
        </div>

        <div class="section-right">
            <div class="card">
                <h3>Quy cách & Hình ảnh</h3>
                <div class="form-container">
                    
                    <div class="form-row-2">
                        <div class="form-group">
                            <label>Kích cỡ</label>
                            <input v-model="formData.kichCo" type="text" placeholder="VD: L, XL, 500ml...">
                        </div>
                        <div class="form-group">
                            <label>Đơn vị tính</label>
                            <input v-model="formData.donVi" type="text" placeholder="VD: Ly, Tô, Cái...">
                        </div>
                    </div>

                    <div class="form-group">
                        <label>Hình ảnh minh họa</label>
                        <div class="upload-container">
                            <label class="custom-file-upload">
                                <input type="file" accept="image/*" @change="handleFileUpload" />
                                <i class="fas fa-cloud-upload-alt"></i> Chọn ảnh
                            </label>
                            <button v-if="formData.hinhAnh" class="btn-clear-img" @click="formData.hinhAnh = ''">Xóa</button>
                        </div>
                        <div class="image-preview-box" v-if="formData.hinhAnh">
                            <img :src="formData.hinhAnh" class="preview-img">
                        </div>
                        <div class="image-preview-box empty" v-else>
                            <span>Chưa có ảnh</span>
                        </div>
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
    </div>

    <div class="page-footer">
        <button class="btn-large btn-cancel" @click="goBack">Hủy bỏ</button>
        <button class="btn-large btn-save" @click="handleSave">Lưu Chi Tiết</button>
    </div>
  </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");
</style>