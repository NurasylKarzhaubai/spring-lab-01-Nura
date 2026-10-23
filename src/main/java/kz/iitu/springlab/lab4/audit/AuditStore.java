package kz.iitu.springlab.lab4.audit;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class AuditStore {
    private static final int MAX_EVENTS = 100;
    private final ConcurrentLinkedDeque<AuditEvent> events = new ConcurrentLinkedDeque<>();

    public void add(AuditEvent event) {
        events.addFirst(event);
        while (events.size() > MAX_EVENTS) {
            events.pollLast();
        }
    }

    public int size() {
        return events.size();
    }

    public List<AuditEvent> snapshot() {
        return List.copyOf(events);
    }
}