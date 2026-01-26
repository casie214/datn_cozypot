<script setup>
import { computed } from 'vue';

const props = defineProps({
  show: Boolean,
  type: { type: String, default: 'alert' }, // 'alert' hoặc 'confirm'
  variant: { type: String, default: 'warning' }, // 'success', 'warning', 'error'
  title: String,
  message: String,
});

const emit = defineEmits(['close', 'confirm']);

// Icon động dựa trên variant
const icon = computed(() => {
  switch (props.variant) {
    case 'success': return '✅';
    case 'error': return '❌';
    case 'warning': return '⚠️';
    default: return 'ℹ️';
  }
});

// Màu tiêu đề động
const titleColor = computed(() => {
  switch (props.variant) {
    case 'success': return '#2e7d32';
    case 'error': return '#d32f2f';
    case 'warning': return '#ed6c02';
    default: return '#333';
  }
});
</script>

<template>
  <Transition name="fade">
    <div v-if="show" class="dialog-overlay" @click.self="type === 'alert' ? emit('close') : null">
      <div class="dialog-content">
        <div class="dialog-header">
          <span class="dialog-icon">{{ icon }}</span>
          <h3 :style="{ color: titleColor }">{{ title }}</h3>
        </div>
        
        <div class="dialog-body">
          <p>{{ message }}</p>
        </div>

        <div class="dialog-footer">
          <button v-if="type === 'confirm'" class="btn-cancel" @click="$emit('close')">
            Hủy bỏ
          </button>
          
          <button class="btn-confirm" 
            :class="variant"
            @click="type === 'confirm' ? $emit('confirm') : $emit('close')">
            {{ type === 'confirm' ? 'Đồng ý' : 'Đóng' }}
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped src="../assets/base.css">

@keyframes slideIn {
  from { transform: translateY(-20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>