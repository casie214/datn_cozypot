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
import Multiselect from '@vueform/multiselect';
import '@vueform/multiselect/themes/default.css';

const sysParams = ref({
  VAT: 10,
  VAT_DONG_GOI: 8, 
  MIN_RESERVE: 30,
  THOI_GIAN_GIU_BAN: 15,
  THOI_GIAN_HUY_HOAN_COC: 120
});

const loadSystemParams = async () => {
  try {
    const res = await axiosClient.get('/tham-so-he-thong/all-map');
    if (res.data) {
      sysParams.value.VAT = Number(res.data.VAT || 10);
      sysParams.value.VAT_DONG_GOI = Number(res.data.VAT_DONG_GOI || 8); 
      sysParams.value.MIN_RESERVE = Number(res.data.MIN_RESERVE || 30);
      sysParams.value.THOI_GIAN_GIU_BAN = Number(res.data.THOI_GIAN_GIU_BAN || 15);
      sysParams.value.THOI_GIAN_HUY_HOAN_COC = Number(res.data.THOI_GIAN_HUY_HOAN_COC || 120);
    }
  } catch (error) {
    console.error("Không lấy được tham số hệ thống, dùng mặc định.");
  }
};

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
  { label: '3h', value: 180 },
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

const openDiscountModal = async (tab = 'PUBLIC') => {
  if (!selectedPhieu.value?.idHoaDon) {
    return Swal.fire({ title: 'Lưu ý', iconColor: '#7D161A', text: 'Cần có hóa đơn (đã gọi món) để xem mã!', icon: 'warning', confirmButtonText: 'Đã hiểu' });  }
  if (hasDiscountInGroup.value) {
    return Swal.fire({ title: 'Lưu ý', iconColor: '#7D161A', text: 'Nhóm bàn này đã được áp dụng một mã giảm giá. Vui lòng hủy mã cũ trước khi áp dụng mã mới!', icon: 'warning', confirmButtonText: 'Đã hiểu' });
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
    activeVoucherTab.value = tab; // Nhảy thẳng vào tab được chỉ định
    isShowDiscountModal.value = true;
  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: 'Không thể lấy danh sách ưu đãi', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

// 🚨 THÊM HÀM MỚI: Tự động tìm & áp dụng mã công cộng tốt nhất
const applyBestPublicVoucher = async () => {
  if (!selectedPhieu.value?.idHoaDon) {
    return Swal.fire({ title: 'Lưu ý', iconColor: '#7D161A', text: 'Cần có hóa đơn (đã gọi món) để áp dụng mã!', icon: 'warning', confirmButtonText: 'Đã hiểu' });
  }
  if (hasDiscountInGroup.value) {
    return Swal.fire({ title: 'Lưu ý', text: 'Nhóm bàn này đã áp dụng mã giảm giá rồi!', icon: 'warning', iconColor: '#7D161A', confirmButtonText: 'Đã hiểu' });
  }

  Swal.fire({ title: 'Đang tìm mã tốt nhất...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

  try {
    const res = await axiosClient.get(`/phieu-giam-gia/search?page=0&size=100`);
    const allVouchers = res.data?.content || [];

    // Lọc các mã CÔNG CỘNG đang hoạt động và ĐỦ ĐIỀU KIỆN (Tiền món >= Đơn hàng tối thiểu)
    const validPublicVouchers = allVouchers.filter(vc => 
        vc.trangThai === 1 && 
        vc.doiTuong === 0 && 
        currentRawTotal.value >= vc.donHangToiThieu
    );

    if (validPublicVouchers.length === 0) {
      return Swal.fire({ title: 'Rất tiếc', text: 'Hóa đơn hiện tại chưa đủ điều kiện áp dụng bất kỳ ưu đãi chung nào.', icon: 'info', confirmButtonText: 'Đóng' });
    }

    let bestVoucher = null;
    let maxDiscountAmount = 0;

    // Chạy vòng lặp tính toán xem mã nào trừ được nhiều tiền nhất
    validPublicVouchers.forEach(vc => {
      let discountForThisVc = 0;

      if (vc.loaiGiamGia === 1) { 
        // 1: Giảm theo phần trăm (%)
        discountForThisVc = currentRawTotal.value * (vc.giaTriGiam / 100);
        // Cắt ngọn nếu vượt quá mức giảm tối đa
        if (vc.giaTriGiamToiDa > 0 && discountForThisVc > vc.giaTriGiamToiDa) {
          discountForThisVc = vc.giaTriGiamToiDa;
        }
      } else { 
        // 0: Giảm tiền mặt trực tiếp (VNĐ)
        discountForThisVc = vc.giaTriGiam;
      }

      // So sánh tìm mã ngon nhất
      if (discountForThisVc > maxDiscountAmount) {
        maxDiscountAmount = discountForThisVc;
        bestVoucher = vc;
      }
    });

    if (bestVoucher) {
      // Đã tìm thấy, gọi hàm apply hiện có của bạn
      await applyVoucher(bestVoucher);
    } else {
      Swal.fire({ title: 'Rất tiếc', text: 'Không tìm thấy ưu đãi phù hợp.', icon: 'info', confirmButtonText: 'Đóng' });
    }

  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: 'Lỗi khi quét mã ưu đãi', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

const applyVoucher = async (voucher = null) => {
  const codeToApply = voucher ? voucher.codeGiamGia : maGiamGiaInput.value;
  if (!codeToApply) return Swal.fire({ title: 'Lỗi', text: 'Vui lòng nhập mã giảm giá!', icon: 'warning', iconColor: '#7D161A', confirmButtonText: 'Đóng' });

  if (voucher && currentRawTotal.value < voucher.donHangToiThieu) {
    return Swal.fire({ title: 'Chưa đạt điều kiện', text: `Đơn hàng cần tối thiểu ${voucher.donHangToiThieu.toLocaleString()}đ!`, icon: 'warning', iconColor: '#7D161A', confirmButtonText: 'Đã hiểu' });
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
    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', text: 'Đã áp dụng giảm giá!', timer: 1500, showConfirmButton: false });
    isShowDiscountModal.value = false;
    
    // 🚨 GỌI HÀM NÀY ĐỂ TẢI DỮ LIỆU MÀ KHÔNG BỊ ĐẨY RA NGOÀI
    await refreshTableData(); 
  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: error.response?.data?.message || 'Mã không hợp lệ hoặc đơn chưa đạt tối thiểu!', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

const removeVoucher = async () => {
  const confirm = await Swal.fire({ title: 'Hủy áp dụng mã?', icon: 'warning', iconColor: '#7D161A', showCancelButton: true, confirmButtonText: 'Đồng ý' });
  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang hủy...', didOpen: () => Swal.showLoading() });
    await axiosClient.post(`/phieu-giam-gia/huy-ap-dung/${selectedPhieu.value.idHoaDon}`);
    
    if (selectedPhieu.value) {
        selectedPhieu.value.soTienDaGiam = 0;
    }

    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Đã hủy mã', timer: 1000, showConfirmButton: false });
    
    // 🚨 GỌI HÀM NÀY ĐỂ TẢI DỮ LIỆU MÀ KHÔNG BỊ ĐẨY RA NGOÀI
    await refreshTableData(); 
  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: 'Không thể hủy mã lúc này!', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

const checkTimeStatus = (dbTime) => {
  if (!dbTime) return { isEarly: false, minutes: 0 };
  const now = dayjs(currentTime.value);
  const target = dayjs(dbTime);
  const minutesLeft = target.diff(now, "minute");
  
  if (minutesLeft > sysParams.value.THOI_GIAN_GIU_BAN) { 
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
      
      if (isMatch && [0, 1].includes(Number(khach.trangThai))) {
        const gioHen = dayjs(khach.thoiGianDat);
        const diff = gioHen.diff(now, "minute");
        // 🚨 Thay -30 bằng -THOI_GIAN_GIU_BAN
        return diff <= window && diff >= -(sysParams.value.THOI_GIAN_GIU_BAN);
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
const sortOption = ref("time_asc");

// THÊM MẢNG NÀY:
const sortOptionsList = [
  { value: "time_asc", label: "Giờ sắp đến (Sớm nhất)" },
  { value: "time_desc", label: "Giờ trễ nhất" },
  { value: "floor_asc", label: "Tầng thấp -> cao" },
  { value: "floor_desc", label: "Tầng cao -> thấp" },
];

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

const getItemVatInfo = (item) => {
  let rate = 0;
  // Lấy tỷ lệ % dựa theo loại cấu hình của món
  if (item.vatType === 1) {
      rate = sysParams.value.VAT;
  } else if (item.vatType === 2) {
      rate = sysParams.value.VAT_DONG_GOI;
  }

  const subtotal = item.price * item.quantity; // Tiền món (Chưa VAT)
  const vatAmount = subtotal * (rate / 100);   // Tiền VAT của món này
  const totalWithVat = subtotal + vatAmount;   // Tổng tiền = Tiền món + Tiền VAT

  return { rate, subtotal, vatAmount, totalWithVat };
};

const isQuickWalkInMode = ref(false);

// 🚨 HÀM XỬ LÝ CLICK VÀO BÀN TRÊN SƠ ĐỒ
const handleTableClick = async (ban) => {
  const trangThaiBan = getTrangThaiTheoNgay(ban.id);
  
  // 1. Nếu bàn đã có khách -> Mở thẳng Modal quản lý
  if (trangThaiBan === 1) {
    openManageModal(ban);
    return;
  } 
  
  // 2. Nếu đang trong chế độ tách/gộp bàn (Có Pending trong LocalStorage)
  const pendingSplitText = localStorage.getItem("pendingSplitCustomer");
  if (pendingSplitText && trangThaiBan === 0) {
    const pendingCustomer = JSON.parse(pendingSplitText);
    const khachDangCho = Number(pendingCustomer.soKhachCanXep) || 0;
    const sucChuaBanNay = Number(ban.soCho) || 0;

    const confirmSplit = await Swal.fire({
      title: 'Xác nhận mở bàn phụ?',
      html: `Đoàn của <b>${pendingCustomer.tenKhachHang}</b> đang thiếu <b>${khachDangCho} chỗ</b>.<br>Bàn <b>${ban.maBan}</b> có <b>${sucChuaBanNay} chỗ</b>.<br>Đồng ý xếp vào đây?`,
      icon: 'question',
      iconColor: '#7D161A',
      showCancelButton: true,
      confirmButtonColor: '#28a745',
      cancelButtonText: 'Chọn bàn khác',
      confirmButtonText: 'Xếp vào bàn này'
    });

    if (confirmSplit.isConfirmed) {
      try {
        let idHoaDonGoc = pendingCustomer.idHoaDonGoc;
        if (!idHoaDonGoc) return Swal.fire({ title: 'Lỗi', text: 'Không tìm thấy Hóa đơn gốc!', icon: 'error', confirmButtonText: 'Đóng' });

        await axiosClient.post(`/hoa-don-thanh-toan/${idHoaDonGoc}/them-ban-phu`, {
            idBanMoi: ban.id
        });
        
        const conLai = khachDangCho - sucChuaBanNay;
        if (conLai <= 0) {
            Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Hoàn tất!', text: 'Đã xếp đủ chỗ cho toàn bộ khách!', timer: 2000 });
            localStorage.removeItem("pendingSplitCustomer");
            isSelectingSecondTable.value = false;
        } else {
            pendingCustomer.soKhachCanXep = conLai;
            localStorage.setItem("pendingSplitCustomer", JSON.stringify(pendingCustomer));
            Swal.fire({ icon: 'warning', iconColor: '#7D161A', title: 'Vẫn thiếu chỗ!', html: `Đã xếp thêm khách vào ${ban.maBan}.<br>Đoàn vẫn còn <b>${conLai} người</b> chưa có chỗ.<br>Vui lòng click chọn thêm bàn nữa!`, confirmButtonText: 'Chọn tiếp' });
        }
        await handleFetchAllCheckIn();
        await fetchAllBan();
        await fetchTableStatus();
      } catch (error) {
        Swal.fire({ title: 'Lỗi', text: 'Không thể mở bàn phụ lúc này', icon: 'error', confirmButtonText: 'Đóng' });
      }
    }
    return;
  }

  // =======================================================
  // 🚨 3. ĐÃ SỬA Ở ĐÂY: CHẶN CLICK NẾU TẮT TOGGLE
  // =======================================================
  if (trangThaiBan === 0) {
    if (isQuickWalkInMode.value) {
       // A. NẾU ĐANG BẬT TOGGLE "MỞ BÀN NHANH" -> Thực hiện Check In
       await quickWalkInCheckIn(ban);
    } else {
       // B. 🚨 NẾU TẮT TOGGLE -> KHÔNG LÀM GÌ CẢ (CHẶN MỞ MODAL)
       return; 
    }
  }
};

const quickWalkInCheckIn = async (ban) => {
  const now = dayjs(currentTime.value);
  let thoiGianKhachDen = null;

  // Validate: Quét xem bàn này có ai book trong 3 tiếng tới không
  const isReservedSoon = danhSachCho.value.some(khach => {
    const isMatchTable = khach.idBanAn === ban.id || khach.maBan === ban.maBan;
    const isValidStatus = [0, 1].includes(Number(khach.trangThai));

    if (isMatchTable && isValidStatus) {
      const thoiGianDat = dayjs(khach.thoiGianDat);
      const diffMinutes = thoiGianDat.diff(now, 'minute');

      // Chặn nếu có khách đến trong vòng 3 tiếng (180 phút) tới
      if (diffMinutes >= -(sysParams.value.THOI_GIAN_GIU_BAN) && diffMinutes <= 180) {
        thoiGianKhachDen = thoiGianDat.format('HH:mm'); 
        return true; 
      }
    }
    return false;
  });

  if (isReservedSoon) {
    return Swal.fire({
        icon: 'error',
        title: 'Không thể mở bàn!',
        html: `Bàn <b>${ban.maBan}</b> đã có khách đặt trước vào lúc <b style="color:red; font-size:18px;">${thoiGianKhachDen}</b>.<br><br>Vui lòng xếp khách vãng lai sang bàn khác để không bị trùng lịch!`,
        confirmButtonText: 'Đã hiểu',
        confirmButtonColor: '#7d161a'
    });
  }

  // Pass Validate -> Gọi API tạo phiếu & Hóa đơn
  try {
    Swal.fire({ title: 'Đang Mở Bàn...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    
    const payload = {
      idBanAn: ban.id, 
      trangThai: 1, 
      id: null, 
      trangThaiPhieu: null,
      vatApDung: sysParams.value.VAT
    };

    await updateTrangThaiBan(payload);
    
    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công!', text: 'Đã mở bàn Khách vãng lai!', timer: 1000, showConfirmButton: false });
    
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();

    // Mở thẳng Modal Lên để gọi món
    await openManageModal(ban);
  } catch (err) {
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Mở bàn thất bại!' });
  }
};

const handleCheckInGuest = async (khach) => {
  const ban = danhSachBan.value.find(b => b.id === khach.idBanAn || b.maBan === khach.maBan);
  if (!ban) return Swal.fire({ title: 'Lỗi', text: 'Không tìm thấy thông tin bàn của khách này!', icon: 'error', confirmButtonText: 'Đóng' });

  const soKhach = khach.soNguoi || 1;
  const sucChua = Number(ban.soCho);

  if (soKhach > sucChua) {
    const swalResult = await Swal.fire({
      title: 'Bàn không đủ chỗ!',
      html: `Khách đi <b>${soKhach} người</b> nhưng bàn <b>${ban.maBan}</b> chỉ có <b>${sucChua} chỗ</b>.<br><br>Vui lòng chọn hướng xử lý:`,
      icon: 'warning',
      iconColor: '#7D161A',
      showDenyButton: true,
      showCancelButton: true,
      confirmButtonColor: '#7d161a',
      denyButtonColor: '#28a745',   
      confirmButtonText: '<i class="fa-solid fa-chair"></i> Kê thêm ghế',
      denyButtonText: '<i class="fa-solid fa-arrows-split-up-and-left"></i> Tách thành nhiều bàn',
      cancelButtonText: 'Hủy bỏ'
    });

    if (swalResult.isConfirmed) {
      selectedPhieu.value = { ...khach, soNguoi: soKhach };
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
    // 1. TÍNH TOÁN SỐ KHÁCH TRƯỚC
    const soKhachBanDau = Number(khach.soNguoi) || 1;
    const sucChuaBanGoc = Number(banGoc.soCho || banGoc.soNguoiToiDa) || 0; // Tùy key FE bạn đang dùng
    
    // Bàn gốc sẽ ngồi full sức chứa (hoặc ngồi hết số khách nếu khách ít hơn sức chứa)
    const soNguoiNgoiBanGoc = soKhachBanDau > sucChuaBanGoc ? sucChuaBanGoc : soKhachBanDau;
    
    // Số khách bị dư ra cần xếp sang bàn phụ
    const soKhachConLai = soKhachBanDau - soNguoiNgoiBanGoc;

    // 2. THỰC HIỆN CHECK-IN BÀN GỐC KÈM SỐ NGƯỜI
    const payloadBanGoc = {
      idBanAn: banGoc.id, 
      trangThai: 1, 
      id: khach.id, 
      trangThaiPhieu: 3,
      vatApDung: 0, // 🚨 Sửa thành 0 theo logic Flat VAT (Tiền thực tế) đã thống nhất
      soNguoi: soNguoiNgoiBanGoc // 🚨 GỬI KÈM SỐ NGƯỜI NGỒI BÀN CHÍNH
    };
    await updateTrangThaiBan(payloadBanGoc); 
    
    // 3. CHỜ 1 NHỊP VÀ LẤY ID HÓA ĐƠN
    const resHoaDon = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${banGoc.id}`);
    if (!resHoaDon.data || !resHoaDon.data.idHoaDon) {
        throw new Error("Lỗi không tạo được hóa đơn cho bàn chính!");
    }

    const idHoaDonVuaTao = resHoaDon.data.idHoaDon; 

    // 4. HIỂN THỊ THÔNG BÁO CHO NHÂN VIÊN
    await Swal.fire({
      title: 'Đã Check-in Bàn 1',
      html: `Đã xếp ${soNguoiNgoiBanGoc} khách vào bàn <b>${banGoc.maBan}</b>.<br><br>
             Đoàn còn <b style="color:red">${soKhachConLai} người</b> chưa có chỗ.<br>
             Vui lòng <b>Click vào một bàn trống khác</b> trên sơ đồ để tiếp tục xếp chỗ.`,
      icon: 'info',
      confirmButtonText: 'Đã hiểu'
    });

    // 5. GHI NHỚ THÔNG TIN VÀO LOCALSTORAGE CHO BÀN TIẾP THEO
    localStorage.setItem("pendingSplitCustomer", JSON.stringify({
      idHoaDonGoc: idHoaDonVuaTao, 
      tenKhachHang: khach.tenKhachHang || 'Khách vãng lai',
      soKhachCanXep: soKhachConLai
    }));

    await handleFetchAllCheckIn();
    await fetchAllBan();
  } catch (error) {
    console.error("Lỗi ghép bàn gốc:", error);
    Swal.fire({ title: 'Lỗi', text: 'Không thể check-in bàn gốc lúc này', icon: 'error', confirmButtonText: 'Đóng' });
  }
};
const filterTableId = ref(null);

const danhSachLoc = computed(() => {
  let filteredList = danhSachCho.value.filter((khach) => {
    if (!khach.maBan && !khach.idBanAn) return false;
    if ([2, 3, 4, 5].includes(Number(khach.trangThai))) return false;

    const thoiGianDat = dayjs(khach.thoiGianDat);
    const hienTai = dayjs(currentTime.value);
    const phutConLai = thoiGianDat.diff(hienTai, "minute");

    const window = Number(selectedTimeWindow.value);
    const matchTime = phutConLai <= window && phutConLai >= -(sysParams.value.THOI_GIAN_GIU_BAN); 
    
    const searchValue = searchQuery.value.trim().toLowerCase();
    const matchSearch = (khach.soDienThoai && khach.soDienThoai.includes(searchValue)) ||
                        (khach.tenKhachHang && khach.tenKhachHang.toLowerCase().includes(searchValue)) ||
                        (khach.maDatBan && khach.maDatBan.toLowerCase().includes(searchValue)) ||
                        (khach.maPhieu && khach.maPhieu.toLowerCase().includes(searchValue));
    
    let matchDate = true;
    if (filterDate.value) {
      matchDate = thoiGianDat.format("YYYY-MM-DD") === filterDate.value;
    }

    // 🔥 THÊM ĐIỀU KIỆN LỌC THEO BÀN
    let matchTable = true;
    if (filterTableId.value) {
      matchTable = (khach.idBanAn === filterTableId.value);
    }

    return matchSearch && matchTime && matchDate && matchTable;
  });

  filteredList.sort((a, b) => {
    // ... (Giữ nguyên logic sort của bạn)
    const timeA = dayjs(a.thoiGianDat).valueOf();
    const timeB = dayjs(b.thoiGianDat).valueOf();

    if (sortOption.value === 'time_asc') {
      return timeA - timeB; 
    } 
    else if (sortOption.value === 'time_desc') {
      return timeB - timeA; 
    } 
    else if (sortOption.value === 'floor_asc' || sortOption.value === 'floor_desc') {
      const banA = danhSachBan.value.find(tbl => tbl.id === a.idBanAn || tbl.maBan === a.maBan);
      const banB = danhSachBan.value.find(tbl => tbl.id === b.idBanAn || tbl.maBan === b.maBan);
      
      const tangA = banA ? Number(banA.soTang || banA.tang) : 999;
      const tangB = banB ? Number(banB.soTang || banB.tang) : 999;

      if (tangA !== tangB) {
        return sortOption.value === 'floor_asc' ? tangA - tangB : tangB - tangA;
      }
      return timeA - timeB; 
    }
    return 0;
  });

  return filteredList;
});


const currentTime = ref(new Date());
let timer;
let expiredTimer = null;
const currentStaffName = ref("");
const orderHistory = ref([]); 

const fetchOrderHistory = async () => {
  if (!selectedPhieu.value?.idHoaDon) return;
  try {
    const res = await axiosClient.get(`/hoa-don-thanh-toan/lich-su/${selectedPhieu.value.idHoaDon}`);
    orderHistory.value = res.data;
    modalView.value = 'orderHistory'; 
  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: 'Không thể lấy dữ liệu lịch sử!', icon: 'error', confirmButtonText: 'Đóng' });
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

onMounted(async () => {
  // 🚨 Đợi lấy tham số hệ thống trước rồi mới load danh sách
  await loadSystemParams();
  
  fetchAllBan();
  handleFetchAllCheckIn();
  
  timer = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);

  expiredTimer = setInterval(() => {
    checkExpiredTickets(); 
  }, 20000); 

  const user = JSON.parse(localStorage.getItem("user"));
  if (user) currentStaffName.value = user.hoTen || user.username;

  setInterval(() => {
    checkOverdueTables();
  }, 3 * 60 * 1000); 
  
  setTimeout(() => checkOverdueTables(), 5000);
});

onUnmounted(() => {
  // Dọn dẹp các bộ đếm khi chuyển sang trang khác
  if (timer) clearInterval(timer);
  if (expiredTimer) clearInterval(expiredTimer);
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
  
  // Chỉ lấy các bàn đang CÓ KHÁCH (trạng thái = 1) và KHÁC bàn hiện tại
  const cacBanCoKhach = danhSachBan.value.filter(ban => 
    getTrangThaiTheoNgay(ban.id) === 1 && ban.id !== selectedBan.value?.id
  );

  for (const ban of cacBanCoKhach) {
    try {
      const res = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${ban.id}`);
      
      // Chỉ cần bàn đó có Hóa đơn hoạt động là được phép gộp (Không quan tâm ID khách nữa)
      if (res.data && res.data.idHoaDon) {
          const chiTiet = res.data.chiTiet || res.data.chiTietHoaDon || res.data.chiTietMonAn || [];
          const monCuaBan = chiTiet.filter(m => m.trangThaiMon !== 0);
          
          // Cờ kiểm tra xem bàn này đã lên đủ món chưa
          const allServed = monCuaBan.length === 0 || monCuaBan.every(m => m.trangThaiMon === 2);
          
          // Tính toán tiền
          const tongTienMon = monCuaBan.reduce((sum, item) => sum + (item.donGia * item.soLuong), 0);
          const canThu = tongTienMon - (res.data.soTienDaGiam || 0) - (res.data.tienCoc || 0);

          listBanCoKhachKhac.value.push({
            ...ban,
            _hoaDonInfo: {
              idHoaDon: res.data.idHoaDon,
              idPhieu: res.data.id,
              idBanAn: ban.id,
              canThu: canThu > 0 ? canThu : 0, 
              allServed: allServed,
              tenKhach: res.data.tenKhachHang || 'Khách vãng lai' // 🚨 Lấy tên khách để hiển thị
            }
          });
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
        selectedPhieu.value = {
          id: targetKhach ? maPhieuCuaKhach : data.id, 
          idHoaDon: data.idHoaDon, 
          maDatBan: targetKhach ? (targetKhach.maDatBan || targetKhach.maPhieu) : (data.maPhieu || data.maDatBan || 'N/A'),
          tenKhachHang: targetKhach ? targetKhach.tenKhachHang : (data.tenKhachHang || 'Khách tại quán'),
          idKhachHang: targetKhach ? targetKhach.idKhachHang : data.idKhachHang, 
          thoiGianDat: targetKhach ? targetKhach.thoiGianDat : data.thoiGianDat,
          soNguoi: (data.soNguoiBanNay && data.soNguoiBanNay > 0) 
                   ? data.soNguoiBanNay 
                   : (targetKhach ? (targetKhach.soNguoi || targetKhach.soLuongKhach) : data.soNguoi),
          tongTienChuaGiam: data.tongTienChuaGiam || 0,
          soTienDaGiam: data.soTienDaGiam || 0,
          tienCoc: data.tienCoc !== undefined ? data.tienCoc : (targetKhach ? targetKhach.tienCoc : 0), 
          tongTienThanhToan: data.tongTienThanhToan || 0,
          vatApDung: data.vatApDung ?? 10,
          idPhieuGiamGia: data.idPhieuGiamGia || null,
          maPhieuGiamGia: data.maPhieuGiamGia || null,
          trangThai: targetKhach ? targetKhach.trangThai : data.trangThai,
          
          // 🚨 ĐÃ FIX: Nhận mảng danhSachBan từ BE (Nếu BE trả về rỗng, tạo mảng chứa chính nó)
          danhSachBan: data.danhSachBan && data.danhSachBan.length > 0 ? data.danhSachBan : [{ id: bId, maBan: selectedBan.value.maBan }], 
          isBanPhu: data.isBanPhu || false,
          tenBanChinh: data.tenBanChinh || selectedBan.value.maBan,
          idBanChinh: data.idBanChinh || bId
        };

        const listFromApiRaw = data.chiTiet || [];
        if (listFromApiRaw.length > 0) {
          listMonDaChon.value = mapAndGroupItems(listFromApiRaw);
        }
      }
    } else {
        handleEmptyTableState(targetKhach);
    }
  } catch (e) {
    console.log("Bàn trống hoặc API lỗi:", e);
    handleEmptyTableState(targetKhach);
  }

  isShowModal.value = true;
};

const isShowAllTicketsModal = ref(false);
const activeTicketTab = ref('WAITING'); // 'WAITING' hoặc 'SERVING'

const openAllTicketsModal = () => {
  isShowAllTicketsModal.value = true;
};

// Computed lấy danh sách BÀN ĐANG ĂN (Bao gồm cả đặt trước và vãng lai)
const danhSachBanDangAn = computed(() => {
  let activeTables = danhSachBan.value.filter(ban => getTrangThaiTheoNgay(ban.id) === 1);
  
  // 🔥 THÊM ĐIỀU KIỆN LỌC THEO BÀN
  if (filterTableId.value) {
    activeTables = activeTables.filter(ban => ban.id === filterTableId.value);
  }

  const searchValue = searchQuery.value.trim().toLowerCase();
  if (searchValue) {
    activeTables = activeTables.filter(ban => 
       ban.maBan.toLowerCase().includes(searchValue) || 
       (ban.tenKhuVuc && ban.tenKhuVuc.toLowerCase().includes(searchValue))
    );
  }
  
  if (sortOption.value === 'floor_asc') {
    activeTables.sort((a, b) => Number(a.soTang || a.tang) - Number(b.soTang || b.tang));
  } else if (sortOption.value === 'floor_desc') {
    activeTables.sort((a, b) => Number(b.soTang || b.tang) - Number(a.soTang || a.tang));
  }

  return activeTables;
});

// Hàm bọc để đóng modal trước khi mở Bàn/Check-in
const modalHandleCheckInGuest = (khach) => {
  isShowAllTicketsModal.value = false;
  handleCheckInGuest(khach);
};

const modalHandleTableClick = (ban) => {
  isShowAllTicketsModal.value = false;
  handleTableClick(ban);
};

const mapAndGroupItems = (rawList) => {
  const validItems = rawList.filter(m => m.trangThaiMon !== 0); 
  const grouped = {};

  validItems.forEach((mon) => {
    const isSet = mon.type === 'SET' || mon.idSetLau != null;
    const originalId = mon.id || mon.idChiTietMonAn || mon.idSetLau;
    const uniqueId = originalId ? (isSet ? `set_${originalId}` : `food_${originalId}`) : `unknown_${mon.tenMon}`;

    let formattedNote = mon.ghiChu || '';
    if (mon.tenBanPhucVu && mon.tenBanPhucVu !== 'Bàn gốc' && mon.tenBanPhucVu !== 'Bàn chung') {
      formattedNote = `[${mon.tenBanPhucVu}] ${mon.ghiChu || ''}`.trim();
    }

    if (grouped[uniqueId]) {
      grouped[uniqueId].quantity += (mon.soLuong || 1);
      if (formattedNote && !grouped[uniqueId].note.includes(formattedNote)) {
        grouped[uniqueId].note += grouped[uniqueId].note ? ' | ' + formattedNote : formattedNote;
      }
    } else {
      grouped[uniqueId] = {
        id: uniqueId,
        dbDetailId: mon.idChiTietHd || mon.id, 
        originalId: originalId,
        type: isSet ? 'SET' : 'FOOD',
        uniqueId: uniqueId,
        name: mon.tenMon || 'Món không tên',
        quantity: mon.soLuong || 1,
        price: mon.donGia || 0,
        note: formattedNote,
        served: mon.trangThaiMon === 2,
        
        // 🚨 THÊM DÒNG NÀY: Lấy cấu hình VAT từ Backend (Nếu null thì mặc định loại 1)
        // Lưu ý: Đổi "apDungLoaiVat" thành đúng tên field mà API của bạn trả về
        vatType: mon.apDungLoaiVat || mon.ap_dung_loai_vat || 1 
      };
    }
  });
  return Object.values(grouped); 
};

// 🚨 ĐÃ FIX HÀM PHỤ: Bổ sung các trường danhSachBan, isBanPhu để không bị null khi load lỗi/Bàn chờ Check-in
const handleEmptyTableState = (targetKhach) => {
    if (targetKhach) {
      selectedPhieu.value = {
        id: targetKhach.idDatBan || targetKhach.idPhieu || targetKhach.id,
        idHoaDon: null, 
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
        trangThai: targetKhach.trangThai,
        // Giả lập danh sách bàn bằng chính nó nếu là bàn trống/chưa gọi món
        danhSachBan: [{ id: selectedBan.value.id, maBan: selectedBan.value.maBan }],
        isBanPhu: false,
        tenBanChinh: selectedBan.value.maBan,
        idBanChinh: selectedBan.value.id
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
  let tongTienMon = 0;
  let tongTienVat = 0;

  // Quét qua từng món đã chọn để tính VAT riêng cho nó
  listMonDaChon.value.forEach(item => {
    const thanhTienCuaMon = item.price * item.quantity;
    tongTienMon += thanhTienCuaMon;

    // Xác định % VAT dựa trên loại VAT của danh mục món đó
    let tyLeVat = 0;
    if (item.vatType === 1) {
        tyLeVat = sysParams.value.VAT; // Vd: 10%
    } else if (item.vatType === 2) {
        tyLeVat = sysParams.value.VAT_DONG_GOI; // Vd: 8%
    }
    // Nếu vatType === 3 (Không VAT) thì tyLeVat = 0

    // Cộng dồn tiền VAT của món này vào Tổng VAT hóa đơn
    tongTienVat += thanhTienCuaMon * (tyLeVat / 100);
  });

  const giam = selectedPhieu.value?.soTienDaGiam || 0;
  const coc = selectedPhieu.value?.tienCoc || 0;
  
  // Công thức thanh toán: (Tiền món + Tiền VAT) - Khuyến mãi - Tiền cọc
  let canThu = (tongTienMon + tongTienVat) - giam - coc; 
  if (canThu < 0) canThu = 0;
  
  return { 
    tong: tongTienMon, 
    tienVat: tongTienVat, 
    giam: giam, 
    coc: coc, 
    canThu: canThu 
  }; 
});

const currentRawTotal = computed(() => billSummary.value.tong || 0);

const totalVatToPay = computed(() => billSummary.value.tienVat || 0);

const grandTotalToPay = computed(() => billSummary.value.canThu || 0);

// Chỉ thanh toán được khi Đã lên đủ món
const canPay = computed(() => {
  if (selectedPhieu.value?.isBanPhu) return false; // Bàn phụ khóa thanh toán
  return hasItems.value && isAllItemsServed.value;
});

// 🚨 HÀM MỚI: Chuyển nhanh từ Bàn Phụ sang Bàn Chủ
const switchToMainTable = async () => {
  if (!selectedPhieu.value?.idBanChinh) return;
  const mainTableId = selectedPhieu.value.idBanChinh;
  
  // Tìm bàn chính trong danh sách bàn trên sơ đồ
  const mainTable = danhSachBan.value.find(b => b.id === mainTableId);
  
  if (mainTable) {
    // Mở đè Modal Bàn Chính lên luôn
    await openManageModal(mainTable);
  } else {
    Swal.fire('Lỗi', 'Không tìm thấy thông tin bàn chính trên sơ đồ!', 'error');
  }
};
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
             iconColor: '#7D161A',
             title: 'Đã hủy ưu đãi!',
             text: 'Tổng tiền món hiện tại không còn đủ điều kiện áp dụng mã giảm giá cũ. Hệ thống đã tự động gỡ ưu đãi này ra khỏi hóa đơn.',
             confirmButtonColor: '#7d161a',
             confirmButtonText: 'Đã hiểu'
          });
      }

      // ========================================================
      // 🚨 ĐÃ SỬA: Đồng bộ cách map dữ liệu giống hệt lúc mở Modal
      // ========================================================
      const listFromApiRaw = resPhieu.data.chiTiet || resPhieu.data.chiTietHoaDon || [];
      if (listFromApiRaw.length > 0) {
        listMonDaChon.value = mapAndGroupItems(listFromApiRaw);
      } else {
        listMonDaChon.value = [];
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
    
    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công!', text: 'Đã check-in bàn!', timer: 1500, showConfirmButton: false });
    
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();

    // 🚨 THÊM DÒNG NÀY (THE FIX): Tự động load lại dữ liệu của chính bàn này 
    // để lấy được idHoaDon vừa được Backend tạo ra -> Các nút sẽ lập tức sáng lên!
    await openManageModal(selectedBan.value);

  } catch (err) {
    Swal.fire({ title: 'Lỗi', text: 'Check-in thất bại!', icon: 'error', confirmButtonText: 'Đóng' });
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
    Swal.fire({ title: 'Lưu ý', text: 'Bàn này chưa có Phiếu đặt trước. Không hỗ trợ thêm món!', icon: 'warning', iconColor: '#7D161A', confirmButtonText: 'Đã hiểu' });
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
    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công!', text: "Đã cập nhật thực đơn!", timer: 1500, showConfirmButton: false });
    handleCloseFoodList();
  } catch (e) {
    Swal.fire({ title: 'Lỗi', text: "Lỗi lưu món", icon: 'error', confirmButtonText: 'Đóng' });
  }
};

const handlePaymentCash = async () => {
  const amountToPay = grandTotalToPay.value;
  
  const confirm = await Swal.fire({
    title: 'Xác nhận thanh toán TIỀN MẶT?',
    html: `Tổng tiền thanh toán: <br><br>
           <b style="color: #28a745; font-size: 26px">${amountToPay.toLocaleString()} đ</b>`,
    icon: 'warning',
    iconColor: '#7D161A',
    showCancelButton: true,
    confirmButtonColor: '#28a745', 
    confirmButtonText: 'Đã nhận đủ tiền',
    cancelButtonText: 'Quay lại'
  });

  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
    
    // Gửi yêu cầu thanh toán DUY NHẤT 1 LẦN cho Hóa đơn (Backend tự dọn dẹp các Bàn phụ)
    const payloadChinh = {
      idBanAn: selectedBan.value.id,
      trangThai: 0,
      id: selectedPhieu.value.id,
      trangThaiPhieu: 4, // 4: Hoàn thành
      trangThaiHoaDon: 6, // 6: Đã thanh toán
      tienMat: amountToPay, 
      ghiChu: `Thanh toán Tiền mặt`
    };
    
    await updateTrangThaiBan(payloadChinh);

    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công!', text: 'Đã thanh toán và dọn bàn hoàn tất!', timer: 1500 });
    
    closeModal();
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();
  } catch (err) {
    Swal.fire({ title: 'Lỗi', text: 'Lỗi khi thanh toán', icon: 'error', confirmButtonText: 'Đóng' });
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
      iconColor: '#7D161A',
      title: 'Đã copy link!', 
      text: 'Bạn có thể dán gửi cho khách',
      timer: 1500, 
      showConfirmButton: false 
    });
  } catch (err) {
    Swal.fire({ title: 'Lỗi', text: 'Không thể copy link', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

const handlePaymentVNPayFull = async () => {
  const amountToPay = grandTotalToPay.value;
  if (amountToPay <= 0) return Swal.fire('Lưu ý', 'Hóa đơn 0đ, vui lòng chọn Hoàn tất (0đ) để đóng bàn!', 'info');
  
  const confirm = await Swal.fire({
    title: 'Thanh toán VNPay?',
    text: `Chuyển cổng VNPay với tổng tiền ${amountToPay.toLocaleString()} đ. Đồng ý?`,
    icon: 'question',
    iconColor: '#7D161A',
    showCancelButton: true,
    confirmButtonColor: '#007bff',
    confirmButtonText: 'Tiếp tục',
    cancelButtonText: 'Hủy'
  });

  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang kết nối VNPay...', didOpen: () => Swal.showLoading() });
    
    let chuoiIdHoaDon = String(selectedPhieu.value.idHoaDon);

    // Cập nhật trạng thái chờ VNPay
    await updateTrangThaiBan({ 
        idBanAn: selectedBan.value.id, 
        trangThai: 1, 
        id: selectedPhieu.value.id, 
        trangThaiPhieu: 3, 
        trangThaiHoaDon: 5, 
        tienMat: 0 
    });
    
    closeModal();
    
    const vnpayRes = await axiosClient.get(`/vnpay/create-payment/${chuoiIdHoaDon}?amount=${amountToPay}`);
    if (vnpayRes.data && vnpayRes.data.url) {
      window.location.href = vnpayRes.data.url;
    }
  } catch (err) {
    Swal.fire({ title: 'Lỗi', text: 'Khởi tạo VNPay thất bại', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

const handlePaymentMixed = async () => {
  const totalInvoice = grandTotalToPay.value;
  if (totalInvoice <= 0) return Swal.fire({ title: 'Lưu ý', text: 'Hóa đơn 0đ, vui lòng chọn Hoàn tất (0đ) để đóng bàn!', icon: 'info', confirmButtonText: 'Đã hiểu' });
  
  const { value: rawCashAmount } = await Swal.fire({
    title: 'Thanh toán hỗn hợp',
    input: 'text', // 🚨 Đổi sang text để hỗ trợ gõ dấu phẩy
    inputLabel: `Tổng hóa đơn: ${totalInvoice.toLocaleString()}đ`,
    inputPlaceholder: 'Nhập số tiền mặt...',
    showCancelButton: true,
    confirmButtonText: 'Tiếp tục sang VNPay',
    cancelButtonText: 'Hủy bỏ',
    
    // 🚨 HIỆU ỨNG TỰ ĐỘNG THÊM DẤU PHẨY KHI GÕ
    didOpen: () => {
      const input = Swal.getInput();
      input.addEventListener('input', (e) => {
        // Lọc bỏ mọi ký tự không phải là số
        let val = e.target.value.replace(/[^0-9]/g, '');
        if (val) {
          // Thêm dấu phẩy phân cách hàng nghìn (en-US sẽ cho chuẩn 000,000)
          e.target.value = parseInt(val, 10).toLocaleString('en-US');
        } else {
          e.target.value = '';
        }
      });
    },
    // Validate dữ liệu (phải loại bỏ dấu phẩy trước khi so sánh)
    inputValidator: (value) => {
      const numValue = Number(value.replace(/,/g, ''));
      if (!numValue || numValue <= 0) return 'Vui lòng nhập số hợp lệ!';
      if (numValue >= totalInvoice) return 'Tiền mặt phải nhỏ hơn tổng hóa đơn!';
    },
    // Trả về dữ liệu dạng số thô để xử lý
    preConfirm: (value) => {
      return value.replace(/,/g, ''); 
    }
  });

  if (rawCashAmount) {
    const cashAmount = Number(rawCashAmount); // Đã là số thô do preConfirm xử lý
    const remaining = totalInvoice - cashAmount;

    try {
      Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
      let chuoiIdHoaDon = String(selectedPhieu.value.idHoaDon);

      await updateTrangThaiBan({ 
          idBanAn: selectedBan.value.id, 
          trangThai: 1, 
          id: selectedPhieu.value.id, 
          trangThaiPhieu: 3, 
          trangThaiHoaDon: 5, 
          idNhanVien: getCurrentStaffId(), 
          tienMat: cashAmount, 
          ghiChu: `Thu hỗn hợp ${cashAmount.toLocaleString()}đ` 
      });

      const vnpayRes = await axiosClient.get(`/vnpay/create-payment/${chuoiIdHoaDon}?amount=${remaining}`);
      if (vnpayRes.data && vnpayRes.data.url) window.location.href = vnpayRes.data.url;
    } catch (error) {
      Swal.fire({ title: 'Lỗi', text: 'Lỗi thanh toán', icon: 'error', confirmButtonText: 'Đóng' });
    }
  }
};

const handleCancelTicket = async () => {
  if (!selectedPhieu.value?.id) return Swal.fire('Lưu ý', 'Bàn này chưa có phiếu để hủy!', 'warning');
  const confirm = await Swal.fire({ title: 'Hủy phiếu đặt bàn?', text: 'Phiếu sẽ bị hủy và bàn sẽ được dọn trống. Bạn chắc chắn chứ?', icon: 'warning', iconColor: '#7D161A', showCancelButton: true, confirmButtonColor: '#d33', confirmButtonText: 'Đồng ý hủy', cancelButtonText: 'Quay lại' });
  
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
  Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Đã hủy', timer: 1000, confirmButtonText: 'Đã hiểu' });
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

watch(isQuickWalkInMode, (newVal) => {
  if (newVal === true) {
    selectedTimeWindow.value = 180;
    handleFetchAllCheckIn();
  } else {
    selectedTimeWindow.value = 15;
    handleFetchAllCheckIn();
  }
});

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
  // 1. Kiểm tra xem bàn hiện tại có đang bị ngồi không (Chống click bàn đỏ)
  if (getTrangThaiTheoNgay(banMoi.id) !== 0) {
      return Swal.fire({ title: 'Lỗi', text: 'Bàn này không trống. Vui lòng chọn bàn màu xám!', icon: 'error', confirmButtonText: 'Đã hiểu' });
  }

  // ========================================================
  // 🚨 2. VALIDATE: KIỂM TRA LỊCH ĐẶT BÀN TRONG 3 GIỜ TỚI
  // ========================================================
  const now = dayjs(currentTime.value);
  let thoiGianKhachDen = null; // Biến lưu lại giờ khách đến để báo lỗi cho chi tiết

  const isReservedSoon = danhSachCho.value.some(khach => {
    const isMatchTable = khach.idBanAn === banMoi.id || khach.maBan === banMoi.maBan;
    
    // Chỉ quan tâm các phiếu chưa check-in (0: Chờ xác nhận, 1: Đã xác nhận)
    const isValidStatus = [0, 1].includes(Number(khach.trangThai));

    if (isMatchTable && isValidStatus) {
      const thoiGianDat = dayjs(khach.thoiGianDat);
      const diffMinutes = thoiGianDat.diff(now, 'minute');

      // Chặn nếu khách đến trong vòng 3 tiếng (180 phút) tới, 
      // Hoặc đang trễ hẹn (diffMinutes >= -30 phút)
      if (diffMinutes >= -30 && diffMinutes <= 180) {
        thoiGianKhachDen = thoiGianDat.format('HH:mm'); // Lấy giờ chuẩn để báo lỗi
        return true; 
      }
    }
    return false;
  });

  if (isReservedSoon) {
      return Swal.fire({
          icon: 'warning',
          iconColor: '#7D161A',
          title: 'Bàn đã được đặt trước!',
          html: `Bàn <b>${banMoi.maBan}</b> sẽ có khách đến nhận vào lúc <b style="color:red; font-size:18px;">${thoiGianKhachDen}</b>.<br><br>Không thể đổi/ghép vào bàn này để tránh trùng lịch. Vui lòng chọn bàn khác!`,
          confirmButtonText: 'Đã hiểu',
          confirmButtonColor: '#7d161a'
      });
  }
  // ========================================================


  // 3. TRƯỜNG HỢP: ĐANG TÁCH / THÊM BÀN PHỤ
  if (isSelectingSecondTable.value || localStorage.getItem("pendingSplitCustomer")) {
        try {
          const pendingText = localStorage.getItem("pendingSplitCustomer");
          let idHoaDonGoc = pendingText ? JSON.parse(pendingText).idHoaDonGoc : selectedPhieu.value?.idHoaDon;
          let khachDangCho = pendingText ? Number(JSON.parse(pendingText).soKhachCanXep) : 0;

          if (!idHoaDonGoc) return Swal.fire({ title: 'Lỗi', text: 'Không tìm thấy Hóa đơn gốc để ghép!', icon: 'error', confirmButtonText: 'Đóng' });

          Swal.fire({ title: 'Đang xử lý...', didOpen: () => Swal.showLoading() });

          // 🚨 TÍNH TOÁN SỐ NGƯỜI
          const sucChuaBanMoi = Number(banMoi.soCho);
          const soNguoiNgoiBanMoi = khachDangCho > sucChuaBanMoi ? sucChuaBanMoi : khachDangCho;

          // In ra console của trình duyệt (F12) để chắc chắn biến không bị undefined
          console.log("PAYLOAD GỬI ĐI THÊM BÀN PHỤ:", { 
              idBanMoi: banMoi.id, 
              soNguoi: soNguoiNgoiBanMoi 
          });

          // GỌI API
          await axiosClient.post(`/hoa-don-thanh-toan/${idHoaDonGoc}/them-ban-phu`, {
              idBanMoi: banMoi.id,
              soNguoi: soNguoiNgoiBanMoi
          });
          
          const conLai = khachDangCho - sucChuaBanMoi;

      if (conLai <= 0) {
          Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Hoàn tất', text: 'Đã ghép bàn thành công!', timer: 1500 });
          isSelectingSecondTable.value = false;
          localStorage.removeItem("pendingSplitCustomer");
          if (modalView.value === 'changeTable') closeModal();
      } else {
          if (pendingText) {
             const obj = JSON.parse(pendingText);
             obj.soKhachCanXep = conLai;
             localStorage.setItem("pendingSplitCustomer", JSON.stringify(obj));
          }
          Swal.fire({ icon: 'warning', iconColor: '#7D161A', title: 'Vẫn thiếu chỗ!', html: `Còn <b>${conLai} khách</b> chưa có chỗ.<br>Hãy chọn thêm bàn trống!`, confirmButtonText: 'Đã hiểu' });
      }

      await handleFetchAllCheckIn();
      await fetchAllBan();
      await fetchTableStatus();
      return;
    } catch (error) {
      return Swal.fire({ title: 'Lỗi', text: 'Không thể mở bàn phụ lúc này', icon: 'error', confirmButtonText: 'Đóng' });
    }
  }

  // 4. TRƯỜNG HỢP: ĐỔI BÀN TOÀN BỘ (CHUYỂN NHÀ)
  const soKhach = selectedPhieu.value?.soNguoi || 1;
  const sucChuaBanMoi = Number(banMoi.soCho);

  if (soKhach > sucChuaBanMoi) {
    const swalResult = await Swal.fire({
      title: 'Bàn mới không đủ chỗ!',
      html: `Đoàn có <b>${soKhach} người</b> nhưng bàn <b>${banMoi.maBan}</b> chỉ có <b>${sucChuaBanMoi} chỗ</b>.<br>Chọn hướng xử lý:`,
      icon: 'warning',
      iconColor: '#7D161A',
      showDenyButton: true,
      showCancelButton: true,
      confirmButtonColor: '#7d161a',
      denyButtonColor: '#28a745',
      confirmButtonText: '<i class="fa-solid fa-chair"></i> Kê ghế',
      denyButtonText: '<i class="fa-solid fa-arrows-split-up-and-left"></i> Đổi & Mở bàn phụ',
      cancelButtonText: 'Hủy'
    });

    if (swalResult.isConfirmed) {
       // Kê ghế
    } else if (swalResult.isDenied) {
      try {
        await updateTrangThaiBan({ id: selectedPhieu.value.id, idBanAn: selectedBan.value.id, idBanAnMoi: banMoi.id, idNhanVien: getCurrentStaffId() || 1, trangThai: 0 });
        isSelectingSecondTable.value = true;
        const soKhachConLai = soKhach - sucChuaBanMoi;

        localStorage.setItem("pendingSplitCustomer", JSON.stringify({
          idHoaDonGoc: selectedPhieu.value.idHoaDon, 
          soKhachCanXep: soKhachConLai 
        }));

        selectedBan.value = { ...banMoi, trangThai: 1 };
        await handleFetchAllCheckIn();
        await fetchAllBan();
        
        Swal.fire({ title: 'Đã chuyển nhà', html: `Vui lòng chọn tiếp 1 bàn trống bên dưới cho ${soKhachConLai} người còn lại.`, iconColor: '#7D161A', icon: 'success' });
        return; 
      } catch (error) {
        return Swal.fire({ title: 'Lỗi', text: 'Lỗi đổi và tách bàn!', icon: 'error', confirmButtonText: 'Đóng' });
      }
    } else { return; }
  } else {
    const confirm = await Swal.fire({ title: 'Xác nhận đổi?', text: `Sang bàn ${banMoi.maBan}?`, icon: 'question', iconColor: '#7D161A', showCancelButton: true, confirmButtonText: 'Lưu thay đổi',
    cancelButtonText: 'Hủy', confirmButtonColor: '#7d161a' });
    if (!confirm.isConfirmed) return;
  }

  try {
    await updateTrangThaiBan({ id: selectedPhieu.value.id, idBanAn: selectedBan.value.id, idBanAnMoi: banMoi.id, idNhanVien: getCurrentStaffId() || 1, trangThai: 0, vatApDung: 0.0 });
    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', text: 'Đổi bàn hoàn tất!', timer: 1500, showConfirmButton: false });
    closeModal();
    await handleFetchAllCheckIn();
    await fetchAllBan();
  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: 'Lỗi đổi bàn!', icon: 'error', confirmButtonText: 'Đóng' });
  }
};


const isTableInCurrentGroup = (banId) => {
  if (!selectedPhieu.value || !selectedPhieu.value.danhSachBan) return false;
  return selectedPhieu.value.danhSachBan.some(b => b.id === banId);
};

const danhSachBanChonGop = ref([]);

const openMergeTableView = () => {
  modalView.value = 'mergeTable';
  modalActiveFloor.value = Number(selectedBan.value?.soTang) || 1;
  danhSachBanChonGop.value = []; // Reset mảng mỗi khi mở lại tab này
};

const toggleSelectBanGop = (ban) => {
  const index = danhSachBanChonGop.value.findIndex(b => b.id === ban.id);
  if (index !== -1) {
    danhSachBanChonGop.value.splice(index, 1); // Bỏ chọn
  } else {
    danhSachBanChonGop.value.push(ban); // Chọn
  }
};

const handleConfirmMergeBulk = async () => {
  if (danhSachBanChonGop.value.length === 0) return;

  const tableNames = danhSachBanChonGop.value.map(b => b.maBan).join(', ');

  const confirm = await Swal.fire({
    title: 'Xác nhận gộp bàn?',
    html: `Chuyển TOÀN BỘ món ăn, khách và tiền cọc từ các bàn: <br><b style="color:red; font-size: 18px;">${tableNames}</b><br><br>Sang đoàn của bàn <b style="color:green; font-size: 18px;">${selectedBan.value.maBan}</b>?<br><br><i>Lưu ý: Các bàn bị gộp sẽ dùng chung hóa đơn với bàn này.</i>`,
    icon: 'warning',
    iconColor: '#7D161A',
    showCancelButton: true,
    confirmButtonColor: '#7d161a',
    cancelButtonText: 'Hủy bỏ',
    confirmButtonText: 'Đồng ý gộp'
  });

  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang xử lý gộp bàn...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    // Dùng vòng lặp for...of để gọi API gộp lần lượt từng bàn vào bàn chính
    for (const banBiNuot of danhSachBanChonGop.value) {
      // Lấy ID Hóa Đơn của Bàn bị gộp
      const resTarget = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${banBiNuot.id}`);
      const idHoaDonBiNuot = resTarget.data?.idHoaDon;

      if (idHoaDonBiNuot) {
        const payload = {
          idHoaDonChu: selectedPhieu.value.idHoaDon, 
          idHoaDonBiNuot: idHoaDonBiNuot,             
          idNhanVien: getCurrentStaffId() || 1
        };
        await axiosClient.post('/hoa-don-thanh-toan/gop-ban', payload);
      }
    }

    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', text: 'Đã gộp tất cả các bàn hoàn tất!', timer: 1500, showConfirmButton: false });
    
    danhSachBanChonGop.value = []; // Clear mảng
    closeModal();
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();

  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: error.response?.data?.message || 'Lỗi khi gộp bàn!', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

// ========================================================
// 🚨 XỬ LÝ KHI CLICK CHỌN BÀN ĐỂ GỘP
// ========================================================
const handleConfirmMerge = async (banBiNuot) => {
  const confirm = await Swal.fire({
    title: 'Xác nhận gộp bàn?',
    html: `Chuyển TOÀN BỘ món ăn, khách và tiền cọc từ <b style="color:red">${banBiNuot.maBan}</b> sang đoàn của bàn <b style="color:green">${selectedBan.value.maBan}</b>?<br><br><i>Lưu ý: Bàn ${banBiNuot.maBan} sẽ dùng chung hóa đơn với bàn này.</i>`,
    icon: 'warning',
    iconColor: '#7D161A',
    showCancelButton: true,
    confirmButtonColor: '#7d161a',
    cancelButtonText: 'Hủy bỏ',
    confirmButtonText: 'Đồng ý gộp'
  });

  if (!confirm.isConfirmed) return;

  try {
    Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

    // Lấy ID Hóa Đơn của Bàn bị gộp
    const resTarget = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${banBiNuot.id}`);
    const idHoaDonBiNuot = resTarget.data?.idHoaDon;

    if (!idHoaDonBiNuot) {
      return Swal.fire('Lỗi', 'Bàn này không có hóa đơn hợp lệ để gộp!', 'error');
    }

    // Gọi API Gộp bàn ở Backend
    const payload = {
      idHoaDonChu: selectedPhieu.value.idHoaDon, 
      idHoaDonBiNuot: idHoaDonBiNuot,             
      idNhanVien: getCurrentStaffId() || 1
    };

    await axiosClient.post('/hoa-don-thanh-toan/gop-ban', payload);

    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', text: 'Đã gộp bàn hoàn tất!', timer: 1500, showConfirmButton: false });
    
    closeModal();
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();

  } catch (error) {
    Swal.fire('Lỗi', error.response?.data?.message || 'Lỗi khi gộp bàn!', 'error');
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

const primaryTableInGroup = computed(() => {
  return listBanCoKhachKhac.value.find(b => b._hoaDonInfo.idPhieu !== selectedPhieu.value?.id);
});


const toggleItemServed = async (item) => {
  try {
    const newStatus = item.served ? 1 : 2; 
    await axiosClient.put(`/dat-ban/chi-tiet-hoa-don/${item.dbDetailId}/trang-thai?trangThai=${newStatus}&idNhanVien=${getCurrentStaffId()||1}`);
    item.served = !item.served;
  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: 'Không thể cập nhật!', icon: 'error', confirmButtonText: 'Đóng' });
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
    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công', timer: 1000, showConfirmButton: false });
  } catch (error) {
    Swal.fire({ title: 'Lỗi', text: 'Lỗi khi cập nhật món!', icon: 'error', confirmButtonText: 'Đóng' });
  }
};

const handleDeleteItem = async (item, index) => {
const confirm = await Swal.fire({ title: 'Xóa món?', icon: 'warning', iconColor: '#7D161A', showCancelButton: true, confirmButtonColor: '#dc3545', confirmButtonText: 'Xóa', cancelButtonText: 'Hủy' });
  if (confirm.isConfirmed) {
    try {
      await axiosClient.put(`/dat-ban/chi-tiet-hoa-don/${item.dbDetailId}/trang-thai?trangThai=0`);
      listMonDaChon.value.splice(index, 1);
      Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Đã xóa!', timer: 1000, showConfirmButton: false });
      if (listMonDaChon.value.length === 0) modalView.value = 'info';
    } catch (error) {
      Swal.fire({ title: 'Lỗi', text: 'Không thể xóa!', icon: 'error', confirmButtonText: 'Đóng' });
    }
  }
};

const handleCancelWaitingTicket = async (khach) => {
  const now = dayjs(currentTime.value);
  const target = dayjs(khach.thoiGianDat);
  const diffMinutes = target.diff(now, "minute");

  // 🚨 Dùng biến THOI_GIAN_HUY_HOAN_COC
  const phutHuyAnToan = sysParams.value.THOI_GIAN_HUY_HOAN_COC;

  // Nếu hủy trước thời gian an toàn thì được hoàn tiền
  const isRefundable = diffMinutes > phutHuyAnToan; 

  let title = "";
  let text = "";
  let confirmText = "";

  if (isRefundable) {
      title = "Xác nhận Hủy (Hoàn cọc)";
      text = `Khách hủy trước lịch hẹn ${Math.floor(diffMinutes/60)}h${diffMinutes%60}p. Hợp lệ để HỦY PHIẾU VÀ HOÀN TRẢ 100% tiền cọc.`;
      confirmText = "Xác nhận & hoàn tiền";
  } else {
      title = "Xác nhận Hủy (Mất cọc)";
      text = `Khách báo hủy quá sát giờ (chỉ còn ${diffMinutes} phút, quy định cần > ${phutHuyAnToan} phút). Phiếu sẽ bị hủy và KHÔNG HOÀN TRẢ tiền cọc.`;
      confirmText = "Xác nhận hủy";
  }

  const confirm = await Swal.fire({
      title: title,
      text: text,
      icon: "warning",
      showCancelButton: true,
      confirmButtonColor: isRefundable ? "#28a745" : "#d33", 
      cancelButtonColor: "#6c757d",
      confirmButtonText: confirmText,
      cancelButtonText: "Đóng lại"
  });

  if (confirm.isConfirmed) {
      try {
          Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });
          
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
          
          await axiosClient.put('/hoa-don-thanh-toan/huy-phieu-cho', payload);
          
          Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Đã hủy phiếu!', timer: 1500, showConfirmButton: false });
          
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
            iconColor: '#7D161A',
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

const monDangCho = computed(() => {
  return listMonDaChon.value.filter(item => !item.served);
});

const monDaLen = computed(() => {
  return listMonDaChon.value.filter(item => item.served);
});

const checkExpiredTickets = async () => {
  const now = dayjs(currentTime.value);
  let needRefresh = false;
  // 🚨 Dùng biến THOI_GIAN_GIU_BAN
  const thoiGianChoPhepTre = sysParams.value.THOI_GIAN_GIU_BAN;

  for (let i = danhSachCho.value.length - 1; i >= 0; i--) {
    const khach = danhSachCho.value[i];
    
    if (![0, 1].includes(Number(khach.trangThai))) continue;

    const thoiGianDat = dayjs(khach.thoiGianDat);
    const phutConLai = thoiGianDat.diff(now, "minute");

    // 🚨 FE tự xóa khỏi UI nếu trễ quá thời gian giữ bàn
    if (phutConLai <= -thoiGianChoPhepTre) {
      danhSachCho.value.splice(i, 1);
      needRefresh = true;
      
      Swal.fire({
        toast: true,
        position: 'bottom-end',
        icon: 'info',
        title: `Hệ thống vừa tự hủy phiếu của ${khach.tenKhachHang} do quá hạn ${thoiGianChoPhepTre}p!`,
        showConfirmButton: false,
        timer: 4000
      });
    }
  }

  if (needRefresh) {
    await handleFetchAllCheckIn();
    await fetchAllBan();
    await fetchTableStatus();
  }
};

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
                
                <div class="floor-header bg-light d-flex justify-content-between align-items-center px-3 py-2 border-bottom">
                   <h6 class="mb-0 fw-bold" style="color: #7d161a;"><i class="fa-solid fa-map-location-dot me-2"></i> Sơ đồ bàn</h6>
                   <div class="form-check form-switch d-flex align-items-center mb-0 custom-switch-red">
                      <input class="form-check-input me-2 mt-0" type="checkbox" role="switch" id="quickWalkInSwitch" v-model="isQuickWalkInMode" style="cursor: pointer; width: 45px; height: 22px;">
                      
                      <label class="form-check-label fw-bold" 
                             :style="{ color: isQuickWalkInMode ? '#7d161a' : '#6c757d', cursor: 'pointer', fontSize: '14px', userSelect: 'none' }" 
                             for="quickWalkInSwitch">
                          <i class="fa-solid fa-bolt me-1" :style="{ color: isQuickWalkInMode ? '#7d161a' : '#6C757D' }"></i> Mở bàn nhanh
                      </label>
                   </div>
                </div>

                <div class="grid-container">
                  <div class="grid-canvas" @dragover.prevent :class="{ 'editing-mode': false }">
                    <div
                      v-for="ban in banTheoTang"
                      :key="ban.id"
                      class="table-card"
                      :class="{ 
                        'highlight-red': getTrangThaiTheoNgay(ban.id) === 0,
                        'is-hovered': hoveredTableId === ban.id,
                        'is-dimmed': !activeBookingTableIds.includes(ban.id) && hoveredTableId !== ban.id,
                        'quick-mode-ready': isQuickWalkInMode && getTrangThaiTheoNgay(ban.id) === 0
                      }"
                      :style="{
                        gridColumnStart: ban.column,
                        gridRowStart: ban.row,
                        gridColumnEnd: 'span 3',
                        gridRowEnd: 'span 2',
                        cursor: (getTrangThaiTheoNgay(ban.id) !== 0 || (isQuickWalkInMode && getTrangThaiTheoNgay(ban.id) === 0)) ? 'pointer' : 'default'
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
                    <div class="d-flex justify-content-between align-items-center mb-3">
                      <h5 style="font-weight: bold; font-size: 16px; margin: 0;">Khách chờ check-in</h5>
                      <button class="btn btn-sm btn-outline-danger fw-bold" style="border-radius: 6px;" @click="openAllTicketsModal">
                        <i class="fa-solid fa-list me-1"></i> Xem tất cả
                      </button>
                    </div>
                    <div class="filter-section">
                      <div class="filter-group">
                        <label class="filter-label"><i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm</label>
                        <div class="filter-input-wrapper">
                          <input type="text" v-model="searchQuery" placeholder="Tìm theo mã phiếu đặt bàn hoặc số điện thoại khách hàng" />
                        </div>
                      </div>

                      <div class="filter-group flex-grow-1">
                        <label class="filter-label"><i class="fa-solid fa-table"></i> Lọc theo bàn</label>
                        <div class="filter-input-wrapper">
                          <Multiselect
                            v-model="filterTableId"
                            :options="danhSachBan"
                            valueProp="id"
                            label="maBan"
                            placeholder="-- Tất cả bàn --"
                            :searchable="true"
                            :canClear="true"
                            no-results-text="Không có bàn nào"
                            class="custom-filter-multiselect red-sort-multiselect w-100"
                          />
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
                      <div class="filter-group flex-grow-1">
                        <label class="filter-label"><i class="fa-solid fa-calendar-days"></i> Ngày đến</label>
                        <div class="filter-input-wrapper">
                          <input type="date" v-model="filterDate" class="date-input w-100" />
                        </div>
                      </div>

                      <div class="filter-group flex-grow-1">
                        <label class="filter-label"><i class="fa-solid fa-arrow-down-short-wide"></i> Sắp xếp</label>
                        <div class="filter-input-wrapper">
                          <Multiselect
                            v-model="sortOption"
                            :options="sortOptionsList"
                            :searchable="false"
                            :canClear="false"
                            class="custom-filter-multiselect red-sort-multiselect w-100"
                          />
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
                <div v-if="selectedPhieu?.danhSachBan && selectedPhieu.danhSachBan.length > 1" class="merge-payment-box mb-3 p-3 rounded" style="background-color: #fff5f5; border: 1px dashed #7d161a;">
                    <div class="d-flex justify-content-between align-items-center mb-2">
                        <h6 class="fw-bold mb-0" style="color: #7d161a;"><i class="fa-solid fa-link me-1"></i> Hóa đơn của đoàn đông</h6>
                        <span class="badge rounded-pill" style="background-color: #7d161a;">Đoàn ngồi {{ selectedPhieu.danhSachBan.length }} bàn</span>
                    </div>
                    <p class="small text-muted mb-2">Hóa đơn này dùng chung cho các bàn sau:</p>
                    <div class="d-flex gap-2 flex-wrap mb-2">
                        <span v-for="ban in selectedPhieu.danhSachBan" :key="ban.id" class="badge bg-secondary px-3 py-2 fs-6">Bàn {{ ban.maBan }}</span>
                    </div>
                    <div class="alert alert-warning py-1 px-2 mb-0 mt-2" style="font-size: 12px;">
                        <i class="fa-solid fa-info-circle me-1"></i> Khi thanh toán, tất cả các bàn trên sẽ được dọn trống.
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
                    <span class="text-danger fw-bold"><i class="fa-solid fa-utensils me-1"></i> Thực đơn đã chọn:</span>
                    <span class="badge bg-danger">{{ listMonDaChon.length }} món</span>
                    </div>
                    <ul class="summary-list mb-3">
                    <li v-for="item in listMonDaChon" :key="item.id">{{ item.name }} <span class="text-muted">x{{ item.quantity }}<span class="text-muted" style="float: right;"><strong>{{ (item.price * item.quantity).toLocaleString() }} đ</strong></span></span></li>
                    </ul>
                    <div class="financial-breakdown border-top pt-2 mt-2">
                    <div class="d-flex justify-content-between text-muted small mb-1"><span>Tạm tính (Tiền món):</span><span>{{ (billSummary?.tong || 0).toLocaleString() }} đ</span></div>
                    <div v-if="(billSummary?.giam || 0) > 0" class="mb-2">
                        <div class="d-flex justify-content-between align-items-center text-success small mb-1">
                            <span>Khuyến mãi / Giảm giá:</span>
                            <div class="d-flex align-items-center">
                                <span class="fw-bold me-2">- {{ (billSummary?.giam || 0).toLocaleString() }} đ</span>
                                <button class="btn btn-sm btn-outline-danger py-0 px-1" style="font-size: 10px;" @click="removeVoucher" title="Hủy mã này">
                                    <i class="fa-solid fa-xmark"></i>
                                </button>
                            </div>
                        </div>
                        <div class="d-flex justify-content-end">
                           <div v-if="selectedPhieu?.maPhieuGiamGia" class="badge rounded-pill bg-danger-subtle text-custom-red border border-danger border-opacity-25 px-2 py-1 d-inline-flex align-items-center" style="font-size: 10px;">
                             <i class="fa-solid fa-ticket-simple me-1"></i>
                             <span>Mã: {{ selectedPhieu.maPhieuGiamGia }}</span>
                           </div>
                        </div>
                    </div>              
                    <div class="d-flex justify-content-between text-muted small mb-1">
                      <span>Thuế VAT (Tính theo món):</span>
                      <span>+ {{ (billSummary?.tienVat || 0).toLocaleString() }} đ</span>
                    </div>
                    <div v-if="(billSummary?.coc || 0) > 0" class="d-flex justify-content-between text-danger small mb-1 fw-bold"><span><i class="fa-solid fa-piggy-bank me-1"></i> Khách đã cọc:</span><span>- {{ (billSummary?.coc || 0).toLocaleString() }} đ</span></div>
                    <div class="d-flex justify-content-between mt-2 pt-2 border-top border-2">
                        <span class="fw-bold text-dark text-uppercase" style="font-size: 14px;">Tiền món bàn này:</span>
                        <span class="fw-bold text-danger" style="font-size: 16px;">{{ (billSummary?.canThu || 0).toLocaleString() }} đ</span>
                    </div>

                    <div v-if="(billSummary?.giam || 0) === 0" class="mt-3 d-flex flex-column gap-2">
                        <button class="btn w-100 fw-bold d-flex align-items-center justify-content-center" 
                                style="border-radius: 8px; font-size: 13px; background-color: #7d161a; color: white; border: none; box-shadow: 0 2px 4px #7d161a;" 
                                @click="applyBestPublicVoucher">
                            <i class="fa-solid fa-wand-magic-sparkles me-2"></i> Tự động đề cử ưu đãi tốt nhất
                        </button>
                        
                        <button class="btn btn-outline-danger w-100 fw-bold d-flex align-items-center justify-content-center" 
                                style="border-radius: 8px; font-size: 13px; background-color: #7d161a; color: white; border: none; box-shadow: 0 2px 4px #7d161a;" 
                                @click="openDiscountModal('PERSONAL')">
                            <i class="fa-solid fa-gift me-2"></i> Chọn mã giảm giá cá nhân
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
                    <div v-if="selectedPhieu?.isBanPhu" class="alert alert-warning py-2 px-3 text-center mb-0 mt-2 shadow-sm" style="font-size: 13px; border-radius: 8px; border: 1px dashed #ffc107;">
                        <i class="fa-solid fa-lock me-1"></i> Bàn phụ không thể thao tác cấu trúc.<br>
                        <button class="btn btn-sm mt-2 fw-bold w-100" style="background-color: #ffc107; color: #7d161a; border-radius: 6px;" @click="switchToMainTable">
                            <i class="fa-solid fa-arrow-right-to-bracket me-1"></i> Sang Bàn Chủ ({{ selectedPhieu.tenBanChinh }}) để xử lý
                        </button>
                    </div>
                    <div v-else class="d-flex gap-2 mt-2">
                        <button 
                            :disabled="!isServing || (selectedPhieu?.danhSachBan && selectedPhieu.danhSachBan.length > 1)" 
                            class="btn-action flex-grow-1" 
                            @click="openChangeTableView"
                            :title="(selectedPhieu?.danhSachBan && selectedPhieu.danhSachBan.length > 1) ? 'Đoàn đang ngồi nhiều bàn, không thể đổi!' : ''"
                        >
                            <i class="fa-solid fa-right-left me-1"></i> Đổi bàn
                        </button>
                        
                        <button :disabled="!isServing" class="btn-action flex-grow-1" @click="openMergeTableView">
                            <i class="fa-solid fa-link me-1"></i> Gộp bàn
                        </button>
                    </div>
                  </div>
                <div v-else class="text-center text-muted mb-3"><small><i>Bàn trống. Cần Check-in phiếu đặt để thực hiện các thao tác thêm món.</i></small></div>

                <hr class="my-3" />
                <h6 class="section-title mb-3">Thao tác tiếp đón</h6>
                
                <div class="d-flex gap-2">
                    <button v-if="getTrangThaiTheoNgay(selectedBan?.id) !== 1" class="btn btn-update-status flex-grow-1" @click="handleDirectCheckIn"><i class="fa-solid fa-bell-concierge me-2"></i> Xác nhận Check-in</button>
                    <div v-else class="alert alert-success p-2 mb-0 text-center flex-grow-1 d-flex align-items-center justify-content-center" style="background-color: #7d161a; border: 1px solid #dc3545; color: white; border-radius: 8px;"><i class="fa-solid fa-check-circle me-2"></i> Bàn đã Check-in</div>
                    <button v-if="selectedPhieu?.id" class="btn px-4" style="background-color: #fff; color: #dc3545; border: 1px solid #dc3545; font-weight: bold; border-radius: 8px;" @click="handleCancelTicket"><i class="fa-solid fa-ban me-1"></i> Hủy phiếu</button>
                </div>

                <div v-if="getTrangThaiTheoNgay(selectedBan?.id) === 1 && selectedPhieu?.id" class="payment-group mt-4 pt-3 border-top">
                    <h6 class="section-title mb-2">Quyết toán hóa đơn</h6>
                    <div v-if="selectedPhieu?.isBanPhu" class="alert alert-warning py-2 px-3 text-center mb-3 shadow-sm" style="font-size: 13px; border-radius: 8px; border-left: 5px solid #ffc107; background-color: #fffbe6;">
                        <i class="fa-solid fa-lock me-1"></i> Đây là <b>Bàn phụ</b>.<br>Hóa đơn được gom chung và tính tiền ở Bàn Chủ.
                        <button class="btn btn-sm mt-2 fw-bold w-100" style="background-color: #7d161a; color: white; border-radius: 6px;" @click="switchToMainTable">
                            <i class="fa-solid fa-file-invoice-dollar me-1"></i> Sang Bàn Chủ thanh toán
                        </button>
                    </div>
                    <div v-if="selectedPhieu?.idHoaDon && (selectedPhieu?.vatApDung || 0) === 0" class="alert alert-warning py-2 px-3 text-center mb-3 shadow-sm" style="font-size: 13px; border-radius: 8px; border-left: 5px solid #ffc107; background-color: #fffbe6;"><i class="fa-solid fa-lock me-1"></i> Đây là <b>Bàn phụ (0% VAT)</b>. Vui lòng chuyển sang bàn chính để thanh toán gộp.</div>
                    <div v-if="!hasItems" class="alert alert-danger py-2 px-3 text-center" style="font-size: 13px; border-radius: 8px; background-color: #fff1f0; border: 1px solid #ffccc7; color: #cf1322;"><i class="fa-solid fa-cart-arrow-down me-1"></i> Bàn chưa có món nào! Vui lòng <strong>Thêm món</strong> trước khi thanh toán.</div>
                    <div v-if="hasItems && !isAllItemsServed" class="alert alert-warning py-2 px-3 text-center" style="font-size: 13px; border-radius: 8px;"><i class="fa-solid fa-triangle-exclamation me-1"></i> Vui lòng vào <strong>"Xem đơn hàng"</strong> và xác nhận đã lên đủ món để mở khóa thanh toán.</div>

                    <div v-if="getTrangThaiTheoNgay(selectedBan?.id) === 1 && selectedPhieu?.id" class="merge-payment-box mb-3 p-3 rounded" style="background-color: #fff5f5; border: 1px dashed #7d161a;">
                        <div class="d-flex justify-content-between align-items-center mb-2"><h6 class="fw-bold mb-0" style="color: #7d161a;"><i class="fa-solid fa-link me-1"></i> Gộp bàn thanh toán</h6><span class="badge rounded-pill" style="background-color: #7d161a;">{{ listBanGop.length }} bàn đã chọn</span></div>
                        <div v-if="listBanCoKhachKhac.length > 0">
                            <p class="small text-muted mb-2">Các bàn sau có cùng khách và đủ điều kiện gộp:</p>
                            <div class="d-flex gap-2 flex-wrap">
                              <button v-for="ban in listBanCoKhachKhac" :key="ban.id" class="btn btn-sm d-flex flex-column align-items-center justify-content-center p-2 transition-all custom-merge-btn" style="min-width: 90px; border-radius: 8px;" :class="listBanGop.some(b => b.id === ban.id) ? 'is-selected shadow' : 'bg-white'" @click="toggleGopBan(ban)">
                                <span class="fw-bold fs-6">{{ ban.maBan }}</span>
                                <small style="font-size: 10px; max-width: 80px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;" class="mb-1 text-secondary" :class="listBanGop.some(b => b.id === ban.id) ? 'text-white' : ''">
                                    <i class="fa-regular fa-user"></i> {{ ban._hoaDonInfo.tenKhach }}
                                </small>
                                <small class="fw-bold" style="font-size: 12px;">+{{ ban._hoaDonInfo.canThu.toLocaleString() }}đ</small>
                            </button>
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
                      <button class="btn-pay cash-btn" @click="handlePaymentCash">
                        <i class="fa-solid" :class="grandTotalToPay <= 0 ? 'fa-check-circle' : 'fa-money-bill-1-wave'"></i>
                        <span>{{ grandTotalToPay <= 0 ? 'Hoàn tất (0đ)' : 'Tiền mặt' }}</span>
                      </button>
                      
                      <div class="d-flex gap-2">
                        <button class="btn-pay vnpay-btn" 
                                :class="{ 'opacity-50 disabled': grandTotalToPay <= 0 }"
                                :disabled="grandTotalToPay <= 0"
                                @click="handlePaymentVNPayFull">
                          <i class="fa-solid fa-qrcode"></i><span>VNPay</span>
                        </button>
                        <button class="btn-pay mixed-btn" 
                                :class="{ 'opacity-50 disabled': grandTotalToPay <= 0 }"
                                :disabled="grandTotalToPay <= 0"
                                @click="handlePaymentMixed">
                          <i class="fa-solid fa-layer-group"></i><span>Hỗn hợp</span>
                        </button>
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
            
            <div class="mb-4">
              <h6 class="fw-bold text-danger mb-2">
                <i class="fa-solid fa-fire-burner me-2"></i>Đang chờ phục vụ ({{ monDangCho.length }})
              </h6>
              
              <div v-if="monDangCho.length === 0" class="text-center text-muted small py-3 bg-light rounded border border-dashed">
                <i class="fa-solid fa-check-double me-1"></i> Bàn này đã lên đủ món!
              </div>

              <div v-for="(item, index) in monDangCho" :key="'wait-'+item.dbDetailId" class="order-item-card pending-item border-danger border-opacity-50">
                <div class="item-icon-box bg-danger text-white bg-opacity-75">
                  <i v-if="item.type === 'SET'" class="fa-solid fa-bowl-food"></i>
                  <i v-else class="fa-solid fa-burger"></i>
                </div>
                
                <div class="item-details flex-grow-1 ms-3">
                  <div class="d-flex justify-content-between align-items-start mb-1">
                    <span class="fw-bold item-name fs-6">{{ item.name }}</span>
                    <button class="btn btn-sm text-danger p-0" @click.stop="handleDeleteItem(item, index)" title="Hủy món này">
                      <i class="fa-solid fa-trash-can"></i>
                    </button>
                  </div>
                  
                  <div class="d-flex justify-content-between align-items-end mt-2">
                    <div class="text-muted small">
                      Số lượng: <strong class="text-danger fs-6">{{ item.quantity }}</strong>
                      <div class="mt-1" style="font-size: 11px;">Giá: {{ item.price.toLocaleString() }}đ</div>
                    </div>
                    
                    <button class="btn btn-sm fw-bold px-3 py-1 text-white shadow-sm" style="background-color: #7d161a; border-radius: 6px;" @click="toggleItemServed(item)">
                      <i class="fa-solid fa-hand-holding-hand me-1"></i> Đã lên món
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="monDaLen.length > 0">
              <h6 class="fw-bold text-success mb-2 border-top pt-3">
                <i class="fa-solid fa-check-circle me-2"></i>Đã phục vụ ({{ monDaLen.length }})
              </h6>

              <div v-for="(item, index) in monDaLen" :key="'done-'+item.dbDetailId" class="order-item-card served-item bg-light border-0 mb-2 p-2">
                <div class="d-flex align-items-center w-100 opacity-75">
                  <i class="fa-solid fa-check text-success me-3 fs-5"></i>
                  <div class="flex-grow-1">
                    <div class="fw-bold text-dark" style="text-decoration: line-through;">{{ item.name }}</div>
                    <small class="text-muted">SL: {{ item.quantity }} | {{ (item.price * item.quantity).toLocaleString() }}đ</small>
                  </div>
                  
                  <button class="btn btn-link text-muted p-0 ms-2" style="font-size: 12px;" @click="toggleItemServed(item)" title="Chưa lên món này">
                    <i class="fa-solid fa-rotate-left"></i> Hoàn tác
                  </button>
                </div>
              </div>
            </div>

          </div>

          <div class="order-summary-card flex-shrink-0" style="background-color: #fff9f9; border: 1px solid #ffccd5;">
            <h6 class="fw-bold mb-3" style="color: #7d161a;"><i class="fa-solid fa-file-invoice-dollar me-2"></i>Chi tiết thanh toán</h6>
            
            <div class="d-flex justify-content-between mb-2 text-muted small">
                <span>Tạm tính (Tiền món):</span>
                <span>{{ (billSummary?.tong || 0).toLocaleString() }} đ</span>
            </div>
            
            <div v-if="(billSummary?.giam || 0) > 0">
              <div class="d-flex justify-content-between align-items-center mb-1 text-success small">
                  <span>Khuyến mãi / Giảm giá:</span>
                  <div class="d-flex align-items-center">
                      <span class="fw-bold me-2">- {{ (billSummary?.giam || 0).toLocaleString() }} đ</span>
                      <button class="btn btn-sm btn-outline-danger py-0 px-1" style="font-size: 10px;" @click="removeVoucher" title="Hủy mã này">
                          <i class="fa-solid fa-xmark"></i>
                      </button>
                  </div>
              </div>
              <div class="d-flex justify-content-end mb-2">
                 <div v-if="selectedPhieu?.maPhieuGiamGia" class="badge rounded-pill bg-danger-subtle text-custom-red border border-danger border-opacity-25 px-2 py-1 d-inline-flex align-items-center" style="font-size: 11px;">
                   <i class="fa-solid fa-ticket-simple me-1"></i>
                   <span>Mã: {{ selectedPhieu.maPhieuGiamGia }}</span>
                 </div>
              </div>
            </div>

            <div class="d-flex justify-content-between mb-2 text-muted small">
                <span>Thuế VAT (Tính theo món):</span>
                <span class="text-dark fw-bold">+ {{ (billSummary?.tienVat || 0).toLocaleString() }} đ</span>
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

        <div v-else-if="modalView === 'mergeTable'" class="change-table-container h-100 d-flex flex-column p-3" style="background-color: #f8f9fa;">
          <div class="d-flex justify-content-between align-items-center mb-3 flex-shrink-0">
            <div class="alert alert-warning mb-0 border-0 shadow-sm flex-grow-1 me-4 d-flex align-items-center">
                <i class="fa-solid fa-link me-2 text-danger fs-5"></i>
                <span>Bạn có thể tick chọn <strong>Nhiều Bàn (Có Khách)</strong> để gộp chung vào bàn <strong class="text-danger fs-5 mx-1">{{ selectedBan?.maBan }}</strong>.</span>
            </div>
            <div class="d-flex bg-white p-1 rounded shadow-sm border">
                <button v-for="tang in danhSachTang" :key="'merge-tang-' + tang" class="btn px-4 py-2 fw-bold" :class="modalActiveFloor === tang ? 'btn-active' : 'text-muted'" @click="modalActiveFloor = tang" style="border-radius: 6px; border: none;">Tầng {{ tang }}</button>
            </div>
          </div>

          <div class="floor-plan-layout flex-grow-1 overflow-hidden d-flex gap-3">
            <div class="floor-plan-section flex-grow-1 bg-white shadow-sm border rounded">
                <div class="grid-container h-100 overflow-auto">
                  <div class="grid-canvas">
                      <div v-for="ban in modalBanTheoTang" :key="ban.id" class="table-card" 
                          :class="{
                            'is-hovered': modalHoveredTableId === ban.id, 
                            'is-dimmed': getTrangThaiTheoNgay(ban.id) !== 1 || isTableInCurrentGroup(ban.id), 
                            'current-table': isTableInCurrentGroup(ban.id),
                            'is-selected-merge': danhSachBanChonGop.some(b => b.id === ban.id) /* 🚨 HIỆU ỨNG KHI TICK CHỌN */
                          }" 
                          :style="{gridColumnStart: ban.column, gridRowStart: ban.row, gridColumnEnd: 'span 3', gridRowEnd: 'span 2', cursor: (getTrangThaiTheoNgay(ban.id) === 1 && !isTableInCurrentGroup(ban.id)) ? 'pointer' : 'not-allowed'}" 
                          @click="(getTrangThaiTheoNgay(ban.id) === 1 && !isTableInCurrentGroup(ban.id)) ? toggleSelectBanGop(ban) : null">
                        
                        <div v-if="danhSachBanChonGop.some(b => b.id === ban.id)" style="position: absolute; top: -10px; right: -10px; background: #28a745; color: white; border-radius: 50%; width: 30px; height: 30px; display: flex; justify-content: center; align-items: center; font-size: 16px; box-shadow: 0 2px 5px rgba(0,0,0,0.3); z-index: 20;">
                            <i class="fa-solid fa-check"></i>
                        </div>

                        <div class="table-content text-center pt-2">
                            <strong class="fs-5">{{ ban.maBan }}</strong>
                            <div class="text-muted small mb-2"><i class="fa-solid fa-users text-secondary"></i> {{ ban.soCho }} chỗ</div>
                            
                            <div v-if="ban.id === selectedBan?.id" class="status-tag bg-primary text-white w-100 rounded-pill"><i class="fa-solid fa-location-dot"></i> Đang chọn</div>
                            <div v-else-if="isTableInCurrentGroup(ban.id)" class="status-tag bg-info text-white w-100 rounded-pill"><i class="fa-solid fa-layer-group"></i> Cùng đoàn</div>
                            <div v-else-if="danhSachBanChonGop.some(b => b.id === ban.id)" class="status-tag bg-success text-white w-100 rounded-pill"><i class="fa-solid fa-check-double"></i> Đã Tick Chọn</div>
                            <div v-else-if="getTrangThaiTheoNgay(ban.id) === 1" class="status-tag status-occupied-light w-100 rounded-pill"><i class="fa-solid fa-plus-circle"></i> Có Khách (Ghép)</div>
                            <div v-else class="status-tag bg-secondary text-white w-100 rounded-pill"><i class="fa-solid fa-ban"></i> Bàn Trống</div>
                        </div>
                      </div>
                  </div>
                </div>
            </div>
          </div>

          <div class="mt-3 pt-3 border-top bg-white px-3 py-2 rounded shadow-sm d-flex justify-content-between flex-shrink-0">
              <button class="btn btn-outline-secondary px-4 fw-bold" @click="modalView = 'info'"><i class="fa-solid fa-arrow-left me-1"></i> Quay lại</button>
              
              <button class="btn btn-update-status px-4 fw-bold" style="width: auto; background-color: #28a745;" 
                      :disabled="danhSachBanChonGop.length === 0" 
                      @click="handleConfirmMergeBulk">
                  Xác nhận gộp {{ danhSachBanChonGop.length }} bàn <i class="fa-solid fa-link ms-2"></i>
              </button>
          </div>
        </div>

        <div v-else-if="modalView === 'orderHistory'" class="history-container">
          <div class="history-header mb-4">
              <h5 class="fw-bold"><i class="fa-solid fa-history me-2"></i>Lịch sử đơn hàng - Bàn {{ selectedBan?.maBan }}</h5>
              <p class="text-muted small">Mã hóa đơn: #{{ selectedPhieu?.idHoaDon }}</p>
          </div>
          <div class="mt-4 pt-3 mb-4 border-top">
              <button class="btn btn-outline-secondary w-100 fw-bold" @click="modalView = 'info'" style="border-radius: 8px;">
                  <i class="fa-solid fa-arrow-left me-1"></i> Quay lại thông tin bàn
              </button>
          </div>
          
          <div class="timeline-wrapper pe-2">
              <div v-if="orderHistory.length === 0" class="text-center py-5">
                  <i class="fa-solid fa-inbox fa-3x text-muted mb-3"></i>
                  <p>Chưa có dữ liệu lịch sử cho đơn hàng này</p>
              </div>
              
              <div v-else class="timeline">
                  <div v-for="(log, index) in orderHistory" :key="index" class="timeline-item">
                      <div class="timeline-badge" :class="{'bg-success': log.trangThaiMoi === 4, 'bg-primary': log.trangThaiMoi === 6}">
                          <i class="fa-solid" :class="log.hanhDong.includes('món') ? 'fa-utensils' : 'fa-check'"></i>
                      </div>
                      <div class="timeline-card shadow-sm">
                          <div class="d-flex justify-content-between align-items-start">
                              <h6 class="fw-bold mb-1 text-dark">{{ log.hanhDong }}</h6>
                              <span class="badge bg-light text-dark border">{{ log.hanhDong.split(':')[0] }}</span>
                          </div>
                          <div class="log-meta mb-2">
                              <small class="text-muted me-3"><i class="fa-regular fa-clock me-1"></i>{{ formatHistoryTime(log.thoiGian || log.thoi_gian_thuc_hien) }}</small>
                              <small class="text-muted"><i class="fa-regular fa-user me-1"></i>{{ log.nguoiThucHien || log.ten_nhan_vien || 'Hệ thống' }}</small>
                          </div>
                          <div class="log-details p-2 bg-light rounded">
                              <p class="mb-0 small text-secondary"><i class="fa-solid fa-info-circle me-1"></i> {{ log.lyDo || 'Không có ghi chú thêm' }}</p>
                          </div>
                      </div>
                  </div>
              </div>
          </div>
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

  <div v-if="isShowAllTicketsModal" class="modal-overlay" style="z-index: 10000;">
      <div class="modal-box" style="max-width: 850px; height: 85vh;">
        <div class="modal-header-custom bg-light">
          <h5 class="modal-title-custom m-0 text-danger"><i class="fa-solid fa-clipboard-list me-2"></i>Quản lý danh sách</h5>
          <button class="close-btn" @click="isShowAllTicketsModal = false">✕</button>
        </div>

        <div class="modal-body-custom p-0 bg-light d-flex flex-column h-100">
          <div class="d-flex border-bottom bg-white px-3 pt-2 flex-shrink-0">
              <button class="btn btn-link text-decoration-none fw-bold px-4 py-2 border-bottom border-3"
                      :class="activeTicketTab === 'WAITING' ? 'border-danger text-danger' : 'border-transparent text-muted'"
                      @click="activeTicketTab = 'WAITING'">
                  <i class="fa-solid fa-hourglass-half me-1"></i> Phiếu đang chờ ({{ danhSachLoc.length }})
              </button>
              <button class="btn btn-link text-decoration-none fw-bold px-4 py-2 border-bottom border-3"
                      :class="activeTicketTab === 'SERVING' ? 'border-danger text-danger' : 'border-transparent text-muted'"
                      @click="activeTicketTab = 'SERVING'">
                  <i class="fa-solid fa-utensils me-1"></i> Bàn đang ăn ({{ danhSachBanDangAn.length }})
              </button>
          </div>

          <div class="d-flex flex-grow-1 overflow-hidden">
            <div class="p-3 border-end bg-white overflow-auto" style="width: 300px; flex-shrink: 0;">
                <h6 class="fw-bold text-danger mb-3"><i class="fa-solid fa-filter me-1"></i> Bộ lọc</h6>

                <div class="filter-group mb-3">
                  <label class="filter-label"><i class="fa-solid fa-table"></i> Lọc theo bàn</label>
                  <div class="filter-input-wrapper">
                    <Multiselect
                      v-model="filterTableId"
                      :options="danhSachBan"
                      valueProp="id"
                      label="maBan"
                      placeholder="-- Tất cả bàn --"
                      :searchable="true"
                      :canClear="true"
                      no-results-text="Không có bàn nào"
                      class="custom-filter-multiselect red-sort-multiselect w-100"
                    />
                  </div>
                </div>
                
                <div class="filter-group mb-3">
                  <label class="filter-label"><i class="fa-solid fa-magnifying-glass"></i> Tìm kiếm</label>
                  <div class="filter-input-wrapper">
                    <input type="text" v-model="searchQuery" placeholder="Mã phiếu, SĐT, Mã bàn..." />
                  </div>
                </div>

                <div class="filter-group mb-3">
                  <label class="filter-label"><i class="fa-solid fa-arrow-down-short-wide"></i> Sắp xếp</label>
                  <div class="filter-input-wrapper">
                    <Multiselect v-model="sortOption" :options="sortOptionsList" :searchable="false" :canClear="false" class="custom-filter-multiselect red-sort-multiselect w-100"/>
                  </div>
                </div>

                <div v-show="activeTicketTab === 'WAITING'">
                  <div class="filter-time-group mb-3">
                    <label class="filter-label-v2"><i class="fa-solid fa-clock-rotate-left me-1"></i> Khung giờ đến:</label>
                    <div class="btn-group-custom flex-wrap gap-1">
                      <button v-for="opt in timeWindowOptions" :key="'modal-'+opt.value" class="time-btn py-1 px-2" :class="{ 'active': selectedTimeWindow === opt.value }" @click="setTimeWindow(opt.value)">
                        {{ opt.label }}
                      </button>
                    </div>
                  </div>

                  <div class="filter-group mb-3">
                    <label class="filter-label"><i class="fa-solid fa-calendar-days"></i> Ngày đến</label>
                    <div class="filter-input-wrapper">
                      <input type="date" v-model="filterDate" class="date-input w-100" />
                    </div>
                  </div>
                </div>

                <button class="btn btn-outline-secondary w-100 mt-2" style="border-radius: 6px;" @click="searchQuery=''; filterDate=null; sortOption='time_asc'">Xóa bộ lọc</button>
            </div>

            <div class="p-3 flex-grow-1 overflow-auto bg-light">
              
              <div v-show="activeTicketTab === 'WAITING'">
                <div v-if="danhSachLoc.length === 0" class="text-center py-5 text-muted">
                    <i class="fa-solid fa-calendar-xmark fa-3x mb-3 opacity-50"></i>
                    <p>Không có phiếu đặt bàn nào đang chờ.</p>
                </div>
                
                <div class="row g-3">
                  <div v-for="khach in danhSachLoc" :key="'modal-khach-' + khach.id" class="col-md-6">
                    <div class="customer-card h-100 m-0 shadow-sm" style="border-left: 4px solid #f1c40f;">
                      <div class="card-header pb-2 border-bottom mb-2">
                        <span class="customer-name text-truncate" style="max-width: 150px;" :title="khach.tenKhachHang">{{ khach.tenKhachHang }}</span>
                        <span class="badge bg-warning text-dark">{{ khach.maBan }}</span>
                      </div>
                      <div class="card-body p-0">
                        <p class="time-info mb-2 small text-muted"><i class="fa-regular fa-calendar me-2"></i>{{ formatDate(khach.thoiGianDat) }}</p>
                        <div class="d-flex justify-content-between align-items-center mb-3">
                          <span class="fw-bold small" :class="getCountdown(khach.thoiGianDat).startsWith('-') ? 'text-danger' : 'text-primary'">
                            {{ getCountdown(khach.thoiGianDat).startsWith('-') ? 'Đã trễ:' : 'Còn:' }} {{ getCountdown(khach.thoiGianDat).replace('-', '') }}
                          </span>
                          <span class="small fw-bold text-muted"><i class="fa-solid fa-users"></i> {{ khach.soNguoi || khach.soLuongKhach || 1 }}</span>
                        </div>
                        <div class="d-flex gap-2">
                          <button class="btn btn-checkable flex-grow-1 py-1" :disabled="checkTimeStatus(khach.thoiGianDat).isEarly" :style="checkTimeStatus(khach.thoiGianDat).isEarly ? 'background-color: #6c757d !important; opacity: 0.7; color: white !important;' : ''" @click="modalHandleCheckInGuest(khach)">
                            <i class="fa-solid" :class="checkTimeStatus(khach.thoiGianDat).isEarly ? 'fa-clock' : 'fa-check'"></i> {{ checkTimeStatus(khach.thoiGianDat).isEarly ? 'Chưa tới' : 'Check-in' }}
                          </button>
                          <button class="btn btn-outline-danger px-2 py-1 rounded" @click="handleCancelWaitingTicket(khach)" title="Hủy phiếu">
                            <i class="fa-solid fa-trash-can"></i>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <div v-show="activeTicketTab === 'SERVING'">
                <div v-if="danhSachBanDangAn.length === 0" class="text-center py-5 text-muted">
                    <i class="fa-solid fa-utensils fa-3x mb-3 opacity-50"></i>
                    <p>Chưa có bàn nào đang được phục vụ.</p>
                </div>

                <div class="row g-3">
                  <div v-for="ban in danhSachBanDangAn" :key="'modal-ban-' + ban.id" class="col-md-4">
                    <div class="mini-table-card border-0 shadow-sm text-center p-3 h-100 d-flex flex-column justify-content-between" style="border-top: 4px solid #28a745 !important;" @click="modalHandleTableClick(ban)">
                      <div>
                        <div class="fw-bold fs-4 text-success mb-1">{{ ban.maBan }}</div>
                        <div class="badge bg-light text-dark border mb-2">Tầng {{ ban.soTang || ban.tang }}</div>
                        <div class="text-muted small"><i class="fa-solid fa-users"></i> Sức chứa: {{ ban.soCho }}</div>
                      </div>
                      <div class="mt-3 pt-2 border-top">
                         <span class="badge bg-success w-100 rounded-pill py-2"><i class="fa-solid fa-fire-burner me-1"></i> Đang ăn</span>
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


.table thead tr th {
  background-color: #e9e9e9 !important;
  color: #767676;
  font-weight: 600;
}

/* Button tầng */
/* Trạng thái cho nút ĐANG được chọn (Active) */
.btn {
  color: #333;
}

.btn:hover {
  background-color: #5c0a16 !important;
  /* Đỏ đậm hơn một chút khi di chuột */
  color: white !important;
}

.btn-active {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white !important;
  border: 1px solid #7d161a;
  cursor: default;
  /* Đã chọn rồi thì không hiện con trỏ tay */
}

.btn-active:hover {
  background: linear-gradient(135deg, #B71C1C 0%, #E65100 100%);
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
.filter-input-wrapper input,
.filter-input-wrapper select {
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
  height: calc(100vh - 250px); /* Ép cao bằng đúng sơ đồ bàn bên trái */
  display: flex;
  flex-direction: column; /* Để phần danh sách tự động đẩy xuống */
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

/* 🚨 THAY THẾ CLASS .list-waiting CŨ THÀNH THẾ NÀY */
.list-waiting {
  flex-grow: 1; /* Tự động ăn hết không gian thừa còn lại của container */
  max-height: none; /* Xóa dòng max-height: 15rem cũ đi */
  overflow-y: auto;
  padding-right: 8px;
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
  max-width: 920px;
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
  /* ĐÃ XÓA pointer-events: none; ĐỂ CÓ THỂ CLICK KHI BẬT TOGGLE */
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

.red-sort-multiselect {
  --ms-border-color: #7d161a;
  --ms-border-color-active: #7d161a;
  --ms-ring-color: rgba(125, 22, 26, 0.2); /* Vòng sáng màu đỏ khi click */
  --ms-bg: #fff5f5; 

  /* XÓA MÀU XANH: Đổi màu các tùy chọn (Options) bên trong menu */
  --ms-option-bg-pointed: #fce8e8;
  --ms-option-color-pointed: #7d161a;
  --ms-option-bg-selected: #7d161a;
  --ms-option-color-selected: #ffffff;
  --ms-option-bg-selected-pointed: #5c0a16;
}

/* Đổi màu chữ đang hiển thị */
.red-sort-multiselect :deep(.multiselect-single-labelText) {
  color: #7d161a !important;
  font-weight: 600;
}

/* Đổi màu icon mũi tên */
.red-sort-multiselect :deep(.multiselect-caret) {
  background-color: #7d161a !important;
}

/* 🚨 QUAN TRỌNG: Ép buộc đổi màu nền của Item đang được chọn phòng trường hợp bị thư viện đè */
.red-sort-multiselect :deep(.multiselect-option.is-selected) {
  background-color: #7d161a !important;
  color: white !important;
}

.red-sort-multiselect :deep(.multiselect-option.is-selected.is-pointed) {
  background-color: #5c0a16 !important;
}

.table-card.quick-mode-ready {
  border: 2px dashed #28a745 !important;
  background-color: #f8fff9 !important;
  box-shadow: 0 0 12px rgba(40, 167, 69, 0.4) !important;
  opacity: 1 !important; /* Không bị làm mờ */
  filter: none !important;
  animation: pulse-green 1.5s infinite;
}

.table-card.quick-mode-ready:hover {
  transform: scale(1.05);
  background-color: #d4edda !important;
  cursor: pointer;
}

@keyframes pulse-green {
  0% { box-shadow: 0 0 0 0 rgba(40, 167, 69, 0.5); }
  70% { box-shadow: 0 0 0 10px rgba(40, 167, 69, 0); }
  100% { box-shadow: 0 0 0 0 rgba(40, 167, 69, 0); }
}

.custom-switch-red .form-check-input:checked {
  background-color: #7d161a;
  border-color: #7d161a;
}
.custom-switch-red .form-check-input:focus {
  box-shadow: 0 0 0 0.25rem rgba(125, 22, 26, 0.25);
  border-color: #7d161a;
}

.custom-switch-red .form-check-input:focus {
  box-shadow: none !important;
  border-color: rgba(0,0,0,.25) !important;
}

/* Khi ĐÃ BẬT: Đổi sang nền đỏ */
.custom-switch-red .form-check-input:checked {
  background-color: #7d161a !important;
  border-color: #7d161a !important;
}

/* Khi ĐÃ BẬT mà click vào: Tỏa bóng mờ màu đỏ (Thay vì xanh) */
.custom-switch-red .form-check-input:checked:focus {
  box-shadow: 0 0 0 0.25rem rgba(125, 22, 26, 0.25) !important;
  border-color: #7d161a !important;
}


/* =======================================================
   🚨 ĐỔI HIỆU ỨNG NHẤP NHÁY BÀN TRỐNG TỪ XANH SANG ĐỎ
   ======================================================= */
.table-card.quick-mode-ready {
  border: 2px dashed #7d161a !important;
  background-color: #fff5f5 !important; /* Đỏ cực nhạt để dễ nhìn chữ */
  box-shadow: 0 0 12px rgba(125, 22, 26, 0.4) !important;
  opacity: 1 !important; 
  filter: none !important;
  animation: pulse-red 1.5s infinite;
}

.table-card.quick-mode-ready:hover {
  transform: scale(1.05);
  background-color: #fce8e8 !important;
  cursor: pointer;
}

@keyframes pulse-red {
  0% { box-shadow: 0 0 0 0 rgba(125, 22, 26, 0.5); }
  70% { box-shadow: 0 0 0 10px rgba(125, 22, 26, 0); }
  100% { box-shadow: 0 0 0 0 rgba(125, 22, 26, 0); }
}

.table-card.is-selected-merge {
  border: 3px solid #28a745 !important; /* Viền xanh lá đậm */
  background-color: #f0fdf4 !important; /* Nền xanh mint nhạt */
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(40, 167, 69, 0.4) !important;
  opacity: 1 !important;
  filter: grayscale(0%) !important;
  z-index: 10;
  position: relative; /* Để hiển thị icon check tròn trên góc */
}
</style>
