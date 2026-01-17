import axios from 'axios';

const API_URL = "http://localhost:8080/api/phieu-giam-gia";

export const PromotionVoucherService = {
    // 1. Chức năng Tìm kiếm & Phân trang
    search: async (filters, page = 0, size = 5) => {
        try {
            // Chặn lỗi PointerEvent: Nếu page không phải là số, mặc định về 0
            const pageNum = typeof page === 'number' ? page : 0;

            const response = await axios.get(`${API_URL}/search`, { 
                params: {
                    keyword: filters.keyword || null,
                    status: filters.status !== '' ? filters.status : null,
                    page: pageNum,
                    size: size
                }
            });
            return response.data; // Trả về đối tượng Page (content, totalPages,...)
        } catch (error) {
            console.error("Lỗi khi tìm kiếm phiếu giảm giá:", error);
            throw error;
        }
    },

    // 2. Chức năng Thêm mới
    create: async (data) => {
        try {
            const response = await axios.post(`${API_URL}/create`, data);
            return response.data;
        } catch (error) {
            console.error("Lỗi khi tạo mới phiếu giảm giá:", error);
            throw error;
        }
    },

    // 3. Chức năng Cập nhật
    update: async (id, data) => {
        try {
            const response = await axios.put(`${API_URL}/update/${id}`, data);
            return response.data;
        } catch (error) {
            console.error("Lỗi khi cập nhật phiếu giảm giá:", error);
            throw error;
        }
    },

    // 4. Lấy tất cả (Nếu cần dùng cho các mục đích khác)
    getAll: async () => {
        try {
            const response = await axios.get(`${API_URL}/all`);
            return response.data;
        } catch (error) {
            console.error("Lỗi khi lấy danh sách phiếu giảm giá:", error);
            throw error;
        }
    }
};