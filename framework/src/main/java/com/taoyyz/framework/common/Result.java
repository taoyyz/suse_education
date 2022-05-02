package com.taoyyz.framework.common;

import com.taoyyz.framework.common.enums.ApiCode;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 公共返回类型
 *
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/3 18:25
 */
@Data
public class Result implements Serializable {
    private static final long serialVersionUID = 19990818L;
    private Integer code;
    private String message;
    private Object data;
    private HashMap<String, Object> map;

    public static Result success() {
        return result(200, null, null);
    }

    public static Result success(String message) {
        return result(200, message, null);
    }

    public static Result success(Object data) {
        return result(200, null, data);
    }

    public static Result success(String message, Object data) {
        return result(200, message, data);
    }

    public static Result error() {
        return result(500, "操作失败", null);
    }

    public static Result error(String errMsg) {
        return result(500, errMsg, null);
    }

    public static Result error(ApiCode apiCode) {
        return result(apiCode.getCode(), apiCode.getMessage(), null);
    }

    public static Result error(Integer code, String errMsg) {
        return result(code, errMsg, null);
    }

    public static Result result(Integer code, String message, Object data) {
        Result result = new Result();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public Result put(String key, Object value) {
        if (this.map == null) {
            this.map = new HashMap<>();
        }
        this.map.put(key, value);
        return this;
    }

    public Result setCode(Integer code) {
        this.code = code;
        return this;
    }

    public Result setMessage(String message) {
        this.message = message;
        return this;
    }

    public Result setData(Object data) {
        this.data = data;
        return this;
    }
}
