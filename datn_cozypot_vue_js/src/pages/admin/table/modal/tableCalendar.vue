<template>
  <div class="m-4">
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
import axios from "axios";
import dayjs from "dayjs";
import axiosClient from "@/services/axiosClient";

const showModal = ref(false);
const selectedEvent = ref({});

const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: "timeGridWeek",
  locale: "vi",
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
      end: start.add(2, "hour").toDate(),
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

  if (status === 0) color = "#f1c40f";
  if (status === 1) color = "#e67e22";
  if (status === 2) color = "gray";

  info.el.style.backgroundColor = color;
  info.el.style.borderColor = color;
}
</script>


<style scoped>
.calendar-wrapper {
  height: 100vh;
}


.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.modal-box {
  background: white;
  padding: 20px 24px;
  border-radius: 12px;
  width: 360px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.modal-box h3 {
  margin-bottom: 12px;
}

.modal-box p {
  margin: 6px 0;
}

.modal-actions {
  margin-top: 16px;
  text-align: right;
}

.modal-actions button {
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  background: #7d1619;
  color: white;
  cursor: pointer;
}
</style>

