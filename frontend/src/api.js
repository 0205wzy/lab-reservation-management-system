import axios from 'axios';import {ElMessage} from 'element-plus';
const api=axios.create({baseURL:'/api',timeout:10000});
api.interceptors.request.use(c=>{const t=localStorage.getItem('token');if(t)c.headers.Authorization=`Bearer ${t}`;return c});
api.interceptors.response.use(r=>r.data,e=>{if(e.response?.status===401){localStorage.clear();location.href='/login'}ElMessage.error(e.response?.data?.message||'请求失败');return Promise.reject(e)});
export default api;
