import staffService from '@/services/staffService';
import Swal from 'sweetalert2';

export const useStaffLogic = () => {
  
  const getStatusDisplay = (status) => {
    const s = Number(status);
    if (s === 1) {
      return { 
        text: 'Đang làm việc', 
        class: 'status-staff-active' 
      };
    }
    if (s === 2) {
      return { 
        text: 'Ngừng hoạt động', 
        class: 'status-staff-locked' 
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

  // Hàm xử lý Khóa/Mở khóa dùng SweetAlert2
  const toggleStaffStatus = async (nv, callback) => {
    const isLocking = nv.trangThaiLamViec === 1;
    const titleText = isLocking ? 'Xác nhận khóa?' : 'Xác nhận mở khóa?';
    const confirmBtnColor = isLocking ? '#d33' : '#28a745';
    
    // Cấu hình chung để nổi lên trên modal
    const swalConfig = {
      target: document.body,
      didOpen: () => {
        const container = document.querySelector('.swal2-container');
        if (container) container.style.zIndex = '100000';
      }
    };

    // 1. Hỏi xác nhận
    const result = await Swal.fire({
      ...swalConfig,
      title: titleText,
      text: `Bạn có chắc chắn muốn ${isLocking ? 'khóa' : 'mở khóa'} nhân viên: ${nv.hoTenNhanVien}?`,
      icon: isLocking ? 'warning' : 'question',
      showCancelButton: true,
      confirmButtonColor: confirmBtnColor,
      cancelButtonColor: '#6c757d',
      confirmButtonText: 'Xác nhận',
      cancelButtonText: 'Hủy'
    });

    if (result.isConfirmed) {
      try {
        // Hiện Loading
        Swal.fire({
          ...swalConfig,
          title: 'Đang xử lý...',
          allowOutsideClick: false,
          didOpen: () => {
            Swal.showLoading();
            if (document.querySelector('.swal2-container')) {
              document.querySelector('.swal2-container').style.zIndex = '100000';
            }
          }
        });

        await staffService.toggleStatus(nv.id);

        // 2. Thông báo thành công
        await Swal.fire({
          ...swalConfig,
          icon: 'success',
          title: 'Thành công!',
          text: `${isLocking ? 'Khóa' : 'Mở khóa'} nhân viên hoàn tất.`,
          timer: 1500,
          showConfirmButton: false
        });

        if (callback) callback(); 
      } catch (error) {
        console.error(error);
        Swal.fire({
          ...swalConfig,
          icon: 'error',
          title: 'Lỗi!',
          text: "Có lỗi xảy ra: " + (error.response?.data?.message || error.message),
          confirmButtonColor: '#800000'
        });
      }
    }
  };

  return { getStatusDisplay, fetchData, toggleStaffStatus };
};