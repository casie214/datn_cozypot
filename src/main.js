import { createApp } from 'vue'
import App from './App.vue'
import 'bootstrap/dist/css/bootstrap.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'; 
import router from "./App";
import '@fortawesome/fontawesome-free/css/all.min.css'

createApp(App).use(router).mount('#app')
