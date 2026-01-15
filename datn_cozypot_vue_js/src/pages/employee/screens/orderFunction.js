import { ref } from 'vue';

export function useOrderManager() {

  const filters = ref({
    status: 'Tất cả',
    fromDate: '',
    toDate: ''
  });

  const isDetailModalOpen = ref(false);
  const selectedOrder = ref(null);

  const isHistoryModalOpen = ref(false);
  const selectedHistoryOrder = ref(null);
  const historyEvents = ref([]);


  const orderList = ref([
    { id: 'ORD14012016123', khachHang: 'Nguyễn Văn A', sdt: '0987654321', ban: 'B-01', tongTien: '320.000 ₫', trangThai: 'Đã xác nhận' },
    { id: 'ORD14012016124', khachHang: 'Nguyễn Văn B', sdt: '0987654321', ban: 'B-03', tongTien: '120.000 ₫', trangThai: 'Đã xác nhận' },
    { id: 'ORD14012016125', khachHang: 'Trần Văn C', sdt: '0987654321', ban: 'B-04', tongTien: '390.000 ₫', trangThai: 'Đã xác nhận' },
    { id: 'ORD14012016126', khachHang: 'Nguyễn Văn E', sdt: '0987654321', ban: 'B-06', tongTien: '700.000 ₫', trangThai: 'Đã xác nhận' },
    { id: 'ORD14012016127', khachHang: 'Nguyễn Văn F', sdt: '0987654321', ban: 'B-04', tongTien: '210.000 ₫', trangThai: 'Đã xác nhận' },
    { id: 'ORD14012016128', khachHang: 'Nguyễn Văn G', sdt: '0987654321', ban: 'B-06', tongTien: '800.000 ₫', trangThai: 'Hoàn thành' },
    { id: 'ORD14012016129', khachHang: 'Nguyễn Văn H', sdt: '0987654321', ban: 'B-01', tongTien: '218.000 ₫', trangThai: 'Hoàn thành' },
    { id: 'ORD14012016130', khachHang: 'Nguyễn Văn Y', sdt: '0987654321', ban: 'B-03', tongTien: '214.000 ₫', trangThai: 'Hoàn thành' },
    { id: 'ORD14012016131', khachHang: 'Nguyễn Văn K', sdt: '0987654321', ban: 'B-04', tongTien: '213.000 ₫', trangThai: 'Hoàn thành' },
  ]);

  
  const mockHistoryData = [
    { 
      id: 1, 
      action: 'Xóa món', 
      title: 'Xóa món Lẩu Thái Tomyum x1', 
      time: 'Vừa xong', 
      user: 'Administrator', 
      detail: 'Lẩu Thái Tomyum x1',
      orderCode: 'ORD20260101174139',
      type: 'delete' 
    },
    { 
      id: 2, 
      action: 'Thêm món', 
      title: 'Thêm món Lẩu nấm x1', 
      time: '5 phút trước', 
      user: 'Administrator', 
      detail: 'Lẩu nấm x1',
      orderCode: 'ORD20260101174139',
      type: 'add' 
    },
    { 
      id: 3, 
      action: 'Thêm món', 
      title: 'Thêm món Lẩu hải sản x1', 
      time: '7 phút trước', 
      user: 'Administrator', 
      detail: 'Lẩu hải sản x1',
      orderCode: 'ORD20260101174139',
      type: 'add'
    },
    { 
      id: 4, 
      action: 'Tạo đơn', 
      title: 'Tạo đơn hàng mới', 
      time: '10 phút trước', 
      user: 'Administrator', 
      detail: '',
      orderCode: 'ORD20260101174139',
      type: 'create' 
    }
  ];



  const handleSearch = () => {
    console.log("Đang tìm kiếm với:", filters.value);
  };

  const handleReset = () => {
    filters.value = { status: 'Tất cả', fromDate: '', toDate: '' };
  };

 
  const handleViewDetail = (id) => {
    const order = orderList.value.find(item => item.id === id);
    if (order) {
        selectedOrder.value = order;
        isDetailModalOpen.value = true;
    }
  };

  const closeDetailModal = () => {
    isDetailModalOpen.value = false;
    selectedOrder.value = null;
  };

 
  const handleViewHistory = (id) => {
    const order = orderList.value.find(item => item.id === id);
    if (order) {
        selectedHistoryOrder.value = order;
        historyEvents.value = mockHistoryData; 
        isHistoryModalOpen.value = true;
    }
  };

  const closeHistoryModal = () => {
    isHistoryModalOpen.value = false;
    selectedHistoryOrder.value = null;
  };

  const handlePrintOrder = (id) => {
    console.log("In hóa đơn:", id);
  };


  return {
    filters,
    orderList,
    isDetailModalOpen,
    selectedOrder,
    handleViewDetail,
    closeDetailModal,
    isHistoryModalOpen,
    selectedHistoryOrder,
    historyEvents,
    handleViewHistory,
    closeHistoryModal,
    handleSearch,
    handleReset,
    handlePrintOrder
  };
}