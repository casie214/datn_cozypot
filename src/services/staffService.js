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
  create: (data) => {
    return axiosClient.post('/nhan-vien/add', data);
  },

  // Cập nhật (Khớp với @PutMapping("/update/{id}"))
  update: (id, data) => {
    return axiosClient.put(`/nhan-vien/update/${id}`, data);
  },

  // Lấy danh sách vai trò cho Dropdown (Khớp với VaiTroController @GetMapping("/active"))
  getActiveRoles: () => {
    return axiosClient.get('/vai-tro/active');
  },

  toggleStatus: (id) => {
    return axiosClient.patch(`/nhan-vien/${id}/toggle-status`);
  },

};

export default staffService;