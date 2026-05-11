package com.petshop.support;

import com.petshop.api.ApiErrorCode;
import com.petshop.api.ApiException;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class ContentSafety {
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?:\\+?86[-\\s]?)?1[3-9]\\d{9}");
    private static final Pattern OFFSITE_CONTACT_PATTERN = Pattern.compile("(?i)(微信|vx|wechat|qq|企鹅|扣扣)[:：\\s-]*[a-z0-9_-]{4,}|[1-9]\\d{5,11}");
    private static final List<String> SENSITIVE_WORDS = Arrays.asList(
            "虐待", "毒药", "赌博", "色情", "诈骗", "保护动物", "野生动物", "线下交易", "加微信", "加qq"
    );

    private ContentSafety() {
    }

    public static void validate(String content) {
        String text = safe(content).toLowerCase();
        if (PHONE_PATTERN.matcher(text).find() || OFFSITE_CONTACT_PATTERN.matcher(text).find()) {
            throw new ApiException(ApiErrorCode.CONTENT_CONTACT_FORBIDDEN, "禁止填写手机号、微信号或 QQ 号，请使用站内沟通");
        }
        for (String word : SENSITIVE_WORDS) {
            if (text.contains(word.toLowerCase())) {
                throw new ApiException(ApiErrorCode.CONTENT_SENSITIVE_FORBIDDEN, "内容包含敏感词，请修改后再提交");
            }
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
