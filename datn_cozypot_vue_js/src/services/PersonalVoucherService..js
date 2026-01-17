import axios from 'axios';

const API_URL = '/api/phieu-giam-gia-ca-nhan'; // Phải có dòng khai báo này
export const PersonalVoucherService = {
    // Thêm hàm search vào đây
    search: async (filters, page, size) => {
        const response = await axios.get(`${API_URL}/search`, {
            params: {
                keyword: filters.keyword,
                status: filters.status,
                page: page,
                size: size
            }
        });
        return response.data;
    },

    create: async (data) => {
        const response = await axios.post(`${API_URL}/create`, data);
        return response.data;
    },
    
    update: async (id, data) => {
        const response = await axios.put(`${API_URL}/update/${id}`, data);
        return response.data;
    }
};