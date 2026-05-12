# 萌宠集市 Pet Shop

一个面向宠物交易、领养、交流和站内私信的 Web 项目原型，包含：

- 普通用户前台：浏览、发布、收藏、评论、私信、交易意向
- 独立管理员后台：举报处理、内容审核、分类管理、地区库管理、普通用户限制

当前项目已经完成“普通用户体系”和“管理员体系”拆分：

- 普通用户使用 `app_user` 与 `user_session`
- 管理员使用 `admin_user` 与 `admin_session`
- 前台与后台分离，不再通过普通用户菜单进入管理台

## 目录结构

```text
pet-shop
├─ backend   # Spring Boot API
├─ frontend  # Vite + React 前端
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

- `MYSQL_URL=jdbc:mysql://localhost:3306/pet_shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
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
npm install
npm run dev
```

普通用户前台：

```text
http://127.0.0.1:5173/index.html
```

独立管理员后台：

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

管理员必须走独立后台登录入口，不允许通过普通用户登录页登录。

相关接口：

- `POST /api/admin/auth/login`
- `POST /api/admin/auth/logout`
- `GET /api/admin/auth/me`

默认管理员账号：

- 用户名：`superadmin`
- 密码：`change-me-admin-password`

后续新增管理员账号，应通过已登录管理员在后台创建，不再通过普通用户升权。

## 当前重要约束

- 普通用户和管理员账号分表存储
- 普通用户前台与管理后台分入口、分登录、分接口
- 普通用户不能在前台看到后台入口
- 管理台只处理管理事务，不承载普通用户业务页面

## 测试与构建

后端编译：

```powershell
cd backend
mvn test
```

前端构建：

```powershell
cd frontend
npm run build
```

前端 E2E：

```powershell
cd frontend
npm run test:e2e
```

## 文档

- 开发规范：[BACKEND_DEVELOPMENT_SPEC.md](/H:/study-code/pet-shop/BACKEND_DEVELOPMENT_SPEC.md)
- 阶段计划：[DEVELOPMENT_PLAN.md](/H:/study-code/pet-shop/DEVELOPMENT_PLAN.md)
