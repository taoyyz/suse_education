package com.taoyyz.framework.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.web.model.entity.User;
import com.taoyyz.framework.web.model.request.UserLoginRequest;
import com.taoyyz.framework.web.model.request.UserUpdate;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 1:11
 */
public interface UserService extends IService<User> {

    Result login(UserLoginRequest userLoginRequest) throws JsonProcessingException;

    Result logout();

    Result updateUser(UserUpdate userUpdate) throws JsonProcessingException;
}
