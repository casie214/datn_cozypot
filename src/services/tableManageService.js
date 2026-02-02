import axiosClient from "@/services/axiosClient"; // Đảm bảo đường dẫn import đúng

// Định nghĩa Prefix cho module này
// URL thực tế sẽ là: http://localhost:8080/api/dat-ban/...
const PREFIX = "/dat-ban";

export const fetchAll = async () => {
    try {
        const response = await axiosClient.get(`${PREFIX}/danh-sach`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const fetchAllBanAn = async () => {
    try {
        const response = await axiosClient.get(`${PREFIX}/danh-sach-ban-an`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const fetchBanAnById = async (id) => {
    try {
        const response = await axiosClient.get(`${PREFIX}/ban-an-detail/${id}`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const fetchSearchDatBan = async () => {
    try {
        const response = await axiosClient.get(`${PREFIX}/search`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const fetchAllCheckIn = async () => {
    try {
        const response = await axiosClient.get(`${PREFIX}/danh-sach-check-in`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const addBanAn = async (banAn) => {
    try {
        // Axios tự động stringify body
        await axiosClient.post(`${PREFIX}/add-ban-an`, banAn);
        return true;
    } catch (error) {
        throw error;
    }
}

export const updateBanAn = async (banAn) => {
    try {
        await axiosClient.put(`${PREFIX}/update-ban-an`, banAn);
        return true;
    } catch (error) {
        throw error;
    }
}

export const fetchAllKhuVuc = async () => {
    try {
        const response = await axiosClient.get(`${PREFIX}/danh-sach-khu-vuc`);
        return response.data;
    } catch (error) {
        throw error;
    }
}

export const updateTrangThaiBan = async (payload) => {
    try {
        const response = await axiosClient.put(`${PREFIX}/update-trang-thai-ban`, payload);
        return response.data;
    } catch (error) {
        throw new Error(error.response?.data || "Chưa có phiếu đặt bàn");
    }
};

export const searchDatBanService = async ({ payload, page, size }) => {
  try {
    const response = await axiosClient.post(`${PREFIX}/search`, payload, {
      params: { page, size }
    });
    return response.data;
  } catch (error) {
    // 🔥 SỬA: In lỗi ra console để debug
    console.error("🔥 Lỗi chi tiết từ Backend:", error.response);
    
    // Ném lỗi gốc ra để Vue bắt được (error.response.data thường chứa message từ Java)
    throw error; 
  }
};

export const updatePhieuDatBanService = async (payload) => {
    try {
        await axiosClient.put(`${PREFIX}/update`, payload);
        return true;
    } catch (error) {
        throw new Error("Cập nhật thất bại");
    }
};

export const updateTTPhieuDatBan = async (id, trangThai) => {
    try {
        await axiosClient.put(`${PREFIX}/update-trang-thai-phieu`, {
            id: id,
            trangThai: trangThai
        });
        return true;
    } catch (error) {
        throw new Error("Cập nhật trạng thái thất bại");
    }
};