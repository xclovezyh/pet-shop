package com.petshop.api;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    INVALID_PARAM(HttpStatus.BAD_REQUEST, "COMMON_400", "请求参数不合法"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "请先登录"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "没有权限执行该操作"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "请求资源不存在"),
    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "系统繁忙，请稍后重试"),
    CONTENT_CONTACT_FORBIDDEN(HttpStatus.BAD_REQUEST, "COMMON_400_001", "禁止填写站外联系方式"),
    CONTENT_SENSITIVE_FORBIDDEN(HttpStatus.BAD_REQUEST, "COMMON_400_002", "内容包含敏感词"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404_001", "用户不存在"),
    USERNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER_400_001", "用户名已被注册"),
    PHONE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "USER_400_002", "手机号已被注册"),
    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "USER_400_003", "用户名格式不正确"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_400_004", "密码格式不正确"),
    INVALID_PHONE(HttpStatus.BAD_REQUEST, "USER_400_005", "手机号格式不正确"),
    INVALID_VERIFY_CODE(HttpStatus.BAD_REQUEST, "USER_400_006", "验证码不正确或已过期"),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "USER_401_001", "账号或密码错误"),
    ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "USER_403_001", "账号已被限制"),
    SUPER_ADMIN_REGISTER_FORBIDDEN(HttpStatus.BAD_REQUEST, "USER_400_007", "超级管理员账号由系统预置，不能在前台注册"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "USER_400_008", "不支持的用户角色"),
    SELF_ROLE_REVOKE_FORBIDDEN(HttpStatus.BAD_REQUEST, "USER_400_009", "不能取消当前登录管理员自己的权限"),

    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST_404_001", "帖子不存在"),
    POST_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "POST_400_001", "请选择宠物分类"),
    POST_PRICE_INVALID(HttpStatus.BAD_REQUEST, "POST_400_002", "价格不能小于 0"),
    POST_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "POST_403_001", "只能操作自己的帖子"),
    POST_AUDIT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "POST_400_003", "不支持的审核状态"),

    MOMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "MOMENT_404_001", "动态不存在"),
    MOMENT_CATEGORY_REQUIRED(HttpStatus.BAD_REQUEST, "MOMENT_400_001", "请选择宠物分类"),
    MOMENT_AUTHOR_MISMATCH(HttpStatus.FORBIDDEN, "MOMENT_403_001", "只能操作自己的动态"),
    MOMENT_AUDIT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "MOMENT_400_002", "不支持的审核状态"),
    MOMENT_COMMENT_EMPTY(HttpStatus.BAD_REQUEST, "MOMENT_400_003", "请输入评论内容"),

    FAVORITE_POST_REQUIRED(HttpStatus.BAD_REQUEST, "FAVORITE_400_001", "请选择要收藏的帖子"),
    FAVORITE_USER_REQUIRED(HttpStatus.UNAUTHORIZED, "FAVORITE_401_001", "请先登录后再收藏"),

    TRADE_INTENT_POST_REQUIRED(HttpStatus.BAD_REQUEST, "TRADE_400_001", "请选择要预约的交易帖"),
    TRADE_INTENT_POST_AUTHOR_EMPTY(HttpStatus.BAD_REQUEST, "TRADE_400_002", "帖子缺少发布者，无法提交意向"),
    TRADE_INTENT_SELF_FORBIDDEN(HttpStatus.BAD_REQUEST, "TRADE_400_003", "不能给自己的帖子提交意向"),
    TRADE_INTENT_DUPLICATE(HttpStatus.BAD_REQUEST, "TRADE_400_004", "你已经提交过该帖子意向"),
    TRADE_INTENT_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "TRADE_400_005", "请输入意向说明"),
    TRADE_INTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TRADE_404_001", "意向单不存在"),
    TRADE_INTENT_FORBIDDEN(HttpStatus.FORBIDDEN, "TRADE_403_001", "只能处理自己的意向单"),
    TRADE_INTENT_REQUESTER_CANCEL_ONLY(HttpStatus.FORBIDDEN, "TRADE_403_002", "买家只能取消自己的意向单"),
    TRADE_INTENT_OWNER_CANCEL_FORBIDDEN(HttpStatus.FORBIDDEN, "TRADE_403_003", "发布者不能替买家取消意向单"),
    TRADE_INTENT_STATUS_INVALID(HttpStatus.BAD_REQUEST, "TRADE_400_006", "不支持的意向状态"),

    MESSAGE_USER_REQUIRED(HttpStatus.UNAUTHORIZED, "MESSAGE_401_001", "请先登录后再使用私信"),
    MESSAGE_POST_REQUIRED(HttpStatus.BAD_REQUEST, "MESSAGE_400_001", "请选择要私信的帖子"),
    MESSAGE_POST_AUTHOR_EMPTY(HttpStatus.BAD_REQUEST, "MESSAGE_400_002", "帖子缺少发布者，无法发起私信"),
    MESSAGE_SELF_FORBIDDEN(HttpStatus.BAD_REQUEST, "MESSAGE_400_003", "不能给自己的帖子发私信"),
    MESSAGE_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "MESSAGE_400_004", "请输入私信内容"),
    MESSAGE_THREAD_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE_404_001", "私信会话不存在"),
    MESSAGE_THREAD_FORBIDDEN(HttpStatus.FORBIDDEN, "MESSAGE_403_001", "只能查看自己的私信"),

    REPORT_CREATE_PAYLOAD_REQUIRED(HttpStatus.BAD_REQUEST, "REPORT_400_001", "请填写举报信息"),
    REPORT_REPORTER_REQUIRED(HttpStatus.UNAUTHORIZED, "REPORT_401_001", "请先登录后再举报"),
    REPORT_TARGET_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "REPORT_400_002", "请选择举报内容类型"),
    REPORT_TARGET_REQUIRED(HttpStatus.BAD_REQUEST, "REPORT_400_003", "请选择举报内容"),
    REPORT_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "REPORT_400_004", "请填写举报原因"),
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_404_001", "举报记录不存在"),
    REPORT_OPERATOR_REQUIRED(HttpStatus.BAD_REQUEST, "REPORT_400_005", "请填写处理人"),
    REPORT_TARGET_TYPE_UNSUPPORTED(HttpStatus.BAD_REQUEST, "REPORT_400_006", "暂不支持该内容类型举报"),
    REPORT_ACTION_INVALID(HttpStatus.BAD_REQUEST, "REPORT_400_007", "不支持的举报处理动作"),

    CATEGORY_PAYLOAD_REQUIRED(HttpStatus.BAD_REQUEST, "CATEGORY_400_001", "请填写分类信息"),
    CATEGORY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "CATEGORY_400_002", "请填写分类名称"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY_404_001", "分类不存在"),

    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_404_001", "宠物资料不存在"),

    REGION_PAYLOAD_REQUIRED(HttpStatus.BAD_REQUEST, "REGION_400_001", "请填写地区信息"),
    REGION_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "REGION_400_002", "请填写地区名称"),
    REGION_LEVEL_INVALID(HttpStatus.BAD_REQUEST, "REGION_400_003", "地区层级只能是 province、city 或 district"),
    REGION_PARENT_REQUIRED(HttpStatus.BAD_REQUEST, "REGION_400_004", "市区必须选择上级地区"),
    REGION_NOT_FOUND(HttpStatus.NOT_FOUND, "REGION_404_001", "地区不存在"),

    UPLOAD_FILE_REQUIRED(HttpStatus.BAD_REQUEST, "UPLOAD_400_001", "请选择要上传的文件"),
    UPLOAD_PATH_INVALID(HttpStatus.BAD_REQUEST, "UPLOAD_400_002", "非法上传路径");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ApiErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
