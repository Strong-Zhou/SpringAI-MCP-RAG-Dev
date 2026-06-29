package com.zhou.aspect;

import cn.hutool.core.date.StopWatch;
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
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("任务1");

        Object proceed = joinPoint.proceed();
        String name = joinPoint.getTarget().getClass().getName();
        String methodName = joinPoint.getSignature().getName();

        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();

        System.out.println(stopWatch.shortSummary());
        System.out.println(stopWatch.prettyPrint());
        if(totalTimeMillis>2000){
            log.error("{} {} cost {} ms", name, methodName, totalTimeMillis);
        }
        
        return proceed;
    }
}
