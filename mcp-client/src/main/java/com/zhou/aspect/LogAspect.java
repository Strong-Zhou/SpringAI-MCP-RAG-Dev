package com.zhou.aspect;

import groovyjarjarpicocli.CommandLine;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @Around("execution(* com.zhou.service.*.*(..))")
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();
        String name = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();

        long end = System.currentTimeMillis();
        if(end-start>2000){
            log.error("{} {} cost {} ms", name, methodName, end - start);
        }
        
        return proceed;
    }
}
