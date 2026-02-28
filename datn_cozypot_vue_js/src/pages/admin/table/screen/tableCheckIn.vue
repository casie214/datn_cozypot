<script setup>
import {
  fetchAllBanAn,
  fetchAllCheckIn,
  fetchTableStatusByDate,
  updateTrangThaiBan,
  createOrder,
  // fetchActiveBillByBan, <-- Có thể xóa nếu không dùng tới nữa
} from "../../../../services/tableManageService";
import { computed, onMounted, ref, onUnmounted, watch } from "vue";
import dayjs from "dayjs";
import router from "@/App";
import { useRoute, useRouter } from "vue-router";
import FoodList from "../modal/innerComponents/foodList.vue";
import Swal from "sweetalert2";
import { BeGetChiTietHoaDon } from "../../order/screens/orderService";
import axiosClient from "@/services/axiosClient";

const activeFloor = ref(1);
const danhSachBan = ref([]);
const selectedItems = ref({});

const checkTimeStatus = (dbTime) => {
  if (!dbTime) return { isEarly: false, minutes: 0 };
  
  const now = dayjs(currentTime.value);
  const target = dayjs(dbTime);
  const minutesLeft = target.diff(now, "minute");

  if (minutesLeft > 10) {
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
  return danhSachBan.value.filter(
    (ban) => Number(ban.soTang) === activeFloor.value,
  );
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

const getTrangThaiTheoNgay = (banId) => {
  return Number(tableStatusMap.value[banId] ?? 0);
};

const selectedDate = ref(
  new Date().toISOString().slice(0, 10), 
);

const fetchTableStatus = async () => {
  try {
    // Lấy song song cả 2 dữ liệu để đối chiếu
    const [dataStatus, dataBan] = await Promise.all([
      fetchTableStatusByDate(selectedDate.value),
      fetchAllBanAn()
    ]);

    const newMap = {};

    // 1. ƯU TIÊN SỐ 1: Lấy trạng thái gốc của Bàn Ăn từ SQL (Vì bạn bảo SQL đã update đúng)
    dataBan.forEach((ban) => {
      newMap[ban.id] = ban.trangThai; 
    });

    // 2. ƯU TIÊN SỐ 2: Ghi đè trạng thái đặt bàn theo ngày
    dataStatus.forEach((item) => {
      // Logic cốt lõi: Nếu bàn đang có khách (1), TUYỆT ĐỐI KHÔNG để API ngày ghi đè thành 0.
      // Chỉ cập nhật trạng thái "Đã đặt" (2) nếu bàn đó đang thực sự trống (0).
      if (newMap[item.banId] === 0 || newMap[item.banId] === undefined) {
        newMap[item.banId] = item.trangThai;
      }
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

const handleFetchAllCheckIn = async () => {
  try {
    danhSachCho.value = await fetchAllCheckIn();
  } catch (error) {
    console.error("Lỗi fetch khách chờ:", error);
  }
};

const handleTableClick = (ban) => {
  const trangThaiBan = getTrangThaiTheoNgay(ban.id);
  
  // Nếu bàn đang có khách (trạng thái 1), cho phép mở modal để tương tác
  if (trangThaiBan === 1) {
    openManageModal(ban);
  } 
  // Nếu bàn trống (0), không làm gì cả (giữ đúng yêu cầu khóa click bàn trống của bạn)
  else {
    console.log("Bàn trống, vui lòng check-in từ danh sách phiếu đặt.");
  }
};

const handleCheckInGuest = async (khach) => {
  const ban = danhSachBan.value.find(b => b.id === khach.idBanAn || b.maBan === khach.maBan);
  
  if (!ban) {
    return Swal.fire('Lỗi', 'Không tìm thấy thông tin bàn của khách này!', 'error');
  }

  // Cảnh báo nếu đến sớm
  const timeInfo = checkTimeStatus(khach.thoiGianDat);
  if (timeInfo.isEarly) {
    const confirm = await Swal.fire({
      title: 'Khách đến sớm!',
      text: `Khách đang đến sớm hơn lịch hẹn ${timeInfo.minutes} phút. Mở thông tin bàn?`,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#7d161a',
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Hủy'
    });
    if (!confirm.isConfirmed) return;
  }

  // CHỈ MỞ MODAL - KHÔNG GỌI API CẬP NHẬT Ở ĐÂY NỮA
  selectedPhieu.value = khach; 
  await openManageModal(ban); 
};

const danhSachLoc = computed(() => {
  return danhSachCho.value.filter((khach) => {
    if (!khach.maBan && !khach.idBanAn) return false;

    const thoiGianDat = dayjs(khach.thoiGianDat);
    const hienTai = dayjs(currentTime.value);
    const phutConLai = thoiGianDat.diff(hienTai, "minute");

    // --- LOGIC MỚI ---
    // 1. Nếu phiếu đã Check-in (trạng thái 3) hoặc Chờ thanh toán (5): LUÔN HIỆN
    if ([3, 5].includes(khach.trangThai)) return true;

    // 2. Nếu phiếu đang chờ (trạng thái 2): Lọc theo thời gian (10p trước và 60p sau)
    const matchTime = phutConLai <= 10 && phutConLai >= -60;
    
    // 3. Kết hợp tìm kiếm và ngày tháng
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

onMounted(() => {
  fetchAllBan();
  handleFetchAllCheckIn();
  timer = setInterval(() => {
    currentTime.value = new Date();
  }, 1000);

  const user = JSON.parse(localStorage.getItem("user"));
    if (user) currentStaffName.value = user.hoTen || user.username;
});

onUnmounted(() => clearInterval(timer));

const getCountdown = (dbTime) => {
  if (!dbTime) return "00:00:00";

  // Xử lý an toàn định dạng ngày (tránh lỗi mảng hoặc chuỗi)
  let target = null;
  if (Array.isArray(dbTime)) {
    const [y, m, d, h, min, s] = dbTime;
    target = dayjs(new Date(y, m - 1, d, h || 0, min || 0, s || 0));
  } else {
    target = dayjs(dbTime);
  }

  const now = dayjs(currentTime.value);
  const diff = target.diff(now); // Đơn vị là mili-giây
  
  // Xác định xem đã quá giờ chưa
  const isNegative = diff < 0;
  const absDiff = Math.abs(diff);

  // Tính Toán Giờ, Phút, Giây
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

const openManageModal = async (ban, forceStatus = null) => {
  const bId = ban.id || ban.idBanAn;
  
  selectedBan.value = { 
    ...ban,
    trangThai: forceStatus !== null ? forceStatus : getTrangThaiTheoNgay(bId) 
  };
  
  modalView.value = 'info';
  activeHoaDonId.value = null;
  listMonDaChon.value = []; 
  selectedPhieu.value = null;

  try {
    const resPhieu = await axiosClient.get(`/hoa-don-thanh-toan/active-by-ban/${bId}`);
    
    if (resPhieu.data) {
      const data = resPhieu.data;
      
      selectedPhieu.value = {
        // Sử dụng data.id (là ID phiếu) để mở khóa các nút chức năng
        id: data.id, 
        idHoaDon: data.idHoaDon || data.id, 
        maDatBan: data.maPhieu || 'N/A',
        tenKhachHang: data.tenKhachHang || 'Khách tại quán',
        idKhachHang: data.idKhachHang, 
        thoiGianDat: data.thoiGianDat 
      };

      // SỬA TẠI ĐÂY: Thêm data.chiTiet vào chuỗi kiểm tra
      const listFromApi = data.chiTiet || data.chiTietHoaDon || data.chiTietMonAn || [];
      
      if (listFromApi.length > 0) {
        listMonDaChon.value = listFromApi.map((mon) => {
            // Kiểm tra type từ Backend hoặc dựa vào ID có sẵn
            const isSet = mon.type === 'SET' || mon.idSetLau != null;
            
            // Lấy originalId (ID món ăn hoặc ID set lẩu)
            const originalId = isSet ? mon.idSetLau : mon.idChiTietMonAn;

            return {
                dbDetailId: mon.id, 
                originalId: originalId,
                type: isSet ? 'SET' : 'FOOD', 
                // Quan trọng: Phải tạo uniqueId để FoodList có thể "tick xanh" món đã chọn
                uniqueId: isSet ? `set_${originalId}` : `food_${originalId}`, 
                name: mon.tenMon,
                quantity: mon.soLuong,
                price: mon.donGia || 0,
                note: mon.ghiChu || ''
            };
        });
      }
    }
  } catch (e) {
    console.log("Bàn này trống hoặc chưa có phiếu:", e.message);
  }
  
  isShowModal.value = true;
};

const closeModal = () => {
  isShowModal.value = false;
  selectedBan.value = null;
  selectedPhieu.value = null;
  listMonDaChon.value = [];
};

const hoveredTableId = ref(null);

const onHoverCustomerCard = (khach) => {
  // Tìm thông tin bàn từ danh sách bàn đã load
  const ban = danhSachBan.value.find(b => b.id === khach.idBanAn || b.maBan === khach.maBan);
  
  if (ban) {
    hoveredTableId.value = ban.id;
    
    // TỰ ĐỘNG CHUYỂN TẦNG:
    // Nếu bàn ở tầng khác với tầng đang xem thì chuyển sang tầng đó
    if (Number(ban.soTang) !== activeFloor.value) {
      activeFloor.value = Number(ban.soTang);
    }
  }
};

const onLeaveCustomerCard = () => {
  hoveredTableId.value = null;
};

const tinhPhut = (thoiGianDat) => {
  const hienTai = dayjs();
  const gioHen = dayjs(thoiGianDat);
  return gioHen.diff(hienTai, 'minute');
};

const updateStatus = async () => {
  if (!selectedBan.value) return;

  let trangThaiPhieuUpdate = null;
  if (selectedPhieu.value?.id) {
    if (selectedBan.value.trangThai === 1) trangThaiPhieuUpdate = 3; // 3 là Checked-in
    if (selectedBan.value.trangThai === 0) trangThaiPhieuUpdate = 4; 
  }

  const bId = selectedBan.value.id;
  const payload = {
    idBanAn: bId, 
    trangThai: Number(selectedBan.value.trangThai),
    id: selectedPhieu.value?.id || null, 
    trangThaiPhieu: trangThaiPhieuUpdate 
  };

  try {
    // 1. Gọi API lưu
    await updateTrangThaiBan(payload);
    
    // 2. ÉP ĐỔI MÀU BÀN NGAY LẬP TỨC
    tableStatusMap.value = {
      ...tableStatusMap.value,
      [bId]: Number(selectedBan.value.trangThai)
    };

    Swal.fire({ icon: 'success', title: 'Thành công!', text: 'Đã check-in bàn!', timer: 1500, showConfirmButton: false });
    
    // KHÔNG gọi closeModal() ở đây để nhân viên có thể Thêm món luôn
    
    await fetchAllBan();
    await fetchTableStatus();
    await handleFetchAllCheckIn();
  } catch (err) {
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Cập nhật thất bại!' });
  }
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

const modalView = ref("info");
const switchToAddFood = () => {
  modalView.value = "addFood";
};

const listMonDaChon = ref([]);

const totalTempPrice = computed(() => {
  return listMonDaChon.value.reduce(
    (sum, item) => sum + item.price * item.quantity,
    0,
  );
});

const handleSaveFood = async (itemsArray) => {
  if (!selectedPhieu.value?.id) {
    Swal.fire('Lưu ý', 'Bàn này chưa có Phiếu đặt trước. Hệ thống không hỗ trợ thêm món cho khách vãng lai!', 'warning');
    return;
  }

  listMonDaChon.value = itemsArray;

  if (itemsArray.length === 0) {
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Vui lòng chọn ít nhất 1 món' });
    return;
  }

  try {
    const userStorage = localStorage.getItem("user");
    let currentStaffId = 1;
    if (userStorage) {
      const user = JSON.parse(userStorage);
      if (user.id) currentStaffId = user.id;
    }

    // LẤY RA ID HÓA ĐƠN ĐỂ THÊM MÓN
    // (Hãy đảm bảo hàm openManageModal trước đó đã lưu idHoaDon vào selectedPhieu nhé)
    const idHoaDonThucTe = selectedPhieu.value.idHoaDon;

    // TẠO PAYLOAD CHUẨN VỚI SƠ ĐỒ DATABASE
    const payload = {
      // BẮT BUỘC PHẢI THÊM DÒNG NÀY ĐỂ BE BIẾT ĐANG SỬA HÓA ĐƠN NÀO
      idHoaDon: selectedPhieu.value?.idHoaDon || null, 
      
      idBanAn: selectedBan.value.id,
      idNhanVien: currentStaffId,
      idKhachHang: selectedPhieu.value?.idKhachHang || null,
      chiTietHoaDon: itemsArray.map(item => ({
        idChiTietMonAn: item.type === 'FOOD' ? item.originalId : null,
        idSetLau: item.type === 'SET' ? item.originalId : null,
        soLuong: item.quantity,
        ghiChu: item.note || '' 
      }))
    };
    

    // GỌI API THÊM MÓN
    await createOrder(payload);

    await Swal.fire({
      icon: 'success',
      title: 'Thành công!',
      text: "Đã cập nhật thực đơn thành công!",
      timer: 1500,
      showConfirmButton: false
    });

    modalView.value = 'info';

  } catch (e) {
    console.error("Lỗi khi thêm món:", e);
    Swal.fire({
      icon: 'error',
      title: 'Lỗi',
      text: "Lỗi khi lưu món: " + (e.response?.data?.message || e.message)
    });
  }
};

const handlePaymentVNPay = async () => {
  // Kiểm tra xem bàn đã có phiếu/hóa đơn chưa
  if (!selectedPhieu.value?.idHoaDon) {
    return Swal.fire('Lưu ý', 'Không tìm thấy hóa đơn để thanh toán!', 'warning');
  }

  try {
    // Hiện thông báo đang xử lý để khóa màn hình
    Swal.fire({ 
      title: 'Đang kết nối VNPay...', 
      text: 'Vui lòng không đóng trình duyệt',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    // Gọi API bạn vừa viết bên Backend
    const response = await axiosClient.get(`/vnpay/create-payment/${selectedPhieu.value.idHoaDon}`);
    
    // Đóng Swal và chuyển hướng sang trang thanh toán của VNPay
    if (response.data && response.data.url) {
      window.location.href = response.data.url;
    } else {
      Swal.fire('Lỗi', 'Không lấy được link thanh toán', 'error');
    }

  } catch (error) {
    console.error("Lỗi tạo VNPay:", error);
    Swal.fire('Lỗi', 'Có lỗi xảy ra khi kết nối cổng thanh toán', 'error');
  }
};

const handlePaymentCash = async () => {
  const totalAmount = totalTempPrice.value; // Lấy tổng tiền hiện tại trong modal

  const confirm = await Swal.fire({
    title: 'Xác nhận thanh toán tiền mặt?',
    text: `Tổng tiền: ${totalAmount.toLocaleString()}đ. Bàn sẽ được giải phóng ngay lập tức.`,
    icon: 'warning',
    showCancelButton: true,
    confirmButtonText: 'Xác nhận'
  });

  if (!confirm.isConfirmed) return;

  const payload = {
    idBanAn: selectedBan.value.id,
    trangThai: 0,        
    id: selectedPhieu.value.id,
    trangThaiPhieu: 4,   
    trangThaiHoaDon: 6,  
    tienMat: totalAmount, // <--- QUAN TRỌNG: Gửi số tiền mặt để Backend lưu Lịch sử
    ghiChu: "Thanh toán 100% tiền mặt tại quầy"
  };

  await executeApiUpdate(payload, 'Đã hoàn tất thanh toán tiền mặt!');
};

const canCheckIn = (dbTime) => {
  if (!dbTime) return false;
  const now = dayjs(currentTime.value);
  const target = dayjs(dbTime);
  const minutesLeft = target.diff(now, "minute");
  return minutesLeft <= 10;
};

const executeApiUpdate = async (payload, successMsg) => {
  try {
    await updateTrangThaiBan(payload);
    Swal.fire({ icon: 'success', title: 'Thành công!', text: successMsg, timer: 1500, showConfirmButton: false });
    closeModal();
    await fetchAllBan();
    await fetchTableStatus();
    await handleFetchAllCheckIn();
  } catch (err) {
    console.error("Lỗi API:", err);
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Thao tác thất bại! Vui lòng thử lại.' });
  }
};

const handlePaymentVNPayFull = async () => {
  if (!selectedPhieu.value?.idHoaDon) {
    return Swal.fire('Lưu ý', 'Bàn này chưa có hóa đơn để thanh toán!', 'warning');
  }

  const confirm = await Swal.fire({
    title: 'Thanh toán qua VNPay?',
    text: 'Hóa đơn sẽ chuyển sang trạng thái chờ thanh toán và hệ thống sẽ chuyển sang cổng VNPay. Bạn đồng ý chứ?',
    icon: 'question',
    showCancelButton: true,
    confirmButtonColor: '#e67e22',
    confirmButtonText: 'Đồng ý & Thanh toán',
    cancelButtonText: 'Quay lại'
  });

  if (!confirm.isConfirmed) return;

  const bId = selectedBan.value.id;
  
  // 1. LƯU TRỮ ID HÓA ĐƠN VÀO BIẾN CỤC BỘ TRƯỚC KHI BỊ XÓA BỞI closeModal()
  const idHoaDonThanhToan = selectedPhieu.value.idHoaDon; 

  const payload = {
    idBanAn: bId,
    trangThai: 1, 
    id: selectedPhieu.value.id,
    trangThaiPhieu: 3, 
    trangThaiHoaDon: 5,
    tienMat: 0, // <--- Bổ sung cái này để gửi xuống Backend không bị lỗi
    ghiChu: "Thanh toán 100% qua VNPay"
  };

  try {
    Swal.fire({ 
      title: 'Đang xử lý...', 
      text: 'Vui lòng không đóng trình duyệt',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    // 2. Cập nhật trạng thái xuống DB
    await updateTrangThaiBan(payload);
    
    // 3. Đóng modal
    closeModal();
    
    Swal.update({ title: 'Đang kết nối VNPay...' });

    // 4. Gọi API lấy link bằng biến đã lưu (Đã sửa idHoaDon thành idHoaDonThanhToan và bỏ remaining)
    const vnpayRes = await axiosClient.get(`/vnpay/create-payment/${idHoaDonThanhToan}`);
    
    if (vnpayRes.data && vnpayRes.data.url) {
      window.location.href = vnpayRes.data.url;
    } else {
      Swal.fire('Lỗi', 'Không lấy được link thanh toán từ hệ thống', 'error');
      await fetchAllBan(); 
    }

  } catch (err) {
    console.error("Lỗi quy trình thanh toán:", err);
    Swal.fire('Lỗi', 'Thao tác thất bại! Vui lòng thử lại.', 'error');
    await fetchAllBan();
  }
};

const handlePaymentMixed = async () => {
  const totalInvoice = totalTempPrice.value;

  const { value: cashAmountStr } = await Swal.fire({
    title: 'Thanh toán hỗn hợp',
    input: 'number',
    inputLabel: `Tổng hóa đơn: ${totalInvoice.toLocaleString()}đ`,
    inputPlaceholder: 'Nhập số tiền mặt khách trả trước...',
    showCancelButton: true,
    confirmButtonText: 'Tiếp tục sang VNPay',
    inputValidator: (value) => {
      if (!value || value <= 0) return 'Vui lòng nhập số tiền hợp lệ!';
      if (value >= totalInvoice) return 'Tiền mặt phải nhỏ hơn tổng hóa đơn!';
    }
  });

  if (cashAmountStr) {
    const cashAmount = Number(cashAmountStr);
    const remaining = totalInvoice - cashAmount;

    try {
      Swal.fire({ title: 'Đang xử lý...', allowOutsideClick: false, didOpen: () => Swal.showLoading() });

      // BƯỚC 1: Lưu lịch sử Tiền mặt vào DB trước
      const payload = {
        idBanAn: selectedBan.value.id,
        trangThai: 1, // Vẫn giữ khách tại bàn cho đến khi VNPay xong
        id: selectedPhieu.value.id,
        trangThaiPhieu: 3, 
        trangThaiHoaDon: 5, // Chuyển sang trạng thái "Chờ thanh toán"
        tienMat: cashAmount, // <--- Gửi số tiền mặt khách vừa đưa
        ghiChu: `Thanh toán hỗn hợp: Đã thu ${cashAmount.toLocaleString()}đ tiền mặt.`
      };
      
      await updateTrangThaiBan(payload); // Gọi API backend để lưu Lịch sử CASH

      // BƯỚC 2: Chuyển hướng sang VNPay cho phần tiền còn lại
      const idHoaDon = selectedPhieu.value.idHoaDon;
      const vnpayRes = await axiosClient.get(`/api/vnpay/create-payment/${idHoaDon}?amount=${remaining}`);
      
      if (vnpayRes.data && vnpayRes.data.url) {
        window.location.href = vnpayRes.data.url;
      }
    } catch (error) {
      Swal.fire('Lỗi', 'Không thể khởi tạo thanh toán hỗn hợp', 'error');
    }
  }
};

const handleCancelTicket = async () => {
  if (!selectedPhieu.value?.id) {
    return Swal.fire('Lưu ý', 'Bàn này chưa có phiếu để hủy!', 'warning');
  }

  const confirm = await Swal.fire({
    title: 'Hủy phiếu đặt bàn?',
    text: 'Phiếu sẽ bị hủy và bàn sẽ được dọn trống. Bạn chắc chắn chứ?',
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    confirmButtonText: 'Đồng ý hủy',
    cancelButtonText: 'Quay lại'
  });

  if (!confirm.isConfirmed) return;

  const bId = selectedBan.value.id;
  const payload = {
    idBanAn: bId,
    trangThai: 0, 
    id: selectedPhieu.value.id,
    trangThaiPhieu: 0 
  };

  // ÉP GIAO DIỆN CHUYỂN VỀ TRỐNG KHI HỦY PHIẾU
  tableStatusMap.value = {
    ...tableStatusMap.value,
    [bId]: 0
  };

  await executeApiUpdate(payload, 'Hủy phiếu thành công. Đã giải phóng bàn!');
};

const activeBookingTableIds = computed(() => {
  // Lấy ID của bàn từ các phiếu đang chờ ở danh sách bên phải
  const bookingIds = danhSachLoc.value.map(khach => {
    const ban = danhSachBan.value.find(b => b.id === khach.idBanAn || b.maBan === khach.maBan);
    return ban ? ban.id : null;
  });

  // Lấy thêm ID của tất cả các bàn đang có trạng thái "Có khách" (1) trên sơ đồ
  const occupiedIds = danhSachBan.value
    .filter(ban => getTrangThaiTheoNgay(ban.id) === 1)
    .map(ban => ban.id);

  return [...new Set([...bookingIds, ...occupiedIds])].filter(id => id !== null);
});

watch(
  selectedDate,
  async () => {
    await fetchTableStatus();
  },
  { immediate: true },
);

const props = defineProps({
    initialItems: {
        type: Array,
        default: () => []
    }
});

const initSelectedItems = () => {
    selectedItems.value = {};
    if (props.initialItems && props.initialItems.length > 0) {
        props.initialItems.forEach(item => {
            const key = item.uniqueId;
            if (key) {
                selectedItems.value[key] = { ...item };
            }
        });
    }
};

watch(() => props.initialItems, (newItems) => {
    initSelectedItems();
}, { deep: true, immediate: true });
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
              <div class="d-inline-block me-2">
                <button
                  class="btn"
                  :class="activeFloor === 1 ? 'btn-active' : 'btn-outline'"
                  @click="activeFloor = 1"
                >
                  Tầng 1
                </button>
              </div>
              <div class="d-inline-block">
                <button
                  class="btn"
                  :class="activeFloor === 2 ? 'btn-active' : 'btn-outline'"
                  @click="activeFloor = 2"
                >
                  Tầng 2
                </button>
              </div>
            </div>

            <div class="floor-info mt-2">
              Tầng {{ activeFloor }} - Trống {{ thongKeTang.free }}/{{ thongKeTang.total }} bàn
            </div>
          </div>
        </div>
        <div>
          <div class="filter-date px-3">
            <label>📅 Lọc theo ngày</label>
            <input type="date" v-model="selectedDate" class="form-control" />
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
                  <div class="grid-canvas" @dragover.prevent @drop="onDrop" :class="{ 'editing-mode': isEditing }">
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
                          
                          <div class="card-footer mb-3">
                            <button
                              class="btn btn-checkable w-100"
                              :style="checkTimeStatus(khach.thoiGianDat).isEarly ? 'background-color: #e67e22 !important;' : ''"
                              @click="handleCheckInGuest(khach)"
                            >
                              <i class="fa-solid fa-check me-1"></i>
                              {{ checkTimeStatus(khach.thoiGianDat).isEarly ? "Khách đến sớm (Vào ngay)" : "Check-in ngay" }}
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
    <div class="modal-box" :class="{ 'modal-fullscreen': modalView === 'addFood' }">
      <div class="modal-header-custom">
        <h4 class="modal-title-custom">
          {{ modalView === "info" ? `Check-in bàn ${selectedBan?.maBan}` : "Thêm món ăn" }}
        </h4>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>

      <div class="modal-body-custom">
        <div v-if="modalView === 'info'">
          
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
          <div class="info-row">
            <span>Mã bàn:</span>
            <strong>{{ selectedBan?.maBan }}</strong>
          </div>
          <div class="info-row">
            <span>Sức chứa:</span>
            <strong>{{ selectedBan?.soCho }} người</strong>
          </div>
          <div class="info-row align-items-center">
            <span>Trạng thái:</span>
            <span class="badge-status">{{ getStatusText(selectedBan?.trangThai) }}</span>
          </div>
          <div class="info-row">
            <span>Vị trí:</span>
            <strong>Tầng {{ selectedBan?.soTang }}</strong>
          </div>
          <div class="info-row">
            <span>Nhân viên:</span>
            <strong>{{ currentStaffName || 'Chưa xác định' }}</strong>
          </div>

          <div v-if="listMonDaChon.length > 0" class="selected-summary mt-3">
            <div class="d-flex justify-content-between">
              <span class="text-success fw-bold">Các món đã chọn:</span>
              <span class="text-danger fw-bold">{{ totalTempPrice.toLocaleString() }}đ</span>
            </div>
            <ul class="summary-list">
              <li v-for="item in listMonDaChon" :key="item.id">
                {{ item.name }} <span class="text-muted">x{{ item.quantity }}<span class="text-muted">: <strong>{{ item.price }} VNĐ</strong></span></span>
              </li>
            </ul>
          </div>

          <hr class="my-3" />

          <div v-if="selectedPhieu?.id" class="action-buttons">
            <button class="btn-action" :class="{ 'has-items': listMonDaChon.length > 0 }" @click="switchToAddFood">
              <i class="fa-solid" :class="listMonDaChon.length > 0 ? 'fa-pen-to-square' : 'fa-plus'"></i>
              <span v-if="listMonDaChon.length === 0">Thêm món</span>
              <span v-else> Đã chọn {{ listMonDaChon.length }} món </span>
            </button>
            <button class="btn-action">QR đặt món</button>
            <button class="btn-action">Xem đơn hàng</button>
            <button class="btn-action">Đổi bàn</button>
          </div>
          <div v-else class="text-center text-muted mb-3">
            <small><i>Bàn trống. Cần Check-in phiếu đặt để thực hiện các thao tác thêm món.</i></small>
          </div>

          <hr class="my-3" />
<h6 class="section-title">Tùy chỉnh trạng thái bàn</h6>
          
          <div class="status-options" :class="{ 'disabled-section': getTrangThaiTheoNgay(selectedBan?.id) === 1 }">
            
            <div class="status-item" :class="{ 'active-border': selectedBan?.trangThai === 0 }" 
                 @click="() => { if(getTrangThaiTheoNgay(selectedBan?.id) !== 1) selectedBan.trangThai = 0 }">
              <label><i :class="selectedBan?.trangThai === 0 ? 'fa-solid fa-circle-dot' : 'fa-regular fa-circle'"></i> Trống</label>
            </div>
            
            <div class="status-item" :class="{ 'active-border': selectedBan?.trangThai === 1 }" 
                 @click="() => { if(getTrangThaiTheoNgay(selectedBan?.id) !== 1) selectedBan.trangThai = 1 }">
              <label><i :class="selectedBan?.trangThai === 1 ? 'fa-solid fa-circle-dot' : 'fa-regular fa-circle'"></i> Checked-in</label>
            </div>
            
          </div>

          <button v-if="getTrangThaiTheoNgay(selectedBan?.id) !== 1" class="btn btn-update-status mt-4" @click="updateStatus">
            Cập nhật trạng thái bàn
          </button>
          
          <div v-else class="alert alert-success mt-4 p-2 text-center" style="background-color: #f0fdf4; border: 1px solid #bbf7d0; color: #166534; border-radius: 8px;">
            <i class="fa-solid fa-check-circle me-1"></i> Bàn đã được Check-in thành công
          </div>

          <div v-if="getTrangThaiTheoNgay(selectedBan?.id) === 1 && selectedPhieu?.id" class="payment-group mt-3 pt-3 border-top">
          <h6 class="section-title mb-2">Quyết toán hóa đơn</h6>
          
          <div class="payment-grid">
            <button class="btn-pay cash-btn" @click="handlePaymentCash">
              <i class="fa-solid fa-money-bill-1-wave"></i>
              <span>Tiền mặt</span>
            </button>
            
            <div class="d-flex gap-2">
              <button class="btn-pay vnpay-btn" @click="handlePaymentVNPayFull">
                <i class="fa-solid fa-qrcode"></i>
                <span>VNPay</span>
              </button>

              <button class="btn-pay mixed-btn" @click="handlePaymentMixed">
                <i class="fa-solid fa-layer-group"></i>
                <span>Hỗn hợp</span>
              </button>
            </div>
          </div>

          <button class="btn-cancel-ticket mt-3" @click="handleCancelTicket">
            <i class="fa-solid fa-ban me-1"></i> Hủy phiếu đặt
          </button>
        </div>

        </div>

        <div v-else class="h-100 full-modal-content">
          <FoodList :initial-items="listMonDaChon" @close="modalView = 'info'" @save="handleSaveFood" />
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
  background-color: #28a745; /* Xanh lá */
  width: 100%;
}

.vnpay-btn {
  background-color: #007bff; /* Xanh dương */
}

.mixed-btn {
  background-color: #ffc107; /* Vàng */
  color: #333;
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
</style>
