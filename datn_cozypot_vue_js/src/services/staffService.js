import axiosClient from './axiosClient';

const staffService = {
  // 1. Lấy danh sách + Tìm kiếm
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

  // 2. Xem chi tiết
  getDetail: (id) => {
    return axiosClient.get(`/nhan-vien/${id}`);
  },

  // 3. Thêm mới (Gửi FormData)
  create: (formData) => {
    return axiosClient.post('/nhan-vien/add', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // 4. Cập nhật (Gửi FormData)
  update: (id, formData) => {
    return axiosClient.put(`/nhan-vien/update/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },

  // 5. Lấy danh sách vai trò active
  getActiveRoles: () => {
    return axiosClient.get('/vai-tro/active');
  },

  // 6. Đổi trạng thái
  toggleStatus: (id) => {
    return axiosClient.patch(`/nhan-vien/${id}/toggle-status`);
  },

  // 7. Check trùng dữ liệu
  // staffService.js
  checkDuplicate: (params) => {
    return axiosClient.get('/nhan-vien/check-duplicate', {
      params: params // Đảm bảo params ở đây là object {type, value, excludeId}
    });
  },

  // 8. Xuất file Excel (Có hỗ trợ xuất theo listId đã chọn)
  exportStaffExcel: (filters) => {
    return axiosClient.get('/nhan-vien/export', {
      params: {
        keyword: filters.keyword || null,
        trangThai: filters.trangThai || null,
        tuNgay: filters.tuNgay || null,
        listId: filters.listId || null
      },
      responseType: 'blob',
    });
  },


  getPrintPdfUrl: (ids) => {
    if (!ids || ids.length === 0) return null;
    const baseUrl = axiosClient.defaults.baseURL || '';
    const queryString = ids.map(id => `ids=${id}`).join('&');

    return `${baseUrl}/nhan-vien/print-pdf?${queryString}`;
  },
// 9. Quét mã QR từ file ảnh (Hỗ trợ nhập liệu CCCD)
  scanQR: (file) => {
    const data = new FormData();
    data.append('file', file);
    return axiosClient.post('/nhan-vien/scan-qr', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },


};
export default staffService;