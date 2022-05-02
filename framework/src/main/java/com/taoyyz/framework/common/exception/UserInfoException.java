package com.taoyyz.framework.common.exception;

import com.taoyyz.framework.common.enums.ApiCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/6 9:39
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInfoException extends RuntimeException {
    private ApiCode apiCode;

    public UserInfoException(ApiCode apiCode) {
        this.apiCode = apiCode;
    }
}
