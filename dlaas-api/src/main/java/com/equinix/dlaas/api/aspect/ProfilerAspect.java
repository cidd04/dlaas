package com.equinix.dlaas.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Created by ransay on 2/1/2017.
 */

@Component
@Aspect
public class ProfilerAspect {

    private static final Logger log = LoggerFactory.getLogger(ProfilerAspect.class);

    @Pointcut("execution(* com.equinix.csg.event.receiver.api.*.*(..))")
    public void apiMethods() { }

    @Around("apiMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("Going to call the method.");
        Object output = pjp.proceed();
        long elapsedTime = System.currentTimeMillis() - start;
        log.info("Method execution completed. Time: " + elapsedTime + " milliseconds.");
        return output;
    }

}
