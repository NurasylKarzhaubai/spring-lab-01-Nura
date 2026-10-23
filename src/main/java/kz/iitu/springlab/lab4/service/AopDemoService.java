package kz.iitu.springlab.lab4.service;

import kz.iitu.springlab.lab4.audit.Audited;
import org.springframework.stereotype.Service;

@Service
public class AopDemoService {
    public String hello(String name) {
        return "Hello, " + name + "! Spring AOP is running.";
    }

    public String slowOperation(int requestedMillis) {
        int delay = Math.max(0, Math.min(requestedMillis, 2000));
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Demo operation was interrupted", ex);
        }
        return "Completed operation in about " + delay + " ms";
    }

    @Audited(action = "LAB4_AUDITED_OPERATION", logArguments = true)
    public String auditedOperation(String value) {
        return "Audited result: " + value;
    }

    public String selfInvocation(String value) {
        // Deliberately bypasses the Spring proxy. AuditAspect is NOT invoked here.
        return auditedOperation(value);
    }

    public String failure() {
        throw new IllegalStateException("Intentional exception for @AfterThrowing demonstration");
    }
}