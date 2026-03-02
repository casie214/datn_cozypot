// src/stores/auth.js
import { defineStore } from 'pinia';
import axiosClient from '@/services/axiosClient';

export const useAuthStore = defineStore('auth', {
    state: () => ({
        token: localStorage.getItem('accessToken') || null,
        role: localStorage.getItem('role') || null,
        user: JSON.parse(localStorage.getItem('user')) || null,
    }),

    actions: {
        async login(email, password, isClient) {
            console.log(">>> Đang kết nối server...");
            try {
                const endpoint = isClient ? '/auth/client/login' : '/auth/admin/login';

                // Dùng destructing lấy thẳng data từ response của axios
                const { data } = await axiosClient.post(endpoint, { username: email, password });

                if (!data?.accessToken) {
                    throw new Error("Không nhận được mã xác thực từ máy chủ.");
                }

                // 1. Chuẩn hóa User Info
                const safeUserInfo = {
                    id: data.id || null,
                    username: data.username || '',
                    hoTen: data.hoTen || 'Người dùng',
                    sdt: data.sdt || '',
                    email: email,
                };

                // 2. Cập nhật Store đồng thời (Pinia)
                this.token = data.accessToken;
                this.refreshToken = data.refreshToken || '';
                this.role = data.role || '';
                this.user = safeUserInfo;

                // 3. Lưu vào LocalStorage một cách hệ thống
                const storageItems = {
                    accessToken: data.accessToken,
                    refreshToken: data.refreshToken || '',
                    role: data.role || '',
                    user: JSON.stringify(safeUserInfo)
                };

                // Chạy vòng lặp để set thay vì viết từng dòng
                Object.entries(storageItems).forEach(([key, value]) => {
                    localStorage.setItem(key, value);
                });
                return true;
            } catch (error) {
                const errorMsg = error.response?.data?.message || error.response?.data || error.message;
                if (error.response?.status === 401) {
                    console.warn(">>> Đăng nhập thất bại: Sai thông tin");
                    throw new Error("Tài khoản hoặc mật khẩu không đúng.");
                }
                console.error(">>> Lỗi hệ thống:", errorMsg);
                throw new Error(errorMsg);
            }
        },

        async register(payload) {
            try {
                await axiosClient.post('/auth/register', payload);
                return true;
            } catch (error) {
                console.error("Register Error:", error);
                throw error;
            }
        },

        logout() {
            this.token = null;
            this.role = null;
            this.user = null;
            localStorage.clear();
            window.location.href = '/login';
        }
    }
});