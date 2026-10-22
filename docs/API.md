# 主要接口说明

接口统一前缀为`/api`。除登录、Swagger外，请求需携带`Authorization: Bearer <token>`。

| 方法 | 路径 | 说明 | 角色 |
|---|---|---|---|
| POST | `/auth/login` | 登录并获取JWT | 公开 |
| GET | `/auth/me` | 获取当前用户 | 已登录 |
| GET | `/dashboard` | 统计概览 | 已登录 |
| GET | `/labs` | 查询实验室 | 已登录 |
| POST/PUT | `/manage/labs` | 新增或修改实验室 | 实验室管理员 |
| GET | `/devices` | 查询设备 | 已登录 |
| POST/PUT | `/manage/devices` | 新增或修改设备 | 实验室管理员 |
| POST | `/reservations` | 提交预约 | 普通用户 |
| GET | `/reservations/my` | 查询本人预约 | 普通用户 |
| PUT | `/reservations/{id}/cancel` | 取消本人预约 | 普通用户 |
| GET | `/manage/reservations` | 查询全部预约 | 实验室管理员 |
| PUT | `/manage/reservations/{id}/audit` | 审核预约 | 实验室管理员 |
| POST | `/repairs` | 提交报修 | 普通用户 |
| GET | `/repairs/my` | 查询本人报修 | 普通用户 |
| GET | `/manage/repairs` | 查询维修工单 | 实验室管理员 |
| PUT | `/manage/repairs/{id}/handle` | 受理、完成或关闭工单 | 实验室管理员 |
| GET/PUT | `/admin/users` | 查询或维护用户 | 系统管理员 |

完整交互参数可在启动后访问Swagger页面查看。
