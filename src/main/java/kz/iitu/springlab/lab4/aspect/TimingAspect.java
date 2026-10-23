package kz.iitu.springlab.lab4.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@Order(30)
public class TimingAspect {
    private static final Logger log = LoggerFactory.getLogger(TimingAspect.class);
    private static final long SLOW_LIMIT_MS = 200;

    @Around("kz.iitu.springlab.lab4.aspect.Pointcuts.serviceOperation()")
    public Object time(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.nanoTime();
        try {
            return pjp.proceed(); // Required: without proceed(), target never executes.
        } finally {
            long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            String method = pjp.getSignature().toShortString();
            if (elapsed >= SLOW_LIMIT_MS) {
                log.warn("LAB4 SLOW: {} took {} ms", method, elapsed);
            } else {
                log.info("LAB4 TIMING: {} took {} ms", method, elapsed);
            }
        }
    }
}