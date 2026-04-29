package edu.tcu.projectpulse.repo;

import edu.tcu.projectpulse.domain.StudentInvitation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface StudentInvitationRepository extends MongoRepository<StudentInvitation, Long> {
    List<StudentInvitation> findBySectionIdAndAcceptedFalseOrderBySentAtDesc(Long sectionId);
    Optional<StudentInvitation> findByToken(String token);
}
