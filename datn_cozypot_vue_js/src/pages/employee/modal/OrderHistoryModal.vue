<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  orderData: Object,
  events: Array
});

const emit = defineEmits(['close']);

// 1. Class cho Badge (Nút trạng thái bên phải)
const getBadgeClass = (type) => {
    if (type === 'delete') return 'badge-delete';
    if (type === 'add') return 'badge-add';
    if (type === 'create') return 'badge-create';
    return '';
};

// 2. Class cho nền Icon tròn bên trái
const getIconBgClass = (type) => {
    if (type === 'delete') return 'bg-delete';
    if (type === 'add') return 'bg-add';
    if (type === 'create') return 'bg-create';
    return 'bg-default';
};

// 3. Chọn Icon FontAwesome
const getIconClass = (type) => {
    if (type === 'delete') return 'fa-solid fa-trash';
    if (type === 'add') return 'fa-solid fa-plus';
    if (type === 'create') return 'fa-solid fa-circle'; // Hoặc fa-pen
    return 'fa-solid fa-circle';
};
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      
      <div class="modal-header">
        <h2>Lịch sử đơn hàng - Bàn {{ orderData?.ban || '---' }}</h2>
        <button class="btn-close" @click="emit('close')">✖</button>
      </div>

      <div class="modal-body">
        <div class="timeline">
            
          <div v-for="(event, index) in events" :key="index" class="timeline-item">
            
            <div class="timeline-left">
                <div class="icon-circle" :class="getIconBgClass(event.type)">
                    <i :class="getIconClass(event.type)" style="font-size: 12px;"></i>
                </div>
                <div v-if="index !== events.length - 1" class="line"></div>
            </div>

            <div class="timeline-content">
                <div class="history-card">
                    
                    <div class="card-header">
                        <div class="header-info">
                            <h3>{{ event.title }}</h3>
                            <div class="meta-row">
                                <span class="time">🕒 {{ event.time }}</span>
                                <span class="user">👤 {{ event.user }}</span>
                            </div>
                        </div>
                        
                        <span class="badge" :class="getBadgeClass(event.type)">
                            {{ event.action }}
                        </span>
                    </div>

                    <div class="card-footer">
                        Đơn hàng: {{ event.orderCode }}
                    </div>

                </div>
            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
/* --- MODAL LAYOUT --- */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.6); /* Nền tối hơn chút */
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  width: 600px; /* Rộng vừa phải như ảnh */
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  animation: slideIn 0.2s ease-out;
}


.modal-header {
  background-color: #8B0000; 
  color: white;
  padding: 15px 20px;
  display: flex; justify-content: space-between; align-items: center;
}
.modal-header h2 { margin: 0; font-size: 16px; font-weight: 600; }
.btn-close { background: none; border: none; color: #a0aec0; font-size: 20px; cursor: pointer; }
.btn-close:hover { color: white; }

.modal-body { 
    padding: 30px 25px; 
    background-color: #f7fafc; 
    max-height: 70vh; 
    overflow-y: auto; 
}


.timeline { display: flex; flex-direction: column; }
.timeline-item { display: flex; gap: 20px; min-height: 100px; }

.timeline-left { 
    display: flex; flex-direction: column; align-items: center; width: 30px; 
}

.icon-circle {
    width: 24px; height: 24px; 
    border-radius: 50%; 
    display: flex; align-items: center; justify-content: center;
    color: white; 
    z-index: 2;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.line {
    flex: 1; width: 2px; background: #e2e8f0; margin-top: 5px; margin-bottom: 5px;
}

.timeline-content { flex: 1; padding-bottom: 20px; }


.bg-delete { background-color: #e53e3e; } 
.bg-add { background-color: #48bb78; }    
.bg-create { background-color: #4299e1; } 
.bg-default { background-color: #cbd5e0; }


.history-card {
    background: white;
    border-radius: 6px;
    padding: 15px;
    box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    border: 1px solid #edf2f7;
}

.card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 5px; }
.header-info h3 { margin: 0 0 5px 0; font-size: 14px; color: #2d3748; font-weight: 700; }

.meta-row { display: flex; gap: 15px; font-size: 12px; color: #718096; }

.card-footer {
    margin-top: 10px; font-size: 11px; color: #a0aec0;
}

.divider { height: 1px; background: #edf2f7; margin: 10px 0; }
.detail-item { font-size: 13px; color: #4a5568; font-weight: 500; }


.badge { 
    padding: 4px 10px; 
    border-radius: 12px; 
    font-size: 11px; 
    font-weight: 700; 
}


.badge-delete { 
    background-color: #fff5f5; 
    color: #e53e3e; 
}


.badge-add { 
    background-color: #f0fff4; 
    color: #38a169; 
}


.badge-create { 
    background-color: #ebf8ff; 
    color: #3182ce; 
}

@keyframes slideIn {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>