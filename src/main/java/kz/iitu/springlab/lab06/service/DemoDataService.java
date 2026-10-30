package kz.iitu.springlab.lab06.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import kz.iitu.springlab.lab06.model.Account;
import kz.iitu.springlab.lab06.repo.AccountRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoDataService {
    private final JdbcTemplate jdbc;
    private final AccountRepository accounts;

    public DemoDataService(JdbcTemplate jdbc, AccountRepository accounts) {
        this.jdbc = jdbc;
        this.accounts = accounts;
    }

    @Transactional
    public void reset() {
        jdbc.update("UPDATE accounts SET balance=1000.00, version=0 WHERE id=1");
        jdbc.update("UPDATE accounts SET balance=500.00, version=0 WHERE id=2");
        jdbc.update("DELETE FROM tx_audit");
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> accountSnapshot() {
        return accounts.findAll().stream()
                .sorted(java.util.Comparator.comparing(Account::getId))
                .map(a -> Map.<String, Object>of(
                        "id", a.getId(), "ownerName", a.getOwnerName(),
                        "balance", a.getBalance(), "version", a.getVersion(),
                        "status", a.getStatus()))
                .toList();
    }

    public List<Map<String, Object>> audits() {
        return jdbc.queryForList("SELECT id, mode, note FROM tx_audit ORDER BY id");
    }

    public int auditCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM tx_audit", Integer.class);
    }

    public List<Map<String, Object>> history() {
        return jdbc.queryForList(
                "SELECT \"installed_rank\", \"version\", \"description\", \"success\" FROM \"flyway_schema_history\" ORDER BY \"installed_rank\"");
    }

    public boolean ownerIndexPresent() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME='ACCOUNTS' AND INDEX_NAME='IDX_ACCOUNTS_OWNER'",
                Integer.class);
        return count != null && count > 0;
    }
}
