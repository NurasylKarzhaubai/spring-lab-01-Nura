package kz.iitu.springlab.lab06.service;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import kz.iitu.springlab.lab06.model.Account;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
public class OptimisticService {
    private final PlatformTransactionManager manager;
    private final EntityManager em;

    public OptimisticService(PlatformTransactionManager manager, EntityManager em) {
        this.manager = manager;
        this.em = em;
    }

    public Map<String, Object> demonstrate(Long accountId) {
        TransactionTemplate tx = new TransactionTemplate(manager);
        // Two different transactions produce two detached copies at the SAME version.
        Account firstCopy = tx.execute(s -> em.find(Account.class, accountId));
        Account staleCopy = tx.execute(s -> em.find(Account.class, accountId));
        if (firstCopy == null || staleCopy == null) {
            throw new IllegalArgumentException("Unknown account: " + accountId);
        }
        Long originalVersion = firstCopy.getVersion();
        tx.execute(s -> {
            Account live = em.merge(firstCopy);
            live.debit(new BigDecimal("10.00"));
            em.flush();
            return null;
        });
        String conflict = "none";
        boolean detected = false;
        try {
            tx.execute(s -> {
                Account live = em.merge(staleCopy);
                live.credit(new BigDecimal("50.00"));
                em.flush();
                return null;
            });
        } catch (RuntimeException ex) {
            detected = true;
            conflict = ex.getClass().getSimpleName();
        }
        Account current = tx.execute(s -> em.find(Account.class, accountId));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("firstVersion", originalVersion);
        result.put("staleVersion", staleCopy.getVersion());
        result.put("committedVersion", current.getVersion());
        result.put("finalBalance", current.getBalance());
        result.put("optimisticConflictDetected", detected);
        result.put("exception", conflict);
        result.put("staleUpdatePrevented", detected);
        return result;
    }
}
