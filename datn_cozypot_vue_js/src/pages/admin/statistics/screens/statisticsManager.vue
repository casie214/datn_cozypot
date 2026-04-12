<template>
    <div class="dashboard-container">
        <header class="header-section">
            <h3 class="title">Thống kê doanh thu</h3>
            <p class="subtitle">Phân tích doanh thu & hoạt động đặt lịch hệ thống CozyPot</p>
        </header>

        <div class="stats-grid">
            <div v-for="(item, index) in danhSachTheTongQuan" :key="index" class="stat-card">
                <p class="card-label">{{ item.label }}</p>
                <p class="card-value">{{ formatVND(item.value) }}</p>
                <div class="card-footer">
                    <p>{{ item.moTa }}</p>
                    <p class="italic-desc">{{ item.chuThich }}</p>
                </div>
            </div>
        </div>

        <div class="filter-section">
            <div class="filter-header">
                <div class="filter-title">
                    <h5 class="section-title">
                        <i class="fa-solid fa-filter icon-title"></i>
                        Bộ lọc doanh thu
                    </h5>
                    <p>Chọn khoảng thời gian để xem báo cáo</p>

                    <div v-if="activeTab === 'Tùy chỉnh'" class="date-picker-row">
                        <div class="input-item">
                            <label>Từ ngày</label>
                            <input type="date" v-model="startDate" />
                        </div>

                        <div class="input-item">
                            <label>Đến ngày</label>
                            <input type="date" v-model="endDate" />
                        </div>

                        <button class="btn-apply" @click="applyCustomFilter">
                            Áp dụng
                        </button>
                    </div>
                </div>

                <div class="filter-actions">
                    <div class="filter-tabs">
                        <button v-for="tab in ['Hôm nay', 'Tuần này', 'Tháng này', 'Năm nay', 'Tùy chỉnh']" :key="tab"
                            @click="changeTab(tab)" :class="['tab-btn', activeTab === tab ? 'active' : '']">
                            {{ tab }}
                        </button>
                    </div>

                    <button type="button" class="btn-export" @click="exportToExcel">
                        <span>Xuất Excel</span>
                    </button>
                </div>
            </div>

            <div class="detail-grid">
                <div v-for="(stat, index) in thongKeChiTiet" :key="index" class="detail-item">
                    <p class="detail-label">{{ stat.label }}</p>
                    <p :class="['detail-value', stat.color || '']">
                        {{ stat.isNumber ? stat.value : formatVND(stat.value) }}
                    </p>
                </div>
            </div>
        </div>

        <div class="charts-row">
            <div class="chart-box large">
                <div class="box-header">
                    <h6>Biểu đồ doanh thu theo tháng</h6>
                    <span class="year-label">Năm {{ new Date().getFullYear() }}</span>
                </div>
                <div class="chart-container">
                    <Line v-if="!isLoading" :data="chartData" :options="chartOptions" />
                    <div v-else class="loading-chart">Đang tải dữ liệu...</div>
                </div>
            </div>
            <div class="chart-box small">
                <h6>Trạng thái đặt lịch</h6>
                <div class="chart-container">
                    <Pie v-if="trangThaiData.length" :data="pieChartData" :options="pieOptions" />

                    <div v-else class="loading-chart">Đang tải...</div>
                </div>
            </div>
        </div>
        <div class="charts-row">
            <div class="chart-box large">
                <h6 class="box-title">Top 5 Set Lẩu Bán Chạy</h6>
                <div class="table-container-mini">
                    <table class="table-custom">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Ảnh</th>
                                <th>Tên Set</th>
                                <th>Giá</th>
                                <th class="text-right">Bán</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in thongKe.topSets" :key="index">
                                <td><span class="rank-mini">{{ index + 1 }}</span></td>
                                <td>
                                    <img :src="item.anh" class="product-img-mini" />
                                </td>

                                <td class="product-name-mini">{{ item.tenSet }}</td>

                                <td class="product-price-mini">
                                    {{ formatCurrency(item.gia) }}
                                </td>

                                <td class="text-right">
                                    <span class="badge-count">{{ item.soLuongBan }}</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="chart-box small">
                <h6>Phân phối kênh đặt lịch</h6>
                <div class="chart-container">
                    <Pie v-if="kenhDatData.length" :data="pieKenhDatData" :options="pieKenhOptions" />
                    <div v-else class="loading-chart">Đang tải...</div>
                </div>
            </div>
        </div>

        <div class="growth-section-new">
            <h6 class="growth-title">Phân tích tăng trưởng</h6>

            <div class="growth-grid">
                <div class="growth-col">
                    <p class="growth-time">Hôm nay</p>
                    <div class="metric-row">
                        <span>Doanh thu</span>
                        <strong>{{ formatVND(thongKeTongQuan.doanhThuHomNay) }}</strong>
                    </div>
                    <div class="metric-row">
                        <span>Đơn hàng</span>
                        <strong>{{ thongKeTongQuan.soDonHomNay }}</strong>
                    </div>
                    <div class="growth-percent neutral">
                        Cập nhật hôm nay
                    </div>
                </div>

                <div class="growth-col">
                    <p class="growth-time">Tuần này</p>
                    <div class="metric-row">
                        <span>Doanh thu</span>
                        <strong>{{ formatVND(thongKeTongQuan.doanhThuTuanNay) }}</strong>
                    </div>
                    <div class="metric-row">
                        <span>Đơn hàng</span>
                        <strong>{{ thongKeTongQuan.soDonTuanNay }}</strong>
                    </div>
                    <div class="growth-percent neutral">
                        So với tuần trước
                    </div>
                </div>

                <div class="growth-col highlight">
                    <p class="growth-time">Tháng này</p>
                    <div class="metric-row">
                        <span>Doanh thu</span>
                        <strong>{{ formatVND(thongKeTongQuan.doanhThuThangNay) }}</strong>
                    </div>
                    <div class="metric-row">
                        <span>Đơn hàng</span>
                        <strong>{{ thongKeTongQuan.soDonThangNay }}</strong>
                    </div>
                    <div class="growth-percent neutral">
                        Dữ liệu tháng hiện tại
                    </div>
                </div>

                <div class="growth-col">
                    <p class="growth-time">Năm {{ new Date().getFullYear() }}</p>
                    <div class="metric-row">
                        <span>Doanh thu</span>
                        <strong>{{ formatVND(thongKeTongQuan.doanhThuNamNay) }}</strong>
                    </div>
                    <div class="metric-row">
                        <span>Khách hàng</span>
                        <strong>{{ thongKeTongQuan.tongKhachHang }}</strong>
                    </div>
                    <div class="growth-percent neutral">
                        Tổng kết năm
                    </div>
                </div>
            </div>
        </div>

    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import axiosClient from '@/services/axiosClient';
import * as XLSX from 'xlsx';
import {
    Chart as ChartJS,
    Title,
    Tooltip,
    Legend,
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Filler,
    ArcElement
} from 'chart.js'
import { Line, Pie } from 'vue-chartjs'

ChartJS.register(
    Title,
    Tooltip,
    Legend,
    LineElement,
    PointElement,
    LinearScale,
    CategoryScale,
    Filler,
    ArcElement
)

const startDate = ref(new Date().toISOString().substr(0, 10));
const endDate = ref(new Date().toISOString().substr(0, 10));
const activeTab = ref('Tháng này');
const isLoading = ref(false);
const trangThaiData = ref([]);
const kenhDatData = ref([]);

const thongKe = ref({
    doanhThuHomNay: 0,
    doanhThuTuanNay: 0,
    doanhThuThangNay: 0,
    doanhThuNamNay: 0,
    doanhThuTienMat: 0,
    doanhThuChuyenKhoan: 0,
    tongHoaDon: 0,
    tongBanDaDat: 0,
    doanhThuDuKien: 0,
    giaTriTrungBinhDon: 0,
    tongTienCoc: 0,
    tongGiamGia: 0,
    tongHoaDonHuy: 0,
    doanhThuThucNhan: 0,
    doanhThu12Thang: [],
    tongKhachHang: 0,
    khachMoi: 0,
    khachQuayLai: 0,
    tyLeQuayLai: 0,
    datBanTheoGio: [],
    topSets: []
});

const thongKeTongQuan = ref({
    doanhThuHomNay: 0,
    doanhThuTuanNay: 0,
    doanhThuThangNay: 0,
    doanhThuNamNay: 0,
    soDonHomNay: 0,
    soDonTuanNay: 0,
    soDonThangNay: 0,
    soDonNamNay: 0,
    tongKhachHang: 0
});

const formatCurrency = (value) => {
    if (!value) return '0 đ';
    return new Intl.NumberFormat('vi-VN').format(value) + ' đ';
};

const formatVND = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);

const changeTab = (tabName) => {
    activeTab.value = tabName;
    if (tabName !== 'Tùy chỉnh') {
        loadThongKe();
    }
}

const applyCustomFilter = () => {
    if (!startDate.value || !endDate.value) {
        alert("Vui lòng chọn đầy đủ ngày");
        return;
    }
    if (startDate.value > endDate.value) {
        alert("Từ ngày không được lớn hơn đến ngày");
        return;
    }
    loadThongKe();
};

const loadThongKeTongQuan = async () => {
    try {
        const response = await axiosClient.get('/thong-ke/doanh-thu', {
            params: { loai: 'Tháng này' }
        });
        
        const data = response.data;
        thongKeTongQuan.value = {
            doanhThuHomNay: data.doanhThuHomNay || 0,
            doanhThuTuanNay: data.doanhThuTuanNay || 0,
            doanhThuThangNay: data.doanhThuThangNay || 0,
            doanhThuNamNay: data.doanhThuNamNay || 0,
            
            soDonHomNay: data.soDonHomNay || 0,
            soDonTuanNay: data.soDonTuanNay || 0,
            soDonThangNay: data.soDonThangNay || 0,
            soDonNamNay: data.soDonNamNay || 0,
            
            tongKhachHang: data.tongKhachHang || 0
        };
    } catch (error) {
        console.error("Lỗi khi lấy thống kê tổng quan:", error);
    }
};

const loadThongKe = async () => {
    isLoading.value = true;
    try {
        const response = await axiosClient.get('/thong-ke/doanh-thu', {
            params: {
                loai: activeTab.value,
                tuNgay: activeTab.value === 'Tùy chỉnh' ? startDate.value : null,
                denNgay: activeTab.value === 'Tùy chỉnh' ? endDate.value : null
            }
        });
        thongKe.value = response.data;
    } catch (error) {
        console.error("Lỗi khi lấy thống kê:", error);
    } finally {
        isLoading.value = false;
    }
};

const loadTrangThai = async () => {
    try {
        const res = await axiosClient.get("/thong-ke/trang-thai-don-hang");
        trangThaiData.value = res.data;
    } catch (e) {
        console.error("Lỗi trạng thái đơn hàng", e);
    }
}

const loadKenhDat = async () => {
    try {
        const res = await axiosClient.get("/thong-ke/kenh-dat");
        kenhDatData.value = res.data;
    } catch (e) {
        console.error("Lỗi kênh đặt lịch", e);
    }
}

onMounted(() => {
    loadThongKeTongQuan();
    loadThongKe();
    loadTrangThai();
    loadKenhDat();
});

watch(activeTab, () => {
    if (activeTab.value !== 'Tùy chỉnh') {
        loadThongKe();
    }
});

const danhSachTheTongQuan = computed(() => {
    return [
        {
            label: 'Doanh thu hôm nay',
            value: Number(thongKeTongQuan.value.doanhThuHomNay) || 0,
            moTa: 'Cập nhật tức thời',
            chuThich: 'Hệ thống CozyPot'
        },
        {
            label: 'Doanh thu tuần này',
            value: Number(thongKeTongQuan.value.doanhThuTuanNay) || 0,
            moTa: `${thongKeTongQuan.value.soDonTuanNay || 0} hóa đơn hoàn tất`,
            chuThich: 'Dữ liệu 7 ngày gần nhất'
        },
        {
            label: 'Doanh thu tháng này',
            value: Number(thongKeTongQuan.value.doanhThuThangNay) || 0,
            moTa: `${thongKeTongQuan.value.soDonThangNay || 0} hóa đơn hoàn tất`,
            chuThich: `Trung bình ${formatVND((Number(thongKeTongQuan.value.doanhThuThangNay) || 0) / 30)}/ngày`
        },
        {
            label: 'Doanh thu năm nay',
            value: Number(thongKeTongQuan.value.doanhThuNamNay) || 0,
            moTa: 'Tổng kết doanh số',
            chuThich: `Năm ${new Date().getFullYear()}`
        }
    ];
});

const thongKeChiTiet = computed(() => [
    { label: 'Tổng hóa đơn hoàn tất', value: thongKe.value.tongHoaDon, isNumber: true },
    { label: 'Doanh thu tiền mặt', value: thongKe.value.doanhThuTienMat, color: 'text-blue' },
    { label: 'Doanh thu chuyển khoản', value: thongKe.value.doanhThuChuyenKhoan, color: 'text-green' },
    { label: 'Doanh thu thực nhận', value: thongKe.value.doanhThuThucNhan, color: 'text-green' },
    { label: 'Giá trị trung bình / đơn', value: thongKe.value.giaTriTrungBinhDon },
    { label: 'Tiền cọc đã thu', value: thongKe.value.tongTienCoc, color: 'text-blue' },
    { label: 'Tổng giảm giá', value: thongKe.value.tongGiamGia, color: 'text-orange' },
    { label: 'Hóa đơn hủy', value: thongKe.value.tongHoaDonHuy, isNumber: true, color: 'text-red' },
]);

const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: { display: false },
        tooltip: {
            backgroundColor: '#8b0000', titleFont: { size: 14 },
            bodyFont: { size: 13 },
            padding: 12,
            displayColors: false,
            callbacks: {
                title: (items) => `Tháng ${items[0].label.replace('T', '')}`,
                label: (context) => ` Doanh thu: ${new Intl.NumberFormat('vi-VN').format(context.raw)} ₫`
            }
        }
    },
    scales: {
        y: {
            beginAtZero: true,
            grid: { color: '#f1f5f9', drawBorder: false },
            ticks: {
                font: { size: 11 },
                color: '#94a3b8',
                callback: (value) => value >= 1000000 ? (value / 1000000) + 'M' : value
            }
        },
        x: {
            grid: { display: false },
            ticks: { font: { size: 11 }, color: '#94a3b8' }
        }
    }
};

const chartData = computed(() => ({
    labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
    datasets: [
        {
            label: 'Doanh thu tháng',
            data: thongKe.value.doanhThu12Thang?.length
                ? thongKe.value.doanhThu12Thang
                : Array(12).fill(0),
            borderColor: '#F4511E',
            backgroundColor: 'rgba(244,81,30,0.12)',
            borderWidth: 3,
            fill: true,
            tension: 0.4,
            pointRadius: 5,
            pointBackgroundColor: '#C62828',
            pointBorderColor: '#fff',
            pointHoverRadius: 8
        }
    ]
}));

const mapTrangThai = (status) => {
    const map = {
        1: "Chờ xác nhận",
        2: "Đã cọc",
        3: "Đang phục vụ",
        4: "Hoàn tất tạm",
        5: "Đã thanh toán",
        6: "Đã giao",
        7: "Hoàn tất",
        8: "Đã hủy"
    }
    return map[status] || "Khác"
}

const pieChartData = computed(() => ({
    labels: trangThaiData.value.map(i => mapTrangThai(i.trangThai)),
    datasets: [
        {
            data: trangThaiData.value.map(i => i.soLuong),
            backgroundColor: [
                '#FFB300', '#FB8C00', '#F4511E', '#E53935',
                '#C62828', '#B71C1C', '#8E0000', '#5D0000'
            ],
            borderColor: '#fff',
            borderWidth: 2
        }
    ]
}));

const pieOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            position: 'bottom'
        }
    }
};

const pieKenhDatData = computed(() => ({
    labels: kenhDatData.value.map(i => i.tenKenh),
    datasets: [
        {
            data: kenhDatData.value.map(i => i.soLuong),
            backgroundColor: ['#F4511E', '#8b0000'],
            borderColor: '#fff',
            borderWidth: 2
        }
    ]
}));

const pieKenhOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
        legend: {
            position: 'bottom',
            labels: { font: { size: 12 } }
        },
        tooltip: {
            callbacks: {
                label: (context) => {
                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                    const value = context.raw;
                    const percent = ((value / total) * 100).toFixed(1);
                    return `${context.label}: ${value} (${percent}%)`;
                }
            }
        }
    }
};

const exportToExcel = () => {
    const reportDate = new Date().toLocaleString('vi-VN');
    const fileName = `Bao-cao-doanh-thu-${activeTab.value.replace(' ', '-')}.xls`;

    const tableHtml = `
        <html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel" xmlns="http://www.w3.org/TR/REC-html40">
        <head><meta charset="UTF-8"></head>
        <body>
            <table border="1" style="border-collapse: collapse; font-family: Arial, sans-serif; width: 500px">
                <tr style="height: 100px;">
                    <th colspan="4" style="font-size: 24px; color: #8b0000; vertical-align: middle; border: none;">COZYPOT</th>
                </tr>
                <tr>
                    <th colspan="4" style="font-size: 16px; color: #444; border: none;">Hệ thống đặt lịch lẩu CozyPot</th>
                </tr>
                
                <tr>
                    <th colspan="4" style="font-size: 18px; font-weight: bold; background-color: #F4511E; color: white; padding: 10px;">
                        BÁO CÁO CHI TIẾT DOANH THU - ${activeTab.value.toUpperCase()}
                    </th>
                </tr>
                <tr>
                    <td colspan="4" style="text-align: right; font-style: italic; border: none;">Ngày xuất báo cáo: ${reportDate}</td>
                </tr>
                <tr><td colspan="4" style="border: none; height: 10px;"></td></tr>

                <tr style="background-color: #8b0000; color: white; font-weight: bold;">
                    <td colspan="2" style="padding: 8px;">HẠNG MỤC THỐNG KÊ</td>
                    <td colspan="2" style="padding: 8px; text-align: center;">GIÁ TRỊ</td>
                </tr>
                <tr>
                    <td colspan="2" style="padding: 8px;">Doanh thu tháng này</td>
                    <td colspan="2" style="padding: 8px; text-align: right; font-weight: bold;">${formatVND(thongKe.value.doanhThuThangNay)}</td>
                </tr>
                <tr>
                    <td colspan="2">Doanh thu tiền mặt</td>
                    <td colspan="2" style="text-align:right">${formatVND(thongKe.value.doanhThuTienMat)}</td>
                </tr>
                <tr>
                    <td colspan="2">Doanh thu chuyển khoản</td>
                    <td colspan="2" style="text-align:right">${formatVND(thongKe.value.doanhThuChuyenKhoan)}</td>
                </tr>
                <tr>
                    <td colspan="2" style="padding: 8px;">Tổng số hóa đơn hoàn tất</td>
                    <td colspan="2" style="padding: 8px; text-align: right;">${thongKe.value.tongHoaDon} đơn</td>
                </tr>
                <tr>
                    <td colspan="2" style="padding: 8px;">Giá trị trung bình đơn (AOV)</td>
                    <td colspan="2" style="padding: 8px; text-align: right;">${formatVND(thongKe.value.giaTriTrungBinhDon)}</td>
                </tr>
                <tr>
                    <td colspan="2" style="padding: 8px;">Tổng tiền cọc đã thu</td>
                    <td colspan="2" style="padding: 8px; text-align: right; color: #0000FF;">${formatVND(thongKe.value.tongTienCoc)}</td>
                </tr>
                <tr style="background-color: #fff7f0;">
                    <td colspan="2" style="padding: 8px; font-weight: bold;">DOANH THU THỰC NHẬN</td>
                    <td colspan="2" style="padding: 8px; text-align: right; font-weight: bold; color: #d32f2f; font-size: 16px;">
                        ${formatVND(thongKe.value.doanhThuThucNhan)}
                    </td>
                </tr>

                <tr><td colspan="4" style="border: none; height: 20px;"></td></tr>

                <tr style="background-color: #444; color: white; font-weight: bold;">
                    <td style="text-align: center; width: 50px;">STT</td>
                    <td style="width: 250px;">Tên Set Lẩu</td>
                    <td style="text-align: center; width: 120px;">Số lượng bán</td>
                    <td style="text-align: right; width: 150px;">Đơn giá</td>
                </tr>
                ${thongKe.value.topSets.map((item, index) => `
                    <tr>
                        <td style="text-align: center;">${index + 1}</td>
                        <td>${item.tenSet}</td>
                        <td style="text-align: center;">${item.soLuongBan}</td>
                        <td style="text-align: right;">${formatVND(item.gia)}</td>
                    </tr>
                `).join('')}

                <tr><td colspan="4" style="border: none; height: 30px;"></td></tr>
                <tr>
                    <td colspan="2" style="border: none;"></td>
                    <td colspan="2" style="text-align: center; border: none;">
                        <strong>Người lập báo cáo</strong><br>
                        <i style="font-size: 12px;">(Ký và ghi rõ họ tên)</i>
                    </td>
                </tr>
            </table>
        </body>
        </html>
    `;

    const blob = new Blob([tableHtml], { type: "application/vnd.ms-excel" });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = fileName;
    a.click();
    window.URL.revokeObjectURL(url);
};
</script>

<style scoped>
.dashboard-container {
    padding: 2rem;
    background-color: #f8fafc;
    min-height: 100vh;
}

.header-section {
    margin-bottom: 2.5rem;
}

.title {
    font-size: 1.75rem;
    font-weight: 700;
    color: #8b0000;
}

.chart-container {
    position: relative;
    height: 300px;
    width: 100%;
}

.loading-chart {
    height: 300px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
}

.box-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
}

.year-label {
    font-size: 0.8rem;
    background: #f1f5f9;
    padding: 4px 12px;
    border-radius: 20px;
    color: #64748b;
    font-weight: 600;
}

.table-container {
    overflow-x: auto;
}

.revenue-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 0.9rem;
}

.revenue-table th {
    text-align: left;
    padding: 12px;
    background-color: #f8fafc;
    color: #64748b;
    font-weight: 600;
    border-bottom: 2px solid #e2e8f0;
}

.revenue-table td {
    padding: 14px 12px;
    border-bottom: 1px solid #f1f5f9;
    color: #334155;
}

.revenue-table tr:hover {
    background-color: #fcfcfc;
}

.font-bold {
    font-weight: 700;
}

.text-green {
    color: #8b0000;
}

.progress-bar {
    width: 100px;
    height: 6px;
    background: #e2e8f0;
    border-radius: 10px;
    overflow: hidden;
}

.progress-fill {
    height: 100%;
    background: #8b0000;
    border-radius: 10px;
}

.text-center {
    text-align: center;
    color: #94a3b8;
    padding: 2rem !important;
}

.date-picker-group {
    display: flex;
    gap: 15px;
    margin-right: 15px;
    align-items: center;
}

.box-title {
    font-weight: 700;
    margin-bottom: 1.5rem;
    color: #1e293b;
}

.product-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f1f5f9;
}

.rank {
    background: #8b0000;
    color: white;
    width: 20px;
    height: 20px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    font-size: 10px;
    margin-right: 10px;
}

.growth-card {
    background: white;
    padding: 1.5rem;
    border-radius: 1rem;
    margin-top: 2rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.growth-value {
    font-size: 1.5rem;
    font-weight: 700;
    margin-top: 10px;
}

.growth-value.up {
    color: #F4511E;
}

.growth-value.down {
    color: #ef4444;
}

.input-item {
    display: flex;
    flex-direction: column;
}

.input-item input {
    border: 1px solid #cbd5e1;
    border-radius: 8px;
    padding: 8px 15px;
    font-size: 14px;
    color: #1e293b;
    background-color: #fff;
    outline: none;
    width: 160px;
    transition: all 0.2s ease;
    cursor: pointer;
}

.input-item label {
    font-size: 11px;
    font-weight: 700;
    color: #94a3b8;
    text-transform: uppercase;
    margin-bottom: 4px;
    letter-spacing: 0.5px;
}

.input-item input:focus {
    border-color: #8b0000;
    background-color: #fff;
    box-shadow: 0 0 0 2px rgba(139, 0, 0, 0.1);
}

.subtitle {
    color: #64748b;
    font-size: 0.875rem;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1.5rem;
    margin-bottom: 2.5rem;
}

.date-picker-row {
    display: flex;
    gap: 20px;
    margin-top: 12px;
}

.filter-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 2rem;
}

.stat-card {
    background: white;
    padding: 1.5rem;
    border-radius: 1rem;
    border-top: 4px solid #8b0000;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.card-label {
    font-size: 0.9rem;
    color: #64748b;
    font-weight: 500;
}

.card-value {
    font-size: 1.6rem;
    font-weight: 700;
    color: #8b0000;
    margin: 0.5rem 0;
}

.card-footer {
    font-size: 0.75rem;
    color: #94a3b8;
}

.italic-desc {
    font-style: italic;
    color: #cbd5e1;
}

.filter-actions {
    display: flex;
    align-items: center;
    gap: 12px;
}

.btn-export {
    background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
    color: white;
    border: none;
    padding: 8px 15px;
    border-radius: 4px;
    cursor: pointer;
    height: inherit;
    text-align: center;
    transition: 0.2s;
}

.icon-excel {
    color: #8b0000;
    flex-shrink: 0;
}

.filter-tabs {
    display: flex;
    background: #f1f5f9;
    padding: 0.25rem;
    border-radius: 0.75rem;
    gap: 4px;
    height: 38px;
    align-items: center;
}

.tab-btn {
    height: 100%;
    padding: 0 1rem;
    font-size: 0.8rem;
    border-radius: 0.6rem;
    border: none;
    cursor: pointer;
    color: #64748b;
    background: transparent;
    transition: 0.3s;
    display: flex;
    align-items: center;
}

.filter-section {
    background: white;
    padding: 2rem;
    border-radius: 1rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.tab-btn.active {
    background: white;
    color: #8b0000;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    font-weight: 700;
}

.detail-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 1rem;
    border-top: 1px solid #f1f5f9;
    padding-top: 2rem;
}

.detail-label {
    font-size: 0.7rem;
    font-weight: 700;
    color: #94a3b8;
    text-transform: uppercase;
    margin-bottom: 0.5rem;
}

.detail-value {
    font-size: 1.25rem;
    font-weight: 700;
}

.invoice-section {
    margin-top: 2rem;
    background: white;
    padding: 1.5rem;
    border-radius: 1rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.invoice-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 1rem;
    font-weight: 700;
    color: #8b0000;
}

.invoice-table {
    width: 100%;
    border-collapse: collapse;
}

.invoice-table th {
    font-size: 12px;
    text-transform: uppercase;
    color: #94a3b8;
    padding: 10px;
    border-bottom: 1px solid #f1f5f9;
    text-align: left;
}

.invoice-table td {
    padding: 12px 10px;
    border-bottom: 1px solid #f8fafc;
    font-size: 14px;
}

.invoice-table tr:hover {
    background: #fcfcfc;
}

.status {
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;
}

.status.success {
    background: #f0fdf4;
    color: #16a34a;
}

.status.cancel {
    background: #fef2f2;
    color: #dc2626;
}

.status.pending {
    background: #fff7ed;
    color: #ea580c;
}

.charts-row {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 2rem;
    margin-top: 2rem;
}

.chart-box {
    background: white;
    padding: 1.5rem;
    border-radius: 1rem;
    min-height: 350px;
}

.placeholder-bg {
    height: 250px;
    background: #f8fafc;
    border: 2px dashed #e2e8f0;
    border-radius: 0.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #cbd5e1;
    margin-top: 1rem;
}

@media (max-width: 1024px) {
    .stats-grid,
    .detail-grid,
    .charts-row {
        grid-template-columns: 1fr;
    }
}

.growth-section-new {
    margin-top: 2rem;
    background: white;
    padding: 2rem;
    border-radius: 1rem;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.growth-title {
    font-weight: 700;
    margin-bottom: 1.5rem;
    color: #8b0000;
}

.growth-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 1.5rem;
}

.growth-col {
    padding: 1.2rem;
    border-radius: 0.8rem;
    background: #f8fafc;
    transition: 0.3s;
}

.growth-col:hover {
    transform: translateY(-4px);
}

.growth-col.highlight {
    background: linear-gradient(135deg, #fff7f0, #ffe8dc);
    border: 1px solid #ffd6c2;
}

.growth-time {
    font-size: 0.8rem;
    font-weight: 700;
    text-transform: uppercase;
    color: #94a3b8;
    margin-bottom: 1rem;
}

.metric-row {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 0.9rem;
}

.metric-row strong {
    color: #8b0000;
}

.growth-percent {
    margin-top: 12px;
    font-weight: 700;
    font-size: 0.9rem;
}

.growth-percent.up {
    color: #F4511E;
}

.growth-percent.down {
    color: #ef4444;
}

.growth-percent.neutral {
    color: #64748b;
}

.table-container-mini {
    margin-top: 10px;
}

.table-custom {
    width: 100%;
    border-collapse: collapse;
}

.table-custom th {
    font-size: 11px;
    text-transform: uppercase;
    color: #94a3b8;
    text-align: left;
    padding: 8px 4px;
    border-bottom: 1px solid #f1f5f9;
}

.table-custom td {
    padding: 10px 4px;
    border-bottom: 1px solid #f8fafc;
    vertical-align: middle;
    font-size: 13px;
}

.product-img-mini {
    width: 35px;
    height: 35px;
    border-radius: 6px;
    object-fit: cover;
    border: 1px solid #f1f5f9;
}

.rank-mini {
    font-weight: 700;
    color: #64748b;
}

.product-name-mini {
    font-weight: 600;
    color: #334155;
    max-width: 100px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.product-price-mini {
    color: #b91c1c;
    font-weight: 700;
    font-size: 12px;
}

.badge-count {
    background: #f0fdf4;
    color: #16a34a;
    padding: 2px 8px;
    border-radius: 12px;
    font-weight: 700;
    font-size: 11px;
    border: 1px solid #dcfce7;
}

.text-right {
    text-align: right;
}

.customer-stats {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
    margin-top: 1rem;
}

.customer-item {
    background: #f8fafc;
    padding: 1rem;
    border-radius: 0.75rem;
}

.customer-item .label {
    font-size: 0.75rem;
    text-transform: uppercase;
    color: #94a3b8;
    margin-bottom: 6px;
}

.customer-item .value {
    font-size: 1.4rem;
    font-weight: 700;
}

.text-blue {
    color: #3b82f6;
}

.text-orange {
    color: #f97316;
}

.btn-apply {
    align-self: flex-end;
    height: 34px;
    padding: 0 14px;
    background: #8b0000;
    color: white;
    border: none;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    transition: 0.2s;
}

.btn-apply:hover {
    background: #a00000;
}
</style>