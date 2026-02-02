import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8080/api', 
    headers: {
        'Content-Type': 'application/json',
    },
});

axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        
        if (token) {
            console.log("🟢 Gửi request với Token:", token.substring(0, 10) + "..."); // Log 10 ký tự đầu
            config.headers.Authorization = `Bearer ${token}`;
        } else {
            console.warn("🔴 Không tìm thấy Token trong localStorage");
        }
        
        return config;
    },
    (error) => Promise.reject(error)
);

axiosClient.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            console.error("⛔ Token hết hạn hoặc không hợp lệ -> Logout");
            localStorage.removeItem('accessToken');
        }
        return Promise.reject(error);
    }
);

export default axiosClient;