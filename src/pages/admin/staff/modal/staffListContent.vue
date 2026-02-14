<template>
  <div class="flex-grow-1 staff-manager-wrapper" style="padding: 25px; background: #ffffff;min-height: 100vh;">
    <div class="mb-3">
      <h2 class="title-page-cozy">Quản lý nhân viên</h2>
    </div>

    <div class="filter-card-premium mb-4">
      <div class="filter-header-simple">
        <i class="fas fa-filter me-2"></i>Bộ lọc tìm kiếm
      </div>
      <div class="row g-3 p-3 align-items-end">
        <div class="col-md-5">
          <label class="filter-label">Từ khóa</label>
          <input v-model="filters.keyword" class="form-control custom-input" placeholder="Tên, mã, email..."
            @input="onSearchInput">
        </div>
        <div class="col-md-5">
          <label class="filter-label">Trạng thái</label>
          <select v-model="filters.trangThai" class="form-select custom-input" @change="handleSearch">
            <option :value="null">Tất cả</option>
            <option :value="1">Đang làm việc</option>
            <option :value="2">Ngừng hoạt động</option>
          </select>
        </div>
        <!-- <div class="col-md-3">
          <label class="filter-label">Từ ngày</label>
          <input type="date" v-model="filters.tuNgay" class="form-control custom-input" @change="handleSearch">
        </div> -->
        <div class="col-md-2">
          <button class="btn-red-dark w-100 py-2" @click="handleSearch">Tìm kiếm</button>
        </div>
      </div>
    </div>

    <div class="shadow-sm mt-4"
      style="background: #ffffff; border-radius: 12px; border: 1px solid #eef2f6; overflow: hidden;">

      <div
        style="padding: 16px 24px; border-bottom: 1px solid #f1f1f1; background-color: #ffffff; display: flex; justify-content: space-between; align-items: center;">

        <h4 style="font-size: 1.25rem; font-weight: 700; color: #800000; margin: 0; font-family: sans-serif;">
          Danh sách nhân viên
        </h4>

        <div style="display: flex; gap: 8px;">
          <button @click="openModalAdd" title="Thêm nhân viên"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-plus"></i>
          </button>

          <!-- <button @click="triggerImport" title="Import dữ liệu"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-cloud-upload-alt"></i>
          </button> -->

          <input type="file" ref="fileInput" @change="handleImportExcel" style="display: none" accept=".xlsx, .xls">
          <button @click="printToPDF" title="In bản PDF"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-print"></i>
          </button>

          <button @click="exportToExcel" title="Xuất Excel"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-file-excel"></i>
          </button>

          <button
            @click="handleSearch(true); Swal.fire({ icon: 'success', title: 'Đã tải lại dữ liệu', timer: 1500, showConfirmButton: false });"
            title="Làm mới dữ liệu"
            style="width: 38px; height: 38px; background: #ffffff; border: 1.5px solid #800000; border-radius: 8px; color: #800000; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: 0.3s;">
            <i class="fas fa-sync-alt"></i>
          </button>
        </div>
      </div>

      <div class="table-responsive p-3">
        <table class="table mb-0 custom-table table-hover align-middle">
          <thead>
            <tr style="background-color: #800000;">
              <th class="text-center" style="width: 40px; border: none;">
                <input type="checkbox" class="form-check-input" @change="toggleSelectAll"
                  :checked="selectedIds.length === listNhanVien.length && listNhanVien.length > 0">
              </th>
              <th class="text-center" style="color: white; border: none;">STT</th>
              <th style="color: white; border: none;">Nhân viên</th>
              <th class="text-center" style="color: white; border: none;">Chức vụ</th>
              <th class="text-center" style="color: white; border: none;">Giới tính</th>
              <th style="color: white; border: none;">Liên hệ</th>
              <th style="color: white; border: none;">Địa chỉ</th>
              <th class="text-center" style="color: white; border: none;">Trạng thái</th>
              <th class="text-center" style="color: white; border: none;">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(nv, index) in listNhanVien" :key="nv.id">
              <td class="text-center">
                <input type="checkbox" class="form-check-input custom-checkbox" :value="nv.id" v-model="selectedIds">
              </td>
              <td class="text-center text-muted font-monospace">
                {{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}
              </td>

              <td>
                <div class="fw-bold text-dark mb-0">{{ nv.hoTenNhanVien }}</div>
              </td>

              <td class="text-center">
                <span class="small fw-semibold" style="color: #555;">
                  {{ nv.idVaiTro === 1 ? 'Quản lý' : 'Nhân viên' }}
                </span>
              </td>

              <td class="text-center">{{ nv.gioiTinh ? 'Nam' : 'Nữ' }}</td>

              <td>
                <div class="contact-box">
                  <div class="small mb-1">
                    <i class="far fa-envelope me-1" style="color: #800000;"></i>{{ nv.email }}
                  </div>
                  <div class="text-danger small">
                    <i class="fas fa-phone-alt me-1"></i>{{ nv.sdtNhanVien }}
                  </div>
                </div>
              </td>

              <td>
                <div class="text-muted small text-wrap" style="max-width: 200px; line-height: 1.4;">
                  {{ nv.diaChi }}
                </div>
              </td>

              <td class="text-center">
                <span :class="['badge-status', nv.trangThaiLamViec === 1 ? 'status-active' : 'status-locked']">
                  {{ nv.trangThaiLamViec === 1 ? 'Đang hoạt động' : 'Ngừng việc' }}
                </span>
              </td>

              <td class="text-center" style="vertical-align: middle;">
                <div class="d-flex justify-content-center align-items-center gap-3">
                  <button class="btn btn-link p-0 action-edit-v2" :class="{ 'is-locked': nv.trangThaiLamViec !== 1 }"
                    @click="handleEdit(nv)" :title="nv.trangThaiLamViec !== 1 ? 'Tài khoản bị khóa' : 'Chỉnh sửa'"
                    style="text-decoration: none; transition: 0.3s;">
                    <i class="fas fa-pen" :style="{ color: nv.trangThaiLamViec !== 1 ? '#ccc' : '#444' }"></i>
                  </button>

                  <div class="form-check form-switch" style="margin-bottom: 0; min-height: auto;">
                    <input class="form-check-input cz-switch shadow-none" type="checkbox" role="switch"
                      style="cursor: pointer; width: 2.5em; height: 1.25em;" :checked="nv.trangThaiLamViec === 1"
                      @click.prevent="onToggleStatus(nv)">
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>



    <CommonPagination v-model:currentPage="pagination.currentPage" v-model:pageSize="pagination.pageSize"
      :totalPages="pagination.totalPages" :totalElements="pagination.totalElements" :currentCount="listNhanVien.length"
      @change="handleSearch" />

  </div>
</template>

<script setup>

import { ref, onMounted, reactive, watch, nextTick } from 'vue'; // Đã thêm watch
import { useStaffLogic } from '../screens/staffFunction.js';

import dayjs from 'dayjs';
import { useRouter } from 'vue-router';
import '../staffStyle.css';
import Swal from 'sweetalert2';
import staffService from '@/services/staffService.js';
import CommonPagination from '@/components/commonPagination.vue';

import logoCozyPot from '../img/logo_upscaled.jpg';
const handlePrintPdf = () => {
  if (selectedIds.value.length === 0) {
    // Bạn có thể dùng toast hoặc alert thông báo
    alert("Vui lòng tích chọn ít nhất một nhân viên để in báo cáo!");
    return;
  }

  // 1. Lấy URL trực tiếp từ Service
  const url = staffService.getPrintPdfUrl(selectedIds.value);

  // 2. Mở tab mới
  // Trình duyệt sẽ tự động gọi API, nhận về mảng byte và hiển thị trình xem PDF của nó
  if (url) {
    window.open(url, '_blank');
  }
};
// --- STATE QUẢN LÝ ---
const listNhanVien = ref([]);

const filters = reactive({
  keyword: '',
  trangThai: null,
  tuNgay: ''
});
const { getStatusDisplay, fetchData, toggleStaffStatus } = useStaffLogic();
const router = useRouter();
const pagination = reactive({
  currentPage: 1,
  pageSize: 8,
  totalPages: 0,
  totalElements: 0
});

const inputPage = ref(1); // Biến hỗ trợ ô nhập số trang

const isDetailModalOpen = ref(false);

// --- LOGIC XỬ LÝ DỮ LIỆU ---

// Đồng bộ ô nhập số mỗi khi trang hiện tại thay đổi (do bấm nút mũi tên)
watch(() => pagination.currentPage, (newVal) => {
  inputPage.value = newVal;
});

const formatDate = (date) => {
  if (!date) return '---';
  return dayjs(date).format('DD/MM/YYYY');
};
const handleSearch = async (showToast = false) => {
  try {
    const data = await fetchData(filters, pagination);

    listNhanVien.value = data.content || [];
    pagination.totalPages = data.totalPages || 0;
    pagination.totalElements = data.totalElements || 0;



  } catch (error) {
    console.error("Lỗi khi load danh sách:", error);
    listNhanVien.value = [];

    Swal.fire('Lỗi!', 'Không thể tải dữ liệu.', 'error');
  }
};

const onSearchInput = () => {
  clearTimeout(window.debounceTimer);
  window.debounceTimer = setTimeout(() => {
    pagination.currentPage = 1;
    handleSearch();
  }, 500);
};

// Hàm nhảy trang khi nhập số và nhấn Enter
const jumpToPage = () => {
  let p = parseInt(inputPage.value);
  if (p >= 1 && p <= pagination.totalPages) {
    goToPage(p);
  } else {
    // Nếu nhập sai, trả về số trang hiện tại của hệ thống
    inputPage.value = pagination.currentPage;
  }
};


const goToPage = (page) => {
  pagination.currentPage = page;
  handleSearch();
};


const onToggleStatus = async (nv) => {
  await toggleStaffStatus(nv, handleSearch);
};

// --- LOGIC MODAL ---

const openModalAdd = () => {
  router.push("/admin/staff/form");
};

const openModalEdit = (id) => {
  console.log("ID nhận được:", id);
  console.log("Dữ liệu dòng này:", listNhanVien.value.find(x => x.id === id || x.id === id));
  router.push(`/admin/staff/form/${id}`);
};

const handleEdit = (nv) => {
  // Kiểm tra trạng thái: Nếu không phải 1 (Đang hoạt động) thì chặn
  if (nv.trangThaiLamViec !== 1) {
    // Hiển thị thông báo (Dùng Toast bạn đã cài)
    toast.warning("Tài khoản bị khóa không thể thực hiện chức năng này", {
      position: "top-center",
      timeout: 2500,
      closeOnClick: true,
      pauseOnHover: true,
      draggable: true,
      // Style cho toast tiệp màu đỏ rượu
      toastClassName: "custom-toast-red",
    });
    return;
  }

  // Nếu hợp lệ, thực hiện chuyển sang trang sửa (Dùng ID đúng từ API)
  router.push(`/admin/staff/form/${nv.id}`);
};


const openModalView = (id) => {
  // detailStaffId.value = id;
  // isDetailModalOpen.value = true;
  router.push(`/admin/staff/view/${id}`);
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
};
// 1. Mảng lưu trữ các ID được chọn
const selectedIds = ref([]);

// 2. Hàm xử lý tích chọn tất cả (Ô dòng đỏ)
const toggleSelectAll = (event) => {
  if (event.target.checked) {
    // Nếu tích ô đỏ: lấy toàn bộ .id gán vào mảng chọn
    selectedIds.value = listNhanVien.value.map(nv => nv.id);
  } else {
    // Nếu bỏ tích ô đỏ: xóa sạch mảng
    selectedIds.value = [];
  }
};

// 3. Reset mảng chọn khi người dùng chuyển trang hoặc tìm kiếm mới
watch(() => listNhanVien.value, () => {
  selectedIds.value = [];
});

// Hàm Xuất Excel: CHỈ TẢI NHỮNG DÒNG ĐÃ TÍCH
const exportToExcel = async () => {
  // BƯỚC 1: Kiểm tra xem người dùng đã tích chọn chưa
  if (selectedIds.value.length === 0) {
    Swal.fire({
      title: 'Thông báo',
      text: 'Bạn chưa chọn nhân viên nào. Vui lòng tích chọn nhân viên để xuất file!',
      icon: 'warning',
      confirmButtonColor: '#800000'
    });
    return; // Dừng lại, không thực hiện tải
  }

  // BƯỚC 2: Xác nhận trước khi tải
  const result = await Swal.fire({
    title: 'Xác nhận xuất file?',
    text: `Hệ thống sẽ tải về danh sách của ${selectedIds.value.length} nhân viên bạn đã chọn.`,
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#800000',
    confirmButtonText: 'Tải xuống ngay',
    cancelButtonText: 'Để sau'
  });

  if (result.isConfirmed) {
    try {
      Swal.showLoading();

      // BƯỚC 3: Gửi danh sách ID lên Server
      // Lưu ý: Tùy vào API của bạn, có thể cần gửi { listId: selectedIds.value } 
      // hoặc { listId: selectedIds.value.toString() }
      const params = {
        listId: selectedIds.value.join(',') // Chuyển [1, 2] thành "1,2"
      };

      const response = await staffService.exportStaffExcel(params);

      // BƯỚC 4: Xử lý file trả về
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `NhanVien_ChonLoc_${dayjs().format('DDMMYYYY')}.xlsx`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      Swal.fire({ title: 'Thành công!', icon: 'success', timer: 1500, showConfirmButton: false });
    } catch (error) {
      console.error("Lỗi xuất file:", error);
      Swal.fire('Lỗi!', 'Không thể tải file. Có thể Server chưa hỗ trợ lọc theo ID.', 'error');
    }
  }
};

// Thêm vào ngay dưới các biến ref hiện có
const fileInput = ref(null);

// Hàm mở cửa sổ chọn file
const triggerImport = () => {
  fileInput.value.click();
};

// Hàm xử lý file sau khi người dùng chọn
const handleImportExcel = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  // Xác nhận trước khi Import
  const result = await Swal.fire({
    title: 'Xác nhận Import?',
    text: "Dữ liệu từ file Excel sẽ được thêm vào hệ thống.",
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#800000',
    confirmButtonText: 'Đồng ý',
    cancelButtonText: 'Hủy'
  });

  if (result.isConfirmed) {
    try {
      Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

      await staffService.importExcel(file);

      Swal.fire('Thành công!', 'Đã nhập dữ liệu nhân viên thành công.', 'success');
      handleSearch(true); // Load lại bảng để thấy dữ liệu mới
    } catch (error) {
      console.error("Lỗi Import:", error);
      Swal.fire('Lỗi!', 'Không thể nhập dữ liệu. Vui lòng kiểm tra lại định dạng file!', 'error');
    } finally {
      event.target.value = ''; // Reset để có thể chọn lại cùng 1 file
    }
  } else {
    event.target.value = ''; // Reset nếu người dùng hủy
  }
};


const printToPDF = () => {
  if (selectedIds.value.length === 0) {
    Swal.fire({ title: 'Thông báo', text: 'Vui lòng chọn nhân viên!', icon: 'warning' });
    return;
  }

  const selectedStaffData = listNhanVien.value.filter(nv =>
    selectedIds.value.includes(nv.id)
  );

  const printWindow = window.open('', '_blank');

  // Bạn nên thay bằng link ảnh logo thực tế của CozyPot

  printWindow.document.write(`
    <html>
      <head>
        <title>Báo cáo nhân sự - CozyPot</title>
        <style>
          @import url('https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&display=swap');
          
          @page { size: A4 portrait; margin: 0; }

          body { 
            font-family: 'Montserrat', sans-serif; 
            color: #2d3436; 
            margin: 0; 
            background-color: #525659; /* Màu nền trình xem PDF */
            display: flex;
            flex-direction: column;
            align-items: center;
          }

          /* THANH TOOLBAR PDF MÔ PHỎNG */
          .pdf-header-nav {
            width: 100%;
            background: #323639;
            color: white;
            padding: 10px 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            position: sticky;
            top: 0;
            box-sizing: border-box;
            z-index: 100;
          }

          /* TRANG GIẤY A4 */
          .a4-page {
            background: white;
            width: 210mm;
            min-height: 297mm;
            margin: 30px 0;
            padding: 20mm;
            box-sizing: border-box;
            box-shadow: 0 0 15px rgba(0,0,0,0.5);
            position: relative;
          }

          /* HEADER GIỐNG ẢNH MẪU */
          .brand-header {
            display: flex;
            justify-content: space-between;
            border-bottom: 2px solid #800000;
            padding-bottom: 20px;
            margin-bottom: 30px;
          }

          .brand-left { display: flex; align-items: center; gap: 15px; }
          .brand-left img { width: 70px; height: 70px; object-fit: contain; }
          .brand-name h1 { margin: 0; color: #800000; font-size: 24px; letter-spacing: 1px; }
          .brand-name p { margin: 0; font-size: 10px; color: #636e72; font-weight: 600; }

          .report-meta-title { text-align: right; }
          .report-meta-title h2 { margin: 0; font-size: 20px; color: #2d3436; }
          .report-meta-title p { margin: 0; font-size: 11px; color: #b78a28; font-weight: bold; }

          /* THÔNG TIN CHI TIẾT */
          .info-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            margin-bottom: 40px;
            font-size: 13px;
            line-height: 1.8;
          }
          .info-label { font-weight: bold; color: #800000; }

          /* TABLE STYLES THEO ẢNH MẪU */
          .cozypot-table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 50px;
          }
          .cozypot-table th {
            border-top: 1px solid #ddd;
            border-bottom: 2px solid #800000;
            padding: 12px 8px;
            font-size: 11px;
            text-transform: uppercase;
            color: #2d3436;
          }
          .cozypot-table td {
            padding: 15px 8px;
            border-bottom: 1px solid #f1f1f1;
            font-size: 13px;
            text-align: center;
          }
          .col-name { text-align: left !important; color: #800000; font-weight: bold; }
          .col-id { font-weight: bold; color: #2d3436; }

          /* BADGE TRẠNG THÁI */
          .status-badge {
            background: #e6f4ea;
            color: #1e7e34;
            padding: 6px 15px;
            border-radius: 20px;
            font-size: 10px;
            font-weight: bold;
            display: inline-block;
          }

          /* FOOTER CHỮ KÝ */
          .signature-section {
            display: grid;
            grid-template-columns: 1fr 1fr;
            margin-top: 80px;
            text-align: center;
          }
          .sig-title { font-weight: bold; font-size: 14px; margin-bottom: 5px; }
          .sig-sub { font-size: 11px; color: #636e72; font-style: italic; }
          .sig-name { margin-top: 80px; font-weight: bold; color: #800000; border-top: 1px solid #eee; display: inline-block; padding-top: 5px; min-width: 180px; }

          @media print {
            body { background: white; }
            .pdf-header-nav, .no-print { display: none !important; }
            .a4-page { margin: 0; box-shadow: none; width: 100%; }
          }
        </style>
      </head>
      <body>
        <div class="pdf-header-nav">
          <div>Bao_cao_nhan_vien_CozyPot.pdf</div>
          <div>
            <button onclick="window.print()" style="padding: 5px 15px; cursor: pointer; background: #800000; color: white; border: none; border-radius: 4px;">🖨️ In báo cáo</button>
          </div>
        </div>

        <div class="a4-page">
          <div class="brand-header">
            <div class="brand-left">
              <img src="${logoCozyPot}" alt="CozyPot Logo">
              <div class="brand-name">
                <h1>COZYPOT</h1>
                <p>PREMIUM HOTPOT SYSTEM</p>
              </div>
            </div>
            <div class="report-meta-title">
              <h2>DANH SÁCH NHÂN VIÊN</h2>
              <p>INTERNAL REPORT / #2026-STF</p>
            </div>
          </div>

          <div class="info-grid">
            <div>
              <div><span class="info-label">Đơn vị:</span> Công ty TNHH CozyPot Việt Nam</div>
              <div><span class="info-label">Địa chỉ:</span> Quận 1, TP. Hồ Chí Minh</div>
              <div><span class="info-label">Phòng ban:</span> Khối vận hành nhà hàng</div>
            </div>
            <div style="text-align: right;">
              <div><span class="info-label">Ngày xuất báo cáo:</span> ${new Date().toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })}</div>
              <div><span class="info-label">Người phê duyệt:</span> Tổng Giám Đốc</div>
            </div>
          </div>

          <table class="cozypot-table">
            <thead>
              <tr>
                <th>STT</th>
                <th>Mã số</th>
                <th style="text-align:left">Họ tên nhân viên</th>
                <th>Số điện thoại</th>
                <th>Vị trí</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
  ${selectedStaffData.map((nv, index) => `
    <tr>
      <td>${(index + 1).toString().padStart(2, '0')}</td>
      
      <td class="col-id">${nv.maNhanVien || 'N/A'}</td>
      
      <td class="col-name">${nv.hoTenNhanVien ? nv.hoTenNhanVien.toUpperCase() : ''}</td>
      
      <td>${nv.sdtNhanVien || ''}</td>
      
      <td>${nv.tenVaiTro || 'Nhân viên'}</td>
      
      <td>
        <span class="status-badge">
          ${nv.trangThaiLamViec === 1 ? 'ĐANG LÀM VIỆC' : 'NGỪNG HOẠT ĐỘNG'}
        </span>
      </td>
    </tr>
  `).join('')}
</tbody>
          </table>

          <div class="signature-section">
            <div>
              <div class="sig-title">NGƯỜI LẬP BIỂU</div>
              <div class="sig-sub">(Ký và ghi rõ họ tên)</div>
              <div class="sig-name">Admin CozyPot</div>
            </div>
            <div>
              <div class="sig-title">ĐẠI DIỆN PHÁP LUẬT</div>
              <div class="sig-sub">(Ký tên và đóng dấu)</div>
              <div class="sig-name">Nguyễn Văn Chủ Tịch</div>
            </div>
          </div>
        </div>
      </body>
    </html>
  `);

  printWindow.document.close();

  // Đợi font và ảnh load xong mới mở hộp thoại in
  setTimeout(() => {
    printWindow.focus();
    printWindow.print();
  }, 800);
};

onMounted(handleSearch);
</script>