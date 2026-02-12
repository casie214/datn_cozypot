<template>
    <div class="main-content" style="padding: 25px;">

        <div v-if="loading" class="skeleton-box"></div>

        <div v-else>

            <!-- Title -->
            <h4 style="color: rgb(125, 22, 26);font-weight: bold;">Báo cáo thống kê</h4>
            <p class="sub">Xem báo cáo doanh thu và bán hàng</p>

            <!-- Lọc nhanh -->
            <div class="quick-filter">

                <button v-for="item in quickList" :key="item.key" :class="{ active: quick === item.key }"
                    @click="setQuick(item.key)">
                    {{ item.name }}
                </button>

            </div>

            <!-- Chọn ngày -->
            <div class="filter-box">

                <div>
                    <label>Từ ngày</label>
                    <input type="date" v-model="from" :disabled="quick !== 'custom'" />
                </div>

                <div>
                    <label>Đến ngày</label>
                    <input type="date" v-model="to" :disabled="quick !== 'custom'" />
                </div>

                <button class="btn-primary" @click="loadData" :disabled="quick === 'custom' && (!from || !to)">
                    Tạo báo cáo
                </button>
                <button class="btn-primary" @click="exportExcel">
                    Xuất Excel
                </button>

            </div>

            <!-- Thống kê -->
            <div class="summary">

                <div class="card blue">
                    <p>Tổng doanh thu</p>
                    <h3>{{ money(tong.tongDoanhThu) }}</h3>
                </div>

                <div class="card purple">
                    <p>Tổng đã giảm</p>
                    <h3>{{ money(tong.tongGiam) }}</h3>
                </div>

                <div class="card green">
                    <p>Số đơn hàng</p>
                    <h3>{{ tong.tongDon }}</h3>
                </div>

                <div class="card orange">
                    <p>Doanh thu TB</p>
                    <h3>{{ money(tong.doanhThuTrungBinh) }}</h3>
                </div>

                <div class="card teal">
                    <p>Online</p>
                    <h3>{{ money(online) }}</h3>
                </div>

                <div class="card red">
                    <p>Offline</p>
                    <h3>{{ money(offline) }}</h3>
                </div>

            </div>
            <!-- Biểu đồ
            <div class="chart-header">

                <h4>Biểu đồ doanh thu</h4>

                <button class="btn-toggle" @click="showChart = !showChart">
                    {{ showChart ? '⬇ Thu gọn' : '⬆ Mở rộng' }}
                </button>

            </div> -->

            <!-- <div v-show="showChart" class="chart-box">
                <canvas id="revenueChart"></canvas>
            </div> -->


            <!-- Chi tiết -->
            <h4 class="mt">Chi tiết</h4>

            <table>

                <thead>
                    <tr>
                        <th>Ngày</th>
                        <th>Số đơn</th>
                        <th>Doanh thu</th>
                        <th>Đã giảm</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>

                <tbody>

                    <tr v-for="(i, index) in theoNgay" :key="index">

                        <td>{{ i.ngay }}</td>
                        <td>{{ i.soDon }}</td>
                        <td class="money">{{ money(i.doanhThu) }}</td>
                        <td class="money">{{ money(i.daGiam) }}</td>

                        <td>
                            <a href="#" @click.prevent="openModal(i)">Xem đơn</a>
                        </td>


                    </tr>

                </tbody>

            </table>
        </div>

    </div><!-- Modal -->
    <div v-if="showModal" class="modal-mask">

        <div class="modal-box">

            <h3>Chi tiết đơn</h3>

            <p>Ngày: {{ selectedOrder?.ngay }}</p>
            <p>Số đơn: {{ selectedOrder?.soDon }}</p>
            <p>Doanh thu: {{ money(selectedOrder?.doanhThu) }}</p>

            <button class="btn-primary" @click="showModal = false">
                Đóng
            </button>

        </div>

    </div>

</template>

<script setup>
import { ref, onMounted, computed, nextTick } from "vue";

import {
    thongKeTong,
    thongKeThanhToan,
    thongKeNgay
} from "@/services/thongKeService";

/* Date */
const from = ref("");
const to = ref("");
const loading = ref(false);
const showChart = ref(true);

/* Data */
const tong = ref({});
const theoNgay = ref([]);
const thanhToan = ref([]);

const online = ref(0);
const offline = ref(0);
const tongThangNay = ref(0);
const tongThangTruoc = ref(0);

/* Quick */
const quick = ref("today");

const quickList = [
    { key: "today", name: "Hôm nay" },
    { key: "7", name: "7 ngày qua" },
    { key: "30", name: "30 ngày qua" },
    { key: "month", name: "Tháng này" },
    { key: "year", name: "Năm nay" },
    { key: "custom", name: "Tùy chọn" } // ADD

];

/* Money */
const money = (v) => {
    if (!v) return "0 đ";
    return Number(v).toLocaleString("vi-VN") + " đ";
};

/* Quick filter */
const setQuick = (key) => {

    quick.value = key;

    // Nếu là custom → cho nhập tay
    if (key === "custom") {
        from.value = "";
        to.value = "";
        return;
    }

    const today = new Date();
    let start = new Date();

    if (key === "today") {
        start = today;
    }

    if (key === "7") {
        start.setDate(today.getDate() - 7);
    }

    if (key === "30") {
        start.setDate(today.getDate() - 30);
    }

    if (key === "month") {
        start = new Date(today.getFullYear(), today.getMonth(), 1);
    }

    if (key === "year") {
        start = new Date(today.getFullYear(), 0, 1);
    }

    from.value = start.toISOString().slice(0, 10);
    to.value = today.toISOString().slice(0, 10);

    loadData();
};


/* Load API */
const loadData = async () => {
    loading.value = true;

    try {

        const [r1, r2, r3] = await Promise.all([
            thongKeTong(from.value, to.value),
            thongKeThanhToan(from.value, to.value),
            thongKeNgay(from.value, to.value)
        ]);

        console.log("yo: ", r3)

        tong.value = r1.data;
        thanhToan.value = r2.data;

        theoNgay.value = r3.data || [];


        online.value = 0;
        offline.value = 0;

        thanhToan.value.forEach(i => {
            if (i.hinhThuc === "Online") online.value = i.tongTien;
            if (i.hinhThuc === "Offline") offline.value = i.tongTien;
        });

    } catch (e) {
        console.error(e);
        alert("Lỗi load thống kê!");

    } finally {

        loading.value = false; // TẮT loading TRƯỚC

        await nextTick(); // ĐỢI DOM render xong

        renderChart(theoNgay.value); // VẼ SAU CÙNG
    }
};


import { Chart } from 'chart.js/auto';

let chartInstance = null;

function renderChart(data) {
    const ctx = document.getElementById('revenueChart');
    if (!ctx) return;

    if (chartInstance) chartInstance.destroy();

    chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.map(i => i.ngay),
            datasets: [
                {
                    label: 'Doanh thu',
                    data: data.map(i => i.doanhThu),
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false
        }
    });
}

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

function exportExcel() {
    const ws = XLSX.utils.json_to_sheet(theoNgay.value);
    const wb = XLSX.utils.book_new();

    XLSX.utils.book_append_sheet(wb, ws, 'ThongKe');

    const buf = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });

    saveAs(
        new Blob([buf]),
        'thong-ke-doanh-thu.xlsx'
    );
}
const now = new Date();

const thisMonth = `${now.getFullYear()}-${now.getMonth() + 1}-01`;

const lastMonth = `${now.getFullYear()}-${now.getMonth()}`;

const compare = computed(() => {
    return tongThangNay.value - tongThangTruoc.value;
});

const showModal = ref(false);
const selectedOrder = ref(null);

function openModal(order) {
    selectedOrder.value = order;
    showModal.value = true;
}


/* Init */
onMounted(() => {
    setQuick("today");
});
</script>
<style>
/* ================= ROOT ================= */
:root {
    --main-red: #8b0000;
    --main-red-light: #b91c1c;
    --bg-light: #f9fafb;
    --text-dark: #1f2937;
}

.skeleton-box {
    height: 200px;
    background: linear-gradient(90deg, #eee, #ddd, #eee);
    animation: skeleton 1.5s infinite;
}

@keyframes skeleton {
    0% {
        background-position: 0%
    }

    100% {
        background-position: 100%
    }
}

.modal-mask {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, .5);
}

.modal-box {
    background: white;
    width: 400px;
    margin: 10% auto;
    padding: 20px;
    border-radius: 10px;
}

/* ================= CONTAINER ================= */
.container {
    padding: 30px;
    background: var(--bg-light);
    min-height: 100vh;
    font-family: "Segoe UI", sans-serif;
    color: var(--text-dark);
}

h2 {
    color: var(--main-red);
    font-weight: 700;
}

input:disabled {
    background: #f3f4f6;
    cursor: not-allowed;
    opacity: 0.7;
}

button:disabled {
    opacity: 0.6;
    cursor: not-allowed;
}

.sub {
    color: #6b7280;
    margin-bottom: 25px;
}

/* ================= QUICK FILTER ================= */
.quick-filter {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    flex-wrap: wrap;
}

.quick-filter button {
    border: 1px solid var(--main-red);
    padding: 7px 16px;
    background: white;
    border-radius: 20px;
    cursor: pointer;
    color: var(--main-red);
    font-weight: 500;
    transition: all 0.25s;
}

.quick-filter button:hover {
    background: var(--main-red-light);
    color: white;
}

.quick-filter .active {
    background: var(--main-red);
    color: white;
}

/* ================= FILTER ================= */
.filter-box {
    display: flex;
    gap: 20px;
    background: white;
    padding: 18px 20px;
    border-radius: 12px;
    align-items: flex-end;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.filter-box label {
    font-size: 13px;
    color: #555;
    margin-bottom: 4px;
    display: block;
}

.filter-box input {
    padding: 7px 10px;
    border: 1px solid #ddd;
    border-radius: 6px;
    min-width: 350px;
}

.filter-box input:focus {
    outline: none;
    border-color: var(--main-red);
}

/* ================= BUTTON ================= */
.btn-primary {
    background: var(--main-red);
    color: white;
    border: none;
    padding: 9px 24px;
    border-radius: 10px;
    cursor: pointer;
    font-weight: 600;
    transition: all 0.25s;
}

.btn-primary:hover {
    background: var(--main-red-light);
    transform: translateY(-1px);
}

/* ================= SUMMARY ================= */
.summary {
    margin: 30px 0;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 18px;
}

/* ================= CARD ================= */
.card {
    padding: 18px;
    background: white;
    border-radius: 14px;
    box-shadow: 0 3px 8px rgba(0, 0, 0, 0.06);
    border-top: 4px solid var(--main-red);
    transition: all 0.25s;
}

.card:hover {
    transform: translateY(-3px);
}

.card p {
    font-size: 13px;
    color: #6b7280;
}

.card h3 {
    margin-top: 6px;
    color: var(--main-red);
    font-size: 20px;
    font-weight: 700;
}

/* Bỏ màu cũ */
.blue,
.purple,
.green,
.orange,
.teal,
.red {
    background: white !important;
}

/* ================= TABLE ================= */
.mt {
    margin: 25px 0 12px;
    color: var(--main-red);
}

table {
    width: 100%;
    background: white;
    border-radius: 12px;
    overflow: hidden;
    border-collapse: collapse;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.chart-box {
    background: white;
    padding: 20px;
    border-radius: 12px;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
    margin-bottom: 30px;
    height: 350px;
}

.chart-box canvas {
    height: 100% !important;
}

th,
td {
    padding: 12px 10px;
    text-align: center;
}

th {
    background: var(--main-red);
    color: white;
    font-weight: 600;
}

tbody tr:nth-child(even) {
    background: #f9fafb;
}

tbody tr:hover {
    background: #fee2e2;
}

/* ================= MONEY ================= */
.money {
    font-weight: 600;
}

/* ================= LINK ================= */
td a {
    font-weight: 500;
    text-decoration: none;
}

td a:hover {
    text-decoration: underline;
}

.chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 25px 0 10px;
}

.chart-header h4 {
    color: var(--main-red);
}

.btn-toggle {
    background: transparent;
    border: 1px solid var(--main-red);
    color: var(--main-red);
    padding: 5px 12px;
    border-radius: 20px;
    cursor: pointer;
    font-size: 14px;
    transition: 0.2s;
}

.btn-toggle:hover {
    background: var(--main-red);
    color: white;
}
</style>
