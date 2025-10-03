import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from 'axios'

// 전역(사용자 사이트) 스타일만
import '@/assets/css/hotel_detail/app.css'
import '@/assets/css/hotel_detail/hotel_detail.css'
import '@/assets/css/homepage/calendar.css'

import 'flatpickr/dist/flatpickr.css' 
createApp(App).use(router).mount('#app')

const app = createApp(App)

app.config.globalProperties.$axios = axios;

// 라우터 사용 설정
app.use(router)

// 앱을 마운트합니다.
app.mount('#app')
