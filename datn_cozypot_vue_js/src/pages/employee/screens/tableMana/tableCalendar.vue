<template>
  <div class="layout-table m-4">
    <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem;">
      Lịch đặt bàn
    </h3>

    <FullCalendar :options="calendarOptions" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import FullCalendar from '@fullcalendar/vue3'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import axios from 'axios'

const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'timeGridWeek',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'dayGridMonth,timeGridWeek,timeGridDay'
  },
  locale: 'vi',
  events: fetchEvents,
  eventClick: handleEventClick,
  eventDidMount: styleEvent
})


async function fetchEvents(fetchInfo, successCallback) {
  try {
    const res = await axios.get('http://localhost:8080/api/phieu-dat-ban/calendar')
    successCallback(res.data)
  } catch (err) {
    console.error('Lỗi load lịch:', err)
  }
}

function handleEventClick(info) {
  const data = info.event.extendedProps
  alert(
    `Khách: ${data.tenKhachHang}
SĐT: ${data.soDienThoai}
Số khách: ${data.soLuongKhach}
Trạng thái: ${data.tenTrangThai}`
  )
}

function styleEvent(info) {
  const status = info.event.extendedProps.trangThai
  let color = '#999'

  if (status === 0) color = '#f39c12' // chờ
  if (status === 1) color = '#2ecc71' // xác nhận
  if (status === 2) color = '#e74c3c' // huỷ

  info.el.style.backgroundColor = color
  info.el.style.borderColor = color
}
</script>

<style scoped>
.fc {
  background: white;
  padding: 10px;
  border-radius: 10px;
}
</style>
