<script setup>
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import { useFoodAddScreen } from '../../../../../services/foodFunction'

const {
    formData, listDanhMuc, filteredSubCategories, handleSave, goBack,
    listChiTiet, newDetail, addDetailToList, removeDetailFromList,
    
    // Ảnh
    mainFileInput, handleMainFileUpload, triggerMainImageUpload,
    detailFileInput, handleDetailImageUpload, triggerDetailImageUpload,

    dialogVisible, dialogConfig, handleDialogConfirm, handleDialogClose
} = useFoodAddScreen();
</script>

<template>
    <div class="main-content">
        <GlobalDialogue :show="dialogVisible" :type="dialogConfig?.type" :variant="dialogConfig?.variant"
            :title="dialogConfig?.title" :message="dialogConfig?.message" @close="handleDialogClose"
            @confirm="handleDialogConfirm" />

        <div class="page-header">
            <div class="header-title"><h1 style="color: #8B0000;">Thêm Món Ăn</h1></div>
            <div class="header-actions">
                <button class="btn-back" @click="goBack"><i class="fas fa-arrow-left"></i> Quay lại</button>
            </div>
        </div>

        <div class="page-content">
            <div class="card general-info">
                <h3 class="card-title">Thông tin chung</h3>
                
                <div class="form-group">
                    <label>Tên món ăn <span class="required">*</span></label>
                    <input v-model="formData.tenMonAn" type="text" class="form-control" placeholder="Nhập tên món...">
                </div>

                <div class="form-row-2">
                    <div class="form-group">
                        <label>Danh mục gốc <span class="required">*</span></label>
                        <select v-model="formData.idDanhMuc" class="form-control">
                            <option value="">-- Chọn danh mục --</option>
                            <option v-for="dm in listDanhMuc" :key="dm.id" :value="dm.id">{{ dm.tenDanhMuc }}</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Chi tiết <span class="required">*</span></label>
                        <select v-model="formData.idDanhMucChiTiet" :disabled="!formData.idDanhMuc" class="form-control">
                            <option value="">-- Chọn chi tiết --</option>
                            <option v-for="sub in filteredSubCategories" :key="sub.id" :value="sub.id">{{ sub.tenDanhMucChiTiet }}</option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label>Hình ảnh chính <span class="required">*</span></label>
                    
                    <input type="file" ref="mainFileInput" style="display: none" accept="image/*" @change="handleMainFileUpload">

                    <div class="image-upload-wrapper">
                        <div class="upload-btn-area" @click="triggerMainImageUpload">
                            <i class="fas fa-cloud-upload-alt"></i>
                            <span>Chọn ảnh</span>
                        </div>

                        <div v-if="formData.hinhAnh" class="preview-box">
                            <img :src="formData.hinhAnh" alt="Preview">
                            <button class="btn-remove-img" @click.stop="formData.hinhAnh = ''">×</button>
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea v-model="formData.moTa" class="form-control" rows="3"></textarea>
                </div>

                <div class="form-group">
                    <label>Trạng thái</label>
                    <div class="toggle-switch" :class="{ 'on': formData.trangThaiKinhDoanh === 1 }"
                        @click="formData.trangThaiKinhDoanh = (formData.trangThaiKinhDoanh === 1 ? 0 : 1)">
                        <div class="toggle-knob"></div>
                    </div>
                </div>
            </div>

            <div class="card detail-info">
                <h3 class="card-title">Thêm chi tiết món</h3>

                <div class="add-detail-box">
                    <div class="detail-inputs-grid">
                        <div class="text-inputs">
                            <div class="input-wrap">
                                <label>Tên chi tiết món <span class="required">*</span></label>
                                <input v-model="newDetail.tenChiTietMonAn" type="text" placeholder="VD: Size L...">
                            </div>
                            <div class="row-inputs">
                                <div class="input-wrap">
                                    <label>Kích cỡ <span class="required">*</span></label>
                                    <input v-model="newDetail.kichCo" type="text" placeholder="S, M...">
                                </div>
                                <div class="input-wrap">
                                    <label>Đơn vị</label>
                                    <input v-model="newDetail.donVi" type="text" placeholder="Cốc">
                                </div>
                            </div>
                            <div class="input-wrap">
                                <label>Giá bán <span class="required">*</span></label>
                                <input v-model="newDetail.giaBan" type="number" placeholder="0">
                            </div>
                        </div>

                        <div class="detail-img-input">
                            <label>Ảnh chi tiết món <span class="required">*</span></label>
                            
                            <input type="file" ref="detailFileInput" style="display: none" accept="image/*" @change="handleDetailImageUpload">
                            
                            <div class="upload-btn-area" @click="triggerDetailImageUpload">
                                <img v-if="newDetail.hinhAnh" :src="newDetail.hinhAnh" class="mini-preview">
                                <div v-else class="upload-placeholder">
                                    <i class="fas fa-camera"></i>
                                    <span>Tải ảnh</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <button class="btn-add-detail" @click="addDetailToList" style="margin-bottom: 15px;">
                        <i class="fas fa-plus-circle"></i> Thêm vào danh sách
                    </button>
                </div>

                <div class="detail-table-container">
                    <table class="detail-table">
                        <thead>
                            <tr>
                                <th>Ảnh</th>
                                <th>Tên</th>
                                <th>Kích cỡ</th>
                                <th>Giá bán</th>
                                <th>Đơn vị</th>
                                <th>Xóa</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(detail, index) in listChiTiet" :key="index">
                                <td class="col-img">
                                    <img :src="detail.hinhAnh" alt="img" class="table-thumb">
                                </td>
                                <td>{{ detail.tenChiTietMonAn }}</td>
                                <td>{{ detail.kichCo }}</td>
                                <td class="price-text">{{ detail.giaBan.toLocaleString() }} đ</td>
                                <td>{{ detail.donVi }}</td>
                                <td>
                                    <button class="btn-icon-delete" @click="removeDetailFromList(index)">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                            <tr v-if="listChiTiet.length === 0">
                                <td colspan="6" class="empty-text">Chưa có chi tiết món nào.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

        <div class="page-footer">
            <button class="btn-cancel-large" @click="goBack">Hủy bỏ</button>
            <button class="btn-save-large" @click="handleSave">Lưu món ăn</button>
        </div>
    </div>
</template>

<style scoped>
@import url("/src/assets/foodModalManager.css");

/* CSS Mới cho phần Upload Ảnh */
.image-upload-wrapper {
    display: flex;
    gap: 15px;
    align-items: flex-start;
    margin-top: 5px;
}

.upload-btn-area {
    width: 100px;
    height: 100px;
    border: 2px dashed #ccc;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #666;
    background: #f9f9f9;
    transition: all 0.2s;
}
.upload-btn-area:hover { border-color: #8B0000; color: #8B0000; background: #fff5f5; }

.preview-box {
    position: relative;
    width: 100px;
    height: 100px;
    border-radius: 8px;
    border: 1px solid #ddd;
    overflow: hidden;
}
.preview-box img { width: 100%; height: 100%; object-fit: cover; }
.btn-remove-img {
    position: absolute; top: 0; right: 0;
    background: rgba(0,0,0,0.5); color: #fff;
    border: none; width: 20px; height: 20px;
    cursor: pointer; font-size: 14px;
}

/* Layout phần thêm chi tiết */
.detail-inputs-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 20px;
    margin-bottom: 15px;
}

.row-inputs {
    display: flex;
    gap: 10px;
}
.row-inputs .input-wrap { flex: 1; }

.detail-img-input {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.mini-upload-area {
    width: 100%;
    height: 120px;
    border: 2px dashed #ddd;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    background: #fff;
    overflow: hidden;
}
.mini-preview { width: 100%; height: 100%; object-fit: contain; }
.upload-placeholder { display: flex; flex-direction: column; align-items: center; color: #aaa; }

.btn-add-detail {
    width: 100%;
    padding: 10px;
    background: #8B0000; 
    color: white;
    border: none; border-radius: 6px;
    font-weight: bold; cursor: pointer;
    display: flex; align-items: center; justify-content: center; gap: 8px;
}

/* Table Style */
.col-img { width: 60px; text-align: center; }
.table-thumb { width: 40px; height: 40px; object-fit: cover; border-radius: 4px; border: 1px solid #eee; }
.price-text { color: #d32f2f; font-weight: bold; }

@media (max-width: 900px) {
    .detail-inputs-grid { grid-template-columns: 1fr; }
    .page-content { grid-template-columns: 1fr; }
}
</style>