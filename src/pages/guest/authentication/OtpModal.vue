<template>
  <div v-if="isOpen" class="otp-overlay">
    <div class="otp-modal">
      <div class="otp-header">
        <i class="fas fa-shield-alt"></i>
        <h3>Xác thực đăng ký</h3>
        <p>Mã OTP đã được gửi đến email:<br><strong>{{ email }}</strong></p>
      </div>

      <div class="otp-inputs">
        <input 
          v-for="(digit, index) in 6" :key="index"
          :ref="el => otpInputs[index] = el"
          v-model="otpArray[index]"
          type="text" maxlength="1"
          @input="handleInput(index)"
          @keydown.backspace="handleBackspace(index)"
          placeholder="-"
        />
      </div>

      <div class="timer-box">
        <p :class="{'timeout': timeLeft <= 60}">
          Mã hết hạn sau: <span>{{ formatTime(timeLeft) }}</span>
        </p>
      </div>

      <div class="modal-actions">
        <button class="btn-verify" @click="onVerify" :disabled="isSubmitting">
          {{ isSubmitting ? 'Đang xác thực...' : 'Xác nhận' }}
        </button>
        
        <div class="resend-text">
          Chưa nhận được mã? 
          <button :disabled="resendDisabled" @click="onResend" class="btn-resend">
            Gửi lại mã {{ resendDisabled ? `(${resendTimer}s)` : '' }}
          </button>
        </div>
      </div>

      <button class="btn-cancel" @click="$emit('cancel')">Hủy và quay lại</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue';

const props = defineProps({
  isOpen: Boolean,
  email: String,
  timeLeft: Number, // Thời gian còn lại từ cha truyền vào
  resendTimer: Number
});

const emit = defineEmits(['verify', 'resend', 'cancel', 'update:timeLeft']);

const otpArray = ref(['', '', '', '', '', '']);
const otpInputs = ref([]);
const isSubmitting = ref(false);
const resendDisabled = ref(true);

// Tự động focus ô đầu tiên khi mở modal
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    setTimeout(() => otpInputs.value[0]?.focus(), 100);
  }
});

// Xử lý khi gõ phím
const handleInput = (index) => {
  // Chỉ cho phép nhập số
  otpArray.value[index] = otpArray.value[index].replace(/[^0-9]/g, '');
  
  if (otpArray.value[index] && index < 5) {
    otpInputs.value[index + 1].focus();
  }
};

const handleBackspace = (index) => {
  if (!otpArray.value[index] && index > 0) {
    otpInputs.value[index - 1].focus();
  }
};

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}:${s < 10 ? '0' : ''}${s}`;
};

const onVerify = () => {
  const code = otpArray.value.join('');
  if (code.length === 6) {
    emit('verify', code);
  } else {
    alert("Vui lòng nhập đủ 6 số");
  }
};

const onResend = () => {
  otpArray.value = ['', '', '', '', '', ''];
  emit('resend');
};
</script>

<style scoped>
.otp-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.6); display: flex; align-items: center; justify-content: center; z-index: 9999;
}
.otp-modal {
  background: white; padding: 40px; border-radius: 15px; width: 450px; text-align: center; box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}
.otp-inputs {
  display: flex; justify-content: center; gap: 10px; margin: 25px 0;
}
.otp-inputs input {
  width: 45px; height: 55px; text-align: center; font-size: 24px; font-weight: bold;
  border: 2px solid #ddd; border-radius: 8px; transition: all 0.3s;
}
.otp-inputs input:focus { border-color: #ff4757; outline: none; box-shadow: 0 0 8px rgba(255, 71, 87, 0.3); }
.timer-box { margin-bottom: 20px; font-size: 15px; color: #666; }
.timeout { color: #ff4757; font-weight: bold; }
.btn-verify {
  width: 100%; background: #2f3542; color: white; border: none; padding: 12px; border-radius: 8px; font-weight: bold; cursor: pointer;
}
.btn-resend {
  background: none; border: none; color: #1e90ff; cursor: pointer; text-decoration: underline; padding: 0;
}
.btn-resend:disabled { color: #ccc; cursor: not-allowed; text-decoration: none; }
.btn-cancel {
  margin-top: 20px; background: none; border: none; color: #888; cursor: pointer; font-size: 14px;
}
</style>