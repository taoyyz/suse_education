package com.taoyyz.framework.common.enums;

import lombok.Getter;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 23:04
 */
@Getter
public enum ApiCode {
    OK(200, "操作成功"),
    ERROR(500, "操作失败"),
    UN_LOGIN(503, "未登录"),
    LOGIN_EXPIRED(504, "登录已过期");

    private final Integer code;
    private final String message;

    ApiCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
