import { getReceivedMessages, getSentMessages, getUnreadMessageCount, Message } from '@/api/message';
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
export const unreadCount = ref(0);
export const receivedMessages = ref<Message[]>([]);
export const sentMessages = ref<Message[]>([]);


// 加载消息
export const messageLoad = async () => {
    const id = sessionStorage.getItem('id');
    if (!id) return;
    unreadCount.value = (await getUnreadMessageCount(Number(id))).data.data;
    receivedMessages.value = (await getReceivedMessages(Number(id))).data.data;
    sentMessages.value = (await getSentMessages(Number(id))).data.data;
}