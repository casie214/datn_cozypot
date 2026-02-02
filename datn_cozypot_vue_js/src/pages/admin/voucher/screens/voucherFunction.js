import axios from 'axios';
import phieuGiamGiaService from '@/services/voucherService';

export const useVoucherLogic = () => {
    /**
     * Trả về class CSS và text hiển thị dựa trên trạng thái phiếu
     */
    const getStatusDisplay = (status) => {
        switch (status) {
            case 1:
                return { text: 'Đang hoạt động', class: 'badge bg-success' };
            case 0:
                return { text: 'Ngừng hoạt động', class: 'badge bg-secondary' };
            case 2:
                return { text: 'Đã hết hạn', class: 'badge bg-danger' };
            default:
                return { text: 'Không xác định', class: 'badge bg-dark' };
        }
    };

    /**
     * Gọi API tìm kiếm và phân trang phiếu giảm giá
     * Đảm bảo tên params khớp với @RequestParam trong Backend
     */
    const fetchData = async (filters, pagination) => {
        try {
            // Chúng ta ưu tiên dùng Service đã viết để code sạch hơn
            const response = await voucherService.getAll(
                filters.keyword || null,
                filters.trangThai,
                pagination.currentPage - 1,
                pagination.pageSize
            );

            // Nếu Backend của bạn có các filter ngày tháng riêng biệt, 
            // hãy dùng axios.get trực tiếp như dưới đây:
            /*
            const params = {
                keyword: filters.keyword || null,
                trangThai: filters.trangThai,
                ngayBatDau: filters.ngayBatDau || null,
                ngayKetThuc: filters.ngayKetThuc || null,
                page: pagination.currentPage - 1,
                size: pagination.pageSize
            };
            const response = await axios.get('http://localhost:8080/api/v1/phieu-giam-gia/search', { params });
            return response.data;
            */

            return response.data;
        } catch (error) {
            console.error("Lỗi khi tải danh sách phiếu giảm giá:", error);
            // Trả về cấu trúc mặc định để giao diện không bị crash
            return { content: [], totalPages: 0 };
        }
    };

    return { getStatusDisplay, fetchData };
};

export default {
  data() {
    return {
      voucher: {
        dotKhuyenMaiId: null, // id đợt KM của phiếu
      },
      danhSachDotKhuyenMai: [],
    };
  },

  mounted() {
    this.loadDotKhuyenMai();
    this.loadChiTietPhieu(); // nếu là màn chi tiết
  },

  methods: {
    async loadDotKhuyenMai() {
      const res = await fetch("http://localhost:8080/api/dot-khuyen-mai");
      this.danhSachDotKhuyenMai = await res.json();
    },

    async loadChiTietPhieu() {
      const res = await fetch("http://localhost:8080/api/phieu-giam-gia/1");
      const data = await res.json();

      // gán id đợt khuyến mãi cho combobox
      this.voucher.dotKhuyenMaiId = data.dotKhuyenMai?.id;
    },
  },
};
