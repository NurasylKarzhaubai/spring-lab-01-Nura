package kz.iitu.springlab.lab06.web;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import kz.iitu.springlab.lab06.service.DemoDataService;
import kz.iitu.springlab.lab06.service.IsolationService;
import kz.iitu.springlab.lab06.service.LabCheckedException;
import kz.iitu.springlab.lab06.service.OptimisticService;
import kz.iitu.springlab.lab06.service.PropagationService;
import kz.iitu.springlab.lab06.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lab06")
public class Lab06Controller {
    private final DemoDataService data;
    private final TransferService transfer;
    private final PropagationService propagation;
    private final IsolationService isolation;
    private final OptimisticService optimistic;

    public Lab06Controller(DemoDataService data, TransferService transfer,
            PropagationService propagation, IsolationService isolation, OptimisticService optimistic) {
        this.data = data;
        this.transfer = transfer;
        this.propagation = propagation;
        this.isolation = isolation;
        this.optimistic = optimistic;
    }

    @GetMapping("/accounts")
    public Object accounts() { return data.accountSnapshot(); }

    @GetMapping("/audits")
    public Object audits() { return data.audits(); }

    @GetMapping("/schema")
    public Object schema() {
        return Map.of("migrations", data.history(), "v3OwnerIndexApplied", data.ownerIndexPresent());
    }

    @PostMapping("/reset")
    public Object reset() {
        data.reset();
        return Map.of("message", "Lab 06 demonstration data reset", "accounts", data.accountSnapshot());
    }

    @PostMapping("/transfer")
    public Object transfer(@RequestParam(defaultValue = "1") Long from,
                           @RequestParam(defaultValue = "2") Long to,
                           @RequestParam(defaultValue = "100.00") BigDecimal amount,
                           @RequestParam(defaultValue = "none") String fail) {
        boolean failed = false;
        String exception = "none";
        try {
            switch (fail.toLowerCase(java.util.Locale.ROOT)) {
                case "none" -> transfer.transfer(from, to, amount, false);
                case "runtime" -> transfer.transfer(from, to, amount, true);
                case "checked" -> transfer.checkedWithoutRollback(from, to, amount);
                case "checkedrollback" -> transfer.checkedWithRollback(from, to, amount);
                default -> throw new IllegalArgumentException("Use none, runtime, checked, checkedRollback");
            }
        } catch (IllegalArgumentException | IllegalStateException | LabCheckedException ex) {
            failed = true;
            exception = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("scenario", fail);
        response.put("failed", failed);
        response.put("exception", exception);
        response.put("accounts", data.accountSnapshot());
        response.put("note", "checked commits by default; checkedRollback uses rollbackFor");
        return response;
    }

    @PostMapping("/propagation")
    public Object propagation(@RequestParam String mode) {
        int before = data.auditCount();
        String reason;
        try {
            if ("required".equalsIgnoreCase(mode)) {
                propagation.outerRequiredFailure();
            } else if ("requires_new".equalsIgnoreCase(mode)) {
                propagation.outerRequiresNewFailure();
            } else {
                throw new IllegalArgumentException("Use required or requires_new");
            }
            reason = "unexpected normal return";
        } catch (IllegalStateException ex) {
            reason = ex.getMessage();
        }
        return Map.of("mode", mode, "outerFailed", true,
                "auditCountBefore", before, "auditCountAfter", data.auditCount(),
                "auditRows", data.audits(), "reason", reason);
    }

    @PostMapping("/isolation")
    public Object isolation(@RequestParam String level) { return isolation.observe(level); }

    @PostMapping("/optimistic")
    public Object optimistic(@RequestParam(defaultValue = "1") Long id) {
        return optimistic.demonstrate(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
