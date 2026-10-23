package kz.iitu.springlab.lab4.audit;

import java.time.Instant;

public record AuditEvent(Instant timestamp, String action, String method,
                         boolean success, String details) { }