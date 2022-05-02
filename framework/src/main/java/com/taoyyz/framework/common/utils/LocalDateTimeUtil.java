package com.taoyyz.framework.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/16 20:56
 */
public class LocalDateTimeUtil {
    /**
     * 根据指定的格式，把指定的时间字符串转换为{@link LocalDateTime}对象
     *
     * @param time    要被转换的日期时间字符串
     * @param pattern 时间日期格式
     * @return 转化后的LocalDateTime对象
     */
    public static LocalDateTime convertToLocalDateTime(String time, String pattern) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 把指定的时间字符串转换为{@link LocalDateTime}对象，默认格式为yyyy-MM-dd HH:mm
     *
     * @param time 要被转换的日期时间字符串
     * @return 转化后的LocalDateTime对象
     */
    public static LocalDateTime convertToLocalDateTime(String time) {
        return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    /**
     * 根据指定的格式，把指定的{@link LocalDateTime}对象，转换为字符串表示
     *
     * @param localDateTime 要被转换的LocalDateTime对象
     * @param pattern       日期时间格式
     * @return 转化后的日期时间字符串
     */
    public static String convertToString(LocalDateTime localDateTime, String pattern) {
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 把指定的{@link LocalDateTime}对象，转换为字符串表示，默认格式为yyyy-MM-dd HH:mm
     *
     * @param localDateTime 要被转换的LocalDateTime对象
     * @return 转化后的日期时间字符串
     */
    public static String convertToString(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
