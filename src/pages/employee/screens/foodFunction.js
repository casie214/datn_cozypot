import { nextTick, onMounted, ref, shallowRef, watch } from 'vue';

import MenuFood from '../screens/foodFragment/FoodManageGeneral.vue'; 
import MenuHotpot from '../screens/foodFragment/FoodHotPotSetGeneral.vue';
import FoodDetailManageGeneral from '../screens/foodFragment/FoodDetailManageGeneral.vue';
import axios from 'axios';
import CategoryGeneral from './foodFragment/CategoryGeneral.vue';
import CategoryDetailGeneral from './foodFragment/CategoryDetailGeneral.vue';
import CategoryHotpotGeneral from './foodFragment/CategoryHotpotGeneral.vue';
const API_BASE_EMPLOYEE = "http://localhost:8080/api/manage";

export function getAllFoodGeneral() {
    return axios.get(`${API_BASE_EMPLOYEE}/food`);
}

export function getFoodGeneralModalById(id) {
    return axios.get(`${API_BASE_EMPLOYEE}/food/modal/${id}`);
}

export function getAllHotpotGeneral() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/hotpotGeneral`);
}

export function getAllFoodDetail() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/foodDetail`);
}

export function getAllCategory(){
    return axios.get(`${API_BASE_EMPLOYEE}/food/category`);
}

export function getAllCategoryDetail(){
    return axios.get(`${API_BASE_EMPLOYEE}/food/category/detail`);
}

export function getAllCategoryHotpot(){
    return axios.get(`${API_BASE_EMPLOYEE}/food/category/hotpotType`);
}

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
    const detailData = ref([]);
    
    function getAllFoodDetails() {
        getAllFoodDetail()
    .then(async res => {
      detailData.value = res.data;
      await nextTick();
    })
    .catch(console.error);
    }

    onMounted(() =>{
        getAllFoodDetails();
    })

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
    const mockData = ref([]);

    function getAllFood() {
        getAllFoodGeneral()
    .then(async res => {
      mockData.value = res.data;
      await nextTick();
    })
    .catch(console.error);
    }

    onMounted(() =>{
        getAllFood();
    })

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

export function useFoodModal(itemSource) { // itemSource nên là getter () => props.foodItem
    const currentView = ref('list'); 
    const selectedVariant = ref(null); 
    const variants = ref([]);
    
    const fetchVariants = () => {

        const item = typeof itemSource === 'function' ? itemSource() : itemSource;

        if (!item || !item.id) return;

        getFoodGeneralModalById(item.id)
            .then(async res => {
                variants.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    watch(
        () => (typeof itemSource === 'function' ? itemSource() : itemSource),
        (newItem) => {
            if (newItem) fetchVariants();
        },
        { immediate: true }
    );

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
        backToList,
        fetchVariants
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
    const hotpotData = ref([]);

    function getAllHotpot() {
        getAllHotpotGeneral()
    .then(async res => {
      hotpotData.value = res.data;
      await nextTick();
    })
    .catch(console.error);
    }

    onMounted(() =>{
        getAllHotpot();
    })

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
    
    const formData = ref({
        maSetLau: '',
        tenSetLau: '',
        idLoaiSet: '', 
        giaBan: 0,
        moTa: '',
        moTaChiTiet: '',
        hinhAnh: '',
        trangThai: 1
    });

    const listLoaiSet = ref([
        { id: 1, name: 'Lẩu Thái' },
        { id: 2, name: 'Lẩu Nấm' },
        { id: 3, name: 'Lẩu Riêu Cua' }
    ]);

    watch(() => props.hotpotItem, (newItem) => {
        if (newItem) {
            formData.value = { ...newItem };
            
            if (newItem.idLoaiSet && typeof newItem.idLoaiSet === 'object') {
                formData.value.idLoaiSet = newItem.idLoaiSet.id;
            }
        } else {
            formData.value = {
                maSetLau: '', tenSetLau: '', idLoaiSet: '', giaBan: 0, 
                moTa: '', moTaChiTiet: '', hinhAnh: '', trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = () => {
        if (!formData.value.maSetLau || !formData.value.tenSetLau) {
            alert("Vui lòng nhập đủ thông tin bắt buộc!");
            return;
        }
        
        console.log("Dữ liệu chuẩn bị lưu:", formData.value);
        emit('save', formData.value);
        emit('close');
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        listLoaiSet,
        handleSave,
        closeModal
    };
}

export function useCategoryTabManager() {
  const tabs = {
    'danhmuc': CategoryGeneral,
    'chitiet': CategoryDetailGeneral,
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

    function getAllCategories() {
        getAllCategory()
    .then(async res => {
      categoryData.value = res.data;
      await nextTick();
    })
    .catch(console.error);
    }

    onMounted(() =>{
        getAllCategories();
    })

    const isModalOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalOpen.value = true;
    };

    return {
        categoryData,
        isModalOpen,
        selectedItem,
        openModal
    };
}

export function useCategoryDetailManager() {
    const detailData = ref([]);

    function getAllCategoriesDetail() {
        getAllCategoryDetail()
    .then(async res => {
      detailData.value = res.data;
      await nextTick();
    })
    .catch(console.error);
    }

    onMounted(() =>{
        getAllCategoriesDetail();
    })

    const isModalOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalOpen.value = true;
    };

    return {
        detailData,
        isModalOpen,
        selectedItem,
        openModal
    };
}

export function useHotpotSetTypeManager() {
    const hotpotTypeData = ref([]);

    function getAllHotpotType() {
        getAllCategoryHotpot()
    .then(async res => {
      hotpotTypeData.value = res.data;
      await nextTick();
    })
    .catch(console.error);
    }

    onMounted(() =>{
        getAllHotpotType();
    })

    const isModalOpen = ref(false);
    const selectedItem = ref(null);

    const openModal = (item = null) => {
        selectedItem.value = item;
        isModalOpen.value = true;
    };

    return {
        hotpotTypeData,
        isModalOpen,
        selectedItem,
        openModal
    };
}