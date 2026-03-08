<template>
  <div>
    <FullCalendar :options="calendarOptions"/>
  </div>

  <!-- MODAL -->
    <div v-if="showModal" class="modal-backdrop" @click.self="closeModal">
      <div class="modal-box">
        <h3>📋 Thông tin đặt bàn</h3>

        <p><b>Khách:</b> {{ selectedEvent.tenKhachHang }}</p>
        <p><b>SĐT:</b> {{ selectedEvent.soDienThoai }}</p>
        <p><b>Số khách:</b> {{ selectedEvent.soLuongKhach }}</p>
        <p><b>Bàn:</b> {{ selectedEvent.tenBan }}</p>
        <p><b>Tầng:</b> {{ selectedEvent.soTang }}</p>
        <p><b>Trạng thái:</b> {{ selectedEvent.trangThai }}</p>

        <div class="modal-actions">
          <button @click="closeModal">Đóng</button>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref } from "vue";
import FullCalendar from "@fullcalendar/vue3";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import viLocale from "@fullcalendar/core/locales/vi"; // Bổ sung import tiếng Việt
import axios from "axios";
import dayjs from "dayjs";
import axiosClient from "@/services/axiosClient";

const showModal = ref(false);
const selectedEvent = ref({});

const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: "timeGridWeek",
  locale: viLocale,
  
  /* --- CẤU HÌNH CHIỀU CAO & DÃN DÒNG (FIX LỖI DÀI NGOẰNG) --- */
  height: "auto",          // Chiều cao tự động theo nội dung, không kéo dãn
  expandRows: false,       // Không cho phép các hàng tự dãn để lấp đầy màn hình
  slotDuration: "00:30:00", // Chia nhỏ mỗi ô là 30 phút để dễ nhìn hơn
  slotLabelInterval: "01:00", // Nhưng chỉ hiện nhãn giờ mỗi 1 tiếng
  
  /* --- CẤU HÌNH KHUNG GIỜ --- */
  slotMinTime: "07:00:00",
  slotMaxTime: "22:00:00",
  allDaySlot: false,

  /* --- ĐỊNH DẠNG HIỂN THỊ GIỜ TRÊN TRỤC DỌC --- */
  slotLabelFormat: {
    hour: '2-digit',
    minute: '2-digit',
    omitZeroMinute: false,
    meridiem: false
  },

  buttonText: {
    today: 'Hôm nay', month: 'Tháng', week: 'Tuần', day: 'Ngày'
  },
  headerToolbar: {
    left: "prev,next today",
    center: "title",
    right: "dayGridMonth,timeGridWeek,timeGridDay",
  },
  events: fetchEvents,
  eventClick: handleEventClick,
  eventDidMount: styleEvent,
});

async function fetchEvents(fetchInfo, successCallback) {
  const res = await axiosClient.get("http://localhost:8080/api/dat-ban/danh-sach");

  const events = res.data.map(item => {
    const start = dayjs(item.thoiGianDat);
    return {
      title: `Bàn ${item.maBan} - ${item.tenKhachHang}`,
      start: start.toDate(),
      end: start.add(2, "hour").toDate(), // Giả sử mỗi ca đặt 2 tiếng
      extendedProps: {
        tenKhachHang: item.tenKhachHang,
        soDienThoai: item.soDienThoai,
        soLuongKhach: item.soLuongKhach,
        tenBan: item.tenBan,
        soTang: item.soTang,
        trangThai: item.trangThai
      }
    };
  });

  successCallback(events);
}

function handleEventClick(info) {
  selectedEvent.value = info.event.extendedProps;
  showModal.value = true;
}

function closeModal() {
  showModal.value = false;
}

function styleEvent(info) {
  const status = info.event.extendedProps.trangThai;
  let color = "#999";

  if (status === 0) color = "#f1c40f"; // Vàng
  if (status === 1) color = "#e67e22"; // Cam
  if (status === 2) color = "gray";    // Xám

  info.el.style.backgroundColor = color;
  info.el.style.borderColor = color;
}
</script>


<style scoped>
/* Bo khung lịch cho chuyên nghiệp */
:deep(.fc) {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.05);
}

/* ÉP CHIỀU CAO CỐ ĐỊNH CHO MỖI Ô GIỜ (Fix lỗi dài ngoằng) */
:deep(.fc-timegrid-slot) {
  height: 50px !important; /* Bạn có thể tăng giảm con số này theo ý muốn */
  border-bottom: 1px solid #f0f0f0 !important;
}

/* Tinh chỉnh màu tiêu đề cột (Thứ 2, Thứ 3...) */
:deep(.fc-col-header-cell) {
  background: #f8f9fa;
  padding: 12px 0 !important;
  font-weight: 600;
  color: #444;
}

/* Tinh chỉnh Style cho các Event (Ô đặt bàn) */
:deep(.fc-v-event) {
  border-radius: 6px !important;
  border: none !important;
  padding: 4px !important;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

/* Modal Style */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  backdrop-filter: blur(4px); /* Làm mờ nền phía sau */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 99999;
}

.modal-box {
  background: white;
  padding: 28px;
  border-radius: 16px;
  width: 400px;
  border-top: 6px solid #7d1619; /* Nhấn nhá màu thương hiệu */
  box-shadow: 0 20px 40px rgba(0,0,0,0.2);
}

.modal-box h3 {
  color: #7d1619;
  font-weight: 800;
  margin-bottom: 20px;
  text-align: center;
}

.modal-box p {
  font-size: 15px;
  padding: 8px 0;
  border-bottom: 1px dashed #eee;
  display: flex;
  justify-content: space-between;
}

.modal-box p b {
  color: #666;
}

.modal-actions {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.modal-actions button {
  padding: 10px 30px;
  border-radius: 8px;
  border: none;
  background: #7d1619;
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-actions button:hover {
  background: #5a1012;
  transform: translateY(-2px);
}
</style>

