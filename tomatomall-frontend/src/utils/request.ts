import axios from 'axios'
import { ElMessage } from 'element-plus'

//创建一个axios的实例service
const service = axios.create({
    baseURL: "" // 在main.ts里设置不会生效 未知原因 可能是两个axios实例不同  但之前未出现此问题
}
)

//判断是否登录
//因为用户登录后会将后端返回的 Token 存入 sessionStorage，退出登录时主动清除 Token。
function hasToken() {
    return !(sessionStorage.getItem('token') === '')
}

//向后端发出请求时的拦截器  符合条件才能给后端
service.interceptors.request.use(
    config => {
        if (hasToken()) {
            config.headers['token'] = sessionStorage.getItem('token')
        }
        return config
    },
    error => {
        console.log(error);
        return Promise.reject();
    }
)

//当前实例的拦截器，对所有从后端收到的请求进行处理，检验http的状态码
// 处理后端返回的响应
service.interceptors.response.use(
    response => {
        if (response.status === 200) {
            return response;
        } else {
            return Promise.reject();
        }
    },
    error => {
        ElMessage({
            message: error.response.data.msg,
            type: 'error',
            duration: 1000
        })
        return Promise.reject();
    }
)

//设置为全局变量
export {
    service as axios
}
