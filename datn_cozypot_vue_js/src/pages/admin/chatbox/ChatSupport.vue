<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue';
import Swal from 'sweetalert2';
import axiosClient from '@/services/axiosClient';

const waitingList = ref([]);
const selectedId = ref(null);
const messages = ref([]);
const replyText = ref('');
const customerSearch = ref('');
const msgContainer = ref(null);

let waitingListInterval = null;
let messageInterval = null;

const filteredWaitingList = computed(() => {
    return waitingList.value.filter(id => id.toLowerCase().includes(customerSearch.value.toLowerCase()));
});

// 1. Lấy danh sách khách hàng chờ từ RAM
const fetchWaitingList = async () => {
    try {
        const res = await axiosClient.get('/chat/active-sessions');
        waitingList.value = res.data;
    } catch (error) {
        console.error("Lỗi lấy danh sách:", error);
    }
};

// 2. Chọn khách hàng lần đầu
const selectCustomer = async (id) => {
    selectedId.value = id;
    try {
        const res = await axiosClient.get(`/chat/history/${id}`);
        messages.value = res.data; // API mới trả về List trực tiếp, không cần .messages.reverse()
        scrollToBottom();
    } catch (error) {
        Swal.fire('Lỗi', 'Không thể tải lịch sử trò chuyện', 'error');
    }
};

// 3. Tự động quét tin nhắn mới mỗi 2 giây
const refreshMessages = async () => {
    if (!selectedId.value) return;

    try {
        const res = await axiosClient.get(`/chat/history/${selectedId.value}`);
        const newMessages = res.data;

        if (newMessages.length > messages.value.length) {
            messages.value = newMessages;
            scrollToBottom();
        } else {
            messages.value = newMessages;
        }
    } catch (error) {
        console.error("Lỗi tải tin nhắn tự động:", error);
    }
};

// 4. Gửi tin nhắn của Nhân viên
const sendReply = async () => {
    if (!replyText.value.trim()) return;
    const textToSend = replyText.value;
    replyText.value = '';

    try {
        await axiosClient.post('/chat/admin-reply', {
            sessionId: selectedId.value,
            message: textToSend
        });
        // Quét lại ngay lập tức để hiện tin nhắn vừa gửi
        await refreshMessages();
    } catch (error) {
        Swal.fire('Thất bại', 'Lỗi server', 'error');
    }
};

// 5. Kết thúc hỗ trợ
const resolveChat = async () => {
    try {
        await axiosClient.delete(`/chat/resolve/${selectedId.value}`);
        selectedId.value = null;
        messages.value = [];
        fetchWaitingList();
        Swal.fire({ icon: 'success', title: 'Hoàn tất hỗ trợ!', timer: 1000, showConfirmButton: false });
    } catch (error) {
        console.error(error);
    }
};

const scrollToBottom = () => {
    nextTick(() => {
        if (msgContainer.value) {
            msgContainer.value.scrollTop = msgContainer.value.scrollHeight;
        }
    });
};


const formatTime = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

onMounted(() => {
    fetchWaitingList();
    waitingListInterval = setInterval(fetchWaitingList, 5000);
    messageInterval = setInterval(refreshMessages, 2000);
});

onUnmounted(() => {
    clearInterval(waitingListInterval);
    clearInterval(messageInterval);
});
</script>

<template>
    <div class="chat-wrapper">
        <div class="chat-admin-container">
            <div class="customer-sidebar">
                <div class="sidebar-header">
                    <h5 class="sidebar-title">Danh sách chờ</h5>
                    <div class="filter-item search">
                        <div class="input-group search-group">
                            <i class="fas fa-search search-icon"></i>
                            <input v-model="customerSearch" type="text" class="form-search form-control"
                                placeholder="Tìm mã KH..." />
                        </div>
                    </div>
                </div>

                <div class="customer-list-scroll custom-scrollbar">
                    <div v-if="waitingList.length === 0" class="empty-state-small">
                        <div class="empty-circle">
                            <i class="fas fa-mug-hot"></i>
                        </div>
                        <span>Chưa có khách cần hỗ trợ</span>
                    </div>

                    <div v-for="id in filteredWaitingList" :key="id" @click="selectCustomer(id)"
                        :class="['customer-item', { active: selectedId === id }]">
                        <div class="avatar">
                            <i class="fas fa-user-circle"></i>
                            <span class="online-indicator"></span>
                        </div>
                        <div class="customer-info">
                            <div class="customer-id">Khách hàng {{ id.substring(0, 8).toUpperCase() }}</div>
                            <div class="last-msg">Đang đợi phản hồi...</div>
                        </div>
                        <div class="action-arrow">
                            <i class="fas fa-chevron-right"></i>
                        </div>
                    </div>
                </div>
            </div>

            <div class="chat-main" v-if="selectedId">
                <div class="chat-header-bar">
                    <div class="header-info">
                        <div class="header-avatar"><i class="fas fa-user-circle"></i></div>
                        <div class="header-text">
                            <strong>KH-{{ selectedId.substring(0, 8).toUpperCase() }}</strong>
                            <span class="status-text"><span class="status-dot"></span> Đang trực tuyến</span>
                        </div>
                    </div>
                    <div class="header-actions">
                        <button class="btn-action-icon btn-refresh-only" @click="selectCustomer(selectedId)">
                            <i class="fas fa-sync-alt"></i>
                        </button>
                    </div>
                </div>

                <div class="message-display custom-scrollbar" ref="msgContainer">
                    <div v-for="(msg, index) in messages" :key="index"
                        :class="['message-row', (msg.sender === 'admin' || msg.sender === 'bot') ? 'row-admin' : 'row-user']">

                        <small v-if="msg.sender === 'bot'" class="sender-label">Hệ thống AI</small>

                        <div class="bubble" :class="{ 'bot-bubble': msg.sender === 'bot' }">
                            {{ msg.content }}
                        </div>
                        <div class="msg-time">{{ formatTime(msg.createdAt) }}</div>
                    </div>
                </div>

                <div class="input-area">
                    <div class="input-wrapper">
                        <button class="btn-attach" title="Đính kèm (Sắp ra mắt)">
                            <i class="fas fa-paperclip"></i>
                        </button>
                        <input v-model="replyText" @keyup.enter="sendReply" placeholder="Nhập tin nhắn hỗ trợ..."
                            class="messenger-input" />
                        <button class="btn-send" @click="sendReply" :disabled="!replyText.trim()">
                            <i class="fas fa-paper-plane"></i>
                        </button>
                    </div>
                </div>
            </div>

            <div class="chat-empty-state" v-else>
                <div class="empty-content">
                    <div class="empty-illustration">
                        <i class="fas fa-comments"></i>
                    </div>
                    <h4>Chọn khách hàng để bắt đầu</h4>
                    <p>Hệ thống CSKH CozyPot - Trực tiếp hỗ trợ khách hàng</p>
                </div>
            </div>

        </div>
    </div>
</template>

<style scoped>
/* Giữ nguyên 100% CSS cũ của bạn */
.chat-wrapper {
    padding: 15px;
    background: #f0f2f5;
    height: calc(100vh - 90px);
    width: 100%;
    display: flex;
}

.chat-admin-container {
    display: flex;
    width: 100%;
    height: 100%;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
    overflow: hidden;
    border: 1px solid #e4e6eb;
}

.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: #cdd0d4;
    border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: #aeb0b4;
}

.customer-sidebar {
    width: 340px;
    border-right: 1px solid #e4e6eb;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    height: 100%;
}

.sidebar-header {
    padding: 20px;
    border-bottom: 1px solid #e4e6eb;
    flex-shrink: 0;
}

.sidebar-title {
    margin: 0 0 15px 0;
    font-weight: 700;
    color: #050505;
    font-size: 1.2rem;
}

.search-group {
    position: relative;
    display: flex;
    align-items: center;
}

.search-icon {
    position: absolute;
    left: 12px;
    color: #65676b;
    font-size: 0.9rem;
}

.form-search {
    width: 100%;
    padding: 10px 10px 10px 35px;
    border-radius: 20px;
    border: 1px solid #e4e6eb;
    background: #f8f9fa;
    font-size: 0.95rem;
    transition: all 0.3s ease;
}

.form-search:focus {
    outline: none;
    background: #ffffff;
    border-color: #7D161A;
    box-shadow: 0 0 0 3px rgba(125, 22, 26, 0.1);
}

.customer-list-scroll {
    flex: 1;
    overflow-y: auto;
}

.customer-item {
    display: flex;
    align-items: center;
    padding: 15px 20px;
    cursor: pointer;
    transition: background 0.2s ease;
    border-bottom: 1px solid #f8f9fa;
    position: relative;
}

.customer-item:hover {
    background-color: #f8f9fa;
}

.customer-item.active {
    background-color: rgba(125, 22, 26, 0.05);
}

.customer-item.active::before {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 4px;
    background: #7D161A;
}

.avatar {
    position: relative;
    font-size: 2.8rem;
    color: #bcc0c4;
    margin-right: 15px;
}

.online-indicator {
    position: absolute;
    bottom: 4px;
    right: 2px;
    width: 12px;
    height: 12px;
    background: #31a24c;
    border: 2px solid #fff;
    border-radius: 50%;
}

.customer-info {
    flex: 1;
    overflow: hidden;
}

.customer-id {
    font-weight: 600;
    color: #050505;
    font-size: 0.95rem;
    margin-bottom: 4px;
}

.last-msg {
    color: #65676b;
    font-size: 0.85rem;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.action-arrow {
    color: #bcc0c4;
    font-size: 0.8rem;
    opacity: 0;
    transition: opacity 0.2s;
}

.customer-item:hover .action-arrow,
.customer-item.active .action-arrow {
    opacity: 1;
}

.chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    height: 100%;
    overflow: hidden;
}

.chat-header-bar {
    padding: 15px 25px;
    border-bottom: 1px solid #e4e6eb;
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    flex-shrink: 0;
    z-index: 10;
}

.header-info {
    display: flex;
    align-items: center;
}

.header-avatar {
    font-size: 2.5rem;
    color: #7D161A;
    margin-right: 12px;
}

.header-text strong {
    display: block;
    font-size: 1.1rem;
    color: #050505;
}

.status-text {
    font-size: 0.8rem;
    color: #65676b;
    display: flex;
    align-items: center;
    gap: 5px;
}

.status-dot {
    width: 8px;
    height: 8px;
    background: #31a24c;
    border-radius: 50%;
    display: inline-block;
}

.header-actions {
    display: flex;
    gap: 10px;
}

.btn-action-icon {
    border: none;
    background: transparent;
    cursor: pointer;
    font-size: 1rem;
    padding: 8px 15px;
    border-radius: 6px;
    transition: all 0.2s;
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 600;
}

.btn-refresh-only {
    color: #65676b;
    background: #f0f2f5;
}

.btn-refresh-only:hover {
    background: #e4e6eb;
}

.btn-resolve {
    color: #fff;
    background: #28a745;
}

.btn-resolve:hover {
    background: #218838;
}

.message-display {
    flex: 1;
    padding: 25px;
    background: #f4f6f9;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
}

.chat-started-info {
    text-align: center;
    margin-bottom: 20px;
}

.chat-started-info span {
    font-size: 0.75rem;
    color: #65676b;
    background: #e4e6eb;
    padding: 4px 12px;
    border-radius: 12px;
}

.message-row {
    margin-bottom: 15px;
    display: flex;
    flex-direction: column;
    max-width: 65%;
}

.row-admin {
    align-self: flex-end;
    align-items: flex-end;
}

.bot-bubble {
    background: #e4e6eb !important;
    /* Màu xám nhẹ cho bot */
    color: #333 !important;
    border: 1px solid #cdd0d4 !important;
}

.sender-label {
    font-size: 0.65rem;
    color: #8c939d;
    margin-bottom: 2px;
    font-weight: bold;
    text-transform: uppercase;
}

.row-admin {
    align-self: flex-end;
    align-items: flex-end;
    /* Cả Bot và Admin đều nảy về bên phải */
}

/* Row user vẫn ở bên trái */
.row-user {
    align-self: flex-start;
    align-items: flex-start;
}

.bubble {
    padding: 12px 18px;
    border-radius: 18px;
    font-size: 0.95rem;
    line-height: 1.4;
    word-wrap: break-word;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.row-user .bubble {
    background: #ffffff;
    color: #050505;
    border: 1px solid #e4e6eb;
    border-bottom-left-radius: 4px;
}

.row-admin .bubble {
    background: linear-gradient(135deg, #7D161A 0%, #a82025 100%);
    color: #ffffff;
    border-bottom-right-radius: 4px;
}

.msg-time {
    font-size: 0.7rem;
    color: #8c939d;
    margin-top: 5px;
    margin-left: 4px;
    margin-right: 4px;
}

.input-area {
    padding: 15px 25px;
    background: #ffffff;
    border-top: 1px solid #e4e6eb;
    flex-shrink: 0;
    z-index: 10;
}

.input-wrapper {
    display: flex;
    background: #f0f2f5;
    border-radius: 25px;
    padding: 8px 15px;
    align-items: center;
    transition: all 0.3s ease;
    border: 1px solid transparent;
}

.input-wrapper:focus-within {
    background: #ffffff;
    border-color: #7D161A;
    box-shadow: 0 0 0 3px rgba(125, 22, 26, 0.1);
}

.btn-attach {
    background: none;
    border: none;
    color: #65676b;
    font-size: 1.2rem;
    padding: 5px 10px;
    cursor: pointer;
    transition: color 0.2s;
}

.btn-attach:hover {
    color: #7D161A;
}

.messenger-input {
    flex: 1;
    border: none;
    background: transparent;
    outline: none;
    font-size: 0.95rem;
    padding: 5px 10px;
    color: #050505;
}

.btn-send {
    background: none;
    border: none;
    color: #7D161A;
    font-size: 1.2rem;
    padding: 5px 10px;
    cursor: pointer;
    transition: transform 0.2s, color 0.2s;
}

.btn-send:hover:not(:disabled) {
    transform: scale(1.1);
}

.btn-send:disabled {
    color: #bcc0c4;
    cursor: not-allowed;
}

.chat-empty-state {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    background: #f8f9fa;
}

.empty-content {
    text-align: center;
    color: #65676b;
}

.empty-illustration {
    font-size: 4rem;
    color: #dee2e6;
    margin-bottom: 20px;
}

.empty-content h4 {
    color: #050505;
    font-weight: 600;
    margin-bottom: 8px;
}

.empty-state-small {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #8c939d;
    text-align: center;
    padding: 20px;
}

.empty-circle {
    width: 60px;
    height: 60px;
    background: #f0f2f5;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.5rem;
    margin-bottom: 10px;
    color: #bcc0c4;
}
</style>