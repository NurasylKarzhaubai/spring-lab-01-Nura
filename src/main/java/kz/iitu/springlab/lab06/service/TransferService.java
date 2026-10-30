package kz.iitu.springlab.lab06.service;

import java.math.BigDecimal;
import kz.iitu.springlab.lab06.model.Account;
import kz.iitu.springlab.lab06.repo.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final AccountRepository accounts;

    public TransferService(AccountRepository accounts) { this.accounts = accounts; }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount, boolean failAfterDebit) {
        Account[] pair = requireAccounts(fromId, toId);
        pair[0].debit(amount);
        accounts.saveAndFlush(pair[0]); // SQL is flushed, but NOT committed yet.
        if (failAfterDebit) {
            throw new IllegalStateException("Intentional unchecked exception after debit");
        }
        pair[1].credit(amount);
        accounts.saveAndFlush(pair[1]);
    }

    // Spring's default @Transactional rule does not roll back on CHECKED exceptions.
    @Transactional
    public void checkedWithoutRollback(Long fromId, Long toId, BigDecimal amount)
            throws LabCheckedException {
        Account[] pair = requireAccounts(fromId, toId);
        pair[0].debit(amount);
        accounts.saveAndFlush(pair[0]);
        throw new LabCheckedException("Checked failure: the debit will commit by default");
    }

    @Transactional(rollbackFor = LabCheckedException.class)
    public void checkedWithRollback(Long fromId, Long toId, BigDecimal amount)
            throws LabCheckedException {
        Account[] pair = requireAccounts(fromId, toId);
        pair[0].debit(amount);
        accounts.saveAndFlush(pair[0]);
        throw new LabCheckedException("Checked failure: rollbackFor requests rollback");
    }

    private Account[] requireAccounts(Long fromId, Long toId) {
        if (fromId == null || toId == null || fromId.equals(toId)) {
            throw new IllegalArgumentException("Source and destination must be distinct IDs");
        }
        return new Account[]{
                accounts.findById(fromId).orElseThrow(() ->
                        new IllegalArgumentException("Unknown source account: " + fromId)),
                accounts.findById(toId).orElseThrow(() ->
                        new IllegalArgumentException("Unknown destination account: " + toId))
        };
    }
}
