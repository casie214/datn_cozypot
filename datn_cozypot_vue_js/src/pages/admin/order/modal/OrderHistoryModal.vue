<script setup>
import { defineProps, defineEmits, computed } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  orderData: Object,
  events: Array
});

const emit = defineEmits(['close']);

const displayTableName = computed(() => {
  const name = props.orderData?.ban || '---';
  return name.toLowerCase().includes('bàn') ? name : `Bàn ${name}`;
});

const getBadgeClass = (type) => {
    const map = {
        'delete': 'bg-danger-subtle text-danger border-danger-subtle',
        'add': 'bg-success-subtle text-success border-success-subtle',
        'create': 'bg-primary-subtle text-primary border-primary-subtle',
        'payment': 'bg-primary-subtle text-primary border-primary-subtle'
    };
    return (map[type] || 'bg-secondary-subtle text-secondary border-secondary-subtle') + ' border';
};

const getIconBgClass = (type) => {
    const map = {
        'delete': 'bg-danger',
        'add': 'bg-success',
        'create': 'bg-primary',
        'payment': 'bg-primary'
    };
    return map[type] || 'bg-secondary';
};

const getIconClass = (type) => {
    const map = {
        'delete': 'fa-trash',
        'add': 'fa-plus',
        'payment': 'fa-money-bill-wave',
        'create': 'fa-clipboard-list'
    };
    return `fa-solid ${map[type] || 'fa-circle-info'}`;
};
</script>

<template>
  <div v-if="isOpen" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);" @click.self="emit('close')">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable" style="max-width: 600px;">
      
      <div class="modal-content shadow border-0 rounded-4">
        
        <div class="modal-header text-white border-0" style="background-color: #8B0000;">
          <h5 class="modal-title fw-bold fs-5">
            <i class="fa-solid fa-clock-rotate-left me-2"></i>
            Lịch sử đơn hàng - {{ displayTableName }}
          </h5>
          <button type="button" class="btn-close btn-close-white" @click="emit('close')"></button>
        </div>

        <div class="modal-body bg-light p-4 custom-scrollbar">
          
          <div v-if="events && events.length > 0">
            <div v-for="(event, index) in events" :key="index" class="d-flex gap-3 position-relative">
              
              <div class="d-flex flex-column align-items-center" style="width: 40px;">
                <div 
                  class="rounded-circle d-flex align-items-center justify-content-center text-white shadow-sm" 
                  :class="getIconBgClass(event.type)"
                  style="width: 36px; height: 36px; z-index: 2;"
                >
                  <i :class="getIconClass(event.type)" class="small"></i>
                </div>
                <div v-if="index !== events.length - 1" class="vr opacity-25 my-1" style="width: 2px; height: 100%; min-height: 40px; background-color: #6c757d;"></div>
              </div>

              <div class="flex-grow-1 pb-4">
                <div class="card border-0 shadow-sm rounded-3">
                  <div class="card-body p-3">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                      <div>
                        <h6 class="fw-bold mb-1 text-dark">{{ event.title }}</h6>
                        <div class="text-muted" style="font-size: 0.8rem;">
                          <span class="me-3"><i class="fa-regular fa-clock me-1"></i>{{ event.time }}</span>
                          <span><i class="fa-regular fa-user me-1"></i><span class="fw-semibold text-dark">{{ event.user }}</span></span>
                        </div>
                      </div>
                      <span class="badge rounded-pill" :class="getBadgeClass(event.type)">
                        {{ event.action }}
                      </span>
                    </div>

                    <div v-if="event.detail" class="bg-light rounded p-2 text-secondary small fst-italic border-start border-3 border-secondary">
                      "{{ event.detail }}"
                    </div>
                    
                    <div class="text-end text-muted mt-2" style="font-size: 0.7rem;">
                      ID: {{ event.orderCode || orderData?.id }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="text-center text-muted py-5">
            Chưa có lịch sử ghi nhận.
          </div>

        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: #f8f9fa;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #ced4da;
  border-radius: 3px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #adb5bd;
}

.modal.show .modal-dialog {
  animation: slideIn 0.3s ease-out;
}
@keyframes slideIn {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>