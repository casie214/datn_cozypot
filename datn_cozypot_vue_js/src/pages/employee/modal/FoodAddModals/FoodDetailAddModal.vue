<script setup>
import { ref, defineProps, defineEmits, onMounted } from 'vue';
import axios from 'axios';
import { useFoodAddModal, useFoodDetailAddModal } from '../../../../services/foodFunction';

const props = defineProps({
  isOpen: Boolean,
  foodItem: Object
});

const emit = defineEmits(['close', 'refresh']);

const {
  formData,
  listDanhMuc,
  isParentLocked,
  handleSave
} = useFoodDetailAddModal(props, emit);

</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <button class="btn-back" @click="$emit('close')">←</button>
        <h2>Thêm Chi Tiết Mới</h2>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div class="modal-body">
        <div class="form-container">
          <div class="form-group">
            <label>Tên chi tiết</label>
            <input v-model="formData.tenChiTietMonAn" type="text" placeholder="VD: Size L">
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Giá bán</label>
              <input v-model="formData.giaBan" type="number">
              <label>Giá vốn</label>
              <input v-model="formData.giaVon" type="number">

              <label>Món ăn
                <span class="required">*</span>
              </label>
              <select :disabled="isParentLocked" :class="{ 'locked-field': isParentLocked }" v-model="formData.idMonAnDiKem" class="form-control">
                <option value="">-- Chọn món ăn --</option>
                <option v-for="item in listDanhMuc" :key="item.id" :value="item.id">
                  {{ item.tenMonAn || item.ten }}
                </option>
              </select>
              <small v-if="isParentLocked" style="color: #666; font-style: italic;">
                *Đang thêm chi tiết cho món: {{ parentFood?.tenMonAn }}
              </small>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>Kích cỡ</label>
              <input v-model="formData.kichCo" type="text">
            </div>
            <div class="form-group">
              <label>Đơn vị</label>
              <input v-model="formData.donVi" type="text">
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn-cancel" @click="$emit('close')">Hủy</button>
        <button class="btn-confirm" @click="handleSave">Lưu lại</button>
      </div>
    </div>
  </div>
</template>

<style scoped src="../../modal/foodModalManager.css"></style>