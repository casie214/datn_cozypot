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
<<<<<<< HEAD
                    username: username, 
=======
                    username: username,
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
                    password: password
                };

                const response = await axiosClient.post(endpoint, payload);

<<<<<<< HEAD
                const { accessToken, role, ...userInfo } = response.data;
=======
                const { accessToken, refreshToken, role, ...userInfo } = response.data;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77

                this.token = accessToken;
                this.role = role;
                this.user = userInfo;

                localStorage.setItem('accessToken', accessToken);
<<<<<<< HEAD
                localStorage.setItem('role', role);
                localStorage.setItem('user', JSON.stringify(userInfo));

                return true; 
            } catch (error) {
                console.error("Login Error:", error);
                throw error; 
=======
                localStorage.setItem('refreshToken', refreshToken);
                localStorage.setItem('role', role);
                localStorage.setItem('user', JSON.stringify(userInfo));

                return true;
            } catch (error) {
                console.error("Login Error:", error);
                throw error;
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
            }
        },

        async register(payload) {
            try {
                await axiosClient.post('/auth/register', payload);
                return true;
            } catch (error) {
                console.error("Register Error:", error);
<<<<<<< HEAD
                throw error; 
            }
        },
        
=======
                throw error;
            }
        },

>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
        logout() {
            this.token = null;
            this.role = null;
            this.user = null;
            localStorage.clear();
<<<<<<< HEAD
            window.location.href = '/login'; 
=======
            window.location.href = '/login';
>>>>>>> 82e4d9f4f6100e25990e1110b92ec0111379fb77
        }
    }
});