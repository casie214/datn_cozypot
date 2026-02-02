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
        async login(username, password, isClient) {
            try {

                const endpoint = isClient ? '/auth/client/login' : '/auth/admin/login';

                const payload = {
                    username: username, 
                    password: password
                };

                const response = await axiosClient.post(endpoint, payload);

                const { accessToken, role, ...userInfo } = response.data;

                this.token = accessToken;
                this.role = role;
                this.user = userInfo;

                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('role', role);
                localStorage.setItem('user', JSON.stringify(userInfo));

                return true; 
            } catch (error) {
                console.error("Login Error:", error);
                throw error; 
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