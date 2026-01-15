import { ref, shallowRef, watch } from 'vue';

import MenuFood from '../screens/foodFragment/FoodManageGeneral.vue'; 
import MenuHotpot from '../screens/foodFragment/FoodHotPotSetGeneral.vue';
import FoodDetailManageGeneral from '../screens/foodFragment/FoodDetailManageGeneral.vue';

//Trang chính food manager
export function useTabManager() {
  const tabs = {
    'thucdon': MenuFood,
    'setlau': MenuHotpot,
    'chitiet': FoodDetailManageGeneral
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
    const detailData = ref([
        { id: 1, ma: 'CCLA-11-15', ten: 'Chai 1.5 L', monAnGoc: 1, monAnTen: 'Coca-Cola', gia: '18.000', kichCo: '1.5L', donVi: 'Chai', trangThai: true },
        { id: 2, ma: 'CCLA-11-18', ten: 'Chai 1.8 L', monAnGoc: 1, monAnTen: 'Coca-Cola', gia: '20.000', kichCo: '1.8L', donVi: 'Chai', trangThai: false },
    ]);

    const isModalOpen = ref(false);
    const selectedDetail = ref(null);

    const openAddModal = () => {
        selectedDetail.value = null; 
        isModalOpen.value = true;
    };

    const openEditModal = (item) => {
        selectedDetail.value = item;
        isModalOpen.value = true;
    };

    const handleSaveData = (data) => {
        console.log("Dữ liệu đã lưu:", data);
        isModalOpen.value = false;
    };

    return {
        detailData,
        isModalOpen,
        selectedDetail,
        openAddModal,
        openEditModal,
        handleSaveData
    };
}


export function useFoodManager() {
    const mockData = ref([
        { stt: 1, ma: 'BA01', ten: 'Viên thả lẩu phô mai', gia: '40.000 - 60.000 VNĐ', danhmuc: 'Đồ nhúng lẩu', chitiet: '-', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1', trangthai: true },
        { stt: 2, ma: 'BA02', ten: 'Coca-Cola', gia: '18.000 - 26.000 VNĐ', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: false },
        { stt: 3, ma: 'BA03', ten: 'Fanta', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 4, ma: 'BA04', ten: 'Nguyễn Văn E', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 5, ma: 'BA05', ten: 'Nguyễn Văn F', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 6, ma: 'BA06', ten: 'Nguyễn Văn G', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 7, ma: 'BA07', ten: 'Nguyễn Văn H', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-8', trangthai: false },
        { stt: 8, ma: 'BA08', ten: 'Nguyễn Văn Y', gia: '0987654321', danhmuc: 'Đồ uống', chitiet: 'Đồ uống đóng chai', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-010', trangthai: true },
    ]);

    const activeTab = ref('thucdon');

    const isModalOpen = ref(false);
    const selectedItem = ref(null);

    const handleViewDetails = (item) => {
        selectedItem.value = item;
        isModalOpen.value = true;
    };

    return {
        mockData,
        activeTab,
        isModalOpen,
        selectedItem,
        handleViewDetails
    };
}

export function useFoodModal() {
    const currentView = ref('list'); 
    const selectedVariant = ref(null); 

    const variants = ref([
        { id: 1, name: 'Chai 1.5 L', price: '18.000 VNĐ', rawPrice: 18000, code: 'CCLA-11-15' },
        { id: 2, name: 'Chai 1.8 L', price: '20.000 VNĐ', rawPrice: 20000, code: 'CCLA-11-18' },
        { id: 3, name: 'Chai 2 L', price: '23.000 VNĐ', rawPrice: 23000, code: 'CCLA-11-20' },
        { id: 4, name: 'Chai 2.5 L', price: '26.000 VNĐ', rawPrice: 26000, code: 'CCLA-11-25' },
    ]);

    const openEditMode = (variant) => {
        selectedVariant.value = variant;
        currentView.value = 'update';
    };

    const backToList = () => {
        selectedVariant.value = null;
        currentView.value = 'list';
    };

    return {
        currentView,
        selectedVariant,
        variants,
        openEditMode,
        backToList
    };
}

export function useCategoryDetailModal(props, emit) {
    
    const formData = ref({
        ma: '',
        ten: '',
        monAnGoc: '',
        gia: '',
        kichCo: '',
        donVi: '',
        trangThai: true
    });

    const listMonAn = [
        { id: 1, name: 'Coca-Cola' },
        { id: 2, name: 'Fanta' },
        { id: 3, name: 'Viên thả lẩu' }
    ];

    watch(() => props.detailItem, (newVal) => {
        if (newVal) {
            formData.value = { ...newVal }; 
        } else {
            formData.value = {
                ma: '', ten: '', monAnGoc: '', gia: '', kichCo: '', donVi: '', trangThai: true
            };
        }
    });

    const handleSave = () => {
        emit('save', formData.value);
        emit('close');
    };

    return {
        formData,
        listMonAn,
        handleSave
    };
}

export function useHotpotManager() {
    const hotpotData = ref([
        { stt: 1, ma: 'BA01', ten: 'Lẩu tôm chua cay', gia: '200.000 VNĐ', loailau: 'Lẩu hải sản cay', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1', trangthai: true },
        { stt: 2, ma: 'BA02', ten: 'Lẩu bò mỹ', gia: '250.000 VNĐ', loailau: 'Lẩu bò', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: false },
        { stt: 3, ma: 'BA03', ten: 'Set lẩu nấm', gia: '180.000 VNĐ', loailau: 'Lẩu chay', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 4, ma: 'BA04', ten: 'Set lẩu thái', gia: '220.000 VNĐ', loailau: 'Lẩu thái', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 5, ma: 'BA05', ten: 'Set lẩu riêu cua', gia: '300.000 VNĐ', loailau: 'Lẩu riêu', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 6, ma: 'BA06', ten: 'Set lẩu ếch', gia: '200.000 VNĐ', loailau: 'Lẩu ếch', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-2', trangthai: true },
        { stt: 7, ma: 'BA07', ten: 'Set lẩu gà lá é', gia: '250.000 VNĐ', loailau: 'Lẩu gà', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-8', trangthai: false },
        { stt: 8, ma: 'BA08', ten: 'Set lẩu uyên ương', gia: '400.000 VNĐ', loailau: 'Lẩu thập cẩm', tao: '16:00 - 01/01/2026', nguoi: 'ADMIN1-010', trangthai: true },
    ]);

    const isModalOpen = ref(false);
    const selectedHotpot = ref(null);

    const handleViewDetails = (item) => {
        selectedHotpot.value = item;
        isModalOpen.value = true;
    };

    return {
        hotpotData,
        isModalOpen,
        selectedHotpot,
        handleViewDetails
    };
}

export function useHotpotModal(props, emit) {
    const currentView = ref('list'); 
    
    const selectedVariant = ref(null); 

    const variants = ref([
        { id: 1, name: 'Set Nhỏ (2-3 người)', price: '200.000 VNĐ', rawPrice: 200000, code: 'SL-TOM-S' },
        { id: 2, name: 'Set Vừa (4-5 người)', price: '350.000 VNĐ', rawPrice: 350000, code: 'SL-TOM-M' },
        { id: 3, name: 'Set Lớn (6-8 người)', price: '500.000 VNĐ', rawPrice: 500000, code: 'SL-TOM-L' },
        { id: 4, name: 'Set Đặc Biệt', price: '800.000 VNĐ', rawPrice: 800000, code: 'SL-TOM-VIP' },
    ]);

    const openEditMode = (variant) => {
        selectedVariant.value = variant; 
        currentView.value = 'update';    
    };

    const backToList = () => {
        selectedVariant.value = null;
        currentView.value = 'list';
    };

    const closeModal = () => {
        backToList(); 
        emit('close');
    };

    return {
        currentView,
        selectedVariant,
        variants,
        openEditMode,
        backToList,
        closeModal
    };
}