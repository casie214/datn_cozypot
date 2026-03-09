<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue';

// --- TRẠNG THÁI UI ---
const isOpen = ref(false);
const messages = ref<any[]>([]);
const inputText = ref('');
const isBotTyping = ref(false);
const msgContainer = ref<HTMLElement | null>(null);

let lastSentText = '';
const CLIENT_ID = 'fef3d7da-24b9-493d-8f99-afe452414499';
const BACKEND_URL = 'http://localhost:8080/api/botpress';
const AUTH_TOKEN = 'Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6InVzZXJfMDFLSzY3MTVDU1lBQVM3REU0RDhTMzg3UkYiLCJpYXQiOjE3NzI5NTY0NTZ9.6wcLiejjTMJlKVTfOe3zYTbFV-sGmWcUv5ySCJQwVWo';

// 1. Hàm nạp script SDK
const loadAndInit = () => {
    return new Promise((resolve) => {
        if ((window as any).botpress) return resolve((window as any).botpress);

        const script = document.createElement('script');
        script.src = 'https://cdn.botpress.cloud/webchat/v3.6/inject.js';
        script.async = true;
        script.onload = () => {
            const check = setInterval(() => {
                if ((window as any).botpress) {
                    clearInterval(check);
                    resolve((window as any).botpress);
                }
            }, 100);
        };
        document.body.appendChild(script);
    });
};

// 🚨 ĐÃ SỬA: Lệnh kích hoạt SDK chạy ngầm
const triggerSdkHandshake = () => {
    const bp = (window as any).botpress;
    if (!bp) return;

    console.log("⚡ Đang thực hiện quy trình cưỡng chế...");

    // 1. Ép hiển thị UI gốc (Phải show thì mạng mới chạy)
    bp.sendEvent({ type: 'show' });

    setTimeout(() => {
        console.log("🚀 Đang nện Payload thô để ép sinh ID...");
        
        // Dùng sendPayload thay vì sendMessage để lách qua Shadow DOM
        if (typeof bp.sendPayload === 'function') {
            bp.sendPayload({
                type: 'text',
                text: 'Bắt đầu' // Chữ này phải khớp với từ khóa bắt đầu trong flow Studio
            });
        }

        // 2. Chờ Network nhảy (POST /messages) rồi mới đóng
        setTimeout(() => {
            bp.sendEvent({ type: 'hide' });
            console.log("🤫 Handshake hoàn tất.");
        }, 1500); 

    }, 1500); // Tăng thời gian chờ lên 1.5s để iFrame kịp nạp Config
};

// HÀM LẤY LỊCH SỬ TIN NHẮN
const fetchHistory = async (convId: string) => {
    try {
        console.log("📜 Đang nạp lịch sử cho ID:", convId);
        const res = await fetch(`${BACKEND_URL}/history/${convId}`, {
            headers: { 'Authorization': AUTH_TOKEN }
        });
        const data = await res.json();
        const apiMsgs = data.messages || data;

        if (Array.isArray(apiMsgs)) {
            messages.value = apiMsgs.map((msg: any) => ({
                id: msg.id || Math.random(),
                text: msg.payload?.block?.block?.text || msg.payload?.payload?.text || msg.payload?.text || msg.text || "",
                isBot: msg.direction === 'outgoing', 
                time: msg.createdAt ? new Date(msg.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ""
            })).filter((m: any) => m.text && m.text !== "."); 
            nextTick(scrollToBottom);
        }
    } catch (e) { console.error("❌ Lỗi Fetch History:", e); }
};

// THEO DÕI MỞ MODAL CUSTOM CỦA BẠN
watch(isOpen, (newVal) => {
    if (newVal) {
        const id = localStorage.getItem('bp_conversation_id');
        // Nếu chưa có ID (mới Reset xong), "mượn tay" bật botpress ngầm lên
        if (!id) {
            triggerSdkHandshake();
        }
        nextTick(scrollToBottom);
    }
});

const initBotpress = async () => {
    const bp = (await loadAndInit()) as any;
    const savedConvId = localStorage.getItem('bp_conversation_id');

    console.log("🛠 Đang khởi tạo Botpress với ID:", CLIENT_ID);

    // 🚨 QUAN TRỌNG: Lắng nghe sự kiện TRƯỚC khi gọi init
    bp.on('webchat:ready', () => {
        console.log("🚀 iFrame đã xuất hiện trong Elements!");
        if (!savedConvId) {
            triggerSdkHandshake();
        } else {
            fetchHistory(savedConvId);
        }
    });

    // 🚨 GỌI INIT THỦ CÔNG (Bỏ cái configScript đi)
    bp.init({
        clientId: CLIENT_ID,
        conversationId: savedConvId || undefined,
        configuration: { 
            hideWidget: false, // Để false để SDK render iFrame ngay lập tức
            disableAnimations: true,
            closeOnEscape: false,
            showCloseButton: false
        }
    });

    // Các lắng nghe tin nhắn giữ nguyên...
    bp.on('message', (event: any) => {
        const currentConvId = event.conversationId || event.payload?.conversationId;
        if (currentConvId && !localStorage.getItem('bp_conversation_id')) {
            localStorage.setItem('bp_conversation_id', currentConvId);
            console.log("🆔 Đã chộp được ID:", currentConvId);
        }
        // ... logic hiển thị tin nhắn của bạn ...
        const text = event.payload?.block?.block?.text || event.payload?.payload?.text || event.payload?.text || event.text || "";
        const direction = event.direction || event.payload?.direction;
        if (text && (direction === 'incoming' || direction === undefined) && text !== lastSentText) {
            isBotTyping.value = false;
            const msgId = event.payload?.id || event.id || Math.random();
            if (!messages.value.some(m => m.id === msgId)) {
                messages.value.push({
                    id: msgId, text, isBot: true,
                    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                });
                nextTick(scrollToBottom);
            }
        }
    });

    bp.on('*', (event: any) => {
        // Log ra để bạn nhìn thấy mọi thứ Shadow DOM đang làm
        console.log("🔍 Sự kiện từ Shadow Root:", event.type, event);

        // Chộp ID từ bất kỳ sự kiện nào có chứa conversationId
        const currentId = event.conversationId || event.payload?.conversationId;
        
        if (currentId && !localStorage.getItem('bp_conversation_id')) {
            localStorage.setItem('bp_conversation_id', currentId);
            console.log("🆔 ĐÃ LẤY ĐƯỢC ID TỪ SHADOW DOM:", currentId);
        }

        // Xử lý hiển thị tin nhắn Bot (vẫn giữ như cũ)
        if (event.type === 'message') {
            const text = event.payload?.block?.block?.text || event.payload?.payload?.text || event.payload?.text || event.text || "";
            const direction = event.direction || event.payload?.direction;
            
            if (text && (direction === 'incoming' || direction === undefined) && text !== lastSentText) {
                isBotTyping.value = false;
                messages.value.push({
                    id: event.payload?.id || Math.random(),
                    text, isBot: true,
                    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                });
                nextTick(scrollToBottom);
            }
        }
    });
};

const createNewChat = () => {
    if (!confirm("Bắt đầu cuộc trò chuyện mới?")) return;
    localStorage.removeItem('bp_conversation_id');
    Object.keys(localStorage).forEach(key => { if (key.includes('bp-')) localStorage.removeItem(key); });
    messages.value = [];
    window.location.reload(); 
};

const sendMessage = () => {
    const bp = (window as any).botpress;
    if (!inputText.value.trim() || !bp) return;
    const text = inputText.value;
    lastSentText = text;
    messages.value.push({ text, isBot: false, time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) });
    
    // Gửi tin nhắn đi, SDK sẽ tự động lấy ID nếu nó vừa được tạo ngầm
    bp.sendMessage(text);
    inputText.value = '';
    scrollToBottom();
};

const parseMarkdown = (text: string) => {
    if (!text || text === ".") return []; 
    return text.split('---').map(part => {
        let html = part.trim();
        html = html.replace(/!\[\[?.*?\]?\]\((.*?)\)/g, '<div class="img-wrapper"><img src="$1" class="menu-img" /></div>');
        html = html.replace(/\*\*(.*?)\*\*/g, '<b>$1</b>');
        html = html.replace(/\n/g, '<br>');
        return html;
    }).filter(p => p !== "");
};

const scrollToBottom = () => {
    nextTick(() => { if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight; });
};

onMounted(initBotpress);
</script>

<template>
    <div class="bot-wrapper">
        <div class="chat-fab" @click="isOpen = !isOpen" v-if="!isOpen">
            <i class="fas fa-comment-alt"></i> <span>Hỗ trợ</span>
        </div>

        <div class="chat-card" v-if="isOpen">
            <div class="chat-header">
                <div class="header-info">
                    <div class="status-dot"></div>
                    <div>
                        <h3>Trợ lý CozyPot</h3><span>Đang hoạt động</span>
                    </div>
                </div>
                <div class="header-actions">
                    <button title="Làm mới" @click="createNewChat" class="action-btn"><i class="fas fa-redo-alt"></i></button>
                    <button @click="isOpen = false" class="action-btn">×</button>
                </div>
            </div>

            <div class="chat-body custom-scrollbar" ref="msgContainer">
                <div v-for="(msg, i) in messages" :key="i" :class="['msg-line', msg.isBot ? 'bot' : 'user']">
                    <div class="bubble-group">
                        <div v-if="msg.isBot" class="rich-bubble">
                            <div v-for="(card, idx) in parseMarkdown(msg.text)" :key="idx" class="menu-card">
                                <div v-html="card"></div>
                            </div>
                        </div>
                        <div v-else class="bubble">{{ msg.text }}</div>
                        <span class="time">{{ msg.time }}</span>
                    </div>
                </div>
                <div v-if="isBotTyping" class="msg-line bot">
                    <div class="bubble typing-dots"><span></span><span></span><span></span></div>
                </div>
            </div>

            <div class="chat-footer">
                <input v-model="inputText" @keyup.enter="sendMessage" placeholder="Nhập tin nhắn..." />
                <button @click="sendMessage"><i class="fas fa-paper-plane"></i></button>
            </div>
        </div>
    </div>
</template>

<style scoped>
/* CSS giữ nguyên bản đẹp của bạn */
.bot-wrapper {
    position: fixed;
    bottom: 30px;
    right: 30px;
    z-index: 9999;
    font-family: 'Segoe UI', sans-serif;
}

.chat-fab {
    display: flex;
    align-items: center;
    background: #7D161A;
    color: white;
    padding: 10px 20px;
    border-radius: 30px;
    cursor: pointer;
    box-shadow: 0 8px 24px rgba(125, 22, 26, 0.3);
}

.chat-card {
    width: 380px;
    height: 550px;
    background: white;
    border-radius: 20px;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    box-shadow: 0 15px 50px rgba(0, 0, 0, 0.15);
    border: 1px solid #eee;
}

.chat-header {
    background: #7D161A;
    color: white;
    padding: 15px 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header-info {
    display: flex;
    align-items: center;
    gap: 12px;
}

.status-dot {
    width: 10px;
    height: 10px;
    background: #2ECC71;
    border-radius: 50%;
    border: 2px solid white;
}

.header-actions {
    display: flex;
    gap: 15px;
}

.action-btn {
    background: none;
    border: none;
    color: white;
    cursor: pointer;
    font-size: 18px;
    opacity: 0.8;
}

.chat-body {
    flex: 1;
    padding: 20px;
    background: #F4F7F9;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
}

.msg-line {
    display: flex;
    margin-bottom: 15px;
    width: 100%;
}

.bot {
    justify-content: flex-start;
}

.user {
    justify-content: flex-end;
}

.bubble-group {
    max-width: 90%;
    display: flex;
    flex-direction: column;
}

.bubble {
    padding: 12px 16px;
    border-radius: 18px;
    font-size: 14px;
    background: #7D161A;
    color: white;
}

.bot .bubble {
    background: white;
    color: #333;
}

.time {
    font-size: 10px;
    color: #999;
    margin-top: 4px;
    padding: 0 5px;
}

.menu-card {
    background: white;
    border-radius: 12px;
    padding: 12px;
    margin-bottom: 10px;
    border: 1px solid #e0e0e0;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.img-wrapper) {
    width: 100%;
    margin: 8px 0;
    border-radius: 8px;
    overflow: hidden;
    background: #f0f0f0;
}

:deep(.menu-img) {
    width: 100%;
    display: block;
    object-fit: cover;
}

:deep(b) {
    color: #7D161A;
}

.chat-footer {
    padding: 15px;
    background: white;
    border-top: 1px solid #eee;
    display: flex;
    gap: 10px;
}

.chat-footer input {
    flex: 1;
    border: none;
    background: #F0F2F5;
    padding: 12px 18px;
    border-radius: 25px;
    outline: none;
}

.chat-footer button {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    border: none;
    background: #7D161A;
    color: white;
    cursor: pointer;
}

.typing-dots {
    display: flex;
    gap: 4px;
    padding: 10px 15px !important;
}

.typing-dots span {
    width: 6px;
    height: 6px;
    background: #999;
    border-radius: 50%;
    animation: bounce 1.4s infinite ease-in-out;
}

@keyframes bounce {

    0%,
    80%,
    100% {
        transform: scale(0);
    }

    40% {
        transform: scale(1);
    }
}

.custom-scrollbar::-webkit-scrollbar {
    width: 5px;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #DBDBDB;
    border-radius: 10px;
}
</style>

<style>
.bpChatContainer, 
#bp-web-widget-container,
bp-web-widget {
    display: block !important;
    position: fixed !important;
    bottom: 0 !important;
    right: 0 !important;
    width: 400px !important;
    height: 600px !important;
    opacity: 0 !important;
    visibility: visible !important; 
    pointer-events: none !important;
    z-index: -9999 !important; 
}
</style>