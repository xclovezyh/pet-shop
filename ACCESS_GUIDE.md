# 访问入口与账号说明

## 当前本机服务

- 前端：http://127.0.0.1:5173/index.html
- 管理台：http://127.0.0.1:5173/admin.html
- 后端接口：http://127.0.0.1:8080/api

## 启动命令

后端：

```powershell
cd H:\study-code\pet-shop\backend
mvn spring-boot:run
```

前端使用 pnpm：

```powershell
cd H:\study-code\pet-shop\frontend
npx pnpm@7.33.7 install
npx pnpm@7.33.7 exec vite --host 127.0.0.1 --port 5173 --strictPort
```

## 普通用户测试账号

以下账号已通过前端一致的 SM3 密码摘要调用注册接口生成，数据库中每个用户都有独立 `password_salt`。

| 用户名 | 手机号 | 密码 |
| --- | --- | --- |
| alice01 | 13800000001 | Test123456 |
| bob02 | 13800000002 | Test123456 |
| cindy03 | 13800000003 | Test123456 |

普通用户只能从前台入口登录和使用业务功能。

## 管理台账号

默认超级管理员：

```text
用户名：superadmin
密码：change-me-admin-password
```

管理台账号只能从管理台入口登录。普通用户账号与管理台账号互相独立，普通用户不能登录管理台。

## 登录与接口调用规则

- 注册、密码登录、短信登录、发送验证码、重置密码属于公开接口，不需要 JWT。
- 需要登录的普通用户接口必须传 `Authorization: Bearer <token>`。
- 普通用户登录成功后，后端会返回 JWT，并写入 `app_user.jwt_token`。
- JWT 默认 6 小时有效，过期时间写入 `app_user.jwt_token_expires_at`。
- 普通用户密码流程：前端 SM3 摘要，后端拼接用户独立盐，再用 BCrypt 存储。
