# 萌宠集市 Pet Shop

一个面向宠物交易、领养、交流和站内私信的 Web 项目原型，包含普通用户前台与独立管理后台两套入口。

当前项目已经完成以下基础边界拆分：

- 普通用户使用 `app_user` 与 `user_session`
- 管理员使用 `admin_user` 与 `admin_session`
- 主站与管理台分离，不再从普通用户页面进入后台
- 管理台列表接口统一支持分页，避免页面堆叠过多数据

## 目录结构

```text
pet-shop
├─ backend    # Spring Boot API
├─ frontend   # Vite + React 前端
├─ README.md
├─ BACKEND_DEVELOPMENT_SPEC.md
└─ DEVELOPMENT_PLAN.md
```

## 技术栈

前端：

- React
- TypeScript
- Vite
- lucide-react

后端：

- Spring Boot 2.7
- Spring Data JPA
- MySQL

## 本地启动

### 1. 创建数据库

```sql
CREATE DATABASE pet_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 后端默认配置

默认开发配置位于 [backend/src/main/resources/application.yml](/H:/study-code/pet-shop/backend/src/main/resources/application.yml)。

默认数据库连接：

- `MYSQL_URL=jdbc:mysql://localhost:3308/pet_shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- `MYSQL_USERNAME=root`
- `MYSQL_PASSWORD=123`

### 3. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

接口根地址：

```text
http://127.0.0.1:8080/api
```

### 4. 启动前端

```powershell
cd frontend
pnpm install
pnpm dev
```

普通用户前台：

```text
http://127.0.0.1:5173/index.html
```

独立管理后台：

```text
http://127.0.0.1:5173/admin.html
```

## 登录说明

### 普通用户

支持：

- 用户名 + 密码登录
- 手机号 + 验证码登录
- 手机号验证码注册

相关接口：

- `POST /api/users/login/password`
- `POST /api/users/login/sms`
- `POST /api/users/register`

### 管理员

管理员必须走独立后台登录入口，不允许通过普通用户登录页进入。

相关接口：

- `POST /api/admin/auth/login`
- `POST /api/admin/auth/logout`
- `GET /api/admin/auth/me`

默认管理员账号：

- 用户名：`superadmin`
- 密码：`change-me-admin-password`

后续新增管理员账号，应由已登录管理员在后台创建，不再通过普通用户升权。

### 管理员权限

当前管理员体系分为两层：

- `SUPER_ADMIN`：超级管理员，拥有全部后台权限，可创建管理员、停用管理员、分配权限
- `ADMIN`：普通管理员，只能访问被授予的后台模块

当前支持的权限码：

- `USER_MODERATE`
- `REPORT_REVIEW`
- `POST_AUDIT`
- `MOMENT_AUDIT`
- `CATEGORY_MANAGE`
- `REGION_VIEW`

管理台页面会按权限显示模块，后端接口也会按权限拦截。

## 管理台分页接口

以下管理接口已统一支持 `page`、`size` 分页参数，并返回统一分页结构：

- `GET /api/admin/accounts`
- `GET /api/admin/users`
- `GET /api/admin/reports`
- `GET /api/admin/posts`
- `GET /api/admin/moments`
- `GET /api/admin/categories`
- `GET /api/admin/regions`

分页返回结构：

```json
{
  "success": true,
  "data": {
    "items": [],
    "total": 0,
    "page": 1,
    "size": 10,
    "totalPages": 0,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

说明：

- `page` 从 1 开始
- `size` 默认按接口配置取值
- 最大 `size` 限制为 50

## 地区库说明

地区库已调整为“标准基础库”模式：

- 管理台按省 / 市 / 区县三级结构浏览，不再平铺堆叠全部数据
- 管理台地区库只支持筛选查看，不再支持后台手工新增、编辑、删除
- 全国地区数据来自民政部公开的 `2023 年中华人民共和国县以上行政区划代码`
- 数据迁移脚本位于：
  - [backend/src/main/resources/db/migration/V8__national_regions.sql](/H:/study-code/pet-shop/backend/src/main/resources/db/migration/V8__national_regions.sql)
  - [scripts/generate_mca_regions_sql.py](/H:/study-code/pet-shop/scripts/generate_mca_regions_sql.py)

当前管理台地区树接口：

- `GET /api/admin/regions/tree`

## 当前重要约束

- 普通用户和管理员账号分表存储
- 普通用户前台与管理后台分入口、分登录、分接口
- 普通用户不能在前台看到后台入口
- 管理台只处理管理事务，不承载普通用户业务页面
- 列表页优先分页展示，避免单页堆积全部数据

## 测试与构建

后端测试：

```powershell
cd backend
mvn test
```

前端构建：

```powershell
cd frontend
pnpm build
```

前端 E2E：

```powershell
cd frontend
pnpm test:e2e
```

## 文档

- 访问入口与账号说明：[ACCESS_GUIDE.md](/H:/study-code/pet-shop/ACCESS_GUIDE.md)
- 开发规范：[BACKEND_DEVELOPMENT_SPEC.md](/H:/study-code/pet-shop/BACKEND_DEVELOPMENT_SPEC.md)
- 阶段计划：[DEVELOPMENT_PLAN.md](/H:/study-code/pet-shop/DEVELOPMENT_PLAN.md)
- 代码审查和功能审查文档统一放在 `[docs/reviews/](/H:/study-code/pet-shop/docs/reviews)`，已完成修复的文件名请加 `已确认修复`

## 用户认证安全补充

- 普通用户登录态已切换为 `JWT Bearer Token`
- 普通用户前端密码提交规则：浏览器端先执行 `SM3` 摘要，再通过 HTTPS/HTTP 请求发送摘要值
- 普通用户数据库密码存储规则：服务端对前端传入的 `SM3` 摘要再执行 `BCrypt` 加盐单向哈希后保存
- 数据库中不保存明文密码，也不提供可逆解密能力
- `user_session` 表保留为历史兼容用途，便于旧 token 平滑退出，新增登录默认不再写入该表
- 管理员账号体系暂时保持独立，不与普通用户 JWT 登录混用
