<template>
  <div class="table-card-wrapper">
    <div class="table-responsive">
      <table class="custom-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>MÃ</th>
            <th>HỌ TÊN</th>
            <th>SĐT</th>
            <th>EMAIL</th>
            <th>NGÀY VÀO LÀM</th>
            <th>ĐỊA CHỈ</th>
            <th>GIỚI TÍNH</th>
            <th>TRẠNG THÁI</th>
            <th class="text-center">CHI TIẾT</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(nv, index) in listNhanVien" :key="nv.id">
            <td class="text-center">{{ index + 1 }}</td>
            <td class="fw-bold">{{ nv.maNhanVien }}</td>
            <td class="text-start">{{ nv.hoTenNhanVien }}</td>
            <td>{{ nv.sdtNhanVien }}</td>
            <td class="text-start">{{ nv.email }}</td>
<td>{{ formatDate(nv.ngayVaoLam) }}</td>            <td class="text-start">{{ nv.diaChi }}</td>
            <td>{{ nv.gioiTinh === true ? 'Nam' : 'Nữ' }}</td>
            <td>{{ nv.trangThaiLamViec === 1 ? 'Đang làm việc' : 'Nghỉ việc' }}</td>
            <td>
              <div class="action-group">
                <i class="fas fa-eye action-icon" @click="$emit('view', nv.id)"></i>
                <i class="fas fa-pen action-icon" @click="$emit('edit', nv.id)"></i>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { fetchAllNhanVien } from "@/services/staffService";
const formatDate = (date) => {
  if (!date) return '---';
  return dayjs(date).format('DD/MM/YYYY');
};
const listNhanVien = ref([]);

const handleFetchAllNhanVien = async () => {
    try {
        listNhanVien.value = await fetchAllNhanVien();
    } catch (error) {
        console.log(error);
    }
};
onMounted(handleFetchAllNhanVien);
</script>