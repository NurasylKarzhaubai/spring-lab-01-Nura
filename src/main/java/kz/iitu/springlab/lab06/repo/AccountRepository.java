package kz.iitu.springlab.lab06.repo;

import kz.iitu.springlab.lab06.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> { }
