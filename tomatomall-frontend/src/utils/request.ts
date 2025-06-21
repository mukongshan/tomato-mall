import router from '@/router'
import axios from 'axios'
import { ElMessage } from 'element-plus'

//创建一个axios的实例service
const service = axios.create({
    baseURL: "http://localhost:8080" // 在main.ts里设置不会生效 未知原因 可能是两个axios实例不同  但之前未出现此问题
})

//判断是否登录
//因为用户登录后会将后端返回的 Token 存入 sessionStorage，退出登录时主动清除 Token。
function hasToken() {
    return !(sessionStorage.getItem('token') == null)
}

//向后端发出请求时的拦截器  符合条件才能给后端
service.interceptors.request.use(
    config => {
        if (hasToken()) {
            config.headers['token'] = sessionStorage.getItem('token')
        }
        return config
    },
)


// 当前实例的拦截器，对所有从后端收到的请求进行处理，检验http的状态码
// 处理后端返回的响应
service.interceptors.response.use(
    response => {
        // 成功的响应直接返回
        return response;
    },
    error => {
        // 确保 error.response 存在，以处理网络断开等没有响应体的错误
        if (error && error.response) {
            // 如果后端返回的状态码为401，表示未登录或登录已过期
            if (error.response.status === 401) {
                // 不再有锁机制，每次遇到401都直接执行操作
                ElMessage.error('请先登录！');
                router.push('/login');

                // 关键部分保留：
                // 仍然返回一个永远不会结束的Promise，来“冻结”当前失败的请求链。
                // 这可以阻止它继续执行.catch()，从而避免“商品加载失败”等次生错误。
                return new Promise(() => { });
            }
        }
        // 对于所有其他非401的错误，我们让它正常失败，以便业务代码可以捕获
        return Promise.reject(error);
    }
);
//设置为全局变量
export {
    service as axios
}
