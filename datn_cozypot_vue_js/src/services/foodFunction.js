import { computed, nextTick, onMounted, ref, shallowRef, watch } from 'vue';

import MenuFood from '../pages/admin/food/screens/FoodManageGeneral.vue';
import MenuHotpot from '../pages/admin/food/screens/FoodHotPotSetGeneral.vue';
import FoodDetailManageGeneral from '../pages/admin/food/screens/FoodDetailManageGeneral.vue';
import axios from 'axios';
import CategoryGeneral from '../pages/admin/category/screens/CategoryGeneral.vue';
import CategoryDetailGeneral from '../pages/admin/category/screens/CategoryDetailGeneral.vue';
import CategoryHotpotGeneral from '../pages/admin/category/screens/CategoryHotpotGeneral.vue';
import { useRoute, useRouter } from 'vue-router';
import Swal from 'sweetalert2';
import * as XLSX from 'xlsx';
import axiosClient from './axiosClient';

const API_BASE_EMPLOYEE = "/manage";

export function getAllFoodGeneral() {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food`);
}

export function getFoodById(id) {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/${id}`);
};

export function getFoodGeneralModalById(id) {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/modal/${id}`);
}

export function getAllHotpotGeneral() {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/hotpotGeneral`);
}

export function getHotpotById(id) {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/hotpotGeneral/${id}`);
}

export function getAllFoodDetail() {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/foodDetail`);
}

export function getFoodDetailById(id) {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/foodDetail/${id}`);
}

export function getAllCategory() {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/category`);
}

export function getAllCategoryDetail() {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/category/detail`);
}

export function getAllCategoryHotpot() {
    return axiosClient.get(`${API_BASE_EMPLOYEE}/food/category/hotpotType`);
}

export function postNewFood(data) {
    return axiosClient.post(`${API_BASE_EMPLOYEE}/food`, data);
}

export function putNewFood(id, data) {
    return axiosClient.put(`${API_BASE_EMPLOYEE}/food/${id}`, data);
}

export function putNewFoodDetail(id, data) {
    return axiosClient.put(`${API_BASE_EMPLOYEE}/food/foodDetail/${id}`, data);
}

export function postNewHotpot(data) {
    return axiosClient.post(`${API_BASE_EMPLOYEE}/food/hotpotGeneral`, data);
}

export function putNewCategory(id, data) {
    return axiosClient.put(`${API_BASE_EMPLOYEE}/food/category/${id}`, data);
}

export function postNewFoodDetail(data) {
    return axiosClient.post(`${API_BASE_EMPLOYEE}/food/foodDetail`, data);
}
export function putNewHotpot(id, data) {
    return axiosClient.put(`${API_BASE_EMPLOYEE}/food/hotpotGeneral/${id}`, data);
}
export function postNewCategory(data) {
    return axiosClient.post(`${API_BASE_EMPLOYEE}/food/category`, data);
}

export function putNewHotpotCategory(id, data) {
    return axiosClient.put(`${API_BASE_EMPLOYEE}/food/category/hotpotType/${id}`, data);
}

export function putCategoryDetail(id, data) {
    return axiosClient.put(`${API_BASE_EMPLOYEE}/food/category/detail/${id}`, data);
}

export function postCategoryDetail(data) {
    return axiosClient.post(`${API_BASE_EMPLOYEE}/food/category/detail`, data);
}

export function postNewCategoryDetail(data) {
    return axiosClient.post(`${API_BASE_EMPLOYEE}/food/category/detail`, data);
}

export function postNewHotpotCategory(data) {
    return axiosClient.post(`${API_BASE_EMPLOYEE}/food/category/hotpotType`, data);
}



//Trang chính food manager
export function useTabManager() {
    const tabs = {
        'thucdon': MenuFood,
        'setlau': MenuHotpot,
        'chitietTD': FoodDetailManageGeneral
    };

    const currentTabName = ref('thucdon');
    const currentComponent = shallowRef(MenuFood);

    const changeTab = (tabName) => {
        currentTabName.value = tabName;
        currentComponent.value = tabs[tabName];
    };

    return {
        currentTabName,
        currentComponent,
        changeTab
    };
}

export function useFoodDetailManager() {
    const route = useRoute();
    const detailData = ref([]);

    const searchQuery = ref('');
    const currentPage = ref(1);
    const itemsPerPage = ref(5);
    const sortOption = ref('id_desc');
    const statusFilter = ref('all');

    // Filter states
    const categoryFilter = ref(null);
    const foodFilter = ref(null);     
    const hotpotFilter = ref(null);

    // Data Lists
    const listCategories = ref([]); 
    const listFoods = ref([]);      
    const listHotpotSets = ref([]); 

    const {
        selectedPriceRange,
        globalMinPrice,
        globalMaxPrice,
        calculatePriceLimits,
        resetPriceFilter
    } = usePriceFilter();

    // 1. Fetch Data
    const fetchFilterData = async () => {
        try {
            const [catRes, foodRes, hotpotRes] = await Promise.all([
                getAllCategory(),
                getAllFoodGeneral(),
                getAllHotpotGeneral()
            ]);
            listCategories.value = catRes.data || [];
            listFoods.value = foodRes.data || [];
            listHotpotSets.value = hotpotRes.data || [];
            console.log("Check Set Lẩu đầu tiên:", listHotpotSets.value[0]);
        } catch (e) {
            console.error("Lỗi data:", e);
        }
    };

    // 2. Main Filtering Logic
    const filteredData = computed(() => {
        let result = [...detailData.value];

        // Search
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item =>
                (item.tenChiTietMonAn || '').toLowerCase().includes(query) ||
                (item.maChiTietMonAn || '').toLowerCase().includes(query)
            );
        }

        // Status
        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value);
            result = result.filter(item => item.trangThai === statusValue);
        }

        // --- FILTER: DANH MỤC ---
        if (categoryFilter.value) {
            const filterVal = String(categoryFilter.value);
            result = result.filter(item => {
                const catId = item.monAnDiKem?.danhMuc?.id || 
                              item.monAnDiKem?.idDanhMuc || 
                              item.danhMuc?.id;
                return catId != null && String(catId) === filterVal;
            });
        }

        // --- FILTER: MÓN ĂN ---
        if (foodFilter.value) {
            const filterVal = String(foodFilter.value);
            result = result.filter(item => {
                if (item.idSetLau || item.setLau) return false;

                const parentId = item.monAnDiKem?.id || item.idMonAnDiKem;
                return parentId != null && String(parentId) === filterVal;
            });
        }

        // --- FILTER: SET LẨU ---
        if (hotpotFilter.value) {
            const filterVal = String(hotpotFilter.value);
            
            // Tìm Set đang chọn
            const selectedSet = listHotpotSets.value.find(s => String(s.id) === filterVal);

            if (selectedSet) {
                // Lấy danh sách con
                const listChildren = selectedSet.listChiTietSetLau || [];
                
                // Debug: Xem cấu trúc con nó là gì để map cho đúng
                if (listChildren.length > 0) {
                    console.log("Mẫu 1 món trong Set:", listChildren[0]);
                }

                // Map lấy ID. Ưu tiên các trường có khả năng là ID món ăn nhất
                const validIds = listChildren.map(child => {
                    return String(
                        child.idChiTietMonAn || 
                        child.chiTietMonAn?.id || 
                        child.monAnChiTietId || 
                        child.id // Cẩn thận: id này có thể là id của bảng nối, không phải id món
                    );
                });

                console.log("Danh sách ID lọc được:", validIds);

                // Lọc bảng chính
                result = result.filter(item => validIds.includes(String(item.id)));
            } else {
                result = [];
            }
        }

        // Price
        if (selectedPriceRange.value && selectedPriceRange.value.length === 2) {
            const [min, max] = selectedPriceRange.value;
            result = result.filter(item => {
                const price = parseFloat(item.giaBan) || 0;
                return price >= min && price <= max;
            });
        }

        // Sort
        return result.sort((a, b) => {
            const priceA = parseFloat(a.giaBan) || 0;
            const priceB = parseFloat(b.giaBan) || 0;
            const nameA = (a.tenChiTietMonAn || '').toLowerCase();
            const nameB = (b.tenChiTietMonAn || '').toLowerCase();

            switch (sortOption.value) {
                case 'price_asc': return priceA - priceB;
                case 'price_desc': return priceB - priceA;
                case 'name_asc': return nameA.localeCompare(nameB);
                case 'id_desc': return b.id - a.id;
                case 'id_asc': default: return a.id - b.id;
            }
        });
    });

    watch([searchQuery, statusFilter, sortOption, selectedPriceRange, categoryFilter, foodFilter, hotpotFilter], () => {
        currentPage.value = 1;
    });

    // 3. Dropdown Data Logic
    const availableFoods = computed(() => {
        if (!categoryFilter.value) return listFoods.value;
        const filterVal = String(categoryFilter.value);
        return listFoods.value.filter(f => {
            const catId = f.danhMuc?.id || f.idDanhMuc;
            return catId != null && String(catId) === filterVal;
        });
    });

    // --- FIX: Available Hotpots ---
    // Vì dữ liệu Set lẩu không có idDanhMuc (theo log), ta trả về toàn bộ danh sách
    // để tránh việc chọn Danh mục xong thì mất hết Set lẩu.
    const availableHotpots = computed(() => {
        return listHotpotSets.value;
    });

    // Pagination & Helpers
    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });
    const totalElements = computed(() => filteredData.value.length);
    const visiblePages = computed(() => { /* ... giữ nguyên code cũ ... */ 
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });
    
    const changePage = (page) => { if (page >= 1 && page <= totalPages.value) currentPage.value = page; };
    const handleMinChange = () => { /* ... giữ nguyên ... */ };
    const handleMaxChange = () => { /* ... giữ nguyên ... */ };

    function getAllFoodDetails() {
        return getAllFoodDetail().then(res => {
             detailData.value = res.data || [];
             calculatePriceLimits(detailData.value);

             const itemInSet = detailData.value.find(x => 
                 (x.listSetLau && x.listSetLau.length > 0) || 
                 (x.setLaus && x.setLaus.length > 0) ||
                 (x.chiTietSetLaus && x.chiTietSetLaus.length > 0)
             );
             console.log("Mẫu dữ liệu N-N:", itemInSet ? itemInSet : "Không tìm thấy món nào có thông tin Set Lẩu đi kèm");
        }).catch(console.error);
    }

    onMounted(async () => { 
        await Promise.all([ getAllFoodDetails(), fetchFilterData() ]);
        if (route.query.preCategory) categoryFilter.value = isNaN(Number(route.query.preCategory)) ? route.query.preCategory : Number(route.query.preCategory);
        if (route.query.preFood) {
            await nextTick();
            foodFilter.value = isNaN(Number(route.query.preFood)) ? route.query.preFood : Number(route.query.preFood);
        }
        if (route.query.preHotpot) {
            await nextTick();
            const hpVal = route.query.preHotpot;
            
            hotpotFilter.value = isNaN(Number(hpVal)) ? hpVal : Number(hpVal);
            
            foodFilter.value = null; 
        }
    });


    // Modal & Actions
    const isModalOpen = ref(false);
    const selectedDetail = ref(null);
    const isAddModalOpen = ref(false);
    const openEditModal = (item) => { selectedDetail.value = item; isModalOpen.value = true; };
    const handleSaveData = () => { isModalOpen.value = false; };
    const handleToggleStatus = async (item) => { 
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = { ...item, trangThai: newStatus };
            if (item.monAnDiKem?.id) payload.idMonAnDiKem = item.monAnDiKem.id;
            await putNewFoodDetail(item.id, payload);
        } catch (error) {
            item.trangThai = oldStatus;
        }
    };

    const clearFilters = () => {
        searchQuery.value = '';
        statusFilter.value = 'all';
        sortOption.value = 'id_asc';
        categoryFilter.value = null;
        foodFilter.value = null;
        hotpotFilter.value = null;
        resetPriceFilter();
    };

    watch(foodFilter, (newVal) => {
        if (newVal !== null && newVal !== undefined && newVal !== '') {
            hotpotFilter.value = null;
        }
    });

    watch(hotpotFilter, (newVal) => {
        if (newVal !== null && newVal !== undefined && newVal !== '') {
            foodFilter.value = null;
        }
    });

    const exportToExcel = () => {
        const dataToExport = filteredData.value.map((item, index) => ({
            'STT': index + 1,
            'Mã Chi Tiết': item.maChiTietMonAn,
            'Tên Chi Tiết': item.tenChiTietMonAn,
            'Giá Bán': item.giaBan,
            'Trạng Thái': item.trangThai === 1 ? 'Hoạt động' : 'Ngưng'
        }));
        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "ChiTietMon");
        XLSX.writeFile(wb, "Danh_Sach_Chi_Tiet.xlsx");
    };

    return {
        detailData, searchQuery, paginatedData, currentPage, totalPages, visiblePages, itemsPerPage, changePage, totalElements,
        isModalOpen, isAddModalOpen, selectedDetail, getAllFoodDetails, openEditModal, handleSaveData, handleToggleStatus,
        sortOption, statusFilter, selectedPriceRange, globalMinPrice, globalMaxPrice, clearFilters, handleMinChange, handleMaxChange,
        categoryFilter, foodFilter, hotpotFilter,
        listCategories, availableFoods, availableHotpots, exportToExcel
    };
}

export function usePriceFilter() {
    const globalMinPrice = ref(0);
    const globalMaxPrice = ref(1000000); // Mặc định
    const selectedPriceRange = ref([0, 1000000]);

    const calculatePriceLimits = (dataList) => {
        if (!dataList || dataList.length === 0) return;
        let min = Infinity;
        let max = -Infinity;

        dataList.forEach(item => {
            const pMin = item.giaThapNhat > 0 ? item.giaThapNhat : item.giaBan;
            const pMax = item.giaCaoNhat > 0 ? item.giaCaoNhat : item.giaBan;
            if (pMin < min) min = pMin;
            if (pMax > max) max = pMax;
        });

        if (min === Infinity) min = 0;
        if (max === -Infinity) max = 0;

        // Làm tròn cho đẹp (xuống 0, lên hàng chục nghìn)
        globalMinPrice.value = Math.floor(min / 10000) * 10000;
        globalMaxPrice.value = Math.ceil(max / 10000) * 10000 + 50000; // Cộng thêm chút dư
        selectedPriceRange.value = [globalMinPrice.value, globalMaxPrice.value];
    };

    const isPriceInRange = (item) => {
        const pMin = item.giaThapNhat > 0 ? item.giaThapNhat : item.giaBan;
        const pMax = item.giaCaoNhat > 0 ? item.giaCaoNhat : item.giaBan;
        return pMax >= selectedPriceRange.value[0] && pMin <= selectedPriceRange.value[1];
    };

    const resetPriceFilter = () => {
        selectedPriceRange.value = [globalMinPrice.value, globalMaxPrice.value];
    };

    return { globalMinPrice, globalMaxPrice, selectedPriceRange, calculatePriceLimits, isPriceInRange, resetPriceFilter };
}

// 2. LOGIC QUẢN LÝ MÓN ĂN (CHÍNH)
export function useFoodManager() {
    const router = useRouter();
    const route = useRoute();
    const mockData = ref([]);
    const activeTab = ref('thucdon');
    const isModalOpen = ref(false);
    const selectedItem = ref(null);
    const isCategoryLocked = ref(false);
    const isAddFoodModalOpen = ref(false);

    

    // --- Filter & Sort ---
    const searchQuery = ref('');
    const sortOption = ref('newest'); 
    const statusFilter = ref('all');
    
    // --- Pagination ---
    const currentPage = ref(1);
    const itemsPerPage = ref(5);

    // --- [MỚI] BIẾN CHO LỌC DANH MỤC ---
    const isCategoryFilterOpen = ref(false); // Đóng/Mở Modal
    const listRootCategories = ref([]);      // Danh sách gốc
    const listSubCategories = ref([]);       // Danh sách chi tiết
    const selectedRootCate = ref('');        // ID danh mục gốc đang chọn
    const selectedSubCate = ref('');         // ID danh mục con đang chọn

    // Sử dụng logic giá
    const { 
        globalMinPrice, globalMaxPrice, selectedPriceRange, 
        calculatePriceLimits, isPriceInRange, resetPriceFilter 
    } = usePriceFilter();

    // --- [MỚI] HÀM LẤY DANH MỤC TỪ API ---
    const fetchCategories = async () => {
        try {
            // Thay bằng hàm gọi API thực tế của bạn
            if (typeof getAllCategory === 'function') {
                const resRoot = await getAllCategory();
                listRootCategories.value = resRoot.data;
            }
            if (typeof getAllCategoryDetail === 'function') {
                const resSub = await getAllCategoryDetail();
                listSubCategories.value = resSub.data;
            }
        } catch (e) {
            console.error("Lỗi tải danh mục:", e);
        }
    };

    // --- [MỚI] Computed: Lọc danh mục con theo danh mục cha ---
    const availableSubCategories = computed(() => {
        if (!selectedRootCate.value) return [];
        // Lọc listSubCategories có idDanhMuc trùng với selectedRootCate
        return listSubCategories.value.filter(sub => sub.idDanhMuc == selectedRootCate.value);
    });

    // --- COMPUTED: LỌC & SẮP XẾP DỮ LIỆU ---
    const filteredAndSortedData = computed(() => {
        let result = [...mockData.value];

        // 1. Search
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }

        // 2. Status
        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value);
            result = result.filter(item => item.trangThaiKinhDoanh === statusValue);
        }

        // 3. Price Slider
        result = result.filter(item => isPriceInRange(item));

        // 4. [MỚI] Filter Category (Root)
        if (selectedRootCate.value) {
            // Tìm object danh mục gốc để lấy tên (vì bảng món ăn lưu tên danh mục)
            const rootCat = listRootCategories.value.find(c => c.id == selectedRootCate.value);
            if (rootCat) {
                // So sánh tên danh mục (hoặc ID nếu backend trả về ID trong list món ăn)
                result = result.filter(item => item.tenDanhMuc === rootCat.tenDanhMuc);
            }
        }

        // 5. [MỚI] Filter Category (Sub)
        if (selectedSubCate.value) {
             const subCat = listSubCategories.value.find(c => c.id == selectedSubCate.value);
             if (subCat) {
                 result = result.filter(item => item.tenDanhMucChiTiet === subCat.tenDanhMucChiTiet);
             }
        }

        // 6. Sort
        const getSafePrice = (item) => {
            let min = item.giaThapNhat; 
            let base = item.giaBan;
            if (min === null || min === undefined) min = 0;
            if (base === null || base === undefined) base = 0;
            const valMin = parseFloat(min);
            const valBase = parseFloat(base);
            return (valMin > 0 ? valMin : valBase) || 0;
        };

        switch (sortOption.value) {
            case 'price_asc': result.sort((a, b) => getSafePrice(a) - getSafePrice(b)); break;
            case 'price_desc': result.sort((a, b) => getSafePrice(b) - getSafePrice(a)); break;
            case 'name_asc': 
                result.sort((a, b) => {
                    const nameA = a.tenMonAn ? String(a.tenMonAn).toLowerCase() : '';
                    const nameB = b.tenMonAn ? String(b.tenMonAn).toLowerCase() : '';
                    return nameA.localeCompare(nameB);
                });
                break;
            case 'newest':
            default: result.sort((a, b) => Number(b.id) - Number(a.id)); break;
        }

        return result;
    });

    // Reset trang về 1 khi filter đổi
    watch([searchQuery, sortOption, statusFilter, selectedPriceRange, selectedRootCate, selectedSubCate], () => {
        currentPage.value = 1;
    });

    const paginatedData = computed(() => {
        if (filteredAndSortedData.value.length === 0) return [];
        const start = (currentPage.value - 1) * itemsPerPage.value; // Thêm .value
        const end = start + itemsPerPage.value; // Thêm .value
        return filteredAndSortedData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredAndSortedData.value.length / itemsPerPage.value) || 1);
    const goToPage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    const totalElements = computed(() => filteredAndSortedData.value.length);

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 1;
        const range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- API & ACTIONS ---
    function getAllFood() {
        if (typeof getAllFoodGeneral !== 'function') return; // Safety check
        getAllFoodGeneral()
            .then(async res => {
                mockData.value = res.data;
                calculatePriceLimits(mockData.value);
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => { 
        getAllFood(); 
        fetchCategories(); 
    });

    const handleViewDetails = (item) => {
        router.push({ 
            name: 'viewFood', 
            params: { id: item.id } 
        });
    };

    const handleEditFood = (item) => {
        router.push({ 
            name: 'updateFood',
            params: { id: item.id } 
        });
    };    
    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThaiKinhDoanh;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThaiKinhDoanh = newStatus;
        try {
            const payload = {...item, trangThaiKinhDoanh: newStatus };
            if (item.danhMucChiTiet?.id) payload.idDanhMucChiTiet = item.danhMucChiTiet.id;
            // Gọi API Update (nếu có)
             if (typeof putNewFood === 'function') await putNewFood(item.id, payload);
        } catch (error) {
            item.trangThaiKinhDoanh = oldStatus;
        }
    };

    onMounted(async () => {
        await Promise.all([
            getAllFood(),
            fetchCategories() 
        ]);

        if (route.query.preRoot) {
            const rootId = Number(route.query.preRoot);
            selectedRootCate.value = isNaN(rootId) ? route.query.preRoot : rootId;

            if (route.query.locked === 'true') {
                isCategoryLocked.value = true;
            } else {
                isCategoryLocked.value = false;
            }

            if (route.query.preSub) {
                await nextTick(); 
                const subId = Number(route.query.preSub);
                selectedSubCate.value = isNaN(subId) ? route.query.preSub : subId;
            }
        } else {
            isCategoryLocked.value = false;
            selectedRootCate.value = '';
            selectedSubCate.value = '';
        }
    });

    const clearFilters = () => {
        searchQuery.value = '';
        statusFilter.value = 'all';
        sortOption.value = 'newest';
        
        if (!isCategoryLocked.value) {
            selectedRootCate.value = '';
        }
        
        selectedSubCate.value = '';
        resetPriceFilter(); 
        
        if (isCategoryLocked.value) {
            currentPage.value = 1;
        }
    };

    const exportToExcel = () => {
        const dataToExport = filteredAndSortedData.value.map((item, index) => {
            
            const min = item.giaThapNhat || 0;
            const max = item.giaCaoNhat || 0;
            let priceText = '0';
            if (min === 0 && max === 0) priceText = 'Chưa cập nhật';
            else if (min === max) priceText = min; 
            else priceText = `${min} - ${max}`;

            return {
                'STT': index + 1,
                'Mã Món': item.maMonAn,
                'Tên Món Ăn': item.tenMonAn,
                'Khoảng Giá (VNĐ)': priceText,
                'Danh Mục Gốc': item.tenDanhMuc || '',
                'Danh Mục Chi Tiết': item.tenDanhMucChiTiet || '',
                'Trạng Thái': item.trangThaiKinhDoanh === 1 ? 'Đang kinh doanh' : 'Ngưng kinh doanh',
            };
        });

        const worksheet = XLSX.utils.json_to_sheet(dataToExport);

        const wscols = [
            { wch: 5 }, 
            { wch: 15 }, 
            { wch: 30 }, 
            { wch: 20 },
            { wch: 20 },
            { wch: 20 }, 
            { wch: 20 } 
        ];
        worksheet['!cols'] = wscols;

        // Tạo Workbook
        const workbook = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(workbook, worksheet, "DanhSachMonAn");

        // Xuất file
        XLSX.writeFile(workbook, "Danh_Sach_Mon_An.xlsx");
    };

    watch(() => route.query, (newQuery) => {
        if (!newQuery.preRoot) {
            isCategoryLocked.value = false;
            selectedRootCate.value = '';
        } else {
            selectedRootCate.value = Number(newQuery.preRoot);
            isCategoryLocked.value = newQuery.locked === 'true';
        }
    });

    const goToDetailTable = (id) => {
        router.push({
            name: 'foodManager',
            query: {
                tab: 'chitietTD', 
                preFood: id
            }
        });
    };


    return {
        mockData, paginatedData, currentPage, itemsPerPage, totalPages, visiblePages, goToPage,
        handleViewDetails,
        handleEditFood, getAllFood, handleToggleStatus, activeTab, isModalOpen, selectedItem, isAddFoodModalOpen,
        searchQuery, sortOption, statusFilter, clearFilters, isCategoryLocked,
        globalMinPrice, globalMaxPrice, selectedPriceRange,
        
        // EXPORT CÁC BIẾN MỚI
        isCategoryFilterOpen,
        listRootCategories,
        selectedRootCate,
        selectedSubCate,
        availableSubCategories,
        exportToExcel,
        goToDetailTable,
        totalElements
    };
}



export function useFoodUpdate() {
    const route = useRoute();
    const router = useRouter();
    const foodId = route.params.id;

    // --- STATE ---
    const isViewMode = computed(() => route.name === 'viewFood');
    const fileInputRef = ref(null);

    const formData = ref({
        id: '',
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        moTa: '',
        giaBan: 0,
        hinhAnh: '',
        trangThaiKinhDoanh: 1 
    });

    const errors = ref({
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        hinhAnh: ''
    });

    const foodInfo = ref(null);
    const variants = ref([]);
    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);
    const existingFoods = ref([]);
    const isLoading = ref(false);

    // --- DROPDOWN LOGIC ---
    const isCatDropdownOpen = ref(false);
    const catSearchQuery = ref('');

    const toggleCatDropdown = () => {
        if (isViewMode.value) return;
        isCatDropdownOpen.value = !isCatDropdownOpen.value;
        if (isCatDropdownOpen.value) { 
            catSearchQuery.value = ''; 
            isSubCatDropdownOpen.value = false; 
        }
    };

    const filteredCategories = computed(() => {
        if (!catSearchQuery.value) return listDanhMuc.value;
        return listDanhMuc.value.filter(item => 
            item.tenDanhMuc.toLowerCase().includes(catSearchQuery.value.toLowerCase())
        );
    });

    const selectCategory = (item) => {
        formData.value.idDanhMuc = item.id;
        formData.value.idDanhMucChiTiet = '';
        errors.value.idDanhMuc = ''; 
        isCatDropdownOpen.value = false;
    };

    // FIX: Convert IDs to String for loose comparison
    const selectedCategoryName = computed(() => {
        const found = listDanhMuc.value.find(i => String(i.id) === String(formData.value.idDanhMuc));
        return found ? found.tenDanhMuc : '';
    });

    const isSubCatDropdownOpen = ref(false);
    const subCatSearchQuery = ref('');

    const toggleSubCatDropdown = () => {
        if (isViewMode.value || !formData.value.idDanhMuc) return;
        isSubCatDropdownOpen.value = !isSubCatDropdownOpen.value;
        if (isSubCatDropdownOpen.value) { 
            subCatSearchQuery.value = ''; 
            isCatDropdownOpen.value = false; 
        }
    };

    const subCategoriesByParent = computed(() => {
        if (!formData.value.idDanhMuc) return [];
        return listDanhMucChiTiet.value.filter(item => {
            const parentId = item.idDanhMuc || (item.danhMuc ? item.danhMuc.id : null);
            // FIX: Comparison using String()
            return String(parentId) === String(formData.value.idDanhMuc);
        });
    });

    const filteredSubCategories = computed(() => {
        const list = subCategoriesByParent.value;
        if (!subCatSearchQuery.value) return list;
        return list.filter(item => 
            item.tenDanhMucChiTiet.toLowerCase().includes(subCatSearchQuery.value.toLowerCase())
        );
    });

    const selectSubCategory = (item) => {
        formData.value.idDanhMucChiTiet = item.id;
        errors.value.idDanhMucChiTiet = '';
        isSubCatDropdownOpen.value = false;
    };

    // FIX: Comparison using String()
    const selectedSubCategoryName = computed(() => {
        // Look in the full list to ensure it's found even if not currently filtered
        const found = listDanhMucChiTiet.value.find(i => String(i.id) === String(formData.value.idDanhMucChiTiet));
        return found ? found.tenDanhMucChiTiet : '';
    });

    const closeAllDropdowns = () => { 
        isCatDropdownOpen.value = false; 
        isSubCatDropdownOpen.value = false; 
    };

    // --- UPLOAD IMAGE (No Changes) ---
    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image();
                img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    const triggerFileInput = () => { fileInputRef.value.click(); };
    const handleFileUpload = async (event) => {
        if (isViewMode.value) return;
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Vui lòng chọn file ảnh hợp lệ!' });
            return;
        }
        try {
            const resized = await resizeImage(file);
            formData.value.hinhAnh = resized;
            errors.value.hinhAnh = ''; 
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    const removeImage = () => {
        if (isViewMode.value) return;
        Swal.fire({
            title: 'Xóa ảnh?', text: "Bạn có chắc muốn xóa ảnh này?", icon: 'warning',
            showCancelButton: true, confirmButtonColor: '#d33', cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa', cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) formData.value.hinhAnh = '';
        });
    };

    // --- FETCH DATA (CRITICAL FIX) ---
    const fetchData = async () => {
        try {
            isLoading.value = true;
            const [resParent, resVariants, resCat, resSubCat, resAllFoods] = await Promise.all([
                getFoodById(foodId),
                getFoodGeneralModalById(foodId),
                getAllCategory(),
                getAllCategoryDetail(),
                getAllFoodGeneral()
            ]);

            listDanhMuc.value = resCat.data;
            listDanhMucChiTiet.value = resSubCat.data;
            if(resAllFoods.data) existingFoods.value = resAllFoods.data;

            const parentData = resParent.data;
            if (parentData) {
                foodInfo.value = { ...parentData };
                
                // 1. Get raw IDs from API response
                const subCatId = parentData.idDanhMucChiTiet || (parentData.danhMucChiTiet ? parentData.danhMucChiTiet.id : '');
                let rootCatId = parentData.idDanhMuc || (parentData.danhMuc ? parentData.danhMuc.id : '');

                // 2. Logic to populate rootCatId if missing (reverse lookup)
                if (!rootCatId && subCatId) {
                    // FIX: Use String() for safe comparison
                    const foundSub = listDanhMucChiTiet.value.find(sub => String(sub.id) === String(subCatId));
                    if (foundSub) {
                        rootCatId = foundSub.idDanhMuc || (foundSub.danhMuc ? foundSub.danhMuc.id : '');
                    }
                }

                // 3. Assign to formData
                formData.value = {
                    id: parentData.id,
                    tenMonAn: parentData.tenMonAn,
                    idDanhMuc: rootCatId, 
                    idDanhMucChiTiet: subCatId,
                    moTa: parentData.moTa,
                    giaBan: parentData.giaBan,
                    hinhAnh: parentData.hinhAnh,
                    trangThaiKinhDoanh: (parentData.trangThaiKinhDoanh == 1 || parentData.trangThaiKinhDoanh === true) ? 1 : 0
                };
            }

            if (Array.isArray(resVariants.data)) {
                variants.value = resVariants.data;
                if (!formData.value.hinhAnh && variants.value.length > 0) {
                    formData.value.hinhAnh = variants.value[0].hinhAnh;
                    if(foodInfo.value) foodInfo.value.hinhAnh = variants.value[0].hinhAnh;
                }
            }
        } catch (e) {
            console.error("Lỗi fetch data:", e);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không tải được dữ liệu món ăn!' });
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => { if (foodId) fetchData(); });

    // --- VALIDATION & ACTIONS ---
    const validateForm = () => {
        let isValid = true;
        errors.value = { tenMonAn: '', idDanhMuc: '', idDanhMucChiTiet: '', hinhAnh: '' };

        if (!formData.value.tenMonAn || !formData.value.tenMonAn.trim()) {
            errors.value.tenMonAn = 'Tên món ăn không được để trống.';
            isValid = false;
        } else if (formData.value.tenMonAn.length < 5) {
            errors.value.tenMonAn = 'Tên món ăn phải có ít nhất 5 ký tự.';
            isValid = false;
        } else {
            const isDuplicate = existingFoods.value.some(f => 
                String(f.id) !== String(formData.value.id) && // FIX: Compare ID safely
                f.tenMonAn.toLowerCase() === formData.value.tenMonAn.trim().toLowerCase()
            );
            if (isDuplicate) {
                errors.value.tenMonAn = 'Tên món ăn này đã tồn tại.';
                isValid = false;
            }
        }

        if (!formData.value.idDanhMuc) {
            errors.value.idDanhMuc = 'Vui lòng chọn danh mục gốc.';
            isValid = false;
        }
        if (!formData.value.idDanhMucChiTiet) {
            errors.value.idDanhMucChiTiet = 'Vui lòng chọn chi tiết danh mục.';
            isValid = false;
        }
        if (!formData.value.hinhAnh) {
            errors.value.hinhAnh = 'Vui lòng chọn hình ảnh.';
            isValid = false;
        }

        return isValid;
    };

    const handleUpdate = async () => {
        if (!validateForm()) {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi nhập liệu',
                text: 'Vui lòng kiểm tra lại thông tin.',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 3000,
                timerProgressBar: true
            });
            return;
        }

        Swal.fire({
            title: 'Xác nhận cập nhật',
            text: 'Bạn có chắc chắn muốn lưu thay đổi?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Lưu',
            cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    isLoading.value = true;
                    const payload = {
                        ...formData.value,
                        tenMonAn: formData.value.tenMonAn.trim(),
                        trangThaiKinhDoanh: Number(formData.value.trangThaiKinhDoanh)
                    };
                    await putNewFood(formData.value.id, payload);
                    
                    Swal.fire({ icon: 'success', title: 'Thành công!', text: 'Đã cập nhật.', timer: 1500, showConfirmButton: false });
                    setTimeout(() => goBack(), 1500);
                } catch (e) {
                    console.error(e);
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Cập nhật thất bại!' });
                } finally { isLoading.value = false; }
            }
        });
    };

    const handleToggleDetailStatus = (variant) => {
        const newStatus = variant.trangThai === 1 ? 0 : 1;
        Swal.fire({
            title: 'Đổi trạng thái?',
            text: `Bạn muốn ${newStatus === 1 ? 'kích hoạt' : 'ngưng'} biến thể "${variant.tenChiTietMonAn}"?`,
            icon: 'warning', showCancelButton: true, confirmButtonColor: '#3085d6', cancelButtonColor: '#d33',
            confirmButtonText: 'Đồng ý'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    await putNewFoodDetail(variant.id, { ...variant, trangThai: newStatus });
                    const index = variants.value.findIndex(v => v.id === variant.id);
                    if (index !== -1) variants.value[index].trangThai = newStatus;
                    Swal.fire({ icon: 'success', title: 'Thành công', toast: true, position: 'top-end', showConfirmButton: false, timer: 2000 });
                } catch (e) { Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể cập nhật trạng thái.' }); }
            }
        });
    };

    const goToDetailTable = () => {
        router.push({
            name: 'foodManager',
            query: {
                tab: 'chitietTD', 
                preFood: formData.value.id 
            }
        });
    };

    const goToFoodListFilter = (type) => {
    const queryParams = { tab: 'thucdon' }; // Tab danh sách món ăn

    if (type === 'root' && formData.value.idDanhMuc) {
        // Chỉ lọc theo danh mục gốc
        queryParams.preRoot = formData.value.idDanhMuc;
    } 
    else if (type === 'sub' && formData.value.idDanhMucChiTiet) {
        // Lọc theo cả gốc và chi tiết (để dropdown hiển thị đúng logic)
        queryParams.preRoot = formData.value.idDanhMuc; 
        queryParams.preSub = formData.value.idDanhMucChiTiet;
    }

    router.push({ 
        name: 'foodManager', 
        query: queryParams 
    });
};

    const goToCategorizedTable = () => {
        router.push({
            name: 'foodManager',
            query: {
                tab: 'thucdon', 
                preCategory: formData.value.idDanhMuc 
            }
        });
    };

    const goBack = () => router.back();
    const goToAddDetail = () => { router.push({ name: 'addFoodDetail', query: { parentId: foodId } }); };

    return {
        isViewMode, isLoading, formData, foodInfo, variants, categoryName: selectedCategoryName,
        handleUpdate, goBack, goToAddDetail, handleToggleDetailStatus,
        fileInputRef, triggerFileInput, handleFileUpload, removeImage, goToDetailTable, goToFoodListFilter,
        isCatDropdownOpen, catSearchQuery, toggleCatDropdown, filteredCategories, selectCategory, selectedCategoryName,
        isSubCatDropdownOpen, subCatSearchQuery, toggleSubCatDropdown, filteredSubCategories, selectSubCategory, selectedSubCategoryName, closeAllDropdowns,
        errors 
    };
}

export function useFoodDetailUpdate() {
    const router = useRouter();
    const route = useRoute();
    const detailId = route.params.id;

    // --- STATE ---
    const isViewMode = computed(() => route.name === 'viewFoodDetail');
    
    const formData = ref({
        id: null,
        maChiTietMonAn: '',
        tenChiTietMonAn: '',
        idMonAnDiKem: '',
        giaBan: 0,
        giaVon: 0,
        kichCo: '',
        donVi: '',
        hinhAnh: '',
        trangThai: 1,
        moTaChiTiet: ''
    });

    // Biến lưu lỗi Validation
    const errors = ref({
        tenChiTietMonAn: '',
        idMonAnDiKem: '',
        giaBan: '',
        hinhAnh: '',
        kichCo: '',
        donVi: ''
    });

    const listMonAn = ref([]); 
    const originalInfo = ref(null);
    const isLoading = ref(true);
    const allGlobalDetails = ref([]); 

    // Search & Sort
    const searchQuery = ref('');
    const sortOption = ref('name_asc');

    // --- COMPUTED: LỌC MÓN ---
    const filteredMonAnList = computed(() => {
        let result = [...listMonAn.value];
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }
        return result.sort((a, b) => {
            const nameA = (a.tenMonAn || '').toLowerCase();
            const nameB = (b.tenMonAn || '').toLowerCase();
            if (sortOption.value === 'price_asc') return (a.giaBan || 0) - (b.giaBan || 0);
            if (sortOption.value === 'price_desc') return (b.giaBan || 0) - (a.giaBan || 0);
            return nameA.localeCompare(nameB); 
        });
    });

    // --- HÀM TÍNH KHOẢNG GIÁ ---
    const getPriceRange = (foodItem) => {
        const childDetails = allGlobalDetails.value.filter(detail => {
            if (detail.idMonAnDiKem && typeof detail.idMonAnDiKem === 'object') {
                return detail.idMonAnDiKem.id === foodItem.id;
            }
            return String(detail.idMonAnDiKem) === String(foodItem.id);
        });

        if (childDetails.length === 0) return foodItem.giaBan ? `${foodItem.giaBan.toLocaleString()}đ` : 'Chưa có giá';
        
        const prices = childDetails.map(d => d.giaBan).filter(p => p !== null && p >= 0);
        if (prices.length === 0) return 'Chưa có giá';

        const min = Math.min(...prices);
        const max = Math.max(...prices);
        return (min === max) ? `${min.toLocaleString()}đ` : `${min.toLocaleString()} - ${max.toLocaleString()}đ`;
    };

    // --- CHECK TRÙNG TÊN (AN TOÀN) ---
    const isDetailNameDuplicate = (name) => {
        if (!name) return false;
        const normalizedName = name.trim().toLowerCase();
        
        // Kiểm tra an toàn để tránh crash
        return allGlobalDetails.value.some(d => 
            d.tenChiTietMonAn && 
            d.tenChiTietMonAn.toLowerCase() === normalizedName && 
            String(d.id) !== String(detailId) // Loại trừ chính nó
        );
    };

    const selectParentFood = (food) => {
        if (isViewMode.value) return;
        formData.value.idMonAnDiKem = food.id;
        errors.value.idMonAnDiKem = ''; // Xóa lỗi khi chọn
    };

    const parentName = computed(() => {
        const found = listMonAn.value.find(m => m.id === formData.value.idMonAnDiKem);
        if (found) return found.tenMonAn;
        if (originalInfo.value && originalInfo.value.monAnDiKem) {
            return originalInfo.value.monAnDiKem.tenMonAn;
        }
        return '---';
    });

    // --- FETCH DATA ---
    const fetchDetailData = async () => {
        try {
            isLoading.value = true;
            const [resDetail, resListFood, resAllDetails] = await Promise.all([
                getFoodDetailById(detailId),
                getAllFoodGeneral(),
                getAllFoodDetail()
            ]);

            listMonAn.value = resListFood.data;
            if (resAllDetails && resAllDetails.data) {
                allGlobalDetails.value = resAllDetails.data;
            }

            const data = resDetail.data;
            originalInfo.value = data; 

            formData.value = {
                id: data.id,
                maChiTietMonAn: data.maChiTietMonAn,
                tenChiTietMonAn: data.tenChiTietMonAn,
                idMonAnDiKem: data.monAnDiKem ? data.monAnDiKem.id : data.idMonAnDiKem,
                giaBan: data.giaBan,
                giaVon: data.giaVon,
                kichCo: data.kichCo,
                donVi: data.donVi,
                hinhAnh: data.hinhAnh,
                moTaChiTiet: data.moTaChiTiet || '',
                trangThai: Number(data.trangThai) === 1 ? 1 : 0 
            };
        } catch (e) {
            console.error("Lỗi:", e);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không tìm thấy dữ liệu!' });
            setTimeout(() => router.back(), 1500);
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => { 
        if (detailId) fetchDetailData(); 
    });

    // --- UPLOAD ẢNH ---
    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image();
                img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    const handleFileUpload = async (event) => {
        if (isViewMode.value) return;
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            Swal.fire({ icon: 'error', title: 'Lỗi định dạng', text: 'Vui lòng chọn file ảnh!' });
            return;
        }
        try {
            const resized = await resizeImage(file);
            formData.value.hinhAnh = resized;
            errors.value.hinhAnh = ''; 
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    // --- VALIDATION & UPDATE ---
    const validateForm = () => {
        let isValid = true;
        errors.value = { tenChiTietMonAn: '', idMonAnDiKem: '', giaBan: '', hinhAnh: '', kichCo: '', donVi: '' };

        if (!formData.value.idMonAnDiKem) {
            errors.value.idMonAnDiKem = 'Vui lòng chọn món ăn gốc.';
            isValid = false;
        }

        const name = formData.value.tenChiTietMonAn ? formData.value.tenChiTietMonAn.trim() : '';
        if (!name) {
            errors.value.tenChiTietMonAn = 'Vui lòng nhập tên chi tiết.';
            isValid = false;
        } else if (name.length < 5) {
            errors.value.tenChiTietMonAn = 'Tên phải chứa trên 5 kí tự.';
            isValid = false;
        } else if (isDetailNameDuplicate(name)) {
            errors.value.tenChiTietMonAn = 'Tên chi tiết này đã tồn tại.';
            isValid = false;
        }

        if (formData.value.giaBan === "" || formData.value.giaBan < 0) {
            errors.value.giaBan = 'Giá bán không hợp lệ.';
            isValid = false;
        }

        if (!formData.value.kichCo) { errors.value.kichCo = 'Nhập kích cỡ.'; isValid = false; }
        if (!formData.value.donVi) { errors.value.donVi = 'Nhập đơn vị.'; isValid = false; }

        if (!formData.value.hinhAnh) {
            errors.value.hinhAnh = 'Vui lòng chọn ảnh.';
            isValid = false;
        }

        return isValid;
    };

    const handleUpdate = async () => {
        if (!validateForm()) {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi nhập liệu',
                text: 'Vui lòng kiểm tra lại thông tin.',
                toast: true, position: 'top-end', showConfirmButton: false, timer: 3000, timerProgressBar: true
            });
            return;
        }

        Swal.fire({
            title: 'Xác nhận cập nhật',
            text: 'Bạn có chắc chắn muốn lưu thay đổi?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#3085d6', cancelButtonColor: '#d33',
            confirmButtonText: 'Lưu', cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    isLoading.value = true;
                    const payload = {
                        ...formData.value,
                        tenChiTietMonAn: formData.value.tenChiTietMonAn.trim(),
                        moTaChiTiet: formData.value.moTaChiTiet ? formData.value.moTaChiTiet.trim() : '',
                        trangThai: Number(formData.value.trangThai)
                    };
                    await putNewFoodDetail(detailId, payload);
                    
                    Swal.fire({
                        icon: 'success', title: 'Thành công!',
                        text: 'Cập nhật thành công.',
                        timer: 1500, showConfirmButton: false
                    });
                    
                    setTimeout(() => goBack(), 1500);
                } catch (e) {
                    console.error(e);
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Đã xảy ra lỗi khi cập nhật!' });
                } finally { isLoading.value = false; }
            }
        });
    };
    
    const goBack = () => router.back();
    const goToEdit = () => { router.push({ name: 'updateFoodDetail', params: { id: detailId } }); };

    return {
        formData, listMonAn, originalInfo, parentName, isLoading, isViewMode,
        searchQuery, sortOption, filteredMonAnList, selectParentFood,
        handleUpdate, goBack, handleFileUpload, goToEdit, getPriceRange,
        errors // Export errors
    };
}

export function useHotpotManager() {
    const hotpotData = ref([]);

    // --- CÁC BIẾN FILTER ---
    const searchQuery = ref('');
    const sortOption = ref('newest');
    const currentPage = ref(1);
    const itemsPerPage = ref(5);
    const statusFilter = ref('all'); 
    const typeFilter = ref(null);   
    const isTypeLocked = ref(false);
    const route = useRoute();

    // 1. GỌI HOOK XỬ LÝ GIÁ
    const { 
        selectedPriceRange, 
        globalMinPrice, 
        globalMaxPrice, 
        calculatePriceLimits,
        resetPriceFilter
    } = usePriceFilter();

    // 2. TẠO DANH SÁCH LOẠI LẨU
    const uniqueTypes = computed(() => {
        const types = new Map();
        hotpotData.value.forEach(item => {
            if (item.tenLoaiSet && item.idLoaiSet) {
                types.set(item.idLoaiSet, item.tenLoaiSet);
            }
        });
        return Array.from(types, ([id, name]) => ({ id, name }));
    });

    // 3. LOGIC LỌC & SẮP XẾP CHÍNH
    const filteredAndSortedData = computed(() => {
        let result = [...hotpotData.value];

        // A. Search
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item =>
                (item.tenSetLau && item.tenSetLau.toLowerCase().includes(query)) ||
                (item.maSetLau && item.maSetLau.toLowerCase().includes(query))
            );
        }

        // B. Giá
        if (selectedPriceRange.value && selectedPriceRange.value.length === 2) {
            const [min, max] = selectedPriceRange.value;
            result = result.filter(item => {
                const price = parseFloat(item.giaBan) || 0;
                return price >= min && price <= max;
            });
        }

        // C. Trạng thái
        if (statusFilter.value !== 'all') {
            const statusValue = Number(statusFilter.value);
            result = result.filter(item => item.trangThai === statusValue);
        }

        // D. Loại Set (Sửa cho Multiselect)
        if (typeFilter.value && typeFilter.value !== 'all') {
            result = result.filter(item => item.idLoaiSet == typeFilter.value);
        }

        // E. Sort
        switch (sortOption.value) {
            case 'price_asc': result.sort((a, b) => (a.giaBan || 0) - (b.giaBan || 0)); break;
            case 'price_desc': result.sort((a, b) => (b.giaBan || 0) - (a.giaBan || 0)); break;
            case 'name_asc': result.sort((a, b) => (a.tenSetLau || '').localeCompare(b.tenSetLau || '')); break;
            case 'newest': default: result.sort((a, b) => b.id - a.id); break;
        }
        return result;
    });

    // Reset phân trang khi filter thay đổi
    watch([searchQuery, sortOption, statusFilter, typeFilter, selectedPriceRange], () => {
        currentPage.value = 1;
    });

    // --- PHÂN TRANG ---
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredAndSortedData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredAndSortedData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredAndSortedData.value.length);
    const goToPage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    const visiblePages = computed(() => {
        // ... (Giữ nguyên logic phân trang cũ của bạn) ...
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 1;
        const range = [], rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- API & ACTIONS ---
    function getAllHotpot() {
        getAllHotpotGeneral().then(async res => {
            hotpotData.value = res.data;
            
            // TÍNH TOÁN GIÁ MAX CHO SLIDER
            calculatePriceLimits(hotpotData.value);
            
            await nextTick();
        }).catch(console.error);
    }
    
    onMounted(async () => {
        await getAllHotpot();
        
        if (route.query.preType) {
            const typeId = Number(route.query.preType);
            
            typeFilter.value = isNaN(typeId) ? route.query.preType : typeId;
            
            isTypeLocked.value = route.query.locked === 'true';
        } else {
            typeFilter.value = 'all';
            isTypeLocked.value = false;
        }
    });
    
    const isModalOpen = ref(false);
    const selectedHotpot = ref(null);
    const handleViewDetails = (item) => { selectedHotpot.value = item; isModalOpen.value = true; };
    
    const handleToggleStatus = async (item) => {
    const oldStatus = item.trangThai;
    const newStatus = oldStatus === 1 ? 0 : 1;
    
    item.trangThai = newStatus;

    try {
        const payload = {
            id: item.id,
            maSetLau: item.maSetLau,
            tenSetLau: item.tenSetLau,
            giaBan: item.giaBan,
            idLoaiSet: item.idLoaiSet,
            moTaChiTiet: item.moTaChiTiet,
            hinhAnh: item.hinhAnh,
            mota: item.mota,
            trangThai: newStatus 
        };


        await putNewHotpot(item.id, payload);
        console.log("Cập nhật trạng thái thành công");

    } catch (error) {
        console.error("Lỗi cập nhật:", error);
        // Hoàn tác nếu lỗi
        item.trangThai = oldStatus;
        
        // Kiểm tra lỗi cụ thể để báo người dùng
        if (error.response && error.response.status === 403) {
            alert("Bạn không có quyền chỉnh sửa mục này!");
        } else {
            alert("Lỗi kết nối, vui lòng thử lại.");
        }
    }
};

    // Hàm Reset bộ lọc
    const clearFilters = () => {
        searchQuery.value = '';
        sortOption.value = 'newest';
        statusFilter.value = 'all';
        typeFilter.value = 'all';
        
        // RESET SLIDER
        resetPriceFilter();
    };

    const exportToExcel = () => {
        const dataToExport = filteredAndSortedData.value.map((item, index) => ({
            'STT': index + 1,
            'Mã Set': item.maSetLau,
            'Tên Set Lẩu': item.tenSetLau,
            'Giá Bán': item.giaBan,
            'Loại Lẩu': item.tenLoaiSet,
            'Trạng Thái': item.trangThai === 1 ? 'Kinh doanh' : 'Ngưng'
        }));
        const ws = XLSX.utils.json_to_sheet(dataToExport);
        const wb = XLSX.utils.book_new();
        XLSX.utils.book_append_sheet(wb, ws, "SetLau");
        XLSX.writeFile(wb, "Danh_Sach_Set_Lau.xlsx");
    };
    const router = useRouter();

    const goToDetailTable = (id) => {
        router.push({
            name: 'foodManager',
            query: {
                tab: 'chitietTD', 
                preHotpot: id 
            }
        });
    };

    return {
        hotpotData, getAllHotpot, isModalOpen, selectedHotpot, handleViewDetails, handleToggleStatus,
        searchQuery, sortOption, paginatedData, currentPage, totalPages, visiblePages, itemsPerPage, goToPage,
        statusFilter, typeFilter, uniqueTypes, clearFilters, exportToExcel, totalElements,
        
        // RETURN CÁC BIẾN SLIDER
        selectedPriceRange, goToDetailTable,
        globalMinPrice,
        globalMaxPrice,
        isTypeLocked
    };
}

export function useHotpotUpdate() {
    const router = useRouter();
    const route = useRoute();
    const hotpotId = route.params.id;
    
    // --- STATE ---
    const hotpotInfo = ref(null);
    const listLoaiSet = ref([]);
    const listFoodDetails = ref([]);
    const selectedIngredients = ref([]);
    const isLoading = ref(true);
    
    // List tên Set đã tồn tại (để check trùng)
    const existingSetNames = ref([]);

    const searchQuery = ref("");
    const sortOption = ref("name_asc");

    const formData = ref({
        id: null,
        tenSetLau: '',
        idLoaiSet: '',
        giaBan: 0,
        hinhAnh: '',
        moTa: '',
        moTaChiTiet: '',
        trangThai: 1
    });

    // Biến lưu lỗi Validation
    const errors = ref({
        tenSetLau: '',
        idLoaiSet: '',
        giaBan: '',
        hinhAnh: '',
        moTaChiTiet: '',
        selectedIngredients: '' 
    });

    // --- COMPUTED ---
    const isViewMode = computed(() => route.name === 'viewHotpotSet');

    const totalComponentsPrice = computed(() => {
        return selectedIngredients.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
    });

    const categoryName = computed(() => {
        if (!hotpotInfo.value) return 'Đang tải...';
        if (hotpotInfo.value.loaiSet?.tenLoaiSet) return hotpotInfo.value.loaiSet.tenLoaiSet;
        const idToFind = hotpotInfo.value.idLoaiSet || (hotpotInfo.value.loaiSet ? hotpotInfo.value.loaiSet.id : null);
        const found = listLoaiSet.value.find(cat => cat.id === idToFind);
        return found ? found.tenLoaiSet : 'Chưa phân loại';
    });

    // Lọc danh sách món ăn (thành phần)
    const filteredFoodList = computed(() => {
        let result = [...listFoodDetails.value];
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item =>
                (item.tenChiTietMonAn || item.tenDanhMucChiTiet || '').toLowerCase().includes(query)
            );
        }
        return result.sort((a, b) => {
            const nameA = (a.tenChiTietMonAn || a.tenDanhMucChiTiet || '').toLowerCase();
            const nameB = (b.tenChiTietMonAn || b.tenDanhMucChiTiet || '').toLowerCase();
            if (sortOption.value === 'name_asc') return nameA.localeCompare(nameB);
            if (sortOption.value === 'price_asc') return (a.giaBan || 0) - (b.giaBan || 0);
            if (sortOption.value === 'price_desc') return (b.giaBan || 0) - (a.giaBan || 0);
            return 0;
        });
    });

    // --- CHECK TRÙNG TÊN (Global Check) ---
    const isSetNameDuplicate = (name) => {
        if (!name) return false;
        const normalizedName = name.trim().toLowerCase();
        
        // Kiểm tra xem tên này có tồn tại trong DB không
        // VÀ ID của nó KHÁC với ID hiện tại đang sửa (để cho phép giữ nguyên tên cũ)
        return existingSetNames.value.some(set => 
            set.tenSetLau.toLowerCase() === normalizedName && 
            String(set.id) !== String(hotpotId) // Quan trọng: Loại trừ chính nó
        );
    };

    // --- FETCH DATA ---
    const fetchDetailData = async() => {
        try {
            isLoading.value = true;
            // Gọi song song 4 API
            const [resDetail, resLoaiSet, resFoodDetail, resAllHotpots] = await Promise.all([
                getHotpotById(hotpotId),
                getAllCategoryHotpot(),
                getAllFoodDetail(),
                getAllHotpotGeneral()
            ]);

            listLoaiSet.value = resLoaiSet.data;
            listFoodDetails.value = resFoodDetail.data;
            
            if (resAllHotpots && resAllHotpots.data) {
                existingSetNames.value = resAllHotpots.data;
            }

            const data = resDetail.data;
            hotpotInfo.value = data;

            formData.value = {
                id: data.id,
                tenSetLau: data.tenSetLau,
                idLoaiSet: data.loaiSet ? data.loaiSet.id : data.idLoaiSet,
                giaBan: data.giaBan,
                hinhAnh: data.hinhAnh,
                moTaChiTiet: data.moTaChiTiet || '',
                moTa: data.moTa || '',
                trangThai: Number(data.trangThai) === 1 ? 1 : 0
            };

            if (data.listChiTietSetLau && data.listChiTietSetLau.length > 0) {
                selectedIngredients.value = data.listChiTietSetLau.map(item => {
                    if (!item.chiTietMonAn) return null;
                    return {
                        id: item.chiTietMonAn.id,
                        ten: item.chiTietMonAn.tenChiTietMonAn,
                        donVi: item.chiTietMonAn.donVi,
                        giaBan: item.chiTietMonAn.giaBan,
                        hinhAnh: item.chiTietMonAn.hinhAnh,
                        moTaChiTiet: item.chiTietMonAn.moTaChiTiet,
                        soLuong: item.soLuong
                    };
                }).filter(i => i !== null);
            }
        } catch (e) {
            console.error(e);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể tải dữ liệu Set Lẩu!' });
            setTimeout(() => router.back(), 1500);
        } finally {
            isLoading.value = false;
        }
    };

    onMounted(() => { if (hotpotId) fetchDetailData(); });

    // --- UPLOAD ẢNH (Resize) ---
    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image();
                img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    const handleFileUpload = async (event) => {
        if (isViewMode.value) return;
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Vui lòng chọn file hình ảnh!' });
            return;
        }
        try {
            const resized = await resizeImage(file);
            formData.value.hinhAnh = resized;
            errors.value.hinhAnh = ''; 
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    // --- ACTIONS ---
    const addIngredient = (foodItem) => {
        if (isViewMode.value) return;
        const exists = selectedIngredients.value.find(item => item.id === foodItem.id);
        if (exists) { exists.soLuong += 1; } 
        else {
            selectedIngredients.value.push({
                id: foodItem.id,
                ten: foodItem.tenChiTietMonAn || foodItem.tenDanhMucChiTiet,
                donVi: foodItem.donVi || 'Phần',
                giaBan: foodItem.giaBan,
                hinhAnh: foodItem.hinhAnh,
                soLuong: 1
            });
        }
        errors.value.selectedIngredients = ''; // Xóa lỗi
    };

    const removeIngredient = (index) => {
        if (isViewMode.value) return;
        Swal.fire({
            title: 'Xóa thành phần?', text: "Bạn muốn loại bỏ món này khỏi Set?", icon: 'warning',
            showCancelButton: true, confirmButtonColor: '#d33', cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa', cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) selectedIngredients.value.splice(index, 1);
        });
    };

    // --- VALIDATE & UPDATE ---
    const validateForm = () => {
        let isValid = true;
        errors.value = { tenSetLau: '', idLoaiSet: '', giaBan: '', hinhAnh: '', selectedIngredients: '', moTaChiTiet: '' };

        const setName = formData.value.tenSetLau ? formData.value.tenSetLau.trim() : '';
        if (!setName) {
            errors.value.tenSetLau = 'Vui lòng nhập tên Set Lẩu.'; isValid = false;
        } else if (setName.length < 5) {
            errors.value.tenSetLau = 'Tên set lẩu phải chứa trên 5 kí tự.'; isValid = false;
        } else if (isSetNameDuplicate(setName)) {
            errors.value.tenSetLau = 'Tên Set Lẩu này đã tồn tại!'; isValid = false;
        }

        const moTa = formData.value.moTaChiTiet ? String(formData.value.moTaChiTiet).trim() : '';
        if (!moTa) { 
            errors.value.moTaChiTiet = 'Vui lòng nhập định lượng.'; 
            isValid = false; 
        }

        if (!formData.value.idLoaiSet) { errors.value.idLoaiSet = 'Vui lòng chọn Loại Set.'; isValid = false; }
        
        if (formData.value.giaBan === "" || formData.value.giaBan < 0) { 
            errors.value.giaBan = 'Giá bán không hợp lệ.'; isValid = false; 
        }

        if (!formData.value.hinhAnh) { errors.value.hinhAnh = 'Vui lòng chọn hình ảnh.'; isValid = false; }

        if (selectedIngredients.value.length === 0) {
            errors.value.selectedIngredients = 'Vui lòng chọn ít nhất 1 món ăn vào Set.'; isValid = false;
        }

        return isValid;
    };

    const handleUpdate = async() => {
        if (!validateForm()) {
            Swal.fire({
                icon: 'error', title: 'Lỗi nhập liệu', text: 'Vui lòng kiểm tra lại thông tin.',
                toast: true, position: 'top-end', showConfirmButton: false, timer: 3000
            });
            return;
        }

        Swal.fire({
            title: 'Xác nhận cập nhật?',
            text: 'Bạn có chắc chắn muốn cập nhật thông tin Set Lẩu?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#3085d6', cancelButtonColor: '#d33',
            confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    isLoading.value = true;
                    const payload = {
                        ...formData.value,
                        tenSetLau: formData.value.tenSetLau.trim(),
                        moTa: formData.value.moTa ? formData.value.moTa.trim() : '',
                        moTaChiTiet: formData.value.moTaChiTiet ? formData.value.moTaChiTiet.trim() : '',
                        trangThai: Number(formData.value.trangThai),
                        listChiTietSetLau: selectedIngredients.value.map(item => ({
                            idChiTietMonAn: item.id,
                            soLuong: item.soLuong
                        }))
                    };
                    
                    await putNewHotpot(hotpotId, payload);
                    
                    Swal.fire({
                        icon: 'success', title: 'Thành công!', text: 'Cập nhật thành công.',
                        timer: 1500, showConfirmButton: false
                    });
                    
                    setTimeout(() => router.back(), 1500);
                } catch (e) {
                    console.error(e);
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Lỗi khi cập nhật! Vui lòng thử lại.' });
                } finally { isLoading.value = false; }
            }
        });
    };

    const goBack = () => router.back();

    return {
        formData, listLoaiSet, selectedIngredients, totalComponentsPrice,
        searchQuery, sortOption, filteredFoodList, hotpotInfo, categoryName, 
        isViewMode, isLoading,
        handleFileUpload, addIngredient, removeIngredient, handleUpdate, goBack,
        errors // Export errors
    };
}

export function useCategoryTabManager() {
    const tabs = {
        'danhmuc': CategoryGeneral,
        'chitietDM': CategoryDetailGeneral,
        'loaiset': CategoryHotpotGeneral
    };

    const currentTabName = ref('danhmuc');
    const currentComponent = shallowRef(CategoryGeneral);

    const changeTab = (tabName) => {
        currentTabName.value = tabName;
        currentComponent.value = tabs[tabName];
    };

    return {
        currentTabName,
        currentComponent,
        changeTab
    };
}

export function useCategoryManager() {
    const categoryData = ref([]);
    const isLoading = ref(false);

    const searchQuery = ref('');
    
    const statusSort = ref('default'); 
    
    const sortOption = ref('id_desc');
    
    const currentPage = ref(1);
    const itemsPerPage = ref(5);

    const filteredData = computed(() => {
        let result = [...categoryData.value];

        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenDanhMuc && item.tenDanhMuc.toLowerCase().includes(query)) ||
                (item.maDanhMuc && item.maDanhMuc.toLowerCase().includes(query))
            );
        }

        return result.sort((a, b) => {
            if (statusSort.value !== 'default') {
                const statusA = Number(a.trangThai);
                const statusB = Number(b.trangThai);
                
                if (statusSort.value === 'active_first') {
                    if (statusA !== statusB) return statusB - statusA;
                } 
                else if (statusSort.value === 'inactive_first') {
                    if (statusA !== statusB) return statusA - statusB;
                }
            }

            switch (sortOption.value) {
                case 'name_asc': 
                    return (a.tenDanhMuc || '').localeCompare(b.tenDanhMuc || '');
                case 'id_asc': 
                    return a.id - b.id;
                case 'id_desc': 
                default:
                    return b.id - a.id;
            }
        });
    });

    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredData.value.length);
    
    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [];
        const rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- ACTIONS ---
    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    // Reset trang khi thay đổi filter/sort
    watch([searchQuery, sortOption, statusSort], () => {
        currentPage.value = 1;
    });

    // --- API & LOGIC KHÁC ---
    function getAllCategories() {
        isLoading.value = true;
        getAllCategory()
            .then(async res => {
                categoryData.value = res.data;
                await nextTick();
            })
            .catch(console.error)
            .finally(() => isLoading.value = false);
    }

    onMounted(() => { getAllCategories(); });

    const isModalOpen = ref(false);
    const isModalUpdateOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalUpdateOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = {
                tenDanhMuc: item.tenDanhMuc,
                moTa: item.moTa || "",
                trangThai: newStatus
            };
            await putNewCategory(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
            alert("Lỗi cập nhật trạng thái!");
        }
    };

    return {
        categoryData, isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllCategories,
        
        // Return các biến mới
        paginatedData, searchQuery, totalElements,
        sortOption, statusSort, // 2 biến sort riêng biệt
        currentPage, totalPages, visiblePages, itemsPerPage, changePage
    };
}

export function useCategoryDetailManager() {
    const detailData = ref([]);
    const parentCategories = ref([]); 
    const isLoading = ref(false);

    const searchQuery = ref('');
    const categoryFilter = ref(null);
    
    const statusSort = ref('default'); 
    
    const sortOption = ref('id_desc');
    
    const currentPage = ref(1);
    const itemsPerPage = ref(5);

    const filteredData = computed(() => {
        let result = [...detailData.value];

        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenDanhMucChiTiet && item.tenDanhMucChiTiet.toLowerCase().includes(query)) ||
                (item.maDanhMucChiTiet && item.maDanhMucChiTiet.toLowerCase().includes(query))
            );
        }

        if (categoryFilter.value) { 
        const filterId = Number(categoryFilter.value); 
    
        result = result.filter(item => {
            const idInObject = item.danhMuc?.id; 
            const idDirect = item.idDanhMuc;
            return (idInObject == filterId) || (idDirect == filterId);
        });
}

        return result.sort((a, b) => {
            if (statusSort.value !== 'default') {
                const statusA = Number(a.trangThai);
                const statusB = Number(b.trangThai);
                
                if (statusSort.value === 'active_first') {
                    if (statusA !== statusB) return statusB - statusA;
                } 
                else if (statusSort.value === 'inactive_first') {
                    if (statusA !== statusB) return statusA - statusB;
                }
            }

            switch (sortOption.value) {
                case 'name_asc': 
                    return (a.tenDanhMucChiTiet || '').localeCompare(b.tenDanhMucChiTiet || '');
                case 'parent_asc': 
                    return (a.tenDanhMuc || '').localeCompare(b.tenDanhMuc || '');
                case 'id_asc': 
                    return a.id - b.id;
                case 'id_desc': 
                default:
                    return b.id - a.id;
            }
        });
    });

    // --- PHÂN TRANG (Giữ nguyên) ---
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredData.value.length);
    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [];
        const rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    // Reset trang khi filter thay đổi (Thêm statusSort vào watcher)
    watch([searchQuery, sortOption, categoryFilter, statusSort], () => {
        currentPage.value = 1;
    });

    // --- API & MODAL (Giữ nguyên) ---
    function getAllData() {
        isLoading.value = true;
        Promise.all([getAllCategoryDetail(), getAllCategory()])
            .then(async ([resDetail, resParent]) => {
                detailData.value = resDetail.data;
                parentCategories.value = resParent.data;
                await nextTick();
            })
            .catch(console.error)
            .finally(() => isLoading.value = false);
    }

    onMounted(() => { getAllData(); });

    const isModalOpen = ref(false);
    const isModalUpdateOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalUpdateOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = { ...item, trangThai: newStatus };
            await putCategoryDetail(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
            alert("Lỗi cập nhật trạng thái!");
        }
    };

    return {
        detailData, parentCategories, 
        isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, 
        getAllCategoriesDetail: getAllData,

        // Pagination & Filter Variables
        paginatedData, 
        searchQuery, 
        categoryFilter,
        totalElements,
        
        sortOption, // Sort chính
        statusSort, // Sort trạng thái (Mới)
        
        currentPage, totalPages, visiblePages, itemsPerPage, changePage
    };
}

export function useHotpotSetTypeManager() {
    const hotpotTypeData = ref([]);
    const isLoading = ref(false);

    // --- STATE FILTER & PAGINATION ---
    const searchQuery = ref('');
    const statusFilter = ref('all'); // Lọc theo trạng thái
    const sortOption = ref('id_desc'); // Sắp xếp
    const currentPage = ref(1);
    const itemsPerPage = ref(5); // Số dòng mỗi trang

    // --- 1. COMPUTED: LỌC & SẮP XẾP ---
    const filteredData = computed(() => {
        let result = [...hotpotTypeData.value];

        // A. Tìm kiếm (Mã hoặc Tên)
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenLoaiSet && item.tenLoaiSet.toLowerCase().includes(query)) ||
                (item.maLoaiSet && item.maLoaiSet.toLowerCase().includes(query))
            );
        }

        // B. Lọc theo Trạng thái
        if (statusFilter.value !== 'all') {
            const statusValue = parseInt(statusFilter.value);
            result = result.filter(item => item.trangThai === statusValue);
        }

        // C. Sắp xếp
        return result.sort((a, b) => {
            const statusA = Number(a.trangThai);
            const statusB = Number(b.trangThai);

            switch (sortOption.value) {
                case 'name_asc': 
                    return (a.tenLoaiSet || '').localeCompare(b.tenLoaiSet || '');
                
                case 'status_active': // Đang hoạt động lên đầu
                    return statusB - statusA;
                case 'status_inactive': // Ngưng hoạt động lên đầu
                    return statusA - statusB;

                case 'id_asc': 
                    return a.id - b.id;
                case 'id_desc': 
                default:
                    return b.id - a.id;
            }
        });
    });

    // --- 2. PHÂN TRANG ---
    const paginatedData = computed(() => {
        const start = (currentPage.value - 1) * itemsPerPage.value;
        const end = start + itemsPerPage.value;
        return filteredData.value.slice(start, end);
    });

    const totalPages = computed(() => Math.ceil(filteredData.value.length / itemsPerPage.value) || 1);
    const totalElements = computed(() => filteredData.value.length);

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 2;
        const range = [];
        const rangeWithDots = [];
        let l;
        for (let i = 1; i <= total; i++) {
            if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) {
                range.push(i);
            }
        }
        for (let i of range) {
            if (l) {
                if (i - l === 2) rangeWithDots.push(l + 1);
                else if (i - l !== 1) rangeWithDots.push('...');
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    // --- ACTIONS ---
    const changePage = (page) => {
        if (page >= 1 && page <= totalPages.value) currentPage.value = page;
    };

    // Reset trang khi filter thay đổi
    watch([searchQuery, statusFilter, sortOption], () => {
        currentPage.value = 1;
    });

    // --- API CALLS ---
    function getAllHotpotType() {
        isLoading.value = true;
        getAllCategoryHotpot()
            .then(async res => {
                hotpotTypeData.value = res.data;
                await nextTick();
            })
            .catch(console.error)
            .finally(() => isLoading.value = false);
    }

    onMounted(() => { getAllHotpotType(); });

    // --- MODAL & TOGGLE ---
    const isModalOpen = ref(false);
    const isModalUpdateOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalUpdateOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;
        try {
            const payload = {...item, trangThai: newStatus };
            await putNewHotpotCategory(item.id, payload);
        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
            alert("Lỗi cập nhật trạng thái!");
        }
    };

    return {
        // Data gốc
        hotpotTypeData,
        
        // Modal
        isModalOpen, isModalUpdateOpen, selectedItem, openModal, handleToggleStatus, getAllHotpotType,

        // Pagination & Filter (Mới)
        paginatedData,
        searchQuery,
        statusFilter,
        sortOption,
        currentPage,
        totalPages,
        visiblePages,
        itemsPerPage,
        totalElements,
        changePage
    };
}


export function useFoodAddScreen() {
    const router = useRouter();

    // --- STATE ---
    const formData = ref({
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        moTa: '',
        giaBan: 0,
        hinhAnh: '',
        trangThaiKinhDoanh: 1
    });

    // Biến lưu lỗi validation
    const errors = ref({
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        hinhAnh: '',
        listChiTiet: ''
    });

    const listChiTiet = ref([]);
    const newDetail = ref({
        tenChiTietMonAn: '',
        giaBan: 0,
        giaVon: 0,
        kichCo: '',
        donVi: 'Cốc',
        trangThai: 1,
        hinhAnh: ''
    });
    
    // Biến lỗi cho phần thêm chi tiết
    const detailErrors = ref({
        tenChiTietMonAn: '',
        kichCo: '',
        giaBan: '',
        hinhAnh: ''
    });

    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);
    const existingFoods = ref([]);
    const existingDetails = ref([]);

    // --- DROPDOWN LOGIC (Giữ nguyên logic dropdown search của bạn) ---
    const isCatDropdownOpen = ref(false);
    const catSearchQuery = ref('');
    const toggleCatDropdown = () => {
        isCatDropdownOpen.value = !isCatDropdownOpen.value;
        if (isCatDropdownOpen.value) { catSearchQuery.value = ''; isSubCatDropdownOpen.value = false; }
    };
    const filteredCategories = computed(() => {
        if (!catSearchQuery.value) return listDanhMuc.value;
        return listDanhMuc.value.filter(item => item.tenDanhMuc.toLowerCase().includes(catSearchQuery.value.toLowerCase()));
    });
    const selectCategory = (item) => {
        formData.value.idDanhMuc = item.id;
        formData.value.idDanhMucChiTiet = '';
        errors.value.idDanhMuc = ''; // Xóa lỗi khi chọn
        isCatDropdownOpen.value = false;
    };
    const selectedCategoryName = computed(() => {
        const found = listDanhMuc.value.find(i => i.id === formData.value.idDanhMuc);
        return found ? found.tenDanhMuc : '';
    });

    const isSubCatDropdownOpen = ref(false);
    const subCatSearchQuery = ref('');
    const toggleSubCatDropdown = () => {
        if (!formData.value.idDanhMuc) return;
        isSubCatDropdownOpen.value = !isSubCatDropdownOpen.value;
        if (isSubCatDropdownOpen.value) { subCatSearchQuery.value = ''; isCatDropdownOpen.value = false; }
    };
    const subCategoriesByParent = computed(() => {
        if (!formData.value.idDanhMuc) return [];
        return listDanhMucChiTiet.value.filter(item => {
            const parentId = item.idDanhMuc || (item.danhMuc ? item.danhMuc.id : null);
            return String(parentId) === String(formData.value.idDanhMuc);
        });
    });
    const filteredSubCategories = computed(() => {
        const list = subCategoriesByParent.value;
        if (!subCatSearchQuery.value) return list;
        return list.filter(item => item.tenDanhMucChiTiet.toLowerCase().includes(subCatSearchQuery.value.toLowerCase()));
    });
    const selectSubCategory = (item) => {
        formData.value.idDanhMucChiTiet = item.id;
        errors.value.idDanhMucChiTiet = ''; // Xóa lỗi
        isSubCatDropdownOpen.value = false;
    };
    const selectedSubCategoryName = computed(() => {
        const found = listDanhMucChiTiet.value.find(i => i.id === formData.value.idDanhMucChiTiet);
        return found ? found.tenDanhMucChiTiet : '';
    });
    const closeAllDropdowns = () => { isCatDropdownOpen.value = false; isSubCatDropdownOpen.value = false; };

    // --- LOGIC ẢNH & CHECK TRÙNG (Giữ nguyên logic, chỉ bỏ showAlert cũ) ---
    const mainFileInput = ref(null);
    const detailFileInput = ref(null);

    const isFoodNameDuplicate = (name) => {
        if (!name) return false;
        return existingFoods.value.some(foodName => foodName.toLowerCase() === name.trim().toLowerCase());
    };

    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image();
                img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
            reader.onerror = error => reject(error);
        });
    };

    const triggerMainImageUpload = () => mainFileInput.value.click();
    const handleMainFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            Swal.fire({ icon: 'error', title: 'Lỗi định dạng', text: 'Vui lòng chọn file hình ảnh!' });
            return;
        }
        try {
            const resized = await resizeImage(file);
            formData.value.hinhAnh = resized;
            errors.value.hinhAnh = ''; // Xóa lỗi ảnh
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    const triggerDetailImageUpload = () => detailFileInput.value.click();
    const handleDetailImageUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            Swal.fire({ icon: 'error', title: 'Lỗi định dạng', text: 'Vui lòng chọn file hình ảnh!' });
            return;
        }
        try {
            const resized = await resizeImage(file);
            newDetail.value.hinhAnh = resized;
            detailErrors.value.hinhAnh = ''; // Xóa lỗi ảnh chi tiết
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    onMounted(async () => {
        /* ... (Giữ nguyên logic fetch data) ... */
        try {
            const resDM = await getAllCategory(); listDanhMuc.value = resDM.data;
            const resDMCT = await getAllCategoryDetail(); listDanhMucChiTiet.value = resDMCT.data;
            const resFood = await getAllFoodGeneral();
            if (resFood?.data) existingFoods.value = resFood.data.map(f => f.tenMonAn);
            const resDetail = await getAllFoodDetail();
            if (resDetail?.data) existingDetails.value = resDetail.data.map(d => d.tenChiTietMonAn);
        } catch (e) { console.error(e); }
    });

    // --- HÀM VALIDATE MỚI ---
    const validateMainForm = () => {
        let isValid = true;
        errors.value = { tenMonAn: '', idDanhMuc: '', idDanhMucChiTiet: '', hinhAnh: '', listChiTiet: '' }; // Reset lỗi

        if (!formData.value.tenMonAn.trim()) {
            errors.value.tenMonAn = 'Tên món ăn không được bỏ trống.';
            isValid = false;
        } else if (formData.value.tenMonAn.length < 5) {
            errors.value.tenMonAn = 'Tên món ăn phải có ít nhất 5 ký tự.';
            isValid = false;
        } else if (isFoodNameDuplicate(formData.value.tenMonAn)) {
            errors.value.tenMonAn = 'Tên món ăn đã tồn tại trong hệ thống.';
            isValid = false;
        }

        if (!formData.value.idDanhMuc) {
            errors.value.idDanhMuc = 'Vui lòng chọn danh mục gốc.';
            isValid = false;
        }
        if (!formData.value.idDanhMucChiTiet) {
            errors.value.idDanhMucChiTiet = 'Vui lòng chọn chi tiết danh mục.';
            isValid = false;
        }
        if (!formData.value.hinhAnh) {
            errors.value.hinhAnh = 'Vui lòng chọn hình ảnh món ăn.';
            isValid = false;
        }
        if (listChiTiet.value.length === 0) {
            errors.value.listChiTiet = 'Vui lòng thêm ít nhất một biến thể món ăn.';
            isValid = false;
        }

        return isValid;
    };

    const isDetailNameDuplicate = (name) => {
        if (!name) return false;
        const normalizedInput = name.trim().toLowerCase();

        // Check trong list đang thêm (Client side)
        const isDuplicateInCurrentList = listChiTiet.value.some(item => 
            item.tenChiTietMonAn && // Kiểm tra tồn tại trước
            item.tenChiTietMonAn.trim().toLowerCase() === normalizedInput
        );
        if (isDuplicateInCurrentList) return true;

        // Check trong DB (Server side data) -> NGUYÊN NHÂN GÂY LỖI Ở ĐÂY
        // Cần kiểm tra d.tenChiTietMonAn có tồn tại không trước khi toLowerCase()
        const isDuplicateInDB = existingDetails.value.some(d => 
            d.tenChiTietMonAn && 
            d.tenChiTietMonAn.trim().toLowerCase() === normalizedInput
        );
        
        return isDuplicateInDB; 
    };

    // --- 2. THÊM VALIDATE CHI TIẾT (ĐẦY ĐỦ) ---
    const validateDetailForm = () => {
        let isValid = true;
        // Reset lỗi cũ
        detailErrors.value = { tenChiTietMonAn: '', kichCo: '', giaBan: '', hinhAnh: '' };

        // 1. Validate Tên
        if (!newDetail.value.tenChiTietMonAn || !newDetail.value.tenChiTietMonAn.trim()) {
            detailErrors.value.tenChiTietMonAn = 'Vui lòng nhập tên chi tiết.';
            isValid = false;
        } else if (newDetail.value.tenChiTietMonAn.length < 2) { // Ví dụ độ dài tối thiểu
            detailErrors.value.tenChiTietMonAn = 'Tên quá ngắn.';
            isValid = false;
        } else if (isDetailNameDuplicate(newDetail.value.tenChiTietMonAn)) {
            detailErrors.value.tenChiTietMonAn = 'Tên biến thể này đã tồn tại!';
            isValid = false;
        }

        // 2. Validate Kích cỡ
        if (!newDetail.value.kichCo || !newDetail.value.kichCo.trim()) {
            detailErrors.value.kichCo = 'Vui lòng nhập kích cỡ.';
            isValid = false;
        }

        // 3. Validate Giá bán
        if (newDetail.value.giaBan === "" || newDetail.value.giaBan === null || newDetail.value.giaBan < 0) {
            detailErrors.value.giaBan = 'Giá bán không hợp lệ.';
            isValid = false;
        }

        // 4. Validate Ảnh
        if (!newDetail.value.hinhAnh) {
            detailErrors.value.hinhAnh = 'Vui lòng chọn ảnh.';
            isValid = false;
        }

        return isValid;
    };

    // --- 3. CẬP NHẬT HÀM THÊM VÀO LIST ---
    const addDetailToList = () => {
        // Gọi hàm validate trước
        if (!validateDetailForm()) {
            // Nếu không hợp lệ -> Dừng lại (Lỗi đỏ đã hiện ra nhờ biến detailErrors)
            return; 
        }

        // Nếu hợp lệ -> Thêm vào list
        listChiTiet.value.push({
            ...newDetail.value,
            tenChiTietMonAn: newDetail.value.tenChiTietMonAn.trim(),
            kichCo: newDetail.value.kichCo.trim(),
            donVi: newDetail.value.donVi ? newDetail.value.donVi.trim() : 'Cốc',
            giaBan: Number(newDetail.value.giaBan),
            giaVon: Number(newDetail.value.giaVon) || 0
        });

        // Xóa lỗi chung của list (nếu có)
        errors.value.listChiTiet = ''; 

        // Reset form nhập chi tiết về ban đầu
        newDetail.value = { 
            tenChiTietMonAn: '', 
            giaBan: 0, 
            giaVon: 0, 
            kichCo: '', 
            donVi: 'Cốc', 
            trangThai: 1, 
            hinhAnh: '' 
        };
        
        // Reset lỗi form con
        detailErrors.value = { tenChiTietMonAn: '', kichCo: '', giaBan: '', hinhAnh: '' };
    };

    const removeDetailFromList = (index) => {
        Swal.fire({
            title: 'Xóa biến thể?',
            text: "Bạn có chắc chắn muốn xóa dòng này?",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Xóa',
            cancelButtonText: 'Hủy'
        }).then((result) => {
            if (result.isConfirmed) {
                listChiTiet.value.splice(index, 1);
            }
        });
    };

    const handleSave = async () => {
        if (!validateMainForm()) {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi nhập liệu',
                text: 'Bạn chưa nhập đủ thông tin hoặc thông tin không hợp lệ.',
                toast: true,
                position: 'top-end',
                showConfirmButton: false,
                timer: 3000,
                timerProgressBar: true,
                didOpen: (toast) => {
                      toast.addEventListener("mouseenter", Swal.stopTimer);
                      toast.addEventListener("mouseleave", Swal.resumeTimer);
                }
            });
            return;
        }

        // 2. Confirm Save
        Swal.fire({
            title: 'Xác nhận thêm mới?',
            text: `Bạn có chắc chắn muốn thêm món "${formData.value.tenMonAn}"?`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#3085d6',
            cancelButtonColor: '#d33',
            confirmButtonText: 'Đồng ý',
            cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const payload = {
                        ...formData.value,
                        tenMonAn: formData.value.tenMonAn.trim(),
                        moTa: formData.value.moTa ? formData.value.moTa.trim() : '',
                        listChiTiet: listChiTiet.value
                    };
                    await postNewFood(payload);
                    
                    Swal.fire({
                        icon: 'success',
                        title: 'Thành công!',
                        text: 'Thêm món ăn mới thành công.',
                        timer: 1500,
                        showConfirmButton: false
                    });
                    
                    setTimeout(() => router.back(), 1500);
                } catch (e) {
                    console.error(e);
                    Swal.fire({
                        icon: 'error',
                        title: 'Lỗi hệ thống',
                        text: 'Đã xảy ra lỗi khi lưu dữ liệu.'
                    });
                }
            }
        });
    };

    const goBack = () => router.back();

    return {
        formData, listDanhMuc, filteredSubCategories, listChiTiet, newDetail, 
        handleSave, goBack, addDetailToList, removeDetailFromList,
        mainFileInput, handleMainFileUpload, triggerMainImageUpload,
        detailFileInput, handleDetailImageUpload, triggerDetailImageUpload,
        
        // Export biến Dropdown
        isCatDropdownOpen, catSearchQuery, toggleCatDropdown, filteredCategories, selectCategory, selectedCategoryName,
        isSubCatDropdownOpen, subCatSearchQuery, toggleSubCatDropdown, selectSubCategory, selectedSubCategoryName, closeAllDropdowns,

        // Export biến Error
        errors, detailErrors
    };
}

export function useHotpotAdd() {
    const router = useRouter();
    
    // --- 1. STATE ---
    const formData = ref({
        tenSetLau: '',
        idLoaiSet: '',
        moTaChiTiet: '',
        giaBan: 0,
        hinhAnh: '',
        moTa: '',
        trangThai: 1
    });

    // Biến lưu lỗi Validation
    const errors = ref({
        tenSetLau: '',
        idLoaiSet: '',
        moTaChiTiet: '',
        giaBan: '',
        hinhAnh: '',
        selectedIngredients: '' // Lỗi cho danh sách thành phần
    });

    // Dữ liệu API
    const listLoaiSet = ref([]);
    const listFoodDetails = ref([]); 
    const existingSetNames = ref([]); 

    // Dữ liệu thao tác
    const selectedIngredients = ref([]);
    const searchQuery = ref("");
    const sortOption = ref("name_asc");

    // --- 2. CHECK TRÙNG TÊN ---
    const isSetNameDuplicate = (name) => {
        if (!name) return false;
        const normalizedName = name.trim().toLowerCase();
        return existingSetNames.value.some(dbName => dbName.toLowerCase() === normalizedName);
    };

    // --- 3. COMPUTED ---
    const filteredFoodList = computed(() => {
        let result = listFoodDetails.value;
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenChiTietMonAn || item.tenDanhMucChiTiet || '').toLowerCase().includes(query)
            );
        }
        return result.sort((a, b) => {
            const nameA = (a.tenChiTietMonAn || a.tenDanhMucChiTiet || '').toLowerCase();
            const nameB = (b.tenChiTietMonAn || b.tenDanhMucChiTiet || '').toLowerCase();
            if (sortOption.value === 'price_asc') return a.giaBan - b.giaBan;
            if (sortOption.value === 'price_desc') return b.giaBan - a.giaBan;
            return nameA.localeCompare(nameB);
        });
    });

    const totalComponentsPrice = computed(() => {
        return selectedIngredients.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
    });

    // --- 4. FETCH DATA ---
    const fetchInitialData = async() => {
        try {
            const [resLoaiSet, resFoodDetail, resAllHotpots] = await Promise.all([
                getAllCategoryHotpot(),
                getAllFoodDetail(),
                getAllHotpotGeneral() 
            ]);

            listLoaiSet.value = resLoaiSet.data;
            listFoodDetails.value = resFoodDetail.data;

            if (resAllHotpots && resAllHotpots.data) {
                existingSetNames.value = resAllHotpots.data.map(set => set.tenSetLau);
            }
        } catch (e) {
            console.error("Lỗi load dữ liệu:", e);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể tải dữ liệu hệ thống!' });
        }
    };

    onMounted(() => { fetchInitialData(); });

    // --- 5. ACTIONS ---
    const addIngredient = (foodItem) => {
        const exists = selectedIngredients.value.find(item => item.id === foodItem.id);
        if (exists) {
            exists.soLuong += 1;
        } else {
            selectedIngredients.value.push({
                id: foodItem.id,
                ten: foodItem.tenChiTietMonAn || foodItem.tenDanhMucChiTiet,
                donVi: foodItem.donVi || 'Phần',
                giaBan: foodItem.giaBan,
                hinhAnh: foodItem.hinhAnh,
                soLuong: 1
            });
        }
        errors.value.selectedIngredients = ''; // Xóa lỗi khi thêm
    };

    const removeIngredient = (index) => {
        // Dùng Swal Confirm cho đẹp (Optional)
        selectedIngredients.value.splice(index, 1);
    };

    // --- 6. UPLOAD ẢNH ---
    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image();
                img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Vui lòng chọn file hình ảnh!' });
            return;
        }
        try {
            const resized = await resizeImage(file);
            formData.value.hinhAnh = resized;
            errors.value.hinhAnh = ''; 
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    const goBack = () => router.back();

    // --- 7. VALIDATE & SAVE ---
    const validateForm = () => {
        let isValid = true;
        errors.value = { tenSetLau: '', idLoaiSet: '', giaBan: '', hinhAnh: '', selectedIngredients: '', moTaChiTiet: '' };

        const setName = formData.value.tenSetLau ? formData.value.tenSetLau.trim() : '';
        if (!setName) {
            errors.value.tenSetLau = 'Vui lòng nhập tên Set Lẩu.'; isValid = false;
        } else if (setName.length < 5) {
            errors.value.tenSetLau = 'Tên set lẩu phải chứa trên 5 kí tự.'; isValid = false;
        } else if (isSetNameDuplicate(setName)) {
            errors.value.tenSetLau = 'Tên Set Lẩu này đã tồn tại!'; isValid = false;
        }

        if (!formData.value.moTaChiTiet) {
            errors.value.moTaChiTiet = 'Vui lòng nhập định lượng.'; isValid = false;
        }

        if (!formData.value.idLoaiSet) { errors.value.idLoaiSet = 'Vui lòng chọn Loại Set.'; isValid = false; }
        
        if (!formData.value.giaBan || formData.value.giaBan < 0) { 
            errors.value.giaBan = 'Giá bán không hợp lệ.'; isValid = false; 
        }

        if (!formData.value.hinhAnh) { errors.value.hinhAnh = 'Vui lòng chọn hình ảnh.'; isValid = false; }

        if (selectedIngredients.value.length === 0) {
            errors.value.selectedIngredients = 'Vui lòng chọn ít nhất 1 món ăn vào Set.'; isValid = false;
        }

        return isValid;
    };

    const handleSave = async() => {
        if (!validateForm()) {
            Swal.fire({
                icon: 'error', title: 'Lỗi nhập liệu',
                text: 'Vui lòng kiểm tra lại thông tin.',
                toast: true, position: 'top-end', showConfirmButton: false, timer: 3000
            });
            return;
        }

        Swal.fire({
            title: 'Xác nhận thêm mới?',
            text: 'Bạn có chắc chắn muốn thêm Set Lẩu này?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#3085d6', cancelButtonColor: '#d33',
            confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const payload = {
                        ...formData.value,
                        moTaChiTiet: formData.value.moTaChiTiet.trim(),
                        tenSetLau: formData.value.tenSetLau.trim(),
                        moTa: formData.value.moTa ? formData.value.moTa.trim() : '',
                        listChiTietSetLau: selectedIngredients.value.map(item => ({
                            idChiTietMonAn: item.id,
                            soLuong: item.soLuong
                        }))
                    };

                    await postNewHotpot(payload);
                    
                    Swal.fire({
                        icon: 'success', title: 'Thành công!',
                        text: 'Thêm mới thành công.',
                        timer: 1500, showConfirmButton: false
                    });
                    
                    setTimeout(() => {
                        router.push({ name: 'foodManager', query: { tab: 'setlau' } });
                    }, 1500);

                } catch (e) {
                    console.error(e);
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Lỗi khi thêm mới! Vui lòng thử lại.' });
                }
            }
        });
    };

    return {
        formData, listLoaiSet, selectedIngredients, totalComponentsPrice,
        searchQuery, sortOption, filteredFoodList,
        addIngredient, removeIngredient, handleSave, goBack, handleFileUpload,
        errors // Export errors
    };
}

export function useFoodDetailAdd() {
    const router = useRouter();
    const route = useRoute();
    const parentIdFromUrl = route.query.parentId;

    // --- STATE ---
    const formData = ref({
        tenChiTietMonAn: '',
        idMonAnDiKem: '',
        moTaChiTiet: '',
        giaBan: 0,
        giaVon: 0,
        kichCo: '',
        donVi: '',
        hinhAnh: '',
        trangThai: 1
    });

    // Biến lưu lỗi Validation
    const errors = ref({
        tenChiTietMonAn: '',
        idMonAnDiKem: '',
        giaBan: '',
        hinhAnh: '',
        kichCo: '',
        donVi: ''
    });

    const listMonAn = ref([]); 
    const isParentLocked = ref(false); 
    const parentFoodName = ref('');
    const existingDetailNames = ref([]);
    const allGlobalDetails = ref([]);

    // Search & Sort
    const searchQuery = ref('');
    const sortOption = ref('name_asc');

    // --- HÀM CHECK TRÙNG TÊN (AN TOÀN) ---
    const isDetailNameDuplicate = (name) => {
        if (!name) return false;
        const normalizedName = name.trim().toLowerCase();
        // Kiểm tra an toàn để tránh lỗi crash nếu dữ liệu null
        return allGlobalDetails.value.some(d => 
            d.tenChiTietMonAn && d.tenChiTietMonAn.toLowerCase() === normalizedName
        );
    };

    // --- COMPUTED: LỌC MÓN ---
    const filteredMonAnList = computed(() => {
        let result = [...listMonAn.value];
        if (searchQuery.value) {
            const query = searchQuery.value.toLowerCase().trim();
            result = result.filter(item => 
                (item.tenMonAn && item.tenMonAn.toLowerCase().includes(query)) ||
                (item.maMonAn && item.maMonAn.toLowerCase().includes(query))
            );
        }
        // Logic sort đơn giản
        return result.sort((a, b) => {
            const nameA = (a.tenMonAn || '').toLowerCase();
            const nameB = (b.tenMonAn || '').toLowerCase();
            if (sortOption.value === 'price_asc') return (a.giaBan || 0) - (b.giaBan || 0);
            if (sortOption.value === 'price_desc') return (b.giaBan || 0) - (a.giaBan || 0);
            return nameA.localeCompare(nameB); 
        });
    });

    const getPriceRange = (foodItem) => {
        const childDetails = allGlobalDetails.value.filter(detail => {
            if (detail.idMonAnDiKem && typeof detail.idMonAnDiKem === 'object') {
                return detail.idMonAnDiKem.id === foodItem.id;
            }
            return String(detail.idMonAnDiKem) === String(foodItem.id);
        });

        if (childDetails.length === 0) return foodItem.giaBan ? `${foodItem.giaBan.toLocaleString()}đ` : 'Chưa có giá';
        
        const prices = childDetails.map(d => d.giaBan).filter(p => p !== null && p >= 0);
        if (prices.length === 0) return 'Chưa có giá';

        const min = Math.min(...prices);
        const max = Math.max(...prices);
        return (min === max) ? `${min.toLocaleString()}đ` : `${min.toLocaleString()} - ${max.toLocaleString()}đ`;
    };

    const selectParentFood = (food) => { 
        if (isParentLocked.value) return; 
        formData.value.idMonAnDiKem = food.id;
        errors.value.idMonAnDiKem = ''; // Xóa lỗi khi chọn
    };

    // --- FETCH DATA ---
    const fetchInitialData = async() => {
        try {
            const [resAllFood, resAllDetails] = await Promise.all([
                getAllFoodGeneral(),
                getAllFoodDetail()
            ]);
            
            listMonAn.value = resAllFood.data;
            if (resAllDetails && resAllDetails.data) {
                allGlobalDetails.value = resAllDetails.data;
            }

            if (parentIdFromUrl) {
                const pId = parseInt(parentIdFromUrl);
                const parent = listMonAn.value.find(f => f.id === pId);
                if (parent) {
                    formData.value.idMonAnDiKem = pId;
                    isParentLocked.value = true;
                    parentFoodName.value = parent.tenMonAn;
                }
            }
        } catch (e) {
            console.error("Lỗi load dữ liệu:", e);
            Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không tải được dữ liệu hệ thống!' });
        }
    };

    onMounted(() => { fetchInitialData(); });

    // --- UPLOAD ẢNH ---
    const resizeImage = (file, maxWidth = 800) => {
        return new Promise((resolve) => {
            const reader = new FileReader();
            reader.readAsDataURL(file);
            reader.onload = (e) => {
                const img = new Image();
                img.src = e.target.result;
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    let w = img.width, h = img.height;
                    if (w > maxWidth) { h *= maxWidth / w; w = maxWidth; }
                    canvas.width = w; canvas.height = h;
                    canvas.getContext('2d').drawImage(img, 0, 0, w, h);
                    resolve(canvas.toDataURL('image/jpeg', 0.8));
                };
            };
        });
    };

    const handleFileUpload = async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        if (!file.type.match('image.*')) {
            Swal.fire({ icon: 'error', title: 'Lỗi định dạng', text: 'Vui lòng chọn file ảnh!' });
            return;
        }
        try {
            const resized = await resizeImage(file);
            formData.value.hinhAnh = resized;
            errors.value.hinhAnh = ''; // Xóa lỗi ảnh
        } catch (e) { console.error(e); }
        event.target.value = '';
    };

    // --- VALIDATION & SAVE ---
    const validateForm = () => {
        let isValid = true;
        // Reset errors
        errors.value = { tenChiTietMonAn: '', idMonAnDiKem: '', giaBan: '', hinhAnh: '', kichCo: '', donVi: '' };

        if (!formData.value.idMonAnDiKem) {
            errors.value.idMonAnDiKem = 'Vui lòng chọn món ăn gốc từ danh sách bên phải.';
            isValid = false;
        }

        const detailName = formData.value.tenChiTietMonAn ? formData.value.tenChiTietMonAn.trim() : '';
        if (!detailName) {
            errors.value.tenChiTietMonAn = 'Vui lòng nhập tên chi tiết.';
            isValid = false;
        } else if (detailName.length < 5) {
            errors.value.tenChiTietMonAn = 'Tên chi tiết phải có ít nhất 5 ký tự.';
            isValid = false;
        } else if (isDetailNameDuplicate(detailName)) {
            errors.value.tenChiTietMonAn = 'Tên chi tiết này đã tồn tại trong hệ thống.';
            isValid = false;
        }

        if (formData.value.giaBan === "" || formData.value.giaBan < 0) {
            errors.value.giaBan = 'Giá bán không hợp lệ.';
            isValid = false;
        }

        if (!formData.value.kichCo) { errors.value.kichCo = 'Nhập kích cỡ.'; isValid = false; }
        if (!formData.value.donVi) { errors.value.donVi = 'Nhập đơn vị.'; isValid = false; }

        if (!formData.value.hinhAnh) {
            errors.value.hinhAnh = 'Vui lòng chọn hình ảnh.';
            isValid = false;
        }

        return isValid;
    };

    const handleSave = async() => {
        if (!validateForm()) {
            Swal.fire({
                icon: 'error',
                title: 'Lỗi nhập liệu',
                text: 'Vui lòng kiểm tra lại các trường báo đỏ.',
                toast: true, position: 'top-end', showConfirmButton: false, timer: 3000, timerProgressBar: true
            });
            return;
        }

        Swal.fire({
            title: 'Xác nhận thêm mới?',
            text: 'Bạn có chắc chắn muốn thêm chi tiết này?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: '#3085d6', cancelButtonColor: '#d33',
            confirmButtonText: 'Đồng ý', cancelButtonText: 'Hủy'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    const payload = {
                        ...formData.value,
                        tenChiTietMonAn: formData.value.tenChiTietMonAn.trim(),
                        trangThai: Number(formData.value.trangThai)
                    };
                    await postNewFoodDetail(payload);
                    
                    Swal.fire({
                        icon: 'success', title: 'Thành công!',
                        text: 'Thêm chi tiết mới thành công.',
                        timer: 1500, showConfirmButton: false
                    });
                    
                    setTimeout(() => router.back(), 1500);
                } catch (e) {
                    console.error(e);
                    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Đã xảy ra lỗi khi lưu dữ liệu!' });
                }
            }
        });
    };

    const goBack = () => router.back();

    return {
        formData, listMonAn, isParentLocked, parentFoodName,
        searchQuery, sortOption, filteredMonAnList, selectParentFood,
        handleSave, goBack, handleFileUpload, getPriceRange,
        errors // Export errors
    };
}

export function useCategoryAddModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });

    // Reset form khi mở modal
    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    // --- 3. VALIDATE FORM ---
    const validateForm = () => {
        if (!formData.value.tenDanhMuc || !formData.value.tenDanhMuc.trim()) {
            showAlert("Vui lòng nhập tên danh mục!", "Thiếu thông tin");
            return false;
        }

        if (formData.value.tenDanhMuc.length < 5) {
            showAlert("Tên danh mục phải chứa ít nhất 5 kí tự", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. HANDLE SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Hiện Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn thêm danh mục mới này?",
            async () => {
                try {
                    // Gọi API Thêm mới
                    await postNewCategory(formData.value);

                    showSuccess("Thêm danh mục thành công!");

                    // Refresh danh sách cha
                    emit('refresh');

                    // Đóng modal sau một chút để người dùng kịp đọc thông báo thành công
                    setTimeout(() => {
                        emit('close');
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi thêm danh mục:", e);
                    showError("Đã xảy ra lỗi khi thêm mới!");
                }
            },
            "Xác nhận thêm mới"
        );
    };

    return {
        formData,
        handleSave,
        
        // Return các biến của Dialog để template sử dụng
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryPutModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        id: null,
        maDanhMuc: '',
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });

    // Theo dõi props để đổ dữ liệu vào form khi mở modal
    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            // Clone object để tránh tham chiếu ngược
            formData.value = { ...newItem };
        } else {
            formData.value = {
                id: null,
                maDanhMuc: '',
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    // --- 3. VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenDanhMuc || !formData.value.tenDanhMuc.trim()) {
            showAlert("Vui lòng nhập tên danh mục!", "Thiếu thông tin");
            return false;
        }
        if (formData.value.tenDanhMuc.length < 5) {
            showAlert("Tên danh mục phải chứa ít nhất 5 kí tự", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn lưu các thay đổi này?",
            async () => {
                try {
                    // Gọi API Cập nhật
                    await putNewCategory(formData.value.id, formData.value);

                    showSuccess("Cập nhật thành công!");

                    // Refresh danh sách cha
                    emit('refresh');
                    
                    // Đợi 1s để người dùng thấy thông báo thành công rồi mới đóng
                    setTimeout(() => {
                        closeModal();
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi cập nhật:", e);
                    showError("Đã xảy ra lỗi khi cập nhật!");
                }
            },
            "Xác nhận cập nhật"
        );
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,

        // Return biến Dialog
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryHotpotPutModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        id: null,
        maLoaiSet: '',
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });

    // Reset/Fill form khi props thay đổi
    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = { ...newItem };
        } else {
            formData.value = {
                id: null,
                maLoaiSet: '',
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    // --- 3. VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenLoaiSet || !formData.value.tenLoaiSet.trim()) {
            showAlert("Vui lòng nhập tên loại set!", "Thiếu thông tin");
            return false;
        }

        if (formData.value.tenLoaiSet.length < 5) {
            showAlert("Tên loại set phải chứa ít nhất 5 kí tự", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. HANDLE SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn cập nhật loại set này?",
            async () => {
                try {
                    // Gọi API
                    await putNewHotpotCategory(formData.value.id, formData.value);

                    showSuccess("Cập nhật thành công!");

                    // Refresh danh sách cha
                    emit('refresh');
                    
                    // Đóng modal sau 1 giây để người dùng kịp đọc thông báo
                    setTimeout(() => {
                        closeModal();
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi cập nhật:", e);
                    showError("Đã xảy ra lỗi khi cập nhật!");
                }
            },
            "Xác nhận cập nhật"
        );
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,

        // Return biến Dialog
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}


export function useHotpotCategoryAddModal(props, emit) {

    // --- 2. GỌI DIALOG ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const formData = ref({
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });

    // Reset form khi mở modal
    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    // --- 3. VALIDATE ---
    const validateForm = () => {
        if (!formData.value.tenLoaiSet || !formData.value.tenLoaiSet.trim()) {
            showAlert("Vui lòng nhập tên loại set!", "Thiếu thông tin");
            return false;
        }

        if (formData.value.tenLoaiSet.length < 5) {
            showAlert("Tên loại set phải chứa ít nhất 5 kí tự", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- 4. HANDLE SAVE VỚI CONFIRM ---
    const handleSave = async() => {
        // Bước 1: Validate
        if (!validateForm()) return;

        // Bước 2: Confirm Dialog
        showConfirm(
            "Bạn có chắc chắn muốn thêm loại set lẩu mới này?",
            async () => {
                try {
                    // Gọi API
                    await postNewHotpotCategory(formData.value);

                    showSuccess("Thêm mới thành công!");

                    // Refresh danh sách cha
                    emit('refresh');
                    
                    // Đóng modal sau 1 giây
                    setTimeout(() => {
                        emit('close');
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi thêm mới:", e);
                    showError("Đã xảy ra lỗi khi thêm mới!");
                }
            },
            "Xác nhận thêm mới"
        );
    };

    return {
        formData,
        handleSave,

        // Return biến Dialog
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryDetailAddModal(props, emit) {

    const listDanhMucGoc = ref([]);
    const searchQuery = ref('');
    const isDropdownOpen = ref(false);

    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, 
        handleConfirm, handleClose 
    } = useDialog();

    // Initial Form Data
    const formData = ref({
        idDanhMuc: '',
        tenDanhMucChiTiet: '',
        moTa: '',
        trangThai: 1
    });

    // Fetch Categories
    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMucGoc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllCategories();
    });

    // --- Searchable Dropdown Logic ---

    // Toggle Dropdown Visibility
    const toggleDropdown = () => {
        isDropdownOpen.value = !isDropdownOpen.value;
        if (isDropdownOpen.value) {
            searchQuery.value = ''; // Reset search on open
        }
    };

    // Filtered List based on Search Query
    const filteredCategories = computed(() => {
        if (!searchQuery.value) return listDanhMucGoc.value;
        const query = searchQuery.value.toLowerCase();
        return listDanhMucGoc.value.filter(item => 
            item.tenDanhMuc.toLowerCase().includes(query)
        );
    });

    // Select an Item
    const selectCategory = (category) => {
        formData.value.idDanhMuc = category.id;
        isDropdownOpen.value = false; // Close dropdown
    };

    // Get Selected Category Name for Display
    const selectedCategoryName = computed(() => {
        const found = listDanhMucGoc.value.find(item => item.id === formData.value.idDanhMuc);
        return found ? found.tenDanhMuc : '';
    });

    // Close Dropdown when clicking outside (Optional helper)
    const closeDropdown = () => {
        isDropdownOpen.value = false;
    };


    // --- Reset Form on Open ---
    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                idDanhMuc: '',
                tenDanhMucChiTiet: '',
                moTa: '',
                trangThai: 1
            };
            isDropdownOpen.value = false;
            searchQuery.value = '';
        }
    });

    // --- Save Handler with Validation ---
    const handleSave = async () => {
        // 1. Validation
        if (!formData.value.tenDanhMucChiTiet || formData.value.tenDanhMucChiTiet.trim() === "") {
            showAlert("Vui lòng nhập tên chi tiết danh mục!");
            return;
        }

        if (formData.value.tenDanhMucChiTiet.length < 5) {
            showAlert("Tên chi tiết danh mục phải có ít nhất 5 ký tự!");
            return;
        }

        if (!formData.value.idDanhMuc) {
            showAlert("Vui lòng chọn danh mục gốc!");
            return;
        }

        try {
            await postNewCategoryDetail(formData.value);
            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            showAlert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        handleSave,
        listDanhMucGoc,
        
        // Export variables for Template
        searchQuery,
        filteredCategories,
        isDropdownOpen,
        toggleDropdown,
        selectCategory,
        selectedCategoryName,
        closeDropdown,

        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useCategoryDetailPutModal(props, emit) {

    // --- 1. Init Dialog ---
    const { 
        isVisible, dialogConfig, 
        showAlert, showError, showSuccess, showConfirm, 
        handleConfirm, handleClose 
    } = useDialog();

    const listDanhMucGoc = ref([]);
    const searchQuery = ref('');
    const isDropdownOpen = ref(false);

    // Initial Form Data
    const formData = ref({
        id: null,
        maDanhMucChiTiet: '',
        idDanhMuc: '',
        tenDanhMucChiTiet: '',
        moTa: '',
        trangThai: 1
    });

    // Fetch Categories
    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMucGoc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }
    
    onMounted(() => {
        getAllCategories();
    });

    // --- Fill Form Data on Open ---
    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = { ...newItem };
        } else {
            // Reset if needed
            formData.value = {
                id: null,
                maDanhMucChiTiet: '',
                idDanhMuc: '',
                tenDanhMucChiTiet: '',
                moTa: '',
                trangThai: 1
            };
        }
        isDropdownOpen.value = false;
        searchQuery.value = '';
    }, { immediate: true });


    // --- Searchable Dropdown Logic ---
    const toggleDropdown = () => {
        isDropdownOpen.value = !isDropdownOpen.value;
        if (isDropdownOpen.value) {
            searchQuery.value = '';
        }
    };

    const filteredCategories = computed(() => {
        if (!searchQuery.value) return listDanhMucGoc.value;
        const query = searchQuery.value.toLowerCase();
        return listDanhMucGoc.value.filter(item => 
            item.tenDanhMuc.toLowerCase().includes(query)
        );
    });

    const selectCategory = (category) => {
        formData.value.idDanhMuc = category.id;
        isDropdownOpen.value = false;
    };

    const selectedCategoryName = computed(() => {
        const found = listDanhMucGoc.value.find(item => item.id === formData.value.idDanhMuc);
        return found ? found.tenDanhMuc : '';
    });

    const closeDropdown = () => {
        isDropdownOpen.value = false;
    };


    // --- Validation ---
    const validateForm = () => {
        if (!formData.value.tenDanhMucChiTiet || formData.value.tenDanhMucChiTiet.trim() === "") {
            showAlert("Vui lòng nhập tên chi tiết!", "Thiếu thông tin");
            return false;
        }
        if (formData.value.tenDanhMucChiTiet.length < 5) {
            showAlert("Tên chi tiết danh mục phải có ít nhất 5 ký tự!", "Dữ liệu không hợp lệ", "warning");
            return false;
        }
        if (!formData.value.idDanhMuc) {
            showAlert("Vui lòng chọn danh mục gốc!", "Thiếu thông tin");
            return false;
        }
        return true;
    };

    // --- Save Handler ---
    const handleSave = async () => {
        if (!validateForm()) return;

        showConfirm(
            "Bạn có chắc chắn muốn cập nhật thông tin này?",
            async () => {
                try {
                    await putCategoryDetail(formData.value.id, formData.value);
                    
                    showSuccess("Cập nhật thành công!");
                    emit('refresh');
                    
                    setTimeout(() => {
                        closeModal();
                    }, 1000);

                } catch (e) {
                    console.error("Lỗi cập nhật:", e);
                    showError("Đã xảy ra lỗi khi cập nhật!");
                }
            },
            "Xác nhận cập nhật"
        );
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,
        listDanhMucGoc,

        // Dropdown variables
        searchQuery,
        filteredCategories,
        isDropdownOpen,
        toggleDropdown,
        selectCategory,
        selectedCategoryName,
        closeDropdown,

        // Dialog variables
        dialogVisible: isVisible,
        dialogConfig,
        handleDialogConfirm: handleConfirm,
        handleDialogClose: handleClose
    };
}

export function useDialog() {
    const isVisible = ref(false);
    const dialogConfig = ref({
        type: 'alert',      
        variant: 'warning', 
        title: '',
        message: '',
        onConfirm: null 
    });

    const showAlert = (message, title = "Thông báo", variant = "warning") => {
        dialogConfig.value = {
            type: 'alert',
            variant,
            title,
            message,
            onConfirm: null
        };
        isVisible.value = true;
    };

    const showSuccess = (message) => showAlert(message, "Thành công", "success");
    
    const showError = (message) => showAlert(message, "Lỗi", "error");

    const showConfirm = (message, onConfirmCallback, title = "Xác nhận", variant = "warning") => {
        dialogConfig.value = {
            type: 'confirm',
            variant,
            title,
            message,
            onConfirm: onConfirmCallback
        };
        isVisible.value = true;
    };

    const handleConfirm = () => {
        if (dialogConfig.value.onConfirm) {
            dialogConfig.value.onConfirm();
        }
        isVisible.value = false;
    };

    const handleClose = () => {
        isVisible.value = false;
    };

    return {
        isVisible,
        dialogConfig,
        showAlert,
        showSuccess,
        showError,
        showConfirm,
        handleConfirm,
        handleClose
    };
}