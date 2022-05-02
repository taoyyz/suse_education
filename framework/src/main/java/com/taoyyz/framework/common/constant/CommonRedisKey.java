package com.taoyyz.framework.common.constant;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 0:40
 */
public interface CommonRedisKey {
    /**
     * 用户id和token的关联键
     */
    String USER_TOKEN_KEY = "user:token:%s";

    /**
     * 用户id和权限id的关联键
     */
    String USER_AUTH_KEY = "user:auth:%s";

    /**
     * token和用户信息的关联键
     */
    String TOKEN_USER_KEY = "token:user:%s";

    /**
     * 新闻列表的键
     */
    String NEWS_KEY = "news";
    String DEPARTMENT_MAP_KEY = "department:map";
    String MAJOR_MAP_KEY = "major:map";
}
