# 萌宠集市 Pet Shop

一个集宠物分类介绍、宠物展示、售卖/互换发帖和用户日常分享于一体的 Web 项目原型。

## 功能进度

- 宠物分类库展示
- 宠物展示与售卖卡片
- 售卖、互换、领养、闲置帖子发布
- 用户日常分享发布
- 昵称登录后发布
- 分类下拉选择
- 省市区三级选择
- 联系方式固定为站内私信
- 前后端手机号拦截
- 本地图片上传
- MySQL 自动建表和示例数据初始化

## 技术栈

前端：

- Vite
- React
- TypeScript
- lucide-react

后端：

- Spring Boot
- Spring Data JPA
- MySQL
- 本地文件上传

## 项目结构

```text
pet-shop
├── backend   # Spring Boot API
└── frontend  # React Web 页面
```

## 本地运行

### 1. 创建数据库

```sql
CREATE DATABASE pet_shop DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置 MySQL 密码

后端默认读取环境变量：

```powershell
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="123"
```

也可以直接修改：

```text
backend/src/main/resources/application.yml
```

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

## 注意

当前版本是基础原型，登录仍是前端昵称登录。后续可以继续扩展真实用户系统、帖子详情、宠物详情、后台审核和订单模块。

