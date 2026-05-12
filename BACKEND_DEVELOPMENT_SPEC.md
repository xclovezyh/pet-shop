# 后端开发规范

## 1. 适用范围

本规范适用于 `backend/` 下的所有新增功能、重构功能和缺陷修复。后续开发默认遵循本文件，不再回退到 Controller 直接拼接 Repository、直接返回 Entity 的写法。

## 2. 架构分层

统一采用 MVC + Service 的后端组织方式：

- `controller`：只负责路由、入参接收、调用 service、返回统一响应。
- `service`：承接业务规则、权限判断、数据组装、状态流转。
- `repository`：只负责数据库访问，不承载业务逻辑。
- `model`：JPA 持久化实体，只在持久化层和 service 内部使用。
- `dto`：接口入参、出参对象。Controller 不直接暴露 Entity。
- `api`：统一响应体、错误码、异常处理等协议基础设施。
- `support` / `config`：横切能力、启动初始化、配置类。

禁止事项：

- Controller 直接返回 Entity。
- Controller 直接操作多个 Repository 拼业务流程。
- Repository 暴露给前端协议层。
- 前端依赖数据库字段名推断业务语义。

## 3. 接口协议

### 3.1 成功响应格式

新增或重构接口统一返回：

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "ok",
  "data": {}
}
```

字段约定：

- `success`：是否成功。
- `code`：业务响应码，成功固定为 `SUCCESS`。
- `message`：面向调用方的简短说明。
- `data`：实际业务数据；无数据时返回 `null` 或空集合。
- `timestamp`：由后端统一补充。

### 3.2 失败响应格式

失败响应统一返回：

```json
{
  "success": false,
  "code": "USER_400_001",
  "message": "用户名已被注册",
  "data": null
}
```

### 3.3 HTTP 状态码规则

- `200 OK`：查询、更新成功。
- `201 Created`：后续如新增严格创建语义接口时使用。
- `400 Bad Request`：参数非法、状态不允许、验证码错误等。
- `401 Unauthorized`：未登录、账号密码错误。
- `403 Forbidden`：角色不够、账号被限制、越权访问。
- `404 Not Found`：资源不存在。
- `500 Internal Server Error`：未预期异常。

## 4. 错误码规范

错误码格式：

```text
{模块}_{HTTP状态码}_{三位序号}
```

示例：

- `USER_400_001`：用户名已被注册
- `USER_401_001`：账号或密码错误
- `COMMON_403`：没有权限执行该操作

模块建议：

- `COMMON`：通用错误
- `USER`：用户与账号
- `POST`：交易帖子
- `MOMENT`：日常动态
- `MESSAGE`：站内私信
- `TRADE`：交易意向
- `ADMIN`：管理后台
- `UPLOAD`：文件上传

要求：

- 错误码必须稳定，不能随意复用含义。
- 同一业务错误尽量复用同一错误码。
- 对外提示优先中文、可直接展示给前端。

## 5. DTO 规范

### 5.1 入参 DTO

- 命名：`XxxRequest`
- 位置：`dto/<module>/`
- 只包含接口所需字段，不透出数据库冗余字段。
- Controller 接收 DTO，不接收 `Map<String, String>` 作为长期方案。

### 5.2 出参 DTO

- 命名：`XxxResponse` 或 `XxxItemResponse`
- 不直接返回 Entity。
- 对敏感字段做裁剪：
  - 不返回密码哈希、密码盐
  - 不返回仅供内部流转的中间状态
  - 不返回前端无权看到的管理字段

### 5.3 列表与分页

当前项目可先统一为列表 `data: []`。
后续分页接口统一升级为：

```json
{
  "list": [],
  "pageNo": 1,
  "pageSize": 20,
  "total": 100
}
```

## 6. Controller 规范

- 一个 Controller 只负责一个明确领域。
- Controller 方法只做 4 件事：
  - 接收参数
  - 调用 Service
  - 返回 `ApiResponse`
  - 声明路由与语义
- 参数校验和权限校验优先放到 Service 或统一组件中。
- 不在 Controller 中拼装复杂业务对象。
- Controller 路由保持 REST 风格，查询、创建、更新、删除语义清晰。
- 管理台接口统一显式区分，例如 `/admin`、`/{id}/audit`、`/{id}/role`。

## 7. Service 规范

- Service 是业务逻辑唯一承载层。
- 多表联动、权限流转、状态判断、消息生成都放在 Service。
- Service 内可以使用 Entity，但对外只返回 DTO。
- 复杂逻辑前允许添加少量解释性注释。
- Service 命名使用领域名，例如 `UserService`、`MarketPostService`。
- 后续涉及多表一致性的写操作，优先在 Service 上补 `@Transactional`。

## 8. Entity 规范

- Entity 只服务持久化。
- 保留中文字段说明注释。
- 敏感字段使用 `@JsonIgnore`，避免被误返回。
- Entity 不承担前端协议职责。

## 9. Repository 规范

- Repository 只声明查询方法。
- 不在 Repository 中封装业务语义。
- 命名要体现查询意图，例如 `findByUsername`、`findByPhone`。

## 10. DTO 与字段命名规范

- 请求 DTO 使用 `XxxRequest`。
- 列表项或详情出参使用 `XxxResponse`。
- 管理动作如果只有单字段更新，可用 `UpdateXxxRequest`。
- 字段命名优先与前端协议保持稳定，不为数据库字段妥协暴露内部细节。
- 不允许前端直接依赖 JPA Entity 的新增字段。

## 11. 异常处理规范

- 业务异常统一抛 `ApiException`。
- 历史接口保留 `ResponseStatusException` 时，由全局异常处理转换为统一响应。
- 不在 Controller 里手写 `try/catch` 返回错误字符串。
- 不吞异常；无法处理的异常交给全局异常处理器兜底。

## 12. 参数校验规范

- Controller 不再长期接收 `Map<String, String>` 作为正式入参模型。
- 基础格式校验可放在 Service，后续逐步补到 `javax.validation` 注解。
- 校验失败统一返回明确中文文案和稳定错误码。
- 所有字符串入参在进入核心逻辑前做 `trim` 或标准化处理。

## 13. 安全与权限规范

- 普通用户只能操作自己的内容。
- 管理后台接口必须校验超级管理员身份。
- 超级管理员账号由系统配置预置到数据库，不走前台注册。
- 后续新增管理员必须由已有超级管理员在后台授权。
- 密码、盐、内部凭据等敏感字段禁止透出到接口响应。

## 14. 数据库与迁移规范

- 所有表结构变更必须走 Flyway 脚本，不直接依赖 Hibernate 自动改表作为正式交付方案。
- 迁移脚本命名使用 `V{n}__description.sql`。
- 已发布迁移不直接改写，新增版本脚本承接变更。
- 表和字段补充中文注释时，优先通过新迁移完成。

## 15. 测试与验证规范

每轮重构或新增功能至少执行：

- `mvn package -DskipTests`
- `tsc -b`
- `vite build`

涉及接口协议变化时，额外执行：

- 至少一条关键接口的请求验证
- 至少一条失败场景的错误码或错误消息验证

## 16. 文档维护规范

每次新增或重构接口时，至少同步更新以下内容之一：

- 本规范文件中的通用约束
- `README.md` 中的运行、配置或账号说明
- 对应模块的接口说明或功能文档

## 17. 开发流程规范

后续开发默认按这个顺序推进：

1. 先定义入参 DTO、出参 DTO、错误码。
2. 再实现 Service 业务逻辑。
3. Controller 只接线并返回 `ApiResponse`。
4. 前端适配统一解包。
5. 补文档、跑验证。

## 18. 当前落地状态

已落地：

- 用户模块开始迁移到 `ApiResponse + DTO + Service`
- 统一错误码与全局异常处理基础设施已建立
- 管理员预置账号初始化已建立
- 交易帖子模块开始迁移到 `ApiResponse + DTO + Service`
- 日常动态与评论模块开始迁移到 `ApiResponse + DTO + Service`

待继续迁移：

- 私信模块
- 交易意向模块
- 收藏模块
- 举报与管理后台其余接口


## 19. 2026-05-12 ??????

????????????????????????????????? Controller ???? Repository ????? Entity ????

- ????
- ??????
- ?????????
- ????
- ??????
- ??????
- ????
- ??????
- ??????
- ?????????
- ????
- ???????

???????

- ????????? Request DTO?Response DTO?ApiErrorCode?Service?
- ??????????????????????????
- ????????????? support ? service???? Controller ????????
- ??????????????? readApiData ?????????????????????????


## 20. ??????

???????????????

- ?? Service ????
- ?? Controller / API ?????
- ?? API ??????

?????????

- ?? Service ??????????????
- ???????????????????????????
- ??????????????????????????????
