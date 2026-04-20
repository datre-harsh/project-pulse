package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    List<UserAccount> findByRole(Role role);
}
