<script setup>
import { ref, nextTick, onUnmounted } from 'vue';
import axiosClient from '@/services/axiosClient';
import Swal from 'sweetalert2';
import { useRouter } from "vue-router";
const router = useRouter();

const handleOrderNow = (itemName) => {
  // Đóng chatbot lại cho khách nhìn thực đơn (tùy chọn)
  // isOpen.value = false; 

  // Điều hướng
  router.push({ 
    path: '/menu', 
    query: { autoAdd: itemName } 
  });
};

const isOpen = ref(false);
const userMessage = ref('');
const isTyping = ref(false);
const chatBodyRef = ref(null);
const messages = ref([]);
let pollInterval = null;

const scrollContainer = ref(null);

// Hàm parse tin nhắn để tách Text và Card
const parseMessage = (text) => {
    if (!text) return { cleanText: '', cards: [] };

    const cards = [];
    const cardRegex = /\[CARD:(.*?)\]/g;
    
    // 1. Tìm các thẻ [CARD:...] chuẩn
    let match;
    let tempText = text;
    while ((match = cardRegex.exec(text)) !== null) {
        const parts = match[1].split('|');
        if (parts.length >= 3) {
            console.log("🔥 Link ảnh AI gửi về:", parts[2].trim()); // 🚨 THÊM DÒNG NÀY
            cards.push({ name: parts[0].trim(), price: parts[1].trim(), image: parts[2].trim() });
        }
    }
    tempText = tempText.replace(cardRegex, '');

    // 2. PHÒNG THỦ: Nếu AI trả về định dạng "Tên | Giá | Link" mà quên ngoặc [CARD]
    // Regex này tìm: Chữ - Dấu gạch đứng - Chữ/Số - Dấu gạch đứng - Link http
    const fallbackRegex = /([A-Za-z0-9À-ỹ\s\(\)]+)\|([0-9.,\sVNĐđk]+)\|(https?:\/\/[^\s]+)/g;
    let fallbackMatch;
    while ((fallbackMatch = fallbackRegex.exec(tempText)) !== null) {
        cards.push({
            name: fallbackMatch[1].trim().replace(/^- /, ''), // Xóa dấu gạch đầu dòng nếu có
            price: fallbackMatch[2].trim(),
            image: fallbackMatch[3].trim()
        });
    }
    tempText = tempText.replace(fallbackRegex, '');

    // 3. Dọn dẹp link thô và ký tự rác
    const urlRegex = /(https?:\/\/[^\s]+)/g;
    let cleanText = tempText.replace(urlRegex, '').replace(/\|/g, '').trim();

    return { cleanText, cards };
};

const handleImageError = (e) => {
    e.target.src = 'https://via.placeholder.com/150?text=CozyPot'; // Ảnh mặc định khi lỗi link
};

const handleWheel = (event) => {
    // Lấy chính phần tử (Card list hoặc Gợi ý) đang được trỏ chuột
    const container = event.currentTarget;
    if (container) {
        // Chuyển thao tác lăn chuột dọc (deltaY) thành cuộn ngang (scrollLeft)
        container.scrollLeft += event.deltaY;
        
        // Tùy chọn: Bỏ comment dòng dưới nếu muốn chặn cuộn toàn bộ khung chat khi đang lăn ngang
        event.preventDefault(); 
    }
};

// Sinh Session ID ngẫu nhiên và lưu vào LocalStorage
let sessionId = localStorage.getItem('chatSessionId');
if (!sessionId) {
    sessionId = 'kh_' + Math.random().toString(36).substr(2, 9);
    localStorage.setItem('chatSessionId', sessionId);
}

const toggleChat = () => {
    isOpen.value = !isOpen.value;
    if (isOpen.value) {
        loadHistory();
        // Quét liên tục mỗi 2s để chờ Admin trả lời
        pollInterval = setInterval(loadHistory, 2000);
    } else {
        clearInterval(pollInterval);
    }
};

const loadHistory = async () => {
    try {
        const res = await axiosClient.get(`/chat/history/${sessionId}`);
        if (res.data.length > messages.value.length) {
            messages.value = res.data;
            scrollToBottom();
        }
    } catch (error) {
        console.error("Lỗi lấy lịch sử:", error);
    }
};

const scrollToBottom = async () => {
    await nextTick();
    if (chatBodyRef.value) {
        chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
};

const sendMessage = async () => {
    if (!userMessage.value.trim()) return;
    const textObj = userMessage.value.trim();
    userMessage.value = '';
    isTyping.value = true;
    scrollToBottom();

    try {
        await axiosClient.post('/chat', { message: textObj, sessionId: sessionId });
        await loadHistory();
    } catch (error) {
        messages.value.push({ sender: 'bot', content: 'Lỗi kết nối rồi ạ!' });
    } finally {
        isTyping.value = false;
        scrollToBottom();
    }
};

const startNewChat = async () => {
    const result = await Swal.fire({
        title: 'Bắt đầu cuộc trò chuyện mới?',
        text: "Toàn bộ lịch sử chat cũ sẽ bị xóa.",
        icon: 'question',
        showCancelButton: true,
        confirmButtonColor: '#7D161A',
        confirmButtonText: 'Đồng ý',
        cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
        try {
            await axiosClient.post('/chat/reset', { sessionId: sessionId });
            messages.value = []; // Xóa trắng tin nhắn trên màn hình
            // Tùy chọn: sinh sessionId mới nếu muốn tách biệt hoàn toàn log cũ
            // sessionId = 'kh_' + Math.random().toString(36).substr(2, 9);
            // localStorage.setItem('chatSessionId', sessionId);
            Swal.fire({ icon: 'success', title: 'Đã làm mới!', timer: 1000, showConfirmButton: false });
        } catch (error) {
            console.error(error);
        }
    }
};

const suggestions = [
    { text: '👨‍💼 Gặp nhân viên', value: 'Tôi muốn gặp nhân viên hỗ trợ' },
    { text: '🍲 Menu hôm nay', value: 'Có món gì ngon không?' },
    { text: '🧧 Khuyến mãi', value: 'Hiện có chương trình giảm giá nào không?' }
];

// Hàm gửi tin nhắn nhanh khi click vào bubble
const quickSend = (text) => {
    userMessage.value = text;
    sendMessage();
};

onUnmounted(() => {
    clearInterval(pollInterval);
});
</script>

<template>
    <div class="chat-widget-container">
        <div class="chat-bubble" @click="toggleChat" v-if="!isOpen">
            <i class="fa-solid fa-comment-dots"></i>
        </div>

        <div class="chat-window shadow-lg" v-if="isOpen">
            <div class="chat-header bg-wine text-white d-flex justify-content-between align-items-center p-3">
                <div class="d-flex align-items-center gap-2">
                    <div class="bot-avatar"><i class="fa-solid fa-robot"></i></div>
                    <h6 class="m-0 fw-bold">CozyPot AI</h6>
                </div>
                <div class="d-flex gap-2 align-items-center">
                    <button class="btn-header-action" @click="startNewChat"><i
                            class="fa-solid fa-rotate-right"></i></button>
                    <button class="btn-close-chat" @click="toggleChat"><i class="fa-solid fa-xmark"></i></button>
                </div>
            </div>

            <div class="chat-body p-3" ref="chatBodyRef">
                <div v-if="messages.length === 0" class="text-center text-muted small mt-2">
                    Dạ CozyPot xin chào! Quý khách cần hỗ trợ gì ạ?
                </div>

                <div v-for="(msg, index) in messages" :key="index"
                    :class="['msg-row', msg.sender === 'user' ? 'user-row' : 'bot-row']">

                    <div class="msg-container">
                        <div :class="['msg-bubble', msg.sender === 'user' ? 'user-msg' : 'bot-msg']">
                            {{ parseMessage(msg.content).cleanText }}
                        </div>

                        <div v-if="msg.sender === 'bot' && parseMessage(msg.content).cards.length > 0"
                            class="card-list mt-2" @wheel="handleWheel">
                            <div v-for="(card, cIdx) in parseMessage(msg.content).cards" :key="cIdx"
                                class="food-card shadow-sm">
                                <img :src="card.image" class="card-img-top" @error="handleImageError">
                                <div class="card-body p-2">
                                    <h6 class="food-title">{{ card.name }}</h6>
                                    <p class="food-price">{{ card.price }}</p>
                                    <button class="btn btn-sm btn-outline-danger w-100"
                                        @click="handleOrderNow(card.name)">
                                        Đặt món này
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-if="isTyping" class="msg-row bot-row">
                    <div class="msg-bubble bot-msg typing-indicator">
                        <span></span><span></span><span></span>
                    </div>
                </div>
            </div>

            <div class="suggestion-container" ref="scrollContainer" v-if="!isTyping" @wheel="handleWheel">
                <div v-for="(item, idx) in suggestions" :key="idx" class="suggestion-pill"
                    @click="quickSend(item.value)">
                    {{ item.text }}
                </div>
            </div>

            <div class="chat-footer p-2 border-top">
                <form @submit.prevent="sendMessage" class="d-flex gap-2">
                    <input v-model="userMessage" type="text" class="form-control shadow-none rounded-pill px-3"
                        placeholder="Nhập câu hỏi của bạn..." />
                    <button type="submit" class="btn btn-send rounded-circle"
                        :disabled="!userMessage.trim() || isTyping">
                        <i class="fa-solid fa-paper-plane"></i>
                    </button>
                </form>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* Giữ nguyên toàn bộ CSS của ChatWidget mà bạn đang có */
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

.bg-wine {
    background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
}

.text-wine {
    color: #7D161A;
}

.chat-widget-container {
    position: fixed;
    bottom: 20px;
    right: 20px;
    z-index: 9999;
}

.chat-bubble {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 24px;
    cursor: pointer;
    box-shadow: 0 4px 15px rgba(125, 22, 26, 0.4);
    transition: 0.3s;
    animation: bounce 2s infinite;
}

.chat-bubble:hover {
    transform: scale(1.1);
}

.chat-window {
    width: 350px;
    height: 500px;
    background: white;
    border-radius: 16px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.bot-avatar {
    width: 30px;
    height: 30px;
    background: white;
    color: #7D161A;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.btn-close-chat {
    background: none;
    border: none;
    color: white;
    font-size: 18px;
    cursor: pointer;
}

.chat-body {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 10px;
    background: #f8f9fa;
}

.msg-row {
    display: flex;
    width: 100%;
}

.user-row .msg-container {
    align-items: flex-end;      /* User thì căn phải */
}

.msg-container {
    display: flex;
    flex-direction: column;
    align-items: flex-start;    /* Bot thì căn trái */
    width: 100%;
}

.user-row {
    justify-content: flex-end;
}

.bot-row {
    justify-content: flex-start;
}

.msg-bubble {
    max-width: 85%;            /* Tăng lên 85% cho thoáng */
    padding: 10px 14px;
    font-size: 14px;
    line-height: 1.5;
    word-wrap: break-word;
    overflow-wrap: anywhere;    /* QUAN TRỌNG: Để cắt link dài nếu cần */
    white-space: pre-line;      /* Giữ lại các dấu xuống dòng của AI */
    width: fit-content;         /* Để bubble rộng theo nội dung */
}

.user-msg {
    background: #7D161A;
    color: white;
    border-radius: 16px 16px 0 16px;
}

.bot-msg {
    background: white;
    color: #333;
    border-radius: 16px 16px 16px 0;
    border: 1px solid #eee;
    box-shadow: 0 2px 5px rgba(0,0,0,0.02);
    align-self: flex-start;    /* Đảm bảo luôn nằm bên trái */
}

.btn-send {
    width: 40px;
    height: 40px;
    background: #7D161A;
    color: white;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
}

.btn-send:disabled {
    background: #ccc;
}

.typing-indicator span {
    display: inline-block;
    width: 6px;
    height: 6px;
    background-color: #888;
    border-radius: 50%;
    margin: 0 2px;
    animation: typing 1.4s infinite ease-in-out both;
}

.typing-indicator span:nth-child(1) {
    animation-delay: -0.32s;
}

.typing-indicator span:nth-child(2) {
    animation-delay: -0.16s;
}

@keyframes typing {

    0%,
    80%,
    100% {
        transform: scale(0);
    }

    40% {
        transform: scale(1);
    }
}

@keyframes bounce {

    0%,
    20%,
    50%,
    80%,
    100% {
        transform: translateY(0);
    }

    40% {
        transform: translateY(-10px);
    }

    60% {
        transform: translateY(-5px);
    }
}

.btn-header-action {
    background: none;
    border: none;
    color: white;
    font-size: 16px;
    cursor: pointer;
    opacity: 0.8;
    transition: 0.3s;
}

.suggestion-container {
    display: flex;
    flex-wrap: nowrap;
    scroll-behavior: smooth;
    /* ⭐ Bắt buộc: Không cho phép xuống dòng */
    gap: 10px;
    /* Khoảng cách giữa các bubble */
    padding: 12px 15px;
    background: #ffffff;
    overflow-x: auto;
    /* ⭐ Cho phép cuộn ngang */
    -webkit-overflow-scrolling: touch;
    /* Cuộn mượt trên điện thoại */
    border-top: 1px solid #f0f0f0;
    scrollbar-width: none;
    /* Ẩn thanh cuộn trên Firefox */
    ms-overflow-style: none;
    /* Ẩn thanh cuộn trên IE/Edge */
}

/* Ẩn thanh cuộn trên Chrome, Safari, Brave... */
.suggestion-container::-webkit-scrollbar {
    display: none;
}

/* Từng bubble gợi ý */
.suggestion-pill {
    flex-shrink: 0;
    /* ⭐ QUAN TRỌNG NHẤT: Không cho phép bubble bị bóp méo */
    background: #fff;
    color: #7D161A;
    border: 1px solid #7D161A;
    padding: 8px 16px;
    /* Tăng padding cho thoáng */
    border-radius: 20px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
    white-space: nowrap;
    /* Đảm bảo chữ bên trong không bị xuống dòng */
    box-shadow: 0 2px 6px rgba(125, 22, 26, 0.1);
    display: flex;
    align-items: center;
    gap: 5px;
}

.suggestion-pill:hover {
    background: #7D161A;
    color: white;
    transform: translateY(-2px);
}

.card-list {
    display: flex;
    flex-wrap: nowrap; /* Bắt buộc các card nằm trên 1 hàng ngang */
    gap: 12px;
    overflow-x: auto; /* Bật cuộn ngang */
    padding: 5px 0 10px 0;
    width: 100%;
    max-width: 310px; /* Điều chỉnh vừa vặn với khung chat 350px */
    -webkit-overflow-scrolling: touch; /* Giúp vuốt mượt trên điện thoại */
    scroll-behavior: smooth;
}

/* Thanh cuộn cho phần Card để khách xài máy tính dễ dùng */
.card-list::-webkit-scrollbar {
    height: 6px; 
}

.card-list::-webkit-scrollbar-thumb {
    background: #d32f2f; /* Màu đỏ tone-sur-tone với quán */
    border-radius: 10px;
}

.card-list::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 10px;
}

.food-card {
    min-width: 200px;
    background: white;
    border-radius: 12px;
    border: 1px solid #eee;
    flex-shrink: 0; /* Quan trọng: Chống bị bóp méo form card khi bị thiếu chỗ */
    box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.card-img-top {
    width: 100%;
    height: 120px;
    object-fit: cover;
}

.food-title {
    font-size: 14px;
    font-weight: 700;
    margin-bottom: 2px;
    color: #333;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.food-price {
    color: #7D161A;
    font-weight: 600;
    font-size: 13px;
    margin-bottom: 8px;
}

/* Tùy chỉnh để thanh scroll card mượt hơn */
.card-list::-webkit-scrollbar {
    height: 4px;
}
.card-list::-webkit-scrollbar-thumb {
    background: #ccc;
    border-radius: 10px;
}
</style>