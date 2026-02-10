import clientService from '@/services/clientService'; // Đảm bảo import đúng tên file service mới

export const useClientLogic = () => {
  
  /**
   * 1. Hiển thị trạng thái khách hàng
   * Khớp với các lớp .status-active và .status-resigned trong clientStyle.css
   */
  const getStatusDisplay = (status) => {
    const s = Number(status);
    // 1: Hoạt động, 0: Ngừng hoạt động
    if (s === 1) return { text: 'Hoạt động', class: 'status-active' };
    if (s === 0) return { text: 'Ngừng hoạt động', class: 'status-resigned' }; 
    
    return { text: 'Khác', class: 'status-other' };
  };

  /**
   * 2. Gọi API lấy danh sách khách hàng từ clientService
   * Kết quả trả về từ Backend (Spring Boot) thường nằm trong response.data
   */
  const fetchData = async (filters, pagination) => {
    // Chuẩn bị tham số gửi lên Backend
    const params = {
      keyword: filters.keyword ? filters.keyword.trim() : null, // Tìm theo mã, tên, sđt, email
      trangThai: filters.trangThai !== null ? filters.trangThai : null,
      tuNgay: filters.tuNgay || null,
      page: pagination.currentPage - 1, // Spring Boot Pageable bắt đầu từ 0
      size: pagination.pageSize
    };

    try {
      // Gọi đến hàm getAll đã định nghĩa trong clientService
      const response = await clientService.getAll(params);
      
      // Trả về dữ liệu (object chứa content, totalPages...)
      return response.data; 
    } catch (error) {
      console.error("Lỗi khi lấy danh sách khách hàng:", error);
      return { content: [], totalPages: 0 };
    }
  };

  return { getStatusDisplay, fetchData };
};