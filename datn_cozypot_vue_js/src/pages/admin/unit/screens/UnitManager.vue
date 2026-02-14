<script setup>
import { nextTick, ref } from 'vue';
import CommonPagination from '@/components/commonPagination.vue';
import { foodApi, useUnitManager } from '../../../../services/foodFunction';
import Swal from 'sweetalert2';
import UnitAddScreen from './UnitAddScreen.vue';
import UnitUpdateScreen from './UnitUpdateScreen.vue';
import Multiselect from '@vueform/multiselect';

const expandedRows = ref([]); // Chứa danh sách ID của các hàng đang mở

const toggleRow = (id) => {
  if (expandedRows.value.includes(id)) {
    // Nếu đang mở thì đóng lại
    expandedRows.value = expandedRows.value.filter(rowId => rowId !== id);
  } else {
    // Nếu đang đóng thì mở ra
    expandedRows.value.push(id);
  }
};

const {
  isLoading, paginatedData, searchQuery, sortOption, totalElements, listCategories,
  currentPage, totalPages, itemsPerPage, fetchAllData, exportToExcel, categoryFilter
} = useUnitManager();

const isModalOpen = ref(false);
const isModalUpdateOpen = ref(false);
const selectedItem = ref(null);

const openUpdateModal = (item) => {
  selectedItem.value = item;
  isModalUpdateOpen.value = true;
};

const handleRefreshListBtn = async () => {
  const newData = await fetchAllData();

  if (selectedItem.value && newData) {
    const updatedItem = newData.find(i => i.id === selectedItem.value.id);

    if (updatedItem) {
      await nextTick();
      selectedItem.value = { ...updatedItem };
    }
  }
  Swal.fire({
    icon: 'success',
    title: 'Đã cập nhật dữ liệu mới nhất',
    timer: 1000,
    showConfirmButton: false,
    toast: true,
    position: 'top-end'
  });
};

const handleRefresh = async () => {
  await fetchAllData();
  isModalOpen.value = false;
  isModalUpdateOpen.value = false;
};

const handleEditValue = async (valItem, parentUnit) => {
    // Sử dụng SweetAlert với HTML custom để tạo form sửa nhanh
    const { value: formValues } = await Swal.fire({
        title: `Cập nhật: ${valItem.giaTri} ${parentUnit.tenDonVi}`,
        html: `
            <div style="text-align: left">
                <label class="fw-bold">Giá trị mới:</label>
                <input id="swal-input1" class="swal2-input" style="margin: 5px 0 15px 0; width: 100%" value="${valItem.giaTri}">
                
                <label class="fw-bold">Trạng thái:</label>
                <select id="swal-input2" class="swal2-select" style="margin: 5px 0 0 0; width: 100%; display: block;">
                    <option value="1" ${valItem.trangThai === 1 ? 'selected' : ''}>Hoạt động (Hiển thị)</option>
                    <option value="0" ${valItem.trangThai === 0 ? 'selected' : ''}>Ngừng hoạt động (Ẩn)</option>
                </select>
            </div>
        `,
        focusConfirm: false,
        showCancelButton: true,
        confirmButtonColor: '#8B0000',
        confirmButtonText: 'Lưu thay đổi',
        cancelButtonText: 'Hủy',
        preConfirm: () => {
            return {
                giaTri: document.getElementById('swal-input1').value,
                trangThai: parseInt(document.getElementById('swal-input2').value)
            }
        }
    });

    if (formValues) {
        // Kiểm tra dữ liệu rỗng
        if (!formValues.giaTri.trim()) {
            return Swal.fire('Lỗi', 'Giá trị không được để trống', 'error');
        }

        try {
            // Gọi API Update Single
            await foodApi.updateSingleUnit(valItem.id, {
                id: valItem.id,
                giaTri: formValues.giaTri,
                trangThai: formValues.trangThai
            });

            Swal.fire({
                icon: 'success',
                title: 'Đã cập nhật!',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 1500
            });

            // Refresh lại list để cập nhật giao diện
            await fetchAllData();
        } catch (error) {
            console.error(error);
            Swal.fire('Lỗi', 'Không thể cập nhật. Vui lòng thử lại', 'error');
        }
    }
};
</script>

<template>
  <div class="tab-content">
    <div class="filter-box">
      <div class="filter-row">
        <div class="filter-item search">
          <label>Tìm kiếm định lượng</label>
          <div class="input-group">
            <input v-model="searchQuery" class="form-control form-search" type="text"
              placeholder="Nhập tên hiển thị, kích cỡ..." />
            <button class="search-btn"><i class="fas fa-search"></i></button>
          </div>
        </div>

        <div class="filter-item">
          <label>Lọc theo danh mục</label>
          <div class="multiselect-wrapper" style="width: 220px;">
            <Multiselect v-model="categoryFilter" :options="listCategories" label="tenDanhMuc" valueProp="id"
              placeholder="-- Tất cả --" :searchable="true" :can-clear="true" />
          </div>
        </div>

        <div class="filter-item">
          <label>Sắp xếp</label>
          <select v-model="sortOption" class="form-control">
            <option value="id_desc">Mới nhất</option>
            <option value="id_asc">Cũ nhất</option>
            <option value="name_asc">Tên hiển thị (A-Z)</option>
          </select>
        </div>

        <button class="btn-clear" @click="searchQuery = ''; sortOption = 'id_desc'; categoryFilter = null">
          Xóa bộ lọc
        </button>
      </div>
    </div>

    <div class="action-row" style="margin-left: auto; display: flex; gap: 10px;">
      <button class="btn-action-icon btn-add-only" @click="isModalOpen = true" title="Thêm định lượng mới">
        <i class="fas fa-plus"></i>
      </button>
      <button class="btn-action-icon" @click="exportToExcel" title="Xuất Excel">
        <i class="fas fa-file-excel"></i>
      </button>
      <button class="btn-action-icon btn-refresh-only" @click="handleRefreshListBtn" title="Tải lại">
        <i class="fas fa-sync-alt"></i>
      </button>
    </div>

    <div class="table-container" style="min-height: 278px;">
      <div v-if="isLoading" class="text-center p-5">
        <i class="fas fa-spinner fa-spin fa-2x text-danger"></i>
        <p>Đang tải định lượng...</p>
      </div>

      <table v-else class="table-custom">
        <thead>
          <tr>
            <th style="width: 50px;"></th>
            <th style="width: 60px;">STT</th>
            <th>ĐƠN VỊ TÍNH</th>
            <th>MÔ TẢ</th>
            <th style="width: 100px;">HÀNH ĐỘNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="paginatedData.length === 0">
            <td colspan="5" class="text-center p-5 text-muted">Không tìm thấy dữ liệu.</td>
          </tr>

          <template v-for="(unit, index) in paginatedData" :key="unit.id">

            <tr class="main-row" :class="{ 'is-expanded': expandedRows.includes(unit.id) }" @click="toggleRow(unit.id)">
              <td class="text-center">
                <i class="fas fa-chevron-right arrow-icon"></i>
              </td>
              <td class="text-center">{{ (currentPage - 1) * itemsPerPage + index + 1 }}</td>
              <td class="" style="font-size: 1.05rem;">
                {{ unit.tenDonVi }}
                <span class="badge-count">{{ unit.values ? unit.values.length : 0 }} size</span>
              </td>
              <td class="text-muted">{{ unit.moTa || '---' }}</td>
              <td class="text-center">
                <button class="btn-icon" @click.stop="openUpdateModal(unit)" title="Chỉnh sửa">
                  <i class="fas fa-pen"></i>
                </button>
              </td>
            </tr>

            <tr v-if="expandedRows.includes(unit.id)" class="detail-row">
              <td colspan="5">
                <div class="detail-content">
                  
                  <div class="detail-section">
                    <h6 class="detail-title"><i class="fas fa-ruler-combined"></i> Các giá trị định lượng:</h6>
                    <div class="d-flex flex-wrap gap-2">
                        <span 
                            v-for="v in unit.values" 
                            :key="v.id" 
                            class="status-badge value-badge clickable-badge"
                            :class="{ 'inactive': v.trangThai === 0 }"
                            @click="handleEditValue(v, unit)"
                            title="Click để sửa hoặc tắt trạng thái"
                        >
                            {{ v.giaTri }}
                            <i v-if="v.trangThai === 0" class="fas fa-eye-slash ms-1 small-icon"></i>
                        </span>
                        
                        <span v-if="!unit.values?.length" class="text-muted fst-italic">Chưa có giá trị nào</span>
                    </div>
                    <small class="text-muted fst-italic mt-1 d-block" style="font-size: 0.8rem;">
                        * Click vào giá trị để chỉnh sửa nhanh hoặc ẩn/hiện.
                    </small>
                  </div>

                  <div class="detail-section mt-3">
                    <h6 class="detail-title"><i class="fas fa-tags"></i> Danh mục áp dụng:</h6>
                    <div class="d-flex flex-wrap gap-2">
                      <span v-for="catId in unit.listIdDanhMuc" :key="catId" class="status-badge category-badge">
                        {{ listCategories.find(c => c.id === catId)?.tenDanhMuc }}
                      </span>
                      <span v-if="!unit.listIdDanhMuc?.length" class="text-muted fst-italic">Chưa áp dụng danh mục nào</span>
                    </div>
                  </div>

                </div>
              </td>
            </tr>
          </template>
        </tbody>
      </table>

      <div style="padding-bottom: 30px;" class="pagination">
        <CommonPagination v-model:currentPage="currentPage" v-model:pageSize="itemsPerPage" :total-pages="totalPages"
          :total-elements="totalElements" :current-count="paginatedData.length" @change="handleRefresh" />
      </div>
    </div>

  </div>

  <UnitAddScreen :isOpen="isModalOpen" :categories="listCategories" @close="isModalOpen = false"
    @refresh="handleRefresh" />
  <UnitUpdateScreen :isOpen="isModalUpdateOpen" :unit-item="selectedItem" :categories="listCategories"
    @close="isModalUpdateOpen = false" @refresh="handleRefresh" />
</template>

<style scoped>
@import url("/src/assets/foodManager.css");

.action-group {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.edit-icon {
  color: #FF9800;
  cursor: pointer;
  font-size: 1.1rem;
  transition: transform 0.2s;
}

.edit-icon:hover {
  transform: scale(1.2);
}

.btn-add-only {
  background-color: #8B0000 !important;
  color: white !important;
}

.btn-action-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  border: 1px solid #eee !important;
  transition: all 0.2s ease;
}

.btn-action-icon:hover {
  background-color: #f8f9fa;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.status-badge {
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 500;
}

.status-badge.active {
  background-color: white;
  color: black;
  border: 1px solid lightgray;
}

.table-custom {
  width: 100%;
  border-collapse: collapse;
  /* Quan trọng để không bị hở khe giữa 2 dòng */
}

/* Style cho hàng chính */
.main-row {
  cursor: pointer;
  transition: background-color 0.2s;
  border-bottom: 1px solid #eee;
}

.main-row:hover {
  background-color: #f9f9f9;
}

.main-row.is-expanded {
  background-color: #fff5f5;
  /* Màu nền nhẹ khi mở */
  border-bottom: none;
}

/* Icon mũi tên xoay */
.arrow-icon {
  color: #999;
  transition: transform 0.3s ease;
  font-size: 0.8rem;
}

.main-row.is-expanded .arrow-icon {
  transform: rotate(90deg);
  color: #8B0000;
}

/* Badge đếm số lượng size bên cạnh tên */
.badge-count {
  font-size: 0.75rem;
  background-color: #eee;
  color: #666;
  padding: 2px 6px;
  border-radius: 10px;
  margin-left: 8px;
  font-weight: normal;
}

/* Style cho hàng chi tiết */
.detail-row td {
  background-color: #fafafa;
  border-bottom: 2px solid #e9ecef;
  padding: 0 !important;
  /* Xóa padding mặc định của td để content bung ra */
}

.detail-content {
  padding: 20px 60px;
  /* Thụt vào trong so với lề trái */
  border-left: 4px solid #8B0000;
  /* Đường viền nhấn bên trái */
  animation: fadeIn 0.3s ease-in;
}

.detail-title {
  font-size: 0.9rem;
  font-weight: 700;
  color: #444;
  margin-bottom: 10px;
}

/* Badge Custom */
.status-badge {
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 600;
  display: inline-block;
}

.value-badge {
  background-color: #ffffff;
  color: #2c3e50;
  border: 1px solid #bdc3c7;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.category-badge {
  background-color: #8B0000;
  color: white;
  border: 1px solid #d2e3fc;
}

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  color: #555;
  transition: 0.2s;
}

.btn-icon:hover {
  color: #8B0000;
  transform: scale(1.1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.clickable-badge {
    cursor: pointer;
    transition: all 0.2s ease;
    border: 1px solid #bdc3c7;
    background-color: white;
    color: #2c3e50;
    position: relative;
    padding-right: 12px;
}

.clickable-badge:hover {
    background-color: #8B0000;
    color: white;
    border-color: #8B0000;
    transform: translateY(-2px);
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

/* Style khi trạng thái = 0 (Ngừng hoạt động) */
.clickable-badge.inactive {
    background-color: #f1f3f5;
    color: #adb5bd;
    border-color: #e9ecef;
    text-decoration: line-through; /* Gạch ngang chữ */
}

.clickable-badge.inactive:hover {
    background-color: #e9ecef;
    color: #495057;
    text-decoration: none;
}

.small-icon {
    font-size: 0.75rem;
    margin-left: 5px;
}
</style>