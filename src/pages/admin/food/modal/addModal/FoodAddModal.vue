<script setup>
import GlobalDialogue from '../../../../../components/globalDialogue.vue';
import { useFoodAddScreen } from '../../../../../services/foodFunction';

const {
    formData,
    listDanhMuc,
    filteredSubCategories,
    handleSave,
    goBack,
    listChiTiet,
    newDetail,
    addDetailToList,
    removeDetailFromList,
    handleFileUpload,
    dialogVisible, 
    dialogConfig,
    handleDialogConfirm,
    handleDialogClose
} = useFoodAddScreen();

</script>

<template>
    <div class="main-content">
        <GlobalDialogue 
            :show="dialogVisible"
            :type="dialogConfig?.type" 
            :variant="dialogConfig?.variant"
            :title="dialogConfig?.title"
            :message="dialogConfig?.message"
            @close="handleDialogClose"
            @confirm="handleDialogConfirm"
        />
        <div class="page-header">
            <div class="header-title">
                <h1 style="color: #8B0000;">Thêm Món Ăn</h1>
            </div>
            <div class="header-actions">
                <button class="btn-back" @click="goBack">
                    <i class="fas fa-arrow-left"></i> Quay lại
                </button>
            </div>
        </div>

        <div class="page-content">
            <div class="card general-info">
                <h3 class="card-title">Thông tin chung</h3>

                <div class="form-row">
                    <div class="form-group">
                        <label>Tên món ăn <span class="required">*</span></label>
                        <input v-model="formData.tenMonAn" type="text" class="form-control"
                            placeholder="Ví dụ: Trà sữa Trân châu">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Danh mục gốc <span class="required">*</span></label>
                        <select v-model="formData.idDanhMuc" class="form-control">
                            <option value="">-- Chọn danh mục --</option>
                            <option v-for="dm in listDanhMuc" :key="dm.id" :value="dm.id">
                                {{ dm.tenDanhMuc }}
                            </option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Danh mục chi tiết <span class="required">*</span></label>
                        <select v-model="formData.idDanhMucChiTiet" :disabled="!formData.idDanhMuc"
                            class="form-control">
                            <option value="">-- Chọn chi tiết --</option>
                            <option v-for="sub in filteredSubCategories" :key="sub.id" :value="sub.id">
                                {{ sub.tenDanhMucChiTiet }}
                            </option>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label>Hình ảnh món ăn</label>
                    <div class="image-upload-box">
                        <input type="file" @change="handleFileUpload" accept="image/*" class="form-control-file">

                        <input v-model="formData.hinhAnh" type="text" class="form-control mt-2"
                            placeholder="Hoặc dán link ảnh tại đây...">

                        <div v-if="formData.hinhAnh" class="preview-container">
                            <img :src="formData.hinhAnh" class="img-preview" alt="Preview">
                        </div>
                    </div>
                </div>

                <div class="form-group">
                    <label>Mô tả</label>
                    <textarea v-model="formData.moTa" class="form-control" rows="3"></textarea>
                </div>

                <div class="form-group">
                    <label>Trạng thái kinh doanh</label>
                    <div class="toggle-switch" :class="{ 'on': formData.trangThaiKinhDoanh === 1 }"
                        @click="formData.trangThaiKinhDoanh = (formData.trangThaiKinhDoanh === 1 ? 0 : 1)">
                        <div class="toggle-knob"></div>
                    </div>
                </div>
            </div>

            <div class="card detail-info">
                <h3 class="card-title">Chi tiết món ăn</h3>

                <div class="add-detail-box">
                    <div class="detail-inputs">
                        <div class="input-wrap">
                            <label>Tên chi tiết</label>
                            <input v-model="newDetail.tenChiTietMonAn" type="text" placeholder="Size L...">
                        </div>
                        <div class="input-wrap">
                            <label>Kích cỡ</label>
                            <input v-model="newDetail.kichCo" type="text" placeholder="S, M, L...">
                        </div>
                        <div class="input-wrap">
                            <label>Giá bán</label>
                            <input v-model="newDetail.giaBan" type="number" placeholder="0">
                        </div>
                        <div class="input-wrap">
                            <label>Đơn vị</label>
                            <input v-model="newDetail.donVi" type="text" placeholder="Cốc">
                        </div>
                    </div>
                    <button class="btn-add-detail" @click="addDetailToList">
                        <i class="fas fa-plus"></i> Thêm biến thể
                    </button>
                </div>

                <div class="detail-table-container">
                    <table class="detail-table">
                        <thead>
                            <tr>
                                <th>Tên</th>
                                <th>Kích cỡ</th>
                                <th>Giá bán</th>
                                <th>Đơn vị</th>
                                <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(detail, index) in listChiTiet" :key="index">
                                <td>{{ detail.tenChiTietMonAn }}</td>
                                <td>{{ detail.kichCo }}</td>
                                <td style="color: #d32f2f; font-weight: bold;">{{ detail.giaBan.toLocaleString() }} đ
                                </td>
                                <td>{{ detail.donVi }}</td>
                                <td>
                                    <button class="btn-icon-delete" @click="removeDetailFromList(index)">🗑️</button>
                                </td>
                            </tr>

                            <tr v-if="listChiTiet.length === 0">
                                <td colspan="5" class="empty-text"
                                    style="text-align: center; padding: 20px; color: #ff9800; font-style: italic;">
                                    <i class="fas fa-exclamation-circle"></i>
                                    Vui lòng thêm ít nhất một chi tiết (biến thể) ở bảng trên trước khi lưu.
                                </td>
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

<style scoped src="/src/assets/foodModalManager.css"></style>

<style scoped>
/* Additional Styles */




.btn-save {
    padding: 8px 16px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-weight: bold;
    display: flex;
    align-items: center;
    gap: 5px;
}

.btn-add-detail {
    padding: 6px 12px;
    background: #8B0000;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-weight: bold;
    display: flex;
    align-items: center;
    gap: 5px;
    margin: 10px 0px 10px 0px;
}


/* Image Upload Style */
.image-upload-box {
    border: 1px dashed #ccc;
    padding: 15px;
    border-radius: 6px;
    background: #f9f9f9;
}

.form-control-file {
    width: 100%;
    padding: 5px;
}

.mt-2 {
    margin-top: 10px;
}

.preview-container {
    margin-top: 10px;
    text-align: center;
}

.img-preview {
    max-width: 100%;
    height: 150px;
    object-fit: contain;
    border: 1px solid #ddd;
    border-radius: 4px;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}



.btn-save-large {
    padding: 12px 24px;
    background: #8B0000;
    color: white;
    border: none;
    border-radius: 6px;
    font-weight: bold;
    cursor: pointer;
    box-shadow: 0 2px 5px rgba(211, 47, 47, 0.3);
}

.btn-save-large:hover {
    background: #b71c1c;
}

@media (max-width: 1024px) {
    .page-content {
        grid-template-columns: 1fr;
    }

    .page-footer {
        left: 0;
    }
}
</style>