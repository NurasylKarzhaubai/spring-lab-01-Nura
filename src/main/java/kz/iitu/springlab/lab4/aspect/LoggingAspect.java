package kz.iitu.springlab.lab4.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Order(20)
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
    private static final String POINTCUT =
            "kz.iitu.springlab.lab4.aspect.Pointcuts.serviceOperation()";

    @Before(POINTCUT)
    public void before(JoinPoint jp) {
        log.info("LAB4 BEFORE -> {} args={}", jp.getSignature().toShortString(),
                Arrays.toString(jp.getArgs()));
    }

    @AfterReturning(pointcut = POINTCUT, returning = "result")
    public void returned(JoinPoint jp, Object result) {
        log.info("LAB4 RETURN <- {} result={}", jp.getSignature().toShortString(), result);
    }

    @AfterThrowing(pointcut = POINTCUT, throwing = "ex")
    public void failed(JoinPoint jp, Throwable ex) {
        log.error("LAB4 ERROR !! {}: {}", jp.getSignature().toShortString(), ex.getMessage());
    }

    @After(POINTCUT)
    public void finallyAdvice(JoinPoint jp) {
        log.info("LAB4 AFTER (finally): {}", jp.getSignature().toShortString());
    }
}