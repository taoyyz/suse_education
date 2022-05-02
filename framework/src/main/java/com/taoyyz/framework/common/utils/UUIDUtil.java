package com.taoyyz.framework.common.utils;

import java.util.UUID;

/**
 * 产生UUID随机字符串工具类
 */
public final class UUIDUtil {
    private UUIDUtil() {
    }

    public static String getUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
