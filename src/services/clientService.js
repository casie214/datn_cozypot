import axiosClient from './axiosClient';

const clientService = {
  getAll: (params) => {
    return axiosClient.get('/khach-hang', {
      params: {
        keyword: params.keyword || null,
        trangThai: params.trangThai !== null ? params.trangThai : null,
        tuNgay: params.tuNgay || null,
        page: params.page || 0,
        size: params.size || 10
      }
    });
  },

  getDetail: (id) => {
    return axiosClient.get(`/khach-hang/${id}`);
  },

  create: (data) => {
    return axiosClient.post('/khach-hang/add', data);
  },

  update: (id, data) => {
    return axiosClient.put(`/khach-hang/update/${id}`, data);
  },

  toggleStatus: (id) => {
    return axiosClient.patch(`/khach-hang/${id}/toggle-status`); 
  }
};

export default clientService;