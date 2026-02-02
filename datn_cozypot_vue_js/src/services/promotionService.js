import axios from 'axios';
import axiosClient from './axiosClient';

const API_URL = 'http://localhost:8080/api/dot-khuyen-mai';

const promotionService = {
    // 1. Tìm kiếm và phân trang
    fetchData: (filters, pagination) => {
        return axiosClient.get(`${API_URL}/search`, {
            params: {
                keyword: filters.keyword || null,
                trangThai: filters.trangThai,
                ngayBatDau: filters.ngayBatDau || null,
                ngayKetThuc: filters.ngayKetThuc || null,
                page: pagination.currentPage - 1,
                size: pagination.pageSize
            }

        }).then(res => res.data);
    },

    // 2. Lấy chi tiết 1 đợt (Để đổ dữ liệu vào modal sửa)
    getById: (id) => axiosClient.get(`${API_URL}/${id}`).then(res => res.data),
    getActiveMonAns: () => axiosClient.get(`${API_MON_AN}/active`).then(res => res.data),
    // 3. Thêm mới
    create: (data) => axiosClient.post(`${API_URL}/create`, data).then(res => res.data),

    // 4. Cập nhật
    // Trong promotionService.js
    update: (id, data) => {
        // Đảm bảo data gửi đi có cấu trúc phẳng hoặc đúng như API yêu cầu
        return axiosClient.put(`${API_URL}/update/${id}`, data).then(res => res.data);
    },
    // 5. Đổi trạng thái (Toggle)
    // Lưu ý: Thường nên tạo 1 API riêng ở Backend: @PatchMapping("/{id}/status")
    // Nếu chưa có, ta dùng tạm update nhưng phải gửi kèm dữ liệu cũ để tránh mất data
    toggleStatus: async (id, currentStatus) => {
        const newStatus = currentStatus === 1 ? 0 : 1;

        // Cách tốt nhất: Lấy data cũ về rồi mới update trạng thái mới
        const currentData = await axiosClient.get(`${API_URL}/${id}`).then(res => res.data);
        currentData.trangThai = newStatus;

        return axiosClient.put(`${API_URL}/update/${id}`, currentData);
    }
};

export default promotionService;