import { ref } from 'vue';
export const isLogin = ref(false);
export const isAdmin = ref(false);

export const checkRole = () => {
    const role = sessionStorage.getItem('role');
    if (role === "admin") {
        isAdmin.value = true;
    } else {
        isAdmin.value = false;
    }
}