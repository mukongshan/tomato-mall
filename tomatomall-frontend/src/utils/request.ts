import router from '@/router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { NavigationFailure } from 'vue-router'

//创建一个axios的实例service
const service = axios.create({
    baseURL: "" // 在main.ts里设置不会生效 未知原因 可能是两个axios实例不同  但之前未出现此问题
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


//当前实例的拦截器，对所有从后端收到的请求进行处理，检验http的状态码
// 处理后端返回的响应
// 在拦截器外部定义一个“锁”，用于防止因并发请求导致的重复跳转
let redirectPromise: Promise<void | NavigationFailure | undefined> | null = null;

service.interceptors.response.use(
    response => {
        // 成功的回调（HTTP状态码为2xx）。
        // 您的原始代码 `if (response.status === 200)` 在这里是多余的，
        // 因为只有2xx的状态码才会进入这个成功回调。我们直接返回响应即可。
        return response;
    },
    error => {
        // 首先，确保 error.response 存在，以处理网络断开等没有响应体的错误
        if (error && error.response) {
            // 如果后端返回的状态码为401，表示未登录或登录已过期
            if (error.response.status === 401) {
                // 检查“锁”是否已经激活。如果已激活，则直接返回一个中止的Promise，不做任何事
                if (!redirectPromise) {
                    // 如果未“上锁”，则立即执行操作
                    ElMessage.error('请先登录！');

                    // “上锁”：将 router.push 返回的 Promise 赋值给锁变量
                    redirectPromise = router.push('/login').finally(() => {
                        // 当路由跳转完成后（无论成功还是失败），必须“解锁”，
                        // 以便未来的 token 过期能再次触发跳转
                        redirectPromise = null;
                    });
                }

                // **关键改动**：
                // 返回一个永远不会结束的Promise，来“冻结”当前失败的请求链。
                // 这可以阻止它继续执行.catch()，从而完美避免“商品加载失败”等次生错误。
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
