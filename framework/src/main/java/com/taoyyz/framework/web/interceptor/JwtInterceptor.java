package com.taoyyz.framework.web.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taoyyz.framework.common.Result;
import com.taoyyz.framework.common.constant.CommonConstant;
import com.taoyyz.framework.common.constant.CommonRedisKey;
import com.taoyyz.framework.common.enums.ApiCode;
import com.taoyyz.framework.common.utils.JwtTokenUtil;
import com.taoyyz.framework.common.utils.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 *
 **/
@Slf4j
@Component
@ConditionalOnProperty(value = {"spring-boot-plus.interceptor.jwt.enable"}, matchIfMissing = true)
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private JwtTokenUtil tokenUtils;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 拦截器
     *
     * @param request  请求对象
     * @param response 响应对象
     * @param handler  处理器
     * @return 如果符合要求，返回true继续执行后续流程。否则拦截请求并提示错误信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        Result result = Result.error(ApiCode.UN_LOGIN);
        try {
            //从header请求头获取传递的token
            String currentToken = request.getHeader("token");
            //验证token是否有效并获取用户id
            String userId = tokenUtils.verify(currentToken).getId();
            //验证redis中此token是否有效
            String tokenUserKey = String.format(CommonRedisKey.TOKEN_USER_KEY, currentToken);
            String user = redisUtil.get(tokenUserKey);
            if (user != null) {
                String userTokenKey = String.format(CommonRedisKey.USER_TOKEN_KEY, userId);
                //获取redis中此用户id的最新且正确的token
                String correctToken = redisUtil.get(userTokenKey);
                //如果最新token不为空
                if (Objects.nonNull(correctToken)) {
                    //如果最新token和请求头的token一致
                    if (correctToken.equals(currentToken)) {
                        //延长redis中此token的有效时间
                        redisUtil.expire(tokenUserKey, CommonConstant.USER_EXPIRE, TimeUnit.SECONDS);
                        redisUtil.expire(userTokenKey, CommonConstant.USER_EXPIRE, TimeUnit.SECONDS);
                        redisUtil.expire(String.format(CommonRedisKey.USER_AUTH_KEY, userId), CommonConstant.USER_EXPIRE, TimeUnit.SECONDS);
                        return true;
                    } else {
                        //否则最新token已经不是请求头token，说明在其他地方登录
                        result.setMessage("账号在其他地方登录");
                    }
                } else {
                    //否则现在的token已经失效
                    result.setMessage("登录已过期");
                }
            } else {
                result.setCode(ApiCode.LOGIN_EXPIRED.getCode()).setMessage(ApiCode.LOGIN_EXPIRED.getMessage());
            }
            //token不存在于redis或最新的userId对应的token已经不是此token，登录已过期
            String json = objectMapper.writeValueAsString(result);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().println(json);
            //拦截请求
            return false;
        } catch (SignatureException e) {
            log.error(request.getRequestURI() + " ：" + e.getMessage());
            result.setMessage("无效签名");
        } catch (ExpiredJwtException e) {
            log.error(request.getRequestURI() + " ：" + e.getMessage());
            result.setMessage("登录已过期");
        } catch (Exception e) {
            log.error(request.getRequestURI() + " ：" + e.getMessage());
            result.setMessage("认证失败,请重新登录");
        }
        String json = objectMapper.writeValueAsString(result);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().println(json);
        //拦截请求
        return false;
    }
}
