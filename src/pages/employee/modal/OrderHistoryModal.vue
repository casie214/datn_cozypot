<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  orderData: Object,
  events: Array
});

const emit = defineEmits(['close']);

// Xử lý hiển thị tên bàn để tránh lỗi "Bàn Bàn S01"
const displayTableName = computed(() => {
  const name = props.orderData?.ban || '---';
  if (name.toLowerCase().includes('bàn')) {
    return name;
  }
  return `Bàn ${name}`;
});

const getBadgeClass = (type) => {
    if (type === 'delete') return 'bg-danger-subtle text-danger border border-danger-subtle';
    if (type === 'add') return 'bg-success-subtle text-success border border-success-subtle';
    if (type === 'create' || type === 'payment') return 'bg-primary-subtle text-primary border border-primary-subtle';
    
    return 'bg-secondary-subtle text-secondary';
};

const getIconBgClass = (type) => {
    if (type === 'delete') return 'bg-danger';
    if (type === 'add') return 'bg-success';
    if (type === 'create' || type === 'payment') return 'bg-primary';
    
    return 'bg-secondary';
};
const getIconClass = (type) => {
    if (type === 'delete') return 'fa-solid fa-trash';
    if (type === 'add') return 'fa-solid fa-plus';
    if (type === 'payment') return 'fa-solid fa-money-bill-wave'; 
    if (type === 'create') return 'fa-solid fa-clipboard-list';
    return 'fa-solid fa-circle-info';
};
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="emit('close')">
    <div class="modal-dialog modal-dialog-centered" style="max-width: 600px; width: 100%;">
      
      <div class="modal-content shadow-lg border-0 overflow-hidden rounded-4">
        
        <div class="modal-header bg-custom-red text-white py-3 px-4">
          <h5 class="modal-title fw-bold" style="font-size: 1.1rem;">
            Lịch sử đơn hàng - {{ displayTableName }}
          </h5>
          <button type="button" class="btn-close btn-close-white" @click="emit('close')"></button>
        </div>

        <div class="modal-body bg-light p-4" style="max-height: 70vh; overflow-y: auto;">
          <div class="timeline">
            
            <div v-for="(event, index) in events" :key="index" class="d-flex gap-3 mb-2 timeline-item">
              
              <div class="d-flex flex-column align-items-center" style="width: 30px;">
                <div 
                  class="rounded-circle d-flex align-items-center justify-content-center text-white shadow-sm flex-shrink-0" 
                  :class="getIconBgClass(event.type)"
                  style="width: 32px; height: 32px; z-index: 2;"
                >
                  <i :class="getIconClass(event.type)" style="font-size: 14px;"></i>
                </div>
                <div v-if="index !== events.length - 1" class="vr opacity-25 my-1" style="width: 2px; height: 100%; background: #ccc;"></div>
              </div>

              <div class="flex-grow-1 pb-3">
                <div class="card border-0 shadow-sm h-100 rounded-3">
                  <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                      <div>
                        <h6 class="fw-bold mb-1 text-dark">{{ event.title }}</h6>
                        <div class="text-muted small">
                          <span class="me-3">🕒 {{ event.time }}</span>
                          <span>👤 <span class="fw-bold text-dark">{{ event.user }}</span></span>
                        </div>
                      </div>
                      
                      <span class="badge rounded-pill" :class="getBadgeClass(event.type)">
                        {{ event.action }}
                      </span>
                    </div>

                    <div v-if="event.detail" class="text-secondary small fst-italic border-top pt-2 mt-2">
                      "{{ event.detail }}"
                    </div>
                    
                    <div class="text-muted mt-2" style="font-size: 0.75rem;">
                      Mã đơn: <strong>{{ event.orderCode || orderData?.id }}</strong>
                    </div>
                  </div>
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
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.5); 
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999; 
  backdrop-filter: blur(2px);
}

.bg-custom-red {
  background-color: #8B0000 !important;
}

.modal-content {
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-20px); }
  to { opacity: 1; transform: translateY(0); }
}

.badge {
  font-weight: 600;
  padding: 6px 12px;
}

.modal-body::-webkit-scrollbar {
  width: 6px;
}
.modal-body::-webkit-scrollbar-track {
  background: #f1f1f1;
}
.modal-body::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}
.modal-body::-webkit-scrollbar-thumb:hover {
  background: #8B0000;
}
</style>