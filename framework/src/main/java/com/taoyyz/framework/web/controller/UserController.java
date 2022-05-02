package com.taoyyz.framework.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.enums.ApiCode;
import com.taoyyz.framework.common.utils.UserInfoUtil;
import com.taoyyz.framework.web.model.request.UserLoginRequest;
import com.taoyyz.framework.web.model.request.UserUpdate;
import com.taoyyz.framework.web.model.vo.UserVO;
import com.taoyyz.framework.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/3 19:11
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserInfoUtil userInfoUtil;
    @Autowired
    private UserService userService;

    @PostMapping("login")
    public Result LoginUser(@RequestBody @Validated UserLoginRequest userLoginRequest) throws JsonProcessingException {
        return userService.login(userLoginRequest);
    }

    @PutMapping("logout")
    public Result logoutUser() {
        return userService.logout();
    }

    @GetMapping("info")
    public Result getUserInfo() {
        UserVO userVO = userInfoUtil.getUserVO().setToken(null);
        return Result.success(userVO);
    }

    @PutMapping("update")
    public Result updateUser(@RequestBody @Validated UserUpdate userUpdate) throws JsonProcessingException {
        return userService.updateUser(userUpdate);
    }

    @GetMapping("loginStatus")
    public Result getLoginStatus() {
        UserVO userVO = userInfoUtil.getUserVO();
        return userVO.isLoggedIn() ? Result.success(userVO) : Result.error(ApiCode.UN_LOGIN);
    }
}
