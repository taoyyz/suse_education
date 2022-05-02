package com.taoyyz.framework.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.enums.ApiCode;
import com.taoyyz.framework.common.exception.UserInfoException;
import com.taoyyz.framework.web.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 15:26
 */
@Component
@Slf4j
public class UserInfoUtil {
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ObjectMapper objectMapper;

    public Long getUserId() {
        UserVO userVO = getUserDetail();
        return userVO.getUserId();
    }

    public UserVO getUserVO() {
        return getUserDetail();
    }

    private UserVO getUserDetail() {
        UserVO userVO;
        String token = request.getHeader("token");
        if (token == null) {
            throw new UserInfoException(ApiCode.LOGIN_EXPIRED);
        }

        String userVOString = redisUtil.get(String.format(CommonRedisKey.TOKEN_USER_KEY, token));
        if (userVOString == null) {
            throw new UserInfoException(ApiCode.LOGIN_EXPIRED);
        }

        try {
            userVO = objectMapper.readValue(userVOString, UserVO.class);
        } catch (JsonProcessingException e) {
            throw new UserInfoException(ApiCode.LOGIN_EXPIRED);
        }
        return userVO;
    }
}
