package com.taoyyz.framework.web.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.constant.CommonConstant;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.JwtTokenUtil;
import com.taoyyz.framework.common.utils.RedisUtil;
import com.taoyyz.framework.common.utils.UserInfoUtil;
import com.taoyyz.framework.web.controller.DepartmentController;
import com.taoyyz.framework.web.controller.MajorController;
import com.taoyyz.framework.web.mapper.UserMapper;
import com.taoyyz.framework.web.model.entity.User;
import com.taoyyz.framework.web.model.request.UserLoginRequest;
import com.taoyyz.framework.web.model.request.UserUpdate;
import com.taoyyz.framework.web.model.vo.UserVO;
import com.taoyyz.framework.web.service.UserService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 1:11
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private JwtTokenUtil tokenUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserInfoUtil userInfoUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private DepartmentController departmentController;
    @Autowired
    private MajorController majorController;

    @Override
    public Result login(UserLoginRequest userLoginRequest) throws JsonProcessingException {
        //查询用户
        User user = this.getOne(Wrappers.lambdaQuery(User.class)
                .eq(User::getNumber, userLoginRequest.getNumber())
                .eq(User::getUserType, userLoginRequest.getUserType()));
        if (user == null) {
            return Result.error(userLoginRequest.getUserType().equals(1) ? "学生不存在"
                    : userLoginRequest.getUserType().equals(2) ? "老师不存在"
                    : "管理员不存在");
        }
        //验证密码
        if (!encoder.matches(userLoginRequest.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        /*密码正确后，登录成功，产生token并更新用户登录信息*/

        //产生token
        String token = tokenUtil.generateToken(user.getUsername(), user.getUserId());
        //包装成VO对象
        UserVO userVO = UserVO.builder().build().setLoggedIn(true).setToken(token);
        BeanUtils.copyProperties(user, userVO);
        //获得学院、专业、班级名称
        userVO.setDepartmentName(departmentController.getIdToDepartment().get(user.getDepartmentId()).getDepartmentName());
        userVO.setMajorName(majorController.getIdToMajor().get(user.getMajorId()).getMajorName());
//        userVO.setClazzName()
        String userString;
        try {
            userString = objectMapper.writeValueAsString(userVO);
        } catch (JsonProcessingException e) {
            return Result.error("序列化用户信息失败");
        }
        //把用户id和token的关联存入redis
        redisUtil.setEx(String.format(CommonRedisKey.USER_TOKEN_KEY, user.getUserId()),
                token, CommonConstant.USER_EXPIRE, TimeUnit.SECONDS);
        //把token和用户信息的关联存入redis
        redisUtil.setEx(String.format(CommonRedisKey.TOKEN_USER_KEY, token),
                userString, CommonConstant.USER_EXPIRE, TimeUnit.SECONDS);
        //把用户id和用户权限的关联存入redis
        redisUtil.setEx(String.format(CommonRedisKey.USER_AUTH_KEY, user.getUserId()), user.getUserType().toString(), CommonConstant.USER_EXPIRE, TimeUnit.SECONDS);
        //更新用户登录信息
        User updateUser = new User().setUserId(user.getUserId())
                .setLastLoginTime(LocalDateTime.now())
                .setLoginCount(user.getLoginCount() + 1);
        this.updateById(updateUser);
        return Result.success(userVO);
    }

    @Override
    public Result logout() {
        //使得当前token失效
        UserVO userVO = userInfoUtil.getUserVO();
        redisUtil.delete(String.format(CommonRedisKey.TOKEN_USER_KEY, userVO.getToken()));
        return Result.success("注销成功");
    }

    @Override
    public Result updateUser(UserUpdate userUpdate) throws JsonProcessingException {
        User user = new User();
        BeanUtils.copyProperties(userUpdate, user);
        if (userUpdate.getPassword() != null) {
            user.setPassword(encoder.encode(userUpdate.getPassword()));
        }
        boolean updated = this.updateById(user);
        if (updated) {
            //更新redis中的用户信息
            UserVO userVO = userInfoUtil.getUserVO();
            userVO.setEmail(userUpdate.getEmail())
                    .setBirthDay(userUpdate.getBirthDay())
                    .setGender(userUpdate.getGender());
            String userVOString = objectMapper.writeValueAsString(userVO);
            redisUtil.setEx(String.format(CommonRedisKey.TOKEN_USER_KEY, userVO.getToken()), userVOString, 7200, TimeUnit.SECONDS);
            return Result.success();
        } else {
            return Result.error("更新信息失败");
        }
    }
}

