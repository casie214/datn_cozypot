import axiosClient from './axiosClient';

const staffService = {
  // Lấy danh sách + Tìm kiếm (Khớp với NhanVienController @GetMapping)
  getAll: (params) => {
    return axiosClient.get('/nhan-vien', {
      params: {
        keyword: params.keyword || null,
        trangThai: params.trangThai || null,
        tuNgay: params.tuNgay || null,
        page: params.page || 0,
        size: params.size || 10
      }
    });
  },

  // Xem chi tiết (Khớp với @GetMapping("/{id}"))
  getDetail: (id) => {
    return axiosClient.get(`/nhan-vien/${id}`);
  },

  // Thêm mới (Khớp với @PostMapping("/add"))
  // SỬA: Thêm mới (Gửi FormData)
  create: (formData) => {
    return axiosClient.post('/nhan-vien/add', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // SỬA: Cập nhật (Gửi FormData)
  update: (id, formData) => {
    return axiosClient.put(`/nhan-vien/update/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // Lấy danh sách vai trò cho Dropdown (Khớp với VaiTroController @GetMapping("/active"))
  getActiveRoles: () => {
    return axiosClient.get('/vai-tro/active');
  },

  toggleStatus: (id) => {
    return axiosClient.patch(`/nhan-vien/${id}/toggle-status`);
  },
  // staffService.js
<<<<<<< HEAD
  checkDuplicate(type, value, excludeId = null) {
  return axiosClient.get("/nhan-vien/check-duplicate", {
    params: {
      type: type,
      value: value,
      excludeId: excludeId
    }
  });
},




=======
  checkDuplicate: (params) => {
    // params gồm { type: 'email', value: 'abc@...', excludeId: 1 }
    return axiosClient.get('/nhan-vien/check-duplicate', { params });
  },
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77

  exportStaffExcel(filters) {
    return axiosClient.get('/nhan-vien/export', { // Bỏ /api nếu axiosClient đã có baseURL
      params: {
        keyword: filters.keyword || null,
        trangThai: filters.trangThai || null,
        tuNgay: filters.tuNgay || null
      },
      responseType: 'blob', // BẮT BUỘC
    });
  }
};

export default staffService;