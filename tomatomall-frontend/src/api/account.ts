import {ACCOUNT_MODULE} from "./_prefix.js";
import {axios} from "../utils/request";

export type UserRole = 'CUSTOMER'|'STAFF'|'SHOPKEEPER'|'ADMINISTRATOR'|''

export interface UserDetail {
    username: string,
    name: string,
    role: UserRole,
    avatar: string,
    telephone: string,
    email: string,
    location: string
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

export const getUserDetails = async (username:string) =>{
    return await axios.get(`${ACCOUNT_MODULE}/${username}`,
        {headers:{'Content-Type':'application/json'}});
}

export const createAccount = async (account :AccountDetail)=>{
    return await axios.post(`${ACCOUNT_MODULE}`,account,
        {headers: {'Content-Type': 'application/json'}});
}

export const login = async (login:LoginCredentials)=>{
    return await axios.post(`${ACCOUNT_MODULE}/login`, login,
        {headers: {'Content-Type': 'application/json'}});
}

export const updateUser = async (account:AccountDetail)=>{
    return await axios.put(`${ACCOUNT_MODULE}`,account,
        {headers: {'Content-Type': 'application/json'}});
}