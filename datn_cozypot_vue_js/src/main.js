import { createApp } from 'vue'
import App from './App.vue'
import { createPinia } from 'pinia'
import router from "./router"; 

// Import CSS
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'; 
import '@fortawesome/fontawesome-free/css/all.min.css'

// Import Datepicker
import { VueDatePicker } from '@vuepic/vue-datepicker';
import '@vuepic/vue-datepicker/dist/main.css';

// 1. Khởi tạo app (Chỉ 1 lần)
const app = createApp(App);

// 2. Sử dụng Pinia
const pinia = createPinia();
app.use(pinia);

// 3. Sử dụng Router
app.use(router);

// 4. Đăng ký Global Component
app.component('VueDatePicker', VueDatePicker);

// 5. Mount (Cuối cùng)
app.mount('#app');