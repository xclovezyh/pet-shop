# 后端下一阶段五项功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 继续收口后端核心业务身份链路，并补齐管理员处置、审计与举报处理能力。

**Architecture:** 以现有 Spring Boot + JPA 结构为基础，优先收紧 `user_id` 主路径，再补齐管理员侧操作闭环。新增的审计日志单独建表，管理员动作通过服务层统一写入，避免散落在控制器里。所有改动都保持 DTO / Service / Repository 的现有分层风格。

**Tech Stack:** Java 8, Spring Boot 2.7, Spring Data JPA, Flyway, JUnit 5, Mockito

---

### Task 1: 管理员重置用户密码

**Files:**
- Modify: `backend/src/main/java/com/petshop/service/UserService.java`
- Modify: `backend/src/main/java/com/petshop/controller/AdminController.java`
- Create: `backend/src/main/java/com/petshop/dto/user/AdminResetPasswordRequest.java`
- Modify: `backend/src/test/java/com/petshop/service/UserServiceTest.java`

- [x] **Step 1: Write the failing test**

```java
@Test
void adminResetPasswordShouldReplacePasswordSaltAndClearSession() {
    AppUser user = new AppUser();
    user.setId(7L);
    user.setUsername("user7");
    user.setPhone("13800138007");
    user.setNickname("user7");
    user.setPasswordSalt("old-salt");
    user.setPasswordHash("old-hash");
    user.setJwtToken("token-old");
    user.setRole("USER");
    user.setBlacklisted(false);

    when(users.findById(7L)).thenReturn(Optional.of(user));
    when(users.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

    AdminResetPasswordRequest request = new AdminResetPasswordRequest();
    request.setPassword(PASSWORD_DIGEST);

    UserResponse response = userService.adminResetPassword(7L, request, "admin");

    assertThat(response.getId()).isEqualTo(7L);
    assertThat(user.getPasswordSalt()).isNotEqualTo("old-salt");
    assertThat(user.getPasswordHash()).isNotEqualTo("old-hash");
    assertThat(user.getJwtToken()).isEmpty();
}
```

- [x] **Step 2: Run the test to verify it fails**

Run: `mvn -Dtest=UserServiceTest#adminResetPasswordShouldReplacePasswordSaltAndClearSession test`
Expected: FAIL with `method adminResetPassword not found`

- [x] **Step 3: Write minimal implementation**

```java
public UserResponse adminResetPassword(Long id, AdminResetPasswordRequest request, String adminNickname) {
    AppUser user = users.findById(id).orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND));
    String passwordDigest = requirePasswordDigest(request.getPassword());
    String salt = generatePasswordSalt();
    user.setPasswordSalt(salt);
    user.setPasswordHash(hashPassword(passwordDigest, salt));
    user.setJwtToken("");
    user.setJwtTokenExpiresAt(null);
    return toResponse(users.save(user));
}
```

- [x] **Step 4: Run the test to verify it passes**

Run: `mvn -Dtest=UserServiceTest#adminResetPasswordShouldReplacePasswordSaltAndClearSession test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add backend/src/main/java/com/petshop/service/UserService.java backend/src/main/java/com/petshop/controller/AdminController.java backend/src/main/java/com/petshop/dto/user/AdminResetPasswordRequest.java backend/src/test/java/com/petshop/service/UserServiceTest.java
git commit -m "feat: add admin password reset"
```

### Task 2: 核心业务只走 user_id

**Files:**
- Modify: `backend/src/main/java/com/petshop/service/MomentService.java`
- Modify: `backend/src/main/java/com/petshop/service/PrivateMessageService.java`
- Modify: `backend/src/main/java/com/petshop/service/TradeIntentService.java`
- Modify: `backend/src/main/java/com/petshop/service/ContentReportService.java`
- Modify: `backend/src/test/java/com/petshop/service/MomentServiceTest.java`
- Modify: `backend/src/test/java/com/petshop/service/PrivateMessageServiceTest.java`
- Modify: `backend/src/test/java/com/petshop/service/TradeIntentServiceTest.java`
- Modify: `backend/src/test/java/com/petshop/service/ContentReportServiceTest.java`

- [x] **Step 1: Write the failing tests**

```java
@Test
void updateShouldRejectNicknameFallbackWhenUserIdsDiffer() {
    Moment moment = new Moment();
    moment.setId(1L);
    moment.setAuthor("alice");
    moment.setAuthorUserId(10L);
    when(moments.findById(1L)).thenReturn(Optional.of(moment));

    AppUser currentUser = new AppUser();
    currentUser.setId(11L);
    currentUser.setNickname("alice");
    currentUser.setRole("USER");
    currentUser.setBlacklisted(false);

    assertThatThrownBy(() -> momentService.update(1L, currentUser, new MomentRequest()))
            .isInstanceOf(ApiException.class)
            .extracting(error -> ((ApiException) error).getErrorCode())
            .isEqualTo(ApiErrorCode.MOMENT_AUTHOR_MISMATCH);
}
```

- [x] **Step 2: Run the tests to verify they fail**

Run: `mvn -Dtest=MomentServiceTest,PrivateMessageServiceTest,TradeIntentServiceTest,ContentReportServiceTest test`
Expected: FAIL on nickname fallback cases

- [x] **Step 3: Write minimal implementation**

```java
private boolean ownsMoment(Moment moment, AppUser user) {
    return moment.getAuthorUserId() != null && user.getId() != null && moment.getAuthorUserId().equals(user.getId());
}
```

- [x] **Step 4: Run the tests to verify they pass**

Run: `mvn -Dtest=MomentServiceTest,PrivateMessageServiceTest,TradeIntentServiceTest,ContentReportServiceTest test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add backend/src/main/java/com/petshop/service/MomentService.java backend/src/main/java/com/petshop/service/PrivateMessageService.java backend/src/main/java/com/petshop/service/TradeIntentService.java backend/src/main/java/com/petshop/service/ContentReportService.java backend/src/test/java/com/petshop/service/MomentServiceTest.java backend/src/test/java/com/petshop/service/PrivateMessageServiceTest.java backend/src/test/java/com/petshop/service/TradeIntentServiceTest.java backend/src/test/java/com/petshop/service/ContentReportServiceTest.java
git commit -m "feat: close core identity fallback"
```

### Task 3: 管理员账号处置入口收口

**Files:**
- Modify: `backend/src/main/java/com/petshop/controller/AdminController.java`
- Modify: `backend/src/main/java/com/petshop/service/UserService.java`
- Create: `backend/src/main/java/com/petshop/dto/user/AdminDisableUserRequest.java`
- Modify: `backend/src/test/java/com/petshop/controller/UserControllerTest.java`

- [x] **Step 1: Write the failing test**

```java
@Test
void adminBlacklistShouldClearSessionAndBlockLogin() {
    // assert the user is blacklisted and token is cleared after admin action
}
```

- [x] **Step 2: Run the test to verify it fails**

Run: `mvn -Dtest=UserControllerTest test`
Expected: FAIL before the controller/service wiring exists

- [x] **Step 3: Write minimal implementation**

```java
user.setBlacklisted(true);
user.setBlacklistReason(reason);
user.setJwtToken("");
user.setJwtTokenExpiresAt(null);
```

- [x] **Step 4: Run the test to verify it passes**

Run: `mvn -Dtest=UserControllerTest test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add backend/src/main/java/com/petshop/controller/AdminController.java backend/src/main/java/com/petshop/service/UserService.java backend/src/main/java/com/petshop/dto/user/AdminDisableUserRequest.java backend/src/test/java/com/petshop/controller/UserControllerTest.java
git commit -m "feat: tighten admin account actions"
```

### Task 4: 管理后台审计日志

**Files:**
- Create: `backend/src/main/java/com/petshop/model/AdminActionLog.java`
- Create: `backend/src/main/java/com/petshop/repository/AdminActionLogRepository.java`
- Create: `backend/src/main/java/com/petshop/service/AdminActionLogService.java`
- Create: `backend/src/main/resources/db/migration/V15__admin_action_log.sql`
- Modify: `backend/src/main/java/com/petshop/controller/AdminController.java`
- Modify: `backend/src/test/java/com/petshop/service/AdminActionLogServiceTest.java`

- [x] **Step 1: Write the failing test**

```java
@Test
void recordShouldPersistAdminAction() {
    // save one log entry and verify stored fields
}
```

- [x] **Step 2: Run the test to verify it fails**

Run: `mvn -Dtest=AdminActionLogServiceTest test`
Expected: FAIL because service is missing

- [x] **Step 3: Write minimal implementation**

```java
public void record(String adminUsername, String action, String targetType, Long targetId, String detail) {
    AdminActionLog log = new AdminActionLog();
    log.setAdminUsername(adminUsername);
    log.setAction(action);
    log.setTargetType(targetType);
    log.setTargetId(targetId);
    log.setDetail(detail);
    log.setCreatedAt(LocalDateTime.now());
    repository.save(log);
}
```

- [x] **Step 4: Run the test to verify it passes**

Run: `mvn -Dtest=AdminActionLogServiceTest test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add backend/src/main/java/com/petshop/model/AdminActionLog.java backend/src/main/java/com/petshop/repository/AdminActionLogRepository.java backend/src/main/java/com/petshop/service/AdminActionLogService.java backend/src/main/resources/db/migration/V15__admin_action_log.sql backend/src/main/java/com/petshop/controller/AdminController.java backend/src/test/java/com/petshop/service/AdminActionLogServiceTest.java
git commit -m "feat: add admin audit log"
```

### Task 5: 举报处理闭环再收紧

**Files:**
- Modify: `backend/src/main/java/com/petshop/service/ContentReportService.java`
- Modify: `backend/src/main/java/com/petshop/controller/AdminController.java`
- Modify: `backend/src/test/java/com/petshop/service/ContentReportServiceTest.java`

- [x] **Step 1: Write the failing test**

```java
@Test
void handleShouldBlacklistByUserIdOnly() {
    // a report target with authorUserId should blacklist that exact user
}
```

- [x] **Step 2: Run the test to verify it fails**

Run: `mvn -Dtest=ContentReportServiceTest test`
Expected: FAIL before strict userId handling is in place

- [x] **Step 3: Write minimal implementation**

```java
if (author.userId == null) {
    throw new ApiException(ApiErrorCode.USER_NOT_FOUND, "内容作者不支持昵称回退");
}
AppUser user = users.findById(author.userId).orElseThrow(() -> new ApiException(ApiErrorCode.USER_NOT_FOUND, "内容作者不存在"));
```

- [x] **Step 4: Run the test to verify it passes**

Run: `mvn -Dtest=ContentReportServiceTest test`
Expected: PASS

- [x] **Step 5: Commit**

```bash
git add backend/src/main/java/com/petshop/service/ContentReportService.java backend/src/main/java/com/petshop/controller/AdminController.java backend/src/test/java/com/petshop/service/ContentReportServiceTest.java
git commit -m "feat: tighten report handling flow"
```
