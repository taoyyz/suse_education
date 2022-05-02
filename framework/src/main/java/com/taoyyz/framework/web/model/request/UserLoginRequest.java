package com.taoyyz.framework.web.model.request;

import com.taoyyz.framework.common.utils.RegexUtil;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 1:00
 */
@Data
public class UserLoginRequest {
    @NotNull(message = "学号/工号不能为空")
    @Pattern(regexp = RegexUtil.NUMBER_REGEX, message = RegexUtil.NUMBER_MSG)
    private String number;
    @NotNull(message = "密码不能为空")
    @Pattern(regexp = RegexUtil.PASSWORD_REGEX, message = RegexUtil.PASSWORD_MSG)
    private String password;
    @Range(min = 1, max = 3, message = "用户类型只能为学生、老师或管理员")
    private Integer userType;
}
