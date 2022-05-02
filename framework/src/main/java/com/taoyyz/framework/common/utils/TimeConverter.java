package com.taoyyz.framework.common.utils;

public class TimeConverter {
    private static final String COURSE_TIME_FORMAT = "星期%s 第%d~%d节";
    private static final String[] CHINESE_WEEK_NUMBER = new String[]
            {"零", "一", "二", "三", "四", "五", "六", "日"};

    public static String covertTimePart(String time) {
        String[] split = time.split("-");
        int week = Integer.parseInt(split[0]);
        Integer lesson = Integer.valueOf(split[1]);
        Integer length = Integer.valueOf(split[2]);
        return String.format(COURSE_TIME_FORMAT,
                CHINESE_WEEK_NUMBER[week], lesson, lesson + length - 1);
    }
}
