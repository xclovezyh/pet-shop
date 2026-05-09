# 萌宠集市 Pet Shop

一个集宠物分类、宠物展示、交易发布、站内私信、用户日常、收藏、举报与管理审核于一体的 Web 项目原型。

完整开发计划见 [DEVELOPMENT_PLAN.md](DEVELOPMENT_PLAN.md)。

## 功能概览

- 宠物分类库、宠物展示、宠物详情相册
- 售卖、互换、领养、闲置、寄养等交易帖发布
- 交易意向单、交易状态流转、我的发布管理
- 用户日常发布、评论、点赞、分享
- 昵称登录、个人主页、头像、简介、常驻城市
- 站内私信、未读数、收藏帖子
- 图片上传、格式校验、大小限制、多图预览
- 内容安全校验、举报、黑名单、后台审核
- 管理后台：用户管理、举报处理、帖子/日常下架与恢复

## 技术栈

前端：

- Vite
- React
- TypeScript
- lucide-react

后端：

- Spring Boot 2.7
- Spring Data JPA
- MySQL 8
- 本地文件上传

## 项目结构

```text
pet-shop
├── backend   # Spring Boot API
├── frontend  # React Web 页面
├── docker-compose.yml
└── DEVELOPMENT_PLAN.md
```

## 本地开发

### 1. 创建数据库

```sql
CREATE DATABASE pet_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置后端环境变量

PowerShell 示例：

```powershell
$env:MYSQL_URL="jdbc:mysql://localhost:3306/pet_shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="123"
$env:APP_UPLOAD_DIR="uploads"
$env:APP_UPLOAD_URL_PREFIX="/api/uploads/"
```

后端默认值仍保留在 `backend/src/main/resources/application.yml` 中，开发环境不设置变量也可以使用默认的 `root / 123`。

### 3. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

接口地址：

```text
http://localhost:8080/api
```

### 4. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

页面地址：

```text
http://localhost:5173/index.html
```

前端开发代理默认转发 `/api` 到 `http://localhost:8080`，可用 `VITE_API_PROXY` 覆盖。

## 账号登录

前台账号入口在右上角“登录 / 注册”。当前支持：

- 用户名 + 密码登录。
- 手机号 + 验证码登录。
- 手机号验证码注册账号。

开发环境的验证码接口会直接返回验证码，便于本地调试；接入真实短信服务后，后端保留同一接口，不再把验证码返回给前端。

## 超级管理员

管理后台只对 `SUPER_ADMIN` 角色开放。普通用户只能管理自己的发布、收藏和私信。

默认开发配置：

```text
管理员昵称：superadmin
管理员口令：change-me-admin-code
```

超级管理员不走前台普通注册。首次使用时在“密码登录”里填写管理员昵称、准备使用的密码和管理员口令，系统会自动初始化 `SUPER_ADMIN` 账号。之后仍使用该账号从密码登录进入管理后台。生产环境必须通过环境变量修改：

```powershell
$env:APP_ADMIN_NICKNAMES="your-admin-name"
$env:APP_ADMIN_CODE="your-strong-admin-code"
```

Docker 部署时可在 `.env` 中配置同名变量。

## 构建验证

后端打包：

```powershell
cd backend
mvn package -DskipTests
```

前端类型检查与构建：

```powershell
cd frontend
.\node_modules\.bin\tsc.cmd -b
.\node_modules\.bin\vite.cmd build
```

## Docker 部署

复制环境变量模板：

```powershell
Copy-Item .env.example .env
```

按需修改 `.env` 中的数据库账号、密码和上传目录，然后启动：

```powershell
docker compose up --build
```

访问地址：

```text
http://localhost:5173
```

服务端口：

- 前端 Nginx：`5173 -> 80`
- 后端 API：`8080`
- MySQL：`3306`

## 生产配置

后端生产配置位于：

```text
backend/src/main/resources/application-prod.yml
```

主要通过环境变量注入：

- `MYSQL_URL`
- `MYSQL_USERNAME`
- `MYSQL_PASSWORD`
- `APP_UPLOAD_DIR`
- `APP_UPLOAD_URL_PREFIX`
- `SERVER_PORT`

前端容器使用 `frontend/nginx.conf`：

- 静态资源由 Nginx 提供
- `/api/` 反向代理到 `backend:8080/api/`
- 单页应用路由回退到 `index.html`

## 初始化数据

应用启动时会通过 `DataSeeder` 自动补齐基础分类、地区和选项数据。

独立 SQL 参考脚本位于：

```text
database/initial-data.sql
```

后续引入 Flyway 或 Liquibase 时，可以将该脚本拆分为正式迁移文件。

## 注意事项

- 当前登录仍是原型阶段的昵称登录，不是完整认证系统。
- 项目已接入 Flyway，迁移脚本位于 `backend/src/main/resources/db/migration`；开发环境默认关闭 Flyway 并保留 `ddl-auto: update` 便于兼容旧本地库，生产环境启用 Flyway 并使用 `ddl-auto: validate`。
- 本地上传文件默认保存在 `backend/uploads`，Docker 部署时挂载到 `uploads` 数据卷。
