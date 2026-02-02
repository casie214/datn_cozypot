import axios from 'axios';
import axiosClient from './axiosClient';

// Đổi lại cho khớp với Controller của bạn
const API_URL = "http://localhost:8080/api/phieu-giam-gia";

const voucherService = {
    // 1. Lấy danh sách
    // services/voucherService.js
    fetchData: (filters, pagination) => {
        // Tạo object params sạch (chỉ lấy các giá trị có dữ liệu)
        const params = {};
        if (filters.keyword) params.keyword = filters.keyword;
        if (filters.doiTuong !== null) params.doiTuong = filters.doiTuong;
        if (filters.loaiGiamGia !== null) params.loaiGiamGia = filters.loaiGiamGia;
        if (filters.trangThai !== null) params.trangThai = filters.trangThai;
        if (filters.ngayBatDau) params.ngayBatDau = filters.ngayBatDau;
        if (filters.ngayKetThuc) params.ngayKetThuc = filters.ngayKetThuc;

        params.page = pagination.currentPage - 1;
        params.size = pagination.pageSize;

        return axiosClient.get("http://localhost:8080/api/phieu-giam-gia/search", { params })
                .then(res => res.data);
    },


    // 2. Lấy chi tiết 1 phiếu
    getById: (id) => axiosClient.get(`${API_URL}/${id}`).then(res => res.data),

    // 3. Thêm mới
    // ĐỔI: Bỏ '/create', sử dụng phương thức POST tới URL gốc
    create: (data) => axiosClient.post(`${API_URL}`, data).then(res => res.data),

    // 4. Cập nhật
    // ĐỔI: Bỏ '/update/', Controller của bạn là @PutMapping("/{id}")
    update: (id, data) => {
        return axiosClient.put(`${API_URL}/${id}`, data).then(res => res.data);
    },

    // 5. Đổi trạng thái (Toggle)
    toggleStatus: async (id, currentStatus) => {
        const newStatus = currentStatus === 1 ? 0 : 1;
        const currentData = await axiosClient.get(`${API_URL}/${id}`).then(res => res.data);

        // Tạo DTO tạm thời để gửi về update (hoặc gửi nguyên object nếu Backend nhận)
        currentData.trangThai = newStatus;
        return axiosClient.put(`${API_URL}/${id}`, currentData).then(res => res.data);
    }
};

export default voucherService;