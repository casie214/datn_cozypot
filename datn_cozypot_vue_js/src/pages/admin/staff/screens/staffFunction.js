import staffService from '@/services/staffService';

export const useStaffLogic = () => {
  
  // Cập nhật chỉ còn 2 trạng thái: 1 (Hoạt động) và 2 (Ngừng hoạt động)
  const getStatusDisplay = (status) => {
    const s = Number(status);
    if (s === 1) {
      return { 
        text: 'Đang làm việc', 
        class: 'status-staff-active' // Class đen, in thường
      };
    }
    if (s === 2) {
      return { 
        text: 'Ngừng hoạt động', 
        class: 'status-staff-locked' // Class xám
      };
    }
    return { text: 'Không xác định', class: 'status-other' };
  };

  const fetchData = async (filters, pagination) => {
    const params = {
      keyword: filters.keyword ? filters.keyword.trim() : null,
      trangThai: filters.trangThai !== null ? filters.trangThai : null,
      tuNgay: filters.tuNgay || null,
      page: pagination.currentPage - 1,
      size: pagination.pageSize
    };

    try {
      const response = await staffService.getAll(params);
      return response.data;
    } catch (error) {
      console.error("Lỗi khi gọi API nhân viên:", error);
      throw error;
    }
  };

  // Thêm hàm xử lý Khóa/Mở khóa để dùng chung
  const toggleStaffStatus = async (nv, callback) => {
    const isLocking = nv.trangThaiLamViec === 1;
    const actionText = isLocking ? 'KHÓA' : 'MỞ KHÓA';
    
    if (confirm(`Bạn có chắc chắn muốn ${actionText} tài khoản nhân viên: ${nv.hoTenNhanVien}?`)) {
      try {
        await staffService.toggleStatus(nv.id);
        alert(`${actionText} thành công!`);
        if (callback) callback(); // Gọi lại hàm loadData()
      } catch (error) {
        alert("Có lỗi xảy ra: " + (error.response?.data || error.message));
      }
    }
  };

  return { getStatusDisplay, fetchData, toggleStaffStatus };
};