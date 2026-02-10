import axiosClient from './axiosClient';

const clientService = {
  getAll: (params) => {
    return axiosClient.get('/khach-hang', {
      params: {
        keyword: params.keyword || null,
        trangThai: params.trangThai !== undefined ? params.trangThai : null,
        tuNgay: params.tuNgay || null,
        page: params.page || 0,
        size: params.size || 10
      }
    });
  },

  
  getDetail: (id) => {
    return axiosClient.get(`/khach-hang/${id}`);
  },

<<<<<<< HEAD
  // SỬA: Thêm config headers để axios hiểu là gửi Form Data có kèm file
=======
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
  create: (formData) => {
    return axiosClient.post('/khach-hang/add', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

<<<<<<< HEAD
  // SỬA: Tương tự cho update
=======
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
  update: (id, formData) => {
    return axiosClient.put(`/khach-hang/update/${id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },

  toggleStatus: (id) => {
    return axiosClient.patch(`/khach-hang/${id}/toggle-status`);
  },

  // BỔ SUNG: Hàm kiểm tra trùng dữ liệu realtime
  checkDuplicate: (type, value, excludeId = null) => {
    return axiosClient.get('/khach-hang/check-duplicate', {
      params: { type, value, excludeId }
    });
  },
  exportExcel: (params) => {
    return axiosClient.get('/khach-hang/export-excel', {
      params: {
        keyword: params.keyword || null,
        trangThai: params.trangThai !== undefined ? params.trangThai : null,
        tuNgay: params.tuNgay || null
      },
      responseType: 'blob' // Rất quan trọng: Để nhận dữ liệu nhị phân của file
    });
  }
};

export default clientService;