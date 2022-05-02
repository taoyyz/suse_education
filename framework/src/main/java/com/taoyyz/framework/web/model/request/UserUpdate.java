package com.taoyyz.framework.web.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.taoyyz.framework.common.utils.RegexUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

/**
 * 更新用户信息请求对象
 *
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/5 16:16
 */
@Data
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserUpdate {
    private Long userId;
    @Email(message = "邮箱格式有误")
    private String email;
    @Past(message = "生日至少是过去的日期")
    private LocalDate birthDay;
    @Pattern(regexp = "^[男女]$", message = "性别只能为男或女")
    private String gender;
    @Pattern(regexp = RegexUtil.PASSWORD_REGEX, message = RegexUtil.PASSWORD_MSG)
    private String password;
}
