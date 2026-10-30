package kz.iitu.springlab.lab06.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final JdbcTemplate jdbc;
    public AuditService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(propagation = Propagation.REQUIRED)
    public void required(String note) {
        jdbc.update("INSERT INTO tx_audit(mode, note) VALUES(?, ?)", "REQUIRED", note);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requiresNew(String note) {
        jdbc.update("INSERT INTO tx_audit(mode, note) VALUES(?, ?)", "REQUIRES_NEW", note);
    }
}
