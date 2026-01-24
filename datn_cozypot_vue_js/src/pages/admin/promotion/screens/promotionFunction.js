import axios from 'axios'; // Đảm bảo đã import axios
import promotionService from '@/services/promotionService';

const fetchData = async (filters, pagination) => {
    const params = {
        keyword: filters.keyword,
        trangThai: filters.trangThai,
        ngayBatDau: filters.ngayBatDau,
        ngayKetThuc: filters.ngayKetThuc,
        page: pagination.currentPage - 1,
        size: pagination.pageSize
    };

    const res = await promotionService.search(params);
    return res;
};


export const usePromotionLogic = () => {
    const getStatusDisplay = (status) => {
        return status === 1 
            ? { text: 'Đang hoạt động', class: 'badge bg-success' }
            : { text: 'Ngừng hoạt động', class: 'badge bg-danger' };
    };

    const fetchData = async (filters, pagination) => {
        try {
            const params = {
                keyword: filters.keyword || null,
                status: filters.trangThai,
                // Chú ý: Tên tham số phải khớp chính xác với @RequestParam trong Controller Java
                ngayBatDau: filters.ngayBatDau || null,
                ngayKetThuc: filters.ngayKetThuc || null,
                page: pagination.currentPage - 1,
                size: pagination.pageSize
            };

            // Sử dụng baseURL từ axios config hoặc viết đầy đủ URL
            const response = await axios.get('http://localhost:8080/api/dot-khuyen-mai/search', { params });
            return response.data;
        } catch (error) {
            console.error("Error fetching promotions:", error);
            throw error;
        }
    };
    

    return { getStatusDisplay, fetchData };
};