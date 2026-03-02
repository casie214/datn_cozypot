<script setup>
import {
  fetchAllBanAn,
  fetchAllCheckIn,
  fetchTableStatusByDate,
  updateTrangThaiBan,
  createOrder,
} from "../../../../services/tableManageService";
import { computed, onMounted, ref, onUnmounted, watch } from "vue";
import dayjs from "dayjs";
import router from "@/App";
import { useRoute, useRouter } from "vue-router";
import FoodList from "../modal/innerComponents/foodList.vue";
import Swal from "sweetalert2";
import axiosClient from "@/services/axiosClient";

const activeFloor = ref(1);

// Tự động quét danh sách bàn, lấy ra các tầng duy nhất
const danhSachTang = computed(() => {
  if (!danhSachBan.value || danhSachBan.value.length === 0) return [];
  const floors = danhSachBan.value.map(ban => Number(ban.soTang || ban.tang));
  const uniqueFloors = [...new Set(floors)].sort((a, b) => a - b);
  if (uniqueFloors.length > 0 && !uniqueFloors.includes(activeFloor.value)) {
    activeFloor.value = uniqueFloors[0];
  }
  return uniqueFloors;
});

const danhSachBan = ref([]);
const selectedItems = ref({});

const timeWindowOptions = [
  { label: '15p', value: 15 },
  { label: '30p', value: 30 },
  { label: '1h', value: 60 },
  { label: '2h', value: 120 },
  { label: 'Hết ngày', value: 1440 }
];

const selectedTimeWindow = ref(15);

const setTimeWindow = (val) => {
  selectedTimeWindow.value = val;
  handleFetchAllCheckIn();
};

const isShowDiscountModal = ref(false);
const activeVoucherTab = ref('PUBLIC'); // 'PUBLIC' hoặc 'PERSONAL'
const publicVouchers = ref([]); 
const personalVouchers = ref([]); 
const maGiamGiaInput = ref("");

const hasDiscountInGroup = computed(() => {
  const mainHasDiscount = (selectedPhieu.value?.soTienDaGiam || 0) > 0;
  const subHasDiscount = listHoaDonGop.value.some(hd => (hd.soTienDaGiam || 0) > 0);
  return mainHasDiscount || subHasDiscount;
});

// 🚨 THÊM BIẾN NÀY: Lấy tổng tiền món hiện tại để check điều kiện tối thiểu
const currentRawTotal = computed(() => {
  let tongTienTho = billSummary.value?.tong || 0; 
  listHoaDonGop.value.forEach(hd => { tongTienTho += (hd.canThu || 0); });
  return tongTienTho;
});

const openDiscountModal = async () => {
  if (!selectedPhieu.value?.idHoaDon) {
    return Swal.fire('Lưu ý', 'Cần có hóa đơn (đã gọi món) để áp dụng mã!', 'warning');
  }
  if (hasDiscountInGroup.value) {
    return Swal.fire('Lưu ý', 'Nhóm bàn này đã được áp dụng một mã giảm giá. Vui lòng hủy mã cũ trước khi áp dụng mã mới!', 'warning');
  }

  try {
    const res = await axiosClient.get(`/phieu-giam-gia/search?page=0&size=100`);
    if (res.data && res.data.content) {
      const currentKhachId = selectedPhieu.value?.idKhachHang;
      
      // 1. Lọc mã Công cộng (doiTuong = 0)
      publicVouchers.value = res.data.content.filter(vc => vc.trangThai === 1 && vc.doiTuong === 0);
      
      // 2. Lọc mã Cá nhân (doiTuong = 1 và ID khách có trong danh sách)
      personalVouchers.value = res.data.content.filter(vc => {
        if (vc.trangThai === 1 && vc.doiTuong === 1 && currentKhachId) {
            return vc.danhSachKhachHang.some(k => k.id === currentKhachId);
        }
        return false;
      });
    }
    
    maGiamGiaInput.value = "";
    activeVoucherTab.value = 'PUBLIC'; // Mở modal mặc định ở tab Công cộng
    isShowDiscountModal.value = true;
  } catch (error) {
    Swal.fire('Lỗi', 'Không thể lấy danh sách ưu đãi', 'error');
  }
};

const applyVoucher = async (voucher = null) => {
  const codeToApply = voucher ? voucher.codeGiamGia : maGiamGiaInput.value;
  if (!codeToApply) return Swal.fire('Lỗi', 'Vui lòng nhập mã giảm giá!', 'warning');

  if (voucher && currentRawTotal.value < voucher.donHangToiThieu) {
    return Swal.fire('Chưa đạt điều kiện', `Đơn hàng cần tối thiểu ${voucher.donHangToiThieu.toLocaleString()}đ!`, 'warning');
  }

  try {
    Swal.fire({ title: 'Đang áp dụng...', didOpen: () => Swal.showLoading() });
    const payload = {
      idHoaDon: selectedPhieu.value.idHoaDon,
      idKhachHang: selectedPhieu.value.idKhachHang || null,
      idPhieuGiamGia: voucher ? voucher.id : null,
      codeGiamGia: codeToApply, 
      tongTienHienTai: currentRawTotal.value 
    };

    await axiosClient.post('/phieu-giam-gia/ap-dung', payload);
    Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đã áp dụng giảm giá!', timer: 1500, showConfirmButton: false });
    isShowDiscountModal.value = false;
    
    // 🚨 GỌI HÀM NÀY ĐỂ TẢI DỮ LIỆU MÀ KHÔNG BỊ ĐẨY RA NGOÀI
    await refreshTableData(); 
  } catch (error) {
    Swal.fire('Lỗi', error.response?.data?.message || 'Mã không hợp lệ hoặc đơn chưa đạt tối thiểu!', 'error');
  }
};

const removeVoucher = async () => {
  const confirm = await Swal.fire({ title: 'Hủy áp dụng mã?', icon: 'warning', showCancelButton: true, confirmButtonText: 'Đồng ý' });
  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang hủy...', didOpen: () => Swal.showLoading() });
    await axiosClient.post(`/phieu-giam-gia/huy-ap-dung/${selectedPhieu.value.idHoaDon}`);
    
    if (selectedPhieu.value) {
        selectedPhieu.value.soTienDaGiam = 0;
    }

    Swal.fire({ icon: 'success', title: 'Đã hủy mã', timer: 1000, showConfirmButton: false });
    
    // 🚨 GỌI HÀM NÀY ĐỂ TẢI DỮ LIỆU MÀ KHÔNG BỊ ĐẨY RA NGOÀI
    await refreshTableData(); 
  } catch (error) {
    Swal.fire('Lỗi', 'Không thể hủy mã lúc này!', 'error');
  }
};

const checkTimeStatus = (dbTime) => {
  if (!dbTime) return { isEarly: false, minutes: 0 };
  const now = dayjs(currentTime.value);
  const target = dayjs(dbTime);
  const minutesLeft = target.diff(now, "minute");
  if (minutesLeft > 15) { 
    return { isEarly: true, minutes: minutesLeft };
  }
  return { isEarly: false, minutes: minutesLeft };
};

const fetchAllBan = async () => {
  try {
    const data = await fetchAllBanAn();
    danhSachBan.value = data.map((ban, index) => {
      const defaultCol = (index % 3) * 4 + 1;
      const defaultRow = Math.floor(index / 3) * 2 + 1;
      return {
        ...ban,
        column: ban.column || defaultCol,
        row: ban.row || defaultRow,
      };
    });
  } catch (e) {
    console.error("Lỗi load danh sách bàn", e);
  }
};

const banTheoTang = computed(() => {
  return danhSachBan.value.filter((ban) => Number(ban.soTang) === activeFloor.value);
});

const thongKeTang = computed(() => {
  const total = banTheoTang.value.length;
  const free = banTheoTang.value.filter((ban) => {
    const trangThai = getTrangThaiTheoNgay(ban.idBanAn);
    return Number(trangThai) === 0;
  }).length;
  return { total, free };
});

const tableStatusMap = ref({});

const calculatedTableStatuses = computed(() => {
  const statuses = {};
  const window = Number(selectedTimeWindow.value);
  const now = dayjs(currentTime.value);
  
  danhSachBan.value.forEach(ban => {
    let baseStatus = Number(tableStatusMap.value[ban.id] ?? 0);
    if (baseStatus === 2) baseStatus = 0; 

    if (baseStatus === 1) {
      statuses[ban.id] = 1;
      return;
    }

    const hasBookingInWindow = danhSachCho.value.some(khach => {
      const isMatch = khach.idBanAn == ban.id || khach.maBan === ban.maBan;
      
      // 🚨 SỬA Ở ĐÂY: Chỉ giữ bàn khi phiếu là 0 (Chờ xác nhận) hoặc 1 (Đã xác nhận)
      if (isMatch && [0, 1].includes(Number(khach.trangThai))) {
        const gioHen = dayjs(khach.thoiGianDat);
        const diff = gioHen.diff(now, "minute");
        return diff <= window && diff >= -30;
      }
      return false;
    });

    statuses[ban.id] = hasBookingInWindow ? 2 : baseStatus;
  });
  return statuses;
});

const getTrangThaiTheoNgay = (banId) => {
  return calculatedTableStatuses.value[banId] ?? 0;
};
const selectedDate = ref(new Date().toISOString().slice(0, 10));

const fetchTableStatus = async () => {
  try {
    const dataBan = await fetchAllBanAn();
    const newMap = {};
    dataBan.forEach((ban) => {
      newMap[ban.id] = ban.trangThai; 
    });
    tableStatusMap.value = newMap;
  } catch (error) {
    console.error("Lỗi đồng bộ trạng thái bàn:", error);
  }
};

// --- QUẢN LÝ KHÁCH CHỜ ---
const searchQuery = ref("");
const filterDate = ref(null);
const danhSachCho = ref([]);

const getCurrentStaffId = () => {
  const user = JSON.parse(localStorage.getItem("user"));
  return user ? user.id : null;
};

const handleFetchAllCheckIn = async () => {
  try {
    danhSachCho.value = await fetchAllCheckIn();
    await fetchTableStatus();
  } catch (error) {
    console.error("Lỗi fetch khách chờ:", error);
  }
};

const handleTableClick = async (ban) => {
  const trangThaiBan = getTrangThaiTheoNgay(ban.id);
  
  if (trangThaiBan === 1) {
    openManageModal(ban);
    return;
  } 
  
  const pendingSplitText = localStorage.getItem("pendingSplitCustomer");
  
  if (pendingSplitText && trangThaiBan === 0) {
    const pendingCustomer = JSON.parse(pendingSplitText);
    const khachDangCho = Number(pendingCustomer.soKhachCanXep) || 0;
    const sucChuaBanNay = Number(ban.soCho) || 0;

    const confirmSplit = await Swal.fire({
      title: 'Xác nhận mở bàn phụ?',
      html: `Đoàn của <b>${pendingCustomer.tenKhachHang}</b> đang thiếu <b>${khachDangCho} chỗ</b>.<br>
             Bàn <b>${ban.maBan}</b> có <b>${sucChuaBanNay} chỗ</b>.<br>Đồng ý xếp vào đây?`,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#28a745',
      cancelButtonText: 'Chọn bàn khác',
      confirmButtonText: 'Xếp vào bàn này'
    });

    if (confirmSplit.isConfirmed) {
      try {
        const payloadBanPhu = {
          idBanAn: ban.id, 
          trangThai: 1, 
          id: null, 
          idKhachHang: pendingCustomer.idKhachHang, 
          idNhanVien: getCurrentStaffId() || 1,
          ghiChu: pendingCustomer.ghiChu,
          vatApDung: 0.0, 
          maDatBanGoc: pendingCustomer.maDatBanGoc 
        };

        await updateTrangThaiBan(payloadBanPhu); 
        
        // 🚨 TOÁN HỌC TRỪ DẦN SỐ KHÁCH
        const conLai = khachDangCho - sucChuaBanNay;

        if (conLai <= 0) {
            // ĐÃ XẾP ĐỦ HOẶC DƯ CHỖ -> KẾT THÚC TỰ ĐỘNG
            Swal.fire({ icon: 'success', title: 'Hoàn tất!', text: 'Đã xếp đủ chỗ cho toàn bộ khách!', timer: 2000 });
            localStorage.removeItem("pendingSplitCustomer");
            isSelectingSecondTable.value = false;
        } else {
            // VẪN CÒN THIẾU CHỖ -> CẬP NHẬT LẠI LOCAL STORAGE VÀ BÁO ĐỎ
            pendingCustomer.soKhachCanXep = conLai;
            localStorage.setItem("pendingSplitCustomer", JSON.stringify(pendingCustomer));
            
            Swal.fire({ 
              icon: 'warning', 
              title: 'Vẫn thiếu chỗ!', 
              html: `Đã xếp thêm khách vào ${ban.maBan}.<br>Đoàn vẫn còn <b>${conLai} người</b> chưa có chỗ.<br>Vui lòng click chọn thêm bàn nữa!`,
              confirmButtonText: 'Chọn tiếp'
            });
        }
        
        await handleFetchAllCheckIn();
        await fetchAllBan();
        await fetchTableStatus();
      } catch (error) {
        Swal.fire('Lỗi', 'Không thể mở bàn phụ lúc này', 'error');
      }
    }
    return;
  }
};

const handleCheckInGuest = async (khach) => {
  const ban = danhSachBan.value.find(b => b.id === khach.idBanAn || b.maBan === khach.maBan);
  if (!ban) return Swal.fire('Lỗi', 'Không tìm thấy thông tin bàn của khách này!', 'error');

  const soKhach = khach.soNguoi || 1;
  const sucChua = Number(ban.soCho);

  if (soKhach > sucChua) {
    const swalResult = await Swal.fire({
      title: 'Bàn không đủ chỗ!',
      html: `Khách đi <b>${soKhach} người</b> nhưng bàn <b>${ban.maBan}</b> chỉ có <b>${sucChua} chỗ</b>.<br><br>Vui lòng chọn hướng xử lý:`,
      icon: 'warning',
      showDenyButton: true,
      showCancelButton: true,
      confirmButtonColor: '#7d161a',
      denyButtonColor: '#28a745',   
      confirmButtonText: '<i class="fa-solid fa-chair"></i> Kê thêm ghế',
      denyButtonText: '<i class="fa-solid fa-arrows-split-up-and-left"></i> Tách thành nhiều bàn',
      cancelButtonText: 'Hủy bỏ'
    });

    if (swalResult.isConfirmed) {
      selectedPhieu.value = khach; 
      await openManageModal(ban);
      return;
    } else if (swalResult.isDenied) {
      await handleSplitTableLogic(khach, ban);
      return;
    } else {
      return;
    }
  }

  const timeInfo = checkTimeStatus(khach.thoiGianDat);
  if (timeInfo.isEarly) {
    await Swal.fire({
      title: 'Chưa đến giờ nhận bàn!',
      text: `Khách đến sớm ${timeInfo.minutes} phút...`,
      icon: 'error',
      confirmButtonColor: '#7d161a',
      confirmButtonText: 'Đã hiểu'
    });
    return;
  }

  selectedPhieu.value = khach; 
  await openManageModal(ban, null, khach);
};

const handleSplitTableLogic = async (khach, banGoc) => {
  try {
    const payloadBanGoc = {
      idBanAn: banGoc.id, 
      trangThai: 1, 
      id: khach.id, 
      trangThaiPhieu: 3,
      vatApDung: 10.0
    };
    await updateTrangThaiBan(payloadBanGoc); 
    
    const soKhachBanDau = Number(khach.soNguoi) || 1;
    const sucChuaBanGoc = Number(banGoc.soCho) || 0;
    const soKhachConLai = soKhachBanDau - sucChuaBanGoc;

    await Swal.fire({
      title: 'Đã Check-in Bàn 1',
      html: `Đã xếp ${sucChuaBanGoc} khách vào bàn <b>${banGoc.maBan}</b>.<br><br>
             Đoàn còn <b style="color:red">${soKhachConLai} người</b> chưa có chỗ.<br>
             Vui lòng <b>Click vào một bàn trống khác</b> trên sơ đồ để tiếp tục xếp chỗ.`,
      icon: 'info',
      confirmButtonText: 'Đã hiểu'
    });

    // 🚨 QUAN TRỌNG NHẤT: Lưu maDatBanGoc vào localStorage
    localStorage.setItem("pendingSplitCustomer", JSON.stringify({
      idKhachHang: khach.idKhachHang || null,
      tenKhachHang: khach.tenKhachHang || 'Khách vãng lai',
      maDatBanGoc: khach.maDatBan || khach.maPhieu || `PDB_${new Date().getTime()}`, // <--- DÒNG NÀY SẼ NỐI CÁC BÀN LẠI VỚI NHAU
      ghiChu: `Bàn phụ - Tách từ đoàn ${khach.soNguoi} người của bàn ${banGoc.maBan}`,
      soKhachCanXep: soKhachConLai,
      vatMacDinh: 0 
    }));

    await handleFetchAllCheckIn();
    await fetchAllBan();
  } catch (error) {
    Swal.fire('Lỗi', 'Không thể check-in bàn gốc lúc này', 'error');
  }
};

const danhSachLoc = computed(() => {
  return danhSachCho.value.filter((khach) => {
    if (!khach.maBan && !khach.idBanAn) return false;
    
    // 🚨 SỬA Ở ĐÂY: Loại bỏ các trạng thái không còn là "Khách chờ" (2, 3, 4, 5)
    // Chỉ giữ lại 0 (Chờ xác nhận) và 1 (Đã xác nhận)
    if ([2, 3, 4, 5].includes(Number(khach.trangThai))) return false;

    const thoiGianDat = dayjs(khach.thoiGianDat);
    const hienTai = dayjs(currentTime.value);
    const phutConLai = thoiGianDat.diff(hienTai, "minute");

    const window = Number(selectedTimeWindow.value);
    
    // Khách quá hạn 30 phút sẽ tự động biến mất khỏi danh sách do điều kiện >= -30 này
    const matchTime = phutConLai <= window && phutConLai >= -30; 
    
    const matchSearch = khach.soDienThoai?.includes(searchQuery.value) ||
                        khach.tenKhachHang?.toLowerCase().includes(searchQuery.value.toLowerCase());
    
    let matchDate = true;
    if (filterDate.value) {
      matchDate = thoiGianDat.format("YYYY-MM-DD") === filterDate.value;
    }

    return matchSearch && matchTime && matchDate;
  });
});

const currentTime = ref(new Date());
let timer;
const currentStaffName = ref("");
const orderHistory = ref([]); 

const fetchOrderHistory = async () => {
  if (!selectedPhieu.value?.idHoaDon) return;
  try {
    const res = await axiosClient.get(`/hoa-don-thanh-toan/lich-su/${selectedPhieu.value.idHoaDon}`);
    orderHistory.value = res.data;
    modalView.value = 'orderHistory'; 
  } catch (error) {
    Swal.fire('Lỗi', 'Không thể lấy dữ liệu lịch sử!', 'error');
  }
};

const formatHistoryTime = (time) => {
  if (!time) return "Vừa xong";
  if (Array.isArray(time)) {
    const [y, m, d, h, min] = time;
    return dayjs(new Date(y, m - 1, d, h, min)).format("HH:mm - DD/MM/YYYY");
  }
  return dayjs(time).format("HH:mm - DD/MM/YYYY");
};

onMounted(() => {
  fetchAllBan();
  handleFetchAllCheckIn();
  timer = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);

  const user = JSON.parse(localStorage.getItem("user"));
  if (user) currentStaffName.value = user.hoTen || user.username;

  setInterval(() => {
    checkOverdueTables();
  }, 3 * 60 * 1000); 
  
  // Chạy ngay 1 lần lúc vừa mở trang web
  setTimeout(() => checkOverdueTables(), 5000);
});

onUnmounted(() => clearInterval(timer));

const getCountdown = (dbTime) => {
  if (!dbTime) return "00:00:00";
  let target = null;
  if (Array.isArray(dbTime)) {
    const [y, m, d, h, min, s] = dbTime;
    target = dayjs(new Date(y, m - 1, d, h || 0, min || 0, s || 0));
  } else {
    target = dayjs(dbTime);
  }

  const now = dayjs(currentTime.value);
  const diff = target.diff(now); 
  const isNegative = diff < 0;
  const absDiff = Math.abs(diff);

  const h = Math.floor(absDiff / 3600000).toString().padStart(2, "0");
  const m = Math.floor((absDiff % 3600000) / 60000).toString().padStart(2, "0");
  const s = Math.floor((absDiff % 60000) / 1000).toString().padStart(2, "0");

  return isNegative ? `-${h}:${m}:${s}` : `${h}:${m}:${s}`;
};

const getStatusText = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "Trống";
  if (status === "1") return "Có khách";
  return "Đã đặt";
};

const getStatusClass = (trangThai) => {
  const status = String(trangThai).trim();
  if (status === "0") return "status-empty";
  if (status === "1") return "status-occupied-light";
  return "status-booked";
};

// --- QUẢN LÝ MODAL & CẬP NHẬT ---
const isShowModal = ref(false);
const selectedBan = ref(null);
const selectedPhieu = ref(null);
const activeHoaDonId = ref(null);
const modalView = ref("info");
const listMonDaChon = ref([]);

// KHAI BÁO BIẾN CHO GỘP BÀN
const listBanGop = ref([]); 
const listHoaDonGop = ref([]); 
const listBanCoKhachKhac = ref([]);

const danhSachBanCungDoan = ref([]); // Mảng dành riêng cho thanh Toggle

const fetchDanhSachBanCungDoan = async () => {
  danhSachBanCungDoan.value = [];
  
  // Lấy ID khách hàng hiện tại làm chuẩn để tìm người nhà
  const currentKhachId = selectedPhieu.value?.idKhachHang;

  // Nếu khách vãng lai không có ID, không thể nhận diện được người nhà -> Bỏ qua
  if (!currentKhachId) return;

  // Lọc các bàn đang có khách (trừ bàn đang mở)
  const activeTables = danhSachBan.value.filter(ban => 
    getTrangThaiTheoNgay(ban.id) === 1 && ban.id !== selectedBan.value?.id
  );

  for (const ban of activeTables) {
    try {
      const res = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${ban.id}`);
      // NẾU CÙNG ID KHÁCH HÀNG -> CHO VÀO THANH TOGGLE
      if (res.data && res.data.idKhachHang === currentKhachId) {
        danhSachBanCungDoan.value.push({
          ...ban,
          _hoaDonInfo: { 
            idPhieu: res.data.id,
            idHoaDon: res.data.idHoaDon 
          }
        });
      }
    } catch (error) {
      console.log(`Lỗi kiểm tra bàn ${ban.maBan}`);
    }
  }
};

// 🚨 HÀM FETCH DANH SÁCH BÀN GỘP HỢP LỆ (CÙNG KHÁCH + LÊN ĐỦ MÓN/HOẶC TRỐNG MÓN)
const fetchDanhSachBanGopHople = async () => {
  listBanCoKhachKhac.value = [];
  
  const currentCustomerId = selectedPhieu.value?.idKhachHang;
  const currentMaPhieu = selectedPhieu.value?.maDatBan;

  const cacBanCoKhach = danhSachBan.value.filter(ban => 
    getTrangThaiTheoNgay(ban.id) === 1 && ban.id !== selectedBan.value?.id
  );

  for (const ban of cacBanCoKhach) {
    try {
      const res = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${ban.id}`);
      if (res.data) {
        const targetCustomerId = res.data.idKhachHang;
        const targetMaPhieu = res.data.maPhieu || res.data.maDatBan;

        let isSameGroup = false;

        // 1. Gom nhóm theo ID Khách hàng (Ưu tiên số 1)
        if (currentCustomerId && targetCustomerId && currentCustomerId === targetCustomerId) {
            isSameGroup = true;
        } 
        // 2. Gom nhóm theo Mã Phiếu Gốc (Dành cho khách vãng lai không có ID)
        else if (currentMaPhieu && currentMaPhieu !== 'N/A' && targetMaPhieu && targetMaPhieu === currentMaPhieu) {
            isSameGroup = true;
        }

        if (isSameGroup) {
            const chiTiet = res.data.chiTiet || res.data.chiTietHoaDon || res.data.chiTietMonAn || [];
            const monCuaBan = chiTiet.filter(m => m.trangThaiMon !== 0);
            
            // Cờ kiểm tra xem bàn này đã lên đủ món chưa
            const allServed = monCuaBan.length === 0 || monCuaBan.every(m => m.trangThaiMon === 2);
            
            // 🚨 SỬA LẠI ĐOẠN NÀY: CHỈ TÍNH TIỀN MÓN - GIẢM GIÁ - CỌC, BỎ HOÀN TOÀN VAT
            const tongTienMon = monCuaBan.reduce((sum, item) => sum + (item.donGia * item.soLuong), 0);
            const canThu = tongTienMon - (res.data.soTienDaGiam || 0) - (res.data.tienCoc || 0);

            listBanCoKhachKhac.value.push({
              ...ban,
              _hoaDonInfo: {
                idHoaDon: res.data.idHoaDon,
                idPhieu: res.data.id,
                idBanAn: ban.id,
                // Gán tiền thuần vào đây để hiển thị ở nút bấm
                canThu: canThu > 0 ? canThu : 0, 
                allServed: allServed
              }
            });
        }
      }
    } catch (error) {
      console.log(`Lỗi kiểm tra bàn ${ban.maBan}`);
    }
  }
};

const openManageModal = async (ban, forceStatus = null, targetKhach = null) => {
  const bId = ban.id || ban.idBanAn;
  
  selectedBan.value = { 
    ...ban,
    trangThai: forceStatus !== null ? forceStatus : getTrangThaiTheoNgay(bId) 
  };
  
  modalView.value = 'info';
  activeHoaDonId.value = null;
  listMonDaChon.value = [];
  listBanGop.value = [];
  listHoaDonGop.value = [];

  try {
    const maPhieuCuaKhach = targetKhach ? (targetKhach.idDatBan || targetKhach.idPhieu || targetKhach.id) : null;
    let apiUrl = `/hoa-don-thanh-toan/active-by-ban/${bId}`;
    
    if (maPhieuCuaKhach) {
        apiUrl += `?idPhieu=${maPhieuCuaKhach}`; 
    }
    
    const resPhieu = await axiosClient.get(apiUrl);
    
    if (resPhieu.data) {
      const data = resPhieu.data;
      
      if (maPhieuCuaKhach && data.id !== maPhieuCuaKhach) {
        handleEmptyTableState(targetKhach);
      } else {
        // 🚨 CHÚ Ý ĐOẠN NÀY: Cập nhật selectedPhieu an toàn hơn
        selectedPhieu.value = {
          id: targetKhach ? maPhieuCuaKhach : data.id, 
          idHoaDon: data.idHoaDon, // CÓ THỂ LÀ NULL (BÌNH THƯỜNG)
          maDatBan: targetKhach ? (targetKhach.maDatBan || targetKhach.maPhieu) : (data.maPhieu || data.maDatBan || 'N/A'),
          tenKhachHang: targetKhach ? targetKhach.tenKhachHang : (data.tenKhachHang || 'Khách tại quán'),
          idKhachHang: targetKhach ? targetKhach.idKhachHang : data.idKhachHang, 
          thoiGianDat: targetKhach ? targetKhach.thoiGianDat : data.thoiGianDat,
          soNguoi: targetKhach ? (targetKhach.soNguoi || targetKhach.soLuongKhach) : data.soNguoi,
          tongTienChuaGiam: data.tongTienChuaGiam || 0,
          soTienDaGiam: data.soTienDaGiam || 0,
          tienCoc: targetKhach ? targetKhach.tienCoc : (data.tienCoc || 0),
          tongTienThanhToan: data.tongTienThanhToan || 0,
          vatApDung: data.vatApDung ?? 10,
          // 🚨 THÊM TRƯỜNG NÀY: Để UI biết được trạng thái thực sự của phiếu
          trangThai: targetKhach ? targetKhach.trangThai : data.trangThai 
        };

        const listFromApiRaw = data.chiTiet || data.chiTietHoaDon || data.chiTietMonAn || [];
        const listFromApi = listFromApiRaw.filter(mon => mon.trangThaiMon !== 0);
        
        if (listFromApi.length > 0) {
          listMonDaChon.value = listFromApi.map((mon) => {
              const isSet = mon.type === 'SET' || mon.idSetLau != null;
              const originalId = isSet ? mon.idSetLau : mon.idChiTietMonAn;
              return {
                  dbDetailId: mon.idChiTietHd, 
                  originalId: originalId,
                  type: isSet ? 'SET' : 'FOOD', 
                  uniqueId: isSet ? `set_${originalId}` : `food_${originalId}`, 
                  name: mon.tenMon,
                  quantity: mon.soLuong,
                  price: mon.donGia || 0,
                  note: mon.ghiChu || '',
                  served: mon.trangThaiMon === 2 
              };
          });
        }
      }
    } else {
        handleEmptyTableState(targetKhach);
    }
  } catch (e) {
    console.log("Bàn trống hoặc API lỗi:", e);
    handleEmptyTableState(targetKhach);
  }
  
  if (getTrangThaiTheoNgay(bId) === 1) {
    await fetchDanhSachBanCungDoan();
    await fetchDanhSachBanGopHople();
  }

  isShowModal.value = true;
};


// Hàm phụ (Giữ nguyên như lúc nãy)
const handleEmptyTableState = (targetKhach) => {
    if (targetKhach) {
      selectedPhieu.value = {
        id: targetKhach.idDatBan || targetKhach.idPhieu || targetKhach.id,
        idHoaDon: null, // ĐÚNG, CHƯA CÓ HÓA ĐƠN
        maDatBan: targetKhach.maDatBan || targetKhach.maPhieu || 'N/A',
        tenKhachHang: targetKhach.tenKhachHang || 'Khách vãng lai',
        idKhachHang: targetKhach.idKhachHang || null,
        thoiGianDat: targetKhach.thoiGianDat,
        soNguoi: targetKhach.soNguoi || targetKhach.soLuongKhach || targetKhach.soLuong || 1,
        tongTienChuaGiam: targetKhach.tongTien || 0,
        soTienDaGiam: targetKhach.giamGia || 0,
        tienCoc: targetKhach.tienCoc || 0,
        tongTienThanhToan: 0,
        vatApDung: targetKhach.vatApDung ?? 10,
        trangThai: targetKhach.trangThai // QUAN TRỌNG
      };
    } else {
      selectedPhieu.value = null;
    }
}

// Hàm toggle chọn bàn gộp
const toggleGopBan = (ban) => {
  const index = listBanGop.value.findIndex(b => b.id === ban.id);
  
  if (index !== -1) {
    listBanGop.value.splice(index, 1);
    listHoaDonGop.value.splice(index, 1);
  } else {
    listBanGop.value.push(ban);
    listHoaDonGop.value.push(ban._hoaDonInfo);
  }
};

const billSummary = computed(() => {
  const tong = listMonDaChon.value.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const giam = selectedPhieu.value?.soTienDaGiam || 0;
  let sauGiam = tong - giam;
  if (sauGiam < 0) sauGiam = 0;
  
  // Dùng ?? để giữ giá trị 0% nếu bàn phụ truyền lên
  const vatRate = selectedPhieu.value?.vatApDung ?? 10; 
  
  const coc = selectedPhieu.value?.tienCoc || 0;
  
  let canThu = sauGiam - coc;
  if (canThu < 0) canThu = 0;
  
  return { tong, giam, coc, canThu, vatRate }; 
});

// Tổng tiền sau gộp
const grandTotalToPay = computed(() => {
  let tongTienCacBan = billSummary.value.canThu + (billSummary.value.coc || 0); 
  
  listHoaDonGop.value.forEach(hd => {
      tongTienCacBan += hd.canThu;
  });

  const vatRate = billSummary.value.vatRate;
  const tienVat = tongTienCacBan * (vatRate / 100);

  const tongTienSauCung = (tongTienCacBan + tienVat) - (billSummary.value.coc || 0);
  return tongTienSauCung > 0 ? tongTienSauCung : 0;
});

// BỔ SUNG BIẾN NÀY ĐỂ GIAO DIỆN KHÔNG BỊ UNDEFINED KHI .toLocaleString()
const totalVatToPay = computed(() => {
  let tongTienCacBan = billSummary.value.canThu + (billSummary.value.coc || 0); 
  listHoaDonGop.value.forEach(hd => { tongTienCacBan += hd.canThu; });
  return tongTienCacBan * ((billSummary.value.vatRate ?? 10) / 100);
});

const modalActiveFloor = ref(1);
const modalHoveredTableId = ref(null);

const modalBanTheoTang = computed(() => {
  return danhSachBan.value.filter(ban => Number(ban.soTang) === modalActiveFloor.value);
});

const openChangeTableView = () => {
  modalView.value = 'changeTable';
  modalActiveFloor.value = Number(selectedBan.value?.soTang) || 1;
};

const onHoverEmptyTable = (ban) => {
  modalHoveredTableId.value = ban.id;
  if (Number(ban.soTang) !== modalActiveFloor.value) {
    modalActiveFloor.value = Number(ban.soTang);
  }
};

const onLeaveEmptyTable = () => {
  modalHoveredTableId.value = null;
};

const closeModal = () => {
  isShowModal.value = false;
  selectedBan.value = null;
  selectedPhieu.value = null;
  listMonDaChon.value = [];
  isSelectingSecondTable.value = false;
  
  listBanGop.value = [];
  listHoaDonGop.value = [];
};

const refreshTableData = async () => {
  const tienGiamCu = selectedPhieu.value?.soTienDaGiam || 0;
  const bId = selectedBan.value.id || selectedBan.value.idBanAn;
  
  try {
    let apiUrl = `/hoa-don-thanh-toan/active-by-ban/${bId}`;
    if (selectedPhieu.value && selectedPhieu.value.id) {
        apiUrl += `?idPhieu=${selectedPhieu.value.id}`; 
    }
    
    const resPhieu = await axiosClient.get(apiUrl);
    
    if (resPhieu.data) {
      if (selectedPhieu.value) {
          selectedPhieu.value.tongTienChuaGiam = resPhieu.data.tongTienChuaGiam || 0;
          selectedPhieu.value.soTienDaGiam = resPhieu.data.soTienDaGiam || 0;
          selectedPhieu.value.tienCoc = resPhieu.data.tienCoc || 0;
          selectedPhieu.value.tongTienThanhToan = resPhieu.data.tongTienThanhToan || 0;
      }
      
      // KIỂM TRA RỚT VOUCHER
      if (tienGiamCu > 0 && (resPhieu.data.soTienDaGiam || 0) === 0) {
          Swal.fire({
             icon: 'warning',
             title: 'Đã hủy ưu đãi!',
             text: 'Tổng tiền món hiện tại không còn đủ điều kiện áp dụng mã giảm giá cũ. Hệ thống đã tự động gỡ ưu đãi này ra khỏi hóa đơn.',
             confirmButtonColor: '#7d161a',
             confirmButtonText: 'Đã hiểu'
          });
      }

      if (resPhieu.data.chiTiet) {
        listMonDaChon.value = resPhieu.data.chiTiet.filter(m => m.trangThaiMon !== 0).map((mon) => {
            const isSet = mon.type === 'SET' || mon.idSetLau != null;
            const originalId = isSet ? mon.idSetLau : mon.idChiTietMonAn;
            return {
                dbDetailId: mon.idChiTietHd, 
                originalId: originalId,
                type: isSet ? 'SET' : 'FOOD', 
                uniqueId: isSet ? `set_${originalId}` : `food_${originalId}`, 
                name: mon.tenMon,
                quantity: mon.soLuong,
                price: mon.donGia || 0,
                note: mon.ghiChu || '',
                served: mon.trangThaiMon === 2 
            };
        });
      }
    }
  } catch (error) {
    console.error("Lỗi đồng bộ", error);
  }
};

const handleCloseFoodList = async () => {
  modalView.value = 'info'; 
  setTimeout(() => {
      const modalBody = document.querySelector('.modal-body-custom');
      if (modalBody) modalBody.scrollTop = 0;
  }, 50);
  await refreshTableData();
};

const hoveredTableId = ref(null);

const onHoverCustomerCard = (khach) => {
  const ban = danhSachBan.value.find(b => b.id === khach.idBanAn || b.maBan === khach.maBan);
  if (ban) {
    hoveredTableId.value = ban.id;
    if (Number(ban.soTang) !== activeFloor.value) {
      activeFloor.value = Number(ban.soTang);
    }
  }
};

const onLeaveCustomerCard = () => {
  hoveredTableId.value = null;
};

const handleDirectCheckIn = async () => {
  if (!selectedBan.value) return;
  const bId = selectedBan.value.id;
  const payload = {
    idBanAn: bId, 
    trangThai: 1, 
    id: selectedPhieu.value?.id || null, 
    trangThaiPhieu: selectedPhieu.value?.id ? 3 : null 
  };
  try {
    await updateTrangThaiBan(payload);
    tableStatusMap.value = { ...tableStatusMap.value, [bId]: 1 };
    selectedBan.value.trangThai = 1; 
    
    Swal.fire({ icon: 'success', title: 'Thành công!', text: 'Đã check-in bàn!', timer: 1500, showConfirmButton: false });
    
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();

    // 🚨 THÊM DÒNG NÀY (THE FIX): Tự động load lại dữ liệu của chính bàn này 
    // để lấy được idHoaDon vừa được Backend tạo ra -> Các nút sẽ lập tức sáng lên!
    await openManageModal(selectedBan.value);

  } catch (err) {
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Check-in thất bại!' });
  }
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

const switchToAddFood = () => {
  modalView.value = "addFood";
};

const totalTempPrice = computed(() => {
  return listMonDaChon.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
});

const handleSaveFood = async (itemsArray) => {
  if (!selectedPhieu.value?.id) {
    Swal.fire('Lưu ý', 'Bàn này chưa có Phiếu đặt trước. Không hỗ trợ thêm món!', 'warning');
    return;
  }
  if (itemsArray.length === 0) {
    listMonDaChon.value = [];
    modalView.value = 'info';
    return;
  }
  try {
    const payload = {
      idHoaDon: selectedPhieu.value?.idHoaDon || null, 
      idBanAn: selectedBan.value.id,
      idNhanVien: getCurrentStaffId() || 1,
      idKhachHang: selectedPhieu.value?.idKhachHang || null,
      chiTietHoaDon: itemsArray.map(item => ({
        id: item.dbDetailId || null, 
        idChiTietMonAn: item.type === 'FOOD' ? item.originalId : null,
        idSetLau: item.type === 'SET' ? item.originalId : null,
        soLuong: item.quantity,
        ghiChu: item.note || '' 
      }))
    };
    await createOrder(payload);
    Swal.fire({ icon: 'success', title: 'Thành công!', text: "Đã cập nhật thực đơn!", timer: 1500, showConfirmButton: false });
    handleCloseFoodList();
  } catch (e) {
    Swal.fire({ icon: 'error', title: 'Lỗi', text: "Lỗi lưu món" });
  }
};

const handlePaymentCash = async () => {
  const amountToPay = grandTotalToPay.value; 
  const confirm = await Swal.fire({
    title: 'Xác nhận thanh toán TIỀN MẶT?',
    html: `Tổng tiền (đã bao gồm các bàn gộp): <br><br>
           <b style="color: #28a745; font-size: 26px">${amountToPay.toLocaleString()} đ</b>`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#28a745', 
    confirmButtonText: 'Đã nhận đủ tiền',
    cancelButtonText: 'Quay lại'
  });

  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    
    // Gửi yêu cầu thanh toán bàn chính
    const payloadChinh = {
      idBanAn: selectedBan.value.id,
      trangThai: 0,
      id: selectedPhieu.value.id,
      trangThaiPhieu: 4, // 🚨 Trạng thái phiếu 4: Hoàn thành
      trangThaiHoaDon: 6, // Hóa đơn 6: Đã thanh toán
      tienMat: billSummary.value.canThu, 
      ghiChu: `Thanh toán Tiền mặt`
    };
    await updateTrangThaiBan(payloadChinh);

    // Gửi yêu cầu thanh toán các bàn phụ gộp chung
    if (listHoaDonGop.value.length > 0) {
      await Promise.all(listHoaDonGop.value.map(hd => {
        return updateTrangThaiBan({
          idBanAn: hd.idBanAn,
          trangThai: 0,
          id: hd.idPhieu,
          trangThaiPhieu: 4, // 🚨 Bàn phụ cũng về 4: Hoàn thành
          trangThaiHoaDon: 6, // Hóa đơn phụ về 6
          tienMat: hd.canThu,
          ghiChu: `Gộp chung với ${selectedBan.value.maBan}`
        });
      }));
    }

    Swal.fire({ icon: 'success', title: 'Thành công!', text: 'Đã thanh toán toàn bộ!', timer: 1500 });
    closeModal();
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();
  } catch (err) {
    Swal.fire('Lỗi', 'Lỗi khi thanh toán', 'error');
  }
};

const customerOrderUrl = computed(() => {
  if (!selectedBan.value || !selectedPhieu.value?.idHoaDon) return '';
  
  const baseUrl = window.location.origin; // Lấy http://localhost:5173
  
  // Link sẽ có dạng: http://localhost:5173/menu-khach?ban=3&hoadon=15
  return `${baseUrl}/menu-khach?ban=${selectedBan.value.id}&hoadon=${selectedPhieu.value.idHoaDon}`;
});

// 2. Hàm copy link gửi cho khách qua Zalo/Mess (nếu không tiện quét)
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    Swal.fire({ 
      icon: 'success', 
      title: 'Đã copy link!', 
      text: 'Bạn có thể dán gửi cho khách',
      timer: 1500, 
      showConfirmButton: false 
    });
  } catch (err) {
    Swal.fire('Lỗi', 'Không thể copy link', 'error');
  }
};

const handlePaymentVNPayFull = async () => {
  const amountToPay = grandTotalToPay.value;
  const confirm = await Swal.fire({
    title: 'Thanh toán VNPay Gộp?',
    text: `Chuyển cổng VNPay với tổng tiền ${amountToPay.toLocaleString()} đ. Đồng ý?`,
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#007bff',
    confirmButtonText: 'Tiếp tục'
  });

  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang kết nối VNPay...', didOpen: () => Swal.showLoading() });
    let chuoiIdHoaDon = String(selectedPhieu.value.idHoaDon);
    if (listHoaDonGop.value.length > 0) {
      const idsPhu = listHoaDonGop.value.map(hd => hd.idHoaDon).join("_");
      chuoiIdHoaDon += "_" + idsPhu;
    }

    await updateTrangThaiBan({ idBanAn: selectedBan.value.id, trangThai: 1, id: selectedPhieu.value.id, trangThaiPhieu: 3, trangThaiHoaDon: 5, tienMat: 0 });
    if (listHoaDonGop.value.length > 0) {
      await Promise.all(listHoaDonGop.value.map(hd => 
        updateTrangThaiBan({ idBanAn: hd.idBanAn, trangThai: 1, id: hd.idPhieu, trangThaiPhieu: 3, trangThaiHoaDon: 5, tienMat: 0 })
      ));
    }
    closeModal();
    const vnpayRes = await axiosClient.get(`/vnpay/create-payment/${chuoiIdHoaDon}?amount=${amountToPay}`);
    if (vnpayRes.data && vnpayRes.data.url) {
      window.location.href = vnpayRes.data.url;
    }
  } catch (err) {
    Swal.fire('Lỗi', 'Khởi tạo VNPay thất bại', 'error');
  }
};

const handlePaymentMixed = async () => {
  const totalInvoice = grandTotalToPay.value;
  const { value: cashAmountStr } = await Swal.fire({
    title: 'Thanh toán hỗn hợp (Gộp)',
    input: 'number',
    inputLabel: `Tổng gộp: ${totalInvoice.toLocaleString()}đ`,
    inputPlaceholder: 'Nhập số tiền mặt khách đưa trước...',
    showCancelButton: true,
    confirmButtonText: 'Tiếp tục sang VNPay',
    inputValidator: (value) => {
      if (!value || value <= 0) return 'Vui lòng nhập số hợp lệ!';
      if (value >= totalInvoice) return 'Tiền mặt phải nhỏ hơn tổng bill!';
    }
  });

  if (cashAmountStr) {
    const cashAmount = Number(cashAmountStr);
    const remaining = totalInvoice - cashAmount;

    try {
      Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
      let chuoiIdHoaDon = String(selectedPhieu.value.idHoaDon);
      if (listHoaDonGop.value.length > 0) {
        const idsPhu = listHoaDonGop.value.map(hd => hd.idHoaDon).join("_");
        chuoiIdHoaDon += "_" + idsPhu;
      }

      await updateTrangThaiBan({ idBanAn: selectedBan.value.id, trangThai: 1, id: selectedPhieu.value.id, trangThaiPhieu: 3, trangThaiHoaDon: 5, idNhanVien: getCurrentStaffId(), tienMat: cashAmount, ghiChu: `Thu hỗn hợp ${cashAmount}đ` });
      
      if (listHoaDonGop.value.length > 0) {
        await Promise.all(listHoaDonGop.value.map(hd => 
          updateTrangThaiBan({ idBanAn: hd.idBanAn, trangThai: 1, id: hd.idPhieu, trangThaiPhieu: 3, trangThaiHoaDon: 5, tienMat: 0 })
        ));
      }

      const vnpayRes = await axiosClient.get(`/vnpay/create-payment/${chuoiIdHoaDon}?amount=${remaining}`);
      if (vnpayRes.data && vnpayRes.data.url) window.location.href = vnpayRes.data.url;
    } catch (error) {
      Swal.fire('Lỗi', 'Lỗi thanh toán', 'error');
    }
  }
};

const handleCancelTicket = async () => {
  if (!selectedPhieu.value?.id) return Swal.fire('Lưu ý', 'Bàn này chưa có phiếu để hủy!', 'warning');
  const confirm = await Swal.fire({ title: 'Hủy phiếu đặt bàn?', text: 'Phiếu sẽ bị hủy và bàn sẽ được dọn trống. Bạn chắc chắn chứ?', icon: 'warning', showCancelButton: true, confirmButtonColor: '#d33', confirmButtonText: 'Đồng ý hủy', cancelButtonText: 'Quay lại' });
  
  if (!confirm.isConfirmed) return;
  
  const bId = selectedBan.value.id;
  await updateTrangThaiBan({ 
      idBanAn: bId, 
      trangThai: 0, // Trả bàn về trạng thái Trống
      id: selectedPhieu.value.id, 
      trangThaiPhieu: 2, // 🚨 SỬA Ở ĐÂY: 2 = Đã hủy
      trangThaiHoaDon: 8 // Hóa đơn trạng thái Hủy
  });
  
  tableStatusMap.value = { ...tableStatusMap.value, [bId]: 0 };
  closeModal();
  await handleFetchAllCheckIn();
  await fetchAllBan();
  Swal.fire({ icon: 'success', title: 'Đã hủy', timer: 1000 });
};

const activeBookingTableIds = computed(() => {
  const bookingIds = danhSachLoc.value.map(khach => {
    const ban = danhSachBan.value.find(b => b.id === khach.idBanAn || b.maBan === khach.maBan);
    return ban ? ban.id : null;
  });
  const occupiedIds = danhSachBan.value.filter(ban => getTrangThaiTheoNgay(ban.id) === 1).map(ban => ban.id);
  return [...new Set([...bookingIds, ...occupiedIds])].filter(id => id !== null);
});

watch(selectedDate, async () => { await fetchTableStatus(); }, { immediate: true });

const props = defineProps({
    initialItems: { type: Array, default: () => [] }
});

const initSelectedItems = () => {
    selectedItems.value = {};
    if (props.initialItems && props.initialItems.length > 0) {
        props.initialItems.forEach(item => {
            const key = item.uniqueId;
            if (key) selectedItems.value[key] = { ...item };
        });
    }
};

const listBanTrong = computed(() => {
  return danhSachBan.value.filter(ban => getTrangThaiTheoNgay(ban.id) === 0 && ban.id !== selectedBan.value?.id);
});

const isSelectingSecondTable = ref(false);

const handleSwitchTable = async (banMoi) => {
  if (isSelectingSecondTable.value) {
    try {
      const pendingText = localStorage.getItem("pendingSplitCustomer");
      let khachHangId = null;
      let ghiChuTach = "Bàn phụ";
      let khachDangCho = 0;
      let maDatBanGoc = null; // Thêm biến này

      if (pendingText) {
         const pendingObj = JSON.parse(pendingText);
         khachHangId = pendingObj.idKhachHang || null;
         ghiChuTach = pendingObj.ghiChu || "Bàn phụ";
         khachDangCho = Number(pendingObj.soKhachCanXep) || 0;
         maDatBanGoc = pendingObj.maDatBanGoc || null; // Lấy mã phiếu gốc ra
      }

      // 🚨 ĐÂY CHÍNH LÀ NƠI GÂY RA PAYLOAD THIẾU DỮ LIỆU CỦA BẠN. ĐÃ THÊM VAT 0.0 VÀ MÃ GỐC!
      await updateTrangThaiBan({ 
          idBanAn: banMoi.id, 
          trangThai: 1, 
          id: null, 
          idKhachHang: khachHangId, 
          idNhanVien: getCurrentStaffId() || 1, 
          ghiChu: ghiChuTach,
          vatApDung: 0.0,
          maDatBanGoc: maDatBanGoc 
      });
      
      const sucChuaBanNay = Number(banMoi.soCho) || 0;
      const conLai = khachDangCho - sucChuaBanNay;

      if (conLai <= 0) {
          // Xếp đủ chỗ
          Swal.fire({ icon: 'success', title: 'Hoàn tất', text: 'Đã xếp đủ chỗ cho đoàn khách!', timer: 1500, showConfirmButton: false });
          isSelectingSecondTable.value = false;
          localStorage.removeItem("pendingSplitCustomer");
          closeModal();
      } else {
          // Vẫn thiếu chỗ
          if (pendingText) {
             const pendingObj = JSON.parse(pendingText);
             pendingObj.soKhachCanXep = conLai;
             localStorage.setItem("pendingSplitCustomer", JSON.stringify(pendingObj));
          }
          Swal.fire({ 
              icon: 'warning', 
              title: 'Vẫn thiếu chỗ!', 
              html: `Đoàn vẫn còn <b>${conLai} người</b> chưa có chỗ.<br>Vui lòng chọn tiếp 1 bàn trống bên dưới!`,
              confirmButtonText: 'Đã hiểu'
          });
      }

      await handleFetchAllCheckIn();
      await fetchAllBan();
      return;
    } catch (error) {
      return Swal.fire('Lỗi', 'Không thể mở bàn phụ lúc này', 'error');
    }
  }

  const soKhach = selectedPhieu.value?.soNguoi || 1;
  const sucChuaBanMoi = Number(banMoi.soCho);

  if (soKhach > sucChuaBanMoi) {
    const swalResult = await Swal.fire({
      title: 'Bàn mới không đủ chỗ!',
      html: `Đoàn có <b>${soKhach} người</b> nhưng bàn <b>${banMoi.maBan}</b> chỉ có <b>${sucChuaBanMoi} chỗ</b>.<br>Chọn hướng xử lý:`,
      icon: 'warning',
      showDenyButton: true,
      showCancelButton: true,
      confirmButtonColor: '#7d161a',
      denyButtonColor: '#28a745',
      confirmButtonText: '<i class="fa-solid fa-chair"></i> Kê ghế',
      denyButtonText: '<i class="fa-solid fa-arrows-split-up-and-left"></i> Đổi & Tách bàn',
      cancelButtonText: 'Hủy'
    });

    if (swalResult.isConfirmed) {
      // Tiếp tục xuống đổi bàn
    } else if (swalResult.isDenied) {
      try {
        await updateTrangThaiBan({ id: selectedPhieu.value.id, idBanAn: selectedBan.value.id, idBanAnMoi: banMoi.id, idNhanVien: getCurrentStaffId() || 1, trangThai: 0 });
        isSelectingSecondTable.value = true;
        
        // 🚨 TÍNH TOÁN SỐ KHÁCH CÒN THIẾU KHI ĐỔI BÀN
        const soKhachConLai = soKhach - sucChuaBanMoi;

        // 🚨 THÊM soKhachCanXep VÀO LOCAL STORAGE (Lúc nãy thiếu dòng này)
        localStorage.setItem("pendingSplitCustomer", JSON.stringify({
          idKhachHang: selectedPhieu.value.idKhachHang || null, 
          ghiChu: `Bàn phụ - Tách từ ${banMoi.maBan}`,
          soKhachCanXep: soKhachConLai // Đã bổ sung biến đếm
        }));

        selectedBan.value = { ...banMoi, trangThai: 1 };
        await handleFetchAllCheckIn();
        await fetchAllBan();
        
        // Cập nhật câu thông báo cho nhân viên biết còn thiếu bao nhiêu chỗ
        Swal.fire({ 
            title: 'Đã chuyển bàn chính', 
            html: `Đã xếp ${sucChuaBanMoi} khách vào bàn ${banMoi.maBan}.<br>
                   Đoàn còn <b style="color:red">${soKhachConLai} người</b> chưa có chỗ.<br><br>
                   Vui lòng <b>chọn tiếp 1 bàn trống</b> bên dưới cho số khách còn lại.`, 
            icon: 'success' 
        });
        return; 
      } catch (error) {
        return Swal.fire('Lỗi', 'Lỗi đổi và tách bàn!', 'error');
      }
    } else {
      return; 
    }
  } else {
    const confirm = await Swal.fire({ title: 'Xác nhận đổi?', text: `Sang bàn ${banMoi.maBan}?`, icon: 'question', showCancelButton: true, confirmButtonColor: '#7d161a' });
    if (!confirm.isConfirmed) return;
  }

  try {
    await updateTrangThaiBan({ id: selectedPhieu.value.id, idBanAn: selectedBan.value.id, idBanAnMoi: banMoi.id, idNhanVien: getCurrentStaffId() || 1, trangThai: 0, vatApDung: 0.0 });
    Swal.fire({ icon: 'success', title: 'Thành công', text: 'Đổi bàn hoàn tất!', timer: 1500, showConfirmButton: false });
    closeModal();
    await handleFetchAllCheckIn();
    await fetchAllBan();
  } catch (error) {
    Swal.fire('Lỗi', 'Lỗi đổi bàn!', 'error');
  }
};

const hasItems = computed(() => {
  return listMonDaChon.value.length > 0;
});

// Biến kiểm tra các món CÓ SẴN đã lên hết chưa
const isAllItemsServed = computed(() => {
  if (listMonDaChon.value.length === 0) return true; 
  return listMonDaChon.value.every(item => item.served);
});

const quickSwitchTable = async (targetBan) => {
  const targetKhachInfo = {
    id: targetBan._hoaDonInfo.idPhieu, 
    idDatBan: targetBan._hoaDonInfo.idPhieu, // Cần truyền ID phiếu để Backend load dữ liệu mới
    idKhachHang: selectedPhieu.value.idKhachHang,
    tenKhachHang: selectedPhieu.value.tenKhachHang,
    maDatBan: targetBan.maBan, // Hiện tạm mã bàn để không bị N/A
    trangThai: 3,
    thoiGianDat: selectedPhieu.value?.thoiGianDat
  };
  
  // Gọi lại hàm mở Modal với bàn mới
  await openManageModal(targetBan, null, targetKhachInfo);
};

// 2. Mở khóa thanh toán cho tất cả các bàn (Xóa điều kiện VAT)
const canPay = computed(() => {
  // 1. Phải là bàn chính mới được thu tiền
  const isPrimaryTable = (selectedPhieu.value?.vatApDung || 0) > 0;
  
  // 2. Bàn hiện tại phải lên đủ món
  const currentTableServed = hasItems.value && isAllItemsServed.value;
  
  // 3. 🚨 Các bàn phụ ĐƯỢC TÍCH CHỌN GỘP cũng phải lên đủ món
  const subTablesServed = listBanGop.value.every(ban => ban._hoaDonInfo.allServed);
  
  return currentTableServed && isPrimaryTable && subTablesServed;
});

const primaryTableInGroup = computed(() => {
  return listBanCoKhachKhac.value.find(b => b._hoaDonInfo.idPhieu !== selectedPhieu.value?.id);
});


const toggleItemServed = async (item) => {
  try {
    const newStatus = item.served ? 1 : 2; 
    await axiosClient.put(`/dat-ban/chi-tiet-hoa-don/${item.dbDetailId}/trang-thai?trangThai=${newStatus}&idNhanVien=${getCurrentStaffId()||1}`);
    item.served = !item.served;
  } catch (error) {
    Swal.fire('Lỗi', 'Không thể cập nhật!', 'error');
  }
};

const markAllServed = async () => {
  const unservedItems = listMonDaChon.value.filter(item => !item.served);
  if (unservedItems.length === 0) return;
  
  try {
    Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    
    // 🚨 THAY THẾ PROMISE.ALL BẰNG VÒNG LẶP FOR...OF
    for (const item of unservedItems) {
      await axiosClient.put(`/dat-ban/chi-tiet-hoa-don/${item.dbDetailId}/trang-thai?trangThai=2&idNhanVien=${getCurrentStaffId()||1}`);
    }
    
    listMonDaChon.value.forEach(item => item.served = true);
    Swal.fire({ icon: 'success', title: 'Thành công', timer: 1000, showConfirmButton: false });
  } catch (error) {
    Swal.fire('Lỗi', 'Lỗi khi cập nhật món!', 'error');
  }
};

const handleDeleteItem = async (item, index) => {
  const confirm = await Swal.fire({ title: 'Xóa món?', icon: 'warning', showCancelButton: true, confirmButtonColor: '#dc3545', confirmButtonText: 'Xóa' });
  if (confirm.isConfirmed) {
    try {
      await axiosClient.put(`/dat-ban/chi-tiet-hoa-don/${item.dbDetailId}/trang-thai?trangThai=0`);
      listMonDaChon.value.splice(index, 1);
      Swal.fire({ icon: 'success', title: 'Đã xóa!', timer: 1000, showConfirmButton: false });
      if (listMonDaChon.value.length === 0) modalView.value = 'info';
    } catch (error) {
      Swal.fire('Lỗi', 'Không thể xóa!', 'error');
    }
  }
};

const handleCancelWaitingTicket = async (khach) => {
  const now = dayjs(currentTime.value);
  const target = dayjs(khach.thoiGianDat);
  const diffMinutes = target.diff(now, "minute");

  // Nếu còn nhiều hơn 60 phút -> Được hoàn tiền
  const isRefundable = diffMinutes > 60; 

  let title = "";
  let text = "";
  let confirmText = "";

  if (isRefundable) {
      title = "Xác nhận Hủy (Hoàn cọc)";
      text = `Khách hủy trước lịch hẹn ${Math.floor(diffMinutes/60)}h${diffMinutes%60}p. Hợp lệ để HỦY PHIẾU VÀ HOÀN TRẢ 100% tiền cọc.`;
      confirmText = "Xác nhận & hoàn tiền";
  } else {
      title = "Xác nhận Hủy (Mất cọc)";
      text = `Khách báo hủy quá sát giờ (chỉ còn ${diffMinutes} phút). Phiếu sẽ bị hủy và KHÔNG HOÀN TRẢ tiền cọc.`;
      confirmText = "Xác nhận hủy";
  }

  const confirm = await Swal.fire({
      title: title,
      text: text,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: isRefundable ? "#28a745" : "#d33", // Xanh nếu hoàn, Đỏ nếu mất cọc
      cancelButtonColor: "#6c757d",
      confirmButtonText: confirmText,
      cancelButtonText: "Đóng lại"
  });

  if (confirm.isConfirmed) {
      try {
          Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
          
          // Gọi API xuống Backend
          const idPhieuCanHuy = khach.idDatBan || khach.idPhieu || khach.id;

          if (!idPhieuCanHuy) {
              Swal.fire('Lỗi', 'Không tìm thấy ID của phiếu này!', 'error');
              return;
          }
          const payload = {
              idPhieu: idPhieuCanHuy,
              isRefundable: isRefundable,
              idNhanVien: getCurrentStaffId() || 1
          };
          
          // Tạo API mới ở Backend để xử lý vụ này
          await axiosClient.put('/hoa-don-thanh-toan/huy-phieu-cho', payload);
          
          Swal.fire({ icon: 'success', title: 'Đã hủy phiếu!', timer: 1500, showConfirmButton: false });
          
          // Tải lại danh sách
          await handleFetchAllCheckIn();
          await fetchAllBan();
          await fetchTableStatus();
      } catch (error) {
          Swal.fire('Lỗi', error.response?.data?.message || 'Không thể hủy phiếu lúc này', 'error');
      }
  }
};

const warnedTables = ref([]);

// 2. Hàm quét tìm các bàn đã ngồi quá 3 tiếng
const checkOverdueTables = async () => {
  // Lấy các bàn đang có người ngồi
  const activeTables = danhSachBan.value.filter(ban => getTrangThaiTheoNgay(ban.id) === 1);
  
  for (const ban of activeTables) {
    if (warnedTables.value.includes(ban.id)) continue; // Bỏ qua nếu đã cảnh báo rồi

    try {
      // Tận dụng API có sẵn để xem thông tin giờ vào của bàn
      const res = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${ban.id}`);
      if (res.data && res.data.thoiGianDat) {
        const thoiGianVao = dayjs(res.data.thoiGianDat);
        const now = dayjs(new Date());
        
        // Tính khoảng cách thời gian (hour, true = lấy số thập phân, vd 3.1 tiếng)
        const diffHours = now.diff(thoiGianVao, 'hour', true); 

        if (diffHours >= 3) {
          warnedTables.value.push(ban.id); // Ghi sổ đen để không báo lại nữa
          
          const confirm = await Swal.fire({
            title: 'Khách ngồi quá 3 tiếng!',
            html: `Bàn <b>${ban.maBan}</b> đã check-in từ ${thoiGianVao.format('HH:mm')}.<br><br>Vui lòng kiểm tra lại tình trạng hoặc yêu cầu thanh toán!`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#7d161a',
            cancelButtonColor: '#6c757d',
            confirmButtonText: 'Mở Modal bàn này',
            cancelButtonText: 'Để sau'
          });

          // Nếu nhân viên bấm đồng ý -> Bật luôn modal quản lý bàn đó lên
          if (confirm.isConfirmed) {
            await openManageModal(ban);
          }
        }
      }
    } catch (error) {
      // Bỏ qua lỗi nếu chưa lấy được thông tin
    }
  }
};

// 3. Tự động xóa bàn khỏi danh sách "đã cảnh báo" khi bàn đó thanh toán xong và dọn dẹp (Về trạng thái Trống)
watch(calculatedTableStatuses, (newStatuses) => {
  warnedTables.value = warnedTables.value.filter(id => newStatuses[id] !== 0);
}, { deep: true });

const isServing = computed(() => {
  return selectedBan.value && 
         selectedBan.value.trangThai === 1 && 
         selectedPhieu.value && 
         selectedPhieu.value.idHoaDon !== null;
});

watch(() => props.initialItems, () => { initSelectedItems(); }, { deep: true, immediate: true });
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search m-4">
      <h3 style="color: #7d161a; font-weight: bold; font-size: 1.5rem">
        Check-in bàn
      </h3>

      <div class="search-form">
        <div>
          <h5 style="font-size: 1rem; font-weight: bold">Khu vực</h5>
          <div class="mb-3">
            <div class="d-inline-block">
              <div v-for="tang in danhSachTang" :key="tang" class="d-inline-block me-2">
                <button
                  class="btn"
                  :class="activeFloor === tang ? 'btn-active' : 'btn-outline'"
                  @click="activeFloor = tang"
                >
                  Tầng {{ tang }}
                </button>
              </div>
            </div>

            <div class="floor-info mt-2">
              Tầng {{ activeFloor }} - Trống {{ thongKeTang.free }}/{{ thongKeTang.total }} bàn
            </div>
          </div>
        </div>
        
      </div>

      <div class="contain-frame mt-3">
        <div class="floor-frame">
          <div class="floor-map">
            <div class="floor-plan-layout">
              
              <div class="floor-plan-section">
                <div class="floor-header"></div>
                <div class="grid-container">
                  <div class="grid-canvas" @dragover.prevent :class="{ 'editing-mode': false }">
                    <div
                      v-for="ban in banTheoTang"
                      :key="ban.id"
                      class="table-card"
                      :class="{ 
                        'highlight-red': getTrangThaiTheoNgay(ban.id) === 0,
                        'is-hovered': hoveredTableId === ban.id,
                        'is-dimmed': !activeBookingTableIds.includes(ban.id) && hoveredTableId !== ban.id
                      }"
                      :style="{
                        gridColumnStart: ban.column,
                        gridRowStart: ban.row,
                        gridColumnEnd: 'span 3',
                        gridRowEnd: 'span 2',
                      }"
                      @click="handleTableClick(ban)"
                    >
                      <div class="table-content">
                        <strong>{{ ban.maBan }}</strong>
                        <div class="small">({{ ban.soCho }} chỗ)</div>
                        <div class="small">Khu vực: {{ ban.tenKhuVuc }}</div>
                        <div :class="['status-tag', getStatusClass(getTrangThaiTheoNgay(ban.id))]">
                          {{ getStatusText(getTrangThaiTheoNgay(ban.id)) }}
                        </div>
                        <div class="small">ID: {{ ban.id }} | Status: {{ getTrangThaiTheoNgay(ban.id) }}</div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div class="list-section">
                <div class="list-card">
                  <div class="checkin-container">
                    <h5 style="font-weight: bold; font-size: 16px">Khách chờ check-in</h5>
                    <div class="filter-section">
                      <div class="filter-group">
                        <label class="filter-label"><i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm</label>
                        <div class="filter-input-wrapper">
                          <input type="text" v-model="searchQuery" placeholder="SĐT khách hàng" />
                        </div>
                      </div>
                      <div class="filter-time-group">
                        <label class="filter-label-v2">
                          <i class="fa-solid fa-clock-rotate-left me-1"></i> Hiển thị phiếu đặt trong:
                        </label>
                        <div class="btn-group-custom">
                          <button 
                            v-for="opt in timeWindowOptions" 
                            :key="opt.value"
                            class="time-btn"
                            :class="{ 'active': selectedTimeWindow === opt.value }"
                            @click="setTimeWindow(opt.value)"
                          >
                            {{ opt.label }}
                          </button>
                        </div>
                      </div>
                      <div class="filter-group">
                        <label class="filter-label"><i class="fa-solid fa-calendar-days"></i> Lọc theo ngày đến</label>
                        <div class="filter-input-wrapper">
                          <input type="date" v-model="filterDate" class="date-input" />
                        </div>
                      </div>
                    </div>

                    <div class="list-waiting">
                      <p v-if="danhSachLoc.length === 0" class="text-center text-muted mt-3">
                        Không có khách nào thỏa mãn tìm kiếm
                      </p>
                      <div v-for="khach in danhSachLoc" :key="khach.id" class="customer-card" @mouseenter="onHoverCustomerCard(khach)" @mouseleave="onLeaveCustomerCard">
                        <div class="card-header">
                          <span class="customer-name">{{ khach.tenKhachHang }}</span>
                          <span class="table-badge">{{ khach.maBan }}</span>
                        </div>
                        <div class="card-body">
                          <p class="time-info"><i class="fa-regular fa-calendar me-2"></i>{{ formatDate(khach.thoiGianDat) }}</p>
                          
                          <div class="card-footer mb-3 d-flex gap-2"> <button
                            class="btn btn-checkable flex-grow-1"
                            :disabled="checkTimeStatus(khach.thoiGianDat).isEarly"
                            :style="checkTimeStatus(khach.thoiGianDat).isEarly ? 'background-color: #6c757d !important; cursor: not-allowed; opacity: 0.7; color: white !important;' : ''"
                            @click="handleCheckInGuest(khach)"
                          >
                            <i class="fa-solid" :class="checkTimeStatus(khach.thoiGianDat).isEarly ? 'fa-clock' : 'fa-check'"></i>
                            <span class="ms-1">
                              {{ checkTimeStatus(khach.thoiGianDat).isEarly ? `Còn ${checkTimeStatus(khach.thoiGianDat).minutes}p` : "Check-in" }}
                            </span>
                          </button>

                          <button 
                            class="btn btn-outline-danger px-3 d-flex align-items-center justify-content-center" 
                            style="border-radius: 8px;"
                            @click="handleCancelWaitingTicket(khach)" 
                            title="Hủy phiếu này"
                          >
                            <i class="fa-solid fa-trash-can"></i>
                          </button>

                        </div>
                          <div class="d-flex justify-content-between align-items-center mt-2 pt-2 border-top">
                            <span class="fw-bold" :class="getCountdown(khach.thoiGianDat).startsWith('-') ? 'text-danger' : 'text-danger'">
                              <i class="fa-regular fa-clock me-1"></i>
                              {{ getCountdown(khach.thoiGianDat).startsWith('-') ? 'Đã trễ hẹn:' : 'Sắp đến sau:' }}
                            </span>
                            
                            <span class="badge px-2 py-1 fs-6" 
                                  :class="getCountdown(khach.thoiGianDat).startsWith('-') ? 'bg-danger' : 'bg-danger'">
                              {{ getCountdown(khach.thoiGianDat).replace('-', '') }}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-if="isShowModal" class="modal-overlay">
    <div class="modal-box" :class="{ 'modal-fullscreen': ['addFood', 'changeTable'].includes(modalView) }">
      <div class="modal-header-custom">
        <h4 class="modal-title-custom">
          <template v-if="modalView === 'info'">Check-in bàn {{ selectedBan?.maBan }}</template>
          <template v-else-if="modalView === 'addFood'">Thêm món ăn - Bàn {{ selectedBan?.maBan }}</template>
          <template v-else-if="modalView === 'viewOrder'">Xem đơn hàng - Bàn {{ selectedBan?.maBan }}</template>
          <template v-else-if="modalView === 'orderHistory'">Lịch sử đơn - Bàn {{ selectedBan?.maBan }}</template>
          <template v-else-if="modalView === 'changeTable'">Đổi bàn / Tách bàn</template>
          <template v-else-if="modalView === 'viewQR'">QR Code Gọi Món</template>
        </h4>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>

      <div class="modal-body-custom p-3 d-flex flex-column h-100">
        
        <div v-if="modalView === 'info'" class="h-100 d-flex flex-column">
            <div class="flex-grow-1 overflow-auto pe-2">
                <div class="quick-switch-wrapper mb-3" v-if="danhSachBanCungDoan.length > 0">
                    <div class="group-tabs-container">
                        <small class="text-muted fw-bold d-block mb-1"><i class="fa-solid fa-layer-group me-1"></i> Các bàn trong đoàn:</small>
                        <div class="d-flex gap-2 flex-wrap">
                        <button class="btn btn-sm px-3 quick-tab-btn active" disabled>{{ selectedBan?.maBan }}</button>
                        <button v-for="ban in danhSachBanCungDoan" :key="ban.id" class="btn btn-sm btn-outline-secondary px-3 quick-tab-btn" @click="quickSwitchTable(ban)">{{ ban.maBan }}</button>
                        </div>
                    </div>
                </div>
                
                <div v-if="selectedPhieu" class="alert alert-danger p-3 mb-3 border-0 shadow-sm" style="background-color: #fff5f5; border-left: 5px solid #7d161a !important;">
                    <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <div class="mb-1"><i class="fa-solid fa-ticket me-2"></i><strong>Mã phiếu:</strong> {{ selectedPhieu?.maDatBan || 'N/A' }}</div>
                        <div class="mb-1"><i class="fa-solid fa-user me-2"></i><strong>Khách:</strong> {{ selectedPhieu?.tenKhachHang }}</div>
                        <div><i class="fa-solid fa-clock me-2"></i><strong>Giờ vào:</strong> {{ formatDate(selectedPhieu?.thoiGianDat) }}</div>
                    </div>
                    <span class="badge bg-danger p-2">ĐÃ ĐẶT CHỖ</span>
                    </div>
                </div>

                <h6 class="section-title">Thông tin bàn</h6>
                <div class="info-row"><span>Mã bàn:</span><strong>{{ selectedBan?.maBan }}</strong></div>
                <div class="info-row"><span>Sức chứa:</span><strong>{{ selectedBan?.soCho }} người</strong></div>
                <div class="info-row align-items-center"><span>Trạng thái:</span><span class="badge-status">{{ getStatusText(selectedBan?.trangThai) }}</span></div>
                <div class="info-row"><span>Vị trí:</span><strong>Tầng {{ selectedBan?.soTang }}</strong></div>
                <div class="info-row"><span>Nhân viên:</span><strong>{{ currentStaffName || 'Chưa xác định' }}</strong></div>

                <div v-if="listMonDaChon.length > 0" class="selected-summary mt-3 shadow-sm">
                    <div class="d-flex justify-content-between mb-2">
                    <span class="text-success fw-bold"><i class="fa-solid fa-utensils me-1"></i> Thực đơn đã chọn:</span>
                    <span class="badge bg-success">{{ listMonDaChon.length }} món</span>
                    </div>
                    <ul class="summary-list mb-3">
                    <li v-for="item in listMonDaChon" :key="item.id">{{ item.name }} <span class="text-muted">x{{ item.quantity }}<span class="text-muted" style="float: right;"><strong>{{ (item.price * item.quantity).toLocaleString() }} đ</strong></span></span></li>
                    </ul>
                    <div class="financial-breakdown border-top pt-2 mt-2">
                    <div class="d-flex justify-content-between text-muted small mb-1"><span>Tạm tính (Tiền món):</span><span>{{ (billSummary?.tong || 0).toLocaleString() }} đ</span></div>
                    <div v-if="(billSummary?.giam || 0) > 0" class="d-flex justify-content-between align-items-center text-success small mb-1">
                        <span>Khuyến mãi / Giảm giá:</span>
                        <div class="d-flex align-items-center">
                            <span class="fw-bold me-2">- {{ (billSummary?.giam || 0).toLocaleString() }} đ</span>
                            <button class="btn btn-sm btn-outline-danger py-0 px-1" style="font-size: 10px;" @click="removeVoucher" title="Hủy mã này">
                                <i class="fa-solid fa-xmark"></i>
                            </button>
                        </div>
                    </div>                    
                    <div class="d-flex justify-content-between text-muted small mb-1"><span>Thuế VAT:</span><span>({{ billSummary?.vatRate || 0 }}% tổng gộp)</span></div>
                    <div v-if="(billSummary?.coc || 0) > 0" class="d-flex justify-content-between text-primary small mb-1 fw-bold"><span><i class="fa-solid fa-piggy-bank me-1"></i> Khách đã cọc:</span><span>- {{ (billSummary?.coc || 0).toLocaleString() }} đ</span></div>
                    <div class="d-flex justify-content-between mt-2 pt-2 border-top border-2">
                        <span class="fw-bold text-dark text-uppercase" style="font-size: 14px;">Tiền món bàn này:</span>
                        <span class="fw-bold text-danger" style="font-size: 16px;">{{ (billSummary?.canThu || 0).toLocaleString() }} đ</span>
                    </div>

                    <div v-if="(billSummary?.giam || 0) === 0" class="mt-3">
                        <button class="btn btn-outline-danger w-100 fw-bold" style="border-radius: 8px; font-size: 13px;" @click="openDiscountModal">
                            <i class="fa-solid fa-tags me-1"></i> Áp dụng ưu đãi
                        </button>
                    </div>
                    </div>
                </div>

                <hr class="my-3" />

                <div v-if="selectedPhieu?.id" class="action-buttons">
                    <button :disabled="!isServing" class="btn-action" :class="{ 'has-items': listMonDaChon.length > 0 }" @click="switchToAddFood"><i class="fa-solid" :class="listMonDaChon.length > 0 ? 'fa-pen-to-square' : 'fa-plus'"></i><span v-if="listMonDaChon.length === 0">Thêm món</span><span v-else> Đã chọn {{ listMonDaChon.length }} món </span></button>
                    <button :disabled="!isServing" class="btn-action" @click="modalView = 'viewQR'"><i class="fa-solid fa-qrcode me-1"></i> QR đặt món</button>
                    <button :disabled="!isServing" class="btn-action" @click="modalView = 'viewOrder'"><i class="fa-solid fa-receipt me-1"></i> Xem đơn hàng</button>
                    <button :disabled="!selectedPhieu?.idHoaDon || !isServing" class="btn-action" @click="fetchOrderHistory"><i class="fa-solid fa-clock-rotate-left me-1"></i> Lịch sử hóa đơn</button>
                    <button :disabled="!isServing" class="btn-action" @click="openChangeTableView"><i class="fa-solid fa-repeat me-1"></i> Đổi bàn</button>
                </div>
                <div v-else class="text-center text-muted mb-3"><small><i>Bàn trống. Cần Check-in phiếu đặt để thực hiện các thao tác thêm món.</i></small></div>

                <hr class="my-3" />
                <h6 class="section-title mb-3">Thao tác tiếp đón</h6>
                
                <div class="d-flex gap-2">
                    <button v-if="getTrangThaiTheoNgay(selectedBan?.id) !== 1" class="btn btn-update-status flex-grow-1" @click="handleDirectCheckIn"><i class="fa-solid fa-bell-concierge me-2"></i> Xác nhận Check-in</button>
                    <div v-else class="alert alert-success p-2 mb-0 text-center flex-grow-1 d-flex align-items-center justify-content-center" style="background-color: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; border-radius: 8px;"><i class="fa-solid fa-check-circle me-2"></i> Bàn đã Check-in</div>
                    <button v-if="selectedPhieu?.id" class="btn px-4" style="background-color: #fff; color: #dc3545; border: 1px solid #dc3545; font-weight: bold; border-radius: 8px;" @click="handleCancelTicket"><i class="fa-solid fa-ban me-1"></i> Hủy phiếu</button>
                </div>

                <div v-if="getTrangThaiTheoNgay(selectedBan?.id) === 1 && selectedPhieu?.id" class="payment-group mt-4 pt-3 border-top">
                    <h6 class="section-title mb-2">Quyết toán hóa đơn</h6>
                    <div v-if="selectedPhieu?.idHoaDon && (selectedPhieu?.vatApDung || 0) === 0" class="alert alert-warning py-2 px-3 text-center mb-3 shadow-sm" style="font-size: 13px; border-radius: 8px; border-left: 5px solid #ffc107; background-color: #fffbe6;"><i class="fa-solid fa-lock me-1"></i> Đây là <b>Bàn phụ (0% VAT)</b>. Vui lòng chuyển sang bàn chính để thanh toán gộp.</div>
                    <div v-if="!hasItems" class="alert alert-danger py-2 px-3 text-center" style="font-size: 13px; border-radius: 8px; background-color: #fff1f0; border: 1px solid #ffccc7; color: #cf1322;"><i class="fa-solid fa-cart-arrow-down me-1"></i> Bàn chưa có món nào! Vui lòng <strong>Thêm món</strong> trước khi thanh toán.</div>
                    <div v-if="hasItems && !isAllItemsServed" class="alert alert-warning py-2 px-3 text-center" style="font-size: 13px; border-radius: 8px;"><i class="fa-solid fa-triangle-exclamation me-1"></i> Vui lòng vào <strong>"Xem đơn hàng"</strong> và xác nhận đã lên đủ món để mở khóa thanh toán.</div>

                    <div v-if="getTrangThaiTheoNgay(selectedBan?.id) === 1 && selectedPhieu?.id" class="merge-payment-box mb-3 p-3 rounded" style="background-color: #fff5f5; border: 1px dashed #7d161a;">
                        <div class="d-flex justify-content-between align-items-center mb-2"><h6 class="fw-bold mb-0" style="color: #7d161a;"><i class="fa-solid fa-link me-1"></i> Gộp bàn thanh toán</h6><span class="badge rounded-pill" style="background-color: #7d161a;">{{ listBanGop.length }} bàn đã chọn</span></div>
                        <div v-if="listBanCoKhachKhac.length > 0">
                            <p class="small text-muted mb-2">Các bàn sau có cùng khách và đủ điều kiện gộp:</p>
                            <div class="d-flex gap-2 flex-wrap">
                                <button v-for="ban in listBanCoKhachKhac" :key="ban.id" class="btn btn-sm d-flex flex-column align-items-center justify-content-center p-2 transition-all custom-merge-btn" style="min-width: 80px; border-radius: 8px;" :class="listBanGop.some(b => b.id === ban.id) ? 'is-selected shadow' : 'bg-white'" @click="toggleGopBan(ban)"><span class="fw-bold fs-6">{{ ban.maBan }}</span><small style="font-size: 11px;">+{{ ban._hoaDonInfo.canThu.toLocaleString() }}đ</small></button>
                            </div>
                            <div v-if="listBanGop.length > 0" class="mt-3 pt-2 border-top">
                                <div class="d-flex justify-content-between align-items-center mb-1"><span class="text-muted small">Cộng dồn tiền món:</span><span class="text-dark small fw-bold">{{ (((grandTotalToPay || 0) + (billSummary?.coc || 0)) - (totalVatToPay || 0)).toLocaleString() }} đ</span></div>
                                <div class="d-flex justify-content-between align-items-center mb-2"><span class="text-muted small">VAT ({{ billSummary?.vatRate || 0 }}%):</span><span class="text-dark small fw-bold">+{{ (totalVatToPay || 0).toLocaleString() }} đ</span></div>
                                <div class="d-flex justify-content-between align-items-center border-top pt-2" style="border-top-color: #7d161a !important;"><span class="text-dark fw-bold" style="font-size: 14px;">TỔNG THANH TOÁN:</span><span class="text-danger fw-bold fs-5">{{ (grandTotalToPay || 0).toLocaleString() }} đ</span></div>
                            </div>
                        </div>
                        <div v-else class="text-center py-2"><small class="text-muted"><i class="fa-solid fa-circle-info me-1"></i>Hiện không có bàn liên quan đủ điều kiện để gộp.</small></div>
                    </div>

                    <div class="payment-grid" :class="{ 'disabled-payment': !canPay }">
                    <button class="btn-pay cash-btn" @click="handlePaymentCash"><i class="fa-solid fa-money-bill-1-wave"></i><span>Tiền mặt</span></button>
                    <div class="d-flex gap-2">
                        <button class="btn-pay vnpay-btn" @click="handlePaymentVNPayFull"><i class="fa-solid fa-qrcode"></i><span>VNPay</span></button>
                        <button class="btn-pay mixed-btn" @click="handlePaymentMixed"><i class="fa-solid fa-layer-group"></i><span>Hỗn hợp</span></button>
                    </div>
                    </div>
                </div>
            </div>
        </div>

        <div v-else-if="modalView === 'viewOrder'" class="order-view-container h-100 d-flex flex-column m-0">
          <div class="order-info-card mb-3 flex-shrink-0">
            <div class="row">
              <div class="col-6 mb-2"><span class="text-muted small">Khách hàng</span><br/><strong>{{ selectedPhieu?.tenKhachHang || 'Khách vãng lai' }}</strong></div>
              <div class="col-6 mb-2"><span class="text-muted small">Bàn</span><br/><strong>{{ selectedBan?.maBan }}</strong></div>
              <div class="col-6"><span class="text-muted small">Trạng thái</span><br/><span class="badge bg-warning text-dark px-3 py-1 mt-1" style="border-radius: 12px;">Đang phục vụ</span></div>
              <div class="col-6"><span class="text-muted small">Ngày tạo</span><br/><strong>{{ formatDate(selectedPhieu?.thoiGianDat) }}</strong></div>
            </div>
          </div>

          <div class="d-flex justify-content-between align-items-center mb-2 flex-shrink-0">
            <h6 class="mb-0 fw-bold"><i class="fa-solid fa-utensils me-2"></i> Danh sách món</h6>
            <button class="btn btn-sm btn-mark-all" @click="markAllServed"><i class="fa-solid fa-check-double me-1"></i> Đã lên tất cả</button>
          </div>

          <div class="order-items-list flex-grow-1 overflow-auto pe-2 mb-3" style="min-height: 100px;">
            <div v-for="(item, index) in listMonDaChon" :key="item.dbDetailId" class="order-item-card" :class="{'served': item.served}">
              <div class="checkbox-wrapper" @click="toggleItemServed(item)"><div class="custom-checkbox" :class="{'checked': item.served}"><i v-if="item.served" class="fa-solid fa-check"></i></div></div>
              <div class="item-icon-box"><i v-if="item.type === 'SET'" class="fa-solid fa-bowl-food"></i><i v-else class="fa-solid fa-burger"></i></div>
              <div class="item-details flex-grow-1">
                <div class="d-flex justify-content-between align-items-start mb-1"><span class="fw-bold item-name">{{ item.name }}</span><button class="btn btn-sm btn-delete-item" @click.stop="handleDeleteItem(item, index)" title="Hủy món này"><i class="fa-solid fa-xmark"></i></button></div>
                <div class="d-flex justify-content-between align-items-center text-muted small mt-1"><span>SL: <strong>{{ item.quantity }}</strong> &nbsp;|&nbsp; Giá: {{ item.price.toLocaleString() }} đ</span><div class="d-flex align-items-center gap-3"><span class="badge" :class="item.served ? 'bg-success' : 'bg-secondary'">{{ item.served ? 'Đã lên' : 'Chưa lên' }}</span><span class="fw-bold text-dark text-end" style="min-width: 100px;">Tổng: {{ (item.price * item.quantity).toLocaleString() }} đ</span></div></div>
              </div>
            </div>
          </div>

          <div class="order-summary-card flex-shrink-0" style="background-color: #fff9f9; border: 1px solid #ffccd5;">
            <h6 class="fw-bold mb-3" style="color: #7d161a;"><i class="fa-solid fa-file-invoice-dollar me-2"></i>Chi tiết thanh toán</h6>
            
            <div class="d-flex justify-content-between mb-2 text-muted small">
                <span>Tạm tính (Tiền món):</span>
                <span>{{ (billSummary?.tong || 0).toLocaleString() }} đ</span>
            </div>
            
            <div v-if="(billSummary?.giam || 0) > 0" class="d-flex justify-content-between align-items-center mb-2 text-success small">
                <span>Khuyến mãi / Giảm giá:</span>
                <div class="d-flex align-items-center">
                    <span class="fw-bold me-2">- {{ (billSummary?.giam || 0).toLocaleString() }} đ</span>
                    <button class="btn btn-sm btn-outline-danger py-0 px-1" style="font-size: 10px;" @click="removeVoucher" title="Hủy mã này">
                        <i class="fa-solid fa-xmark"></i>
                    </button>
                </div>
            </div>

            <div class="d-flex justify-content-between mb-2 text-muted small">
                <span>Thuế VAT:</span>
                <span>({{ billSummary?.vatRate || 0 }}% tổng gộp)</span>
            </div>

            <div v-if="(billSummary?.coc || 0) > 0" class="d-flex justify-content-between mb-3 fw-bold" style="color: #007bff;">
                <span><i class="fa-solid fa-piggy-bank me-1"></i> Khách đã cọc trước:</span>
                <span>- {{ (billSummary?.coc || 0).toLocaleString() }} đ</span>
            </div>

            <div class="d-flex justify-content-between pt-3 border-top" style="border-top-color: #ffccd5 !important;">
                <h5 class="fw-bold mb-0">Tiền món bàn này:</h5>
                <h5 class="fw-bold text-danger mb-0">{{ (billSummary?.canThu || 0).toLocaleString() }} đ</h5>
            </div>

            <div v-if="(billSummary?.giam || 0) === 0" class="mt-3">
                <button class="btn btn-outline-danger w-100 fw-bold" style="border-radius: 8px; font-size: 13px;" @click="openDiscountModal">
                    <i class="fa-solid fa-tags me-1"></i> Áp dụng ưu đãi
                </button>
            </div>
          </div>

          <div class="d-flex justify-content-between mt-3 pt-3 border-top flex-shrink-0 gap-2">
            <button class="btn btn-outline-secondary px-4 fw-bold" @click="modalView = 'info'" style="border-radius: 8px;"><i class="fa-solid fa-arrow-left me-1"></i> Quay lại</button>
            <button class="btn btn-update-status px-4" @click="modalView = 'info'" :disabled="!isAllItemsServed">Tiến hành thanh toán <i class="fa-solid fa-arrow-right ms-1"></i></button>
          </div>
        </div>

        <div v-else-if="modalView === 'orderHistory'" class="history-container h-100 d-flex flex-column">
          <div class="history-header mb-4">
              <h5 class="fw-bold"><i class="fa-solid fa-history me-2"></i>Lịch sử đơn hàng - Bàn {{ selectedBan?.maBan }}</h5>
              <p class="text-muted small">Mã hóa đơn: #{{ selectedPhieu?.idHoaDon }}</p>
          </div>
          <div class="timeline-wrapper flex-grow-1 overflow-auto pe-2">
              <div v-if="orderHistory.length === 0" class="text-center py-5"><i class="fa-solid fa-inbox fa-3x text-muted mb-3"></i><p>Chưa có dữ liệu lịch sử cho đơn hàng này</p></div>
              <div v-else class="timeline">
                  <div v-for="(log, index) in orderHistory" :key="index" class="timeline-item">
                      <div class="timeline-badge" :class="{'bg-success': log.trangThaiMoi === 4, 'bg-primary': log.trangThaiMoi === 6}"><i class="fa-solid" :class="log.hanhDong.includes('món') ? 'fa-utensils' : 'fa-check'"></i></div>
                      <div class="timeline-card shadow-sm">
                          <div class="d-flex justify-content-between align-items-start"><h6 class="fw-bold mb-1 text-dark">{{ log.hanhDong }}</h6><span class="badge bg-light text-dark border">{{ log.hanhDong.split(':')[0] }}</span></div>
                          <div class="log-meta mb-2"><small class="text-muted me-3"><i class="fa-regular fa-clock me-1"></i>{{ formatHistoryTime(log.thoiGian || log.thoi_gian_thuc_hien) }}</small><small class="text-muted"><i class="fa-regular fa-user me-1"></i>{{ log.nguoiThucHien || log.ten_nhan_vien || 'Hệ thống' }}</small></div>
                          <div class="log-details p-2 bg-light rounded"><p class="mb-0 small text-secondary"><i class="fa-solid fa-info-circle me-1"></i> {{ log.lyDo || 'Không có ghi chú thêm' }}</p></div>
                      </div>
                  </div>
              </div>
          </div>
          <div class="mt-3 pt-3 border-top"><button class="btn btn-outline-secondary w-100 fw-bold" @click="modalView = 'info'" style="border-radius: 8px;"><i class="fa-solid fa-arrow-left me-1"></i> Quay lại thông tin bàn</button></div>
        </div>

        <div v-else-if="modalView === 'changeTable'" class="change-table-container h-100 d-flex flex-column p-3" style="background-color: #f8f9fa;">
          <div class="d-flex justify-content-between align-items-center mb-3 flex-shrink-0">
             <div class="alert alert-secondary mb-0 border-0 shadow-sm flex-grow-1 me-4 d-flex align-items-center"><i class="fa-solid fa-person-walking-arrow-right me-2 text-danger fs-5"></i><span>Đang chuyển đoàn <strong class="text-primary fs-5 mx-1">{{ selectedPhieu?.soNguoi || 1 }}</strong> khách từ bàn <strong class="text-danger fs-5 mx-1">{{ selectedBan?.maBan }}</strong> sang bàn trống.</span></div>
             <div class="d-flex bg-white p-1 rounded shadow-sm border"><button v-for="tang in danhSachTang" :key="'modal-tang-' + tang" class="btn px-4 py-2 fw-bold" :class="modalActiveFloor === tang ? 'btn-active' : 'text-muted'" @click="modalActiveFloor = tang" style="border-radius: 6px; border: none;">Tầng {{ tang }}</button></div>
          </div>
          <div class="floor-plan-layout flex-grow-1 overflow-hidden d-flex gap-3">
             <div class="floor-plan-section flex-grow-1 bg-white shadow-sm border rounded">
                <div class="grid-container h-100 overflow-auto">
                   <div class="grid-canvas">
                      <div v-for="ban in modalBanTheoTang" :key="ban.id" class="table-card" :class="{'highlight-red': getTrangThaiTheoNgay(ban.id) === 0 && ban.id !== selectedBan?.id, 'is-hovered': modalHoveredTableId === ban.id, 'is-dimmed': (getTrangThaiTheoNgay(ban.id) !== 0 || ban.id === selectedBan?.id) && modalHoveredTableId !== ban.id, 'current-table': ban.id === selectedBan?.id}" :style="{gridColumnStart: ban.column, gridRowStart: ban.row, gridColumnEnd: 'span 3', gridRowEnd: 'span 2', cursor: (getTrangThaiTheoNgay(ban.id) === 0 && ban.id !== selectedBan?.id) ? 'pointer' : 'not-allowed'}" @click="(getTrangThaiTheoNgay(ban.id) === 0 && ban.id !== selectedBan?.id) ? handleSwitchTable(ban) : null">
                         <div class="table-content text-center pt-2"><strong class="fs-5">{{ ban.maBan }}</strong><div class="text-muted small mb-2"><i class="fa-solid fa-users text-secondary"></i> {{ ban.soCho }} chỗ</div><div v-if="ban.id === selectedBan?.id" class="status-tag bg-primary text-white w-100 rounded-pill"><i class="fa-solid fa-location-dot"></i> Đang ở đây</div><div v-else-if="getTrangThaiTheoNgay(ban.id) === 0" class="status-tag status-empty w-100 rounded-pill"><i class="fa-solid fa-check"></i> Trống (Chọn)</div><div v-else class="status-tag bg-secondary text-white w-100 rounded-pill"><i class="fa-solid fa-ban"></i> Không khả dụng</div></div>
                      </div>
                   </div>
                </div>
             </div>
             <div class="bg-white shadow-sm border rounded p-3 d-flex flex-column" style="width: 350px;">
                <h6 class="fw-bold mb-3 pb-2 border-bottom text-success flex-shrink-0"><i class="fa-solid fa-list-check me-2"></i> Bàn trống có thể đổi</h6>
                <div class="overflow-auto flex-grow-1 pe-2" style="max-height: calc(100vh - 250px);">
                   <div v-if="listBanTrong.length === 0" class="text-center text-muted mt-5"><i class="fa-solid fa-box-open fa-3x mb-3 text-light"></i><p>Hiện không có bàn nào trống!</p></div>
                   <div v-else class="row g-2">
                      <div v-for="ban in listBanTrong" :key="ban.id" class="col-6">
                         <div class="mini-table-card border rounded p-3 text-center transition-all" style="cursor: pointer;" :class="{'border-danger bg-light': modalHoveredTableId === ban.id}" @mouseenter="onHoverEmptyTable(ban)" @mouseleave="onLeaveEmptyTable" @click="handleSwitchTable(ban)"><div class="fw-bold text-success fs-5">{{ ban.maBan }}</div><div class="small text-muted mb-1"><i class="fa-solid fa-users text-secondary"></i> {{ ban.soCho }} chỗ</div><div class="badge bg-light text-dark border">Tầng {{ ban.soTang }}</div></div>
                      </div>
                   </div>
                </div>
             </div>
          </div>
          <div class="mt-3 pt-3 border-top bg-white px-3 py-2 rounded shadow-sm d-flex justify-content-between flex-shrink-0"><button class="btn btn-outline-secondary px-4 fw-bold" @click="modalView = 'info'"><i class="fa-solid fa-arrow-left me-1"></i> Hủy đổi bàn</button></div>
        </div>

        <div v-else-if="modalView === 'viewQR'" class="qr-container h-100 d-flex flex-column align-items-center p-4" style="background-color: #f8f9fa;">
            <h5 class="fw-bold mb-2 text-center" style="color: #7d161a;"><i class="fa-solid fa-qrcode me-2"></i>Mã QR Đặt Món Bàn {{ selectedBan?.maBan }}</h5>
            <p class="text-center text-muted small mb-4">Khuyến khích khách hàng quét mã này để tự chọn món. Đơn hàng sẽ tự động đồng bộ vào hóa đơn hiện tại.</p>
            <div class="qr-box p-3 bg-white shadow rounded border mb-4 d-flex justify-content-center align-items-center" style="width: 250px; height: 250px;">
                <img :src="`https://api.qrserver.com/v1/create-qr-code/?size=220x220&data=${encodeURIComponent(customerOrderUrl)}&margin=10`" alt="QR Code" class="img-fluid" />
            </div>
            <div class="input-group mb-4 w-100 shadow-sm">
                <span class="input-group-text bg-white"><i class="fa-solid fa-link text-muted"></i></span>
                <input type="text" class="form-control bg-light" :value="customerOrderUrl" readonly style="font-size: 13px;">
                <button class="btn btn-outline-secondary" style="background-color: white;" @click="copyToClipboard(customerOrderUrl)"><i class="fa-regular fa-copy text-primary"></i> Copy</button>
            </div>
            <div class="mt-auto w-100 pt-3 border-top">
                <button class="btn btn-outline-secondary w-100 fw-bold" @click="modalView = 'info'" style="border-radius: 8px;"><i class="fa-solid fa-arrow-left me-1"></i> Quay lại thông tin bàn</button>
            </div>
        </div>

        <div v-else-if="modalView === 'addFood'" class="h-100 full-modal-content">
          <FoodList :initial-items="listMonDaChon" @close="handleCloseFoodList" @save="handleSaveFood" />
        </div>
      </div>
    </div>
    <div v-if="isShowDiscountModal" class="modal-overlay" style="z-index: 10000;">
          <div class="modal-box" style="max-width: 500px; height: 85vh;">
            <div class="modal-header-custom bg-light">
              <h5 class="modal-title-custom m-0 text-danger"><i class="fa-solid fa-tags me-2"></i>Chọn ưu đãi</h5>
              <button class="close-btn" @click="isShowDiscountModal = false">✕</button>
            </div>

            <div class="modal-body-custom p-0 bg-light d-flex flex-column h-100">
              
              <div class="d-flex border-bottom bg-white px-3 pt-2">
                  <button class="btn btn-link text-decoration-none fw-bold px-3 py-2 border-bottom border-3"
                          :class="activeVoucherTab === 'PUBLIC' ? 'border-danger text-danger' : 'border-transparent text-muted'"
                          @click="activeVoucherTab = 'PUBLIC'">
                      <i class="fa-solid fa-bullhorn me-1"></i> Mã chung ({{ publicVouchers.length }})
                  </button>
                  <button class="btn btn-link text-decoration-none fw-bold px-3 py-2 border-bottom border-3"
                          :class="activeVoucherTab === 'PERSONAL' ? 'border-danger text-danger' : 'border-transparent text-muted'"
                          @click="activeVoucherTab = 'PERSONAL'">
                      <i class="fa-solid fa-gift me-1"></i> Ưu đãi của tôi ({{ personalVouchers.length }})
                  </button>
              </div>

              <div class="flex-grow-1 overflow-auto p-3">
                  <div v-show="activeVoucherTab === 'PUBLIC'">
                      <div class="input-group mb-4 shadow-sm">
                          <input type="text" class="form-control" v-model="maGiamGiaInput" placeholder="Nhập mã công khai..." style="text-transform: uppercase;">
                          <button class="btn btn-danger fw-bold" @click="applyVoucher(null)">Áp dụng</button>
                      </div>
                      
                      <div v-if="publicVouchers.length === 0" class="text-center py-5 text-muted">
                          <i class="fa-solid fa-box-open fa-3x mb-3 opacity-50"></i>
                          <p>Hiện không có chương trình khuyến mãi chung nào.</p>
                      </div>

                      <div v-for="vc in publicVouchers" :key="vc.id" class="card mb-3 border-0 shadow-sm" style="border-radius: 12px; border-left: 5px solid red !important;">
                          <div class="card-body p-3 d-flex align-items-center">
                              <div class="bg-danger text-white rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 50px; height: 50px;">
                                  <i class="fa-solid fa-percent fs-5"></i>
                              </div>
                              <div class="flex-grow-1">
                                  <h6 class="fw-bold mb-1 text-dark" style="font-size: 14px;">{{ vc.codeGiamGia }}</h6>
                                  <p class="text-muted small mb-0">{{ vc.tenPhieuGiamGia }}</p>
                                  <p class="text-danger small fw-bold mb-0 mt-1">Giảm: {{ vc.loaiGiamGia === 1 ? vc.giaTriGiam + '%' : vc.giaTriGiam.toLocaleString() + 'đ' }}
                                      <span v-if="vc.loaiGiamGia === 1 && vc.giaTriGiamToiDa > 0" class="text-muted fw-normal" style="font-size: 11px;">
                                          (Tối đa: {{ vc.giaTriGiamToiDa.toLocaleString() }}đ)
                                      </span>
                                  </p>
                                  <small class="text-muted" style="font-size: 11px;">Đơn tối thiểu: {{ vc.donHangToiThieu.toLocaleString() }}đ</small>
                              </div>
                              <div>
                                  <button class="btn btn-sm rounded-pill px-3 fw-bold" 
                                          :class="currentRawTotal >= vc.donHangToiThieu ? 'btn-outline-danger' : 'btn-outline-secondary disabled'"
                                          :disabled="currentRawTotal < vc.donHangToiThieu"
                                          @click="applyVoucher(vc)">
                                      {{ currentRawTotal >= vc.donHangToiThieu ? 'Dùng' : 'Chưa đủ ĐK' }}
                                  </button>
                              </div>
                          </div>
                      </div>
                  </div>

                  <div v-show="activeVoucherTab === 'PERSONAL'">
                      <div v-if="!selectedPhieu?.idKhachHang" class="alert alert-warning text-center" style="font-size: 13px;">
                          <i class="fa-solid fa-triangle-exclamation me-1"></i> Bàn này là <b>Khách vãng lai</b>, không có ưu đãi cá nhân.
                      </div>
                      
                      <div v-else-if="personalVouchers.length === 0" class="text-center py-5 text-muted">
                          <i class="fa-solid fa-gift fa-3x mb-3 opacity-50"></i>
                          <p>Khách hàng này hiện không có mã ưu đãi cá nhân nào.</p>
                      </div>

                      <div v-else v-for="vc in personalVouchers" :key="vc.id" class="card mb-3 border-0 shadow-sm" style="border-radius: 12px; border-left: 5px solid #ff9800 !important; background: linear-gradient(to right, #fff, #fffbf2);">
                          <div class="card-body p-3 d-flex align-items-center">
                              <div class="bg-warning text-white rounded-circle d-flex align-items-center justify-content-center me-3" style="width: 50px; height: 50px;">
                                  <i class="fa-solid fa-crown fs-5"></i>
                              </div>
                              <div class="flex-grow-1">
                                  <h6 class="fw-bold mb-1 text-dark" style="font-size: 14px;">{{ vc.codeGiamGia }}</h6>
                                  <p class="text-muted small mb-0">{{ vc.tenPhieuGiamGia }}</p>
                                  <p class="text-danger small fw-bold mb-0 mt-1">Giảm: {{ vc.loaiGiamGia === 1 ? vc.giaTriGiam + '%' : vc.giaTriGiam.toLocaleString() + 'đ' }}
                                      <span v-if="vc.loaiGiamGia === 1 && vc.giaTriGiamToiDa > 0" class="text-muted fw-normal" style="font-size: 11px;">
                                          (Tối đa: {{ vc.giaTriGiamToiDa.toLocaleString() }}đ)
                                      </span>
                                  </p>
                                  <small class="text-muted" style="font-size: 11px;">Đơn tối thiểu: {{ vc.donHangToiThieu.toLocaleString() }}đ</small>
                              </div>
                              <div>
                                  <button class="btn btn-sm rounded-pill px-3 fw-bold" 
                                          :class="currentRawTotal >= vc.donHangToiThieu ? 'btn-outline-danger' : 'btn-outline-secondary disabled'"
                                          :disabled="currentRawTotal < vc.donHangToiThieu"
                                          @click="applyVoucher(vc)">
                                      {{ currentRawTotal >= vc.donHangToiThieu ? 'Dùng' : 'Chưa đủ ĐK' }}
                                  </button>
                              </div>
                          </div>
                      </div>
                  </div>
              </div>

            </div>
          </div>
        </div>
  </div>
</template>

<style scoped>
.layout-table {
  display: flex;
  background-color: white;
}

.search-form {
  border: solid 1px #cacaca;
  border-radius: 15px;
  padding-top: 1%;
  padding-left: 2%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-outline {
  border: 1px solid #ccc !important;
  background-color: white;
  color: #333;
}

/* Tùy chỉnh thêm hiệu ứng khi hover vào nút chưa chọn */
.btn-outline:hover {
  border-color: #7d161a !important;
  color: #7d161a !important;
  background-color: #fff5f5 !important;
}

.navbar-search {
  width: 100%;
}

hr {
  border: 0;
  border-top: 2px solid #7d161a;
  /* Chỉnh độ dày ở đây */
}

.btn-checkable {
  background-color: #7d161a !important;
  color: white !important;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

/* Khi button bị disabled */
.btn-checkable:disabled {
  background-color: #ccc !important;
  color: #666 !important;
  cursor: not-allowed;
  box-shadow: none;
  opacity: 0.8;
}

.table-container {
  border: 1px solid #dee2e6;
  /* Viền bao ngoài */
  border-radius: 15px;
  /* Độ bo góc bạn muốn */
  overflow: hidden;
  /* Quan trọng: Cắt các góc nhọn của header/footer bên trong */
}

.table thead tr th {
  background-color: #e9e9e9 !important;
  color: #767676;
  font-weight: 600;
}

/* Button tầng */
/* Trạng thái cho nút ĐANG được chọn (Active) */
.btn {
  background-color: white;
  color: #333;
}

.btn:hover {
  background-color: #5c0a16 !important;
  /* Đỏ đậm hơn một chút khi di chuột */
  color: white !important;
}

.btn-active {
  background-color: #7d161a !important;
  color: white !important;
  border: 1px solid #7d161a;
  cursor: default;
  /* Đã chọn rồi thì không hiện con trỏ tay */
}

.btn-active:hover {
  background-color: #5c0a16 !important;
  /* Đỏ đậm hơn một chút khi di chuột */
  color: white !important;
}

/* Frame chung */
.floor-frame {
  width: 100%;
  height: calc(100vh - 250px);
  /* Tự động tính toán: Toàn màn hình trừ đi phần header/search ở trên */
  min-height: 450px;
  /* Đảm bảo không quá nhỏ trên màn hình thấp */
  border: 1px solid #dee2e6;
  border-radius: 12px;
  background: #fff;
  padding: 16px;
  display: flex;
  overflow: hidden;
  /* Không cho phép cả khung lớn bị cuộn */
}

/* Nội dung sơ đồ */
.floor-map {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floor-title {
  font-size: 1rem;
  font-weight: 600;
  color: #7d161a;
}

.floor-info {
  margin-top: 6px;
  font-size: 0.875rem;
  /* 14px */
  color: #555;
  font-weight: 500;
}

.floor-plan-layout {
  display: flex;
  gap: 15px;
  height: 100%;
  width: 100%;
}

/* --- PHẦN SƠ ĐỒ BÀN --- */
.floor-plan-section {
  flex: 2;
  background: white;
  border-radius: 8px;
  border: 1px solid #ddd;
  display: flex;
  flex-direction: column;
}

.floor-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
}

.tab-btn.active {
  background: #5c0a16;
  color: white;
}

.edit-pos-btn {
  background: #f5f5f5;
  border: 1px solid #ccc;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

/* Vùng chứa lưới có chức năng scroll */
.grid-container {
  flex-grow: 1;
  overflow: auto;
  /* Hiện thanh cuộn khi grid bên trong lớn hơn */
  position: relative;
  background-color: #f8f9fa;
}

/* Lưới bàn - Thiết lập kích thước cố định để buộc phải scroll */
.grid-canvas {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  grid-template-rows: repeat(15, 1fr);
  /* Tăng số hàng và cố định chiều cao mỗi hàng (ví dụ 100px) */
  gap: 15px;
  padding: 20px;
  width: 100%;
  min-width: 800px;
  /* Đảm bảo không bị quá hẹp trên màn hình nhỏ */
  box-sizing: border-box;
  background-image:
    linear-gradient(to right, #eee 1px, transparent 1px),
    linear-gradient(to bottom, #eee 1px, transparent 1px);
  background-size: calc(100% / 12) 100px;
}

.grid-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.grid-container::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.grid-container::-webkit-scrollbar-thumb {
  background: #7d161a;
  border-radius: 10px;
}

.grid-container::-webkit-scrollbar-thumb:hover {
  background: #5c0a16;
}

/* Thẻ bàn */
.table-card {
  background: white;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  font-size: 0.8rem;
}

.table-card.highlight-red {
  background: #f9ebeb;
}

.table-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.table-id strong {
  font-size: 14px;
  display: block;
}

.table-id span {
  font-size: 13px;
  color: #555;
}

/* Badge trạng thái */
.status-tag {
  text-align: center;
  padding: 6px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: bold;
  color: white;
  margin-top: 10px;
}

.status-occupied-light {
  background-color: #e67e22;
}

.status-empty {
  background-color: #5c0a16;
}

.status-booked {
  background-color: #f1c40f;
  color: #333;
}

/* --- PHẦN DANH SÁCH --- */
/* Container chứa bộ lọc */
.filter-section {
  display: flex;
  flex-direction: column;
  /* Ép các nhóm lọc xếp chồng lên nhau */
  gap: 10px;
  margin-bottom: 15px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 10px;
}

/* Từng nhóm nhãn + ô nhập */
.filter-group {
  display: flex;
  flex-direction: column;
  /* QUAN TRỌNG: Nhãn ở trên, Input ở dưới */
  width: 100%;
}

/* Nhãn tiêu đề nhỏ gọn */
.filter-label {
  font-size: 11px !important;
  font-weight: 700;
  color: #656565;
  margin-bottom: 4px;
  /* Tạo khoảng cách với ô nhập */
  display: flex;
  align-items: center;
  gap: 5px;
  text-transform: uppercase;
}

/* Ô nhập liệu */
.filter-input-wrapper input {
  width: 100%;
  height: 34px;
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 13px;
  outline: none;
  transition: all 0.2s;
}

.filter-input-wrapper input:focus {
  border-color: #7d161a;
  box-shadow: 0 0 0 2px rgba(125, 22, 26, 0.1);
}

.list-section {
  flex: 1.2;
}

.checkin-container {
  max-width: 450px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  font-family: Arial, sans-serif;
}

.title {
  font-weight: bold;
  margin-bottom: 15px;
}

.input-group label {
  display: block;
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
}

.search-input {
  position: relative;
  display: flex;
  align-items: center;
}

.search-input input,
.date-input {
  width: 100%;
  padding: 6px 10px 6px 10px !important;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 13px;
}

/* Style cho từng Card khách hàng */
.customer-card {
  border: 1px solid #eee;
  border-radius: 15px;
  padding: 15px;
  margin-bottom: 15px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.customer-name {
  font-weight: bold;
  font-size: 16px;
}

.table-badge {
  background: #f0f0f0;
  color: #888;
  padding: 5px 12px;
  border-radius: 6px;
  font-size: 14px;
}

.time-info {
  font-size: 15px;
  margin-bottom: 15px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-checkin {
  background: #68051b;
  /* Màu đỏ sẫm như hình */
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  cursor: pointer;
}

.countdown-timer {
  border: 1px solid #68051b;
  color: #68051b;
  padding: 5px 15px;
  border-radius: 8px;
  font-weight: bold;
  background: #fff5f5;
}

.arrival-note {
  font-size: 13px;
  color: #888;
  margin-top: 10px;
  margin-bottom: 0;
}

/* Tìm và thay thế class .list-waiting cũ bằng cái này */
.list-waiting {
  max-height: 15rem;
  /* Giới hạn chiều cao cố định (khoảng 3-4 card) */
  overflow-y: auto;
  /* Hiện thanh cuộn khi danh sách dài vượt quá max-height */
  padding-right: 8px;
  /* Khoảng cách để không đè lên card khi hiện thanh cuộn */

  /* Tùy chỉnh thanh cuộn (Scrollbar) cho Chrome/Edge/Safari */
}

.list-waiting::-webkit-scrollbar {
  width: 6px;
}

.list-waiting::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.list-waiting::-webkit-scrollbar-thumb {
  background: #7d161a;
  /* Màu đỏ sẫm đồng bộ với theme của bạn */
  border-radius: 10px;
}

.list-waiting::-webkit-scrollbar-thumb:hover {
  background: #5c0a16;
}

.countdown-layout {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

/* CSS cho Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  width: 100%;
  max-width: 620px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  max-height: 90vh;
  transition: all 0.1s ease-in-out;
}

.modal-box.modal-fullscreen {
  width: 80vw !important;
  height: 90vh !important;
  max-width: none !important;
  max-height: none !important;

  border-radius: 0 !important;
  margin: 0 !important;
}

.modal-header-custom {
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
}

.modal-title-custom {
  color: #7d161a;
  font-weight: bold;
  margin: 0;
}

.modal-body-custom {
  padding: 20px;
  overflow-y: auto;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.h-100 {
  height: 100%;
}

.full-modal-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  /* Prevent double scrollbars */
}

.selected-summary {
  background: #f9f9f9;
  padding: 10px;
  border-radius: 6px;
  border: 1px dashed #ddd;
  font-size: 0.9rem;
}

.summary-list {
  margin: 5px 0 0 0;
  padding-left: 20px;
  color: #555;
  max-height: 180px;
  overflow-y: auto;
}

.btn-action.has-items {
  background: #5c0a16;
  color: white;
}

.close-btn {
  border: none;
  background: transparent;
  font-size: 1.2rem;
  cursor: pointer;
}

.modal-title-custom {
  margin: 0;
  font-weight: bold;
  color: #7d161a;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.badge-status {
  background: #7d161a;
  color: white;
  padding: 2px 12px;
  border-radius: 15px;
  font-size: 13px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.btn-action {
  width: 100%;
  padding: 10px;
  border-radius: 6px;
  border: none;
  font-weight: bold;
  transition:
    background 0.3s ease,
    color 0.3s ease,
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.btn-action:hover {
  background: linear-gradient(to right, #7d161a, #c0392b);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(125, 22, 26, 0.35);
}

.status-options {
  display: flex;

  flex-direction: row;
  gap: 10px;
  margin-top: 10px;
}

.status-item {
  padding: 3%;
  width: 100%;
  border: 1px solid #ddd;
  /* Viền mặc định xám */
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #666;
  /* Màu chữ mặc định */
}

/* Khi được chọn: Viền đỏ, Chữ đỏ, Nền hồng nhạt */
.status-item.active-border {
  border: 2px solid #7d161a !important;
  /* Màu đỏ viền */
  color: #7d161a;
  background-color: #fff5f5;
  font-weight: bold;
}

.status-item i {
  flex: 1;
  margin-right: 10px;
  font-size: 1.1rem;
}

.status-item:hover:not(.active-border) {
  background-color: #f9f9f9;
}

.btn-update-status {
  width: 100%;
  background: #7d161a;
  color: white;
  border-radius: 8px;
  padding: 12px;
  border: none;
  font-weight: bold;
}

.table-card {
  cursor: pointer;
  /* Thêm con trỏ tay để khách biết bàn có thể click */
  transition: transform 0.2s;
}

.table-card:hover {
  transform: scale(1.02);
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.15);
}

.close-btn {
  border: none;
  background-color: white;
}

:global(.swal2-container) {
  z-index: 20000 !important;
  /* Cao hơn 9999 của .modal-overlay */
}

.table-card {
  transition: all 0.3s ease;
  background: white;
  border: 1px solid #ccc;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  font-size: 0.8rem;
  /* cursor: pointer; <--- ĐẢM BẢO ĐÃ XÓA DÒNG NÀY */
}

/* HIỆU ỨNG HIGHLIGHT KHI HOVER TỪ BÊN PHẢI */
.table-card.is-hovered {
  transform: scale(1.08) !important; /* Phóng to bàn lên một chút */
  border: 2px solid #7d161a !important; /* Đổi viền sang màu đỏ đậm của quán */
  box-shadow: 0 0 20px rgba(125, 22, 26, 0.5) !important; /* Phát sáng tỏa ra ngoài */
  z-index: 10; /* Đẩy bàn nổi lên trên các bàn khác */
  background-color: #fff9f9; /* Đổi nhẹ màu nền */
}

/* Thêm hiệu ứng cho Card khách hàng bên phải để nhân viên biết có thể tương tác */
.customer-card {
  transition: all 0.2s ease;
}
.customer-card:hover {
  border-color: #7d161a;
  box-shadow: 0 4px 12px rgba(125, 22, 26, 0.15);
}

.table-card.is-dimmed {
  opacity: 0.6; /* Làm mờ hẳn đi */
  filter: grayscale(80%); /* Chuyển sang tông xám */
  pointer-events: none; /* Không cho tương tác để tránh nhầm lẫn */
  transition: all 0.4s ease;
}

/* Khi bàn được hover từ phiếu đặt bàn, nó phải rõ nét trở lại */
.table-card.is-hovered {
  opacity: 1 !important;
  filter: grayscale(0%) !important;
  transform: scale(1.1) !important;
  border: 2px solid #7d161a !important;
  box-shadow: 0 0 25px rgba(125, 22, 26, 0.6) !important;
  z-index: 100;
  background-color: #ffffff !important;
}

/* Hiệu ứng mượt khi chuyển đổi */
.table-card {
  transition: opacity 0.3s, transform 0.3s, filter 0.3s;
}

.disabled-section {
  opacity: 0.6;
  pointer-events: none; /* Chặn hoàn toàn click chuột */
  filter: grayscale(30%);
}

.dish-served {
  color: #28a745; /* Màu xanh lá */
  text-decoration: line-through; /* Gạch ngang nếu muốn */
  opacity: 0.8;
}

.payment-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* Style chung cho các nút thanh toán */
.btn-pay {
  border: none;
  border-radius: 8px;
  padding: 10px;
  font-weight: 700;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s;
  cursor: pointer;
  color: white;
  flex: 1; /* Để các nút trong d-flex bằng nhau */
}

.btn-pay:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
}

.btn-pay:active {
  transform: translateY(0);
}

/* Màu sắc riêng từng loại */
.cash-btn {
  background-color: #5c0a16; /* Xanh lá */
  width: 100%;
}

.vnpay-btn {
  background-color: #5c0a16; /* Xanh dương */
}

.mixed-btn {
  background-color: #5c0a16; /* Vàng */
}

/* Nút hủy phiếu nhỏ gọn */
.btn-cancel-ticket {
  width: 100%;
  background: transparent;
  border: 1px solid #dc3545;
  color: #dc3545;
  padding: 6px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  transition: 0.3s;
}

.btn-cancel-ticket:hover {
  background: #fff5f5;
}

/* Section Title trong Modal */
.section-title {
  font-size: 13px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 1px;
  font-weight: bold;
}

.disabled-payment {
  opacity: 0.4;
  pointer-events: none;
  filter: grayscale(80%);
  transition: all 0.3s ease;
}

.order-view-container {
  background-color: #f8f9fa;
  margin: -20px; /* Bỏ padding mặc định của modal body */
  padding: 20px;
}

.order-info-card {
  background: white;
  border-radius: 12px;
  padding: 15px 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
}

.btn-mark-all {
  background-color: #28a745;
  color: white;
  border-radius: 6px;
  font-weight: 600;
  font-size: 12px;
}
.btn-mark-all:hover {
  background-color: #218838;
  color: white;
}

.order-item-card {
  background: white;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.03);
  border: 1px solid #eee;
  transition: all 0.2s;
}

.order-item-card.served {
  background-color: #f8fff9;
  border-color: #c3e6cb;
}

.checkbox-wrapper {
  cursor: pointer;
  padding: 5px;
}

.custom-checkbox {
  width: 22px;
  height: 22px;
  border: 2px solid #ccc;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 12px;
  transition: all 0.2s;
}

.custom-checkbox.checked {
  background-color: #7d161a;
  border-color: #7d161a;
}

.item-icon-box {
  width: 50px;
  height: 50px;
  background-color: #f0f0f0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #888;
  font-size: 20px;
}

.item-name {
  font-size: 15px;
  color: #333;
}

.order-summary-card {
  background: white;
  border-radius: 12px;
  padding: 15px 20px;
  box-shadow: 0 -4px 10px rgba(0,0,0,0.03);
}

.btn-delete-item {
  color: #dc3545;
  background: transparent;
  border: none;
  padding: 0 5px;
  font-size: 16px;
  opacity: 0.5;
  transition: all 0.2s;
}
.btn-delete-item:hover {
  opacity: 1;
  transform: scale(1.2);
}

button:disabled {
  background-color: #ccc !important;
  cursor: not-allowed;
  opacity: 0.6;
}

/* Giao diện thông báo khi chưa check-in */
.lock-overlay {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fdfdfd;
  border: 2px dashed #ddd;
  border-radius: 8px;
  color: #888;
}

.lock-overlay i {
  font-size: 3rem;
  margin-bottom: 15px;
  color: #ccc;
}

.btn-checkin-now {
  margin-top: 15px;
  padding: 8px 20px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.btn-history {
    background-color: #f8f9fa;
    border: 1px solid #ddd !important;
    color: #555;
}
.btn-history:hover {
    background-color: #7d161a !important;
    color: white !important;
}

/* Container lịch sử */
.history-container {
    background-color: #fff;
    padding: 10px;
}

/* Timeline Layout */
.timeline {
    position: relative;
    padding-left: 30px;
    margin-left: 15px;
    border-left: 2px solid #eee;
}

.timeline-item {
    position: relative;
    margin-bottom: 25px;
}

/* Điểm tròn mốc thời gian */
.timeline-badge {
    position: absolute;
    left: -41px;
    top: 5px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 10px;
    z-index: 1;
    border: 3px solid #fff;
    box-shadow: 0 0 5px rgba(0,0,0,0.1);
}

.timeline-card {
    background: white;
    border-radius: 10px;
    padding: 12px 15px;
    border: 1px solid #f0f0f0;
    position: relative;
    transition: all 0.2s ease-in-out; /* Tạo hiệu ứng mượt mà */
    cursor: default;
}

/* Mũi tên trỏ vào timeline line */
.timeline-card::before {
    content: '';
    position: absolute;
    left: -8px;
    top: 10px;
    width: 15px;
    height: 15px;
    background: white;
    transform: rotate(45deg);
    border-left: 1px solid #f0f0f0;
    border-bottom: 1px solid #f0f0f0;
}

.timeline-card:hover {
    background-color: #f5f5f5; /* Làm xám nhẹ nền */
    border-color: #d1d1d1;     /* Làm đậm viền một chút */
    transform: translateX(5px); /* Nhích nhẹ sang phải để tạo cảm giác tương tác */
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08) !important; /* Đổ bóng đậm hơn */
}

/* Đảm bảo mũi tên trỏ vào timeline cũng đổi màu đồng bộ khi hover */
.timeline-card:hover::before {
    background-color: #f5f5f5;
    border-left: 1px solid #d1d1d1;
    border-bottom: 1px solid #d1d1d1;
}

/* Thêm hiệu ứng cho phần chi tiết (ghi chú) bên trong khi hover */
.timeline-card:hover .log-details {
    background-color: #e9ecef !important;
    transition: background-color 0.2s;
}

.log-meta {
    display: flex;
    font-size: 12px;
}

/* Màu sắc các trạng thái trong timeline */
.bg-success { background-color: #28a745 !important; } /* Lên món, Nhận bàn */
.bg-primary { background-color: #007bff !important; } /* Thanh toán */
.bg-danger { background-color: #7d161a !important; }  /* Hủy đơn */

.timeline-wrapper::-webkit-scrollbar {
    width: 5px;
}
.timeline-wrapper::-webkit-scrollbar-thumb {
    background: #7d161a;
    border-radius: 10px;
}

.mini-table-card {
    border: 1px solid #ddd;
    border-radius: 10px;
    padding: 15px 10px;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    background: #fff;
}

.mini-table-card:hover {
    border-color: #7d161a;
    background-color: #fff5f5;
    transform: translateY(-3px);
    box-shadow: 0 4px 10px rgba(125, 22, 26, 0.15);
}

.mini-table-card .fw-bold {
    font-size: 16px;
    color: #7d161a !important;
}

.change-table-container {
    max-height: 500px;
}

/* Thanh cuộn cho danh sách bàn trống */
.available-tables-grid::-webkit-scrollbar {
    width: 6px;
}
.available-tables-grid::-webkit-scrollbar-thumb {
    background: #7d161a;
    border-radius: 10px;
}

.filter-label-v2 {
  display: block;
  font-size: 11px;
  font-weight: 700;
  color: #888;
  text-transform: uppercase;
  margin-bottom: 5px;
}

.btn-group-custom {
  display: flex;
  background: #f1f1f1;
  padding: 4px;
  border-radius: 10px;
  border: 1px solid #ddd;
  justify-content: space-around;
}

.time-btn {
  border: none;
  background: transparent;
  padding: 6px 15px;
  font-size: 13px;
  font-weight: 600;
  color: #666;
  border-radius: 7px;
  transition: all 0.2s ease;
}

.merge-payment-box {
  transition: all 0.3s ease;
}
.merge-payment-box button {
  border-width: 2px;
}
.time-btn:hover {
  color: #7d161a;
}

.time-btn.active {
  background: #7d161a;
  color: white;
  box-shadow: 0 2px 6px rgba(125, 22, 26, 0.3);
}

.date-input-v2 {
  border: 1px solid #ddd;
  border-radius: 10px;
  padding: 6px 12px;
  font-size: 13px;
  outline: none;
  height: 38px;
}

.date-input-v2:focus {
  border-color: #7d161a;
}

/* Làm nổi bật bàn khách đang ngồi (Bàn gốc) */
.current-table {
  border: 3px dashed #007bff !important;
  background-color: #e9f5ff !important;
  opacity: 1 !important;
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(0, 123, 255, 0.4);
  z-index: 5;
}

/* Các bàn bị mờ đi (Bàn đang có người ngồi hoặc đã đặt) */
.table-card.is-dimmed {
  opacity: 0.4 !important;
  filter: grayscale(100%) !important;
  pointer-events: none; /* Cấm click */
}

/* Các thẻ Mini Table bên phải khi hover */
.mini-table-card.transition-all {
  transition: all 0.2s ease-in-out;
}
.mini-table-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  border-color: #28a745 !important; /* Đổi viền sang xanh lá thay vì đỏ */
  background-color: #f8fff9 !important;
}

.merge-payment-box {
  transition: all 0.3s ease;
}

/* Nút gộp bàn mặc định */
.custom-merge-btn {
  min-height: 55px; /* Cố định chiều cao nút */
  transition: all 0.2s ease;
  border: 1px solid #dee2e6 !important;
}

.group-tabs-container {
  min-height: 60px; /* Giữ khoảng trống cố định cho thanh chuyển bàn nhanh */
}

/* 🚨 XỬ LÝ HOVER: Bắt buộc ra màu đỏ nhạt, không được ra màu xanh */
.custom-merge-btn:hover:not(.is-selected) {
  border-color: #7d161a !important;
  background-color: #fce8e8 !important; /* Màu nền đỏ cực nhạt */
  color: #7d161a !important;
}

/* Đảm bảo chữ bên trong cũng đổi màu khi hover */
.custom-merge-btn:hover:not(.is-selected) span,
.custom-merge-btn:hover:not(.is-selected) small {
  color: #7d161a !important;
}

/* 🚨 KHI ĐÃ CHỌN: Đỏ đậm chữ trắng */
.custom-merge-btn.is-selected {
  background-color: #7d161a !important;
  border-color: #7d161a !important;
  box-shadow: 0 4px 8px rgba(125, 22, 26, 0.3) !important;
}

/* Chống hiệu ứng xanh của Bootstrap khi click/focus */
.custom-merge-btn:focus, 
.custom-merge-btn:active {
  box-shadow: none !important;
  outline: none !important;
}

/* Ép chữ trắng tuyệt đối khi đã chọn */
.custom-merge-btn.is-selected span,
.custom-merge-btn.is-selected small {
  color: #ffffff !important;
}

.quick-switch-wrapper {
  min-height: 65px; 
}

.alert-wrapper {
  min-height: 45px;
}

.quick-tab-btn {
  border-radius: 6px !important;
  font-weight: 600;
  transition: all 0.2s;
}

.quick-tab-btn.active {
  background-color: #7d161a !important;
  color: white !important;
  border: none;
}
</style>
