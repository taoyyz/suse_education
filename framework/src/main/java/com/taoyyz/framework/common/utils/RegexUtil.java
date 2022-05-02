package com.taoyyz.framework.common.utils;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 1:05
 */
public class RegexUtil {
    public static final String USERNAME_REGEX = "\\w{4,8}";
    public static final String USERNAME_MSG = "用户名为4到8位的数字、字母、下划线";
    public static final String NUMBER_REGEX = "\\d{6,12}";
    public static final String NUMBER_MSG = "学号/工号为6到12位的数字";
    public static final String PASSWORD_REGEX = "[0-9a-zA-Z]{6,16}";
    public static final String PASSWORD_MSG = "密码为6到16位的数字或中英文";
}
