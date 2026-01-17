import { computed, nextTick, onMounted, ref, shallowRef, watch } from 'vue';

import MenuFood from '../pages/admin/food/screens/FoodManageGeneral.vue';
import MenuHotpot from '../pages/admin/food/screens/FoodHotPotSetGeneral.vue';
import FoodDetailManageGeneral from '../pages/admin/food/screens/FoodDetailManageGeneral.vue';
import axios from 'axios';
import CategoryGeneral from '../pages/admin/category/screens/CategoryGeneral.vue';
import CategoryDetailGeneral from '../pages/admin/category/screens/CategoryDetailGeneral.vue';
import CategoryHotpotGeneral from '../pages/admin/category/screens/CategoryHotpotGeneral.vue';
import { useRouter } from 'vue-router';
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

export function getAllCategory() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/category`);
}

export function getAllCategoryDetail() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/category/detail`);
}

export function getAllCategoryHotpot() {
    return axios.get(`${API_BASE_EMPLOYEE}/food/category/hotpotType`);
}

export function postNewFood(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food`, data);
}

export function putNewFood(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/${id}`, data);
}

export function putNewFoodDetail(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/foodDetail/${id}`, data);
}

export function postNewHotpot(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/hotpotGeneral`, data);
}

export function putNewCategory(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/category/${id}`, data);
}

export function postNewFoodDetail(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/foodDetail`, data);
}
export function putNewHotpot(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/hotpotGeneral/${id}`, data);
}
export function postNewCategory(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category`, data);
}

export function putNewHotpotCategory(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/category/hotpotType/${id}`, data);
}

export function putCategoryDetail(id, data) {
    return axios.put(`${API_BASE_EMPLOYEE}/food/category/detail/${id}`, data);
}

export function postCategoryDetail(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category/detail`, data);
}

export function postNewCategoryDetail(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category/detail`, data);
}

export function postNewHotpotCategory(data) {
    return axios.post(`${API_BASE_EMPLOYEE}/food/category/hotpotType`, data);
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
    const detailData = ref([]);

    function getAllFoodDetails() {
        getAllFoodDetail()
            .then(async res => {
                detailData.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllFoodDetails();
    })


    const isModalOpen = ref(false);
    const selectedDetail = ref(null);
    const isAddModalOpen = ref(false);

    const openEditModal = (item) => {
        selectedDetail.value = item;
        isModalOpen.value = true;
    };

    const handleSaveData = (data) => {
        isModalOpen.value = false;
        emit('refresh');
        emit('close');
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;

        try {
            const payload = {...item, trangThai: newStatus };
            if (item.monAnDiKem && item.monAnDiKem.id) {
                payload.idMonAnDiKem = item.monAnDiKem.id;
            }
            await putNewFoodDetail(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    return {
        detailData,
        isModalOpen,
        isAddModalOpen,
        selectedDetail,
        getAllFoodDetails,
        openEditModal,
        handleSaveData,
        handleToggleStatus
    };
}

export function useFoodManager() {
    const mockData = ref([]);
    const activeTab = ref('thucdon');
    const isModalOpen = ref(false);
    const selectedItem = ref(null);
    const isAddFoodModalOpen = ref(false);

    function getAllFood() {
        getAllFoodGeneral()
            .then(async res => {
                mockData.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllFood();
    });

    const handleViewDetails = (item) => {
        selectedItem.value = item;
        isModalOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThaiKinhDoanh;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThaiKinhDoanh = newStatus;

        try {
            const payload = {...item, trangThaiKinhDoanh: newStatus };
            if (item.danhMucChiTiet && item.danhMucChiTiet.id) {
                payload.idDanhMucChiTiet = item.danhMucChiTiet.id;
            }
            await putNewFood(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    
    const currentPage = ref(1);
    const itemsPerPage = 5;

    const paginatedData = computed(() => {
        if (!mockData.value || mockData.value.length === 0) return [];
        
        const start = (currentPage.value - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        return mockData.value.slice(start, end);
    });

    const totalPages = computed(() => {
        return mockData.value ? Math.ceil(mockData.value.length / itemsPerPage) : 0;
    });

    const goToPage = (page) => {
        if (page >= 1 && page <= totalPages.value) {
            currentPage.value = page;
        }
    };

    const visiblePages = computed(() => {
        const total = totalPages.value;
        const current = currentPage.value;
        const delta = 1;
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
                if (i - l === 2) {
                    rangeWithDots.push(l + 1);
                } else if (i - l !== 1) {
                    rangeWithDots.push('...');
                }
            }
            rangeWithDots.push(i);
            l = i;
        }
        return rangeWithDots;
    });

    return {
        mockData,
        activeTab,
        isModalOpen,
        selectedItem,
        isAddFoodModalOpen,
        handleViewDetails,
        getAllFood,
        handleToggleStatus,

        paginatedData,
        currentPage,
        itemsPerPage,
        totalPages,
        visiblePages,
        goToPage
    };
}

export function useFoodModal(itemSource) {
    const currentView = ref('list');
    const selectedVariant = ref(null);
    const variants = ref([]);
    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);

    const parentFormData = ref({
        id: '',
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        moTa: '',
        giaBan: 0,
        hinhAnh: '',
        trangThai: 1
    });


    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMuc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    function getAllCategoriesDetail() {
        getAllCategoryDetail()
            .then(async res => {
                listDanhMucChiTiet.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

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

    const openEditMode = (variant) => {
        selectedVariant.value = {...variant };
        currentView.value = 'update';
    };

    const backToList = () => {
        selectedVariant.value = null;
        currentView.value = 'list';
    };

    const filteredSubCategories = computed(() => {
    if (!parentFormData.value || !parentFormData.value.idDanhMuc) {
        return [];
    }
    return listDanhMucChiTiet.value.filter(item => {
        return String(item.idDanhMuc) === String(parentFormData.value.idDanhMuc);
    });
});

    onMounted(() => {
        getAllCategories();
        getAllCategoriesDetail();
    });

    watch(
        () => (typeof itemSource === 'function' ? itemSource() : itemSource),
        (newVal) => {
            if (newVal) {
                parentFormData.value = {...newVal };
                if (newVal.idDanhMuc) {
                    parentFormData.value.idDanhMuc = newVal.danhMuc.id;
                }

                if (newVal.idDanhMucChiTiet) {
                    parentFormData.value.idDanhMucChiTiet = newVal.idDanhMucChiTiet;
                }
                fetchVariants();
                currentView.value = 'list';
            } else {
                parentFormData.value = {
                    id: '',
                    tenMonAn: '',
                    idDanhMuc: '',
                    idDanhMucChiTiet: '',
                    moTa: '',
                    giaBan: 0,
                    hinhAnh: '',
                    trangThai: 1
                };
                variants.value = [];
            }
        }, { immediate: true, deep: true }
    );

    const handleSave = async(emit) => {
        try {
            const data = parentFormData.value;
            console.log("Dữ liệu cập nhật Variant:", data);
            await putNewFood(data.id, data);

            if (typeof emit === 'function') {
                emit('refresh');
                emit('close');
            }

            fetchVariants();
            backToList();

        } catch (e) {
            console.error("Lỗi cập nhật:", e);
        }
    };

    // 6. RETURN
    return {
        currentView,
        selectedVariant,
        variants,
        parentFormData,
        listDanhMuc,
        filteredSubCategories,

        openEditMode,
        backToList,
        fetchVariants,
        handleSave
    };
}

export function useFoodDetailModal(props, emit) {

    const formData = ref({
        tenChiTietMonAn: '',
        idMonAnDiKem: '',
        giaBan: '',
        giaVon: '',
        kichCo: '',
        donVi: '',
        trangThai: 1
    });

    const listMonAn = ref([]);
    const dataChiTiet = ref([]);

    function getAllFood() {
        getAllFoodGeneral()
            .then(async res => {
                listMonAn.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    function getAllFoodDetails() {
        getAllFoodDetail()
            .then(async res => {
                dataChiTiet.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllFood();
        getAllFoodDetail();
    })


    watch(() => props.detailItem, (newVal) => {
        if (newVal) {
            formData.value = {...newVal };
        } else {
            formData.value = {
                tenChiTietMonAn: '',
                idMonAnDiKem: '',
                giaBan: '',
                giaVon: '',
                kichCo: '',
                donVi: '',
                trangThai: 1
            };
        }
    });

    const handleSave = () => {
        try {
            if (!formData.value.tenChiTietMonAn || !formData.value.giaBan) {
                alert("Vui lòng nhập tên món và chọn danh mục chi tiết!");
                return;
            }

            putNewFoodDetail(formData.value.id, formData.value);

            console.log("Dữ liệu cập nhật:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        listMonAn,
        getAllFoodDetails,
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

    onMounted(() => {
        getAllHotpot();
    })

    const isModalOpen = ref(false);
    const selectedHotpot = ref(null);
    const isAddHotpotModalOpen = ref(false);

    const handleViewDetails = (item) => {
        selectedHotpot.value = item;
        isModalOpen.value = true;
    };

    const handleToggleStatus = async(item) => {
        const oldStatus = item.trangThai;
        const newStatus = oldStatus === 1 ? 0 : 1;
        item.trangThai = newStatus;

        try {
            const payload = {...item, trangThai: newStatus };
            await putNewHotpot(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    return {
        hotpotData,
        isModalOpen,
        selectedHotpot,
        isAddHotpotModalOpen,
        getAllHotpot,
        handleViewDetails,
        handleToggleStatus
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

    const listLoaiSet = ref([]);

    function getAllCategoriesHotpot() {
        getAllCategoryHotpot()
            .then(async res => {
                listLoaiSet.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }
    onMounted(() => {
        getAllCategoriesHotpot();
    })

    watch(() => props.hotpotItem, (newItem) => {
        if (newItem) {
            formData.value = {...newItem };

            if (newItem.idLoaiSet && typeof newItem.idLoaiSet === 'object') {
                formData.value.idLoaiSet = newItem.idLoaiSet.id;
            }
        } else {
            formData.value = {
                maSetLau: '',
                tenSetLau: '',
                idLoaiSet: '',
                giaBan: 0,
                moTa: '',
                moTaChiTiet: '',
                hinhAnh: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            await putNewHotpot(formData.value.id, formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
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

    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                categoryData.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllCategories();
    })

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
            console.log(payload);
            console.log(item.id);
            await putNewCategory(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    return {
        categoryData,
        isModalOpen,
        isModalUpdateOpen,
        selectedItem,
        openModal,
        getAllCategories,
        handleToggleStatus
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

    onMounted(() => {
        getAllCategoriesDetail();
    })

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
            console.log(payload);
            console.log(item.id);
            await putCategoryDetail(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };

    return {
        detailData,
        isModalOpen,
        isModalUpdateOpen,
        selectedItem,
        openModal,
        handleToggleStatus,
        getAllCategoriesDetail
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

    onMounted(() => {
        getAllHotpotType();
    })

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
            console.log(payload);
            console.log(item.id);
            await putNewHotpotCategory(item.id, payload);

        } catch (error) {
            console.error("Lỗi: ", error);
            item.trangThai = oldStatus;
        }
    };


    return {
        hotpotTypeData,
        isModalOpen,
        isModalUpdateOpen,
        selectedItem,
        openModal,
        handleToggleStatus,
        getAllHotpotType
    };
}

export function useFoodAddModal(props, emit) {

    const formData = ref({
        tenMonAn: '',
        idDanhMuc: '',
        idDanhMucChiTiet: '',
        giaBan: 0,
        moTa: '',
        hinhAnh: '',
        trangThaiKinhDoanh: 1
    });

    const listDanhMuc = ref([]);
    const listDanhMucChiTiet = ref([]);

    function getAllCategories() {
        getAllCategory()
            .then(async res => {
                listDanhMuc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    function getAllCategoriesDetail() {
        getAllCategoryDetail()
            .then(async res => {
                listDanhMucChiTiet.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }

    onMounted(() => {
        getAllCategories();
        getAllCategoriesDetail();
    })

    const filteredSubCategories = computed(() => {
    if (!formData.value.idDanhMuc) return [];
    return listDanhMucChiTiet.value.filter(item => item.idDanhMuc === formData.value.idDanhMuc);
});

    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenMonAn: '',
                idDanhMuc: '',
                idDanhMucChiTiet: '',
                moTa: '',
                giaBan: 0,
                hinhAnh: '',
                trangThaiKinhDoanh: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            if (!formData.value.tenMonAn || !formData.value.idDanhMucChiTiet) {
                alert("Vui lòng nhập tên món và chọn danh mục chi tiết!");
                return;
            }

            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewFood(formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        listDanhMuc,
        filteredSubCategories,
        handleSave
    };
}
export function useHotpotAdd() {
    const router = useRouter();

    const formData = ref({
        tenSetLau: '',
        idLoaiSet: '',
        giaBan: 0,
        hinhAnh: '',
        moTa: '',
        trangThai: 1
    });

    const listLoaiSet = ref([]);       
    const listFoodDetails = ref([]);   

    // --- 3. STATE QUẢN LÝ MÓN TRONG SET (N-N) ---
    // Mảng chứa các món đã chọn: [{ id, ten, soLuong, ... }]
    const selectedIngredients = ref([]); 
    const currentIngredientId = ref(''); // Biến tạm cho dropdown chọn món

    // --- 4. HÀM LOAD DỮ LIỆU BAN ĐẦU ---
    const fetchInitialData = async () => {
        try {
            // Gọi song song 2 API để tiết kiệm thời gian
            const [resLoaiSet, resFoodDetail] = await Promise.all([
                getAllCategoryHotpot(),
                getAllFoodDetail()
            ]);
            listLoaiSet.value = resLoaiSet.data;
            listFoodDetails.value = resFoodDetail.data;
        } catch (e) {
            console.error("Lỗi load dữ liệu:", e);
        }
    };

    onMounted(() => {
        fetchInitialData();
    });

    // --- 5. LOGIC THÊM/XÓA MÓN VÀO SET ---
    
    // Thêm món từ dropdown vào bảng
    const addIngredient = () => {
        if (!currentIngredientId.value) return;

        // Check trùng
        const exists = selectedIngredients.value.find(item => item.id === currentIngredientId.value);
        if (exists) {
            alert("Món này đã có trong set, vui lòng tăng số lượng ở bảng bên dưới!");
            return;
        }

        // Lấy thông tin chi tiết món
        const foodItem = listFoodDetails.value.find(item => item.id === currentIngredientId.value);
        if (foodItem) {
            selectedIngredients.value.push({
                id: foodItem.id,
                ten: foodItem.tenChiTietMonAn || foodItem.tenDanhMucChiTiet,
                donVi: foodItem.donVi || 'Phần',
                giaBan: foodItem.giaBan,
                soLuong: 1 // Mặc định số lượng là 1
            });
        }
        currentIngredientId.value = ''; // Reset dropdown
    };

    // Xóa món khỏi bảng
    const removeIngredient = (index) => {
        selectedIngredients.value.splice(index, 1);
    };

    // Tính tổng giá trị món lẻ (Optional - để tham khảo giá vốn)
    const totalComponentsPrice = computed(() => {
        return selectedIngredients.value.reduce((sum, item) => sum + (item.giaBan * item.soLuong), 0);
    });

    const handleSave = async () => {
        try {
            if (!formData.value.tenSetLau || !formData.value.idLoaiSet || !formData.value.giaBan) {
                alert("Vui lòng nhập đủ: Tên set, Loại set và Giá bán!");
                return;
            }
            if (selectedIngredients.value.length === 0) {
                alert("Vui lòng thêm ít nhất 1 món ăn vào Set!");
                return;
            }

            const payload = {
                ...formData.value,
                listChiTietSetLau: selectedIngredients.value.map(item => ({
                    idChiTietMonAn: item.id,
                    soLuong: item.soLuong
                }))
            };

            console.log("Payload gửi đi:", payload);
            await postNewHotpot(payload);

            alert("Thêm mới thành công!");
            router.push({ name: 'foodManager', query: { tab: 'setlau' } });

        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    const goBack = () => {
        router.back();
    };

    return {
        formData,
        listLoaiSet,
        listFoodDetails,
        selectedIngredients,
        currentIngredientId,
        totalComponentsPrice,
        addIngredient,
        removeIngredient,
        handleSave,
        goBack
    };
}

export function useFoodDetailAddModal(props, emit) {

    const formData = ref({
        tenChiTietMonAn: '',
        idMonAnDiKem: '',
        moTaChiTiet: '',
        giaBan: 0,
        giaVon: 0,
        kichCo: '',
        donVi: '',
        trangThai: 1
    });

    const listDanhMuc = ref([]);
    const isParentLocked = ref(false);

    function getAllFoodDetails() {
        getAllFoodGeneral()
            .then(async res => {
                listDanhMuc.value = res.data;
                await nextTick();
            })
            .catch(console.error);
    }


    onMounted(() => {
        getAllFoodDetails();
        // alert(props.foodItem.id)
    })


    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenChiTietMonAn: '',
                idMonAnDiKem: '',
                moTaChiTiet: '',
                giaBan: 0,
                giaVon: 0,
                kichCo: '',
                donVi: '',
                trangThai: 1
            };

            if (props.foodItem && props.foodItem.id) {
                formData.value.idMonAnDiKem = props.foodItem.id;
                isParentLocked.value = true;
            } else {
                isParentLocked.value = false;
            }
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            if (!formData.value.tenChiTietMonAn || !formData.value.idMonAnDiKem) {
                alert("Vui lòng nhập tên set và chọn loại set chi tiết!");
                return;
            }

            await postNewFoodDetail(formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        listDanhMuc,
        isParentLocked,
        handleSave
    };
}

export function useCategoryAddModal(props, emit) {

    const formData = ref({
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });


    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewCategory(formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');

            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        handleSave
    };
}

export function useCategoryPutModal(props, emit) {

    const formData = ref({
        tenDanhMuc: '',
        moTa: '',
        trangThai: 1
    });

    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = {...newItem };
        } else {
            formData.value = {
                tenDanhMuc: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            await putNewCategory(formData.value.id, formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal
    };
}

export function useCategoryHotpotPutModal(props, emit) {

    const formData = ref({
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });

    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = {...newItem };
        } else {
            formData.value = {
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            await putNewHotpotCategory(formData.value.id, formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal
    };
}


export function useHotpotCategoryAddModal(props, emit) {

    const formData = ref({
        tenLoaiSet: '',
        moTa: '',
        trangThai: 1
    });


    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                tenLoaiSet: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewHotpotCategory(formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        handleSave
    };
}

export function useCategoryDetailAddModal(props, emit) {

    const listDanhMucGoc = ref([]);

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
    })

    const formData = ref({
        idDanhMuc: '',
        tenDanhMucChiTiet: '',
        moTa: '',
        trangThai: 1
    });


    watch(() => props.isOpen, (val) => {
        if (val) {
            formData.value = {
                idDanhMuc: '',
                tenDanhMucChiTiet: '',
                moTa: '',
                trangThai: 1
            };
        }
    });

    const handleSave = async() => {
        try {
            // Gọi API Thêm mới (Example)
            // await axios.post('http://localhost:8080/api/manage/food', formData.value);
            postNewCategoryDetail(formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    return {
        formData,
        handleSave,
        listDanhMucGoc
    };
}

export function useCategoryDetailPutModal(props, emit) {

    const listDanhMucGoc = ref([]);

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
    })

    const formData = ref({
        idDanhMuc: '',
        tenDanhMucChiTiet: '',
        moTa: '',
        trangThai: 1
    });

    watch(() => props.itemList, (newItem) => {
        if (newItem) {
            formData.value = {...newItem };
        } else {
            formData.value = {
                idDanhMuc: '',
                tenDanhMucChiTiet: '',
                moTa: '',
                trangThai: 1
            };
        }
    }, { immediate: true });

    const handleSave = async() => {
        try {
            await putCategoryDetail(formData.value.id, formData.value);

            console.log("Dữ liệu thêm mới:", formData.value);

            emit('refresh');
            emit('close');
        } catch (e) {
            console.error("Lỗi thêm món ăn:", e);
            alert("Đã xảy ra lỗi khi thêm mới!");
        }
    };

    const closeModal = () => {
        emit('close');
    };

    return {
        formData,
        handleSave,
        closeModal,
        listDanhMucGoc
    };
}