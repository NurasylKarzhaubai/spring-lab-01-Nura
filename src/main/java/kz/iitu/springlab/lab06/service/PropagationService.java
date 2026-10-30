package kz.iitu.springlab.lab06.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PropagationService {
    private final AuditService audit;
    public PropagationService(AuditService audit) { this.audit = audit; }

    @Transactional
    public void outerRequiredFailure() {
        audit.required("Outer transaction will roll back");
        throw new IllegalStateException("Outer REQUIRED demo failure");
    }

    @Transactional
    public void outerRequiresNewFailure() {
        audit.requiresNew("Inner transaction committed independently");
        throw new IllegalStateException("Outer REQUIRES_NEW demo failure");
    }
}
