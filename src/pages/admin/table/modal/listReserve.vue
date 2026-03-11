<script setup>
import "@vuepic/vue-datepicker/dist/main.css";
import { onMounted, onUnmounted, ref } from "vue";
import dayjs from "dayjs";
// import utc from "dayjs/plugin/utc";
// import timezone from "dayjs/plugin/timezone";
import {
  fetchAllBanAn,
  searchDatBanService,
  updatePhieuDatBanService,
  updateTTPhieuDatBan,
  fetchAllCheckIn,
  fetchAllPreCheckIn
} from "@/services/tableManageService";
import Swal from "sweetalert2";
import CommonPagination from "@/components/commonPagination.vue";

// dayjs.extend(utc);
// dayjs.extend(timezone);

// --- Khai báo State ---
const listPhieuDatBan = ref([]);
const totalPages = ref(0);
const totalElements = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);

const danhSachCho = ref([]);

const loadDanhSachCho = async () => {
  try {
    danhSachCho.value = await fetchAllPreCheckIn(); 
  } catch (error) {
    console.error("Lỗi lấy danh sách khách chờ:", error);
  }
};

const searchForm = ref({
  soDienThoai: "",
  trangThai: "",
  thoiGianDat: "",
});

const trangThaiList = [
  { value: "0", label: "Ch\u1EDD x\u00E1c nh\u1EADn" },
  { value: "1", label: "\u0110\u00E3 x\u00E1c nh\u1EADn" },
  { value: "2", label: "\u0110\u00E3 h\u1EE7y" },
  { value: "3", label: "\u0110ang s\u1EED d\u1EE5ng" },
  { value: "4", label: "Ho\u00E0n th\u00E0nh" },
  { value: "5", label: "Qu\u00E1 h\u1EA1n" },
];

const selectedPhieu = ref(null);
const selectedBan = ref(null);
const showModal = ref(false);
const danhSachBan = ref([]);

// --- Các hàm xử lý Logic ---

// 1. Hàm tìm kiếm và phân trang (POST)
const buildSearchPayload = () => {
  let ngayDat = null;

  if (searchForm.value.thoiGianDat) {
    // logic giống format("YYYY-MM-DD")
    ngayDat = dayjs(searchForm.value.thoiGianDat).format("YYYY-MM-DD");
  }

  return {
    soDienThoai: searchForm.value.soDienThoai || null,
    trangThai: searchForm.value.trangThai || null,
    thoiGianDat: ngayDat, // yyyy-MM-dd
  };
};

const searchDatBan = async () => {
  try {
    const payload = buildSearchPayload();
    console.log(buildSearchPayload());

    const data = await searchDatBanService({
      payload,
      page: currentPage.value - 1,
      size: pageSize.value,
    });

    listPhieuDatBan.value = data.content;
    // listPhieuDatBanFiltered.value = data.content; 
    totalPages.value = data.totalPages;
    totalElements.value = data.totalElements ?? data.content?.length ?? 0;
  } catch (error) {
    console.error("Lỗi searchDatBan:", error);
  }
};
const onResetSearch = () => {
  searchForm.value = {
    soDienThoai: "",
    trangThai: "",
    thoiGianDat: "",
  };
  currentPage.value = 1;
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
  searchDatBan();
};

let searchDebounceTimer = null;

const triggerAutoSearch = () => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
  searchDebounceTimer = setTimeout(() => {
    currentPage.value = 1;
    searchDatBan();
  }, 250);
};
// const listPhieuDatBanFiltered = computed(() => {
//   return listPhieuDatBan.value.filter((phieu) => {
//     if (searchForm.value.soDienThoai && 
//         !phieu.soDienThoai?.includes(searchForm.value.soDienThoai)) {
//       return false;
//     }

//     if (searchForm.value.trangThai && 
//         Number(phieu.trangThai) !== Number(searchForm.value.trangThai)) {
//       return false;
//     }

//     if (searchForm.value.ngayDat) {
//       const phieuDate = dayjs(phieu.thoiGianDat).format("YYYY-MM-DD");
//       const searchDate = dayjs(searchForm.value.ngayDat).format("YYYY-MM-DD");
//       if (phieuDate !== searchDate) return false;
//     }

//     return true;
//   });
// });

// 2. Hàm chuyển trang
const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  searchDatBan();
};

const openChonBanModal = async (phieu) => {
  selectedPhieu.value = phieu;
  try {
    const [banData] = await Promise.all([
      fetchAllBanAn(),
      loadDanhSachCho()
    ]);
    danhSachBan.value = banData;
    showModal.value = true;
  } catch (error) {
    console.error("Lỗi lấy danh sách bàn:", error);
  }
};

const closeModal = () => {
  showModal.value = false;
  selectedBan.value = null;
};

const isTableReserved = (banId, thoiGianDatMoi) => {
  if (!thoiGianDatMoi || danhSachCho.value.length === 0) return false;

  const timeMoi_BatDau = dayjs(thoiGianDatMoi);
  // Giả sử mỗi ca ăn kéo dài 3 tiếng (180 phút)
  const timeMoi_KetThuc = timeMoi_BatDau.add(180, 'minute');

  return danhSachCho.value.some(khach => {
    // 1. Bỏ qua chính cái phiếu mình đang thao tác xếp bàn (Tránh tự chặn chính mình)
    if (khach.idDatBan === selectedPhieu.value?.idDatBan) return false;

    // 2. Bỏ qua nếu phiếu kia chưa được xếp bàn nào cả
    if (!khach.idBanAn) return false;

    // 3. Nếu phiếu kia đang giữ ĐÚNG cái bàn mình đang xét
    if (khach.idBanAn === banId) {
      
      // 4. Chỉ tính các phiếu đang Active (0: Chờ XN, 1: Đã XN, 3: Đang ăn)
      // Dựa vào JSON của bạn, trạng thái đang là 1
      if ([0, 1, 3].includes(Number(khach.trangThai))) {
        
        const timeKhach_BatDau = dayjs(khach.thoiGianDat);
        const timeKhach_KetThuc = timeKhach_BatDau.add(180, 'minute');

        // 5. Logic kiểm tra Giao nhau thời gian (Overlap)
        // (Bắt đầu A < Kết thúc B) VÀ (Bắt đầu B < Kết thúc A)
        const isOverlap = timeMoi_BatDau.isBefore(timeKhach_KetThuc) && timeKhach_BatDau.isBefore(timeMoi_KetThuc);
        
        if (isOverlap) {
           return true; // Phất cờ báo hiệu TRÙNG LỊCH!
        }
      }
    }
    return false;
  });
};

// 2. HÀM XỬ LÝ CHẶN CLICK VÀO BÀN
const chonBan = (ban) => {
  // Bỏ chọn nếu bấm lại vào bàn đang chọn
  if (selectedBan.value?.id === ban.id) {
    selectedBan.value = null;
    return;
  }

  // Check 1: Bàn không đủ chỗ
  if (ban.soCho < selectedPhieu.value?.soLuongKhach) {
     Swal.fire({ toast: true, position: 'top-end', icon: 'warning', title: 'Bàn không đủ chỗ!', showConfirmButton: false, timer: 2000 });
     return; // Chặn
  }
  
  // Check 2: Bị trùng lịch
  if (isTableReserved(ban.id, selectedPhieu.value?.thoiGianDat)) {
     Swal.fire({ toast: true, position: 'top-end', icon: 'error', title: 'Bàn đã bị đặt trong khung giờ này!', showConfirmButton: false, timer: 2000 });
     return; // Chặn
  }

  // Hợp lệ thì mới gán
  selectedBan.value = ban;
};

const updatePhieuDatBan = async () => {
  if (!selectedBan.value || !selectedPhieu.value) return;

  const payload = {
    id: selectedPhieu.value.idDatBan,
    idBanAn: selectedBan.value.id,
  };

  try {
    await updatePhieuDatBanService(payload);

    Swal.fire({ icon: 'success', iconColor: '#7D161A', title: 'Thành công!', timer: 1500, showConfirmButton: false });
    closeModal();
    await searchDatBan(); // Load lại danh sách tại trang hiện tại
  } catch (err) {
    console.error(err);
    Swal.fire('Lỗi', 'Không thể xếp bàn', 'error');
  }
};

const formatDate = (time) => {
  if (!time) return "";
  return dayjs(time).format("DD/MM/YYYY HH:mm");
};

const onToggleCheckIn = async (phieu) => {
  // chỉ xử lý khi đang = 1
  if (phieu.trangThai !== 1) return;

  try {
    await updateTTPhieuDatBan(phieu.idDatBan, 2);
    phieu.trangThai = 2; // sync UI
  } catch (err) {
    console.error("Check-in thất bại", err);
  }
};
// const listPhieuDatBanFiltered = ref([]); // data sau khi bấm tìm kiếm



// const onSearchClick = () => {
//   console.log("searchForm.trangThai:", searchForm.value.trangThai, typeof searchForm.value.trangThai);
//   console.log("listPhieuDatBan:", listPhieuDatBan.value.map(p => ({
//     id: p.idDatBan,
//     trangThai: p.trangThai,
//     type: typeof p.trangThai
//   })));
//   listPhieuDatBanFiltered.value = listPhieuDatBan.value.filter((phieu) => {
//     // SDT
//     const matchPhone =
//       !searchForm.value.soDienThoai ||
//       phieu.soDienThoai?.includes(searchForm.value.soDienThoai);

//     // Trạng thái
//     const matchTrangThai =
//       !searchForm.value.trangThai ||
//       String(phieu.trangThai) === String(searchForm.value.trangThai);

//     // Ngày (logic GIỐNG cái mày đưa)
//     let matchDate = true;
//     if (searchForm.value.ngayDat) {
//       matchDate =
//         dayjs(phieu.thoiGianDat).format("YYYY-MM-DD") ===
//         searchForm.value.ngayDat;
//     }

//     return matchPhone && matchTrangThai && matchDate;
//   });
// };




// --- Lifecycle ---
onMounted(() => {
  searchDatBan();
});

onUnmounted(() => {
  if (searchDebounceTimer) clearTimeout(searchDebounceTimer);
});
</script>

<template>
  <div class="layout-table">
    <div class="navbar-search">
      <div class="search-form">
        Tìm kiếm
                <form class="d-flex gap-2 mt-2" role="search">
          <input
            class="form-control reserve-filter-phone"
            type="search"
            placeholder="S&#7889; &#273;i&#7879;n tho&#7841;i kh&#225;ch h&#224;ng"
            v-model="searchForm.soDienThoai"
            @input="triggerAutoSearch"
          />
          <select
            v-model="searchForm.trangThai"
            @change="triggerAutoSearch"
            class="form-select reserve-filter-status"
          >
            <option value="" disabled>Tr&#7841;ng th&#225;i</option>
            <option
              v-for="item in trangThaiList"
              :key="item.value"
              :value="item.value"
            >
              {{ item.label }}
            </option>
          </select>

          <div class="reserve-filter-date">
            <input
              type="date"
              v-model="searchForm.thoiGianDat"
              @input="triggerAutoSearch"
              class="form-control"
            />
          </div>

          <button class="btn text-nowrap" type="button" @click="onResetSearch">
             X&#243;a b&#7897; l&#7885;c
          </button>
        </form>
      </div>
      <div class="mt-3">
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th scope="col">STT</th>
                <th scope="col">MÃ</th>
                <th scope="col">KHÁCH HÀNG</th>
                <th scope="col">SĐT</th>
                <th scope="col">BÀN</th>
                <th scope="col">TẦNG</th>
                <th scope="col">NGÀY GIỜ</th>
                <th scope="col">SỐ NGƯỜI</th>
                <th scope="col">TRẠNG THÁI</th>
                <th scope="col">HÀNH ĐỘNG</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(phieuDatBan, index) in listPhieuDatBan"
                :key="phieuDatBan.id"
              >
                <th>{{ (currentPage - 1) * pageSize + index + 1 }}</th>
                <td>{{ phieuDatBan.maDatBan }}</td>
                <td>{{ phieuDatBan.tenKhachHang }}</td>
                <td>{{ phieuDatBan.soDienThoai }}</td>
                <td>
                  <!-- Nếu đã có bàn -->
                  <span v-if="phieuDatBan.maBan">
                    {{ phieuDatBan.maBan }}
                  </span>

                  <!-- Nếu chưa có bàn -->
                  <button
                    v-else
                    class="btn btn-sm btn-custom-outline"
                    :disabled="Number(phieuDatBan.trangThai) === 2"
                    :title="Number(phieuDatBan.trangThai) === 2 ? 'Phiếu đã hủy, không thể chọn bàn' : 'Chọn bàn'"
                    @click="openChonBanModal(phieuDatBan)"
                  >
                    Chọn bàn
                  </button>
                </td>

                <td>
                  {{
                    phieuDatBan.soTang == null ? "Trống" : phieuDatBan.soTang
                  }}
                </td>
                <td>{{ formatDate(phieuDatBan.thoiGianDat) }}</td>
                <td>{{ phieuDatBan.soLuongKhach }}</td>
                <td>{{ phieuDatBan.tenTrangThai }}</td>
                <td>
                  <div class="action-groups d-flex align-items-center gap-3">

                    <div class="checkbox-wrapper-5">
                      <!-- From Uiverse.io by JaydipPrajapati1910 -->
                      <label class="switch">
                        <input
                          type="checkbox"
                          :checked="phieuDatBan.trangThai === 1"
                          :disabled="phieuDatBan.trangThai !== 1"
                          @click.prevent="onToggleCheckIn(phieuDatBan)"
                        />

                        <div class="slider">
                          <div class="circle">
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              version="1.1"
                              xmlns:xlink="http://www.w3.org/1999/xlink"
                              width="6"
                              height="6"
                              x="0"
                              y="0"
                              viewBox="0 0 365.696 365.696"
                              style="enable-background: new 0 0 512 512"
                              xml:space="preserve"
                              class="cross"
                            >
                              <g>
                                <path
                                  d="M243.188 182.86 356.32 69.726c12.5-12.5 12.5-32.766 0-45.247L341.238 9.398c-12.504-12.503-32.77-12.503-45.25 0L182.86 122.528 69.727 9.374c-12.5-12.5-32.766-12.5-45.247 0L9.375 24.457c-12.5 12.504-12.5 32.77 0 45.25l113.152 113.152L9.398 295.99c-12.503 12.503-12.503 32.769 0 45.25L24.48 356.32c12.5 12.5 32.766 12.5 45.247 0l113.132-113.132L295.99 356.32c12.503 12.5 32.769 12.5 45.25 0l15.081-15.082c12.5-12.504 12.5-32.77 0-45.25zm0 0"
                                  fill="currentColor"
                                  data-original="#000000"
                                ></path>
                              </g>
                            </svg>
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              version="1.1"
                              xmlns:xlink="http://www.w3.org/1999/xlink"
                              width="10"
                              height="10"
                              x="0"
                              y="0"
                              viewBox="0 0 24 24"
                              style="enable-background: new 0 0 512 512"
                              xml:space="preserve"
                              class="checkmark"
                            >
                              <g>
                                <path
                                  d="M9.707 19.121a.997.997 0 0 1-1.414 0l-5.646-5.647a1.5 1.5 0 0 1 0-2.121l.707-.707a1.5 1.5 0 0 1 2.121 0L9 14.171l9.525-9.525a1.5 1.5 0 0 1 2.121 0l.707.707a1.5 1.5 0 0 1 0 2.121z"
                                  fill="currentColor"
                                  data-original="#000000"
                                  class=""
                                ></path>
                              </g>
                            </svg>
                          </div>
                        </div>
                      </label>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="custom-pagination-wrapper mt-4 pt-3 border-top">
        <CommonPagination
          v-model:currentPage="currentPage"
          v-model:pageSize="pageSize"
          :total-pages="totalPages"
          :total-elements="totalElements"
          :current-count="listPhieuDatBan.length"
          @change="searchDatBan"
        />
      </div>
    </div>
  </div>

  <!--POPUP XẾP BÀN-->
  <div v-if="showModal" class="modal-overlay">
    <div class="modal-box">
      <!-- Header -->
      <div class="modal-header">
        <h3>Xếp bàn</h3>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>
      <hr />
      <!-- Thông tin -->
      <div class="info-grid">
        <div>
          <label>Khách hàng:</label>
          <strong>{{ selectedPhieu?.tenKhachHang }}</strong>
        </div>
        <div>
          <label>Số điện thoại:</label>
          <strong>{{ selectedPhieu?.soDienThoai }}</strong>
        </div>
        <div>
          <label>Số người:</label>
          <strong>{{ selectedPhieu?.soLuongKhach }} người</strong>
        </div>
        <div>
          <label>Ngày và giờ:</label>
          <strong>{{ formatDate(selectedPhieu?.thoiGianDat) }}</strong>
        </div>
      </div>

      <hr />

      <!-- Danh sách bàn (demo) -->
      <!-- Danh sách bàn (DYNAMIC) -->
      <div class="ban-grid">
        <div
          v-for="ban in danhSachBan"
          :key="ban.id"
          class="ban-card"
          :class="{
            'selected': selectedBan?.id === ban.id,
            /* 🚨 Xóa bỏ cái disabled cũ, chỉ giữ lại disabled-table */
            'disabled-table': ban.soCho < selectedPhieu.soLuongKhach || isTableReserved(ban.id, selectedPhieu.thoiGianDat)
          }"
          @click="chonBan(ban)"
        >
          <i
            v-if="selectedBan?.id === ban.id"
            class="fa-solid fa-star suggest"
          ></i>

          <div class="ban-name">{{ ban.maBan }}</div>
          <div class="ban-size">({{ ban.soCho }} chỗ)</div>
          
          <div v-if="isTableReserved(ban.id, selectedPhieu.thoiGianDat)" class="conflict-badge">
            <i class="fa-regular fa-clock"></i> Đã có khách đặt
          </div>
          <div v-else-if="ban.soCho < selectedPhieu.soLuongKhach" class="conflict-badge bg-warning text-dark">
            <i class="fa-solid fa-users-slash"></i> Thiếu chỗ
          </div>
        </div>
      </div>

      <hr />

      <!-- Footer -->
      <div class="modal-footer">
        <button class="btn-cancel" @click="closeModal">Hủy</button>
        <button
          class="btn-confirm"
          :disabled="!selectedBan"
          @click="updatePhieuDatBan"
        >
          Xác nhận xếp bàn
        </button>
      </div>
    </div>
  </div>
  <!-- 🔥 HẾT POPUP 🔥 -->
</template>

<style scoped>
.layout-table {
  display: flex;
  background-color: white;
}

.table tr th{
  padding: 14px 15px;
}

.table tr th {
  font-family: "Segoe UI", sans-serif;
  font-size: 14px;
  font-weight: 500;
}

.search-form {
  border: solid 1px #cacaca;
  border-radius: 15px;
  padding: 2%;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.navbar-search {
  width: 100%;
}

hr {
  border: 0;
  border-top: 2px solid #7d161a;
  /* Chỉnh độ dày ở đây */
}

.btn {
  background-color: #7d161a;
  color: white;
}

/* From Uiverse.io by JaydipPrajapati1910 */
.switch {
  /* switch */
  --switch-width: 46px;
  --switch-height: 24px;
  --switch-bg: rgb(131, 131, 131);
  --switch-checked-bg: #222;
  --switch-offset: calc((var(--switch-height) - var(--circle-diameter)) / 2);
  --switch-transition: all 0.2s cubic-bezier(0.27, 0.2, 0.25, 1.51);
  /* circle */
  --circle-diameter: 18px;
  --circle-bg: #fff;
  --circle-shadow: 1px 1px 2px rgba(146, 146, 146, 0.45);
  --circle-checked-shadow: -1px 1px 2px rgba(163, 163, 163, 0.45);
  --circle-transition: var(--switch-transition);
  /* icon */
  --icon-transition: all 0.2s cubic-bezier(0.27, 0.2, 0.25, 1.51);
  --icon-cross-color: var(--switch-bg);
  --icon-cross-size: 6px;
  --icon-checkmark-color: var(--switch-checked-bg);
  --icon-checkmark-size: 10px;
  /* effect line */
  --effect-width: calc(var(--circle-diameter) / 2);
  --effect-height: calc(var(--effect-width) / 2 - 1px);
  --effect-bg: var(--circle-bg);
  --effect-border-radius: 1px;
  --effect-transition: all 0.2s ease-in-out;
}

.switch input {
  display: none;
}

.switch {
  display: inline-block;
}

.switch svg {
  -webkit-transition: var(--icon-transition);
  -o-transition: var(--icon-transition);
  transition: var(--icon-transition);
  position: absolute;
  height: auto;
}

.switch .checkmark {
  width: var(--icon-checkmark-size);
  color: var(--icon-checkmark-color);
  -webkit-transform: scale(0);
  -ms-transform: scale(0);
  transform: scale(0);
}

.switch .cross {
  width: var(--icon-cross-size);
  color: var(--icon-cross-color);
}

.slider {
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
  width: var(--switch-width);
  height: var(--switch-height);
  background: var(--switch-bg);
  border-radius: 999px;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  position: relative;
  -webkit-transition: var(--switch-transition);
  -o-transition: var(--switch-transition);
  transition: var(--switch-transition);
  cursor: pointer;
}

.circle {
  width: var(--circle-diameter);
  height: var(--circle-diameter);
  background: var(--circle-bg);
  border-radius: inherit;
  -webkit-box-shadow: var(--circle-shadow);
  box-shadow: var(--circle-shadow);
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-align: center;
  -ms-flex-align: center;
  align-items: center;
  -webkit-box-pack: center;
  -ms-flex-pack: center;
  justify-content: center;
  -webkit-transition: var(--circle-transition);
  -o-transition: var(--circle-transition);
  transition: var(--circle-transition);
  z-index: 1;
  position: absolute;
  left: var(--switch-offset);
}

.slider::before {
  content: "";
  position: absolute;
  width: var(--effect-width);
  height: var(--effect-height);
  left: calc(var(--switch-offset) + (var(--effect-width) / 2));
  background: var(--effect-bg);
  border-radius: var(--effect-border-radius);
  -webkit-transition: var(--effect-transition);
  -o-transition: var(--effect-transition);
  transition: var(--effect-transition);
}

/* actions */

.switch input:checked + .slider {
  background: var(--switch-checked-bg);
}

.switch input:checked + .slider .checkmark {
  -webkit-transform: scale(1);
  -ms-transform: scale(1);
  transform: scale(1);
}

.switch input:checked + .slider .cross {
  -webkit-transform: scale(0);
  -ms-transform: scale(0);
  transform: scale(0);
}

.switch input:checked + .slider::before {
  left: calc(
    100% - var(--effect-width) - (var(--effect-width) / 2) -
      var(--switch-offset)
  );
}

.switch input:checked + .slider .circle {
  left: calc(100% - var(--circle-diameter) - var(--switch-offset));
  -webkit-box-shadow: var(--circle-checked-shadow);
  box-shadow: var(--circle-checked-shadow);
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  width: 720px;
  background: #fff;
  border-radius: 10px;
  padding: 20px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  color: #8b1d2c;
  margin: 0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 16px 0;
}

.info-grid label {
  color: #777;
  display: block;
}

.ban-grid {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  margin: 20px 0;
}

.ban-card {
  width: 150px;
  height: 90px;
  border: 2px solid #ccc;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  position: relative;
  padding-top: 18px;

  /* 👇 hiệu ứng mượt */
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.25s ease,
    border-color 0.25s ease;
}

/* Hover nhẹ */
.ban-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
}

.ban-card.selected {
  border-color: #8b1d2c;
  background: #f4eaea;
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.25s ease,
    border-color 0.25s ease;
}

.ban-card.disabled {
  opacity: 0.4;
  pointer-events: none;
}

.ban-name {
  font-weight: bold;
  font-size: 18px;
}

.ban-size {
  margin-top: 6px;
}

.suggest {
  position: absolute;
  top: 6px;
  right: 10px;
  color: #8b1d2c;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
}

.btn-cancel {
  background: #eee;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
}

.btn-confirm {
  background: #8b1d2c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
}

.btn-confirm:disabled {
  opacity: 0.6;
}

.btn-confirm:hover {
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  transition:
    transform 0.25s ease,
    box-shadow 0.25s ease,
    background-color 0.25s ease,
    border-color 0.25s ease;
}

.btn-custom-outline {
  background-color: transparent !important; /* Ép không có màu nền mặc định */
  color: #7d161a !important;
  border: 1px solid #7d161a !important;
}

.btn-custom-outline:hover {
  background-color: #7d161a !important; /* Chỉ đỏ khi hover */
  color: #ffffff !important;
}

/* Màu cho nút đang chọn (Active) */
.pagination .page-item.active .page-link {
  background-color: #7d161a !important;
  border-color: #7d161a !important;
  color: #ffffff !important;
}

/* Màu chữ cho các nút bình thường */
.pagination .page-link {
  color: #7d161a;
}

/* Hiệu ứng khi di chuột qua các nút */
.pagination .page-link:hover {
  background-color: #f8ecec;
  color: #5a1013;
  border-color: #dee2e6;
}

/* Xóa viền xanh khi click (box-shadow) */
.pagination .page-link:focus {
  box-shadow: 0 0 0 0.2rem rgba(125, 22, 26, 0.25);
}

.btn{
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white !important;
  transition: 0.2s;
}

.btn:hover {
  background: linear-gradient(135deg, #7D161A 0%, #D32F2F 100%);
  color: white !important;
  transform: scale(1.04);
}

.table-container{
  box-shadow: var(--bs-box-shadow-sm) !important;
  border-radius: 15px;
}

.reserve-filter-phone {
  width: 30%;
}

.reserve-filter-status {
  width: 25%;
}

.reserve-filter-date {
  width: 30%;
}

@media (max-width: 992px) {
  .reserve-filter-phone,
  .reserve-filter-status,
  .reserve-filter-date {
    width: 100%;
  }
}

.ban-card.disabled-table {
  background: repeating-linear-gradient(
    45deg,
    #f5f5f5,
    #f5f5f5 10px,
    #e9e9e9 10px,
    #e9e9e9 20px
  );
  border-color: #ddd;
  color: #a0a0a0;
  cursor: not-allowed;
  opacity: 0.65;
  /* Chặn hoàn toàn hover/click trên CSS */
  pointer-events: none; 
}

/* Badge thông báo lỗi ngay trên thẻ bàn */
.conflict-badge {
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  background-color: #dc3545;
  color: white;
  font-size: 11px;
  font-weight: bold;
  padding: 3px 8px;
  border-radius: 10px;
  white-space: nowrap;
  box-shadow: 0 2px 5px rgba(0,0,0,0.2);
}

:global(.swal2-container) {
  z-index: 99999 !important; /* Phải cao hơn 9999 của .modal-overlay */
}
</style>