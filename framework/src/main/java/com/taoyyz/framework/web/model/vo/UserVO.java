package com.taoyyz.framework.web.model.vo;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 23:37
 */
@Data
@Accessors(chain = true)
@Builder
public class UserVO {
    private Long userId;
    private String number;
    private String username;
    private String gender;
    private String email;
    private LocalDate birthDay;
    private Integer userType;
    private String departmentName;
    private String majorName;
    private String clazzName;
    private LocalDateTime creatTime;
    private LocalDateTime lastLoginTime;
    private Integer loginCount;
    /**
     * 是否登录，true是，false否
     */
    private boolean loggedIn;
    /**
     * 用户登录成功产生的token
     */
    private String token;
}
