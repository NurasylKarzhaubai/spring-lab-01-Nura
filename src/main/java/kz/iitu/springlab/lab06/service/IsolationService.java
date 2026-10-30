package kz.iitu.springlab.lab06.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class IsolationService {
    private final PlatformTransactionManager manager;
    private final JdbcTemplate jdbc;

    public IsolationService(PlatformTransactionManager manager, JdbcTemplate jdbc) {
        this.manager = manager;
        this.jdbc = jdbc;
    }

    public Map<String, Object> observe(String isolationName) {
        int level = switch (isolationName.toUpperCase(java.util.Locale.ROOT)) {
            case "READ_COMMITTED" -> TransactionDefinition.ISOLATION_READ_COMMITTED;
            case "REPEATABLE_READ" -> TransactionDefinition.ISOLATION_REPEATABLE_READ;
            default -> throw new IllegalArgumentException("Use READ_COMMITTED or REPEATABLE_READ");
        };
        jdbc.update("UPDATE accounts SET balance=500.00, version=0 WHERE id=2");
        TransactionTemplate reader = new TransactionTemplate(manager);
        reader.setIsolationLevel(level);
        TransactionTemplate writer = new TransactionTemplate(manager);
        writer.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        ExecutorService worker = Executors.newSingleThreadExecutor();
        try {
            return reader.execute(status -> {
                BigDecimal first = readProbe();
                Future<?> update = worker.submit(() -> writer.execute(ws -> {
                    jdbc.update("UPDATE accounts SET balance=510.00, version=version+1 WHERE id=2");
                    return null;
                }));
                try { update.get(8, TimeUnit.SECONDS); }
                catch (Exception ex) { throw new IllegalStateException("Concurrent writer failed", ex); }
                BigDecimal second = readProbe();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("database", "H2");
                result.put("isolation", isolationName.toUpperCase(java.util.Locale.ROOT));
                result.put("firstRead", first);
                result.put("secondRead", second);
                result.put("changedSecondRead", first.compareTo(second) != 0);
                result.put("independentCommittedWriter", true);
                return result;
            });
        } finally {
            worker.shutdownNow();
        }
    }

    private BigDecimal readProbe() {
        return jdbc.queryForObject("SELECT balance FROM accounts WHERE id=2", BigDecimal.class);
    }
}
