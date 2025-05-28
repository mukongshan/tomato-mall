import { ACCOUNT_MODULE } from "./_prefix.js";
import { axios } from "../utils/request";

export type UserRole = 'CUSTOMER' | 'STAFF' | 'SHOPKEEPER' | 'admin' | ''

export interface UserDetail {
    id: number,
    username: string,
    name: string,
    role: UserRole,
    avatar: string,
    telephone: string,
    email: string,
    location: string,
    shopId: number,
    isValidStaff: number
}

// 账户类型（包含密码）
export interface AccountDetail extends UserDetail {
    password: string
}

// 登录凭证类型
export interface LoginCredentials {
    username: string,
    password: string
}

export const getUserDetails = async (username: string) => {
    return await axios.get(`${ACCOUNT_MODULE}/${username}`,
        { headers: { 'Content-Type': 'application/json' } });
}

export const createAccount = async (account: AccountDetail) => {
    return await axios.post(`${ACCOUNT_MODULE}`, account,
        { headers: { 'Content-Type': 'application/json' } });
}

export const login = async (login: LoginCredentials) => {
    return await axios.post(`${ACCOUNT_MODULE}/login`, login,
        { headers: { 'Content-Type': 'application/json' } });
}

export const updateUser = async (account: AccountDetail) => {
    return await axios.put(`${ACCOUNT_MODULE}`, account,
        { headers: { 'Content-Type': 'application/json' } });
}
//通过id更新用户身份
export const updateUserRole = async (id: number, role: UserRole) => {
    return await axios.put(`${ACCOUNT_MODULE}/role/${id}`, role,
        { headers: { 'Content-Type': 'application/json' } });
}
// 根据id获取用户role
export const getUserRoleById = async (id: number) => {
    return await axios.get(`${ACCOUNT_MODULE}/role/${id}`,
        { headers: { 'Content-Type': 'application/json' } });
}
