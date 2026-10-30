package kz.iitu.springlab.lab06;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import kz.iitu.springlab.lab06.service.DemoDataService;
import kz.iitu.springlab.lab06.service.IsolationService;
import kz.iitu.springlab.lab06.service.LabCheckedException;
import kz.iitu.springlab.lab06.service.OptimisticService;
import kz.iitu.springlab.lab06.service.PropagationService;
import kz.iitu.springlab.lab06.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:lab06_test;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=validate"
})
@ActiveProfiles("lab06")
class Lab06TransactionTests {
    @Autowired DemoDataService data;
    @Autowired TransferService transfers;
    @Autowired PropagationService propagation;
    @Autowired OptimisticService optimistic;
    @Autowired IsolationService isolation;

    @BeforeEach
    void reset() { data.reset(); }

    private BigDecimal balance(int index) {
        return (BigDecimal) data.accountSnapshot().get(index).get("balance");
    }

    @Test
    void transferIsAtomic() {
        transfers.transfer(1L, 2L, new BigDecimal("100.00"), false);
        assertEquals(0, balance(0).compareTo(new BigDecimal("900.00")));
        assertEquals(0, balance(1).compareTo(new BigDecimal("600.00")));
    }

    @Test
    void overdraftDoesNotChangeBalances() {
        assertThrows(IllegalStateException.class, () ->
                transfers.transfer(1L, 2L, new BigDecimal("9999.00"), false));
        assertEquals(0, balance(0).compareTo(new BigDecimal("1000.00")));
        assertEquals(0, balance(1).compareTo(new BigDecimal("500.00")));
    }

    @Test
    void uncheckedExceptionRollsBackFlushedDebit() {
        assertThrows(IllegalStateException.class, () ->
                transfers.transfer(1L, 2L, new BigDecimal("100.00"), true));
        assertEquals(0, balance(0).compareTo(new BigDecimal("1000.00")));
        assertEquals(0, balance(1).compareTo(new BigDecimal("500.00")));
    }

    @Test
    void checkedExceptionCommitsByDefault() {
        assertThrows(LabCheckedException.class, () ->
                transfers.checkedWithoutRollback(1L, 2L, new BigDecimal("100.00")));
        assertEquals(0, balance(0).compareTo(new BigDecimal("900.00")));
        assertEquals(0, balance(1).compareTo(new BigDecimal("500.00")));
    }

    @Test
    void rollbackForAlsoRollsBackCheckedException() {
        assertThrows(LabCheckedException.class, () ->
                transfers.checkedWithRollback(1L, 2L, new BigDecimal("100.00")));
        assertEquals(0, balance(0).compareTo(new BigDecimal("1000.00")));
    }

    @Test
    void requiredAuditRolledBackWithOuter() {
        assertThrows(IllegalStateException.class, propagation::outerRequiredFailure);
        assertEquals(0, data.auditCount());
    }

    @Test
    void requiresNewAuditSurvivesOuterFailure() {
        assertThrows(IllegalStateException.class, propagation::outerRequiresNewFailure);
        assertEquals(1, data.auditCount());
    }

    @Test
    void optimisticLockStopsStaleUpdate() {
        assertEquals(true, optimistic.demonstrate(1L).get("optimisticConflictDetected"));
        assertEquals(0, balance(0).compareTo(new BigDecimal("990.00")));
    }

    @Test
    void flywayAppliedBaselineMigrations() {
        assertTrue(data.history().size() >= 2);
    }

    @Test
    void isolationReadsCanBeComparedOnH2() {
        var readCommitted = isolation.observe("READ_COMMITTED");
        var repeatable = isolation.observe("REPEATABLE_READ");
        assertEquals("H2", readCommitted.get("database"));
        assertEquals("H2", repeatable.get("database"));
        assertNotNull(readCommitted.get("secondRead"));
        assertNotNull(repeatable.get("secondRead"));
    }
}
