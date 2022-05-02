package com.taoyyz.framework.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.annotation.Auth;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.utils.JwtTokenUtil;
import com.taoyyz.framework.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 权限拦截器，会检查header中的token对应的当前用户的权限是否满足标记了{@link Auth}的方法上指定的权限
 *
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @since 2022/4/4 00:50
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final Result NO_PERMISSION = Result.error("权限不足");

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtTokenUtil tokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            String token = request.getHeader("token");
            //检查权限，如果权限不足，返回错误
            if (!checkAuth(token, handler)) {
                String json = objectMapper.writeValueAsString(NO_PERMISSION);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().println(json);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查此token的权限是否满足方法注解上{@link com.taoyyz.framework.common.annotation.Auth}的权限
     */
    private boolean checkAuth(String token, Object handler) {
        //注意AliasFor注解是Spring注解，要用AnnotationUtils才能取到别名的值
        Auth auth = AnnotationUtils.getAnnotation(((HandlerMethod) handler).getMethod(), Auth.class);
        if (auth == null) {
            //没有权限控制要求
            return true;
        } else {
            //获取当前token的权限
            String authString = redisUtil.get(String.format(CommonRedisKey.USER_AUTH_KEY, tokenUtil.getUserIdFromToken(token)));
            if (authString == null) {
                return false;
            }
            int currentAuth = Integer.parseInt(authString);
            boolean allowed = false;
            //先检查注解的最低权限要求
            int minAuthRequire = auth.minAuthRequire();
            //如果最小权限要求 <= -1就是没有最低权限要求，或当前权限大于最小权限要求，上述两种情况都检查指定权限和当前权限是否有匹配。
            if (minAuthRequire <= -1 || currentAuth >= minAuthRequire) {
                allowed = checkProperties(auth, currentAuth);
            }
            return allowed;
        }
    }

    private boolean checkProperties(Auth auth, int currentAuth) {
        boolean allowed = false;
        int[] values = auth.value();
        //如果允许的权限值为-1就是默认没有权限要求
        if (values.length == 1 && values[0] == -1) {
            allowed = true;
        } else {
            //否则有权限要求，检查权限要求是否满足
            for (int value : values) {
                if (value == currentAuth) {
                    allowed = true;
                    break;
                }
            }
        }
        return allowed;
    }
}
