import axios from "axios";

const API_URL = "http://localhost:8080/api/khach-hang";

const khachHangService = {

    // 🔥 Thống kê khách hàng theo tháng (dùng cho chọn KH cá nhân)
    thongKeTheoThang: (thang, nam) => {
        return axios.get(`${API_URL}/thong-ke`, {
            params: { thang, nam }
        }).then(res => res.data);
    },

    // (OPTIONAL) lấy khách đang hoạt động
    getActive: () => {
        return axios.get(`${API_URL}/active`)
            .then(res => res.data);
    }

};

export default khachHangService;
