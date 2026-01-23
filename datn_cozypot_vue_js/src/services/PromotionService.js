import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/dot-khuyen-mai';

export const PromotionService = {

    async search(filters) {
        // Hiện tại BE chưa có filter -> gọi get-all
        const res = await axios.get(`${API_BASE_URL}/get-all`);
        return res.data;
    },

    // THÊM HÀM NÀY VÀO
    async create(data) {
        return await axios.post(`${API_BASE_URL}/create`, data);
    },

    async update(id, data) {
        return await axios.put(`${API_BASE_URL}/update/${id}`, data);
    },
 async search(filters, page = 0, size = 5) {
    // Đảm bảo page luôn là số
    const pageNumber = typeof page === 'number' ? page : 0;
    
    const res = await axios.get(`${API_BASE_URL}/search`, {
        params: {
            keyword: filters.keyword || null,
            status: filters.status !== '' ? filters.status : null,
            type: filters.type !== '' ? filters.type : null,
            page: pageNumber,
            size: size
        }
    });
    return res.data;
}

    
};