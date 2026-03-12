<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import axiosClient from "@/services/axiosClient";
import Swal from "sweetalert2";
import { useRouter } from "vue-router";

const router = useRouter();
// --- 1. STATE QUẢN LÝ DỮ LIỆU ---
const categories = ref([]);
const menuData = ref([]);
const isLoading = ref(false);
const searchQuery = ref("");
const sortOption = ref("default");
const activeCategory = ref("");

// --- 2. STATE GIỎ HÀNG ---
const cart = ref([]);
const isCartOpen = ref(false); // Trạng thái Modal giỏ hàng
const isCartHover = ref(false); // Trạng thái Hover xem trước

// --- 3. STATE MODAL CHI TIẾT MÓN (BIẾN THỂ) ---
const variantsMap = ref({}); // Map lưu: { groupKey: [variant1, variant2...] }
const isDetailModalOpen = ref(false);
const selectedProduct = ref(null); // Món đang click chọn
const currentVariants = ref([]); // Danh sách biến thể của món đang chọn
const selectedVariantId = ref(null); // ID biến thể đang chọn
const modalQuantity = ref(1); // Số lượng trong modal

// --- HELPER HÌNH ẢNH ---
const getImg = (url) => {
  if (!url) return "";
  if (url.startsWith("http") || url.startsWith("data:image")) return url;
  return `http://localhost:8080/uploads/${url}`;
};

const handleImageError = (event) => {
  // Ẩn thẻ img bị lỗi
  event.target.style.display = "none";
  // Tìm thẻ div chứa icon ngay phía sau thẻ img và hiển thị nó lên
  const fallbackIcon = event.target.nextElementSibling;
  if (fallbackIcon && fallbackIcon.classList.contains("fallback-icon")) {
    fallbackIcon.style.display = "flex";
  }
};

// ==========================================
// A. PHẦN XỬ LÝ DỮ LIỆU (THUẬT TOÁN GOM NHÓM THÔNG MINH)
// ==========================================
const fetchData = async () => {
  isLoading.value = true;
  try {
    const [resDanhMuc, resMonAn, resSetLau, resLoaiSet] = await Promise.all([
      axiosClient.get("/guest/category/active"),
      axiosClient.get("/guest/food/active"),
      axiosClient.get("/guest/hotpot/active"),
      axiosClient.get("/guest/hotpot-type/active"),
    ]);

    const listDanhMuc = resDanhMuc.data || [];
    const listMonAn = resMonAn.data || [];
    const listSetLau = resSetLau.data || [];
    const listLoaiSet = resLoaiSet.data || [];

    const groupedFoods = {};
    const vMap = {};

    // 1. THUẬT TOÁN GOM NHÓM MÓN LẺ (ĐÃ FIX LỖI TÁCH CHUỖI)
    listMonAn.forEach((m) => {
      let baseName = (m.tenMon || "Món chưa đặt tên").trim();

      // Lấy kích cỡ từ DB (nếu có)
      let variantName =
        m.tenDinhLuong || m.kichCo || m.giaTriDinhLuong || m.dinhLuong || "";
      if (m.donViTinh || m.tenDonVi) {
        variantName = `${variantName} ${m.donViTinh || m.tenDonVi}`.trim();
      }

      // SỬA LỖI Ở ĐÂY: Bắt buộc phải tách đuôi khỏi baseName
      if (baseName.includes(" - ")) {
        const parts = baseName.split(" - ");
        const extractedSize = parts.pop().trim(); // Luôn lấy phần đuôi ra

        // Gán tên gốc là phần còn lại
        baseName = parts.join(" - ").trim();

        // Nếu DB không trả về size, thì lấy phần đuôi vừa cắt làm size
        if (!variantName) variantName = extractedSize;
      } else if (baseName.includes("(") && baseName.endsWith(")")) {
        const match = baseName.match(/(.*)\((.*?)\)$/);
        if (match) {
          baseName = match[1].trim();
          if (!variantName) variantName = match[2].trim();
        }
      }

      if (!variantName) variantName = "Tiêu chuẩn";

      // Tạo Key Nhóm (Dùng .toLowerCase() để gom chính xác)
      const catId = m.idDanhMuc || m.danhMuc?.id || "unknown";
      const groupKey = `cat_${catId}_name_${baseName.toLowerCase()}`;

      if (!groupedFoods[groupKey]) {
        groupedFoods[groupKey] = {
          groupKey: groupKey,
          name: baseName, // Hiển thị "ÁDASDASDSADASD" (đã mất đuôi)
          image: m.hinhAnh,
          desc: m.moTa,
          idDanhMuc: catId,
        };
        vMap[groupKey] = [];
      }

      vMap[groupKey].push({
        id: m.id,
        name: variantName, // Hiển thị "100 gram", "3 kg"
        price: parseFloat(m.giaBan) || 0,
        fullName:
          variantName === "Tiêu chuẩn"
            ? baseName
            : `${baseName} (${variantName})`,
        image: m.hinhAnh,
      });
    });

    // Sắp xếp các biến thể theo giá từ thấp đến cao
    Object.keys(vMap).forEach((key) => {
      vMap[key].sort((a, b) => a.price - b.price);
    });
    variantsMap.value = vMap;

    // 2. XÂY DỰNG MENU HIỂN THỊ
    const processedMenu = [];

    // --- NHÓM SET LẨU ---
    if (listSetLau.length > 0) {
      const hotpotFilters = [
        { id: "all", name: "Tất cả" },
        ...listLoaiSet.map((ls) => ({ id: ls.id, name: ls.tenLoaiSet })),
      ];
      processedMenu.push({
        categoryId: "combo-set",
        categoryName: "SET LẨU",
        activeFilter: "all",
        filters: hotpotFilters,
        items: listSetLau.map((item) => ({
          id: item.id,
          name: item.tenSetLau,
          price: item.giaBan,
          image: getImg(item.hinhAnh),
          desc: item.moTa || "Set lẩu đầy đặn, thích hợp cho nhiều người.",
          type: "SET",
          groupId: item.idLoaiSet || item.loaiSet?.id,
          isRange: false,
          hasVariants: false,
        })),
      });
    }

    // --- NHÓM MÓN LẺ ---
    listDanhMuc.forEach((dm) => {
      const groupsInCat = Object.values(groupedFoods).filter(
        (g) => String(g.idDanhMuc) === String(dm.id),
      );

      if (groupsInCat.length > 0) {
        processedMenu.push({
          categoryId: `cat-${dm.id}`,
          categoryName: dm.tenDanhMuc.toUpperCase(),
          activeFilter: "all",
          filters: [],
          items: groupsInCat.map((group) => {
            const variants = vMap[group.groupKey] || [];
            let displayPrice = variants.length > 0 ? variants[0].price : 0;
            let isRange = false;

            if (variants.length > 1) {
              const minP = variants[0].price;
              const maxP = variants[variants.length - 1].price;
              if (minP !== maxP) {
                isRange = true;
                displayPrice = { min: minP, max: maxP };
              }
            }

            return {
              id: group.groupKey,
              name: group.name,
              price: displayPrice,
              image: getImg(group.image),
              desc: group.desc,
              type: "MON",
              isRange: isRange,
              groupId: group.idDanhMuc,
            };
          }),
        });
      }
    });

    menuData.value = processedMenu;
    categories.value = processedMenu.map((s) => ({
      id: s.categoryId,
      name: s.categoryName,
    }));
    if (categories.value.length) activeCategory.value = categories.value[0].id;
  } catch (error) {
    console.error("Lỗi tải dữ liệu Menu:", error);
  } finally {
    isLoading.value = false;
  }
};

// ==========================================
// B. LOGIC CHỌN MÓN & MODAL SẢN PHẨM
// ==========================================
const handleItemClick = (item) => {
  if (item.type === "SET") {
    pushToCart({
      id: item.id,
      name: item.name,
      price: item.price,
      image: item.image,
      type: "SET",
      quantity: 1,
    });
    return;
  }

  const variants = variantsMap.value[item.id] || [];

  if (variants.length <= 1) {
    // Món chỉ có 1 dung tích -> Bỏ thẳng giỏ hàng
    const variant =
      variants.length === 1
        ? variants[0]
        : { id: item.id, price: item.price, fullName: item.name };
    pushToCart({
      id: variant.id,
      name: variant.fullName, // Hiển thị tên đầy đủ
      price: variant.price,
      image: item.image,
      type: "MON",
      quantity: 1,
    });
  } else {
    // Có nhiều lựa chọn -> Bật Modal
    openProductModal(item, variants);
  }
};

const openProductModal = (item, variants) => {
  selectedProduct.value = item;
  currentVariants.value = variants;
  selectedVariantId.value = variants[0].id; // Mặc định chọn Size rẻ nhất
  modalQuantity.value = 1;
  isDetailModalOpen.value = true;
};

const closeDetailModal = () => {
  isDetailModalOpen.value = false;
  selectedProduct.value = null;
  currentVariants.value = [];
};

const confirmAddToCart = () => {
  if (!selectedProduct.value || !selectedVariantId.value) return;
  const variant = currentVariants.value.find(
    (v) => v.id === selectedVariantId.value,
  );

  pushToCart({
    id: variant.id,
    name: variant.fullName, // Chỗ này sẽ hiện "Coca (3L)" rất đẹp
    price: variant.price,
    image: variant.image ? getImg(variant.image) : selectedProduct.value.image,
    type: "MON",
    quantity: modalQuantity.value,
  });
  closeDetailModal();
};

// ==========================================
// C. LOGIC GIỎ HÀNG (CHỐNG SPAM TOÀN DIỆN)
// ==========================================
const getMaxQty = (itemType) => {
  return itemType === "SET" ? 5 : 20;
};

// Thiết lập giới hạn tổng
const MAX_TOTAL_ITEMS = 50;
const MAX_TOTAL_PRICE = 5000000; // 5 triệu VNĐ

// Hàm hiển thị cảnh báo chuẩn màu CozyPot
const showWarning = (title, text) => {
  Swal.fire({
    icon: "warning",
    title: title,
    text: text,
    confirmButtonText: "Đã hiểu",
    confirmButtonColor: "#7d161a", // Màu đỏ đô của quán
  });
};

const pushToCart = (newItem) => {
  // 1. Check giới hạn TỔNG TIỀN
  const currentTotal = totalPrice.value;
  const itemTotal = newItem.price * newItem.quantity;
  if (currentTotal + itemTotal > MAX_TOTAL_PRICE) {
    return showWarning("Vượt hạn mức", `Tổng hóa đơn đặt trước không được vượt quá ${formatPrice(MAX_TOTAL_PRICE)}. Vui lòng liên hệ Hotline nếu bạn muốn đặt tiệc lớn!`);
  }

  // 2. Check giới hạn TỔNG SỐ LƯỢNG MÓN
  const currentTotalItems = totalCount.value;
  if (currentTotalItems + newItem.quantity > MAX_TOTAL_ITEMS) {
    return showWarning("Giỏ hàng đầy", `Hệ thống chỉ nhận tối đa ${MAX_TOTAL_ITEMS} món trên một đơn đặt online. Bạn có thể gọi thêm khi đến quán!`);
  }

  // 3. Check giới hạn TỪNG MÓN (Như cũ)
  const MAX_QTY = getMaxQty(newItem.type);
  const existingIndex = cart.value.findIndex(
    (i) => i.id === newItem.id && i.type === newItem.type,
  );

  if (existingIndex !== -1) {
    const newTotal = cart.value[existingIndex].quantity + newItem.quantity;
    if (newTotal > MAX_QTY) {
      showWarning("Giới hạn món", `Nhà hàng chỉ nhận tối đa ${MAX_QTY} phần cho món này khi đặt online.`);
      cart.value[existingIndex].quantity = MAX_QTY;
    } else {
      cart.value[existingIndex].quantity = newTotal;
    }
  } else {
    if (newItem.quantity > MAX_QTY) {
      showWarning("Giới hạn món", `Nhà hàng chỉ nhận tối đa ${MAX_QTY} phần cho món này khi đặt online.`);
      newItem.quantity = MAX_QTY;
    }
    cart.value.push(newItem);
  }
};

const openCartModal = () => {
  if (cart.value.length > 0) isCartOpen.value = true;
};
const closeCartModal = () => (isCartOpen.value = false);

const increaseQty = (index) => {
  const item = cart.value[index];
  const MAX_QTY = getMaxQty(item.type);

  // Check 3 lớp bảo vệ khi bấm dấu + trong giỏ hàng
  if (totalPrice.value + item.price > MAX_TOTAL_PRICE) {
    return showWarning("Vượt hạn mức", `Tổng hóa đơn không được vượt quá ${formatPrice(MAX_TOTAL_PRICE)}.`);
  }
  
  if (totalCount.value + 1 > MAX_TOTAL_ITEMS) {
    return showWarning("Giỏ hàng đầy", `Bạn đã đạt giới hạn ${MAX_TOTAL_ITEMS} món trong giỏ hàng.`);
  }

  if (item.quantity < MAX_QTY) {
    item.quantity++;
  } else {
    showWarning("Giới hạn món", `Nhà hàng chỉ nhận đặt trước tối đa ${MAX_QTY} phần cho món này.`);
  }
};

const decreaseQty = (index) => {
  if (cart.value[index].quantity > 1) cart.value[index].quantity--;
  else removeItem(index);
};

const removeItem = (index) => {
  cart.value.splice(index, 1);
  if (cart.value.length === 0) closeCartModal();
};

const totalCount = computed(() =>
  cart.value.reduce((sum, item) => sum + item.quantity, 0),
);
const totalPrice = computed(() =>
  cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0),
);

watch(
  cart,
  (newCart) => {
    localStorage.setItem("cart", JSON.stringify(newCart));
  },
  { deep: true },
);

const navigateToBooking = () => {
  closeCartModal();
  router.push("/dat-ban");
};

// ==========================================
// D. UTILS & SCROLL SPY
// ==========================================
const formatPrice = (value) => {
  if (typeof value === "object" && value !== null && value.min !== undefined) {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value.min);
  }
  if (typeof value !== "number" || isNaN(value)) return "0 ₫";
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const getFilteredItems = (section) => {
  if (section.activeFilter === "all") return section.items;
  return section.items.filter((item) => item.groupId === section.activeFilter);
};

const selectFilter = (section, filterId) => {
  const originalSection = menuData.value.find(
    (s) => s.categoryId === section.categoryId,
  );
  if (originalSection) {
    originalSection.activeFilter = filterId;
  }
};
const allSearchItem = computed(() =>
  menuData.value ? menuData.value.flatMap((s) => s.items) : [],
);

const displayedMenu = computed(() => {
  const q = searchQuery.value.trim().toLowerCase();

  // 1. BƯỚC LỌC: Lọc theo tên món
  let filteredSections = menuData.value
    .map((section) => {
      const filteredItems = section.items.filter((item) => {
        return (
          item.name.toLowerCase().includes(q) ||
          (item.desc && item.desc.toLowerCase().includes(q))
        );
      });
      // QUAN TRỌNG: Trả về một bản copy hoàn toàn mới của mảng items
      // để bước sắp xếp bên dưới không làm hỏng dữ liệu gốc.
      return { ...section, items: [...filteredItems] };
    })
    .filter((section) => section.items.length > 0);

  // 2. BƯỚC SẮP XẾP: Sắp xếp món ăn BÊN TRONG từng danh mục
  if (sortOption.value !== "default") {
    filteredSections.forEach((section) => {
      section.items.sort((a, b) => {
        // Lấy giá trị để so sánh (nếu món có nhiều size thì lấy giá min)
        const priceA = typeof a.price === "object" ? a.price.min : a.price;
        const priceB = typeof b.price === "object" ? b.price.min : b.price;

        if (sortOption.value === "price_asc") {
          return priceA - priceB;
        } else if (sortOption.value === "price_desc") {
          return priceB - priceA;
        }
        return 0;
      });
    });
  }

  return filteredSections;
});

// Thêm hàm reset nút xoá tìm kiếm
const clearSearch = () => {
  searchQuery.value = "";
  sortOption.value = "default";
};

// Thêm một biến cờ để chặn sự kiện cuộn (scroll spy) khi người dùng đang click
let isClickScrolling = false;

const pageContainer = ref(null);

const scrollToCategory = (id) => {
  activeCategory.value = id;
  const el = document.getElementById(id);

  if (el) {
    isClickScrolling = true; // Bật cờ khóa Scroll Spy lại
    el.scrollIntoView({ behavior: "smooth", block: "start" });

    // Mở khóa sau khi cuộn xong (800ms thường là đủ cho animation smooth scroll)
    setTimeout(() => {
      isClickScrolling = false;
    }, 800);
  }
};

const onScroll = () => {
  // 1. Chặn tính toán lúc trang đang tự động trôi do click
  if (isClickScrolling) return;

  // 2. Đặt "vạch quét" ở vị trí 1/3 màn hình tính từ trên xuống (cực kỳ nhạy)
  const offset = window.innerHeight / 3;
  let currentActiveId = activeCategory.value;

  // 3. Duyệt mảng NGƯỢC (từ dưới lên trên)
  for (let i = categories.value.length - 1; i >= 0; i--) {
    const cat = categories.value[i];
    const el = document.getElementById(cat.id);

    if (el) {
      const rect = el.getBoundingClientRect();
      // Nếu phần đỉnh của mục này vượt qua vạch quét -> Bắt lấy nó!
      if (rect.top <= offset) {
        currentActiveId = cat.id;
        break; // Chốt đơn và thoát vòng lặp
      }
    }
  }

  // 4. Nếu danh mục đang xem thay đổi thì cập nhật UI
  if (currentActiveId && currentActiveId !== activeCategory.value) {
    activeCategory.value = currentActiveId;

    // Tính năng tự trôi thanh menu (Chỉ chạy trên Điện thoại)
    if (window.innerWidth < 768) {
      const navItem = document.getElementById(`nav-item-${currentActiveId}`);
      // Lấy thẻ div bọc ngoài thanh menu
      const navContainer = document.querySelector(".nav-scroll-container");

      if (navItem && navContainer) {
        // Tự tính toán vị trí để đưa thẻ đang chọn ra giữa màn hình
        const scrollPos =
          navItem.offsetLeft -
          navContainer.offsetWidth / 2 +
          navItem.offsetWidth / 2;

        // CHỈ CUỘN NGANG (Scroll X), tuyệt đối không đụng tới cuộn dọc
        navContainer.scrollTo({
          left: scrollPos,
          behavior: "smooth",
        });
      }
    }
  }
};

onMounted(() => {
  fetchData();
  window.addEventListener("scroll", onScroll, true);

  // KHI VÀO TRANG MENU, TỰ ĐỘNG KHÔI PHỤC GIỎ HÀNG TỪ LOCALSTORAGE
  const savedCart = localStorage.getItem("cart");
  if (savedCart) {
    try {
      cart.value = JSON.parse(savedCart);
    } catch (e) {
      console.error("Lỗi parse giỏ hàng:", e);
    }
  }
});
onUnmounted(() => {
  window.removeEventListener("scroll", onScroll, true);
});
</script>

<template>
  <div class="customer-menu-page" ref="pageContainer">
    <div v-if="isLoading" class="text-center py-5 mt-5">
      <div class="spinner-border text-primary-red" role="status"></div>
      <p class="mt-2 text-muted">Đang tải thực đơn...</p>
    </div>

    <div v-else class="container py-4">
      <div class="row">
        <div class="col-lg-3 col-md-4 mb-4">
          <div
            class="sidebar-wrapper sticky-top"
            style="top: 20px; z-index: 1000"
          >
            <div
              class="sidebar-header d-none d-md-flex align-items-center justify-content-center"
            >
              THỰC ĐƠN
            </div>

            <div class="nav-scroll-container">
              <ul class="nav-list sidebar-list">
                <li
                  v-for="cat in categories"
                  :key="cat.id"
                  :id="`nav-item-${cat.id}`"
                  class="nav-item"
                  :class="{ active: activeCategory === cat.id }"
                  @click="scrollToCategory(cat.id)"
                >
                  {{ cat.name }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="col-lg-9 col-md-8">
          <div
            class="search-filter-bar bg-white p-3 mb-4 rounded-4 border shadow-sm sticky-top"
            style="top: 20px; z-index: 999"
          >
            <div class="row g-2 align-items-center">
              <div class="col-lg-6 col-md-5 col-sm-12">
                <div class="input-group">
                  <span class="input-group-text bg-white border-end-0">
                    <i class="fas fa-search text-muted"></i>
                  </span>
                  <input
                    type="text"
                    class="form-control border-start-0 ps-0 shadow-none"
                    placeholder="Tìm tên món, mô tả..."
                    v-model="searchQuery"
                  />
                </div>
              </div>

              <div class="col-lg-4 col-md-5 col-sm-9 col-10">
                <div class="input-group">
                  <span class="input-group-text bg-light text-muted">
                    <i class="fas fa-sort-amount-down"></i>
                  </span>
                  <select
                    class="form-select shadow-none fw-semibold"
                    v-model="sortOption"
                    style="color: #495057"
                  >
                    <option value="default">Sắp xếp mặc định</option>
                    <option value="price_asc">Giá: Thấp đến Cao</option>
                    <option value="price_desc">Giá: Cao đến Thấp</option>
                  </select>
                </div>
              </div>

              <div class="col-lg-2 col-md-2 col-sm-3 col-2 text-end">
                <button
                  class="btn btn-light border text-muted w-100"
                  @click="clearSearch"
                  title="Xóa bộ lọc"
                >
                  <i class="fas fa-sync-alt"></i>
                </button>
              </div>
            </div>
          </div>

          <div class="main-content">
            <div
              v-if="displayedMenu.length === 0 && searchQuery"
              class="text-center py-5 mt-4 text-muted"
            >
              <i class="fas fa-search fa-3x mb-3" style="color: #ddd"></i>
              <h5>Không tìm thấy món "{{ searchQuery }}"</h5>
            </div>

            <div
              v-for="section in displayedMenu"
              :key="section.categoryId"
              :id="section.categoryId"
              class="menu-section mb-5"
            >
              <h3 class="section-title">{{ section.categoryName }}</h3>

              <div
                class="filter-bar mb-3"
                v-if="section.filters && section.filters.length > 1"
              >
                <div class="filter-scroll-wrapper">
                  <button
                    v-for="filter in section.filters"
                    :key="filter.id"
                    class="btn-filter"
                    :class="{ active: section.activeFilter === filter.id }"
                    @click="selectFilter(section, filter.id)"
                  >
                    {{ filter.name }}
                  </button>
                </div>
              </div>

              <TransitionGroup name="filter-list" tag="div" class="row g-3">
                <div
                  v-for="item in getFilteredItems(section)"
                  :key="item.id"
                  :class="
                    section.categoryId === 'combo-set'
                      ? 'col-md-4 col-lg-3 col-6'
                      : 'col-md-4 col-lg-3 col-6'
                  "
                >
                  <div class="food-card" @click="handleItemClick(item)">
                    <div
                      class="card-img-wrapper"
                      :class="{
                        'square-ratio': section.categoryId !== 'combo-set',
                      }"
                    >
                      <img
                        v-if="item.image"
                        :src="item.image"
                        :alt="item.name"
                        loading="lazy"
                        @error="handleImageError"
                      />
                      <div
                        class="fallback-icon"
                        :style="{ display: item.image ? 'none' : 'flex' }"
                      >
                        <i class="fas fa-utensils fa-3x"></i>
                      </div>
                      <div
                        v-if="section.categoryId === 'combo-set'"
                        class="combo-badge"
                      >
                        HOT
                      </div>
                    </div>

                    <div class="card-body">
                      <div v-if="item.subCategoryName" class="sub-cat-name">
                        {{ item.subCategoryName }}
                      </div>
                      <h5
                        class="food-name"
                        :class="{
                          'text-small': section.categoryId !== 'combo-set',
                        }"
                      >
                        {{ item.name }}
                      </h5>
                      <p
                        v-if="section.categoryId === 'combo-set'"
                        class="food-desc"
                      >
                        {{ item.desc }}
                      </p>

                      <div class="card-footer-action">
                        <span
                          class="food-price"
                          :class="
                            section.categoryId === 'combo-set'
                              ? 'food-price-lg'
                              : ''
                          "
                        >
                          {{
                            item.isRange
                              ? `${formatPrice(item.price.min)} +`
                              : formatPrice(item.price)
                          }}
                        </span>
                        <button
                          class="btn-add"
                          @click.stop="handleItemClick(item)"
                        >
                          <i class="fas fa-plus"></i>
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </TransitionGroup>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      class="cart-wrapper-fixed"
      v-if="cart.length > 0"
      @mouseenter="isCartHover = true"
      @mouseleave="isCartHover = false"
    >
      <Transition name="fade-up">
        <div v-if="isCartHover" class="cart-preview-popup">
          <div class="preview-header">Đang chọn {{ totalCount }} món</div>
          <div class="preview-list">
            <div
              v-for="(item, index) in cart.slice().reverse().slice(0, 3)"
              :key="index"
              class="preview-item"
            >
              <span class="p-name">{{ item.quantity }}x {{ item.name }}</span>
              <span class="p-price">{{
                formatPrice(item.price * item.quantity)
              }}</span>
            </div>
            <div
              v-if="cart.length > 3"
              class="preview-more text-center text-muted small"
            >
              ...và {{ cart.length - 3 }} món khác
            </div>
          </div>
          <div class="preview-arrow"></div>
        </div>
      </Transition>

      <div class="floating-cart" @click="openCartModal">
        <div
          class="cart-info"
          style="display: flex; align-items: center; gap: 0.5em"
        >
          <span class="cart-count"
            ><i class="fas fa-shopping-cart me-1"></i>
            {{ totalCount }} món</span
          >
          <span class="cart-total">{{ formatPrice(totalPrice) }}</span>
        </div>
        <button class="btn-view-cart">
          Chi tiết <i class="fas fa-chevron-up ms-1"></i>
        </button>
      </div>
    </div>

    <Transition name="modal-fade">
      <div
        v-if="isCartOpen"
        class="cart-modal-overlay"
        @click.self="closeCartModal"
      >
        <div class="cart-modal-content">
          <div class="modal-header">
            <h5>
              Giỏ hàng
              <span class="badge bg-danger rounded-pill">{{ totalCount }}</span>
            </h5>
            <button class="btn-close-modal" @click="closeCartModal">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="modal-body custom-scrollbar">
            <div
              v-for="(item, index) in cart"
              :key="index"
              class="cart-item-row"
            >
              <div class="item-img">
                <img
                  v-if="item.image"
                  :src="item.image"
                  :alt="item.name"
                  @error="handleImageError"
                />
                <div
                  class="fallback-icon"
                  :style="{ display: item.image ? 'none' : 'flex' }"
                >
                  <i class="fas fa-utensils"></i>
                </div>
              </div>
              <div class="item-details">
                <h6 class="item-name">{{ item.name }}</h6>
                <div class="item-price-unit">{{ formatPrice(item.price) }}</div>
              </div>
              <div class="item-actions">
                <div class="qty-control">
                  <button @click="decreaseQty(index)">-</button>
                  <span>{{ item.quantity }}</span>
                  <button @click="increaseQty(index)">+</button>
                </div>
                <button class="btn-remove" @click="removeItem(index)">
                  <i class="fas fa-trash-alt"></i>
                </button>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <div
              class="d-flex justify-content-between align-items-center w-100"
            >
              <span class="fw-bold text-dark fs-5"
                >Tổng:
                <span class="text-danger">{{
                  formatPrice(totalPrice)
                }}</span></span
              >
              <button class="btn-checkout" @click="navigateToBooking">
                ĐẾN TRANG ĐẶT BÀN
              </button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <Transition name="modal-fade">
      <div
        v-if="isDetailModalOpen"
        class="product-modal-overlay"
        @click.self="closeDetailModal"
      >
        <div class="product-modal-content">
          <button class="btn-close-product" @click="closeDetailModal">
            <i class="fas fa-times"></i>
          </button>
          <div class="product-header">
            <div class="product-img">
              <img
                v-if="selectedProduct?.image"
                :src="selectedProduct?.image"
                :alt="selectedProduct?.name"
                @error="handleImageError"
              />
              <div
                class="fallback-icon"
                :style="{ display: selectedProduct?.image ? 'none' : 'flex' }"
              >
                <i class="fas fa-image fa-4x"></i>
              </div>
            </div>
            <div class="product-info-basic">
              <h4>{{ selectedProduct?.name }}</h4>
              <p class="text-muted small mb-0">
                {{ selectedProduct?.desc || "Món ngon mỗi ngày" }}
              </p>
            </div>
          </div>
          <div class="product-body custom-scrollbar">
            <div class="option-group">
              <label class="option-title">Chọn phân loại:</label>
              <div class="option-list">
                <label
                  v-for="variant in currentVariants"
                  :key="variant.id"
                  class="option-item"
                  :class="{ active: selectedVariantId === variant.id }"
                >
                  <input
                    type="radio"
                    :value="variant.id"
                    v-model="selectedVariantId"
                    hidden
                  />
                  <span class="opt-name">{{ variant.name }}</span>
                  <span class="opt-price">{{
                    formatPrice(variant.price)
                  }}</span>
                </label>
              </div>
            </div>
          </div>
          <div class="product-footer">
            <div class="qty-selector">
              <button
                @click="modalQuantity > 1 ? modalQuantity-- : null"
                :disabled="modalQuantity <= 1"
              >
                -
              </button>
              <span>{{ modalQuantity }}</span>
              <button
                @click="
                  modalQuantity < getMaxQty(selectedProduct?.type)
                    ? modalQuantity++
                    : null
                "
                :disabled="modalQuantity >= getMaxQty(selectedProduct?.type)"
              >
                +
              </button>
            </div>
            <button class="btn-confirm-add" @click="confirmAddToCart">
              Thêm -
              {{
                formatPrice(
                  currentVariants.find((v) => v.id === selectedVariantId)
                    ?.price * modalQuantity,
                )
              }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
@import url("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css");

:root {
  --primary-red: #7d161a;
  --text-dark: #333;
}

.text-primary-red {
  color: #7d161a !important;
}

.customer-menu-page {
  background-color: #f9f9f9;
  min-height: 100vh;
  padding-bottom: 90px;
}

.menu-section {
  scroll-margin-top: 20px;
}

/* NAV & SEARCH */
.sidebar-header {
  background-color: #ba2c26; /* Màu đỏ đô */
  color: white;
  font-size: 1.2rem;
  font-weight: bold;
  padding: 12px 0;
  border: 2px solid #8c1914;
  margin-bottom: 20px;
  position: relative;
}

/* Icon viền 2 góc cho giống Long Wang */
.sidebar-header::before,
.sidebar-header::after {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid white;
}
.sidebar-header::before {
  top: 4px;
  left: 4px;
  border-right: none;
  border-bottom: none;
}
.sidebar-header::after {
  bottom: 4px;
  right: 4px;
  border-left: none;
  border-top: none;
}

.sidebar-list {
  display: flex !important;
  flex-direction: row !important; /* Mobile sẽ cuộn ngang */
  flex-wrap: nowrap !important;
  list-style: none;
  padding: 0;
  margin: 0;
}

/* KHI LÊN MÀN HÌNH MÁY TÍNH (DESKTOP) SẼ DỰNG DỌC MENU */
/* --- STYLE CHO NAV ITEM (Dùng chung) --- */
.nav-item {
  cursor: pointer;
  padding: 10px 15px;
  color: #666;
  font-weight: 600;
  transition: all 0.3s ease;
  user-select: none; /* Tránh bôi đen text khi click liên tục */
}

/* Hiệu ứng hover chung */
.nav-item:hover {
  color: #7d161a;
  background-color: #fdf2f2;
}

/* --- MOBILE (Màn hình nhỏ cuộn ngang) --- */
@media (max-width: 767px) {
  .nav-item {
    white-space: nowrap;
    border-radius: 20px; /* Bo tròn giống dạng viên thuốc */
    margin-right: 10px;
    border: 1px solid #eaeaea;
  }

  /* Đang chọn trên mobile (Nền đỏ chữ trắng) */
  .nav-item.active {
    background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%);
    color: white;
    border-color: transparent;
    box-shadow: 0 4px 10px rgba(125, 22, 26, 0.2);
  }
}

/* --- DESKTOP (Màn hình lớn dựng dọc) --- */
@media (min-width: 768px) {
  .sidebar-list {
    flex-direction: column !important;
  }

  .nav-item {
    border-bottom: 1px solid #eaeaea;
    padding: 15px 15px;
    white-space: normal;
    border-left: 4px solid transparent; /* Tạo sẵn viền trái tàng hình để lúc active không bị giật */
  }

  /* Đang chọn trên desktop (Nền hồng nhạt, vạch đỏ bên trái) */
  .nav-item.active {
    background: #fdf2f2;
    color: #7d161a;
    font-weight: 700;
    border-left: 4px solid #7d161a;
    border-bottom: 1px solid #eaeaea;
  }

  .nav-scroll-container {
    overflow-x: hidden;
  }
}

/* FOOD CARDS */
.section-title {
  color: #7d161a;
  font-weight: 800;
  font-size: 1.4rem;
  margin-bottom: 20px;
  border-left: 5px solid #7d161a;
  padding-left: 10px;
  text-transform: uppercase;
}

.filter-scroll-wrapper {
  display: flex;
  overflow-x: auto;
  gap: 8px;
  padding: 5px 0;
  scrollbar-width: none;
}

.filter-scroll-wrapper::-webkit-scrollbar {
  display: none;
}

.btn-filter {
  background: #fff;
  border: 1px solid #eaeaea;
  color: #666;
  padding: 8px 20px;
  border-radius: 30px;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.02);
}

.btn-filter:hover {
  background: #fdf2f2;
  color: #7d161a;
  border-color: #fbdada;
}

.btn-filter.active {
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 12px rgba(125, 22, 26, 0.25);
}

.food-card {
  background: #ffffff;
  border-radius: 20px; /* Bo góc mềm mại hơn */
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.04); /* Đổ bóng mịn màng */
  height: 100%;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(0, 0, 0, 0.02);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); /* Hiệu ứng mượt hơn */
}

.food-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(125, 22, 26, 0.15); /* Bóng đổ màu đỏ nhạt tạo điểm nhấn */
  border-color: rgba(125, 22, 26, 0.1);
}

.card-img-wrapper {
  position: relative;
  padding-top: 66%;
  overflow: hidden;
}

.card-img-wrapper.square-ratio {
  padding-top: 100%;
}

.card-img-wrapper img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: 0.5s;
}

.food-card:hover img {
  transform: scale(1.05);
}

.combo-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: #d32f2f;
  color: white;
  font-size: 11px;
  font-weight: 800;
  padding: 4px 10px;
  border-radius: 20px;
}

.card-body {
  padding: 12px;
  display: flex;
  flex-direction: column;
  flex-grow: 1;
}

.sub-cat-name {
  font-size: 11px;
  color: #999;
  text-transform: uppercase;
  font-weight: 600;
  margin-bottom: 2px;
}

.food-name {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 40px;
}

.food-name.text-small {
  font-size: 14px;
  min-height: 36px;
}

.food-desc {
  font-size: 12px;
  color: #888;
  margin-bottom: 10px;
  flex-grow: 1;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.card-footer-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
}

.food-price {
  font-weight: 800;
  color: #7d161a;
  font-size: 13px;
}

.food-price-lg {
  font-size: 16px !important;
}

.btn-add {
  background: #fff;
  border: 2px solid #f0f0f0; /* Viền nhạt hơn */
  border-radius: 50%;
  width: 36px; /* To hơn chút để dễ bấm */
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7d161a;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

.btn-add:hover {
  background: linear-gradient(
    135deg,
    #7d161a 0%,
    #d32f2f 100%
  ); /* Đổi màu nền thành dải màu xịn xò */
  border-color: transparent;
  color: white;
  transform: rotate(90deg) scale(1.1); /* To ra xíu và xoay */
  box-shadow: 0 4px 10px rgba(125, 22, 26, 0.3);
}

/* FLOATING CART & PREVIEW */
.cart-wrapper-fixed {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 500px;
  z-index: 1100;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.floating-cart {
  position: relative;
  width: 100%;
  background: #7d161a;
  color: white;
  border-radius: 50px;
  padding: 10px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 25px rgba(125, 22, 26, 0.4);
  cursor: pointer;
  border: 2px solid rgba(255, 255, 255, 0.2);
  transition: 0.2s;
}

.floating-cart:active {
  transform: scale(0.98);
}

.cart-count {
  font-size: 12px;
  opacity: 0.9;
  font-weight: 500;
}

.cart-total {
  font-weight: 800;
  font-size: 16px;
  color: #fff;
}

.btn-view-cart {
  background: white;
  border: none;
  color: #7d161a;
  font-weight: bold;
  font-size: 14px;
  padding: 8px 16px;
  border-radius: 20px;
}

.cart-preview-popup {
  position: absolute;
  bottom: 100%;
  margin-bottom: 15px;
  width: 100%;
  background: white;
  border-radius: 12px;
  box-shadow: 0 5px 20px rgba(0, 0, 0, 0.15);
  padding: 12px;
  border: 1px solid #eee;
  z-index: 1090;
}

.preview-header {
  font-size: 12px;
  font-weight: 700;
  color: #999;
  text-transform: uppercase;
  margin-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 4px;
}

.preview-item {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
  color: #333;
}

.preview-item .p-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 70%;
}

.preview-item .p-price {
  font-weight: 700;
  color: #7d161a;
}

.preview-arrow {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  width: 12px;
  height: 12px;
  background: white;
  border-bottom: 1px solid #eee;
  border-right: 1px solid #eee;
  transform: translateX(-50%) rotate(45deg);
}

/* CART MODAL (LARGE) */
.cart-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.7);
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
}

.cart-modal-content {
  background: white;
  width: 95%;
  max-width: 900px;
  height: 90vh;
  border-radius: 16px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  animation: zoomIn 0.3s;
}

@keyframes zoomIn {
  from {
    transform: scale(0.95);
    opacity: 0;
  }

  to {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-header {
  padding: 20px 25px;
  background: #f8f9fa;
  border-bottom: 1px solid #e0e0e0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h5 {
  margin: 0;
  font-weight: 700;
  color: #333;
  font-size: 1.25rem;
}

.btn-close-modal {
  background: #f1f1f1;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: 0.2s;
}

.btn-close-modal:hover {
  background: #e0e0e0;
}

.modal-body {
  flex: 1;
  overflow-y: auto;
  padding: 25px;
  background: #fff;
}

.cart-item-row {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 12px;
  transition: background 0.2s;
}

.cart-item-row:hover {
  background: #fcfcfc;
  border-color: #ddd;
}

.item-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  overflow: hidden;
  margin-right: 20px;
  flex-shrink: 0;
}

.item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-right: 10px;
}

.item-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
  line-height: 1.3;
}

.item-price-unit {
  font-size: 15px;
  color: #7d161a;
  font-weight: 700;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

@media (min-width: 768px) {
  .cart-item-row {
    justify-content: space-between;
  }

  .item-details {
    margin-right: 40px;
  }

  .item-actions {
    flex-direction: row;
  }
}

.qty-control {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 6px;
  padding: 4px;
  border: 1px solid #ddd;
}

.qty-control button {
  width: 32px;
  height: 32px;
  border: none;
  background: white;
  border-radius: 4px;
  font-weight: bold;
  color: #333;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  font-size: 16px;
}

.qty-control span {
  width: 40px;
  text-align: center;
  font-size: 15px;
  font-weight: 600;
}

.btn-remove {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff0f0;
  color: #dc3545;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
  transition: 0.2s;
}

.btn-remove:hover {
  background: #dc3545;
  color: white;
}

.modal-footer {
  padding: 25px;
  background: #f8f9fa;
  border-top: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

@media (min-width: 768px) {
  .modal-footer {
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }

  .btn-checkout {
    width: 300px;
  }
}

.btn-checkout {
  background: linear-gradient(135deg, #7d161a 0%, #d32f2f 100%) !important;
  color: white;
  border: none;
  padding: 16px;
  border-radius: 12px;
  font-weight: 800;
  font-size: 18px;
  letter-spacing: 1px;
  cursor: pointer;
  transition: 0.3s;
  box-shadow: 0 4px 15px rgba(125, 22, 26, 0.3);
  text-transform: uppercase;
}

.btn-checkout:hover {
  background: #5e1013;
  transform: translateY(-2px);
}

/* PRODUCT DETAIL MODAL */
.product-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  z-index: 2100;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(3px);
}

.product-modal-content {
  background: white;
  width: 90%;
  max-width: 500px;
  min-height: 450px; /* Thêm dòng này để Modal không bị xẹp lại */
  background-color: #fff;
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  max-height: 85vh;
  position: relative;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }

  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.btn-close-product {
  position: absolute;
  top: 15px;
  right: 15px;
  width: 32px;
  height: 32px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  z-index: 10;
  cursor: pointer;
  backdrop-filter: blur(4px);
}

.product-img {
  width: 100%;
  height: 200px;
  overflow: hidden;
  position: relative;
}

.product-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info-basic {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.product-info-basic h4 {
  margin: 0;
  font-weight: 800;
  color: #333;
  font-size: 1.2rem;
}

.product-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1 1 auto;
  min-height: 150px;
}

.option-title {
  font-size: 14px;
  font-weight: 700;
  color: #555;
  margin-bottom: 10px;
  display: block;
}

.option-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.option-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
  user-select: none;
}

.option-item:hover {
  background: #fafafa;
}

.option-item.active {
  border-color: #7d161a;
  background: #fdf2f2;
  color: #7d161a;
}

.opt-name {
  font-weight: 600;
  font-size: 14px;
}

.opt-price {
  font-weight: 700;
  font-size: 14px;
}

.product-footer {
  padding: 15px 20px;
  border-top: 1px solid #eee;
  background: white;
  display: flex;
  gap: 15px;
  align-items: center;
}

.qty-selector {
  display: flex;
  align-items: center;
  border: 1px solid #ddd;
  border-radius: 8px;
  height: 44px;
}

.qty-selector button {
  width: 36px;
  height: 100%;
  background: none;
  border: none;
  font-weight: bold;
  font-size: 18px;
  color: #333;
  cursor: pointer;
}

.qty-selector button:disabled {
  color: #ccc;
  cursor: not-allowed;
}

.qty-selector span {
  width: 30px;
  text-align: center;
  font-weight: 600;
}

.btn-confirm-add {
  flex: 1;
  background: #7d161a;
  color: white;
  border: none;
  height: 44px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 15px;
  cursor: pointer;
  transition: 0.2s;
  box-shadow: 0 4px 10px rgba(125, 22, 26, 0.2);
}

.btn-confirm-add:hover {
  background: #5e1013;
  transform: translateY(-2px);
}

/* TRANSITIONS */
.fade-up-enter-active,
.fade-up-leave-active {
  transition: all 0.3s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.filter-list-enter-active,
.filter-list-leave-active {
  transition: all 0.5s cubic-bezier(0.55, 0, 0.1, 1);
}

.filter-list-enter-from,
.filter-list-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(30px);
}

.filter-list-leave-active {
  position: absolute;
  z-index: -1;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 4px;
}

.fallback-icon {
  width: 100%;
  height: 100%;
  position: absolute;
  top: 0;
  left: 0;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ced4da;
}
.search-img-box .fallback-icon,
.item-img .fallback-icon {
  position: relative; /* Ghi đè cho thẻ nhỏ */
}
.product-header,
.product-footer {
  flex-shrink: 0;
}
.search-filter-bar {
  background: white;
  padding: 12px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  border: 1px solid #eee;
}

.nav-scroll-container {
  overflow-x: auto; /* Cho phép cuộn ngang */
  scroll-behavior: smooth;
  /* Ẩn thanh cuộn xấu xí đi cho giống app */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE / Edge */
}

/* Ẩn thanh cuộn trên Chrome/Safari */
.nav-scroll-container::-webkit-scrollbar {
  display: none;
}
</style>
