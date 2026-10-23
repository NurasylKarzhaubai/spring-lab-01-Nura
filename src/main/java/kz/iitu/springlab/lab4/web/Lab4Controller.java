package kz.iitu.springlab.lab4.web;

import kz.iitu.springlab.lab4.audit.AuditEvent;
import kz.iitu.springlab.lab4.audit.AuditStore;
import kz.iitu.springlab.lab4.service.AopDemoService;
import org.springframework.aop.support.AopUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lab4")
public class Lab4Controller {
    private final AopDemoService service;
    private final AuditStore auditStore;

    public Lab4Controller(AopDemoService service, AuditStore auditStore) {
        this.service = service;
        this.auditStore = auditStore;
    }

    @GetMapping("/hello")
    public Map<String, Object> hello(@RequestParam(defaultValue = "IITU") String name) {
        return Map.of("result", service.hello(name), "advice", "See LAB4 BEFORE / RETURN / AFTER / TIMING in IntelliJ");
    }

    @GetMapping("/slow")
    public Map<String, Object> slow(@RequestParam(defaultValue = "250") int ms) {
        return Map.of("result", service.slowOperation(ms), "slowThresholdMs", 200);
    }

    @GetMapping("/audit")
    public Map<String, Object> audit(@RequestParam(defaultValue = "IITU") String text) {
        String result = service.auditedOperation(text); // External call goes THROUGH proxy.
        return Map.of("result", result, "auditEvents", auditStore.size());
    }

    @GetMapping("/self")
    public Map<String, Object> self(@RequestParam(defaultValue = "IITU") String text) {
        int before = auditStore.size();
        String result = service.selfInvocation(text); // Inside service: this.auditedOperation(), NO audit.
        int after = auditStore.size();
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("result", result);
        output.put("auditEventsBefore", before);
        output.put("auditEventsAfter", after);
        output.put("newAuditEvents", after - before);
        output.put("reason", "self-invocation bypasses the Spring AOP proxy");
        return output;
    }

    @GetMapping("/events")
    public List<AuditEvent> events() {
        return auditStore.snapshot();
    }

    @GetMapping("/proxy")
    public Map<String, Object> proxy() {
        return Map.of(
                "proxyClass", service.getClass().getName(),
                "isAopProxy", AopUtils.isAopProxy(service),
                "isCglibProxy", AopUtils.isCglibProxy(service),
                "targetClass", AopUtils.getTargetClass(service).getName());
    }

    @GetMapping("/fail")
    public Map<String, Object> fail() {
        return Map.of("result", service.failure());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleFailure(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage(), "advice", "Check LAB4 ERROR in IntelliJ"));
    }
}