import axios from 'axios'

const thongKeKhuyenMaiService = {
  getThongKePhieuGiamGia() {
    return axios
      .get('http://localhost:8080/api/phieu-giam-gia/thong-ke')
      .then(res => res.data)
  }
}

export default thongKeKhuyenMaiService
