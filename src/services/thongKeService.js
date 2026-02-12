import axiosClient from './axiosClient';

// Tổng
export const thongKeTong = (from, to) => {
  return axiosClient.get("/thong-ke/tong", {
    params: { from, to }
  });
};

// Thanh toán
export const thongKeThanhToan = (from, to) => {
  return axiosClient.get("/thong-ke/thanh-toan", {
    params: { from, to }
  });
};

export const thongKeNgay = (from, to) => {
  return axiosClient.get("/thong-ke/theo-ngay", {
    params: { from, to }
  });
};

