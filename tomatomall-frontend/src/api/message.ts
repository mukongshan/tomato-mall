import { MESSAGE_MODULE } from "./_prefix";
import { axios } from "@/utils/request";

export type messageContentType =
    | "NEW_EMPLOYEE_APPLICATION"
    | "NEW_STORE_APPLICATION"
    | "LOW_INVENTORY"
    | "APPLICATION_APPROVED"
    | "APPLICATION_REJECTED"
    | "YOU_ARE_FIRED";


export interface Message {
    id: number; // 消息ID
    content: messageContentType; // 消息内容
    isRead: boolean; // 是否已读
    fromUser: number;
    toUser: number;
    createdTime: string; // 创建时间
}

export const sendMessage = async (message: Message) => {
    return await axios.post(`${MESSAGE_MODULE}/send`, message, {
        headers: { "Content-Type": "application/json" },
    });
}
// 获取收到的消息
export const getReceivedMessages = async (toUserId: number) => {
    return await axios.get(`${MESSAGE_MODULE}/received-list/${toUserId}`);
}

export const getSentMessages = async (fromUserId: number) => {
    return await axios.get(`${MESSAGE_MODULE}/sent-list/${fromUserId}`);
}

export const markMessageAsRead = async (messageId: number) => {
    return await axios.put(`${MESSAGE_MODULE}/mark-read/${messageId}`), {
        headers: { "Content-Type": "application/json" },
    };
}

export const deleteMessage = async (messageId: number) => {
    return await axios.delete(`${MESSAGE_MODULE}/delete/${messageId}`);
}

export const getUnreadMessageCount = async (userId: number) => {
    return await axios.get(`${MESSAGE_MODULE}/unread-count/${userId}`);
}
