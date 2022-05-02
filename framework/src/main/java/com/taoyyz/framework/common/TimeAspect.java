package com.taoyyz.framework.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @Author taoyyz(陶俊杰)
 * @Date 2021/10/27 10:40
 * @Version 1.0
 */
@Aspect
@Slf4j
@Component
public class TimeAspect {

    /**
     * AOP计算方法执行时间
     *
     * @param joinPoint 要执行AOP的连接点
     */
    @Around("execution(public * com.taoyyz.framework.web.controller..* (..))")
    Object countTime(ProceedingJoinPoint joinPoint) throws Throwable {
        //获取时间
        long start = System.currentTimeMillis();
        boolean success = false;
        try {
            //执行方法
            Object proceed = joinPoint.proceed();
            log.debug(joinPoint.getSignature().getName() + " 执行结果：{ " + proceed + " }");
            success = true;
            return proceed;
        } finally {
            //计算时间
            if (success) {
                log.debug(joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName() + "耗时" + (System.currentTimeMillis() - start) + " ms");
            } else {
                log.warn(joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName() + "耗时" + (System.currentTimeMillis() - start) + " ms");
            }
        }
    }
}
