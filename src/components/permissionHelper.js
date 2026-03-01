//Check các chức năng bên trong, (nút thêm/ sửa, chuyển tt...) để phân quyền
import Swal from 'sweetalert2';
import { useAuthStore } from "@/pages/guest/authentication/authenticationServices/authenticationService";

export const usePermission = () => {
    const authStore = useAuthStore();

    const handleActionWithAuth = (callbackAction, requiredRole = 'ADMIN') => {
        const userRole = authStore.role;

        if (userRole !== requiredRole) {
            Swal.fire({
                icon: 'error',
                title: 'Không có quyền thực hiện!',
                text: 'Bạn không có quyền hạn thao tác chức năng này. Vui lòng liên hệ Quản trị viên.',
                confirmButtonText: 'Đã hiểu',
                confirmButtonColor: '#7d161a',
                timer: 3000,
                timerProgressBar: true
            });
            return; 
        }

        if (callbackAction && typeof callbackAction === 'function') {
            callbackAction();
        }
    };

    return {
        handleActionWithAuth
    };
};