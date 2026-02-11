import axios from 'axios';

const axiosClient = axios.create({
    baseURL: 'http://localhost:8080/api', 
    headers: {
        'Content-Type': 'application/json',
    },
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
    failedQueue.forEach((prom) => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

axiosClient.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            console.log("🟢 Gửi request với Token:", token.substring(0, 10) + "...");
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
    async (error) => {
        const originalRequest = error.config;
        if (!error.response) {
            return Promise.reject(error);
        }

        // Nếu lỗi 401 (Hết hạn token) và chưa thử lại lần nào
        if (error.response.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise(function(resolve, reject) {
                    failedQueue.push({ resolve, reject });
                })
                .then((token) => {
                    originalRequest.headers.Authorization = `Bearer ${token}`;
                    return axiosClient(originalRequest);
                })
                .catch((err) => {
                    return Promise.reject(err);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) throw new Error("Không có refresh token");

                // Gọi API để lấy Access Token mới
                const res = await axios.post('http://localhost:8080/api/auth/refresh-token', {
                    refreshToken: refreshToken
                }, {
                    headers: { 'Authorization': '' }
                });

                if (res.status === 200) {
                    const { accessToken } = res.data;
                    localStorage.setItem('accessToken', accessToken);
                    
                    axiosClient.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
                    originalRequest.headers.Authorization = `Bearer ${accessToken}`;

                    processQueue(null, accessToken);
                    return axiosClient(originalRequest);
                }
            } catch (refreshError) {
                console.error("Refresh token thất bại:", refreshError);
                processQueue(refreshError, null);

                // Xóa sạch thông tin và đá ra trang login
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('role');

                window.location.href = '/login';
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

export default axiosClient;