package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.Role;
import edu.tcu.projectpulse.domain.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends MongoRepository<UserAccount, Long> {
    List<UserAccount> findByRole(Role role);
    List<UserAccount> findByRoleOrderByLastNameAscFirstNameAsc(Role role);
    Optional<UserAccount> findByEmailIgnoreCase(String email);
}
