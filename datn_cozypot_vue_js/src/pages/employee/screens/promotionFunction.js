import { ref, shallowRef } from 'vue';

// Bỏ "screens/" vì bạn đang đứng trong thư mục screens rồi
import PromotionStats from './promotionFragment/PromotionStats.vue';
import PromotionCampaign from './promotionFragment/PromotionCampaign.vue';
import PromotionVoucher from './promotionFragment/PromotionVoucher.vue';
import PersonalVoucher from './promotionFragment/PersonalVoucher.vue'; 

export function usePromotionManager() {
    const currentTabName = ref('thongke');
    const currentComponent = shallowRef(PromotionStats);

    const changeTab = (tab) => {
        currentTabName.value = tab;
        switch (tab) {
            case 'thongke':
                currentComponent.value = PromotionStats;
                break;
            case 'dotkhuyenmai':
                currentComponent.value = PromotionCampaign;
                break;
            case 'phieugiamgia':
                currentComponent.value = PromotionVoucher;
                break;
            case 'canhan':
                currentComponent.value = PersonalVoucher;
                break;
        }
    };

    return { currentTabName, currentComponent, changeTab };
}