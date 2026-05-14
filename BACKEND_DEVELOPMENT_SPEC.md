# 后端开发规范

后续后端开发默认遵守本规范。

## 1. 分层约束

- Controller 只负责收参、出参和路由，不直接编写业务逻辑。
- Service 负责业务编排、校验和状态流转。
- Repository 只负责数据访问。
- DTO 负责接口输入输出。
- Entity 不直接作为普通接口返回对象。

## 2. 认证与权限

### 普通用户体系

- 用户表：`app_user`
- 登录接口：`/api/users/...`
- 登录成功后后端返回 JWT，并写入 `app_user.jwt_token`。
- JWT 过期时间写入 `app_user.jwt_token_expires_at`，默认有效期 6 小时。
- 需要登录的普通用户接口必须传 `Authorization: Bearer <token>`。
- 注册、密码登录、短信登录、发送验证码、重置密码等公开接口不得要求 JWT。

### 密码体系

- 前端提交密码前必须先做 SM3 摘要。
- 后端首次设置密码时为每个用户生成独立盐，写入 `app_user.password_salt`。
- 后端存储密码时使用 `salt + ":" + sm3Digest` 再进行 BCrypt 加密。
- 登录校验时必须使用数据库中该用户自己的盐。
- 不允许接口返回 `password_hash`、`password_salt`、JWT 明文等敏感字段。

### 管理员体系

- 管理员表：`admin_user`
- 管理员登录接口：`/api/admin/auth/...`
- 管理台接口：`/api/admin/...`
- 管理员账号与普通用户账号互相独立，不能混用。
- 普通用户不能升级为管理员。
- 新管理员账号必须由已登录管理员在管理后台创建。

## 3. 接口规范

- 统一返回 `ApiResponse<T>`。
- 成功返回 `success=true`。
- 失败返回明确的 `code` 和 `message`。
- Controller 参数使用 Request DTO，不直接暴露 Entity。
- 需要登录态的接口从鉴权上下文获取当前用户，不信任前端传入的身份字段。

## 4. 分页规范

- 所有分页列表默认每页 10 条记录。
- 分页参数统一使用 `page` 和 `size`。
- `page` 从 1 开始。
- `size` 必须有上限，避免一次返回过多数据。
- 分页结果统一使用 `PageResponse<T>`。

示例：

```json
{
  "items": [],
  "total": 0,
  "page": 1,
  "size": 10,
  "totalPages": 0,
  "hasNext": false,
  "hasPrevious": false
}
```

## 5. 数据库与迁移

- 表结构变更必须通过 `db/migration` 脚本维护。
- 开发期允许手动兼容旧库，但新增结构必须同步写入迁移脚本。
- 用户相关测试数据可以清理重建，但不得误删 `admin_user` 和 `admin_session`。
- 清理普通用户时，应同步清理普通用户关联业务表，例如收藏、帖子、动态、私信、举报、交易意向和普通用户会话。

## 6. 错误码规范

- 使用 `ApiErrorCode`。
- 新增错误场景时同步补充枚举和语义说明。
- 错误信息必须可读，避免空泛描述。

## 7. 文档与编码

- 文档统一使用 UTF-8。
- 中文内容不得出现乱码。
- 访问入口、测试账号、启动方式统一维护在 `ACCESS_GUIDE.md`。
