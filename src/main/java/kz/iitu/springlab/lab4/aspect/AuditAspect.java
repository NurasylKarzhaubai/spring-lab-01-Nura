package kz.iitu.springlab.lab4.aspect;

import kz.iitu.springlab.lab4.audit.AuditEvent;
import kz.iitu.springlab.lab4.audit.AuditStore;
import kz.iitu.springlab.lab4.audit.Audited;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

@Aspect
@Component
@Order(10)
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);
    private final AuditStore auditStore;

    public AuditAspect(AuditStore auditStore) {
        this.auditStore = auditStore;
    }

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint pjp, Audited audited) throws Throwable {
        String method = pjp.getSignature().toShortString();
        String arguments = audited.logArguments()
                ? Arrays.toString(pjp.getArgs()) : "arguments not logged";
        try {
            Object result = pjp.proceed();
            auditStore.add(new AuditEvent(Instant.now(), audited.action(), method, true, arguments));
            log.info("LAB4 AUDIT SUCCESS action={} method={}", audited.action(), method);
            return result;
        } catch (Throwable ex) {
            auditStore.add(new AuditEvent(Instant.now(), audited.action(), method, false,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage()));
            log.warn("LAB4 AUDIT FAILURE action={} method={}", audited.action(), method);
            throw ex; // The original exception is never suppressed.
        }
    }
}