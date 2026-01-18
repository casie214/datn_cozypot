<script setup>
import Sidebar from "../../../components/sidebar.vue";
import { useOrderManager } from "./orderFunction";
import { useRouter } from "vue-router";
const router = useRouter();

import OrderHistoryModal from "../modal/OrderHistoryModal.vue";

const {
  filters,
  orderList,
  handleSearch,
  handleReset,
  handleViewHistory,
  handlePrintOrder,

  isHistoryModalOpen,
  selectedHistoryOrder,
  historyEvents,
  closeHistoryModal,
  currentVAT,
} = useOrderManager();
</script>

<template>
  <div class="app-layout">
    <Sidebar />

    <main class="main-content">
      <h1 class="page-title">Quản lý đơn hàng</h1>

      <div class="filter-card">
        <div class="filter-inputs">
          <div class="filter-group">
            <label>Tìm kiếm</label>
            <input
              type="text"
              v-model="filters.search"
              class="form-control search-input"
              placeholder="Mã ĐH, tên KH, SĐT"
            />
          </div>

          <div class="filter-group">
            <label>Trạng thái</label>
            <select v-model="filters.status" class="form-control">
              <option>Tất cả</option>
              <option>Đã xác nhận</option>
              <option>Hoàn thành</option>
              <option>Đã hủy</option>
            </select>
          </div>

          <div class="filter-group">
            <label>Từ ngày</label>
            <input
              type="date"
              v-model="filters.fromDate"
              class="form-control"
              placeholder="dd/mm/yyyy"
            />
          </div>

          <div class="filter-group">
            <label>Đến ngày</label>
            <input
              type="date"
              v-model="filters.toDate"
              class="form-control"
              placeholder="dd/mm/yyyy"
            />
          </div>
        </div>

        <div class="filter-actions">
          <button class="btn-search" @click="handleSearch">🔍 Tìm kiếm</button>
          <button class="btn-cancel" @click="handleReset">Hủy</button>
        </div>
      </div>

      <div class="table-card">
        <div class="table-responsive">
          <table>
            <thead>
              <tr>
                <th>STT</th>
                <th>MÃ ĐƠN</th>
                <th>KHÁCH HÀNG</th>
                <th>SĐT</th>
                <th>BÀN</th>
                <th>LOẠI</th>
                <th>TỔNG TIỀN</th>
                <th>TRẠNG THÁI</th>
                <th>THAO TÁC</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(order, index) in orderList" :key="order.id">
                <td><b>{{ index + 1 }}</b></td>
                <td>{{ order.id }}</td>
                <td>{{ order.khachHang }}</td>
                <td>{{ order.sdt }}</td>
                <td>{{ order.ban }}</td>
                <td>{{ order.loai }}</td>
                <td>{{ order.tongTien }}</td>
                <td>{{ order.trangThai }}</td>
                <td class="action-icons">
                  <button
                    class="icon-btn"
                    title="Xem chi tiết"
                    @click="
                      router.push({
                        name: 'OrderDetail',
                        params: { id: order.dbId },
                      })
                    "
                  >
                    👁️
                  </button>

                  <button
                    class="icon-btn"
                    title="Lịch sử"
                    @click="handleViewHistory(order.id)"
                  >
                    🕒
                  </button>

                  <button
                    class="icon-btn"
                    title="In hóa đơn"
                    @click="handlePrintOrder(order.id)"
                  >
                    🖨️
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="pagination">
          <button class="page-btn prev">&lt;</button>
          <button class="page-btn active">1</button>
          <button class="page-btn">2</button>
          <button class="page-btn dot">...</button>
          <button class="page-btn">7</button>
          <button class="page-btn">8</button>
          <button class="page-btn next">&gt;</button>
        </div>
      </div>
    </main>

    <OrderHistoryModal
      :isOpen="isHistoryModalOpen"
      :orderData="selectedHistoryOrder"
      :events="historyEvents"
      @close="closeHistoryModal"
    />
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  background-color: #f8f9fa;
}

.main-content {
  margin-left: 250px;
  width: calc(100% - 250px);
  padding: 20px 30px;
  background-color: #fff;
}

.page-title {
  color: #8b0000;
  font-size: 24px;
  margin-bottom: 20px;
  font-weight: bold;
}

.filter-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 15px;
}

.filter-inputs {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.filter-group label {
  font-size: 13px;
  color: #666;
}

.form-control {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
  outline: none;
}

.form-control:focus {
  border-color: #8b0000;
}

.filter-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.btn-search {
  background-color: #720e1e;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
  justify-content: center;
}

.btn-cancel {
  background-color: #8b0000;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
}

.btn-search:hover,
.btn-cancel:hover {
  opacity: 0.9;
}

.table-card {
  border: 1px solid #ccc;
  border-radius: 8px;
  overflow: hidden;
}

.table-responsive {
  width: 100%;
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: white;
}

th {
  background-color: #8b0000;
  color: white;
  padding: 12px 15px;
  text-align: left;
  font-size: 13px;
  text-transform: uppercase;
  font-weight: 600;
}

td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  color: #333;
  font-size: 14px;
  vertical-align: middle;
}

tbody tr:hover {
  background-color: #f9f9f9;
}

.action-icons {
  display: flex;
  gap: 10px;
}

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.2s;
}

.icon-btn:hover {
  background-color: #eee;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 5px;
  padding: 15px;
  background: white;
}

.page-btn {
  width: 35px;
  height: 35px;
  border: 1px solid #ddd;
  background: white;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-btn:hover {
  background-color: #f0f0f0;
}

.page-btn.active {
  background-color: #8b0000;
  color: white;
  border-color: #8b0000;
}

.page-btn.dot {
  border: none;
  cursor: default;
}

.search-input {
  min-width: 450px;
}
</style>
