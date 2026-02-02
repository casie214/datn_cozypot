
// Do axiosClient đã có baseURL là 'http://localhost:8080/api'

import axiosClient from "@/services/axiosClient";

// Nên ta chỉ cần khai báo phần đuôi
const PREFIX = "/hoa-don-thanh-toan";

export const BeGetAllHoaDon = async (page = 0, size = 5) => {
    try {
        // Sử dụng axiosClient giúp tự động xử lý query params
        const response = await axiosClient.get(`${PREFIX}/get-all`, {
            params: { 
                page, 
                size 
            }
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const BeGetHoaDonById = async (id) => {
    try {
        const response = await axiosClient.get(`${PREFIX}/get-by-id/${id}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const BeSearchHoaDon = async (key, trangThai, trangThaiHoanTien, tuNgay, denNgay, page = 0, size = 5) => {
    try {
        const params = { page, size };

        // Kiểm tra hợp lệ (giữ logic của HEAD: bỏ qua null, undefined và chuỗi rỗng "")
        // Lưu ý: Không dùng if(trangThai) vì nếu trạng thái = 0 sẽ bị coi là false
        if (key !== null && key !== undefined && key !== "") {
            params.key = key;
        }
        if (trangThai !== null && trangThai !== undefined && trangThai !== "") {
            params.trangThai = trangThai;
        }
        if (trangThaiHoanTien !== null && trangThaiHoanTien !== undefined && trangThaiHoanTien !== "") {
            params.trangThaiHoanTien = trangThaiHoanTien;
        }
        if (tuNgay) params.tuNgay = tuNgay;
        if (denNgay) params.denNgay = denNgay;

        const response = await axiosClient.get(`${PREFIX}/search`, { params });
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const BeGetChiTietHoaDon = async (idHoaDon) => {
    try {
        const response = await axiosClient.get(`${PREFIX}/chi-tiet-hoa-don/${idHoaDon}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const BeGetChiTietSetLau = async (idSetLau) => {
    try {
        const response = await axiosClient.get(`${PREFIX}/chi-tiet-hoa-don/chi-tiet-set-lau/${idSetLau}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const BeXacNhanThanhToan = async (payload) => {
    try {
        // Axios tự động stringify body
        const response = await axiosClient.put(`${PREFIX}/xac-nhan-thanh-toan`, payload);
        return true; // Hoặc return response.data nếu backend có trả về gì đó
    } catch (error) {
        throw error;
    }
};

export const BeGetLichSuThanhToan = async (id) => {
    try {
        const response = await axiosClient.get(`${PREFIX}/lich-su-thanh-toan/${id}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const BeGetLichSuHoaDon = async (idHoaDon) => {
    try {
        const response = await axiosClient.get(`${PREFIX}/lich-su/${idHoaDon}`);
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const BeUpdateMonDaLen = async (idChiTiet) => {
    try {
        await axiosClient.put(`${PREFIX}/chi-tiet-hoa-don/cap-nhat-da-len/${idChiTiet}`);
        return true;
    } catch (error) {
        throw error;
    }
};

export const BeUpdateTatCaDaLen = async (idHoaDon) => {
    try {
        await axiosClient.put(`${PREFIX}/chi-tiet-hoa-don/cap-nhat-tat-ca-da-len/${idHoaDon}`);
        return true;
    } catch (error) {
        throw error;
    }
};

export const BeHuyHoaDon = async (payload) => {
    try {
        await axiosClient.put(`${PREFIX}/huy-don`, payload);
        return true;
    } catch (error) {
        throw error;
    }
};

const SYSTEM_PREFIX = "/tham-so-he-thong";
export const BeGetThamSoHeThong = async () => {
    try {
        const response = await axiosClient.get(`${SYSTEM_PREFIX}/get-all-system`);
        return response.data;
    } catch (error) {
        console.error("Lỗi lấy cấu hình hệ thống:", error);
        throw error;
    }
};