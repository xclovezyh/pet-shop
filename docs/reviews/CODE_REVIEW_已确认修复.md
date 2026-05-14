# 静态代码审查报告 — 萌宠集市 Pet Shop

**审查日期**：2026-05-14  
**审查范围**：`backend/`（Spring Boot 2.7 + Java 8）、`frontend/`（React 18 + TypeScript + Vite）、`scripts/`、数据库迁移脚本、Docker 配置  
**审查方式**：静态分析，未执行自动化扫描工具

---

## 一、项目概观

一个宠物交易、领养、社交和站内私信的 Web 原型项目。技术栈为 Spring Boot 2.7 (Java 8) + React 18 + TypeScript + Vite + MySQL 8.0 + Flyway + Docker Compose。普通用户前台与管理后台分入口、分登录、分接口，两套认证体系互不干扰。整体架构清晰，分层合理。

---

## 二、架构与设计

### 2.1 做得好的

**后端分层清晰。** Controller 保持薄层，只做参数接收和路由转发；Service 负责业务编排和校验；Repository 只负责数据访问。DTO 与 Entity 分离，JPA 实体不直接暴露给 API 响应。全局异常处理器 `GlobalExceptionHandler` 统一了错误响应格式。

**普通用户和管理员体系彻底分离。** `app_user` 与 `admin_user` 分表存储，`user_session` 与 `admin_session` 分开，认证 Filter 和 Guard 各自独立。普通用户不能升级为管理员，管理员必须由已有管理员在后台创建。

**前端双入口设计。** 通过 Vite 的 `rollupOptions.input` 配置 `index.html`（用户端）和 `admin.html`（管理台）两个构建入口，代码在浏览器端完全隔离。

### 2.2 需要改进

**`UserService` 承担过多职责。** 用户注册、密码登录、短信登录、密码重置、个人资料更新、拉黑/解黑、角色管理全部集中在一个类（340+ 行）。建议按职责拆分为 `UserAuthService`、`UserProfileService` 和 `UserAdminService`，每个类控制在 150 行以内。

**前端 `main.tsx` 和 `admin.tsx` 过于庞大。** `main.tsx` 超过 1500 行，`admin.tsx` 超过 890 行。两者都是单体组件，承载了所有页面逻辑。建议：

- `admin.tsx`：将每个 Tab 面板（账号管理、用户管理、举报处理、帖子审核、动态审核、分类管理、地区库）拆分为独立组件文件
- `main.tsx`：将市场、动态、个人中心、消息等页面拆分为独立组件
- 将类型定义（如 `UserProfile`、`MarketPost`、`Moment` 等）提取到独立的 `types.ts` 文件
- 将 demo 数据和兜底数据提取到独立的 `fixtures.ts` 文件

**`admin.tsx` 中 `loadTab` 函数使用了冗长的 if-else if 链**（约 35 行）处理不同 tab 的加载。建议用策略映射或 lookup map 重构：

```typescript
const tabLoaders: Record<TabKey, (page: number) => Promise<void>> = {
  accounts: async (page) => { /* ... */ },
  users: async (page) => { /* ... */ },
  // ...
};
```

---

## 三、安全性

### 3.1 做得好的

**密码安全层次设计合理。** 前端 SM3 摘要 → 后端 per-user salt → BCrypt 加密，形成纵深防御。数据库不存明文密码。已有密码比对方法 `passwordMatches()` 支持了旧的无盐 hash 的平滑升级——如果旧用户没有 salt，验证通过后自动补充 salt 并重新 hash。

**JWT 使用白名单机制。** 服务端在 `app_user.jwt_token` 中存储当前有效 token，不是纯粹依赖 JWT 签名。这使得服务端可以通过清除 token 字段来主动让 token 失效（拉黑用户、重置密码时自动清空 JWT token）。

**前后端双重内容安全校验。** 后端 `ContentSafety` 类校验敏感词和联系方式；前端 `main.tsx` 中也有对应的 `sensitiveWords` 和 `offsiteContactPattern` 常量及校验逻辑。

**认证 Filter 正确实现。** `AuthenticationFilter` 从 `Authorization` header 提取 Bearer token，通过 JWT 解析出当前用户并注入到 request attribute，不做业务判断，责任单一。

### 3.2 需要改进

**1. JWT 密钥默认值过弱。** `application.yml` 中 `app.user-jwt-secret: change-me-user-jwt-secret` 是可预测的默认值。建议在 `UserJwtService` 启动时检查 secret 是否为默认值：当 Spring profile 为 `prod` 时，如果 secret 等于默认值则拒绝启动并打印明确的错误信息。

**2. 默认管理员密码暴露在文档中。** `README.md` 中明文列出了默认管理员密码 `change-me-admin-password`，`application.yml` 中也有相同默认值。建议首次启动时通过 `AdminBootstrapInitializer`（项目已有此组件）检查密码是否为默认值，如果是则打印强提醒或强制要求修改。

**3. 验证码明文返回存在风险。** `UserService.sendVerifyCode()` 将验证码直接返回在 API 响应中，注释说明"接入短信服务后这里不会再返回 code 字段"。这是开发便利措施，但如果某次部署忘记切换就会在生产环境暴露验证码。建议加上明确的 profile 检查：

```java
if (!Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
    response.setCode(null); // 或返回脱敏值
}
```

**4. 前端使用 `localStorage` 存储 token。** JWT token 和用户信息存储在 `localStorage` 中，在 XSS 场景下存在泄露风险。对于原型项目可以接受，如果要上线，建议考虑 httpOnly cookie 方案或 Short-lived token + refresh token 模式。

**5. 前后端正则不完全一致。** 前端 `offsiteContactPattern` 和后端 `ContentSafety.OFFSITE_CONTACT_PATTERN` 的正则表达略有差异——前端多了一个 `[1-9]\\d{5,11}` 的匹配。建议统一正则定义，或者在开发规范中明确只以后端校验为准、前端仅做初步拦截。

---

## 四、后端代码质量

### 4.1 做得好的

- 统一使用构造器注入，可测试性好
- `ApiResponse` 包装统一了所有接口的返回格式
- `UserGuard` / `AdminGuard` 的静态工具方法设计简洁，权限校验在 Controller 中一行完成
- Flyway 数据库迁移脚本从 V1 到 V15，版本管理清晰
- `PageSupport` 统一了分页逻辑
- 密码比对方法 `passwordMatches()` 设计了从无盐到有盐的平滑升级路径，业务考虑周到

### 4.2 需要改进

**1. `UserService.list()` 全表查询后在内存中分页（性能隐患）。**

```java
// UserService.java 当前实现
return PageSupport.slice(users.findAll().stream()
    .filter(user -> !UserGuard.ROLE_SUPER_ADMIN.equals(user.getRole()))
    .sorted((left, right) -> right.getId().compareTo(left.getId()))
    .collect(Collectors.toList()), page, size, this::toResponse);
```

这会将整个 `app_user` 表加载到内存中再分页。当用户量增长时会成为严重的性能瓶颈。应使用 Spring Data JPA 的 `Pageable` 在数据库层面分页：

```java
Page<AppUser> pageResult = users.findByRoleNot(
    UserGuard.ROLE_SUPER_ADMIN,
    PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"))
);
```

同样的问题可能存在于其他使用 `findAll()` 然后 stream 处理的服务中，建议全面排查。

**2. 验证码存储在内存 `ConcurrentHashMap` 中。** 服务重启后所有验证码丢失。这是原型阶段的合理选择，但要注意这是一个有状态的单机方案，无法水平扩展。后续如果引入 Redis 或消息队列，验证码管理需要重构。

**3. `UserJwtService.createToken()` 有两份重载。** 不带 `LocalDateTime` 参数的版本直接调用 `LocalDateTime.now()`，在测试中时间不可控。建议统一使用可注入的 `Clock` 或始终要求调用方传入时间。

**4. SQL 迁移脚本 V1 缺少关键索引。** `app_user` 表的 `username`、`phone`、`nickname` 字段在 login/register 场景是高频查询列，V1 建表时没有定义索引。虽然后续 V12 等迁移补充了部分索引，但建议初始建表时就加上这些关键索引，或至少在紧接着的 V2 迁移中补充，避免开发初期就留下性能坑。

**5. `adminResetPassword` 的 null 处理不够明确。**

```java
// UserService.java
public UserResponse adminResetPassword(Long id, ResetPasswordRequest request, String adminNickname) {
    // ...
    String passwordDigest = requirePasswordDigest(request == null ? null : request.getPassword());
    // 如果 request 为 null，requirePasswordDigest(null) 会抛出异常
}
```

调用方可能不理解为什么传 null 会报错。建议要么明确禁止 request 为 null（加 `@NonNull` 注解），要么在 null 时生成随机密码并返回给调用方。

**6. `@CurrentUser` 参数解析器依赖 Filter 执行顺序。** `CurrentUserArgumentResolver` 从 request attribute 中读取用户对象。如果 `AuthenticationFilter` 因为配置了排除路径而没有执行，Controller 会收到 null——目前的 Guard 方法对此做了 null 检查，处理是正确的。但建议在 `CurrentUserArgumentResolver` 中加上注释，说明它与 `AuthenticationFilter` 的依赖关系。

---

## 五、前端代码质量

### 5.1 做得好的

- TypeScript 类型定义完整，`AdminProfile`、`PageResult<T>`、`AdminPermissionCode` 等类型语义清晰
- E2E 测试通过 Playwright 的 `page.route()` 拦截 API 请求做 mock，覆盖了核心流程
- `api.ts` 中的 `repairStoredUser` 处理了乱码昵称的边界情况，体现了对异常数据的防御性思考
- `admin.tsx` 中权限过滤逻辑 `hasTabAccess` 清晰，支持 `SUPER_ADMIN` 通配和细分权限码

### 5.2 需要改进

**1. `useApi` hook 吞掉了所有错误。**

```typescript
// main.tsx
function useApi<T>(path: string, fallback: T) {
  // ...
  apiFetch(`${API_BASE}${path}`)
    .then((res) => readApiData<T>(res))
    .then((next) => setData(next ?? fallback))
    .catch(() => setData(fallback))  // 错误被静默吞掉
    .finally(() => setLoading(false));
}
```

API 加载失败时用户看到的是 fallback demo 数据，没有任何提示说这是本地兜底数据而非真实数据。建议至少区分"加载中"、"加载失败"和"加载成功"三种状态，在加载失败时展示一个不易察觉的小提示。

**2. `adminFetch` 的 Content-Type 自动设置存在边界风险。**

```typescript
if (!headers.has('Content-Type') && init.body && !(init.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
}
```

如果未来引入 `URLSearchParams` 或 `Blob` 类型的 body，会被错误地设置为 `application/json`。建议使用更精确的类型判断，或反转逻辑为显式声明需要 JSON 的场景。

**3. 硬编码的 demo 数据分散在组件中。** `main.tsx` 中的 `demoCategories`、`demoPets`、`demoPosts`、`demoMoments`、`fallbackRegions` 等硬编码数据占据了约 50 行代码。建议提取到独立的 `src/fixtures.ts` 文件。

**4. `localStorage` session 管理缺少多标签页同步。** 如果用户在多个标签页打开应用，一个标签页的登录/登出不会同步到其他标签页。可以通过 `storage` 事件监听或 BroadcastChannel API 实现跨标签页状态同步。

---

## 六、数据库设计

### 6.1 做得好的

- 使用 Flyway 管理数据库迁移，V1-V15 的增量变更记录清晰
- 表名和字段使用下划线命名法，规范统一
- 字符集使用 `utf8mb4`，支持 emoji 等四字节字符
- `post_favorites` 表有 `uk_post_favorites_user_post` 唯一约束
- 迁移脚本逐步补全了认证安全字段（V4 用户认证、V10 密码安全、V12 salt + JWT）

### 6.2 需要改进

**1. V1 schema 中部分 VARCHAR 字段长度不合理。** 例如 `pet` 表的 `gender`、`status` 字段定义为 `VARCHAR(255)`，而实际值只有几个字。虽 MySQL 的 VARCHAR 是变长存储不会浪费空间，但从数据模型自文档化角度看，长度过大的字段暗示缺少输入约束。

**2. 早期表缺少审计时间戳。** `pet` 表在 V1 中没有 `created_at` / `updated_at` 字段，`moment` 表有 `created_at` 但没有 `updated_at`。审计追踪的基础字段建议在初始建表时就加好。

**3. `private_message_threads` 表用 VARCHAR 而非外键关联用户。** 字段 `starter` 和 `recipient` 用 VARCHAR 存储用户名而非 `app_user.id`。用户改名后历史对话记录会断裂。后续迁移 V14 补充了 `user_id` 字段，说明这个问题已被察觉，但历史数据需要在迁移中处理。

---

## 七、测试

### 7.1 做得好的

- 后端 `UserServiceTest` 使用 Mockito + AssertJ，覆盖了注册、登录（含密码升级、黑名单、超级管理员拒绝登录）、密码重置、拉黑等核心场景
- 前端 Playwright E2E 测试覆盖了管理员登录、用户登录/注册、普通用户无权限访问管理等关键路径
- E2E 测试的 mock 体系设计合理，`installApiMocks` 集中管理所有 mock 端点，state 可变使得测试可以验证状态变更

### 7.2 需要改进

**1. 后端测试覆盖率不足。** 以下服务缺乏对应的单元测试：

- `AdminAuthService`（管理员认证与权限管理，安全关键模块）
- `TradeIntentService`（交易意向，核心业务流程）
- `PetService`（宠物管理）
- `ReferenceDataService`（地区库数据）

**2. 前端缺少组件单元测试。** 除了 `api.test.ts` 外，没有使用 `@testing-library/react` 对 React 组件做单元测试。`admin.tsx` 中的 `hasTabAccess`、`filterRegionTree`、`summarizeRegionTree` 等纯函数非常适合单元测试，但尚未覆盖。

**3. E2E 测试完全 mock 了后端。** Playwright 测试通过 `page.route()` 拦截了所有 API 请求并返回 mock 数据。这意味着一轮真正的集成测试都没有——前后端集成问题无法通过现有 E2E 测试发现。建议至少保留一个简单的 smoke test 走完整的后端调用链路。

**4. 测试数据中的时间断言存在脆弱性。** 例如 `UserServiceTest` 中使用了 `LocalDateTime.now()` 相关断言，但未使用 `Clock` 注入，理论上可能在特定条件下出现时间偏差导致的 flaky test。

---

## 八、DevOps 与配置

### 8.1 做得好的

- `docker-compose.yml` 配置完整，MySQL → Backend → Frontend 三项服务编排清晰，使用了 healthcheck + `depends_on` + `condition: service_healthy`
- `.gitignore` 覆盖全面：IDE 文件、构建产物、环境变量文件、日志文件均已排除
- 前后端都有 Dockerfile，前端使用多阶段构建（Node 18 构建 + Nginx 服务）
- Vite 配置了开发代理 `/api` → `localhost:8080`，前后端分离开发体验流畅

### 8.2 需要改进

**1. `flyway.enabled: false` 默认关闭迁移。** 意味着首次启动不会自动执行数据库迁移脚本。新手部署时可能会遇到"表不存在"的错误。建议在 README 中明确说明需要手动执行迁移，或改为默认开启并配合 `baseline-on-migrate: true`（当前已配置此属性）。

**2. Docker Compose 的数据库密码默认值较弱。** `MYSQL_ROOT_PASSWORD` 默认为 `change-root-password`。虽然可以通过 `.env` 覆盖，建议在不会纳入版本控制的 `.env.example` 中提供更安全的示例值。

**3. 缺少 `.env.example` 文件。** `.gitignore` 排除了 `.env` 和 `.env.*` 文件，但项目中没有提供 `.env.example` 作为模板，新开发者在启动 Docker Compose 前需要自己猜测需要哪些环境变量。

**4. 前端 Nginx 配置未出现在审查中。** 不清楚 Nginx 是否配置了安全响应头（如 `Content-Security-Policy`、`X-Frame-Options`、`Strict-Transport-Security` 等）、gzip 压缩、缓存策略等。建议审查 `nginx.conf` 的完整配置。

---

## 九、改进优先级建议

### 高优先级（建议在上线前解决）

1. **JWT 密钥随机化**：生产环境必须使用强随机密钥，禁止使用默认值
2. **默认管理员密码强制修改**：首次启动时检测并强制要求修改
3. **全表查询改数据库分页**：`UserService.list()` 等方法的性能隐患
4. **验证码返回加 profile 检查**：防止生产环境暴露验证码

### 中优先级（建议在下一迭代解决）

5. **拆分 `UserService` 和前端大组件**：提升可维护性
6. **补充 `AdminAuthService` 的单元测试**：安全关键模块
7. **数据库关键索引补充**：`username`、`phone`、`nickname` 等高频查询字段
8. **补充 `.env.example` 文件**：降低新人上手成本

### 低优先级（可在后续迭代逐步优化）

9. 前端组件单元测试覆盖
10. 前后端正则表达式统一
11. `Clock` 注入提升测试稳定性
12. Nginx 安全响应头配置
13. 跨标签页 session 同步

---

*报告结束*
