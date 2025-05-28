import { ref } from 'vue';
export const isLogin = ref(false);
export const isAdmin = ref(false);
export const isShopOwner = ref(false);
export const isStaff = ref(false);
export const isCustomer = ref(false);

export const checkRole = async () => {
    const role = sessionStorage.getItem('role');
    if (role === "admin") {
        isAdmin.value = true;
    } else {
        isAdmin.value = false;
    }

    if (role === "SHOPKEEPER") {
        isShopOwner.value = true;
    } else {
        isShopOwner.value = false;
    }
    if (role === "STAFF") {
        isStaff.value = true;
    } else {
        isStaff.value = false;
    }

    if (role === "CUSTOMER") {
        isCustomer.value = true;
    } else {
        isCustomer.value = false;
    }
}