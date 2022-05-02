package com.taoyyz.framework.common.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 针对方法的权限控制，拦截器会检查此注解所在的方法的访问权限<br>
 * <pre>
 *     规则：
 *       1. 如果此注解的所有值都是默认值 -1，通过检查
 *       2. 如果指定了{@link #minAuthRequire()}，并且实际权限大于此{@link #minAuthRequire()}，继续检查以下：
 *          2.1 如果没有指定{@link #value()}和{@link #allowedRoleId()}，通过检查
 *          2.2 如果指定了{@link #value()}和{@link #allowedRoleId()}，检查实际权限是否包含在注解允许的权限内，一旦成功匹配其中一个，通过检查
 *       否则权限不匹配
 * </pre>
 *
 * @author taoyyz(陶俊杰)
 * @version 1.0
 * @see com.taoyyz.framework.web.interceptor.AuthInterceptor
 * @since 2022/4/4 00:39
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Auth {
    /**
     * 允许通过授权的角色id，只有角色id符合此{@link #allowedRoleId()}且符合{@link #minAuthRequire()}时视为通过权限验证
     */
    @AliasFor("allowedRoleId")
    int[] value() default -1;

    @AliasFor("value")
    int[] allowedRoleId() default -1;

    /**
     * 最低权限要求，只有大于等于这个值，且符合{@link #allowedRoleId()}时视为通过权限验证
     */
    int minAuthRequire() default -1;
}
